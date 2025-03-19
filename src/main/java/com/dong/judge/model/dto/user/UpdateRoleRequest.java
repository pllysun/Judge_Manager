package com.dong.judge.model.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户角色更新请求
 */
@Data
public class UpdateRoleRequest {

    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色级别不能为空")
    @Min(value = 1, message = "角色级别最小为1")
    @Max(value = 5, message = "角色级别最大为5")
    private Integer roleLevel;
}