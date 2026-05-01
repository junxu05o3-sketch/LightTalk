#include "metrics.h"
#include <nlohmann/json.hpp>

Metrics& Metrics::Instance() {
  static Metrics instance;
  return instance;
}

void Metrics::OnConnect()      { ++total_connections_; ++active_connections_; }
void Metrics::OnDisConnect()   { --active_connections_; }
void Metrics::OnMessage()      { ++total_messages_; }
void Metrics::OnRoomCreated()  { ++active_rooms_; }
void Metrics::OnRoomRemoved()  { --active_rooms_; }

std::string Metrics::Snapshot() const {
  return nlohmann::json{
    {"total_connections",  total_connections_.load()},
    {"active_connections", active_connections_.load()},
    {"total_messages",     total_messages_.load()},
    {"active_rooms",       active_rooms_.load()}
  }.dump();
}
