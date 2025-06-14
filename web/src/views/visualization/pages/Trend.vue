<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';

const mockData = [
  { title: '项目1', gender: '男', level: '国家级', ethnic: '汉族', batch: '2020', region: '北京', introduce: '介绍1' },
  { title: '项目2', gender: '男', level: '省级', ethnic: '满族', batch: '2020', region: '上海', introduce: '介绍2' },
  { title: '项目3', gender: '男', level: '市级', ethnic: '汉族', batch: '2021', region: '广州', introduce: '介绍3' },
  { title: '项目4', gender: '女', level: '国家级', ethnic: '壮族', batch: '2021', region: '南京', introduce: '介绍4' },
  { title: '项目5', gender: '男', level: '省级', ethnic: '汉族', batch: '2022', region: '武汉', introduce: '介绍5' },
  { title: '项目6', gender: '男', level: '市级', ethnic: '回族', batch: '2022', region: '成都', introduce: '介绍6' },
  { title: '项目7', gender: '男', level: '国家级', ethnic: '汉族', batch: '2023', region: '重庆', introduce: '介绍7' },
  { title: '项目8', gender: '女', level: '省级', ethnic: '苗族', batch: '2023', region: '西安', introduce: '介绍8' },
];

const charts = ref([]);

const initCharts = async () => {
  await nextTick();
  
  // 批次时间线图
  const timelineChart = echarts.init(document.getElementById('timelineChart'));
  charts.value.push(timelineChart);
  const batchData = mockData.reduce((acc, cur) => {
    acc[cur.batch] = (acc[cur.batch] || 0) + 1;
    return acc;
  }, {});

  timelineChart.setOption({
    title: { text: '批次时间线趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: Object.keys(batchData).sort(),
      name: '批次'
    },
    yAxis: {
      type: 'value',
      name: '传承人数量'
    },
    series: [{
      data: Object.keys(batchData).sort().map(key => batchData[key]),
      type: 'line',
      smooth: true,
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      },
      markLine: {
        data: [{ type: 'average', name: '平均值' }]
      }
    }]
  });

  // 批次与级别堆叠柱形图
  const stackChart = echarts.init(document.getElementById('stackChart'));
  charts.value.push(stackChart);
  const batchLevelData = mockData.reduce((acc, cur) => {
    if (!acc[cur.batch]) acc[cur.batch] = {};
    acc[cur.batch][cur.level] = (acc[cur.batch][cur.level] || 0) + 1;
    return acc;
  }, {});

  const levels = [...new Set(mockData.map(item => item.level))];
  const batches = Object.keys(batchLevelData).sort();

  stackChart.setOption({
    title: { text: '批次级别分布', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: '10%' },
    xAxis: { type: 'category', data: batches },
    yAxis: { type: 'value' },
    series: levels.map(level => ({
      name: level,
      type: 'bar',
      stack: 'total',
      label: { show: true },
      emphasis: { focus: 'series' },
      data: batches.map(batch => batchLevelData[batch][level] || 0)
    }))
  });

  // 地区批次热力图
  const heatmapChart = echarts.init(document.getElementById('heatmapChart'));
  charts.value.push(heatmapChart);
  const regions = [...new Set(mockData.map(item => item.region))];
  const heatmapData = mockData.reduce((acc, cur) => {
    const regionIndex = regions.indexOf(cur.region);
    const batchIndex = batches.indexOf(cur.batch);
    const key = `${regionIndex}-${batchIndex}`;
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  heatmapChart.setOption({
    title: { text: '地区批次分布热力图', left: 'center' },
    tooltip: { position: 'top' },
    grid: { height: '50%', top: '10%' },
    xAxis: { type: 'category', data: batches, splitArea: { show: true } },
    yAxis: { type: 'category', data: regions, splitArea: { show: true } },
    visualMap: {
      min: 0,
      max: Math.max(...Object.values(heatmapData)),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [{
      name: '传承人数量',
      type: 'heatmap',
      data: Object.entries(heatmapData).map(([key, value]) => {
        const [i, j] = key.split('-').map(Number);
        return [j, i, value];
      }),
      label: { show: true },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
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
  <div class="trend-container">
    <a-row :gutter="[16, 16]">
      <a-col :span="24">
        <a-card>
          <div id="timelineChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="stackChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="heatmapChart" class="chart"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.trend-container {
  padding: 20px;
}

.chart {
  height: 400px;
  width: 100%;
}

:deep(.arco-card) {
  height: 100%;
}
</style> 