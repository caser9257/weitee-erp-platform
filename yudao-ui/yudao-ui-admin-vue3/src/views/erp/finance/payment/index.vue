<template>
<ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="76px"
      class="finance-payment-page__query-form"
    >
      <div class="finance-payment-page__query-grid">
        <el-form-item label="付款单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入付款单�?
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
            start-placeholder="开始日�?
            end-placeholder="结束日期"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="供应�? prop="supplierId">
          <el-select
            v-model="queryParams.supplierId"
            clearable
            filterable
            placeholder="请选择供应�?
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
        <el-form-item label="创建�? prop="creator">
          <el-select
            v-model="queryParams.creator"
            clearable
            filterable
            placeholder="请选择创建�?
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
        <el-form-item label="状�? prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状�? clearable class="!w-full">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单号" prop="bizNo">
          <el-input
            v-model="queryParams.bizNo"
            placeholder="请输入业务单�?
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="queryParams.remark"
            placeholder="请输入备�?
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
      </div>
      <div class="finance-payment-page__query-actions">
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
          @click="handleDelete(selectionList.map((item) => Number(item.id)))"
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
      <el-result icon="error" title="付款单加载失�? :sub-title="listErrorMessage">
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
                  {{ dateFormatter2(row, undefined, row.paymentTime, undefined) || '-' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="供应商与单据" min-width="220">
            <template #default="{ row }">
              <div class="finance-payment-page__primary-cell">
                <span class="finance-payment-page__primary-text">{{ row.supplierName || '-' }}</span>
                <span class="finance-payment-page__muted-text" :title="row.bizNo || '-'">
                  {{ row.bizNo || '未关联业务单�? }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="人员与账�? min-width="200">
            <template #default="{ row }">
              <div class="finance-payment-page__primary-cell">
                <span class="finance-payment-page__muted-text">{{ row.financeUserName || '-' }}</span>
                <span class="finance-payment-page__muted-text">{{ row.accountName || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建�? align="center" prop="creatorName" min-width="120" />
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
          <el-table-column label="状�? align="center" fixed="right" width="90" prop="status">
            <template #default="{ row }">
              <el-tag
                size="small"
                effect="light"
                :type="getRowStatusDescriptor(row).tagType"
              >
                {{ getRowStatusDescriptor(row).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="作废原因" align="center" fixed="right" width="120" prop="voidReason">
            <template #default="{ row }">
              <el-tooltip v-if="row.voidReason" :content="row.voidReason" placement="top">
                <span class="inline-block max-w-100px truncate">{{ row.voidReason }}</span>
              </el-tooltip>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="280">
            <template #default="{ row }">
              <el-button
                link
                :disabled="isRowBusy(row.id)"
                @click="openForm('detail', row.id)"
                v-hasPermi="['erp:finance-payment:query']"
              >
                详情
              </el-button>
              <el-button
                link
                type="primary"
                :disabled="!canEditRow(row) || isRowBusy(row.id)"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:finance-payment:update']"
              >
                编辑
              </el-button>
              <el-button
                v-if="canSubmitRow(row)"
                link
                type="primary"
                :disabled="isRowBusy(row.id)"
                @click="openSubmitDialog(row)"
                v-hasPermi="['erp:finance-payment:submit']"
              >
                提交审批
              </el-button>
              <el-button
                v-if="canCancelApprovalRow(row)"
                link
                type="warning"
                :loading="isCancelingApproval(row.id)"
                :disabled="isDeleting(row.id)"
                @click="handleCancelApproval(row)"
                v-hasPermi="['erp:finance-payment:cancel-approval']"
              >
                撤回审批
              </el-button>
              <el-button
                v-if="canDeleteRow(row)"
                link
                type="danger"
                :loading="isDeleting(row.id)"
                :disabled="isCancelingApproval(row.id)"
                @click="handleDelete([Number(row.id)])"
                v-hasPermi="['erp:finance-payment:delete']"
              >
                删除
              </el-button>
              <el-button
                v-if="canVoidRow(row)"
                link
                type="warning"
                :loading="isVoiding(row.id)"
                :disabled="isDeleting(row.id) || isCancelingApproval(row.id)"
                @click="handleVoid(row)"
                v-hasPermi="['erp:finance-payment:void']"
              >
                作废
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-else class="finance-payment-page__state" description="暂无付款单数�? />

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
  <FinancePaymentSubmitDialog ref="submitDialogRef" @success="handleSubmitSuccess" />
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
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
  getFinancePaymentRowActionDescriptor,
  getFinancePaymentStatusDescriptor
} from './financePaymentStatus.helpers'

defineOptions({ name: 'ErpFinancePayment' })

const message = useMessage()
const userStore = useUserStoreWithOut()
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const loadingList = ref(false)
const exportLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingIds = ref<number[]>([])
const cancelApprovalLoadingIds = ref<number[]>([])
const voidLoadingIds = ref<number[]>([])

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
const isDeleting = (id?: number) => (id != null ? deleteLoadingIds.value.includes(id) : false)
const isCancelingApproval = (id?: number) =>
  id != null ? cancelApprovalLoadingIds.value.includes(id) : false
const isVoiding = (id?: number) =>
  id != null ? voidLoadingIds.value.includes(id) : false
const isRowBusy = (id?: number) => isDeleting(id) || isCancelingApproval(id) || isVoiding(id)
const batchDeleteLoading = computed(() => deleteLoadingIds.value.length > 0)
const canBatchDelete = computed(
  () =>
    selectionList.value.length > 0 &&
    !deleteLoadingIds.value.length &&
    !cancelApprovalLoadingIds.value.length
)

const getRowActionDescriptor = (row: FinancePaymentVO) =>
  getFinancePaymentRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value
  })

const canEditRow = (row: FinancePaymentVO) => getRowActionDescriptor(row).canEdit
const canSubmitRow = (row: FinancePaymentVO) => getRowActionDescriptor(row).canSubmit
const canCancelApprovalRow = (row: FinancePaymentVO) => getRowActionDescriptor(row).canCancelApproval
const canDeleteRow = (row: FinancePaymentVO) => getRowActionDescriptor(row).canDelete
const canVoidRow = (row: FinancePaymentVO) => getRowActionDescriptor(row).canVoid
const getRowStatusDescriptor = (row: FinancePaymentVO) =>
  getFinancePaymentStatusDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId
  })

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
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '请检查网络或稍后重试�?
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
  queryParams.pageNo = 1
  await getList()
}

const openForm = (type: 'create' | 'update' | 'detail', id?: number) => {
  formRef.value?.open(type, id)
}

const openSubmitDialog = (row?: FinancePaymentVO) => {
  if (!row?.id) {
    return
  }
  submitDialogRef.value?.open(row)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length || deleteLoadingIds.value.length || cancelApprovalLoadingIds.value.length) {
    return
  }
  try {
    await message.delConfirm()
    deleteLoadingIds.value = [...ids]
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
    deleteLoadingIds.value = []
  }
}

const handleCancelApproval = async (row?: FinancePaymentVO) => {
  if (!row?.id || isDeleting(row.id) || isCancelingApproval(row.id)) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原�?, '撤回审批', {
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空',
      closeOnClickModal: false
    })
    cancelApprovalLoadingIds.value = [row.id]
    await FinancePaymentApi.cancelFinancePaymentApproval({
      id: row.id,
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    cancelApprovalLoadingIds.value = []
  }
}

const handleVoid = async (row?: FinancePaymentVO) => {
  if (!row?.id || isDeleting(row.id) || isCancelingApproval(row.id)) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原�?, '作废付款�?, {
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '作废原因不能为空',
      closeOnClickModal: false
    })
    voidLoadingIds.value = [row.id]
    await FinancePaymentApi.voidFinancePayment({
      id: row.id,
      reason: value
    })
    message.success('作废成功')
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    voidLoadingIds.value = []
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await FinancePaymentApi.exportFinancePayment(queryParams)
    download.excel(data, '付款�?xls')
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

const handleSubmitSuccess = async () => {
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

.finance-payment-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
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

.finance-payment-page__toolbar-actions {
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
  color: #0f172a;
  font-weight: 600;
  line-height: 1.4;
}

.finance-payment-page__muted-text {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.finance-payment-page__mono-tag {
  width: fit-content;
  padding: 2px 8px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #334155;
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
