package com.dong.judge.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
@Schema(description = "用户表")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @TableField("username")
    @Schema(description = "用户名", example = "user123")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度必须在6-16个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$",
            message = "密码只能包含字母和数字")
    @TableField("password")
    @Schema(description = "密码", example = "abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$",
            message = "邮箱格式不正确，请输入有效的邮箱地址"
    )
    @TableField("email")
    @Schema(description = "邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Size(max = 30, message = "昵称长度不能超过30个字符")
    @TableField("nickname")
    @Schema(description = "昵称", example = "新用户")
    private String nickname;

    @TableField("avatar")
    @Schema(description = "头像URL")
    private String avatar;

    @TableField("bio")
    @Schema(description = "个人简介")
    private String bio;

    @TableField(exist = false)
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String captcha;

    @TableField(exist = false)
    @Schema(description = "验证码ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String captchaId;

    @Builder.Default
    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("roles")
    private String roles;

    @Builder.Default
    @TableField("ban")
    private Boolean ban = false;

    public User(String email) {
        this.email = email;
    }
}