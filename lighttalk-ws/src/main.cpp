#include "server.h"

#include <boost/asio.hpp>
#include <iostream>
#include <stdexcept>

namespace asio = boost::asio;
using tcp = asio::ip::tcp;

int main(int argc, char* argv[]) {
  unsigned short port = 9000;
  if (argc > 1) {
    try {
        int value = std::stoi(argv[1]);
        if (value <= 0 || value > 65536) {
          throw std::out_of_range("port out of range");
        }
        port = static_cast<unsigned int>(std::stoi(argv[1]));
    } catch(...) {
        std::cerr << "invalid port, using 9090\n";
    }
  }

  try {
    asio::io_context ioc;
    // 信号处理
    asio::signal_set signals(ioc, SIGINT, SIGTERM);
    signals.async_wait([&](auto, auto){
      std::cout << "\n[main]shutting down\n";
      ioc.stop();
    });

    Server server(ioc, tcp::endpoint(tcp::v4(), port));
    server.Run();

    std::cout << "[main]LightTalk-WS on ws://localhost:" << port << "/ws\n";
    
    ioc.run();
  } catch(const std::exception& e) {
    std::cerr << "[fatal]" << e.what() << "\n";
    return 1;
  }

  return 0;
}
