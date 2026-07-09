<template>
  <el-form
    v-show="getShow"
    ref="formLogin"
    :model="loginData.loginForm"
    :rules="loginRules"
    :class="['login-form', 'login-auth-form', { 'login-auth-form--dark': props.isDarkMode }]"
    label-position="top"
    label-width="120px"
    size="large"
  >
    <div class="login-auth-form__content">
      <LoginFormTitle />

      <div class="login-auth-form__fields">
        <el-form-item prop="username">
          <label class="login-auth-form__label">{{ loginText('accountLabel') }}</label>
          <el-input
            v-model="loginData.loginForm.username"
            :placeholder="loginText('accountPlaceholder')"
            :prefix-icon="iconAvatar"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <label class="login-auth-form__label">{{ loginText('passwordLabel') }}</label>
          <div class="login-auth-form__password">
            <el-input
              v-model="loginData.loginForm.password"
              :placeholder="loginText('passwordPlaceholder')"
              :prefix-icon="iconLock"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              @keyup.enter="getCode()"
            >
              <template #suffix>
                <button
                  class="login-auth-form__password-toggle"
                  type="button"
                  :aria-label="showPassword ? loginText('hidePassword') : loginText('showPassword')"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 3l18 18" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M10.6 10.6A2 2 0 0012 15a2 2 0 001.4-3.4" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6.2 6.2C4.1 7.8 2.7 10 2 12c1.6 4.8 6.1 8 10 8 1.6 0 3.1-.3 4.4-.9" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9.6 4.2A11.6 11.6 0 0112 4c4 0 8.4 3.2 10 8-.5 1.4-1.2 2.8-2.2 4" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z" />
                    <circle cx="12" cy="12" r="3.2" />
                  </svg>
                </button>
              </template>
            </el-input>
          </div>
        </el-form-item>
      </div>

      <div class="login-auth-form__meta">
        <el-checkbox v-model="loginData.loginForm.rememberMe" :disabled="loginLoading">
          {{ loginText('rememberMe') }}
        </el-checkbox>
        <el-link
          class="login-auth-form__forgot"
          :disabled="loginLoading"
          type="primary"
          @click="setLoginState(LoginStateEnum.RESET_PASSWORD)"
        >
          {{ loginText('forgetPassword') }}
        </el-link>
      </div>

      <XButton
        :disabled="!canSubmitLogin"
        :loading="loginLoading"
        :title="loginText('loginButton')"
        class="login-auth-form__submit w-full"
        type="primary"
        @click="getCode()"
      />
    </div>

    <Verify
      v-if="loginData.captchaEnable === 'true'"
      ref="verify"
      :captchaType="captchaType"
      :imgSize="{ width: '400px', height: '200px' }"
      mode="pop"
      @success="handleLogin"
    />
  </el-form>
</template>

<script lang="ts" setup>
import { ElLoading } from 'element-plus'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { required, usernameRule } from '@/utils/formRules'
import { useIcon } from '@/hooks/web/useIcon'
import * as authUtil from '@/utils/auth'
import { usePermissionStore } from '@/store/modules/permission'
import * as LoginApi from '@/api/login'
import LoginFormTitle from './LoginFormTitle.vue'
import { LoginStateEnum, useFormValid, useLoginState } from './useLogin'
import { useLoginCopy } from './useLoginCopy'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'LoginForm' })
const props = withDefaults(
  defineProps<{
    isDarkMode?: boolean
  }>(),
  {
    isDarkMode: false
  }
)

const { t } = useI18n()
const iconAvatar = useIcon({ icon: 'ep:user' })
const iconLock = useIcon({ icon: 'ep:lock' })
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { setLoginState, getLoginState } = useLoginState()
const message = useMessage()
const { currentRoute, push } = useRouter()
const permissionStore = usePermissionStore()
const redirect = ref<string>('')
const loginLoading = ref(false)
const verify = ref()
const captchaType = ref('blockPuzzle')
const loading = ref<any>()
const showPassword = ref(false)
const { loginText } = useLoginCopy()
const temporaryCaptchaDisabled = true

const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN)

const loginRules = {
  username: [required, usernameRule],
  password: [required]
}

const defaultLoginForm = {
  username: import.meta.env.VITE_APP_DEFAULT_LOGIN_USERNAME || 'superadmin',
  password: import.meta.env.VITE_APP_DEFAULT_LOGIN_PASSWORD || '123456'
}

const loginData = reactive({
  captchaEnable: temporaryCaptchaDisabled ? 'false' : import.meta.env.VITE_APP_CAPTCHA_ENABLE,
  loginForm: {
    username: defaultLoginForm.username,
    password: defaultLoginForm.password,
    captchaVerification: '',
    rememberMe: true
  }
})

const canSubmitLogin = computed(() => {
  return !!loginData.loginForm.username.trim() && !!loginData.loginForm.password && !loginLoading.value
})

const getCode = async () => {
  if (!canSubmitLogin.value) {
    return
  }
  if (loginData.captchaEnable === 'false') {
    await handleLogin({})
  } else {
    verify.value?.show?.()
  }
}

const getLoginFormCache = () => {
  const loginForm = authUtil.getLoginForm()
  if (loginForm) {
    const isLegacyDefaultLogin = loginForm.username === 'admin' && loginForm.password === 'admin123'
    if (isLegacyDefaultLogin) {
      authUtil.removeLoginForm()
      return
    }

    loginData.loginForm = {
      ...loginData.loginForm,
      username: loginForm.username || loginData.loginForm.username,
      password: loginForm.password || loginData.loginForm.password,
      rememberMe: loginForm.rememberMe
    }
  }
}

