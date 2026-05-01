#pragma once

#include "noncopymoveable.h"

#include <boost/beast.hpp>
#include <boost/beast/websocket.hpp>
#include <boost/asio.hpp>
#include <memory>
#include <queue>
#include <string>

namespace beast = boost::beast;
namespace asio = boost::asio;
namespace websocket = beast::websocket;

using tcp = asio::ip::tcp;

class RoomManager;

class Session : public std::enable_shared_from_this<Session>, public NonCopyMoveable {
public:
  Session(tcp::socket socket, RoomManager& room_manager);
  ~Session();

  void Run();

  void Send(std::shared_ptr<const std::string> msg);
  void Send(const std::string& msg);
private:
  void do_read();

  void do_write();
  void on_write(beast::error_code ec, std::size_t bytes);

  void handle_message(const std::string& text);
  void cleanup();
  void leave_room();
private:
  websocket::stream<tcp::socket> ws_;
  beast::flat_buffer buf_;
  RoomManager& room_manager_;

  std::string nickname_;
  std::string room_id_;

  std::queue<std::shared_ptr<const std::string>> write_queue_;
  bool is_writing_{false};
  bool cleaned_up_{false};
};
