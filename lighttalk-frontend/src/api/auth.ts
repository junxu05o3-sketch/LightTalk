import request from '@/utils/request';

// API: 登录
export const login = (data: any) => {
  return request.post('/auth/login', data);
};

// API: 注册
export const register = (data: any) => {
  return request.post('/auth/register', data);
};
