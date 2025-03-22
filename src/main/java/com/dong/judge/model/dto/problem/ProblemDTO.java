package com.dong.judge.model.dto.problem;

import com.dong.judge.model.enums.DifficultyLevel;
import com.dong.judge.model.pojo.judge.Problem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目数据传输对象
 * 用于返回精简的题目信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "题目DTO")
public class ProblemDTO {
    @Schema(description = "题目ID")
    private String id;
    
    @Schema(description = "题目标题")
    private String title;
    
    @Schema(description = "难度级别枚举")
    private DifficultyLevel difficultyLevel;
    
    @Schema(description = "题目标签列表")
    private List<String> tags;
    
    @Schema(description = "题目内容")
    private String content;
    
    @Schema(description = "提交总数")
    private Integer submissionCount;
    
    @Schema(description = "正确提交数量")
    private Integer acceptedCount;
    
    @Schema(description = "答案错误数量")
    private Integer wrongAnswerCount;
    
    @Schema(description = "超时数量")
    private Integer timeExceededCount;
    
    @Schema(description = "编译错误数量")
    private Integer compileErrorCount;
    
    @Schema(description = "内存超限数量")
    private Integer memoryExceededCount;
    
    @Schema(description = "通过百分比")
    private Double acceptedRate;
    
    /**
     * 将Problem实体转换为ProblemDTO
     *
     * @param problem 题目实体
     * @return 题目DTO
     */
    public static ProblemDTO fromProblem(Problem problem) {
        if (problem == null) {
            return null;
        }
        
        return ProblemDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .difficultyLevel(problem.getDifficultyLevel())
                .tags(problem.getTags())
                .content(problem.getContent())
                .submissionCount(problem.getSubmissionCount())
                .acceptedCount(problem.getAcceptedCount())
                .wrongAnswerCount(problem.getWrongAnswerCount())
                .timeExceededCount(problem.getTimeExceededCount())
                .compileErrorCount(problem.getCompileErrorCount())
                .memoryExceededCount(problem.getMemoryExceededCount())
                .acceptedRate(problem.getAcceptedRate())
                .build();
    }
}