package com.dong.judge.model.dto.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 创建题目请求DTO
 */
@Data
@Schema(description = "创建题目请求")
public class CreateProblemRequest {

    @NotNull(message = "题号不能为空")
    @Min(value = 1, message = "题号必须大于0")
    @Schema(description = "题号", example = "1")
    private Integer number;

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 100, message = "题目标题长度不能超过100")
    @Schema(description = "题目标题", example = "两数之和")
    private String title;

    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级必须在1-8之间")
    @Max(value = 8, message = "难度等级必须在1-8之间")
    @Schema(description = "难度等级(1-8): 1-2简单, 3-5中等, 6-8困难", example = "1")
    private Integer level;

    @NotBlank(message = "题目内容不能为空")
    @Schema(description = "题目内容", example = "给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值的那两个整数...")
    private String content;

    @Schema(description = "题目标签", example = "[\"数组\", \"哈希表\"]")
    private List<String> tags;

    @NotNull(message = "时间限制不能为空")
    @Min(value = 100, message = "时间限制最小为100ms")
    @Max(value = 10000, message = "时间限制最大为10000ms")
    @Schema(description = "时间限制(ms)", example = "1000")
    private Integer timeLimit;

    @NotNull(message = "内存限制不能为空")
    @Min(value = 16, message = "内存限制最小为16MB")
    @Max(value = 1024, message = "内存限制最大为1024MB")
    @Schema(description = "内存限制(MB)", example = "256")
    private Integer memoryLimit;

    @NotBlank(message = "示例输入不能为空")
    @Schema(description = "示例输入", example = "[2, 7, 11, 15]\\n9")
    private String sampleInput;

    @NotBlank(message = "示例输出不能为空")
    @Schema(description = "示例输出", example = "[0, 1]")
    private String sampleOutput;

    @Schema(description = "提示", example = "你可以假设每种输入只会对应一个答案。")
    private String hint;
}