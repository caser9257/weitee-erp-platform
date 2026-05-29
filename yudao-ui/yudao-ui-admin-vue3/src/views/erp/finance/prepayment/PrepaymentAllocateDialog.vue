<template>
  <Dialog
    v-model="dialogVisible"
    title="预付款核销"
    width="min(1180px, 96vw)"
    scroll
    maxHeight="80vh"
  >
    <div class="finance-prepayment-allocate">
      <ContentWrap class="finance-prepayment-allocate__summary-card">
        <div class="finance-prepayment-allocate__summary-grid">
          <div class="finance-prepayment-allocate__summary-item">
            <span>预付款单号</span>
            <strong>{{ prepaymentData?.no || '-' }}</strong>
          </div>
          <div class="finance-prepayment-allocate__summary-item">
            <span>供应商</span>
            <strong>{{ prepaymentData?.supplierName || '-' }}</strong>
          </div>
          <div class="finance-prepayment-allocate__summary-item">
            <span>可用余额</span>
            <strong class="finance-prepayment-allocate__amount">
              {{ formatAmount(prepaymentData?.remainPrice) }}
            </strong>
          </div>
        </div>
      </ContentWrap>

      <ContentWrap>
        <el-form
          ref="queryFormRef"
          :model="queryParams"
          label-width="80px"
          class="finance-prepayment-allocate__form"
        >
          <div class="finance-prepayment-allocate__form-grid">
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
          <div class="finance-prepayment-allocate__actions">
            <el-button :loading="loadingList" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button :disabled="loadingList" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>
      </ContentWrap>

      <ContentWrap>
        <div v-if="listErrorMessage && !list.length" class="finance-prepayment-allocate__state">
          <el-result icon="error" title="可核销应付台账加载失败" :sub-title="listErrorMessage">
            <template #extra>
              <el-button type="primary" @click="getList">重试</el-button>
            </template>
          </el-result>
        </div>
        <template v-else>
          <div class="finance-prepayment-allocate__table-wrap">
            <el-table
              v-loading="loadingList"
              :data="list"
              row-key="id"
              stripe
              class="finance-prepayment-allocate__table"
              :show-overflow-tooltip="false"
            >
              <el-table-column prop="statementNo" label="台账编号" min-width="180" />
              <el-table-column prop="bizNo" label="业务单号" min-width="160" />
              <el-table-column label="业务类型" min-width="110" align="center">
                <template #default="{ row }">
                  {{ getBizTypeLabel(row.bizType) }}
                </template>
              </el-table-column>
              <el-table-column label="业务日期" min-width="160" align="center">
                <template #default="{ row }">
                  {{ formatDateValue(row.bizDate) }}
                </template>
              </el-table-column>
              <el-table-column label="应付金额" min-width="120" align="right">
                <template #default="{ row }">
                  <span class="finance-prepayment-allocate__amount">{{ formatAmount(row.amount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="已核销金额" min-width="120" align="right">
                <template #default="{ row }">
                  <span class="finance-prepayment-allocate__amount">{{ formatAmount(row.paidAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="剩余金额" min-width="120" align="right">
                <template #default="{ row }">
                  <span class="finance-prepayment-allocate__amount">{{ formatAmount(row.remainAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="本次核销" min-width="160">
                <template #default="{ row }">
                  <el-input-number
                    v-model="allocateAmountMap[row.id!]"
                    controls-position="right"
                    :precision="2"
                    :min="0"
                    :max="getAllocateMax(row)"
                    class="!w-full"
                    placeholder="请输入核销金额"
                  />
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="180">
                <template #default="{ row }">
                  <el-input
                    v-model="allocateRemarkMap[row.id!]"
                    placeholder="请输入备注"
                    clearable
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-if="!loadingList && !list.length" description="暂无可核销应付台账" />
          <Pagination
            v-if="total > 0"
            v-model:page="queryParams.pageNo"
            v-model:limit="queryParams.pageSize"
            :total="total"
            @pagination="getList"
          />
        </template>
      </ContentWrap>
    </div>
    <template #footer>
      <el-button :disabled="allocateLoading" @click="dialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="allocateLoading"
        :disabled="submitDisabled"
        @click="submitAllocate"
      >
        保存
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import {
  ApStatementApi,
  ERP_AP_BIZ_TYPE_OPTIONS,
  type ApStatementPaymentEnableVO
} from '@/api/erp/finance/apar'
import {
  FinancePrepaymentApi,
  type ErpFinancePrepaymentVO
} from '@/api/erp/finance/prepayment'
import { formatAmount, formatDateValue } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'PrepaymentAllocateDialog' })

const message = useMessage()

const dialogVisible = ref(false)
const loadingList = ref(false)
const allocateLoading = ref(false)
const listErrorMessage = ref('')
const total = ref(0)
const list = ref<ApStatementPaymentEnableVO[]>([])
const accountList = ref<AccountVO[]>([])
const prepaymentData = ref<ErpFinancePrepaymentVO>()
const queryFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  supplierId: undefined as number | undefined,
  bizType: undefined as number | undefined,
  statementNo: undefined as string | undefined,
  bizNo: undefined as string | undefined,
  accountId: undefined as number | undefined
})

const allocateAmountMap = reactive<Record<number, number | undefined>>({})
const allocateRemarkMap = reactive<Record<number, string | undefined>>({})

const getBizTypeLabel = (value?: number) =>
  ERP_AP_BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

const totalAllocateAmount = computed(() =>
  Object.values(allocateAmountMap).reduce((sum, value) => sum + Number(value || 0), 0)
)

const getAllocateMax = (row: ApStatementPaymentEnableVO) => {
  const remainAmount = Number(row.remainAmount || 0)
  const currentValue = Number(allocateAmountMap[row.id!] || 0)
  const otherRowsTotal = Object.entries(allocateAmountMap).reduce((sum, [key, value]) => {
    if (Number(key) === Number(row.id)) {
      return sum
    }
    return sum + Number(value || 0)
  }, 0)
  const availableBalance = Number(prepaymentData.value?.remainPrice || 0) - otherRowsTotal
  return Math.max(Math.min(remainAmount, availableBalance + currentValue), 0)
}

const hasValidItems = computed(() =>
  list.value.some((row) => Number(allocateAmountMap[row.id!] || 0) > 0)
)

const submitDisabled = computed(
  () =>
    allocateLoading.value ||
    loadingList.value ||
    !prepaymentData.value?.id ||
    !hasValidItems.value
)

const resetAllocateDraft = () => {
  Object.keys(allocateAmountMap).forEach((key) => delete allocateAmountMap[Number(key)])
  Object.keys(allocateRemarkMap).forEach((key) => delete allocateRemarkMap[Number(key)])
}

const getList = async () => {
  if (!queryParams.supplierId) {
    list.value = []
    total.value = 0
    return
  }
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await ApStatementApi.getPaymentEnablePage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch (error: any) {
    if (!list.value.length) {
      listErrorMessage.value = error?.message || '请检查网络或稍后重试。'
    }
  } finally {
    loadingList.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.supplierId = prepaymentData.value?.supplierId
  await handleQuery()
}

const emit = defineEmits<{
  (e: 'success'): void
}>()

const submitAllocate = async () => {
  if (submitDisabled.value || !prepaymentData.value?.id) {
    return
  }
  const items = list.value
    .map((row) => ({
      apStatementId: Number(row.id),
      allocateAmount: Number(allocateAmountMap[row.id!] || 0),
      remark: allocateRemarkMap[row.id!]
    }))
    .filter((item) => item.allocateAmount > 0)

  if (!items.length) {
    message.error('请至少填写一条核销金额')
    return
  }

  if (items.some((item) => item.allocateAmount <= 0)) {
    message.error('核销金额必须大于 0')
    return
  }

  const invalidRow = list.value.find((row) => {
    const amount = Number(allocateAmountMap[row.id!] || 0)
    return amount > 0 && amount > getAllocateMax(row)
  })
  if (invalidRow) {
    message.error('本次核销金额不能超过可核销余额')
    return
  }

  if (totalAllocateAmount.value > Number(prepaymentData.value.remainPrice || 0)) {
    message.error('本次核销合计不能超过预付款可用余额')
    return
  }

  allocateLoading.value = true
  try {
    await FinancePrepaymentApi.allocateFinancePrepayment({
      prepaymentId: Number(prepaymentData.value.id),
      items
    })
    message.success('核销成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    allocateLoading.value = false
  }
}

const open = async (prepayment: ErpFinancePrepaymentVO) => {
  dialogVisible.value = true
  prepaymentData.value = prepayment
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  queryParams.supplierId = prepayment.supplierId
  queryParams.bizType = undefined
  queryParams.statementNo = undefined
  queryParams.bizNo = undefined
  queryParams.accountId = undefined
  list.value = []
  total.value = 0
  listErrorMessage.value = ''
  resetAllocateDraft()
  if (!accountList.value.length) {
    accountList.value = await AccountApi.getAccountSimpleList()
  }
  await getList()
}

defineExpose({ open })
</script>

<style scoped>
.finance-prepayment-allocate {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-prepayment-allocate__summary-card :deep(.el-card__body) {
  padding: 16px;
}

.finance-prepayment-allocate__summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.finance-prepayment-allocate__summary-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
}

.finance-prepayment-allocate__summary-item span {
  color: #64748b;
  font-size: 12px;
}

.finance-prepayment-allocate__summary-item strong {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.finance-prepayment-allocate__amount {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.finance-prepayment-allocate__form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-prepayment-allocate__form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-prepayment-allocate__actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-prepayment-allocate__table-wrap {
  overflow-x: auto;
}

.finance-prepayment-allocate__table {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

@media (max-width: 1200px) {
  .finance-prepayment-allocate__summary-grid,
  .finance-prepayment-allocate__form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-prepayment-allocate__summary-grid,
  .finance-prepayment-allocate__form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
