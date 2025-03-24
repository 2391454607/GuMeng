import { createApp } from 'vue'
import App from '@/App.vue'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import { createPinia } from 'pinia';  // 引入 Pinia
import {router} from "@/router/index.js";

//创建 Vue 应用实例
const app = createApp(App)

app.use(router)
//注册 Ant Design Vue组件
app.use(Antd)
app.use(createPinia()); // 使用 Pinia
//将应用挂载到 DOM 中
app.mount('#app')
