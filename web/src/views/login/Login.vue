<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Message} from '@arco-design/web-vue';
import {useRouter} from 'vue-router';
import {userLoginAPI} from "@/api/Login.js";
//引入3D文本组件
import {St3DText, St3DTiltContainer, StDynamicBorder1, StGhostText,} from "st-common-ui-vue3";


const router = useRouter();
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

const handleSubmit = async () => {
  loading.value = true;

  if (!login.username || !login.password) {
    Message.error("用户名或密码不能为空！")
    loading.value = false;
    return
  }
  if (login.username.length >= 5 && login.password.length >= 6) {
    try {
      userLoginAPI(login).then((res) => {
        if (res.code === 200) {
          Message.success(res.msg);
          router.push('/login');
        } else {
          Message.error(res.msg);
        }
      })
    } catch (error) {
      Message.error(error);
    } finally {
      loading.value = false;
    }
  }
  loading.value = false;
};

</script>

<template>

  <div class="login-container">
    
    <div>
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
            <a-form-item field="username" label="用户名">
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

            <a-form-item field="password" label="密码">
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
              <div class="login-options">
                <a-checkbox v-model="login.rememberMe">记住我</a-checkbox>
                <a-link>忘记密码？</a-link>
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

背景滚动效果
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f3f3f3;
}

#container {
  height: 100%;
  display: flex;
  justify-content: center;
  border-radius: 30px;
  overflow: hidden;
}

section {
  width: 450px;
  height: 300px;
  position: relative;
  transform-style: preserve-3d;
  animation: rotate 900s linear infinite;
}

section:nth-child(2) {
  animation: rotate2 900s linear infinite;
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
</style>
