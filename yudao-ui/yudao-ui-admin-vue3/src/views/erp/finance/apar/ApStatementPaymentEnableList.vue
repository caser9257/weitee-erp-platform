<template>
  <Dialog
    v-model="dialogVisible"
    title="可核销应付记录"
    width="min(1180px, 96vw)"
    scroll
    maxHeight="80vh"
  >
    <div class="ap-payment-selector">
      <ContentWrap>
        <el-form ref="queryFormRef" :model="queryParams" label-width="80px" class="ap-payment-selector__form">
          <div class="ap-payment-selector__form-grid">
            <el-form-item label="台账编号" prop="statementNo">
              <el-input
                v-model="queryParams.statementNo"
                placeholder="请输入台账编号"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable>
                <el-option
                  v-for="item in ERP_AP_BIZ_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="业务单号" prop="bizNo">
              <el-input
                v-model="queryParams.bizNo"
                placeholder="请输入业务单号"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="结算账户" prop="accountId">
              <el-select v-model="queryParams.accountId" placeholder="请选择结算账户" clearable filterable>
                <el-option
                  v-for="item in accountList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="ap-payment-selector__actions">
            <el-button :loading="loading" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" />
              查询
            </el-button>
            <el-button :disabled="loading" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>
      </ContentWrap>

      <ContentWrap>
        <div class="ap-payment-selector__hint">
          当前供应商：<strong>{{ currentSupplierName || `#${currentSupplierId}` }}</strong>
        </div>
        <div class="ap-payment-selector__table-wrap">
          <el-table
            v-loading="loading"
            :data="list"
            row-key="id"
            stripe
            show-overflow-tooltip
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="42" />
            <el-table-column prop="statementNo" label="台账编号" min-width="180" />
            <el-table-column prop="bizType" label="业务类型" width="110" align="center">
              <template #default="{ row }">
                {{ getBizTypeLabel(row.bizType) }}
              </template>
            </el-table-column>
            <el-table-column prop="bizNo" label="业务单号" min-width="150" />
            <el-table-column prop="sourceOrderNo" label="来源采购单" min-width="150" />
            <el-table-column
              prop="bizDate"
              label="业务日期"
              width="160"
              align="center"
              :formatter="dateFormatter2"
            />
            <el-table-column
              prop="dueDate"
              label="到期日期"
              width="160"
              align="center"
              :formatter="dateFormatter2"
            />
            <el-table-column
              prop="amount"
              label="应付金额"
              min-width="120"
              align="right"
              class-name="font-mono"
              :formatter="erpPriceTableColumnFormatter"
            />
            <el-table-column
              prop="paidAmount"
              label="已核销金额"
              min-width="120"
              align="right"
              class-name="font-mono"
              :formatter="erpPriceTableColumnFormatter"
            />
            <el-table-column
              prop="remainAmount"
              label="剩余金额"
              min-width="120"
              align="right"
              class-name="font-mono"
              :formatter="erpPriceTableColumnFormatter"
            />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" effect="light">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <Pagination
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="getList"
        />
      </ContentWrap>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="loading || !selectionList.length" @click="submitSelection">
        确定
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import {
  ApStatementApi,
  ApStatementPaymentEnableVO,
  ERP_AP_BIZ_TYPE_OPTIONS,
  ERP_AP_STATEMENT_STATUS_OPTIONS
} from '@/api/erp/finance/apar'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { dateFormatter2 } from '@/utils/formatTime'
import { erpPriceTableColumnFormatter } from '@/utils'

defineOptions({ name: 'ApStatementPaymentEnableList' })

const dialogVisible = ref(false)
const loading = ref(false)
const total = ref(0)
const list = ref<ApStatementPaymentEnableVO[]>([])
const selectionList = ref<ApStatementPaymentEnableVO[]>([])
const queryFormRef = ref()
const accountList = ref<AccountVO[]>([])
const currentSupplierId = ref<number>()
const currentSupplierName = ref('')

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  supplierId: undefined as number | undefined,
  bizType: undefined as number | undefined,
  statementNo: undefined as string | undefined,
  bizNo: undefined as string | undefined,
  accountId: undefined as number | undefined
})

const getBizTypeLabel = (value?: number) =>
  ERP_AP_BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

const getStatusLabel = (value?: number) =>
  ERP_AP_STATEMENT_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getStatusTagType = (value?: number) =>
  (
    {
      10: 'warning',
      20: 'primary',
      30: 'success',
      40: 'info'
    } as Record<number, 'primary' | 'success' | 'warning' | 'info'>
  )[value || 0] || 'info'

const handleSelectionChange = (rows: ApStatementPaymentEnableVO[]) => {
  selectionList.value = rows
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ApStatementApi.getPaymentEnablePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  selectionList.value = []
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.supplierId = currentSupplierId.value
  await handleQuery()
}

const emit = defineEmits<{
  (e: 'success', value: ApStatementPaymentEnableVO[]): void
}>()

const submitSelection = () => {
  emit('success', selectionList.value)
  dialogVisible.value = false
}

const open = async (supplierId: number, supplierName?: string) => {
  dialogVisible.value = true
  currentSupplierId.value = supplierId
  currentSupplierName.value = supplierName || ''
  queryParams.supplierId = supplierId
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  queryParams.bizType = undefined
  queryParams.statementNo = undefined
  queryParams.bizNo = undefined
  queryParams.accountId = undefined
  selectionList.value = []
  if (!accountList.value.length) {
    accountList.value = await AccountApi.getAccountSimpleList()
  }
  await getList()
}

defineExpose({ open })
</script>

<style scoped>
.ap-payment-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ap-payment-selector__form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ap-payment-selector__form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.ap-payment-selector__actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.ap-payment-selector__hint {
  margin-bottom: 12px;
  color: #475569;
}

.ap-payment-selector__hint strong {
  color: #0f172a;
}

.ap-payment-selector__table-wrap {
  overflow-x: auto;
}

@media (max-width: 1200px) {
  .ap-payment-selector__form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .ap-payment-selector__form-grid {
    grid-template-columns: 1fr;
  }

  .ap-payment-selector__actions {
    justify-content: stretch;
  }
}
</style>
