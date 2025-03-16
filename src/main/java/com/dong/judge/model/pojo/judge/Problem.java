package com.dong.judge.model.pojo.judge;

import com.dong.judge.model.dto.code.TestCaseSet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "problems")
@Schema(description = "题目实体")
public class Problem {
    @Id
    @Schema(description = "题目ID", example = "507f1f77bcf86cd799439011")
    private String id;
    
    @Indexed(unique = true)
    @Schema(description = "题号", example = "1")
    private Integer number;
    
    @Schema(description = "题目标题", example = "两数之和")
    private String title;
    
    @Schema(description = "题目难度", example = "简单", allowableValues = {"简单", "中等", "困难"})
    private String difficulty;
    
    @Schema(description = "题目内容", example = "给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值的那两个整数...")
    private String content;
    
    @Schema(description = "题目标签", example = "[\"数组\", \"哈希表\"]")
    private List<String> tags;

    @Schema(description = "测试集ID", example = "407f1a77bcf86cd709431589")
    private String testGroupId;
    
    @Schema(description = "提交次数", example = "0")
    private Integer submissionCount;
    
    @Schema(description = "通过次数", example = "0")
    private Integer acceptedCount;
    
    @Schema(description = "通过率", example = "0.0")
    private Double acceptanceRate;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "时间限制(ms)", example = "1000")
    private Integer timeLimit;
    
    @Schema(description = "内存限制(MB)", example = "256")
    private Integer memoryLimit;
    
    @Schema(description = "示例输入", example = "[2, 7, 11, 15]\n9")
    private String sampleInput;
    
    @Schema(description = "示例输出", example = "[0, 1]")
    private String sampleOutput;
    
    @Schema(description = "提示", example = "你可以假设每种输入只会对应一个答案。")
    private String hint;
}