<template>
  <BusinessScaffoldPage
    title="工艺管理 / 工艺路线与 SOP 配置"
    description="工艺路线、工序步骤和 SOP 规范配置的前端入口，直接复用现有工艺路线 API。"
    view-path="src/views/erp/route/index.vue"
    :api-paths="['src/api/erp/route/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpRoute' })
</script>
