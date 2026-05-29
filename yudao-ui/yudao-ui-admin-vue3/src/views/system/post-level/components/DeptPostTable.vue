<template>
  <ContentWrap>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">岗位列表</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            展示当前节点及其下级节点下的岗位
          </div>
        </div>
        <el-tag v-if="posts.length" type="success" effect="plain">{{ posts.length }} 个岗位</el-tag>
      </div>
    </template>
    <el-table
      v-loading="loading"
      :data="posts"
      empty-text="当前节点暂无岗位"
      @row-click="handleRowClick"
    >
      <el-table-column prop="label" label="岗位名称" min-width="150" />
      <el-table-column prop="level" label="层级" min-width="110" />
      <el-table-column prop="fullPath" label="所在路径" min-width="220" show-overflow-tooltip />
      <el-table-column prop="staffQuota" label="编制" width="100" />
      <el-table-column label="在岗" width="100">
        <template #default="{ row }">
          {{ row.assignedUserCount || 0 }}
        </template>
      </el-table-column>
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
import type { PostOrgTreeNodeVO } from '@/api/system/postLevel'

defineOptions({ name: 'DeptPostTable' })

defineProps<{
  loading: boolean
  posts: PostOrgTreeNodeVO[]
}>()

const emit = defineEmits<{
  (e: 'row-click', row: PostOrgTreeNodeVO): void
}>()

const handleRowClick = (row: PostOrgTreeNodeVO) => {
  emit('row-click', row)
}
</script>