import request from '@/config/axios'
import type { RegisterVO, UserLoginVO } from './types'

export interface SmsCodeVO {
  mobile: string
  scene: number
}

export interface SmsLoginVO {
  mobile: string
  code: string
}

// 鐧诲綍
export const login = (data: UserLoginVO) => {
  return request.post({
    url: '/system/auth/login',
    data,
    headers: {
      isEncrypt: false
    }
  })
}

// 娉ㄥ唽
export const register = (data: RegisterVO) => {
  return request.post({ url: '/system/auth/register', data })
}

// 鐧诲嚭
export const loginOut = () => {
  return request.post({ url: '/system/auth/logout' })
}

// 鑾峰彇鐢ㄦ埛鏉冮檺淇℃伅
export const getInfo = () => {
  return request.get({ url: '/system/auth/get-permission-info' })
}

//鑾峰彇鐧诲綍楠岃瘉鐮?
export const sendSmsCode = (data: SmsCodeVO) => {
  return request.post({ url: '/system/auth/send-sms-code', data })
}

// 鐭俊楠岃瘉鐮佺櫥褰?
export const smsLogin = (data: SmsLoginVO) => {
  return request.post({ url: '/system/auth/sms-login', data })
}

// 绀句氦蹇嵎鐧诲綍锛屼娇鐢?code 鎺堟潈鐮?
export function socialLogin(type: string, code: string, state: string) {
  return request.post({
    url: '/system/auth/social-login',
    data: {
      type,
      code,
      state
    }
  })
}

// 绀句氦鎺堟潈鐨勮烦杞?
export const socialAuthRedirect = (type: number, redirectUri: string) => {
  return request.get({
    url: '/system/auth/social-auth-redirect?type=' + type + '&redirectUri=' + redirectUri
  })
}

// 鑾峰彇楠岃瘉鍥剧墖浠ュ強 token
export const getCode = (data: any) => {
  return request.postOriginal({ url: 'system/captcha/get', data })
}

// 婊戝姩鎴栬€呯偣閫夐獙璇?
export const reqCheck = (data: any) => {
  return request.postOriginal({ url: 'system/captcha/check', data })
}

// 閫氳繃鐭俊閲嶇疆瀵嗙爜
export const smsResetPassword = (data: any) => {
  return request.post({ url: '/system/auth/reset-password', data })
}
