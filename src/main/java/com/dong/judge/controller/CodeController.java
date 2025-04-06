package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.dto.code.*;
import com.dong.judge.model.enums.ExecutionStatus;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.SubmissionStatistics;
import com.dong.judge.model.pojo.judge.UserCodeDraft;
import com.dong.judge.model.pojo.judge.Submission;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.CodeService;
import com.dong.judge.service.ProblemService;
import com.dong.judge.service.ProblemStatisticsService;
import com.dong.judge.service.UserCodeDraftService;
import com.dong.judge.service.SubmissionService;
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
    private final UserCodeDraftService userCodeDraftService;
    private final ProblemStatisticsService problemStatisticsService;
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    @PostMapping("/submit")
    @Operation(summary = "提交代码", description = "运行代码并返回测试用例执行结果")
    public Result<CodeSubmitResponse> submitCode(@RequestBody @Valid CodeSubmitRequest request) {
        log.info("收到代码运行请求: problemId={}, language={}", request.getProblemId(), request.getLanguage());
        
        String userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsString();
        }
        
        try {
            TestCaseSetResult result = codeService.submitCode(request, userId);

            result.setTestCaseResults(null);
            // 构建响应对象
            CodeSubmitResponse response = CodeSubmitResponse.builder()
                    .submissionId(result.getSubmissionId())
                    .testCaseSetResult(result)
                    .build();
            
            if (result.isAllPassed()) {
                result.setTestCaseResults(null);
                return Result.success("恭喜！所有测试用例通过 " + result.getPassRatio(), response);
            }
            
            if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
                return Result.error(
                    ExecutionStatus.COMPILE_ERROR.getHttpStatus(),
                    ExecutionStatus.COMPILE_ERROR.getMessage() + ": " + result.getCompileError(),
                    response
                );
            }
            
            TestCaseResult failedTestCase = result.getFirstFailedTestCase();
            if (failedTestCase != null) {
                TestCaseSetResult simplifiedResult = TestCaseSetResult.builder()
                        .id(result.getId())
                        .name(result.getName())
                        .description(result.getDescription())
                        .testCaseResults(List.of(failedTestCase))
                        .totalCount(result.getTotalCount())
                        .passedCount(result.getPassedCount())
                        .avgTime(result.getAvgTime())
                        .avgTimeInMs(result.getAvgTimeInMs())
                        .avgMemory(result.getAvgMemory())
                        .avgMemoryInMB(result.getAvgMemoryInMB())
                        .build();
                
                response.setTestCaseSetResult(simplifiedResult);
                ExecutionStatus status = ExecutionStatus.getByCode(failedTestCase.getStatus());
                
                return Result.error(
                    status.getHttpStatus(),
                    status.getMessage() + " - 通过情况: " + result.getPassRatio(),
                    response
                );
            }
            
            return Result.success("测试通过情况: " + result.getPassRatio(), response);
        } catch (IllegalArgumentException e) {
            log.error("代码提交参数错误", e);
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("代码提交异常", e);
            return Result.error("代码提交失败: " + e.getMessage());
        }
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

    @PostMapping("/draft/save")
    @Operation(summary = "保存代码草稿", description = "保存用户的代码草稿")
    public Result<UserCodeDraft> saveCodeDraft(@RequestBody @Valid CodeDraftRequest request) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, "请先登录");
        }
        
        String userId = StpUtil.getLoginIdAsString();
        UserCodeDraft draft = userCodeDraftService.saveDraft(
            userId,
            request.getProblemId(),
            request.getCode(),
            request.getLanguage()
        );
        
        return Result.success("代码草稿保存成功", draft);
    }

    @GetMapping("/draft/{problemId}")
    @Operation(summary = "获取代码草稿", description = "获取用户保存的代码草稿")
    public Result<UserCodeDraft> getCodeDraft(@PathVariable String problemId) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, "请先登录");
        }
        
        String userId = StpUtil.getLoginIdAsString();
        UserCodeDraft draft = userCodeDraftService.getDraft(userId, problemId);
        
        if (draft == null) {
            return Result.success("未找到代码草稿", null);
        }
        
        return Result.success("获取代码草稿成功", draft);
    }

    @GetMapping("/statistics/{problemId}")
    @Operation(summary = "获取提交统计", description = "获取用户的提交统计信息")
    public Result<List<Submission>> getSubmissionStatistics(@PathVariable String problemId) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, "请先登录");
        }
        
        String userId = StpUtil.getLoginIdAsString();
        List<Submission> submissions = submissionService.getUserSubmissions(userId, problemId);
        
        return Result.success("获取提交统计成功", submissions);
    }

    @GetMapping("/statistics/time-ranking/{problemId}")
    @Operation(summary = "获取时间排名", description = "获取题目的执行时间排名")
    public Result<List<Submission>> getTimeRanking(@PathVariable String problemId) {
        List<Submission> ranking = submissionService.getProblemTimeRanking(problemId);
        return Result.success("获取时间排名成功", ranking);
    }

    @GetMapping("/statistics/memory-ranking/{problemId}")
    @Operation(summary = "获取内存排名", description = "获取题目的内存使用排名")
    public Result<List<Submission>> getMemoryRanking(@PathVariable String problemId) {
        List<Submission> ranking = submissionService.getProblemMemoryRanking(problemId);
        return Result.success("获取内存排名成功", ranking);
    }

    @GetMapping("/submission/{submissionId}")
    @Operation(summary = "获取提交详情", description = "根据提交ID获取提交的详细信息")
    public Result<Submission> getSubmissionDetail(@PathVariable String submissionId) {
        try {
            Submission submission = submissionService.getSubmissionById(submissionId);
            return Result.success("获取提交详情成功", submission);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        }
    }
}
