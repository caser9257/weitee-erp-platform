<template>
  <div class="post-level-overview grid gap-16px">
    <ContentWrap class="overview-hero">
      <div class="flex flex-wrap items-start justify-between gap-12px">
        <div class="grid gap-6px">
          <div class="text-20px font-600 leading-none">{{ text.pageTitle }}</div>
          <div class="text-13px text-[var(--el-text-color-secondary)]">{{ text.pageSubtitle }}</div>
        </div>
        <el-button :disabled="pageRefreshing" @click="reloadPage">
          <Icon icon="ep:refresh" class="mr-4px" />{{ text.refresh }}
        </el-button>
      </div>

      <div class="overview-stats mt-18px">
        <div
          v-for="card in statCards"
          :key="card.label"
          class="overview-stat-card"
          :class="card.tone ? `overview-stat-card--${card.tone}` : ''"
        >
          <div class="overview-stat-card__label">{{ card.label }}</div>
          <div class="overview-stat-card__value">{{ card.value }}</div>
        </div>
      </div>
    </ContentWrap>

    <el-alert v-if="treeError" type="error" :title="treeError" :closable="false" show-icon />

    <div class="overview-main">
      <div class="overview-main__tree">
        <PostLevelHierarchyTree
          :loading="treeLoading"
          :tree-data="treeData"
          :selected-key="selectedNode?.id"
          @node-click="handleNodeClick"
        />
      </div>

      <div class="overview-main__detail">
        <div class="overview-detail-panel">
          <ContentWrap class="selected-post-panel">
            <template #header>
              <div class="flex items-center justify-between gap-12px">
                <span class="text-16px font-600">{{ text.selectedPostTitle }}</span>
                <el-tag v-if="selectedPostMeta" type="primary" effect="light">
                  {{ selectedPostMeta.level || text.levelUnclassified }}
                </el-tag>
              </div>
            </template>

            <el-skeleton v-if="detailLoading && !selectedPostMeta" :rows="3" animated />
            <el-empty v-else-if="!selectedPostMeta" :description="text.selectPostEmpty" />
            <div v-else class="grid gap-14px">
              <div class="selected-post-panel__headline">
                <div class="selected-post-panel__title">{{ selectedPostMeta.name }}</div>
                <div class="selected-post-panel__dept">{{ selectedPostMeta.deptName || text.deptFallback }}</div>
              </div>
              <div class="selected-post-panel__metrics">
                <div class="selected-post-panel__metric">
                  <span class="selected-post-panel__metric-label">{{ text.staffQuota }}</span>
                  <span class="selected-post-panel__metric-value">{{ selectedPostMeta.staffQuota }}</span>
                </div>
                <div class="selected-post-panel__metric">
                  <span class="selected-post-panel__metric-label">{{ text.onDuty }}</span>
                  <span class="selected-post-panel__metric-value">{{ selectedPostMeta.assignedUserCount }}</span>
                </div>
                <div class="selected-post-panel__metric">
                  <span class="selected-post-panel__metric-label">{{ text.primaryCount }}</span>
                  <span class="selected-post-panel__metric-value">{{ selectedPostMeta.primaryUserCount }}</span>
                </div>
                <div class="selected-post-panel__metric">
                  <span class="selected-post-panel__metric-label">{{ text.code }}</span>
                  <span class="selected-post-panel__metric-value selected-post-panel__metric-value--code">
                    {{ selectedPostMeta.code || '-' }}
                  </span>
                </div>
              </div>
            </div>
          </ContentWrap>

          <PostAssignedUserTable :loading="detailLoading" :users="selectedUsers" />
          <PostDetailCard :loading="detailLoading" :detail="selectedPostDetail" :show-actions="false" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
  getDashboard,
  getDetail,
  getTree,
  type PostLevelDashboardVO,
  type PostLevelDetailVO,
  type PostLevelTreeNodeVO
} from '@/api/system/postLevel'
import PostAssignedUserTable from '../components/PostAssignedUserTable.vue'
import PostDetailCard from '../components/PostDetailCard.vue'
import PostLevelHierarchyTree from '../components/PostLevelHierarchyTree.vue'

defineOptions({ name: 'SystemDeptPersonOverview' })

