package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.code.CodeRunRequest;
import com.dong.judge.model.dto.code.CodeSubmitRequest;
import com.dong.judge.model.dto.code.TestCaseSet;
import com.dong.judge.model.dto.code.TestCaseSetResult;
import com.dong.judge.model.enums.ExecutionStatus;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.CodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代码运行
 * <p>
 * 提供OJ风格的代码执行功能
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
@Tag(name = "代码执行", description = "OJ风格代码执行相关接口")
public class CodeController {

    private final CodeService codeService;

    @PostMapping("/submit")
    @Operation(summary = "提交代码", description = "运行代码并返回测试用例执行结果")
    public Result<TestCaseSetResult> submitCode(@RequestBody @Valid CodeSubmitRequest request) {
        log.info("收到代码运行请求: problemId={}, language={}", request.getProblemId(), request.getLanguage());
        
        // 获取当前登录用户ID
        String userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsString();
        }
        
        // 执行代码并返回结果
        TestCaseSetResult result = codeService.submitCode(request, userId);
        return Result.success(result);
    }

    @PostMapping("/run")
    @Operation(summary = "运行代码", description = "提交代码并返回测试用例执行结果")
    public Result<TestCaseSetResult> runCode(@RequestBody @Valid CodeRunRequest request) {
        log.info("收到代码运行请求: language={}", request.getLanguage());
        
        try {
            // 执行代码并返回结果
            TestCaseSetResult result = codeService.runCode(request);
            
            // 检查测试用例结果中是否有非ACCEPTED状态
            if (result.getTestCaseResults() != null && !result.getTestCaseResults().isEmpty()) {
                String statusCode = result.getTestCaseResults().get(0).getStatus();
                ExecutionStatus status = ExecutionStatus.getByCode(statusCode);
                
                // 如果不是成功状态，则返回对应的错误信息
                if (status != ExecutionStatus.ACCEPTED) {
                    String errorMessage = status == ExecutionStatus.COMPILE_ERROR 
                            ? status.getMessage() + ": " + result.getCompileError() 
                            : status.getMessage();
                            
                    return Result.error(status.getHttpStatus(), errorMessage, result);
                }
            }
            
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            // 处理参数错误，如不支持的语言等
            log.error("代码运行参数错误", e);
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            // 处理其他异常，如编译错误、运行时错误等
            log.error("代码运行异常", e);
            return Result.error("代码执行失败: " + e.getMessage());
        }
    }
}
