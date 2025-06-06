<script setup>
import Footer from "@/views/web/layout/Footer.vue";
import {onMounted, reactive, ref} from 'vue';
import { getGoodsListAPI } from "@/api/web/Shop.js"
import { IconSearch, IconLoading, IconPlayArrowFill } from '@arco-design/web-vue/es/icon';
import { useRouter } from 'vue-router';
import {Message} from "@arco-design/web-vue";

// 初始化路由器
const router = useRouter();

//分页器状态
const status = reactive({
  current: 1,
  size: 10
})
//总数
const total = ref(0);
// 分页处理函数
const handlePageChange = (page) => {
  status.current = page;
  getGoodsListAPI(status).then((res)=>{
    Goods.value = res.data.records;
    total.value = res.data.total;
  })
}

const loading = ref(true);
const Goods = ref({})

onMounted(()=>{
  getGoodsListAPI(status).then(res=>{
    console.log(res)
    Goods.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
    console.log(Goods.value)
  })
})

// 搜索关键词状态
const searchKeyword = ref('');

// 搜索处理方法
const handleSearch = () => {
  loading.value = true;
  // 将搜索条件添加到查询参数中
  const params = {
    ...status,
    keyword: searchKeyword.value,
  };

  getGoodsListAPI(params).then((res) => {
    Goods.value = res.data.records;
    total.value = res.data.total;
    loading.value = false;
  });
}

// 添加查看详情方法
const viewDetail = (id) => {
  Message.warning("点击了商品，进入详情页")
}

</script>


<template>
  <div>
    <div class="goods-title">
      <div class="title-content">
        <h1>文创商城</h1>
        <p>匠心传承千年瑰宝 · 创意焕发文化新生</p>
      </div>
    </div>

    <div class="information-container">
      <div class="search-section">
        <div class="search-wrapper">
          <IconSearch class="search-icon"/>
          <input
              v-model="searchKeyword"
              type="text"
              placeholder="请输入关键词搜索"
              class="search-input"
              @keyup.enter="handleSearch"
          >
        </div>
      </div>


      <a-spin :loading="loading" :size="32" tip="加载中">
        <template #icon><icon-loading /></template>
        <div class="goods-grid">
          <a-card
              v-for="goods in Goods"
              :key="goods.id"
              class="goods-card"
              :bordered="false"
              hoverable
          >
            <div class="goods-image">
              <img :src="goods.imageUrl.split(',')[0].replace(/[\s`]/g, '')" :alt="goods.name">
              <div class="goods-overlay" @click="viewDetail(goods.id)">
                <div class="overlay-content">
                  <icon-eye class="overlay-icon" />
                  <span>查看详情</span>
                </div>
              </div>
              <div class="video-badge" v-if="goods.video">
                <icon-play-arrow-fill class="video-icon" />
              </div>
            </div>
            <div class="goods-content">
              <h3>{{ goods.name }}</h3>

              <div class="price-section">
                <template v-if="goods.mixedPriceMoney && goods.mixedPricePoints && goods.priceMoney">
                  <div class="price-section">
                    <div class="price-container">
                      <div class="mixed-price">
                        <span class="price-value">¥ {{ goods.mixedPriceMoney }}元 + {{ goods.mixedPricePoints }}积分</span>
                      </div>
                    </div>
                    <div class="original-price" style="display: block; margin-top: 5px;">
                      <span style="text-decoration: line-through; color: #999;">原价：¥{{ goods.priceMoney }}</span>
                    </div>
                  </div>
                </template>
              </div>

              <div class="goods-card-bottom">
                <div>
                  <a-space class="stock">
                    <icon-common />
                    库存剩余:
                    {{ goods.stock }}
                  </a-space>
                </div>
                <div class="shoppingCart">
                  <a-button>
                    加入购物车
                  </a-button>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </a-spin>

      <!-- 分页器 -->
      <div class="pagination-container">
        <a-pagination
            :total="total"
            :current="status.current"
            :page-size="status.size"
            @change="handlePageChange"
        />
      </div>
    </div>

    <Footer class="footer"/>

  </div>
</template>

<style scoped>
.goods-title {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('/image/shopTitle.webp');
  background-size: 100% 100%;  /* 完全覆盖 */
  background-position: center;
  background-repeat: no-repeat;
}

.title-content {
  text-align: center;
  padding: 10px;  /* 从20px减小到10px */
  z-index: 2;
  width: 100%;
}

