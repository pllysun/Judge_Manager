package com.dong.judge.service;

import com.dong.judge.model.pojo.judge.SubmissionStatistics;

import java.util.List;

public interface SubmissionStatisticsService {
    void saveStatistics(SubmissionStatistics statistics);
    List<SubmissionStatistics> getUserProblemStatistics(String userId, String problemId);
    List<SubmissionStatistics> getProblemTimeRanking(String problemId);
    List<SubmissionStatistics> getProblemMemoryRanking(String problemId);
} 