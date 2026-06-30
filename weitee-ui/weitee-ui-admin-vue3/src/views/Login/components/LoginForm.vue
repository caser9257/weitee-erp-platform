<template>
  <el-form
    v-show="getShow"
    ref="formLogin"
    :model="loginData.loginForm"
    :rules="loginRules"
    class="login-form login-auth-form"
    label-position="top"
    label-width="120px"
    size="large"
  >
    <div class="login-auth-form__content">
      <LoginFormTitle />

      <div class="login-auth-form__fields">
        <el-form-item prop="username">
          <el-input
            v-model="loginData.loginForm.username"
            :placeholder="t('sys.login.accountPlaceholder')"
            :prefix-icon="iconAvatar"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginData.loginForm.password"
            :placeholder="t('sys.login.passwordPlaceholder')"
            :prefix-icon="iconLock"
            autocomplete="current-password"
            show-password
            type="password"
            @keyup.enter="getCode()"
          />
        </el-form-item>
      </div>

      <div class="login-auth-form__meta">
        <el-checkbox v-model="loginData.loginForm.rememberMe" :disabled="loginLoading">
          {{ t('sys.login.rememberMe') }}
        </el-checkbox>
        <el-link
          class="login-auth-form__forgot"
          :disabled="loginLoading"
          type="primary"
          @click="setLoginState(LoginStateEnum.RESET_PASSWORD)"
        >
          {{ t('sys.login.forgetPassword') }}
        </el-link>
      </div>

      <XButton
        :disabled="!canSubmitLogin"
        :loading="loginLoading"
        :title="t('sys.login.loginButton')"
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

defineOptions({ name: 'LoginForm' })

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
  captchaEnable: import.meta.env.VITE_APP_CAPTCHA_ENABLE,
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
      captchaVerification: params.captchaVerification
    }
    const res = await LoginApi.login(loginDataLoginForm)
    if (!res) {
      return
    }
    loading.value = ElLoading.service({
      lock: true,
      text: t('sys.login.loadingText'),
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
  gap: 24px;

  &__content {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  &__fields {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    color: rgba(226, 232, 240, 0.82);
    font-size: 14px;
  }

  &__forgot {
    font-weight: 500;
  }

  &__submit {
    min-height: 52px;
    font-weight: 600;
    letter-spacing: 0.08em;
  }
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-input__wrapper) {
  min-height: 52px;
}

:deep(.el-checkbox) {
  color: rgba(226, 232, 240, 0.82);
}

@media (max-width: 767px) {
  .login-auth-form {
    &__meta {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
