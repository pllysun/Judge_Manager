package com.dong.judge.model.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统通知请求DTO
 */
@Data
@Schema(description = "系统通知请求")
public class NotificationRequest {
    
    @NotBlank(message = "通知类型不能为空")
    @Schema(description = "通知类型", example = "SYSTEM_UPDATE", allowableValues = {"SYSTEM_UPDATE", "COMPETITION_START", "ANNOUNCEMENT", "OTHER"})
    private String type;
    
    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", example = "系统更新通知")
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", example = "系统将于2023年12月15日进行维护升级，届时平台将暂停服务2小时。")
    private String content;
    
    @Schema(description = "接收者ID，为空表示发送给所有用户", example = "all")
    private String receiverId;
}