<template>
  <BusinessScaffoldPage
    title="生产管理 / 工艺路线"
    description="工艺路线菜单已接入前端路由。当前先复用兼容页承接，后续再补齐真实工序、工位和工艺版本管理。"
    view-path="src/views/erp/manufacturing/process-route/index.vue"
    :reuse-paths="['src/views/erp/route/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpManufacturingProcessRoute' })
</script>
