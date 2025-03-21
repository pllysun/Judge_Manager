package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试用例执行结果")
public class TestCaseResult {
    @Schema(description = "测试用例ID", example = "1")
    private Long id;
    
    @Schema(description = "CPU 执行时间（纳秒）", example = "1173000")
    private Long time;
    
    @Schema(description = "内存使用（字节）", example = "10637312")
    private Long memory;
    
    @Schema(description = "实际运行时间（纳秒）", example = "1100200")
    private Long runTime;
    
    @Schema(description = "标准输出", example = "3\n")
    private String stdout;
}