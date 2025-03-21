package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试用例")
public class TestCase {
    @Schema(description = "测试用例ID", example = "1")
    private Long id;
    
    @Schema(description = "测试用例输入数据", example = "1 2")
    private String input;
}