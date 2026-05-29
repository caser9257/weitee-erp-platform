<template>
  <BusinessScaffoldPage
    title="质量管理 / OQC 完工与出货检验"
    description="完工检验、发货检验证明和签署归档的业务入口，先补目录骨架，后续接入出库与发货流程。"
    view-path="src/views/qms/oqc/index.vue"
    :api-paths="['src/api/qms/oqc/index.ts']"
    :reuse-paths="['src/views/erp/sale/out/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'QmsOqc' })
</script>
