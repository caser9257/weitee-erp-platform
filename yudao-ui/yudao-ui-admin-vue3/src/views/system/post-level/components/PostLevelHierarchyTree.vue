<template>
  <ContentWrap class="post-level-hierarchy-tree h-full">
    <template #header>
      <div class="text-16px font-600">{{ text.title }}</div>
    </template>
    <el-input v-model="keyword" clearable :placeholder="text.searchPlaceholder" class="mb-12px">
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </el-input>
    <div class="post-level-hierarchy-tree__body">
      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="!treeData.length" :description="text.empty" />
      <div v-else class="post-level-hierarchy-tree__scroll">
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
            <div class="flex w-full items-center justify-between gap-8px py-2px">
              <div class="flex min-w-0 items-center gap-6px overflow-hidden">
                <Icon
                  :icon="data.type === 'level' ? 'ep:folder' : 'ep:office-building'"
                  :color="data.type === 'level' ? '#0f766e' : '#1d4ed8'"
                />
                <span class="truncate">{{ data.label }}</span>
              </div>
              <div class="flex items-center gap-8px text-12px text-[var(--el-text-color-secondary)]">
                <el-popover v-if="data.type === 'post'" placement="right" :width="260" trigger="hover">
                  <template #reference>
                    <span class="cursor-pointer text-[#2563eb]">{{ text.previewTrigger }}</span>
                  </template>
                  <div class="grid gap-6px">
                    <div class="font-600 text-[var(--el-text-color-primary)]">{{ data.label }}</div>
                    <div v-if="data.primaryUserName">{{ text.primaryPrefix }}{{ data.primaryUserName }}</div>
                    <div v-if="data.previewUserNames?.length">
                      {{ text.usersPrefix }}{{ data.previewUserNames.join(text.userSeparator) }}
                    </div>
                    <div v-if="!data.primaryUserName && !data.previewUserNames?.length">
                      {{ text.noUsers }}
                    </div>
                  </div>
                </el-popover>
                <span>{{ quotaLabel(data) }}</span>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import type { PostLevelTreeNodeVO } from '@/api/system/postLevel'

defineOptions({ name: 'PostLevelHierarchyTree' })

const props = defineProps<{
  loading: boolean
  treeData: PostLevelTreeNodeVO[]
  selectedKey?: string
}>()

const emit = defineEmits<{
  (e: 'node-click', node: PostLevelTreeNodeVO): void
}>()

const text = {
  title: '\u5c97\u4f4d\u5c42\u7ea7\u4f53\u7cfb',
  searchPlaceholder: '\u641c\u7d22\u5c97\u4f4d\u5c42\u7ea7\u6216\u5c97\u4f4d\u540d\u79f0',
  empty: '\u6682\u65e0\u5c97\u4f4d\u5c42\u7ea7\u6570\u636e',
  previewTrigger: '\u4eba\u5458',
  primaryPrefix: '\u4e3b\u5c97\uff1a',
  usersPrefix: '\u4eba\u5458\uff1a',
  userSeparator: '\u3001',
  noUsers: '\u6682\u65e0\u4efb\u804c\u4eba\u5458',
  quotaPrefix: '\u7f16\u5236 ',
  onDutyPrefix: ' / \u5728\u5c97 '
} as const

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

const filterNode = (value: string, data: PostLevelTreeNodeVO) => {
  if (!value) return true
  const keywordValue = value.trim().toLowerCase()
  return [data.label, data.level, ...(data.previewUserNames || []), data.primaryUserName]
    .filter(Boolean)
    .some((item) => String(item).toLowerCase().includes(keywordValue))
}

const quotaLabel = (data: PostLevelTreeNodeVO) => {
  const quota = data.staffQuota ?? (data.assignedUserCount || 0) + (data.vacancyCount || 0)
  return `${text.quotaPrefix}${quota}${text.onDutyPrefix}${data.assignedUserCount || 0}`
}

const handleNodeClick = (node: PostLevelTreeNodeVO) => {
  emit('node-click', node)
}
</script>

<style lang="scss" scoped>
.post-level-hierarchy-tree {
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

.post-level-hierarchy-tree__body {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.post-level-hierarchy-tree__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}
</style>
