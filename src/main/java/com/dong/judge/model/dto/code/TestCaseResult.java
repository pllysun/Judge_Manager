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
    
    @Schema(description = "执行状态", example = "Accepted")
    private String status;
    
    @Schema(description = "CPU 执行时间（纳秒）", example = "1173000")
    private Long time;
    
    @Schema(description = "CPU 执行时间（毫秒）", example = "1.17")
    private Double timeInMs;
    
    @Schema(description = "内存使用（字节）", example = "10637312")
    private Long memory;
    
    @Schema(description = "内存使用（MB）", example = "10.14")
    private Double memoryInMB;
    
    /**
     * 获取带单位的CPU执行时间（毫秒）
     * @return 带单位的CPU执行时间字符串
     */
    public String getTimeInMsWithUnit() {
        return timeInMs != null ? String.format("%.2f ms", timeInMs) : "0.00 ms";
    }
    
    /**
     * 获取带单位的内存使用（MB）
     * @return 带单位的内存使用字符串
     */
    public String getMemoryInMBWithUnit() {
        return memoryInMB != null ? String.format("%.2f MB", memoryInMB) : "0.00 MB";
    }
    
    @Schema(description = "实际运行时间（纳秒）", example = "1100200")
    private Long runTime;
    
    @Schema(description = "标准输出", example = "3\n")
    private String stdout;
}