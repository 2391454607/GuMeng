import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'url';
import { URL } from 'node:url';

export default defineConfig({
  plugins: [vue()],

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