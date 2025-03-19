package com.dong.judge.controller;

import com.dong.judge.model.converter.ProblemConverter;
import com.dong.judge.model.dto.problem.CreateProblemRequest;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.ProblemTag;
import com.dong.judge.model.vo.Result;
import jakarta.validation.Valid;
import com.dong.judge.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
@Tag(name = "题目管理", description = "题目管理相关接口")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping
    @Operation(summary = "创建题目", description = "创建一个新的题目，难度等级为1-8：1-2简单，3-5中等，6-8困难")
    public Result<Problem> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        try {
            Problem problem = ProblemConverter.toEntity(request);
            Problem createdProblem = problemService.createProblem(problem);
            return Result.success("创建题目成功", createdProblem);
        } catch (Exception e) {
            return Result.error("创建题目失败: " + e.getMessage());
        }
    }


    public Result<Problem> updateProblem(
            @Parameter(description = "题目ID") @PathVariable String problemId,
            @RequestBody Problem problem) {
        try {
            Problem updatedProblem = problemService.updateProblem(problemId, problem);
            return Result.success("更新题目成功", updatedProblem);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新题目失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{problemId}")
    @Operation(summary = "删除题目", description = "根据题目ID删除题目")
    public Result<Boolean> deleteProblem(
            @Parameter(description = "题目ID") @PathVariable String problemId) {
        try {
            boolean result = problemService.deleteProblem(problemId);
            return Result.success("删除题目成功", result);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/{problemId}")
    @Operation(summary = "获取题目详情", description = "根据题目ID获取题目详情")
    public Result<Problem> getProblemById(
            @Parameter(description = "题目ID") @PathVariable String problemId) {
        try {
            Problem problem = problemService.getProblemById(problemId);
            return Result.success(problem);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "根据题号获取题目", description = "根据题号获取题目详情")
    public Result<Problem> getProblemByNumber(
            @Parameter(description = "题号") @PathVariable Integer number) {
        try {
            Problem problem = problemService.getProblemByNumber(number);
            return Result.success(problem);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取题目列表", description = "分页获取题目列表，支持按难度等级、标签、关键词筛选")
    public Result<Page<Problem>> getProblemList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "难度等级(1-8)") @RequestParam(required = false) Integer level,
            @Parameter(description = "标签") @RequestParam(required = false) String tag,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        try {
            Page<Problem> problems = problemService.getProblemList(page, size, level, tag, keyword);
            return Result.success(problems);
        } catch (Exception e) {
            return Result.error("获取题目列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/tag")
    @Operation(summary = "创建题目标签", description = "创建一个新的题目标签")
    public Result<ProblemTag> createProblemTag(@RequestBody ProblemTag tag) {
        // 参数校验
        if (tag.getName() == null || tag.getName().isEmpty()) {
            return Result.badRequest("标签名称不能为空");
        }

        try {
            ProblemTag createdTag = problemService.createProblemTag(tag);
            return Result.success("创建标签成功", createdTag);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建标签失败: " + e.getMessage());
        }
    }

    @GetMapping("/tag/list")
    @Operation(summary = "获取标签列表", description = "分页获取所有题目标签")
    public Result<Page<ProblemTag>> getAllTags(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        try {
            Page<ProblemTag> tags = problemService.getAllTags(page, size);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error("获取标签列表失败: " + e.getMessage());
        }
    }
}
