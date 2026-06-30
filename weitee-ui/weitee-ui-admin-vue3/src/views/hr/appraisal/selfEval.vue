<template>
  <BusinessScaffoldPage
    title="人事管理 / 员工自评"
    description="员工自评弹窗页骨架，后续在此补强制字数校验、草稿保存和提交状态控制。"
    view-path="src/views/hr/appraisal/selfEval.vue"
    :api-paths="['src/api/hr/appraisal/index.ts']"
    :reuse-paths="['src/views/hr/appraisal/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'HrAppraisalSelfEval' })
</script>
