<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { getTopicsAPI, createPostAPI, getPostDetailAPI, updatePostAPI, checkSensitiveWordsAPI, uploadImageAPI } from '@/api/forum';
import { useUserStore } from '@/stores/userStore.js';
import { IconLoading } from '@arco-design/web-vue/es/icon';

// 导入自定义Markdown编辑器和查看器
import { Editor, Viewer } from '@/views/web/pages/forum/bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import gemoji from '@bytemd/plugin-gemoji'
import zhHans from 'bytemd/locales/zh_Hans.json'
// 导入ByteMD样式
import 'bytemd/dist/index.css'
// 导入getProcessor用于Markdown到HTML的转换
import { getProcessor } from 'bytemd';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// ByteMD插件
const plugins = [
  gfm(),
  highlight(),
  gemoji(),
]

// 判断是新建还是编辑
const isEdit = computed(() => route.name === 'EditPost');
const postId = computed(() => route.params.id);

// 表单数据
const postForm = reactive({
  title: '',
  topic: '',
  content: '',
  images: [] // 添加图片数组字段
});

// 图片上传相关
const fileInputRef = ref(null);
const uploading = ref(false);
const imageFiles = ref([]); // 存储选择的图片文件
const previewImages = ref([]); // 存储图片预览URL
const uploadProgress = ref(0); // 上传进度

// 敏感词相关
const checkingSensitiveWords = ref(false);
const sensitiveWordsError = ref('');

// 添加加载状态
const loading = ref(false);

// 在script setup开始位置添加草稿相关常量和editorValue初始化
const DRAFT_KEY = 'forum_post_draft';
const DRAFT_TIMESTAMP_KEY = 'forum_post_draft_timestamp';

// 创建预览区域 - 移动到前面
const editorValue = ref('');  // 用于存储编辑器内容

// 计算编辑器内容的字数
const contentWordCount = computed(() => {
  // 没有内容返回0
  if (!editorValue.value) return 0;
  
  // 去除markdown图片语法后计算字数
  const textOnly = editorValue.value.replace(/!\[.*?\]\(.*?\)/g, '图片');
  return textOnly.length;
});

// ByteMD编辑器上传图片处理函数
const handleUploadBytemdImages = async (files) => {
  try {
    // 检查已有图片数量
    const imageRegex = /!\[.*?\]\((.*?)\)/g;
    // 使用可选链操作符避免未定义错误
    const matches = [...((editorValue?.value || '').matchAll(imageRegex))];
    const currentImages = matches.length;
    
    if (currentImages + files.length > 9) {
      Message.warning('最多只能上传9张图片');
      return [];
    }
    
    // 检查文件类型和大小
    const validFiles = Array.from(files).filter(file => {
      const isImage = file.type.startsWith('image/');
      const isValidSize = file.size <= 5 * 1024 * 1024; // 5MB限制
      
      if (!isImage) {
        Message.warning(`文件 ${file.name} 不是有效的图片格式`);
      }
      
      if (!isValidSize) {
        Message.warning(`文件 ${file.name} 超过5MB大小限制`);
      }
      
      return isImage && isValidSize;
    });
    
    if (!validFiles.length) return [];
    
    // 开始上传
    uploading.value = true;
    const urls = [];
    
    // 逐个上传文件
    for (let i = 0; i < validFiles.length; i++) {
      const file = validFiles[i];
      
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        // 上传到服务器
        const res = await uploadImageAPI(formData);
        
        if (res.code === 200 && res.data) {
          // 保证URL是绝对URL
          let imageUrl = res.data;
          
          // 不是绝对URL，转换为绝对URL
          if (imageUrl && typeof imageUrl === 'string' && !imageUrl.startsWith('http')) {
            const baseUrl = 'http://sw8nkdw7h.hn-bkt.clouddn.com';
            imageUrl = new URL(imageUrl, baseUrl).href;
          }
          
          // 保证图片URL不为undefined
          if (imageUrl && imageUrl !== 'undefined') {
            // 将URL添加到结果数组 - 这些URL将直接被ByteMD使用
            urls.push(imageUrl);
            
            // 确保URL也被添加到表单数据中
            if (!postForm.images.includes(imageUrl)) {
              postForm.images.push(imageUrl);
            }
            
            console.log('图片上传成功，URL:', imageUrl);
          }
        } else {
          Message.warning(`图片 ${file.name} 上传失败`);
          console.error('图片上传失败，响应:', res);
        }
      } catch (error) {
        console.error('上传单张图片失败:', error);
        Message.error(`图片 ${file.name} 上传失败`);
      }
      
      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / validFiles.length) * 100);
    }
    
    uploading.value = false;
    uploadProgress.value = 0;
    
    // 打印上传完成的URL数组
    console.log('所有图片上传完成，URLs:', urls);
    
    // 返回URL数组，不包含undefined
    return urls.filter(url => url && url !== 'undefined');
  } catch (err) {
    console.error('上传编辑器图片出错:', err);
    Message.error('上传图片失败，请稍后重试');
    uploading.value = false;
    uploadProgress.value = 0;
    return [];
  }
}

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题' },
    { minLength: 2, maxLength: 50, message: '标题长度在2到50个字符之间' }
  ],
  topic: [
    { required: true, message: '请选择话题' }
  ],
  content: [
    { required: true, message: '请输入内容' },
    { minLength: 10, maxLength: 10000, message: '内容长度在10到10000个字符之间' }
  ]
};

// 话题列表
const topics = ref([]);
const submitting = ref(false);
const postFormRef = ref(null);

