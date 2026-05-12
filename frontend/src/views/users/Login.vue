<template>
  <div class="login-container">
    <div class="login-box">
      <h3 class="title">用户登录</h3>
      <el-form :model="loginForm" label-position="top">
        <el-form-item label="账号">
          
          <el-input 
            v-model="loginForm.name" 
            placeholder="请输入账号" 
            :prefix-icon = 'User'
          >
        </el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            show-password 
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button type="primary" @click="doLogin" class="submit-btn">
          确认登录
        </el-button>

        <div class="form-footer">
          <el-link type="info" :underline="false" @click="router.push('/register')">立即注册</el-link>
          <el-link type="info" :underline="false" @click="router.push('/forget-password')">忘记密码？</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue' // 需要安装 @element-plus/icons-vue
import { apiUserLogin } from '@/api/user'
import type { LoginDTO } from '@/models/user/userTypes'
import { useUserStore } from '@/models/user/userStore'

const route = useRoute()
const router = useRouter()
const loginForm = ref<LoginDTO>({
  name: '',
  password: ''
})

const doLogin = async () => {
  if (!loginForm.value.name || !loginForm.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const userStore = useUserStore()
    const res = await apiUserLogin(loginForm.value)
    userStore.handleLogin(res)
    
    ElMessage.success('登录成功！')
    const redirectPath = route.query.redirect as string
    router.push(redirectPath || '/')
  } catch (error) {
    // 错误处理逻辑
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa; /* 浅灰色背景让页面更有层次感 */
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  height: 40px;
  font-size: 16px;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 14px;
}
</style>