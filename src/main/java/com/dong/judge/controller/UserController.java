package com.dong.judge.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.judge.model.dto.user.BanUserRequest;
import com.dong.judge.model.dto.user.SystemNotificationRequest;
import com.dong.judge.model.dto.user.UpdateRoleRequest;
import com.dong.judge.model.dto.user.UpdateUserInfoRequest;
import com.dong.judge.model.enums.RoleEnum;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.EmailService;
import com.dong.judge.service.FileStorageService;
import com.dong.judge.service.RoleService;
import com.dong.judge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息管理
 * <p>
 * 提供用户信息管理相关的API接口
 * </p>
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static  final  String LocalImageURL="http://pllysun.top:7500/api";

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
            
            // 处理头像URL，确保可以在浏览器中直接访问
            String avatarPath = user.getAvatar();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                // 如果头像路径不是以http开头的URL，则添加服务器上下文路径
                if (!avatarPath.startsWith("http")) {
                    // 使用/uploads/前缀，与WebConfig中的资源映射匹配
                    avatarPath = LocalImageURL+"/uploads/" + avatarPath;
                }
            }
            data.put("avatar", avatarPath);
            data.put("bio", user.getBio());

            // 获取用户的最高角色
            RoleEnum highestRole = roleService.getHighestRole(user);
            data.put("role", highestRole != null ? highestRole.getName() : null);
            data.put("roleCode", highestRole != null ? highestRole.getCode() : null);
            data.put("roleLevel", highestRole != null ? highestRole.getLevel() : 0);

            // 保留原始角色列表以便需要
            data.put("roles", user.getRoles());

            data.put("ban", user.getBan());
            data.put("createdAt", user.getCreatedAt());
            data.put("lastLoginAt", user.getLastLoginAt());
            return data;
        }
    
    /**
     * 获取用户列表
     * <p>
     * 分页获取系统中的用户列表，支持按邮箱进行筛选。
     * 仅管理员及以上角色可访问。
     * </p>
     *
     * @param page  页码，从0开始
     * @param size  每页大小
     * @param email 邮箱筛选条件（可选）
     * @return 用户列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "分页获取系统中的用户列表，支持按邮箱筛选（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "邮箱筛选") @RequestParam(required = false) String email) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            if (email != null && !email.isEmpty()) {
                queryWrapper.like(User::getEmail, email);
            }
            
            // 执行分页查询
            Page<User> userPage = new Page<>(page, size);
            Page<User> result = userService.page(userPage, queryWrapper);
            
            // 处理返回结果，移除敏感信息
            List<Map<String, Object>> userList = result.getRecords().stream()
                    .map(this::buildUserInfoMap)
                    .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());
            data.put("records", userList);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

   /**
     * 修改用户角色
     * <p>
     * 管理员可以修改普通用户或会员的角色。
     * 超级管理员可以修改任何角色，包括管理员。
     * 修改权限受到当前用户角色级别的限制。
     * </p>
     *
     * @param request 包含目标用户邮箱和角色级别的请求
     * @return 操作结果
     */
    @PostMapping("/updateRole")
    @Operation(summary = "修改用户角色", description = "修改用户的角色权限级别（需要管理员或超级管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> updateUserRole(@RequestBody @Valid UpdateRoleRequest request) {
        try {
            // 获取请求参数
            String targetEmail = request.getEmail();
            int roleLevel = request.getRoleLevel();

            // 参数验证
            if (roleLevel < 1 || roleLevel > 5) {
                return Result.badRequest("无效的角色级别，有效值为1-5");
            }

            // 获取当前用户角色
            RoleEnum currentUserRole = getRoleEnum();

            // 获取目标用户
            User targetUser = userService.getByEmail(targetEmail);
            if (targetUser == null) {
                return Result.badRequest("目标用户不存在");
            }

            // 获取目标用户当前角色
            RoleEnum targetUserRole = roleService.getHighestRole(targetUser);

            // 权限检查1：不能操作比自己角色高的用户
            if (targetUserRole.getLevel() > currentUserRole.getLevel()) {
                return Result.error(403, "无权操作比自己角色高的用户");
            }

            // 权限检查2：不能设置比自己角色高的角色
            if (roleLevel > currentUserRole.getLevel()) {
                return Result.error(403, "无权设置比自己更高的角色");
            }

            // 权限检查3：普通管理员不能设置/修改管理员角色
            if (currentUserRole == RoleEnum.ADMIN && roleLevel >= RoleEnum.ADMIN.getLevel()) {
                return Result.error(403, "普通管理员不能设置管理员或更高角色");
            }

            // 权限检查4：自己无法设定自己的权限
            if (targetEmail.equals((String) StpUtil.getLoginId())) {
                return Result.error(403, "无法修改自己的角色");
            }

            // 获取对应的角色枚举
            RoleEnum newRole = null;
            for (RoleEnum role : RoleEnum.values()) {
                if (role.getLevel() == roleLevel) {
                    newRole = role;
                    break;
                }
            }

            if (newRole == null) {
                return Result.badRequest("无效的角色级别");
            }

            // 更新用户角色
            targetUser.setRoles(newRole.getCode());
            targetUser.setUpdatedAt(LocalDateTime.now());

            if (!userService.updateById(targetUser)) {
                return Result.error("角色更新失败，请重试");
            }

            // 如果降级用户角色，需要强制登出
            if (targetUserRole.getLevel() > newRole.getLevel() && StpUtil.isLogin(targetEmail)) {
                StpUtil.logout(targetEmail);
            }

            return Result.success("用户角色已更新为: " + newRole.getName());
        } catch (Exception e) {
            return Result.error("角色更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据邮箱获取用户信息
     * <p>
     * 通过邮箱查询特定用户的详细信息。
     * 仅管理员及以上角色可访问。
     * </p>
     *
     * @param email 用户邮箱
     * @return 用户详细信息
     */
    @GetMapping("/byEmail")
    @Operation(summary = "根据邮箱获取用户", description = "通过邮箱查询特定用户的详细信息（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> getUserByEmail(@Parameter(description = "用户邮箱") @RequestParam String email) {
        try {
            // 参数校验
            if (email == null || email.isEmpty()) {
                return Result.badRequest("邮箱不能为空");
            }
            
            // 查询用户
            User user = userService.getByEmail(email);
            if (user == null) {
                return Result.badRequest("用户不存在");
            }
            
            // 返回用户信息
            return Result.success(buildUserInfoMap(user));
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 封禁/解封用户
     * <p>
     * 管理员可以封禁或解封指定用户的账号。
     * 被封禁的用户将无法登录系统。
     * </p>
     *
     * @param request 包含用户邮箱和封禁状态的请求
     * @return 操作结果
     */
    @PostMapping("/ban")
    @Operation(summary = "封禁/解封用户", description = "管理员封禁或解封指定用户的账号（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> banUser(@RequestBody @Valid BanUserRequest request) {
        try {
            // 获取请求参数
            String targetEmail = request.getEmail();
            boolean ban = request.isBan();

            // 获取当前用户角色
            RoleEnum currentUserRole = getRoleEnum();

            // 获取目标用户
            User targetUser = userService.getByEmail(targetEmail);
            if (targetUser == null) {
                return Result.badRequest("目标用户不存在");
            }

            // 获取目标用户角色
            RoleEnum targetUserRole = roleService.getHighestRole(targetUser);

            // 权限检查：不能自己封禁自己
            if(targetEmail.equals((String) StpUtil.getLoginId())){
                return Result.error(403, "无法封禁自己");
            }

            // 权限检查：不能封禁超级管理员
            if(targetUserRole.getLevel()==RoleEnum.SUPER_ADMIN.getLevel()){
                return Result.error(403, "无权操作超级管理员");
            }

            // 权限检查：不能封禁比自己角色高的用户
            if (targetUserRole.getLevel() > currentUserRole.getLevel()) {
                return Result.error(403, "无权操作比自己角色高的用户");
            }

            // 更新用户封禁状态
            if(ban){
                StpUtil.logout(targetEmail);
            }
            targetUser.setBan(ban);
            targetUser.setUpdatedAt(LocalDateTime.now());
            boolean success = userService.updateById(targetUser);

            if (!success) {
                return Result.error("操作失败，请重试");
            }

            return Result.success(ban ? "用户已被封禁" : "用户已被解封");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     * <p>
     * 管理员可以直接重置指定用户的密码。
     * 重置后的密码将通过邮件发送给用户。
     * </p>
     *
     * @param targetEmail 用户ID
     * @return 操作结果
     */
    @PostMapping("/resetPassword/{targetEmail}")
    @Operation(summary = "重置用户密码", description = "管理员直接重置指定用户的密码（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> resetUserPassword(@Parameter(description = "用户ID") @PathVariable String targetEmail) {
        try {
            // 获取当前用户角色
            RoleEnum currentUserRole = getRoleEnum();

            // 获取目标用户
            User targetUser = userService.getByEmail(targetEmail);
            if (targetUser == null) {
                return Result.badRequest("目标用户不存在");
            }
            
            // 获取目标用户角色
            RoleEnum targetUserRole = roleService.getHighestRole(targetUser);
            
            // 权限检查：不能操作比自己角色高的用户
            if (targetUserRole.getLevel() > currentUserRole.getLevel()) {
                return Result.error(403, "无权操作比自己角色高的用户");
            }
            
            // 生成默认密码
            String newPassword = "123456Aa";
            
            // 更新用户密码
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(6));
            targetUser.setPassword(hashedPassword);
            targetUser.setUpdatedAt(LocalDateTime.now());
            boolean success = userService.updateById(targetUser);
            
            if (!success) {
                return Result.error("密码重置失败，请重试");
            }
            
            // 发送密码重置邮件
            String subject = "密码重置通知";
            String content = "您的账户密码已被管理员重置。新密码: " + newPassword + "\n请登录后立即修改密码。";
            boolean emailSent = emailService.sendSystemNotification(targetUser.getEmail(), subject, content);
            
            if (!emailSent) {
                return Result.success("密码重置成功，但邮件发送失败。新密码: " + newPassword);
            }
            
            return Result.success("密码重置成功，新密码已发送至用户邮箱");
        } catch (Exception e) {
            return Result.error("密码重置失败: " + e.getMessage());
        }
    }




    /**
     * 获取当前用户角色
     * @return 当前用户角色
     */
    private RoleEnum getRoleEnum() {
        String email = (String) StpUtil.getLoginId();
        User currentUser = userService.getByEmail(email);
        return roleService.getHighestRole(currentUser);
    }

    /**
     * 发送系统消息
     * <p>
     * 管理员可以向指定用户发送系统通知消息。
     * 消息将通过邮件方式发送给用户。
     * </p>
     *
     * @param request 系统通知请求
     * @return 操作结果
     */
    @PostMapping("/sendNotification")
    @Operation(summary = "发送系统消息", description = "管理员向指定用户发送系统通知消息（需要管理员权限）")
    @SaCheckRole("ROLE_ADMIN")
    public Result<?> sendSystemNotification(@RequestBody @Valid SystemNotificationRequest request) {
        try {
            // 参数校验
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return Result.badRequest("邮箱不能为空");
            }
            if (request.getSubject() == null || request.getSubject().isEmpty()) {
                return Result.badRequest("邮件主题不能为空");
            }
            if (request.getContent() == null || request.getContent().isEmpty()) {
                return Result.badRequest("邮件内容不能为空");
            }
            
            // 检查用户是否存在
            User targetUser = userService.getByEmail(request.getEmail());
            if (targetUser == null) {
                return Result.badRequest("目标用户不存在");
            }
            
            // 发送系统通知
            boolean success = emailService.sendSystemNotification(
                    request.getEmail(),
                    request.getSubject(),
                    request.getContent()
            );
            
            if (!success) {
                return Result.error("消息发送失败，请重试");
            }
            
            return Result.success("系统消息发送成功");
        } catch (Exception e) {
            return Result.error("消息发送失败: " + e.getMessage());
        }
    }
}
