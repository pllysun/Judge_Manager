package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.TestGroupRepository;
import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.dto.code.TestCaseResult;
import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.judge.TestGroupResult;
import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
import com.dong.judge.model.vo.sandbox.CompileResult;
import com.dong.judge.model.vo.sandbox.RunResult;
import com.dong.judge.service.SandboxService;
import com.dong.judge.service.TestGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 测试集服务实现类
 */
@Service
@Slf4j
public class TestGroupServiceImpl implements TestGroupService {

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private SandboxService sandboxService;

    @Override
    public TestGroupResult createTestGroup(TestGroup testGroup, String userId) {
        // 设置创建者ID和创建时间
        testGroup.setCreatorId(userId);
        LocalDateTime now = LocalDateTime.now();
        testGroup.setCreatedAt(now);
        testGroup.setUpdatedAt(now);

        // 保存测试集
        TestGroup savedTestGroup = testGroupRepository.save(testGroup);

        // 执行测试集并返回结果
        return executeTestGroup(savedTestGroup);
    }

    @Override
    public TestGroupResult updateTestGroup(TestGroup testGroup, String userId) {
        // 获取原测试集
        TestGroup existingTestGroup = testGroupRepository.findById(testGroup.getId())
                .orElseThrow(() -> new RuntimeException("测试集不存在"));

        // 验证权限
        if (!existingTestGroup.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限修改此测试集");
        }

        // 更新测试集信息
        existingTestGroup.setUpdatedAt(LocalDateTime.now());

        // 如果代码或语言有更新，则更新并重新执行所有测试用例
        boolean needRerunAll = false;
        if (testGroup.getCode() != null && !testGroup.getCode().equals(existingTestGroup.getCode())) {
            existingTestGroup.setCode(testGroup.getCode());
            needRerunAll = true;
        }
        if (testGroup.getLanguage() != null && !testGroup.getLanguage().equals(existingTestGroup.getLanguage())) {
            existingTestGroup.setLanguage(testGroup.getLanguage());
            needRerunAll = true;
        }

        // 更新测试用例
        if (testGroup.getTestCases() != null) {
            existingTestGroup.setTestCases(testGroup.getTestCases());
        }

        // 保存更新后的测试集
        TestGroup updatedTestGroup = testGroupRepository.save(existingTestGroup);

        // 执行测试集并返回结果
        return executeTestGroup(updatedTestGroup);
    }

