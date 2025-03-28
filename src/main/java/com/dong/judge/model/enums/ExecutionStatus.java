package com.dong.judge.model.enums;

import lombok.Getter;

/**
 * 代码执行状态枚举
 */
@Getter
public enum ExecutionStatus {
    
    ACCEPTED("Accepted", "执行成功", 200),
    COMPILE_ERROR("Compile Error", "编译错误", 400),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存超限", 400),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "时间超限", 400),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出超限", 400),
    FILE_ERROR("File Error", "文件错误", 400),
    NONZERO_EXIT_STATUS("Nonzero Exit Status", "程序异常退出", 400),
    SIGNALLED("Signalled", "程序被信号终止", 400),
    INTERNAL_ERROR("Internal Error", "内部错误", 500),
    SYSTEM_ERROR("System Error", "系统错误", 500);

    /**
     * 状态代码
     */
    private final String code;
    
    /**
     * 状态描述
     */
    private final String message;
    
    /**
     * 对应的HTTP状态码
     */
    private final int httpStatus;

    ExecutionStatus(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * 根据状态代码获取对应的枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果找不到则返回SYSTEM_ERROR
     */
    public static ExecutionStatus getByCode(String code) {
        if (code == null) {
            return SYSTEM_ERROR;
        }
        
        for (ExecutionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return SYSTEM_ERROR;
    }
}
