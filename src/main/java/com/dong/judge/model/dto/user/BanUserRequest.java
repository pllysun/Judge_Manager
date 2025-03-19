package com.dong.judge.model.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户封禁请求
 */
@Data
public class BanUserRequest {

    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private boolean ban;
}