const handleLogin = async (params: any) => {
  if (loginLoading.value) {
    return
  }
  const data = await validForm()
  if (!data) {
    return
  }

  loginLoading.value = true
  try {
    const loginDataLoginForm = {
      ...loginData.loginForm,
      captchaVerification: params?.captchaVerification || ''
    }
    const res = await LoginApi.login(loginDataLoginForm)
    if (!res) {
      return
    }
    loading.value = ElLoading.service({
      lock: true,
      text: loginText('loadingText'),
      background: 'rgba(2, 6, 23, 0.72)'
    })
    if (loginDataLoginForm.rememberMe) {
      authUtil.setLoginForm(loginDataLoginForm)
    } else {
      authUtil.removeLoginForm()
    }
    authUtil.setToken(res)
    if (!redirect.value) {
      redirect.value = '/'
    }
    if (redirect.value.indexOf('sso') !== -1) {
      window.location.href = window.location.href.replace('/login?redirect=', '')
    } else {
      await push({ path: redirect.value || permissionStore.addRouters[0].path })
    }
  } catch (error: any) {
    message.error(error?.message || t('sys.api.apiRequestFailed'))
  } finally {
    loginLoading.value = false
    loading.value?.close?.()
  }
}

watch(
  () => currentRoute.value,
  (route: RouteLocationNormalizedLoaded) => {
    redirect.value = route?.query?.redirect as string
  },
  {
    immediate: true
  }
)

onMounted(() => {
  getLoginFormCache()
})
</script>

<style lang="scss" scoped>
.login-auth-form {
  display: flex;
  flex-direction: column;
  padding: 30px 28px 26px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.98) 100%);
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.04),
    0 25px 50px rgba(0, 0, 0, 0.3),
    0 10px 20px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);

  &__content {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  &__fields {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__label {
    display: inline-flex;
    margin-bottom: 10px;
    color: #475569;
    font-size: 13px;
    font-weight: 700;
    line-height: 1;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }

  &__password {
    width: 100%;
  }

  &__password-toggle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    padding: 0;
    margin: 0;
    border: 0;
    background: transparent;
    color: #96a4ba;
    cursor: pointer;

    svg {
      width: 18px;
      height: 18px;
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    color: #7787a0;
    font-size: 13px;
  }

  &__forgot {
    font-weight: 700;
  }

  &__submit {
    min-height: 60px;
    margin-top: 2px;
  }
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 10px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.06),
    0 2px 6px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    inset 0 -1px 0 rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.08);
  padding: 0 14px;
  transition: all 0.2s ease;
}

:deep(.el-input__prefix-inner),
:deep(.el-input__suffix-inner) {
  color: #94a3b8;
}

:deep(.el-input__inner) {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

:deep(.el-input__inner::placeholder) {
  color: #94a3b8;
  font-weight: 500;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-color: rgba(6, 182, 212, 0.4);
  box-shadow: 
    0 0 0 3px rgba(6, 182, 212, 0.12),
    0 0 0 1px rgba(6, 182, 212, 0.25),
    0 4px 12px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

:deep(.el-checkbox) {
  color: #475569;
  font-size: 13px;
}

:deep(.el-checkbox__inner) {
  border-color: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  background: #ffffff;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  border-color: #06b6d4;
  box-shadow: 
    0 0 0 1px rgba(6, 182, 212, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

:deep(.el-link) {
  color: #475569;
}

:deep(.el-link:hover) {
  color: #06b6d4;
}

:deep(.x-button.el-button--primary),
:deep(.el-button--primary) {
  border: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  box-shadow: 
    0 0 0 1px rgba(6, 182, 212, 0.2),
    0 12px 28px rgba(6, 182, 212, 0.3),
    0 4px 10px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.25),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 700;
  transition: all 0.2s ease;

  &:hover {
    background: linear-gradient(135deg, #0891b2 0%, #0e7490 100%);
    box-shadow: 
      0 0 0 1px rgba(8, 145, 178, 0.3),
      0 16px 32px rgba(8, 145, 178, 0.35),
      0 6px 12px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.3),
      inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 
      0 0 0 1px rgba(8, 145, 178, 0.25),
      0 8px 20px rgba(8, 145, 178, 0.3),
      0 2px 6px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.2),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  }
}

.login-auth-form--dark {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.98) 100%);
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.04),
    0 25px 50px rgba(0, 0, 0, 0.3),
    0 10px 20px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.login-auth-form--dark .login-auth-form__label,
.login-auth-form--dark .login-auth-form__meta,
.login-auth-form--dark :deep(.el-link) {
  color: #475569;
}

.login-auth-form--dark .login-auth-form__password-toggle,
.login-auth-form--dark :deep(.el-input__prefix-inner),
.login-auth-form--dark :deep(.el-input__suffix-inner) {
  color: #94a3b8;
}

.login-auth-form--dark :deep(.el-input__wrapper) {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-color: rgba(0, 0, 0, 0.08);
}

.login-auth-form--dark :deep(.el-input__inner) {
  color: #1e293b;
}

.login-auth-form--dark :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

.login-auth-form--dark :deep(.el-input.is-focus .el-input__wrapper) {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-color: rgba(6, 182, 212, 0.4);
  box-shadow: 
    0 0 0 3px rgba(6, 182, 212, 0.12),
    0 0 0 1px rgba(6, 182, 212, 0.25),
    0 4px 12px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

@media (max-width: 767px) {
  .login-auth-form {
    padding: 24px 20px;

    &__meta {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
