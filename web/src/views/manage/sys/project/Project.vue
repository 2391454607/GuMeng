<script setup>
import {onMounted, reactive, ref, nextTick} from "vue";
import {addProjectAPI, deleteFileAPI, deleteProjectAPI, getIchProjectAPI, updateProjectAPI, uploadImageAPI, uploadVideoAPI, batchUploadImagesAPI, batchDeleteFilesAPI} from "@/api/manage/IchProject.js";
import {Message} from "@arco-design/web-vue";
import { Editor, Viewer } from './bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm';
import highlight from '@bytemd/plugin-highlight';
import gemoji from '@bytemd/plugin-gemoji';
import zhHans from 'bytemd/locales/zh_Hans.json';
// 导入ByteMD样式
import 'bytemd/dist/index.css';
// 导入格式化工具
import { formatDate } from '@/utils/format.js';

// ByteMD插件配置
const plugins = [
  gfm(),
  highlight(),
  gemoji(),
]

// 提取七牛云图片URL的数组
const viewerQiniuUrls = ref([]);

//表格数据
const ProjectList = ref([])
// 封装加载数据的方法
const loading = ref();
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getIchProjectAPI(status);
    
    // 处理每条记录的封面图片
    if (res.data && res.data.records) {
      res.data.records.forEach(record => {
        // 处理封面图片
        if (record.images && record.images.includes(',')) {
          record.coverImage = record.images.split(',')[0].trim();
        } else if (record.images) {
          record.coverImage = record.images;
        } else {
          record.coverImage = '/image/default-cover.png';
        }
        
        // 格式化日期
        if (record.createTime) {
          record.createTimeFormatted = formatDate(record.createTime, 'yyyy-MM-dd hh:mm:ss');
        }
      });
    }
    
    ProjectList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    Message.error('数据加载失败：' + error.message);
  } finally {
    loading.value = false;
  }
};
//初始化页面
onMounted(()=>{
  loadData();
})

//分页器状态
const status = reactive({
  current: 1,
  size: 10,
  levelId: null,
  categoryId: null
})
const total = ref(0);
// 分页处理函数
const handlePageChange = async (current) => {
  status.current = current;
  await loadData();
};

// 添加选项数据
const levelOptions = [
  { label: '国家级', value: '1' },
  { label: '省级', value: '2' },
  { label: '市级', value: '3' },
  { label: '县级', value: '4' }
];

const categoryOptions = [
  { label: '民间文学', value: '1' },
  { label: '传统音乐', value: '2' },
  { label: '传统舞蹈', value: '3' },
  { label: '传统戏剧', value: '4' },
  { label: '曲艺', value: '5' },
  { label: '传统体育、游艺与杂技', value: '6' },
  { label: '传统美术', value: '7' },
  { label: '民俗', value: '8' },
  { label: '传统技艺', value: '9' },
  { label: '传统医药', value: '10' }
];

//定义新增的表单数据
const addProject = ref(false);
const ProjectAddClick = async () => {
  addProject.value = true;
  editorValue.value = ''; // 清空编辑器内容
  newProject.name = '';
  newProject.levelId = '';
  newProject.categoryId = '';
  newProject.video = null;
  newProject.images = [];
  imgFile.value = null;
};

const newProject = reactive({
  name: "",
  levelId: "",
  categoryId: "",
  video: null,
  images: []
});
const imgFile = ref(null);
// 编辑器内容
const editorValue = ref('');

// 处理临时上传的视频
const tempUploadedVideo = ref(null);
const originalVideoUrl = ref(null);

// 处理视频清理请求
const handleCleanupVideo = async (videoUrl) => {
  if (!videoUrl) return;
  
  try {
    // 只处理有效的URL
    if (videoUrl.startsWith('http')) {
      console.log('尝试清理视频:', videoUrl);
      
      // 从视频URL提取相对路径，比如video/timestamp_filename.mp4
      let fileName = videoUrl;
      if (videoUrl.includes('/video/')) {
        fileName = 'video/' + videoUrl.split('/video/')[1];
      }
      
      console.log('清理未保存的视频, fileName:', fileName);
      await deleteFileAPI('/sys/deleteFile?fileName=' + encodeURIComponent(fileName));
    }
  } catch (error) {
    console.error('清理视频失败:', error);
  }
};

