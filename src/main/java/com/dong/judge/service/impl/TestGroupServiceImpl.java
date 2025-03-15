package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.ProblemRepository;
import com.dong.judge.dao.repository.TestGroupRepository;
import com.dong.judge.model.dto.code.StandardCodeRequest;
import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
import com.dong.judge.service.SandboxService;
import com.dong.judge.service.TestGroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Slf4j
@Service
@RequiredArgsConstructor
public class TestGroupServiceImpl implements TestGroupService {

    private final TestGroupRepository testGroupRepository;
    private final ProblemRepository problemRepository;
    private final SandboxService sandboxService;

    @Override
    public TestGroup createTestGroup(TestGroup testGroup) {
        // 验证题目是否存在
        if (testGroup.getProblemId() != null) {
            problemRepository.findById(testGroup.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + testGroup.getProblemId()));
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        testGroup.setCreateTime(now);
        testGroup.setUpdateTime(now);
        
        // 初始化测试用例列表
        if (testGroup.getTestCases() == null) {
            testGroup.setTestCases(new ArrayList<>());
        }
        
        // 为测试用例分配ID
        if (testGroup.getTestCases() != null && !testGroup.getTestCases().isEmpty()) {
            long testCaseId = 1;
            for (TestCase testCase : testGroup.getTestCases()) {
                testCase.setId(testCaseId++);
            }
        }
        
        return testGroupRepository.save(testGroup);
    }
    
