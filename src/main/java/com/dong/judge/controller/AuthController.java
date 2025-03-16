package com.dong.judge.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dong.judge.model.dto.user.*;
import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.EmailService;
import com.dong.judge.service.FileStorageService;
import com.dong.judge.service.RoleService;
import com.dong.judge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户权限
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private RoleService roleService;


    /**
     * 用户注册
     *
     * @param register 用户信息
     * @return 结果
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid Register register) {

        try {
            // 1. 验证验证码
            String cacheCode = redisTemplate.opsForValue().get("verification:" + register.getVerificationId());
            if (cacheCode == null) {
                return Result.error(400, "验证码已过期");
            }

            if (!cacheCode.equals(register.getVerificationCode())) {
                return Result.error(400, "验证码错误");
            }

            // 2. 检查邮箱是否已被注册
            if (userService.isEmailExist(register.getEmail())) {
                return Result.error(400, "邮箱已被注册");
            }

            User user = new User(register.getEmail());

            // 3. 密码加密
            String hashedPassword = BCrypt.hashpw(register.getPassword(), BCrypt.gensalt(6));
            user.setPassword(hashedPassword);

            // 4. 设置默认值
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setBan(false);

            // 如果没有提供昵称，使用邮箱前缀作为默认昵称
            if (user.getNickname() == null || user.getNickname().isEmpty()) {
                String emailPrefix = user.getEmail().split("@")[0];
                user.setUsername(emailPrefix);
                user.setNickname(register.getEmail());
            }

            user.setAvatar("https://picx.zhimg.com/80/v2-8859acdb8fc8ae5c862624b842970ae9_720w.webp?source=1def8aca");
            user.setBio("暂无！");

            // 5. 保存用户
            boolean saved = userService.save(user);
            if (!saved) {
                return Result.error("注册失败，请重试");
            }
            
            // 6. 设置默认用户角色
            long count = userService.count();
            //第一个创建的用户设置为超级管理员。
            if (count==0) {
                roleService.setUserRoles(user, RoleEnum.SUPER_ADMIN);
            }else{
                roleService.setUserRoles(user, RoleEnum.USER);
            }

            // 7. 删除验证码
            redisTemplate.delete("verification:" + register.getVerificationId());

            // 8. 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("email", user.getEmail());

            return Result.success("注册成功", data);

        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }


    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // 检查参数是否为空
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                return Result.error(400, "邮箱和密码不能为空");
            }

            // 根据邮箱查找用户
            User user = userService.getByEmail(email);
            if (user == null) {
                return Result.error(400, "用户不存在");
            }

            // 验证密码
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return Result.error(400, "密码错误");
            }

            // 检查账户状态
            if (user.getBan() != null && user.getBan()) {
                return Result.error(403, "账号已被禁用");
            }

            // 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateById(user);

            // 第1步，先登录上
            StpUtil.login(loginRequest.getEmail());
            // 第2步，获取 Token  相关参数
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            // 第3步，返回给前端
            return Result.success("登录成功", tokenInfo);

        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注销登录
     * <p>
     * 该接口用于用户退出登录状态，清除用户的登录凭证。
     * 成功注销后，用户需要重新登录才能访问需要身份验证的资源。
     * </p>
     *
     * @return 注销结果，包含操作状态信息
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "用户退出登录状态，清除登录凭证")
    public Result<?> logout() {
        String tokenValue = StpUtil.getTokenValue();
        StpUtil.logoutByTokenValue(tokenValue);
        return Result.success("注销成功");
    }

    /**
     * # 修改用户密码
     * <p>
     * 允许已登录用户修改自己的账户密码，需要提供旧密码进行身份验证。
     * <p>
     * - 验证用户登录状态
     * - 验证旧密码是否正确
     * - 确保新密码和确认密码一致
     * - 使用BCrypt进行密码加密存储
     */
    @PostMapping("/changePassword")
    @Operation(summary = "修改密码", description = "用户修改自己的登录密码")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "密码修改成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前用户
            String email = (String) StpUtil.getLoginId();
            User user = userService.getByEmail(email);

            if (user == null) {
                return Result.error(400, "用户不存在");
            }

            // 验证验证码
            String cacheCode = redisTemplate.opsForValue().get("password_reset:" + request.getVerificationId());
            if (cacheCode == null) {
                return Result.error(400, "验证码已过期");
            }

            if (!cacheCode.equals(request.getVerificationCode())) {
                return Result.error(400, "验证码错误");
            }


            // 密码验证
            if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
                return Result.error(400, "旧密码错误");
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return Result.error(400, "新密码与确认密码不一致");
            }

            // 更新密码
            String hashedPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt(6));
            user.setPassword(hashedPassword);
            user.setUpdatedAt(LocalDateTime.now());

            if (!userService.updateById(user)) {
                return Result.error("密码修改失败，请重试");
            }

            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error("密码修改失败: " + e.getMessage());
        }
    }


}