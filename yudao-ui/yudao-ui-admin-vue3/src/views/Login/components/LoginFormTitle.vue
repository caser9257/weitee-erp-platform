<template>
  <div class="login-form-title">
    <p class="login-form-title__eyebrow">{{ getFormEyebrow }}</p>
    <h2 class="login-form-title__title">{{ getFormTitle }}</h2>
  </div>
</template>

<script lang="ts" setup>
import { LoginStateEnum, useLoginState } from './useLogin'
import { useLoginCopy } from './useLoginCopy'

defineOptions({ name: 'LoginFormTitle' })

const { getLoginState } = useLoginState()
const { loginText } = useLoginCopy()

const getFormEyebrow = computed(() => {
  const eyebrowMap = {
    [LoginStateEnum.RESET_PASSWORD]: loginText('resetEyebrow'),
    [LoginStateEnum.LOGIN]: loginText('signInEyebrow'),
    [LoginStateEnum.SSO]: loginText('ssoEyebrow')
  }
  return eyebrowMap[unref(getLoginState)]
})

const getFormTitle = computed(() => {
  const titleMap = {
    [LoginStateEnum.RESET_PASSWORD]: loginText('forgetFormTitle'),
    [LoginStateEnum.LOGIN]: loginText('signInTitle'),
    [LoginStateEnum.SSO]: loginText('ssoFormTitle')
  }
  return titleMap[unref(getLoginState)]
})
</script>

<style lang="scss" scoped>
.login-form-title {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 6px;

  &__eyebrow {
    margin: 0;
    color: #2563eb;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0;
    text-transform: uppercase;
  }

  &__title {
    margin: 0;
    color: #18233c;
    font-size: 23px;
    font-weight: 900;
    line-height: 1.25;
  }
}

:global(.dark) .login-form-title__title {
  color: #ffffff;
}

@media (max-width: 767px) {
  .login-form-title {
    &__title {
      font-size: 21px;
    }
  }
}
</style>
