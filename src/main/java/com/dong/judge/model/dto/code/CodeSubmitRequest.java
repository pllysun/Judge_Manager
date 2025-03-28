package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OJ风格代码执行请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OJ风格代码执行请求")
public class CodeSubmitRequest {
    @NotBlank(message = "代码内容不能为空")
    @Schema(description = "代码内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "java")
    private String language;

    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String problemId;
}