// 获取话题列表
const fetchTopics = async () => {
  loading.value = true;
  try {
    const res = await getTopicsAPI();
    if (res.code === 200) {
      topics.value = res.data || [];
    } else {
      Message.warning(res.msg || '获取话题失败');
    }
  } catch (err) {
    console.error('获取话题列表出错:', err);
    Message.error('获取话题失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 编辑模式，获取帖子详情
const fetchPostDetail = async () => {
  if (!isEdit.value) return;
  
  loading.value = true;
  try {
    const res = await getPostDetailAPI(postId.value);
    if (res.code === 200) {
      const post = res.data;
      console.log('获取到的帖子详情:', post);
      
      postForm.title = post.title;
      
      // 检查话题并设置
      const foundTopic = topics.value.find(t => t.name === post.topic);
      if (foundTopic) {
        postForm.topic = foundTopic.id;
      } else {
        // 找不到对应话题，尝试查找默认话题
        const defaultTopic = topics.value.length > 0 ? topics.value[0].id : '';
        postForm.topic = defaultTopic;
      }
      
      // 检查内容是否为HTML格式
      const contentIsHTML = post.content && (
        post.content.includes('<p>') || 
        post.content.includes('<div>') || 
        post.content.includes('<h1>') ||
        post.content.includes('<h2>') ||
        post.content.includes('<h3>') ||
        post.content.includes('<span>') ||
        post.content.includes('<ul>') ||
        post.content.includes('<ol>') ||
        post.content.includes('<li>')
      );
      
      if (contentIsHTML) {
        // 在前端显示的内容需要添加HTML格式的注释，以便用户在编辑时了解
        postForm.content = `<!-- 注意：此内容为HTML格式，保存后将会保留当前视觉效果 -->\n${post.content}`;
        
        // 给用户显示提示信息
        Message.info('当前内容为HTML格式，保存后将保留渲染后的视觉效果。');
      } else {
        // 正常的Markdown内容
        postForm.content = post.content;
      }
      
      // 同时更新editorValue      
      editorValue.value = postForm.content;
      
      // 处理已有的图片
      if (post.images && post.images.length > 0) {
        let imageArray = [];
        
        // 字符串分割成数组
        if (typeof post.images === 'string') {
          imageArray = post.images.split(',').filter(img => img);
        } else if (Array.isArray(post.images)) {
          imageArray = [...post.images];
        }
        
        console.log('编辑模式: 处理帖子图片:', imageArray);
        
        // 设置图片和预览
        postForm.images = imageArray;
        previewImages.value = [...imageArray];
      }
      
      // 检查内容中是否有图片，并提取
      const imageRegex = /!\[.*?\]\((.*?)\)/g;
      const matches = [...(post.content || '').matchAll(imageRegex)];
      if (matches.length > 0) {
        const contentImages = matches.map(match => match[1]);
        console.log('从Markdown内容中提取的图片:', contentImages);
        
        // 添加到图片列表中，避免重复
        contentImages.forEach(url => {
          if (!postForm.images.includes(url)) {
            postForm.images.push(url);
            previewImages.value.push(url);
          }
        });
      }
      
      // 提取HTML中的图片
      if (contentIsHTML) {
        const imgTagRegex = /<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>/g;
        const imgMatches = [...(post.content || '').matchAll(imgTagRegex)];
        if (imgMatches.length > 0) {
          const htmlImages = imgMatches.map(match => match[1]);
          console.log('从HTML内容中提取的图片:', htmlImages);
          
          // 添加到图片列表中，避免重复
          htmlImages.forEach(url => {
            if (!postForm.images.includes(url)) {
              postForm.images.push(url);
              previewImages.value.push(url);
            }
          });
        }
      }
    } else {
      Message.error(res.msg || '获取帖子详情失败');
      goBack();
    }
  } catch (err) {
    console.error('获取帖子详情出错:', err);
    Message.error('获取帖子详情失败，请稍后重试');
    goBack();
  } finally {
    loading.value = false;
  }
};

// 触发文件选择
const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

// 处理图片选择
const handleFileChange = (event) => {
  const files = event.target.files;
  if (!files.length) return;
  
  // 检查已有图片数量
  if (previewImages.value.length + files.length > 9) {
    Message.warning('最多只能上传9张图片');
    return;
  }
  
  // 检查文件类型和大小
  const validFiles = Array.from(files).filter(file => {
    const isImage = file.type.startsWith('image/');
    const isValidSize = file.size <= 5 * 1024 * 1024; // 5MB限制
    
    if (!isImage) {
      Message.warning(`文件 ${file.name} 不是有效的图片格式`);
    }
    
    if (!isValidSize) {
      Message.warning(`文件 ${file.name} 超过5MB大小限制`);
    }
    
    return isImage && isValidSize;
  });
  
  if (!validFiles.length) return;
  
  // 添加到文件列表
  imageFiles.value = [...imageFiles.value, ...validFiles];
  
  // 创建预览
  validFiles.forEach(file => {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImages.value.push(e.target.result);
    };
    reader.readAsDataURL(file);
  });
  
  // 清除input的value，允许重复选择同一文件
  event.target.value = '';
};

// 删除预览图片
const removeImage = (index) => {
  // 已上传的图片
  if (index < postForm.images.length) {
    postForm.images.splice(index, 1);
  }
  
  // 新选择的图片
  const newImageIndex = index - postForm.images.length;
  if (newImageIndex >= 0 && newImageIndex < imageFiles.value.length) {
    imageFiles.value.splice(newImageIndex, 1);
  }
  
  // 无论是哪种情况，都从预览中移除
  previewImages.value.splice(index, 1);
};

// 上传图片
const uploadImages = async () => {
  if (!imageFiles.value.length) return [];
  
  try {
    uploading.value = true;
    const uploadedUrls = [];
    
    // 逐个上传文件
    for (let i = 0; i < imageFiles.value.length; i++) {
      const file = imageFiles.value[i];
      
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', file);
      
      // 上传到服务器
      const res = await uploadImageAPI(formData);
      
      if (res.code === 200 && res.data) {
        uploadedUrls.push(res.data);
      } else {
        Message.warning(`图片 ${file.name} 上传失败`);
      }
      
      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / imageFiles.value.length) * 100);
    }
    
    return uploadedUrls;
  } catch (err) {
    console.error('上传图片出错:', err);
    Message.error('上传图片失败，请稍后重试');
    return [];
  } finally {
    uploading.value = false;
    uploadProgress.value = 0;
  }
};