const text = {
  pageTitle: '\u5c97\u4f4d\u5c42\u7ea7\u4f53\u7cfb\u53ef\u89c6\u5316',
  pageSubtitle: '\u5de6\u4fa7\u67e5\u770b\u5c97\u4f4d\u5c42\u7ea7\uff0c\u53f3\u4fa7\u5728\u5f53\u524d\u89c6\u53e3\u5185\u67e5\u770b\u5c97\u4f4d\u4e0e\u4eba\u5458\u4fe1\u606f',
  refresh: '\u5237\u65b0',
  selectedPostTitle: '\u5f53\u524d\u9009\u4e2d\u5c97\u4f4d',
  selectPostEmpty: '\u8bf7\u9009\u62e9\u5de6\u4fa7\u5c97\u4f4d\u67e5\u770b\u8be6\u60c5',
  deptFallback: '\u672a\u914d\u7f6e\u6240\u5c5e\u90e8\u95e8',
  staffQuota: '\u7f16\u5236',
  onDuty: '\u5728\u5c97',
  primaryCount: '\u4e3b\u5c97',
  code: '\u7f16\u7801',
  postCount: '\u5c97\u4f4d\u603b\u6570',
  totalQuota: '\u7f16\u5236\u603b\u6570',
  assignedCount: '\u5728\u5c97\u4eba\u6570',
  levelHigh: '\u9ad8\u7ea7',
  levelMiddle: '\u4e2d\u7ea7',
  levelPrimary: '\u521d\u7ea7',
  levelUnclassified: '\u672a\u5206\u7ea7',
  dashboardLoadError: '\u5c97\u4f4d\u603b\u89c8\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
  detailLoadError: '\u5c97\u4f4d\u8be6\u60c5\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
  treeLoadError: '\u5c97\u4f4d\u5c42\u7ea7\u6811\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5'
} as const

const message = useMessage()
const dashboardLoading = ref(false)
const treeLoading = ref(false)
const detailLoading = ref(false)
const treeError = ref('')
const dashboard = ref<PostLevelDashboardVO | null>(null)
const treeData = ref<PostLevelTreeNodeVO[]>([])
const selectedNode = ref<PostLevelTreeNodeVO | null>(null)
const selectedPostDetail = ref<PostLevelDetailVO | null>(null)

const pageRefreshing = computed(
  () => dashboardLoading.value || treeLoading.value || detailLoading.value
)

const summaryCards = computed(() => [
  { label: text.postCount, value: dashboard.value?.postCount || 0 },
  {
    label: text.totalQuota,
    value: (dashboard.value?.assignedUserCount || 0) + (dashboard.value?.vacancyCount || 0)
  },
  { label: text.assignedCount, value: dashboard.value?.assignedUserCount || 0 }
])

const levelCards = computed(() => {
  const levelMap = new Map((dashboard.value?.levelSummaries || []).map((item) => [item.level, item.postCount]))
  return [
    { label: text.levelHigh, value: levelMap.get(text.levelHigh) || 0 },
    { label: text.levelMiddle, value: levelMap.get(text.levelMiddle) || 0 },
    { label: text.levelPrimary, value: levelMap.get(text.levelPrimary) || 0 },
    { label: text.levelUnclassified, value: levelMap.get(text.levelUnclassified) || 0 }
  ]
})

const statCards = computed(() => [
  ...summaryCards.value.map((item, index) => ({
    ...item,
    tone: index === 0 ? 'primary' : index === 1 ? 'neutral' : 'teal'
  })),
  ...levelCards.value.map((item) => ({
    ...item,
    tone:
      item.label === text.levelHigh
        ? 'gold'
        : item.label === text.levelMiddle
          ? 'blue'
          : item.label === text.levelPrimary
            ? 'green'
            : 'slate'
  }))
])

const selectedUsers = computed(() => selectedPostDetail.value?.assignedUsers ?? [])
const selectedPostMeta = computed(() => {
  if (!selectedPostDetail.value) {
    return null
  }
  const detail = selectedPostDetail.value
  return {
    name: detail.name,
    level: detail.level,
    deptName: detail.deptName,
    staffQuota: detail.staffQuota,
    assignedUserCount: detail.assignedUserCount,
    primaryUserCount: detail.primaryUserCount,
    code: detail.code
  }
})

const findNodeById = (nodes: PostLevelTreeNodeVO[], id?: string): PostLevelTreeNodeVO | null => {
  if (!id) return null
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    const child = findNodeById(node.children || [], id)
    if (child) {
      return child
    }
  }
  return null
}

const findFirstPostNode = (nodes: PostLevelTreeNodeVO[]): PostLevelTreeNodeVO | null => {
  for (const node of nodes) {
    if (node.type === 'post') {
      return node
    }
    const child = findFirstPostNode(node.children || [])
    if (child) {
      return child
    }
  }
  return null
}

const loadDashboardData = async () => {
  dashboardLoading.value = true
  try {
    dashboard.value = await getDashboard()
  } catch (error) {
    dashboard.value = null
    message.error(text.dashboardLoadError)
    console.error(error)
  } finally {
    dashboardLoading.value = false
  }
}

const loadSelectedPostDetail = async () => {
  if (selectedNode.value?.type !== 'post' || !selectedNode.value.postId) {
    selectedPostDetail.value = null
    return
  }
  detailLoading.value = true
  try {
    selectedPostDetail.value = await getDetail(selectedNode.value.postId)
  } catch (error) {
    selectedPostDetail.value = null
    message.error(text.detailLoadError)
    console.error(error)
  } finally {
    detailLoading.value = false
  }
}

