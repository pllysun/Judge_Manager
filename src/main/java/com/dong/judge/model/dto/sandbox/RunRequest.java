package com.dong.judge.model.dto.sandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码运行请求
 * <p>
 * 用于传递代码运行所需的参数
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunRequest {
    /**
     * 代码内容（解释型语言需要）
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 输入数据
     */
    private String input;

    /**
     * 文件ID（编译型语言使用已编译的文件）
     */
    private String fileId;
}