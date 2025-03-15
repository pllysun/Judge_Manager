package com.dong.judge.model.pojo.judge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 题目标签实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "problem_tags")
@Schema(description = "题目标签实体")
public class ProblemTag {
    @Id
    @Schema(description = "标签ID", example = "507f1f77bcf86cd799439011")
    private String id;
    
    @Indexed(unique = true)
    @Schema(description = "标签名称", example = "数组")
    private String name;
    
    @Schema(description = "标签描述", example = "与数组相关的题目")
    private String description;
    
    @Schema(description = "使用次数", example = "10")
    private Integer useCount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}