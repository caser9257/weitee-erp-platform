<template>
  <ContentWrap>
    <template #header>
      <div class="flex items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">直属人员</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            展示当前节点下直接挂靠的人员
          </div>
        </div>
        <el-tag v-if="users.length" type="success" effect="plain">{{ users.length }} 人</el-tag>
      </div>
    </template>
    <el-table v-loading="loading" :data="users" empty-text="当前节点暂无人员">
      <el-table-column prop="nickname" label="姓名" min-width="120" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="140" />
      <el-table-column prop="fullPath" label="所属路径" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">
            {{ row.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { DeptPersonTreeNodeVO } from '@/api/system/deptPerson'

defineOptions({ name: 'DeptPersonUserTable' })

defineProps<{
  loading: boolean
  users: DeptPersonTreeNodeVO[]
}>()
</script>
