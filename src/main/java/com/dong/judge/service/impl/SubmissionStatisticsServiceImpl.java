package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.SubmissionStatisticsRepository;
import com.dong.judge.model.pojo.judge.SubmissionStatistics;
import com.dong.judge.service.SubmissionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionStatisticsServiceImpl implements SubmissionStatisticsService {
    
    private final SubmissionStatisticsRepository submissionStatisticsRepository;
    
    @Override
    public void saveStatistics(SubmissionStatistics statistics) {
        submissionStatisticsRepository.save(statistics);
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