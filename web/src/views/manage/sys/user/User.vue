<script setup>
import {reactive, ref, onMounted} from "vue";
import {getUserList} from "@/api/manage/User.js";


//分页器状态
const status = reactive({
  current: 1,  // 当前页码
  size: 10     // 每页显示数量
})
const total = ref(0);  // 总数据量
//表格数据
const UserList = ref()
const loading = ref(true);

//获取轮播图列表（初始化表格）
onMounted(()=>{
  getUserList(status).then(res=>{
    UserList.value = res.data.records;
    total.value = res.total;
    loading.value = false;
  })
})

//添加轮播图
//定义新增的表单数据
const newUser = reactive({
  id: "",
  imageUrl: "",
});
const addUser = ref(false);
const UserAddClick = async () => {
  addUser.value = true;
};

const addOk = async () => {

  addUser.value = false;
};

//修改操作
const updateUser = ref(false);
// 用于存储正在编辑的轮播图数据
const updateUserData = reactive({
  id: "",
  imageUrl: "",
});
const record = reactive({
  id: "",
  imageUrl: "",
})
const updateUserClick = async (record) => {
  // 将当前行数据赋值填充到表单中
  updateUserData.id = record.id;
  updateUserData.imageUrl = record.imageUrl;
  updateUser.value = true;
};
const updateOk = () => {

  updateUser.value = false;
};

// 删除操作
const del = ref(false);
const delClick = () => {
  del.value = true;
};
const delOk = (record) => {

  del.value = false;
};
// 时间格式化函数
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};
</script>

<template>

  <div>
    <div class="top">

      <div class="add">
        <a-button class="add-button" type="outline" @click="UserAddClick">
          <template #icon>
            <icon-plus/>
          </template>
          新增
        </a-button>
      </div>

    </div>

    <div class="form">
      <a-table v-model:loading="loading" :bordered="false" :data="UserList" :pagination="{showJumper: true}" :size="'small'">
        <template #columns>
          <a-table-column align="center" data-index="id" title="ID"></a-table-column>
          <a-table-column align="center" data-index="userPic" title="头像">
            <template #cell="{ record }">
              <a-image
                  :src="record.userPic"
                  :preview="true"
                  :width="100"
                  :height="60"
                  fit="cover"
              />
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="nickname" title="昵称"></a-table-column>
          <a-table-column align="center" data-index="username" title="用户名"></a-table-column>
          <a-table-column align="center" data-index="email" title="邮箱"></a-table-column>
          <a-table-column align="center" data-index="address" title="地址"></a-table-column>
          <a-table-column align="center" data-index="createTime" title="创建时间">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column align="center" data-index="updateTime" title="更新时间">
            <template #cell="{ record }">
              {{ formatTime(record.updateTime) }}
            </template>
          </a-table-column>

          <a-table-column align="center" data-index="optional" title="操作">
            <template #cell="{record}" class="option">

              <a-button class="edit-button" type="text" @click="updateUserClick(record)">
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
  <a-modal v-model:visible="addUser" @ok="addOk">
    <template #title>
      新增轮播图
    </template>
    <a-form :model="newUser" :style="{ width: '450px' }">
      <a-form-item field="imageUrl" label="轮播图路径">
        <a-input v-model="newUser.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="newUser.imageUrl"
            :src="newUser.imageUrl"
            :width="200"
            :height="120"
            fit="cover"
        />
        <span v-else>无预览图</span>
      </a-form-item>
    </a-form>
  </a-modal>

  <!--修改-->
  <a-modal v-model:visible="updateUser" @ok="updateOk">
    <template #title>
      修改轮播图
    </template>
    <a-form :model="updateUserData" :style="{ width: '450px' }">
      <a-form-item field="id" label="id">
        <a-input v-model="updateUserData.id" disabled/>
      </a-form-item>
      <a-form-item field="imageUrl" label="轮播图路径">
        <a-input v-model="updateUserData.imageUrl"/>
      </a-form-item>
      <a-form-item label="图片预览">
        <a-image
            v-if="updateUserData.imageUrl"
            :src="updateUserData.imageUrl"
            :width="200"
            :height="120"
            fit="cover"
        />
        <span v-else>无预览图</span>
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