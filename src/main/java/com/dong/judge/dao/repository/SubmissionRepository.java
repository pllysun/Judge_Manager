package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.Submission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    /**
     * 根据用户ID和题目ID查询提交记录，按提交时间降序排序
     */
    List<Submission> findByUserIdAndProblemIdOrderBySubmissionTimeDesc(String userId, String problemId);
    
    /**
     * 根据题目ID查询提交记录，按执行时间升序排序
     */
    List<Submission> findByProblemIdOrderByExecutionTimeAsc(String problemId);
    
    /**
     * 根据题目ID查询提交记录，按内存使用升序排序
     */
    List<Submission> findByProblemIdOrderByMemoryUsedAsc(String problemId);
}