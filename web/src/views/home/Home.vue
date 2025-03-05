<script setup>
import Header from "@/components/home/Header.vue";
import Content from "@/components/home/Content.vue";
import Footer from "@/components/home/Footer.vue";

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