    @Override
    public TestGroup createTestGroupWithStandardCode(StandardCodeRequest request) {
        TestGroup testGroup = request.getTestGroup();
        String standardCode = request.getStandardCode();
        String language = request.getLanguage();
        
        // 验证参数
        if (testGroup == null) {
            throw new IllegalArgumentException("测试集信息不能为空");
        }
        if (standardCode == null || standardCode.isEmpty()) {
            throw new IllegalArgumentException("标准代码不能为空");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("编程语言不能为空");
        }
        
        // 验证题目是否存在
        if (testGroup.getProblemId() != null) {
            problemRepository.findById(testGroup.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + testGroup.getProblemId()));
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        testGroup.setCreateTime(now);
        testGroup.setUpdateTime(now);
        
        // 初始化测试用例列表
        if (testGroup.getTestCases() == null) {
            testGroup.setTestCases(new ArrayList<>());
        }
        
        // 为测试用例分配ID并使用标准代码生成期望输出
        if (testGroup.getTestCases() != null && !testGroup.getTestCases().isEmpty()) {
            long testCaseId = 1;
            for (TestCase testCase : testGroup.getTestCases()) {
                testCase.setId(testCaseId++);
                
                // 使用标准代码运行测试用例输入，生成期望输出
                if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                    try {
                        CodeExecuteRequest executeRequest = CodeExecuteRequest.builder()
                                .code(standardCode)
                                .language(language)
                                .input(testCase.getInput())
                                .build();
                        
                        CodeExecuteResult result = sandboxService.executeCode(executeRequest);
                        
                        // 检查执行结果
                        if ("Accepted".equals(result.getStatus()) || result.getExitStatus() == 0) {
                            // 使用标准代码的输出作为期望输出
                            testCase.setExpectedOutput(result.getStdout());
                            this.log.info("测试用例 {} 使用标准代码生成期望输出: {}", testCase.getId(), result.getStdout());
                        } else {
                            this.log.warn("测试用例 {} 标准代码执行失败: {}", testCase.getId(), result.getStatus());
                            if (result.getStderr() != null && !result.getStderr().isEmpty()) {
                                this.log.warn("错误信息: {}", result.getStderr());
                            }
                            if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
                                this.log.warn("编译错误: {}", result.getCompileError());
                            }
                        }
                    } catch (Exception e) {
                        this.log.error("执行标准代码生成期望输出时发生错误", e);
                    }
                }
            }
        }
        
        return testGroupRepository.save(testGroup);
    }

    @Override
    public TestGroup updateTestGroup(String testGroupId, TestGroup testGroup) {
        TestGroup existingTestGroup = testGroupRepository.findById(testGroupId)
                .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

        // 更新基本信息
        if (testGroup != null) {
            if (StringUtils.hasText(testGroup.getName())) {
                existingTestGroup.setName(testGroup.getName());
            }
            if (StringUtils.hasText(testGroup.getDescription())) {
                existingTestGroup.setDescription(testGroup.getDescription());
            }

            // 更新题目ID（如果提供了新的题目ID）
            if (testGroup.getProblemId() != null && !testGroup.getProblemId().equals(existingTestGroup.getProblemId())) {
                // 验证新题目是否存在
                problemRepository.findById(testGroup.getProblemId())
                        .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + testGroup.getProblemId()));
                existingTestGroup.setProblemId(testGroup.getProblemId());
            }

            // 更新测试用例（如果提供了新的测试用例列表）
            if (testGroup.getTestCases() != null) {
                // 为新的测试用例分配ID
                long maxId = 0;
                if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
                    maxId = existingTestGroup.getTestCases().stream()
                            .mapToLong(TestCase::getId)
                            .max()
                            .orElse(0);
                }

                for (TestCase testCase : testGroup.getTestCases()) {
                    if (testCase.getId() == null) {
                        testCase.setId(++maxId);
                    }
                }

                existingTestGroup.setTestCases(testGroup.getTestCases());
            }
        }

        // 更新时间
        existingTestGroup.setUpdateTime(LocalDateTime.now());

        return testGroupRepository.save(existingTestGroup);
    }

    @Override
    public TestGroup updateTestGroupWithStandardCode(String testGroupId, StandardCodeRequest request) {
        TestGroup existingTestGroup = testGroupRepository.findById(testGroupId)
                .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

        TestGroup newTestGroup = request.getTestGroup();
        String standardCode = request.getStandardCode();
        String language = request.getLanguage();

        // 验证参数
        if (standardCode == null || standardCode.isEmpty()) {
            throw new IllegalArgumentException("标准代码不能为空");
        }
        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("编程语言不能为空");
        }

        // 更新基本信息
        if (newTestGroup != null) {
            if (StringUtils.hasText(newTestGroup.getName())) {
                existingTestGroup.setName(newTestGroup.getName());
            }
            if (StringUtils.hasText(newTestGroup.getDescription())) {
                existingTestGroup.setDescription(newTestGroup.getDescription());
            }

            // 更新题目ID（如果提供了新的题目ID）
            if (newTestGroup.getProblemId() != null && !newTestGroup.getProblemId().equals(existingTestGroup.getProblemId())) {
                // 验证新题目是否存在
                problemRepository.findById(newTestGroup.getProblemId())
                        .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + newTestGroup.getProblemId()));
                existingTestGroup.setProblemId(newTestGroup.getProblemId());
            }

            // 更新测试用例（如果提供了新的测试用例列表）
            if (newTestGroup.getTestCases() != null) {
                // 为新的测试用例分配ID
                long maxId = 0;
                if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
                    maxId = existingTestGroup.getTestCases().stream()
                            .mapToLong(TestCase::getId)
                            .max()
                            .orElse(0);
                }

                for (TestCase testCase : newTestGroup.getTestCases()) {
                    if (testCase.getId() == null) {
                        testCase.setId(++maxId);
                    }
                }

                existingTestGroup.setTestCases(newTestGroup.getTestCases());
            }
        }

        // 使用标准代码为测试用例生成期望输出
        if (existingTestGroup.getTestCases() != null && !existingTestGroup.getTestCases().isEmpty()) {
            for (TestCase testCase : existingTestGroup.getTestCases()) {
                // 使用标准代码运行测试用例输入，生成期望输出
                if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                    try {
                        CodeExecuteRequest executeRequest = CodeExecuteRequest.builder()
                                .code(standardCode)
                                .language(language)
                                .input(testCase.getInput())
                                .build();

                        CodeExecuteResult result = sandboxService.executeCode(executeRequest);

                        // 检查执行结果
                        if ("Accepted".equals(result.getStatus()) || result.getExitStatus() == 0) {
                            // 使用标准代码的输出作为期望输出
                            testCase.setExpectedOutput(result.getStdout());
                            this.log.info("测试用例 {} 使用标准代码生成期望输出: {}", testCase.getId(), result.getStdout());
                        } else {
                            this.log.warn("测试用例 {} 标准代码执行失败: {}", testCase.getId(), result.getStatus());
                            if (result.getStderr() != null && !result.getStderr().isEmpty()) {
                                this.log.warn("错误信息: {}", result.getStderr());
                            }
                            if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
                                this.log.warn("编译错误: {}", result.getCompileError());
                            }
                        }
                    } catch (Exception e) {
                        this.log.error("执行标准代码生成期望输出时发生错误", e);
                    }
                }
            }
        }

        // 更新时间
        existingTestGroup.setUpdateTime(LocalDateTime.now());

        return testGroupRepository.save(existingTestGroup);
    }


    @Override
    public boolean deleteTestGroup(String testGroupId) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));

        testGroupRepository.delete(testGroup);
        return true;
    }

    @Override
    public TestGroup getTestGroupById(String testGroupId) {
        return testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
    }

    @Override
    public List<TestGroup> getTestGroupsByProblemId(String problemId) {
        // 验证题目是否存在
        problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + problemId));

        return testGroupRepository.findByProblemId(problemId);
    }

    @Override
    public Page<TestGroup> getTestGroupList(int page, int size, String problemId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));

        // 无筛选条件时使用标准分页查询
        if (!StringUtils.hasText(problemId) && !StringUtils.hasText(keyword)) {
            return testGroupRepository.findAll(pageable);
        }

        // 单条件查询
        if (StringUtils.hasText(problemId) && !StringUtils.hasText(keyword)) {
            return testGroupRepository.findByProblemId(problemId, pageable);
        } else if (!StringUtils.hasText(problemId) && StringUtils.hasText(keyword)) {
            return testGroupRepository.findByNameContaining(keyword, pageable);
        }

        // 复合条件查询需要手动实现
        List<TestGroup> allTestGroups = testGroupRepository.findByProblemId(problemId);
        List<TestGroup> filteredTestGroups = allTestGroups.stream()
            .filter(tg -> tg.getName() != null && tg.getName().contains(keyword))
            .collect(Collectors.toList());

        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredTestGroups.size());

        // 如果start超出了列表大小，返回空页
        if (start >= filteredTestGroups.size()) {
            return Page.empty(pageable);
        }

        return new org.springframework.data.domain.PageImpl<>(filteredTestGroups.subList(start, end), pageable, filteredTestGroups.size());
    }




    
    @Override
    public TestGroup updateTestCase(String testGroupId, Long testCaseId, TestCase testCase) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("测试集中没有测试用例");
        }
        
        // 查找并更新测试用例
        boolean found = false;
        for (int i = 0; i < testGroup.getTestCases().size(); i++) {
            if (testGroup.getTestCases().get(i).getId().equals(testCaseId)) {
                // 保留原ID
                testCase.setId(testCaseId);
                testGroup.getTestCases().set(i, testCase);
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new IllegalArgumentException("测试用例不存在: " + testCaseId);
        }
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }

    @Override
    public TestGroup addTestCase(String testGroupId, TestCase testCase) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        // 初始化测试用例列表（如果为空）
        if (testGroup.getTestCases() == null) {
            testGroup.setTestCases(new ArrayList<>());
        }
        
        // 为新测试用例分配ID
        long maxId = 0;
        if (!testGroup.getTestCases().isEmpty()) {
            maxId = testGroup.getTestCases().stream()
                .mapToLong(TestCase::getId)
                .max()
                .orElse(0);
        }
        testCase.setId(maxId + 1);
        
        // 添加测试用例
        testGroup.getTestCases().add(testCase);
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }

    @Override
    public TestGroup deleteTestCase(String testGroupId, Long testCaseId) {
        TestGroup testGroup = testGroupRepository.findById(testGroupId)
            .orElseThrow(() -> new IllegalArgumentException("测试集不存在: " + testGroupId));
        
        if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
            throw new IllegalArgumentException("测试集中没有测试用例");
        }
        
        // 查找并删除测试用例
        boolean removed = testGroup.getTestCases().removeIf(tc -> tc.getId().equals(testCaseId));
        
        if (!removed) {
            throw new IllegalArgumentException("测试用例不存在: " + testCaseId);
        }
        
        // 更新时间
        testGroup.setUpdateTime(LocalDateTime.now());
        
        return testGroupRepository.save(testGroup);
    }
    
   @Override
        public String exportTestGroupJson(String testGroupId) {
            TestGroup testGroup = getTestGroupById(testGroupId);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // 配置日期时间格式
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                // 格式化输出，方便阅读
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                return objectMapper.writeValueAsString(testGroup);
            } catch (JsonProcessingException e) {
                this.log.error("导出测试集JSON失败", e);
                throw new RuntimeException("导出测试集JSON失败: " + e.getMessage());
            }
        }
}