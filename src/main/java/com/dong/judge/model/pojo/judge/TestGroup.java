package com.dong.judge.model.pojo.judge;

import com.dong.judge.model.dto.code.TestCase;
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
@Schema(description = "测试集实体")
public class TestGroup {
    @Id
    @Schema(description = "测试集ID")
    private String id;
    
    @Schema(description = "测试集名称", example = "基础测试集")
    private String name;
    
    @Schema(description = "测试集描述", example = "用于测试基本功能")
    private String description;
    
    @Schema(description = "关联的题目ID", example = "507f1f77bcf86cd799439011")
    private String problemId;
    
    @Schema(description = "测试用例列表")
    private List<TestCase> testCases;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}