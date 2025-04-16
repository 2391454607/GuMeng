<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Message} from '@arco-design/web-vue';
import {useRouter} from 'vue-router';
import {SendEmailApi, userRegisterAPI} from "@/api/Auth.js";
//引入3D文本组件
import {St3DText, St3DTiltContainer, StDynamicBorder1, StGhostText,} from "st-common-ui-vue3";

const img = ref(72) // 边数

const getDivStyle = (i, {start, end}) => {
  let imageIndex = start + Math.floor(i % (end - start + 1))
  return {
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%',
    transform: `rotateX(${(360 / img.value) * i}deg) translateZ(3430px)`,
    background: `url(/src/assets/background/${imageIndex}.jpg) no-repeat`,
    backgroundSize: 'cover'
  }
}

const getImageRange = (sectionIndex) => {
  const start = 1 + (sectionIndex * 3)
  const end = 3 + (sectionIndex * 3)
  return {start, end}
}

const router = useRouter();
const loading = ref(false);

const register = reactive({
  username: '',
  password: '',
  confirmPassword :'',
  email:'',
  code:'',
  rememberMe: false,
});


const rules = {
  username: [
    {required: true, message: '请输入用户名'},
    {minLength: 5, message: '用户名至少5个字符'},
  ],
  password: [
    {required: true, message: '请输入密码'},
    {minLength: 6, message: '密码至少6个字符'}
  ],
  confirmPassword: [
    {required: true, message: '请确认密码'},
    {
      validator: (value, callback) => {
        if (value === '') {
          callback('请确认密码');
        } else if (value !== register.password) {
          callback('两次输入的密码不一致');
        } else {
          callback();
        }
      }
    }
  ],
  email: [
    {required: true, message: '输入绑定邮箱'},
    {
      match: /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/,
      message: '请输入有效的邮箱地址'
    }
  ],
  code: [
    {required: true, message: '输入验证码'},
    {minLength: 6, message: '输入六位数验证码'}
  ]
};
const emailPattern = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/;

const handleSubmit = async () => {
  loading.value = true;

  if (!register.username || !register.password || !register.confirmPassword) {
    loading.value = false;
    return;
  }

  if (register.password !== register.confirmPassword) {
    loading.value = false;
    return;
  }

  if (!emailPattern.test(register.email)) {
    loading.value = false;
    return;
  }

  if (!register.code) {
    loading.value = false;
    return;
  }

  if (register.username.length >= 5 && register.password.length >= 6) {
    try {
      const res = await userRegisterAPI(register);
      if (res.code === 200) {
        Message.success(res.msg);
        await router.push('/login');
      } else {
        Message.error(res.msg);
      }
    } catch (error) {
      Message.error(error);
    }
  }
  loading.value = false;
};

// 添加倒计时相关的响应式变量
const countdown = ref(60);
const isCountingDown = ref(false);

// 添加发送验证码的方法
const sendCode = async () => {
  if (isCountingDown.value) return;
  // 点击发送开始倒计时
  isCountingDown.value = true;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      isCountingDown.value = false;
      countdown.value = 60;
    }
  }, 1000);
  // 验证邮箱格式
  if (!register.email) {
    isCountingDown.value = false;
    Message.error("邮箱不能为空！")
    return;
  }
  if (!emailPattern.test(register.email)) {
    isCountingDown.value = false;
    Message.error("请输入有效的的邮箱格式！")
    return;
  }
  try {
    const res = await SendEmailApi(register);
    if (res.code === 200) {
      Message.success(res.msg);
    } else {
      Message.error(res.msg);
    }
  } catch (error) {
    Message.error('发送验证码失败，请稍后重试');
  }
};
</script>