// 视频处理函数
const getVideoFile = (videoUrl) => {
  // 记录临时视频，以便在取消时清理
  tempUploadedVideo.value = videoUrl;
  newProject.video = videoUrl;
};

// 使用localStorage存储内容
const PROJECT_CONTENT_KEY_PREFIX = 'project_content_';

const addOk = async () => {
  if (!newProject.name) {
    Message.warning('请输入项目名称');
    return;
  }
  
  if (!newProject.levelId) {
    Message.warning('请选择保护级别');
    return;
  }
  
  if (!newProject.categoryId) {
    Message.warning('请选择项目类别');
    return;
  }

  const formData = new FormData();

  formData.append('projectInfo', JSON.stringify({
    name: newProject.name,
    levelId: newProject.levelId,
    categoryId: newProject.categoryId,
    content: editorValue.value,
    video: newProject.video,
    images: Array.isArray(newProject.images) ? newProject.images.join(',') : newProject.images
  }));

  try {
    const res = await addProjectAPI(formData);
    if (res.code === 200) {
      if (editorValue.value && res.data) {
        localStorage.setItem(`${PROJECT_CONTENT_KEY_PREFIX}${res.data}`, editorValue.value);
      }
      Message.success(res.msg);
      await loadData();
      addProject.value = false;
    } else {
      Message.error(res.msg);
    }
  } catch(error) {
    Message.error('提交失败：' + error.message);
  }
};

// 从内容中提取所有可能的七牛云图片URL
const extractQiniuUrls = (content) => {
  // 优先使用项目的images字段
  if (viewProjectData.images) {
    if (typeof viewProjectData.images === 'string') {
      const imageArray = viewProjectData.images.split(',');
      if (imageArray.length > 0) {
        return imageArray;
      }
    }
  }
  
  if (!content) return [];

  // 匹配七牛云域名图片
  const qiniuDomainPattern = /https?:\/\/[^)\s"'<>]+?(?:clouddn\.com|hn-bkt\.clouddn\.com)[^)\s"'<>]*?\.(?:png|jpg|jpeg|gif|webp)/gi;
  let qiniuLinks = content.match(qiniuDomainPattern) || [];
  
  // 如果没找到七牛云图片，尝试匹配其他图片
  if (qiniuLinks.length === 0) {
    const genericImagePattern = /https?:\/\/[^)\s"'<>]+\.(?:png|jpg|jpeg|gif|webp)/gi;
    qiniuLinks = content.match(genericImagePattern) || [];
  }
  
  return qiniuLinks;
};

// 修复图片显示函数
const fixViewerImages = () => {
  setTimeout(() => {
    const viewer = document.querySelector('.project-view-container .markdown-body');
    if (!viewer) return;

    const images = viewer.querySelectorAll('img');
    if (!images.length) return;

    if (viewerQiniuUrls.value.length === 0) {
      viewerQiniuUrls.value = extractQiniuUrls(viewProjectData.content || '');
    }

    if (viewerQiniuUrls.value.length === 0) return;

    images.forEach((img, index) => {
      const src = img.getAttribute('src');
      const isInvalidUrl = !src || 
                          src === 'undefined' || 
                          src.includes('localhost') || 
                          src.includes('undefined');
      
      if (isInvalidUrl) {
        const newSrc = index < viewerQiniuUrls.value.length ? 
          viewerQiniuUrls.value[index] : 
          viewerQiniuUrls.value[0];
        
        if (newSrc) {
          img.src = newSrc;
          img.style.maxWidth = '90%';
          img.style.height = 'auto';
          img.style.margin = '15px auto';
          img.style.display = 'block';
        }
      }
    });
  }, 300);
};

// 添加查看状态和数据
const viewProject = ref(false);
const viewProjectData = reactive({
  id: "",
  name: "",
  levelName: "",
  categoryName: "",
  images: "",
  video: "",
  content: "",
  viewCount: 0,
  createTime: "",
  coverImage: ""
});

