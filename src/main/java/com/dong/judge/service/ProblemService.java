package com.dong.judge.service;

import com.dong.judge.model.dto.code.TestCaseSet;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.ProblemTag;
import org.springframework.data.domain.Page;

public interface ProblemService {

    
    /**
     * 创建题目
     * @param problem 题目对象
     * @return 创建后的题目对象
     */
    Problem createProblem(Problem problem);
    
    /**
     * 更新题目
     * @param problemId 题目ID
     * @param problem 题目对象
     * @return 更新后的题目对象
     */
    Problem updateProblem(String problemId, Problem problem);
    
    /**
     * 删除题目
     * @param problemId 题目ID
     * @return 是否删除成功
     */
    boolean deleteProblem(String problemId);
    
    /**
     * 根据题目ID查询题目
     * @param problemId 题目ID
     * @return 题目对象
     */
    Problem getProblemById(String problemId);
    
    /**
     * 根据题号查询题目
     * @param number 题号
     * @return 题目对象
     */
    Problem getProblemByNumber(Integer number);
    
    /**
     * 分页查询题目列表
     * @param page 页码
     * @param size 每页大小
     * @param difficulty 难度（可选）
     * @param tag 标签（可选）
     * @param keyword 关键词（可选）
     * @return 题目分页列表
     */
    Page<Problem> getProblemList(int page, int size, String difficulty, String tag, String keyword);
    
    /**
     * 创建题目标签
     * @param tag 标签对象
     * @return 创建后的标签对象
     */
    ProblemTag createProblemTag(ProblemTag tag);
    
    /**
     * 获取所有题目标签
     * @param page 页码
     * @param size 每页大小
     * @return 标签分页列表
     */
    Page<ProblemTag> getAllTags(int page, int size);
}
