<template>
  <BusinessScaffoldPage
    title="制造执行管理 / 生产工单大厅"
    description="车间工单和排产入口，前端目录独立为 MES，同时直接复用后端生产工单接口。"
    view-path="src/views/mes/work-order/index.vue"
    :api-paths="['src/api/mes/work-order/index.ts']"
    :reuse-paths="['src/api/erp/mrp/suggest/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'MesWorkOrder' })
</script>
