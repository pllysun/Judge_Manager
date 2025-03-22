package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.ProblemRepository;
import com.dong.judge.model.enums.DifficultyLevel;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.TestGroup;
import com.dong.judge.service.ProblemService;
import com.dong.judge.service.TestGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 题目服务实现类
 */
@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private ProblemRepository problemRepository;
    
    @Autowired
    private TestGroupService testGroupService;
    
    @Override
    public Problem createProblem(Problem problem, String userId) {
        // 验证测试集是否存在
        if (StringUtils.hasText(problem.getTestGroupId())) {
            try {
                testGroupService.getTestGroupById(problem.getTestGroupId());
            } catch (Exception e) {
                throw new IllegalArgumentException("关联的测试集不存在: " + problem.getTestGroupId());
            }
        }
        
        // 设置创建者ID
        problem.setCreatorId(userId);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        problem.setCreatedAt(now);
        problem.setUpdatedAt(now);
        
        // 设置难度级别枚举
        if (problem.getDifficulty() != null) {
            problem.setDifficultyLevel(DifficultyLevel.getByLevel(problem.getDifficulty()));
        }
        
        // 保存题目
        return problemRepository.save(problem);
    }

    @Override
    public Problem updateProblem(Problem problem, String userId) {
        // 获取原题目
        Problem existingProblem = getProblemById(problem.getId());
        
        // 验证用户权限
        if (!existingProblem.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("您没有权限修改此题目");
        }
        
        // 验证测试集是否存在
        if (StringUtils.hasText(problem.getTestGroupId()) && 
            !problem.getTestGroupId().equals(existingProblem.getTestGroupId())) {
            try {
                testGroupService.getTestGroupById(problem.getTestGroupId());
            } catch (Exception e) {
                throw new IllegalArgumentException("关联的测试集不存在: " + problem.getTestGroupId());
            }
        }
        
        // 更新字段
        if (StringUtils.hasText(problem.getTitle())) {
            existingProblem.setTitle(problem.getTitle());
        }
        
        if (problem.getDifficulty() != null) {
            existingProblem.setDifficulty(problem.getDifficulty());
            existingProblem.setDifficultyLevel(DifficultyLevel.getByLevel(problem.getDifficulty()));
        }
        
        if (problem.getTags() != null) {
            existingProblem.setTags(problem.getTags());
        }
        
        if (StringUtils.hasText(problem.getContent())) {
            existingProblem.setContent(problem.getContent());
        }
        
        if (StringUtils.hasText(problem.getTestGroupId())) {
            existingProblem.setTestGroupId(problem.getTestGroupId());
        }
        
        // 更新时间
        existingProblem.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        return problemRepository.save(existingProblem);
    }

    @Override
    public Problem getProblemById(String id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("题目不存在: " + id));
    }

    @Override
    public List<Problem> getUserProblems(String userId) {
        return problemRepository.findByCreatorIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public boolean deleteProblem(String id, String userId) {
        // 获取题目
        Problem problem = getProblemById(id);
        
        // 验证用户权限
        if (!problem.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("您没有权限删除此题目");
        }
        
        // 删除题目
        problemRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Problem> searchProblems(String userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        // 根据标题搜索用户的题目
        return problemRepository.findByCreatorIdAndTitleContainingOrderByCreatedAtDesc(userId, keyword);
    }

    @Override
    public List<Problem> getProblemsByDifficulty(String userId, Integer level) {
        // 验证难度级别
        DifficultyLevel.getByLevel(level);
        
        // 查询指定难度级别的题目
        List<Problem> problems = problemRepository.findByDifficulty(level);
        
        // 过滤出用户创建的题目
        return problems.stream()
                .filter(problem -> problem.getCreatorId().equals(userId))
                .toList();
    }

    @Override
    public List<Problem> getProblemsByTag(String userId, String tag) {
        if (!StringUtils.hasText(tag)) {
            return new ArrayList<>();
        }
        
        // 查询包含指定标签的题目
        List<Problem> problems = problemRepository.findByTagsContaining(tag);
        
        // 过滤出用户创建的题目
        return problems.stream()
                .filter(problem -> problem.getCreatorId().equals(userId))
                .toList();
    }
    
    @Override
    public List<String> getAllTags() {
        // 获取所有题目
        List<Problem> allProblems = problemRepository.findAll();
        
        // 提取所有标签并去重
        return allProblems.stream()
                .filter(problem -> problem.getTags() != null && !problem.getTags().isEmpty())
                .flatMap(problem -> problem.getTags().stream())
                .distinct()
                .sorted()
                .toList();
    }
    
    @Override
    public List<Problem> getAllProblems() {
        // 获取所有题目并按创建时间降序排序
        return problemRepository.findAll();
    }

    @Override
    public Page<Problem> getAllProblemsPage(PageRequest pageRequest) {
        // 使用分页参数查询所有题目
        return problemRepository.findAll(pageRequest);
    }
}