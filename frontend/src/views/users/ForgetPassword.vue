<template>
  <div class="login-container">
    <div class="login-box">
      <h3 class="title">找回密码</h3>
      
      <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 30px">
        <el-step title="验证身份" />
        <el-step title="重置密码" />
      </el-steps>

      <el-form :model="forgetForm" label-position="top">
        <div v-if="activeStep === 0">
          <el-form-item label="请输入您创建账号时的邮箱">
            <el-input v-model="forgetForm.email" placeholder="请输入您的账号或注册邮箱" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="captcha-row">
              <el-input v-model="forgetForm.eCode" placeholder="请输入验证码" />
              <el-button 
                :disabled="!!timer" 
                @click="sendCode"
                class="code-btn"
              >
                {{ timer ? `${countdown}s后获取` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-button type="primary" @click="checkCode" class="submit-btn" :disabled="!forgetForm.eCode">
            下一步
          </el-button>
        </div>

        <div v-else>
          <el-form-item label="新密码">
            <el-input v-model="forgetForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-button type="success" @click="handleReset" class="submit-btn">
            提交修改
          </el-button>
          <el-button @click="activeStep = 0" style="width: 100%; margin-top: 10px">
            返回上一步
          </el-button>
        </div>

        <div class="form-footer">
          <el-link type="primary" :underline="false" @click="router.push('/login')">想起密码了？去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiCheckEmailValid, apiCheckCodeValid, apiResetPassword} from '@/api/user'

const router = useRouter()
const activeStep = ref(0)
const countdown = ref(60)
const timer = ref<any>(null)

const forgetForm = ref({
  email: '',
  eCode: '',
  newPassword: ''
})
const confirmPassword = ref('')

// 模拟发送验证码
const sendCode = async() => {
  if (!forgetForm.value.email || !forgetForm.value.email.trim()) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailReg.test(forgetForm.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式');
    return;
  }
  const dto = ({
    email: forgetForm.value.email
  })
  const res = await apiCheckEmailValid(dto)
  if(res){
    ElMessage.success("验证码成功发送!")
  }
  else{
    ElMessage.warning("没有找到这个邮箱的信息")
    return
  }
  // 倒计时逻辑
  timer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer.value)
      timer.value = null
      countdown.value = 60
    }
  }, 1000)
}

// 检查验证码 
const checkCode = async() => {
  const dto = ({
      email: forgetForm.value.email,
      code: forgetForm.value.eCode
  })
  const res = await apiCheckCodeValid(dto)
  if (res){
    activeStep.value = 1
    ElMessage.success("验证成功!")
  }else{
    ElMessage.warning("验证失败!(验证码错误|邮箱地址更改)")
  }
}

const handleReset = async () => {
  if (forgetForm.value.newPassword !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  const dto = ({
    code: forgetForm.value.eCode,
    password: forgetForm.value.newPassword,
    email: forgetForm.value.email
  })
  // 这里调用你的后端找回密码接口
  const res = await apiResetPassword(dto)
  if (res){
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  }else{
    ElMessage.warning('密码重置失败')
  }
}
</script>

<style scoped>
/* 保持与登录页一致的布局 */
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
  margin-bottom: 25px;
  color: #303133;
}
.captcha-row {
  display: flex;
  gap: 10px;
}
.code-btn {
  width: 120px;
}
.submit-btn {
  width: 100%;
  margin-top: 10px;
}
.form-footer {
  margin-top: 25px;
  text-align: center;
}
</style>