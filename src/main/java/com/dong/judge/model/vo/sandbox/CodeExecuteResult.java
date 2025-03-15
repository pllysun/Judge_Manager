package com.dong.judge.model.vo.sandbox;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码执行结果")
public class CodeExecuteResult {
    @Schema(description = "执行状态", example = "Accepted")
    private String status;

    @Schema(description = "执行退出码", example = "0")
    private Integer exitStatus;

    @Schema(description = "CPU 执行时间（纳秒）", example = "1173000")
    private Long time;

    @Schema(description = "内存使用（字节）", example = "10637312")
    private Long memory;

    @Schema(description = "实际运行时间（纳秒）", example = "1100200")
    private Long runTime;

    @Schema(description = "标准输出", example = "2\n")
    private String stdout;

    @Schema(description = "标准错误", example = "")
    private String stderr;

    @Schema(description = "编译错误信息")
    private String compileError;
}