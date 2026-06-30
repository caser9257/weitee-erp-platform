<template>
  <div class="post-level-manage grid gap-16px">
    <ContentWrap>
      <div class="flex flex-wrap items-center justify-between gap-12px">
        <div>
          <div class="text-18px font-600">人事部组织岗位管理</div>
          <div class="text-12px text-[var(--el-text-color-secondary)]">
            同源组织数据，树上展示组织与岗位，点击岗位后在右侧查看任职人员
          </div>
        </div>
        <div class="flex flex-wrap gap-8px">
          <el-button :disabled="pageRefreshing" @click="reloadTree(selectedNode?.id)">
            <Icon icon="ep:refresh" class="mr-4px" />刷新
          </el-button>
          <el-button type="primary" :disabled="!canCreatePost" @click="openCreatePost">
            <Icon icon="ep:plus" class="mr-4px" />新增岗位
          </el-button>
        </div>
      </div>

      <el-row :gutter="12" class="mt-16px">
        <el-col v-for="card in summaryCards" :key="card.label" :xs="12" :md="6">
          <div class="rounded-4px border border-[var(--el-border-color)] p-16px">
            <div class="text-12px text-[var(--el-text-color-secondary)]">{{ card.label }}</div>
            <div class="mt-8px text-24px font-600">{{ card.value }}</div>
          </div>
        </el-col>
      </el-row>
    </ContentWrap>

    <el-alert v-if="treeError" type="error" :title="treeError" :closable="false" show-icon />

    <div class="manage-main">
      <div class="manage-main__tree">
        <DeptPostTree
          :loading="treeLoading"
          :tree-data="treeData"
          :selected-key="selectedNode?.id"
          @node-click="handleNodeClick"
        />
      </div>
      <div ref="detailPanelRef" class="manage-main__detail">
        <div ref="detailAnchorRef" class="manage-main__detail-anchor" aria-hidden="true"></div>
        <div class="grid gap-16px">
          <DeptPersonDetailCard
            v-if="selectedDeptNode"
            :loading="treeLoading"
            :node="selectedDeptNode"
            :show-actions="false"
          />

          <PostDetailCard
            v-if="selectedPostDetail"
            :loading="detailLoading"
            :detail="selectedPostDetail"
            :show-actions="true"
            @edit="handleEditPost"
            @assign="handleAssignPost"
          />

          <DeptPostTable
            :loading="treeLoading"
            :posts="selectedPosts"
            @row-click="handlePostRowClick"
          />

          <PostAssignedUserTable
            v-if="selectedPostDetail"
            :loading="detailLoading"
            :users="selectedPostDetail.assignedUsers"
          />
        </div>
      </div>
    </div>

    <PostTemplateFormDialog ref="postFormRef" :submit-action="handlePostSubmit" />
    <PostAssignUserForm ref="assignFormRef" :submit-action="handleAssignSubmit" />
  </div>
</template>

<script lang="ts" setup>
import { getSimpleDeptList, type DeptVO } from '@/api/system/dept'
import {
  assignUsers,
  getDetail,
  getOrgTree,
  type PostLevelAssignUserItemVO,
  type PostLevelDetailVO,
  type PostOrgTreeNodeVO,
  type PostOrgTreeVO
} from '@/api/system/postLevel'
import { createPost, getPost, updatePost, type PostVO } from '@/api/system/post'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import DeptPersonDetailCard from '../components/DeptPersonDetailCard.vue'
import DeptPostTable from '../components/DeptPostTable.vue'
import DeptPostTree from '../components/DeptPostTree.vue'
import PostAssignedUserForm from '../components/PostAssignUserForm.vue'
import PostAssignedUserTable from '../components/PostAssignedUserTable.vue'
import PostDetailCard from '../components/PostDetailCard.vue'
import PostTemplateFormDialog from '../components/PostTemplateFormDialog.vue'

defineOptions({ name: 'SystemDeptPersonManage' })

interface DeptTreeOption {
  id: number
  label: string
  parentId?: number
  children?: DeptTreeOption[]
}

const message = useMessage()
const treeLoading = ref(false)
const detailLoading = ref(false)
const deptOptionsLoading = ref(false)
const userOptionsLoading = ref(false)
const treeError = ref('')
const treeData = ref<PostOrgTreeNodeVO[]>([])
const orgSummary = ref<PostOrgTreeVO | null>(null)
const selectedNode = ref<PostOrgTreeNodeVO | null>(null)
const selectedPostDetail = ref<PostLevelDetailVO | null>(null)
const deptOptions = ref<DeptTreeOption[]>([])
const userOptions = ref<SimpleUserVO[]>([])
const postFormRef = ref<any>()
const assignFormRef = ref<any>()
const detailPanelRef = ref<HTMLElement>()
const detailAnchorRef = ref<HTMLElement>()

