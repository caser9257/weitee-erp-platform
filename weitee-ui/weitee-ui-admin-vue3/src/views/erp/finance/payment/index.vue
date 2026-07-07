<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="76px"
      class="finance-payment-page__query-form"
    >
      <div class="finance-payment-page__query-head">
        <div class="finance-payment-page__query-title">基础筛选</div>
        <el-button link type="primary" @click="toggleAdvancedSearch">
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
        </el-button>
      </div>
      <div class="finance-payment-page__query-grid finance-payment-page__query-grid--basic">
        <el-form-item label="付款单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入付款单号"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="付款时间" prop="paymentTime">
          <el-date-picker
            v-model="queryParams.paymentTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select
            v-model="queryParams.supplierId"
            clearable
            filterable
            placeholder="请选择供应商"
            class="!w-full"
          >
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div
        v-if="advancedSearchVisible"
        class="finance-payment-page__query-grid finance-payment-page__query-grid--advanced"
      >
        <el-form-item label="创建人" prop="creator">
          <el-select
            v-model="queryParams.creator"
            clearable
            filterable
            placeholder="请选择创建人"
            class="!w-full"
          >
            <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.nickname"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="财务人员" prop="financeUserId">
          <el-select
            v-model="queryParams.financeUserId"
            clearable
            filterable
            placeholder="请选择财务人员"
            class="!w-full"
          >
            <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.nickname"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付款账户" prop="accountId">
          <el-select
            v-model="queryParams.accountId"
            clearable
            filterable
            placeholder="请选择付款账户"
            class="!w-full"
          >
            <el-option
              v-for="item in accountList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单号" prop="bizNo">
          <el-input
            v-model="queryParams.bizNo"
            placeholder="请输入业务单号"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="queryParams.remark"
            placeholder="请输入备注"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
      </div>
      <div class="finance-payment-page__query-actions">
        <el-button :loading="loadingList" :disabled="!canQuery" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button :disabled="!canReset" @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="finance-payment-page__toolbar">
      <div class="finance-payment-page__toolbar-actions">
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['erp:finance-payment:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          :disabled="loadingList"
          v-hasPermi="['erp:finance-payment:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
        <el-button
          type="danger"
          plain
          @click="handleDelete(deletableSelectionIds)"
          :disabled="!canBatchDelete"
          :loading="batchDeleteLoading"
          v-hasPermi="['erp:finance-payment:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" />
          批量删除
        </el-button>
      </div>
    </div>

    <div v-if="listErrorMessage && !list.length" class="finance-payment-page__state">
      <el-result icon="error" title="付款单加载失败" :sub-title="listErrorMessage">
        <template #extra>
          <el-button type="primary" @click="getList">重试</el-button>
        </template>
      </el-result>
    </div>
    <template v-else>
      <div v-if="loadingList || list.length" class="finance-payment-page__table-wrap">
        <el-table
          v-loading="loadingList"
          :data="list"
          stripe
          show-overflow-tooltip
          @selection-change="handleSelectionChange"
        >
          <el-table-column width="42" label="选择" type="selection" />
          <el-table-column label="付款信息" min-width="220">
            <template #default="{ row }">
              <div class="finance-payment-page__primary-cell">
                <span class="finance-payment-page__primary-text">{{ row.no || '-' }}</span>
                <span class="finance-payment-page__mono-tag">
                  {{ dateFormatter2(row, undefined, row.paymentTime) || '-' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="供应商与单据" min-width="220">
            <template #default="{ row }">
              <div class="finance-payment-page__primary-cell">
                <span class="finance-payment-page__primary-text">{{ row.supplierName || '-' }}</span>
                <span class="finance-payment-page__muted-text" :title="row.bizNo || '-'">
                  {{ row.bizNo || '未关联业务单号' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="人员与账户" min-width="200">
            <template #default="{ row }">
              <div class="finance-payment-page__primary-cell">
                <span class="finance-payment-page__muted-text">{{ row.financeUserName || '-' }}</span>
                <span class="finance-payment-page__muted-text">{{ row.accountName || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建人" align="center" prop="creatorName" min-width="120" />
          <el-table-column
            label="合计付款"
            align="right"
            prop="totalPrice"
            min-width="120"
            class-name="font-mono"
            :formatter="erpPriceTableColumnFormatter"
          />
          <el-table-column
            label="优惠金额"
            align="right"
            prop="discountPrice"
            min-width="120"
            class-name="font-mono"
            :formatter="erpPriceTableColumnFormatter"
          />
          <el-table-column
            label="实际付款"
            align="right"
            prop="paymentPrice"
            min-width="120"
            class-name="font-mono"
            :formatter="erpPriceTableColumnFormatter"
          />
          <el-table-column label="状态" align="center" fixed="right" width="96" prop="status">
            <template #default="{ row }">
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
              >
                {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="320">
            <template #default="{ row }">
              <div class="finance-payment-page__row-actions">
                <el-button
                  link
                  :disabled="isRowBusy(row.id)"
                  @click="openForm('detail', row.id)"
                  v-hasPermi="['erp:finance-payment:query']"
                >
                  详情
                </el-button>
                <el-button
                  v-if="canEditRow(row)"
                  link
                  type="primary"
                  :disabled="isRowBusy(row.id)"
                  @click="openForm('update', row.id)"
                  v-hasPermi="['erp:finance-payment:update']"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="canSubmitRow(row)"
                  link
                  :type="Number(row.status) === FINANCE_PAYMENT_STATUS.FAILED ? 'warning' : 'primary'"
                  :loading="isSubmittingApproval(row.id)"
                  :disabled="isDeleting(row.id) || isCancelingApproval(row.id)"
                  @click="openSubmitDialog(row)"
                  v-hasPermi="['erp:finance-payment:submit']"
                >
                  {{
                    Number(row.status) === FINANCE_PAYMENT_STATUS.REJECT ||
                    Number(row.status) === FINANCE_PAYMENT_STATUS.FAILED
                      ? '重新提交审批'
                      : '提交审批'
                  }}
                </el-button>
                <el-button
                  v-if="canCancelApprovalRow(row)"
                  link
                  type="warning"
                  :loading="isCancelingApproval(row.id)"
                  :disabled="isDeleting(row.id) || isSubmittingApproval(row.id)"
                  @click="handleCancelApproval(row)"
                  v-hasPermi="['erp:finance-payment:cancel-approval']"
                >
                  撤回审批
                </el-button>
                <el-button
                  v-if="canViewProcessRow(row)"
                  link
                  :disabled="isRowBusy(row.id)"
                  @click="handleProcessDetail(row)"
                  v-hasPermi="['erp:finance-payment:query']"
                >
                  查看审批
                </el-button>
                <el-button
                  v-if="canDeleteRow(row)"
                  link
                  type="danger"
                  :loading="isDeleting(row.id)"
                  :disabled="isSubmittingApproval(row.id) || isCancelingApproval(row.id)"
                  @click="handleDelete([Number(row.id)])"
                  v-hasPermi="['erp:finance-payment:delete']"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-else class="finance-payment-page__state" description="暂无付款单数据" />

      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </template>
  </ContentWrap>

  <FinancePaymentForm ref="formRef" @success="handleFormSuccess" />
  <FinancePaymentSubmitDialog
    ref="submitDialogRef"
    @success="handleSubmitSuccess"
    @close="handleSubmitDialogClose"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import {
  FinancePaymentApi,
  FinancePaymentPageReqVO,
  FinancePaymentVO
} from '@/api/erp/finance/payment'
import FinancePaymentForm from './FinancePaymentForm.vue'
import FinancePaymentSubmitDialog from './FinancePaymentSubmitDialog.vue'
import { type SimpleUserVO, getSimpleUserList } from '@/api/system/user'
import { erpPriceTableColumnFormatter } from '@/utils'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  FINANCE_PAYMENT_STATUS,
  getFinancePaymentRowActionDescriptor
} from './paymentStatus.helpers'

defineOptions({ name: 'ErpFinancePayment' })

const message = useMessage()
const userStore = useUserStoreWithOut()
const { push } = useRouter()
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const loadingList = ref(false)
const exportLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingIds = ref<number[]>([])
const submitApprovalIds = ref<number[]>([])
const cancelApprovalIds = ref<number[]>([])
const advancedSearchVisible = ref(false)
const activeSubmitRowId = ref<number>()

const list = ref<FinancePaymentVO[]>([])
const total = ref(0)
const supplierList = ref<SupplierVO[]>([])
const userList = ref<SimpleUserVO[]>([])
const accountList = ref<AccountVO[]>([])
const selectionList = ref<FinancePaymentVO[]>([])

const queryParams = reactive<FinancePaymentPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  paymentTime: [],
  supplierId: undefined,
  creator: undefined,
  financeUserId: undefined,
  accountId: undefined,
  status: undefined,
  remark: undefined,
  bizNo: undefined
})

const queryFormRef = ref()
const formRef = ref<InstanceType<typeof FinancePaymentForm>>()
const submitDialogRef = ref<InstanceType<typeof FinancePaymentSubmitDialog>>()

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const setIdsLoading = (source: typeof deleteLoadingIds, ids: number[], loadingState: boolean) => {
  if (loadingState) {
    source.value = Array.from(new Set([...source.value, ...ids]))
    return
  }
  source.value = source.value.filter((item) => !ids.includes(item))
}

const getRowDescriptor = (row: FinancePaymentVO) =>
  getFinancePaymentRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value
  })

const isDeleting = (id?: number) => (id != null ? deleteLoadingIds.value.includes(id) : false)
const isSubmittingApproval = (id?: number) =>
  id != null ? submitApprovalIds.value.includes(id) : false
const isCancelingApproval = (id?: number) =>
  id != null ? cancelApprovalIds.value.includes(id) : false
const isRowBusy = (id?: number) =>
  isDeleting(id) || isSubmittingApproval(id) || isCancelingApproval(id)

const deletableSelectionIds = computed(() =>
  selectionList.value
    .filter((item) => item.id != null && getRowDescriptor(item).canDelete)
    .map((item) => Number(item.id))
)
const batchDeleteLoading = computed(() => deleteLoadingIds.value.length > 1)
const canBatchDelete = computed(
  () =>
    deletableSelectionIds.value.length > 0 &&
    !deleteLoadingIds.value.length &&
    !submitApprovalIds.value.length &&
    !cancelApprovalIds.value.length
)
const canQuery = computed(
  () => !loadingList.value && !deleteLoadingIds.value.length && !submitApprovalIds.value.length
)
const canReset = computed(
  () =>
    canQuery.value &&
    (!!queryParams.no ||
      !!queryParams.supplierId ||
      !!queryParams.creator ||
      !!queryParams.financeUserId ||
      !!queryParams.accountId ||
      queryParams.status !== undefined ||
      !!queryParams.remark ||
      !!queryParams.bizNo ||
      !!queryParams.paymentTime?.length ||
      advancedSearchVisible.value)
)

const canEditRow = (row: FinancePaymentVO) => getRowDescriptor(row).canEdit
const canDeleteRow = (row: FinancePaymentVO) => getRowDescriptor(row).canDelete
const canSubmitRow = (row: FinancePaymentVO) => getRowDescriptor(row).canSubmit
const canCancelApprovalRow = (row: FinancePaymentVO) =>
  getRowDescriptor(row).canCancelApproval
const canViewProcessRow = (row: FinancePaymentVO) => getRowDescriptor(row).canViewProcess

const loadQueryOptions = async () => {
  const [suppliers, users, accounts] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    getSimpleUserList(),
    AccountApi.getAccountSimpleList()
  ])
  supplierList.value = suppliers
  userList.value = users
  accountList.value = accounts
}

const getList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinancePaymentApi.getFinancePaymentPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
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
  advancedSearchVisible.value = false
  queryParams.pageNo = 1
  await getList()
}

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const openForm = (type: 'create' | 'update' | 'detail', id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length || deleteLoadingIds.value.length) {
    return
  }
  try {
    await message.delConfirm()
    setIdsLoading(deleteLoadingIds, ids, true)
    await FinancePaymentApi.deleteFinancePayment(ids)
    message.success('删除成功')
    if (list.value.length === ids.length && queryParams.pageNo > 1) {
      queryParams.pageNo -= 1
    }
    selectionList.value = selectionList.value.filter((item) => !ids.includes(Number(item.id)))
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    setIdsLoading(deleteLoadingIds, ids, false)
  }
}

