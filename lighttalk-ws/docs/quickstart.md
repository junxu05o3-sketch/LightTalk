# LightTalk-WS 快速上手

C++17 WebSocket 聊天服务器，基于 Boost.Beast + Boost.Asio。

---

## 已实现功能

| 功能 | 说明 |
|------|------|
| 多房间聊天 | 客户端加入指定 `roomId`，消息只在同一房间广播 |
| 加入/退出通知 | 用户加入或断开连接时，房间内所有人收到系统消息 |
| 在线人数推送 | 每次人数变化后广播 `online_count` 给房间内所有人 |
| 异步写队列 | `write_queue_` 串行化 `async_write`，避免 Beast 并发写 UB |
| 连接生命周期管理 | `shared_ptr` + `enable_shared_from_this`，断开或读写错误时自动离开房间 |
| 运行时指标 | 原子计数器记录总连接数、活跃连接、总消息数、活跃房间数 |
| 优雅退出 | 捕获 `SIGINT` / `SIGTERM`，调用 `ioc.stop()` 停止事件循环 |

---

## 依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Boost | >= 1.74 | Asio（异步 I/O）、Beast（WebSocket） |
| nlohmann/json | 3.11.3（自动下载） | JSON 序列化/反序列化 |
| CMake | >= 3.20 | 构建 |

---

## 构建

```bash
cd lighttalk-ws

# 配置（首次会下载 nlohmann/json）
cmake -B build -DCMAKE_BUILD_TYPE=Debug

# 编译
cmake --build build -j$(nproc)
```

产物：`build/lighttalk-ws`

---

## 运行

```bash
# 默认端口 9000
./build/lighttalk-ws

# 推荐指定端口
./build/lighttalk-ws [port]
# 指定 8888
./build/lighttalk-ws 8888
```

启动成功输出：

```
[main]LightTalk-WS on ws://localhost:9000
```

按 `Ctrl+C` 优雅退出：

```
[main]shutting down
```

---

## WebSocket 地址

默认连接地址：

```text
ws://localhost:9000/ws
```

当前服务端不做登录、鉴权、数据库和历史消息存储。客户端连接后必须先发送 `join`，再发送 `chat`。

完整对接协议见 [`docs/websocket_api.md`](websocket_api.md)。

---

## 消息协议摘要

### 客户端 → 服务端

#### 加入房间

```json
{
  "type": "join",
  "roomId": "room1",
  "nickname": "Felix"
}
```

- `roomId` 和 `nickname` 均不能为空，否则返回 `error`
- 同一连接同一时间只属于一个房间
- 如果再次 join 到另一个房间，服务端会先退出旧房间，再加入新房间

#### 发送聊天

```json
{
  "type": "chat",
  "roomId": "room1",
  "content": "hello world"
}
```

- 必须先 join，否则返回 `error`
- `roomId` 必须等于当前已加入的房间

#### 心跳

```json
{
  "type": "ping"
}
```

- 服务端当前不回复 pong（Beast 自动处理 WebSocket 协议层 ping/pong 帧）

---

### 服务端 → 客户端

#### 聊天消息

```json
{
  "type": "message",
  "roomId": "room1",
  "nickname": "Felix",
  "content": "hello world",
  "timestamp": 1746057600
}
```

#### 系统通知

```json
{
  "type": "system",
  "roomId": "room1",
  "content": "Felix joined the room",
  "timestamp": 1746057600
}
```

触发时机：用户加入房间、用户断开连接

#### 在线人数

```json
{
  "type": "online_count",
  "roomId": "room1",
  "count": 3
}
```

触发时机：每次人数变化（join / leave）

#### 错误

```json
{
  "type": "error",
  "reason": "roomId and nickname required."
}
```

---

## 快速测试

### 方式一：浏览器控制台

打开任意网页，F12 → Console：

```javascript
const ws = new WebSocket("ws://localhost:9000/ws");

ws.onopen = () => {
  ws.send(JSON.stringify({ type: "join", roomId: "room1", nickname: "Felix" }));
};

ws.onmessage = e => console.log(JSON.parse(e.data));

// 加入后发消息
ws.send(JSON.stringify({ type: "chat", roomId: "room1", content: "hello!" }));
```

### 方式二：wscat 命令行

```bash
npm install -g wscat
wscat -c ws://localhost:9000/ws
```

连接成功后逐行发送：

```json
{"type":"join","roomId":"room1","nickname":"Felix"}
{"type":"chat","roomId":"room1","content":"hello!"}
```

### 方式三：测试页面

直接用浏览器打开：

```text
examples/chat.html
```

默认地址已经是 `ws://localhost:9000/ws`。

---

## 目录结构

```
lighttalk-ws/
├── CMakeLists.txt
├── include/
│   ├── message.h        消息类型枚举 + JSON 工厂函数声明
│   ├── metrics.h        运行时指标单例
│   ├── room.h           单房间：Session 集合 + 广播
│   ├── room_manager.h   房间 map，线程安全
│   ├── session.h        单 WebSocket 连接生命周期
│   └── server.h         TCP acceptor 循环
├── src/
│   ├── main.cpp         入口：解析端口 → 启动 Server
│   ├── message.cpp      JSON 序列化/反序列化
│   ├── metrics.cpp      Metrics 单例实现
│   ├── room.cpp         广播 + 清理失效 weak_ptr
│   ├── room_manager.cpp 房间创建/删除
│   ├── session.cpp      async_read / async_write 队列
│   └── server.cpp       async_accept 循环
├── docs/
│   ├── quickstart.md    本文件
│   └── websocket_api.md 完整 API 协议文档
└── examples/
    └── chat.html        浏览器联调页面
```

---

## 关键设计说明

**为什么 Room 用 `weak_ptr<Session>`？**
Room 广播时需要持有 Session 引用，若用 `shared_ptr` 会形成循环引用（Session → Room → Session），导致内存泄漏。用 `weak_ptr` + `lock()` 在广播时检查连接是否还存活，失效的直接清理。

**为什么需要 write_queue_？**
Boost.Beast 禁止并发调用 `async_write`，必须等上一次写完再发下一条。`write_queue_` + `is_writing_` flag 保证串行投递，所有队列操作都在单线程 `io_context` 内，无需加锁。

**Session 什么时候清理房间？**
每个异步回调持有 `shared_from_this()`，只要还有回调在飞，Session 就不会析构。连接关闭或读写错误时，Session 会调用 `cleanup()`，主动离开房间并广播最新在线人数；析构阶段只扣减连接指标。
