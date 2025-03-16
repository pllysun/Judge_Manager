package com.dong.judge.model.vo.sandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompileResult {
    private boolean success;
    private String errorMessage;
    private String fileId;
    private String status;
    /**
     * 是否需要编译
     */
    private boolean isCompile;
}