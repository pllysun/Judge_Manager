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

}