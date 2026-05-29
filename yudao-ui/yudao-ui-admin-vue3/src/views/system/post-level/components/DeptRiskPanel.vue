<template>
  <ContentWrap>
    <template #header>
      <div class="flex items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">组织提醒</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            关注空部门、未分配人员和层级过深问题
          </div>
        </div>
        <el-tag type="warning" effect="plain">{{ tips.length }} 条</el-tag>
      </div>
    </template>
    <el-empty v-if="!tips.length" description="当前没有明显风险提示" />
    <div v-else class="grid gap-8px">
      <el-alert v-for="tip in tips" :key="tip" type="warning" :title="tip" show-icon :closable="false" />
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
defineOptions({ name: 'DeptRiskPanel' })

defineProps<{
  tips: string[]
}>()
</script>
