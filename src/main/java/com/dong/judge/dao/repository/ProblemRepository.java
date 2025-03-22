package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.Problem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目数据访问接口
 */
@Repository
public interface ProblemRepository extends MongoRepository<Problem, String> {
    
    /**
     * 根据创建者ID查找题目
     *
     * @param creatorId 创建者ID
     * @return 题目列表
     */
    List<Problem> findByCreatorIdOrderByCreatedAtDesc(String creatorId);
    
    /**
     * 根据难度级别查找题目
     *
     * @param difficulty 难度级别
     * @return 题目列表
     */
    List<Problem> findByDifficulty(Integer difficulty);
    
    /**
     * 根据标签查找题目
     *
     * @param tag 标签
     * @return 题目列表
     */
    List<Problem> findByTagsContaining(String tag);
    
    /**
     * 根据标题模糊查询题目
     *
     * @param title 标题关键词
     * @return 题目列表
     */
    List<Problem> findByTitleContaining(String title);
    
    /**
     * 根据创建者ID和标题模糊查询题目
     *
     * @param creatorId 创建者ID
     * @param title 标题关键词
     * @return 题目列表
     */
    List<Problem> findByCreatorIdAndTitleContainingOrderByCreatedAtDesc(String creatorId, String title);
}