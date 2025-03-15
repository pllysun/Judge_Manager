package com.dong.judge.dao.repository;

import com.dong.judge.model.pojo.judge.ProblemTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProblemTagRepository extends MongoRepository<ProblemTag, String> {
    /**
     * 根据标签名称查找标签
     * @param name 标签名称
     * @return 标签对象
     */
    ProblemTag findByName(String name);
    
    /**
     * 根据标签名称模糊查询（分页）
     * @param name 标签名称关键词
     * @param pageable 分页参数
     * @return 标签分页列表
     */
    Page<ProblemTag> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 根据使用次数排序查询（分页）
     * @param pageable 分页参数
     * @return 标签分页列表
     */
    Page<ProblemTag> findAllByOrderByUseCountDesc(Pageable pageable);
}