<script setup>
import {onMounted, reactive, ref} from "vue";
import {addProjectAPI, deleteFileAPI, deleteProjectAPI, getIchProjectAPI, updateProjectAPI} from "@/api/manage/IchProject.js";
import {Message} from "@arco-design/web-vue";
import { Editor, Viewer } from './bytemd';
// 导入ByteMD插件
import gfm from '@bytemd/plugin-gfm';
import highlight from '@bytemd/plugin-highlight';
import gemoji from '@bytemd/plugin-gemoji';
import zhHans from 'bytemd/locales/zh_Hans.json';
// 导入ByteMD样式
import 'bytemd/dist/index.css';

// ByteMD插件配置
const plugins = [
  gfm(),
  highlight(),
  gemoji(),
]

//表格数据
const ProjectList = ref([])
// 封装加载数据的方法
const loading = ref();
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getIchProjectAPI(status);
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
  newProject.summary = '';
  newProject.file = null;
  newProject.video = null;
  imgFile.value = null;
};

const newProject = reactive({
  name: "",
  levelId: "",
  categoryId: "",
  summary: "",
  file: null,
  video: null,
});
const imgFile = ref(null);
// 编辑器内容
const editorValue = ref('');

const getFile = (file) => {
  imgFile.value = file;
};

// 处理临时上传的视频
const tempUploadedVideo = ref(null); // 存储临时上传的视频URL

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
  
  if (!imgFile.value) {
    Message.warning('请上传封面图片');
    return;
  }

  const formData = new FormData();
  formData.append('file', imgFile.value);
  formData.append('projectInfo', JSON.stringify({
    name: newProject.name,
    levelId: newProject.levelId,
    categoryId: newProject.categoryId,
    summary: newProject.summary,
    content: editorValue.value,
    video: newProject.video
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

// 添加查看状态和数据
const viewProject = ref(false);
const viewProjectData = reactive({
  id: "",
  name: "",
  levelName: "",
  categoryName: "",
  summary: "",
  coverImage: "",
  video: "",
  content: "",
  viewCount: 0,
  createTime: ""
});

// 添加查看方法
const viewProjectClick = (record) => {
  viewProjectData.id = record.id;
  viewProjectData.name = record.name;
  viewProjectData.levelName = record.levelName;
  viewProjectData.categoryName = record.categoryName;
  viewProjectData.summary = record.summary;
  viewProjectData.coverImage = record.coverImage;
  viewProjectData.video = record.video || '';
  viewProjectData.viewCount = record.viewCount;
  viewProjectData.createTime = record.createTime;
  viewProjectData.updateTime = record.updateTime;
  
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
};


//修改操作
const updateProject = ref(false);
// 用于存储正在编辑数据
const updateProjectData = reactive({
  id: "",
  name: "",
  levelId: "",
  levelName: "",
  categoryId: "",
  categoryName: "",
  summary: "",
  coverImage: "",
  video: "",
  content: "",
  file: null
});

// 更新编辑器内容
const updateEditorValue = ref('');

// 文件处理函数
const getUpdateFile = (file) => {
  updateProjectData.file = file;
};

// 定义临时更新视频URL
const tempUpdateVideo = ref(null);

// 视频处理函数
const getUpdateVideo = (videoUrl) => {
  // 记录临时视频，以便在取消时清理
  tempUpdateVideo.value = videoUrl;
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
  updateProjectData.summary = record.summary;
  updateProjectData.coverImage = record.coverImage;
  updateProjectData.video = record.video || '';
  
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
  if (updateProjectData.file) {
    formData.append('file', updateProjectData.file);
  }

  formData.append('projectInfo', JSON.stringify({
    id: updateProjectData.id,
    name: updateProjectData.name,
    levelId: updateProjectData.levelId,
    categoryId: updateProjectData.categoryId,
    summary: updateProjectData.summary,
    content: updateEditorValue.value,
    video: updateProjectData.video
  }));

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
  // 清理临时上传的视频
  if (tempUpdateVideo.value && tempUpdateVideo.value !== updateProjectData.video) {
    await handleCleanupVideo(tempUpdateVideo.value);
    tempUpdateVideo.value = null;
    updateProjectData.video = null;
  }
  updateProject.value = false;
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
                  :src="record.coverImage"
                  :preview="true"
                  :width="100"
                  :height="60"
                  fit="cover"
              />
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="name" title="项目名称"></a-table-column>
          <a-table-column align="center" data-index="levelName" title="保护级别"></a-table-column>
          <a-table-column align="center" data-index="categoryName" title="项目类别"></a-table-column>
          <a-table-column align="center" data-index="viewCount" title="浏览量"></a-table-column>
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
          <div class="project-cover">
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
        
        <div class="project-summary">
          <h3>项目简介</h3>
          <p>{{ viewProjectData.summary }}</p>
        </div>
        
        <div class="project-content">
          <h3>项目详情</h3>
          <Viewer v-if="viewProjectData.content" :value="viewProjectData.content" :plugins="plugins" />
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
          :summary="newProject.summary"
          :levelOptions="levelOptions"
          :categoryOptions="categoryOptions"
          @nameChange="val => newProject.name = val"
          @levelChange="val => newProject.levelId = val"
          @categoryChange="val => newProject.categoryId = val"
          @summaryChange="val => newProject.summary = val"
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
          :summary="updateProjectData.summary"
          :levelOptions="levelOptions"
          :categoryOptions="categoryOptions"
          :coverImage="updateProjectData.coverImage"
          :video="updateProjectData.video"
          @nameChange="val => updateProjectData.name = val"
          @levelChange="val => updateProjectData.levelId = val"
          @categoryChange="val => updateProjectData.categoryId = val"
          @summaryChange="val => updateProjectData.summary = val"
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
  border-bottom: 1px solid #E4D9C3;
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

.project-summary {
  margin: 20px 0;
  padding: 15px;
  background-color: #FFF7E9;
  border-radius: 8px;
  border-left: 3px solid #8C1F28;
}

.project-summary h3 {
  font-size: 18px;
  color: #8C1F28;
  margin-bottom: 10px;
  font-family: "STKaiti", "楷体", serif;
}

.project-summary p {
  font-size: 16px;
  line-height: 1.6;
  color: #582F0E;
}

.project-content {
  margin: 20px 0;
}

.project-content h3 {
  font-size: 18px;
  color: #8C1F28;
  margin-bottom: 15px;
  font-family: "STKaiti", "楷体", serif;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 8px;
}

/* 确保markdown内容正确显示 */
:deep(.markdown-body) {
  font-family: "SimSun", "宋体", serif;
  line-height: 1.8;
  color: #333;
  padding: 10px 0;
}

:deep(.markdown-body img) {
  max-width: 100%;
  display: block;
  margin: 15px auto;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin: 20px 0 10px;
  padding-bottom: 5px;
}

:deep(.markdown-body h1, .markdown-body h2) {
  border-bottom: 1px solid #E4D9C3;
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