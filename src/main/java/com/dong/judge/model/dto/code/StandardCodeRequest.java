package com.dong.judge.model.dto.code;

import com.dong.judge.model.pojo.judge.TestGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准代码请求
 * <p>
 * 用于创建或更新测试集时提交标准代码
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标准代码请求")
public class StandardCodeRequest {
    @Schema(description = "测试集信息", required = true)
    private TestGroup testGroup;
    
    @Schema(description = "标准代码", required = true, example = "public class Solution { public static void main(String[] args) { ... } }")
    private String standardCode;
    
    @Schema(description = "编程语言", required = true, example = "java")
    private String language;
}