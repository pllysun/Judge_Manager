package com.dong.judge.model.dto.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmitResponse {
    /**
     * 提交ID
     */
    private String submissionId;
    
    /**
     * 测试用例执行结果
     */
    private TestCaseSetResult testCaseSetResult;
} 