#pragma once

#include <memory>
#include <mutex>
#include <set>
#include <string>

class Session;

class Room{
public:
  explicit Room(std::string id);
  void Add(std::shared_ptr<Session> session);
  void Remove(std::shared_ptr<Session> session);
  // 向房间内所有存活连接广播消息
  void Broadcast(std::shared_ptr<std::string> msg);

  std::size_t Count() const;
  bool Empty() const;
  const std::string& Id() const { return id_; }
private:
  std::string id_;
  std::set<std::weak_ptr<Session>, std::owner_less<std::weak_ptr<Session>>> sessions_;
  mutable std::mutex mutex_;
};