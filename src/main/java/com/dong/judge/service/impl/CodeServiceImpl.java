package com.dong.judge.service.impl;

import com.dong.judge.config.SandboxConfig;
import com.dong.judge.model.dto.code.*;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.enums.ExecutionStatus;
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
                .id("0")
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
                .id(testGroup.getId() != null ? testGroup.getId() : null)
                .name(testGroup.getCode() != null ? "题目测试集" : null)
                .description("题目 " + problem.getTitle() + " 的测试集")
                .build();
        
        // 获取测试用例
        List<TestCase> testCases = testGroup.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            throw new IllegalArgumentException("题目测试集没有测试用例: " + request.getProblemId());
        }
        
        // 获取预期输出结果
        List<TestCaseResult> expectedResults = testGroup.getTestCaseResults();
        
        // 如果存在预期输出结果，将其合并到测试用例中
        if (expectedResults != null && !expectedResults.isEmpty()) {
            // 确保测试用例数量与预期结果数量一致
            if (testCases.size() == expectedResults.size()) {
                // 创建新的测试用例列表，包含预期输出
                List<TestCase> enrichedTestCases = new ArrayList<>();
                for (int i = 0; i < testCases.size(); i++) {
                    TestCase testCase = testCases.get(i);
                    TestCaseResult expectedResult = expectedResults.get(i);
                    
                    // 创建包含预期输出的新测试用例
                    TestCase enrichedTestCase = TestCase.builder()
                            .id(testCase.getId())
                            .input(testCase.getInput())
                            .output(expectedResult.getStdout()) // 使用标准输出作为预期输出
                            .build();
                    
                    enrichedTestCases.add(enrichedTestCase);
                }
                testCases = enrichedTestCases;
            } else {
                log.warn("测试用例数量({})与预期结果数量({})不一致，无法合并预期输出", 
                        testCases.size(), expectedResults.size());
            }
        } else {
            log.warn("题目测试集没有预期输出结果: {}", request.getProblemId());
        }
        
        testCaseSet.setTestCases(testCases);

        // 4. 验证语言并编译
        SandboxConfig.LanguageConfig langConfig = validateLanguage(request.getLanguage());
        CompilationResult compilationResult = compileIfNeeded(langConfig, request.getCode(), request.getLanguage());

        if (compilationResult.hasError()) {
            TestCaseSetResult result = createCompileErrorResult(testCaseSet, compilationResult.error());
            Submission submission = saveSubmission(request, userId, result);
            if (submission != null) {
                result.setSubmissionId(submission.getId()); // 设置提交ID
            }
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

        // 7. 保存提交记录并获取提交ID
        Submission submission = saveSubmission(request, userId, result);
        if (submission != null) {
            result.setSubmissionId(submission.getId()); // 设置提交ID
        }

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
    private TestCaseResult executeTestCase(String code, String language, TestCase testCase, String fileId) {
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
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getOutput())
                        .stdout(runResult.getErrorMessage())
                        .build();
            }

            // 计算单位转换
            // 纳秒转毫秒 (1毫秒 = 1000000纳秒)
            long time = runResult.getTime();
            Double timeInMs = time / 1000000.0;
            // 字节转MB (1MB = 1024*1024字节)
            long memory = runResult.getMemory();
            Double memoryInMB = memory / (1024.0 * 1024.0);
            
            // 获取用户输出和预期输出，进行规范化比较
            String userOutput = normalizeOutput(runResult.getStdout());
            String expectedOutput = testCase.getOutput() != null ? normalizeOutput(testCase.getOutput()) : "";
            
            // 判断输出是否与预期输出相符
            String status;
            String displayOutput = runResult.getStdout();
            
            if (testCase.getOutput() != null && !testCase.getOutput().isEmpty()) {
                if (!userOutput.equals(expectedOutput)) {
                    status = ExecutionStatus.WRONG_ANSWER.getCode();
                } else {
                    status = ExecutionStatus.ACCEPTED.getCode();
                }
            } else {
                // 如果没有预期输出，则使用沙盒返回的状态
                status = runResult.getStatus();
            }
            
            // 构建测试用例结果，添加单位转换字段
            return TestCaseResult.builder()
                    .id(testCase.getId())
                    .status(status)
                    .time(time)
                    .timeInMs(timeInMs)
                    .memory(memory)
                    .memoryInMB(memoryInMB)
                    .runTime(runResult.getRunTime())
                    .input(testCase.getInput())
                    .expectedOutput(testCase.getOutput())
                    .stdout(displayOutput)
                    .build();
        } catch (Exception e) {
            log.error("执行测试用例失败: {}", testCase.getId(), e);
            return createErrorTestCaseResult(testCase, "执行异常: " + e.getMessage());
        }
    }

    /**
     * 规范化输出字符串，用于比较用户输出和预期输出
     */
    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        
        // 1. 统一换行符（Windows \r\n -> \n）
        String normalized = output.replace("\r\n", "\n");
        
        // 2. 移除末尾空白字符
        normalized = normalized.trim();
        
        // 3. 规范化每行的空白字符（移除每行末尾的空白）
        if (normalized.contains("\n")) {
            String[] lines = normalized.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                sb.append(lines[i].trim());
                if (i < lines.length - 1) {
                    sb.append("\n");
                }
            }
            normalized = sb.toString();
        }
        
        return normalized;
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
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getOutput())
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
                .input(testCase.getInput())
                .expectedOutput(testCase.getOutput())
                .stdout("Runtime Error: " + errorMessage)
                .build();
    }

    /**
     * 保存提交记录并更新题目统计数据
     *
     * @param request 代码提交请求
     * @param userId 用户ID
     * @param result 测试结果
     * @return 保存后的提交记录，包含生成的ID
     */
    private Submission saveSubmission(CodeSubmitRequest request, String userId, TestCaseSetResult result) {
        try {
            Problem problem = problemService.getProblemById(request.getProblemId());
            if (problem == null) {
                throw new IllegalArgumentException("题目不存在: " + request.getProblemId());
            }

            // 确定提交状态和构建提交记录
            String status = determineStatus(result);
            TestCaseResult failedTestCase = result.getFirstFailedTestCase();

            Submission.SubmissionBuilder builder = Submission.builder()
                    .userId(userId)
                    .problemId(request.getProblemId())
                    .problemNumber(problem.getProblemId())
                    .problemTitle(problem.getTitle())
                    .code(request.getCode())
                    .language(request.getLanguage())
                    .status(status)
                    .passedCount(result.getPassedCount())
                    .totalCount(result.getTotalCount())
                    .executionTime(result.getAvgTime() != null ? result.getAvgTime() : 0)
                    .memoryUsed(result.getAvgMemory() != null ? result.getAvgMemory() : 0)
                    .compileError(result.getCompileError())
                    .submissionTime(LocalDateTime.now());

            // 如果有失败的测试用例，添加相关信息
            if (failedTestCase != null) {
                builder.firstInput(failedTestCase.getInput())
                       .firstExpectedOutput(failedTestCase.getExpectedOutput())
                       .firstOutput(failedTestCase.getStdout());
            }

            Submission submission = submissionRepository.save(builder.build());

            // 更新题目统计数据
            updateProblemStatistics(request.getProblemId(), result);

            return submission;
        } catch (Exception e) {
            log.error("保存提交记录失败", e);
            return null;
        }
    }

    /**
     * 确定提交状态
     *
     * @param result 测试结果
     * @return 状态码
     */
    private String determineStatus(TestCaseSetResult result) {
        // 1. 判断是否有编译错误
        if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
            return ExecutionStatus.COMPILE_ERROR.getCode();
        }
        // 2. 判断是否全部通过
        else if (result.isAllPassed()) {
            return ExecutionStatus.ACCEPTED.getCode();
        }
        // 3. 判断具体的错误类型
        else {
            // 获取第一个失败的测试用例
            TestCaseResult failedTestCase = result.getFirstFailedTestCase();
            return failedTestCase != null
                    ? failedTestCase.getStatus()
                    : ExecutionStatus.WRONG_ANSWER.getCode();
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
            if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
                // 编译错误
                problem.setCompileErrorCount(problem.getCompileErrorCount() + 1);
            } else if (result.isAllPassed()) {
                // 全部通过
                problem.setAcceptedCount(problem.getAcceptedCount() + 1);
            } else {
                // 统计具体的错误类型
                boolean hasTimeExceeded = false;
                boolean hasMemoryExceeded = false;
                boolean hasWrongAnswer = false;
                
                for (TestCaseResult testResult : result.getTestCaseResults()) {
                    String status = testResult.getStatus();
                    if (ExecutionStatus.TIME_LIMIT_EXCEEDED.getCode().equals(status)) {
                        hasTimeExceeded = true;
                        break;
                    } else if (ExecutionStatus.MEMORY_LIMIT_EXCEEDED.getCode().equals(status)) {
                        hasMemoryExceeded = true;
                        break;
                    } else if (!ExecutionStatus.ACCEPTED.getCode().equals(status)) {
                        hasWrongAnswer = true;
                        // 不立即break，因为时间超限和内存超限优先级更高
                    }
                }
                
                // 按优先级更新统计数据：时间超限 > 内存超限 > 其他错误
                if (hasTimeExceeded) {
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

