package com.dong.judge.service;

import com.dong.judge.model.dto.sandbox.CodeExecuteRequest;
import com.dong.judge.model.dto.sandbox.CompileRequest;
import com.dong.judge.model.dto.sandbox.RunRequest;
import com.dong.judge.model.vo.sandbox.CodeExecuteResult;
import com.dong.judge.model.vo.sandbox.CompileResult;
import com.dong.judge.model.vo.sandbox.RunResult;

/**
 * 代码沙箱服务
 *
 * 提供代码的编译、运行和执行（编译+运行）功能
 */
public interface SandboxService {
    /**
     * 执行代码（完整流程：编译+运行）
     *
     * @param request 代码执行请求
     * @return 代码执行结果
     */
    CodeExecuteResult executeCode(CodeExecuteRequest request);

    /**
     * 仅编译代码
     *
     * @param request 编译请求
     * @return 编译结果
     */
    CompileResult compileCode(CompileRequest request);

    /**
     * 仅运行代码
     *
     * @param request 运行请求
     * @return 运行结果
     */
    RunResult runCode(RunRequest request);

    /**
     * 删除文件
     * @param fileId 文件ID
     */
    void deleteFile(String fileId);
}