<template>

  <div class="register-container">
    <div class="blur-overlay"></div>
    <div id="container">
      <section v-for="(_, index) in 3" :key="index">
        <div v-for="i in img" :key="i"
             :style="getDivStyle(i - 1, getImageRange(index))">
        </div>
      </section>
    </div>

    <div class="St3DTiltContainer">
      <St3DTiltContainer :start-x="10" :start-y="20" full-page-listening>
        <St3DText base-color="#59B6EB" color="#fefefe" content="故梦阑珊" font-size="5rem"></St3DText>
      </St3DTiltContainer>
    </div>

    <div class="dynamic-border-1-demo-1">
      <StDynamicBorder1 :border-radius="8" background-color="white">
        <div class="register-box">
          <div class="register-title">
            <st-ghost-text
                content="欢迎注册"
                horizontal-align="center"
                vertical-align="center"
                :speed="600"
                :delay="60"
            />
          </div>
          <a-form
              :model="register"
              :rules="rules"
              layout="vertical"
              @submit="handleSubmit"
          >
            <a-form-item field="username" label="用户名" validate-trigger="input">
              <a-input
                  v-model="register.username"
                  :max-length="20"
                  placeholder="请输入用户名"
              >
                <template #prefix>
                  <icon-user/>
                </template>
              </a-input>
            </a-form-item>

            <a-form-item field="password" label="密码" validate-trigger="input">
              <a-input-password
                  v-model="register.password"
                  placeholder="请输入密码"
              >
                <template #prefix>
                  <icon-lock/>
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item field="confirmPassword" label="确认密码" validate-trigger="input">
              <a-input-password
                  v-model="register.confirmPassword"
                  placeholder="请确认密码"
              >
                <template #prefix>
                  <icon-lock/>
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item field="email" label="邮箱" validate-trigger="input">
              <a-input
                  v-model="register.email"
                  placeholder="输入绑定邮箱"
              >
                <template #prefix>
                  <icon-email/>
                </template>
              </a-input>
            </a-form-item>

            <a-form-item field="code" label="验证码" class="code-form-item" validate-trigger="input">
              <div class="code-input-group">
                <a-input
                    v-model="register.code"
                    :max-length="6"
                    placeholder="输入邮箱验证码"
                >
                  <template #prefix>
                    <icon-safe/>
                  </template>
                </a-input>
                <a-button 
                    type="primary" 
                    :disabled="isCountingDown"
                    @click="sendCode"
                >
                  {{ isCountingDown ? `${countdown}秒后重新发送` : '发送验证码' }}
                </a-button>
              </div>
            </a-form-item>

            <div class="register-button">
              <a-form-item>
                <a-button :loading="loading" html-type="submit" long type="primary">
                  注册
                </a-button>
              </a-form-item>
              <div class="register-link">
                已有账号
                <a-link href="/login">返回登录</a-link>
              </div>
            </div>
          </a-form>
        </div>
      </StDynamicBorder1>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.blur-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  background: rgba(255, 255, 255, 0.1);
  z-index: 2;
  pointer-events: none;
}

#container {
  z-index: 1;
}

.dynamic-border-1-demo-1 {
  position: relative;
  z-index: 3;
}

.St3DTiltContainer {
  z-index: 3;
}

.dynamic-border-1-demo-1 {
  margin-top: 1rem;
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 1px;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.register-options {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.register-button{
  margin-top: 20px;
}

.register-link {
  text-align: center;
  margin-top: 16px;
}


* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#container {
  height: 100vh;
  width: 100vw;
  display: flex;
  position: absolute;
  justify-content: center;
  overflow: hidden;
}

section {
  width: 600px;
  height: 300px;
  position: relative;
  transform-style: preserve-3d;
  animation: rotate 800s linear infinite;
}

section:nth-child(2) {
  animation: rotate2 800s linear infinite;
}

@keyframes rotate {
  0% {
    transform: rotateX(0deg);
  }
  100% {
    transform: rotateX(360deg);
  }
}

@keyframes rotate2 {
  0% {
    transform: rotateX(0deg);
  }
  100% {
    transform: rotateX(-360deg);
  }
}

:deep(.arco-form-item-layout-vertical > .arco-form-item-label-col) {
  justify-content: flex-start;
  margin-top: 5px;
  margin-bottom: 0;
  padding: 0;
  line-height: 1.5715;
  white-space: normal;
}

:deep(.arco-input-wrapper .arco-input-prefix) {
  padding-right: 0;
  color: var(--color-text-2);
}

/* 添加图标居中样式 */
:deep(.arco-input-wrapper) {
  display: flex;
  align-items: center;
  height: 36px;
  border-radius: 8px;  /* 添加圆角 */
  transition: all 0.3s;  /* 添加过渡效果 */
}

:deep(.arco-input-wrapper:hover) {
  border-color: rgb(var(--primary-6));
  box-shadow: 0 0 0 2px rgba(var(--primary-6), 0.1);
}

:deep(.arco-btn) {
  border-radius: 8px;  /* 按钮也添加圆角 */
  transition: all 0.3s;
}

:deep(.arco-btn:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--primary-6), 0.2);
}

:deep(.arco-input-prefix) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;  /* 设置固定宽度 */
  height: 100%;
}

:deep(.arco-icon) {
  font-size: 16px;
  color: rgb(var(--gray-6));
}

:deep(.arco-input-wrapper .arco-input.arco-input-size-medium) {
  width: 82%;
}
:deep(.arco-input-wrapper .arco-input-suffix) {
  padding-left: 0;
  color: var(--color-text-2);
}

.code-input-group {
  display: flex;
  gap: 12px;  /* 设置间距 */
  align-items: center;
}

.code-input-group :deep(.arco-input-wrapper) {
  flex: 1;  /* 输入框占据剩余空间 */
}

.code-input-group .arco-btn {
  width: 150px;  /* 固定按钮宽度 */
  white-space: nowrap;
}
</style>
