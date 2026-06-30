<template>
  <Dialog
    v-model="dialogVisible"
    :title="title"
    :width="width"
    scroll
    :maxHeight="maxHeight"
    custom-class="finance-shell__print-dialog"
    @closed="emit('closed')"
  >
    <div :id="printAreaId" v-loading="loading" class="finance-shell__print-shell">
      <slot name="context"></slot>
      <div class="finance-shell__print-body">
        <slot></slot>
      </div>
    </div>

    <template #footer>
      <div class="finance-shell__print-footer">
        <slot name="footer"></slot>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
const dialogVisible = defineModel<boolean>({ required: true })
const emit = defineEmits<{
  closed: []
}>()

withDefaults(
  defineProps<{
    title: string
    width?: string
    maxHeight?: string
    printAreaId: string
    loading?: boolean
  }>(),
  {
    width: 'min(1080px, 96vw)',
    maxHeight: '80vh',
    loading: false
  }
)
</script>

<style>
@import './readOnlyPage.css';
</style>

<style scoped>
.finance-shell__print-shell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
}

.finance-shell__print-body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
  padding: 12px 12px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.finance-shell__print-body :deep(.finance-shell__section) {
  margin-top: 0;
  padding: 12px 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.finance-shell__print-body :deep(.finance-shell__section-head) {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .finance-shell__print-shell,
  .finance-shell__print-body {
    gap: 12px;
  }

  .finance-shell__print-footer {
    width: 100%;
  }
}

.finance-shell__print-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
  width: 100%;
  padding-top: 0;
}

.finance-shell__print-footer :deep(.el-button) {
  min-width: 92px;
}

:global(.finance-shell__print-dialog .el-dialog__body) {
  padding: 12px 12px 10px;
}
</style>
