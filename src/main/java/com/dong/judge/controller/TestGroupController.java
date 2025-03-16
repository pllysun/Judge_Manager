package com.dong.judge.controller;

import com.dong.judge.model.dto.code.StandardCodeRequest;
import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.Result;
import com.dong.judge.service.TestGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/testGroup")
@RequiredArgsConstructor
public class TestGroupController {

    private final TestGroupService testGroupService;
    
    @PostMapping
    @Operation(summary = "创建测试集", description = "创建一个新的测试集")
    public Result<TestGroup> createTestGroup(@RequestBody TestGroup testGroup) {
        // 参数校验
        if (testGroup.getName() == null || testGroup.getName().isEmpty()) {
            return Result.badRequest("测试集名称不能为空");
        }
        
        try {
            TestGroup createdTestGroup = testGroupService.createTestGroup(testGroup);
            return Result.success("创建测试集成功", createdTestGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建测试集失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/withStandardCode")
    @Operation(summary = "使用标准代码创建测试集", description = "创建一个新的测试集，并使用标准代码生成期望输出")
    public Result<TestGroup> createTestGroupWithStandardCode(@RequestBody StandardCodeRequest request) {
        // 参数校验
        if (request.getTestGroup() == null || request.getTestGroup().getName() == null || request.getTestGroup().getName().isEmpty()) {
            return Result.badRequest("测试集名称不能为空");
        }
        if (request.getStandardCode() == null || request.getStandardCode().isEmpty()) {
            return Result.badRequest("标准代码不能为空");
        }
        if (request.getLanguage() == null || request.getLanguage().isEmpty()) {
            return Result.badRequest("编程语言不能为空");
        }
        
        try {
            log.info("使用标准代码创建测试集: {}, 语言: {}", request.getTestGroup().getName(), request.getLanguage());
            TestGroup createdTestGroup = testGroupService.createTestGroupWithStandardCode(request);
            return Result.success("创建测试集成功", createdTestGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("使用标准代码创建测试集失败", e);
            return Result.error("创建测试集失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{testGroupId}")
    @Operation(summary = "更新测试集", description = "根据测试集ID更新测试集信息")
    public Result<TestGroup> updateTestGroup(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId,
            @RequestBody TestGroup testGroup) {
        try {
            TestGroup updatedTestGroup = testGroupService.updateTestGroup(testGroupId, testGroup);
            return Result.success("更新测试集成功", updatedTestGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新测试集失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{testGroupId}/withStandardCode")
    @Operation(summary = "使用标准代码更新测试集", description = "根据测试集ID更新测试集信息，并使用标准代码重新生成期望输出")
    public Result<TestGroup> updateTestGroupWithStandardCode(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId,
            @RequestBody StandardCodeRequest request) {
        // 参数校验
        if (request.getStandardCode() == null || request.getStandardCode().isEmpty()) {
            return Result.badRequest("标准代码不能为空");
        }
        if (request.getLanguage() == null || request.getLanguage().isEmpty()) {
            return Result.badRequest("编程语言不能为空");
        }
        
        try {
            log.info("使用标准代码更新测试集: {}, 语言: {}", testGroupId, request.getLanguage());
            TestGroup updatedTestGroup = testGroupService.updateTestGroupWithStandardCode(testGroupId, request);
            return Result.success("更新测试集成功", updatedTestGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("使用标准代码更新测试集失败", e);
            return Result.error("更新测试集失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{testGroupId}")
    @Operation(summary = "删除测试集", description = "根据测试集ID删除测试集")
    public Result<Boolean> deleteTestGroup(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId) {
        try {
            boolean result = testGroupService.deleteTestGroup(testGroupId);
            return Result.success("删除测试集成功", result);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除测试集失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{testGroupId}")
    @Operation(summary = "获取测试集详情", description = "根据测试集ID获取测试集详情")
    public Result<TestGroup> getTestGroupById(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId) {
        try {
            TestGroup testGroup = testGroupService.getTestGroupById(testGroupId);
            return Result.success(testGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取测试集失败: " + e.getMessage());
        }
    }

    
    @PutMapping("/{testGroupId}/testCase/{testCaseId}")
    @Operation(summary = "更新测试用例", description = "更新测试集中的指定测试用例")
    public Result<TestGroup> updateTestCase(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId,
            @Parameter(description = "测试用例ID") @PathVariable Long testCaseId,
            @RequestBody TestCase testCase) {
        try {
            TestGroup testGroup = testGroupService.updateTestCase(testGroupId, testCaseId, testCase);
            return Result.success("更新测试用例成功", testGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新测试用例失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{testGroupId}/testCase")
    @Operation(summary = "添加测试用例", description = "向测试集中添加新的测试用例")
    public Result<TestGroup> addTestCase(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId,
            @RequestBody TestCase testCase) {
        try {
            TestGroup testGroup = testGroupService.addTestCase(testGroupId, testCase);
            return Result.success("添加测试用例成功", testGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加测试用例失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{testGroupId}/testCase/{testCaseId}")
    @Operation(summary = "删除测试用例", description = "删除测试集中的指定测试用例")
    public Result<TestGroup> deleteTestCase(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId,
            @Parameter(description = "测试用例ID") @PathVariable Long testCaseId) {
        try {
            TestGroup testGroup = testGroupService.deleteTestCase(testGroupId, testCaseId);
            return Result.success("删除测试用例成功", testGroup);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除测试用例失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{testGroupId}/export")
    @Operation(summary = "导出测试集JSON", description = "导出测试集的JSON格式数据")
    public Result<String> exportTestGroupJson(
            @Parameter(description = "测试集ID") @PathVariable String testGroupId) {
        try {
            String json = testGroupService.exportTestGroupJson(testGroupId);
            return Result.success(json);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("导出测试集失败: " + e.getMessage());
        }
    }
}
