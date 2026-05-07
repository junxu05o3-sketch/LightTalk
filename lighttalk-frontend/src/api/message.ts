import request from '@/utils/request';

// API: 落库聊天消息 (普通模式下是 WebSocket 收发，此接口用于持久化)
export const saveMessage = (data: any) => {
  return request.post('/messages', data);
};

// API: 游标分页获取历史消息
export const getHistoryMessages = (roomId: number | string, lastMsgId?: number | null, pageSize: number = 20) => {
  return request.get(`/messages/${roomId}`, {
    params: {
      lastMsgId: lastMsgId || undefined,
      pageSize
    }
  });
};
