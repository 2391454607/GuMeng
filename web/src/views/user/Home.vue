<script setup>
import Header from "@/components/user/Header.vue";
import Content from "@/components/user/Content.vue";
import Footer from "@/components/user/Footer.vue";

</script>

<template>
  <a-space :style="{ width: '100%' }" direction="vertical">
    <Header></Header>
    <Content></Content>
    <Footer></Footer>
  </a-space>
</template>

<style scoped>


</style>