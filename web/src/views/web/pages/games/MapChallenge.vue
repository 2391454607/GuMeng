<template>
  <div class="map-challenge-container">
    <h1>云图探遗・地标寻珍记</h1>
    <p>点击地图上正确的非遗项目分布地</p>
    <div ref="mapBox" class="map-box"></div>
    <div class="game-info">
      <p v-if="!gameEnd">当前题目：<span v-if="currentQuestion" v-html="currentQuestion.text"></span></p>
      <p>得分：{{ score }}</p>
      <div v-if="gameEnd" class="game-end-message">
        <p>恭喜你完成挑战！</p>
        <p>你的最终得分是：<span>{{ score }}</span></p>
        <a-button type="primary" @click="restartGame">重新挑战</a-button>
      </div>
    </div>
    <!-- 音效audio标签 -->
    <audio ref="correctAudio" src="/music/答题正确音.mp3"></audio>
    <audio ref="errorAudio" src="/music/操作失误音.mp3"></audio>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import yunnanGeoJson from '@/utils/yunnan.json'; // 导入地图数据
import { Message } from '@arco-design/web-vue';
import * as echarts from 'echarts'; // 导入echarts

const score = ref(0);
const currentQuestion = ref(null);
const questions = ref([]);
const mapBox = ref(null); // 地图容器的ref引用
let myChart = null; // echarts实例

// 音效相关
const correctAudio = ref(null);
const errorAudio = ref(null);
function playCorrect() { correctAudio.value && correctAudio.value.play && correctAudio.value.play(); }
function playError() { errorAudio.value && errorAudio.value.play && errorAudio.value.play(); }

// 示例非遗项目数据 (非完整数据，仅用于演示)
const ichData = [
  { name: '白族扎染技艺', location: '大理白族自治州', adcode: 532900 },
  { name: '傣族泼水节', location: '西双版纳傣族自治州', adcode: 532800 },
  { name: '哈尼梯田文化系统', location: '红河哈尼族彝族自治州', adcode: 532500 },
  { name: '纳西古乐', location: '丽江市', adcode: 530700 },
  { name: '云南普洱茶制作技艺', location: '普洱市', adcode: 530800 },
  { name: '彝族火把节', location: '楚雄彝族自治州', adcode: 532300 },
  { name: '剑川木雕', location: '大理白族自治州', adcode: 532900 },
  { name: '彝族毕摩经', location: '楚雄彝族自治州', adcode: 532300 },
  { name: '傣族慢轮制陶技艺', location: '西双版纳傣族自治州', adcode: 532800 },
  { name: '乌铜走银技艺', location: '昆明市', adcode: 530100 },
];

const gameEnd = ref(false); // 游戏是否结束的状态

// 生成题目
const generateQuestions = () => {
  questions.value = ichData.map(item => ({
    text: `请点击 <span style="color: #C2101C; font-weight: bold;">${item.name}</span> 所在的地区`,
    adcode: item.adcode,
    locationName: item.location // 添加地市名称方便显示
  }));
  questions.value.sort(() => Math.random() - 0.5); // 随机打乱
  currentQuestion.value = questions.value[0];
  score.value = 0; // 重置得分
  gameEnd.value = false; // 重置游戏结束状态
};

// 处理地图点击事件
const handleMapClick = (params) => {
  if (gameEnd.value) return; // 游戏结束时不再响应点击
  if (params.data && currentQuestion.value) {
    if (currentQuestion.value.adcode === params.data.adcode) {
      score.value++;
      playCorrect();
      Message.success('回答正确！');
      highlightArea(params.data.adcode, true); // 高亮正确区域
      nextQuestion();
    } else {
      playError();
      Message.error('回答错误！');
      highlightArea(params.data.adcode, false); // 错误提示高亮（可选）
      // 可以选择是否立即进行下一题或等待用户再次点击
      setTimeout(nextQuestion, 1000); // 1秒后自动下一题
    }
  }
};

// 高亮地图区域
const highlightArea = (adcode, isCorrect) => {
  // 找到对应的feature
  const feature = yunnanGeoJson.features.find(f => f.properties.adcode === adcode);
  if (feature) {
    // 使用dispatchAction来选中或高亮区域
    myChart.dispatchAction({
      type: 'mapSelect',
      seriesIndex: 0,
      name: feature.properties.name // 使用地市名称进行选中
    });

    // 可以进一步修改选中区域的样式，例如添加动画或改变颜色
    // 注意：直接修改option并setOption可能会重置其他状态，这里暂时只用select
    // 更复杂的样式控制可能需要更高级的Echarts用法或手动SVG操作

    // 如果是错误点击，可以在一段时间后取消高亮
    if (!isCorrect) {
      setTimeout(() => {
        myChart.dispatchAction({
          type: 'mapUnSelect',
          seriesIndex: 0,
          name: feature.properties.name
        });
      }, 1000); // 1秒后取消高亮
    }
  }
};

