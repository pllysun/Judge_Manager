package com.dong.judge.model.pojo.judge;

import com.dong.judge.model.dto.code.TestCase;
import com.dong.judge.model.dto.code.TestCaseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试集实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_groups")
@Schema(description = "测试集")
public class TestGroup {
    @Id
    @Schema(description = "测试集ID")
    private String id;
    
    @Schema(description = "代码内容")
    private String code;
    
    @Schema(description = "编程语言", example = "java")
    private String language;
    
    @Schema(description = "测试用例列表")
    private List<TestCase> testCases;
    
    @Schema(description = "创建者ID")
    private String creatorId;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @Schema(description = "测试用例结果列表")
    private List<TestCaseResult> testCaseResults;
    
    @Schema(description = "总测试用例数", example = "5")
    private int totalCount;
    
    @Schema(description = "通过测试用例数", example = "4")
    private int passedCount;
    
    @Schema(description = "总CPU执行时间（纳秒）", example = "5865000")
    private Long totalTime;
    
    @Schema(description = "平均CPU执行时间（纳秒）", example = "1173000")
    private Long avgTime;
    
    @Schema(description = "总内存使用（字节）", example = "53186560")
    private Long totalMemory;
    
    @Schema(description = "平均内存使用（字节）", example = "10637312")
    private Long avgMemory;
    
    @Schema(description = "总实际运行时间（纳秒）", example = "5501000")
    private Long totalRunTime;
    
    @Schema(description = "平均实际运行时间（纳秒）", example = "1100200")
    private Long avgRunTime;
    
    @Schema(description = "编译错误信息")
    private String compileError;
    
    @Schema(description = "是否全部通过", example = "false")
    private boolean allPassed;
}