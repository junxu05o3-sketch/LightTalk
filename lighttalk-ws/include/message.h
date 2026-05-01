#pragma once

#include <cstddef>
#include <cstdint>
#include <string>

// 客户端发给服务端的消息类型
enum class MessageType {
  Join,     // 加入房间
  Chat,     // 聊天消息
  Ping,     // 心跳消息
  Unknown   // 未知类型 / JSON 解析失败 / 字段不合法
};

// 客户端消息结构体
// 用于承载客户端 JSON 解析后的结果
struct ClientMessage {
  MessageType type{MessageType::Unknown};

  std::string roomId;    // Join / Chat / Ping 都可能需要
  std::string nickname;  // Join 时有效
  std::string content;   // Chat 时有效
};

namespace message {

// 解析客户端发来的 JSON 字符串。
// 解析失败或字段不合法时，返回 type == MessageType::Unknown。
ClientMessage Parse(const std::string& json);

// 生成服务端广播的聊天消息 JSON。
std::string MakeChat(
    const std::string& roomId,
    const std::string& nickname,
    const std::string& content,
    std::int64_t timestamp);

// 生成服务端系统消息 JSON。
// 例如：用户加入、用户退出、房间提示等。
std::string MakeSystem(
    const std::string& roomId,
    const std::string& content,
    std::int64_t timestamp);

// 生成在线人数变化消息 JSON。
std::string MakeOnlineCount(
    const std::string& roomId,
    std::size_t count);

// 生成错误消息 JSON。
// 例如：非法 JSON、未加入房间、房间不存在等。
std::string MakeError(const std::string& reason);

}  // namespace message

// 客户端格式

// Join:
// {
//   "type": "join",
//   "roomId": "room1",
//   "nickname": "Felix"
// }
//
// Chat:
// {
//   "type": "chat",
//   "roomId": "room1",
//   "content": "hello"
// }
//
// Ping:
// {
//   "type": "ping"
// }


// 服务端格式

// Chat:
// {
//   "type": "chat",
//   "roomId": "room1",
//   "nickname": "Felix",
//   "content": "hello",
//   "timestamp": 1710000000
// }
//
// System:
// {
//   "type": "system",
//   "roomId": "room1",
//   "content": "Felix joined",
//   "timestamp": 1710000000
// }
//
// OnlineCount:
// {
//   "type": "online_count",
//   "roomId": "room1",
//   "count": 3
// }
//
// Error:
// {
//   "type": "error",
//   "reason": "invalid json"
// }