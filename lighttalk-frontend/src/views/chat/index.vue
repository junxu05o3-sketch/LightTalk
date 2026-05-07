<template>
  <div class="chat-container">
    <!-- 顶部状态栏 -->
    <header class="glass-container chat-header">
      <div class="left-action">
        <el-button circle icon="ArrowLeft" @click="handleBack" />
        <span class="room-title">房间 ID: {{ roomId }}</span>
      </div>
      <div class="right-status">
        <el-tag v-if="!wsConnected" type="danger" effect="dark" size="small" class="blink">断开连接，正在重连...</el-tag>
        <el-tag v-else type="success" effect="dark" size="small" round>
          <el-icon><User /></el-icon> {{ onlineCount }} 人在线
        </el-tag>
      </div>
    </header>

    <!-- 中间消息瀑布流 -->
    <main class="chat-main" ref="chatMainRef" @scroll="handleScroll">
      <div class="load-more" v-if="hasMoreHistory">
        <el-button text size="small" :loading="loadingHistory" @click="loadHistory">
          查看更早的消息
        </el-button>
      </div>
      <div class="no-more" v-else>
        没有更早的消息了
      </div>

      <div class="message-list">
        <div 
          v-for="msg in messages" 
          :key="msg.id || msg.tempId" 
          :class="['message-item', msg.userId === userStore.userId ? 'is-me' : 'is-other']"
        >
          <!-- 系统消息居中 -->
          <div v-if="msg.type === 'system'" class="system-msg">
            <span class="system-text">{{ msg.content }}</span>
          </div>
          
          <!-- 普通文本消息 -->
          <template v-else>
            <div class="avatar-wrap">
              <el-avatar :size="36" :src="msg.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </div>
            <div class="msg-content-wrap">
              <div class="msg-nickname" v-if="msg.userId !== userStore.userId">{{ msg.nickname }}</div>
              <div class="msg-bubble glass-container">
                {{ msg.content }}
              </div>
            </div>
          </template>
        </div>
      </div>
    </main>

    <!-- 底部输入栏 -->
    <footer class="glass-container chat-footer">
      <el-input 
        v-model="inputMsg" 
        type="textarea" 
        :rows="3" 
        resize="none" 
        placeholder="输入聊天内容，按 Enter 发送，Shift + Enter 换行"
        @keydown.enter.prevent="handleEnter"
      />
      <div class="action-area">
        <el-button type="primary" size="large" icon="Position" @click="sendMessage">发 送</el-button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { getHistoryMessages, saveMessage } from '@/api/message';
import { LightTalkWebSocket } from '@/utils/websocket';
import { ArrowLeft, User, Position } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const roomId = Number(route.params.roomId);
const inputMsg = ref('');
const chatMainRef = ref<HTMLElement | null>(null);

// 状态
const messages = ref<any[]>([]);
const onlineCount = ref(0);
const wsConnected = ref(false);

// 历史分页状态
const lastMsgId = ref<number | null>(null);
const hasMoreHistory = ref(true);
const loadingHistory = ref(false);

let ws: LightTalkWebSocket | null = null;
let unregisterMsg: () => void;
let unregisterOpen: () => void;

// === 1. 历史消息拉取 ===
const loadHistory = async () => {
  if (loadingHistory.value || !hasMoreHistory.value) return;
  
  loadingHistory.value = true;
  // 记录拉取前的高度，为了加载老消息后保持滚动条位置不闪动
  const prevScrollHeight = chatMainRef.value?.scrollHeight || 0;
  
  try {
    const res: any = await getHistoryMessages(roomId, lastMsgId.value, 20);
    if (res && res.messages) {
      // 插入到前面
      messages.value = [...res.messages, ...messages.value];
      lastMsgId.value = res.lastMsgId;
      hasMoreHistory.value = res.hasMore;
      
      await nextTick();
      // 如果是首次加载，滚动到底部
      if (prevScrollHeight === 0) {
        scrollToBottom();
      } else if (chatMainRef.value) {
        // 加载更多历史时，保持原有视觉位置
        chatMainRef.value.scrollTop = chatMainRef.value.scrollHeight - prevScrollHeight;
      }
    }
  } catch (e) {
    //
  } finally {
    loadingHistory.value = false;
  }
};

// === 2. 滚动处理 ===
const scrollToBottom = () => {
  if (chatMainRef.value) {
    chatMainRef.value.scrollTop = chatMainRef.value.scrollHeight;
  }
};

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  // 触顶加载更多（简单骨架）
  if (target.scrollTop <= 10 && !loadingHistory.value && hasMoreHistory.value) {
    loadHistory();
  }
};

