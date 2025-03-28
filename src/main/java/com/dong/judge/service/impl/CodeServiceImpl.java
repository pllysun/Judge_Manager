package com.dong.judge.service.impl;

import com.dong.judge.config.SandboxConfig;
import com.dong.judge.model.dto.code.*;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.Submission;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.model.vo.sandbox.CompileResult;
import com.dong.judge.model.vo.sandbox.RunResult;
import com.dong.judge.service.CodeService;
import com.dong.judge.service.ProblemService;
import com.dong.judge.service.SandboxService;
import com.dong.judge.service.TestGroupService;
import com.dong.judge.dao.repository.SubmissionRepository;
import lombok.Getter;
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
    private final ProblemService problemService;
    private final TestGroupService testGroupService;
    private final SubmissionRepository submissionRepository;
    private final SandboxConfig sandboxConfig;

    @Override
    public TestCaseSetResult runCode(CodeRunRequest request) {
        log.info("运行代码: language={}", request.getLanguage());

        // 1. 判断语言是否支持
        SandboxConfig.LanguageConfig langConfig = validateLanguage(request.getLanguage());

        // 2. 创建单个测试用例
        TestCase testCase = TestCase.builder()
                .id(1L)
                .input(request.getInput())
                .build();

        // 3. 创建测试集
        TestCaseSet testCaseSet = TestCaseSet.builder()
                .id(0L)
                .name("代码运行")
                .description("单次代码运行")
                .testCases(List.of(testCase))
                .build();

        // 4. 编译代码(如果需要)
        CompilationResult compilationResult = compileIfNeeded(langConfig, request.getCode(), request.getLanguage());
        if (compilationResult.hasError()) {
            return createCompileErrorResult(testCaseSet, compilationResult.error());
        }

        // 5. 执行代码
        TestCaseResult testCaseResult = executeTestCase(request.getCode(),request.getLanguage(), testCase, compilationResult.fileId());

        // 6. 构建并返回结果集
        TestCaseSetResult result = TestCaseSetResult.builder()
                .id(testCaseSet.getId())
                .name(testCaseSet.getName())
                .description(testCaseSet.getDescription())
                .testCaseResults(List.of(testCaseResult))
                .compileError(compilationResult.error())
                .build();

        // 计算统计信息
        result.calculateStatistics();

        return result;
    }

    @Override
    public TestCaseSetResult submitCode(CodeSubmitRequest request, String userId) {
        // 1. 获取题目信息
        Problem problem = problemService.getProblemById(request.getProblemId());
        if (problem == null) {
            throw new IllegalArgumentException("题目不存在: " + request.getProblemId());
        }

        // 2. 获取测试集
        TestGroup testGroup = testGroupService.getTestGroupById(problem.getTestGroupId());
        if (testGroup == null) {
            throw new IllegalArgumentException("题目没有测试集: " + request.getProblemId());
        }

        // 3. 转换为测试集对象
        TestCaseSet testCaseSet = TestCaseSet.builder()
                .id(testGroup.getId() != null ? Long.parseLong(testGroup.getId()) : null)
                .testCases(testGroup.getTestCases())
                .build();

        // 4. 验证语言并编译
        SandboxConfig.LanguageConfig langConfig = validateLanguage(request.getLanguage());
        CompilationResult compilationResult = compileIfNeeded(langConfig, request.getCode(), request.getLanguage());

        if (compilationResult.hasError()) {
            TestCaseSetResult result = createCompileErrorResult(testCaseSet, compilationResult.error());
            saveSubmission(request, userId, result);
            return result;
        }

        // 5. 使用虚拟线程并发执行测试用例
        List<TestCaseResult> testCaseResults = executeAllTestCases(request, testCaseSet, compilationResult.fileId());

        // 6. 构建结果集
        TestCaseSetResult result = TestCaseSetResult.builder()
                .id(testCaseSet.getId())
                .name(testCaseSet.getName())
                .description(testCaseSet.getDescription())
                .testCaseResults(testCaseResults)
                .compileError(compilationResult.error())
                .build();

        // 计算统计信息
        result.calculateStatistics();

        // 7. 保存提交记录
        saveSubmission(request, userId, result);

        return result;
    }

    /**
     * 验证语言是否支持
     */
    private SandboxConfig.LanguageConfig validateLanguage(String language) {
        SandboxConfig.LanguageConfig langConfig = sandboxConfig.getLanguageConfig(language);
        if (langConfig == null) {
            throw new IllegalArgumentException("不支持的语言: " + language);
        }
        return langConfig;
    }

    /**
     * 如果需要则编译代码
     */
    private CompilationResult compileIfNeeded(SandboxConfig.LanguageConfig langConfig, String code, String language) {
        if (!langConfig.isNeedCompile()) {
            return new CompilationResult(null, null);
        }

        try {
            CompileRequest compileRequest = CompileRequest.builder()
                    .code(code)
                    .language(language)
                    .build();

            CompileResult compileResult = sandboxService.compileCode(compileRequest);

            if (!compileResult.isSuccess()) {
                return new CompilationResult(null, compileResult.getErrorMessage());
            }

            return new CompilationResult(compileResult.getFileId(), null);
        } catch (Exception e) {
            log.error("编译代码失败", e);
            return new CompilationResult(null, "编译服务异常: " + e.getMessage());
        }
    }


        private record CompilationResult(String fileId, String error) {

        public boolean hasError() {
                return error != null;
            }

        }
    /**
     * 执行所有测试用例
     */
    private List<TestCaseResult> executeAllTestCases(CodeSubmitRequest request, TestCaseSet testCaseSet, String fileId) {
        List<TestCaseResult> testCaseResults = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<TestCaseResult>> futures = new ArrayList<>();

            // 提交所有测试用例执行任务
            for (TestCase testCase : testCaseSet.getTestCases()) {
                futures.add(executor.submit(() -> executeTestCase(request.getCode(),request.getLanguage(), testCase, fileId)));
            }

            // 收集所有执行结果
            for (Future<TestCaseResult> future : futures) {
                try {
                    testCaseResults.add(future.get());
                } catch (Exception e) {
                    log.error("执行测试用例失败", e);
                    testCaseResults.add(createErrorTestCaseResult(testCaseSet.getTestCases().getFirst(), "执行异常: " + e.getMessage()));
                }
            }
        }

        return testCaseResults;
    }


    /**
     * 执行单个测试用例
     */
    private TestCaseResult executeTestCase(String code,String language, TestCase testCase, String fileId) {
        try {
            // 构建运行请求
            RunRequest runRequest = RunRequest.builder()
                    .code(code)
                    .language(language)
                    .input(testCase.getInput())
                    .fileId(fileId)
                    .build();

            // 执行代码
            RunResult runResult = sandboxService.runCode(runRequest);

            // 检查运行结果是否有错误
            if (!runResult.isSuccess()) {
                // 如果运行失败，直接返回错误结果
                return TestCaseResult.builder()
                        .id(testCase.getId())
                        .status(runResult.getStatus())
                        .time(0L)
                        .timeInMs(0.0)
                        .memory(0L)
                        .memoryInMB(0.0)
                        .runTime(0L)
                        .stdout("Runtime Error: " + runResult.getErrorMessage())
                        .build();
            }

            // 计算单位转换
            // 纳秒转毫秒 (1毫秒 = 1000000纳秒)
            long time = runResult.getTime();
            Double timeInMs = time / 1000000.0;
            // 字节转MB (1MB = 1024*1024字节)
            long memory = runResult.getMemory();
            Double memoryInMB = memory / (1024.0 * 1024.0);
            
            // 构建测试用例结果，添加单位转换字段
            return TestCaseResult.builder()
                    .id(testCase.getId())
                    .status(runResult.getStatus())
                    .time(runResult.getTime())
                    .timeInMs(timeInMs)
                    .memory(runResult.getMemory())
                    .memoryInMB(memoryInMB)
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
                        .status("Compile Error")
                        .time(0L)
                        .timeInMs(0.0)
                        .memory(0L)
                        .memoryInMB(0.0)
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
                .status("Runtime Error")
                .time(0L)
                .timeInMs(0.0)
                .memory(0L)
                .memoryInMB(0.0)
                .runTime(0L)
                .stdout("Runtime Error: " + errorMessage)
                .build();
    }

    /**
     * 保存提交记录并更新题目统计数据
     */
    private void saveSubmission(CodeSubmitRequest request, String userId, TestCaseSetResult result) {
        try {
            // 1. 保存提交记录
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
            
            // 2. 更新题目统计数据
            updateProblemStatistics(request.getProblemId(), result);
        } catch (Exception e) {
            log.error("保存提交记录失败", e);
        }
    }
    
    /**
     * 更新题目统计数据
     */
    private void updateProblemStatistics(String problemId, TestCaseSetResult result) {
        try {
            // 1. 获取题目信息
            Problem problem = problemService.getProblemById(problemId);
            if (problem == null) {
                log.error("更新题目统计数据失败：题目不存在 {}", problemId);
                return;
            }
            
            // 2. 更新提交总数
            problem.setSubmissionCount(problem.getSubmissionCount() + 1);
            
            // 3. 根据提交结果更新相应计数
            if (result.getCompileError() != null) {
                // 编译错误
                problem.setCompileErrorCount(problem.getCompileErrorCount() + 1);
            } else {
                // 检查测试用例结果
                boolean hasTimeExceeded = false;
                boolean hasMemoryExceeded = false;
                boolean hasWrongAnswer = false;
                
                for (TestCaseResult testResult : result.getTestCaseResults()) {
                    String status = testResult.getStatus();
                    if (status.contains("Time Limit Exceeded")) {
                        hasTimeExceeded = true;
                    } else if (status.contains("Memory Limit Exceeded")) {
                        hasMemoryExceeded = true;
                    } else if (!status.equals("Accepted") && !status.contains("Compile Error")) {
                        hasWrongAnswer = true;
                    }
                }
                
                // 更新对应计数
                if (result.isAllPassed()) {
                    problem.setAcceptedCount(problem.getAcceptedCount() + 1);
                } else if (hasTimeExceeded) {
                    problem.setTimeExceededCount(problem.getTimeExceededCount() + 1);
                } else if (hasMemoryExceeded) {
                    problem.setMemoryExceededCount(problem.getMemoryExceededCount() + 1);
                } else if (hasWrongAnswer) {
                    problem.setWrongAnswerCount(problem.getWrongAnswerCount() + 1);
                }
            }
            
            // 4. 重新计算通过率
            if (problem.getSubmissionCount() > 0) {
                double acceptedRate = (double) problem.getAcceptedCount() / problem.getSubmissionCount() * 100;
                problem.setAcceptedRate(Math.round(acceptedRate * 100) / 100.0); // 保留两位小数
            }
            
            // 5. 更新题目
            problem.setUpdatedAt(LocalDateTime.now());
            problemService.updateProblem(problem, problem.getCreatorId());
            
            log.info("更新题目统计数据成功: problemId={}, submissionCount={}, acceptedRate={}", 
                    problemId, problem.getSubmissionCount(), problem.getAcceptedRate());
        } catch (Exception e) {
            log.error("更新题目统计数据失败", e);
        }
    }
}

