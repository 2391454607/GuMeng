<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { Message, Spin } from '@arco-design/web-vue';
import { useRoute } from 'vue-router';


const apiKey = 'sk-798948f93254417192030b65e13e3663';

// 答题相关状态
const currentQuestion = ref(0);
const selectedAnswer = ref(null);
const score = ref(0);
const isLoading = ref(false);
const showExplanation = ref(false);
const questions = ref([]);
const totalQuestions = ref(5);
const loadError = ref(false);

// 大模型相关参数
const model = ref('gpt-3.5-turbo');
const topic = '云南非遗';
const difficulty = '初级';

// mock数据（本地测试用）
const mockQuestions = [
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

const loadingProgress = ref(0); // 加载进度
const loadingMessage = ref(''); // 加载提示
const testResults = ref([]); // 测试记录
const showTestResults = ref(false);

// 新增：题量、难度、限时、无尽模式相关状态
const countOptions = [5, 10, 20, 50];
const selectedCount = ref(6);
const difficultyOptions = [
  { label: '简单', value: '简单', count: 6 },
  { label: '中级', value: '中级', count: 10 },
  { label: '高级', value: '高级', count: 14 }
];
const selectedDifficulty = ref('简单');
const timePerQuestion = ref(30); // 每题30秒
const timeLeft = ref(timePerQuestion.value);
let timer = null;
const endlessMode = ref(false);
const endlessScore = ref(0);
const endlessBest = ref(Number(localStorage.getItem('endlessBest') || 0));
const quizStarted = ref(false);

const route = useRoute();

const questionPool = ref([]); // 当前已获取的题目池
const currentIndex = ref(0);  // 当前题目在池中的索引

const isPreloading = ref(false);

// 音效相关
const correctAudio = ref(null);
const consecutiveCorrectAudio = ref(null);
const errorAudio = ref(null);
let consecutiveCorrectCount = 0; // 连续答对计数器

const correctAudioPath = '/music/答题正确音.mp3';
const consecutiveCorrectAudioPath = '/music/连续正确.mp3';
const errorAudioPath = '/music/操作失误音.mp3';

// 监听难度变化自动调整题量
watch(selectedDifficulty, (val) => {
  const opt = difficultyOptions.find(d => d.value === val);
  selectedCount.value = opt ? opt.count : 6;
});

onMounted(() => {
  // 只读取difficulty参数
  const difficulty = route.query.difficulty || '简单';
  selectedDifficulty.value = difficulty;
  const opt = difficultyOptions.find(d => d.value === selectedDifficulty.value);
  selectedCount.value = opt ? opt.count : 6;
  quizStarted.value = true;
  startQuiz();
});

// 获取系统提示词
const getSystemPrompt = (count = selectedCount.value, difficulty = selectedDifficulty.value) => {
  return `你是一个非遗知识出题机器人，请严格只返回如下格式的JSON，不要有任何解释或多余内容：\n@[{\n  "question": "题目内容",\n  "options": ["A", "B", "C", "D"],\n  "answer": 0,\n  "explain": "解析"\n}]@\n请生成${count}道云南非遗相关的${difficulty}单选题。`;
};

// 批量请求题目，append=true时拼接到池尾
const fetchQuestionsBatch = async (batchCount = 2, append = false, difficulty = selectedDifficulty.value) => {
  if (!append) {
    isLoading.value = true;
  }
  loadingProgress.value = 10;
  loadingMessage.value = '正在生成题目...';
  let progressInterval = null;
  if (!append) {
    progressInterval = setInterval(() => {
      if (loadingProgress.value < 90) {
        loadingProgress.value += 5;
        if (loadingProgress.value < 30) {
          loadingMessage.value = '正在理解出题需求...';
        } else if (loadingProgress.value < 60) {
          loadingMessage.value = '正在构思题目内容...';
        } else {
          loadingMessage.value = '正在精心生成中...';
        }
      }
    }, 400);
  }
  try {
    // 最多拼到selectedCount题
    let maxBatch = batchCount;
    const remain = selectedCount.value - questionPool.value.length;
    if (remain <= 0) return;
    maxBatch = Math.min(batchCount, remain);
    const prompt = getSystemPrompt(maxBatch, difficulty);
    const apiCallPromise = fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: 'qwen-plus',
        messages: [
          { role: 'system', content: prompt },
          { role: 'user', content: '请生成题目' }
        ],
        temperature: 0.7
      })
    }).then(res => res.json());
    const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject(new Error('请求超时，请稍后再试')), 15000));
    const data = await Promise.race([apiCallPromise, timeoutPromise]);
    const response = data.choices?.[0]?.message?.content || '';
    let match = response.match(/@\[(.*)\]@/s);
    let batchQuestions = [];
    if (match) {
      try {
        batchQuestions = JSON.parse('[' + match[1] + ']');
      } catch (e) {
        const arrMatch = response.match(/\[\s*{[\s\S]*?}\s*\]/);
        if (arrMatch) {
          batchQuestions = JSON.parse(arrMatch[0]);
        } else {
          throw new Error('未能解析出题目数组');
        }
      }
    } else {
      const arrMatch = response.match(/\[\s*{[\s\S]*?}\s*\]/);
      if (arrMatch) {
        batchQuestions = JSON.parse(arrMatch[0]);
      } else {
        throw new Error('模型返回格式异常');
      }
    }
    if (append) {
      const remain = selectedCount.value - questionPool.value.length;
      if (remain > 0) {
        questionPool.value = questionPool.value.concat(batchQuestions.slice(0, remain));
      }
    } else {
      questionPool.value = batchQuestions;
      currentIndex.value = 0;
    }
    saveTestResult({
      input: '大模型生成',
      output: batchQuestions,
      timestamp: new Date().toISOString(),
      response: 'batched'
    });
  } catch (error) {
    if (!append) {
      Message.error('题目加载失败或暂无题目，请稍后重试。');
      questionPool.value = mockQuestions;
      currentIndex.value = 0;
    }
    loadError.value = true;
    saveTestResult({
      input: '本地题库',
      output: mockQuestions,
      timestamp: new Date().toISOString(),
      response: error.message
    });
  } finally {
    if (!append && progressInterval) clearInterval(progressInterval);
    if (!append) {
      isLoading.value = false;
      loadingProgress.value = 100;
      loadingMessage.value = '完成！';
      setTimeout(() => {
        loadingProgress.value = 0;
        loadingMessage.value = '';
      }, 500);
    }
  }
  checkQuestionPool();
};