const openSubmitDialog = (row: FinancePaymentVO) => {
  if (!row.id || isSubmittingApproval(row.id)) {
    return
  }
  activeSubmitRowId.value = Number(row.id)
  setIdsLoading(submitApprovalIds, [row.id], true)
  submitDialogRef.value
    ?.open(row)
    .catch(() => {
      activeSubmitRowId.value = undefined
      setIdsLoading(submitApprovalIds, [row.id], false)
    })
}

const handleSubmitSuccess = async () => {
  const rowId = activeSubmitRowId.value
  try {
    await getList()
    if (!rowId) {
      message.warning('提交请求已发送，请刷新后确认状态')
      return
    }
    const latestRow = list.value.find((item) => Number(item.id) === rowId)
    if (latestRow?.status === FINANCE_PAYMENT_STATUS.FAILED) {
      message.warning('提交已受理，但流程创建失败')
      return
    }
    if (
      latestRow?.status === FINANCE_PAYMENT_STATUS.PROCESS &&
      latestRow.processInstanceId
    ) {
      message.success('已提交审批，等待流程受理')
      return
    }
    message.warning('提交请求已发送，请刷新后确认状态')
  } finally {
    if (rowId) {
      setIdsLoading(submitApprovalIds, [rowId], false)
    } else {
      submitApprovalIds.value = []
    }
    activeSubmitRowId.value = undefined
  }
}

