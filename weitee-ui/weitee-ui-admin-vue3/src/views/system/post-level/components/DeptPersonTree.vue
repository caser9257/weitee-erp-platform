<template>
  <ContentWrap class="h-full">
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">部门人员树</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            公司 - 中心 - 部门 - 组 - 人员
          </div>
        </div>
        <el-tag v-if="selectedKey" type="success" effect="plain">已选中</el-tag>
      </div>
    </template>
    <el-input v-model="keyword" clearable placeholder="搜索部门或人员" class="mb-12px">
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!treeData.length" description="暂无部门人员数据" />
    <el-tree
      v-else
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      node-key="id"
      highlight-current
      default-expand-all
      :expand-on-click-node="false"
      :filter-node-method="filterNode"
      @node-click="handleNodeClick"
    >
      <template #default="{ data }">
        <div class="flex w-full items-center justify-between gap-8px">
          <div class="flex min-w-0 items-center gap-6px overflow-hidden">
            <Icon :icon="nodeIcon(data.type)" :color="nodeColor(data.type)" />
            <span class="truncate">{{ data.label }}</span>
          </div>
          <span class="whitespace-nowrap text-12px text-[var(--el-text-color-secondary)]">
            <template v-if="data.type === 'dept'">
              部门 {{ data.childDeptCount || 0 }} / 人员 {{ data.directUserCount || 0 }}
            </template>
            <template v-else-if="data.type === 'bucket'">
              {{ data.userCount || 0 }} 人
            </template>
            <template v-else-if="data.type === 'user'">
              人员
            </template>
            <template v-else>
              公司
            </template>
          </span>
        </div>
      </template>
    </el-tree>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import type { DeptPersonTreeNodeVO } from '@/api/system/deptPerson'

defineOptions({ name: 'DeptPersonTree' })

const props = defineProps<{
  loading: boolean
  treeData: DeptPersonTreeNodeVO[]
  selectedKey?: string
}>()

const emit = defineEmits<{
  (e: 'node-click', node: DeptPersonTreeNodeVO): void
}>()

const keyword = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeProps = {
  children: 'children',
  label: 'label'
}

watch(
  () => props.selectedKey,
  (value) => {
    if (value) {
      treeRef.value?.setCurrentKey(value)
    }
  },
  { immediate: true }
)

watch(keyword, (value) => treeRef.value?.filter(value))

const filterNode = (value: string, data: DeptPersonTreeNodeVO) => {
  if (!value) return true
  const keywordValue = value.trim().toLowerCase()
  return [data.label, data.fullPath, data.nickname, data.username]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(keywordValue))
}

const nodeIcon = (type?: string) => {
  if (type === 'dept') return 'ep:folder'
  if (type === 'bucket') return 'ep:user-filled'
  if (type === 'user') return 'ep:user'
  return 'ep:office-building'
}

const nodeColor = (type?: string) => {
  if (type === 'dept') return '#0f766e'
  if (type === 'bucket') return '#b45309'
  if (type === 'user') return '#2563eb'
  return '#1d4ed8'
}

const handleNodeClick = (node: DeptPersonTreeNodeVO) => {
  emit('node-click', node)
}
</script>
