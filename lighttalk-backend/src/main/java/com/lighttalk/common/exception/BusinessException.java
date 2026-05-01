package com.lighttalk.common.exception;

import com.lighttalk.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑中抛出可预期的异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常状态码
     */
    private final Integer code;

    /**
     * 异常消息
     */
    private final String message;

    /**
     * 使用 ResultCode 构造业务异常
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    /**
     * 使用自定义消息构造业务异常（使用默认失败码）
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
        this.message = message;
    }

    /**
     * 使用自定义状态码和消息构造业务异常
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用 ResultCode 和自定义消息构造业务异常
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
}