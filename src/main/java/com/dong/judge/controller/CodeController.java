package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.code.CodeRunRequest;
import com.dong.judge.model.dto.code.CodeSubmitRequest;
import com.dong.judge.model.dto.code.TestCaseSet;
import com.dong.judge.model.dto.code.TestCaseSetResult;
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
        // 执行代码并返回结果
        TestCaseSetResult result = codeService.runCode(request);
        return Result.success(result);
    }
}
