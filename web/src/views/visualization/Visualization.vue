<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue';
import * as echarts from 'echarts';
import 'echarts-wordcloud';

import yunnanGeoJson from '@/utils/yunnan.json';



// --- mockData 合并 ---
// 更贴近云南省非遗真实数据的模拟
const trendData = [
  { title: '白族扎染技艺', gender: '男', level: '国家级', ethnic: '白族', batch: '2006', region: '大理白族自治州', introduce: '白族传统染织工艺，国家级非遗。' },
  { title: '彝族火把节', gender: '女', level: '省级', ethnic: '彝族', batch: '2008', region: '楚雄彝族自治州', introduce: '彝族传统节日，省级非遗。' },
  { title: '傣族泼水节', gender: '男', level: '国家级', ethnic: '傣族', batch: '2006', region: '西双版纳傣族自治州', introduce: '傣族传统节日，国家级非遗。' },
  { title: '哈尼梯田文化系统', gender: '女', level: '国家级', ethnic: '哈尼族', batch: '2013', region: '红河哈尼族彝族自治州', introduce: '哈尼族梯田文化，国家级非遗。' },
  { title: '纳西古乐', gender: '男', level: '省级', ethnic: '纳西族', batch: '2008', region: '丽江市', introduce: '纳西族传统音乐，省级非遗。' },
  { title: '普洱茶制作技艺', gender: '女', level: '国家级', ethnic: '汉族', batch: '2008', region: '普洱市', introduce: '普洱茶传统制作工艺，国家级非遗。' },
  { title: '剑川木雕', gender: '男', level: '省级', ethnic: '白族', batch: '2011', region: '大理白族自治州', introduce: '白族传统木雕工艺，省级非遗。' },
  { title: '乌铜走银', gender: '女', level: '市级', ethnic: '汉族', batch: '2015', region: '昆明市', introduce: '昆明传统金属工艺，市级非遗。' },
  { title: '苗族银饰锻制技艺', gender: '男', level: '省级', ethnic: '苗族', batch: '2011', region: '文山壮族苗族自治州', introduce: '苗族传统银饰锻造，省级非遗。' },
  { title: '景颇族目瑙纵歌', gender: '女', level: '省级', ethnic: '景颇族', batch: '2011', region: '德宏傣族景颇族州', introduce: '景颇族传统舞蹈，省级非遗。' },
  { title: '藏族锅庄舞', gender: '男', level: '市级', ethnic: '藏族', batch: '2015', region: '迪庆藏族自治州', introduce: '藏族传统舞蹈，市级非遗。' },
  { title: '傈僳族刺绣', gender: '女', level: '市级', ethnic: '傈僳族', batch: '2015', region: '怒江傈僳族自治州', introduce: '傈僳族传统刺绣，市级非遗。' },
  { title: '壮族铜鼓舞', gender: '男', level: '省级', ethnic: '壮族', batch: '2011', region: '文山壮族苗族自治州', introduce: '壮族传统舞蹈，省级非遗。' },
  { title: '回族清真饮食文化', gender: '女', level: '市级', ethnic: '回族', batch: '2015', region: '昆明市', introduce: '回族传统饮食文化，市级非遗。' },
  { title: '汉族剪纸', gender: '男', level: '市级', ethnic: '汉族', batch: '2015', region: '曲靖市', introduce: '汉族传统剪纸艺术，市级非遗。' },
  { title: '佤族木鼓舞', gender: '女', level: '省级', ethnic: '佤族', batch: '2011', region: '临沧市', introduce: '佤族传统舞蹈，省级非遗。' },
];

