<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';

const mockData = [
  { title: '项目1', gender: '男', level: '国家级', ethnic: '汉族', batch: '2023', region: '北京', introduce: '介绍1' },
  { title: '项目2', gender: '女', level: '省级', ethnic: '满族', batch: '2023', region: '上海', introduce: '介绍2' },
  { title: '项目3', gender: '男', level: '市级', ethnic: '汉族', batch: '2022', region: '广州', introduce: '介绍3' },
];

const charts = ref([]);

const initCharts = async () => {
  await nextTick();
  
  // 性别比例饼图
  const genderChart = echarts.init(document.getElementById('genderChart'));
  charts.value.push(genderChart);
  const genderData = mockData.reduce((acc, cur) => {
    acc[cur.gender] = (acc[cur.gender] || 0) + 1;
    return acc;
  }, {});
  
  genderChart.setOption({
    title: { text: '性别比例分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: Object.entries(genderData).map(([name, value]) => ({ name, value })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  });

  // 级别分布柱形图
  const levelChart = echarts.init(document.getElementById('levelChart'));
  charts.value.push(levelChart);
  const levelData = mockData.reduce((acc, cur) => {
    acc[cur.level] = (acc[cur.level] || 0) + 1;
    return acc;
  }, {});

  levelChart.setOption({
    title: { text: '级别分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(levelData) },
    yAxis: { type: 'value' },
    series: [{
      data: Object.values(levelData),
      type: 'bar',
      showBackground: true,
      backgroundStyle: {
        color: 'rgba(180, 180, 180, 0.2)'
      }
    }]
  });

  // 民族分布条形图
  const ethnicChart = echarts.init(document.getElementById('ethnicChart'));
  charts.value.push(ethnicChart);
  const ethnicData = mockData.reduce((acc, cur) => {
    acc[cur.ethnic] = (acc[cur.ethnic] || 0) + 1;
    return acc;
  }, {});

  ethnicChart.setOption({
    title: { text: '民族分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: Object.keys(ethnicData) },
    series: [{
      data: Object.values(ethnicData),
      type: 'bar',
      showBackground: true,
      backgroundStyle: {
        color: 'rgba(180, 180, 180, 0.2)'
      }
    }]
  });

  // 地区分布图（使用散点图）
  const regionChart = echarts.init(document.getElementById('regionChart'));
  charts.value.push(regionChart);
  const regionData = mockData.reduce((acc, cur) => {
    acc[cur.region] = (acc[cur.region] || 0) + 1;
    return acc;
  }, {});

  const regions = [
    { name: '北京', value: regionData['北京'] || 0, coord: [116.405285, 39.904989] },
    { name: '上海', value: regionData['上海'] || 0, coord: [121.472644, 31.231706] },
    { name: '广州', value: regionData['广州'] || 0, coord: [113.280637, 23.125178] },
    { name: '武汉', value: regionData['武汉'] || 0, coord: [114.298572, 30.584355] },
    { name: '成都', value: regionData['成都'] || 0, coord: [104.065735, 30.659462] },
    { name: '西安', value: regionData['西安'] || 0, coord: [108.948024, 34.263161] }
  ];

  regionChart.setOption({
    title: { text: '地区分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'scatter',
      coordinateSystem: 'cartesian2d',
      data: regions.map(item => ({
        name: item.name,
        value: [item.coord[0], item.coord[1], item.value],
        symbolSize: item.value ? Math.sqrt(item.value) * 10 : 0
      })),
      label: {
        show: true,
        formatter: '{b}',
        position: 'right'
      },
      itemStyle: {
        color: '#c03'
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
  <div class="overview-container">
    <a-row :gutter="[16, 16]">
      <a-col :span="12">
        <a-card>
          <div id="genderChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card>
          <div id="levelChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card>
          <div id="ethnicChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card>
          <div id="regionChart" class="chart"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.overview-container {
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