<template>
  <div class="print-attachment" :class="{ 'print-attachment--compact': compact }">
    <PrintStatePanel
      v-if="loading"
      title="正在加载来源附件"
      icon="ep:loading"
      tone="primary"
      spinning
    />
    <PrintStatePanel
      v-else-if="errorMessage"
      title="来源附件加载失败"
      :description="errorMessage"
      icon="ep:warning-filled"
      tone="danger"
    >
      <template #action>
        <el-button type="primary" plain @click="emit('retry')">重试加载</el-button>
      </template>
    </PrintStatePanel>
    <div v-else-if="attachments.length" class="print-attachment__content">
      <div v-if="compact" class="print-attachment__compact-head">
        <span class="print-attachment__compact-label">来源附件</span>
      </div>
      <div class="print-attachment__list">
        <div v-for="item in attachments" :key="item.url" class="print-attachment__item">
          <span class="print-attachment__icon">
            <Icon icon="ep:paperclip" />
          </span>
          <el-link
            class="print-attachment__link"
            :href="item.url"
            target="_blank"
            download
            type="primary"
            :underline="false"
          >
            {{ item.name }}
          </el-link>
        </div>
      </div>
    </div>
    <PrintStatePanel v-else title="暂无来源附件" icon="ep:folder-opened" />
  </div>
</template>

<script setup lang="ts">
import PrintStatePanel from '@/views/erp/finance/shared/PrintStatePanel.vue'

defineOptions({ name: 'PrintAttachmentList' })

export interface PrintAttachmentItem {
  name: string
  url: string
}

const emit = defineEmits<{
  retry: []
}>()

withDefaults(
  defineProps<{
    attachments: PrintAttachmentItem[]
    loading?: boolean
    errorMessage?: string
    compact?: boolean
  }>(),
  {
    loading: false,
    compact: false
  }
)
</script>

<style scoped>
.print-attachment {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  padding-bottom: 0;
}

.print-attachment__content {
  min-width: 0;
}

.print-attachment__compact-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.print-attachment__compact-label {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
}

.print-attachment__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 1px 2px;
}

.print-attachment__item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 9px 10px;
  border: 1px solid #dbe4f0;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  color: #475569;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
  break-inside: avoid;
  page-break-inside: avoid;
}

.print-attachment__item:hover {
  border-color: #cbd5e1;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.print-attachment__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  flex-shrink: 0;
}

.print-attachment__link {
  min-width: 0;
  font-weight: 600;
  word-break: break-word;
}

@media (max-width: 768px) {
  .print-attachment__item {
    padding: 9px 10px;
  }
}

.print-attachment--compact {
  gap: 0;
  padding-bottom: 0;
}

.print-attachment--compact .print-attachment__content {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.print-attachment--compact .print-attachment__compact-head {
  margin-bottom: 6px;
}

.print-attachment--compact .print-attachment__list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  padding: 0;
}

.print-attachment:not(.print-attachment--compact) .print-attachment__list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 8px;
  padding: 2px;
}

.print-attachment--compact .print-attachment__item {
  flex: 0 1 auto;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.print-attachment--compact .print-attachment__icon {
  width: 18px;
  height: 18px;
  border: 0;
  background: transparent;
  color: #2563eb;
}
</style>
