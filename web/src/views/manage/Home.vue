<script setup>
import Menu from "@/views/manage/layout/Menu.vue";
import Header from "@/views/manage/layout/Header.vue";
import Footer from "@/views/manage/layout/Footer.vue";
import {ref} from "vue";

const collapsed = ref(false);
const onCollapse = () => {
  collapsed.value = !collapsed.value;
}
</script>

<template>
  <a-layout class="layout">
    <Menu v-model:hidden="collapsed"></Menu>

    <a-layout :style="{ 
      marginLeft: collapsed ? '48px' : '200px',
      transition: 'all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1)'
    }">
      <Header :collapsed="collapsed" :coll="onCollapse"></Header>
      
      <a-layout-content class="content">
        <RouterView></RouterView>
      </a-layout-content>

      <Footer></Footer>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.content {
  margin: 10px 10px 0px 10px;
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}
</style>