package com.dong.judge.controller;

import com.dong.judge.model.enums.DifficultyLevel;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.ProblemService;
import com.dong.judge.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api/problems")
@Tag(name = "题目管理", description = "题目的创建、更新、查询和删除")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    /**
     * 创建题目
     *
     * @param problem 题目信息
     * @return 创建的题目
     */
    @PostMapping
    @Operation(summary = "创建题目", description = "创建一个新的题目")
    public Result<Problem> createProblem(@RequestBody @Valid Problem problem) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            // 验证难度级别
            validateDifficulty(problem.getDifficulty());
            
            Problem createdProblem = problemService.createProblem(problem, userId);
            return Result.success("题目创建成功", createdProblem);
        } catch (Exception e) {
            log.error("创建题目失败", e);
            return Result.error("创建题目失败: " + e.getMessage());
        }
    }

    /**
     * 更新题目
     *
     * @param id 题目ID
     * @param problem 题目信息
     * @return 更新后的题目
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新题目", description = "更新题目信息")
    public Result<Problem> updateProblem(
            @PathVariable("id") @Parameter(description = "题目ID") String id,
            @RequestBody @Valid Problem problem) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            // 设置题目ID
            problem.setId(id);
            
            // 验证难度级别
            if (problem.getDifficulty() != null) {
                validateDifficulty(problem.getDifficulty());
            }
            
            Problem updatedProblem = problemService.updateProblem(problem, userId);
            return Result.success("题目更新成功", updatedProblem);
        } catch (Exception e) {
            log.error("更新题目失败", e);
            return Result.error("更新题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取题目详情
     *
     * @param id 题目ID
     * @return 题目信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取题目详情", description = "根据ID获取题目详细信息")
    public Result<Problem> getProblemById(
            @PathVariable("id") @Parameter(description = "题目ID") String id) {
        try {
            Problem problem = problemService.getProblemById(id);
            return Result.success(problem);
        } catch (Exception e) {
            log.error("获取题目详情失败", e);
            return Result.error("获取题目详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的题目列表
     *
     * @return 题目列表
     */
    @GetMapping
    @Operation(summary = "获取用户的题目列表", description = "获取当前用户创建的所有题目")
    public Result<List<Problem>> getUserProblems() {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<Problem> problems = problemService.getUserProblems(userId);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("获取用户题目列表失败", e);
            return Result.error("获取用户题目列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除题目
     *
     * @param id 题目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目", description = "删除指定ID的题目")
    public Result<Boolean> deleteProblem(
            @PathVariable("id") @Parameter(description = "题目ID") String id) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            boolean success = problemService.deleteProblem(id, userId);
            return Result.success("题目删除成功", success);
        } catch (Exception e) {
            log.error("删除题目失败", e);
            return Result.error("删除题目失败: " + e.getMessage());
        }
    }

    /**
     * 搜索题目
     *
     * @param keyword 关键词
     * @return 题目列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索题目", description = "根据关键词搜索题目")
    public Result<List<Problem>> searchProblems(
            @RequestParam("keyword") @Parameter(description = "搜索关键词") String keyword) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<Problem> problems = problemService.searchProblems(userId, keyword);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("搜索题目失败", e);
            return Result.error("搜索题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 按难度级别查询题目
     *
     * @param level 难度级别
     * @return 题目列表
     */
    @GetMapping("/difficulty/{level}")
    @Operation(summary = "按难度级别查询题目", description = "根据难度级别查询题目")
    public Result<List<Problem>> getProblemsByDifficulty(
            @PathVariable("level") @Parameter(description = "难度级别") Integer level) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            // 验证难度级别
            validateDifficulty(level);
            
            List<Problem> problems = problemService.getProblemsByDifficulty(userId, level);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("按难度级别查询题目失败", e);
            return Result.error("按难度级别查询题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 按标签查询题目
     *
     * @param tag 标签
     * @return 题目列表
     */
    @GetMapping("/tag/{tag}")
    @Operation(summary = "按标签查询题目", description = "根据标签查询题目")
    public Result<List<Problem>> getProblemsByTag(
            @PathVariable("tag") @Parameter(description = "标签") String tag) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<Problem> problems = problemService.getProblemsByTag(userId, tag);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("按标签查询题目失败", e);
            return Result.error("按标签查询题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    @Operation(summary = "获取所有标签", description = "获取所有题目中的标签（去重）")
    public Result<List<String>> getAllTags() {
        try {
            List<String> tags = problemService.getAllTags();
            return Result.success(tags);
        } catch (Exception e) {
            log.error("获取所有标签失败", e);
            return Result.error("获取所有标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证难度级别
     *
     * @param difficulty 难度级别
     */
    private void validateDifficulty(Integer difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("难度级别不能为空");
        }
        
        // 验证难度级别是否在有效范围内
        try {
            DifficultyLevel.getByLevel(difficulty);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的难度级别: " + difficulty);
        }
    }
}