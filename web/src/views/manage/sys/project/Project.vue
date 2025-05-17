<script setup>
import {onMounted, reactive, ref} from "vue";
import {addProjectAPI, deleteProjectAPI, getIchProjectAPI, updateProjectAPI} from "@/api/manage/IchProject.js";
import {Message} from "@arco-design/web-vue";

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
};

const newProject = reactive({
  name: "",
  levelId: "",
  categoryId: "",
  summary: "",
  file: null,
});
const imgFile = ref([])

const getFile = (file) => {
  imgFile.value = file.target.files[0];
  console.log(imgFile.value)
}

const addOk = async () => {
  if (!imgFile.value) {
    Message.warning('请上传封面图片')
    return
  }

  const formData = new FormData()
  formData.append('file', imgFile.value)
  formData.append('projectInfo', JSON.stringify({
    name: newProject.name,
    levelId: newProject.levelId,
    categoryId: newProject.categoryId,
    summary: newProject.summary
  }))

  try {
    const res = await addProjectAPI(formData);
    if (res.code === 200) {
      Message.success(res.msg);
      await loadData();
    } else {
      Message.error(res.msg);
    }
  } catch(error) {
    Message.error('提交失败：' + error.message);
  }
  addProject.value = false;
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
  viewProjectData.viewCount = record.viewCount;
  viewProjectData.createTime = record.createTime;
  viewProjectData.updateTime = record.updateTime;
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
  file: null
});

// 文件处理函数
const getUpdateFile = (file) => {
  updateProjectData.file = file.target.files[0];
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
  updateProject.value = true;
};

const updateOk = async () => {
  const formData = new FormData();
  if (updateProjectData.file) {
    formData.append('file', updateProjectData.file);
  }

  formData.append('projectInfo', JSON.stringify({
    id: updateProjectData.id,
    name: updateProjectData.name,
    levelId: updateProjectData.levelId,
    categoryId: updateProjectData.categoryId,
    summary: updateProjectData.summary
  }));

  try {
    const res = await updateProjectAPI(formData);
    if (res.code === 200) {
      Message.success(res.msg);
      await loadData();
    } else {
      Message.error(res.msg);
    }
  } catch (error) {
    Message.error('更新失败：' + error.message);
  }
  updateProject.value = false;
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
          v-model:loading="loading"
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
            <template #cell="{record}" class="option">

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
            </template>
          </a-table-column>

        </template>
      </a-table>
    </div>
  </div>

  <!-- 查看详情的模态框 -->
  <a-modal v-model:visible="viewProject" @ok="viewProject = false">
    <template #title>
      查看非遗项目详情
    </template>
    <a-form :model="viewProjectData" :style="{ width: '450px' }">
      <a-form-item field="id" label="ID">
        {{ viewProjectData.id }}
      </a-form-item>
      <a-form-item field="name" label="项目名称">
        {{ viewProjectData.name }}
      </a-form-item>
      <a-form-item field="levelName" label="保护级别">
        {{ viewProjectData.levelName }}
      </a-form-item>
      <a-form-item field="categoryName" label="项目类别">
        {{ viewProjectData.categoryName }}
      </a-form-item>
      <a-form-item field="summary" label="项目简介">
        {{ viewProjectData.summary }}
      </a-form-item>
      <a-form-item field="coverImage" label="封面图片">
        <a-image
            :src="viewProjectData.coverImage"
            :preview="true"
            :width="200"
            fit="cover"
        />
      </a-form-item>
      <a-form-item field="viewCount" label="浏览量">
        {{ viewProjectData.viewCount }}
      </a-form-item>
      <a-form-item field="createTime" label="创建时间">
        {{ viewProjectData.createTime }}
      </a-form-item>
      <a-form-item field="updateTime" label="修改时间">
        {{ viewProjectData.updateTime }}
      </a-form-item>
    </a-form>
  </a-modal>

  <!--新增-->
  <a-modal v-model:visible="addProject" @ok="addOk">
    <template #title>
      新增非遗项目信息
    </template>
    <a-form :model="newProject" :style="{ width: '450px' }">
      <a-form-item field="name" label="项目名称">
        <a-input v-model="newProject.name" placeholder="请输入项目名称"/>
      </a-form-item>
      <a-form-item field="levelName" label="保护级别">
        <a-select
          v-model="newProject.levelId"
          placeholder="请选择保护级别"
          :filter-option="false"
        >
          <a-option v-for="option in levelOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="categoryName" label="项目类别">
        <a-select
          v-model="newProject.categoryId"
          placeholder="请选择项目类别"
          :filter-option="false"
        >
          <a-option v-for="option in categoryOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="summary" label="项目简介">
        <a-input v-model="newProject.summary" placeholder="请输入项目简介"/>
      </a-form-item>
      <a-form-item field="file" label="封面图片">
        <input accept="image/*" type="file" @change="getFile($event)"/>
      </a-form-item>
    </a-form>
  </a-modal>

  <!--修改-->
  <a-modal v-model:visible="updateProject" @ok="updateOk">
    <template #title>
      修改非遗项目信息
    </template>
    <a-form :model="updateProjectData" :style="{ width: '450px' }">
      <a-form-item field="id" label="ID">
        <a-input v-model="updateProjectData.id" disabled/>
      </a-form-item>
      <a-form-item field="name" label="项目名称">
        <a-input v-model="updateProjectData.name" placeholder="请输入项目名称"/>
      </a-form-item>
      <!--修改模态框中的选择器部分-->
      <a-form-item field="levelName" label="保护级别">
        <a-select
          v-model="updateProjectData.levelId"
          placeholder="请选择保护级别"
          :filter-option="false"
        >
          <a-option v-for="option in levelOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="categoryName" label="项目类别">
        <a-select
          v-model="updateProjectData.categoryId"
          placeholder="请选择项目类别"
          :filter-option="false"
        >
          <a-option v-for="option in categoryOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="summary" label="项目简介">
        <a-input v-model="updateProjectData.summary" placeholder="请输入项目简介"/>
      </a-form-item>
      <a-form-item field="coverImage" label="封面图片">
        <a-image
            :src="updateProjectData.coverImage"
            :preview="true"
            :width="200"
            fit="cover"
        />
      </a-form-item>
      <a-form-item field="file" label="更换图片">
        <input accept="image/*" type="file" @change="getUpdateFile($event)"/>
      </a-form-item>
    </a-form>
  </a-modal>



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

  &:hover {
    background-color: rgb(var(--danger-1));
    color: rgb(var(--danger-7));
  }
}
</style>