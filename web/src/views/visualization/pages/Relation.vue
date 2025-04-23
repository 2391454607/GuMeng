<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';

const charts = ref([]);

const mockData = {
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

const initCharts = async () => {
  await nextTick();
  
  // 关系网络图
  const relationChart = echarts.init(document.getElementById('relationChart'));
  charts.value.push(relationChart);
  
  relationChart.setOption({
    title: { text: '非遗项目关系网络', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: [{
      data: mockData.categories.map(a => a.name),
      top: '10%'
    }],
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      name: '非遗项目关系',
      type: 'graph',
      layout: 'force',
      data: mockData.nodes.map(node => ({
        name: node.name,
        category: node.category,
        symbolSize: 30,
        itemStyle: {
          color: node.category === 0 ? '#C2101C' : 
                 node.category === 1 ? '#8b1f1f' : 
                 node.category === 2 ? '#a52a2a' : '#8b4513'
        }
      })),
      links: mockData.links,
      categories: mockData.categories,
      roam: true,
      label: {
        show: true,
        position: 'right'
      },
      force: {
        repulsion: 100,
        edgeLength: 100
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3
      }
    }]
  });

  // 桑基图
  const sankeyChart = echarts.init(document.getElementById('sankeyChart'));
  charts.value.push(sankeyChart);
  
  const sankeyData = {
    nodes: [
      { name: '传统技艺' },
      { name: '传统音乐' },
      { name: '传统舞蹈' },
      { name: '陶瓷' },
      { name: '古琴' },
      { name: '秧歌' },
      { name: '青花瓷' },
      { name: '琵琶' },
      { name: '龙舞' }
    ],
    links: [
      { source: '传统技艺', target: '陶瓷', value: 10 },
      { source: '陶瓷', target: '青花瓷', value: 8 },
      { source: '传统音乐', target: '古琴', value: 6 },
      { source: '传统音乐', target: '琵琶', value: 4 },
      { source: '传统舞蹈', target: '秧歌', value: 7 },
      { source: '传统舞蹈', target: '龙舞', value: 5 }
    ]
  };
  
  sankeyChart.setOption({
    title: { text: '非遗项目传承关系', left: 'center' },
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [{
      type: 'sankey',
      data: sankeyData.nodes,
      links: sankeyData.links,
      emphasis: {
        focus: 'adjacency'
      },
      levels: [{
        depth: 0,
        itemStyle: {
          color: '#C2101C'
        },
        lineStyle: {
          color: 'source',
          opacity: 0.6
        }
      }, {
        depth: 1,
        itemStyle: {
          color: '#8b1f1f'
        },
        lineStyle: {
          color: 'source',
          opacity: 0.6
        }
      }, {
        depth: 2,
        itemStyle: {
          color: '#a52a2a'
        },
        lineStyle: {
          color: 'source',
          opacity: 0.6
        }
      }],
      lineStyle: {
        color: 'source',
        curveness: 0.5
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
  <div class="relation-container">
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
            更多关联分析功能正在开发中，敬请期待...
          </template>
        </a-alert>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="relationChart" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-card>
          <div id="sankeyChart" class="chart"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.relation-container {
  padding: 20px;
}

.chart {
  height: 500px;
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