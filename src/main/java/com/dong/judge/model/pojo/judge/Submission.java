package com.dong.judge.model.pojo.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "submissions")
public class Submission {
    @Id
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 题目ID
     */
    private String problemId;

    /**
     * 题目编号
     */
    private String problemNumber;

    /**
     * 题目标题
     */
    private String problemTitle;
    
    /**
     * 提交的代码
     */
    private String code;
    
    /**
     * 编程语言
     */
    private String language;
    
    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;
    
    /**
     * 提交状态
     */
    private String status;
    
    /**
     * 通过测试用例数
     */
    private Integer passedCount;
    
    /**
     * 总测试用例数
     */
    private Integer totalCount;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 内存使用（MB）
     */
    private Long memoryUsed;

    /**
     * 出现错误的情况下返回的第一个错误用例的输入
     */
    private String firstInput;

    /**
     * 出现错误的情况下返回的第一个错误用例的预期输出
     */
    private String firstExpectedOutput;

    /**
     * 出现错误的情况下返回的第一个错误用例的实际输出
     */
    private String firstOutput;


    /**
     * 编译错误信息
     */
    private String compileError;
    
    /**
     * 通过率
     */
    private String passRatio;
}
