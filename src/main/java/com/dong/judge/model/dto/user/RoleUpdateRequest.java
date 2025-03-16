package com.dong.judge.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色更新请求类
 * <p>
 * 包含更新用户角色所需的信息
 * </p>
 */
@Data
@Schema(description = "角色更新请求")
public class RoleUpdateRequest {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;
}