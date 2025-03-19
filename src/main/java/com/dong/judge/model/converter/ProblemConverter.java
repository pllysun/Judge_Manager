package com.dong.judge.model.converter;

import com.dong.judge.model.dto.problem.CreateProblemRequest;
import com.dong.judge.model.enums.DifficultyLevel;
import com.dong.judge.model.pojo.judge.Problem;

import java.time.LocalDateTime;

/**
 * 题目相关的转换器
 */
public class ProblemConverter {

    /**
     * 将创建题目请求转换为题目实体
     */
    public static Problem toEntity(CreateProblemRequest request) {
        // 根据level获取对应的难度等级枚举
        DifficultyLevel difficultyLevel = DifficultyLevel.getByLevel(request.getLevel());

        return Problem.builder()
                .number(request.getNumber())
                .title(request.getTitle())
                .difficultyLevel(difficultyLevel)
                .content(request.getContent())
                .tags(request.getTags())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .sampleInput(request.getSampleInput())
                .sampleOutput(request.getSampleOutput())
                .hint(request.getHint())
                // 设置默认值
                .submissionCount(0)
                .acceptedCount(0)
                .acceptanceRate(0.0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 更新题目实体
     */
    public static void updateEntity(Problem problem, CreateProblemRequest request) {
        DifficultyLevel difficultyLevel = DifficultyLevel.getByLevel(request.getLevel());

        problem.setNumber(request.getNumber());
        problem.setTitle(request.getTitle());
        problem.setDifficultyLevel(difficultyLevel);
        problem.setContent(request.getContent());
        problem.setTags(request.getTags());
        problem.setTimeLimit(request.getTimeLimit());
        problem.setMemoryLimit(request.getMemoryLimit());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());
        problem.setHint(request.getHint());
        problem.setUpdateTime(LocalDateTime.now());
    }
}