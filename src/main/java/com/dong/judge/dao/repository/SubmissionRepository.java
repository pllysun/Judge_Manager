package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.Submission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findByProblemId(String problemId);
    List<Submission> findByUserId(String userId);
    List<Submission> findByUserIdAndProblemId(String userId, String problemId);
}