<script setup>
import { ref, onMounted, reactive } from 'vue';
import { getCommentStatsAPI } from "@/api/manage/Comment.js";

// 统计数据
const stats = reactive({
  // 访问人次
  visitors: {
    total: 5225,
    trend: 50,
    chartData: [1200, 1800, 1600, 2100, 1800, 2400, 1900]
  },
  // 内容发布量
  contents: {
    total: 1142,
    trend: 50,
    chartData: [42, 65, 75, 58, 69, 80, 65]
  },
  // 评论总量 (真实数据)
  comments: {
    total: 0,
    trend: 0,
    chartData: [120, 180, 150, 210, 170, 190, 220]
  },
  // 用户分布
  users: {
    total: 5225,
    trend: 30,
    distribution: {
      student: 65,
      worker: 25,
      other: 10
    }
  }
});

// 加载状态
const loading = ref(true);
// 动画控制
const animationStarted = ref(false);
// 数字计数器
const counters = reactive({
  visitors: 0,
  contents: 0,
  comments: 0,
  users: 0
});

onMounted(() => {
  // 获取评论的真实数据
  getCommentStats().then(() => {
    loading.value = false;
    // 数据加载完成后启动动画
    setTimeout(() => {
      animationStarted.value = true;
      startCounterAnimations();
    }, 300);
  });
});

// 获取评论统计数据
const getCommentStats = async () => {
  try {
    const res = await getCommentStatsAPI();
    if (res.code === 200) {
      stats.comments.total = res.data.totalComments;
      stats.comments.trend = res.data.todayComments;
    }
  } catch (err) {
    console.error("获取评论统计出错:", err);
  }
};

// 数字增长动画
const startCounterAnimations = () => {
  const duration = 1500; // 动画持续时间
  const steps = 60; // 动画步数
  const interval = duration / steps;
  
  let step = 0;
  
  const timer = setInterval(() => {
    step++;
    
    // 计算当前步骤的百分比
    const progress = step / steps;
    
    // 使用缓动函数使动画更自然
    const easedProgress = easeOutQuart(progress);
    
    // 更新计数器的值
    counters.visitors = Math.round(easedProgress * stats.visitors.total);
    counters.contents = Math.round(easedProgress * stats.contents.total);
    counters.comments = Math.round(easedProgress * stats.comments.total);
    counters.users = Math.round(easedProgress * stats.users.total);
    
    if (step >= steps) {
      // 动画结束，设置为最终值
      counters.visitors = stats.visitors.total;
      counters.contents = stats.contents.total;
      counters.comments = stats.comments.total;
      counters.users = stats.users.total;
      clearInterval(timer);
    }
  }, interval);
};

// 缓动函数 - 快速开始，缓慢结束
const easeOutQuart = (x) => {
  return 1 - Math.pow(1 - x, 4);
};
</script>

