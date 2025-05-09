<script setup>
import {onMounted, reactive, ref} from "vue";

import {Message} from '@arco-design/web-vue';
import {addPolicyAPI, getPolicyListAPI, updatePolicyAPI} from "@/api/manage/Policy.js";

//分页器状态
const status = reactive({
  current: 1,
  size: 10
})
const total = ref(0);
// 分页处理函数
const handlePageChange = (current) => {
  status.current = current;
  loading.value = true;
  getPolicyListAPI(status).then(res => {
    PolicyList.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  });
};

//表格数据
const PolicyList = ref([])
const loading = ref(true);

onMounted(() => {
  getPolicyListAPI(status).then(res => {
    PolicyList.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  });
});

// 定义新增的表单数据
const newPolicy = ref({
  title: '',
  type: '',
  documentNumber: '',
  publishOrg: '',
  publishDate: '',
  effectiveDate: '',
  content: null,
});
const addPolicy = ref(false);
const PolicyAddClick = async () => {
  addPolicy.value = true;
};

// 文件上传处理函数
const handleFileChange = (fileList, target = 'new') => {
  const fileInfo = fileList[0];
  if (!fileInfo.file || !fileInfo.file.name.toLowerCase().endsWith('.pdf')) {
    Message.error('只支持PDF文件');
    fileInfo.status = 'error';
    return;
  }
  // 将文件转换为 base64
  const reader = new FileReader();
  reader.onload = (e) => {
    if (target === 'new') {
      newPolicy.value.content = e.target.result.split(',')[1];
    } else {
      updatePolicyData.content = e.target.result.split(',')[1];
    }
    fileInfo.status = 'done';
  };
  reader.onerror = (error) => {
    fileInfo.status = 'error';
  };
  reader.readAsDataURL(fileInfo.file);
};

const addOk = async () => {
  if (!newPolicy.value.content) {
    Message.error('请上传文件');
    return;
  }
  // 构建请求数据，确保日期字段在没有值时传递 null
  const requestData = {
    title: newPolicy.value.title,
    type: newPolicy.value.type,
    documentNumber: newPolicy.value.documentNumber,
    publishOrg: newPolicy.value.publishOrg,
    publishDate: newPolicy.value.publishDate || null,
    effectiveDate: newPolicy.value.effectiveDate || null,
    base64File: newPolicy.value.content
  };
  addPolicyAPI(requestData).then((res) => {
    if (res.code === 200) {
      Message.success("添加成功")
      loading.value = true;
      //更新列表
      getPolicyListAPI(status).then((res) => {
        PolicyList.value = res.data.records;
        loading.value = false;
      })
    } else {
      Message.error(res.msg)
    }
  })
  addPolicy.value = false;
};

//修改操作
const updatePolicy = ref(false);
const updatePolicyData = reactive({
  id: "",
  title: "",
  type: '',
  documentNumber: '',
  publishOrg: '',
  publishDate: '',
  effectiveDate: '',
  content: null
});

const updatePolicyClick = async (record) => {
  // 将当前行数据赋值填充到表单中
  updatePolicyData.id = String(record.id);
  updatePolicyData.title = record.title;
  updatePolicyData.type = record.type;
  updatePolicyData.documentNumber = String(record.documentNumber);
  updatePolicyData.publishOrg = record.publishOrg;
  updatePolicyData.publishDate = record.publishDate;
  updatePolicyData.effectiveDate = record.effectiveDate;
  updatePolicy.value = true;
};

const updateOk = () => {
  const requestData = {
    ...updatePolicyData,
    publishDate: updatePolicyData.publishDate || null,
    effectiveDate: updatePolicyData.effectiveDate || null,
    content: updatePolicyData.content  // 添加 base64File 字段
  };
  updatePolicyAPI(requestData).then((res) => {
    if (res.code === 200) {
      Message.success("修改成功")
      loading.value = true;
      //更新列表
      getPolicyListAPI(status).then((res) => {
        PolicyList.value = res.data.records;
        loading.value = false;
      })
    } else {
      Message.error(res.msg)
    }
  })
  updatePolicy.value = false;
};

// 删除操作
const del = ref(false);
const delClick = () => {
  del.value = true;
};
const delOk = (record) => {

};
</script>

