<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';

const charts = ref([]);

const mockData = [
  { category: '传统技艺', count: 120, avgViews: 1500, avgComments: 45 },
  { category: '传统音乐', count: 85, avgViews: 2300, avgComments: 68 },
  { category: '传统舞蹈', count: 65, avgViews: 1800, avgComments: 32 },
  { category: '传统戏剧', count: 95, avgViews: 2100, avgComments: 56 },
  { category: '曲艺', count: 45, avgViews: 1200, avgComments: 28 },
  { category: '传统体育', count: 30, avgViews: 900, avgComments: 15 },
  { category: '传统美术', count: 110, avgViews: 1900, avgComments: 42 },
  { category: '传统医药', count: 40, avgViews: 1100, avgComments: 25 },
];

const initCharts = async () => {
  await nextTick();
  
  // 内容分布雷达图
  const radarChart = echarts.init(document.getElementById('radarChart'));
  charts.value.push(radarChart);
  
  radarChart.setOption({
    title: { text: '内容分布分析', left: 'center' },
    tooltip: { trigger: 'item' },
    radar: {
      indicator: mockData.map(item => ({
        name: item.category,
        max: Math.max(...mockData.map(d => d.count))
      }))
    },
    series: [{
      type: 'radar',
      data: [{
        value: mockData.map(item => item.count),
        name: '内容数量',
        areaStyle: {
          color: 'rgba(194, 16, 28, 0.3)'
        },
        lineStyle: {
          color: '#C2101C'
        }
      }]
    }]
  });

  // 互动分析柱状图
  const interactionChart = echarts.init(document.getElementById('interactionChart'));
  charts.value.push(interactionChart);
  
  interactionChart.setOption({
    title: { text: '互动分析', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['平均浏览量', '平均评论数'], top: '10%' },
    xAxis: { type: 'category', data: mockData.map(item => item.category) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '平均浏览量',
        type: 'bar',
        data: mockData.map(item => item.avgViews),
        itemStyle: { color: '#C2101C' }
      },
      {
        name: '平均评论数',
        type: 'bar',
        data: mockData.map(item => item.avgComments),
        itemStyle: { color: '#8b1f1f' }
      }
    ]
  });

  // 内容热度词云图
  const wordCloudChart = echarts.init(document.getElementById('wordCloudChart'));
  charts.value.push(wordCloudChart);
  
  const wordData = [
    { name: '传承', value: 100 },
    { name: '文化', value: 90 },
    { name: '传统', value: 85 },
    { name: '技艺', value: 80 },
    { name: '非遗', value: 75 },
    { name: '保护', value: 70 },
    { name: '发展', value: 65 },
    { name: '创新', value: 60 },
    { name: '艺术', value: 55 },
    { name: '历史', value: 50 }
  ];
  
  wordCloudChart.setOption({
    title: { text: '内容热度词云', left: 'center' },
    tooltip: { show: true },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      right: null,
      bottom: null,
      sizeRange: [12, 60],
      rotationRange: [-90, 90],
      rotationStep: 45,
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return 'rgb(' + [
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160)
          ].join(',') + ')';
        }
      },
      emphasis: {
        focus: 'self',
        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: wordData
    }]
  });
};

// 监听窗口大小变化
const handleResize = () => {
  charts.value.forEach(chart => {
    chart && chart.resize();
  });
};

onMounted(async () => {
  await initCharts();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  charts.value.forEach(chart => {
    chart && chart.dispose();
  });
});
</script>

<template>
  <div class="content-container">
    <a-row :gutter="[16, 16]">
      <a-col :span="24">
        <a-alert type="warning" class="dev-alert">
          <template #icon>
            <icon-exclamation-circle-fill />
          </template>
          <template #title>
            部分功能开发中
          </template>
          <template #content>
            更多数据分析功能正在开发中，敬请期待...
          </template>
        </a-alert>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="radarChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="interactionChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="wordCloudChart" class="chart"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.content-container {
  padding: 20px;
}

.chart {
  height: 400px;
  width: 100%;
}

:deep(.arco-card) {
  height: 100%;
}

.dev-alert {
  margin-bottom: 16px;
}

:deep(.arco-alert-warning .arco-alert-icon) {
  color: #ff7d00;
}
</style> 