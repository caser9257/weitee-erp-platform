<template>
<ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="76px"
      class="finance-receipt-page__query-form"
    >
      <div class="finance-receipt-page__query-grid">
        <el-form-item label="收款单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入收款单�?
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="收款时间" prop="receiptTime">
          <el-date-picker
            v-model="queryParams.receiptTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日�?
            end-placeholder="结束日期"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="queryParams.customerId"
            clearable
            filterable
            placeholder="请选择客户"
            class="!w-full"
          >
            <el-option
              v-for="item in customerList"
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
        <el-form-item label="收款账户" prop="accountId">
          <el-select
            v-model="queryParams.accountId"
            clearable
            filterable
            placeholder="请选择收款账户"
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
      <div class="finance-receipt-page__query-actions">
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
    <div class="finance-receipt-page__toolbar">
      <div class="finance-receipt-page__toolbar-actions">
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['erp:finance-receipt:create']"
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
          v-hasPermi="['erp:finance-receipt:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
        <el-button
          type="danger"
          plain
          @click="handleDelete(selectionList.map((item) => item.id!))"
          :disabled="!canBatchDelete"
          :loading="deleteLoading"
          v-hasPermi="['erp:finance-receipt:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" />
          批量删除
        </el-button>
      </div>
    </div>

    <div v-if="listErrorMessage && !list.length" class="finance-receipt-page__state">
      <el-result icon="error" title="收款单加载失�? :sub-title="listErrorMessage">
        <template #extra>
          <el-button type="primary" @click="getList">重试</el-button>
        </template>
      </el-result>
    </div>
    <template v-else>
      <div v-if="loadingList || list.length" class="finance-receipt-page__table-wrap">
        <el-table
          v-loading="loadingList"
          :data="list"
          stripe
          show-overflow-tooltip
          @selection-change="handleSelectionChange"
        >
          <el-table-column width="42" label="选择" type="selection" />
          <el-table-column label="收款信息" min-width="220">
            <template #default="{ row }">
              <div class="finance-receipt-page__primary-cell">
                <span class="finance-receipt-page__primary-text">{{ row.no || '-' }}</span>
                <span class="finance-receipt-page__mono-tag">
                  {{ dateFormatter2(row, undefined, row.receiptTime, undefined) || '-' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="客户与单�? min-width="220">
            <template #default="{ row }">
              <div class="finance-receipt-page__primary-cell">
                <span class="finance-receipt-page__primary-text">{{ row.customerName || '-' }}</span>
                <span class="finance-receipt-page__muted-text" :title="row.bizNo || '-'">
                  {{ row.bizNo || '未关联业务单�? }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="人员与账�? min-width="200">
            <template #default="{ row }">
              <div class="finance-receipt-page__primary-cell">
                <span class="finance-receipt-page__muted-text">{{ row.financeUserName || '-' }}</span>
                <span class="finance-receipt-page__muted-text">{{ row.accountName || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建�? align="center" prop="creatorName" min-width="120" />
          <el-table-column
            label="合计收款"
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
            label="实际收款"
            align="right"
            prop="receiptPrice"
            min-width="120"
            class-name="font-mono"
            :formatter="erpPriceTableColumnFormatter"
          />
          <el-table-column label="状�? align="center" fixed="right" width="90" prop="status">
            <template #default="{ row }">
              <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="240">
            <template #default="{ row }">
              <el-button
                link
                :disabled="isRowBusy(row.id)"
                @click="openForm('detail', row.id)"
                v-hasPermi="['erp:finance-receipt:query']"
              >
                详情
              </el-button>
              <el-button
                link
                type="primary"
                :disabled="row.status === 20 || isRowBusy(row.id)"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:finance-receipt:update']"
              >
                编辑
              </el-button>
              <el-button
                v-if="row.status === 10"
                link
                type="primary"
                :loading="statusLoadingId === row.id"
                :disabled="deleteLoading"
                @click="handleUpdateStatus(row.id, 20)"
                v-hasPermi="['erp:finance-receipt:update-status']"
              >
                审核
              </el-button>
              <el-button
                v-else
                link
                type="warning"
                :loading="statusLoadingId === row.id"
                :disabled="deleteLoading"
                @click="handleUpdateStatus(row.id, 10)"
                v-hasPermi="['erp:finance-receipt:update-status']"
              >
                反审�?
              </el-button>
              <el-button
                link
                type="danger"
                :loading="deleteLoading && deleteTargetIds.includes(Number(row.id))"
                :disabled="isRowBusy(row.id)"
                @click="handleDelete([row.id!])"
                v-hasPermi="['erp:finance-receipt:delete']"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty
        v-else
        class="finance-receipt-page__state"
        description="暂无收款单数�?
      />

      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </template>
  </ContentWrap>

  <FinanceReceiptForm ref="formRef" @success="handleFormSuccess" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  FinanceReceiptApi,
  FinanceReceiptPageReqVO,
  FinanceReceiptVO
} from '@/api/erp/finance/receipt'
import FinanceReceiptForm from './FinanceReceiptForm.vue'
import { type SimpleUserVO, getSimpleUserList } from '@/api/system/user'
import { erpPriceTableColumnFormatter } from '@/utils'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'

defineOptions({ name: 'ErpFinanceReceipt' })

const message = useMessage()

const loadingList = ref(false)
const exportLoading = ref(false)
const deleteLoading = ref(false)
const statusLoadingId = ref<number>()
const listErrorMessage = ref('')

const list = ref<FinanceReceiptVO[]>([])
const total = ref(0)
const customerList = ref<CustomerVO[]>([])
const userList = ref<SimpleUserVO[]>([])
const accountList = ref<AccountVO[]>([])
const selectionList = ref<FinanceReceiptVO[]>([])
const deleteTargetIds = ref<number[]>([])

const queryParams = reactive<FinanceReceiptPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  receiptTime: [],
  customerId: undefined,
  creator: undefined,
  financeUserId: undefined,
  accountId: undefined,
  status: undefined,
  remark: undefined,
  bizNo: undefined
})

const canBatchDelete = computed(
  () => selectionList.value.length > 0 && !deleteLoading.value && !statusLoadingId.value
)

const queryFormRef = ref()
const formRef = ref<InstanceType<typeof FinanceReceiptForm>>()

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) =>
  (id != null && statusLoadingId.value === id) ||
  (deleteLoading.value && id != null && deleteTargetIds.value.includes(Number(id)))

const loadQueryOptions = async () => {
  const [customers, users, accounts] = await Promise.all([
    CustomerApi.getCustomerSimpleList(),
    getSimpleUserList(),
    AccountApi.getAccountSimpleList()
  ])
  customerList.value = customers
  userList.value = users
  accountList.value = accounts
}

const getList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceReceiptApi.getFinanceReceiptPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectionList.value = selectionList.value.filter((item) =>
      data.list.some((current) => current.id === item.id)
    )
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

const handleDelete = async (ids: number[]) => {
  if (!ids.length || deleteLoading.value) {
    return
  }
  try {
    await message.delConfirm()
    deleteLoading.value = true
    deleteTargetIds.value = ids
    await FinanceReceiptApi.deleteFinanceReceipt(ids)
    message.success('删除成功')
    if (list.value.length === ids.length && queryParams.pageNo > 1) {
      queryParams.pageNo -= 1
    }
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(Number(item.id)))
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    deleteLoading.value = false
    deleteTargetIds.value = []
  }
}

const handleUpdateStatus = async (id?: number, status?: number) => {
  if (!id || !status || statusLoadingId.value) {
    return
  }
  const isApprove = status === 20
  try {
    await message.confirm(`确定${isApprove ? '审核' : '反审�?}该收款单吗？`)
    statusLoadingId.value = id
    if (isApprove) {
      await FinanceReceiptApi.approveFinanceReceipt(id)
    } else {
      await FinanceReceiptApi.unapproveFinanceReceipt(id)
    }
    message.success(`${isApprove ? '审核' : '反审�?}成功`)
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
    const data = await FinanceReceiptApi.exportFinanceReceipt(queryParams)
    download.excel(data, '收款�?xls')
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: FinanceReceiptVO[]) => {
  selectionList.value = rows
}

const handleFormSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadQueryOptions()])
})
</script>

<style scoped>
.finance-receipt-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-receipt-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-receipt-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-receipt-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-receipt-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-receipt-page__table-wrap {
  overflow-x: auto;
}

.finance-receipt-page__state {
  padding: 24px 0 8px;
}

.finance-receipt-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-receipt-page__primary-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.finance-receipt-page__mono-tag {
  width: fit-content;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 2px 8px;
  background: #f8fafc;
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
}

.finance-receipt-page__muted-text {
  color: var(--el-text-color-secondary);
}

@media (max-width: 1440px) {
  .finance-receipt-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-receipt-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-receipt-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-receipt-page__query-actions,
  .finance-receipt-page__toolbar {
    justify-content: stretch;
  }
}
</style>
