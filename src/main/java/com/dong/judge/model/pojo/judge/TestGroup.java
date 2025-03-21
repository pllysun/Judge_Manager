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
}