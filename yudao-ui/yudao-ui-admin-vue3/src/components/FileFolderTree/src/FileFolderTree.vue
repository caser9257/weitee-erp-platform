<template>
  <div class="file-folder-tree">
    <div class="file-folder-tree__header">
      <div class="file-folder-tree__title">文件夹</div>
      <el-button link type="primary" @click="handleCreateFolder">
        <Icon icon="ep:plus" />
      </el-button>
    </div>
    <div class="file-folder-tree__content">
      <div
        v-for="folder in folderList"
        :key="folder.id"
        class="folder-item"
        :class="{ 'folder-item--active': activeFolderId === folder.id }"
        @click="handleSelectFolder(folder)"
      >
        <Icon :icon="folder.icon || 'ep:folder'" class="folder-item__icon" />
        <span class="folder-item__name">{{ folder.name }}</span>
        <span class="folder-item__count">{{ folder.fileCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

defineOptions({ name: 'FileFolderTree' })

interface FolderItem {
  id: number
  name: string
  icon?: string
  fileCount?: number
}

const emit = defineEmits(['select', 'create'])

const activeFolderId = ref<number | null>(null)
const folderList = ref<FolderItem[]>([])

// 加载文件夹列表
const loadFolders = async () => {
  try {
    const { request } = await import('@/config/axios')
    const data = await request.get({ url: '/infra/file-folder/list' })
    folderList.value = data || []
  } catch (e) {
    console.error('加载文件夹失败', e)
  }
}

// 选择文件夹
const handleSelectFolder = (folder: FolderItem) => {
  activeFolderId.value = folder.id
  emit('select', folder)
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

.folder-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--erp-slate-50);
  }

  &--active {
    background: var(--erp-primary-50);
    color: var(--erp-primary-600);
  }
}

.folder-item__icon {
  font-size: 16px;
  color: var(--erp-slate-400);
}

.folder-item__name {
  flex: 1;
  font-size: 13px;
}

.folder-item__count {
  font-size: 12px;
  color: var(--erp-slate-400);
  background: var(--erp-slate-100);
  padding: 2px 6px;
  border-radius: 10px;
}
</style>