<template>
  <div class="dashboard">
    <a-spin :loading="loading">
      <div class="stats-row">
        <!-- 访问人次 -->
        <div class="stat-card">
          <div class="stat-header">
            <div class="stat-title">访问人次</div>
          </div>
          <div class="stat-body">
            <div class="stat-number">{{ counters.visitors.toLocaleString() }}</div>
            <div class="stat-trend">
              较昨日 <span class="trend-up">{{ stats.visitors.trend }} ↑</span>
            </div>
          </div>
          <div class="stat-chart">
            <svg viewBox="0 0 300 80" class="line-chart blue">
              <polyline
                :class="{ 'animate-line': animationStarted }"
                :points="stats.visitors.chartData.map((value, index) => 
                  `${index * 42},${80 - (value / 2500) * 80}`).join(' ')"
              />
              <circle 
                v-for="(value, index) in stats.visitors.chartData" 
                :key="index"
                :cx="index * 42" 
                :cy="80 - (value / 2500) * 80" 
                r="4"
                class="chart-point"
                :class="{ 'animate-point': animationStarted }"
                :style="{ animationDelay: `${index * 0.1}s` }"
              />
            </svg>
          </div>
        </div>
        
        <!-- 内容发布量 -->
        <div class="stat-card">
          <div class="stat-header">
            <div class="stat-title">内容发布量</div>
          </div>
          <div class="stat-body">
            <div class="stat-number">{{ counters.contents.toLocaleString() }}</div>
            <div class="stat-trend">
              较昨日 <span class="trend-up">{{ stats.contents.trend }} ↑</span>
            </div>
          </div>
          <div class="stat-chart">
            <svg viewBox="0 0 300 80" class="bar-chart green">
              <rect 
                v-for="(value, index) in stats.contents.chartData" 
                :key="index"
                :x="index * 42 + 5" 
                :y="80"
                :height="0"
                width="32"
                :class="{ 'animate-bar': animationStarted }"
                :style="{ 
                  animationDelay: `${index * 0.1}s`,
                  height: animationStarted ? `${(value / 100) * 80}px` : '0px',
                  y: animationStarted ? `${80 - (value / 100) * 80}` : '80'
                }"
              />
            </svg>
          </div>
        </div>
        
        <!-- 评论总量 -->
        <div class="stat-card">
          <div class="stat-header">
            <div class="stat-title">评论总量</div>
          </div>
          <div class="stat-body">
            <div class="stat-number">{{ counters.comments.toLocaleString() }}</div>
            <div class="stat-trend">
              较昨日 <span class="trend-up">{{ stats.comments.trend }} ↑</span>
            </div>
          </div>
          <div class="stat-chart">
            <svg viewBox="0 0 300 80" class="line-chart blue">
              <polyline
                :class="{ 'animate-line': animationStarted }"
                :points="stats.comments.chartData.map((value, index) => 
                  `${index * 42},${80 - (value / 250) * 80}`).join(' ')"
              />
              <circle 
                v-for="(value, index) in stats.comments.chartData" 
                :key="index"
                :cx="index * 42" 
                :cy="80 - (value / 250) * 80" 
                r="4"
                class="chart-point"
                :class="{ 'animate-point': animationStarted }"
                :style="{ animationDelay: `${index * 0.1}s` }"
              />
            </svg>
          </div>
        </div>
        
        <!-- 用户分布 -->
        <div class="stat-card">
          <div class="stat-header">
            <div class="stat-title">用户分布</div>
          </div>
          <div class="stat-body">
            <div class="stat-number">{{ counters.users.toLocaleString() }}</div>
            <div class="stat-trend">
              较昨日 <span class="trend-up">{{ stats.users.trend }} ↑</span>
            </div>
          </div>
          <div class="stat-chart donut-container">
            <svg viewBox="0 0 100 100" class="donut-chart">
              <!-- 饼图背景 -->
              <circle cx="50" cy="50" r="40" fill="transparent" stroke="#eee" stroke-width="20" />
              
              <!-- 学生占比 -->
              <circle cx="50" cy="50" r="40" fill="transparent" 
                stroke="#1890ff" stroke-width="20"
                stroke-dasharray="251.2 251.2"
                stroke-dashoffset="251.2"
                :class="{ 'animate-circle': animationStarted }"
                :style="{
                  strokeDashoffset: animationStarted 
                    ? 251.2 - (251.2 * stats.users.distribution.student / 100) 
                    : 251.2
                }"
              />
              
              <!-- 工人占比 -->
              <circle cx="50" cy="50" r="40" fill="transparent" 
                stroke="#52c41a" stroke-width="20"
                stroke-dasharray="251.2 251.2"
                :class="{ 'animate-circle': animationStarted }"
                :style="{
                  strokeDashoffset: animationStarted 
                    ? 251.2 - (251.2 * stats.users.distribution.worker / 100)
                    : 251.2,
                  transform: `rotate(${stats.users.distribution.student * 3.6}deg)`,
                  transformOrigin: '50% 50%',
                  animationDelay: '0.3s'
                }"
              />
              
              <!-- 其他占比 -->
              <circle cx="50" cy="50" r="40" fill="transparent" 
                stroke="#ff4d4f" stroke-width="20"
                stroke-dasharray="251.2 251.2"
                :class="{ 'animate-circle': animationStarted }"
                :style="{
                  strokeDashoffset: animationStarted 
                    ? 251.2 - (251.2 * stats.users.distribution.other / 100)
                    : 251.2,
                  transform: `rotate(${(stats.users.distribution.student + stats.users.distribution.worker) * 3.6}deg)`,
                  transformOrigin: '50% 50%',
                  animationDelay: '0.6s'
                }"
              />
            </svg>
            <div class="donut-legend">
              <div class="legend-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.7s">
                <span class="legend-color student"></span>
                <span>学生</span>
              </div>
              <div class="legend-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.8s">
                <span class="legend-color worker"></span>
                <span>职工</span>
              </div>
              <div class="legend-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.9s">
                <span class="legend-color other"></span>
                <span>其他</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 系统信息卡片 -->
      <div class="system-card" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.5s">
        <div class="system-header">
          <div class="system-title">系统信息</div>
        </div>
        <div class="system-body">
          <div class="system-row">
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.6s">
              <div class="item-label">系统名称</div>
              <div class="item-value">故梦阑珊非遗文化管理系统</div>
            </div>
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.7s">
              <div class="item-label">系统版本</div>
              <div class="item-value">v1.0.0</div>
            </div>
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.8s">
              <div class="item-label">服务器环境</div>
              <div class="item-value">CentOS 7.9 / MySQL 8.0</div>
            </div>
          </div>
          <div class="system-row">
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 0.9s">
              <div class="item-label">前端框架</div>
              <div class="item-value">Vue 3 + Vite + Arco Design</div>
            </div>
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 1s">
              <div class="item-label">后端框架</div>
              <div class="item-value">Spring Boot 2.7.0</div>
            </div>
            <div class="system-item" :class="{ 'fade-in': animationStarted }" style="animation-delay: 1.1s">
              <div class="item-label">上线时间</div>
              <div class="item-value">2024-06-01</div>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  min-width: 280px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.stat-header {
  margin-bottom: 8px;
}

