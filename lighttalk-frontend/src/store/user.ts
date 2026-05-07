import { defineStore } from 'pinia';
import { ref } from 'vue';

// 定义当前登录用户信息的类型
export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  avatar?: string;
  token: string;
}

export const useUserStore = defineStore('user', () => {
  // 尝试从 localStorage 恢复状态
  const token = ref<string>(localStorage.getItem('token') || '');
  const userId = ref<number>(Number(localStorage.getItem('userId')) || 0);
  const username = ref<string>(localStorage.getItem('username') || '');
  const nickname = ref<string>(localStorage.getItem('nickname') || '');
  const avatar = ref<string>(localStorage.getItem('avatar') || '');

  // 设置登录信息，并持久化到 localStorage
  const setLoginInfo = (info: UserInfo) => {
    token.value = info.token;
    userId.value = info.userId;
    username.value = info.username;
    nickname.value = info.nickname;
    if (info.avatar) avatar.value = info.avatar;

    localStorage.setItem('token', info.token);
    localStorage.setItem('userId', info.userId.toString());
    localStorage.setItem('username', info.username);
    localStorage.setItem('nickname', info.nickname);
    if (info.avatar) localStorage.setItem('avatar', info.avatar);
  };

  // 清空状态与本地缓存
  const logout = () => {
    token.value = '';
    userId.value = 0;
    username.value = '';
    nickname.value = '';
    avatar.value = '';

    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('avatar');
  };

  return {
    token,
    userId,
    username,
    nickname,
    avatar,
    setLoginInfo,
    logout
  };
});
