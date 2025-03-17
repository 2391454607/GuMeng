<script setup>
import { ref } from 'vue';
import Header from "@/views/home/layout/Header.vue";
import Content from "@/views/home/layout/Content.vue";
import Footer from "@/views/home/layout/Footer.vue";

</script>

<template>
  <a-layout  class="layout">
    <Header></Header>

    <Content></Content>

    <Footer></Footer>
  </a-layout>


</template>

<style scoped>

</style>