import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store/user';

// 定义基础路由
const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    // 这里使用懒加载，虽然现在还没写页面，但结构先搭好
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/room/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/room/:roomId',
    name: 'ChatRoom',
    component: () => import('@/views/chat/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/index.vue'),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const isAuthenticated = !!userStore.token;

  // 1. 如果用户未登录，且访问的不是 login 页面，则强行重定向到 /login
  if (!isAuthenticated && to.path !== '/login') {
    next('/login');
  } 
  // 2. 如果用户已登录，且访问的是 /login，则自动跳转到首页大厅
  else if (isAuthenticated && to.path === '/login') {
    next('/');
  } 
  // 3. 其他情况正常放行
  else {
    next();
  }
});

export default router;
