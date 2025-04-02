import { createApp } from 'vue'
import App from '@/App.vue'
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import { createPinia } from 'pinia';  // 引入 Pinia
import {router} from "@/router/index.js";
import '@arco-design/web-vue/dist/arco.css';

//创建 Vue 应用实例
const app = createApp(App)

app.use(router)
//注册 UI 组件
app.use(ArcoVue)
app.use(ArcoVueIcon);
app.use(createPinia()); // 使用 Pinia
//将应用挂载到 DOM 中
app.mount('#app')
