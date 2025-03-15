package com.dong.judge.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.user.PasswordResetCodeRequest;
import com.dong.judge.model.dto.user.SystemNotificationRequest;
import com.dong.judge.model.pojo.user.User;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.EmailService;
import com.dong.judge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 邮件通知服务
 *
 * 处理系统中各类邮件发送需求，包括但不限于验证码、通知和提醒
 */
@RestController
@RequestMapping("/email")
@Tag(name = "邮件服务", description = "发送各类邮件通知，包括验证码、系统通知等")
public class EmailNotificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送密码修改验证码
     * <p>
     * 为已登录用户发送密码修改验证码邮件，用于验证身份。
     * 验证步骤:
     * - 验证用户登录状态
     * - 验证旧密码是否正确
     * - 发送验证码到用户邮箱
     * <p>
     * 验证码将发送到用户的注册邮箱，有效期10分钟。
     */
    @PostMapping("/password-reset-code")
    @Operation(summary = "发送密码修改验证码", description = "验证旧密码后发送密码修改验证码到用户邮箱")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证码发送成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public Result<?> sendPasswordResetCode(@RequestBody @Valid PasswordResetCodeRequest request) {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }

        try {
            // 获取当前登录用户的邮箱
            String email = (String) StpUtil.getLoginId();
            User user = userService.getByEmail(email);

            if (user == null) {
                return Result.error(400, "用户不存在");
            }

            // 验证旧密码
            if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
                return Result.error(400, "旧密码错误");
            }

            // 生成并发送验证码邮件
            return sendVerificationEmail(email, EmailType.PASSWORD_RESET);
        } catch (Exception e) {
            return Result.error("发送邮件失败: " + e.getMessage());
        }
    }

    /**
     * 发送注册验证码
     *
     * 为用户注册发送验证码邮件，验证用户邮箱的有效性。
     * 验证码将发送到提供的邮箱，有效期10分钟。
     *
     * @param email 用户邮箱地址
     * @return 结果，包含验证码ID
     */
    @PostMapping("/registration-code")
    @Operation(summary = "发送注册验证码", description = "发送验证码到用户邮箱用于注册")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证码发送成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public Result<?> sendRegistrationCode(@RequestParam String email) {
        try {
            // 检查邮箱是否已注册
            if (userService.isEmailExist(email)) {
                return Result.error(400, "邮箱已被注册");
            }

            // 生成并发送验证码邮件
            return sendVerificationEmail(email, EmailType.REGISTRATION);
        } catch (Exception e) {
            return Result.error("发送邮件失败: " + e.getMessage());
        }
    }

    /**
     * 发送系统通知邮件
     *
     * @param request 系统通知请求
     * @return 处理结果
     */
    @PostMapping("/system-notification")
    @Operation(summary = "发送系统通知", description = "向用户发送系统通知邮件")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "通知发送成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未授权操作")
    })
    public Result<?> sendSystemNotification(@RequestBody @Valid SystemNotificationRequest request) {
        // 此处应添加管理员权限检查
        if (!StpUtil.hasRole("admin")) {
            return Result.error(401, "未授权操作");
        }

        try {
            boolean sent = emailService.sendSystemNotification(
                    request.getEmail(),
                    request.getSubject(),
                    request.getContent()
            );

            if (!sent) {
                return Result.error("邮件发送失败，请稍后再试");
            }

            return Result.success("系��通知邮件已发送");
        } catch (Exception e) {
            return Result.error("发送邮件失败: " + e.getMessage());
        }
    }

    /**
     * 发送通用验证码邮件
     *
     * @param email 接收邮件的地址
     * @param emailType 邮件类型
     * @return 处理结果
     */
    private Result<?> sendVerificationEmail(String email, EmailType emailType) throws Exception {
        // 生成6位数验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 生成验证码ID
        String verificationId = UUID.randomUUID().toString();

        // 确定Redis存储前缀
        String codePrefix = emailType.getRedisPrefix();

        // 保存验证码到Redis，10分钟有效
        redisTemplate.opsForValue().set(codePrefix + ":" + verificationId, code, 10, TimeUnit.MINUTES);

        // 根据邮件类型发送验证码
        boolean sent = switch (emailType) {
            case PASSWORD_RESET -> emailService.sendPasswordResetEmail(email, code);
            case REGISTRATION -> emailService.sendVerificationEmail(email, code);
            default -> throw new IllegalArgumentException("不支持的邮件类型");
        };

        if (!sent) {
            return Result.error("邮件发送失败，请稍后再试");
        }

        // 返回验证码ID
        Map<String, String> data = new HashMap<>();
        data.put("verificationId", verificationId);

        return Result.success("验证码邮件已发送，请查收", data);
    }

    /**
     * 邮件类型枚举
     */
    private enum EmailType {
        PASSWORD_RESET("password_reset"),
        REGISTRATION("verification");

        private final String redisPrefix;

        EmailType(String redisPrefix) {
            this.redisPrefix = redisPrefix;
        }

        public String getRedisPrefix() {
            return redisPrefix;
        }
    }
}