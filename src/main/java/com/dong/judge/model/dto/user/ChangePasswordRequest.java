package com.dong.judge.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度必须在6-16个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$", message = "密码只能包含字母和数字")
    private String newPassword;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "验证码ID不能为空")
    private String verificationId;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
}