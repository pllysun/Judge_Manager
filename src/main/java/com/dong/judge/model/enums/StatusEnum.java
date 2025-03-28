package com.dong.judge.model.enums;

/**
 * 代码沙盒执行状态枚举
 */
public enum StatusEnum {
    /**
     * 正常情况
     */
    ACCEPTED("Accepted", "执行成功"),
    
    /**
     * 内存超限
     */
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存超限"),
    
    /**
     * 时间超限
     */
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "时间超限"),
    
    /**
     * 输出超限
     */
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出超限"),
    
    /**
     * 文件错误
     */
    FILE_ERROR("File Error", "文件错误"),
    
    /**
     * 非0退出值
     */
    NONZERO_EXIT_STATUS("Nonzero Exit Status", "非0退出值"),
    
    /**
     * 进程被信号终止
     */
    SIGNALLED("Signalled", "进程被信号终止"),
    
    /**
     * 内部错误
     */
    INTERNAL_ERROR("Internal Error", "内部错误"),
    
    /**
     * 编译错误
     */
    COMPILE_ERROR("Compile Error", "编译错误"),
    
    /**
     * 系统错误
     */
    ERROR("Error", "系统错误");
    
    private final String value;
    private final String description;
    
    StatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态值获取枚举
     *
     * @param value 状态值
     * @return 对应的枚举，如果没有找到则返回ERROR
     */
    public static StatusEnum fromValue(String value) {
        if (value == null) {
            return ERROR;
        }
        
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        
        return ERROR;
    }
}