<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconClose, IconSend } from '@arco-design/web-vue/es/icon';
import { sendAssistantMessage } from '@/api/web/baikeAssistant';
import { useUserStore } from '@/stores/userStore';

const props = defineProps({
  // 传入属性，如果需要的话
});

// 获取用户store
const userStore = useUserStore();

// 控制聊天窗口的显示状态
const isOpen = ref(false);
// 控制拖动状态
const isDragging = ref(false);
// 助手位置
const position = reactive({
  x: window.innerWidth - 100,
  y: window.innerHeight * 0.8
});
// 拖动起始位置
const dragOffset = reactive({ x: 0, y: 0 });

// 聊天消息历史
const chatHistory = ref([
  {
    role: 'assistant',
    content: '您好，我是非遗小助手，有什么关于非物质文化遗产的问题，我很乐意为您解答！'
  }
]);

// 用户输入
const userInput = ref('');
// 加载状态
const isLoading = ref(false);
// 获取用户头像
const userInfo = computed(() => userStore.userInfo);
const userAvatar = computed(() => userInfo.value.userPic || '/image/gumeng.png');
// 助手头像
const assistantAvatar = ref('/background/ai-assist.png');

// 打开/关闭聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value;
  
  // 当窗口打开时，滚动到最新消息
  if (isOpen.value) {
    setTimeout(() => {
      const chatBody = document.querySelector('.chat-body');
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
      }
    }, 0);
  }
};

// 模拟AI响应
const simulateAIResponse = (question) => {
  const responses = {
    default: "关于非物质文化遗产，我可以告诉您这是指代代相传的传统文化表现形式和文化空间，包括传统音乐、舞蹈、戏剧、手工技艺等。中国的非遗保护工作始于2004年加入《保护非物质文化遗产公约》，目前已有42个项目列入联合国教科文组织非遗名录。",
    "什么是非物质文化遗产": "非物质文化遗产是指各族人民世代相传并视为其文化遗产组成部分的各种传统文化表现形式，以及与传统文化表现形式相关的实物和场所。包括口头传统、表演艺术、社会实践、礼仪、节庆活动、有关自然界和宇宙的知识和实践、传统手工艺技能等。",
    "中国有哪些著名的非物质文化遗产": "中国著名的非物质文化遗产包括京剧、昆曲、黄梅戏等传统戏剧；剪纸、中国书法、年画等传统美术；端午节、春节、清明节等传统节日；太极拳、中医药、针灸等传统知识和实践；景德镇陶瓷制作技艺、苏州刺绣、宜兴紫砂陶艺等传统手工艺。截至目前，中国共有42个项目被列入联合国教科文组织非遗名录，位居世界第一。",
    "非遗保护的意义": "非物质文化遗产保护的意义主要有：1) 文化传承：确保传统文化不会因现代化和全球化而消失；2) 文化多样性：维护世界文化的多样性和创造力；3) 民族认同：增强民族凝聚力和文化自信；4) 可持续发展：许多非遗项目蕴含着与自然和谐相处的智慧；5) 经济价值：通过文化旅游和文创产品开发，促进地方经济发展；6) 教育价值：为年轻一代提供文化教育和创新灵感。",
    "如何参与非遗保护": "参与非遗保护的方式包括：1) 了解与学习：积极了解本地非遗项目，参加相关展览和活动；2) 传播推广：在社交媒体分享非遗知识，提高公众认知；3) 购买支持：优先选择非遗传承人的正宗产品，支持其生计；4) 技艺传承：学习非遗技艺，参加工作坊和培训课程；5) 志愿服务：参与非遗保护机构的志愿活动；6) 政策支持：关注非遗保护政策，为相关法律法规建设提供建议。",
    "传统手工艺": "传统手工艺是中国非遗的重要组成部分，包括织绣、陶瓷、漆器、金属工艺、木雕、石雕、竹编、纸艺等。这些技艺多以家庭作坊形式代代相传，蕴含丰富的文化内涵和技术智慧。如苏州刺绣、景德镇陶瓷、潮州木雕等都是国家级非遗项目。目前面临的挑战包括传承人老龄化、市场竞争、原材料短缺等问题。保护措施包括建立传习所、开发文创产品、融入现代设计等。"
  };
  
  // 尝试找到问题中的关键词
  for (const key in responses) {
    if (key !== 'default' && question.includes(key)) {
      return responses[key];
    }
  }
  
  return responses.default;
};

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return;
  
  // 添加用户消息到聊天历史
  chatHistory.value.push({
    role: 'user',
    content: userInput.value
  });
  
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 设置加载状态
  isLoading.value = true;
  
  // 滚动到底部显示最新消息
  scrollToBottom();
  
  try {
    // 延迟模拟网络请求时间
    setTimeout(() => {
      try {
        // 准备发送给API的消息历史
        const messages = chatHistory.value.map(msg => ({
          role: msg.role,
          content: msg.content
        }));
        
        // 尝试调用API获取回复
        sendAssistantMessage(messages).then(response => {
          if (response && response.code === 200) {
            chatHistory.value.push({
              role: 'assistant',
              content: response.data
            });
          } else {
            throw new Error(response?.msg || '请求失败');
          }
        }).catch(error => {
          console.error('API请求失败，使用模拟响应:', error);
          // 调用失败则使用模拟响应
          chatHistory.value.push({
            role: 'assistant',
            content: simulateAIResponse(userQuestion)
          });
        }).finally(() => {
          isLoading.value = false;
          scrollToBottom();
        });
      } catch (error) {
        console.error('API调用异常，使用模拟响应:', error);
        // 出现异常则使用模拟响应
        chatHistory.value.push({
          role: 'assistant',
          content: simulateAIResponse(userQuestion)
        });
        isLoading.value = false;
        scrollToBottom();
      }
    }, 800); // 添加一点延迟，使体验更自然
    
  } catch (error) {
    console.error('聊天处理错误:', error);
    chatHistory.value.push({
      role: 'assistant',
      content: '抱歉，我暂时无法回答您的问题。请稍后再试。'
    });
    isLoading.value = false;
    scrollToBottom();
  }
};

