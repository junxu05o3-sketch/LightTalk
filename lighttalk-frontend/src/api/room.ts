import request from '@/utils/request';

// API: 获取房间列表
export const getRooms = () => {
  return request.get('/rooms');
};

// API: 创建房间
export const createRoom = (data: any) => {
  return request.post('/rooms', data);
};

// API: 加入房间
export const joinRoom = (roomId: number | string) => {
  return request.post(`/rooms/${roomId}/join`);
};
