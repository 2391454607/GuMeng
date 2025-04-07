import { createApp } from 'vue'
import App from '@/App.vue'
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import { createPinia } from 'pinia';  // 引入 Pinia
import {router} from "@/router/index.js";
import '@arco-design/web-vue/dist/arco.css';
import {StCommonUiVue3} from "st-common-ui-vue3"
import 'st-common-ui-vue3/es/st-common-ui-vue3.css'
//创建 Vue 应用实例
const app = createApp(App)

app.use(router)
//注册 UI 组件
app.use(ArcoVue)
app.use(ArcoVueIcon);
app.use(createPinia()); // 使用 Pinia
app.use(StCommonUiVue3)
//将应用挂载到 DOM 中
app.mount('#app')
