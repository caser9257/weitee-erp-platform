<template>
  <ContentWrap title="文件夹管理">
    <template #header>
      <el-button type="primary" plain @click="openForm()" v-hasPermi="['infra:file-folder:create']">
        <Icon icon="ep:folder-add" class="mr-5px" /> 新增文件夹
      </el-button>
    </template>

    <el-table
      v-loading="loading"
      :data="folderList"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      default-expand-all
    >
      <el-table-column label="文件夹名称" prop="name" min-width="200">
        <template #default="{ row }">
          <div class="flex items-center">
            <Icon :icon="row.icon || 'ep:folder'" class="mr-8px text-lg text-[var(--erp-primary-500)]" />
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="路径" prop="path" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :formatter="dateFormatter" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(undefined, row.id)" v-hasPermi="['infra:file-folder:create']">
            <Icon icon="ep:folder-add" class="mr-3px" /> 新增子文件夹
          </el-button>
          <el-button link type="primary" @click="openForm(row.id)" v-hasPermi="['infra:file-folder:update']">
            <Icon icon="ep:edit" class="mr-3px" /> 编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['infra:file-folder:delete']">
            <Icon icon="ep:delete" class="mr-3px" /> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <FileFolderForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileFolderApi from '@/api/infra/fileFolder'
import type { FileFolderVO } from '@/api/infra/fileFolder'
import FileFolderForm from './FileFolderForm.vue'

defineOptions({ name: 'InfraFileFolder' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(false)
const folderList = ref<FileFolderVO[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileFolderApi.getFileFolderTree()
    folderList.value = data
  } finally {
    loading.value = false
  }
}

/** 新增/编辑操作 */
const formRef = ref()
const openForm = (id?: number, parentId?: number) => {
  formRef.value.open(id, parentId)
}

/** 删除操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileFolderApi.deleteFileFolder(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
