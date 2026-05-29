<template>
  <ContentWrap class="dept-post-tree h-full">
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <div>
          <div class="text-16px font-600">组织岗位树</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            公司 / 中心 / 部门 / 岗位
          </div>
        </div>
        <el-tag v-if="selectedKey" type="success" effect="plain">已选中</el-tag>
      </div>
    </template>
    <el-input v-model="keyword" clearable placeholder="搜索部门或岗位" class="mb-12px">
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>
    <div class="dept-post-tree__body">
      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="!treeData.length" description="暂无组织岗位数据" />
      <div v-else class="dept-post-tree__scroll">
        <el-tree
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
                <template v-if="data.type === 'dept' || data.type === 'root'">
                  部门 {{ data.childDeptCount || 0 }} / 岗位 {{ data.postCount || 0 }}
                </template>
                <template v-else-if="data.type === 'post'">
                  编制 {{ data.staffQuota || 0 }} / 在岗 {{ data.assignedUserCount || 0 }}
                </template>
                <template v-else>
                  公司
                </template>
              </span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import type { PostOrgTreeNodeVO } from '@/api/system/postLevel'

defineOptions({ name: 'DeptPostTree' })

const props = defineProps<{
  loading: boolean
  treeData: PostOrgTreeNodeVO[]
  selectedKey?: string
}>()

const emit = defineEmits<{
  (e: 'node-click', node: PostOrgTreeNodeVO): void
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
    treeRef.value?.setCurrentKey(value)
    nextTick(() => {
      const currentNode = (treeRef.value as any)?.$el?.querySelector('.el-tree-node.is-current') as
        | HTMLElement
        | undefined
      currentNode?.scrollIntoView({ block: 'center' })
    })
  },
  { immediate: true }
)

watch(keyword, (value) => treeRef.value?.filter(value))

const filterNode = (value: string, data: PostOrgTreeNodeVO) => {
  if (!value) return true
  const keywordValue = value.trim().toLowerCase()
  return [data.label, data.fullPath]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(keywordValue))
}

const nodeIcon = (type?: string) => {
  if (type === 'dept') return 'ep:folder'
  if (type === 'post') return 'ep:briefcase'
  return 'ep:office-building'
}

const nodeColor = (type?: string) => {
  if (type === 'dept') return '#0f766e'
  if (type === 'post') return '#2563eb'
  return '#1d4ed8'
}

const handleNodeClick = (node: PostOrgTreeNodeVO) => {
  emit('node-click', node)
}
</script>

<style lang="scss" scoped>
.dept-post-tree {
  :deep(.el-card) {
    height: 100%;
  }

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
  }
}

.dept-post-tree__body {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.dept-post-tree__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}
</style>
