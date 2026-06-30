<template>
  <BusinessScaffoldPage
    title="生产管理 / 设备台账"
    description="设备台账菜单已接入前端路由。当前先以兼容页承接，后续再补齐点检、维保和设备状态联动。"
    view-path="src/views/erp/manufacturing/device/index.vue"
    :reuse-paths="['src/views/mes/equipment/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'ErpManufacturingDevice' })
</script>
