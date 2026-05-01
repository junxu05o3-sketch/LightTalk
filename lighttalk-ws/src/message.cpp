#include "message.h"
#include <nlohmann/json.hpp>

using json = nlohmann::json;

ClientMessage message::Parse(const std::string& raw) {
  ClientMessage msg;
  try {
    auto j = json::parse(raw);
    std::string type = j.value("type", "");

    if      (type == "chat") msg.type = MessageType::Chat;
    else if (type == "join") msg.type = MessageType::Join;
    else if (type == "ping") msg.type = MessageType::Ping;
    else msg.type = MessageType::Unknown;

    msg.roomId = j.value("roomId", "");
    msg.nickname = j.value("nickname", "");
    msg.content = j.value("content", "");
  } catch (...) {
    // 视为Unknown, 交给上层丢弃
  }
  return msg;
}

std::string message::MakeChat(const std::string& roomId, const std::string& nickname, const std::string& content, std::int64_t ts) {
  return json{
    {"type", "message"},
    {"roomId", roomId},
    {"nickname", nickname},
    {"content", content},
    {"timestamp", ts}
  }.dump();
}



std::string message::MakeSystem(const std::string& roomId, const std::string& content, std::int64_t ts) {
  return json{
    {"type", "system"},
    {"roomId", roomId},
    {"content", content},
    {"timestamp", ts}
  }.dump();
}

std::string message::MakeOnlineCount(const std::string& roomId, std::size_t count) {
  return json{
    {"type", "online_count"},
    {"roomId", roomId},
    {"count", count}
  }.dump();
}

std::string message::MakeError(const std::string& reason) {
  return json{{"type", "error"}, {"reason", reason}}.dump();
}