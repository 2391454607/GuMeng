<script setup>
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import Footer from "@/views/web/layout/Footer.vue";
import {chatWithAI} from "@/api/web/Web.js";

const messageList = ref([
  {
    type: 'bot',
    content: '你好！我是故梦阑珊非遗守护精灵，很高兴为您服务。我可以为您解答非遗相关的问题，让我们开始对话吧！'
  }
]);

const inputMessage = ref('');
const chatContainer = ref(null);

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    Message.warning('请输入内容');
    return;
  }

  // 添加用户消息
  messageList.value.push({
    type: 'user',
    content: inputMessage.value
  });

  // 添加思考中的消息
  messageList.value.push({
    type: 'bot',
    content: '正在思考中...'
  });

  try {
    const userMessage = inputMessage.value;
    inputMessage.value = '';

    const response = await chatWithAI({
      bot: 'aides',
      message: userMessage
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let botResponse = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      
      const chunk = decoder.decode(value);
      const lines = chunk.split('\n').filter(line => line.trim());
      
      for (const line of lines) {
        try {
          const data = JSON.parse(line);
          if (data.role === 'assistant' && data.type === 'answer' && data.content) {
            botResponse += data.content;
            messageList.value[messageList.value.length - 1].content = botResponse;
            scrollToBottom();
          }
        } catch (e) {
          console.error('解析响应数据失败:', e);
        }
      }
    }

  } catch (error) {
    console.error('AI 对话错误:', error);
    Message.error('发送失败，请重试');
    messageList.value.pop();
  }
};

// 监听回车发送
const handleKeyPress = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
};

// 自动滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

onMounted(() => {
  scrollToBottom();
});
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2>故梦阑珊非遗守护精灵</h2>
      <p>专业解答非遗相关问题，让传统文化焕发新生机</p>
    </div>
    
    <div class="chat-messages" ref="chatContainer">
      <div v-for="(message, index) in messageList" 
           :key="index" 
           :class="['message', message.type]">
        <div class="avatar">
          <img :src="message.type === 'bot' ? '/bot-avatar.png' : '/user-avatar.png'" 
               :alt="message.type === 'bot' ? '机器人头像' : '用户头像'">
        </div>
        <div class="content">{{ message.content }}</div>
      </div>
    </div>

    <div class="chat-input">
      <a-textarea
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        :auto-size="{ minRows: 1, maxRows: 4 }"
        @keypress="handleKeyPress"
      />
      <a-button type="primary" @click="sendMessage">
        发送
        <template #icon>
          <icon-send />
        </template>
      </a-button>
    </div>
  </div>

  <Footer class="footer"/>
</template>

<style scoped>
.chat-container {
  height: calc(100vh - 169px);
  display: flex;
  flex-direction: column;
  background: #faf6f0;
  padding: 20px;
}

.chat-header {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid rgba(194, 16, 28, 0.1);
}

.chat-header h2 {
  color: #C2101C;
  margin-bottom: 10px;
}

.chat-header p {
  color: #666;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 10px;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message.bot .content {
  background: #fff;
  color: #333;
}

.message.user .content {
  background: #C2101C;
  color: white;
}

.chat-input {
  padding: 20px;
  background: #faf6f0;
  border-top: 1px solid rgba(194, 16, 28, 0.1);
  display: flex;
  gap: 10px;
}

.chat-input :deep(.arco-textarea) {
  flex: 1;
  border-color: #C2101C;
}

.chat-input :deep(.arco-textarea:focus) {
  box-shadow: 0 0 0 2px rgba(194, 16, 28, 0.2);
}

.chat-input :deep(.arco-btn-primary) {
  background-color: #C2101C;
  border-color: #C2101C;
}

.chat-input :deep(.arco-btn-primary:hover) {
  background-color: #d32f2f;
  border-color: #d32f2f;
}

.footer{
  display: flex;
  bottom: 0;
}
</style>