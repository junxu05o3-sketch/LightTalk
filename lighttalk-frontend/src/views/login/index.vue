<template>
  <div class="login-wrapper">
    <div class="glass-container login-card">
      <div class="logo-area">
        <h1 class="logo-text">LightTalk</h1>
        <p class="subtitle">{{ isLoginMode ? '欢迎回来，请登录' : '创建您的新账号' }}</p>
      </div>

      <!-- 登录/注册表单 -->
      <transition name="slide" mode="out-in">
        <el-form 
          :key="isLoginMode ? 'login' : 'register'"
          ref="formRef" 
          :model="formData" 
          :rules="rules" 
          class="login-form" 
          @keyup.enter="handleSubmit"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="formData.username" 
              placeholder="请输入用户名" 
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="formData.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 注册时需要昵称 -->
          <el-form-item v-if="!isLoginMode" prop="nickname">
            <el-input 
              v-model="formData.nickname" 
              placeholder="请输入您的专属昵称" 
              size="large"
            >
              <template #prefix>
                <el-icon><Star /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button 
            type="primary" 
            class="submit-btn" 
            size="large" 
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isLoginMode ? '登 录' : '立 即 注 册' }}
          </el-button>

          <div class="switch-mode">
            <a href="javascript:void(0)" @click="toggleMode">
              {{ isLoginMode ? '还没有账号？点此注册' : '已有账号？直接登录' }}
            </a>
          </div>
        </el-form>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { login, register } from '@/api/auth';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { User, Lock, Star } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const isLoginMode = ref(true);
const loading = ref(false);
const formRef = ref<FormInstance>();

const formData = reactive({
  username: '',
  password: '',
  nickname: ''
});

// 表单校验规则对接后端 @Validated
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ]
});

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value;
  formRef.value?.resetFields();
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        if (isLoginMode.value) {
          // 登录请求
          const res: any = await login({
            username: formData.username,
            password: formData.password
          });
          
          ElMessage.success('登录成功');
          // 保存状态
          userStore.setLoginInfo({
            token: res.token,
            userId: res.user.id,
            username: res.user.username,
            nickname: res.user.nickname,
            avatar: res.user.avatar
          });
          // 跳转到大厅
          router.push('/');
          
        } else {
          // 注册请求
          await register({
            username: formData.username,
            password: formData.password,
            nickname: formData.nickname
          });
          ElMessage.success('注册成功，请登录');
          toggleMode(); // 切换到登录态
        }
      } catch (e) {
        // 请求层的 error 拦截器已经弹出错误，这里无需处理
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.login-card {
  width: 400px;
  padding: 40px;
  text-align: center;
}

.logo-text {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--brand-color), #b86bfe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 30px;
}

.submit-btn {
  width: 100%;
  margin-top: 15px;
  border-radius: 8px;
  font-weight: bold;
}

.switch-mode {
  margin-top: 20px;
}

.switch-mode a {
  color: var(--brand-color);
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.2s;
}

.switch-mode a:hover {
  opacity: 0.8;
}

/* 切换模式动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}
.slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
