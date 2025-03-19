package com.dong.judge.model.pojo.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@Schema(description = "系统通知实体")
public class Notification {
    @Id
    @Schema(description = "通知ID")
    private String id;
    
    @Schema(description = "通知类型", example = "SYSTEM_UPDATE", allowableValues = {"SYSTEM_UPDATE", "COMPETITION_START", "ANNOUNCEMENT", "OTHER"})
    private String type;
    
    @Schema(description = "通知标题", example = "系统更新通知")
    private String title;
    
    @Schema(description = "通知内容", example = "系统将于2023年12月15日进行维护升级，届时平台将暂停服务2小时。")
    private String content;
    
    @Schema(description = "是否已读", example = "false")
    private Boolean read;
    
    @Schema(description = "接收者ID", example = "all")
    private String receiverId;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}