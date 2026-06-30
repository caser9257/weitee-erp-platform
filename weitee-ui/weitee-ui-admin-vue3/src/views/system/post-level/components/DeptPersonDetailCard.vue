<template>
  <ContentWrap>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">部门详情</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            展示部门、岗位和汇总信息
          </div>
        </div>
        <div v-if="showActions && node && canOperate" class="flex flex-wrap gap-8px">
          <el-button v-if="node.type === 'dept'" type="primary" plain @click="$emit('add-child', node)">
            新增岗位
          </el-button>
          <el-button v-if="node.type === 'dept'" plain @click="$emit('edit', node)">编辑部门</el-button>
          <el-button
            v-if="node.type === 'dept'"
            type="danger"
            plain
            :disabled="!canDelete"
            @click="$emit('delete', node)"
          >
            删除部门
          </el-button>
        </div>
      </div>
    </template>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!node" description="请先在左侧选择一个节点" />
    <div v-else class="grid gap-16px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="名称">{{ node.label }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ nodeTypeLabel }}</el-descriptions-item>
        <el-descriptions-item label="路径" :span="2">{{ node.fullPath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位数">{{ node.postCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="编制总数">{{ node.staffQuota || 0 }}</el-descriptions-item>
        <el-descriptions-item label="在岗人数">{{ node.assignedUserCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="当前层级">{{ node.depth || 0 }}</el-descriptions-item>
        <el-descriptions-item
          v-if="node.type === 'dept' || node.type === 'root'"
          label="直接下级部门"
        >
          {{ node.childDeptCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="node.type === 'dept' || node.type === 'root'"
          label="直接岗位"
        >
          {{ node.directPostCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item v-if="node.type === 'dept' || node.type === 'root'" label="状态">
          <el-tag :type="node.status === 0 ? 'success' : 'info'">
            {{ node.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="node.type === 'root'" label="节点说明" :span="2">
          顶层组织节点，用于查看该中心或一级组织下的岗位编制和岗位分布
        </el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="(node.type === 'dept' || node.type === 'root') && !canDelete"
        type="warning"
        show-icon
        :closable="false"
        title="当前部门存在下级部门或岗位，暂不建议直接删除"
      />
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { PostOrgTreeNodeVO } from '@/api/system/postLevel'

defineOptions({ name: 'DeptPersonDetailCard' })

const props = defineProps<{
  loading: boolean
  node?: PostOrgTreeNodeVO | null
  showActions?: boolean
}>()

defineEmits<{
  (e: 'add-child', node: PostOrgTreeNodeVO): void
  (e: 'edit', node: PostOrgTreeNodeVO): void
  (e: 'delete', node: PostOrgTreeNodeVO): void
}>()

const canOperate = computed(() => props.node?.type === 'dept' || props.node?.type === 'root')
const canDelete = computed(() => {
  if (props.node?.type !== 'dept' && props.node?.type !== 'root') {
    return false
  }
  return (props.node.childDeptCount || 0) === 0 && (props.node.directPostCount || 0) === 0
})

const nodeTypeLabel = computed(() => {
  if (props.node?.type === 'root') return '公司'
  if (props.node?.type === 'dept') return '部门'
  if (props.node?.type === 'post') return '岗位'
  return '-'
})
</script>