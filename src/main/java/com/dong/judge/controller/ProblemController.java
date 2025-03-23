package com.dong.judge.controller;

import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.dto.code.TestCaseResult;
import com.dong.judge.model.dto.problem.ProblemDTO;
import com.dong.judge.model.dto.problem.ProblemQueryDTO;
import com.dong.judge.model.dto.problem.TestCaseDTO;
import com.dong.judge.model.enums.DifficultyLevel;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.PageResult;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.ProblemService;
import com.dong.judge.service.TestGroupService;
import com.dong.judge.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/problem")
@Tag(name = "题目管理", description = "题目的创建、更新、查询和删除")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;
    
    @Autowired
    private TestGroupService testGroupService;

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
    public Result<ProblemDTO> getProblemById(
            @PathVariable("id") @Parameter(description = "题目ID") String id) {
        try {
            Problem problem = problemService.getProblemById(id);
            ProblemDTO problemDTO = ProblemDTO.fromProblem(problem);
            return Result.success(problemDTO);
        } catch (Exception e) {
            log.error("获取题目详情失败", e);
            return Result.error("获取题目详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有题目列表
     *
     * @return 题目列表
     */
    @GetMapping
    @Operation(summary = "获取所有题目列表", description = "获取系统中的所有题目")
    public Result<List<ProblemDTO>> getAllProblems() {
        try {
            List<Problem> problems = problemService.getAllProblems();
            List<ProblemDTO> problemDTOs = problems.stream()
                    .map(ProblemDTO::fromProblem)
                    .collect(Collectors.toList());
            return Result.success(problemDTOs);
        } catch (Exception e) {
            log.error("获取题目列表失败", e);
            return Result.error("获取题目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页获取题目列表（支持多条件查询）
     *
     * @param queryDTO 查询参数DTO
     * @return 分页题目列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页获取题目列表", description = "分页获取系统中的题目，支持按难度、关键词和多标签筛选")
    public Result<PageResult<ProblemDTO>> getProblemsPage(
            @RequestBody @Valid ProblemQueryDTO queryDTO) {
        try {
            // 创建分页请求（Spring Data页码从0开始，需要减1）
            PageRequest pageRequest = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 查询分页数据（使用多条件查询）
            Page<Problem> problemPage = problemService.searchProblemsWithConditions(pageRequest, queryDTO.getDifficulty(), queryDTO.getKeyword(), queryDTO.getTags());
            
            // 转换为DTO
            List<ProblemDTO> problemDTOs = problemPage.getContent().stream()
                    .map(ProblemDTO::fromProblem)
                    .collect(Collectors.toList());
            
            // 创建分页结果
            PageResult<ProblemDTO> pageResult = PageResult.fromPage(problemPage, problemDTOs);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("分页获取题目列表失败", e);
            return Result.error("分页获取题目列表失败: " + e.getMessage());
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
    public Result<List<ProblemDTO>> searchProblems(
            @RequestParam("keyword") @Parameter(description = "搜索关键词") String keyword) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<Problem> problems = problemService.searchProblems(userId, keyword);
            List<ProblemDTO> problemDTOs = problems.stream()
                    .map(ProblemDTO::fromProblem)
                    .collect(Collectors.toList());
            return Result.success(problemDTOs);
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
    public Result<List<ProblemDTO>> getProblemsByDifficulty(
            @PathVariable("level") @Parameter(description = "难度级别") Integer level) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            // 验证难度级别
            validateDifficulty(level);
            
            List<Problem> problems = problemService.getProblemsByDifficulty(userId, level);
            List<ProblemDTO> problemDTOs = problems.stream()
                    .map(ProblemDTO::fromProblem)
                    .collect(Collectors.toList());
            return Result.success(problemDTOs);
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
    public Result<List<ProblemDTO>> getProblemsByTag(
            @PathVariable("tag") @Parameter(description = "标签") String tag) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<Problem> problems = problemService.getProblemsByTag(userId, tag);
            List<ProblemDTO> problemDTOs = problems.stream()
                    .map(ProblemDTO::fromProblem)
                    .collect(Collectors.toList());
            return Result.success(problemDTOs);
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
     * 获取题目的测试用例
     *
     * @param id 题目ID
     * @return 测试用例列表（仅返回前两个）
     */
    @GetMapping("/{id}/test-cases")
    @Operation(summary = "获取题目测试用例", description = "根据题目ID获取关联测试集的前两个测试用例")
    public Result<List<TestCaseDTO>> getProblemTestCases(
            @PathVariable("id") @Parameter(description = "题目ID") String id) {
        try {
            // 获取题目信息
            Problem problem = problemService.getProblemById(id);
            
            // 检查题目是否关联了测试集
            if (problem.getTestGroupId() == null || problem.getTestGroupId().isEmpty()) {
                return Result.error("该题目未关联测试集");
            }
            
            // 获取测试集信息
            TestGroup testGroup = testGroupService.getTestGroupById(problem.getTestGroupId());
            
            // 检查测试集是否有测试用例
            if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
                return Result.error("该题目的测试集没有测试用例");
            }
            
            // 获取测试用例结果（如果有）
            List<TestCaseResult> testCaseResults = testGroup.getTestCaseResults();
            
            // 创建返回结果列表
            List<TestCaseDTO> testCaseDTOs = new ArrayList<>();
            
            // 获取前两个测试用例（或者全部，如果少于两个）
            int count = Math.min(2, testGroup.getTestCases().size());
            for (int i = 0; i < count; i++) {
                TestCase testCase = testGroup.getTestCases().get(i);
                String expectedOutput = "";
                
                // 如果有对应的测试结果，获取预期输出
                if (testCaseResults != null && !testCaseResults.isEmpty() && i < testCaseResults.size()) {
                    expectedOutput = testCaseResults.get(i).getStdout();
                }
                
                // 创建DTO并添加到列表
                TestCaseDTO testCaseDTO = TestCaseDTO.builder()
                        .id(testCase.getId())
                        .input(testCase.getInput())
                        .expectedOutput(expectedOutput)
                        .build();
                
                testCaseDTOs.add(testCaseDTO);
            }
            
            return Result.success(testCaseDTOs);
        } catch (Exception e) {
            log.error("获取题目测试用例失败", e);
            return Result.error("获取题目测试用例失败: " + e.getMessage());
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