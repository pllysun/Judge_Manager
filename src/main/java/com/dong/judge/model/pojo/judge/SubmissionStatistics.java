package com.dong.judge.model.pojo.judge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "submission_statistics")
@Schema(description = "提交统计信息")
public class SubmissionStatistics {
    @Id
    @Schema(description = "统计ID")
    private String id;
    
    @Schema(description = "提交ID")
    private String submissionId;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "题目ID")
    private String problemId;
    
    @Schema(description = "执行时间(ms)")
    private Long executionTime;
    
    @Schema(description = "内存使用(MB)")
    private Long memoryUsed;
    
    @Schema(description = "提交时间")
    private LocalDateTime submissionTime;
    
    @Schema(description = "提交状态")
    private String status;
} 