<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconClose, IconSend, IconCamera, IconImage, IconRefresh, IconExpand, IconShrink } from '@arco-design/web-vue/es/icon';
import { sendAssistantMessage } from '@/api/web/baikeAssistant';
import { uploadImage, recognizeImage } from '@/api/web/IchRecognition';
import { useUserStore } from '@/stores/userStore';
import { useRouter } from 'vue-router';

// 本地存储键名
const CHAT_HISTORY_KEY = 'baike_assistant_chat_history';
const MAX_HISTORY_LENGTH = 20; // 最大历史记录数量

const props = defineProps({
  // 传入属性，如果需要的话
});

// 获取用户store
const userStore = useUserStore();

// 在setup中获取路由器实例
const router = useRouter();

// 控制聊天窗口的显示状态
const isOpen = ref(false);
// 控制拖动状态
const isDragging = ref(false);
// 控制窗口扩大状态
const isExpanded = ref(false);
// 助手位置
const position = reactive({
  x: window.innerWidth - 100,
  y: window.innerHeight * 0.8
});
// 记录扩大前的位置，以便还原
const originalPosition = reactive({
  x: 0,
  y: 0
});
// 拖动起始位置
const dragOffset = reactive({ x: 0, y: 0 });

// 模拟历史对话列表（固定的示例对话）
const historyChats = ref([
  { id: 1, title: '今天', isHeader: true },
  { id: 2, title: '非遗', time: '14:30' },
  { id: 3, title: '7 天内', isHeader: true },
  { id: 4, title: '云南白族非遗', time: '3天前' },
  { id: 5, title: '30 天内', isHeader: true },
  { id: 6, title: '建水紫陶', time: '12天前' },
  { id: 7, title: '中国非遗有哪些', time: '28天前' },
]);

// 切换扩大/缩小状态
const toggleExpand = () => {
  if (!isExpanded.value) {
    // 记录当前位置，以便缩小时还原
    originalPosition.x = position.x;
    originalPosition.y = position.y;
    
    // 全屏模式不需要计算居中位置
    position.x = 0;
    position.y = 0;
  } else {
    // 还原位置
    position.x = originalPosition.x;
    position.y = originalPosition.y;
  }
  
  isExpanded.value = !isExpanded.value;
  
  // 滚动到底部
  setTimeout(scrollToBottom, 100);
};

// 从localStorage加载聊天历史
const loadChatHistory = () => {
  try {
    const savedHistory = localStorage.getItem(CHAT_HISTORY_KEY);
    if (savedHistory) {
      const parsedHistory = JSON.parse(savedHistory);
      if (Array.isArray(parsedHistory) && parsedHistory.length > 0) {
        return parsedHistory;
      }
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error);
  }
  
  // 默认欢迎消息
  return [
    {
      role: 'assistant',
      content: '您好，我是非遗小助手，有什么关于非物质文化遗产的问题，我很乐意为您解答！您还可以上传图片，我能识别图片中的非遗内容。'
    }
  ];
};

// 保存聊天历史到localStorage
const saveChatHistory = (history) => {
  try {
    // 限制历史记录长度，防止localStorage超出限制
    const limitedHistory = history.slice(-MAX_HISTORY_LENGTH);
    localStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(limitedHistory));
  } catch (error) {
    console.error('保存聊天历史失败:', error);
  }
};

// 聊天消息历史，从localStorage加载
const chatHistory = ref(loadChatHistory());

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

// 处理流式响应
const handleStreamResponse = async (data, messageIndex) => {
  if (data.choices && data.choices.length > 0) {
    const delta = data.choices[0].delta;
    if (delta && delta.content) {
      // 使用打字机效果显示内容，而不是直接追加
      await typeWriterEffect(delta.content, messageIndex, 20, true);
    }
  }
};

