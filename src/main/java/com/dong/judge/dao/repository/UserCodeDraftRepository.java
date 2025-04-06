package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.UserCodeDraft;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCodeDraftRepository extends MongoRepository<UserCodeDraft, String> {
    Optional<UserCodeDraft> findByUserIdAndProblemId(String userId, String problemId);
    void deleteByUserIdAndProblemId(String userId, String problemId);
} 