// 数据概览（取部分代表性项目）
const overviewData = [
  { title: '白族扎染技艺', gender: '男', level: '国家级', ethnic: '白族', batch: '2006', region: '大理白族自治州', introduce: '白族传统染织工艺，国家级非遗。' },
  { title: '傣族泼水节', gender: '男', level: '国家级', ethnic: '傣族', batch: '2006', region: '西双版纳傣族自治州', introduce: '傣族传统节日，国家级非遗。' },
  { title: '彝族火把节', gender: '女', level: '省级', ethnic: '彝族', batch: '2008', region: '楚雄彝族自治州', introduce: '彝族传统节日，省级非遗。' },
  { title: '哈尼梯田文化系统', gender: '女', level: '国家级', ethnic: '哈尼族', batch: '2013', region: '红河哈尼族彝族自治州', introduce: '哈尼族梯田文化，国家级非遗。' },
  { title: '纳西古乐', gender: '男', level: '省级', ethnic: '纳西族', batch: '2008', region: '丽江市', introduce: '纳西族传统音乐，省级非遗。' },
  { title: '普洱茶制作技艺', gender: '女', level: '国家级', ethnic: '汉族', batch: '2008', region: '普洱市', introduce: '普洱茶传统制作工艺，国家级非遗。' },
  { title: '苗族银饰锻制技艺', gender: '男', level: '省级', ethnic: '苗族', batch: '2011', region: '文山壮族苗族自治州', introduce: '苗族传统银饰锻造，省级非遗。' },
  { title: '景颇族目瑙纵歌', gender: '女', level: '省级', ethnic: '景颇族', batch: '2011', region: '德宏傣族景颇族州', introduce: '景颇族传统舞蹈，省级非遗。' },
  { title: '藏族锅庄舞', gender: '男', level: '市级', ethnic: '藏族', batch: '2015', region: '迪庆藏族自治州', introduce: '藏族传统舞蹈，市级非遗。' },
  { title: '傈僳族刺绣', gender: '女', level: '市级', ethnic: '傈僳族', batch: '2015', region: '怒江傈僳族自治州', introduce: '傈僳族传统刺绣，市级非遗。' },
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
    { name: '白族', category: 0 },
    { name: '彝族', category: 0 },
    { name: '傣族', category: 0 },
    { name: '哈尼族', category: 0 },
    { name: '纳西族', category: 0 },
    { name: '白族扎染技艺', category: 1 },
    { name: '剑川木雕', category: 1 },
    { name: '彝族火把节', category: 1 },
    { name: '傣族泼水节', category: 1 },
    { name: '哈尼梯田文化系统', category: 1 },
    { name: '纳西古乐', category: 1 },
    { name: '大理', category: 2 },
    { name: '楚雄', category: 2 },
    { name: '西双版纳', category: 2 },
    { name: '红河', category: 2 },
    { name: '丽江', category: 2 }
  ],
  links: [
    { source: '白族', target: '白族扎染技艺', value: 1 },
    { source: '白族', target: '剑川木雕', value: 1 },
    { source: '大理', target: '白族扎染技艺', value: 1 },
    { source: '大理', target: '剑川木雕', value: 1 },
    { source: '彝族', target: '彝族火把节', value: 1 },
    { source: '楚雄', target: '彝族火把节', value: 1 },
    { source: '傣族', target: '傣族泼水节', value: 1 },
    { source: '西双版纳', target: '傣族泼水节', value: 1 },
    { source: '哈尼族', target: '哈尼梯田文化系统', value: 1 },
    { source: '红河', target: '哈尼梯田文化系统', value: 1 },
    { source: '纳西族', target: '纳西古乐', value: 1 },
    { source: '丽江', target: '纳西古乐', value: 1 }
  ],
  categories: [
    { name: '民族' },
    { name: '非遗项目' },
    { name: '发源地' }
  ]
};

const chartConfigs = [
  { id: 'relationNetworkChart', title: '项目关系网络' },
  { id: 'trendStackChart', title: '批次级别分布' },
  { id: 'overviewGenderChart', title: '性别比例分布' },
  { id: 'overviewEthnicChart', title: '民族分布' },
  { id: 'contentRadarChart', title: '内容分布分析' },
  { id: 'contentInteractionChart', title: '互动分析' },
  { id: 'trendTimelineChart', title: '批次时间线趋势' },
  { id: 'contentWordCloudChart', title: '内容热度词云' },
];

