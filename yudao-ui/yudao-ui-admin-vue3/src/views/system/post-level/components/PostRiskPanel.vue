<template>
  <ContentWrap>
    <template #header>
      <div class="flex items-center justify-between gap-12px">
        <span class="text-16px font-600">缺口与风险提醒</span>
        <span class="text-13px text-[var(--el-text-color-secondary)]">演示版根据编制与任职自动计算</span>
      </div>
    </template>
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="!riskTips.length" description="当前没有需要关注的风险提醒" />
    <div v-else class="grid gap-10px">
      <el-alert v-for="tip in riskTips" :key="tip" :title="tip" type="warning" :closable="false" show-icon />
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
defineOptions({ name: 'PostRiskPanel' })

defineProps<{
  loading: boolean
  riskTips: string[]
}>()
</script>
