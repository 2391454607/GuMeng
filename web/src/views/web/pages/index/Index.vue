<script setup>
import { IconMenuFold, IconMenuUnfold } from "@arco-design/web-vue/es/icon";
</script>

<template>
  <a-layout-content class="content">
    <a-carousel arrows autoplay class="carousel">
      <template #prevArrow>
        <div class="custom-slick-arrow" style="left: 10px; z-index: 1">
          <icon-menu-fold />
        </div>
      </template>
      <template #nextArrow>
        <div class="custom-slick-arrow" style="right: 10px">
          <icon-menu-unfold />
        </div>
      </template>

      <div><img src="" alt="Slide 1" /></div>
      <div><img src="" alt="Slide 1" /></div>
      <div><img src="" alt="Slide 1" /></div>
    </a-carousel>


    <div class="c123">
      123
    </div>
    <div class="c123">
      123
    </div>
    <div class="c123">
      123
    </div>
    <div class="c123">
      123
    </div>
  </a-layout-content>
</template>

<style scoped>
.c123 {
  height: 300px;
  background-color: #40a9ff;
}

.content {
  text-align: center;
}

/* 轮播图样式 */
.carousel {
  width: 100%;
  height:calc(100vh); /* 继承父容器高度 */
}

/* 轮播图图片样式 */
.carousel img {
  width: 100%;
  height: calc(100vh); /* 继承父容器高度 */
  object-fit: cover; /* 保持图片比例并铺满容器 */
}

/* 轮播图箭头样式 */
:deep(.slick-arrow.custom-slick-arrow) {
  width: 40px;
  height: 40px;
  font-size: 40px;
  color: rgba(200, 200, 200, 0.3);
  transition: ease all 0.3s;
  opacity: 0.7;
  z-index: 1;
}

:deep(.slick-arrow.custom-slick-arrow:hover) {
  opacity: 1;
  color: rgba(255, 255, 255, 0.65);
}

:deep(.slick-arrow.custom-slick-arrow:before) {
  display: none;
}

/* 轮播图内容样式 */
:deep(.slick-slide) {
  text-align: center;
  height: 100%; /* 继承父容器高度 */
  line-height: calc(100vh - 100px); /* 垂直居中，减去头部高度 */
  background: #364d79;
  overflow: hidden;
}

:deep(.slick-slide h3) {
  color: #fff;
}
</style>