<script setup>
import Header from "@/views/web/layout/Header.vue";
import Footer from "@/views/web/layout/Footer.vue";

</script>

<template>
  <a-layout>

    <a-layout class="layout">
      <Header></Header>

      <a-layout-content>
        <RouterView></RouterView>
      </a-layout-content>

    </a-layout>

    <Footer class="footer"></Footer>
  </a-layout>

</template>

<style scoped>
.layout {
  min-height: calc(100vh - 64px); /* 确保布局占满整个视口高度 */
}


</style>