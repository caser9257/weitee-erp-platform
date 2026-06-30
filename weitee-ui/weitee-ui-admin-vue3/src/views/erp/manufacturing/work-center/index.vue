<template>
  <BusinessScaffoldPage
    title="生产管理 / 工作中心"
    description="工作中心菜单已接入前端路由。当前先以兼容页恢复菜单可用性，后续再补齐工位产能、班组和排产能力。"
    view-path="src/views/erp/manufacturing/work-center/index.vue"
    :reuse-paths="['src/views/mes/work-order/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpManufacturingWorkCenter' })
</script>
