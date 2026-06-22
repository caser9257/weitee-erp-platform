<template>
  <div class="file-folder-tree">
    <div class="file-folder-tree__header">
      <div class="file-folder-tree__title">文件夹</div>
      <el-button link type="primary" @click="handleCreateFolder">
        <Icon icon="ep:plus" />
      </el-button>
    </div>
    <div class="file-folder-tree__content">
      <el-tree
        ref="treeRef"
        :data="folderTree"
        :props="treeProps"
        node-key="id"
        highlight-current
        default-expand-all
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="folder-node">
            <Icon :icon="data.icon || 'ep:folder'" class="folder-node__icon" />
            <span class="folder-node__name">{{ node.label }}</span>
            <span v-if="data.fileCount" class="folder-node__count">{{ data.fileCount }}</span>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { ElTree } from 'element-plus'
import request from '@/config/axios'

defineOptions({ name: 'FileFolderTree' })

interface FolderItem {
  id: number
  name: string
  icon?: string
  fileCount?: number
  children?: FolderItem[]
}

const emit = defineEmits(['select', 'create'])

const treeRef = ref<InstanceType<typeof ElTree>>()
const activeFolderId = ref<number | null>(null)
const folderTree = ref<FolderItem[]>([])

const treeProps = {
  label: 'name',
  children: 'children'
}

// 加载文件夹树
const loadFolders = async () => {
  try {
    const data = await request.get({ url: '/infra/file-folder/tree' })
    folderTree.value = data || []
  } catch (e) {
    console.error('加载文件夹失败', e)
  }
}

// 选择文件夹
const handleNodeClick = (data: FolderItem) => {
  activeFolderId.value = data.id
  emit('select', data)
}

// 创建文件夹
const handleCreateFolder = () => {
  emit('create')
}

// 刷新
const refresh = () => {
  loadFolders()
}

onMounted(() => {
  loadFolders()
})

defineExpose({ refresh })
</script>

<style scoped lang="scss">
.file-folder-tree {
  width: 200px;
  border-right: 1px solid var(--erp-slate-200);
  background: white;
}

.file-folder-tree__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--erp-slate-100);
}

.file-folder-tree__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
}

.file-folder-tree__content {
  padding: 8px;
}

.folder-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
}

.folder-node__icon {
  font-size: 16px;
  color: var(--erp-slate-400);
}

.folder-node__name {
  flex: 1;
  font-size: 13px;
}

.folder-node__count {
  font-size: 12px;
  color: var(--erp-slate-400);
  background: var(--erp-slate-100);
  padding: 2px 6px;
  border-radius: 10px;
}

:deep(.el-tree-node__content) {
  height: 36px;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--erp-primary-50);
  color: var(--erp-primary-600);
}
</style>
