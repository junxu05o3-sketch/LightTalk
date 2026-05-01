package com.lighttalk.common.constants;

/**
 * Redis Key 常量类
 * 定义系统中使用的所有 Redis Key 前缀和格式
 */
public class RedisKeyConstants {

    /**
     * Key 前缀
     */
    private static final String PREFIX = "lighttalk:";

    /**
     * 用户在线状态 Key
     * 格式: lighttalk:user:online:{userId}
     * 值: 1 (在线) / 0 (离线)
     */
    public static final String USER_ONLINE_KEY = PREFIX + "user:online:";

    /**
     * 获取用户在线状态 Key
     */
    public static String getUserOnlineKey(Long userId) {
        return USER_ONLINE_KEY + userId;
    }

    /**
     * 房间在线人数 Key
     * 格式: lighttalk:room:online:{roomId}
     * 值: 在线人数 (整数)
     */
    public static final String ROOM_ONLINE_KEY = PREFIX + "room:online:";

    /**
     * 获取房间在线人数 Key
     */
    public static String getRoomOnlineKey(Long roomId) {
        return ROOM_ONLINE_KEY + roomId;
    }

    /**
     * 房间在线用户集合 Key
     * 格式: lighttalk:room:users:{roomId}
     * 值: Set<userId> (在线用户ID集合)
     */
    public static final String ROOM_USERS_KEY = PREFIX + "room:users:";

    /**
     * 获取房间在线用户集合 Key
     */
    public static String getRoomUsersKey(Long roomId) {
        return ROOM_USERS_KEY + roomId;
    }

    /**
     * 用户 Token Key
     * 格式: lighttalk:user:token:{userId}
     * 值: JWT Token
     */
    public static final String USER_TOKEN_KEY = PREFIX + "user:token:";

    /**
     * 获取用户 Token Key
     */
    public static String getUserTokenKey(Long userId) {
        return USER_TOKEN_KEY + userId;
    }

    /**
     * 用户禁言状态 Key
     * 格式: lighttalk:user:banned:{userId}
     * 值: 1 (禁言) / 0 (正常)
     */
    public static final String USER_BANNED_KEY = PREFIX + "user:banned:";

    /**
     * 获取用户禁言状态 Key
     */
    public static String getUserBannedKey(Long userId) {
        return USER_BANNED_KEY + userId;
    }

    /**
     * 默认过期时间（秒）
     */
    public static final long DEFAULT_EXPIRE_TIME = 3600; // 1小时

    /**
     * Token 过期时间（秒）
     */
    public static final long TOKEN_EXPIRE_TIME = 86400; // 24小时

    /**
     * 在线状态过期时间（秒）
     */
    public static final long ONLINE_EXPIRE_TIME = 300; // 5分钟
}