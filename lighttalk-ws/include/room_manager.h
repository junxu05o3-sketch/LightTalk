#pragma once

#include <memory>
#include <mutex>
#include <string>
#include <unordered_map>

class Room;

class RoomManager {
public:
  // 不存在则创建， 线程安全
  std::shared_ptr<Room> GetOrCreate(const std::string& id);
  void RemoveIfEmpty(const std::string& id);
  std::size_t RoomCount() const;
private:
  std::unordered_map<std::string, std::shared_ptr<Room>> rooms_;
  mutable std::mutex mutex_;
};