// 交换内容热度词云与性别比例分布
const temp = chartConfigs[2];
chartConfigs[2] = chartConfigs[7];
chartConfigs[7] = temp;

const charts = ref([]);
const modalVisible = ref(false);
const modalChartIdx = ref(0);
const yunnanMapModalVisible = ref(false);

// 民族分布数据（用于民族分布图）
const overviewEthnicData = [
  { ethnic: '汉族', count: 100 },
  { ethnic: '白族', count: 80 },
  { ethnic: '哈尼族', count: 60 },
  { ethnic: '傣族', count: 50 },
  { ethnic: '傈僳族', count: 40 },
  { ethnic: '佤族', count: 35 },
  { ethnic: '纳西族', count: 30 },
  { ethnic: '普米族', count: 25 },
  { ethnic: '基诺族', count: 15 },
  { ethnic: '德昂族', count: 15 },
  { ethnic: '独龙族', count: 10 },
  { ethnic: '回族', count: 20 },
  { ethnic: '苗族', count: 45 },
  { ethnic: '壮族', count: 30 },
  { ethnic: '瑶族', count: 25 },
  { ethnic: '景颇族', count: 30 },
  { ethnic: '布朗族', count: 20 },
  { ethnic: '阿昌族', count: 15 },
  { ethnic: '怒族', count: 15 }
];

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
  overviewEthnicChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: overviewEthnicData.map(item => item.ethnic) },
    series: [{
      data: overviewEthnicData.map(item => item.count),
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
  { "name": "白族扎染技艺", "value": 91 },
  { "name": "傣族泼水节", "value": 88 },
  { "name": "彝族火把节", "value": 88 },
  { "name": "永子围棋", "value": 87 },
  { "name": "乌铜走银", "value": 57 },  // 补充高频非遗
  { "name": "剑川木雕", "value": 50 },  // 补充高频非遗
  { "name": "普洱茶制作技艺", "value": 65 },  // 补充高频非遗
  { "name": "哈尼梯田文化系统", "value": 75 },  // 补充高频非遗
  { "name": "纳西古乐", "value": 69 }   // 补充高频非遗
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

  // --- 云南省热度分布地图 ---
  echarts.registerMap('云南', yunnanGeoJson); // <--- 将注册地图移到这里
  await nextTick(); // 确保DOM已渲染
  const yunnanMapChart = echarts.init(document.getElementById('yunnanMapChart'));
  charts.value.push(yunnanMapChart);
  const yunnanData = [
    { name: '昆明市', value: 120, count: 8, types: ['传统技艺', '传统音乐'], levels: ['国家级', '省级'] },
    { name: '曲靖市', value: 80, count: 5, types: ['传统舞蹈'], levels: ['省级'] },
    { name: '玉溪市', value: 60, count: 3, types: ['传统医药'], levels: ['市级'] },
    { name: '保山市', value: 40, count: 2, types: ['传统美术'], levels: ['市级'] },
    { name: '昭通市', value: 70, count: 4, types: ['传统技艺', '传统戏剧'], levels: ['省级'] },
    { name: '丽江市', value: 50, count: 3, types: ['传统音乐'], levels: ['国家级'] },
    { name: '普洱市', value: 30, count: 1, types: ['传统体育'], levels: ['市级'] },
    { name: '临沧市', value: 20, count: 1, types: ['传统美术'], levels: ['市级'] },
    { name: '楚雄彝族自治州', value: 55, count: 2, types: ['传统技艺'], levels: ['省级'] },
    { name: '红河哈尼族彝族自治州', value: 65, count: 3, types: ['传统舞蹈', '传统音乐'], levels: ['省级', '市级'] },
    { name: '文山壮族苗族自治州', value: 35, count: 1, types: ['传统戏剧'], levels: ['市级'] },
    { name: '西双版纳傣族自治州', value: 25, count: 1, types: ['传统技艺'], levels: ['市级'] },
    { name: '大理白族自治州', value: 75, count: 4, types: ['传统技艺', '传统美术'], levels: ['国家级', '省级'] },
    { name: '德宏傣族景颇族州', value: 15, count: 1, types: ['传统舞蹈'], levels: ['市级'] },
    { name: '怒江傈僳族自治州', value: 10, count: 1, types: ['传统医药'], levels: ['市级'] },
    { name: '迪庆藏族自治州', value: 5, count: 1, types: ['传统美术'], levels: ['市级'] }
  ];
  yunnanMapChart.setOption({
    title: { text: '', left: 'center' },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        const data = params.data || {};
        return `
          <b>${data.name}</b><br/>
          热度: ${data.value || '-'}<br/>
          非遗项目数量: ${data.count || '-'}<br/>
          种类: ${(data.types || []).join('、') || '-'}<br/>
          级别: ${(data.levels || []).join('、') || '-'}
        `;
      }
    },
    visualMap: {
      min: 0,
      max: 120,
      left: 'left',
      top: 'bottom',
      text: ['高','低'],
      inRange: { color: ['#fceabb', '#f8b500', '#c0392b'] },
      calculable: true
    },
    series: [{
      name: '热度',
      type: 'map',
      map: '云南',
      roam: true,
      label: { show: true },
      data: yunnanData
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

const enlargeYunnanMap = () => {
  yunnanMapModalVisible.value = true;
  setTimeout(() => {
    nextTick(() => {
      if (charts.value['yunnan-modal']) charts.value['yunnan-modal'].dispose();
      const dom = document.getElementById('yunnanMapChart-modal');
      if (!dom) return;
      const smallChart = echarts.getInstanceByDom(document.getElementById('yunnanMapChart'));
      let option = smallChart ? smallChart.getOption() : null;
      charts.value['yunnan-modal'] = echarts.init(dom);
      if (option) charts.value['yunnan-modal'].setOption(option);
    });
  }, 100);
};
const closeYunnanMapModal = () => {
  yunnanMapModalVisible.value = false;
  if (charts.value['yunnan-modal']) {
    charts.value['yunnan-modal'].dispose();
    charts.value['yunnan-modal'] = null;
  }
};
</script>

<template>
  <div>
    <div class="visualization-root-bg"></div>
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
      <!-- 云南省热度分布地图 -->
      <div class="yunnan-map-block">
        <div class="chart-title">非遗足迹</div>
        <div id="yunnanMapChart" class="yunnan-map-chart"></div>
        <button class="enlarge-btn" @click="enlargeYunnanMap" title="放大">🔍</button>
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
      <transition name="modal-fade">
        <div v-if="yunnanMapModalVisible" class="modal-mask" @click.self="closeYunnanMapModal">
          <div class="modal-content">
            <div class="modal-header">
              <span class="modal-title">云南省热度分布</span>
              <button class="modal-close" @click="closeYunnanMapModal">×</button>
            </div>
            <div id="yunnanMapChart-modal" class="modal-map-chart"></div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<style>
.visualization-root-bg {
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  z-index: -2;
  background: url('/background/BB.jpg') no-repeat center center;
  background-size: cover;
  background-attachment: fixed;
}
.visualization-bg {
  min-height: 120vh;
  width: 1600px;
  background: url('/background/background.png') no-repeat center top;
  background-size: cover;
  background-position: top center;
  overflow-x: hidden;
  padding: 0 0 80px 0;
  margin-top: -120px;
}
.visualization-title {
  width: 100%;
  text-align: center;
  font-size: 42px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
  color: #8b1f1f;
  font-weight: bold;
  letter-spacing: 12px;
  margin-top: 450px;
  margin-bottom: 32px;
  text-shadow: 0 2px 12px rgba(194,16,28,0.08);
  user-select: none;
}
.visualization-grid {
  width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px 32px;
  padding: 0 0 64px 0;
  margin-top: 80px;
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
.yunnan-map-block {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 40px 0;
}
.yunnan-map-chart {
  width: 600px;
  height: 450px;
  margin: 0 auto;
}
.modal-map-chart {
  width: 900px;
  height: 700px;
  max-width: 90vw;
  max-height: 80vh;
  background: transparent;
}
</style>