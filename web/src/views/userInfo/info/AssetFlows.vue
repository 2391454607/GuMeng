<script setup>
import { onMounted, ref } from "vue";
import { getAssetLogoAPI } from "@/api/user/UserInfo.js";

const pointLogs = ref([]);
const balanceLogs = ref([]);

onMounted(() => {
  getAssetLogoAPI().then((res) => {
    if (res.code === 200) {
      pointLogs.value = res.data.pointLogs;
      balanceLogs.value = res.data.balanceLogs;
    }
  });
});



</script>

<template>

</template>

<style scoped>

</style>