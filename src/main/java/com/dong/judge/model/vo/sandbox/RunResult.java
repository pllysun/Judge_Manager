package com.dong.judge.model.vo.sandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码运行结果
 * <p>
 * 包含代码运行的状态、输出、资源使用等信息
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunResult {
    /**
     * 运行状态 (Accepted/Runtime Error/Time Limit Exceeded/Memory Limit Exceeded 等)
     */
    private String status;

    /**
     * 退出状态码
     */
    private int exitStatus;

    /**
     * CPU时间 (ms)
     */
    private long time;

    /**
     * 内存使用 (KB)
     */
    private long memory;

    /**
     * 实际运行时间 (ms)
     */
    private long runTime;

    /**
     * 标准输出内容
     */
    private String stdout;

    /**
     * 标准错误输出内容
     */
    private String stderr;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 判断运行是否成功
     * 
     * @return 如果运行状态为Accepted或退出状态码为0，则返回true
     */
    public boolean isSuccess() {
        return "Accepted".equals(status) || exitStatus == 0;
    }
}