<script setup>
import Menu from "@/views/manage/layout/Menu.vue";
import Header from "@/views/manage/layout/Header.vue";
import Footer from "@/views/manage/layout/Footer.vue";

</script>

<template>

  <a-layout class="layout">
    <Menu></Menu>

    <a-layout>
      <Header></Header>

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
}

.content{
  background: #fff;
  margin: 24px 16px 0;
}

</style>