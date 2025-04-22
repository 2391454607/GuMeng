<script setup>
import {onMounted, ref} from "vue";
import {getPolicyList} from "@/api/web/Web.js";
import {IconEye, IconDownload, IconLoading } from '@arco-design/web-vue/es/icon';
import Footer from "@/views/web/layout/Footer.vue";

const PolicyList = ref({
  id:"",
  title:"",
  type: "",
  documentNumber: "",
  publishOrg: "",
  publishDate: "",
  effectiveDate: "",
  content: "",
  attachmentUrl: "",
})

const loading = ref(true);

onMounted(()=>{
  getPolicyList().then((res)=>{
    PolicyList.value = res.data;
    loading.value = false;
  })
})
</script>

<template>
  <div class="policy-container">
    <div class="policy-header">
      <div class="header-title">
        <h1>非遗政策文件</h1>
        <p class="subtitle">传承文化精髓，守护非遗瑰宝</p>
      </div>
    </div>

    <div class="policy-list">
      <a-spin :loading="loading" size="large">
        <template #icon><icon-loading /></template>
        <a-list :data="PolicyList" :bordered="false">
          <template #item="{ item }">
            <a-list-item class="policy-item">
              <div class="policy-content">
                <div class="left-content">
                  <div class="policy-type">{{ item.type }}</div>
                  <h3 class="policy-title">{{ item.title }}</h3>
                  <div class="policy-meta">
                    <span>发文字号：{{ item.documentNumber }}</span>
                    <span>发布机构：{{ item.publishOrg }}</span>
                    <span>颁布日期：{{ item.publishDate }}</span>
                  </div>
                </div>
                <div class="policy-actions">
                  <a-button type="primary" shape="round">
                    <template #icon><icon-eye /></template>
                    查看详情
                  </a-button>
                  <a-button v-if="item.attachmentUrl" shape="round">
                    <template #icon><icon-download /></template>
                    下载附件
                  </a-button>
                </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-spin>
    </div>
  </div>

  <div>
    <Footer class="footer"></Footer>
  </div>

</template>

<style scoped>
.policy-container {
  margin-left: 100px;
  margin-right: 100px;
  padding: 24px;
  background: #f7f8fa;
}

.policy-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(194, 16, 28, 0.1);
}

.policy-item {
  margin-top: 10px;
  width: calc(100vw - 300px);
  height: 120px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e6eb;
  transition: all 0.3s;
  display: flex !important;  /* 强制使用flex布局 */
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.policy-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-content {
  flex: 1;
}

.policy-actions {
  display: flex;
  gap: 12px;
  margin-left: 24px;
}

.policy-item:hover {
  border-color: #C2101C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.policy-type {
  display: inline-block;
  padding: 4px 12px;
  background: #C2101C;
  color: white;
  border-radius: 16px;
  font-size: 14px;
}

.policy-title {
  font-size: 18px;
  color: #1d2129;
  margin: 12px 0;
  font-weight: 500;
}

.policy-meta {
  display: flex;
  gap: 20px;
  color: #86909c;
  font-size: 14px;
}

.policy-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.arco-spin) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.arco-spin-icon) {
  color: #C2101C;
  font-size: 24px;
}

.footer{
  position: absolute;
  bottom: 0;
}
</style>