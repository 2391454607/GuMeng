<script setup>
import {onMounted, ref} from "vue";
import {userListAPI} from "@/api/user.js";


const userData = ref([]); // 用户列表数据

onMounted(() => {
  userListAPI().then((res) => {
    userData.value = res;
    console.log(res);
    console.log(userData.value);
  })
});


</script>

<template>
  <a-table :dataSource="userData" bordered>

    <a-table-column align="center" data-index="id" title="ID"></a-table-column>
    <a-table-column align="center" data-index="nickname" title="昵称"></a-table-column>
    <a-table-column align="center" data-index="username" title="用户名"></a-table-column>
    <a-table-column align="center" data-index="email" title="邮箱"></a-table-column>
    <a-table-column align="center" data-index="address" title="地址"></a-table-column>

  </a-table>
</template>
<style scoped>

.editable-row-operations a {
  margin-right: 8px;
}

</style>