const handleSubmitDialogClose = () => {
  const rowId = activeSubmitRowId.value
  if (rowId) {
    setIdsLoading(submitApprovalIds, [rowId], false)
  } else {
    submitApprovalIds.value = []
  }
  activeSubmitRowId.value = undefined
}

const handleCancelApproval = async (row: FinancePaymentVO) => {
  if (!row.id || cancelApprovalIds.value.includes(Number(row.id))) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    setIdsLoading(cancelApprovalIds, [Number(row.id)], true)
    await FinancePaymentApi.cancelFinancePaymentApproval({
      id: Number(row.id),
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    setIdsLoading(cancelApprovalIds, [Number(row.id)], false)
  }
}

const handleProcessDetail = (row: FinancePaymentVO) => {
  if (!row.processInstanceId) {
    message.warning('当前付款单暂无审批流程')
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId
    }
  })
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await FinancePaymentApi.exportFinancePayment(queryParams)
    download.excel(data, '付款单.xls')
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: FinancePaymentVO[]) => {
  selectionList.value = rows
}

const handleFormSuccess = async () => {
  await getList()
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadQueryOptions()])
})
</script>

<style scoped>
.finance-payment-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-payment-page__query-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.finance-payment-page__query-title {
  color: var(--erp-slate-700);
  font-size: 14px;
  font-weight: 600;
}

.finance-payment-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-payment-page__query-grid--advanced {
  padding-top: 4px;
  border-top: 1px solid var(--erp-slate-100);
}

.finance-payment-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-payment-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-payment-page__toolbar-actions,
.finance-payment-page__row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-payment-page__table-wrap {
  overflow-x: auto;
}

.finance-payment-page__state {
  padding: 8px 0 4px;
}

.finance-payment-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-payment-page__primary-text {
  color: var(--erp-slate-900);
  font-weight: 600;
  line-height: 1.4;
}

.finance-payment-page__muted-text {
  color: var(--erp-slate-500);
  font-size: 12px;
  line-height: 1.5;
}

.finance-payment-page__mono-tag {
  width: fit-content;
  padding: 2px 8px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 999px;
  background: var(--erp-slate-50);
  color: var(--erp-slate-700);
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, 'SFMono-Regular', Consolas, 'Liberation Mono',
    Menlo, monospace;
}

@media (max-width: 1440px) {
  .finance-payment-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-payment-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-payment-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-payment-page__query-actions,
  .finance-payment-page__toolbar {
    justify-content: stretch;
  }
}
</style>
