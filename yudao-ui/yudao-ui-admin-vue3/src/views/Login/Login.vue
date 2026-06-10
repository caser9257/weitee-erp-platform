<template>
  <div
    :class="[prefixCls, { dark: isDarkMode }]"
    class="login-shell"
    :style="{ colorScheme: isDarkMode ? 'dark' : 'light' }"
  >
    <div class="login-shell__frame">
      <aside
        class="login-shell__brand lt-xl:hidden"
        @mousedown="handleMouseDown"
        @touchstart="handleTouchStart"
      >
        <div class="login-shell__brand-top">
          <div class="login-shell__brand-badge">WT</div>
          <div class="login-shell__brand-copy">
            <div class="login-shell__brand-name">{{ loginText('heroBrandEnglish') }}</div>
            <div class="login-shell__brand-sub">{{ loginText('heroBrandChinese') }}</div>
          </div>
        </div>

        <div class="login-shell__stage">
          <div class="login-shell__board-wrap" :style="boardStyle">
            <div class="login-shell__board">
              <span class="login-shell__board-line login-shell__board-line--tl"></span>
              <span class="login-shell__board-line login-shell__board-line--br"></span>
              <span class="login-shell__board-corner login-shell__board-corner--lt"></span>
              <span class="login-shell__board-corner login-shell__board-corner--rb"></span>

              <div class="login-shell__board-top">
                <span class="login-shell__board-label">MODEL: WT-COMM-STATION</span>
                <span class="login-shell__board-status">
                  <i></i>
                </span>
              </div>

              <div class="login-shell__chip">
                <span class="login-shell__chip-label">WT CORE</span>
                <span class="login-shell__chip-sub">RF ENGINE</span>
              </div>

              <div class="login-shell__float-card login-shell__float-card--rf" :style="cardStyle1">
                <div class="login-shell__float-head">
                  <span>RF MODULE</span>
                  <strong>42.5 dBm</strong>
                </div>
                <div class="login-shell__float-title">{{ loginText('card1Title') }}</div>
                <div class="login-shell__float-meta">
                  <span>VSWR 1.15</span>
                  <span>{{ loginText('card1Tag') }}</span>
                </div>
                <svg class="login-shell__float-wave" viewBox="0 0 88 30" fill="none" aria-hidden="true">
                  <path
                    d="M4 14 C10 4, 18 4, 24 14 S38 24, 44 14 S58 4, 64 14 S78 24, 84 14"
                    stroke="currentColor"
                    stroke-width="2.2"
                    stroke-linecap="round"
                  />
                </svg>
              </div>

              <div class="login-shell__float-card login-shell__float-card--sync" :style="cardStyle3">
                <div class="login-shell__float-head">
                  <span>DIGITAL CO.</span>
                  <strong>&lt; 0.1 ns</strong>
                </div>
                <div class="login-shell__float-title">{{ loginText('card3Title') }}</div>
                <div class="login-shell__sync-box">
                  <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <circle cx="12" cy="12" r="9" />
                    <path d="M12 7v5l3 2" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <div>
                    <span>{{ loginText('card3Metric1') }}</span>
                    <p>{{ loginText('card3Metric2') }}</p>
                  </div>
                </div>
              </div>

              <div class="login-shell__float-card login-shell__float-card--opt" :style="cardStyle2">
                <div class="login-shell__float-head">
                  <span>OPTICAL TESTING</span>
                  <strong>99.98%</strong>
                </div>
                <div class="login-shell__float-title">{{ loginText('card2Title') }}</div>
                <div class="login-shell__bars">
                  <span
                    v-for="(bar, index) in opticalBars"
                    :key="index"
                    class="login-shell__bar"
                    :style="{ height: `${bar}%` }"
                  ></span>
                </div>
                <div class="login-shell__float-foot">
                  <span>{{ loginText('card2Metric1') }}</span>
                  <span>{{ loginText('card2Metric2') }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-show="isSnapping" class="login-shell__cards-overlay">
            <div class="login-shell__clear-card login-shell__clear-card--rf login-shell__clear-card--compact">
              <div class="login-shell__float-head">
                <span>RF MODULE</span>
                <strong>42.5 dBm</strong>
              </div>
              <div class="login-shell__float-title">{{ loginText('card1Title') }}</div>
              <div class="login-shell__float-meta">
                <span>VSWR 1.15</span>
                <span>{{ loginText('card1Tag') }}</span>
              </div>
              <svg class="login-shell__float-wave" viewBox="0 0 88 30" fill="none" aria-hidden="true">
                <path
                  d="M4 14 C10 4, 18 4, 24 14 S38 24, 44 14 S58 4, 64 14 S78 24, 84 14"
                  stroke="currentColor"
                  stroke-width="2.2"
                  stroke-linecap="round"
                />
              </svg>
            </div>

            <div class="login-shell__clear-card login-shell__clear-card--sync">
              <div class="login-shell__float-head">
                <span>DIGITAL CO.</span>
                <strong>&lt; 0.1 ns</strong>
              </div>
              <div class="login-shell__float-title">{{ loginText('card3Title') }}</div>
              <div class="login-shell__sync-box">
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" />
                  <path d="M12 7v5l3 2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <div>
                  <span>{{ loginText('card3Metric1') }}</span>
                  <p>{{ loginText('card3Metric2') }}</p>
                </div>
              </div>
            </div>

            <div class="login-shell__clear-card login-shell__clear-card--opt login-shell__clear-card--compact">
              <div class="login-shell__float-head">
                <span>OPTICAL TESTING</span>
                <strong>99.98%</strong>
              </div>
              <div class="login-shell__float-title">{{ loginText('card2Title') }}</div>
              <div class="login-shell__bars">
                <span
                  v-for="(bar, index) in opticalBars"
                  :key="`overlay-${index}`"
                  class="login-shell__bar"
                  :style="{ height: `${bar}%` }"
                ></span>
              </div>
              <div class="login-shell__float-foot">
                <span>{{ loginText('card2Metric1') }}</span>
                <span>{{ loginText('card2Metric2') }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="login-shell__brand-bottom">
          <h1>{{ loginText('heroBrandEnglish') }}</h1>
          <h2>{{ loginText('heroBrandChinese') }}</h2>
          <p>{{ loginText('heroCapability') }}</p>
          <div class="login-shell__tag-list">
            <span class="login-shell__tag">{{ loginText('heroTagMes') }}</span>
            <span class="login-shell__tag">{{ loginText('heroTagErp') }}</span>
            <span class="login-shell__tag">{{ loginText('heroTagWms') }}</span>
          </div>
        </div>
      </aside>

      <section class="login-shell__auth">
        <div class="login-shell__topbar">
          <div class="login-shell__mobile-brand at-xl:hidden">
            <div class="login-shell__brand-badge login-shell__brand-badge--mobile">WT</div>
            <div class="login-shell__mobile-copy">
              <div class="login-shell__brand-name">{{ loginText('heroBrandEnglish') }}</div>
              <div class="login-shell__brand-sub">{{ loginText('heroBrandChinese') }}</div>
            </div>
          </div>

          <div class="login-shell__tools">
            <button type="button" class="login-shell__lang" @click="toggleLanguage">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                <path
                  d="M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path d="M8 8.5h8M9.5 15.5h5M12 4.5c2 2 3 4.5 3 7.5s-1 5.5-3 7.5c-2-2-3-4.5-3-7.5s1-5.5 3-7.5z" />
              </svg>
              <span>{{ currentLocaleName }}</span>
            </button>

            <button
              type="button"
              class="login-shell__theme"
              @click="toggleTheme"
              :aria-label="isDarkMode ? loginText('toLightMode') : loginText('toDarkMode')"
            >
              <span class="login-shell__theme-track">
                <span class="login-shell__theme-thumb" :class="{ 'login-shell__theme-thumb--dark': isDarkMode }">
                  <svg v-if="!isDarkMode" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="12" r="4" />
                    <path d="M12 2.5v2.2M12 19.3v2.2M4.7 4.7l1.6 1.6M17.7 17.7l1.6 1.6M2.5 12h2.2M19.3 12h2.2M4.7 19.3l1.6-1.6M17.7 6.3l1.6-1.6" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path d="M20 14.7A8.5 8.5 0 1 1 9.3 4a6.8 6.8 0 0 0 10.7 10.7z" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                </span>
              </span>
            </button>
          </div>
        </div>

        <div class="login-shell__auth-body">
          <div class="login-shell__panel">
            <LoginForm :is-dark-mode="isDarkMode" class="login-shell__panel-section" />
            <ForgetPasswordForm :is-dark-mode="isDarkMode" class="login-shell__panel-section" />
            <SSOLoginVue :is-dark-mode="isDarkMode" class="login-shell__panel-section" />
          </div>
        </div>

        <div class="login-shell__footer">
          <span>{{ loginText('recommendEngine') }}</span>
          <div>
            <span>{{ loginText('privacyPolicy') }}</span>
            <i>·</i>
            <span>{{ loginText('termsOfUse') }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { useLocaleStore } from '@/store/modules/locale'
import { useLocale } from '@/hooks/web/useLocale'
import { ForgetPasswordForm, LoginForm, SSOLoginVue } from './components'
import { useLoginCopy } from './components/useLoginCopy'
import { LoginStateEnum, useLoginState } from './components/useLogin'

defineOptions({ name: 'Login' })

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
const appStore = useAppStore()
const localeStore = useLocaleStore()
const { changeLocale } = useLocale()
const { setLoginState } = useLoginState()
const { currentLang, loginText } = useLoginCopy()

const isDarkMode = ref(appStore.getIsDark)
const currentLocaleLang = computed(() => localeStore.getCurrentLocale.lang)
const currentLocaleName = computed(() =>
  currentLang.value === 'zh-CN' ? '简体中文' : 'English'
)

const rotateX = ref(30)
const rotateY = ref(0)
const rotateZ = ref(0)
const isSnapping = ref(false)
let targetZ = 0

const opticalBars = [40, 58, 74, 90]

const boardStyle = computed(() => ({
  transform: `rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg) rotateZ(${rotateZ.value}deg)`
}))

const cardStyle1 = computed(() => ({
  transform: 'translateZ(55px) rotate(-42deg)',
  transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.16s ease',
  opacity: isSnapping.value ? '0' : '1'
}))

const cardStyle2 = computed(() => ({
  transform: 'translateZ(85px) rotate(-28deg)',
  transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.16s ease',
  opacity: isSnapping.value ? '0' : '1'
}))

const cardStyle3 = computed(() => ({
  transform: 'translateZ(35px) rotate(42deg)',
  transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.16s ease',
  opacity: isSnapping.value ? '0' : '1'
}))

watch(
  isDarkMode,
  (value) => {
    appStore.setIsDark(value)
    document.documentElement.classList.toggle('dark', value)
  },
  { immediate: true }
)

const normalizeAngle = (value: number) => {
  if (value > 3600) return value - 3600
  if (value < -3600) return value + 3600
  return value
}

const handleGlobalMouseUp = () => {
  isSnapping.value = false
  rotateZ.value = rotateZ.value % 360
  window.removeEventListener('mouseup', handleGlobalMouseUp)
  window.removeEventListener('mouseleave', handleGlobalMouseUp)
  window.removeEventListener('touchend', handleGlobalMouseUp)
}

const handleMouseDown = (event?: MouseEvent | TouchEvent) => {
  event?.preventDefault?.()
  isSnapping.value = true
  targetZ = Math.round(rotateZ.value / 360) * 360
  window.addEventListener('mouseup', handleGlobalMouseUp)
  window.addEventListener('mouseleave', handleGlobalMouseUp)
}

const handleTouchStart = (event: TouchEvent) => {
  handleMouseDown(event)
  window.addEventListener('touchend', handleGlobalMouseUp)
}

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
}

const toggleLanguage = async () => {
  const nextLang = currentLocaleLang.value === 'zh-CN' ? 'en' : 'zh-CN'
  await changeLocale(nextLang as LocaleType)
}

let animationId = 0
const spinBoard = () => {
  if (isSnapping.value) {
    rotateX.value += (0 - rotateX.value) * 0.22
    rotateY.value += (0 - rotateY.value) * 0.22
    rotateZ.value += (targetZ - rotateZ.value) * 0.22

    if (Math.abs(rotateX.value) < 0.05) rotateX.value = 0
    if (Math.abs(rotateY.value) < 0.05) rotateY.value = 0
    if (Math.abs(targetZ - rotateZ.value) < 0.05) rotateZ.value = targetZ
  } else {
    rotateX.value += (30 - rotateX.value) * 0.16
    rotateY.value += (0 - rotateY.value) * 0.16
    rotateZ.value = normalizeAngle((rotateZ.value + 0.15) % 360)
  }
  animationId = window.requestAnimationFrame(spinBoard)
}

onMounted(() => {
  setLoginState(LoginStateEnum.LOGIN)
  animationId = window.requestAnimationFrame(spinBoard)
})

onBeforeUnmount(() => {
  window.cancelAnimationFrame(animationId)
  handleGlobalMouseUp()
})
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  min-height: 100%;
  overflow: auto;
}

.login-shell {
  min-height: 100vh;
  padding: 0;
  background: #e9edf4;
  color: #1e293b;
  transition: background 0.35s ease;

  &__frame {
    display: grid;
    grid-template-columns: 1fr 1fr;
    min-height: 100vh;
    overflow: hidden;
    background: linear-gradient(180deg, #334155 0%, #1e293b 100%);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background:
        linear-gradient(90deg, transparent 49.5%, rgba(148, 163, 184, 0.08) 49.5%, rgba(148, 163, 184, 0.08) 50.5%, transparent 50.5%),
        linear-gradient(0deg, transparent 49.5%, rgba(148, 163, 184, 0.04) 49.5%, rgba(148, 163, 184, 0.04) 50.5%, transparent 50.5%);
      background-size: 60px 60px;
      pointer-events: none;
    }
  }

  &__brand {
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 60px 56px 54px;
    border-right: 1px solid rgba(148, 163, 184, 0.15);
    background: linear-gradient(180deg, #475569 0%, #334155 100%);
    z-index: 1;
  }

  &__brand-top {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__brand-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    box-shadow: 
      0 0 0 1px rgba(96, 165, 250, 0.3),
      0 8px 24px rgba(59, 130, 246, 0.4),
      0 3px 8px rgba(0, 0, 0, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.2),
      inset 0 -1px 0 rgba(0, 0, 0, 0.15);
    color: #fff;
    font-size: 18px;
    font-weight: 900;
    letter-spacing: 0;
    border: 1px solid rgba(96, 165, 250, 0.4);

    &--mobile {
      width: 34px;
      height: 34px;
      border-radius: 6px;
      font-size: 15px;
    }
  }

  &__brand-copy,
  &__mobile-copy {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__brand-name {
    color: #f8fafc;
    font-size: 17px;
    font-weight: 900;
    line-height: 1;
    letter-spacing: 1px;
  }

  &__brand-sub {
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    letter-spacing: 0.5px;
  }

  &__stage {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 440px;
    perspective: 1400px;
    perspective-origin: 50% 50%;
  }

  &__board-wrap {
    width: 520px;
    transform-style: preserve-3d;
  }

  &__board {
    position: relative;
    width: 100%;
    height: 312px;
    border-radius: 12px;
    border: 1px solid rgba(148, 163, 184, 0.3);
    background:
      linear-gradient(rgba(148, 163, 184, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(148, 163, 184, 0.1) 1px, transparent 1px),
      linear-gradient(180deg, rgba(71, 85, 105, 0.92) 0%, rgba(51, 65, 85, 0.88) 100%);
    background-size: 26px 26px, 26px 26px, 100% 100%;
    box-shadow: 
      0 0 0 1px rgba(148, 163, 184, 0.1),
      0 25px 60px rgba(0, 0, 0, 0.2),
      0 8px 20px rgba(148, 163, 184, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.12),
      inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    transform-style: preserve-3d;
  }

  &__board-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 18px 24px;
  }

  &__board-label,
  &__board-status {
    color: #3b82f6;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 1px;
    text-transform: uppercase;
  }

  &__board-status {
    display: inline-flex;
    align-items: center;
    gap: 8px;

    i {
      width: 10px;
      height: 10px;
      border-radius: 999px;
      background: #10b981;
      box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
      animation: pulse 2s infinite;
    }
  }

  &__board-line {
    position: absolute;
    background: rgba(59, 130, 246, 0.3);

    &--tl {
      left: 48px;
      top: 70px;
      width: 1px;
      height: 170px;
      transform: rotate(64deg);
      transform-origin: top left;
    }

    &--br {
      right: 52px;
      bottom: 44px;
      width: 1px;
      height: 140px;
      transform: rotate(64deg);
      transform-origin: bottom right;
    }
  }

  &__board-corner {
    position: absolute;
    color: #3b82f6;
    font-size: 13px;
    font-weight: 800;
    transform: translateZ(24px);
    text-shadow: 0 0 10px rgba(59, 130, 246, 0.5);

    &::before {
      content: '×';
    }

    &--lt {
      left: 58px;
      top: 72px;
    }

    &--rb {
      right: 72px;
      bottom: 52px;
    }
  }

  &__chip {
    position: absolute;
    left: 50%;
    top: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 94px;
    height: 72px;
    border-radius: 8px;
    border: 1px solid rgba(148, 163, 184, 0.4);
    background: linear-gradient(180deg, rgba(71, 85, 105, 0.92) 0%, rgba(51, 65, 85, 0.92) 100%);
    box-shadow: 
      0 0 0 1px rgba(148, 163, 184, 0.12),
      0 8px 24px rgba(148, 163, 184, 0.15),
      0 3px 8px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.12),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
    transform: translate3d(-50%, -50%, 18px);
  }

  &__chip-label {
    color: #3b82f6;
    font-size: 10px;
    font-weight: 900;
    line-height: 1;
    letter-spacing: 1px;
    text-transform: uppercase;
  }

  &__chip-sub {
    margin-top: 6px;
    color: #64748b;
    font-size: 9px;
    font-weight: 700;
    line-height: 1;
    letter-spacing: 0.5px;
  }

  &__float-card {
    position: absolute;
    z-index: 2;
    border-radius: 6px;
    border: 1px solid rgba(96, 165, 250, 0.4);
    background: linear-gradient(135deg, rgba(71, 85, 105, 0.9) 0%, rgba(51, 65, 85, 0.85) 100%);
    box-shadow: 
      0 0 0 1px rgba(96, 165, 250, 0.15),
      0 8px 32px rgba(0, 0, 0, 0.2),
      0 2px 8px rgba(96, 165, 250, 0.12),
      inset 0 1px 0 rgba(255, 255, 255, 0.12),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(12px);
    padding: 16px 18px;
    backface-visibility: hidden;
    -webkit-font-smoothing: antialiased;
    transform-style: preserve-3d;
    opacity: 0.95;
    transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.16s ease, box-shadow 0.3s ease;

    &--rf {
      left: 124px;
      top: -56px;
      width: 186px;
      color: #60a5fa;
      border-color: rgba(96, 165, 250, 0.5);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.92) 0%, rgba(30, 58, 138, 0.3) 100%);
      box-shadow: 
        0 0 0 1px rgba(96, 165, 250, 0.2),
        0 12px 40px rgba(96, 165, 250, 0.15),
        0 4px 12px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    }

    &--sync {
      right: -36px;
      top: 124px;
      width: 176px;
      color: #10b981;
      border-color: rgba(16, 185, 129, 0.4);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.92) 0%, rgba(6, 78, 59, 0.3) 100%);
      box-shadow: 
        0 0 0 1px rgba(16, 185, 129, 0.15),
        0 12px 40px rgba(16, 185, 129, 0.12),
        0 4px 12px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    }

    &--opt {
      left: 108px;
      bottom: -46px;
      width: 188px;
      color: #38bdf8;
      border-color: rgba(56, 189, 248, 0.4);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.92) 0%, rgba(12, 74, 110, 0.3) 100%);
      box-shadow: 
        0 0 0 1px rgba(56, 189, 248, 0.15),
        0 12px 40px rgba(56, 189, 248, 0.12),
        0 4px 12px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    }
  }

  &__cards-overlay {
    position: absolute;
    left: 50%;
    top: 50%;
    z-index: 4;
    width: 520px;
    height: 312px;
    transform: translate(-50%, -50%);
    pointer-events: none;
  }

  &__clear-card {
    position: absolute;
    border-radius: 6px;
    border: 1px solid rgba(96, 165, 250, 0.3);
    background: linear-gradient(135deg, rgba(71, 85, 105, 0.88) 0%, rgba(51, 65, 85, 0.85) 100%);
    box-shadow: 
      0 0 0 1px rgba(96, 165, 250, 0.12),
      0 10px 36px rgba(0, 0, 0, 0.18),
      0 3px 10px rgba(96, 165, 250, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 0.12),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    padding: 16px 18px;
    backface-visibility: hidden;
    -webkit-font-smoothing: antialiased;
    text-rendering: geometricPrecision;
    opacity: 0.95;

    &--rf {
      left: 28px;
      top: 8px;
      width: 188px;
      min-height: 108px;
      color: #60a5fa;
      border-color: rgba(96, 165, 250, 0.4);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.9) 0%, rgba(30, 58, 138, 0.25) 100%);
      box-shadow: 
        0 0 0 1px rgba(96, 165, 250, 0.18),
        0 14px 44px rgba(96, 165, 250, 0.12),
        0 4px 14px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    &--sync {
      right: 20px;
      top: 92px;
      width: 176px;
      color: #10b981;
      border-color: rgba(16, 185, 129, 0.35);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.9) 0%, rgba(6, 78, 59, 0.25) 100%);
      box-shadow: 
        0 0 0 1px rgba(16, 185, 129, 0.12),
        0 14px 44px rgba(16, 185, 129, 0.08),
        0 4px 14px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    }

    &--opt {
      left: 42px;
      bottom: 6px;
      width: 164px;
      color: #38bdf8;
      border-color: rgba(56, 189, 248, 0.35);
      background: linear-gradient(135deg, rgba(71, 85, 105, 0.9) 0%, rgba(12, 74, 110, 0.25) 100%);
      box-shadow: 
        0 0 0 1px rgba(56, 189, 248, 0.12),
        0 14px 44px rgba(56, 189, 248, 0.08),
        0 4px 14px rgba(0, 0, 0, 0.18),
        inset 0 1px 0 rgba(255, 255, 255, 0.15),
        inset 0 -1px 0 rgba(0, 0, 0, 0.12);
    }

    &--compact {
      padding: 12px 14px;

      .login-shell__float-title {
        margin-top: 6px;
        font-size: 12px;
        line-height: 1.28;
      }

      .login-shell__float-meta,
      .login-shell__float-foot {
        margin-top: 6px;
        font-size: 9px;
      }

      .login-shell__float-wave {
        height: 22px;
        margin-top: 6px;
      }

      .login-shell__bars {
        gap: 5px;
        height: 32px;
        margin-top: 8px;
      }

      .login-shell__bar {
        width: 10px;
      }
    }
  }

  &__float-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #64748b;
    font-size: 9px;
    font-weight: 700;
    letter-spacing: 0.5px;
    text-transform: uppercase;

    strong {
      color: #f8fafc;
      font-size: 10px;
      font-weight: 900;
    }
  }

  &__float-title {
    margin-top: 8px;
    color: #f8fafc;
    font-size: 14px;
    font-weight: 900;
    line-height: 1.35;
    letter-spacing: 0.5px;
  }

  &__float-meta,
  &__float-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 7px;
    color: #64748b;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.3px;
  }

  &__float-wave {
    width: 100%;
    height: 28px;
    margin-top: 8px;
  }

  &__sync-box {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 10px;

    svg {
      width: 32px;
      height: 32px;
      color: #10b981;
      stroke: currentColor;
      stroke-width: 1.8;
      transform-origin: center;
      animation: ringSpin 8s linear infinite;
      flex: 0 0 auto;
      filter: drop-shadow(0 0 8px rgba(16, 185, 129, 0.4));
    }

    span {
      color: #10b981;
      font-size: 12px;
      font-weight: 900;
      line-height: 1.1;
      letter-spacing: 0.5px;
    }

    p {
      margin: 4px 0 0;
      color: #64748b;
      font-size: 10px;
      font-weight: 700;
      line-height: 1.3;
    }

  }

  &__bars {
    display: flex;
    align-items: flex-end;
    gap: 6px;
    height: 40px;
    margin-top: 12px;
  }

  &__bar {
    width: 12px;
    border-radius: 2px;
    background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
    box-shadow: 
      0 0 0 1px rgba(59, 130, 246, 0.3),
      0 4px 12px rgba(59, 130, 246, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.2),
      inset 0 -1px 0 rgba(0, 0, 0, 0.2);
  }

  &__brand-bottom {
    max-width: 500px;

    h1 {
      margin: 0;
      color: #f8fafc;
      font-size: 58px;
      font-weight: 900;
      line-height: 1;
      letter-spacing: 2px;
      text-transform: uppercase;
    }

    h2 {
      margin: 18px 0 0;
      color: #e2e8f0;
      font-size: 17px;
      font-weight: 800;
      line-height: 1.2;
      letter-spacing: 0.5px;
    }

    p {
      margin: 18px 0 0;
      color: #64748b;
      font-size: 15px;
      font-weight: 500;
      line-height: 1.75;
    }
  }

  &__tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 30px;
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 82px;
    height: 28px;
    padding: 0 12px;
    border: 1px solid rgba(148, 163, 184, 0.25);
    border-radius: 4px;
    background: linear-gradient(180deg, rgba(71, 85, 105, 0.65) 0%, rgba(51, 65, 85, 0.75) 100%);
    color: #cbd5e1;
    font-size: 11px;
    font-weight: 600;
    line-height: 1;
    letter-spacing: 0.5px;
    text-transform: uppercase;
    transition: all 0.2s ease;
    box-shadow: 
      0 0 0 1px rgba(148, 163, 184, 0.06),
      0 2px 6px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.08);

    &:hover {
      border-color: #3b82f6;
      color: #2563eb;
      background: rgba(59, 130, 246, 0.05);
    }
  }

  &__auth {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 54px 58px 26px;
    background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
    border-left: 1px solid rgba(59, 130, 246, 0.2);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #3b82f6, #2563eb, #1d4ed8);
      box-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
    }
  }

  &__topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  &__mobile-brand {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__tools {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-left: auto;
  }

  &__lang {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    border: 0;
    background: transparent;
    color: #64748b;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    padding: 0;

    svg {
      width: 14px;
      height: 14px;
    }
  }

  &__theme {
    border: 0;
    background: transparent;
    padding: 0;
    cursor: pointer;
  }

  &__theme-track {
    position: relative;
    display: inline-flex;
    align-items: center;
    width: 46px;
    height: 24px;
    padding: 2px;
    border-radius: 999px;
    background: linear-gradient(180deg, rgba(71, 85, 105, 0.65) 0%, rgba(51, 65, 85, 0.75) 100%);
    border: 1px solid rgba(148, 163, 184, 0.2);
    box-shadow: 
      inset 0 1px 0 rgba(255, 255, 255, 0.08),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  }

  &__theme-thumb {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 999px;
    background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
    color: #fff;
    box-shadow: 
      0 0 0 1px rgba(96, 165, 250, 0.3),
      0 4px 10px rgba(59, 130, 246, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    transition: transform 0.25s ease, background 0.25s ease, color 0.25s ease;

    svg {
      width: 12px;
      height: 12px;
    }

    &--dark {
      transform: translateX(22px);
      background: linear-gradient(180deg, #10b981 0%, #059669 100%);
      color: #fff;
      box-shadow: 
        0 0 0 1px rgba(16, 185, 129, 0.3),
        0 4px 10px rgba(16, 185, 129, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
  }

  &__auth-body {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 1;
    padding: 28px 0;
  }

  &__panel {
    width: min(100%, 378px);
  }

  &__panel-section + &__panel-section {
    margin-top: 18px;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding-top: 22px;
    border-top: 1px solid rgba(59, 130, 246, 0.2);
    color: #64748b;
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-transform: uppercase;

    div {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    i {
      font-style: normal;
      color: #3b82f6;
    }
  }

  &.dark {
    background: #05070b;
    color: #eef4ff;

    .login-shell__frame {
      background:
        radial-gradient(circle at top right, rgba(96, 165, 250, 0.07), transparent 20%),
        linear-gradient(180deg, #02050d 0%, #030711 100%);
    }

    .login-shell__brand {
      border-right-color: rgba(255, 255, 255, 0.06);
      background:
        linear-gradient(180deg, rgba(4, 9, 20, 0.96), rgba(2, 6, 15, 0.98)),
        linear-gradient(180deg, #030711 0%, #050912 100%);
    }

    .login-shell__brand-name,
    .login-shell__brand-bottom h1,
    .login-shell__brand-bottom h2,
    .login-shell__float-title {
      color: #ffffff;
    }

    .login-shell__brand-sub,
    .login-shell__brand-bottom p,
    .login-shell__board-label,
    .login-shell__board-status,
    .login-shell__float-head,
    .login-shell__float-meta,
    .login-shell__float-foot,
    .login-shell__footer,
    .login-shell__lang {
      color: #7e8aa1;
    }

    .login-shell__board {
      border-color: rgba(51, 64, 92, 0.82);
      background:
        linear-gradient(rgba(57, 73, 104, 0.12) 1px, transparent 1px),
        linear-gradient(90deg, rgba(57, 73, 104, 0.12) 1px, transparent 1px),
        linear-gradient(180deg, rgba(17, 25, 45, 0.98), rgba(12, 18, 33, 0.98));
      box-shadow: 0 40px 90px rgba(0, 0, 0, 0.42);
    }

    .login-shell__chip {
      background: linear-gradient(180deg, rgba(20, 29, 48, 0.98), rgba(16, 23, 40, 0.98));
      box-shadow: 0 16px 34px rgba(37, 99, 235, 0.14);
    }

    .login-shell__float-card {
      border-color: rgba(42, 56, 86, 0.9);
      background: rgba(8, 13, 26, 0.92);
      box-shadow: 0 28px 60px rgba(0, 0, 0, 0.42);
    }

    .login-shell__clear-card {
      border-color: rgba(42, 56, 86, 0.9);
      background: rgba(8, 13, 26, 0.96);
      box-shadow: 0 28px 54px rgba(0, 0, 0, 0.34);
    }

    .login-shell__float-head strong {
      color: #ffffff;
    }

    .login-shell__tag {
      border-color: rgba(58, 70, 98, 0.92);
      background: rgba(8, 13, 26, 0.72);
      color: #dde7f7;
    }

    .login-shell__auth {
      background:
        radial-gradient(circle at top right, rgba(59, 130, 246, 0.05), transparent 20%),
        linear-gradient(180deg, #080a10 0%, #05070d 100%);
    }

    .login-shell__theme-track {
      background: #22304c;
    }

    .login-shell__footer {
      border-top-color: rgba(255, 255, 255, 0.06);
    }
  }
}

@media (max-width: 1439px) {
  .login-shell {
    &__brand-bottom h1 {
      font-size: 48px;
    }

    &__board-wrap {
      width: 460px;
    }

    &__cards-overlay {
      width: 460px;
    }

    &__clear-card {
      &--rf {
        left: 18px;
      }

      &--sync {
        right: 12px;
      }

      &--opt {
        left: 30px;
      }
    }
  }
}

@media (max-width: 1279px) {
  .login-shell {
    &__frame {
      grid-template-columns: 1fr;
    }

    &__auth {
      min-height: 100vh;
      padding: 28px 24px 20px;
    }
  }
}

@media (max-width: 767px) {
  .login-shell {
    &__topbar {
      align-items: flex-start;
      flex-direction: column;
    }

    &__tools {
      width: 100%;
      justify-content: flex-end;
    }

    &__panel {
      width: 100%;
    }

    &__footer {
      align-items: flex-start;
      flex-direction: column;
    }
  }
}

@keyframes ringSpin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
