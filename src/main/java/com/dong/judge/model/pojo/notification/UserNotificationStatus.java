package com.dong.judge.model.pojo.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 用户通知状态实体类
 * <p>
 * 用于存储用户对特定通知的已读状态
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_notification_status")
@CompoundIndexes({
        @CompoundIndex(name = "user_notification_idx", def = "{userId: 1, notificationId: 1}", unique = true)
})
@Schema(description = "用户通知状态实体")
public class UserNotificationStatus {
    @Id
    @Schema(description = "状态ID")
    private String id;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "通知ID")
    private String notificationId;
    
    @Schema(description = "是否已读", example = "false")
    private Boolean read;
    
    @Schema(description = "是否已删除", example = "false")
    private Boolean deleted;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}