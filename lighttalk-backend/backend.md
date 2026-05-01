Java 后端 (Spring Boot) 目录树
遵循领域驱动设计（DDD）的雏形与传统 MVC 结合，突出工程规范。
```text
lighttalk-backend
├── src/main/java/com/lighttalk
│   ├── LightTalkApplication.java
│   ├── common              # 全局通用包
│   │   ├── api             # 统一响应结构 (Result)
│   │   ├── exception       # 全局异常处理 (GlobalExceptionHandler)
│   │   └── constants       # 全局常量 (RedisKeyConstants 等)
│   ├── config              # 配置类 (Swagger, Redis, WebMvc, Security)
│   ├── security            # 安全模块 (JWT 工具, 拦截器/过滤器)
│   ├── controller          # 表现层 (Admin, Auth, Room, Message)
│   ├── service             # 业务逻辑层 (接口与 impl 实现)
│   ├── mapper              # 数据访问层 (MyBatis Plus Mapper)
│   └── model               # 数据模型
│       ├── entity          # 数据库映射对象 (DO)
│       ├── dto             # 数据传输对象 (Req/Resp 封装)
│       └── vo              # 视图对象 (返回给前端的数据)
├── src/main/resources
│   ├── application.yml     # 核心配置 (MySQL, Redis, JWT 密钥)
│   └── mapper              # MyBatis XML 映射文件
└── docker                  # 容器化部署编排
    ├── docker-compose.yml  # MySQL + Redis 启动脚本
    └── init.sql            # 数据库初始化脚本

# LightTalk 后端开发任务逻辑 (Java Spring Boot)

# 🚀 LightTalk 后端开发指南 (Java Spring Boot)

## 1. 核心架构分析
本项目采用 **异构系统协作架构**：
- **Java 端**：作为业务中心，处理用户鉴权、房间元数据、历史消息持久化及管理后台。
- **C++ 端**：作为实时中转站，利用 Boost.Beast 处理高并发 WebSocket 长连接。
- **Redis 桥梁**：用于维护实时状态（如在线人数），实现 Java 与 C++ 的状态同步。

## 2. 数据库设计 (MySQL)
建立以下四张核心表，重点关注索引优化以适配简历技术点：

### 2.1 用户表 (user)
- `id`: BIGINT (Primary Key)
- `username`: VARCHAR(50) (Unique Index)
- `password`: VARCHAR(100) (BCrypt 加密)
- `nickname`: VARCHAR(50)
- `status`: TINYINT (1:正常, 0:禁言)

### 2.2 房间表 (room)
- `id`: BIGINT (Primary Key)
- `name`: VARCHAR(100)
- `owner_id`: BIGINT (关联用户 ID)

### 2.3 房间成员表 (room_member)
- `id`: BIGINT
- `room_id`: BIGINT (Index)
- `user_id`: BIGINT (Index)
- `role`: TINYINT (0:成员, 1:房主)
- **约束**: `UNIQUE(room_id, user_id)` 防止重复加入。

### 2.4 消息表 (message)
- `id`: BIGINT
- `room_id`: BIGINT
- `user_id`: BIGINT
- `content`: TEXT
- `type`: VARCHAR(20) (text/system)
- **索引**: 复合索引 `(room_id, create_time)` 优化历史消息分页。

## 3. 核心开发任务
### Phase 1: 工程化基础
- [ ] 配置 `Result<T>` 统一响应体与 `GlobalExceptionHandler`。
- [ ] 使用 Docker Compose 启动 MySQL 8.0 与 Redis。
- [ ] 集成 MyBatis Plus 实现基础 CRUD。

### Phase 2: 安全与鉴权
- [ ] 实现基于 **Spring Security + JWT** 的登录校验。
- [ ] 编写 `JwtInterceptor` 拦截器，除登录注册外所有接口需校验 Token。

### Phase 3: 业务逻辑深化
- [ ] **房间模块**: 实现加入房间逻辑（需校验重复性）。
- [ ] **消息模块**: 实现历史消息 **分页查询** (推荐 `id < lastId` 方案提升性能)。
- [ ] **状态模块**: 提供接口从 Redis 读取由 C++ 维护的房间在线人数。

### Phase 4: 管理后台
- [ ] 实现用户禁言、踢出接口（预留逻辑，通过修改数据库状态/发送 Redis 信号实现）。