// 模拟打字机效果的函数
const typeWriterEffect = async (text, messageIndex, speed = 30, isAppend = false) => {
  let i = 0;
  const len = text.length;
  
  return new Promise((resolve) => {
    const typeChar = () => {
      if (i < len) {
        // 每次添加一个字符
        if (isAppend) {
          // 追加模式，添加到现有内容后面
          chatHistory.value[messageIndex].content += text.charAt(i);
        } else {
          // 覆盖模式，替换整个内容（用于模拟响应）
          const currentContent = chatHistory.value[messageIndex].content;
          chatHistory.value[messageIndex].content = currentContent + text.charAt(i);
        }
        i++;
        scrollToBottom();
        setTimeout(typeChar, speed);
      } else {
        resolve();
      }
    };
    
    // 开始打字效果
    typeChar();
  });
};

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return;
  
  // 检查用户是否已登录
  if (!userStore.isLogin) {
    Message.warning('请先登录后再使用非遗小助手');
    toggleChat(); // 关闭聊天窗口
    router.push('/login'); // 导航到登录页面
    return;
  }
  
  // 添加用户消息到聊天历史
  chatHistory.value.push({
    role: 'user',
    content: userInput.value
  });
  
  // 保存更新后的历史(包含用户消息)
  saveChatHistory(chatHistory.value);
  
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 设置加载状态
  isLoading.value = true;
  
  // 滚动到底部显示最新消息
  scrollToBottom();
  
  try {
    // 准备发送给API的消息历史
    const messagesToSend = chatHistory.value
      .slice(-10) // 只发送最近的10条消息，避免过多数据
      .map(msg => ({
        role: msg.role,
        content: msg.content
      }));
    
    // 先添加一条空的助手消息，用于流式显示
    chatHistory.value.push({
      role: 'assistant',
      content: ''
    });
    
    // 最新消息的索引
    const newMessageIndex = chatHistory.value.length - 1;
    
    try {
      // 尝试调用API获取流式回复
      const response = await sendAssistantMessage(messagesToSend);
      
      if (response && response.code === 200) {
        // 处理流式响应
        if (response.data && typeof response.data === 'object') {
          // 如果是流式响应对象
          if (response.data.choices && Array.isArray(response.data.choices)) {
            // 从流式响应中提取完整文本
            const fullContent = response.data.choices.reduce((text, choice) => {
              if (choice.delta && choice.delta.content) {
                return text + choice.delta.content;
              }
              return text;
            }, '');
            
            // 使用打字机效果显示
            await typeWriterEffect(fullContent, newMessageIndex, 20);
          } else {
            // 单块响应处理
            await handleStreamResponse(response.data, newMessageIndex);
          }
        } else if (typeof response.data === 'string') {
          // 如果不是流式响应，但返回了文本，使用打字机效果显示
          await typeWriterEffect(response.data || '抱歉，我没能理解您的问题。', newMessageIndex, 20);
        }
        
        // 保存更新后的历史(包含AI回复)
        saveChatHistory(chatHistory.value);
      } else {
        throw new Error(response?.msg || '请求失败');
      }
    } catch (error) {
      console.error('API请求失败，使用模拟响应:', error);
      
      // 模拟流式打字效果
      const simulatedResponse = simulateAIResponse(userQuestion);
      await typeWriterEffect(simulatedResponse, newMessageIndex, 20);
      
      // 保存更新后的历史(包含模拟回复)
      saveChatHistory(chatHistory.value);
    }
  } catch (error) {
    console.error('聊天处理错误:', error);
    chatHistory.value.push({
      role: 'assistant',
      content: '抱歉，我暂时无法回答您的问题。请稍后再试。'
    });
    // 保存更新后的历史
    saveChatHistory(chatHistory.value);
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 清除聊天历史，开启新对话
const startNewChat = () => {
  chatHistory.value = [{
    role: 'assistant',
    content: '您好，我是非遗小助手，有什么关于非物质文化遗产的问题，我很乐意为您解答！您还可以上传图片，我能识别图片中的非遗内容。'
  }];
  saveChatHistory(chatHistory.value);
  Message.success('已开启新对话');
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
  // 扩展模式下禁止拖拽
  if (isExpanded.value) {
    return;
  }
  
  // 点击的是输入框、发送按钮或聊天内容，不启动拖拽
  if (e.target.closest('.chat-input') || 
      e.target.closest('.chat-send-btn') || 
      e.target.closest('.chat-messages') || 
      e.target.closest('.chat-actions') ||
      e.target.closest('.image-upload-btn') ||
      e.target.closest('.camera-btn') ||
      e.target.closest('.image-preview')) {
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
  // 检查用户是否已登录
  if (!userStore.isLogin) {
    Message.warning('请先登录后再使用非遗小助手');
    router.push('/login'); // 导航到登录页面
    return;
  }
  
  toggleChat();
};

// 图片识别相关
const imageFile = ref(null);
const imagePreviewUrl = ref('');
const isUploadingImage = ref(false);

// 处理图片选择
const handleImageSelect = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    Message.error('请选择图片文件');
    return;
  }
  
  // 验证文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    Message.error('图片大小不能超过5MB');
    return;
  }
  
  // 设置预览
  imageFile.value = file;
  imagePreviewUrl.value = URL.createObjectURL(file);
};

