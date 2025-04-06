package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "代码草稿请求")
public class CodeDraftRequest {
    @NotBlank(message = "题目ID不能为空")
    @Schema(description = "题目ID")
    private String problemId;
    
    @NotBlank(message = "代码内容不能为空")
    @Schema(description = "代码内容")
    private String code;
    
    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言")
    private String language;
} 