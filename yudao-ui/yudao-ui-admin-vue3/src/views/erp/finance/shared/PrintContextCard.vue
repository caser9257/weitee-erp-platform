<template>
  <div class="print-context-card">
    <div class="print-context-card__main">
      <div class="print-context-card__headline">
        <div class="print-context-card__titles">
          <div class="print-context-card__title">{{ title }}</div>
          <div v-if="subtitle" class="print-context-card__subtitle">{{ subtitle }}</div>
        </div>
        <el-tag v-if="statusLabel" :type="statusType" effect="light" round>
          {{ statusLabel }}
        </el-tag>
      </div>
      <div v-if="summaryItems.length" class="print-context-card__summary">
        <div v-for="item in summaryItems" :key="item.label" class="print-context-card__summary-item">
          <div class="print-context-card__summary-label">{{ item.label }}</div>
          <div class="print-context-card__summary-value">{{ item.value || '-' }}</div>
        </div>
      </div>
      <div v-if="metaItems.length" class="print-context-card__meta-list">
        <span
          v-for="(item, index) in metaItems"
          :key="`${index}-${item}`"
          class="print-context-card__meta-item"
        >
          {{ item }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PrintContextCard' })

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    summaryItems?: Array<{ label: string; value: string }>
    metaItems?: string[]
    statusLabel?: string
    statusType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  }>(),
  {
    summaryItems: () => [],
    metaItems: () => [],
    statusType: 'info'
  }
)
</script>

<style scoped>
.print-context-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.14), transparent 36%),
    linear-gradient(135deg, #0f172a 0%, #111827 58%, #0b1220 100%);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.16);
  color: #fff;
}

.print-context-card::before {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(96, 165, 250, 0.12), transparent 28%);
  content: '';
  pointer-events: none;
}

.print-context-card__main {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
}

.print-context-card__headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px 16px;
  flex-wrap: wrap;
}

.print-context-card__titles {
  min-width: 0;
  flex: 1;
}

.print-context-card__title {
  overflow: hidden;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.print-context-card__subtitle {
  margin-top: 4px;
  color: #cbd5e1;
  font-size: 12px;
  line-height: 1.5;
}

.print-context-card__summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.print-context-card__summary-item {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.24);
}

.print-context-card__summary-label {
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.4;
}

.print-context-card__summary-value {
  overflow: hidden;
  margin-top: 4px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.print-context-card__meta-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.print-context-card__meta-item {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.24);
  color: #e2e8f0;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .print-context-card__main {
    padding: 16px;
  }

  .print-context-card__title {
    white-space: normal;
  }

  .print-context-card__summary {
    grid-template-columns: 1fr;
  }
}
</style>
