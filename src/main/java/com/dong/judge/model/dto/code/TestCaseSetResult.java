package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试集合执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试集合执行结果")
public class TestCaseSetResult {
    @Schema(description = "测试集ID", example = "1")
    private Long id;
    
    @Schema(description = "测试集名称", example = "基础测试集")
    private String name;
    
    @Schema(description = "测试集描述", example = "用于测试基本功能")
    private String description;
    
    @Schema(description = "测试用例结果列表")
    private List<TestCaseResult> testCaseResults;
    
    @Schema(description = "总测试用例数", example = "5")
    private int totalCount;
    
    @Schema(description = "通过测试用例数", example = "4")
    private int passedCount;
    
    @Schema(description = "总CPU执行时间（纳秒）", example = "5865000")
    private Long totalTime;
    
    @Schema(description = "平均CPU执行时间（纳秒）", example = "1173000")
    private Long avgTime;
    
    @Schema(description = "总内存使用（字节）", example = "53186560")
    private Long totalMemory;
    
    @Schema(description = "平均内存使用（字节）", example = "10637312")
    private Long avgMemory;
    
    @Schema(description = "总实际运行时间（纳秒）", example = "5501000")
    private Long totalRunTime;
    
    @Schema(description = "平均实际运行时间（纳秒）", example = "1100200")
    private Long avgRunTime;
    
    @Schema(description = "编译错误信息")
    private String compileError;
    
    @Schema(description = "是否全部通过", example = "false")
    private boolean allPassed;
    
    /**
     * 计算统计信息
     */
    public void calculateStatistics() {
        if (testCaseResults == null || testCaseResults.isEmpty()) {
            totalCount = 0;
            passedCount = 0;
            totalTime = 0L;
            avgTime = 0L;
            totalMemory = 0L;
            avgMemory = 0L;
            totalRunTime = 0L;
            avgRunTime = 0L;
            allPassed = false;
            return;
        }
        
        totalCount = testCaseResults.size();
        passedCount = (int) testCaseResults.stream().filter(TestCaseResult::isPassed).count();
        allPassed = passedCount == totalCount;
        
        totalTime = testCaseResults.stream()
                .mapToLong(result -> result.getTime() != null ? result.getTime() : 0)
                .sum();
        
        totalMemory = testCaseResults.stream()
                .mapToLong(result -> result.getMemory() != null ? result.getMemory() : 0)
                .sum();
        
        totalRunTime = testCaseResults.stream()
                .mapToLong(result -> result.getRunTime() != null ? result.getRunTime() : 0)
                .sum();
        
        avgTime = totalCount > 0 ? totalTime / totalCount : 0;
        avgMemory = totalCount > 0 ? totalMemory / totalCount : 0;
        avgRunTime = totalCount > 0 ? totalRunTime / totalCount : 0;
    }
}