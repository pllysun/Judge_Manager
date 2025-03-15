package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.TestGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 测试集数据访问接口
 */
public interface TestGroupRepository extends MongoRepository<TestGroup, String> {
    /**
     * 根据题目ID查找测试集列表
     * @param problemId 题目ID
     * @return 测试集列表
     */
    List<TestGroup> findByProblemId(String problemId);
    
    /**
     * 根据题目ID查找测试集列表（分页）
     * @param problemId 题目ID
     * @param pageable 分页参数
     * @return 测试集分页列表
     */
    Page<TestGroup> findByProblemId(String problemId, Pageable pageable);
    
    /**
     * 根据名称模糊查询（分页）
     * @param name 名称关键词
     * @param pageable 分页参数
     * @return 测试集分页列表
     */
    Page<TestGroup> findByNameContaining(String name, Pageable pageable);
}