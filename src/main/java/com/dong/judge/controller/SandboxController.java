package com.dong.judge.controller;


import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.SandboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 沙箱接口
 * 直接对接沙箱的服务，提供代码执行环境
 */
@Slf4j
@RestController
@RequestMapping("/sandbox")
@RequiredArgsConstructor
@Tag(name = "沙箱管理", description = "代码执行沙箱相关接口")
public class SandboxController {

    private final SandboxService sandboxService;

    @PostMapping("/execute")
    @Operation(summary = "执行代码", description = "在安全的沙箱环境中执行用户提交的代码")
    public Result<?> executeCode(@RequestBody @Valid CodeExecuteRequest request) {
        log.info("收到代码执行请求: language={},内容:{}", request.getLanguage(), request.getCode());
        return Result.success(sandboxService.executeCode(request));
    }

}