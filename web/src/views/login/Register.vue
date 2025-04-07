<script setup>
import { reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { useRouter } from 'vue-router';
import { userRegisterAPI } from "@/api/Login.js";

const router = useRouter();
const loading = ref(false);

const register = reactive({
  username: '',
  password: '',
  rememberMe: false,
});

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 5, message: '用户名至少5个字符' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少6个字符' }
  ]
};

const handleSubmit = async () => {
  loading.value = true;

  console.log(register);
  if (!register.username || !register.password) {
    Message.error("用户名或密码不能为空！")
    loading.value = false;
    return
  }
  if (register.username.length >= 5 && register.password.length >= 6) {
    try {
      userRegisterAPI(register).then((res) => {
        if (res.code === 200) {
          Message.success(res.msg);
          router.push('/');
        } else {
          Message.error(res.msg);
        }
      })
    }catch (error) {
      Message.error(error);
    }finally {
      loading.value = false;
    }
  }
  Message.error("请输入合法的参数！")
  loading.value = false;
};

</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="register-title">欢迎注册</h2>
      <a-form
          :model="register"
          :rules="rules"
          @submit="handleSubmit"
          layout="vertical"
      >
        <a-form-item field="username" label="用户名">
          <a-input
              v-model="register.username"
              placeholder="请输入用户名"
              :max-length="20"
              allow-clear
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="password" label="密码">
          <a-input-password
              v-model="register.password"
              placeholder="请输入密码"
              allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>


        <a-form-item>
          <a-button type="primary" html-type="submit" long :loading="loading">
            注册
          </a-button>
        </a-form-item>

        <div class="login-link">
          已有账号 <a-link href="/login">返回登录</a-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}


.login-link {
  text-align: center;
  margin-top: 16px;
}
</style>