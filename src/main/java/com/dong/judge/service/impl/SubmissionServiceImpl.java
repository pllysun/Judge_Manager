package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.SubmissionRepository;
import com.dong.judge.model.dto.code.TestCaseResult;
import com.dong.judge.model.dto.code.TestCaseSetResult;
import com.dong.judge.model.enums.ExecutionStatus;
import com.dong.judge.model.pojo.judge.Submission;
import com.dong.judge.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    
    private final SubmissionRepository submissionRepository;
    
    @Override
    public Submission saveSubmission(String userId, String problemId, String code, String language, TestCaseSetResult result) {
        // 计算平均执行时间和内存使用
        long totalTime = 0;
        long totalMemory = 0;
        int count = 0;
        
        if (result.getTestCaseResults() != null) {
            for (var testCase : result.getTestCaseResults()) {
                if (testCase.getTimeInMs() != null) {
                    totalTime += testCase.getTimeInMs();
                }
                if (testCase.getMemoryInMB() != null) {
                    totalMemory += testCase.getMemoryInMB();
                }
                count++;
            }
        }
        
        long avgTime = count > 0 ? totalTime / count : 0;
        long avgMemory = count > 0 ? totalMemory / count : 0;
        
        Submission submission = Submission.builder()
                .userId(userId)
                .problemId(problemId)
                .code(code)
                .language(language)
                .submissionTime(LocalDateTime.now())
                .status(determineSubmissionStatus(result))
                .passedCount(result.getPassedCount())
                .totalCount(result.getTotalCount())
                .executionTime(avgTime)
                .memoryUsed(avgMemory)
                .compileError(result.getCompileError())
                .passRatio(result.getPassRatio())
                .build();
        
        return submissionRepository.save(submission);
    }
    
    @Override
    public List<Submission> getUserSubmissions(String userId, String problemId) {
        return submissionRepository.findByUserIdAndProblemIdOrderBySubmissionTimeDesc(userId, problemId);
    }
    
    @Override
    public List<Submission> getProblemTimeRanking(String problemId) {
        return submissionRepository.findByProblemIdOrderByExecutionTimeAsc(problemId);
    }
    
    @Override
    public List<Submission> getProblemMemoryRanking(String problemId) {
        return submissionRepository.findByProblemIdOrderByMemoryUsedAsc(problemId);
    }
    
    @Override
    public Submission getSubmissionById(String submissionId) {
        return submissionRepository.findById(submissionId).orElse(null);
    }
    
    /**
     * 根据测试结果确定提交状态
     * 
     * @param result 测试结果
     * @return 状态码
     */
    private String determineSubmissionStatus(TestCaseSetResult result) {
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
            
            if (failedTestCase != null) {
                // 直接使用测试用例的状态
                return failedTestCase.getStatus();
            } else {
                // 如果没有找到失败的测试用例但结果显示未全部通过，使用WRONG_ANSWER
                return ExecutionStatus.WRONG_ANSWER.getCode();
            }
        }
    }
}