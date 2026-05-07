import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 创建 Axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000, // 超时时间 10 秒
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    // 自动在 Header 附加 Authorization: Bearer <token>
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 提取后端的统一返回结构 Result<T>
    const res = response.data;
    
    // 如果返回 code 为 200，说明业务成功，直接返回 data
    if (res.code === 200) {
      return res.data;
    } 
    
    // 捕获到 401：未授权或 Token 过期
    if (res.code === 401) {
      ElMessage.error('认证已过期，请重新登录');
      const userStore = useUserStore();
      userStore.logout();
      router.push('/login');
      return Promise.reject(new Error(res.msg || 'Error'));
    }

    // 其他业务错误全局提示
    ElMessage.error(res.msg || '系统异常');
    return Promise.reject(new Error(res.msg || 'Error'));
  },
  (error: any) => {
    // HTTP 状态码层面的错误处理 (如 401, 403, 500)
    if (error.response && error.response.status === 401) {
      ElMessage.error('认证失败或 Token 过期，请重新登录');
      const userStore = useUserStore();
      userStore.logout();
      router.push('/login');
    } else {
      ElMessage.error(error.message || '网络请求异常');
    }
    return Promise.reject(error);
  }
);

export default service;
