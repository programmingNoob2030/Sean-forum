/**
 * 用户相关的类型定义
 */

// 1. 基准模型 (对应数据库 User 表)
export interface User {
  id: number;
  name: string;
  avatar: string;
  email: string;
  registerTime: string;
  lastLoginTime: string;
  postCount: number;
}

// 2. 登录成功后返回的 VO (包含 Token)
export interface LoginVO extends User{
  token: string;
}

// 3. 登录请求用的 DTO
export type LoginDTO = Pick<User, 'name'> & { password: string };

// 4. 注册请求用的 DTO
export type RegisterDTO = LoginDTO & Pick<User, 'email'>;

// 5. 找回时验证邮箱的 DTO
export type VerifyEmailDTO = Pick<User, 'email'>;

// 6. 找回时验证邮箱验证码的 DTO
export type CheckEmailCodeDTO = VerifyEmailDTO & {code:number | string}

// 7. 重置密码时候的 DTO
export type ResetPasswordDTO = CheckEmailCodeDTO & {password:string}

// 8. 更新信息的 DTO
export type UpdateInfoDTO = Pick<User, 'name' | 'avatar' | 'email'>

