<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue';
import * as echarts from 'echarts';

// --- mockData 合并 ---
const trendData = [
  { title: '项目1', gender: '男', level: '国家级', ethnic: '汉族', batch: '2020', region: '北京', introduce: '介绍1' },
  { title: '项目2', gender: '女', level: '省级', ethnic: '满族', batch: '2020', region: '上海', introduce: '介绍2' },
  { title: '项目3', gender: '男', level: '市级', ethnic: '汉族', batch: '2021', region: '广州', introduce: '介绍3' },
  { title: '项目4', gender: '女', level: '国家级', ethnic: '壮族', batch: '2021', region: '南京', introduce: '介绍4' },
  { title: '项目5', gender: '男', level: '省级', ethnic: '汉族', batch: '2022', region: '武汉', introduce: '介绍5' },
  { title: '项目6', gender: '女', level: '市级', ethnic: '回族', batch: '2022', region: '成都', introduce: '介绍6' },
  { title: '项目7', gender: '男', level: '国家级', ethnic: '汉族', batch: '2023', region: '重庆', introduce: '介绍7' },
  { title: '项目8', gender: '女', level: '省级', ethnic: '苗族', batch: '2023', region: '西安', introduce: '介绍8' },
];
const overviewData = [
  { title: '项目1', gender: '男', level: '国家级', ethnic: '汉族', batch: '2023', region: '北京', introduce: '介绍1' },
  { title: '项目2', gender: '女', level: '省级', ethnic: '满族', batch: '2023', region: '上海', introduce: '介绍2' },
  { title: '项目3', gender: '男', level: '市级', ethnic: '汉族', batch: '2022', region: '广州', introduce: '介绍3' },
];
const contentData = [
  { category: '传统技艺', count: 120, avgViews: 1500, avgComments: 45 },
  { category: '传统音乐', count: 85, avgViews: 2300, avgComments: 68 },
  { category: '传统舞蹈', count: 65, avgViews: 1800, avgComments: 32 },
  { category: '传统戏剧', count: 95, avgViews: 2100, avgComments: 56 },
  { category: '曲艺', count: 45, avgViews: 1200, avgComments: 28 },
  { category: '传统体育', count: 30, avgViews: 900, avgComments: 15 },
  { category: '传统美术', count: 110, avgViews: 1900, avgComments: 42 },
  { category: '传统医药', count: 40, avgViews: 1100, avgComments: 25 },
];
const relationData = {
  nodes: [
    { name: '传统技艺', category: 0 },
    { name: '陶瓷', category: 1 },
    { name: '青花瓷', category: 2 },
    { name: '景德镇', category: 3 },
    { name: '传统音乐', category: 0 },
    { name: '古琴', category: 1 },
    { name: '琵琶', category: 1 },
    { name: '二胡', category: 1 },
    { name: '传统舞蹈', category: 0 },
    { name: '秧歌', category: 1 },
    { name: '龙舞', category: 1 },
    { name: '狮舞', category: 1 }
  ],
  links: [
    { source: '传统技艺', target: '陶瓷', value: 1 },
    { source: '陶瓷', target: '青花瓷', value: 1 },
    { source: '青花瓷', target: '景德镇', value: 1 },
    { source: '传统音乐', target: '古琴', value: 1 },
    { source: '传统音乐', target: '琵琶', value: 1 },
    { source: '传统音乐', target: '二胡', value: 1 },
    { source: '传统舞蹈', target: '秧歌', value: 1 },
    { source: '传统舞蹈', target: '龙舞', value: 1 },
    { source: '传统舞蹈', target: '狮舞', value: 1 }
  ],
  categories: [
    { name: '非遗类别' },
    { name: '具体项目' },
    { name: '子项目' },
    { name: '发源地' }
  ]
};

const chartConfigs = [
  { id: 'trendTimelineChart', title: '批次时间线趋势' },
  { id: 'trendStackChart', title: '批次级别分布' },
  { id: 'overviewGenderChart', title: '性别比例分布' },
  { id: 'overviewEthnicChart', title: '民族分布' },
  { id: 'contentRadarChart', title: '内容分布分析' },
  { id: 'contentInteractionChart', title: '互动分析' },
  { id: 'contentWordCloudChart', title: '内容热度词云' },
  { id: 'relationNetworkChart', title: '项目关系网络' },
];

const charts = ref([]);
const modalVisible = ref(false);
const modalChartIdx = ref(0);

