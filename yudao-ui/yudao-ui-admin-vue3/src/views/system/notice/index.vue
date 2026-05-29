<template>
  <ContentWrap class="notice-page__filter-card">
    <div class="notice-page__title">通知公告</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="notice-query"
    >
      <div class="notice-query__grid">
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入公告标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="公告状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择公告状态" clearable>
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </div>

      <div class="notice-query__footer">
        <div></div>
        <div class="notice-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="notice-page__list-card">
    <div class="notice-toolbar">
      <div class="notice-toolbar__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['system:notice:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          plain
          type="primary"
          :disabled="!canBatchEdit"
          @click="handleOpenBatchEdit"
          v-hasPermi="['system:notice:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
        </el-button>
      </div>
      <div class="notice-toolbar__meta">
        <el-button
          plain
          type="danger"
          :disabled="!canBatchDelete"
          :loading="batchDeleteLoading"
          @click="handleDeleteBatch"
          v-hasPermi="['system:notice:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <div class="notice-page__table-shell">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="list"
        row-key="id"
        :stripe="true"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <div v-if="listLoadError" class="notice-empty notice-empty--error">
            <div class="notice-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="notice-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="loading" @click="handleReloadList">
              重试加载
            </el-button>
          </div>
          <div v-else class="notice-empty">
            <div class="notice-empty__icon">
              <Icon icon="ep:bell" />
            </div>
            <div class="notice-empty__title">暂无通知公告</div>
          </div>
        </template>

        <el-table-column width="36" type="selection" />
        <el-table-column label="公告编号" prop="id" width="120" />
        <el-table-column label="公告标题" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="公告类型" min-width="120" align="center">
          <template #default="{ row }">
            <dict-tag :type="DICT_TYPE.SYSTEM_NOTICE_TYPE" :value="row.type" />
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120" align="center">
          <template #default="{ row }">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" :formatter="dateFormatter" />
        <el-table-column label="操作" width="220" fixed="right" align="left">
          <template #default="{ row }">
            <div class="notice-actions">
              <el-button link type="primary" @click="openForm('update', row.id)" v-hasPermi="['system:notice:update']">
                编辑
              </el-button>
              <el-button link @click="handlePush(row.id)" v-hasPermi="['system:notice:update']">
                推送
              </el-button>
              <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['system:notice:delete']">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="notice-page__footer">
      <div class="notice-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handlePagination"
      />
    </div>
  </ContentWrap>

  <NoticeForm ref="formRef" @success="getList" />
  <NoticeBatchEditDrawer ref="batchEditDrawerRef" @success="handleBatchEditSuccess" />
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as NoticeApi from '@/api/system/notice'
import NoticeForm from './NoticeForm.vue'
import NoticeBatchEditDrawer from './components/NoticeBatchEditDrawer.vue'

defineOptions({ name: 'SystemNotice' })

type NoticeRow = NoticeApi.NoticeVO

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const listLoadError = ref(false)
const batchDeleteLoading = ref(false)
const list = ref<NoticeRow[]>([])
const total = ref(0)
const selectedRows = ref<NoticeRow[]>([])
const tableRef = ref()
const formRef = ref()
const batchEditDrawerRef = ref()
const queryFormRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: '',
  status: undefined as number | undefined
})

const selectedRowIds = computed(() =>
  selectedRows.value
    .map((item) => item.id)
    .filter((id): id is number => id !== undefined && id !== null)
)
const hasSelectedRows = computed(() => selectedRowIds.value.length > 0)
const canBatchEdit = computed(() => hasSelectedRows.value && !loading.value)
const canBatchDelete = computed(() => hasSelectedRows.value && !batchDeleteLoading.value)

const clearSelectionState = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}

const getList = async () => {
  clearSelectionState()
  loading.value = true
  listLoadError.value = false
  try {
    const data = await NoticeApi.getNoticePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listLoadError.value = true
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getList()
}

const handlePagination = async () => {
  await getList()
}

const handleReloadList = async () => {
  await getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleSelectionChange = (rows: NoticeRow[]) => {
  selectedRows.value = (rows || []).filter((item) => item.id !== undefined && item.id !== null)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await NoticeApi.deleteNotice(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleDeleteBatch = async () => {
  if (!hasSelectedRows.value || batchDeleteLoading.value) {
    return
  }
  const selectedIdsSnapshot = selectedRowIds.value.slice()
  try {
    await message.delConfirm()
    batchDeleteLoading.value = true
    await NoticeApi.deleteNoticeList(selectedIdsSnapshot)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    batchDeleteLoading.value = false
  }
}

const handlePush = async (id: number) => {
  try {
    await message.confirm('是否推送该通知公告？')
    await NoticeApi.pushNotice(id)
    message.success('推送成功')
  } catch {}
}

const handleOpenBatchEdit = () => {
  if (!hasSelectedRows.value) {
    message.warning('请先选择要修改的数据')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: selectedRowIds.value.slice(),
    rows: selectedRows.value.slice()
  })
}

const handleBatchEditSuccess = async () => {
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.notice-page__filter-card,
.notice-page__list-card {
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.notice-page__title {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.notice-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.notice-query__footer,
.notice-toolbar,
.notice-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.notice-query__actions,
.notice-toolbar__actions,
.notice-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.notice-page__table-shell {
  overflow-x: auto;
}

.notice-empty {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
}

.notice-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.notice-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.notice-empty--error .notice-empty__icon {
  background: rgba(248, 113, 113, 0.12);
  color: #e11d48;
}

.notice-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.notice-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 1279px) {
  .notice-query__grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .notice-query__footer,
  .notice-toolbar,
  .notice-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .notice-query__actions,
  .notice-toolbar__actions,
  .notice-toolbar__meta {
    width: 100%;
  }
}
</style>