// 保存测试记录到本地
const saveTestResult = (result) => {
  try {
    const existing = JSON.parse(localStorage.getItem('quizTestResults') || '[]');
    existing.push(result);
    const trimmed = existing.slice(-50);
    localStorage.setItem('quizTestResults', JSON.stringify(trimmed));
    testResults.value = trimmed;
  } catch (e) { console.error('保存测试记录失败', e); }
};

// 获取测试记录
const getTestResults = () => {
  try {
    return JSON.parse(localStorage.getItem('quizTestResults') || '[]');
  } catch (e) { return []; }
};

// 导出测试记录为CSV
const exportTestResults = () => {
  try {
    if (!testResults.value.length) {
      Message.info('没有测试数据可导出');
      return;
    }
    let csv = 'timestamp,input,output,response\n';
    testResults.value.forEach(r => {
      csv += [r.timestamp, r.input, '"' + JSON.stringify(r.output).replace(/"/g, '""') + '"', '"' + (r.response || '').replace(/"/g, '""') + '"'].join(',') + '\n';
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `quiz_test_results_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
  } catch (e) {
    Message.error('导出失败: ' + e.message);
  }
};

// 启动普通模式
const startQuiz = async () => {
  endlessMode.value = false;
  quizStarted.value = true;
  score.value = 0;
  showExplanation.value = false;
  questionPool.value = [];
  await fetchQuestionsBatch(2, false, selectedDifficulty.value);
  // 后台预取剩余题
  while (questionPool.value.length < selectedCount.value) {
    await fetchQuestionsBatch(2, true, selectedDifficulty.value);
  }
  startTimer();
};
// 启动无尽模式
const startEndless = async () => {
  endlessMode.value = true;
  quizStarted.value = true;
  endlessScore.value = 0;
  showExplanation.value = false;
  await fetchQuestionsBatch(1, false, selectedDifficulty.value);
  startTimer();
  checkQuestionPool();
};

// 新增：限时功能
const startTimer = () => {
  timeLeft.value = timePerQuestion.value;
  clearInterval(timer);
  timer = setInterval(() => {
    timeLeft.value--;
    if (timeLeft.value <= 0) {
      clearInterval(timer);
      handleTimeout();
    }
  }, 1000);
};
const stopTimer = () => { clearInterval(timer); };
const handleTimeout = () => {
  // Message.error('超时！');
  if (endlessMode.value) {
    endEndless();
  } else {
    showExplanation.value = true;
    nextQuestionHandler();
  }
};

// 新增：无尽之战结束
const endEndless = () => {
  quizStarted.value = false;
  endlessBest.value = Math.max(endlessBest.value, endlessScore.value);
  localStorage.setItem('endlessBest', endlessBest.value);
  Message.info(`无尽之战结束，连对${endlessScore.value}题，历史最佳${endlessBest.value}题！`);
};

// 新增：播放音效函数
const playAudio = (audioRef) => {
  if (audioRef.value) {
    audioRef.value.currentTime = 0; // 重置到开始
    audioRef.value.play().catch(e => console.warn("Audio play failed:", e));
  }
};

// 修改答题流程，支持动态预取
const selectAnswerHandler = (index) => {
  if (selectedAnswer.value !== null) return;
  stopTimer();
  selectedAnswer.value = index;
  const curQ = questionPool.value[currentIndex.value];
  if (index === curQ.answer) {
    playAudio(correctAudio);
    consecutiveCorrectCount++;
    if (consecutiveCorrectCount === 2) {
      playAudio(consecutiveCorrectAudio);
      consecutiveCorrectCount = 0; // 连续播放两次后重置计数
    }
    if (endlessMode.value) {
      endlessScore.value++;
      Message.success('回答正确！');
      setTimeout(async () => {
        await fetchQuestionsBatch(1, false, selectedDifficulty.value);
        selectedAnswer.value = null;
        showExplanation.value = false;
        startTimer();
      }, 800);
      return;
    } else {
      score.value++;
      Message.success('回答正确！');
    }
  } else {
    playAudio(errorAudio);
    consecutiveCorrectCount = 0; // 答错重置计数
    if (endlessMode.value) {
      endEndless();
      return;
    } else {
      Message.error('回答错误！');
    }
  }
  showExplanation.value = true;
};

const nextQuestionHandler = () => {
  stopTimer();
  if (currentIndex.value + 1 < selectedCount.value) {
    currentIndex.value++;
    selectedAnswer.value = null;
    showExplanation.value = false;
    startTimer();
  } else {
    Message.info(`最终得分：${score.value}/${selectedCount.value}`);
    quizStarted.value = false;
  }
};

const restartQuiz = () => {
  quizStarted.value = false;
  endlessMode.value = false;
  currentIndex.value = 0;
  selectedAnswer.value = null;
  score.value = 0;
  showExplanation.value = false;
  questionPool.value = [];
  if (endlessMode.value) {
    startEndless();
  } else {
    startQuiz();
  }
};

// 页面卸载时自动停止倒计时和重置状态
onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
  quizStarted.value = false;
  endlessMode.value = false;
});

// 题目池为空时兜底处理
function checkQuestionPool() {
  if (questionPool.value.length === 0) {
    stopTimer();
    quizStarted.value = false;
    Message.error('题目加载失败或暂无题目，请稍后重试。');
  }
}

// 新增：重试方法
const retryQuiz = () => {
  quizStarted.value = false;
  endlessMode.value = false;
  currentIndex.value = 0;
  selectedAnswer.value = null;
  score.value = 0;
  showExplanation.value = false;
  if (endlessMode.value) {
    startEndless();
  } else {
    startQuiz();
  }
};
</script>

<template>
  <div class="quiz-container">
    <audio ref="correctAudio" :src="correctAudioPath" preload="auto"></audio>
    <audio ref="consecutiveCorrectAudio" :src="consecutiveCorrectAudioPath" preload="auto"></audio>
    <audio ref="errorAudio" :src="errorAudioPath" preload="auto"></audio>
    <div v-if="isLoading" class="loading-bar-wrap">
      <div class="loading-bar">
        <div class="loading-progress" :style="{ width: loadingProgress + '%' }"></div>
      </div>
      <div class="loading-message">{{ loadingMessage }}</div>
      <Spin :size="32" style="margin: 30px 0"/>
    </div>
    <div v-else class="quiz-question-box">
      <h2 class="quiz-title">「非遗问对」文心解码竞赛系统</h2>
      <p class="quiz-subtitle">由大模型驱动的云南非遗知识挑战</p>
      <div v-if="questionPool.length === 0 && !isLoading" class="quiz-error">
        题目加载失败或暂无题目，请稍后重试。
        <a-button type="primary" @click="retryQuiz" style="margin-left: 16px;">重新生成</a-button>
      </div>
      <template v-else>
        <div class="question-item">
          <span class="question-number">
            第{{ currentIndex + 1 }}题 / {{ selectedCount }}题
          </span>
          <p class="question-text">{{ questionPool[currentIndex]?.question }}</p>
        </div>
        <div class="option-list">
          <div
            v-for="(option, idx) in questionPool[currentIndex]?.options"
            :key="idx"
            class="option-item"
            :class="{
              'is-selected': selectedAnswer === idx,
              'is-correct': selectedAnswer !== null && idx === questionPool[currentIndex].answer,
              'is-wrong': selectedAnswer === idx && idx !== questionPool[currentIndex].answer
            }"
            @click="selectAnswerHandler(idx)"
          >
            {{ option }}
          </div>
        </div>
        <div v-if="showExplanation" class="explanation">
          <p class="explanation-title">知识解析：</p>
          <span>{{ questionPool[currentIndex]?.explain }}</span>
        </div>
        <div class="button-group">
          <a-button
            type="primary"
            :disabled="selectedAnswer === null || isLoading"
            @click="nextQuestionHandler"
          >
            {{ currentIndex === selectedCount - 1 ? '查看成绩' : '下一题' }}
          </a-button>
          <a-button
            v-if="currentIndex === selectedCount - 1"
            type="secondary"
            @click="restartQuiz"
          >
            重新挑战
          </a-button>
          <a-button
            type="outline"
            @click="showTestResults = !showTestResults"
            style="margin-left: 12px;"
          >
            题目记录
          </a-button>
          <a-button
            v-if="showTestResults"
            type="outline"
            @click="exportTestResults"
            style="margin-left: 8px;"
          >
            导出CSV
          </a-button>
        </div>
        <!-- 测试记录弹窗 -->
        <div v-if="showTestResults" class="test-results-modal">
          <div class="test-results-content">
            <h3>测试记录</h3>
            <table class="test-results-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>来源</th>
                  <th>题目数</th>
                  <th>原始响应</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(r, idx) in testResults.slice().reverse()" :key="idx">
                  <td>{{ new Date(r.timestamp).toLocaleString() }}</td>
                  <td>{{ r.input }}</td>
                  <td>{{ Array.isArray(r.output) ? r.output.length : '' }}</td>
                  <td><span style="max-width:200px;display:inline-block;overflow:auto;">{{ r.response?.slice(0, 80) }}...</span></td>
                </tr>
              </tbody>
            </table>
            <a-button @click="showTestResults = false" style="margin-top: 16px;">关闭</a-button>
          </div>
        </div>
      </template>
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
.quiz-question-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  padding: 36px 32px 28px 32px;
  min-width: 380px;
  max-width: 98vw;
  margin: 0 auto;
}
.loading-bar-wrap {
  width: 400px;
  max-width: 90vw;
  margin: 60px auto 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(194,16,28,0.08);
  padding: 36px 32px 28px 32px;
  text-align: center;
}
.loading-bar {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}
.loading-progress {
  height: 100%;
  background-color: #C2101C;
  transition: width 0.3s ease;
  border-radius: 4px;
}
.loading-message {
  text-align: center;
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 12px;
}
.quiz-title {
  font-size: 28px;
  color: #C2101C;
  margin-bottom: 10px;
  font-family: 'FZKai-Z03', 'KaiTi', 'STKaiti', serif;
  text-align: center;
}
.quiz-subtitle {
  color: #666;
  font-size: 18px;
  margin-bottom: 18px;
  text-align: center;
}
.quiz-error {
  color: #b91c1c;
  font-size: 18px;
  text-align: center;
  margin: 40px 0;
}
.question-item {
  margin-bottom: 24px;
}
.question-number {
  display: block;
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}
.question-text {
  font-size: 20px;
  color: #333;
  line-height: 1.5;
}
.option-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.option-item {
  padding: 12px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f9fafb;
}
.option-item:hover {
  background: #f3f4f6;
}
.option-item.is-selected {
  border-color: #C2101C;
  background: #ffebe9;
}
.option-item.is-correct {
  border-color: #3b82f6;
  background: #e0f2fe;
  color: #1d4ed8;
}
.option-item.is-wrong {
  border-color: #dc2626;
  background: #fee2e2;
  color: #b91c1c;
}
.explanation {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  color: #666;
  font-size: 16px;
}
.button-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}
.test-results-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.test-results-content {
  background-color: #fff;
  border-radius: 12px;
  padding: 30px;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}
.test-results-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  margin-bottom: 16px;
}
.test-results-table th, .test-results-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
.test-results-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}
</style> 