<template>
  <doc-alert title="【财务】账簿管理" url="https://doc.iocoder.cn/erp/" />

  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-ledger-page__query-form">
      <div class="finance-ledger-page__query-grid">
        <el-form-item label="账簿编码" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入账簿编码"
            clearable
            class="!w-full"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="账簿名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入账簿名称"
            clearable
            class="!w-full"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择启用状态" clearable class="!w-full">
            <el-option
              v-for="item in COMMON_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="默认账簿" prop="defaultStatus">
          <el-select v-model="queryParams.defaultStatus" placeholder="请选择默认账簿" clearable class="!w-full">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="queryParams.remark"
            placeholder="请输入备注"
            clearable
            class="!w-full"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
      </div>
      <div class="finance-ledger-page__query-actions">
        <el-button :loading="listLoading" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          查询
        </el-button>
        <el-button :disabled="listLoading" @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="finance-ledger-page__toolbar">
      <div class="finance-ledger-page__toolbar-actions">
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:finance-ledger:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          type="success"
          plain
          :loading="exportLoading"
          :disabled="listLoading"
          @click="handleExport"
          v-hasPermi="['erp:finance-ledger:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
      </div>
    </div>

    <div v-if="listErrorMessage && !list.length" class="finance-ledger-page__state">
      <el-result icon="error" title="账簿列表加载失败" :sub-title="listErrorMessage">
        <template #extra>
          <el-button type="primary" @click="getList">重试</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <div v-if="listLoading || list.length" class="finance-ledger-page__table-wrap">
        <el-table v-loading="listLoading" :data="list" stripe show-overflow-tooltip>
          <el-table-column label="账簿信息" min-width="240">
            <template #default="{ row }">
              <div class="finance-ledger-page__primary-cell">
                <span class="finance-ledger-page__primary-text">{{ row.name || '-' }}</span>
                <span class="finance-ledger-page__muted-text font-mono">{{ row.no || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="启用状态" align="center" width="110">
            <template #default="{ row }">
              <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="默认账簿" align="center" width="110">
            <template #default="{ row }">
              <el-tag :type="row.defaultStatus ? 'success' : 'info'" effect="light" round>
                {{ row.defaultStatus ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="排序" align="right" width="90" prop="sort" />
          <el-table-column label="备注" min-width="220">
            <template #default="{ row }">
              <span class="finance-ledger-page__muted-text" :title="row.remark || '-'">
                {{ row.remark || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" width="180" :formatter="dateFormatter" prop="createTime" />
          <el-table-column label="操作" align="center" fixed="right" width="240">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="isRowBusy(row.id)" @click="openForm('update', row.id)">
                编辑
              </el-button>
              <el-button
                link
                :type="row.defaultStatus ? 'warning' : 'success'"
                :loading="defaultStatusLoadingId === row.id"
                :disabled="isRowBusy(row.id)"
                @click="toggleDefaultStatus(row)"
              >
                {{ row.defaultStatus ? '取消默认' : '设为默认' }}
              </el-button>
              <el-button
                link
                type="danger"
                :loading="deleteLoadingId === row.id"
                :disabled="isRowBusy(row.id)"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-else class="finance-ledger-page__state" description="暂无账簿数据" />

      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </template>
  </ContentWrap>

  <LedgerForm ref="formRef" @success="handleFormSuccess" />
</template>

<script setup lang="ts">
import { COMMON_STATUS_OPTIONS } from '@/views/erp/finance/shared/accounting'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  ErpFinanceLedgerPageReqVO,
  ErpFinanceLedgerVO,
  FinanceLedgerApi
} from '@/api/erp/finance/ledger'
import LedgerForm from './LedgerForm.vue'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'ErpFinanceLedger' })

const message = useMessage()

const queryFormRef = ref()
const formRef = ref<InstanceType<typeof LedgerForm>>()
const listLoading = ref(false)
const exportLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingId = ref<number>()
const defaultStatusLoadingId = ref<number>()
const list = ref<ErpFinanceLedgerVO[]>([])
const total = ref(0)

const queryParams = reactive<ErpFinanceLedgerPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  name: undefined,
  status: undefined,
  defaultStatus: undefined,
  remark: undefined
})

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) =>
  (id != null && deleteLoadingId.value === id) || (id != null && defaultStatusLoadingId.value === id)

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceLedgerApi.getLedgerPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '账簿列表加载失败'
    }
  } finally {
    listLoading.value = false
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

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  try {
    await message.delConfirm()
    deleteLoadingId.value = id
    await FinanceLedgerApi.deleteLedger(id)
    message.success('删除成功')
    if (list.value.length === 1 && queryParams.pageNo > 1) {
      queryParams.pageNo -= 1
    }
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    deleteLoadingId.value = undefined
  }
}

const toggleDefaultStatus = async (row: ErpFinanceLedgerVO) => {
  if (!row.id || defaultStatusLoadingId.value) {
    return
  }
  const nextValue = !row.defaultStatus
  try {
    await message.confirm(`确认${nextValue ? '设为' : '取消'}“${row.name || '-'}”为默认账簿吗？`)
    defaultStatusLoadingId.value = row.id
    await FinanceLedgerApi.updateLedgerDefaultStatus(row.id, nextValue)
    message.success(nextValue ? '设为默认成功' : '取消默认成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    defaultStatusLoadingId.value = undefined
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await FinanceLedgerApi.exportLedger(queryParams)
    download.excel(data, '财务账簿.xls')
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    exportLoading.value = false
  }
}

const handleFormSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

onMounted(() => {
  void getList()
})
</script>

<style scoped>
.finance-ledger-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-ledger-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-ledger-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-ledger-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-ledger-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-ledger-page__table-wrap {
  overflow-x: auto;
}

.finance-ledger-page__state {
  padding: 24px 0 8px;
}

.finance-ledger-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-ledger-page__primary-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.finance-ledger-page__muted-text {
  color: var(--el-text-color-secondary);
}

@media (max-width: 1440px) {
  .finance-ledger-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-ledger-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-ledger-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-ledger-page__query-actions,
  .finance-ledger-page__toolbar {
    justify-content: stretch;
  }
}
</style>
