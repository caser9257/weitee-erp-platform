<template>
  <BusinessScaffoldPage
    title="人事管理 / 绩效汇总与 ABC 定级"
    description="人事汇总、等级校准和结果归档的业务入口。"
    view-path="src/views/hr/appraisal/summary.vue"
    :api-paths="['src/api/hr/appraisal/index.ts']"
    :reuse-paths="['src/views/hr/appraisal/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'HrAppraisalSummary' })
</script>