// 修改onMounted钩子，添加加载草稿功能
onMounted(() => {
  fetchTopics();
  
  // 尝试从localStorage加载草稿
  if (!isEdit.value) { // 只在创建新帖子时加载草稿
    loadDraft();
  }
  
  fetchPostDetail();
  
  // 初始化时设置状态栏
  updateWordCount();
  
  // 创建一个可重复执行的图片刷新函数
  const attemptRefreshImages = (attempts = 0) => {
    if (attempts >= 3) return; // 最多尝试3次
    
    refreshPreviewImages();
    
    // 递归调用，每次间隔增加
    setTimeout(() => {
      attemptRefreshImages(attempts + 1);
    }, 1000 + attempts * 500); // 依次增加间隔时间
  };
  
  // 延迟启动刷新尝试，完全初始化编辑器
  setTimeout(() => {
    attemptRefreshImages();
    
    // 添加一个间隔检查，正常显示图片
    const intervalId = setInterval(() => {
      refreshPreviewImages();
    }, 5000); // 每5秒检查一次
    
    // 组件卸载时清除定时器
    onUnmounted(() => {
      clearInterval(intervalId);
    });
  }, 1000);
  
  // 添加页面关闭前的提示
  window.addEventListener('beforeunload', handleBeforeUnload);
});

// 添加单独的onUnmounted钩子
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
});

// 添加草稿相关函数
// 保存草稿到localStorage
const saveDraft = () => {
  if (!editorValue.value && !postForm.title) return; // 内容为空，则不保存
  
  try {
    const draft = {
      title: postForm.title,
      content: editorValue.value,
      topic: postForm.topic,
      images: postForm.images
    };
    
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
    localStorage.setItem(DRAFT_TIMESTAMP_KEY, Date.now().toString());
  } catch (error) {
    console.error('保存草稿失败:', error);
  }
};

// 加载草稿
const loadDraft = () => {
  try {
    const draftJson = localStorage.getItem(DRAFT_KEY);
    const timestamp = localStorage.getItem(DRAFT_TIMESTAMP_KEY);
    
    if (!draftJson || !timestamp) return;
    
    // 计算草稿的保存时间（超过24小时的草稿不加载）
    const now = Date.now();
    const draftTime = parseInt(timestamp, 10);
    const hoursDiff = (now - draftTime) / (1000 * 60 * 60);
    
    if (hoursDiff > 24) {
      clearDraft();
      return;
    }
    
    // 解析并加载草稿
    const draft = JSON.parse(draftJson);
    
    if (draft.content) {
      editorValue.value = draft.content;
      postForm.content = draft.content;
    }
    
    if (draft.title) {
      postForm.title = draft.title;
    }
    
    if (draft.topic) {
      postForm.topic = draft.topic;
    }
    
    if (draft.images && Array.isArray(draft.images)) {
      postForm.images = draft.images;
      previewImages.value = [...draft.images];
    }
    
    // 显示提示
    Message.info('已加载上次编辑的草稿');
  } catch (error) {
    console.error('加载草稿失败:', error);
  }
};

// 清除草稿
const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY);
  localStorage.removeItem(DRAFT_TIMESTAMP_KEY);
};

// 页面关闭前提示 - 修改为不显示确认提示
const handleBeforeUnload = (e) => {
  // 自动保存草稿但不提示确认
  if (editorValue.value && !submitting.value) {
    saveDraft();
  }
};

// 修改监听编辑器内容变化的函数，添加自动保存草稿功能
watch(editorValue, (newVal) => {
  postForm.content = newVal;
  
  // 提取内容中的图片URL
  const imageRegex = /!\[.*?\]\((.*?)\)/g;
  const matches = [...(newVal || '').matchAll(imageRegex)];
  const contentImages = matches.map(match => match[1]);
  
  // console.log('编辑器内容已更新，字数:', contentWordCount.value);
  // console.log('提取到的图片URLs:', contentImages);
  
  // 将图片添加到表单数据中
  if (contentImages.length > 0) {
    contentImages.forEach(url => {
      if (!postForm.images.includes(url)) {
        postForm.images.push(url);
      }
    });
  }
  
  // 添加延迟刷新预览区域中的图片
  setTimeout(() => {
    refreshPreviewImages();
  }, 500);
  
  // 自动保存草稿（防抖）
  clearTimeout(window.draftSaveTimeout);
  window.draftSaveTimeout = setTimeout(() => {
    if (!isEdit.value) { // 只在创建新帖子时保存草稿
      saveDraft();
    }
  }, 1000);
}, { immediate: true, deep: true });

// 标题和话题变化时也保存草稿
watch(() => postForm.title, () => {
  if (!isEdit.value) {
    clearTimeout(window.draftSaveTimeout);
    window.draftSaveTimeout = setTimeout(saveDraft, 1000);
  }
});

