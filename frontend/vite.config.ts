// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 报错就 npm install @types/node -D

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 以后你就可以直接写 import '@/models/post' 了
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080', // 试试换成 IP，有时候 localhost 解析有延迟
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})