package com.lighttalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lighttalk.common.api.ResultCode;
import com.lighttalk.common.exception.BusinessException;
import com.lighttalk.mapper.UserMapper;
import com.lighttalk.model.dto.LoginResp;
import com.lighttalk.model.dto.UserLoginReq;
import com.lighttalk.model.dto.UserRegisterReq;
import com.lighttalk.model.entity.User;
import com.lighttalk.model.vo.UserVO;
import com.lighttalk.security.JwtUtils;
import com.lighttalk.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 *
 * 核心业务：
 * - register：校验用户名唯一 → BCrypt 加密密码 → 写库
 * - login：查用户 → BCrypt 匹配密码 → 生成 JWT → 封装响应
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // ───────────────────────────────────────────────────────
    // 注册
    // ───────────────────────────────────────────────────────

    /**
     * 用户注册
     * 1. 校验用户名唯一性
     * 2. BCrypt 加密密码
     * 3. 持久化用户数据（默认状态正常）
     *
     * @param req 注册请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReq req) {
        // 1. 检查用户名是否已存在（包含逻辑删除的用户也算占用）
        boolean exists = userMapper.exists(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername())
        );
        if (exists) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 构建用户实体
        User user = new User();
        user.setUsername(req.getUsername());
        // BCrypt 加密密码，每次加密结果不同（内置随机盐），安全可靠
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        // 默认头像可后续在个人中心修改
        user.setAvatar(null);
        // 默认状态：1-正常
        user.setStatus(1);

        // 3. 插入数据库（createTime/updateTime 由 MyMetaObjectHandler 自动填充）
        userMapper.insert(user);

        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());
    }

    // ───────────────────────────────────────────────────────
    // 登录
    // ───────────────────────────────────────────────────────

    /**
     * 用户登录
     * 1. 根据用户名查询用户（MyBatis Plus 会自动过滤逻辑删除记录）
     * 2. 校验用户状态（是否被禁言）
     * 3. BCrypt 匹配密码
     * 4. 生成 JWT Token
     * 5. 封装 LoginResp 返回
     *
     * @param req 登录请求
     * @return 含 token 和用户信息的响应
     */
    @Override
    public LoginResp login(UserLoginReq req) {
        // 1. 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername())
        );
        if (user == null) {
            // 不暴露"用户不存在"，统一提示，防止用户名枚举攻击
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 校验用户状态：禁言用户禁止登录
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_BANNED);
        }

        // 3. BCrypt 密码比对
        // passwordEncoder.matches(明文, 加密后) → 内部会提取盐值再加密对比
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 生成 JWT Token（载荷中存入 userId 和 username）
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 5. 组装用户 VO（过滤敏感字段，不返回密码）
        UserVO userVO = new UserVO();
        // BeanUtils.copyProperties 按属性名复制，自动过滤 User 中没有对应字段的属性
        BeanUtils.copyProperties(user, userVO);

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

        // 6. 封装并返回登录响应
        return new LoginResp(token, jwtUtils.getExpiration(), userVO);
    }
}
