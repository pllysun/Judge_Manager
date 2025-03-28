package com.dong.judge.service;

import com.dong.judge.model.dto.code.CodeRunRequest;
import com.dong.judge.model.dto.code.CodeSubmitRequest;
import com.dong.judge.model.dto.code.TestCaseSetResult;
import jakarta.validation.Valid;

/**
 * 代码执行服务
 * <p>
 * 提供OJ风格的代码执行功能
 * </p>
 */
public interface CodeService {
    
    /**
     * 运行代码并执行测试用例
     * 
     * @param request 代码运行请求
     * @return 测试用例执行结果集
     */
    TestCaseSetResult runCode(@Valid CodeRunRequest request);

    TestCaseSetResult submitCode(@Valid CodeSubmitRequest request, String userId);
}