.title-content h1 {
  font-size: clamp(2.5em, 5vw, 3.2em);
  color: #8C1F28;
  font-weight: 800;
  text-shadow: 2px 2px 4px rgba(140, 31, 40, 0.2);
  font-family: "华文行楷", "STXingkai", "楷体", serif;
  letter-spacing: 0.1em;
  position: relative;
  display: inline-block;
  animation: titleFadeIn 1.2s ease-out forwards;
  margin-bottom: 5px;  /* 添加较小的底部间距 */
}

.title-content p {
  font-size: clamp(1.2em, 2.2vw, 1.5em);
  color: #594433;
  font-family: "华文仿宋", "STFangsong", "仿宋", serif;
  letter-spacing: 3px;
  text-shadow: 1px 1px 2px rgba(89, 68, 51, 0.1);
  animation: subtitleFadeIn 1.5s ease-out forwards;
  position: relative;
  margin-top: 0;  /* 移除顶部间距 */
}

@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes subtitleFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.title-content p {
  font-size: clamp(1.5em, 2vw, 1.2em);  /* 响应式字体大小 */
  color: #594433;
  font-family: "STFangsong", "仿宋", serif;
  letter-spacing: 2px;
}


.information-container {
  padding: 20px;
  max-width: 100vw;
  min-height: calc(100vh - 169px);
  margin: 0 auto;
  background-color: #FFF7E9;
}

.search-section {
  display: flex;
  justify-content: center;
}

.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #d32f2f;
  font-size: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 20px 12px 45px;
  border: 2px solid #d32f2f;
  border-radius: 24px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #fff;
}

.search-input:focus {
  outline: none;
  border-color: #b71c1c;
  box-shadow: 0 0 8px rgba(211, 47, 47, 0.3);
}

.search-input::placeholder {
  color: #999;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin: 20px auto;
  max-width: 1350px;
  padding: 0 40px;
}

.goods-card {
  border: 1px solid #ffcdd2;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s;
  background: #fff;
  height: 100%;  /* 确保所有卡片高度一致 */
  display: flex;
  flex-direction: column;
}

.goods-content {
  padding: 20px;
  flex: 1;  /* 让内容区域占据剩余空间 */
  display: flex;
  flex-direction: column;
}

.goods-content p {
  flex: 1;  /* 让描述文字占据剩余空间 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
}

.goods-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.15);
  border-color: #d32f2f;
}

.goods-image {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.goods-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(33, 33, 33, 0.75);  /* 灰色遮罩 */
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.goods-card:hover .goods-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  transform: translateY(20px);
  transition: all 0.3s ease;
}

.goods-card:hover .overlay-content {
  transform: translateY(0);
}

.overlay-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-content {
  padding: 15px;
}

.goods-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.goods-content p {
  color: #666;
  margin: 0 0 10px 0;
  font-size: 14px;
}

/*加入购物车样式*/
.shoppingCart :deep(.arco-btn) {
  background: linear-gradient(45deg, #C2101C, #FF4D4F);
  border: none;
  color: white;
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.shoppingCart :deep(.arco-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 16, 28, 0.3);
}

.goods-card-bottom{
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stock {
  color: #666;
  font-size: 14px;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  bottom: 0;
  color: #b71c1c;
}

:deep(.arco-pagination) {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

:deep(.arco-pagination-item:hover),
:deep(.arco-pagination-item-active) {
  color: #fff !important;
  background-color: #C2101C !important;
  border-color: #C2101C !important;
}

:deep(.arco-pagination-item-previous:hover),
:deep(.arco-pagination-item-next:hover) {
  color: #C2101C !important;
  background-color: #fff !important;
  border-color: #C2101C !important;
}

:deep(.arco-pagination-jumper-input:focus) {
  border-color: #C2101C !important;
  box-shadow: 0 0 0 2px rgba(194, 16, 28, 0.2) !important;
}

:deep(.arco-pagination-jumper-input:hover) {
  border-color: #C2101C !important;
}

:deep(.arco-pagination-total) {
  color: #86909c;
}

/* 加载动画相关样式 */
:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.arco-spin-icon) {
  color: #C2101C;
}

/* 加载提示文字样式 */
:deep(.arco-spin-tip) {
  color: #C2101C;
  margin-top: 8px;
}


.price-section {
  margin: 5px 0;
  border-radius: 10px;
}

.price-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mixed-price {
  flex: 1;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(45deg, #C2101C, #FF4D4F);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 2px 2px 4px rgba(194, 16, 28, 0.1);
}

.original-price {
  color: #999;
  font-size: 16px;
  text-decoration: line-through;
}
</style>
