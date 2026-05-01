#include "room_manager.h"
#include "room.h"
#include "metrics.h"

#include <memory>

std::shared_ptr<Room> RoomManager::GetOrCreate(const std::string& id) {
  std::lock_guard lk{mutex_};
  auto it = rooms_.find(id);
  if (it != rooms_.end()) {
    return it->second;
  }

  auto room = std::make_shared<Room>(id);
  rooms_[id] = room;
  Metrics::Instance().OnRoomCreated();
  return room;
}

void RoomManager::RemoveIfEmpty(const std::string& id) {
  std::lock_guard lk{mutex_};
  auto it = rooms_.find(id);
  if (it == rooms_.end()) {
    return;
  }
  if (it->second->Empty()) {
    rooms_.erase(id);
    Metrics::Instance().OnRoomRemoved();
  }
}

std::size_t RoomManager::RoomCount() const {
  std::lock_guard lk{mutex_};
  return rooms_.size();
}