<template>

  <div>
    <div class="top">

      <div class="add">
        <a-button class="add-button" type="outline" @click="PolicyAddClick">
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
        :data="PolicyList"
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
          <a-table-column align="center" data-index="title" title="标题"></a-table-column>
          <a-table-column align="center" data-index="type" title="类型"></a-table-column>
          <a-table-column align="center" data-index="documentNumber" title="发文字号"></a-table-column>
          <a-table-column align="center" data-index="publishOrg" title="发布机构"></a-table-column>
          <a-table-column align="center" data-index="optional" title="操作">
            <template #cell="{record}" class="option">

              <a-button class="edit-button" type="text" @click="">
                <template #icon>
                  <icon-eye/>
                </template>
                查看
              </a-button>

              <a-button class="edit-button" type="text" @click="updatePolicyClick(record)">
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

  <!--新增-->
  <a-modal v-model:visible="addPolicy" @ok="addOk">
    <template #title>
      新增政策信息
    </template>
    <a-form :model="newPolicy" :style="{ width: '450px' }">
      <a-form-item field="title" label="标题">
        <a-input v-model="newPolicy.title" placeholder="请输入标题"/>
      </a-form-item>
      <a-form-item field="type" label="类型">
        <a-input v-model="newPolicy.type" placeholder="请输入类型"/>
      </a-form-item>
      <a-form-item field="documentNumber" label="发文字号">
        <a-input v-model="newPolicy.documentNumber" placeholder="请输入发文字号"/>
      </a-form-item>
      <a-form-item field="publishOrg" label="发布机构">
        <a-input v-model="newPolicy.publishOrg" placeholder="请输入发布机构"/>
      </a-form-item>
      <a-form-item field="publishDate" label="发布日期">
        <a-date-picker
            v-model="newPolicy.publishDate"
            format="YYYY-MM-DD"
            placeholder="请选择发布日期"
        />
      </a-form-item>
      <a-form-item field="effectiveDate" label="生效日期">
        <a-date-picker
            v-model="newPolicy.effectiveDate"
            format="YYYY-MM-DD"
            placeholder="请选择生效日期"
        />
      </a-form-item>
      <a-form-item field="content" label="政策文件">
        <a-upload
          :limit="1"
          @change="(fileList) => handleFileChange(fileList, 'new')"
          :custom-request="() => {}"
          accept="application/pdf"
          :show-file-list="true"
          list-type="text"
        >
        </a-upload>
      </a-form-item>
    </a-form>
  </a-modal>

  <!--修改-->
  <a-modal v-model:visible="updatePolicy" @ok="updateOk">
    <template #title>
      修改政策信息
    </template>
    <a-form :model="updatePolicyData" :style="{ width: '450px' }">
      <a-form-item field="id" label="ID">
        <a-input v-model="updatePolicyData.id" disabled/>
      </a-form-item>
      <a-form-item field="title" label="标题">
        <a-input v-model="updatePolicyData.title" placeholder="请输入标题"/>
      </a-form-item>
      <a-form-item field="type" label="类型">
        <a-input v-model="updatePolicyData.type" placeholder="请输入类型"/>
      </a-form-item>
      <a-form-item field="documentNumber" label="发文字号">
        <a-input v-model="updatePolicyData.documentNumber" placeholder="请输入发文字号"/>
      </a-form-item>
      <a-form-item field="publishOrg" label="发布机构">
        <a-input v-model="updatePolicyData.publishOrg" placeholder="请输入发布机构"/>
      </a-form-item>
      <a-form-item field="publishDate" label="发布日期">
        <a-date-picker
            v-model="updatePolicyData.publishDate"
            format="YYYY-MM-DD"
            placeholder="请选择发布日期"
        />
      </a-form-item>
      <a-form-item field="effectiveDate" label="生效日期">
        <a-date-picker
            v-model="updatePolicyData.effectiveDate"
            format="YYYY-MM-DD"
            placeholder="请选择生效日期"
        />
      </a-form-item>
      <a-form-item field="content" label="政策文件">
        <a-upload
            :limit="1"
            @change="(fileList) => handleFileChange(fileList, 'update')"
            :custom-request="() => {}"
            accept="application/pdf"
            :show-file-list="true"
            list-type="text"
        >
        </a-upload>
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
