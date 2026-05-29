import { useLocaleStore } from '@/store/modules/locale'

type LoginCopyKey =
  | 'backSignIn'
  | 'signInEyebrow'
  | 'resetEyebrow'
  | 'ssoEyebrow'
  | 'signInFormTitle'
  | 'ssoFormTitle'
  | 'forgetFormTitle'
  | 'forgetFormDesc'
  | 'ssoFormDesc'
  | 'signInTitle'
  | 'signInDesc'
  | 'heroBrandEnglish'
  | 'heroBrandChinese'
  | 'heroCapability'
  | 'boardStatus'
  | 'card1Title'
  | 'card1Tag'
  | 'card2Title'
  | 'card2Metric1'
  | 'card2Metric2'
  | 'card3Title'
  | 'card3Metric1'
  | 'card3Metric2'
  | 'heroTagMes'
  | 'heroTagErp'
  | 'heroTagWms'
  | 'recommendEngine'
  | 'privacyPolicy'
  | 'termsOfUse'
  | 'toLightMode'
  | 'toDarkMode'
  | 'loginButton'
  | 'rememberMe'
  | 'forgetPassword'
  | 'loadingText'
  | 'showPassword'
  | 'hidePassword'
  | 'accountLabel'
  | 'passwordLabel'
  | 'accountPlaceholder'
  | 'passwordPlaceholder'

const loginCopyMap: Record<LocaleType, Record<LoginCopyKey, string>> = {
  'zh-CN': {
    backSignIn: '返回',
    signInEyebrow: '账号认证',
    resetEyebrow: '安全校验',
    ssoEyebrow: '安全授权',
    signInFormTitle: '登录',
    ssoFormTitle: '三方授权',
    forgetFormTitle: '重置密码',
    forgetFormDesc: '请完成身份校验并重置账号密码。',
    ssoFormDesc: '请确认授权范围后继续访问第三方系统。',
    signInTitle: '欢迎登录微泰智能制造平台',
    signInDesc: '请输入账号信息，进入制造协同工作台',
    heroBrandEnglish: 'WAVE TECHNIC',
    heroBrandChinese: '微泰ERP系统',
    heroCapability: '成都微泰科技有限公司射频通信、光模块封测与时频同步业务中枢',
    boardStatus: '节点在线',
    card1Title: '射频功放与频率源稳定性',
    card1Tag: '运行稳定',
    card2Title: '封装与光测试良率指标',
    card2Metric1: '良率',
    card2Metric2: '监测中',
    card3Title: '时频同步与定位校准',
    card3Metric1: '同步误差',
    card3Metric2: '高精度校准中',
    heroTagMes: '射频通信 RF',
    heroTagErp: '光通信 OPTICAL',
    heroTagWms: '数字通信 DIGITAL',
    recommendEngine: '推荐浏览器：Chromium 内核',
    privacyPolicy: '隐私政策',
    termsOfUse: '使用条款',
    toLightMode: '切换到浅色模式',
    toDarkMode: '切换到深色模式',
    loginButton: '登录',
    rememberMe: '记住我',
    forgetPassword: '忘记密码？',
    loadingText: '正在登录系统...',
    showPassword: '显示密码',
    hidePassword: '隐藏密码',
    accountLabel: '工作账号 / 用户名',
    passwordLabel: '身份安全密钥',
    accountPlaceholder: '请输入工作账号',
    passwordPlaceholder: '请输入密码'
  },
  en: {
    backSignIn: 'Back',
    signInEyebrow: 'Account Access',
    resetEyebrow: 'Security Reset',
    ssoEyebrow: 'Security Authorization',
    signInFormTitle: 'Sign In',
    ssoFormTitle: 'SSO Authorization',
    forgetFormTitle: 'Reset Password',
    forgetFormDesc: 'Complete identity verification and reset your account password.',
    ssoFormDesc: 'Confirm the authorization scope before continuing to the third-party system.',
    signInTitle: 'Welcome to Wave Technic Manufacturing Platform',
    signInDesc: 'Enter your account information to access the manufacturing workspace.',
    heroBrandEnglish: 'WAVE TECHNIC',
    heroBrandChinese: 'Wave Technic ERP',
    heroCapability: 'RF communication, optical module testing and time-frequency synchronization control center.',
    boardStatus: 'Nodes Online',
    card1Title: 'RF Amplifier and Frequency Source Stability',
    card1Tag: 'Stable',
    card2Title: 'Optical Packaging and Test Yield',
    card2Metric1: 'Yield',
    card2Metric2: 'Monitoring',
    card3Title: 'Time Sync and Positioning Calibration',
    card3Metric1: 'Sync Error',
    card3Metric2: 'High-Precision Calibration',
    heroTagMes: 'RF COMMUNICATION',
    heroTagErp: 'OPTICAL MODULE',
    heroTagWms: 'DIGITAL COMMUNICATION',
    recommendEngine: 'Recommended Browser: Chromium',
    privacyPolicy: 'Privacy Policy',
    termsOfUse: 'Terms of Use',
    toLightMode: 'Switch to light mode',
    toDarkMode: 'Switch to dark mode',
    loginButton: 'Sign in',
    rememberMe: 'Remember me',
    forgetPassword: 'Forgot password?',
    loadingText: 'Signing in...',
    showPassword: 'Show password',
    hidePassword: 'Hide password',
    accountLabel: 'Work Account / Username',
    passwordLabel: 'Security Password',
    accountPlaceholder: 'Enter your work account',
    passwordPlaceholder: 'Enter your password'
  }
}

export const useLoginCopy = () => {
  const { t } = useI18n()
  const localeStore = useLocaleStore()

  const currentLang = computed<LocaleType>(() => {
    return localeStore.getCurrentLocale.lang === 'en' ? 'en' : 'zh-CN'
  })

  const loginText = (key: LoginCopyKey) => {
    const fullKey = `sys.login.${key}`
    const translated = t(fullKey)
    if (translated && translated !== fullKey) {
      return translated
    }
    return loginCopyMap[currentLang.value][key]
  }

  return {
    currentLang,
    loginText
  }
}
