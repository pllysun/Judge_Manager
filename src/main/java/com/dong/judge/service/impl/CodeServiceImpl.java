package com.dong.judge.service.impl;

import com.dong.judge.config.SandboxConfig;
import com.dong.judge.model.dto.code.*;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.pojo.judge.Submission;
import com.dong.judge.model.vo.sandbox.CompileResult;
import com.dong.judge.model.vo.sandbox.RunResult;
import com.dong.judge.service.CodeService;
import com.dong.judge.service.SandboxService;
import com.dong.judge.dao.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 代码执行服务实现类
 * <p>
 * 提供OJ风格的代码执行功能，使用虚拟线程并发执行测试用例
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {

    private final SandboxService sandboxService;
//    private final ProblemService problemService;
//    private final TestGroupService testGroupService;
    private final SubmissionRepository submissionRepository;
    private final SandboxConfig sandboxConfig;

    @Override
    public TestCaseSetResult runCode(CodeRunRequest request, String userId) {
//        // 1. 获取题目信息
//        Problem problem = problemService.getProblemById(request.getProblemId());
//        if (problem == null) {
//            throw new IllegalArgumentException("题目不存在: " + request.getProblemId());
//        }
//
//        // 2. 获取测试集
//        List<TestGroup> testGroups = testGroupService.getTestGroupsById(problem.getTestGroupId());
//        if (testGroups == null || testGroups.isEmpty()) {
//            throw new IllegalArgumentException("题目没有测试集: " + request.getProblemId());
//        }
//
//        // 使用第一个测试集
//        TestGroup testGroup = testGroups.getFirst();
//
//        // 3. 转换为测试集对象
//        TestCaseSet testCaseSet = TestCaseSet.builder()
//                .id(testGroup.getId() != null ? Long.parseLong(testGroup.getId()) : null)
//                .name(testGroup.getName())
//                .description(testGroup.getDescription())
//                .testCases(testGroup.getTestCases())
//                .build();
//
//        // 4. 判断语言是否需要编译
//        SandboxConfig.LanguageConfig langConfig = sandboxConfig.getLanguages().get(request.getLanguage());
//        if (langConfig == null) {
//            throw new IllegalArgumentException("不支持的语言: " + request.getLanguage());
//        }
//
//        String fileId = null;
//        String compileError = null;
//
//        // 5. 如果需要编译，先编译代码
//        if (langConfig.isNeedCompile()) {
//            try {
//                CompileRequest compileRequest = CompileRequest.builder()
//                        .code(request.getCode())
//                        .language(request.getLanguage())
//                        .build();
//
//                CompileResult compileResult = sandboxService.compileCode(compileRequest);
//
//                if (!compileResult.isSuccess()) {
//                    // 编译失败，直接返回结果
//                    TestCaseSetResult result = createCompileErrorResult(testCaseSet, compileResult.getErrorMessage());
//
//                    // 保存提交记录
//                    saveSubmission(request, userId, result);
//
//                    return result;
//                }
//
//                fileId = compileResult.getFileId();
//            } catch (Exception e) {
//                log.error("编译代码失败", e);
//                TestCaseSetResult result = createCompileErrorResult(testCaseSet, "编译服务异常: " + e.getMessage());
//
//                // 保存提交记录
//                saveSubmission(request, userId, result);
//
//                return result;
//            }
//        }
//
//        // 6. 使用虚拟线程并发执行测试用例
//        List<TestCaseResult> testCaseResults = new ArrayList<>();
//
//        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
//            List<Future<TestCaseResult>> futures = new ArrayList<>();
//
//            // 提交所有测试用例执行任务
//            for (TestCase testCase : testCaseSet.getTestCases()) {
//                String finalFileId = fileId;
//                futures.add(executor.submit(() -> executeTestCase(request, testCase, finalFileId)));
//            }
//
//            // 收集所有执行结果
//            for (Future<TestCaseResult> future : futures) {
//                try {
//                    testCaseResults.add(future.get());
//                } catch (Exception e) {
//                    log.error("执行测试用例失败", e);
//                    testCaseResults.add(createErrorTestCaseResult(testCaseSet.getTestCases().getFirst(), "执行异常: " + e.getMessage()));
//                }
//            }
//        }
//
//        // 7. 构建并返回结果集
//        TestCaseSetResult result = TestCaseSetResult.builder()
//                .id(testCaseSet.getId())
//                .name(testCaseSet.getName())
//                .description(testCaseSet.getDescription())
//                .testCaseResults(testCaseResults)
//                .compileError(compileError)
//                .build();
//
//        // 计算统计信息
//        result.calculateStatistics();
//
//        // 8. 保存提交记录
//        saveSubmission(request, userId, result);r

        return null;
    }

    @Override
    public TestCaseSetResult submitCode(CodeRunRequest request) {
        return null;
    }

    /**
     * 执行单个测试用例
     */
    private TestCaseResult executeTestCase(CodeRunRequest request, TestCase testCase, String fileId) {
        try {
            // 构建运行请求
            RunRequest runRequest = RunRequest.builder()
                    .code(request.getCode())
                    .language(request.getLanguage())
                    .input(testCase.getInput())
                    .fileId(fileId)
                    .build();

            // 执行代码
            RunResult runResult = sandboxService.runCode(runRequest);

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
     * 创建编译错误的结果集
     */
    private TestCaseSetResult createCompileErrorResult(TestCaseSet testCaseSet, String errorMessage) {
        List<TestCaseResult> testCaseResults = testCaseSet.getTestCases().stream()
                .map(testCase -> TestCaseResult.builder()
                        .id(testCase.getId())
                        .time(0L)
                        .memory(0L)
                        .runTime(0L)
                        .stdout("Compile Error: " + errorMessage)
                        .build())
                .collect(Collectors.toList());

        TestCaseSetResult result = TestCaseSetResult.builder()
                .id(testCaseSet.getId())
                .name(testCaseSet.getName())
                .description(testCaseSet.getDescription())
                .testCaseResults(testCaseResults)
                .compileError(errorMessage)
                .build();

        result.calculateStatistics();
        return result;
    }

    /**
     * 创建错误的测试用例结果
     */
    private TestCaseResult createErrorTestCaseResult(TestCase testCase, String errorMessage) {
        return TestCaseResult.builder()
                .id(testCase.getId())
                .time(0L)
                .memory(0L)
                .runTime(0L)
                .stdout("Runtime Error: " + errorMessage)
                .build();
    }

    /**
     * 保存提交记录
     */
    private void saveSubmission(CodeRunRequest request, String userId, TestCaseSetResult result) {
        try {
            Submission submission = Submission.builder()
                    .userId(userId)
                    .problemId(request.getProblemId())
                    .code(request.getCode())
                    .language(request.getLanguage())
                    .status(result.isAllPassed() ? "Accepted" : "Failed")
                    .passedCount(result.getPassedCount())
                    .totalCount(result.getTotalCount())
                    .executionTime(result.getAvgTime() != null ? result.getAvgTime() : 0)
                    .memoryUsed(result.getAvgMemory() != null ? result.getAvgMemory() : 0)
                    .compileError(result.getCompileError())
                    .submissionTime(LocalDateTime.now())
                    .build();

            submissionRepository.save(submission);
        } catch (Exception e) {
            log.error("保存提交记录失败", e);
        }
    }
}