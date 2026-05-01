cmake_minimum_required(VERSION 3.20)
project(lighttalk-ws CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

find_package(Boost 1.74 REQUIRED COMPONENTS system)
find_package(Threads REQUIRED)

find_package(nlohmann_json QUIET)
if(NOT nlohmann_json_FOUND)
    include(FetchContent)
    FetchContent_Declare(
        nlohmann_json
        URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz
    )
    FetchContent_MakeAvailable(nlohmann_json)
endif()

set(SOURCES
    src/main.cpp
    src/server.cpp
    src/session.cpp
    src/room.cpp
    src/room_manager.cpp
    src/message.cpp
    src/metrics.cpp
)

add_executable(lighttalk-ws ${SOURCES})

target_include_directories(lighttalk-ws PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${Boost_INCLUDE_DIRS}
)

target_link_libraries(lighttalk-ws PRIVATE
    Boost::system
    Threads::Threads
    nlohmann_json::nlohmann_json
)

target_compile_options(lighttalk-ws PRIVATE -Wall -Wextra)
