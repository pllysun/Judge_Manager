package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.ProblemRepository;
import com.dong.judge.dao.repository.SubmissionStatisticsRepository;
import com.dong.judge.model.dto.code.TestCaseResult;
import com.dong.judge.model.dto.code.TestCaseSetResult;
import com.dong.judge.model.enums.ExecutionStatus;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.SubmissionStatistics;
import com.dong.judge.service.ProblemStatisticsService;
import com.dong.judge.service.SubmissionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemStatisticsServiceImpl implements ProblemStatisticsService {

    private final ProblemRepository problemRepository;
    private final SubmissionStatisticsRepository submissionStatisticsRepository;

    @Override
    public Problem updateProblemStatistics(Problem problem, TestCaseSetResult result) {
        // 更新提交总数
        problem.setSubmissionCount(problem.getSubmissionCount() + 1);
        
        // 根据提交结果更新不同状态的计数
        if (result.getCompileError() != null && !result.getCompileError().isEmpty()) {
            problem.setCompileErrorCount(problem.getCompileErrorCount() + 1);
        } else if (result.isAllPassed()) {
            problem.setAcceptedCount(problem.getAcceptedCount() + 1);
        } else {
            // 获取第一个失败的测试用例状态
            TestCaseResult failedTestCase = result.getFirstFailedTestCase();
            if (failedTestCase != null) {
                ExecutionStatus status = ExecutionStatus.getByCode(failedTestCase.getStatus());
                switch (status) {
                    case TIME_LIMIT_EXCEEDED:
                        problem.setTimeExceededCount(problem.getTimeExceededCount() + 1);
                        break;
                    case MEMORY_LIMIT_EXCEEDED:
                        problem.setMemoryExceededCount(problem.getMemoryExceededCount() + 1);
                        break;
                    case WRONG_ANSWER:
                        problem.setWrongAnswerCount(problem.getWrongAnswerCount() + 1);
                        break;
                }
            }
        }
        
        // 计算通过率：通过数 / (通过数 + 所有失败数) * 100，只保留整数部分
        int totalFailedCount = problem.getWrongAnswerCount() + 
                             problem.getTimeExceededCount() + 
                             problem.getMemoryExceededCount() + 
                             problem.getCompileErrorCount();
        
        int totalAttempts = problem.getAcceptedCount() + totalFailedCount;
        if (totalAttempts > 0) {
            // 直接计算整数百分比，丢弃小数部分
            problem.setAcceptedRate((double) (problem.getAcceptedCount() * 100 / totalAttempts));
        } else {
            problem.setAcceptedRate(0.0);
        }
        
        // 保存更新后的题目数据
        return problemRepository.save(problem);
    }

    @Override
    public void saveSubmissionStatistics(String userId, String problemId, TestCaseSetResult result) {
        if (userId != null) {
            SubmissionStatistics statistics = SubmissionStatistics.builder()
                    .submissionId(result.getId())
                    .userId(userId)
                    .problemId(problemId)
                    .executionTime(result.getAvgTimeInMs() != null ? result.getAvgTimeInMs().longValue() : 0L)
                    .memoryUsed(result.getAvgMemoryInMB() != null ? result.getAvgMemoryInMB().longValue() : 0L)
                    .submissionTime(LocalDateTime.now())
                    .status(result.isAllPassed() ? "ACCEPTED" : "FAILED")
                    .build();
            submissionStatisticsRepository.save(statistics);
        }
    }

    @Override
    public List<SubmissionStatistics> getUserProblemStatistics(String userId, String problemId) {
        return submissionStatisticsRepository.findByUserIdAndProblemIdOrderBySubmissionTimeDesc(userId, problemId);
    }

    @Override
    public List<SubmissionStatistics> getProblemTimeRanking(String problemId) {
        return submissionStatisticsRepository.findByProblemIdOrderByExecutionTimeAsc(problemId);
    }

    @Override
    public List<SubmissionStatistics> getProblemMemoryRanking(String problemId) {
        return submissionStatisticsRepository.findByProblemIdOrderByMemoryUsedAsc(problemId);
    }
} 