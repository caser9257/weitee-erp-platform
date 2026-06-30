<template>
  <BusinessScaffoldPage
    title="质量管理 / IQC 来料检验"
    description="来料检验业务入口。当前目录层独立到 QMS，底层优先复用现有采购入库质检 API。"
    view-path="src/views/qms/iqc/index.vue"
    :api-paths="['src/api/qms/iqc/index.ts']"
    :reuse-paths="['src/views/erp/purchase/in-quality/index.vue', 'src/api/erp/purchase/in-quality/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'QmsIqc' })
</script>
