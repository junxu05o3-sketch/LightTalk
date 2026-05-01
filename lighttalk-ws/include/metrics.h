#pragma once

#include "noncopymoveable.h"

#include <atomic>
#include <cstdint>
#include <string>

// 单例指标统计器，使用原子计数保证并发安全，适合高频轻量统计。
class Metrics : public NonCopyMoveable
{
public:
    static Metrics &Instance();
    void OnConnect();
    void OnDisConnect();
    void OnMessage();
    void OnRoomCreated();
    void OnRoomRemoved();

    // 返回 json 字符串供 /metrics 接口使用
    std::string Snapshot() const;

private:
    Metrics() = default;

    std::atomic<std::uint64_t> total_connections_{0};
    std::atomic<std::uint64_t> active_connections_{0};
    std::atomic<std::uint64_t> total_messages_{0};
    std::atomic<std::uint64_t> active_rooms_{0};
};