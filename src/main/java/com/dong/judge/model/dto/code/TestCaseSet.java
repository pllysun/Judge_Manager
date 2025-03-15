package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试集合
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试集合")
public class TestCaseSet {
    @Schema(description = "测试集ID", example = "1")
    private Long id;
    
    @Schema(description = "测试集名称", example = "基础测试集")
    private String name;
    
    @Schema(description = "测试集描述", example = "用于测试基本功能")
    private String description;
    
    @Schema(description = "测试用例列表")
    private List<TestCase> testCases;
}