// 滚动到聊天窗口底部
const scrollToBottom = () => {
  setTimeout(() => {
    const chatBody = document.querySelector('.chat-body');
    if (chatBody) {
      chatBody.scrollTop = chatBody.scrollHeight;
    }
  }, 0);
};

// 开始拖动
const startDrag = (e) => {
  // 如果点击的是输入框、发送按钮或聊天内容，不启动拖拽
  if (e.target.closest('.chat-input') || 
      e.target.closest('.chat-send-btn') || 
      e.target.closest('.chat-messages') || 
      e.target.closest('.chat-actions')) {
    return;
  }
  
  isDragging.value = true;
  dragOffset.x = e.clientX - position.x;
  dragOffset.y = e.clientY - position.y;
  
  // 添加全局鼠标事件监听器
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
  
  // 阻止事件冒泡和默认行为
  e.preventDefault();
  e.stopPropagation();
};

// 拖动中
const onDrag = (e) => {
  if (isDragging.value) {
    position.x = e.clientX - dragOffset.x;
    position.y = e.clientY - dragOffset.y;
    
    // 边界检查
    const maxX = window.innerWidth - 80;
    const maxY = window.innerHeight - 80;
    
    position.x = Math.max(0, Math.min(position.x, maxX));
    position.y = Math.max(0, Math.min(position.y, maxY));
    
    // 阻止事件冒泡和默认行为
    e.preventDefault();
    e.stopPropagation();
  }
};

// 停止拖动 - 修复拖动问题
const stopDrag = (e) => {
  if (isDragging.value) {
    isDragging.value = false;
    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('mouseup', stopDrag);
    
    // 明确地阻止后续事件
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
  }
};

// 处理按回车键发送消息
const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
};

// 处理双击事件
const handleDoubleClick = () => {
  toggleChat();
};

