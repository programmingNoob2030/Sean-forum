import type { CheckEmailCodeDTO, LoginDTO, LoginVO, RegisterDTO, ResetPasswordDTO, UpdateInfoDTO, VerifyEmailDTO } from '@/models/user/userTypes'
import request from '@/utils/requests'

// 用户登录
export const apiUserLogin = (dto: LoginDTO) => request.post<any, LoginVO>('/session', dto)

// 用户注册
export const apiUserRegister = (dto: RegisterDTO) => request.post('/users', dto)

// 根据邮箱获取验证码
export const apiCheckEmailValid = (dto: VerifyEmailDTO) => request.get<any, boolean>('/email', {params:dto})

// 检查填写的验证码是否正确
export const apiCheckCodeValid = (dto: CheckEmailCodeDTO) => request.get<any, boolean>('/code', {params:dto})

// 重置密码
export const apiResetPassword = (dto: ResetPasswordDTO) => request.put<any, boolean>('/password', dto)

// 上传头像
export const apiUploadAvatar = (data: FormData) => request.post<any, string>('/user/avatar', data)

// 更新信息
export const apiUpdateUserInfo = (dto: UpdateInfoDTO) => request.put<any, LoginVO>('/info', dto)