// 添加查看方法
const viewProjectClick = (record) => {
  viewProjectData.id = record.id;
  viewProjectData.name = record.name;
  viewProjectData.levelName = record.levelName;
  viewProjectData.categoryName = record.categoryName;
  viewProjectData.images = record.images;
  viewProjectData.video = record.video || '';
  viewProjectData.viewCount = record.viewCount;
  viewProjectData.createTime = formatDate(record.createTime, 'yyyy-MM-dd hh:mm:ss');
  viewProjectData.updateTime = record.updateTime;
  
  // 处理封面图片
  if (record.images && record.images.includes(',')) {
    viewProjectData.coverImage = record.images.split(',')[0].trim();
  } else if (record.images) {
    viewProjectData.coverImage = record.images;
  } else {
    viewProjectData.coverImage = '/image/default-cover.png';
  }
  
  // 优先使用数据库中的content
  viewProjectData.content = record.content || '';
  
  // 假如数据库中的content为空，尝试从localStorage获取内容作为备份
  if (!viewProjectData.content) {
    const storedContent = localStorage.getItem(`${PROJECT_CONTENT_KEY_PREFIX}${record.id}`);
    console.log('数据库内容为空，从localStorage获取项目内容，ID:', record.id, '内容:', storedContent ? storedContent.substring(0, 50) + '...' : '无内容');
    viewProjectData.content = storedContent || '';
  } else {
    console.log('从数据库获取项目内容，ID:', record.id);
  }
  
  viewProject.value = true;
  
  // 重置图片URL数组
  viewerQiniuUrls.value = [];
  
  // 在DOM更新后修复图片
  nextTick(() => {
    fixViewerImages();
  });
};


//修改操作
const updateProject = ref(false);
// 用于存储正在编辑数据
const updateProjectData = reactive({
  id: "",
  name: "",
  levelName: "",
  levelId: "",
  categoryName: "",
  categoryId: "",
  video: "",
  images: [],
  content: ""
});

// 更新编辑器内容
const updateEditorValue = ref('');

// 处理更新表单的视频上传
const getUpdateVideo = (videoUrl) => {
  updateProjectData.video = videoUrl;
};

const updateProjectClick = async (record) => {
  // 将当前行数据赋值填充到表单中
  updateProjectData.id = record.id;
  updateProjectData.name = record.name;
  updateProjectData.levelName = record.levelName;
  updateProjectData.categoryName = record.categoryName;
  // 根据名称找到对应的ID
  updateProjectData.levelId = levelOptions.find(option => option.label === record.levelName)?.value || '';
  updateProjectData.categoryId = categoryOptions.find(option => option.label === record.categoryName)?.value || '';
  updateProjectData.video = record.video || '';
  
  // 处理images字段
  if (record.images) {
    // 字符串转换为数组
    updateProjectData.images = record.images.split(',');
  } else {
    updateProjectData.images = [];
  }
  
  // 保存原始视频URL
  originalVideoUrl.value = record.video || '';
  
  // 优先使用数据库中的content
  updateProjectData.content = record.content || '';
  
  // 假如数据库中的content为空，尝试从localStorage获取内容作为备份
  if (!updateProjectData.content) {
    const storedContent = localStorage.getItem(`${PROJECT_CONTENT_KEY_PREFIX}${record.id}`);
    updateProjectData.content = storedContent || '';
    console.log('数据库内容为空，从localStorage获取项目内容用于编辑，ID:', record.id);
  } else {
    console.log('从数据库获取项目内容用于编辑，ID:', record.id);
  }
  
  updateEditorValue.value = updateProjectData.content;
  updateProject.value = true;
};

