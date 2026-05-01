package com.lighttalk.common.api;

import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统常用的响应状态码和消息
 */
@Getter
public enum ResultCode {

    /**
     * 成功状态码
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败状态码
     */
    FAIL(400, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(4001, "参数错误"),

    /**
     * 参数缺失
     */
    PARAM_MISSING(4002, "参数缺失"),

    /**
     * 未授权（未登录）
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(5001, "用户名已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(5002, "用户不存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(5003, "密码错误"),

    /**
     * 用户已被禁言
     */
    USER_BANNED(5004, "用户已被禁言"),

    /**
     * 房间不存在
     */
    ROOM_NOT_FOUND(6001, "房间不存在"),

    /**
     * 房间已存在
     */
    ROOM_EXISTS(6002, "房间已存在"),

    /**
     * 已在房间中
     */
    ALREADY_IN_ROOM(6003, "已在房间中"),

    /**
     * 房间已满
     */
    ROOM_FULL(6004, "房间已满"),

    /**
     * Token 无效
     */
    TOKEN_INVALID(7001, "Token无效"),

    /**
     * Token 已过期
     */
    TOKEN_EXPIRED(7002, "Token已过期"),

    /**
     * 系统内部错误
     */
    INTERNAL_ERROR(500, "系统内部错误");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}