// 清除已选择的图片
const clearSelectedImage = () => {
  imageFile.value = null;
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value);
    imagePreviewUrl.value = '';
  }
};

// 拍照功能
const openCamera = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.capture = 'camera';
  input.onchange = handleImageSelect;
  input.click();
};

// 上传并识别图片
const uploadAndRecognizeImage = async () => {
  if (!imagePreviewUrl.value || isUploadingImage.value) return;
  
  // 检查用户是否已登录
  if (!userStore.isLogin) {
    Message.warning('请先登录后再使用非遗小助手');
    toggleChat(); // 关闭聊天窗口
    router.push('/login'); // 导航到登录页面
    return;
  }
  
  isUploadingImage.value = true;
  
  try {
    // 先上传图片
    const uploadResponse = await uploadImage(imageFile.value);
    if (uploadResponse.code !== 200) {
      throw new Error(uploadResponse.msg || '图片上传失败');
    }
    
    const imageUrl = uploadResponse.data;
    
    // 添加用户消息（带图片）到聊天历史
    chatHistory.value.push({
      role: 'user',
      content: '请识别这张图片中的非遗内容',
      imageUrl: imageUrl
    });
    
    // 保存更新后的历史
    saveChatHistory(chatHistory.value);
    
    // 滚动到底部显示最新消息
    scrollToBottom();
    
    // 清除预览
    clearSelectedImage();
    
    // 设置加载状态
    isLoading.value = true;
    
    // 调用识别API
    const recognizeResponse = await recognizeImage(imageUrl);
    
    if (recognizeResponse.code !== 200) {
      throw new Error(recognizeResponse.msg || '图片识别失败');
    }
    
    // 提取识别结果并添加到聊天历史
    const recognitionResult = recognizeResponse.data.result;
    
    // 将AI回复添加到聊天历史
    chatHistory.value.push({
      role: 'assistant',
      content: recognitionResult
    });
    
    // 保存更新后的历史
    saveChatHistory(chatHistory.value);
  } catch (error) {
    console.error('图片识别错误:', error);
    Message.error(error.message || '图片识别失败');
    
    // 添加错误消息到聊天
    chatHistory.value.push({
      role: 'assistant',
      content: '很抱歉，无法识别该图片。请确保图片清晰且包含非遗相关内容，或稍后再试。'
    });
    
    // 保存更新后的历史
    saveChatHistory(chatHistory.value);
  } finally {
    isUploadingImage.value = false;
    isLoading.value = false;
    scrollToBottom();
  }
};

// 拖拽上传相关
const handleDrop = (e) => {
  e.preventDefault();
  e.stopPropagation();
  
  if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
    const file = e.dataTransfer.files[0];
    
    // 验证是否为图片
    if (!file.type.startsWith('image/')) {
      Message.error('请拖拽图片文件');
      return;
    }
    
    // 验证大小
    if (file.size > 5 * 1024 * 1024) {
      Message.error('图片大小不能超过5MB');
      return;
    }
    
    // 设置图片预览
    imageFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

const handleDragOver = (e) => {
  e.preventDefault();
  e.stopPropagation();
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
  // 组件卸载前保存聊天历史
  saveChatHistory(chatHistory.value);
  
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  
  // 清理图片预览URL
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value);
  }
});
</script>

