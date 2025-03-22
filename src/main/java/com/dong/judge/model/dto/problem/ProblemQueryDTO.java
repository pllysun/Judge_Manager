package com.dong.judge.model.dto.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "题目查询参数DTO")
public class ProblemQueryDTO {
    
    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码，从1开始", defaultValue = "1")
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
    
    /**
     * 难度类型（简单、中等、困难）
     */
    @Schema(description = "难度类型（简单、中等、困难）")
    private String difficulty;
    
    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词")
    private String keyword;
    
    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;
}