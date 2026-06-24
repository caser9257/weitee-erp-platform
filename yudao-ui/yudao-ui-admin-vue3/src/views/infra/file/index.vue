<template>
<!-- 页面头部：快捷导�?-->
  <div class="file-management-header">
    <div class="flex items-center gap-8px">
      <Icon icon="ep:folder-opened" class="text-2xl text-[var(--erp-primary-500)]" />
      <span class="text-lg font-semibold text-[var(--erp-slate-800)]">文件管理</span>
    </div>
    <div class="flex items-center gap-8px">
      <el-button type="primary" plain size="small" @click="openFolderDialog">
        <Icon icon="ep:folder-add" class="mr-3px" /> 文件夹管�?
      </el-button>
      <el-button type="primary" plain size="small" @click="openTagDialog">
        <Icon icon="ep:price-tag" class="mr-3px" /> 标签管理
      </el-button>
      <el-button type="primary" plain size="small" @click="openBizRelDialog">
        <Icon icon="ep:connection" class="mr-3px" /> 业务关联
      </el-button>
      <el-button type="warning" plain size="small" @click="openRecycleBin">
        <Icon icon="ep:delete" class="mr-3px" /> 回收�?
      </el-button>
      <el-button type="info" plain size="small" @click="openOperationLog">
        <Icon icon="ep:document" class="mr-3px" /> 操作日志
      </el-button>
    </div>
  </div>

  <div class="file-management-container">
    <!-- 左侧：文件夹�?-->
    <div class="file-management-sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">文件�?/span>
        <el-button link type="primary" @click="loadFolderTree">
          <Icon icon="ep:refresh" />
        </el-button>
      </div>
      <div class="sidebar-content">
        <el-tree
          v-loading="folderLoading"
          :data="folderTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="handleFolderClick"
        >
          <template #default="{ node, data }">
            <div class="flex items-center gap-6px">
              <Icon :icon="data.icon || 'ep:folder'" class="text-sm text-[var(--erp-primary-500)]" />
              <span>{{ node.label }}</span>
            </div>
          </template>
        </el-tree>
      </div>
      
      <!-- 标签筛�?-->
      <div class="sidebar-header mt-12px">
        <span class="sidebar-title">标签筛�?/span>
      </div>
      <div class="sidebar-content">
        <div v-loading="tagLoading" class="flex flex-wrap gap-6px">
          <el-tag
            v-for="tag in tagList"
            :key="tag.id"
            :color="tag.color"
            :style="{ color: '#fff', borderColor: tag.color }"
            class="cursor-pointer"
            :class="{ 'ring-2 ring-offset-2': selectedTagId === tag.id }"
            @click="handleTagClick(tag)"
          >
            {{ tag.name }}
          </el-tag>
          <el-tag
            v-if="selectedTagId"
            type="info"
            class="cursor-pointer"
            @click="clearTagFilter"
          >
            清除筛�?
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 右侧：文件列�?-->
    <div class="file-management-main">
      <!-- 搜索 -->
      <ContentWrap>
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="文件�? prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入文件名"
              clearable
              @keyup.enter="handleQuery"
              class="!w-200px"
            />
          </el-form-item>
          <el-form-item label="文件路径" prop="path">
            <el-input
              v-model="queryParams.path"
              placeholder="请输入文件路�?
              clearable
              @keyup.enter="handleQuery"
              class="!w-200px"
            />
          </el-form-item>
          <el-form-item label="文件类型" prop="type">
            <el-input
              v-model="queryParams.type"
              placeholder="请输入文件类�?
              clearable
              @keyup.enter="handleQuery"
              class="!w-160px"
            />
          </el-form-item>
          <el-form-item label="创建时间" prop="createTime">
            <el-date-picker
              v-model="queryParams.createTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="开始日�?
              end-placeholder="结束日期"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button type="primary" plain @click="openForm">
              <Icon icon="ep:upload" class="mr-5px" /> 上传文件
            </el-button>
            <el-button
              type="danger"
              plain
              :disabled="checkedIds.length === 0"
              @click="handleDeleteBatch"
              v-hasPermi="['infra:file:delete']"
            >
              <Icon icon="ep:delete" class="mr-5px" /> 批量删除
            </el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>

      <!-- 当前位置提示 -->
      <div v-if="currentFolder || selectedTagId" class="current-location-tip">
        <Icon icon="ep:location" class="text-[var(--erp-primary-500)]" />
        <span v-if="currentFolder">
          当前文件夹：<strong>{{ currentFolder.name }}</strong>
        </span>
        <span v-if="selectedTagId" class="ml-8px">
          当前标签�?el-tag size="small" :color="selectedTag?.color" :style="{ color: '#fff' }">
            {{ selectedTag?.name }}
          </el-tag>
        </span>
        <el-button link type="primary" class="ml-8px" @click="clearFilters">
          清除筛�?
        </el-button>
      </div>

      <!-- 列表 -->
      <ContentWrap>
        <el-table v-loading="loading" :data="list" @selection-change="handleRowCheckboxChange">
          <el-table-column type="selection" width="55" />
          <el-table-column label="文件�? prop="name" min-width="200" :show-overflow-tooltip="true">
            <template #default="{ row }">
              <div class="flex items-center gap-8px">
                <Icon :icon="getFileIcon(row.type)" class="text-lg" :class="getFileIconColor(row.type)" />
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="文件路径" prop="path" min-width="150" :show-overflow-tooltip="true" />
          <el-table-column label="URL" prop="url" min-width="150" :show-overflow-tooltip="true" />
          <el-table-column label="文件大小" prop="size" width="100" align="right" :formatter="fileSizeFormatter" />
          <el-table-column label="文件类型" prop="type" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="文件内容" prop="url" width="100" align="center">
            <template #default="{ row }">
              <el-image
                v-if="row.type?.includes('image')"
                class="h-60px w-60px rounded"
                lazy
                :src="row.url"
                :preview-src-list="[row.url]"
                preview-teleported
                fit="cover"
              />
              <el-link
                v-else-if="row.type?.includes('pdf')"
                type="primary"
                :href="row.url"
                :underline="false"
                target="_blank"
              >
                预览
              </el-link>
              <el-link v-else type="primary" download :href="row.url" :underline="false" target="_blank">
                下载
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="上传时间" prop="createTime" width="160" align="center" :formatter="dateFormatter" />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="copyToClipboard(row.url)">
                <Icon icon="ep:copy-document" class="mr-3px" /> 复制
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete(row.id)"
                v-hasPermi="['infra:file:delete']"
              >
                <Icon icon="ep:delete" class="mr-3px" /> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </ContentWrap>
    </div>
  </div>

  <!-- 表单弹窗：添�?修改 -->
  <FileForm ref="formRef" @success="getList" />

  <!-- 文件夹管理弹�?-->
  <Dialog v-model="folderDialogVisible" title="文件夹管�? width="800px">
    <FileFolderPage />
  </Dialog>

  <!-- 标签管理弹窗 -->
  <Dialog v-model="tagDialogVisible" title="标签管理" width="700px">
    <FileTagPage />
  </Dialog>

  <!-- 业务关联弹窗 -->
  <Dialog v-model="bizRelDialogVisible" title="文件业务关联" width="900px">
    <FileBizRelPage />
  </Dialog>
