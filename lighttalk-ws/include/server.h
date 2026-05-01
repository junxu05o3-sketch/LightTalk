#pragma once

#include "noncopymoveable.h"
#include "room_manager.h"

#include <boost/asio.hpp>

namespace asio = boost::asio;
using tcp = asio::ip::tcp;

class Server : public NonCopyMoveable {
public:
  Server(asio::io_context& ioc, tcp::endpoint endpoint);
  void Run();
private:
  void do_accept();
  
  tcp::acceptor acceptor_;
  RoomManager room_manager_;
};