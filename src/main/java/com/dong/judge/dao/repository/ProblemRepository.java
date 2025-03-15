package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProblemRepository extends MongoRepository<Problem, String> {
    /**
     * 根据题号查找题目
     * @param number 题号
     * @return 题目对象
     */
    Problem findByNumber(Integer number);
    
    /**
     * 查找最大题号
     * @return 最大题号
     */
    Problem findFirstByOrderByNumberDesc();
    
    /**
     * 根据难度查找题目（分页）
     * @param difficulty 难度
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Problem> findByDifficulty(String difficulty, Pageable pageable);
    
    /**
     * 根据标签查找题目（分页）
     * @param tag 标签
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Problem> findByTagsContaining(String tag, Pageable pageable);
    
    /**
     * 根据标题模糊查询（分页）
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Problem> findByTitleContaining(String title, Pageable pageable);
}
