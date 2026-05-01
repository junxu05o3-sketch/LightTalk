第一代版本

我要开发一个名为 LightTalk 的轻量级实时通信系统，我负责 Java Spring Boot 后端和 Vue3 前端。另一个同学负责 C++ Boost.Beast WebSocket 长连接服务。

请你作为一名资深 Java 全栈工程师，帮助我设计并实现这个项目，使它适合写进 Java 后端 / 全栈开发简历。

项目定位：
LightTalk 是一个轻量级实时聊天系统，支持用户登录、房间聊天、历史消息、在线人数统计和管理后台。实时消息通过 C++ WebSocket 服务完成，Java 后端负责业务系统，Vue3 负责页面展示。

我的技术栈：
前端：
- Vue3
- TypeScript
- Vite
- Pinia
- Vue Router
- Element Plus
- Axios

后端：
- Spring Boot
- Spring Security
- JWT
- MyBatis Plus
- MySQL
- Redis
- Swagger / Knife4j
- Docker Compose

整体架构：
Vue3 前端：
1. HTTP 请求 Java 后端，用于登录、注册、房间列表、历史消息、管理后台
2. WebSocket 直接连接 C++ 服务，用于实时聊天

Java 后端：
1. 用户认证
2. 房间管理
3. 历史消息存储
4. 在线状态维护
5. 管理后台接口

C++ WebSocket 服务：
1. 长连接管理
2. 房间广播
3. 在线人数统计
4. 心跳检测

请帮我设计并实现以下模块：

后端模块：
1. 用户模块
- 用户注册
- 用户登录
- 密码加密
- JWT token 生成与校验
- 用户信息查询

2. 房间模块
- 创建房间
- 查询房间列表
- 查询房间详情
- 加入房间
- 房间成员记录

3. 消息模块
- 聊天消息落库
- 历史消息分页查询
- 按 roomId 查询消息
- 消息类型支持 text / system

4. 在线状态模块
- 使用 Redis 存储在线用户
- 使用 Redis 存储房间在线人数
- 查询用户在线状态
- 查询房间在线人数

5. 管理后台模块
- 用户列表
- 房间列表
- 消息查询
- 禁言用户
- 踢出用户接口预留

6. 工程化
- 统一响应 Result
- 全局异常处理
- 参数校验
- 登录拦截器 / JWT 过滤器
- Swagger / Knife4j 文档
- Docker Compose 启动 MySQL 和 Redis
- README 部署文档

数据库表设计：
1. user
2. room
3. room_member
4. message

前端页面：
1. 登录页
2. 注册页
3. 房间列表页
4. 聊天页
5. 历史消息页
6. 管理后台页

聊天页要求：
1. 用户登录后进入房间
2. 通过 HTTP 从 Java 后端拉取历史消息
3. 通过 WebSocket 连接 C++ 服务
4. 发送 join 消息
5. 发送 chat 消息
6. 展示服务端广播 message
7. 展示在线人数 online_count
8. 支持断线重连提示

WebSocket 协议：

连接地址：
ws://localhost:9000/ws

客户端 join：
{
"type": "join",
"roomId": "cpp",
"nickname": "akiba"
}

客户端 chat：
{
"type": "chat",
"roomId": "cpp",
"content": "hello"
}

服务端 message：
{
"type": "message",
"roomId": "cpp",
"nickname": "akiba",
"content": "hello",
"timestamp": 1710000000
}

服务端 online_count：
{
"type": "online_count",
"roomId": "cpp",
"count": 3
}

要求：
1. 不要只做 CRUD，要突出 Java 后端工程能力
2. 重点体现 JWT、Redis、MySQL、分页查询、统一异常、接口文档、Docker 部署
3. 代码要适合新手逐步实现
4. 每个阶段给出明确任务
5. 最终项目要能写进简历
