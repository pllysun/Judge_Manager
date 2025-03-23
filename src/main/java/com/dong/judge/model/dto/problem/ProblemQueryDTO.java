package com.dong.judge.model.dto.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目查询参数DTO
 * 用于接收分页查询题目的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "题目查询参数DTO")
public class ProblemQueryDTO {
    @Schema(description = "页码，从1开始")
    private Integer pageNum = 1;
    
    @Schema(description = "每页大小")
    private Integer pageSize = 10;
    
    @Schema(description = "难度类型（简单、中等、困难）")
    private String difficulty;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "标签列表")
    private List<String> tags;
}