<script setup>
import Header from "@/views/web/layout/Header.vue";
import Footer from "@/views/web/layout/Footer.vue";

</script>

<template>
  <a-layout>

    <a-layout class="layout">
      <Header></Header>

      <div class="content">
        <RouterView></RouterView>
      </div>

    </a-layout>

  </a-layout>

</template>

<style scoped>

.content {
  z-index: 0;
}


</style>