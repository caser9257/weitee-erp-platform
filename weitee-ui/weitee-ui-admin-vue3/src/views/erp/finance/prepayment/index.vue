<template>
  <div class="finance-shell finance-prepayment-page finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">棰勪粯娆?</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip finance-shell__metric-chip--primary">
              褰撳墠鍒楄〃 {{ total }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--success">
              宸插鏍?{{ approvedCount }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--warning">
              寰呭鐞?{{ pendingCount }}
            </span>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">鍩虹绛涢�?</div>
        <el-button link type="primary" @click="advancedExpanded = !advancedExpanded">
          <Icon :icon="advancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
          {{ advancedExpanded ? '鏀惰捣楂樼骇鎼滅储' : '灞曞紑楂樼骇鎼滅储' }}
        </el-button>
      </div>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="76px"
        class="finance-shell__query-form"
        @submit.prevent
      >
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="棰勪粯娆惧崟鍙?" prop="no">
            <el-input
              v-model="queryParams.no"
              placeholder="璇疯緭鍏ラ浠樻鍗曞彿"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="棰勪粯鏃堕棿" prop="prepaymentTime">
            <el-date-picker
              v-model="queryParams.prepaymentTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="寮€濮嬫棩鏈?"
              end-placeholder="缁撴潫鏃ユ湡"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="渚涘簲鍟?" prop="supplierId">
            <el-select
              v-model="queryParams.supplierId"
              clearable
              filterable
              placeholder="璇烽€夋嫨渚涘簲鍟?"
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
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <div v-if="advancedExpanded" class="finance-shell__query-advanced">
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
                :value="String(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="璐㈠姟浜哄憳" prop="financeUserId">
            <el-select
              v-model="queryParams.financeUserId"
              clearable
              filterable
              placeholder="璇烽€夋嫨璐㈠姟浜哄憳"
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
          <el-form-item label="缁撶畻璐︽埛" prop="accountId">
            <el-select
              v-model="queryParams.accountId"
              clearable
              filterable
              placeholder="璇烽€夋嫨缁撶畻璐︽埛"
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
          <el-form-item label="澶囨敞" prop="remark">
            <el-input
              v-model="queryParams.remark"
              placeholder="璇疯緭鍏ュ娉?"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            鎼滅储
          </el-button>
          <el-button :disabled="loadingList" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            閲嶇疆
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">棰勪粯娆惧垪琛?</div>
          <div class="finance-shell__toolbar-count">
            褰撳墠鍏?<strong>{{ total }}</strong> 鏉?          </div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button
            type="primary"
            plain
            @click="openForm('create')"
            v-hasPermi="['erp:finance-prepayment:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            鏂板
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="!canBatchDelete"
            :loading="batchDeleteLoading"
            @click="handleDelete(selectionList.map((item) => Number(item.id)))"
            v-hasPermi="['erp:finance-prepayment:delete']"
          >
            <Icon icon="ep:delete" class="mr-5px" />
            鎵归噺鍒犻櫎
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !list.length" class="finance-prepayment-page__state">
        <el-result icon="error" title="预付款加载失败" :sub-title="listErrorMessage">
          <template #extra>
            <el-button type="primary" @click="getList">閲嶈瘯</el-button>
          </template>
        </el-result>
      </div>

      <template v-else>
        <div v-if="loadingList || list.length" class="finance-shell__table-wrap">
          <el-table
            v-loading="loadingList"
            :data="list"
            stripe
            class="finance-shell__table finance-shell__table--dense"
            :show-overflow-tooltip="false"
            @selection-change="handleSelectionChange"
          >
            <el-table-column
              width="42"
              type="selection"
              label="閫夋嫨"
              :selectable="canSelectForBatchDelete"
            />
            <el-table-column label="预付款信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.no || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">
                    {{ formatDateTimeValue(row.prepaymentTime) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="渚涘簲鍟嗕笌璐︽埛" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.supplierName || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.accountName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="浜哄憳淇℃伅" min-width="180">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__muted-text">{{ row.financeUserName || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.creatorName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="棰勪粯閲戦" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.prepaymentPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="宸叉牳閿€閲戦" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.allocatedPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="鍓╀綑閲戦" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.remainPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="100" align="center">
              <template #default="{ row }">
                <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="澶囨敞" min-width="180">
              <template #default="{ row }">
                <span class="finance-shell__muted-text" :title="row.remark || '-'">
                  {{ row.remark || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" align="center" width="320">
              <template #default="{ row }">
                <div class="finance-prepayment-page__row-actions">
                  <el-button
                    link
                    :disabled="isRowBusy(row.id)"
                    @click="openForm('detail', row.id)"
                    v-hasPermi="['erp:finance-prepayment:query']"
                  >
                    璇︽儏
                  </el-button>
                  <el-button
                    link
                    type="primary"
                    :disabled="!canEdit(row)"
                    @click="openForm('update', row.id)"
                    v-hasPermi="['erp:finance-prepayment:update']"
                  >
                    缂栬緫
                  </el-button>
                  <el-button
                    v-if="canApprove(row)"
                    link
                    type="primary"
                    :loading="statusLoadingId === row.id"
                    :disabled="batchDeleteLoading"
                    @click="handleUpdateStatus(row.id, 20)"
                    v-hasPermi="['erp:finance-prepayment:update-status']"
                  >
                    瀹℃牳
                  </el-button>
                  <el-button
                    v-if="canUnapprove(row)"
                    link
                    type="warning"
                    :loading="statusLoadingId === row.id"
                    :disabled="batchDeleteLoading"
                    @click="handleUpdateStatus(row.id, 10)"
                    v-hasPermi="['erp:finance-prepayment:update-status']"
                  >
                    鍙嶅鏍?                  </el-button>
                  <el-button
                    link
                    type="success"
                    :disabled="!canAllocate(row)"
                    @click="openAllocateDialog(row)"
                    v-hasPermi="['erp:finance-prepayment:update']"
                  >
                    鏍搁攢
                  </el-button>
                  <el-button
                    link
                    :loading="traceLoadingId === row.id"
                    :disabled="isRowBusy(row.id)"
                    @click="openTraceDrawer(row)"
                    v-hasPermi="['erp:finance-prepayment:query']"
                  >
                    杩芥函
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    :loading="rowDeleteLoadingId === row.id"
                    :disabled="!canDelete(row)"
                    @click="handleDelete([Number(row.id)])"
                    v-hasPermi="['erp:finance-prepayment:delete']"
                  >
                    鍒犻櫎
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else class="finance-prepayment-page__state" description="暂无预付款数据" />
        <Pagination
          v-if="total > 0"
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <PrepaymentForm ref="formRef" @success="handleRefreshCurrentContext" />
    <PrepaymentAllocateDialog ref="allocateDialogRef" @success="handleRefreshCurrentContext" />

    <el-drawer
      v-model="traceDrawerVisible"
      size="520px"
      destroy-on-close
      :with-header="false"
      :modal-class="'finance-shell__drawer-mask'"
      @closed="clearTraceDrawer"
    >
      <div class="finance-prepayment-page__drawer">
        <div v-if="traceData.prepayment" class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ traceData.prepayment.no || '预付款追踪' }}</div>
            <div class="finance-shell__context-subtitle">
              {{ traceData.prepayment.supplierName || '-' }} / {{ traceData.prepayment.accountName || '-' }}
            </div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>状态</span>
              <span>{{ resolveErpAuditStatusLabel(traceData.prepayment.status) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>剩余金额</span>
              <span>{{ formatAmount(traceData.prepayment.remainPrice) }}</span>
            </div>
          </div>
        </div>

        <div class="finance-prepayment-page__drawer-body" v-loading="loadingTrace">
          <el-result
            v-if="traceErrorMessage"
            icon="error"
            title="棰勪粯娆捐拷婧姞杞藉け璐?"
            :sub-title="traceErrorMessage"
          >
            <template #extra>
              <el-button type="primary" @click="retryLoadTrace">閲嶈瘯</el-button>
            </template>
          </el-result>

          <template v-else-if="traceData.prepayment">
            <div class="finance-shell__section">
              <div class="finance-shell__section-head">
                <div class="finance-shell__section-title">鍏宠仈搴斾粯鍙拌处</div>
              </div>
              <div class="finance-prepayment-page__trace-cards">
                <div
                  v-for="statement in traceData.statements || []"
                  :key="statement.id"
                  class="finance-prepayment-page__trace-card"
                >
                  <div class="finance-prepayment-page__trace-card-title">{{ statement.statementNo || '-' }}</div>
                  <div class="finance-prepayment-page__trace-card-sub">{{ statement.bizNo || '-' }}</div>
                  <div class="finance-prepayment-page__trace-card-meta">
                    <span>搴斾粯 {{ formatAmount(statement.amount) }}</span>
                    <span>鍓╀綑 {{ formatAmount(statement.remainAmount) }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-if="!(traceData.statements || []).length" description="鏆傛棤鍏宠仈搴斾粯鍙拌处" />
            </div>

            <div class="finance-shell__section">
              <div class="finance-shell__section-head">
                <div class="finance-shell__section-title">鏍搁攢璁板綍</div>
                <el-button
                  type="danger"
                  plain
                  size="small"
                  :disabled="!selectedRollbackIds.length || rollbackLoading"
                  :loading="rollbackLoading"
                  @click="handleRollbackAllocate"
                  v-hasPermi="['erp:finance-prepayment:update']"
                >
                  鍥炴粴鏍搁攢
                </el-button>
              </div>
              <div class="finance-shell__table-wrap">
                <el-table
                  :data="traceData.allocates || []"
                  stripe
                  class="finance-shell__table finance-shell__table--dense"
                  :show-overflow-tooltip="false"
                  @selection-change="handleRollbackSelectionChange"
                >
                  <el-table-column type="selection" width="42" :selectable="canSelectRollbackRow" />
                  <el-table-column prop="bizNo" label="涓氬姟鍗曞彿" min-width="150" />
                  <el-table-column prop="statusName" label="鐘舵€?" min-width="110" align="center" />
                  <el-table-column label="鏍搁攢閲戦" min-width="120" align="right">
                    <template #default="{ row }">
                      <span class="finance-shell__amount">{{ formatAmount(row.allocateAmount) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="remark" label="澶囨敞" min-width="160" />
                </el-table>
              </div>
              <el-empty v-if="!(traceData.allocates || []).length" description="鏆傛棤鏍搁攢璁板綍" />
            </div>
          </template>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import {
  FinancePrepaymentApi,
  type ErpFinancePrepaymentPageReqVO,
  type ErpFinancePrepaymentTraceVO,
  type ErpFinancePrepaymentVO
} from '@/api/erp/finance/prepayment'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'
import { resolveErpAuditStatusLabel } from '@/utils/erpAuditStatus'
import PrepaymentForm from './PrepaymentForm.vue'
import PrepaymentAllocateDialog from './PrepaymentAllocateDialog.vue'
import '@/views/erp/finance/shared/readOnlyPage.css'

defineOptions({ name: 'ErpFinancePrepayment' })

const message = useMessage()

const loadingList = ref(false)
const batchDeleteLoading = ref(false)
const rowDeleteLoadingId = ref<number>()
const statusLoadingId = ref<number>()
const traceLoadingId = ref<number>()
const loadingTrace = ref(false)
const rollbackLoading = ref(false)
const advancedExpanded = ref(false)
const traceDrawerVisible = ref(false)
const currentTraceId = ref<number>()
const listErrorMessage = ref('')
const traceErrorMessage = ref('')

const list = ref<ErpFinancePrepaymentVO[]>([])
const total = ref(0)
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<SimpleUserVO[]>([])
const selectionList = ref<ErpFinancePrepaymentVO[]>([])
const selectedRollbackIds = ref<number[]>([])
const traceData = reactive<ErpFinancePrepaymentTraceVO>({
  prepayment: undefined,
  statements: [],
  allocates: []
})

const queryFormRef = ref<FormInstance>()
const formRef = ref<InstanceType<typeof PrepaymentForm>>()
const allocateDialogRef = ref<InstanceType<typeof PrepaymentAllocateDialog>>()

const queryParams = reactive<ErpFinancePrepaymentPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  prepaymentTime: [],
  supplierId: undefined,
  creator: undefined,
  financeUserId: undefined,
  accountId: undefined,
  status: undefined,
  remark: undefined
})

const approvedCount = computed(() => list.value.filter((item) => item.status === 20).length)
const pendingCount = computed(() => list.value.filter((item) => item.status !== 20).length)
const canBatchDelete = computed(
  () =>
    selectionList.value.length > 0 &&
    selectionList.value.every((item) => Number(item.status) !== 20) &&
    !batchDeleteLoading.value
)

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'

const canEdit = (row: ErpFinancePrepaymentVO) => Number(row.status) !== 20
const canApprove = (row: ErpFinancePrepaymentVO) => [10, 30].includes(Number(row.status))
const canUnapprove = (row: ErpFinancePrepaymentVO) => Number(row.status) === 20
const canAllocate = (row: ErpFinancePrepaymentVO) =>
  Number(row.status) === 20 && Number(row.remainPrice || 0) > 0
const canDelete = (row: ErpFinancePrepaymentVO) =>
  Number(row.status) !== 20 &&
  rowDeleteLoadingId.value !== Number(row.id) &&
  !batchDeleteLoading.value &&
  statusLoadingId.value !== Number(row.id)

const canSelectForBatchDelete = (row: ErpFinancePrepaymentVO) =>
  Number(row.status) !== 20 && !isRowBusy(row.id)

const canSelectRollbackRow = (row: { status?: number }) => Number(row.status) === 20

const isRowBusy = (id?: number) =>
  Number(statusLoadingId.value) === Number(id) ||
  Number(rowDeleteLoadingId.value) === Number(id) ||
  Number(traceLoadingId.value) === Number(id)

const loadOptions = async () => {
  const [suppliers, accounts, users] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    AccountApi.getAccountSimpleList(),
    getSimpleUserList()
  ])
  supplierList.value = suppliers
  accountList.value = accounts
  userList.value = users
}

const getList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinancePrepaymentApi.getFinancePrepaymentPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectionList.value = selectionList.value.filter((item) =>
      data.list.some((current) => current.id === item.id)
    )
  } catch (error: any) {
    if (!list.value.length) {
      listErrorMessage.value = error?.message || '璇锋鏌ョ綉缁滄垨绋嶅悗閲嶈瘯銆?
    }
  } finally {
    loadingList.value = false
  }
}

const handleSelectionChange = (rows: ErpFinancePrepaymentVO[]) => {
  selectionList.value = rows
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const openForm = async (type: 'create' | 'update' | 'detail', id?: number) => {
  await formRef.value?.open(type, id)
}

const handleFormSuccess = async () => {
  await getList()
  if (traceDrawerVisible.value && currentTraceId.value) {
    await loadTrace(currentTraceId.value)
  }
}

const handleRefreshCurrentContext = async () => {
  await handleFormSuccess()
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length || batchDeleteLoading.value || rowDeleteLoadingId.value) {
    return
  }
  try {
    await message.confirm(`确认删除选中的 ${ids.length} 条预付款吗？`)
  } catch (error) {
    if (isActionCanceled(error)) {
      return
    }
    throw error
  }

  const singleId = ids.length === 1 ? Number(ids[0]) : undefined
  if (singleId) {
    rowDeleteLoadingId.value = singleId
  } else {
    batchDeleteLoading.value = true
  }
  try {
    await FinancePrepaymentApi.deleteFinancePrepayment(ids)
    message.success('删除成功')
    await getList()
  } finally {
    rowDeleteLoadingId.value = undefined
    batchDeleteLoading.value = false
  }
}

const handleUpdateStatus = async (id?: number, status?: number) => {
  if (!id || !status || statusLoadingId.value) {
    return
  }
  try {
    await message.confirm(`确认${status === 20 ? '审核' : '反审核'}该预付款吗？`)
  } catch (error) {
    if (isActionCanceled(error)) {
      return
    }
    throw error
  }
  statusLoadingId.value = Number(id)
  try {
    await FinancePrepaymentApi.updateFinancePrepaymentStatus(Number(id), Number(status))
    message.success(`${status === 20 ? '审核' : '反审核'}成功`)
    await getList()
    if (traceDrawerVisible.value && currentTraceId.value === Number(id)) {
      await loadTrace(Number(id))
    }
  } finally {
    statusLoadingId.value = undefined
  }
}

const clearTraceData = () => {
  traceData.prepayment = undefined
  traceData.statements = []
  traceData.allocates = []
  traceErrorMessage.value = ''
  selectedRollbackIds.value = []
}

const loadTrace = async (id: number) => {
  loadingTrace.value = true
  traceErrorMessage.value = ''
  try {
    const data = await FinancePrepaymentApi.getFinancePrepaymentTrace(id)
    traceData.prepayment = data?.prepayment
    traceData.statements = data?.statements || []
    traceData.allocates = data?.allocates || []
  } catch (error: any) {
    clearTraceData()
    traceErrorMessage.value = error?.message || '璇锋鏌ョ綉缁滄垨绋嶅悗閲嶈瘯銆?
  } finally {
    loadingTrace.value = false
  }
}

const openTraceDrawer = async (row: ErpFinancePrepaymentVO) => {
  const id = Number(row.id)
  if (!id || traceLoadingId.value) {
    return
  }
  traceDrawerVisible.value = true
  currentTraceId.value = id
  traceLoadingId.value = id
  clearTraceData()
  try {
    await loadTrace(id)
  } finally {
    traceLoadingId.value = undefined
  }
}

const retryLoadTrace = async () => {
  if (!currentTraceId.value) {
    return
  }
  await loadTrace(currentTraceId.value)
}

const clearTraceDrawer = () => {
  currentTraceId.value = undefined
  clearTraceData()
}

const openAllocateDialog = async (row: ErpFinancePrepaymentVO) => {
  await allocateDialogRef.value?.open(row)
}

const handleRollbackSelectionChange = (rows: Array<{ id?: number; status?: number }>) => {
  selectedRollbackIds.value = rows
    .filter((item) => Number(item.status) === 20)
    .map((item) => Number(item.id))
}

const handleRollbackAllocate = async () => {
  if (!selectedRollbackIds.value.length || rollbackLoading.value) {
    return
  }
  try {
    await message.confirm('确认回滚选中的核销记录吗？')
  } catch (error) {
    if (isActionCanceled(error)) {
      return
    }
    throw error
  }
  rollbackLoading.value = true
  try {
    await FinancePrepaymentApi.rollbackFinancePrepaymentAllocate({
      ids: selectedRollbackIds.value
    })
    message.success('回滚成功')
    if (currentTraceId.value) {
      await Promise.all([getList(), loadTrace(currentTraceId.value)])
    } else {
      await getList()
    }
  } finally {
    rollbackLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOptions(), getList()])
})
</script>

<style scoped>
.finance-prepayment-page__state {
  min-height: 260px;
}

.finance-prepayment-page__row-actions {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0 8px;
}

.finance-prepayment-page__drawer {
  height: 100%;
  padding: 16px;
  background: #f8fafc;
}

.finance-prepayment-page__drawer-body {
  display: flex;
  min-height: 240px;
  flex-direction: column;
  gap: 12px;
}

.finance-prepayment-page__trace-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.finance-prepayment-page__trace-card {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.finance-prepayment-page__trace-card-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.finance-prepayment-page__trace-card-sub {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.finance-prepayment-page__trace-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 8px;
  color: #334155;
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

@media (max-width: 1024px) {
  .finance-prepayment-page__row-actions {
    gap: 4px 8px;
  }
}
</style>
