<template>
  <BusinessScaffoldPage
    title="经营指标看板"
    description="综合计划管理下的经营分析入口，后续汇总销售、交付、成本和绩效指标。"
    view-path="src/views/pmo/kpi/index.vue"
    :api-paths="['src/api/pmo/kpi/index.ts']"
    :reuse-paths="['src/views/erp/home/index.vue', 'src/views/erp/finance/receipt/index.vue', 'src/views/erp/finance/payment/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'PmoKpi' })
</script>
