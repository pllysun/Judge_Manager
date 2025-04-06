package com.dong.judge.model.dto.code;

import com.dong.judge.model.enums.ExecutionStatus;
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
    private String id;
    
    @Schema(description = "提交ID", example = "submission-123")
    private String submissionId;
    
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
    
    @Schema(description = "总CPU执行时间（毫秒）", example = "5.865")
    private Double totalTimeInMs;
    
    @Schema(description = "平均CPU执行时间（毫秒）", example = "1.173")
    private Double avgTimeInMs;
    
    @Schema(description = "总内存使用（MB）", example = "50.72")
    private Double totalMemoryInMB;
    
    @Schema(description = "平均内存使用（MB）", example = "10.14")
    private Double avgMemoryInMB;
    
    /**
     * 获取带单位的总CPU执行时间（毫秒）
     * @return 带单位的总CPU执行时间字符串
     */
    public String getTotalTimeInMsWithUnit() {
        return totalTimeInMs != null ? String.format("%.2f ms", totalTimeInMs) : "0.00 ms";
    }
    
    /**
     * 获取带单位的平均CPU执行时间（毫秒）
     * @return 带单位的平均CPU执行时间字符串
     */
    public String getAvgTimeInMsWithUnit() {
        return avgTimeInMs != null ? String.format("%.2f ms", avgTimeInMs) : "0.00 ms";
    }
    
    /**
     * 获取带单位的总内存使用（MB）
     * @return 带单位的总内存使用字符串
     */
    public String getTotalMemoryInMBWithUnit() {
        return totalMemoryInMB != null ? String.format("%.2f MB", totalMemoryInMB) : "0.00 MB";
    }
    
    /**
     * 获取带单位的平均内存使用（MB）
     * @return 带单位的平均内存使用字符串
     */
    public String getAvgMemoryInMBWithUnit() {
        return avgMemoryInMB != null ? String.format("%.2f MB", avgMemoryInMB) : "0.00 MB";
    }
    
    @Schema(description = "编译错误信息")
    private String compileError;
    
    @Schema(description = "是否全部通过", example = "false")
    private boolean allPassed;
    
    /**
     * 获取简洁的通过情况描述
     * @return 格式为"x/y"的字符串，表示通过了x个测试用例，共y个测试用例
     */
    @Schema(description = "通过情况描述", example = "3/5")
    public String getPassRatio() {
        return passedCount + "/" + totalCount;
    }
    
    /**
     * 获取第一个错误的测试用例详情
     * @return 第一个错误的测试用例，如果全部通过则返回null
     */
    @Schema(description = "第一个错误的测试用例")
    public TestCaseResult getFirstFailedTestCase() {
        if (testCaseResults == null || testCaseResults.isEmpty() || allPassed) {
            return null;
        }
        
        return testCaseResults.stream()
                .filter(result -> !ExecutionStatus.ACCEPTED.getCode().equals(result.getStatus()))
                .findFirst()
                .orElse(null);
    }
    
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
            totalTimeInMs = 0.0;
            avgTimeInMs = 0.0;
            totalMemoryInMB = 0.0;
            avgMemoryInMB = 0.0;
            allPassed = false;
            return;
        }
        
        totalCount = testCaseResults.size();
        
        // 计算通过的测试用例数量，只有状态为Accepted的测试用例才算通过
        passedCount = (int) testCaseResults.stream()
                .filter(result -> "Accepted".equals(result.getStatus()))
                .count();
        
        // 只有当所有测试用例都通过时，allPassed才为true
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
        
        // 转换为毫秒和MB的单位
        totalTimeInMs = totalTime != null ? totalTime / 1_000_000.0 : 0.0;
        avgTimeInMs = avgTime != null ? avgTime / 1_000_000.0 : 0.0;
        totalMemoryInMB = totalMemory != null ? totalMemory / 1_048_576.0 : 0.0;
        avgMemoryInMB = avgMemory != null ? avgMemory / 1_048_576.0 : 0.0;
    }
}