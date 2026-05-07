<template>
  <div class="lobby-container">
    <!-- 顶部导航栏 -->
    <header class="glass-container header">
      <div class="logo">LightTalk 聊天大厅</div>
      <div class="user-info">
        <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
        <span class="nickname">{{ userStore.nickname }}</span>
        <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="main-content">
      <div class="toolbar">
        <h2 class="title">发现房间</h2>
        <el-button type="primary" @click="dialogVisible = true">新建房间</el-button>
      </div>

      <!-- 房间卡片网格 -->
      <div class="room-grid" v-loading="loading">
        <div v-for="room in roomList" :key="room.id" class="glass-container room-card">
          <div class="room-header">
            <span class="room-name">{{ room.name }}</span>
            <el-tag size="small" type="success" effect="dark" round>
              <el-icon><User /></el-icon> {{ room.onlineCount }} 人在线
            </el-tag>
          </div>
          <p class="room-desc">{{ room.description || '主人很懒，什么都没写~' }}</p>
          <div class="room-footer">
            <span class="owner">房主: {{ room.ownerNickname }}</span>
            <el-button type="primary" size="small" @click="handleJoin(room.id)">进入聊天</el-button>
          </div>
        </div>

        <el-empty v-if="!loading && roomList.length === 0" description="暂无房间，快去创建一个吧！" />
      </div>
    </main>

    <!-- 新建房间弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建房间" width="400px" custom-class="glass-dialog">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="房间名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="房间描述" prop="description">
          <el-input v-model="formData.description" placeholder="请输入一句话简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleCreateRoom">确认创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { getRooms, createRoom, joinRoom } from '@/api/room';
import { ElMessage, FormInstance } from 'element-plus';
import { User } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const roomList = ref<any[]>([]);
const loading = ref(false);

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = reactive({
  name: '',
  description: ''
});

const rules = {
  name: [
    { required: true, message: '房间名称不能为空', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

// 退出登录
const handleLogout = () => {
  userStore.logout();
  router.push('/login');
};

// 获取房间列表
const fetchRooms = async () => {
  loading.value = true;
  try {
    const res: any = await getRooms();
    roomList.value = res || [];
  } catch (e) {
    // 错误已被 request.ts 拦截
  } finally {
    loading.value = false;
  }
};

// 创建房间
const handleCreateRoom = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        await createRoom(formData);
        ElMessage.success('房间创建成功');
        dialogVisible.value = false;
        formData.name = '';
        formData.description = '';
        fetchRooms(); // 刷新列表
      } catch (e) {
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 加入房间并跳转
const handleJoin = async (roomId: number) => {
  try {
    await joinRoom(roomId);
    router.push(`/room/${roomId}`);
  } catch (e: any) {
    // 如果已经加入了，后端可能会抛出 ALREADY_IN_ROOM 异常
    // 但无论如何我们都放行跳转，除非是未找到房间之类的严重错误
    // 假设 ALREADY_IN_ROOM 也会在 request.ts 中提示，这里直接跳
    router.push(`/room/${roomId}`);
  }
};

onMounted(() => {
  fetchRooms();
});
</script>

<style scoped>
.lobby-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  border-radius: 0 0 16px 16px; /* 顶部不需要全包裹圆角，只需下面 */
  margin-bottom: 30px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, var(--brand-color), #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nickname {
  font-weight: 500;
}

.main-content {
  flex: 1;
  padding: 0 40px;
  overflow-y: auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 28px;
  font-weight: 600;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding-bottom: 40px;
}

.room-card {
  padding: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px 0 rgba(0, 0, 0, 0.5);
  border-color: rgba(255, 255, 255, 0.4);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.room-name {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

.room-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 20px;
  flex: 1;
}

.room-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 15px;
}

.owner {
  font-size: 12px;
  color: #888;
}

/* 弹窗样式修正 */
:deep(.el-dialog) {
  background: var(--bg-gradient-mid);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
}
:deep(.el-dialog__title) {
  color: #fff;
}
</style>
