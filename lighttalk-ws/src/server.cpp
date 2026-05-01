#include "server.h"
#include "session.h"

#include <iostream>
#include <memory>

Server::Server(asio::io_context& ioc, tcp::endpoint endpoint) 
  : acceptor_(ioc, endpoint) {}

void Server::Run() {
  do_accept();
}

void Server::do_accept() {
  acceptor_.async_accept(
    [this](boost::system::error_code ec, tcp::socket socket){
      if (!ec) {
        std::cout << "[server] new tcp connection\n";
        std::make_shared<Session>(
          std::move(socket),
          room_manager_
        )->Run();
      } else {
        std::cerr << "[server] accepr error: " << ec.message() << "\n";
      }

      do_accept();
    }
  );
}