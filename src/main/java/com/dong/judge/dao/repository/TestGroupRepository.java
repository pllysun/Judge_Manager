package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.TestGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 测试集数据访问接口
 */
@Repository
public interface TestGroupRepository extends MongoRepository<TestGroup, String> {
    
    /**
     * 根据创建者ID查找测试集
     *
     * @param creatorId 创建者ID
     * @return 测试集列表
     */
    List<TestGroup> findByCreatorIdOrderByCreatedAtDesc(String creatorId);
    
    /**
     * 根据创建者ID查询测试集
     *
     * @param creatorId 创建者ID
     * @param keyword 关键词（不使用）
     * @return 测试集列表
     */
    List<TestGroup> findByCreatorIdOrderByCreatedAtDesc(String creatorId, String keyword);
}