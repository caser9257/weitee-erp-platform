<template>
  <BusinessScaffoldPage
    title="人事管理 / 月度绩效考核中心"
    description="绩效考核计划发起列表骨架，后续在此串联自评、主管评价和人事汇总。"
    view-path="src/views/hr/appraisal/index.vue"
    :api-paths="['src/api/hr/appraisal/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'HrAppraisal' })
</script>