const updateOk = async () => {
  if (!updateProjectData.name) {
    Message.warning('请输入项目名称');
    return;
  }
  
  if (!updateProjectData.levelId) {
    Message.warning('请选择保护级别');
    return;
  }
  
  if (!updateProjectData.categoryId) {
    Message.warning('请选择项目类别');
    return;
  }

  const formData = new FormData();
  
  // 使用临时对象，方便调试
  const updateData = {
    id: updateProjectData.id,
    name: updateProjectData.name,
    levelId: updateProjectData.levelId,
    categoryId: updateProjectData.categoryId,
    content: updateEditorValue.value,
    video: updateProjectData.video,

    images: Array.isArray(updateProjectData.images) ? updateProjectData.images.join(',') : updateProjectData.images
  };
  
  console.log('准备更新的项目数据:', updateData);
  console.log('更新图片数据类型:', typeof updateProjectData.images, '内容:', updateProjectData.images);
  
  formData.append('projectInfo', JSON.stringify(updateData));

  try {
    const res = await updateProjectAPI(formData);
    if (res.code === 200) {
      if (updateEditorValue.value) {
        localStorage.setItem(`${PROJECT_CONTENT_KEY_PREFIX}${updateProjectData.id}`, updateEditorValue.value);
      }
      Message.success(res.msg);
      await loadData();
      updateProject.value = false;
    } else {
      Message.error(res.msg);
    }
  } catch (error) {
    Message.error('更新失败：' + error.message);
  }
};

// 删除操作
const del = ref(false);
const delClick = () => {
  del.value = true;
};
const delOk = async (record) => {
  try {
    const res = await deleteProjectAPI(`id=${record.id}`);
    if (res.code === 200) {
      // 删除对应的localStorage内容
      localStorage.removeItem(`${PROJECT_CONTENT_KEY_PREFIX}${record.id}`);
      Message.success(res.msg);
      await loadData();
    } else {
      Message.error(res.msg);
    }
  } catch (error) {
    Message.error('删除失败：' + error.message);
  }
  del.value = false;
};

// 编辑器内容变化处理函数
const handleEditorChange = (value) => {
  editorValue.value = value;
};

const handleUpdateEditorChange = (value) => {
  updateEditorValue.value = value;
};

// 关闭对话框的处理
const closeAddDialog = async () => {
  // 清理临时上传的视频
  if (tempUploadedVideo.value) {
    await handleCleanupVideo(tempUploadedVideo.value);
    tempUploadedVideo.value = null;
    newProject.video = null;
  }
  addProject.value = false;
};

const closeUpdateDialog = async () => {
  updateProject.value = false;
  // 清理可能的临时视频
  if (updateProjectData.video && updateProjectData.video !== originalVideoUrl.value) {
    await handleCleanupVideo(updateProjectData.video);
  }
};

// 处理图片上传
const getFile = (imageUrls) => {
  console.log('收到图片上传数据:', imageUrls);
  if (imageUrls) {
    if (!Array.isArray(newProject.images)) {
      newProject.images = [];
    }
    
    if (Array.isArray(imageUrls)) {
      newProject.images = [...newProject.images, ...imageUrls];
    } else if (typeof imageUrls === 'string') {
      newProject.images.push(imageUrls);
    }
    console.log('累积后的项目图片数据(原始数组):', newProject.images);
  }
};

// 处理编辑时的图片上传
const getUpdateFile = (imageUrls) => {
  console.log('收到编辑时的图片上传数据:', imageUrls);
  if (imageUrls) {
    if (!Array.isArray(updateProjectData.images)) {
      updateProjectData.images = [];
    }
    
    if (Array.isArray(imageUrls)) {
      updateProjectData.images = [...updateProjectData.images, ...imageUrls];
    } else if (typeof imageUrls === 'string') {
      updateProjectData.images.push(imageUrls);
    }
    console.log('累积后的编辑项目图片数据(原始数组):', updateProjectData.images);
  }
};

</script>

