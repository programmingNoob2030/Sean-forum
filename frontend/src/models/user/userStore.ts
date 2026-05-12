import { defineStore } from 'pinia'
import { ref, computed } from 'vue' // 像组件一样引入 ref 和 computed
import type { User, LoginVO, UpdateInfoDTO} from '@/models/user/userTypes'


export const useUserStore = defineStore('user', () => {
  // --- 1. State (用 ref 定义) ---
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(
    JSON.parse(localStorage.getItem('user_info') || 'null')
  )

  // --- 2. Getters (用 computed 定义) ---
  const isLoggedIn = computed(() => !!token.value)

  // --- 3. Actions (直接写 function) ---
  function handleLogin(loginData: LoginVO) {
    const {token : receivedToken, ...UserInfoPart} = loginData
    // 更新响应式数据 (注意要写 .value)
    token.value = receivedToken
    userInfo.value = UserInfoPart as User

    // 同步到本地
    localStorage.setItem('token', token.value)
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
  }

  function handleLogout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user_info')
  }
  function setUserInfo(data: UpdateInfoDTO){
    if (userInfo.value) {
      // This is a "shallow merge" - very clean!
      Object.assign(userInfo.value, data);
      localStorage.setItem('user_info', JSON.stringify(userInfo.value))

      
      // If you need to persist it to LocalStorage, do it here:
      // localStorage.setItem('user_info', JSON.stringify(userInfo.value));
    }
  }

  // 最后一定要记得把所有的变量和方法 return 出来！
  return {
    token,
    userInfo,
    isLoggedIn,
    handleLogin,
    handleLogout,
    setUserInfo
  }
})