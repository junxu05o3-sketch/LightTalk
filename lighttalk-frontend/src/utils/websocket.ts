import { ElMessage } from 'element-plus';

// 定义基础的回调函数类型
type MessageHandler = (data: any) => void;

/**
 * 预封装的高标准 WebSocket 客户端类
 * 实现了指数退避（Exponential Backoff）的自动断线重连机制
 */
export class LightTalkWebSocket {
  private url: string;
  private ws: WebSocket | null = null;
  
  // 重连机制相关参数
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private baseReconnectDelay = 1000; // 基础延迟 1 秒
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private isIntentionalClose = false; // 是否为主动关闭连接

  // 事件监听器注册表
  private messageHandlers: Set<MessageHandler> = new Set();
  private openHandlers: Set<() => void> = new Set();

  constructor(url: string) {
    this.url = url;
  }

  /**
   * 建立连接
   */
  public connect() {
    this.isIntentionalClose = false;
    this.initWebSocket();
  }

  /**
   * 内部初始化逻辑
   */
  private initWebSocket() {
    if (this.ws) {
      this.ws.close();
    }

    try {
      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        console.log(`[WebSocket] Connected to ${this.url}`);
        // 连接成功后重置重连次数
        this.reconnectAttempts = 0;
        // 触发上线回调
        this.openHandlers.forEach(handler => handler());
      };

      this.ws.onmessage = (event: MessageEvent) => {
        try {
          // 这里默认 C++ 网关推下来的都是 JSON 格式文本
          const parsedData = JSON.parse(event.data);
          this.notifyHandlers(parsedData);
        } catch (e) {
          console.error('[WebSocket] Failed to parse message', event.data);
        }
      };

      this.ws.onclose = (event: CloseEvent) => {
        console.warn(`[WebSocket] Connection closed (code: ${event.code})`);
        this.handleReconnect();
      };

      this.ws.onerror = (error: Event) => {
        console.error('[WebSocket] Error occurred', error);
      };

    } catch (e) {
      console.error('[WebSocket] Initialization failed', e);
      this.handleReconnect();
    }
  }

  /**
   * 封装发送方法，自动将参数序列化为 JSON 帧
   * @param type 协议类型 (例如 'join', 'chat')
   * @param payload 协议数据体
   */
  public send(type: string, payload: Record<string, any> = {}) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = JSON.stringify({ type, ...payload });
      this.ws.send(message);
    } else {
      console.warn('[WebSocket] Cannot send message, socket is not open');
      ElMessage.warning('网络未连接，发送失败');
    }
  }

  /**
   * 处理断线重连逻辑 (Exponential Backoff 指数退避)
   */
  private handleReconnect() {
    if (this.isIntentionalClose) return;

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      ElMessage.error('服务器连接断开，且重连失败。请刷新页面重试');
      return;
    }

    // 计算退避延迟 (例如: 1s, 2s, 4s, 8s...)
    const delay = this.baseReconnectDelay * Math.pow(2, this.reconnectAttempts);
    this.reconnectAttempts++;

    console.log(`[WebSocket] Reconnecting in ${delay}ms... (Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectTimer = setTimeout(() => {
      this.initWebSocket();
    }, delay);
  }

  /**
   * 注册消息监听器
   */
  public onMessage(handler: MessageHandler) {
    this.messageHandlers.add(handler);
    // 返回注销函数
    return () => {
      this.messageHandlers.delete(handler);
    };
  }

  /**
   * 注册连接成功监听器
   */
  public onOpen(handler: () => void) {
    this.openHandlers.add(handler);
    return () => {
      this.openHandlers.delete(handler);
    };
  }

  /**
   * 分发消息给所有注册的监听器
   */
  private notifyHandlers(data: any) {
    this.messageHandlers.forEach(handler => handler(data));
  }

  /**
   * 主动断开连接 (例如用户退出登录时)
   */
  public disconnect() {
    this.isIntentionalClose = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.messageHandlers.clear();
    this.openHandlers.clear();
    console.log('[WebSocket] Disconnected intentionally');
  }
}
