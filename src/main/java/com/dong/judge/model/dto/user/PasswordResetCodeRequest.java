package com.dong.judge.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码重置验证码请求DTO
 */
@Data
public class PasswordResetCodeRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
}