.stat-title {
  font-size: 16px;
  color: #86909c;
  font-weight: 500;
}

.stat-body {
  margin-bottom: 16px;
  z-index: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 13px;
  color: #86909c;
}

.trend-up {
  color: #00b42a;
  font-weight: 500;
}

.trend-down {
  color: #f53f3f;
  font-weight: 500;
}

.stat-chart {
  height: 80px;
  width: 100%;
  position: relative;
}

/* 折线图样式 */
.line-chart {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.line-chart polyline {
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.blue polyline {
  stroke: #1890ff;
  stroke-opacity: 0.8;
}

.chart-point {
  fill: #1890ff;
  opacity: 0;
}

/* 柱状图样式 */
.bar-chart {
  width: 100%;
  height: 100%;
}

.green rect {
  fill: #52c41a;
  fill-opacity: 0.8;
  rx: 3;
  ry: 3;
}

/* 环形图样式 */
.donut-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.donut-chart {
  width: 80px;
  height: 80px;
  transform: rotate(-90deg);
}

.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #4e5969;
  opacity: 0;
}

.legend-color {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 8px;
}

.student {
  background-color: #1890ff;
}

.worker {
  background-color: #52c41a;
}

.other {
  background-color: #ff4d4f;
}

/* 系统信息卡片 */
.system-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
  opacity: 0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.system-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.08);
}

.system-header {
  margin-bottom: 16px;
}

.system-title {
  font-size: 16px;
  color: #86909c;
  font-weight: 500;
}

.system-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.system-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.system-item {
  flex: 1;
  min-width: 200px;
  opacity: 0;
}

.item-label {
  font-size: 14px;
  color: #86909c;
  margin-bottom: 4px;
}

.item-value {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .stats-row {
    flex-direction: column;
  }
  
  .stat-card {
    width: 100%;
  }
  
  .system-row {
    flex-direction: column;
  }
}

/* 动画样式 */
.animate-line {
  stroke-dasharray: 1000;
  stroke-dashoffset: 1000;
  animation: drawLine 1.5s ease-out forwards;
}

@keyframes drawLine {
  to {
    stroke-dashoffset: 0;
  }
}

.animate-point {
  animation: fadeInPoint 0.5s ease-out forwards;
}

@keyframes fadeInPoint {
  0% {
    opacity: 0;
    r: 0;
  }
  100% {
    opacity: 1;
    r: 4;
  }
}

.animate-bar {
  transition: height 1s cubic-bezier(0.34, 1.56, 0.64, 1), y 1s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.animate-circle {
  transition: stroke-dashoffset 1.5s ease-out;
}

.fade-in {
  animation: fadeIn 0.8s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>