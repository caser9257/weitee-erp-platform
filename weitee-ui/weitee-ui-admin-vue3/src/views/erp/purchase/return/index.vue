<template>
  <section class="purchase-return-hero">
    <div class="purchase-return-hero__header">
      <div>
        <div class="purchase-return-hero__breadcrumb">采购执行 / 采购退货台账</div>
        <div class="purchase-return-page__title">采购退货台账</div>
      </div>
    </div>

    <div class="purchase-return-kpi-grid">
      <div class="purchase-return-kpi-card purchase-return-kpi-card--blue">
        <div>
          <div class="purchase-return-kpi-card__label">累计退货总额</div>
          <div class="purchase-return-kpi-card__value">{{
            formatCurrency(statistics.totalRefundValue)
          }}</div>
        </div>
        <div class="purchase-return-kpi-card__suffix">元</div>
      </div>
      <div class="purchase-return-kpi-card purchase-return-kpi-card--green">
        <div>
          <div class="purchase-return-kpi-card__label">已完成退货退款</div>
          <div class="purchase-return-kpi-card__value">{{
            formatCurrency(statistics.completedRefundValue)
          }}</div>
        </div>
        <div class="purchase-return-kpi-card__meta">{{ statistics.completedCount }} 单</div>
      </div>
      <div class="purchase-return-kpi-card purchase-return-kpi-card--amber">
        <div>
          <div class="purchase-return-kpi-card__label">部分退款处理中</div>
          <div class="purchase-return-kpi-card__value">{{
            formatCurrency(statistics.partialRefundValue)
          }}</div>
        </div>
        <div class="purchase-return-kpi-card__meta">{{ statistics.partialCount }} 单</div>
      </div>
      <div class="purchase-return-kpi-card purchase-return-kpi-card--teal">
        <div>
          <div class="purchase-return-kpi-card__label">待提交审批单据</div>
          <div class="purchase-return-kpi-card__value">{{ statistics.pendingApprovalCount }}</div>
        </div>
        <div class="purchase-return-kpi-card__meta">待处理</div>
      </div>
    </div>
  </section>

  <ContentWrap class="purchase-return-page__filter-card">
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="purchase-return-query"
    >
      <div class="purchase-return-query__grid purchase-return-query__grid--primary">
        <el-form-item label="退货单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入退货单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select
            v-model="queryParams.supplierId"
            clearable
            filterable
            placeholder="请选择供应商"
          >
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" prop="orderNo">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="请输入关联订单"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="退款进度" prop="refundStatus">
          <el-select v-model="queryParams.refundStatus" placeholder="请选择退款进度" clearable>
            <el-option label="未退款" value="0" />
            <el-option label="部分退款" value="1" />
            <el-option label="全部退款" value="2" />
          </el-select>
        </el-form-item>
      </div>

      <transition name="purchase-return-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="purchase-return-query__grid purchase-return-query__grid--advanced"
        >
          <el-form-item label="退货日期（开始）" prop="returnDateStart">
            <el-date-picker
              v-model="returnDateStart"
              value-format="YYYY-MM-DD 00:00:00"
              type="date"
              placeholder="年 / 月 / 日"
            />
          </el-form-item>
          <el-form-item label="退货日期（结束）" prop="returnDateEnd">
            <el-date-picker
              v-model="returnDateEnd"
              value-format="YYYY-MM-DD 23:59:59"
              type="date"
              placeholder="年 / 月 / 日"
            />
          </el-form-item>
          <el-form-item label="创建人" prop="creator">
            <el-select
              v-model="queryParams.creator"
              clearable
              filterable
              placeholder="请选择创建人"
            >
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="收发仓库" prop="warehouseId">
            <el-select
              v-model="queryParams.warehouseId"
              clearable
              filterable
              placeholder="请输入发货仓位过滤"
            >
              <el-option
                v-for="item in warehouseList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>
      </transition>

      <div class="purchase-return-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
          <span v-if="advancedFilterCount" class="purchase-return-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="purchase-return-query__footer-actions">
          <div class="purchase-return-query__quick-search">
            <el-input
              v-model="quickSearchKeyword"
              clearable
              placeholder="全局快搜（单号/供应商/产品）"
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
          </div>
          <div class="purchase-return-query__actions">
            <el-button @click="resetQuery" :disabled="loading">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
            <el-button type="primary" @click="handleQuery" :loading="loading">
              <Icon icon="ep:search" class="mr-5px" /> 查询
            </el-button>
          </div>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="purchase-return-page__list-card">
    <div class="purchase-return-control-panel">
      <div class="purchase-return-control-panel__tabs">
        <button
          v-for="tab in filterTabs"
          :key="tab.key"
          type="button"
          :class="[
            'purchase-return-status-tab',
            { 'purchase-return-status-tab--active': activeStatusTab === tab.key }
          ]"
          @click="activeStatusTab = tab.key"
        >
          <span>{{ tab.label }}</span>
          <span class="purchase-return-status-tab__count">{{ getTabCount(tab.key) }}</span>
        </button>
      </div>
    </div>

    <div class="purchase-return-toolbar">
      <div class="purchase-return-toolbar__actions">
        <el-button
          v-if="toolbarState.showCreate"
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:purchase-return:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增退货
        </el-button>
        <el-button
          v-if="toolbarState.showExport"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:purchase-return:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
      </div>
      <div class="purchase-return-toolbar__meta">
        <el-button
          v-if="toolbarState.showBatchDelete"
          plain
          type="danger"
          :disabled="toolbarState.disableBatchDelete"
          @click="handleDelete(deletableSelectionIds)"
          v-hasPermi="['erp:purchase-return:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <el-table
      v-if="!isCompactLayout"
      v-loading="loading"
      :data="displayList"
      :stripe="true"
      class="purchase-return-ledger"
      @selection-change="handleSelectionChange"
    >
      <template #empty>
        <div v-if="listLoadFailed" class="purchase-return-empty purchase-return-empty--error">
          <div class="purchase-return-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="purchase-return-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
            重试加载
          </el-button>
        </div>
        <div v-else class="purchase-return-empty">
          <div class="purchase-return-empty__icon">
            <Icon icon="ep:box" />
          </div>
          <div class="purchase-return-empty__title">暂无采购退货记录</div>
        </div>
      </template>
      <el-table-column width="36" type="selection" />
      <el-table-column label="退货信息" min-width="158">
        <template #default="{ row }">
          <div class="ledger-order">
            <div class="ledger-order__top">
              <div class="ledger-order__no">{{ row.no || '-' }}</div>
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
              >
                {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
              </el-tag>
            </div>
            <div class="ledger-order__meta">退货 {{ formatDateValue(row.returnTime) }}</div>
            <div class="ledger-order__meta">创建人 {{ row.creatorName || '-' }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="供应商与订单" min-width="142">
        <template #default="{ row }">
          <div class="ledger-party">
            <div class="ledger-party__supplier">{{ row.supplierName || '-' }}</div>
            <div class="ledger-party__meta">
              <span class="ledger-party__tag">关联订单</span>
              <span class="ledger-party__text">{{ row.orderNo || '-' }}</span>
            </div>
            <div class="ledger-party__meta">
              <span class="ledger-party__tag">仓库</span>
              <span class="ledger-party__text">{{ resolveWarehouseSummary(row) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品摘要" min-width="148">
        <template #default="{ row }">
          <div class="ledger-product">
            <div class="ledger-product__name" :title="row.productNames || '-'">
              {{ row.productNames || '-' }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量 / 金额" min-width="128" align="right">
        <template #default="{ row }">
          <div class="ledger-finance">
            <div class="ledger-finance__amount">{{ formatCurrency(row.totalPrice) }}</div>
            <div class="ledger-finance__meta">数量 {{ formatCount(row.totalCount) }}</div>
            <div class="ledger-finance__sub">已退 {{ formatCurrency(row.refundPrice) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="退款进度" min-width="164">
        <template #default="{ row }">
          <div class="ledger-progress">
            <div class="ledger-progress__top">
              <span>已退金额</span>
              <strong
                >{{ formatCurrency(row.refundPrice) }} /
                {{ formatCurrency(row.totalPrice) }}</strong
              >
            </div>
            <el-progress
              :stroke-width="6"
              :show-text="false"
              :percentage="getRefundPercent(row)"
              :color="resolveRefundProgressColor(row)"
            />
            <div class="ledger-progress__summary">
              <span>未退 {{ formatCurrency(getRemainingRefund(row)) }}</span>
              <el-tag size="small" effect="light" :type="resolveRefundStatusTagType(row)">
                {{ getRefundStatusLabel(row) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="184" align="center">
        <template #default="{ row }">
          <div class="purchase-return-row-actions">
            <el-button
              v-if="getPrimaryRowAction(row)"
              :type="getPrimaryRowAction(row)?.type || 'primary'"
              size="small"
              class="purchase-return-row-actions__primary"
              :disabled="getPrimaryRowAction(row)?.disabled"
              :loading="getPrimaryRowAction(row)?.loading"
              @click="handlePrimaryAction(row)"
            >
              {{ getPrimaryRowAction(row)?.label }}
            </el-button>
            <el-button
              link
              type="primary"
              class="purchase-return-row-actions__detail"
              @click="openForm('detail', row.id)"
              v-hasPermi="['erp:purchase-return:query']"
            >
              详情
            </el-button>
            <span class="purchase-return-row-actions__divider"></span>
            <el-dropdown
              v-if="getOverflowActionDescriptors(row).length"
              trigger="click"
              @command="(command) => handleCommand(command, row)"
            >
              <template #default>
                <el-button
                  link
                  type="primary"
                  class="purchase-return-row-actions__more"
                  title="更多操作"
                  aria-label="更多操作"
                >
                  <Icon icon="ep:more-filled" />
                </el-button>
              </template>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="action in getOverflowActionDescriptors(row)"
                    :key="`desktop-overflow-${row.id}-${action.key}`"
                    :command="action.key"
                    :disabled="action.disabled"
                    :class="{ 'text-red-500': action.danger }"
                    :divided="action.danger"
                  >
                    {{ action.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-else-if="listLoadFailed" class="purchase-return-empty purchase-return-empty--error">
      <div class="purchase-return-empty__icon">
        <Icon icon="ep:warning-filled" />
      </div>
      <div class="purchase-return-empty__title">列表加载失败</div>
      <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
        重试加载
      </el-button>
    </div>
    <div v-else-if="list.length === 0" class="purchase-return-empty">
      <div class="purchase-return-empty__icon">
        <Icon icon="ep:box" />
      </div>
      <div class="purchase-return-empty__title">暂无采购退货记录</div>
    </div>
    <div v-else class="purchase-return-mobile-list">
      <section v-for="row in displayList" :key="row.id" class="purchase-return-mobile-card">
        <div class="purchase-return-mobile-card__head">
          <div class="purchase-return-mobile-card__no">{{ row.no || '-' }}</div>
          <el-tag
            size="small"
            effect="light"
            :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
          >
            {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
          </el-tag>
        </div>
        <div class="purchase-return-mobile-card__meta">
          <span>退货 {{ formatDateValue(row.returnTime) }}</span>
          <span>创建人 {{ row.creatorName || '-' }}</span>
        </div>
        <div class="purchase-return-mobile-card__detail">
          <div class="purchase-return-mobile-card__line">
            <span class="purchase-return-mobile-card__label">供应商</span>
            <span class="purchase-return-mobile-card__value">{{ row.supplierName || '-' }}</span>
          </div>
          <div class="purchase-return-mobile-card__line">
            <span class="purchase-return-mobile-card__label">关联订单</span>
            <span class="purchase-return-mobile-card__value">{{ row.orderNo || '-' }}</span>
          </div>
          <div class="purchase-return-mobile-card__line">
            <span class="purchase-return-mobile-card__label">产品信息</span>
            <span class="purchase-return-mobile-card__value">{{ row.productNames || '-' }}</span>
          </div>
          <div class="purchase-return-mobile-card__line">
            <span class="purchase-return-mobile-card__label">总数量</span>
            <span
              class="purchase-return-mobile-card__value purchase-return-mobile-card__value--mono"
            >
              {{ formatCount(row.totalCount) }}
            </span>
          </div>
          <div class="purchase-return-mobile-card__line">
            <span class="purchase-return-mobile-card__label">未退金额</span>
            <span
              class="purchase-return-mobile-card__value purchase-return-mobile-card__value--danger"
            >
              {{ formatCurrency(getRemainingRefund(row)) }}
            </span>
          </div>
        </div>
        <div class="purchase-return-mobile-card__progress">
          <div class="purchase-return-mobile-card__progress-top">
            <span>{{ getRefundStatusLabel(row) }}</span>
            <strong
              >{{ formatCurrency(row.refundPrice) }} / {{ formatCurrency(row.totalPrice) }}</strong
            >
          </div>
          <el-progress
            :stroke-width="6"
            :show-text="false"
            :percentage="getRefundPercent(row)"
            :color="resolveRefundProgressColor(row)"
          />
        </div>
        <div class="purchase-return-mobile-card__actions">
          <el-button
            v-if="getPrimaryRowAction(row)"
            :type="getPrimaryRowAction(row)?.type || 'primary'"
            size="small"
            class="purchase-return-mobile-card__primary"
            :disabled="getPrimaryRowAction(row)?.disabled"
            :loading="getPrimaryRowAction(row)?.loading"
            @click="handlePrimaryAction(row)"
          >
            {{ getPrimaryRowAction(row)?.label }}
          </el-button>
          <el-button
            link
            type="primary"
            class="purchase-return-mobile-card__detail"
            @click="openForm('detail', row.id)"
            v-hasPermi="['erp:purchase-return:query']"
          >
            详情
          </el-button>
          <span class="purchase-return-mobile-card__divider"></span>
          <el-dropdown
            v-if="getOverflowActionDescriptors(row).length"
            trigger="click"
            @command="(command) => handleCommand(command, row)"
          >
            <template #default>
              <el-button
                link
                type="primary"
                class="purchase-return-mobile-card__more"
                title="更多操作"
              >
                <Icon icon="ep:more-filled" />
              </el-button>
            </template>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="action in getOverflowActionDescriptors(row)"
                  :key="`mobile-overflow-${row.id}-${action.key}`"
                  :command="action.key"
                  :disabled="action.disabled"
                  :class="{ 'text-red-500': action.danger }"
                  :divided="action.danger"
                >
                  {{ action.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </section>
    </div>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <PurchaseReturnForm ref="formRef" @success="getList" />
  <PurchaseReturnPrintDialog ref="printDialogRef" />
</template>

<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { PurchaseReturnApi, PurchaseReturnVO } from '@/api/erp/purchase/return'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { checkPermi } from '@/utils/permission'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { useUserStoreWithOut } from '@/store/modules/user'
import { getPurchaseReturnRowActionDescriptor } from './purchaseReturnStatus.helpers'
import PurchaseReturnForm from './PurchaseReturnForm.vue'
import PurchaseReturnPrintDialog from './PurchaseReturnPrintDialog.vue'

defineOptions({ name: 'ErpPurchaseReturn' })

const PURCHASE_RETURN_STATUS = {
  DRAFT: 0,
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30,
  FAILED: 60
} as const

type PurchaseReturnStatusTabKey = 'all' | 'pending' | 'refundPending' | 'completed'

type PurchaseReturnActionKey =
  | 'detail'
  | 'edit'
  | 'submit'
  | 'print'
  | 'cancelApproval'
  | 'processDetail'
  | 'delete'

type PurchaseReturnActionDescriptor = {
  key: PurchaseReturnActionKey
  label: string
  type?: '' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const canSubmitPurchaseReturn = checkPermi([
  'erp:purchase-return:submit',
  'erp:purchase-return:update-status'
])
const canCancelPurchaseReturnApproval = checkPermi([
  'erp:purchase-return:cancel-approval',
  'erp:purchase-return:update-status'
])
const canQueryPurchaseReturn = checkPermi(['erp:purchase-return:query'])

const message = useMessage()
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const { width } = useWindowSize()
const { push } = useRouter()
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<PurchaseReturnVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  supplierId: undefined,
  productId: undefined,
  warehouseId: undefined,
  returnTime: [],
  orderNo: undefined,
  accountId: undefined,
  status: undefined,
  refundStatus: undefined,
  remark: undefined,
  creator: undefined
})

const queryFormRef = ref()
const exportLoading = ref(false)
const cancelApprovalIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const userList = ref<UserVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const accountList = ref<AccountVO[]>([])
const formRef = ref()
const printDialogRef = ref<InstanceType<typeof PurchaseReturnPrintDialog>>()
const selectionList = ref<PurchaseReturnVO[]>([])

const isCompactLayout = computed(() => width.value < 1100)
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const advancedSearchVisible = ref(false)
const activeStatusTab = ref<PurchaseReturnStatusTabKey>('all')
const quickSearchKeyword = ref('')
const resolveRowActionState = (row: PurchaseReturnVO) =>
  getPurchaseReturnRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value
  })
const isApprovalRunning = (row: PurchaseReturnVO) =>
  resolveRowActionState(row).isApprovalRunning
const canEditRow = (row: PurchaseReturnVO) => resolveRowActionState(row).canEdit
const canDeleteRow = (row: PurchaseReturnVO) => resolveRowActionState(row).canDelete
const canSubmitRow = (row: PurchaseReturnVO) => resolveRowActionState(row).canSubmit
const canCancelApprovalRow = (row: PurchaseReturnVO) => resolveRowActionState(row).canCancelApproval
const canViewProcessRow = (row: PurchaseReturnVO) => resolveRowActionState(row).canViewProcess
const isAwaitingSubmitRow = (row: PurchaseReturnVO) => canSubmitRow(row)
const isPendingApprovalTabRow = (row: PurchaseReturnVO) => isApprovalRunning(row)
const deletableSelectionIds = computed(() =>
  selectionList.value.filter(canDeleteRow).map((item) => item.id)
)

const filterTabs = [
  { key: 'all' as const, label: '全部' },
  { key: 'pending' as const, label: '待审批' },
  { key: 'refundPending' as const, label: '退款中' },
  { key: 'completed' as const, label: '已完成' }
]

const advancedFilterCount = computed(() => {
  const advancedFields = [
    queryParams.warehouseId,
    queryParams.creator,
    returnDateStart.value,
    returnDateEnd.value
  ]
  return advancedFields.filter((item) => item !== undefined && item !== null && item !== '').length
})

const toolbarState = computed(() => ({
  showCreate: true,
  showExport: true,
  showBatchDelete: true,
  disableBatchDelete: deletableSelectionIds.value.length === 0 || loading.value
}))

const statistics = computed(() => {
  return list.value.reduce(
    (acc, row) => {
      const totalPrice = normalizeNumber(row.totalPrice)
      const refundPrice = normalizeNumber(row.refundPrice)
      acc.totalRefundValue += totalPrice

      if (refundPrice >= totalPrice && totalPrice > 0) {
        acc.completedRefundValue += refundPrice
        acc.completedCount += 1
      } else if (refundPrice > 0) {
        acc.partialRefundValue += refundPrice
        acc.partialCount += 1
      }

      if (isAwaitingSubmitRow(row)) {
        acc.pendingApprovalCount += 1
      }

      return acc
    },
    {
      totalRefundValue: 0,
      completedRefundValue: 0,
      partialRefundValue: 0,
      completedCount: 0,
      partialCount: 0,
      pendingApprovalCount: 0
    }
  )
})

const displayList = computed(() => {
  const keyword = quickSearchKeyword.value.trim().toLowerCase()

  return list.value.filter((row) => {
    const refundPercent = getRefundPercent(row)
    const matchesTab =
      activeStatusTab.value === 'all' ||
      (activeStatusTab.value === 'pending' && isPendingApprovalTabRow(row)) ||
      (activeStatusTab.value === 'refundPending' &&
        row.status === PURCHASE_RETURN_STATUS.APPROVE &&
        refundPercent < 100) ||
      (activeStatusTab.value === 'completed' &&
        row.status === PURCHASE_RETURN_STATUS.APPROVE &&
        refundPercent >= 100)

    if (!matchesTab) {
      return false
    }

    if (!keyword) {
      return true
    }

    return [row.no, row.supplierName, row.productNames, row.orderNo]
      .map((item) => String(item || '').toLowerCase())
      .some((item) => item.includes(keyword))
  })
})

const returnDateStart = computed({
  get: () =>
    Array.isArray(queryParams.returnTime) ? queryParams.returnTime[0] || undefined : undefined,
  set: (value: string | undefined) => {
    const end = Array.isArray(queryParams.returnTime) ? queryParams.returnTime[1] : undefined
    queryParams.returnTime = value || end ? [value, end].filter(Boolean) : []
  }
})

const returnDateEnd = computed({
  get: () =>
    Array.isArray(queryParams.returnTime) ? queryParams.returnTime[1] || undefined : undefined,
  set: (value: string | undefined) => {
    const start = Array.isArray(queryParams.returnTime) ? queryParams.returnTime[0] : undefined
    queryParams.returnTime = start || value ? [start, value].filter(Boolean) : []
  }
})

const getTabCount = (tabKey: PurchaseReturnStatusTabKey) => {
  return list.value.filter((row) => {
    const refundPercent = getRefundPercent(row)
    switch (tabKey) {
      case 'all':
        return true
      case 'pending':
        return isPendingApprovalTabRow(row)
      case 'refundPending':
        return row.status === PURCHASE_RETURN_STATUS.APPROVE && refundPercent < 100
      case 'completed':
        return row.status === PURCHASE_RETURN_STATUS.APPROVE && refundPercent >= 100
    }
  }).length
}

const formatDateValue = (value?: Date | string | number) =>
  value ? formatDate(value as Date, 'YYYY-MM-DD') : '-'

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatCount = (value?: number | string | null) => {
  const numberValue = normalizeNumber(value)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) =>
  new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(normalizeNumber(value))

const resolveWarehouseName = (warehouseId?: number) =>
  warehouseList.value.find((item) => item.id === warehouseId)?.name || '-'

const resolveWarehouseSummary = (row: PurchaseReturnVO) => {
  const warehouseNames = Array.from(
    new Set(
      (row.items || [])
        .map((item) => resolveWarehouseName(item.warehouseId))
        .filter((name) => name && name !== '-')
    )
  )
  return warehouseNames.length ? warehouseNames.join('、') : '-'
}

const getRemainingRefund = (row: PurchaseReturnVO) =>
  Math.max(normalizeNumber(row.totalPrice) - normalizeNumber(row.refundPrice), 0)

const getRefundPercent = (row: PurchaseReturnVO) => {
  const totalPrice = normalizeNumber(row.totalPrice)
  if (totalPrice <= 0) {
    return 0
  }
  return Math.min((normalizeNumber(row.refundPrice) / totalPrice) * 100, 100)
}

const resolveRefundProgressColor = (row: PurchaseReturnVO) => {
  const percent = getRefundPercent(row)
  if (percent >= 100) {
    return 'var(--erp-success-600)'
  }
  if (percent > 0) {
    return 'var(--erp-warning-600)'
  }
  return 'var(--erp-slate-400)'
}

const getRefundStatusLabel = (row: PurchaseReturnVO) => {
  const percent = getRefundPercent(row)
  if (percent >= 100) {
    return '全部退款'
  }
  if (percent > 0) {
    return '部分退款'
  }
  return '未退款'
}

const resolveRefundStatusTagType = (row: PurchaseReturnVO) => {
  const percent = getRefundPercent(row)
  if (percent >= 100) {
    return 'success'
  }
  if (percent > 0) {
    return 'warning'
  }
  return 'info'
}

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await PurchaseReturnApi.getPurchaseReturnPage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
    message.error('采购退货列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleRetryList = () => {
  if (!canRetryList.value) {
    return
  }
  getList()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  activeStatusTab.value = 'all'
  quickSearchKeyword.value = ''
  handleQuery()
}

const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const openPrintDialog = (id?: number) => {
  if (!id) {
    return
  }
  printDialogRef.value?.open(id)
}

const handleDelete = async (ids: number[]) => {
  try {
    await message.delConfirm()
    await PurchaseReturnApi.deletePurchaseReturn(ids)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch {}
}

const isCancelingApproval = (id?: number) => !!id && cancelApprovalIds.value.includes(id)

const getPrimaryRowAction = (row: PurchaseReturnVO): PurchaseReturnActionDescriptor | null => {
  if (canSubmitPurchaseReturn && row.status === PURCHASE_RETURN_STATUS.DRAFT && canSubmitRow(row)) {
    return {
      key: 'submit',
      label: '送审',
      type: 'primary'
    }
  }

  if (checkPermi(['erp:purchase-return:update']) && row.status === PURCHASE_RETURN_STATUS.REJECT) {
    return {
      key: 'edit',
      label: '重新编辑',
      type: 'warning'
    }
  }

  if (canSubmitPurchaseReturn && row.status === PURCHASE_RETURN_STATUS.FAILED && canSubmitRow(row)) {
    return {
      key: 'submit',
      label: '重新提交审批',
      type: 'warning'
    }
  }

  if (checkPermi(['erp:purchase-return:update']) && canEditRow(row)) {
    return {
      key: 'edit',
      label: '编辑',
      type: 'info'
    }
  }

  if (row.status === PURCHASE_RETURN_STATUS.APPROVE && getRefundPercent(row) >= 100) {
    return {
      key: 'detail',
      label: '查看',
      type: 'info'
    }
  }

  return null
}

const getOverflowActionDescriptors = (row: PurchaseReturnVO): PurchaseReturnActionDescriptor[] => {
  const primaryAction = getPrimaryRowAction(row)
  const actions: PurchaseReturnActionDescriptor[] = []

  if (
    checkPermi(['erp:purchase-return:update']) &&
    canEditRow(row) &&
    primaryAction?.key !== 'edit'
  ) {
    actions.push({
      key: 'edit',
      label: '编辑'
    })
  }

  if (canSubmitPurchaseReturn && canSubmitRow(row) && primaryAction?.key !== 'submit') {
    actions.push({
      key: 'submit',
      label:
        row.status === PURCHASE_RETURN_STATUS.REJECT || row.status === PURCHASE_RETURN_STATUS.FAILED
          ? '重新提交审批'
          : '提交审批'
    })
  }

  if (canQueryPurchaseReturn && primaryAction?.key !== 'detail') {
    actions.unshift({
      key: 'detail',
      label: '查看详情'
    })
  }

  if (row.id) {
    actions.push({
      key: 'print',
      label: '打印'
    })
  }

  if (canQueryPurchaseReturn && canViewProcessRow(row)) {
    actions.push({
      key: 'processDetail',
      label: '查看审批'
    })
  }

  if (canCancelPurchaseReturnApproval && canCancelApprovalRow(row)) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      disabled: isCancelingApproval(row.id)
    })
  }

  if (checkPermi(['erp:purchase-return:delete']) && canDeleteRow(row)) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: isCancelingApproval(row.id),
      danger: true
    })
  }

  return actions
}

const handlePrimaryAction = async (row: PurchaseReturnVO) => {
  const action = getPrimaryRowAction(row)
  if (!action) {
    return
  }
  await handleCommand(action.key, row)
}

const handleCommand = async (command: string, row: PurchaseReturnVO) => {
  switch (command as PurchaseReturnActionKey) {
    case 'detail':
      openForm('detail', row.id)
      return
    case 'edit':
      openForm('update', row.id)
      return
    case 'submit':
      await handleSubmit(row)
      return
    case 'print':
      openPrintDialog(row.id)
      return
    case 'cancelApproval':
      await handleCancelApproval(row)
      return
    case 'processDetail':
      handleProcessDetail(row)
      return
    case 'delete':
      if (row.id) {
        await handleDelete([row.id])
      }
      return
  }
}

const handleSubmit = async (row: PurchaseReturnVO) => {
  if (!row.id) return
  try {
    await message.confirm('确定提交审批该退货单吗？')
    await PurchaseReturnApi.submitPurchaseReturn({ id: row.id })
    await getList()
    const latestRow = list.value.find((item) => item.id === row.id)
    if (latestRow?.status === PURCHASE_RETURN_STATUS.FAILED) {
      message.warning('提交已受理，但流程创建失败')
      return
    }
    if (latestRow && isApprovalRunning(latestRow)) {
      message.success('已提交审批，等待流程受理')
      return
    }
    message.warning('提交请求已发送，请刷新后确认状态')
  } catch {}
}

const handleCancelApproval = async (row: PurchaseReturnVO) => {
  if (!row.id || cancelApprovalIds.value.includes(row.id)) return
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    cancelApprovalIds.value = Array.from(new Set([...cancelApprovalIds.value, row.id]))
    await PurchaseReturnApi.cancelPurchaseReturnApproval({
      id: row.id,
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch {
  } finally {
    cancelApprovalIds.value = cancelApprovalIds.value.filter((id) => id !== row.id)
  }
}

const handleProcessDetail = (row: PurchaseReturnVO) => {
  if (!row.processInstanceId) {
    message.warning('当前采购退货暂无审批流程')
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
    const data = await PurchaseReturnApi.exportPurchaseReturn(queryParams)
    download.excel(data, '采购退货.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: PurchaseReturnVO[]) => {
  selectionList.value = rows
}

onMounted(async () => {
  await getList()
  productList.value = await ProductApi.getProductSimpleList()
  supplierList.value = await SupplierApi.getSupplierSimpleList()
  userList.value = await UserApi.getSimpleUserList()
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  accountList.value = await AccountApi.getAccountSimpleList()
})
</script>

<style scoped>
.purchase-return-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.purchase-return-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.purchase-return-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.purchase-return-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.purchase-return-kpi-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 108px;
  padding: 18px 20px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 16px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.purchase-return-kpi-card--blue {
  background: var(--erp-stat-gradient-blue);
}

.purchase-return-kpi-card--green {
  background: var(--erp-stat-gradient-green);
}

.purchase-return-kpi-card--amber {
  background: var(--erp-stat-gradient-amber);
}

.purchase-return-kpi-card--teal {
  background: var(--erp-stat-gradient-teal);
}

.purchase-return-kpi-card__label {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 8px;
}

.purchase-return-kpi-card__value {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  font-family:
    ui-monospace,
    SFMono-Regular,
    Menlo,
    Monaco,
    Consolas,
    Liberation Mono,
    Courier New,
    monospace;
}

.purchase-return-kpi-card__suffix,
.purchase-return-kpi-card__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
}

.purchase-return-page__filter-card,
.purchase-return-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.purchase-return-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.purchase-return-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-date-editor.el-input__wrapper.is-focus) {
    border-color: var(--erp-primary-300);
    background: var(--erp-surface-white);
    box-shadow: 0 0 0 3px var(--erp-primary-50);
  }

  :deep(.el-input__inner),
  :deep(.el-select__placeholder),
  :deep(.el-range-input),
  :deep(.el-input__prefix),
  :deep(.el-input__suffix) {
    font-size: 12px;
  }

  :deep(.el-input__inner::placeholder),
  :deep(.el-range-input::placeholder) {
    color: var(--erp-slate-400);
  }
}

.purchase-return-query__grid {
  display: grid;
  gap: 14px 14px;
}

.purchase-return-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-return-query__grid--advanced {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--erp-slate-200);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-return-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.purchase-return-query__footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.purchase-return-query__quick-search {
  width: 320px;
}

.purchase-return-query__quick-search :deep(.el-input__wrapper) {
  min-height: 38px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  background: var(--erp-slate-50);
}

.purchase-return-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.purchase-return-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  font-size: 11px;
  font-weight: 700;
}

.purchase-return-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.purchase-return-control-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 16px;
  margin-bottom: 16px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 16px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.purchase-return-control-panel__tabs {
  display: flex;
  align-items: center;
  gap: 20px;
  overflow-x: auto;
  min-width: 0;
}

.purchase-return-control-panel__search {
  width: 280px;
  flex-shrink: 0;
  padding: 10px 0;
}

.purchase-return-control-panel__search :deep(.el-input__wrapper) {
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  background: var(--erp-slate-50);
  box-shadow: none;
}

.purchase-return-status-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 14px 0;
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s ease;
}

.purchase-return-status-tab:hover {
  color: var(--erp-slate-800);
}

.purchase-return-status-tab--active {
  border-bottom-color: var(--erp-primary-600);
  color: var(--erp-primary-600);
}

.purchase-return-status-tab__count {
  min-width: 18px;
  padding: 1px 6px;
  border-radius: 999px;
  background: var(--erp-slate-100);
  color: inherit;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
}

.purchase-return-status-tab--active .purchase-return-status-tab__count {
  background: var(--erp-primary-50);
}

.purchase-return-toolbar__actions,
.purchase-return-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.purchase-return-ledger {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-500);
    font-size: 11px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 8px;
    padding-left: 8px;
  }

  :deep(.cell) {
    overflow: visible;
  }

  :deep(.el-table__row td) {
    padding-top: 14px;
    padding-bottom: 14px;
    vertical-align: top;
  }

  :deep(.el-progress-bar__outer) {
    background: var(--erp-slate-200);
  }
}

.ledger-order,
.ledger-party,
.ledger-product,
.ledger-finance,
.ledger-progress {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.ledger-order__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.ledger-order__no,
.ledger-party__supplier,
.ledger-product__name,
.ledger-finance__amount {
  color: var(--erp-slate-900);
  font-weight: 800;
  line-height: 22px;
}

.ledger-order__meta,
.ledger-party__meta,
.ledger-finance__meta,
.ledger-finance__sub {
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.ledger-party__meta {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.ledger-party__tag {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--erp-slate-100);
  color: var(--erp-slate-600);
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.ledger-party__text,
.ledger-product__name {
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-finance {
  align-items: flex-end;
}

.ledger-finance__amount {
  font-size: 20px;
  line-height: 1;
  font-family:
    ui-monospace,
    SFMono-Regular,
    Menlo,
    Monaco,
    Consolas,
    Liberation Mono,
    Courier New,
    monospace;
}

.ledger-finance__meta {
  color: var(--erp-success-600);
  font-weight: 600;
  text-align: right;
}

.ledger-finance__sub {
  text-align: right;
}

.ledger-progress__top,
.ledger-progress__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.ledger-progress__top {
  color: var(--erp-slate-600);
  font-size: 11px;
  line-height: 17px;
}

.ledger-progress__top strong {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.ledger-progress__summary {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.purchase-return-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.purchase-return-row-actions__more,
.purchase-return-mobile-card__more {
  min-width: 28px;
  height: 28px;
  padding: 0;
  border-radius: 999px;
  color: var(--erp-slate-500);
}

.purchase-return-row-actions__detail,
.purchase-return-mobile-card__detail {
  padding: 0 2px;
  font-weight: 600;
}

.purchase-return-row-actions__primary,
.purchase-return-mobile-card__primary {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
}

.purchase-return-row-actions__divider,
.purchase-return-mobile-card__divider {
  width: 1px;
  height: 16px;
  background: var(--erp-slate-200);
}

.purchase-return-row-actions__danger {
  color: var(--erp-danger-600);
}

.purchase-return-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.purchase-return-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.purchase-return-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.purchase-return-empty--error .purchase-return-empty__icon {
  background: var(--erp-stat-gradient-rose);
  color: var(--erp-danger-600);
}

.purchase-return-mobile-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.purchase-return-mobile-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  padding: 16px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.purchase-return-mobile-card__head,
.purchase-return-mobile-card__meta,
.purchase-return-mobile-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.purchase-return-mobile-card__head {
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.purchase-return-mobile-card__no {
  color: var(--erp-slate-900);
  font-weight: 700;
  line-height: 22px;
}

.purchase-return-mobile-card__meta {
  margin-bottom: 12px;
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 20px;
}

.purchase-return-mobile-card__detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.purchase-return-mobile-card__line,
.purchase-return-mobile-card__progress-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.purchase-return-mobile-card__label {
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 20px;
  flex-shrink: 0;
}

.purchase-return-mobile-card__value {
  color: var(--erp-slate-900);
  font-size: 13px;
  line-height: 20px;
  text-align: right;
  word-break: break-word;
}

.purchase-return-mobile-card__value--mono {
  font-family:
    ui-monospace,
    SFMono-Regular,
    Menlo,
    Monaco,
    Consolas,
    Liberation Mono,
    Courier New,
    monospace;
}

.purchase-return-mobile-card__value--danger {
  color: var(--erp-danger-600);
  font-weight: 600;
}

.purchase-return-mobile-card__progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.purchase-return-mobile-card__progress-top {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.purchase-return-mobile-card__actions {
  display: inline-flex;
  padding-top: 12px;
  border-top: 1px solid var(--erp-slate-200);
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.purchase-return-mobile-card__more {
  flex-shrink: 0;
}

.purchase-return-query-collapse-enter-active,
.purchase-return-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.purchase-return-query-collapse-enter-from,
.purchase-return-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 1439px) {
  .purchase-return-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .purchase-return-query__grid--primary,
  .purchase-return-query__grid--advanced {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .purchase-return-page__title {
    font-size: 24px;
    line-height: 32px;
  }

  .ledger-finance__amount {
    font-size: 18px;
  }
}

@media (max-width: 1023px) {
  .purchase-return-query__grid--primary,
  .purchase-return-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .purchase-return-control-panel {
    flex-direction: column;
    align-items: stretch;
    padding-top: 8px;
    padding-bottom: 12px;
  }

  .purchase-return-control-panel__search {
    width: 100%;
    padding-top: 0;
  }
}

@media (max-width: 767px) {
  .purchase-return-kpi-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-return-query__grid--primary,
  .purchase-return-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-return-query__footer,
  .purchase-return-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-return-query__footer-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-return-query__quick-search {
    width: 100%;
  }

  .purchase-return-query__actions,
  .purchase-return-toolbar__actions,
  .purchase-return-toolbar__meta {
    width: 100%;
  }

  .purchase-return-query__actions :deep(.el-button),
  .purchase-return-toolbar__actions :deep(.el-button),
  .purchase-return-toolbar__meta :deep(.el-button) {
    flex: 1 1 calc(50% - 6px);
    min-width: 0;
  }
}
</style>