const initCharts = async () => {
  await nextTick();
  // --- 趋势分析 ---
  // 1. 批次时间线趋势
  const trendTimelineChart = echarts.init(document.getElementById('trendTimelineChart'));
  charts.value.push(trendTimelineChart);
  const batchData = trendData.reduce((acc, cur) => {
    acc[cur.batch] = (acc[cur.batch] || 0) + 1;
    return acc;
  }, {});
  trendTimelineChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(batchData).sort(), name: '批次' },
    yAxis: { type: 'value', name: '传承人数量' },
    series: [{
      data: Object.keys(batchData).sort().map(key => batchData[key]),
      type: 'line', smooth: true,
      markPoint: { data: [{ type: 'max', name: '最大值' }, { type: 'min', name: '最小值' }] },
      markLine: { data: [{ type: 'average', name: '平均值' }] }
    }]
  });
  // 2. 批次与级别堆叠柱形图
  const trendStackChart = echarts.init(document.getElementById('trendStackChart'));
  charts.value.push(trendStackChart);
  const batchLevelData = trendData.reduce((acc, cur) => {
    if (!acc[cur.batch]) acc[cur.batch] = {};
    acc[cur.batch][cur.level] = (acc[cur.batch][cur.level] || 0) + 1;
    return acc;
  }, {});
  const levels = [...new Set(trendData.map(item => item.level))];
  const batches = Object.keys(batchLevelData).sort();
  trendStackChart.setOption({
    title: { text: '', left: 'center' },
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

  // --- 数据概览 ---
  // 1. 性别比例饼图
  const overviewGenderChart = echarts.init(document.getElementById('overviewGenderChart'));
  charts.value.push(overviewGenderChart);
  const genderData = overviewData.reduce((acc, cur) => {
    acc[cur.gender] = (acc[cur.gender] || 0) + 1;
    return acc;
  }, {});
  overviewGenderChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie', radius: '50%',
      data: Object.entries(genderData).map(([name, value]) => ({ name, value })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  });
  // 2. 民族分布条形图
  const overviewEthnicChart = echarts.init(document.getElementById('overviewEthnicChart'));
  charts.value.push(overviewEthnicChart);
  const ethnicData = overviewData.reduce((acc, cur) => {
    acc[cur.ethnic] = (acc[cur.ethnic] || 0) + 1;
    return acc;
  }, {});
  overviewEthnicChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: Object.keys(ethnicData) },
    series: [{
      data: Object.values(ethnicData),
      type: 'bar',
      showBackground: true,
      backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' }
    }]
  });

  // --- 内容分析 ---
  // 1. 内容分布雷达图
  const contentRadarChart = echarts.init(document.getElementById('contentRadarChart'));
  charts.value.push(contentRadarChart);
  contentRadarChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'item' },
    radar: {
      indicator: contentData.map(item => ({ name: item.category, max: Math.max(...contentData.map(d => d.count)) }))
    },
    series: [{
      type: 'radar',
      data: [{
        value: contentData.map(item => item.count),
        name: '内容数量',
        areaStyle: { color: 'rgba(194, 16, 28, 0.3)' },
        lineStyle: { color: '#C2101C' }
      }]
    }]
  });
  // 2. 互动分析柱状图
  const contentInteractionChart = echarts.init(document.getElementById('contentInteractionChart'));
  charts.value.push(contentInteractionChart);
  contentInteractionChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['平均浏览量', '平均评论数'], top: '10%' },
    xAxis: { type: 'category', data: contentData.map(item => item.category) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '平均浏览量',
        type: 'bar',
        data: contentData.map(item => item.avgViews),
        itemStyle: { color: '#C2101C' }
      },
      {
        name: '平均评论数',
        type: 'bar',
        data: contentData.map(item => item.avgComments),
        itemStyle: { color: '#8b1f1f' }
      }
    ]
  });
  // 3. 内容热度词云图
  const contentWordCloudChart = echarts.init(document.getElementById('contentWordCloudChart'));
  charts.value.push(contentWordCloudChart);
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
  contentWordCloudChart.setOption({
    title: { text: '', left: 'center' },
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

  // --- 关联分析 ---
  // 1. 关系网络图
  const relationNetworkChart = echarts.init(document.getElementById('relationNetworkChart'));
  charts.value.push(relationNetworkChart);
  relationNetworkChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: [{ data: relationData.categories.map(a => a.name), top: '10%' }],
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      name: '非遗项目关系',
      type: 'graph',
      layout: 'force',
      data: relationData.nodes.map(node => ({
        name: node.name,
        category: node.category,
        symbolSize: 30,
        itemStyle: {
          color: node.category === 0 ? '#C2101C' : node.category === 1 ? '#8b1f1f' : node.category === 2 ? '#a52a2a' : '#8b4513'
        }
      })),
      links: relationData.links,
      categories: relationData.categories,
      roam: true,
      label: { show: true, position: 'right' },
      force: { repulsion: 100, edgeLength: 100 },
      lineStyle: { color: 'source', curveness: 0.3 }
    }]
  });
};

// 放大图表弹窗初始化
const initModalChart = async () => {
  await nextTick();
  const idx = modalChartIdx.value;
  const config = chartConfigs[idx];
  // 销毁原有modal图表
  if (charts.value['modal']) {
    charts.value['modal'].dispose();
  }
  // 重新初始化大尺寸图表
  const domId = config.id + '-modal';
  const dom = document.getElementById(domId);
  if (!dom) return;
  let option = null;
  // 取小图表option
  const smallChart = echarts.getInstanceByDom(document.getElementById(config.id));
  if (smallChart) option = smallChart.getOption();
  charts.value['modal'] = echarts.init(dom);
  if (option) charts.value['modal'].setOption(option);
};

