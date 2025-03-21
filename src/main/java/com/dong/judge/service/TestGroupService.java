package com.dong.judge.service;

import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.judge.TestGroupResult;

import java.util.List;

/**
 * 测试集服务接口
 */
public interface TestGroupService {
    
    /**
     * 创建测试集
     *
     * @param testGroup 测试集信息
     * @param userId 用户ID
     * @return 测试集执行结果
     */
    TestGroupResult createTestGroup(TestGroup testGroup, String userId);
    
    /**
     * 更新测试集
     *
     * @param testGroup 测试集信息
     * @param userId 用户ID
     * @return 测试集执行结果
     */
    TestGroupResult updateTestGroup(TestGroup testGroup, String userId);
    
    /**
     * 获取测试集详情
     *
     * @param id 测试集ID
     * @return 测试集信息
     */
    TestGroup getTestGroupById(String id);
    
    /**
     * 获取用户的测试集列表
     *
     * @param userId 用户ID
     * @return 测试集列表
     */
    List<TestGroup> getUserTestGroups(String userId);
    
    /**
     * 删除测试集
     *
     * @param id 测试集ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteTestGroup(String id, String userId);
    
    /**
     * 搜索测试集
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 测试集列表
     */
    List<TestGroup> searchTestGroups(String userId, String keyword);
}