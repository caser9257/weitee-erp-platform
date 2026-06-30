<template>
  <BusinessScaffoldPage
    title="制造执行管理 / 设备台账与点检"
    description="设备台账、维保、点检电子化的业务入口，目录先独立，后续按设备管理能力逐步补齐。"
    view-path="src/views/mes/equipment/index.vue"
    :api-paths="['src/api/mes/equipment/index.ts']"
    :reuse-paths="['src/views/iot/device/device/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'MesEquipment' })
</script>