onMounted(async () => {
  await initCharts();
  window.addEventListener('resize', handleResize);
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  Object.values(charts.value).forEach(chart => {
    if (chart && chart.dispose) chart.dispose();
  });
});

const handleResize = () => {
  chartConfigs.forEach(cfg => {
    const chart = echarts.getInstanceByDom(document.getElementById(cfg.id));
    if (chart) chart.resize();
  });
  if (charts.value['modal']) charts.value['modal'].resize();
};

const enlargeChart = idx => {
  modalChartIdx.value = idx;
  modalVisible.value = true;
  setTimeout(() => {
    initModalChart();
  }, 100);
};
const closeModal = () => {
  modalVisible.value = false;
  if (charts.value['modal']) {
    charts.value['modal'].dispose();
    charts.value['modal'] = null;
  }
};

watch(modalVisible, v => {
  if (!v && charts.value['modal']) {
    charts.value['modal'].dispose();
    charts.value['modal'] = null;
  }
});

const filteredChartConfigs = computed(() => chartConfigs.filter(c => !['overviewRegionChart','relationSankeyChart','trendHeatmapChart','overviewLevelChart'].includes(c.id)));
</script>

<template>
  <div class="visualization-bg">
    <div class="visualization-title">故梦阑珊数据可视化</div>
    <div class="visualization-grid">
      <div
        v-for="(chart, idx) in filteredChartConfigs"
        :key="chart.id"
        class="chart-block"
        :class="{ left: idx % 2 === 0, right: idx % 2 === 1 }"
      >
        <div class="chart-title">{{ chart.title }}</div>
        <div :id="chart.id" class="chart"></div>
        <button class="enlarge-btn" @click="enlargeChart(idx)" title="放大">🔍</button>
      </div>
    </div>
    <transition name="modal-fade">
      <div v-if="modalVisible" class="modal-mask" @click.self="closeModal">
        <div class="modal-content">
          <div class="modal-header">
            <span class="modal-title">{{ chartConfigs[modalChartIdx].title }}</span>
            <button class="modal-close" @click="closeModal">×</button>
          </div>
          <div :id="chartConfigs[modalChartIdx].id + '-modal'" class="modal-chart"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.visualization-bg {
  min-height: 100vh;
  width: 100vw;
  background: url('/background/background.png') no-repeat center top;
  background-size: 100% auto;
  overflow-x: hidden;
  padding: 0;
}
.visualization-title {
  width: 100%;
  text-align: center;
  font-size: 42px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
  color: #8b1f1f;
  font-weight: bold;
  letter-spacing: 12px;
  margin-top: 190px;
  margin-bottom: 32px;
  text-shadow: 0 2px 12px rgba(194,16,28,0.08);
  user-select: none;
}
.visualization-grid {
  width: 1300px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px 32px;
  padding: 0 0 64px 0;
  margin-top: 85px;
}
.chart-block {
  background: rgba(255,255,255,0.18);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1.5px solid rgba(194,16,28,0.08);
  padding: 18px 8px 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 0;
  transition: box-shadow 0.2s, background 0.2s;
}
.chart-block.left { justify-self: start; }
.chart-block.right { justify-self: end; }
.chart-title {
  font-size: 20px;
  font-weight: bold;
  color: #8b1f1f;
  margin-bottom: 8px;
  letter-spacing: 2px;
  text-align: center;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.chart {
  width: 400px;
  height: 220px;
  min-height: 140px;
  background: transparent;
}
.enlarge-btn {
  position: absolute;
  top: 10px;
  right: 12px;
  background: rgba(255,255,255,0.7);
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  transition: background 0.2s;
}
.enlarge-btn:hover {
  background: #f5e6d6;
}

/* Modal 样式 */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}
.modal-mask {
  position: fixed;
  z-index: 9999;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: rgba(255,255,255,0.98);
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  padding: 32px 32px 24px 32px;
  min-width: 700px;
  min-height: 500px;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.modal-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.modal-title {
  font-size: 26px;
  font-weight: bold;
  color: #8b1f1f;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.modal-close {
  background: none;
  border: none;
  font-size: 32px;
  color: #8b1f1f;
  cursor: pointer;
  line-height: 1;
}
.modal-chart {
  width: 900px;
  height: 600px;
  max-width: 80vw;
  max-height: 70vh;
  background: transparent;
}
@media (max-width: 1500px) {
  .visualization-grid { width: 98vw; }
  .chart { width: 38vw; }
  .modal-chart { width: 80vw; }
}
@media (max-width: 900px) {
  .visualization-title { font-size: 28px; letter-spacing: 6px; }
  .visualization-grid { grid-template-columns: 1fr; width: 98vw; }
  .chart-block { width: 90vw; }
  .chart { width: 90vw; }
  .modal-chart { width: 98vw; }
}
</style>