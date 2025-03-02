import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { fileURLToPath } from 'url';
import {URL} from "node:url";

export default defineConfig({
  plugins: [vue()],

  /**
   * @description 处理'@'符号
   */
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});

