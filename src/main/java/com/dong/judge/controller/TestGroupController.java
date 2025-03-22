package com.dong.judge.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.Result;
import com.dong.judge.model.vo.judge.TestGroupResult;
import com.dong.judge.service.TestGroupService;
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
 * 测试集控制器
 */
@RestController
@RequestMapping("/api/test-groups")
@Tag(name = "测试集管理", description = "测试集的创建、更新、查询和删除")
@Slf4j
public class TestGroupController {

    @Autowired
    private TestGroupService testGroupService;

    /**
     * 创建测试集
     *
     * @param testGroup 测试集信息
     * @return 测试集执行结果
     */
    @PostMapping
    @Operation(summary = "创建测试集", description = "创建一个新的测试集并执行测试")
    public Result<TestGroupResult> createTestGroup(@RequestBody @Valid TestGroup testGroup) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            TestGroupResult result = testGroupService.createTestGroup(testGroup, userId);
            return Result.success("测试集创建成功", result);
        } catch (Exception e) {
            log.error("创建测试集失败", e);
            return Result.error("创建测试集失败: " + e.getMessage());
        }
    }

    /**
     * 更新测试集
     *
     * @param id 测试集ID
     * @param testGroup 测试集信息
     * @return 测试集执行结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新测试集", description = "更新测试集信息并重新执行测试")
    public Result<TestGroupResult> updateTestGroup(
            @PathVariable("id") @Parameter(description = "测试集ID") String id,
            @RequestBody @Valid TestGroup testGroup) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            testGroup.setId(id);
            TestGroupResult result = testGroupService.updateTestGroup(testGroup, userId);
            return Result.success("测试集更新成功", result);
        } catch (Exception e) {
            log.error("更新测试集失败", e);
            return Result.error("更新测试集失败: " + e.getMessage());
        }
    }

    /**
     * 获取测试集详情
     *
     * @param id 测试集ID
     * @return 测试集信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取测试集详情", description = "根据ID获取测试集详细信息")
    public Result<TestGroup> getTestGroupById(
            @PathVariable("id") @Parameter(description = "测试集ID") String id) {
        try {
            TestGroup testGroup = testGroupService.getTestGroupById(id);
            return Result.success(testGroup);
        } catch (Exception e) {
            log.error("获取测试集详情失败", e);
            return Result.error("获取测试集详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的测试集列表
     *
     * @return 测试集列表
     */
    @GetMapping
    @Operation(summary = "获取用户的测试集列表", description = "获取当前用户创建的所有测试集")
    public Result<List<TestGroup>> getUserTestGroups() {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<TestGroup> testGroups = testGroupService.getUserTestGroups(userId);
            return Result.success(testGroups);
        } catch (Exception e) {
            log.error("获取用户测试集列表失败", e);
            return Result.error("获取用户测试集列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除测试集
     *
     * @param id 测试集ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除测试集", description = "删除指定ID的测试集")
    public Result<Boolean> deleteTestGroup(
            @PathVariable("id") @Parameter(description = "测试集ID") String id) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            boolean success = testGroupService.deleteTestGroup(id, userId);
            return Result.success("测试集删除成功", success);
        } catch (Exception e) {
            log.error("删除测试集失败", e);
            return Result.error("删除测试集失败: " + e.getMessage());
        }
    }

    /**
     * 搜索测试集
     *
     * @param keyword 关键词
     * @return 测试集列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索测试集", description = "根据关键词搜索测试集")
    public Result<List<TestGroup>> searchTestGroups(
            @RequestParam("keyword") @Parameter(description = "搜索关键词") String keyword) {
        // 从会话中获取用户ID
        String userId = UserUtil.getUserId();
        
        try {
            List<TestGroup> testGroups = testGroupService.searchTestGroups(userId, keyword);
            return Result.success(testGroups);
        } catch (Exception e) {
            log.error("搜索测试集失败", e);
            return Result.error("搜索测试集失败: " + e.getMessage());
        }
    }
}