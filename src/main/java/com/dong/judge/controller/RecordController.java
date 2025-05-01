package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.pojo.judge.Submission;
import com.dong.judge.model.vo.PageResult;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提交记录控制器
 * <p>
 * 处理用户代码提交记录相关的请求，包括获取用户提交记录、获取提交详情、获取题目排行榜等
 * </p>
 */
@RestController
@RequestMapping("/record")
@Tag(name = "提交记录", description = "提交记录相关接口")
@RequiredArgsConstructor
@Slf4j
public class RecordController {
    
    private final SubmissionService submissionService;
    
    /**
     * 获取用户的提交记录
     *
     * @param problemId 题目ID
     * @return 提交记录列表
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户提交记录", description = "获取当前登录用户对指定题目的提交记录列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取提交记录"),
            @ApiResponse(responseCode = "401", description = "用户未登录")
    })
    public Result<List<Submission>> getUserSubmissions(
            @Parameter(description = "题目ID") @RequestParam(required = false) String problemId) {
        // 检查用户是否登录
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录或登录已过期");
        }
        
        String userId = (String) StpUtil.getLoginId();
        try {
            List<Submission> submissions = submissionService.getUserSubmissions(userId, problemId);
            return Result.success(submissions);
        } catch (Exception e) {
            log.error("获取用户提交记录失败", e);
            return Result.error(500, "获取提交记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取提交详情
     *
     * @param submissionId 提交ID
     * @return 提交详情
     */
    @GetMapping("/{submissionId}")
    @Operation(summary = "获取提交详情", description = "根据提交ID获取提交详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取提交详情"),
            @ApiResponse(responseCode = "404", description = "提交记录不存在")
    })
    public Result<Submission> getSubmissionDetail(
            @Parameter(description = "提交ID") @PathVariable String submissionId) {
        try {
            Submission submission = submissionService.getSubmissionById(submissionId);
            if (submission == null) {
                return Result.error(404, "提交记录不存在");
            }
            return Result.success(submission);
        } catch (Exception e) {
            log.error("获取提交详情失败", e);
            return Result.error(500, "获取提交详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取题目执行时间排行榜
     *
     * @param problemId 题目ID
     * @return 排行榜列表
     */
    @GetMapping("/ranking/time")
    @Operation(summary = "获取题目执行时间排行榜", description = "获取指定题目的执行时间排行榜")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取排行榜")
    })
    public Result<List<Submission>> getTimeRanking(
            @Parameter(description = "题目ID") @RequestParam String problemId) {
        try {
            List<Submission> rankings = submissionService.getProblemTimeRanking(problemId);
            return Result.success(rankings);
        } catch (Exception e) {
            log.error("获取题目执行时间排行榜失败", e);
            return Result.error(500, "获取排行榜失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取题目内存使用排行榜
     *
     * @param problemId 题目ID
     * @return 排行榜列表
     */
    @GetMapping("/ranking/memory")
    @Operation(summary = "获取题目内存使用排行榜", description = "获取指定题目的内存使用排行榜")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取排行榜")
    })
    public Result<List<Submission>> getMemoryRanking(
            @Parameter(description = "题目ID") @RequestParam String problemId) {
        try {
            List<Submission> rankings = submissionService.getProblemMemoryRanking(problemId);
            return Result.success(rankings);
        } catch (Exception e) {
            log.error("获取题目内存使用排行榜失败", e);
            return Result.error(500, "获取排行榜失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页获取所有用户的提交记录
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param problemId 题目ID（可选）
     * @param userId 用户ID（可选）
     * @param status 提交状态（可选）
     * @param language 编程语言（可选）
     * @param sortBy 排序字段（可选，默认为提交时间）
     * @param sortDirection 排序方向（可选，默认为降序）
     * @return 分页提交记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取所有提交记录", description = "分页获取所有用户的提交记录，支持多种查询条件和排序方式")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取提交记录")
    })
    public Result<PageResult<Submission>> getAllSubmissionsPage(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "题目ID") @RequestParam(required = false) String problemId,
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "提交状态") @RequestParam(required = false) String status,
            @Parameter(description = "编程语言") @RequestParam(required = false) String language,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            
            Page<Submission> submissionPage = submissionService.getAllSubmissionsPage(pageRequest, problemId, userId, status, language);
            PageResult<Submission> pageResult = PageResult.fromPage(submissionPage, submissionPage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("分页获取提交记录失败", e);
            return Result.error(500, "获取提交记录失败: " + e.getMessage());
        }
    }
}
