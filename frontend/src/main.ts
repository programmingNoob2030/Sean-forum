// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 👈 确认这里引入了你写的 router 文件夹
import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'

const app = createApp(App)
const pinia = createPinia()
app.use(router) // 👈 这行是“点火”开关，必须在 mount 之前！
app.use(ElementPlus)
app.use(pinia)
app.mount('#app')