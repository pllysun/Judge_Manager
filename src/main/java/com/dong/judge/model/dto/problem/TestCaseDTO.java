package com.dong.judge.model.dto.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例数据传输对象
 * 用于返回测试用例的输入和预期输出
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试用例DTO")
public class TestCaseDTO {
    @Schema(description = "测试用例ID", example = "1")
    private Long id;
    
    @Schema(description = "测试用例输入", example = "1 2")
    private String input;
    
    @Schema(description = "测试用例预期输出", example = "3")
    private String expectedOutput;
}