</template>

<script lang="ts" setup>
import { fileSizeFormatter } from '@/utils'
import { dateFormatter } from '@/utils/formatTime'
import * as FileApi from '@/api/infra/file'
import * as FileFolderApi from '@/api/infra/fileFolder'
import * as FileTagApi from '@/api/infra/fileTag'
import type { FileFolderVO } from '@/api/infra/fileFolder'
import type { FileTagVO } from '@/api/infra/fileTag'
import FileForm from './FileForm.vue'
import FileFolderPage from './folder/index.vue'
import FileTagPage from './tag/index.vue'
import FileBizRelPage from './bizRel/index.vue'
import { useClipboard } from '@vueuse/core'

defineOptions({ name: 'InfraFile' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

// ========== 文件列表相关 ==========
const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  type: undefined,
  path: undefined,
  createTime: []
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileApi.getFilePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = () => {
  formRef.value.open()
}

/** 复制到剪贴板方法 */
const copyToClipboard = async (text: string) => {
  const { copy, copied, isSupported } = useClipboard({ legacy: true, source: text })
  if (!isSupported) {
    message.error(t('common.copyError'))
    return
  }
  await copy()
  if (unref(copied)) {
    message.success(t('common.copySuccess'))
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileApi.deleteFile(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除按钮操作 */
const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (rows) => {
  checkedIds.value = rows.map((row) => row.id)
}

const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await FileApi.deleteFileList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

// ========== 文件夹相�?==========
const folderLoading = ref(false)
const folderTree = ref<FileFolderVO[]>([])
const currentFolder = ref<FileFolderVO | null>(null)

/** 加载文件夹树 */
const loadFolderTree = async () => {
  folderLoading.value = true
  try {
    const data = await FileFolderApi.getFileFolderTree()
    folderTree.value = [{ id: 0, name: '根目�?, children: data, icon: 'ep:home-filled' } as any]
  } finally {
    folderLoading.value = false
  }
}

/** 文件夹点击事�?*/
const handleFolderClick = (data: FileFolderVO) => {
  currentFolder.value = data
  // 这里可以根据文件夹ID筛选文�?
  // queryParams.folderId = data.id
  // handleQuery()
}

// ========== 标签相关 ==========
const tagLoading = ref(false)
const tagList = ref<FileTagVO[]>([])
const selectedTagId = ref<number | null>(null)
const selectedTag = computed(() => tagList.value.find((t) => t.id === selectedTagId.value))

/** 加载标签列表 */
const loadTagList = async () => {
  tagLoading.value = true
  try {
    const data = await FileTagApi.getFileTagList()
    tagList.value = data
  } finally {
    tagLoading.value = false
  }
}

/** 标签点击事件 */
const handleTagClick = (tag: FileTagVO) => {
  selectedTagId.value = selectedTagId.value === tag.id ? null : tag.id
  // 这里可以根据标签ID筛选文�?
  // queryParams.tagId = selectedTagId.value
  // handleQuery()
}

/** 清除标签筛�?*/
const clearTagFilter = () => {
  selectedTagId.value = null
  // queryParams.tagId = undefined
  // handleQuery()
}

/** 清除所有筛�?*/
const clearFilters = () => {
  currentFolder.value = null
  selectedTagId.value = null
  // queryParams.folderId = undefined
  // queryParams.tagId = undefined
  // handleQuery()
}

// ========== 弹窗相关 ==========
const folderDialogVisible = ref(false)
const tagDialogVisible = ref(false)
const bizRelDialogVisible = ref(false)

const openFolderDialog = () => {
  folderDialogVisible.value = true
}

const openTagDialog = () => {
  tagDialogVisible.value = true
}

const openBizRelDialog = () => {
  bizRelDialogVisible.value = true
}

const openRecycleBin = () => {
  router.push('/infra/file/recycle')
}

const openOperationLog = () => {
  router.push('/infra/file/log')
}

// ========== 工具方法 ==========
const getFileIcon = (type: string) => {
  if (!type) return 'ep:document'
  if (type.includes('image')) return 'ep:picture'
  if (type.includes('pdf')) return 'ep:document'
  if (type.includes('word') || type.includes('doc')) return 'ep:document'
  if (type.includes('excel') || type.includes('xls')) return 'ep:grid'
  if (type.includes('ppt') || type.includes('presentation')) return 'ep:picture'
  if (type.includes('video')) return 'ep:video-camera'
  if (type.includes('audio')) return 'ep:microphone'
  if (type.includes('zip') || type.includes('rar') || type.includes('7z')) return 'ep:folder'
  return 'ep:document'
}

const getFileIconColor = (type: string) => {
  if (!type) return 'text-[var(--erp-slate-500)]'
  if (type.includes('image')) return 'text-[var(--erp-success-500)]'
  if (type.includes('pdf')) return 'text-[var(--erp-danger-500)]'
  if (type.includes('word') || type.includes('doc')) return 'text-[var(--erp-primary-500)]'
  if (type.includes('excel') || type.includes('xls')) return 'text-[var(--erp-success-600)]'
  if (type.includes('video')) return 'text-[var(--erp-warning-500)]'
  return 'text-[var(--erp-slate-500)]'
}

/** 初始�?**/
onMounted(() => {
  getList()
  loadFolderTree()
  loadTagList()
})
</script>

<style scoped lang="scss">
.file-management-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: var(--erp-shadow-sm);
}

.file-management-container {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 200px);
}

.file-management-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: white;
  border-radius: 8px;
  box-shadow: var(--erp-shadow-sm);
  padding: 16px;
  overflow-y: auto;
}

.file-management-main {
  flex: 1;
  min-width: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--erp-slate-100);
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
}

.sidebar-content {
  max-height: 300px;
  overflow-y: auto;
}

.current-location-tip {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: var(--erp-primary-50);
  border-radius: 8px;
  font-size: 14px;
  color: var(--erp-slate-700);
}
</style>