<template>
  <div>
    <div class="top">
      <div class="add">
        <a-button class="add-button" type="outline" @click="ProjectAddClick">
          <template #icon>
            <icon-plus/>
          </template>
          新增
        </a-button>
      </div>
    </div>

    <div class="form">
      <a-table
          :loading="loading"
          :bordered="false"
          :data="ProjectList"
          :pagination="{
          total: total,
          current: status.current,
          pageSize: status.size,
          showTotal: true,
        }"
          :size="'small'"
          @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID"></a-table-column>
          <a-table-column align="center" data-index="coverImage" title="封面图片">
            <template #cell="{ record }">
              <a-image
                  v-if="record.coverImage"
                  :src="record.coverImage"
                  :preview="true"
                  :width="100"
                  :height="60"
                  fit="cover"
              />
              <div v-else style="width: 100px; height: 60px; display: flex; align-items: center; justify-content: center; background-color: #f5f5f5;">
                无图片
              </div>
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="name" title="项目名称"></a-table-column>
          <a-table-column align="center" data-index="levelName" title="保护级别"></a-table-column>
          <a-table-column align="center" data-index="categoryName" title="项目类别"></a-table-column>
          <a-table-column align="center" data-index="viewCount" title="浏览量"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="创建时间">
            <template #cell="{ record }">
              {{ record.createTimeFormatted || formatDate(record.createTime, 'yyyy-MM-dd') }}
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="optional" title="操作">
            <template #cell="{ record }">
              <div class="option">
                <a-button class="edit-button" type="text" @click="viewProjectClick(record)">
                  <template #icon>
                    <icon-eye/>
                  </template>
                  查看
                </a-button>

                <a-button class="edit-button" type="text" @click="updateProjectClick(record)">
                  <template #icon>
                    <icon-edit/>
                  </template>
                  修改
                </a-button>

                <a-popconfirm content="此操作不可逆，你确定要删除吗？" position="tr" type="warning" @ok="delOk(record)">
                  <a-button class="delete-button" status="danger" type="text" @click="delClick">
                    <template #icon>
                      <icon-delete/>
                    </template>
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 查看详情的模态框 -->
    <a-modal :visible="viewProject" @ok="viewProject = false" @cancel="() => viewProject = false" :footer="false" width="80%" title="查看非遗项目详情">
      <div class="project-view-container">
        <div class="project-info-header">
          <h2 class="project-title">{{ viewProjectData.name }}</h2>
          <div class="project-meta">
            <span class="meta-item"><strong>保护级别:</strong> {{ viewProjectData.levelName }}</span>
            <span class="meta-item"><strong>项目类别:</strong> {{ viewProjectData.categoryName }}</span>
            <span class="meta-item"><strong>浏览量:</strong> {{ viewProjectData.viewCount }}</span>
            <span class="meta-item"><strong>创建时间:</strong> {{ viewProjectData.createTime }}</span>
          </div>
        </div>
        
        <div class="project-medias">
          <div class="project-cover" v-if="viewProjectData.coverImage">
            <h3>封面图片</h3>
            <a-image
              :src="viewProjectData.coverImage"
              :preview="true"
              :width="300"
              fit="cover"
            />
          </div>
          
          <div v-if="viewProjectData.video" class="project-video">
            <h3>项目视频</h3>
            <video 
              :src="viewProjectData.video" 
              controls 
              width="300"
              height="auto"
              preload="metadata"
            ></video>
          </div>
        </div>

        
        <div class="project-content">
          <h3>项目详情</h3>
          <Viewer 
            v-if="viewProjectData.content" 
            :value="viewProjectData.content" 
            :plugins="[...plugins, {
              viewerEffect({ markdownBody }) {
                nextTick(() => {
                  fixViewerImages();
                });
              }
            }]" 
          />
          <div v-else class="no-content">暂无详细内容</div>
        </div>
        
        <div class="project-actions">
          <a-button @click="viewProject = false">关闭</a-button>
        </div>
      </div>
    </a-modal>

    <!--新增-->
    <a-modal 
      :visible="addProject" 
      @ok="addOk" 
      :footer="false"
      width="90%" 
      :mask-closable="false"
      @cancel="closeAddDialog"
      title="新增非遗项目"
    >
      <div class="project-editor-container">
        <Editor
          :value="editorValue"
          :plugins="plugins"
          :locale="zhHans"
          placeholder="请输入项目详细内容（支持Markdown格式）"
          @change="handleEditorChange"
          :split="true"
          :name="newProject.name"
          :levelId="newProject.levelId"
          :categoryId="newProject.categoryId"
          :levelOptions="levelOptions"
          :categoryOptions="categoryOptions"
          @nameChange="val => newProject.name = val"
          @levelChange="val => newProject.levelId = val"
          @categoryChange="val => newProject.categoryId = val"
          @fileChange="getFile"
          @videoChange="getVideoFile"
          @cleanupVideo="handleCleanupVideo"
          @back="closeAddDialog"
          @save="addOk"
        />
      </div>
    </a-modal>

    <!--修改-->
    <a-modal 
      :visible="updateProject" 
      @ok="updateOk" 
      :footer="false"
      width="90%"
      :mask-closable="false"
      @cancel="closeUpdateDialog"
      title="修改非遗项目"
    >
      <div class="project-editor-container">
        <Editor
          :value="updateEditorValue"
          :plugins="plugins"
          :locale="zhHans"
          placeholder="请输入项目详细内容（支持Markdown格式）"
          @change="handleUpdateEditorChange"
          :split="true"
          :name="updateProjectData.name"
          :levelId="updateProjectData.levelId"
          :categoryId="updateProjectData.categoryId"
          :levelOptions="levelOptions"
          :categoryOptions="categoryOptions"
          :images="Array.isArray(updateProjectData.images) ? updateProjectData.images.join(',') : updateProjectData.images"
          :video="updateProjectData.video"
          @nameChange="val => updateProjectData.name = val"
          @levelChange="val => updateProjectData.levelId = val"
          @categoryChange="val => updateProjectData.categoryId = val"
          @fileChange="getUpdateFile"
          @videoChange="getUpdateVideo"
          @cleanupVideo="handleCleanupVideo"
          @back="closeUpdateDialog"
          @save="updateOk"
        />
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.form {
  margin: 20px;
}

