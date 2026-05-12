<template>
  <div class="login-container">
    <div class="login-box">
      <h3 class="title">新用户注册</h3>
      <el-form :model="regForm" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="regForm.name" type="username" placeholder="设置您的用户名 " />
        </el-form-item>
        <el-form-item label="设置密码">
          <el-input v-model="regForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="设置邮箱">
          <el-input v-model="regForm.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-button type="success" @click="handleRegister" class="submit-btn">
          完成注册
        </el-button>
        <div class="form-footer" style="justify-content: center;">
          <el-link type="primary" :underline="false" @click="router.push('/login')">已有账号？返回登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiUserRegister } from '@/api/user'
import type { RegisterDTO } from '@/models/user/userTypes'

const router = useRouter()
const confirmPassword = ref('')
const regForm = ref<RegisterDTO>({
  name: '',
  password: '',
  email:''
})

const handleRegister = async () => {
  if (!regForm.value.name || !regForm.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (regForm.value.password !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  // 校验用户名长度
  if (regForm.value.name.length < 1 || regForm.value.name.length > 20) {
    ElMessage.warning('用户名长度必须在1到20个字符之间');
    return;
  }

  // 校验邮箱格式
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailReg.test(regForm.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式');
    return;
  }

  // 校验密码正则 (直接搬你的 Java 正则，稍微改下反斜杠)
  const passwordReg = /^(?=.*[0-9])(?=.*[a-zA-Z])[^\s\u4e00-\u9fa5]{8,14}$/;
  if (!passwordReg.test(regForm.value.password)) {
    ElMessage.warning('密码必须包含字母和数字，且不能有空格或中文(8-14位)');
    return;
  }
  try {
    await apiUserRegister(regForm.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
  }
}
</script>

<style scoped>
/* 复用登录页样式 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
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
}
.submit-btn {
  width: 100%;
  margin-top: 10px;
}
.form-footer {
  margin-top: 20px;
}
</style>