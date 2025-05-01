package com.dong.judge.service;

import com.dong.judge.model.dto.code.TestCaseSetResult;
import com.dong.judge.model.pojo.judge.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface SubmissionService {
    /**
     * 保存提交记录
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param code 提交的代码
     * @param language 编程语言
     * @param result 测试用例执行结果
     * @return 保存的提交记录
     */
    Submission saveSubmission(String userId, String problemId, String code, String language, TestCaseSetResult result);
    
    /**
     * 获取用户的提交记录
     * @param userId 用户ID
     * @param problemId 题目ID
     * @return 提交记录列表
     */
    List<Submission> getUserSubmissions(String userId, String problemId);
    
    /**
     * 获取题目的执行时间排名
     * @param problemId 题目ID
     * @return 提交记录列表（按执行时间升序）
     */
    List<Submission> getProblemTimeRanking(String problemId);
    
    /**
     * 获取题目的内存使用排名
     * @param problemId 题目ID
     * @return 提交记录列表（按内存使用升序）
     */
    List<Submission> getProblemMemoryRanking(String problemId);
    
    /**
     * 根据提交ID获取提交详情
     * @param submissionId 提交ID
     * @return 提交记录
     */
    Submission getSubmissionById(String submissionId);
    
    /**
     * 分页获取所有用户的提交记录
     * @param pageRequest 分页请求参数
     * @param problemId 题目ID（可选）
     * @param userId 用户ID（可选）
     * @param status 提交状态（可选）
     * @param language 编程语言（可选）
     * @return 分页提交记录
     */
    Page<Submission> getAllSubmissionsPage(PageRequest pageRequest, String problemId, String userId, String status, String language);
}