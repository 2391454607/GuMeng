<script setup>
import Header from "@/components/manage/Header.vue";
import Sider from "@/components/manage/Sider.vue";
import Content from "@/components/manage/Content.vue";
import Footer from "@/components/manage/Footer.vue";
</script>

<template>
  <a-layout>
    <Sider></Sider>

    <a-layout>
      <Header></Header>
      <Content></Content>
      <Footer></Footer>
    </a-layout>

  </a-layout>
</template>

<style scoped>

</style>