    @Override
    public TestGroup getTestGroupById(String id) {
        return testGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("测试集不存在"));
    }

    @Override
    public List<TestGroup> getUserTestGroups(String userId) {
        return testGroupRepository.findByCreatorIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public boolean deleteTestGroup(String id, String userId) {
        TestGroup testGroup = testGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("测试集不存在"));

        // 验证权限
        if (!testGroup.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限删除此测试集");
        }

        testGroupRepository.deleteById(id);
        return true;
    }

    @Override
    public List<TestGroup> searchTestGroups(String userId, String keyword) {
        return testGroupRepository.findByCreatorIdOrderByCreatedAtDesc(userId, keyword);
    }

    /**
     * 执行测试集
     *
     * @param testGroup 测试集
     * @return 测试集执行结果
     */
    private TestGroupResult executeTestGroup(TestGroup testGroup) {
        // 1. 准备结果对象
        TestGroupResult result = TestGroupResult.builder()
                .id(testGroup.getId())
                .build();

        // 如果没有测试用例，直接返回空结果
        if (testGroup.getTestCases() == null || testGroup.getTestCases().isEmpty()) {
            result.setTestCaseResults(new ArrayList<>());
            result.calculateStatistics();
            // 保存测试集（即使没有测试用例）
            testGroupRepository.save(testGroup);
            return result;
        }

        // 2. 编译代码（如果需要）
        String fileId = null;
        CompileResult compileResult = null;
        try {
            CompileRequest compileRequest = CompileRequest.builder()
                    .code(testGroup.getCode())
                    .language(testGroup.getLanguage())
                    .build();

            compileResult = sandboxService.compileCode(compileRequest);

            // 检查编译结果
            if (!compileResult.isSuccess()) {
                // 编译失败，抛出异常而不是返回结果
                throw new RuntimeException("编译失败: " + compileResult.getErrorMessage());
            }

            fileId = compileResult.getFileId();
        } catch (Exception e) {
            log.error("编译代码失败", e);
            // 编译服务异常也抛出异常，而不是返回结果
            throw new RuntimeException("编译服务异常: " + e.getMessage(), e);
        }

        // 3. 使用虚拟线程并行执行所有测试用例
        List<TestCaseResult> testCaseResults = new ArrayList<>();
        try {
            // 使用Java 21的虚拟线程执行器
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<TestCaseResult>> futures = new ArrayList<>();

                // 提交所有测试用例任务
                for (TestCase testCase : testGroup.getTestCases()) {
                    String finalFileId = fileId;
                    futures.add(executor.submit(() -> executeTestCase(testGroup, testCase, finalFileId)));
                }

                // 收集所有测试结果
                for (Future<TestCaseResult> future : futures) {
                    try {
                        testCaseResults.add(future.get());
                    } catch (Exception e) {
                        log.error("执行测试用例失败", e);
                        testCaseResults.add(createErrorTestCaseResult(testGroup.getTestCases().get(0), "执行异常: " + e.getMessage()));
                    }
                }
            }
        } finally {
            // 4. 清理资源
            if (fileId != null) {
                try {
                    sandboxService.deleteFile(fileId);
                } catch (Exception e) {
                    log.error("删除编译文件失败: {}", fileId, e);
                }
            }
        }

        // 5. 设置结果并计算统计信息
        result.setTestCaseResults(testCaseResults);
        result.calculateStatistics();
        
        // 6. 将测试结果保存到TestGroup对象中
        testGroup.setTestCaseResults(result.getTestCaseResults());
        testGroup.setTotalCount(result.getTotalCount());
        testGroup.setPassedCount(result.getPassedCount());
        testGroup.setTotalTime(result.getTotalTime());
        testGroup.setAvgTime(result.getAvgTime());
        testGroup.setTotalMemory(result.getTotalMemory());
        testGroup.setAvgMemory(result.getAvgMemory());
        testGroup.setTotalRunTime(result.getTotalRunTime());
        testGroup.setAvgRunTime(result.getAvgRunTime());
        testGroup.setCompileError(result.getCompileError());
        testGroup.setAllPassed(result.isAllPassed());
        
        // 7. 保存测试集结果到数据库
        testGroupRepository.save(testGroup);
        
        return result;
    }

    /**
     * 执行单个测试用例
     *
     * @param testGroup 测试集
     * @param testCase  测试用例
     * @param fileId    编译后的文件ID（如果有）
     * @return 测试用例结果
     */
    private TestCaseResult executeTestCase(TestGroup testGroup, TestCase testCase, String fileId) {
        try {
            // 创建运行请求
            RunRequest runRequest = RunRequest.builder()
                    .code(testGroup.getCode())
                    .language(testGroup.getLanguage())
                    .input(testCase.getInput())
                    .fileId(fileId)
                    .build();

            // 运行代码
            RunResult runResult = sandboxService.runCode(runRequest);
            
            // 检查运行结果是否有错误
            if (!runResult.isSuccess()) {
                // 如果运行失败，直接返回错误结果
                return TestCaseResult.builder()
                        .id(testCase.getId())
                        .time(0L)
                        .memory(0L)
                        .runTime(0L)
                        .stdout("Runtime Error: " + runResult.getErrorMessage())
                        .build();
            }

            // 构建测试用例结果，只保留必要字段
            return TestCaseResult.builder()
                    .id(testCase.getId())
                    .time(runResult.getTime())
                    .memory(runResult.getMemory())
                    .runTime(runResult.getRunTime())
                    .stdout(runResult.getStdout())
                    .build();
        } catch (Exception e) {
            log.error("执行测试用例失败: {}", testCase.getId(), e);
            return createErrorTestCaseResult(testCase, "执行异常: " + e.getMessage());
        }
    }

    /**
     * 创建错误的测试用例结果
     *
     * @param testCase     测试用例
     * @param errorMessage 错误信息
     * @return 测试用例结果
     */
    private TestCaseResult createErrorTestCaseResult(TestCase testCase, String errorMessage) {
        return TestCaseResult.builder()
                .id(testCase.getId())
                .time(0L)
                .memory(0L)
                .runTime(0L)
                .stdout(errorMessage)
                .build();
    }
}
