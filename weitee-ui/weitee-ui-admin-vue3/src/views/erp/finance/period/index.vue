<template>
  <div class="finance-period-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-period-page__query-form">
        <div class="finance-period-page__query-grid">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select v-model="queryParams.ledgerId" placeholder="请选择账簿" clearable filterable :loading="ledgerLoading" class="!w-full">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="displayLedgerName(item.name)" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="期间编码" prop="periodCode">
            <el-input v-model="queryParams.periodCode" placeholder="请输入期间编码" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="会计年度" prop="periodYear">
            <el-input-number v-model="queryParams.periodYear" :min="2000" :max="2100" controls-position="right" class="!w-full" />
          </el-form-item>
          <el-form-item label="会计月份" prop="periodMonth">
            <el-input-number v-model="queryParams.periodMonth" :min="1" :max="12" controls-position="right" class="!w-full" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in PERIOD_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-period-page__query-actions">
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
      <div class="finance-period-page__toolbar">
        <div class="finance-period-page__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:finance-period:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增
          </el-button>
          <el-button type="success" plain @click="openCreateYear()" v-hasPermi="['erp:finance-period:create']">
            <Icon icon="ep:calendar" class="mr-5px" />
            按年生成
          </el-button>
          <el-button
            type="success"
            plain
            :loading="exportLoading"
            :disabled="listLoading"
            @click="handleExport"
            v-hasPermi="['erp:finance-period:export']"
          >
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !list.length" class="finance-period-page__state">
        <el-result icon="error" title="期间列表加载失败" :sub-title="listErrorMessage">
          <template #extra>
            <el-button type="primary" @click="getList">重试</el-button>
          </template>
        </el-result>
      </div>

      <template v-else>
        <div v-if="listLoading || list.length" class="finance-period-page__table-wrap">
          <el-table v-loading="listLoading" :data="list" stripe show-overflow-tooltip>
            <el-table-column label="期间信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-period-page__primary-cell">
                  <span class="finance-period-page__primary-text font-mono">{{ row.periodCode || '-' }}</span>
                  <span class="finance-period-page__muted-text">{{ displayLedgerName(row.ledgerName) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="会计年度" prop="periodYear" align="right" width="100" />
            <el-table-column label="会计月份" prop="periodMonth" align="right" width="100" />
            <el-table-column label="日期范围" min-width="220">
              <template #default="{ row }">
                {{ formatDateValue(row.startDate) }} 至 {{ formatDateValue(row.endDate) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 10 ? 'success' : 'info'" effect="light" round>
                  {{ getPeriodStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="关账时间" min-width="180">
              <template #default="{ row }">{{ formatDateTimeValue(row.closeTime) }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" fixed="right" width="220">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 10"
                  link
                  type="warning"
                  :loading="statusLoadingId === row.id"
                  :disabled="isRowBusy(row.id)"
                  @click="handleClose(row.id)"
                >
                  关账
                </el-button>
                <el-button
                  v-else
                  link
                  type="success"
                  :loading="statusLoadingId === row.id"
                  :disabled="isRowBusy(row.id)"
                  @click="handleReopen(row.id)"
                >
                  反关账
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-else description="暂无数据" />

        <Pagination
          v-if="total > 0"
          v-model:limit="queryParams.pageSize"
          v-model:page="queryParams.pageNo"
          :total="total"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <PeriodForm ref="formRef" @success="handleFormSuccess" />
    <PeriodCreateYearForm ref="createYearFormRef" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { PERIOD_STATUS_OPTIONS, formatDateTimeValue, formatDateValue, getPeriodStatusLabel } from '@/views/erp/finance/shared/accounting'
import download from '@/utils/download'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import {
  ErpFinancePeriodPageReqVO,
  ErpFinancePeriodVO,
  FinancePeriodApi
} from '@/api/erp/finance/period'
import PeriodForm from './PeriodForm.vue'
import PeriodCreateYearForm from './PeriodCreateYearForm.vue'
import { displayLedgerName } from '@/utils/financeDisplay'

defineOptions({ name: 'ErpFinancePeriod' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref<InstanceType<typeof PeriodForm>>()
const createYearFormRef = ref<InstanceType<typeof PeriodCreateYearForm>>()
const ledgerLoading = ref(false)
const listLoading = ref(false)
const exportLoading = ref(false)
const listErrorMessage = ref('')
const statusLoadingId = ref<number>()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const list = ref<ErpFinancePeriodVO[]>([])
const total = ref(0)

const queryParams = reactive<ErpFinancePeriodPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined,
  periodCode: undefined,
  periodYear: undefined,
  periodMonth: undefined,
  status: undefined
})

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) => id != null && statusLoadingId.value === id

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    ledgerLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinancePeriodApi.getPeriodPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '期间列表加载失败'
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

const openForm = (type: 'create', id?: number) => {
  formRef.value?.open(type, id)
}

const openCreateYear = () => {
  createYearFormRef.value?.open()
}

const handleClose = async (id?: number) => {
  if (!id || statusLoadingId.value) {
    return
  }
  try {
    await message.confirm('确认关账该期间吗？')
    statusLoadingId.value = id
    await FinancePeriodApi.closePeriod(id)
    message.success('关账成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    statusLoadingId.value = undefined
  }
}

const handleReopen = async (id?: number) => {
  if (!id || statusLoadingId.value) {
    return
  }
  try {
    await message.confirm('确认反关账该期间吗？')
    statusLoadingId.value = id
    await FinancePeriodApi.reopenPeriod(id)
    message.success('反关账成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    statusLoadingId.value = undefined
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await FinancePeriodApi.exportPeriod(queryParams)
    download.excel(data, '会计期间.xls')
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

onMounted(async () => {
  await Promise.allSettled([loadLedgers(), getList()])
})
</script>

<style scoped>
.finance-period-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-period-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-period-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-period-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-period-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-period-page__table-wrap {
  overflow-x: auto;
}

.finance-period-page__state {
  padding: 24px 0 8px;
}

.finance-period-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-period-page__primary-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.finance-period-page__muted-text {
  color: var(--el-text-color-secondary);
}

@media (max-width: 1440px) {
  .finance-period-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-period-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-period-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-period-page__query-actions,
  .finance-period-page__toolbar {
    justify-content: stretch;
  }
}
</style>
