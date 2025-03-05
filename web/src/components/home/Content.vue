<script setup>
import { LeftCircleOutlined, RightCircleOutlined} from "@ant-design/icons-vue";

</script>

<template>
  <a-layout-content class="content">

      <a-carousel arrows autoplay class="carousel">
        <template #prevArrow>
          <div class="custom-slick-arrow" style="left: 10px; z-index: 1">
            <left-circle-outlined />
          </div>
        </template>
        <template #nextArrow>
          <div class="custom-slick-arrow" style="right: 10px">
            <right-circle-outlined />
          </div>
        </template>

        <div><img src=""> 1 </div>
        <div><img src=""> 2 </div>
        <div><img src=""> 3 </div>
        <div><img src=""> 4 </div>
      </a-carousel>

  </a-layout-content>

</template>

<style scoped>
.content {
  width: 100%;
  text-align: center;
  color: #000000;
  background-color: #ffffff;
  min-height: calc(100vh - 128px);
}

/* For demo */
.carousel{
  width: 80%;
  margin: auto;
}

:deep(.slick-slide) {
  text-align: center;
  height: 480px;
  line-height: 160px;
  background: #364d79;
  overflow: hidden;
}

:deep(.slick-arrow.custom-slick-arrow) {
  width: 25px;
  height: 25px;
  font-size: 25px;
  color: #fff;
  background-color: rgba(31, 45, 61, 0.11);
  transition: ease all 0.3s;
  opacity: 0.3;
  z-index: 1;
}
:deep(.slick-arrow.custom-slick-arrow:before) {
  display: none;
}
:deep(.slick-arrow.custom-slick-arrow:hover) {
  color: #fff;
  opacity: 0.5;
}

:deep(.slick-slide h3) {
  color: #fff;
}
</style>