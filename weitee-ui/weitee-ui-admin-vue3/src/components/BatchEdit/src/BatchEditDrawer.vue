<template>
  <el-drawer
    v-model="visibleProxy"
    :size="drawerSize"
    :show-close="false"
    :with-header="false"
    :destroy-on-close="true"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    append-to-body
    class="batch-edit-drawer"
    modal-class="batch-edit-drawer__mask"
    @closed="emit('closed')"
  >
    <div class="batch-edit-drawer__shell">
      <div class="batch-edit-drawer__head">
        <div class="batch-edit-drawer__head-main">
          <div class="batch-edit-drawer__head-icon">
            <Icon icon="ep:edit" />
          </div>
          <div class="batch-edit-drawer__head-copy">
            <div class="batch-edit-drawer__title">{{ title }}</div>
            <div class="batch-edit-drawer__count">已选择 {{ selectedCount }} 条</div>
          </div>
        </div>
        <button
          type="button"
          class="batch-edit-drawer__close"
          :disabled="loading"
          @click="handleClose"
        >
          <Icon icon="ep:close" />
        </button>
      </div>

      <div class="batch-edit-drawer__body">
        <slot name="context"></slot>

        <div class="batch-edit-drawer__summary">
          <div class="batch-edit-drawer__summary-label">当前已选</div>
          <div class="batch-edit-drawer__summary-value">{{ selectedCount }}</div>
        </div>

        <div class="batch-edit-drawer__panel">
          <slot></slot>
        </div>
      </div>

      <div class="batch-edit-drawer__footer">
        <slot name="footer">
          <el-button :disabled="loading" @click="handleClose">{{ cancelText }}</el-button>
          <el-button
            type="primary"
            :disabled="confirmDisabled || loading"
            :loading="loading"
            @click="emit('confirm')"
          >
            {{ confirmText }}
          </el-button>
        </slot>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWindowSize } from '@vueuse/core'

defineOptions({ name: 'BatchEditDrawer' })

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    selectedCount: number
    loading?: boolean
    confirmDisabled?: boolean
    confirmText?: string
    cancelText?: string
  }>(),
  {
    title: '批量编辑',
    loading: false,
    confirmDisabled: false,
    confirmText: '提交',
    cancelText: '取消'
  }
)

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'confirm'): void
  (event: 'closed'): void
}>()

const { width } = useWindowSize()

const visibleProxy = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const drawerSize = computed(() => {
  if (width.value <= 1024) {
    return '100%'
  }
  if (width.value <= 1440) {
    return '960px'
  }
  return '1080px'
})

const handleClose = () => {
  if (props.loading) {
    return
  }
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.batch-edit-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
    overflow: hidden;
  }
}

.batch-edit-drawer__mask {
  backdrop-filter: blur(8px);
}

.batch-edit-drawer__shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  background: #f8fafc;
}

.batch-edit-drawer__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.batch-edit-drawer__head-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.batch-edit-drawer__head-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
}

.batch-edit-drawer__head-copy {
  min-width: 0;
}

.batch-edit-drawer__title {
  color: #0f172a;
  font-size: 18px;
  line-height: 28px;
  font-weight: 800;
}

.batch-edit-drawer__count {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.batch-edit-drawer__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #94a3b8;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.batch-edit-drawer__close:hover:not(:disabled) {
  background: #f1f5f9;
  color: #475569;
}

.batch-edit-drawer__close:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.batch-edit-drawer__body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.batch-edit-drawer__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 18px;
  background: #0f172a;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);
}

.batch-edit-drawer__summary-label {
  color: #94a3b8;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.batch-edit-drawer__summary-value {
  color: #60a5fa;
  font-size: 34px;
  line-height: 1;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.batch-edit-drawer__panel {
  margin-top: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  overflow: hidden;
}

.batch-edit-drawer__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 18px 24px;
  border-top: 1px solid #e2e8f0;
  background: #fff;
}

@media (max-width: 1024px) {
  .batch-edit-drawer__head,
  .batch-edit-drawer__summary,
  .batch-edit-drawer__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .batch-edit-drawer__body {
    padding: 16px;
  }

  .batch-edit-drawer__summary-value {
    font-size: 28px;
  }
}
</style>
