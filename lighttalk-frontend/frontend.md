```text
lighttalk-frontend
├── src
│   ├── api                 # Axios 实例及各模块接口定义 (auth.ts, room.ts)
│   ├── assets              # 静态资源
│   ├── components          # 基础/复用组件 (ChatBubble, RoomCard)
│   ├── layout              # 页面布局 (Sidebar, Header)
│   ├── router              # Vue Router 路由配置
│   ├── store               # Pinia 状态管理 (userStore, chatStore)
│   ├── utils               # 工具类 (request.ts, websocket.ts)
│   └── views               # 页面视图
│       ├── login           # 登录注册页
│       ├── room            # 房间列表页
│       ├── chat            # 聊天室核心页
│       └── admin           # 管理后台
├── .env.development        # 环境变量 (Java/C++ 接口地址)
├── package.json
└── vite.config.ts

# 🎨 LightTalk 前端开发指南 (Vue3 + TS)

## 1. 技术栈选型
- **核心**: Vue3 (Composition API) + TypeScript
- **状态**: Pinia (存储用户信息与 JWT)
- **UI**: Element Plus (快速构建聊天布局)
- **通信**: Axios (HTTP) + 原生 WebSocket (实时)

## 2. 页面路由设计
- `/login`: 登录页
- `/register`: 注册页
- `/rooms`: 房间大厅（展示列表、加入房间）
- `/chat/:roomId`: 核心聊天页

## 3. 核心开发任务
### Phase 1: 网络层封装
- [ ] **Axios 封装**: 设置 `baseURL`，在请求拦截器中自动注入 `Authorization: Bearer <token>`。
- [ ] **WebSocket 封装**: 创建 `useWebSocket` hook，支持心跳检测与断线重连。

### Phase 2: 聊天页逻辑 (重点)
- [ ] **初始化**: 页面加载时请求 Java 接口拉取历史消息记录。
- [ ] **建立连接**: WS 连接成功后发送 `join` 协议。
- [ ] **消息处理**:
    - 监听 `message`: 将新消息推入 Vue 响应式数组。
    - 监听 `online_count`: 实时更新顶栏人数显示。
- [ ] **发送交互**: 封装 `chat` 协议消息体发送给 C++ 服务。

### Phase 3: UI/UX 优化
- [ ] 消息列表自动滚动至底部。
- [ ] 区分自己发送的消息（右侧）与他人消息（左侧）。
- [ ] 实现断线提示 UI。

## 4. 简历项目亮点描述 (面试参考)
- "实现了基于 Vue3 组合式 API 的高性能 WebSocket 客户端封装。"
- "设计并实现了异构架构下的前端实时数据流管理，支持断线重连与心跳同步。"
- "采用 JWT 无状态鉴权方案，确保了前端请求的安全性和扩展性。"
