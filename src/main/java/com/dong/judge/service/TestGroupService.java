package com.dong.judge.service;

import com.dong.judge.model.dto.code.StandardCodeRequest;
import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.pojo.judge.TestGroup;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TestGroupService {
    
    /**
     * 创建测试集
     * @param testGroup 测试集对象
     * @return 创建后的测试集对象
     */
    TestGroup createTestGroup(TestGroup testGroup);
    
    /**
     * 使用标准代码创建测试集
     * @param request 标准代码请求，包含测试集信息、标准代码和编程语言
     * @return 创建后的测试集对象
     */
    TestGroup createTestGroupWithStandardCode(StandardCodeRequest request);
    
    /**
     * 更新测试集
     * @param testGroupId 测试集ID
     * @param testGroup 测试集对象
     * @return 更新后的测试集对象
     */
    TestGroup updateTestGroup(String testGroupId, TestGroup testGroup);
    
    /**
     * 使用标准代码更新测试集
     * @param testGroupId 测试集ID
     * @param request 标准代码请求，包含测试集信息、标准代码和编程语言
     * @return 更新后的测试集对象
     */
    TestGroup updateTestGroupWithStandardCode(String testGroupId, StandardCodeRequest request);
    
    /**
     * 删除测试集
     * @param testGroupId 测试集ID
     * @return 是否删除成功
     */
    boolean deleteTestGroup(String testGroupId);
    
    /**
     * 根据ID获取测试集
     * @param testGroupId 测试集ID
     * @return 测试集对象
     */
    TestGroup getTestGroupById(String testGroupId);
    
    /**
     * 更新测试用例
     * @param testGroupId 测试集ID
     * @param testCaseId 测试用例ID
     * @param testCase 测试用例对象
     * @return 更新后的测试集对象
     */
    TestGroup updateTestCase(String testGroupId, Long testCaseId, TestCase testCase);
    
    /**
     * 添加测试用例
     * @param testGroupId 测试集ID
     * @param testCase 测试用例对象
     * @return 更新后的测试集对象
     */
    TestGroup addTestCase(String testGroupId, TestCase testCase);
    
    /**
     * 删除测试用例
     * @param testGroupId 测试集ID
     * @param testCaseId 测试用例ID
     * @return 更新后的测试集对象
     */
    TestGroup deleteTestCase(String testGroupId, Long testCaseId);
    
    /**
     * 导出测试集JSON
     * @param testGroupId 测试集ID
     * @return 测试集JSON字符串
     */
    String exportTestGroupJson(String testGroupId);

    /**
     * 获取测试集列表
     * @param testGroupId 测试集ID
     * @return 测试集列表
     */
    List<TestGroup> getTestGroupsById(String testGroupId);
}