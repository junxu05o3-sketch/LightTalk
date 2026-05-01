#include "room.h"
#include "session.h"

#include <vector>

Room::Room(std::string id) : id_(id) {}

void Room::Add(std::shared_ptr<Session> session) {
  std::lock_guard lk{mutex_};
  sessions_.insert(session);
}

void Room::Remove(std::shared_ptr<Session> session) {
  std::lock_guard lk{mutex_};
  sessions_.erase(session);
}

void Room::Broadcast(std::shared_ptr<std::string> msg) {
  std::vector<std::weak_ptr<Session>> dead;
  std::lock_guard lk{mutex_};
  for (const auto& wp : sessions_) {
    auto sp = wp.lock();
    if (sp) {
      sp->Send(msg);
    } else {
      dead.push_back(wp);
    }
  }
  for (const auto& wp : dead) sessions_.erase(wp);
}

std::size_t Room::Count() const {
  std::lock_guard lk{mutex_};
  return sessions_.size();
}

bool Room::Empty() const {
  std::lock_guard lk{mutex_};
  return sessions_.empty();
}