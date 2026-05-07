<template>
  <el-container class="admin-layout">
    <!-- 左侧边栏 -->
    <el-aside width="220px" class="glass-aside">
      <div class="brand">
        <h2>LightTalk Admin</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="transparent"
        text-color="#e0e0e0"
        active-text-color="#4facfe"
        @select="handleSelectMenu"
      >
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="rooms">
          <el-icon><ChatDotRound /></el-icon>
          <span>房间管理</span>
        </el-menu-item>
      </el-menu>
      
      <div class="bottom-action">
        <el-button type="info" plain @click="goHome">返回大厅</el-button>
      </div>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-main class="admin-main">
      <transition name="fade-transform" mode="out-in">
        
        <!-- 用户管理面板 -->
        <div v-if="activeMenu === 'users'" class="glass-container panel-box" key="users">
          <h3>全站用户管理</h3>
          
          <div class="filter-bar">
            <el-input v-model="userQuery.username" placeholder="搜索用户名" class="filter-item" clearable />
            <el-input v-model="userQuery.nickname" placeholder="搜索昵称" class="filter-item" clearable />
            <el-select v-model="userQuery.status" placeholder="用户状态" class="filter-item" clearable>
              <el-option label="正常" :value="1" />
              <el-option label="禁言" :value="0" />
            </el-select>
            <el-button type="primary" icon="Search" @click="fetchUsers">搜 索</el-button>
          </div>

          <el-table :data="usersData" v-loading="loadingUsers" style="width: 100%" class="custom-table">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="role" label="权限">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                  {{ row.status === 1 ? '正常' : '已禁言' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 1 && row.role !== 'ADMIN'" 
                  type="danger" 
                  size="small" 
                  plain 
                  @click="handleMute(row.id)"
                >
                  禁言
                </el-button>
                <el-button 
                  v-if="row.status === 0" 
                  type="success" 
                  size="small" 
                  plain 
                  @click="handleUnmute(row.id)"
                >
                  解禁
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="userTotal"
              :current-page="userQuery.current"
              :page-size="userQuery.size"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 房间管理面板 -->
        <div v-else-if="activeMenu === 'rooms'" class="glass-container panel-box" key="rooms">
          <h3>系统房间巡查</h3>
          <el-table :data="roomsData" v-loading="loadingRooms" style="width: 100%" class="custom-table">
            <el-table-column prop="id" label="房间ID" width="100" />
            <el-table-column prop="name" label="房间名称" />
            <el-table-column prop="ownerNickname" label="房主昵称" />
            <el-table-column prop="onlineCount" label="当前在线">
              <template #default="{ row }">
                <el-tag type="success" effect="dark" round>
                  <el-icon><User /></el-icon> {{ row.onlineCount }} 人
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '活跃' : '关闭' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

      </transition>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { pageUsers, listAdminRooms, muteUser } from '@/api/admin';
import { ElMessage, ElMessageBox } from 'element-plus';
import { User, ChatDotRound, Search } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

// 菜单状态
const activeMenu = ref('users');

const handleSelectMenu = (index: string) => {
  activeMenu.value = index;
  if (index === 'users') fetchUsers();
  if (index === 'rooms') fetchRooms();
};

const goHome = () => {
  router.push('/');
};

// --- 用户管理状态 ---
const loadingUsers = ref(false);
const usersData = ref<any[]>([]);
const userTotal = ref(0);
const userQuery = reactive({
  username: '',
  nickname: '',
  status: undefined as number | undefined,
  current: 1,
  size: 10
});

const fetchUsers = async () => {
  loadingUsers.value = true;
  try {
    const res: any = await pageUsers(userQuery);
    usersData.value = res.records;
    userTotal.value = res.total;
  } catch (e) {
  } finally {
    loadingUsers.value = false;
  }
};

const handlePageChange = (val: number) => {
  userQuery.current = val;
  fetchUsers();
};

const handleMute = (userId: number) => {
  ElMessageBox.prompt('请输入禁言时长（秒）：', '全网禁言', {
    confirmButtonText: '执行禁言',
    cancelButtonText: '取消',
    inputPattern: /^[1-9]\d*$/,
    inputErrorMessage: '请输入大于0的整数',
    customClass: 'glass-dialog'
  }).then(async ({ value }) => {
    try {
      await muteUser({ userId, muteDuration: parseInt(value) });
      ElMessage.success('禁言指令已通过 Redis 广播，秒级生效');
      fetchUsers();
    } catch (e) { }
  }).catch(() => {});
};

const handleUnmute = async (userId: number) => {
  try {
    await muteUser({ userId, muteDuration: 0 });
    ElMessage.success('已解除禁言');
    fetchUsers();
  } catch (e) { }
};

// --- 房间管理状态 ---
const loadingRooms = ref(false);
const roomsData = ref<any[]>([]);

const fetchRooms = async () => {
  loadingRooms.value = true;
  try {
    const res: any = await listAdminRooms();
    roomsData.value = res;
  } catch (e) {
  } finally {
    loadingRooms.value = false;
  }
};

// 越权拦截
onMounted(() => {
  // 根据此前后端设计的规范，userId = 3 为唯一超管
  if (userStore.userId !== 3) {
    ElMessage.error('非法访问，越权行为已记录！');
    router.push('/');
    return;
  }
  
  fetchUsers();
});
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100vw;
}

.glass-aside {
  background: rgba(15, 32, 39, 0.6);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand h2 {
  font-size: 20px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.admin-menu {
  flex: 1;
  border-right: none;
}

.bottom-action {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.admin-main {
  background: transparent;
  padding: 20px 40px;
  overflow-y: auto;
}

.panel-box {
  padding: 24px;
  min-height: calc(100vh - 80px);
}

.panel-box h3 {
  margin-bottom: 24px;
  font-size: 22px;
  font-weight: 600;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.filter-item {
  width: 200px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 覆盖 Element Plus 表格透明样式 */
:deep(.custom-table) {
  background-color: transparent !important;
  color: #fff;
}
:deep(.el-table th.el-table__cell) {
  background-color: rgba(0,0,0,0.3) !important;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
:deep(.el-table tr) {
  background-color: transparent !important;
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background-color: rgba(255,255,255,0.05) !important;
}
:deep(.el-table::before) {
  display: none;
}
</style>
