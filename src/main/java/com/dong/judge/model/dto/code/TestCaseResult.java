package com.dong.judge.model.dto.code;

import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
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
    
    @Schema(description = "测试用例描述", example = "测试基本加法")
    private String description;
    
    @Schema(description = "执行状态", example = "Accepted")
    private String status;
    
    @Schema(description = "是否通过", example = "true")
    private boolean passed;
    
    @Schema(description = "CPU 执行时间（纳秒）", example = "1173000")
    private Long time;
    
    @Schema(description = "内存使用（字节）", example = "10637312")
    private Long memory;
    
    @Schema(description = "实际运行时间（纳秒）", example = "1100200")
    private Long runTime;
    
    @Schema(description = "标准输出", example = "3\n")
    private String stdout;
    
    @Schema(description = "标准错误", example = "")
    private String stderr;
    
    @Schema(description = "期望输出", example = "3")
    private String expectedOutput;
    
    @Schema(description = "编译错误信息")
    private String compileError;
    
    /**
     * 从代码执行结果创建测试用例结果
     */
    public static TestCaseResult fromCodeExecuteResult(TestCase testCase, CodeExecuteResult result) {
        boolean passed = "Accepted".equals(result.getStatus()) && 
                         result.getStdout() != null && 
                         result.getStdout().trim().equals(testCase.getExpectedOutput().trim());
        
        return TestCaseResult.builder()
                .id(testCase.getId())
                .description(testCase.getDescription())
                .status(result.getStatus())
                .passed(passed)
                .time(result.getTime())
                .memory(result.getMemory())
                .runTime(result.getRunTime())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .expectedOutput(testCase.getExpectedOutput())
                .compileError(result.getCompileError())
                .build();
    }
}