// 组件挂载和卸载时的事件处理
onMounted(() => {
  // 窗口大小改变时检查边界
  window.addEventListener('resize', () => {
    const maxX = window.innerWidth - 80;
    const maxY = window.innerHeight - 80;
    
    position.x = Math.max(0, Math.min(position.x, maxX));
    position.y = Math.max(0, Math.min(position.y, maxY));
  });
  
  // 获取用户信息
  if (userStore.token) {
    userStore.fetchUserInfo();
  }
  
  // 确保拖动结束时移除事件监听
  document.addEventListener('mouseup', stopDrag);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
});
</script>

<template>
  <div 
    class="baike-assistant"
    :class="{ 'is-open': isOpen, 'is-dragging': isDragging }"
    :style="{ 
      left: `${position.x}px`, 
      top: `${position.y}px` 
    }"
    @mousedown="startDrag"
  >
    <!-- 折叠状态下显示的图标 -->
    <div class="assistant-icon" @dblclick="handleDoubleClick" v-if="!isOpen">
      <img src="/background/ai-assist.png" alt="非遗小助手" />
    </div>
    
    <!-- 展开的聊天窗口 -->
    <div class="chat-window" :class="{ 'chat-window-left': true }" v-else>
      <div class="chat-header" @mousedown="startDrag">
        <div class="chat-title">
          <span>非遗小助手</span>
        </div>
        <div class="chat-actions">
          <a-button type="text" size="mini" @click.stop="toggleChat">
            <icon-close />
          </a-button>
        </div>
      </div>
      
      <div class="chat-body">
        <div class="chat-messages">
          <div 
            v-for="(msg, index) in chatHistory" 
            :key="index"
            :class="['chat-message', msg.role === 'user' ? 'user-message' : 'assistant-message']"
          >
            <div class="message-avatar">
              <img 
                :src="msg.role === 'user' ? userAvatar : assistantAvatar" 
                :alt="msg.role === 'user' ? '用户' : '助手'"
              />
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
          
          <div v-if="isLoading" class="chat-message assistant-message">
            <div class="message-avatar">
              <img :src="assistantAvatar" alt="助手" />
            </div>
            <div class="message-content loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-footer">
        <a-input
          v-model="userInput"
          class="chat-input"
          placeholder="请输入您的问题..."
          @keydown="handleKeyDown"
          allow-clear
        />
        <a-button 
          class="chat-send-btn" 
          type="primary" 
          @click.stop="sendMessage" 
          :disabled="!userInput.trim() || isLoading"
        >
          <icon-send />
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.baike-assistant {
  position: fixed;
  z-index: 999;
  transition: transform 0.2s ease;
}

.baike-assistant.is-dragging {
  opacity: 0.8;
  user-select: none;
}

.assistant-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  border: 2px solid #8C1F28;
}

.assistant-icon:hover {
  transform: scale(1.05);
}

.assistant-icon img {
  width: 90%;
  height: 90%;
  border-radius: 50%;
  object-fit: cover;
}

.chat-window {
  width: 360px;
  height: 500px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 2px solid #8C1F28;
  position: absolute;
  top: 0;
}

/* 聊天窗口向左打开 */
.chat-window-left {
  right: 80px;
  top: -150px;
}

.chat-header {
  background-color: #8C1F28;
  color: #fff;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: move;
}

.chat-title {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.chat-body {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #fff7e9;
  scroll-behavior: smooth;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  max-width: 95%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.assistant-message {
  align-self: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  margin: 0 8px;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
}

.message-content {
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 14px;
  word-break: break-word;
  max-width: calc(100% - 52px); /* 考虑头像和margin */
}

.user-message .message-content {
  background-color: #8C1F28;
  color: white;
  border-radius: 12px 2px 12px 12px;
}

.assistant-message .message-content {
  background-color: white;
  color: #333;
  border-radius: 2px 12px 12px 12px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.chat-footer {
  padding: 10px;
  background-color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 1px solid #eee;
}

.chat-input {
  flex: 1;
}

.chat-send-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
}

.chat-send-btn:hover {
  background-color: #751a22;
  border-color: #751a22;
}

/* 加载动画 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 10px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #8C1F28;
  opacity: 0.7;
  animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}
</style> 