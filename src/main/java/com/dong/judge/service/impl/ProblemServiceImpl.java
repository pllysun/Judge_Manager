package com.dong.judge.service.impl;

import com.dong.judge.dao.repository.ProblemRepository;
import com.dong.judge.dao.repository.ProblemTagRepository;
import com.dong.judge.model.dto.code.TestCaseSet;
import com.dong.judge.model.pojo.judge.Problem;
import com.dong.judge.model.pojo.judge.ProblemTag;
import com.dong.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.management.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemTagRepository problemTagRepository;

    
    @Override
    public Problem createProblem(Problem problem) {
        // 设置题号（自增）
        Problem lastProblem = problemRepository.findFirstByOrderByNumberDesc();
        int nextNumber = (lastProblem == null) ? 1 : lastProblem.getNumber() + 1;
        problem.setNumber(nextNumber);
        
        // 设置默认值
        if (problem.getSubmissionCount() == null) {
            problem.setSubmissionCount(0);
        }
        if (problem.getAcceptedCount() == null) {
            problem.setAcceptedCount(0);
        }
        if (problem.getAcceptanceRate() == null) {
            problem.setAcceptanceRate(0.0);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        problem.setCreateTime(now);
        problem.setUpdateTime(now);
        
        // 更新标签使用次数
        if (problem.getTags() != null && !problem.getTags().isEmpty()) {
            for (String tagName : problem.getTags()) {
                ProblemTag tag = problemTagRepository.findByName(tagName);
                if (tag != null) {
                    tag.setUseCount(tag.getUseCount() + 1);
                    problemTagRepository.save(tag);
                }
            }
        }
        
        return problemRepository.save(problem);
    }
    
    @Override
    public Problem updateProblem(String problemId, Problem problem) {
        Problem existingProblem = problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + problemId));
        
        // 更新基本信息
        if (StringUtils.hasText(problem.getTitle())) {
            existingProblem.setTitle(problem.getTitle());
        }
        if (StringUtils.hasText(problem.getDifficulty())) {
            existingProblem.setDifficulty(problem.getDifficulty());
        }
        if (StringUtils.hasText(problem.getContent())) {
            existingProblem.setContent(problem.getContent());
        }
        if (problem.getTags() != null) {
            // 更新标签使用次数
            if (existingProblem.getTags() != null) {
                for (String oldTag : existingProblem.getTags()) {
                    if (!problem.getTags().contains(oldTag)) {
                        // 标签被移除，减少使用次数
                        ProblemTag tag = problemTagRepository.findByName(oldTag);
                        if (tag != null && tag.getUseCount() > 0) {
                            tag.setUseCount(tag.getUseCount() - 1);
                            problemTagRepository.save(tag);
                        }
                    }
                }
            }
            
            for (String newTag : problem.getTags()) {
                if (existingProblem.getTags() == null || !existingProblem.getTags().contains(newTag)) {
                    // 新增标签，增加使用次数
                    ProblemTag tag = problemTagRepository.findByName(newTag);
                    if (tag != null) {
                        tag.setUseCount(tag.getUseCount() + 1);
                        problemTagRepository.save(tag);
                    }
                }
            }
            
            existingProblem.setTags(problem.getTags());
        }
        
        // 更新其他可选字段
        if (problem.getTimeLimit() != null) {
            existingProblem.setTimeLimit(problem.getTimeLimit());
        }
        if (problem.getMemoryLimit() != null) {
            existingProblem.setMemoryLimit(problem.getMemoryLimit());
        }
        if (StringUtils.hasText(problem.getSampleInput())) {
            existingProblem.setSampleInput(problem.getSampleInput());
        }
        if (StringUtils.hasText(problem.getSampleOutput())) {
            existingProblem.setSampleOutput(problem.getSampleOutput());
        }
        if (StringUtils.hasText(problem.getHint())) {
            existingProblem.setHint(problem.getHint());
        }
        
        // 更新时间
        existingProblem.setUpdateTime(LocalDateTime.now());
        
        return problemRepository.save(existingProblem);
    }
    
    @Override
    public boolean deleteProblem(String problemId) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + problemId));
        
        // 更新标签使用次数
        if (problem.getTags() != null && !problem.getTags().isEmpty()) {
            for (String tagName : problem.getTags()) {
                ProblemTag tag = problemTagRepository.findByName(tagName);
                if (tag != null && tag.getUseCount() > 0) {
                    tag.setUseCount(tag.getUseCount() - 1);
                    problemTagRepository.save(tag);
                }
            }
        }
        
        problemRepository.delete(problem);
        return true;
    }
    
    @Override
    public Problem getProblemById(String problemId) {
        return problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + problemId));
    }
    
    @Override
    public Problem getProblemByNumber(Integer number) {
        Problem problem = problemRepository.findByNumber(number);
        if (problem == null) {
            throw new IllegalArgumentException("题目不存在: 题号" + number);
        }
        return problem;
    }
    
  @Override
  public Page<Problem> getProblemList(int page, int size, String difficulty, String tag, String keyword) {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "number"));

      // 无筛选条件时使用标准分页查询
      if (!StringUtils.hasText(difficulty) && !StringUtils.hasText(tag) && !StringUtils.hasText(keyword)) {
          return problemRepository.findAll(pageable);
      }

      // 使用JPA提供的条件查询（针对单条件查询）
      if (StringUtils.hasText(difficulty) && !StringUtils.hasText(tag) && !StringUtils.hasText(keyword)) {
          return problemRepository.findByDifficulty(difficulty, pageable);
      } else if (!StringUtils.hasText(difficulty) && StringUtils.hasText(tag) && !StringUtils.hasText(keyword)) {
          return problemRepository.findByTagsContaining(tag, pageable);
      } else if (!StringUtils.hasText(difficulty) && !StringUtils.hasText(tag) && StringUtils.hasText(keyword)) {
          return problemRepository.findByTitleContaining(keyword, pageable);
      }

      // 复合条件查询需要手动实现
      List<Problem> allProblems = problemRepository.findAll();
      List<Problem> filteredProblems = allProblems.stream()
          .filter(p -> !StringUtils.hasText(difficulty) || p.getDifficulty().equals(difficulty))
          .filter(p -> !StringUtils.hasText(tag) || (p.getTags() != null && p.getTags().contains(tag)))
          .filter(p -> !StringUtils.hasText(keyword) || (p.getTitle() != null && p.getTitle().contains(keyword)))
          .collect(Collectors.toList());

      // 手动分页
      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), filteredProblems.size());

      // 如果start超出了列表大小，返回空页
      if (start >= filteredProblems.size()) {
          return new PageImpl<>(new ArrayList<>(), pageable, filteredProblems.size());
      }

      return new PageImpl<>(
          filteredProblems.subList(start, end),
          pageable,
          filteredProblems.size()
      );
  }
    
    @Override
    public ProblemTag createProblemTag(ProblemTag tag) {
        // 检查标签名是否已存在
        ProblemTag existingTag = problemTagRepository.findByName(tag.getName());
        if (existingTag != null) {
            throw new IllegalArgumentException("标签已存在: " + tag.getName());
        }
        
        // 设置默认值
        if (tag.getUseCount() == null) {
            tag.setUseCount(0);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        tag.setCreateTime(now);
        tag.setUpdateTime(now);
        
        return problemTagRepository.save(tag);
    }
    
    @Override
    public Page<ProblemTag> getAllTags(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "useCount"));
        return problemTagRepository.findAll(pageable);
    }
}
