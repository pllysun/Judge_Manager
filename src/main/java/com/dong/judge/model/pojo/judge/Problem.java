package com.dong.judge.model.pojo.judge;

import com.dong.judge.model.enums.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "problems")
@Schema(description = "题目")
public class Problem {
    @Id
    @Schema(description = "题目ID")
    private String id;
    
    @Schema(description = "题目标题")
    private String title;
    
    @Schema(description = "难度级别数值", example = "5")
    private Integer difficulty;
    
    @Schema(description = "难度级别枚举")
    private DifficultyLevel difficultyLevel;
    
    @Schema(description = "题目标签列表")
    private List<String> tags;
    
    @Schema(description = "题目内容")
    private String content;
    
    @Schema(description = "测试集ID")
    private String testGroupId;
    
    @Schema(description = "创建者ID")
    private String creatorId;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}