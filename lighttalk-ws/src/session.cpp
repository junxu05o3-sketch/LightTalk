#include "session.h"
#include "room_manager.h"
#include "room.h"
#include "message.h"
#include "metrics.h"
#include <iostream>
#include <chrono>

Session::Session(tcp::socket socket, RoomManager& room_manager)
  : ws_(std::move(socket)), room_manager_(room_manager) {
  Metrics::Instance().OnConnect();
}

Session::~Session() {
  Metrics::Instance().OnDisConnect();
}

void Session::Run() {
  // WebSocket 握手： HTTP Upgrade -> WS
  auto self = shared_from_this();
  ws_.async_accept(
    [self](beast::error_code ec) {
      if (ec) {
        std::cerr << "[session] accept: " << ec.message() << "\n";
        return;
      }
      self->do_read();
    }
  );
}

void Session::do_read() {
  buf_.clear();
  auto self = shared_from_this();
  ws_.async_read(buf_,
  [self](beast::error_code ec, std::size_t){
    if (ec == beast::websocket::error::closed) {
      self->cleanup();
      return;
    }
    if (ec) {
      std::cerr << "[session] read: " << ec.message() << "\n";
      self->cleanup();
      return;
    }  
    self->handle_message(beast::buffers_to_string(self->buf_.data()));
    self->do_read();
  });
}

void Session::handle_message(const std::string& text) {
  auto msg = message::Parse(text);
  auto ts  = std::chrono::duration_cast<std::chrono::seconds>(
    std::chrono::system_clock::now().time_since_epoch()).count();
  
    switch (msg.type) {
    case MessageType::Join: {
      if (msg.roomId.empty() || msg.nickname.empty()) {
        Send(std::make_shared<std::string>(message::MakeError("roomId and nickname required.")));
        return;
      }
      if (!room_id_.empty() && room_id_ != msg.roomId) {
        leave_room();
      }
      room_id_ = msg.roomId;
      nickname_= msg.nickname;

      auto room = room_manager_.GetOrCreate(room_id_);
      room->Add(shared_from_this());

      room->Broadcast(std::make_shared<std::string>
      (message::MakeSystem(room_id_, nickname_ + " joined the room", ts)));
      room->Broadcast(std::make_shared<std::string>
      (message::MakeOnlineCount(room_id_, room->Count())));
      break;
    }
    case MessageType::Chat: {
      if (room_id_.empty()) {
        Send(std::make_shared<std::string>(message::MakeError("join a room before chat.")));
        return;
      }
      if (msg.roomId.empty() || msg.roomId != room_id_) {
        Send(std::make_shared<std::string>(message::MakeError("chat roomId must match joined room.")));
        return;
      }
      Metrics::Instance().OnMessage();

      auto room = room_manager_.GetOrCreate(room_id_);
      room->Broadcast(std::make_shared<std::string>
     (message::MakeChat(room_id_, nickname_, msg.content, ts)));
      break;
    }
    case MessageType::Ping:
      // 预留心跳响应 (当前 Beast 会自动处理 WS ping/pon 帧)
      break;
    default:
      Send(std::make_shared<std::string>(message::MakeError("unknown message")));
      break;
    }
}

void Session::Send(std::shared_ptr<const std::string> msg) {
  write_queue_.push(msg);
  if (!is_writing_) do_write();
}

void Session::Send(const std::string& msg) {
  return Send(std::make_shared<std::string>(msg));
}
void Session::do_write() {
  if (write_queue_.empty()) { is_writing_ = false; return; }
  is_writing_ = true;

  auto self = shared_from_this();
  ws_.async_write(asio::buffer(*write_queue_.front()),
  [self](beast::error_code ec, std::size_t){
    self->write_queue_.pop();
    if (ec) {
        std::cerr << "[session] write: " << ec.message() << "\n";
        self->cleanup();
        return;
    }
    self->do_write();
  });
}

void Session::cleanup() {
  if (cleaned_up_) {
    return;
  }
  cleaned_up_ = true;
  leave_room();
}

void Session::leave_room() {
  if (room_id_.empty()) {
    return;
  }
  auto ts = std::chrono::duration_cast<std::chrono::seconds>(
    std::chrono::system_clock::now().time_since_epoch()).count();
  auto room = room_manager_.GetOrCreate(room_id_);
  room->Remove(shared_from_this());
  room->Broadcast(std::make_shared<std::string>(
    message::MakeSystem(room_id_, nickname_ + " left the room", ts)));
  room->Broadcast(std::make_shared<std::string>(
    message::MakeOnlineCount(room_id_, room->Count())));

  room_manager_.RemoveIfEmpty(room_id_);
  room_id_.clear();
}