// 下一题
const nextQuestion = () => {
  const currentIndex = questions.value.indexOf(currentQuestion.value);
  if (currentIndex < questions.value.length - 1) {
    // 取消上一题正确高亮的地市的选中状态（如果保留选中状态，则不需要这步）
    if (currentQuestion.value) {
      const prevFeature = yunnanGeoJson.features.find(f => f.properties.adcode === currentQuestion.value.adcode);
      if (prevFeature) {
        myChart.dispatchAction({
          type: 'mapUnSelect',
          seriesIndex: 0,
          name: prevFeature.properties.name
        });
      }
    }
    currentQuestion.value = questions.value[currentIndex + 1];
  } else {
    Message.info('所有题目已答完！');
    gameEnd.value = true; // 设置游戏结束状态
    currentQuestion.value = null; // 清空当前题目
    // 游戏结束时取消所有选中状态
    myChart.dispatchAction({
      type: 'mapUnSelect',
      seriesIndex: 0,
      data: yunnanGeoJson.features.map(f => ({ name: f.properties.name }))
    });
  }
};

// 重新开始游戏
const restartGame = () => {
  generateQuestions(); // 重新生成题目并重置状态
  // 确保地图已初始化再清除选中状态
  if (myChart) {
    myChart.dispatchAction({
      type: 'mapUnSelect',
      seriesIndex: 0,
      data: yunnanGeoJson.features.map(f => ({ name: f.properties.name }))
    });
  }
};

// 初始化地图
const initMap = () => {
  if (mapBox.value) {
    myChart = echarts.init(mapBox.value);
    echarts.registerMap('yunnan', yunnanGeoJson);

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}'
      },
      visualMap: {
        min: 0,
        max: 1000, // 示例范围，可以根据实际非遗项目数量调整
        left: 'left',
        top: 'bottom',
        text: ['高', '低'], // 文本，默认为数值文本
        calculable: true,
        inRange: {
          color: ['#f7f3ea', '#C2101C'] // 颜色范围
        }
      },
      series: [
        {
          name: '非遗项目数量', // 系列名称更改为更有意义的
          type: 'map',
          map: 'yunnan',
          roam: true,
          label: {
            show: true,
            color: '#333' // 地市名称颜色
          },
          itemStyle: {
            areaColor: '#f7f3ea',
            borderColor: '#C2101C'
          },
          emphasis: {
            itemStyle: {
              areaColor: '#ffcfcf'
            },
            label: {
              show: true,
              color: '#C2101C'
            }
          },
          select: { // 添加选中样式
            itemStyle: {
              areaColor: '#ffe6e6', // 选中时区域颜色
              borderColor: '#C2101C'
            },
            label: {
              color: '#C2101C'
            }
          },
          data: yunnanGeoJson.features.map(feature => ({
            name: feature.properties.name,
            // 这里的value可以关联该地市的非遗项目数量，实现颜色深浅映射
            // 目前为了演示，先使用随机数或固定值
            value: Math.floor(Math.random() * 500), // 示例数据
            adcode: feature.properties.adcode
          }))
        }
      ]
    };

    myChart.setOption(option);
    myChart.on('click', handleMapClick);
  }
};

onMounted(() => {
  initMap();
  generateQuestions();
});

onBeforeUnmount(() => {
  if (myChart) {
    myChart.dispose();
  }
});

</script>

<style scoped>
.map-challenge-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f7f3ea; /* 与其他游戏页面保持一致的背景 */
  min-height: 100vh;
}

.map-challenge-container h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}

.map-challenge-container p {
  color: #666;
  font-size: 18px;
  margin-bottom: 18px;
}

.map-box {
  width: 80%; /* 地图容器宽度 */
  max-width: 800px; /* 最大宽度 */
  margin-top: 20px;
  /* border: 1px solid #ccc; */ /* 地图本身带有边框，可以移除外层容器边框 */
  aspect-ratio: 1.2; /* 保持地图的长宽比，避免变形 */
}

.game-info {
  margin-top: 20px;
  font-size: 18px;
  text-align: center;
}

.game-info span {
  color: #C2101C;
  font-weight: bold;
}

.game-end-message {
  margin-top: 20px;
  text-align: center;
}

.game-end-message p {
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .map-box {
    width: 95%;
  }
}
</style> 