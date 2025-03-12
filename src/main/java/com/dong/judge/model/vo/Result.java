package com.dong.judge.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 *
 * @param <T> 数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    /**
     * 成功响应（无数据）
     * @return 成功的响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（有数据）
     * @param data 响应数据
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息和数据）
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应（默认500错误）
     * @return 失败的响应结果
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "系统错误", null);
    }

    /**
     * 失败响应（自定义消息）
     * @param message 错误消息
     * @return 失败的响应结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（自定义错误码和消息）
     * @param code 错误码
     * @param message 错误消息
     * @return 失败的响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（自定义错误码、消息和数据）
     * @param code 错误码
     * @param message 错误消息
     * @param data 错误数据
     * @return 失败的响应结果
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 参数错误响应（400错误）
     * @param message 错误消息
     * @return 参数错误的响应结果
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 未授权响应（401错误）
     * @return 未授权的响应结果
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或登录已过期", null);
    }

    /**
     * 禁止访问响应（403错误）
     * @return 禁止访问的响应结果
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "没有操作权限", null);
    }

    /**
     * 资源不存在响应（404错误）
     * @return 资源不存在的响应结果
     */
    public static <T> Result<T> notFound() {
        return new Result<>(404, "请求的资源不存在", null);
    }
}