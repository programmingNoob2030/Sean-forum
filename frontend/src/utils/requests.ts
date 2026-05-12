import { useUserStore } from '@/models/user/userStore'
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 axios 实例 (相当于配置 Spring 的 RestTemplate Bean)
const service = axios.create({ 
  baseURL: '/api', // 👈 关键！这会让所有 apiCreatePost 等请求自动带上 /api
  timeout: 5000 // 超过 5 秒没反应就报错，别让战神等太久
})

// 2. 请求拦截器 (相当于后端的 Filter/Interceptor)
service.interceptors.request.use(
  
  config => {
    console.log("准备检验身份")
    const userStore = useUserStore()
    // 以后如果你搞了登录，可以在这里统一加 Header
    config.headers['Authorization'] = `Bearer ${userStore.token}`

      // 战神打印：把整个对象转成 JSON，防止引用偏移
    console.log('--- 最终外发配置 ---');
    console.dir(JSON.parse(JSON.stringify(config.headers)));
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    // response.data 才是后端的 Result { code, msg, data }
    const { code, msg, data } = response.data;

    // 逻辑判断：只有 200 才放行 data
    if (code === 200) {
      return data; // 👈 此时返回的才是 PageResult 或者 PostVO
    } else {
      // 业务报错（比如余额不足、帖子不存在）
      ElMessage.error(`${msg}`); 
      return Promise.reject(new Error(msg));
    }
  },
  error => {
    // 网络层报错（404, 500, 网络断开）
    return Promise.reject(error);
  }
)

export default service