export type UserLoginVO = {
  username: string
  password: string
  captchaVerification: string
  socialType?: string
  socialCode?: string
  socialState?: string
}

export type TokenType = {
  id: number // 缂栧彿
  accessToken: string // 璁块棶浠ょ墝
  refreshToken: string // 鍒锋柊浠ょ墝
  userId: number // 鐢ㄦ埛缂栧彿
  userType: number //鐢ㄦ埛绫诲瀷
  clientId: string //瀹㈡埛绔紪鍙?
  expiresTime: number //杩囨湡鏃堕棿
}

export type UserVO = {
  id: number
  username: string
  nickname: string
  deptId: number
  email: string
  mobile: string
  sex: number
  avatar: string
  loginIp: string
  loginDate: string
}

export type RegisterVO = {
  username: string
  password: string
  captchaVerification: string
}
