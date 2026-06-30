<template>
  <div :class="prefixCls" class="login-shell">
    <div class="login-shell__frame">
      <aside :class="`${prefixCls}__brand login-shell__brand lt-xl:hidden`">
        <div class="login-shell__brand-header">
          <div aria-hidden="true" class="login-shell__brand-mark">
            <span>WT</span>
          </div>
          <div class="login-shell__brand-headings">
            <span class="login-shell__brand-mini">{{ t('sys.login.heroBrandEnglish') }}</span>
            <span class="login-shell__brand-mini-cn">{{ t('sys.login.heroBrandChinese') }}</span>
          </div>
        </div>

        <TransitionGroup
          appear
          enter-active-class="animate__animated animate__fadeInLeft"
          tag="div"
          class="login-shell__brand-content"
        >
          <div key="brand-en" class="login-shell__title-en">{{ t('sys.login.heroBrandEnglish') }}</div>
          <div key="brand-cn" class="login-shell__title-cn">{{ t('sys.login.heroBrandChinese') }}</div>
          <p key="brand-desc" class="login-shell__brand-desc">
            {{ t('sys.login.heroCapability') }}
          </p>
          <div key="brand-tags" class="login-shell__tag-list">
            <span class="login-shell__tag login-shell__tag--cyan">
              {{ t('sys.login.heroTagMes') }}
            </span>
            <span class="login-shell__tag login-shell__tag--orange">
              {{ t('sys.login.heroTagErp') }}
            </span>
            <span class="login-shell__tag login-shell__tag--light">
              {{ t('sys.login.heroTagWms') }}
            </span>
          </div>
        </TransitionGroup>
      </aside>

      <section class="login-shell__auth">
        <div class="login-shell__tools">
          <div class="login-shell__mobile-brand at-xl:hidden">
            <div aria-hidden="true" class="login-shell__brand-mark login-shell__brand-mark--mobile">
              <span>WT</span>
            </div>
            <div class="login-shell__mobile-copy">
              <span class="login-shell__mobile-en">{{ t('sys.login.heroBrandEnglish') }}</span>
              <span class="login-shell__mobile-cn">{{ t('sys.login.heroBrandChinese') }}</span>
            </div>
          </div>
          <div class="login-shell__tool-actions">
            <ThemeSwitch />
            <LocaleDropdown />
          </div>
        </div>

        <Transition appear enter-active-class="animate__animated animate__fadeInRight">
          <div class="login-shell__auth-body">
            <LoginForm class="login-shell__panel" />
            <ForgetPasswordForm class="login-shell__panel" />
            <SSOLoginVue class="login-shell__panel" />
          </div>
        </Transition>
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useDesign } from '@/hooks/web/useDesign'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'

import { LoginForm, SSOLoginVue, ForgetPasswordForm } from './components'

defineOptions({ name: 'Login' })

const { t } = useI18n()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  min-height: 100%;
  overflow: auto;
}

