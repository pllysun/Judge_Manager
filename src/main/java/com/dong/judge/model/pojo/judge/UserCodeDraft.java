package com.dong.judge.model.pojo.judge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_code_drafts")
@CompoundIndexes({
    @CompoundIndex(name = "user_problem_idx", def = "{userId: 1, problemId: 1}", unique = true)
})
@Schema(description = "用户代码草稿")
public class UserCodeDraft {
    @Id
    @Schema(description = "草稿ID")
    private String id;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "题目ID")
    private String problemId;
    
    @Schema(description = "代码内容")
    private String code;
    
    @Schema(description = "编程语言")
    private String language;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 