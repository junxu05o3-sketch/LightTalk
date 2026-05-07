# 🌌 LightTalk 

<div align="center">
  <p><strong>基于多语言异构的长连接 IM 系统 (Java + C++ + Vue3)</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Java-17-orange.svg?style=flat-square&logo=java" alt="Java" />
    <img src="https://img.shields.io/badge/C++-17-blue.svg?style=flat-square&logo=c%2B%2B" alt="C++" />
    <img src="https://img.shields.io/badge/Vue-3.0-brightgreen.svg?style=flat-square&logo=vuedotjs" alt="Vue" />
    <img src="https://img.shields.io/badge/Spring%20Boot-2.7-green.svg?style=flat-square&logo=springboot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/Redis-6.2-red.svg?style=flat-square&logo=redis" alt="Redis" />
    <img src="https://img.shields.io/badge/Docker-Compose-blue.svg?style=flat-square&logo=docker" alt="Docker" />
  </p>
</div>

---

## 🎯 项目定位 (Project Positioning)

LightTalk 是一款**追求极致架构分离与全栈工程体验**的实时聊天应用。
为了解决传统 IM 系统中业务耦合、WebSocket 扛并发难、长连接状态同步复杂等痛点，本项目开创性地采用了 **“多语言异构架构”**：
- **Java (Spring Boot)** 作为坚实的 HTTP 业务底座，负责用户认证、房间元数据管理以及消息持久化。
- **C++ (Boost.Beast)** 作为纯异步的高性能 WebSocket 网关，剥离所有业务逻辑，只负责长连接维持与网络 I/O 分发。
- **Vue3 (TypeScript)** 配合玻璃拟态 UI 提供极佳的前端交互体验。
- **Redis** 作为两者之间的通信枢纽，以优雅的无状态设计实现了“跨语言状态解耦”。

## 🛠 核心技术栈看板 (Core Tech Stack)

| 领域 | 核心技术选型 | 技术作用解析 |
| --- | --- | --- |
| **Frontend** | Vue3, Vite, TypeScript, Pinia, Element Plus | 组合式 API 管理视图，Pinia 状态持久化，指数退避重连的原生 WebSocket 客户端。 |
| **Backend** | Java 17, Spring Boot, MyBatis Plus, JWT | 承载 RESTful 接口，RBAC / JWT 无状态防越权拦截，复杂关系型数据落库。 |
| **Gateway** | C++17, Boost.Asio, Boost.Beast | Epoll 纯异步网络模型，剥离业务，实现百万级长连接维持与实时帧推送。 |
| **Storage** | MySQL 8.0, Redis 6.2 | MySQL 保障消息持久性，Redis 做高速缓存层及跨端事件总线（在线状态、禁言黑名单）。 |

## 💡 架构亮点深度解析 (Architecture Highlights)

### 1. 跨语言无状态解耦 (Stateless Multilingual Decoupling)
> *如何让 Java 业务线与 C++ 网关实现零 RPC 通信的完美协同？*
- **秒级全网禁言拦截**：当管理员在后台触发对某用户的禁言时，Java 端更新 DB 的同时向 Redis 写入黑名单（`lighttalk:user:muted:{userId}`）。C++ 收到客户端上行发包时，仅需查 Redis，发现命中规则立刻阻断广播，实现了**双端秒级封禁**。
- **在线状态共享**：C++ 仅负责在用户 WebSocket 连上或断开时变更 Redis 的在线人数 Counter。前端需要展示人数时，直接通过 HTTP 问询 Java。双方业务完全解耦，任一组件宕机均不互相阻塞。

### 2. 游标分页 (Cursor Pagination)
> *彻底解决 IM 场景下的深分页和“新消息滑入导致重复”的问题*
- 在历史记录拉取中，放弃了传统的 `LIMIT offset, size`，转而使用 `WHERE id < {lastMsgId} ORDER BY id DESC LIMIT size`。
- **防内存漏/防穿透**：避免了 MySQL 在深度翻页时因 Offset 过大导致的全表扫描，不论漫游到多远的历史，查询耗时永远保持常数级。

### 3. 长短链接双写机制
- 当发送消息时，前端同时向 C++（长连接）发射实时广播包，再向 Java（短连接）提交持久化落库请求。
- 结合前端的**指数退避重连（Exponential Backoff）算法**，即便网络发生瞬断，也不会引发重连风暴，确保体验平滑。

## 📁 目录规约 (Monorepo Structure)

本项目是一个标准的 Monorepo，各端代码完全独立，切勿混淆嵌套：
```text
LightTalk/
├── lighttalk-backend/      # ☕ Java Spring Boot 核心业务层
│   ├── docker/             # Docker Compose 脚本与 MySQL init.sql
│   ├── src/main/java/...   # Controller, Service, Mapper, Security
│   └── pom.xml
├── lighttalk-ws/           # 🚀 C++ 长连接网关 (待完善/对接)
├── lighttalk-frontend/     # 🎨 Vue3 前端视图层
│   ├── src/api/            # 接口封装
│   ├── src/views/          # Login, Room, Chat, Admin 等视图
│   └── vite.config.ts
└── README.md
```

## 🚀 极简启动指南 (Quick Start)

### 1. 中间件与数据库 (Docker)
```bash
cd lighttalk-backend/docker
docker-compose up -d
# MySQL 映射到 3307，Redis 映射到 6379。会自动执行 init.sql 创建 admin 超管 (密码: 123456)。
```

### 2. 后端服务 (Java)
```bash
cd lighttalk-backend
mvn clean install
java -jar target/lighttalk-backend-1.0.jar
# HTTP 业务启动于 localhost:8080
```

### 3. 前端服务 (Vue3)
```bash
cd lighttalk-frontend
npm install
npm run dev
# 前端启动于 localhost:3000
```
> *注: WebSocket C++ 网关启动方式请参考 `lighttalk-ws` 下的 README（预计运行在 9000 端口）。在开发期间如果不启动 C++ 网关，前端重连机制会自动触发，但不影响 HTTP 接口测试。*

## 🗺 演进路线图 (Roadmap V2.0)

- [ ] **微服务化/MQ 削峰**：引入 RabbitMQ/Kafka 缓冲消息队列，防止极端海量并发打穿 Java 数据库。
- [ ] **多端多生态扩展**：开发基于 C# / .NET 的桌面端 SDK 以及移动端 App，彻底验证 C++ 网关的通用分发能力。
- [ ] **读写分离**：利用 Redis ZSet 进一步优化热点房间的拉取性能。

---

*“用架构解决问题，用代码改变世界。” —— LightTalk Team 敬上*
