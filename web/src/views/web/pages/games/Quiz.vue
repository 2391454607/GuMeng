<script setup>
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';

const questions = [
  {
    question: '云南省哪一个民族的扎染技艺被列为国家级非遗？',
    options: ['彝族', '白族', '傣族', '哈尼族'],
    answer: 1,
    explain: '白族扎染技艺是国家级非遗，主要分布在大理白族自治州。'
  },
  {
    question: '云南西双版纳最著名的传统节日是什么？',
    options: ['火把节', '泼水节', '花山节', '三月三'],
    answer: 1,
    explain: '傣族泼水节是西双版纳最著名的传统节日。'
  },
  {
    question: '哈尼梯田文化系统主要分布在哪个自治州？',
    options: ['红河哈尼族彝族自治州', '楚雄彝族自治州', '大理白族自治州', '德宏傣族景颇族州'],
    answer: 0,
    explain: '哈尼梯田文化系统主要分布在红河哈尼族彝族自治州。'
  },
  {
    question: '下列哪项是纳西族的传统音乐？',
    options: ['锅庄舞', '古琴', '纳西古乐', '芦笙舞'],
    answer: 2,
    explain: '纳西古乐是纳西族的传统音乐。'
  },
  {
    question: '云南普洱茶制作技艺属于哪类非遗？',
    options: ['传统技艺', '传统医药', '传统美术', '传统体育'],
    answer: 0,
    explain: '普洱茶制作技艺属于传统技艺类非遗。'
  }
];

const current = ref(0);
const selected = ref(null);
const score = ref(0);
const showResult = ref(false);
const showExplain = ref(false);

function selectOption(idx) {
  if (selected.value !== null) return;
  selected.value = idx;
  if (idx === questions[current.value].answer) {
    score.value++;
    Message.success('回答正确！');
  } else {
    Message.error('回答错误！');
  }
  showExplain.value = true;
}
function nextQuestion() {
  if (current.value < questions.length - 1) {
    current.value++;
    selected.value = null;
    showExplain.value = false;
  } else {
    showResult.value = true;
  }
}
function restart() {
  current.value = 0;
  selected.value = null;
  score.value = 0;
  showResult.value = false;
  showExplain.value = false;
}
</script>

<template>
  <div class="quiz-container">
    <div class="quiz-header">
      <h1>非遗知识答题</h1>
      <p>答对更多云南非遗知识题目，赢取"非遗守护者"称号！</p>
    </div>
    <div v-if="!showResult" class="quiz-question-box">
      <div class="quiz-question">{{ questions[current].question }}</div>
      <div class="quiz-options">
        <div
          v-for="(opt, idx) in questions[current].options"
          :key="idx"
          class="quiz-option"
          :class="{
            selected: selected === idx,
            correct: selected !== null && idx === questions[current].answer,
            wrong: selected === idx && idx !== questions[current].answer
          }"
          @click="selectOption(idx)"
        >
          {{ opt }}
        </div>
      </div>
      <div v-if="showExplain" class="quiz-explain">
        {{ questions[current].explain }}
      </div>
      <div class="quiz-actions">
        <a-button type="primary" :disabled="selected === null" @click="nextQuestion">
          {{ current === questions.length - 1 ? '提交并查看成绩' : '下一题' }}
        </a-button>
      </div>
      <div class="quiz-progress">题目进度：{{ current + 1 }}/{{ questions.length }}</div>
    </div>
    <div v-else class="quiz-result-box">
      <div class="quiz-result-title">🎉 答题完成！</div>
      <div class="quiz-score">你的得分：<span>{{ score }}</span> / {{ questions.length }}</div>
      <div class="quiz-eval">
        <span v-if="score === questions.length">非遗守护者！满分！</span>
        <span v-else-if="score >= questions.length - 1">很棒，继续加油！</span>
        <span v-else>继续努力，了解更多非遗知识吧！</span>
      </div>
      <a-button type="primary" @click="restart">重新挑战</a-button>
    </div>
  </div>
</template>

<style scoped>
.quiz-container {
  min-height: 100vh;
  background: #f7f3ea;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
}
.quiz-header {
  text-align: center;
  margin-bottom: 32px;
}
.quiz-header h1 {
  font-size: 36px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.quiz-header p {
  color: #666;
  font-size: 18px;
  margin-bottom: 18px;
}
.quiz-question-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  padding: 36px 32px 28px 32px;
  min-width: 380px;
  max-width: 98vw;
  margin-bottom: 24px;
}
.quiz-question {
  font-size: 22px;
  color: #8b1f1f;
  margin-bottom: 22px;
  font-weight: bold;
}
.quiz-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 18px;
}
.quiz-option {
  background: #fdf6e3;
  border: 1.5px solid #e6c9a8;
  border-radius: 8px;
  padding: 12px 18px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.quiz-option.selected {
  border-color: #C2101C;
  background: #ffe6e6;
}
.quiz-option.correct {
  border-color: #4caf50;
  background: #e8f5e9;
  color: #388e3c;
}
.quiz-option.wrong {
  border-color: #e53935;
  background: #ffebee;
  color: #b71c1c;
}
.quiz-explain {
  color: #C2101C;
  font-size: 16px;
  margin-bottom: 12px;
  margin-top: 4px;
}
.quiz-actions {
  text-align: right;
  margin-bottom: 8px;
}
.quiz-progress {
  color: #888;
  font-size: 15px;
  text-align: right;
}
.quiz-result-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  padding: 40px 32px 32px 32px;
  min-width: 380px;
  max-width: 98vw;
  text-align: center;
}
.quiz-result-title {
  font-size: 26px;
  color: #C2101C;
  margin-bottom: 18px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
}
.quiz-score {
  font-size: 22px;
  color: #8b1f1f;
  margin-bottom: 12px;
}
.quiz-score span {
  color: #C2101C;
  font-weight: bold;
  font-size: 28px;
}
.quiz-eval {
  font-size: 18px;
  color: #666;
  margin-bottom: 22px;
}
@media (max-width: 600px) {
  .quiz-question-box, .quiz-result-box {
    min-width: 0;
    padding: 18px 6px 12px 6px;
  }
}
</style> 