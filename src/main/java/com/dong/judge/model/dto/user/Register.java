package com.dong.judge.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求类
 * <p>
 * 包含用户注册所需的信息，包括密码、邮箱和验证码信息
 * </p>
 */
@Data
@Schema(description = "用户注册请求")
public class Register {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$",
            message = "邮箱格式不正确，请输入有效的邮箱地址"
    )
    @Schema(description = "邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;


    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度必须在6-16个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$",
            message = "密码只能包含字母和数字")
    @TableField("password")
    @Schema(description = "密码", example = "abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "验证码ID不能为空")
    @Schema(description = "验证码ID，由系统生成", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationId;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    @Schema(description = "验证码，6位数字", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}