<script setup>
import { ref, watch, nextTick, onMounted } from 'vue';
import Footer from "@/views/web/layout/Footer.vue";

// 左侧菜单数据
const menuItems = ref([
  { title: '今天', items: ['Greeting and Offering Assistance'] },
  { title: '7 天内', items: [
    '影响他人人生决定的深度图谱',
  ]},
  { title: '30 天内', items: [
    '国内外校园家庭心理干预研究',
    '故事细节项目日记与自动化方向'
  ]}
]);

// 聊天记录
const messages = ref([
  { role: 'ai', text: '你好！请问有什么可以帮你的呢？ 😊' }
]);

// 用户输入
const userInput = ref('');
const isTyping = ref(false);
const messageContainer = ref(null);

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom();
}, { deep: true });

// 监听打字状态变化，自动滚动到底部
watch(isTyping, () => {
  scrollToBottom();
});

onMounted(() => {
  scrollToBottom();
});

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
}

// 发送消息
async function sendMessage() {
  if (!userInput.value.trim()) return;
  
  // 添加用户消息
  messages.value.push({ role: 'user', text: userInput.value });
  const question = userInput.value;
  userInput.value = '';
  
  // 显示AI正在输入状态
  isTyping.value = true;
  
  try {
    // 这里添加实际的API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    messages.value.push({
      role: 'ai',
      text: `我是DeepSeek Chat，由深度求索公司开发的智能AI助手！ 我的回答是根据你的问题：${question}。提供的是模拟回答，需要接入实际的API才能实现真实对话。 有什么我可以帮你的呢？ ✨\n\n你可以大胆，或者尝试告诉学习3dg工作中的问题，无论日常烦恼、技术问题，还是需要写作建议，我都会尽力帮你！`
    });
  } catch (error) {
    messages.value.push({
      role: 'ai',
      text: '抱歉，发生了一些错误，请稍后重试。'
    });
  } finally {
    isTyping.value = false;
  }
}
</script>

<template>
  <div class="chat-page">
    <!-- 左侧菜单 -->
    <div class="sidebar">
      <div class="new-chat-btn">
        <button @click="messages = [{ role: 'ai', text: '你好！请问有什么可以帮你的呢？ 😊' }]">
          <i class="fas fa-plus"></i> 开始新对话
        </button>
      </div>
      
      <div class="menu-container">
        <div v-for="(section, index) in menuItems" :key="index" class="menu-section">
          <div class="section-title">{{ section.title }}</div>
          <div v-for="(item, itemIndex) in section.items" :key="itemIndex" class="menu-item">
            {{ item }}
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-container">
      <div class="chat-header">
        <h1>智能助手</h1>
      </div>
      
      <div class="messages" ref="messageContainer">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
          <div class="avatar" :class="msg.role">
            {{ msg.role === 'user' ? '我' : 'AI' }}
          </div>
          <div class="bubble">
            {{ msg.text }}
          </div>
        </div>
        
        <!-- AI正在输入提示 -->
        <div v-if="isTyping" class="message ai">
          <div class="avatar ai">AI</div>
          <div class="bubble typing">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <input 
          v-model="userInput" 
          @keyup.enter="sendMessage" 
          placeholder="请输入您的问题..." 
          class="message-input"
        />
        <div class="input-buttons">
          <button class="action-button" title="上传文件">
            <i class="fas fa-paperclip"></i>
          </button>
          <button class="action-button" title="录音">
            <i class="fas fa-microphone"></i>
          </button>
          <button 
            @click="sendMessage" 
            class="send-button"
            :disabled="!userInput.trim()"
          >
            发送
          </button>
        </div>
      </div>
    </div>
    <Footer class="footer" />
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 128px);
  background-color: #fff;
}

/* 左侧菜单样式 */
.sidebar {
  width: 260px;
  background-color: #f5f5f5;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.new-chat-btn button {
  width: 100%;
  padding: 10px;
  background-color: #4a55e6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.menu-container {
  margin-top: 20px;
  overflow-y: auto;
}

.menu-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.menu-item {
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.menu-item:hover {
  background-color: #e8e8e8;
}

/* 右侧聊天区域样式 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.chat-header h1 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.avatar.user {
  background-color: #4a55e6;
  color: white;
}

.avatar.ai {
  background-color: #10a37f;
  color: white;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  max-width: 100%;
  word-break: break-word;
  line-height: 1.5;
}

.message.user .bubble {
  background-color: #4a55e6;
  color: white;
}

.message.ai .bubble {
  background-color: #f5f5f5;
  color: #333;
}

.typing .dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #666;
  margin: 0 2px;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing .dot:nth-child(1) { animation-delay: 0s; }
.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.6; }
  40% { transform: scale(1); opacity: 1; }
}

.input-area {
  border-top: 1px solid #e0e0e0;
  padding: 16px;
  display: flex;
  gap: 12px;
}

.message-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.message-input:focus {
  border-color: #4a55e6;
}

.input-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-button {
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  border-radius: 4px;
}

.action-button:hover {
  background-color: #f5f5f5;
}

.send-button {
  padding: 8px 16px;
  background-color: #4a55e6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.send-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.send-button:not(:disabled):hover {
  background-color: #3a44d5;
}

.footer {
  display: flex;
  position: absolute;
  bottom: 0;
}
</style>

