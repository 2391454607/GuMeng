<script setup>
import { reactive, computed, ref } from 'vue';

import { UserOutlined, LockOutlined } from "@ant-design/icons-vue";

const loginType = ref('user'); // 默认用户登录

const formState = reactive({
  username: '',
  password: '',
  remember: true,
});
const onFinish = values => {
  if (loginType.value === 'admin') {
    console.log('管理员登录:', values);
    // 这里处理管理员登录逻辑
  } else {
    console.log('用户登录:', values);
    // 这里处理用户登录逻辑
  }
};
const onFinishFailed = errorInfo => {
  console.log('Failed:', errorInfo);
};
const disabled = computed(() => {
  return !(formState.username && formState.password);
});
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-type-switch">
        <a-radio-group v-model:value="loginType" button-style="solid">
          <a-radio-button value="user">用户登录</a-radio-button>
          <a-radio-button value="admin">管理员登录</a-radio-button>
        </a-radio-group>
      </div>
      
      <h2 class="login-title">{{ loginType === 'admin' ? '管理员登录' : '用户登录' }}</h2>
      <a-form
        :model="formState"
        name="normal_login"
        class="login-form"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名！' }]"
        >
          <a-input 
            v-model:value="formState.username" 
            size="large" 
            :placeholder="loginType === 'admin' ? '管理员账号' : '用户名'"
          >
            <template #prefix>
              <UserOutlined class="form-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码！' }]"
        >
          <a-input-password v-model:value="formState.password" size="large" placeholder="密码">
            <template #prefix>
              <LockOutlined class="form-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <div class="remember-forgot">
            <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
            <a class="forgot-link" href="">忘记密码？</a>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button 
            :disabled="disabled"
            :type="'primary'"
            html-type="submit" 
            class="login-button"
            size="large"
          >
            {{ loginType === 'admin' ? '管理员登录' : '用户登录' }}
          </a-button>
          <div class="register-link" v-if="loginType === 'user'">
            还没有账号？<a href="">立即注册</a>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}

.login-box {
  width: 400px;
  height: 500px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.login-title {
  font-size: 28px;
  color: #1890ff;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 600;
}

.login-form {
  width: 100%;
}

.form-icon {
  color: #bfbfbf;
  font-size: 16px;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-link {
  color: #1890ff;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #40a9ff;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-bottom: 16px;
}

.register-link {
  text-align: center;
  color: #595959;
}

.register-link a {
  color: #1890ff;
  margin-left: 4px;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #40a9ff;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 6px;
}

:deep(.ant-btn) {
  border-radius: 6px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

.login-type-switch {
  text-align: center;
  margin-bottom: 24px;
}




</style>