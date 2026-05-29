<template>
  <BusinessScaffoldPage
    title="人事管理 / 员工档案"
    description="正式、实习、外派员工档案业务入口，目录独立，底层先复用现有员工档案 API。"
    view-path="src/views/hr/archive/index.vue"
    :api-paths="['src/api/hr/archive/index.ts']"
    :reuse-paths="['src/views/hr/employee/index.vue', 'src/api/hr/employee/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'HrArchive' })
</script>
