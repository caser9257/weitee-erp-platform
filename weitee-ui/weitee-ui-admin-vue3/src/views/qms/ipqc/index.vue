<template>
  <BusinessScaffoldPage
    title="质量管理 / IPQC 过程检验"
    description="过程检验和上下工序卡控的前端骨架，后续在此接入巡检、抽检和工序拦截能力。"
    view-path="src/views/qms/ipqc/index.vue"
    :api-paths="['src/api/qms/ipqc/index.ts']"
    :reuse-paths="['src/views/mes/work-order/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'QmsIpqc' })
</script>
