package com.dong.judge.exception;

import com.dong.judge.model.vo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     *
     * @param ex 参数校验异常
     * @return 自定义错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();

        // 收集所有字段的错误信息
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // 如果只有一个错误，直接返回错误消息
        if (errors.size() == 1) {
            String message = errors.values().iterator().next();
            return Result.error(400, message);
        }

        // 多个错误时返回错误集合
        return Result.error(400, "输入参数有误", errors);
    }

    /**
     * 处理其他异常
     *
     * @param e 异常
     * @return 自定义错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}