const pageRefreshing = computed(
  () =>
    treeLoading.value ||
    detailLoading.value ||
    deptOptionsLoading.value ||
    userOptionsLoading.value
)

const summaryCards = computed(() => [
  { label: '岗位总数', value: orgSummary.value?.postCount || 0 },
  { label: '编制总数', value: orgSummary.value?.staffQuota || 0 },
  { label: '在岗人数', value: orgSummary.value?.assignedUserCount || 0 },
  { label: '部门总数', value: orgSummary.value?.deptCount || 0 }
])

const selectedDeptNode = computed(() => {
  if (!selectedNode.value) return null
  if (selectedNode.value.type === 'dept' || selectedNode.value.type === 'root') {
    return selectedNode.value
  }
  if (selectedNode.value.type === 'post') {
    return findNodeById(treeData.value, `dept-${selectedNode.value.deptId || ''}`)
  }
  return null
})

const selectedDeptId = computed(() => {
  if (selectedNode.value?.type === 'dept' || selectedNode.value?.type === 'root') {
    return selectedNode.value.deptId
  }
  if (selectedNode.value?.type === 'post') {
    return selectedNode.value.deptId
  }
  return undefined
})

const selectedPosts = computed(() => {
  const node = selectedDeptNode.value
  if (!node) {
    return []
  }
  return collectPostNodes([node])
})

const createPostDeptId = computed(() => selectedDeptId.value || findFirstDeptId(treeData.value))
const canCreatePost = computed(
  () => !!createPostDeptId.value && !treeLoading.value && !deptOptionsLoading.value
)

const scrollDetailPanelToTop = async (smooth = false) => {
  await nextTick()
  const detailPanel = detailPanelRef.value
  if (!detailPanel) {
    return
  }
  detailPanel.scrollTo({
    top: detailAnchorRef.value?.offsetTop || 0,
    behavior: smooth ? 'smooth' : 'auto'
  })
}

