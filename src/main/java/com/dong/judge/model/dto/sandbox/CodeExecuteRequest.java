package com.dong.judge.model.dto.sandbox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "沙箱代码执行请求")
public class CodeExecuteRequest {
    @Schema(description = "代码内容", required = true)
    private String code;

    @Schema(description = "编程语言", required = true, example = "cpp")
    private String language;

    @Schema(description = "输入数据", example = "1 2")
    private String input;

    @Schema(description = "额外参数")
    private Map<String, String> extraParams;
}