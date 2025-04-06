package com.dong.judge.service;

import com.dong.judge.model.dto.code.TestCaseSetResult;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.SubmissionStatistics;

import java.util.List;

public interface ProblemStatisticsService {
    /**
     * 更新题目统计数据
     *
     * @param problem 题目
     * @param result 提交结果
     * @return 更新后的题目
     */
    Problem updateProblemStatistics(Problem problem, TestCaseSetResult result);

    /**
     * 保存提交统计信息
     *
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param result 提交结果
     */
    void saveSubmissionStatistics(String userId, String problemId, TestCaseSetResult result);

    /**
     * 获取用户的题目提交统计
     *
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 提交统计列表
     */
    List<SubmissionStatistics> getUserProblemStatistics(String userId, String problemId);

    /**
     * 获取题目的执行时间排名
     *
     * @param problemId 题目ID
     * @return 时间排名列表
     */
    List<SubmissionStatistics> getProblemTimeRanking(String problemId);

    /**
     * 获取题目的内存使用排名
     *
     * @param problemId 题目ID
     * @return 内存排名列表
     */
    List<SubmissionStatistics> getProblemMemoryRanking(String problemId);
} 