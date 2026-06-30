<template>
  <BusinessScaffoldPage
    title="研发管理 / 文档与图纸库"
    description="研发文档、图纸和军民品隔离资料的目录骨架，后续在此补文件权限、版本和分类能力。"
    view-path="src/views/erp/rd/document/index.vue"
    :api-paths="['src/api/erp/rd/document/index.ts']"
    :reuse-paths="['src/views/erp/rd/design/index.vue', 'src/api/infra/file/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpRdDocument' })
</script>
