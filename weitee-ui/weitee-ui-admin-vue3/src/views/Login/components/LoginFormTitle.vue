<template>
  <div class="login-form-title enter-x text-left">
    <p class="login-form-title__eyebrow">{{ getFormEyebrow }}</p>
    <h2 class="login-form-title__title">{{ getFormTitle }}</h2>
    <p v-if="getFormDesc" class="login-form-title__desc">{{ getFormDesc }}</p>
  </div>
</template>

<script lang="ts" setup>
import { LoginStateEnum, useLoginState } from './useLogin'

defineOptions({ name: 'LoginFormTitle' })

const { t } = useI18n()
const { getLoginState } = useLoginState()

const getFormEyebrow = computed(() => {
  const eyebrowObj = {
    [LoginStateEnum.RESET_PASSWORD]: t('sys.login.resetEyebrow'),
    [LoginStateEnum.LOGIN]: t('sys.login.signInEyebrow'),
    [LoginStateEnum.SSO]: t('sys.login.ssoEyebrow')
  }
  return eyebrowObj[unref(getLoginState)]
})

const getFormTitle = computed(() => {
  const titleObj = {
    [LoginStateEnum.RESET_PASSWORD]: t('sys.login.forgetFormTitle'),
    [LoginStateEnum.LOGIN]: t('sys.login.signInTitle'),
    [LoginStateEnum.SSO]: t('sys.login.ssoFormTitle')
  }
  return titleObj[unref(getLoginState)]
})

const getFormDesc = computed(() => {
  const descObj = {
    [LoginStateEnum.RESET_PASSWORD]: t('sys.login.forgetFormDesc'),
    [LoginStateEnum.LOGIN]: t('sys.login.signInDesc'),
    [LoginStateEnum.SSO]: t('sys.login.ssoFormDesc')
  }
  return descObj[unref(getLoginState)]
})
</script>

<style lang="scss" scoped>
.login-form-title {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__eyebrow {
    margin: 0;
    color: #7dd3fc;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.24em;
    text-transform: uppercase;
  }

  &__title {
    margin: 0;
    color: #f8fafc;
    font-size: 34px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__desc {
    margin: 0;
    color: rgba(226, 232, 240, 0.82);
    font-size: 14px;
    line-height: 1.7;
  }
}

@media (max-width: 767px) {
  .login-form-title {
    &__title {
      font-size: 28px;
    }
  }
}
</style>
