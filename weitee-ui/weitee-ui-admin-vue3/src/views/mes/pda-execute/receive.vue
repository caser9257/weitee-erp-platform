<template>
  <BusinessScaffoldPage
    title="MES PDA / 扫码领退料终端"
    description="面向工业平板和 PDA 的领料、退料终端骨架，后续补大字号卡片、扫码流和离线提示。"
    view-path="src/views/mes/pda-execute/receive.vue"
    :api-paths="['src/api/mes/pda-execute/index.ts']"
    :reuse-paths="['src/views/erp/stock/out/index.vue', 'src/views/erp/stock/in/index.vue']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'MesPdaReceive' })
</script>
