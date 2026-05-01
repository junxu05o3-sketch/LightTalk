# LightTalk WebSocket API

本文档面向前端和 Java 客户端开发者，描述 LightTalk C++ WebSocket 服务的连接方式、消息协议和对接约定。

## 连接信息

默认地址：

```text
ws://localhost:9000/ws
```

生产或联调环境只需要替换 host 和 port：

```text
ws://<server-host>:<port>/ws
```

当前服务端不做登录、鉴权、数据库和历史消息存储。客户端连接后必须先发送 `join`，再发送 `chat`。

## 数据格式

所有业务消息都使用 UTF-8 JSON 文本帧。

字段命名采用 camelCase，例如 `roomId`。服务端不会保存用户身份，`nickname` 由客户端在 `join` 时传入。

## 客户端消息

### 加入房间 join

客户端连接成功后发送：

```json
{
  "type": "join",
  "roomId": "cpp",
  "nickname": "akiba"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 是 | 固定为 `join` |
| `roomId` | string | 是 | 房间 ID，例如 `cpp` |
| `nickname` | string | 是 | 当前连接在房间内展示的昵称 |

规则：

- `roomId` 和 `nickname` 不能为空。
- 一个 WebSocket 连接当前只属于一个房间。
- 如果已经在房间 A，再 `join` 房间 B，服务端会先退出房间 A，再加入房间 B。
- 加入成功后，房间内会收到 `system` 和 `online_count`。

### 发送聊天 chat

加入房间后发送：

```json
{
  "type": "chat",
  "roomId": "cpp",
  "content": "hello"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 是 | 固定为 `chat` |
| `roomId` | string | 是 | 必须等于当前已加入的房间 ID |
| `content` | string | 是 | 聊天内容 |

规则：

- 必须先发送 `join`。
- `roomId` 必须和当前连接已加入的房间一致。
- 服务端收到后向同房间所有在线连接广播 `message`。
- 第一版不做内容审核、长度限制、敏感词过滤和消息持久化。

### 应用层心跳 ping

当前预留格式：

```json
{
  "type": "ping"
}
```

第一版服务端不会返回应用层 `pong`。WebSocket 协议层 ping/pong 由 Boost.Beast 自动处理。业务心跳会在后续版本补齐。

## 服务端消息

### 聊天消息 message

```json
{
  "type": "message",
  "roomId": "cpp",
  "nickname": "akiba",
  "content": "hello",
  "timestamp": 1710000000
}
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | string | 固定为 `message` |
| `roomId` | string | 房间 ID |
| `nickname` | string | 发送者昵称 |
| `content` | string | 聊天内容 |
| `timestamp` | number | 服务端 Unix 秒级时间戳 |

### 在线人数 online_count

```json
{
  "type": "online_count",
  "roomId": "cpp",
  "count": 3
}
```

触发时机：

- 有连接加入房间。
- 有连接断开并离开房间。
- 有连接从当前房间切换到其他房间。

### 系统消息 system

```json
{
  "type": "system",
  "roomId": "cpp",
  "content": "akiba joined the room",
  "timestamp": 1710000000
}
```

当前系统消息用于提示用户加入和离开房间。前端可以选择展示，也可以只用于调试。

### 错误消息 error

```json
{
  "type": "error",
  "reason": "roomId and nickname required."
}
```

常见错误：

| reason | 说明 |
|--------|------|
| `roomId and nickname required.` | `join` 缺少房间 ID 或昵称 |
| `join a room before chat.` | 未加入房间就发送聊天 |
| `chat roomId must match joined room.` | 聊天消息的房间 ID 和当前房间不一致 |
| `unknown message` | JSON 非法、`type` 未知或格式无法识别 |

## 前端示例

```javascript
const ws = new WebSocket("ws://localhost:9000/ws");

ws.onopen = () => {
  ws.send(JSON.stringify({
    type: "join",
    roomId: "cpp",
    nickname: "akiba"
  }));
};

ws.onmessage = event => {
  const message = JSON.parse(event.data);
  console.log(message);
};

function sendChat(content) {
  ws.send(JSON.stringify({
    type: "chat",
    roomId: "cpp",
    content
  }));
}
```

也可以直接打开项目内测试页：

```text
examples/chat.html
```

## Java 对接示例

下面是 Java 客户端需要发送的 JSON。具体 WebSocket 客户端库可以使用 Java 11 `java.net.http.WebSocket`、Spring WebSocket 或 OkHttp。

连接地址：

```text
ws://localhost:9000/ws
```

连接成功后先发送：

```json
{"type":"join","roomId":"cpp","nickname":"java-user"}
```

之后发送聊天：

```json
{"type":"chat","roomId":"cpp","content":"hello from java"}
```

收到服务端消息后按 `type` 分发：

| type | 客户端处理建议 |
|------|----------------|
| `message` | 添加到聊天消息列表 |
| `online_count` | 更新房间在线人数 |
| `system` | 展示系统提示或写入调试日志 |
| `error` | 展示错误提示或触发重试逻辑 |

## 当前限制

- 无 TLS，当前是 `ws://`，不是 `wss://`。
- 无鉴权，任何客户端都可以连接。
- 无数据库，不保存历史消息。
- 无离线消息。
- 一个连接同一时间只在一个房间。
- 服务端当前不校验 WebSocket HTTP path，但客户端统一使用 `/ws`。
- 服务端不保证消息跨进程顺序，因为第一版是单进程版本，后续多进程需要引入消息中间件。

## 推荐客户端流程

1. 创建 WebSocket 连接。
2. `onopen` 后立即发送 `join`。
3. 收到 `online_count` 后更新人数。
4. 用户点击发送时发送 `chat`。
5. 收到 `message` 后追加到聊天列表。
6. 收到 `error` 后提示用户或写入日志。
7. 连接关闭后由客户端决定是否重连。
