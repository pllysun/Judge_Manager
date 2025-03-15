package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.user.UpdateUserInfoRequest;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.FileStorageService;
import com.dong.judge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;


    /**
     * # 修改用户个人信息
     * <p>
     * 允许已登录用户更新个人资料，包括：
     * <p>
     * - 用户名
     * - 昵称
     * - 个人简介
     * - 头像（支持文件上传或URL设置）
     * <p>
     * 所有字段均为可选，仅更新提供的内容。
     */
    @PostMapping(value = "/updateUserInfo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "修改用户信息", description = "用户修改自己的个人信息，包括头像上传")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "用户信息修改成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> updateUserInfo(@ModelAttribute @Valid UpdateUserInfoRequest request) {
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

            // 更新提供的字段
            updateUserFields(user, request);

            // 持久化更新
            if (!userService.updateById(user)) {
                return Result.error("用户信息修改失败，请重试");
            }

            // 返回更新后的信息
            return Result.success("用户信息修改成功", buildUserInfoMap(user));
        } catch (IOException e) {
            return Result.error("头像上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("用户信息修改失败: " + e.getMessage());
        }
    }

    /**
     * # 获取当前用户信息
     * <p>
     * 返回当前登录用户的个人资料，不包含敏感信息如密码。
     * <p>
     * 返回字段包括：
     * - 用户ID
     * - 邮箱
     * - 用户名
     * - 昵称
     * - 头像URL
     * - 个人简介
     * - 账户创建时间
     * - 最后登录时间
     */
    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取用户信息"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> getUserInfo() {
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

            // 构建返回数据
            Map<String, Object> data = buildUserInfoMap(user);
            data.put("createdAt", user.getCreatedAt());
            data.put("lastLoginAt", user.getLastLoginAt());

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户字段
     *
     * @param user 用户实体
     * @param request 请求包含要更新的字段
     * @throws IOException 如果文件上传失败
     */
    private void updateUserFields(User user, UpdateUserInfoRequest request) throws IOException {
        // 更新基本信息
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user.setUsername(request.getUsername());
        }

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // 处理头像
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            String avatarPath = fileStorageService.uploadFile(request.getAvatarFile(), user.getId());
            user.setAvatar(avatarPath);
        } else if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        // 更新时间
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 构建用户信息Map
     *
     * @param user 用户实体
     * @return 包含用户可公开信息的Map
     */
    private Map<String, Object> buildUserInfoMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("bio", user.getBio());
        return data;
    }
}
