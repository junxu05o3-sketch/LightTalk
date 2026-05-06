package com.lighttalk.service;

import com.lighttalk.model.dto.LoginResp;
import com.lighttalk.model.dto.UserLoginReq;
import com.lighttalk.model.dto.UserRegisterReq;

/**
 * 认证服务接口
 * 定义用户注册与登录的核心业务契约
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param req 注册请求参数（用户名、密码、昵称）
     * @throws com.lighttalk.common.exception.BusinessException 用户名已存在时抛出
     */
    void register(UserRegisterReq req);

    /**
     * 用户登录
     *
     * @param req 登录请求参数（用户名、密码）
     * @return 登录成功响应（包含 JWT Token 和用户基本信息）
     * @throws com.lighttalk.common.exception.BusinessException 用户不存在或密码错误时抛出
     */
    LoginResp login(UserLoginReq req);
}