watch(() => postForm.topic, () => {
  if (!isEdit.value) {
    clearTimeout(window.draftSaveTimeout);
    window.draftSaveTimeout = setTimeout(saveDraft, 1000);
  }
});

const prepareContentForSubmission = (content) => {
  if (!content) return '';
  
  try {
    let processedContent = content;
    
    // 1. 修复图片URL
    processedContent = fixImageUrlsInContent(processedContent);
    
    // 2. 删除任何undefined的图片引用
    processedContent = processedContent.replace(/!\[.*?\]\(undefined\)/g, '');
    processedContent = processedContent.replace(/!\[.*?\]\(.*?undefined.*?\)/g, '');
    
    // 3. 处理相对路径图片
    const relativeImageRegex = /!\[(.*?)\]\((?!http)(.*?)\)/g;
    processedContent = processedContent.replace(relativeImageRegex, (match, alt, url) => {
      // 只处理非http开头的URL
      if (!url || !url.startsWith('http')) {
        // 检查是否为空URL
        if (!url || url.trim() === '') {
          return ''; // 移除无效图片标记
        }
        
        const baseUrl = 'http://sw8nkdw7h.hn-bkt.clouddn.com';
        const absoluteUrl = url.startsWith('/') ? 
          `${baseUrl}${url}` : 
          `${baseUrl}/${url}`;
        return `![${alt}](${absoluteUrl})`;
      }
      return match;
    });
    
    // 4. 从已上传图片中尝试恢复丢失的链接
    const brokenImageRegex = /!\[(.*?)\]\(\s*\)/g;
    processedContent = processedContent.replace(brokenImageRegex, (match, alt) => {
      // 尝试从已上传图片中找到匹配的URL
      const possibleImage = postForm.images.find(img => 
        img && img.includes(alt.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
      );
      
      if (possibleImage) {
        return `![${alt}](${possibleImage})`;
      }
      return '';
    });

    const allImagesRegex = /!\[(.*?)\]\((.*?)\)/g;
    processedContent = processedContent.replace(allImagesRegex, (match, alt, url) => {
      if (!url || !url.startsWith('http')) {
        return ''; // 移除无效图片
      }
      return match;
    });
    
    // 6. 清理多余的空行
    processedContent = processedContent.replace(/\n{3,}/g, '\n\n');
    
    // console.log('处理后的内容：', processedContent);
    return processedContent;
  } catch (error) {
    console.error('处理提交内容出错:', error);
    return content; // 出错时返回原始内容
  }
};

// 修改submitForm函数中的内容处理部分
const submitForm = async () => {
  if (!userStore.isLogin) {
    Message.warning('请先登录再发布帖子');
    return;
  }
  
  // 表单验证
  if (!postFormRef.value) {
    Message.warning('表单引用不存在');
    return;
  }
  
  submitting.value = true;
  
  try {
    // 在提交前全面处理内容
    if (postForm.content) {
      // 使用新的预处理函数
      const processedContent = prepareContentForSubmission(postForm.content);
      
      if (processedContent !== postForm.content) {
        console.log('内容已完全预处理，准备提交');
        postForm.content = processedContent;
        editorValue.value = processedContent;
      }
    }
    
    // 检查内容中是否还有undefined图片URL
    const undefinedCheck = /!\[.*?\]\(undefined\)/.test(postForm.content);
    if (undefinedCheck) {
      // console.warn('内容中仍然存在undefined的图片URL，继续尝试修复...');
      
      // 移除所有undefined图片
      postForm.content = postForm.content.replace(/!\[.*?\]\(undefined\)/g, '');
      editorValue.value = postForm.content;
      
      // 强制刷新预览区域图片
      refreshPreviewImages();
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    // 手动验证表单
    let isValid = true;
    
    // 手动验证标题
    if (!postForm.title || postForm.title.length < 2 || postForm.title.length > 50) {
      isValid = false;
      Message.error('标题长度在2到50个字符之间');
    }
    
    // 手动验证话题
    if (!postForm.topic) {
      isValid = false;
      Message.error('请选择话题');
    }
    
    // 手动验证内容
    if (!postForm.content || postForm.content.length < 10 || postForm.content.length > 10000) {
      isValid = false;
      Message.error('内容长度在10到10000个字符之间');
    }
    
    if (!isValid) {
      submitting.value = false;
      return;
    }
    
    // 准备提交前检查敏感词
    checkingSensitiveWords.value = true;
    sensitiveWordsError.value = '';
    
    // 合并标题和内容进行检查
    const textToCheck = postForm.title + ' ' + postForm.content;
    
    try {
      const checkResult = await checkSensitiveWordsAPI({ text: textToCheck });
      // console.log('敏感词检查结果:', checkResult);
      
      if (checkResult.code === 200 && checkResult.data.containsSensitiveWords) {
        const sensitiveWords = checkResult.data.sensitiveWords || [];
        // 检查是否通过了AI审核 - 关键修改点
        if (!checkResult.data.aiApproved) {
          sensitiveWordsError.value = `内容包含敏感词: ${sensitiveWords.join(', ')}，请修改后重新提交`;
          Message.error(sensitiveWordsError.value);
          submitting.value = false;
          checkingSensitiveWords.value = false;
          return;
        } else {
          // AI判断内容合规，只记录日志但不向用户展示提示
          console.log('内容包含敏感词，但AI判断合规，继续提交');
        }
      }
    } catch (err) {
      console.error('敏感词检查出错:', err);
      // 敏感词检查出错，继续提交流程
    } finally {
      checkingSensitiveWords.value = false;
    }
    
    // 上传新选择的图片
    let newUploadedImages = [];
    if (imageFiles.value.length > 0) {
      newUploadedImages = await uploadImages();
      if (!newUploadedImages.length && imageFiles.value.length > 0) {
        Message.error('图片上传失败，请稍后重试');
        submitting.value = false;
        return;
      }
    }
    
    // 提取内容中所有的图片URL，避免图片重复提交
    const imageRegex = /!\[.*?\]\((.*?)\)/g;
    const contentMatches = [...(postForm.content.matchAll(imageRegex))];
    const contentImageUrls = contentMatches.map(match => match[1]);
    
    // 合并所有图片URL并去重
    const allImageUrls = [...new Set([...postForm.images, ...newUploadedImages, ...contentImageUrls])];
    
    // 过滤出服务器的图片地址（即去除base64和其他非服务器图片）
    const serverImageUrls = allImageUrls.filter(url => 
      url.startsWith('http') && !url.includes('base64')
    );
    
    // 准备话题数据
    // 查找选定话题的名称
    const selectedTopic = topics.value.find(t => t.id === postForm.topic);
    const topicValue = selectedTopic ? selectedTopic.name : '';
    
    // 将Markdown内容转换为HTML (关键修改)
    
    // 检查内容是否已经是HTML格式
    const contentIsHTML = postForm.content && (
      postForm.content.includes('<p>') || 
      postForm.content.includes('<div>') || 
      postForm.content.includes('<h1>') ||
      postForm.content.includes('<h2>') ||
      postForm.content.includes('<h3>') ||
      postForm.content.includes('<span>') ||
      postForm.content.includes('<ul>') ||
      postForm.content.includes('<ol>') ||
      postForm.content.includes('<li>')
    );
    
    let htmlContent;
    
    if (contentIsHTML) {

      htmlContent = postForm.content.replace(/<!--\s*注意：此内容为HTML格式，保存后将会保留当前视觉效果\s*-->\n?/g, '');
      // console.log('内容已经是HTML格式，跳过转换');
    } else {
      // 创建与预览区相同的转换选项
      const processorOptions = {
        plugins: plugins,
        sanitize: undefined, // 保持默认或与预览区相同
        remarkRehype: undefined // 保持默认或与预览区相同
      };
      
      // 将Markdown转换为HTML格式
      const processor = getProcessor(processorOptions);
      htmlContent = processor.processSync(postForm.content).toString();
      // console.log('转换后的HTML内容长度:', htmlContent.length);
    }
    
    const formData = {
      title: postForm.title,
      content: htmlContent, // 使用转换后的HTML而不是原始Markdown
      topic: topicValue,
      images: serverImageUrls.join(',') // 将去重后的图片URL数组转换为逗号分隔的字符串
    };
    
    // console.log('提交表单数据:', formData);
    
    let res;
    
    if (isEdit.value) {
      res = await updatePostAPI(postId.value, formData);
    } else {
      res = await createPostAPI(formData);
    }
    
    if (res.code === 200) {
      // 清除草稿
      clearDraft();
      
      Message.success(isEdit.value ? '帖子更新成功' : '帖子发布成功');
      // 修改跳转逻辑：编辑模式仍跳转到详情页，新建模式跳转到帖子列表页
      if (isEdit.value) {
        router.push(`/forum/detail/${postId.value}`);
      } else {
        // 发布新帖后跳转到帖子列表页而不是详情页，避免数据未就绪问题
        router.push('/forum/list');
      }
    } else {
      Message.error(res.msg || (isEdit.value ? '更新失败' : '发布失败'));
    }
  } catch (err) {
    console.error(isEdit.value ? '更新帖子出错:' : '发布帖子出错:', err);
    Message.error(isEdit.value ? '更新失败，请稍后重试' : '发布失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/forum/list');
};

// 修改fixImageUrlsInContent函数
const fixImageUrlsInContent = (content) => {
  if (!content) return content;
  
  // 查找所有图片Markdown语法
  const imageRegex = /!\[(.*?)\]\((.*?)\)/g;
  
  // 检查是否有图片需要替换
  const matches = [...content.matchAll(imageRegex)];
  
  // 替换所有undefined或本地路径的图片URL
  return content.replace(imageRegex, (match, altText, url) => {
    // 检查URL是否有效
    if (!url || url === 'undefined' || url === '图片上传中...' || 
        url.includes('undefined') || url.startsWith('http://localhost')) {
      
      // URL有问题，尝试根据alt文本在已上传的图片中查找
      const possibleImage = postForm.images.find(img => 
        img && img.includes(altText.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
      );
      
      if (possibleImage) {
        return `![${altText}](${possibleImage})`;
      }
      
      // 找不到匹配的图片，返回原始标记而不是占位符
      return match;
    }
    
    // 保证URL是绝对路径
    if (!url.startsWith('http')) {
      // 是相对路径，尝试转为绝对路径
      if (url.includes('/')) {
        const baseUrl = 'http://sw8nkdw7h.hn-bkt.clouddn.com';
        const absoluteUrl = url.startsWith('/') ? 
          `${baseUrl}${url}` : 
          `${baseUrl}/${url}`;
        return `![${altText}](${absoluteUrl})`;
      }
      
      // 尝试在已上传图片中查找匹配
      const possibleImage = postForm.images.find(img => 
        img && (img.includes(altText) || img.includes(url))
      );
      
      if (possibleImage) {
        return `![${altText}](${possibleImage})`;
      }
    }
    
    return match;
  });
};

// 修改enhancePreviewImages函数
const enhancePreviewImages = () => {
  // 查找预览区域
  const previewElement = document.querySelector('.bytemd-preview');
  if (!previewElement) return;
  
  // 查找所有图片元素
  const images = previewElement.querySelectorAll('img');
  if (images.length === 0) return;
  
  // 处理每个图片
  images.forEach((img, index) => {
    // 检查图片src是否有问题
    const src = img.getAttribute('src');
    if (!src || src === 'undefined' || src.includes('undefined') || 
        src.startsWith('http://localhost') || src === '图片上传中...') {
      
      // 获取alt文本
      const alt = img.getAttribute('alt') || '';
      
      // 尝试从alt文本和已上传图片中找到匹配
      let possibleImage = null;
      
      // 1. 首先尝试通过alt文本精确匹配
      possibleImage = postForm.images.find(imgUrl => 
        imgUrl && imgUrl.includes(alt.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
      );
      
      // 2. 没找到匹配，尝试通过索引匹配
      if (!possibleImage && index < postForm.images.length) {
        possibleImage = postForm.images[index];
      }
      
      // 3. 仍找不到匹配，使用任何可用的图片
      if (!possibleImage && postForm.images.length > 0) {
        possibleImage = postForm.images[0];
      }

      if (possibleImage) {
        img.src = possibleImage;
        
        // 强制应用样式
        img.style.display = 'block';
        img.style.maxWidth = '80%'; // 调整为更小的尺寸
        img.style.margin = '10px auto';
        img.style.border = '1px solid #E4D9C3';
        img.style.borderRadius = '4px';
        img.style.visibility = 'visible';
        img.style.opacity = '1';
      } else {
        // 无法修复，应用占位符样式
        img.style.display = 'none'; // 隐藏图片以避免显示损坏的图标
        
        // 添加占位符提示
        const placeholder = document.createElement('div');
        placeholder.innerHTML = `<div style="border:1px dashed #8C1F28; padding:10px; text-align:center; margin:10px auto; width:200px; background-color:#FFF7E9">
          <div style="color:#8C1F28; font-weight:bold;">图片上传中...</div>
          <div style="font-size:12px; color:#582F0E; margin-top:5px;">${alt || '图片'}</div>
        </div>`;
        img.parentNode.insertBefore(placeholder, img.nextSibling);
      }
    } else {
      // 图片URL正常
      img.style.display = 'block';
      img.style.maxWidth = '80%'; // 调整为更小的尺寸
      img.style.margin = '10px auto';
      img.style.border = '1px solid #E4D9C3';
      img.style.borderRadius = '4px';
      img.style.visibility = 'visible';
      img.style.opacity = '1';
      img.style.height = 'auto';
    }
    
    // 为图片添加错误处理
    if (!img.hasErrorHandler) {
      img.hasErrorHandler = true;
      img.onerror = function() {
        // 尝试从postForm.images中获取替代图片
        if (index < postForm.images.length) {
          this.src = postForm.images[index];
        } else if (postForm.images.length > 0) {
          this.src = postForm.images[0];
        } else {
          // 应用占位符样式
          this.style.display = 'none';
          const placeholder = document.createElement('div');
          placeholder.innerHTML = `<div style="border:1px dashed #8C1F28; padding:10px; text-align:center; margin:10px auto; width:200px; background-color:#FFF7E9">
            <div style="color:#8C1F28; font-weight:bold;">图片加载失败</div>
            <div style="font-size:12px; color:#582F0E; margin-top:5px;">${this.alt || '未知图片'}</div>
          </div>`;
          this.parentNode.insertBefore(placeholder, this.nextSibling);
        }
      };
    }
  });
};

// 更新refreshPreviewImages函数
const refreshPreviewImages = () => {
  // 查找预览区域
  const previewElement = document.querySelector('.bytemd-preview');
  if (!previewElement) return;

  if (editorValue?.value) {
    const fixedContent = fixImageUrlsInContent(editorValue.value);
    if (fixedContent !== editorValue.value) {
      editorValue.value = fixedContent;
      postForm.content = fixedContent;
    }
  }
  
  // 增强预览区域图片显示
  enhancePreviewImages();
  
  // 刷新预览区域中的所有图片
  const previewImgs = previewElement.querySelectorAll('img');
  if (previewImgs.length === 0) return;
  
  // 收集所有有效的图片URL
  const validImageUrls = postForm.images.filter(url => 
    url && typeof url === 'string' && url.startsWith('http') && !url.includes('undefined')
  );
  
  previewImgs.forEach((img, index) => {
    // 检查图片URL是否有效
    const imgSrc = img.getAttribute('src');
    let needsReplacement = false;
    
    if (!imgSrc || 
        imgSrc === 'undefined' || 
        imgSrc.includes('undefined') || 
        imgSrc === '图片上传中...' ||
        imgSrc.startsWith('http://localhost')) {
      needsReplacement = true;
    }
    
    // 替换URL
    if (needsReplacement) {
      // 尝试找到合适的替代URL
      const alt = img.getAttribute('alt') || '';
      let replacementUrl = null;
      
      // 1. 尝试通过alt文本匹配
      replacementUrl = validImageUrls.find(url => 
        url.includes(alt.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
      );
      
      // 2. 没找到，使用索引位置匹配
      if (!replacementUrl && index < validImageUrls.length) {
        replacementUrl = validImageUrls[index];
      }
      
      // 3. 仍然没有，使用第一个有效URL
      if (!replacementUrl && validImageUrls.length > 0) {
        replacementUrl = validImageUrls[0];
      }
      
      if (replacementUrl) {
        img.src = replacementUrl;
      }
    }
    
    // 应用样式
    img.style.display = 'block';
    img.style.visibility = 'visible';
    img.style.opacity = '1';
    img.style.maxWidth = '80%'; // 调整为更小的尺寸
    img.style.height = 'auto';
    img.style.margin = '10px auto';
    img.style.border = '1px solid #E4D9C3';
    img.style.borderRadius = '4px';
    
    // 添加加载错误处理
    if (!img._hasErrorHandler) {
      img._hasErrorHandler = true;
      img.onerror = function() {
        if (validImageUrls.length > 0) {
          let replacementUrl;
          
          // 优先使用相同索引的URL
          if (index < validImageUrls.length) {
            replacementUrl = validImageUrls[index];
          } else {
            replacementUrl = validImageUrls[0];
          }
          
          this.src = replacementUrl;
        } else {
          // 没有可用的有效URL，使用占位符
          this.style.display = 'none';

          if (!this.nextElementSibling || !this.nextElementSibling.classList.contains('image-placeholder')) {
            const placeholder = document.createElement('div');
            placeholder.className = 'image-placeholder';
            placeholder.style.border = '1px dashed #8C1F28';
            placeholder.style.padding = '10px';
            placeholder.style.textAlign = 'center';
            placeholder.style.margin = '10px 0';
            placeholder.style.backgroundColor = '#FFF7E9';
            placeholder.innerHTML = `<span style="color:#8C1F28; font-weight:bold;">图片加载中...</span>
                                     <br><span style="font-size:12px; color:#582F0E;">${this.alt || '图片'}</span>`;
            
            this.parentNode.insertBefore(placeholder, this.nextSibling);
          }
        }
      };
    }
  });
};

// 修改处理编辑器内容变化的事件处理函数
const handleEditorChange = (value) => {
  editorValue.value = value;
  postForm.content = value;
  
  // 提取内容中的图片URL
  const imageRegex = /!\[.*?\]\((.*?)\)/g;
  const matches = [...(value || '').matchAll(imageRegex)];
  const contentImages = matches.map(match => match[1]);
  
  // console.log('编辑器内容变更，当前字数:', value.length);
  // console.log('当前内容中的图片:', contentImages);
  
  // 手动更新状态栏字数统计
  updateWordCount();
  
  // 对内容进行预处理，在列表中正确显示
  processContentForPreview(value);
};

// 添加内容预处理函数
const processContentForPreview = (content) => {
  if (!content) return;
  
  try {
    // Markdown图片语法
    const imageRegex = /!\[(.*?)\]\((.*?)\)/g;
    let correctedContent = content;
    
    // 修复图片路径问题
    correctedContent = correctedContent.replace(imageRegex, (match, alt, url) => {
      // 检查URL是否有效
      if (!url || url === 'undefined' || url.includes('undefined') || !url.startsWith('http')) {
        // 尝试查找匹配的图片
        const possibleImage = postForm.images.find(img => 
          img && img.includes(alt.replace('.jpg', '').replace('.png', '').replace('.gif', ''))
        );
        
        if (possibleImage) {
          return `![${alt}](${possibleImage})`;
        }
      }
      return match;
    });
    
    // 更新有变化的内容
    if (correctedContent !== content) {
      editorValue.value = correctedContent;
      postForm.content = correctedContent;
    }
  } catch (error) {
    console.error('处理内容预览出错:', error);
  }
};

const updateWordCount = () => {
  setTimeout(() => {
    const statusElement = document.querySelector('.bytemd-status-left');
    if (statusElement && editorValue?.value !== undefined) {
      // 去除图片标记后的内容长度
      const textContent = editorValue.value.replace(/!\[.*?\]\(.*?\)/g, '图片');
      statusElement.innerHTML = `字数: ${textContent.length}`;
    }
  }, 10);
};

// 处理编辑器标题变化
const handleTitleChange = (value) => {
  postForm.title = value;
};

// 处理编辑器话题变化
const handleTopicChange = (value) => {
  postForm.topic = value;
};
</script>

<template>
  <div class="create-post-page">
    <div class="create-post-container">
      <div class="main-content">
        <a-spin :loading="loading" :size="40">
          <template #icon><icon-loading /></template>
          <a-form
            ref="postFormRef"
            :model="postForm"
            :rules="rules"
            class="post-form"
            :label-col-props="{ span: 0 }"  
            :wrapper-col-props="{ span: 24 }"
          >
            <!-- 集成式Markdown编辑器 -->
            <a-form-item field="content" class="editor-form-item" :label="null" hide-label>
              <div class="markdown-editor-wrapper">
                <Editor
                  :value="editorValue"
                  :plugins="plugins"
                  :locale="zhHans"
                  placeholder="请输入帖子内容（支持Markdown格式）"
                  :uploadImages="handleUploadBytemdImages"
                  @change="handleEditorChange"
                  :split="true"
                  :title="postForm.title"
                  :topicId="postForm.topic"
                  :topics="topics"
                  @titleChange="handleTitleChange"
                  @topicChange="handleTopicChange"
                  @back="goBack"
                  @publish="submitForm"
                />
                <!-- 添加调试信息显示区 -->
                <div v-if="previewImages.length > 0" class="debug-image-info" style="display:none;">
                  <p>预览图片数量: {{ previewImages.length }}</p>
                  <p>表单图片数量: {{ postForm.images.length }}</p>
                  <div v-for="(img, index) in previewImages" :key="index" style="margin-bottom: 5px;">
                    <span>图片 {{index+1}}: {{img}}</span>
                  </div>
                </div>
              </div>
            </a-form-item>
          </a-form>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-post-page {
  width: 100%;
  min-height: 100vh;
  background-color: #FFF7E9;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23D6C6AF' fill-opacity='0.05'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z'/%3E%3C/g%3E%3C/svg%3E");
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.create-post-container {
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: calc(100vh - 176px);
}

.main-content {
  display: flex;
  height: calc(100vh - 130px);
  position: relative;
  margin-top: 40px;
  width: 100%;
  overflow-x: hidden; /* 防止水平溢出 */
}

.post-form {
  flex: 1;
  overflow: hidden; /* 防止内容溢出 */
  margin-right: 15px;
  width: 100%;
  min-width: 0;
}

.editor-form-item {
  height: 100%;
  width: 100%;
}

/* 移除表单项内边距 */
:deep(.arco-form-item-content) {
  margin-bottom: 0;
  width: 100%;
}

.markdown-editor-wrapper {
  height: 100%;
  width: 100%;
  overflow: hidden;
  border: 1px solid #D6C6AF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-height: 550px !important;
}

/* ByteMD编辑器防止溢出 */
:deep(.bytemd) {
  height: 100% !important;
  min-height: 500px !important;
  max-height: none !important;
  resize: none !important;
  width: 100% !important;
  max-width: 100% !important;
  overflow-x: hidden !important;
}

:deep(.CodeMirror) {
  height: auto !important;
  min-height: 500px !important;
  width: 100% !important;
  max-width: 100% !important;
  word-wrap: break-word !important;
  white-space: pre-wrap !important;
  overflow-x: hidden !important;
}

:deep(.CodeMirror-scroll) {
  overflow-x: hidden !important;
}

:deep(.bytemd-preview) {
  height: auto !important;
  min-height: 500px !important;
  width: 100% !important;
  max-width: 100% !important;
  overflow-x: hidden !important;
}

/* 编辑与预览区域高度固定，宽度合适 */
:deep(.bytemd-split .bytemd-editor),
:deep(.bytemd-split .bytemd-preview) {
  height: 500px !important;
  min-height: 500px !important;
  overflow-y: auto !important;
  width: 50% !important;
  overflow-x: hidden !important; /* 防止水平溢出 */
}

/* 处理markdown内容区域溢出问题 */
:deep(.markdown-body) {
  overflow-wrap: break-word !important;
  word-break: break-all !important;
  max-width: 100% !important;
}

/* 代码块和预格式文本的溢出处理 */
:deep(.markdown-body pre),
:deep(.markdown-body code),
:deep(.markdown-body pre code) {
  white-space: pre-wrap !important;
  word-wrap: break-word !important;
  overflow-x: hidden !important;
}

/* 敏感词错误提示 */
.sensitive-words-alert {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  width: 90%;
  max-width: 600px;
  background-color: #FFFDF7;
  border: 1px solid #D6C6AF;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-icon {
  font-size: 24px;
  color: #8C1F28;
  margin-bottom: 2px;
}

.upload-text {
  font-size: 12px;
  color: #582F0E;
}

.file-input {
  display: none;
}

.upload-tips {
  font-size: 12px;
  color: #7F4F24;
  margin-top: 5px;
}

/* 敏感词警告 */
.sensitive-words-alert {
  margin-top: 0;
}

/* 按钮样式 */
.submit-btn {
  background-color: #8C1F28 !important;
  border-color: #8C1F28 !important;
  border-radius: 4px;
  padding: 0 16px;
  height: 36px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(140, 31, 40, 0.2);
  transition: all 0.3s ease;
  margin-top: 4px;
}

.submit-btn:hover {
  background-color: #A52A2A !important;
  border-color: #A52A2A !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(140, 31, 40, 0.3);
}

/* Markdown预览样式增强 */
:deep(.markdown-body) {
  font-family: "SimSun", "宋体", serif;
  color: #582F0E;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4),
:deep(.markdown-body h5),
:deep(.markdown-body h6) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 0.3em;
}

:deep(.markdown-body a) {
  color: #8C1F28;
  text-decoration: none;
}

:deep(.markdown-body a:hover) {
  text-decoration: underline;
}

:deep(.markdown-body blockquote) {
  padding: 0 1em;
  color: #6B5B45;
  border-left: 0.25em solid #E4D9C3;
}

:deep(.markdown-body pre) {
  background-color: #FFF7E9;
  border: 1px solid #E4D9C3;
  border-radius: 4px;
}

:deep(.markdown-body code) {
  background-color: #FFF7E9;
  color: #8C1F28;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
}

:deep(.markdown-body img) {
  max-width: 100%;
  box-sizing: content-box;
  border-radius: 4px;
  border: 1px solid #E4D9C3;
  display: block !important;
  margin: 10px auto !important;
  height: auto !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 图片显示在预览区域 */
:deep(.bytemd-preview img) {
  display: block !important;
  max-width: 100% !important;
  height: auto !important;
  margin: 10px auto !important;
  border-radius: 4px;
  border: 1px solid #D6C6AF;
}

/* 调整预览区域和编辑器样式 */
:deep(.bytemd-preview) {
  background-color: #FFFDF7 !important;
  padding: 15px !important;
}

/* 响应式样式 */
@media screen and (max-width: 1200px) {
  .sidebar {
    width: 250px;
  }
}

@media screen and (max-width: 900px) {
  .main-content {
    flex-direction: column;
    height: auto;
  }
  
  .markdown-editor-wrapper {
    height: 500px;
    margin-bottom: 10px;
  }
  
  .post-form {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .sidebar {
    width: 100%;
    margin-left: 0;
  }
}

@media screen and (max-width: 768px) {
  .create-post-container {
    width: 98%;
  }
  
  .image-preview-item,
  .image-upload-trigger {
    width: calc(33.333% - 7px);
  }
}

.editor-container {
  width: 100%;
  min-height: 300px;
}

:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.arco-spin-icon) {
  color: #8C1F28;
  font-size: 24px;
}
</style>