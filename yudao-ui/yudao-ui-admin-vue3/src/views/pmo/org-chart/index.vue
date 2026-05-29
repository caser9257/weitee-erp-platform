<template>
  <BusinessScaffoldPage
    title="组织架构"
    description="综合计划管理下的组织与岗位入口，后续复用部门、岗位和人员主数据。"
    view-path="src/views/pmo/org-chart/index.vue"
    :api-paths="['src/api/pmo/org-chart/index.ts']"
    :reuse-paths="['src/views/system/dept/index.vue', 'src/views/system/post/index.vue', 'src/views/system/user/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'PmoOrgChart' })
</script>
