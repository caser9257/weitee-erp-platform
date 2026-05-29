<template>
  <div class="finance-shell__state" :class="`finance-shell__state--${tone}`">
    <div class="finance-shell__state-icon" :class="{ 'finance-shell__state-icon--spin': spinning }">
      <Icon :icon="icon" />
    </div>
    <div class="finance-shell__state-main">
      <div class="finance-shell__state-title">{{ title }}</div>
      <div v-if="description" class="finance-shell__state-desc">{{ description }}</div>
      <div v-if="$slots.action" class="finance-shell__state-actions">
        <slot name="action"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PrintStatePanel' })

withDefaults(
  defineProps<{
    title: string
    description?: string
    icon?: string
    tone?: 'primary' | 'success' | 'warning' | 'danger' | 'neutral'
    spinning?: boolean
  }>(),
  {
    icon: 'ep:document',
    tone: 'neutral',
    spinning: false
  }
)
</script>

<style scoped>
.finance-shell__state {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
  padding: 16px 18px;
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.finance-shell__state-icon {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 22px;
}

.finance-shell__state-icon--spin {
  animation: finance-shell-spin 1s linear infinite;
}

.finance-shell__state-main {
  min-width: 0;
  flex: 1;
}

.finance-shell__state-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.5;
}

.finance-shell__state-desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.finance-shell__state-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.finance-shell__state-actions :deep(.el-button) {
  min-width: 92px;
}

.finance-shell__state--success .finance-shell__state-icon {
  background: #ecfdf5;
  color: #059669;
}

.finance-shell__state--warning .finance-shell__state-icon {
  background: #fffbeb;
  color: #d97706;
}

.finance-shell__state--danger .finance-shell__state-icon {
  background: #fff1f2;
  color: #e11d48;
}

.finance-shell__state--neutral .finance-shell__state-icon {
  background: #eff6ff;
  color: #2563eb;
}

@media print {
  .finance-shell__state {
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    border-radius: 10px;
    box-shadow: none;
  }

  .finance-shell__state-icon {
    width: 24px;
    height: 24px;
    border-radius: 8px;
    font-size: 14px;
  }

  .finance-shell__state-title {
    font-size: 12px;
  }

  .finance-shell__state-desc {
    margin-top: 1px;
    font-size: 10px;
  }

  .finance-shell__state-actions {
    margin-top: 6px;
  }
}

@keyframes finance-shell-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
