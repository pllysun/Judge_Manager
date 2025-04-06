package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.SubmissionStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionStatisticsRepository extends MongoRepository<SubmissionStatistics, String> {
    List<SubmissionStatistics> findByUserIdAndProblemIdOrderBySubmissionTimeDesc(String userId, String problemId);
    List<SubmissionStatistics> findByProblemIdOrderByExecutionTimeAsc(String problemId);
    List<SubmissionStatistics> findByProblemIdOrderByMemoryUsedAsc(String problemId);
} 