<template>
  <div 
    class="baike-assistant"
    :class="{ 'is-open': isOpen, 'is-dragging': isDragging, 'is-expanded': isExpanded }"
    :style="{ 
      left: `${position.x}px`, 
      top: `${position.y}px`,
      width: isExpanded ? '100vw' : 'auto',
      height: isExpanded ? '100vh' : 'auto' 
    }"
    @mousedown="startDrag"
  >
    <!-- 折叠状态下显示的图标 -->
    <div class="assistant-icon" @dblclick="handleDoubleClick" v-if="!isOpen">
      <img src="/background/ai-assist.png" alt="非遗小助手" />
    </div>
    
    <!-- 展开的聊天窗口 -->
    <div class="chat-window" :class="{ 'chat-window-left': !isExpanded, 'chat-window-expanded': isExpanded }" v-else>
      <!-- 全屏模式下的左侧历史对话区域 -->
      <div class="chat-history-sidebar" v-if="isExpanded">
        <div class="history-header">
          <button class="new-chat-btn" @click="startNewChat">
            开始新对话
          </button>
        </div>
        <div class="history-list">
          <div 
            v-for="item in historyChats" 
            :key="item.id"
            :class="['history-item', { 'history-header-item': item.isHeader }]"
          >
            <template v-if="item.isHeader">
              <div class="history-date">{{ item.title }}</div>
            </template>
            <template v-else>
              <div class="history-content">
                <div class="history-title">{{ item.title }}</div>
                <div class="history-time" v-if="item.time">{{ item.time }}</div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div :class="['chat-main', { 'chat-main-expanded': isExpanded }]">
        <div class="chat-header" @mousedown="startDrag">
          <div class="chat-title">
            <span>故梦阑珊非遗小助手</span>
          </div>
          <div class="chat-actions">
            <a-tooltip :content="isExpanded ? '缩小窗口' : '扩大窗口'">
              <a-button type="text" size="mini" @click.stop="toggleExpand">
                <icon-shrink v-if="isExpanded" />
                <icon-expand v-else />
              </a-button>
            </a-tooltip>
            <a-tooltip content="开启新对话">
              <a-button type="text" size="mini" @click.stop="startNewChat">
                <icon-refresh />
              </a-button>
            </a-tooltip>
            <a-button type="text" size="mini" @click.stop="toggleChat">
              <icon-close />
            </a-button>
          </div>
        </div>
        
        <div class="chat-body" @dragover="handleDragOver" @drop="handleDrop">
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
              <div class="message-content">
                <!-- 如果消息包含图片 -->
                <div v-if="msg.imageUrl" class="message-image">
                  <img :src="msg.imageUrl" alt="图片" class="uploaded-image" />
                  <div class="image-caption">{{ msg.content }}</div>
                </div>
                <div v-else>{{ msg.content }}</div>
              </div>
            </div>
            
            <div v-if="isLoading || isUploadingImage" class="chat-message assistant-message">
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
        
        <!-- 图片预览区域 -->
        <div class="image-preview-area" v-if="imagePreviewUrl">
          <div class="image-preview">
            <img :src="imagePreviewUrl" alt="预览图片" />
            <div class="image-preview-actions">
              <a-button type="text" size="mini" @click.stop="clearSelectedImage">
                <icon-delete />
              </a-button>
            </div>
          </div>
          <a-button 
            type="primary" 
            class="recognize-btn" 
            @click.stop="uploadAndRecognizeImage" 
            :loading="isUploadingImage"
          >
            识别图片
          </a-button>
        </div>
        
        <div class="chat-footer">
          <!-- 图片上传相关按钮 -->
          <div class="chat-tools">
            <input
              type="file"
              accept="image/*"
              class="file-input"
              ref="fileInput"
              @change="handleImageSelect"
              style="display: none"
            />
            
            <a-button
              type="text"
              class="image-upload-btn"
              size="mini"
              @click.stop="$refs.fileInput.click()"
              :disabled="isLoading || isUploadingImage"
            >
              <icon-image />
            </a-button>
            
            <a-button
              type="text"
              class="camera-btn"
              size="mini"
              @click.stop="openCamera"
              :disabled="isLoading || isUploadingImage"
            >
              <icon-camera />
            </a-button>
          </div>
          
          <a-input
            v-model="userInput"
            class="chat-input"
            placeholder="请输入您的问题，或拖拽图片到此处..."
            @keydown="handleKeyDown"
            allow-clear
            :disabled="isLoading || isUploadingImage"
          />
          
          <a-button 
            class="chat-send-btn" 
            type="primary" 
            @click.stop="sendMessage" 
            :disabled="!userInput.trim() || isLoading || isUploadingImage"
          >
            <icon-send />
          </a-button>
        </div>
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

