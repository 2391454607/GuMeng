<script setup>
import {ref, watch, nextTick, onMounted, reactive} from 'vue';
import Footer from "@/views/web/layout/Footer.vue";
import { createConversationAPI, chatWithAI } from '@/api/web/Web.js';
import {Message} from "@arco-design/web-vue";

// 左侧菜单数据
const menuItems = ref([
  { title: '今天', items: ['糖画'] },
  { title: '7 天内', items: [
    '云南有哪些非遗',
  ]},
  { title: '30 天内', items: [
    '建水紫陶',
    '中国非遗有哪些'
  ]}
]);

// 聊天记录
const messages = ref([
  { role: 'ai', text: '嗨，你好！我是故梦阑珊非遗守护精灵，专注于非物质文化遗产领域，能为你解答你想要了解的非遗文化领域的相关知识哦。 😊' }
]);

// 添加会话ID
const conversationId = ref('');

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

onMounted(async () => {
  try {
    const response = await createConversationAPI();
    conversationId.value = response.data.conversationId;
  } catch (error) {
    console.error('创建会话失败:', error);
    messages.value.push({
      role: 'ai',
      text: '初始化对话失败，请刷新页面重试。'
    });
  }
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

// 添加预设问题
const suggestedQuestions = ref([
    "有哪些非遗技艺濒临失传？",
    "非遗项目如何传承发展？",
    "介绍一个有趣的非遗项目吧。"
]);
// 发送消息
async function sendMessage(content = '') {
  if (isTyping.value) {
    Message.warning('非遗小精灵 正在回复，请稍候...');
    return;
  }
  const messageText = content || userInput.value;
  if (!messageText || messageText.trim() === '') {
    Message.warning('不能发送空消息');
    return;
  }

  //发送信息清空预设问题
  suggestedQuestions.value = [];

  messages.value.push({ role: 'user', text: messageText });
  const question = messageText;
  userInput.value = '';
  isTyping.value = true;

  try {
    const response = await chatWithAI({
      conversationId: conversationId.value,
      message: question
    });
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let aiMessage = reactive({ role: 'ai', text: '' });
    messages.value.push(aiMessage);
    let buffer = '';
    let tempSuggested = [];
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      const chunk = decoder.decode(value, { stream: true });
      buffer += chunk;
      while (buffer.includes('\n')) {
        const newlineIndex = buffer.indexOf('\n');
        const line = buffer.slice(0, newlineIndex);
        buffer = buffer.slice(newlineIndex + 1);
        if (line.startsWith('data:')) {
          const data = line.slice('data:'.length).trim();
          // 判断是否为中文且长度大于5
          if (/[一-龥]/.test(data) && data.length > 5) {
            tempSuggested.push(data);
          } else {
            await typewriterEffect(data, aiMessage, 30);
          }
        }
      }
    }
    // 替换预设问题
    if (tempSuggested.length > 0) {
      suggestedQuestions.value = tempSuggested.filter(q => q.trim() !== '');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    const errorMessage = { role: 'ai', text: '' };
    messages.value.push(errorMessage);
    await typewriterEffect('抱歉，发生了一些错误，请稍后重试。', errorMessage, 30);
  } finally {
    isTyping.value = false;
  }
}


// 添加开始新对话的函数
async function startNewChat() {
  try {
    const response = await createConversationAPI();
    conversationId.value = response.data.conversationId;
    messages.value = [{ role: 'ai', text: '嗨，你好！我是故梦阑珊非遗守护精灵，专注于非物质文化遗产领域，能为你解答你想要了解的非遗文化领域的相关知识哦。 😊' }];
  } catch (error) {
    console.error('创建新对话失败:', error);
    messages.value.push({
      role: 'ai',
      text: '创建新对话失败，请稍后重试。'
    });
  }
}
// 添加打字机效果方法
async function typewriterEffect(text, message, delay = 30) {
  for (let i = 0; i < text.length; i++) {
    message.text = message.text + text[i];  // 用赋值替代 +=
    await nextTick();                        // 等待视图刷新
    await new Promise(resolve => setTimeout(resolve, delay));
  }
}

// 处理预设问题点击
const handleSuggestionClick = (question) => {
  userInput.value = question;
  sendMessage();
  suggestedQuestions.value = [];
};

// 处理菜单项点击
function handleMenuItemClick(item) {
  Message.info(item)
}
</script>

<template>
  <div class="chat-page">
    <!-- 左侧菜单 -->
    <div class="sidebar">
      <div class="new-chat-btn">
        <button @click="startNewChat">
          <i class="fas fa-plus"></i> 开始新对话
        </button>
      </div>
      
      <div class="menu-container">
        <div v-for="(section, index) in menuItems" :key="index" class="menu-section">
          <div class="section-title">{{ section.title }}</div>
          <div
            v-for="(item, itemIndex) in section.items"
            :key="itemIndex"
            class="menu-item"
            @click="handleMenuItemClick(item)"
          >
            {{ item }}
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-container">
      <div class="chat-header">
        <h1>故梦阑珊非遗守护精灵</h1>
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
          <div class="ai">小精灵正在思考中</div>
          <div class="typing">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <!-- 预设问题建议 -->
        <div class="suggested-questions">
          <div
            v-for="(question, index) in suggestedQuestions"
            :key="index"
            class="suggestion"
            @click="handleSuggestionClick(question)"
          >
            {{ question }}
          </div>
        </div>

        <div class="input-wrapper">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage()"
            placeholder="请输入您的问题..."
            class="message-input"
          />
          <button
            @click="sendMessage()"
            class="send-button"
            :disabled="!userInput.trim() || isTyping"
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
  flex-direction: column;
  gap: 12px;
}

.suggested-questions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.suggestion {
  background-color: #f0f0f0;
  padding: 8px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
}

.suggestion:hover {
  background-color: #e0e0e0;
}

.input-wrapper {
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

