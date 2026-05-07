import request from '@/utils/request';

// API: 分页查询用户
export const pageUsers = (data: any) => {
  return request.post('/admin/users/page', data);
};

// API: 获取所有房间列表
export const listAdminRooms = () => {
  return request.get('/admin/rooms');
};

// API: 禁言/解禁用户
export const muteUser = (data: { userId: number; muteDuration: number }) => {
  return request.post('/admin/users/mute', data);
};