// === 3. WebSocket 联调 ===
const initWebSocket = () => {
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:9000/ws';
  ws = new LightTalkWebSocket(wsUrl);
  
  // 注册建连成功回调 -> 发送 join
  unregisterOpen = ws.onOpen(() => {
    wsConnected.value = true;
    ws?.send('join', {
      roomId: roomId,
      nickname: userStore.nickname
    });
  });

  // 注册消息接收回调
  unregisterMsg = ws.onMessage((data: any) => {
    if (data.type === 'message') {
      // 收到聊天消息，如果是自己发的可能已经在视图里了（乐观更新），这里简单做去重或直接追加
      // 为了安全，假设有临时ID去重。这里按您的要求直接追加并滚到底部
      messages.value.push(data);
      nextTick(() => {
        scrollToBottom();
      });
    } else if (data.type === 'online_count') {
      onlineCount.value = data.count;
    }
  });

  ws.connect();
};

// === 4. 发送消息机制 (双写) ===
const handleEnter = (e: KeyboardEvent) => {
  if (!e.shiftKey) {
    sendMessage();
  }
};

const sendMessage = async () => {
  const content = inputMsg.value.trim();
  if (!content) return;

  const msgPayload = {
    roomId,
    userId: userStore.userId,
    nickname: userStore.nickname,
    content,
    type: 'text'
  };

  // 1. 马上渲染到本地（乐观体验）
  const tempMsg = { ...msgPayload, tempId: Date.now() };
  messages.value.push(tempMsg);
  inputMsg.value = '';
  nextTick(() => scrollToBottom());

  // 2. 长连接发送给 C++ 网关扩散
  ws?.send('chat', msgPayload);

  // 3. 落库给 Java 端
  try {
    await saveMessage(msgPayload);
  } catch (e: any) {
    // 之前如果 Java 报错（如被禁言），这里会被拦截并在 request.ts 提示
    // 为了防止发送失败，可以将刚追加的临时消息撤回或标记失败（这里简化处理）
    ElMessage.error('消息持久化失败');
  }
};

const handleBack = () => {
  router.push('/');
};

onMounted(async () => {
  await loadHistory();
  initWebSocket();
});

onBeforeUnmount(() => {
  if (unregisterMsg) unregisterMsg();
  if (unregisterOpen) unregisterOpen();
  if (ws) ws.disconnect();
});
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.chat-header {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.left-action {
  display: flex;
  align-items: center;
  gap: 15px;
}

.room-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.blink {
  animation: blinker 1.5s linear infinite;
}
@keyframes blinker {
  50% { opacity: 0.5; }
}

.chat-main {
  flex: 1;
  overflow-y: auto;
  border-radius: 16px;
  padding: 20px;
  /* 背景也是半透明玻璃拟态 */
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border);
  box-shadow: inset 0 0 20px rgba(0,0,0,0.2);
}

.chat-main::-webkit-scrollbar {
  width: 6px;
}
.chat-main::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 3px;
}

.load-more, .no-more {
  text-align: center;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

/* 我发的消息靠右 */
.message-item.is-me {
  flex-direction: row-reverse;
}

.msg-content-wrap {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-item.is-me .msg-content-wrap {
  align-items: flex-end;
}

.msg-nickname {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  margin-left: 4px;
}

.msg-bubble {
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-all;
}

/* 他人的气泡 */
.message-item.is-other .msg-bubble {
  background: rgba(255, 255, 255, 0.05);
  border-top-left-radius: 2px;
}

/* 我的气泡 */
.message-item.is-me .msg-bubble {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.4), rgba(0, 242, 254, 0.4));
  border: 1px solid rgba(79, 172, 254, 0.3);
  border-top-right-radius: 2px;
  color: #fff;
}

/* 系统消息 */
.system-msg {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 10px 0;
}
.system-text {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(0,0,0,0.3);
  color: var(--text-secondary);
}

/* 底部输入框 */
.chat-footer {
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  flex-shrink: 0;
}

:deep(.el-textarea__inner) {
  background: transparent !important;
  color: #fff;
  border: none !important;
  box-shadow: none !important;
  font-size: 15px;
  padding: 0;
}
:deep(.el-textarea__inner):focus {
  box-shadow: none !important;
}

.action-area {
  display: flex;
  justify-content: flex-end;
}
</style>
