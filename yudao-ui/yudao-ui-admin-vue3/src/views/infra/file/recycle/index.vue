<template>
  <ContentWrap title="文件回收站">
    <template #header>
      <div class="flex items-center gap-8px">
        <el-button
          type="danger"
          plain
          :disabled="checkedIds.length === 0"
          @click="handlePermanentDeleteBatch"
          v-hasPermi="['infra:file:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 彻底删除
        </el-button>
        <el-button
          type="warning"
          plain
          :disabled="checkedIds.length === 0"
          @click="handleRestoreBatch"
          v-hasPermi="['infra:file:update']"
        >
          <Icon icon="ep:refresh-left" class="mr-5px" /> 批量恢复
        </el-button>
        <el-button
          type="danger"
          @click="handleEmptyRecycleBin"
          v-hasPermi="['infra:file:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 清空回收站
        </el-button>
      </div>
    </template>

    <!-- 搜索 -->
    <el-form
      class="-mb-15px mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="文件名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入文件名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="文件类型" prop="type">
        <el-input
          v-model="queryParams.type"
          placeholder="请输入文件类型"
          clearable
          @keyup.enter="handleQuery"
          class="!w-160px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleRowCheckboxChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="文件名" prop="name" min-width="200" :show-overflow-tooltip="true">
        <template #default="{ row }">
          <div class="flex items-center gap-8px">
            <Icon :icon="getFileIcon(row.type)" class="text-lg" :class="getFileIconColor(row.type)" />
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文件类型" prop="type" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" prop="size" width="100" align="right" :formatter="fileSizeFormatter" />
      <el-table-column label="删除人" prop="deleteUserName" width="100" align="center" />
      <el-table-column label="删除原因" prop="deleteReason" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="删除时间" prop="deleteTime" width="180" align="center" :formatter="dateFormatter" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleRestore(row.id)"
            v-hasPermi="['infra:file:update']"
          >
            <Icon icon="ep:refresh-left" class="mr-3px" /> 恢复
          </el-button>
          <el-button
            link
            type="danger"
            @click="handlePermanentDelete(row.id)"
            v-hasPermi="['infra:file:delete']"
          >
            <Icon icon="ep:delete" class="mr-3px" /> 彻底删除
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
</template>

<script lang="ts" setup>
import { fileSizeFormatter } from '@/utils'
import { dateFormatter } from '@/utils/formatTime'
import * as FileApi from '@/api/infra/file'

defineOptions({ name: 'InfraFileRecycleBin' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const list = ref([])
const queryFormRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  type: undefined
})

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileApi.getRecycleFilePage(queryParams)
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

/** 恢复文件 */
const handleRestore = async (id: number) => {
  try {
    await message.confirm('确定要恢复该文件吗？')
    await FileApi.restoreFile(id)
    message.success('恢复成功')
    await getList()
  } catch {}
}

/** 批量恢复 */
const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (rows: any[]) => {
  checkedIds.value = rows.map((row) => row.id)
}

const handleRestoreBatch = async () => {
  try {
    await message.confirm(`确定要恢复选中的 ${checkedIds.value.length} 个文件吗？`)
    await FileApi.restoreFileList(checkedIds.value)
    checkedIds.value = []
    message.success('恢复成功')
    await getList()
  } catch {}
}

/** 彻底删除 */
const handlePermanentDelete = async (id: number) => {
  try {
    await message.delConfirm('确定要彻底删除该文件吗？此操作不可恢复！')
    await FileApi.permanentDeleteFile(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量彻底删除 */
const handlePermanentDeleteBatch = async () => {
  try {
    await message.delConfirm(`确定要彻底删除选中的 ${checkedIds.value.length} 个文件吗？此操作不可恢复！`)
    await FileApi.permanentDeleteFileList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 清空回收站 */
const handleEmptyRecycleBin = async () => {
  try {
    await message.delConfirm('确定要清空回收站吗？此操作将永久删除所有文件，不可恢复！')
    await FileApi.emptyRecycleBin()
    message.success('清空成功')
    await getList()
  } catch {}
}

/** 获取文件图标 */
const getFileIcon = (type: string) => {
  if (!type) return 'ep:document'
  if (type.includes('image')) return 'ep:picture'
  if (type.includes('pdf')) return 'ep:document'
  if (type.includes('video')) return 'ep:video-camera'
  if (type.includes('audio')) return 'ep:microphone'
  return 'ep:document'
}

const getFileIconColor = (type: string) => {
  if (!type) return 'text-[var(--erp-slate-500)]'
  if (type.includes('image')) return 'text-[var(--erp-success-500)]'
  if (type.includes('pdf')) return 'text-[var(--erp-danger-500)]'
  return 'text-[var(--erp-slate-500)]'
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
