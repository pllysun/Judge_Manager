package com.dong.judge.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dong.judge.model.dto.user.LoginRequest;
import com.dong.judge.model.dto.user.Register;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.EmailService;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送验证码
     *
     * @param email 邮箱地址
     * @return 结果
     */
    @PostMapping("/sendVerificationCode")
    public Result<?> sendVerificationCode(@RequestParam String email) {
        try {
            // 检查邮箱是否已注册
            if (userService.isEmailExist(email)) {
                return Result.error(400, "邮箱已被注册");
            }

            // 生成6位数验证码
            String code = String.format("%04d", (int) (Math.random() * 1000000));

            // 生成验证码ID
            String verificationId = UUID.randomUUID().toString();

            // 保存验证码到Redis，10分钟有效
            redisTemplate.opsForValue().set("verification:" + verificationId, code, 10, TimeUnit.MINUTES);

            // 发送验证码邮件
            boolean sent = emailService.sendVerificationEmail(email, code);
            if (!sent) {
                return Result.error("邮件发送失败，请检查邮箱地址");
            }

            // 返回验证码ID
            Map<String, String> data = new HashMap<>();
            data.put("verificationId", verificationId);

            return Result.success("验证码已发送，请查收邮件", data);

        } catch (Exception e) {
            return Result.error("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     *
     * @param register             用户信息
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
                user.setNickname(emailPrefix);
            }

            // 5. 保存用户
            boolean saved = userService.save(user);
            if (!saved) {
                return Result.error("注册失败，请重试");
            }

            // 6. 删除验证码
            redisTemplate.delete("verification:" + register.getVerificationId());

            // 7. 返回结果
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
}