.top {
  display: flex;
  justify-content: end;
}

.add{
  margin-top: 30px;
  margin-right: 20px;
}

.delete-button {
  color: rgb(var(--danger-6));
}

.delete-button:hover {
  background-color: rgb(var(--danger-1));
  color: rgb(var(--danger-7));
}

/* 项目详情查看样式 */
.project-view-container {
  padding: 10px;
  max-width: 1200px;
  margin: 0 auto;
}

.project-info-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
}

.project-title {
  font-size: 24px;
  font-family: "STKaiti", "楷体", serif;
  color: #8C1F28;
  margin-bottom: 10px;
}

.project-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 14px;
  color: #582F0E;
}

.meta-item {
  border-right: 1px solid #E4D9C3;
  padding-right: 15px;
}

.meta-item:last-child {
  border-right: none;
  padding-right: 0;
}

.project-medias {
  display: flex;
  gap: 30px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.project-cover, .project-video {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.project-cover h3, .project-video h3 {
  font-size: 18px;
  color: #8C1F28;
  margin-bottom: 10px;
  font-family: "STKaiti", "楷体", serif;
  align-self: flex-start;
}

.project-content {
  margin: 20px 0;
}

.project-content h3 {
  font-size: 18px;
  color: #8C1F28;
  margin-bottom: 15px;
  font-family: "STKaiti", "楷体", serif;
  padding-bottom: 8px;
}

/* 确保markdown内容正确显示 */
:deep(.markdown-body) {
  font-family: "SimSun", "宋体", serif;
  line-height: 1.8;
  color: #333;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.markdown-body img) {
  max-width: 90%;
  display: block;
  margin: 15px auto;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

:deep(.markdown-body p) {
  margin: 16px 0;
  line-height: 1.8;
  width: 100%;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 20px;
  margin: 16px 0;
  width: 100%;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin: 20px 0 10px;
  padding-bottom: 5px;
  border-bottom: none;
  width: 100%;
}

:deep(.markdown-body h1, .markdown-body h2) {
  border-bottom: none;
}

.no-content {
  color: #A89D84;
  font-style: italic;
  padding: 20px;
  text-align: center;
  background-color: #FFFDF7;
  border-radius: 4px;
  border: 1px dashed #D6C6AF;
}

.project-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 编辑器容器样式 */
.project-editor-container {
  width: 100%;
  height: 80vh;
}
</style>