.login-shell {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.18), transparent 28%),
    radial-gradient(circle at bottom right, rgba(249, 115, 22, 0.18), transparent 24%),
    linear-gradient(135deg, #06111f 0%, #0b1f36 48%, #091a2e 100%);

  &__frame {
    display: grid;
    grid-template-columns: minmax(420px, 0.9fr) minmax(560px, 1.1fr);
    min-height: calc(100vh - 48px);
    border: 1px solid rgba(125, 211, 252, 0.12);
    border-radius: 32px;
    overflow: hidden;
    background: rgba(2, 6, 23, 0.38);
    box-shadow: 0 28px 60px rgba(2, 6, 23, 0.42);
    backdrop-filter: blur(10px);
  }

  &__brand {
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 36px 42px;
    overflow: hidden;

    &::before {
      position: absolute;
      inset: 0;
      background:
        linear-gradient(180deg, rgba(4, 20, 38, 0.35), rgba(4, 20, 38, 0.72)),
        url('@/assets/imgs/login_bg.png') center/cover no-repeat;
      content: '';
    }

    &::after {
      position: absolute;
      inset: 0;
      background-image:
        radial-gradient(circle at 80% 20%, rgba(56, 189, 248, 0.18) 0, transparent 38%),
        radial-gradient(circle at 16% 84%, rgba(249, 115, 22, 0.2) 0, transparent 42%),
        linear-gradient(rgba(255, 255, 255, 0.025) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
      background-size: 100% 100%, 100% 100%, 42px 42px, 42px 42px;
      content: '';
    }
  }

  &__brand-header,
  &__brand-content {
    position: relative;
    z-index: 1;
  }

  &__brand-header {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__brand-mark {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 46px;
    height: 46px;
    border: 1px solid rgba(125, 211, 252, 0.26);
    border-radius: 14px;
    background:
      linear-gradient(145deg, rgba(14, 165, 233, 0.26), rgba(249, 115, 22, 0.18)),
      rgba(7, 18, 34, 0.86);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.08),
      0 12px 24px rgba(2, 6, 23, 0.24);

    span {
      color: #f8fafc;
      font-size: 17px;
      font-weight: 800;
      letter-spacing: 0.12em;
      transform: translateX(1px);
    }
  }

  &__brand-mark--mobile {
    width: 38px;
    height: 38px;
    border-radius: 12px;

    span {
      font-size: 14px;
    }
  }

  &__brand-headings {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__brand-mini {
    color: #f8fafc;
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 0.14em;
    text-transform: uppercase;
  }

  &__brand-mini-cn {
    color: rgba(226, 232, 240, 0.82);
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 0.08em;
  }

  &__brand-content {
    display: flex;
    flex-direction: column;
    gap: 18px;
    max-width: 520px;
    margin-top: auto;
    margin-bottom: 72px;
  }

  &__title-en {
    color: rgba(248, 250, 252, 0.96);
    font-size: 72px;
    font-weight: 800;
    line-height: 0.94;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    text-shadow: 0 8px 24px rgba(8, 47, 73, 0.34);
  }

  &__title-cn {
    color: #f8fafc;
    font-size: 28px;
    font-weight: 700;
    line-height: 1.4;
    letter-spacing: 0.08em;
  }

  &__brand-desc {
    margin: 0;
    color: rgba(226, 232, 240, 0.78);
    font-size: 16px;
    line-height: 1.9;
  }

  &__tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  &__tag {
    padding: 8px 16px;
    border-radius: 999px;
    border: 1px solid transparent;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.08em;

    &--cyan {
      color: #67e8f9;
      border-color: rgba(103, 232, 249, 0.38);
      background: rgba(8, 145, 178, 0.12);
    }

    &--orange {
      color: #fdba74;
      border-color: rgba(249, 115, 22, 0.34);
      background: rgba(249, 115, 22, 0.12);
    }

    &--light {
      color: rgba(226, 232, 240, 0.86);
      border-color: rgba(226, 232, 240, 0.18);
      background: rgba(255, 255, 255, 0.04);
    }
  }

  &__auth {
    display: flex;
    flex-direction: column;
    padding: 30px 34px;
  }

  &__tools {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
  }

  &__tool-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    margin-left: auto;
  }

  &__mobile-brand {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__mobile-copy {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__mobile-en {
    color: #f8fafc;
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
  }

  &__mobile-cn {
    color: rgba(226, 232, 240, 0.8);
    font-size: 12px;
  }

  &__auth-body {
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: center;
    padding: 30px 12px 16px;
  }

  &__panel {
    width: min(100%, 560px);
    padding: 34px;
    border: 1px solid rgba(125, 211, 252, 0.16);
    border-radius: 28px;
    background:
      linear-gradient(180deg, rgba(12, 31, 52, 0.92), rgba(6, 17, 31, 0.86));
    box-shadow:
      0 20px 45px rgba(2, 6, 23, 0.42),
      inset 0 1px 0 rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(18px);
  }

  &__panel:deep(.el-input__wrapper) {
    border: 1px solid rgba(148, 163, 184, 0.18);
    background: rgba(8, 20, 34, 0.88);
    box-shadow: none;
  }

  &__panel:deep(.el-input__wrapper.is-focus) {
    border-color: rgba(125, 211, 252, 0.48);
    box-shadow: 0 0 0 1px rgba(125, 211, 252, 0.08);
  }

  &__panel:deep(.el-input__inner) {
    color: #f8fafc;
  }

  &__panel:deep(.el-input__inner::placeholder) {
    color: rgba(148, 163, 184, 0.82);
  }

  &__panel:deep(.el-link) {
    color: #fdba74;
  }

  &__panel:deep(.el-button--primary) {
    border-color: #f97316;
    background: linear-gradient(135deg, #f97316, #fb923c);
    color: #fff7ed;
    box-shadow: 0 16px 26px rgba(249, 115, 22, 0.24);
  }

  &__panel:deep(.el-button--primary:hover) {
    border-color: #fb923c;
    background: linear-gradient(135deg, #fb923c, #fdba74);
  }
}

@media (max-width: 1439px) {
  .login-shell {
    padding: 18px;

    &__frame {
      grid-template-columns: minmax(360px, 0.82fr) minmax(520px, 1.18fr);
      min-height: calc(100vh - 36px);
    }

    &__title-en {
      font-size: 58px;
    }
  }
}

@media (max-width: 1279px) {
  .login-shell {
    &__frame {
      grid-template-columns: 1fr;
    }

    &__auth {
      padding: 20px;
    }

    &__auth-body {
      padding-top: 18px;
    }
  }
}

@media (max-width: 767px) {
  .login-shell {
    padding: 12px;

    &__frame {
      min-height: calc(100vh - 24px);
      border-radius: 24px;
    }

    &__tools {
      align-items: flex-start;
      flex-direction: column;
    }

    &__tool-actions {
      width: 100%;
      justify-content: flex-end;
    }

    &__auth {
      padding: 18px 14px;
    }

    &__panel {
      padding: 24px 18px;
      border-radius: 22px;
    }
  }
}
</style>

<style lang="scss">
.login-form {
  --el-color-primary: #f97316;
  --el-color-primary-light-3: #fb923c;
  --el-color-primary-light-5: #fdba74;
  --el-color-primary-light-7: #fed7aa;
  --el-color-primary-light-9: #fff7ed;
  --el-color-primary-dark-2: #c2410c;
}
</style>
