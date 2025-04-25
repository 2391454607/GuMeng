import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'url';
import { URL } from 'node:url';
import Components from 'unplugin-vue-components/vite'
import { StCommonUIVue3Resolver} from "st-common-ui-vue3"

export default defineConfig({
  /**
   * 控制打包后所有资源的相对路径。
   */
  base: '/',

  plugins: [
      vue(),
    // 配置按需自动加载组件的 Vite 插件
    Components({
      // 配置解析器，用于帮助按需自动加载组件的 Vite 插件解析并按需加载组件
      resolvers: [
        StCommonUIVue3Resolver()
      ],
    })
  ],

  /**
   * @description 处理 '@' 符号
   */
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  /**
   * @description 配置开发服务器
   */
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true, // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, ''), // 重写路径，去掉 /api 前缀
      },
    },
  },
});