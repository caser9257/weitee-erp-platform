<template>
  <BusinessScaffoldPage
    title="生产管理 / 生产报工"
    description="生产报工菜单已接入前端路由。当前先以兼容页承接，后续再补齐报工明细、工时采集和异常提报。"
    view-path="src/views/erp/manufacturing/production-report/index.vue"
    :reuse-paths="['src/views/mes/pda-execute/report.vue', 'src/views/mes/work-order/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpManufacturingProductionReport' })
</script>
