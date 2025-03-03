<script setup>
import Header from "@/components/layout/Header.vue";
import Content from "@/components/layout/Content.vue";
import Footer from "@/components/layout/Footer.vue";

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