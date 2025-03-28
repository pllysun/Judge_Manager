package com.dong.judge.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "运行代码")
public class CodeRunRequest {
    @Schema(description = "代码", example = "print('Hello, World!')")
    private String code;

    @Schema(description = "语言", example = "python")
    private String language;

    @Schema(description = "输入")
    private String input;
}
