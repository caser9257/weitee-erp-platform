<template>
  <ContentWrap>
    <template #header>
      <div class="flex items-center justify-between gap-12px">
        <span class="text-16px font-600">任职人员</span>
        <el-button v-if="showAssignAction" type="primary" plain @click="$emit('assign')">
          <Icon icon="ep:user-filled" class="mr-4px" />
          分配人员
        </el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="users" empty-text="当前岗位暂无任职人员">
      <el-table-column prop="nickname" label="姓名" min-width="120" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="140" />
      <el-table-column label="任职类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.primary ? 'danger' : 'info'">{{ row.primary ? '主岗' : '兼岗' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始时间" min-width="160" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { PostLevelAssignedUserVO } from '@/api/system/postLevel'

defineOptions({ name: 'PostAssignedUserTable' })

defineProps<{
  loading: boolean
  users: PostLevelAssignedUserVO[]
  showAssignAction?: boolean
}>()

defineEmits<{
  (e: 'assign'): void
}>()
</script>