const findNodeById = (nodes: PostOrgTreeNodeVO[], id?: string): PostOrgTreeNodeVO | null => {
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

const findNodeByPostId = (nodes: PostOrgTreeNodeVO[], postId: number): PostOrgTreeNodeVO | null => {
  for (const node of nodes) {
    if (node.type === 'post' && node.postId === postId) {
      return node
    }
    const child = findNodeByPostId(node.children || [], postId)
    if (child) {
      return child
    }
  }
  return null
}

const findFirstDeptId = (nodes: PostOrgTreeNodeVO[]): number | undefined => {
  for (const node of nodes) {
    if ((node.type === 'dept' || node.type === 'root') && node.deptId) {
      return node.deptId
    }
    const childDeptId = findFirstDeptId(node.children || [])
    if (childDeptId) {
      return childDeptId
    }
  }
  return undefined
}

const findFirstDeptNode = (nodes: PostOrgTreeNodeVO[]): PostOrgTreeNodeVO | null => {
  for (const node of nodes) {
    if (node.type === 'root' || node.type === 'dept') {
      return node
    }
    const child = findFirstDeptNode(node.children || [])
    if (child) {
      return child
    }
  }
  return null
}

const collectPostNodes = (nodes: PostOrgTreeNodeVO[]): PostOrgTreeNodeVO[] => {
  const result: PostOrgTreeNodeVO[] = []
  const visit = (items: PostOrgTreeNodeVO[]) => {
    items.forEach((node) => {
      if (node.type === 'post') {
        result.push(node)
      }
      if (node.children?.length) {
        visit(node.children)
      }
    })
  }
  visit(nodes)
  return result
}

const buildDeptTreeOptions = (depts: DeptVO[]): DeptTreeOption[] => {
  const orderedDepts = [...depts].sort((a, b) => a.sort - b.sort || a.id - b.id)
  const map = new Map<number, DeptTreeOption>()
  const roots: DeptTreeOption[] = []
  orderedDepts.forEach((dept) => {
    map.set(dept.id, {
      id: dept.id,
      label: dept.name,
      parentId: dept.parentId,
      children: []
    })
  })
  orderedDepts.forEach((dept) => {
    const node = map.get(dept.id)!
    const parent = map.get(dept.parentId)
    if (parent) {
      parent.children!.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

const ensureDeptOptions = async () => {
  if (deptOptions.value.length) {
    return
  }
  deptOptionsLoading.value = true
  try {
    deptOptions.value = buildDeptTreeOptions(await getSimpleDeptList())
  } catch (error) {
    message.error('部门选项加载失败，请稍后重试')
    console.error(error)
    throw error
  } finally {
    deptOptionsLoading.value = false
  }
}

const ensureUserOptions = async () => {
  if (userOptions.value.length) {
    return
  }
  userOptionsLoading.value = true
  try {
    userOptions.value = await getSimpleUserList()
  } catch (error) {
    message.error('人员选项加载失败，请稍后重试')
    console.error(error)
    throw error
  } finally {
    userOptionsLoading.value = false
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
    message.error('岗位详情加载失败，请稍后重试')
    console.error(error)
  } finally {
    detailLoading.value = false
  }
}

const reloadTree = async (preferredKey?: string) => {
  treeLoading.value = true
  treeError.value = ''
  try {
    const response = await getOrgTree()
    orgSummary.value = response
    treeData.value = response.tree || []
    if (preferredKey) {
      const preferredNode = findNodeById(treeData.value, preferredKey)
      if (preferredNode) {
        selectedNode.value = preferredNode
      }
    }
    if (!selectedNode.value) {
      selectedNode.value = findFirstDeptNode(treeData.value) || treeData.value[0] || null
    } else {
      const currentNode = findNodeById(treeData.value, selectedNode.value.id)
      selectedNode.value = currentNode || findFirstDeptNode(treeData.value) || treeData.value[0] || null
    }
  } catch (error) {
    orgSummary.value = null
    treeData.value = []
    selectedNode.value = null
    selectedPostDetail.value = null
    treeError.value = '组织岗位树加载失败，请稍后重试'
    console.error(error)
  } finally {
    treeLoading.value = false
  }
}

const handleNodeClick = (node: PostOrgTreeNodeVO) => {
  void scrollDetailPanelToTop()
  selectedNode.value = node
}

const openCreatePost = async () => {
  try {
    const deptId = createPostDeptId.value
    if (!deptId) {
      return
    }
    await ensureDeptOptions()
    postFormRef.value?.open('create', deptOptions.value, { deptId })
  } catch (error) {
    console.error(error)
  }
}

const handleEditPost = async (postId: number) => {
  try {
    await ensureDeptOptions()
    const payload = await getPost(postId)
    postFormRef.value?.open('update', deptOptions.value, payload)
  } catch (error) {
    message.error('岗位信息加载失败，请稍后重试')
    console.error(error)
  }
}

const handleAssignPost = async (postId: number) => {
  try {
    await ensureUserOptions()
    const detail =
      selectedPostDetail.value?.postId === postId ? selectedPostDetail.value : await getDetail(postId)
    assignFormRef.value?.open(detail, userOptions.value)
  } catch (error) {
    message.error('岗位人员数据加载失败，请稍后重试')
    console.error(error)
  }
}

const handlePostRowClick = (row: PostOrgTreeNodeVO) => {
  if (!row.postId) {
    return
  }
  const node = findNodeByPostId(treeData.value, row.postId)
  if (node) {
    void scrollDetailPanelToTop()
    selectedNode.value = node
  }
}

const handlePostSubmit = async (payload: PostVO) => {
  try {
    if (payload.id) {
      await updatePost(payload)
      message.success('岗位已更新')
      await reloadTree(`post-${payload.id}`)
      return
    }
    const postId = await createPost(payload)
    message.success('岗位已创建')
    await reloadTree(`post-${postId}`)
  } catch (error) {
    message.error(payload.id ? '岗位更新失败，请稍后重试' : '岗位创建失败，请稍后重试')
    console.error(error)
    throw error
  }
}

const handleAssignSubmit = async (postId: number, assignments: PostLevelAssignUserItemVO[]) => {
  try {
    await assignUsers({ postId, assignments })
    message.success('人员分配已更新')
    await reloadTree(`post-${postId}`)
  } catch (error) {
    message.error('人员分配失败，请稍后重试')
    console.error(error)
    throw error
  }
}

watch(
  () => selectedNode.value?.id,
  async () => {
    await loadSelectedPostDetail()
  }
)

onMounted(async () => {
  await reloadTree()
})
</script>

<style lang="scss" scoped>
.post-level-manage {
  .manage-main {
    display: grid;
    grid-template-columns: minmax(320px, 0.92fr) minmax(460px, 1.4fr);
    gap: 16px;
    align-items: start;
    height: clamp(560px, calc(100vh - 252px), 820px);
    min-height: 0;
  }

  .manage-main__tree,
  .manage-main__detail {
    min-width: 0;
    min-height: 0;
    height: 100%;
  }

  .manage-main__tree {
    overflow: hidden;
  }

  .manage-main__detail {
    overflow-y: auto;
    padding-right: 2px;
  }

  .manage-main__detail-anchor {
    height: 0;
  }
}

@media (max-width: 1279px) {
  .post-level-manage {
    .manage-main {
      grid-template-columns: minmax(280px, 0.88fr) minmax(0, 1.32fr);
    }
  }
}

@media (max-width: 1023px) {
  .post-level-manage {
    .manage-main {
      grid-template-columns: minmax(0, 1fr);
      height: auto;
    }

    .manage-main__tree,
    .manage-main__detail {
      height: auto;
      overflow: visible;
    }
  }
}
</style>