.baike-assistant.is-expanded {
  top: 0 !important;
  left: 0 !important;
  width: 100vw;
  height: 100vh;
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
  transition: width 0.3s ease, height 0.3s ease;
}

/* 聊天窗口向左打开 */
.chat-window-left {
  right: 80px;
  top: -370px;
}

/* 扩大后的聊天窗口 */
.chat-window-expanded {
  width: 100%;
  height: 100%;
  top: 0;
  right: 0;
  left: 0;
  border-radius: 0;
  border: none;
  box-shadow: none;
  display: flex;
  flex-direction: row;
}

/* 左侧历史对话列表 */
.chat-history-sidebar {
  width: 300px;
  height: 100%;
  background-color: #f7f7f7;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.new-chat-btn {
  width: 100%;
  padding: 12px;
  background-color: #8C1F28;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.new-chat-btn:hover {
  background-color: #751a22;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  padding: 10px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
}

.history-item:hover:not(.history-header-item) {
  background-color: #e9e9e9;
}

.history-header-item {
  cursor: default;
  padding: 15px 10px 5px 10px;
}

.history-date {
  font-size: 14px;
  color: #666;
  font-weight: 600;
}

.history-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-title {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 75%;
}

.history-time {
  font-size: 12px;
  color: #999;
}

/* 右侧主聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.chat-main-expanded {
  max-width: calc(100% - 300px);
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

.is-expanded .chat-header {
  padding: 15px 20px;
}

.chat-title {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.is-expanded .chat-title {
  font-size: 18px;
}

/* 头部按钮样式 */
.chat-actions {
  display: flex;
  gap: 5px;
}

.chat-actions .arco-btn {
  color: white !important;
}

.is-expanded .chat-actions {
  gap: 10px;
}

.is-expanded .chat-actions .arco-btn {
  font-size: 20px !important;
  width: 44px;
  height: 44px;
}

.chat-actions .arco-btn:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
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

.is-expanded .message-avatar {
  width: 60px;
  height: 60px;
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

.is-expanded .chat-footer {
  padding: 15px 20px;
}

.chat-input {
  flex: 1;
}

.is-expanded .chat-input {
  font-size: 16px;
  height: 46px;
}

.chat-send-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
}

.is-expanded .chat-send-btn {
  width: 52px;
  height: 52px;
  font-size: 22px;
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

/* 图片上传相关样式 */
.chat-tools {
  display: flex;
  align-items: center;
  gap: 5px;
}

.is-expanded .chat-tools {
  gap: 10px;
}

.image-upload-btn,
.camera-btn {
  color: #8C1F28;
}

.is-expanded .image-upload-btn,
.is-expanded .camera-btn {
  font-size: 22px !important;
  width: 46px;
  height: 46px;
}

.image-upload-btn:hover,
.camera-btn:hover {
  background-color: rgba(140, 31, 40, 0.1);
}

/* 图片预览区域 */
.image-preview-area {
  padding: 10px;
  background-color: #fff;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.image-preview {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  max-height: 150px;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.image-preview-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  display: flex;
  gap: 5px;
}

.image-preview-actions .arco-btn {
  padding: 4px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
}

.recognize-btn {
  background-color: #8C1F28;
  border-color: #8C1F28;
  width: 100%;
}

.recognize-btn:hover {
  background-color: #751a22;
  border-color: #751a22;
}

/* 用户上传的图片样式 */
.message-image {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.uploaded-image {
  max-width: 100%;
  max-height: 150px;
  border-radius: 8px;
  margin-bottom: 5px;
}

.is-expanded .uploaded-image {
  max-height: 300px;
}

.image-caption {
  font-size: 12px;
  color: #888;
}

.is-expanded .image-caption {
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .chat-history-sidebar {
    display: none;
  }
  
  .chat-main-expanded {
    max-width: 100%;
  }
}
</style> 