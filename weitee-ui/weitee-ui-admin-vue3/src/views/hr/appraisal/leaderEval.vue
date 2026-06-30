<template>
  <BusinessScaffoldPage
    title="人事管理 / 主管评分评价"
    description="主管评分和评价页骨架，后续在此补评分规则、状态切换和意见沉淀。"
    view-path="src/views/hr/appraisal/leaderEval.vue"
    :api-paths="['src/api/hr/appraisal/index.ts']"
    :reuse-paths="['src/views/hr/appraisal/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'HrAppraisalLeaderEval' })
</script>
