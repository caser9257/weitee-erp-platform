<template>
  <ContentWrap class="h-full">
    <template #header>
      <div class="text-16px font-600">岗位层级体系</div>
    </template>
    <el-input v-model="keyword" clearable placeholder="搜索岗位层级或岗位名称" class="mb-12px">
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!treeData.length" description="暂无岗位层级数据" />
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
        <div class="flex w-full items-start justify-between gap-8px py-2px">
          <div class="flex min-w-0 items-start gap-6px overflow-hidden">
            <Icon
              :icon="data.type === 'level' ? 'ep:folder' : 'ep:office-building'"
              :color="data.type === 'level' ? '#0f766e' : '#1d4ed8'"
              class="mt-2px"
            />
            <div class="min-w-0">
              <div class="truncate">{{ data.label }}</div>
              <div
                v-if="data.type === 'post'"
                class="mt-2px truncate text-12px text-[var(--el-text-color-secondary)]"
              >
                {{ buildPreviewText(data) }}
              </div>
            </div>
          </div>
          <span class="whitespace-nowrap text-12px text-[var(--el-text-color-secondary)]">
            编制 {{ data.staffQuota ?? (data.assignedUserCount || 0) + (data.vacancyCount || 0) }} / 在岗
            {{ data.assignedUserCount || 0 }}
          </span>
        </div>
      </template>
    </el-tree>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import type { PostLevelTreeNodeVO } from '@/api/system/postLevel'

defineOptions({ name: 'PostLevelTree' })

const props = defineProps<{
  loading: boolean
  treeData: PostLevelTreeNodeVO[]
  selectedKey?: string
}>()

const emit = defineEmits<{
  (e: 'node-click', node: PostLevelTreeNodeVO): void
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
  },
  { immediate: true }
)

watch(keyword, (value) => treeRef.value?.filter(value))

const filterNode = (value: string, data: PostLevelTreeNodeVO) => {
  if (!value) return true
  const keywordValue = value.trim().toLowerCase()
  return [data.label, data.level, ...(data.previewUserNames || [])]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(keywordValue))
}

const buildPreviewText = (data: PostLevelTreeNodeVO) => {
  if (data.primaryUserName) {
    return `主岗：${data.primaryUserName}`
  }
  if (data.previewUserNames?.length) {
    return `人员：${data.previewUserNames.join('、')}`
  }
  return '暂无任职人员'
}

const handleNodeClick = (node: PostLevelTreeNodeVO) => {
  emit('node-click', node)
}
</script>