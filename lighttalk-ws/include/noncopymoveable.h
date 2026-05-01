#pragma once

class NonCopyMoveable{
public:
 NonCopyMoveable(const NonCopyMoveable&) = delete;
 NonCopyMoveable& operator=(const NonCopyMoveable&) = delete;
 NonCopyMoveable(const NonCopyMoveable&&) = delete;
 NonCopyMoveable& operator=(const NonCopyMoveable&&) = delete;
protected:
 NonCopyMoveable() = default;
 ~NonCopyMoveable() = default;
};