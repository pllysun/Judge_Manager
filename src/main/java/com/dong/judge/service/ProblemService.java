package com.dong.judge.service;

import com.dong.judge.model.pojo.judge.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * 题目服务接口
 */
public interface ProblemService {
    
    /**
     * 创建题目
     *
     * @param problem 题目信息
     * @param userId 用户ID
     * @return 创建的题目
     */
    Problem createProblem(Problem problem, String userId);
    
    /**
     * 更新题目
     *
     * @param problem 题目信息
     * @param userId 用户ID
     * @return 更新后的题目
     */
    Problem updateProblem(Problem problem, String userId);
    
    /**
     * 获取题目详情
     *
     * @param id 题目ID
     * @return 题目信息
     */
    Problem getProblemById(String id);
    
    /**
     * 获取用户的题目列表
     *
     * @param userId 用户ID
     * @return 题目列表
     */
    List<Problem> getUserProblems(String userId);
    
    /**
     * 删除题目
     *
     * @param id 题目ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteProblem(String id, String userId);
    
    /**
     * 搜索题目
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 题目列表
     */
    List<Problem> searchProblems(String userId, String keyword);
    
    /**
     * 按难度级别查询题目
     *
     * @param userId 用户ID
     * @param level 难度级别
     * @return 题目列表
     */
    List<Problem> getProblemsByDifficulty(String userId, Integer level);
    
    /**
     * 按标签查询题目
     *
     * @param userId 用户ID
     * @param tag 标签
     * @return 题目列表
     */
    List<Problem> getProblemsByTag(String userId, String tag);
    
    /**
     * 获取所有题目中的标签（去重）
     *
     * @return 标签列表
     */
    List<String> getAllTags();
    
    /**
     * 获取所有题目列表
     *
     * @return 题目列表
     */
    List<Problem> getAllProblems();
    
    /**
     * 分页获取所有题目列表
     *
     * @param pageRequest 分页请求参数
     * @return 分页题目列表
     */
    Page<Problem> getAllProblemsPage(PageRequest pageRequest);
    
    /**
     * 多条件查询题目并分页
     *
     * @param pageRequest 分页请求参数
     * @param difficulty 难度类型（简单、中等、困难），可为null
     * @param keyword 搜索关键词，可为null
     * @param tags 标签列表，可为null
     * @return 分页题目列表
     */
    Page<Problem> searchProblemsWithConditions(PageRequest pageRequest, String difficulty, String keyword, List<String> tags);
}