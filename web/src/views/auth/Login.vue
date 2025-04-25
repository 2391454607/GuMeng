<script setup>
import {onMounted, onBeforeUnmount, reactive, ref, computed} from 'vue';
import {Message} from '@arco-design/web-vue';
import {userLoginAPI} from "@/api/Auth.js";
import { IconCheck } from '@arco-design/web-vue/es/icon';

//引入组件
import Verify from "@/components/verifition/Verify.vue";

// 添加验证相关的响应式引用
const verify = ref(null);

// 添加验证状态
const isVerified = ref(false);
const verifyData = ref(null);

// 验证成功回调
const success = (params) => {
  console.log('验证成功:', params);
  isVerified.value = true;
  verifyData.value = params;
};

// 显示验证组件
const useVerify = () => {
  verify.value.show();
};

//引入3D文本组件
import {St3DText, St3DTiltContainer, StDynamicBorder1, StGhostText,} from "st-common-ui-vue3";

const img = ref(72) // 边数

const getDivStyle = (i, { start, end }) => {
  let imageIndex = start + Math.floor(i % (end - start + 1))
  return {
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%',
    transform: `rotateX(${(360 / img.value) * i}deg) translateZ(3430px)`,
    background: `url(/public/background/${imageIndex}.jpg) no-repeat`,
    backgroundSize: 'cover'
  }
}

const getImageRange = (sectionIndex) => {
  const start = 1 + (sectionIndex * 3)
  const end = 3 + (sectionIndex * 3)
  return { start, end }
}

const loading = ref(false);

const login = reactive({
  username: '',
  password: '',
  rememberMe: false,
});


const rules = {
  username: [
    {required: true, message: '请输入用户名'},
    {minLength: 5, message: '用户名至少5个字符'}
  ],
  password: [
    {required: true, message: '请输入密码'},
    {minLength: 6, message: '密码至少6个字符'}
  ]
};

// 添加状态控制变量
const show3DEffect = ref(true);

const canVerify = computed(() => {
  return login.username.length >= 5 && login.password.length >= 6;
});

const handleSubmit = async () => {

  loading.value = true;

  if (!login.username || !login.password) {
    Message.error("用户名或密码不能为空！")
    loading.value = false;
    return;
  }

  if (!isVerified.value) {
    Message.warning("请先完成验证");
    loading.value = false;
    return;
  }


  if (login.username.length >= 5 && login.password.length >= 6) {
    try {
      // 将验证数据添加到登录请求中
      const loginParams = {
        ...login,
        captchaVerification: verifyData.value.captchaVerification
      };
      
      const res = await userLoginAPI(loginParams);
      if (res.code === 200) {
        // 存储 token
        localStorage.setItem('token', res.data);
        
        // 解析 token 中的用户信息
        const tokenParts = res.data.split('.');
        const claims = JSON.parse(atob(tokenParts[1]));
        
        Message.success(res.msg);
        show3DEffect.value = false;
        
        setTimeout(() => {
          // 根据解析出的角色信息决定跳转路径
          if (claims.claims.role.includes('superAdmin') || claims.claims.role.includes('admin')) {
            window.location.href = '/sys';
          } else {
            window.location.href = '/';
          }
        }, 500);
      } else {
        Message.error(res.msg);
        // 重置验证状态
        isVerified.value = false;
        verifyData.value = null;
      }
    } catch (error) {
      Message.error("登录失败，请稍后重试");
      // 登录失败时重置验证状态
      isVerified.value = false;
      verifyData.value = null;
    }
  }
  loading.value = false;
};

// 添加 ref 用于存储组件实例
const tiltContainerRef = ref(null);

// 卸载前的清理函数
onBeforeUnmount(() => {
  try {
    // 移除所有事件监听器
    window.removeEventListener('mousemove', () => {});
    window.removeEventListener('deviceorientation', () => {});
    // 清除组件实例
    if (tiltContainerRef.value) {
      tiltContainerRef.value = null;
    }
  } catch (error) {
    console.warn('组件卸载清理失败:', error);
  }
});
</script>

<template>

  <div class="login-container">
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
        <div class="login-box">
          <div class="login-title">
            <st-ghost-text
                content="欢迎登录"
                horizontal-align="center"
                vertical-align="center"
                :speed="600"
                :delay="60"
            />
          </div>
          <a-form
              :model="login"
              :rules="rules"
              layout="vertical"
              @submit="handleSubmit"
          >
            <a-form-item field="username" label="用户名" validate-trigger="input">
              <a-input
                  v-model="login.username"
                  :max-length="20"
                  allow-clear
                  placeholder="请输入用户名"
              >
                <template #prefix>
                  <icon-user/>
                </template>
              </a-input>
            </a-form-item>

            <a-form-item field="password" label="密码" validate-trigger="input">
              <a-input-password
                  v-model="login.password"
                  allow-clear
                  placeholder="请输入密码"
              >
                <template #prefix>
                  <icon-lock/>
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item>
              <Verify
                  @success="success"
                  :mode="'pop'"
                  :captchaType="'blockPuzzle'"
                  :imgSize="{ width: '330px', height: '155px' }"
                  ref="verify"
              ></Verify>
              <a-button 
                  @click.prevent="useVerify" 
                  long
                  :disabled="isVerified || !canVerify"
                  type="outline"
                  class="verify-button"
                  :status="isVerified ? 'success' : 'normal'"
              >
                <template v-if="isVerified">
                  <icon-check /> 验证成功
                </template>
                <template v-else>
                  {{ canVerify ? '点击进行验证' : '请先输入用户名和密码' }}
                </template>
              </a-button>
            </a-form-item>

            <a-form-item>
              <div class="login-options">
                <a-checkbox v-model="login.rememberMe">记住我</a-checkbox>
                <a-link href="/forget">忘记密码？</a-link>
              </div>
            </a-form-item>

            <a-form-item>
              <a-button :loading="loading" html-type="submit" long type="primary">
                登录
              </a-button>
            </a-form-item>
            <div class="register-link">
              还没有账号？
              <a-link href="/register">立即注册</a-link>
            </div>
          </a-form>
        </div>
      </StDynamicBorder1>
    </div>
  </div>
</template>

<style scoped>
.login-container {
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

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 1px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.login-options {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

:deep(.arco-input-wrapper .arco-input-prefix) {
  padding-right: 0;
  color: var(--color-text-2);
}
/* 添加图标居中样式 */
:deep(.arco-input-wrapper) {
  display: flex;
  align-items: center;
  height: 36px;  /* 设置固定高度 */
  padding: 0 10px 0 0;
}

:deep(.arco-input-prefix) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;  /* 设置固定宽度 */
  height: 100%;
}

.verify-button {
  border-color: #165DFF;
  color: #165DFF;
}

.verify-button:not(:disabled):hover {
  background-color: rgba(22, 93, 255, 0.1);
}
</style>