const reloadTree = async (preferredKey?: string) => {
  treeLoading.value = true
  treeError.value = ''
  try {
    treeData.value = await getTree()
    if (preferredKey) {
      const preferredNode = findNodeById(treeData.value, preferredKey)
      if (preferredNode) {
        selectedNode.value = preferredNode
      }
    }
    if (!selectedNode.value) {
      selectedNode.value = findFirstPostNode(treeData.value) || treeData.value[0] || null
    } else {
      const currentNode = findNodeById(treeData.value, selectedNode.value.id)
      selectedNode.value = currentNode || findFirstPostNode(treeData.value) || treeData.value[0] || null
    }
  } catch (error) {
    treeData.value = []
    selectedNode.value = null
    selectedPostDetail.value = null
    treeError.value = text.treeLoadError
    console.error(error)
  } finally {
    treeLoading.value = false
  }
}

const reloadPage = async () => {
  await Promise.all([loadDashboardData(), reloadTree(selectedNode.value?.id)])
}

const handleNodeClick = (node: PostLevelTreeNodeVO) => {
  selectedNode.value = node
}

watch(
  () => selectedNode.value?.id,
  async () => {
    await loadSelectedPostDetail()
  }
)

onMounted(async () => {
  await reloadPage()
})
</script>

<style lang="scss" scoped>
.post-level-overview {
  .overview-hero {
    :deep(.el-card__body) {
      padding: 18px 20px 20px;
    }
  }

  .overview-stats {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 12px;
  }

  .overview-stat-card {
    min-height: 92px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 14px;
    padding: 14px 16px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 250, 252, 0.98) 100%);
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
  }

  .overview-stat-card__label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.4;
  }

  .overview-stat-card__value {
    margin-top: 16px;
    font-size: 32px;
    font-weight: 700;
    line-height: 1;
    color: #0f172a;
  }

  .overview-stat-card--primary {
    background: linear-gradient(135deg, rgba(219, 234, 254, 0.9) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--neutral {
    background: linear-gradient(135deg, rgba(226, 232, 240, 0.72) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--teal {
    background: linear-gradient(135deg, rgba(204, 251, 241, 0.9) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--gold {
    background: linear-gradient(135deg, rgba(254, 249, 195, 0.92) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--blue {
    background: linear-gradient(135deg, rgba(219, 234, 254, 0.92) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--green {
    background: linear-gradient(135deg, rgba(220, 252, 231, 0.92) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-stat-card--slate {
    background: linear-gradient(135deg, rgba(241, 245, 249, 0.96) 0%, rgba(255, 255, 255, 0.98) 100%);
  }

  .overview-main {
    display: grid;
    grid-template-columns: minmax(320px, 0.95fr) minmax(440px, 1.35fr);
    height: clamp(560px, calc(100vh - 252px), 820px);
    gap: 16px;
    align-items: start;
    overflow: hidden;
  }

  .overview-main__tree,
  .overview-main__detail {
    min-width: 0;
    height: 100%;
    overflow: hidden;
  }

  .overview-detail-panel {
    display: grid;
    gap: 16px;
    height: 100%;
    overflow-y: auto;
    padding-right: 2px;
  }

  .selected-post-panel {
    border-radius: 14px;
    overflow: hidden;

    :deep(.el-card__body) {
      background: linear-gradient(135deg, rgba(239, 246, 255, 0.92) 0%, rgba(255, 255, 255, 1) 75%);
    }
  }

  .selected-post-panel__headline {
    display: grid;
    gap: 6px;
  }

  .selected-post-panel__title {
    font-size: 24px;
    font-weight: 700;
    line-height: 1.2;
    color: #0f172a;
  }

  .selected-post-panel__dept {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .selected-post-panel__metrics {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
  }

  .selected-post-panel__metric {
    display: grid;
    gap: 6px;
    padding: 12px 14px;
    border: 1px solid rgba(148, 163, 184, 0.18);
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.82);
  }

  .selected-post-panel__metric-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1;
  }

  .selected-post-panel__metric-value {
    font-size: 20px;
    font-weight: 700;
    line-height: 1.1;
    color: #0f172a;
  }

  .selected-post-panel__metric-value--code {
    font-size: 14px;
    word-break: break-all;
  }
}

@media (max-width: 1439px) {
  .post-level-overview {
    .overview-stats {
      grid-template-columns: repeat(4, minmax(0, 1fr));
    }

    .selected-post-panel__metrics {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}

@media (max-width: 1023px) {
  .post-level-overview {
    .overview-main {
      grid-template-columns: minmax(0, 1fr);
      height: auto;
      overflow: visible;
    }

    .overview-detail-panel {
      height: auto;
      overflow: visible;
    }

    .overview-main__tree,
    .overview-main__detail {
      height: auto;
      overflow: visible;
    }
  }
}

@media (max-width: 767px) {
  .post-level-overview {
    .overview-stats {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .overview-stat-card {
      min-height: 84px;
    }

    .overview-stat-card__value {
      font-size: 26px;
    }

    .selected-post-panel__metrics {
      grid-template-columns: minmax(0, 1fr);
    }
  }
}
</style>
