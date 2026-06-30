<template>
  <ContentWrap>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <span class="text-16px font-600">岗位详情</span>
        <div v-if="showActions && detail" class="flex flex-wrap gap-8px">
          <el-button type="primary" plain @click="$emit('edit', detail.postId)">编辑岗位</el-button>
          <el-button plain @click="$emit('assign', detail.postId)">分配人员</el-button>
        </div>
      </div>
    </template>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!detail" description="请选择左侧岗位查看详情" />
    <div v-else class="grid gap-16px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="岗位编码">{{ detail.code }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ detail.level || '未分级' }}</el-descriptions-item>
        <el-descriptions-item label="编制人数">{{ detail.staffQuota }}</el-descriptions-item>
        <el-descriptions-item label="在岗人数">{{ detail.assignedUserCount }}</el-descriptions-item>
        <el-descriptions-item label="主岗人数">{{ detail.primaryUserCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 0 ? 'success' : 'info'">{{ detail.status === 0 ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="岗位说明" :span="2">{{ detail.jobDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail.changeLogs?.length">
        <div class="mb-8px text-14px font-600">最近变动</div>
        <el-timeline>
          <el-timeline-item
            v-for="item in detail.changeLogs"
            :key="`${item.userId}-${item.actionTime}`"
            :timestamp="item.actionTime"
            placement="top"
          >
            {{ item.nickname || '未知人员' }} - {{ item.action }}{{ item.remark ? `（${item.remark}）` : '' }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { PostLevelDetailVO } from '@/api/system/postLevel'

defineOptions({ name: 'PostDetailCard' })

defineProps<{
  loading: boolean
  detail?: PostLevelDetailVO | null
  showActions?: boolean
}>()

defineEmits<{
  (e: 'edit', postId: number): void
  (e: 'assign', postId: number): void
}>()
</script>
