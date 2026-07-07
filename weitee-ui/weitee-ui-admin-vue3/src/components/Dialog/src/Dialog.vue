<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { isNumber } from '@/utils/is'

defineOptions({ name: 'Dialog' })

const slots = useSlots()
const emits = defineEmits(['update:modelValue'])

const props = defineProps({
  modelValue: propTypes.bool.def(false),
  title: propTypes.string.def('Dialog'),
  fullscreen: propTypes.bool.def(true),
  width: propTypes.oneOfType([String, Number]).def('40%'),
  scroll: propTypes.bool.def(false), // 是否开启滚动条。如果是的话，按照 maxHeight 设置最大高度
  maxHeight: propTypes.oneOfType([String, Number]).def('400px')
})

// ==========================================================
// 表单校验错误横幅 + 自动聚焦
// 当 Dialog 内 el-form-item 出现 .is-error 时自动触发
// ==========================================================
const showErrorBanner = ref(false)
const errorMessage = ref('请完善必填信息后再提交')
let errorObserver: MutationObserver | null = null
let prevErrorCount = 0 // 上一次检测到的错误字段数，用于区分新增/移除
const dialogBodyRef = ref<HTMLElement | null>(null)

function setupErrorObserver(bodyEl: Element) {
  tearDownErrorObserver()
  showErrorBanner.value = false
  prevErrorCount = 0

  errorObserver = new MutationObserver(() => {
    nextTick(() => {
      const currentCount = bodyEl.querySelectorAll('.el-form-item.is-error').length
      if (currentCount > 0 && prevErrorCount === 0) {
        // 错误从无到有 → 显示横幅 + 聚焦第一个错误字段
        showErrorBanner.value = true
        const firstError = bodyEl.querySelector('.el-form-item.is-error')
        if (firstError) {
          const input = firstError.querySelector<HTMLElement>(
            'input:not([type="hidden"]):not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
          )
          input?.focus()
        }
      } else if (currentCount === 0 && prevErrorCount > 0) {
        // 所有错误已修正 → 收起横幅
        showErrorBanner.value = false
      }
      prevErrorCount = currentCount
    })
  })

  errorObserver.observe(bodyEl, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ['class']
  })
}

function tearDownErrorObserver() {
  errorObserver?.disconnect()
  errorObserver = null
  prevErrorCount = 0
}

const elDialogRef = ref<InstanceType<typeof ElDialog> | null>(null)

function onDialogOpened() {
  nextTick(() => {
    const bodyEl = dialogBodyRef.value
    if (bodyEl) {
      setupErrorObserver(bodyEl)
    }
  })
}

// 点击表单字段时自动收起错误横幅
const FORM_FIELD_SELECTOR = '.el-form-item, .el-input, .el-select, .el-textarea, .el-date-editor, .el-radio, .el-checkbox, .el-switch, .el-tree-select'
function onBodyClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target.closest(FORM_FIELD_SELECTOR)) {
    showErrorBanner.value = false
  }
}

onUnmounted(() => {
  tearDownErrorObserver()
})

const getBindValue = computed(() => {
  const delArr: string[] = ['fullscreen', 'title', 'maxHeight', 'appendToBody']
  const attrs = useAttrs()
  const obj = { ...attrs, ...props }
  for (const key in obj) {
    if (delArr.indexOf(key) !== -1) {
      delete obj[key]
    }
  }
  return obj
})

const isFullscreen = ref(false)

const toggleFull = () => {
  isFullscreen.value = !unref(isFullscreen)
}

const dialogHeight = ref(isNumber(props.maxHeight) ? `${props.maxHeight}px` : props.maxHeight)

watch(
  () => isFullscreen.value,
  async (val: boolean) => {
    await nextTick()
    if (val) {
      const windowHeight = document.documentElement.offsetHeight
      dialogHeight.value = `${windowHeight - 55 - 60 - (slots.footer ? 63 : 0)}px`
    } else {
      dialogHeight.value = isNumber(props.maxHeight) ? `${props.maxHeight}px` : props.maxHeight
    }
  },
  {
    immediate: true
  }
)

const dialogStyle = computed(() => {
  return {
    height: unref(dialogHeight)
  }
})

const closing = ref(false)

function closeHandler() {
  emits('update:modelValue', false)
  closing.value = true
}

function closedHandler() {
  closing.value = false
  showErrorBanner.value = false
  tearDownErrorObserver()
}
</script>

<template>
  <ElDialog
    ref="elDialogRef"
    v-bind="getBindValue"
    :close-on-click-modal="true"
    :fullscreen="isFullscreen"
    :width="width"
    destroy-on-close
    lock-scroll
    draggable
    class="com-dialog"
    :show-close="false"
    @opened="onDialogOpened"
    @close="closeHandler"
    @closed="closedHandler"
  >
    <template #header="{ close }">
      <div class="relative h-54px flex items-center justify-between pl-15px pr-15px">
        <slot name="title">
          {{ title }}
        </slot>
        <div
          class="absolute right-15px top-[50%] h-54px flex translate-y-[-50%] items-center justify-between"
        >
          <Icon
            v-if="fullscreen"
            class="is-hover mr-10px cursor-pointer"
            :icon="isFullscreen ? 'radix-icons:exit-full-screen' : 'radix-icons:enter-full-screen'"
            color="var(--el-color-info)"
            hover-color="var(--el-color-primary)"
            @click="toggleFull"
          />
          <Icon
            class="is-hover cursor-pointer"
            icon="ep:close"
            hover-color="var(--el-color-primary)"
            color="var(--el-color-info)"
            @click.stop="close"
          />
        </div>
      </div>
    </template>

    <ElScrollbar v-if="scroll" :style="dialogStyle">
      <div ref="dialogBodyRef" class="dialog-body-wrapper" @click="onBodyClick">
        <ElAlert
          v-if="showErrorBanner"
          class="mb-15px"
          :title="errorMessage"
          type="error"
          show-icon
          closable
          @close="showErrorBanner = false"
        />
        <slot></slot>
      </div>
    </ElScrollbar>
    <template v-else>
      <div ref="dialogBodyRef" class="dialog-body-wrapper" @click="onBodyClick">
        <ElAlert
          v-if="showErrorBanner"
          class="mb-15px"
          :title="errorMessage"
          type="error"
          show-icon
          closable
          @close="showErrorBanner = false"
        />
        <slot></slot>
      </div>
    </template>
    <template v-if="slots.footer" #footer>
      <div :style="{ 'pointer-events': closing ? 'none' : 'auto' }">
        <slot name="footer"></slot>
      </div>
    </template>
  </ElDialog>
</template>

<style lang="scss">
.com-dialog {
  .#{$elNamespace}-overlay-dialog {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .#{$elNamespace}-dialog {
    margin: 0 !important;

    &__header {
      height: 54px;
      padding: 0;
      margin-right: 0 !important;
      border-bottom: 1px solid var(--el-border-color);
    }

    &__body {
      padding: 15px !important;
    }

    &__footer {
      border-top: 1px solid var(--el-border-color);
    }

    &__headerbtn {
      top: 0;
    }
  }
}
</style>
