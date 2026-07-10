<template>
  <ContentWrap v-if="traceActive" class="purchase-order-page__trace-card">
    <div class="purchase-order-trace">
      <div class="purchase-order-trace__text">
        当前按销售订单
        <el-tag class="mx-8px" type="success">{{ traceLabel }}</el-tag>
        追踪采购订单
      </div>
      <el-button link type="primary" @click="clearTraceFilter">清除筛选</el-button>
    </div>
  </ContentWrap>

  <ContentWrap v-if="shouldBlockSaleTraceByPermission" class="purchase-order-page__permission-card">
    <div class="purchase-order-empty purchase-order-empty--permission">
      <div class="purchase-order-empty__icon">
        <Icon icon="ep:lock" />
      </div>
      <div class="purchase-order-empty__title">当前账号没有采购订单查看权限</div>
      <div class="purchase-order-empty__actions">
        <el-button type="primary" @click="goBackFromUnauthorizedState">
          {{ unauthorizedBackLabel }}
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap v-else class="purchase-order-page__filter-card">
    <div class="purchase-order-page__title">采购订单台账</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="purchase-order-query"
    >
      <div class="purchase-order-query__grid purchase-order-query__grid--primary">
        <el-form-item label="订单单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入订单单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select
            v-model="queryParams.supplierId"
            placeholder="请选择供应商"
            clearable
            filterable
          >
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源销售单" prop="sourceOrderId">
          <el-input-number
            v-model="queryParams.sourceOrderId"
            :min="1"
            :precision="0"
            :controls="false"
            :disabled="traceActive"
            placeholder="请输入来源销售单 ID"
          />
        </el-form-item>
        <el-form-item label="订单时间" prop="orderTime">
          <el-date-picker
            v-model="queryParams.orderTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
      </div>

      <transition name="purchase-order-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="purchase-order-query__grid purchase-order-query__grid--advanced"
        >
          <el-form-item label="产品" prop="productId">
            <el-select
              v-model="queryParams.productId"
              placeholder="请选择产品"
              clearable
              filterable
            >
              <el-option
                v-for="item in productList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建人" prop="creator">
            <el-select
              v-model="queryParams.creator"
              placeholder="请选择创建人"
              clearable
              filterable
            >
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="入库进度" prop="inStatus">
            <el-select v-model="queryParams.inStatus" placeholder="请选择入库进度" clearable>
              <el-option label="未入库" value="0" />
              <el-option label="部分入库" value="1" />
              <el-option label="全部入库" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="退货进度" prop="returnStatus">
            <el-select v-model="queryParams.returnStatus" placeholder="请选择退货进度" clearable>
              <el-option label="未退货" value="0" />
              <el-option label="部分退货" value="1" />
              <el-option label="全部退货" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="queryParams.remark"
              placeholder="请输入备注"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
      </transition>

      <div class="purchase-order-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
          <span v-if="advancedFilterCount" class="purchase-order-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="purchase-order-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="purchase-order-page__list-card">
    <div class="purchase-order-toolbar">
      <div class="purchase-order-toolbar__actions">
        <el-button
          v-if="toolbarState.showCreate && canCreatePurchaseOrder"
          type="primary"
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增订单
        </el-button>
        <el-button
          v-if="toolbarState.showExport"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:purchase-order:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
        <el-button
          v-if="toolbarState.showBatchEdit"
          plain
          :disabled="toolbarState.disableBatchEdit"
          @click="openBatchEditDrawer()"
          v-hasPermi="['erp:purchase-order:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
        </el-button>
        <el-button
          v-if="toolbarState.showTodo"
          plain
          @click="openPurchaseOrderTodoTask"
          v-hasPermi="['bpm:task:query']"
        >
          <Icon icon="ep:promotion" class="mr-5px" /> 审批待办
        </el-button>
      </div>
      <div class="purchase-order-toolbar__meta">
        <el-button
          v-if="toolbarState.showBatchDelete"
          plain
          type="danger"
          :disabled="toolbarState.disableBatchDelete"
          @click="handleDelete(deletableSelectionIds)"
          v-hasPermi="['erp:purchase-order:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>

    <el-table
      v-if="!isCompactLayout"
      v-loading="loading"
      :data="list"
      :stripe="true"
      class="purchase-order-ledger"
      @selection-change="handleSelectionChange"
    >
      <template #empty>
        <div v-if="listLoadFailed" class="purchase-order-empty purchase-order-empty--error">
          <div class="purchase-order-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="purchase-order-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
            重试加载
          </el-button>
        </div>
        <div v-else class="purchase-order-empty">
          <div class="purchase-order-empty__icon">
            <Icon icon="ep:box" />
          </div>
          <div class="purchase-order-empty__title">暂无采购订单记录</div>
        </div>
      </template>
      <el-table-column width="36" type="selection" :selectable="canSelectRow" />
      <el-table-column label="订单信息" min-width="154">
        <template #default="{ row }">
          <div class="ledger-order">
            <div class="ledger-order__top">
              <div class="ledger-order__no">{{ row.no || '-' }}</div>
              <el-tag size="small" effect="light" :type="resolveSourceTypeTagType(row.sourceType)">
                {{ resolveSourceTypeText(row.sourceType) }}
              </el-tag>
            </div>
            <div class="ledger-order__meta">下单 {{ formatDateValue(row.orderTime) }}</div>
            <div class="ledger-order__meta">创建 {{ formatDateValue(row.createTime) }}</div>
            <div class="ledger-order__meta">
              归属 {{ row.businessOwnerName || row.creatorName || '-' }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="供应商与来源" min-width="144">
        <template #default="{ row }">
          <div class="ledger-party">
            <div class="ledger-party__supplier">{{ row.supplierName || '-' }}</div>
            <div class="ledger-party__meta" :title="row.sourceOrderNos || '-'">
              <span class="ledger-party__tag">来源销售</span>
              <span class="ledger-party__text">{{ row.sourceOrderNos || '-' }}</span>
            </div>
            <div class="ledger-party__meta">
              <span class="ledger-party__tag">项目</span>
              <span class="ledger-party__text">{{ formatProjectIds(row) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品摘要" min-width="112">
        <template #default="{ row }">
          <div class="ledger-product" :title="row.productNames || '-'">
            {{ row.productNames || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量 / 入库" min-width="164">
        <template #default="{ row }">
          <div class="ledger-progress">
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>入库进度</span>
                <strong>{{ formatCount(row.inCount) }} / {{ formatCount(row.totalCount) }}</strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.inCount, row.totalCount)"
                :color="resolveInboundProgressColor(row)"
              />
            </div>
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>退货进度</span>
                <strong>
                  {{ formatCount(row.returnCount) }} /
                  {{ formatCount(normalizeNumber(row.inCount) || normalizeNumber(row.totalCount)) }}
                </strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getReturnPercent(row.returnCount, row.inCount, row.totalCount)"
                color="var(--erp-warning-600)"
              />
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="财务结算（元）" min-width="124" align="right">
        <template #default="{ row }">
          <div class="ledger-finance">
            <div class="ledger-finance__amount">{{ formatCurrency(row.totalPrice) }}</div>
            <div class="ledger-finance__meta">货款 {{ formatCurrency(row.totalProductPrice) }}</div>
            <div class="ledger-finance__sub">税额 {{ formatCurrency(row.totalTaxPrice) }}</div>
            <div class="ledger-finance__sub">
              {{
                normalizeNumber(row.depositPrice) > 0
                  ? `订金 ${formatCurrency(row.depositPrice)}`
                  : '无订金'
              }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态 / 入库" min-width="110">
        <template #default="{ row }">
          <div class="ledger-status">
            <div class="ledger-status__badges">
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
              >
                {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="resolveInboundResultTagType(row)">
                {{ resolveInboundStatus(row).resultLabel }}
              </el-tag>
              <el-tag
                v-if="resolveInboundStatus(row).processingLabel"
                size="small"
                effect="light"
                type="primary"
              >
                {{ resolveInboundStatus(row).processingLabel }}
              </el-tag>
              <el-tag size="small" effect="light" :type="resolveReturnTagType(row)">
                {{ resolveReturnText(row) }}
              </el-tag>
            </div>
            <div
              v-if="row.lastRejectReason"
              class="ledger-status__reject"
              :title="row.lastRejectReason"
            >
              {{ row.lastRejectReason }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="left">
        <template #default="{ row }">
          <div class="ledger-actions">
            <template
              v-for="action in getInlineActionDescriptors(row)"
              :key="`desktop-${row.id}-${action.key}`"
            >
              <el-button
                link
                :type="action.type"
                :disabled="action.disabled"
                :loading="action.loading"
                @click="handleCommand(action.key, row)"
              >
                {{ action.label }}
              </el-button>
            </template>
            <el-dropdown
              v-if="getOverflowActionDescriptors(row).length"
              @command="(command) => handleCommand(command, row)"
            >
              <el-tooltip content="更多操作" placement="top">
                <el-button link type="primary" class="ledger-actions__more">
                  <Icon icon="ep:more-filled" />
                </el-button>
              </el-tooltip>
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

    <div
      v-else
      v-loading="loading"
      class="purchase-order-mobile-list"
      element-loading-background="rgba(248, 250, 252, 0.78)"
    >
      <template v-if="list.length">
        <article v-for="row in list" :key="row.id" class="purchase-order-mobile-card">
          <div class="purchase-order-mobile-card__head">
            <el-checkbox
              :model-value="selectedIdSet.has(row.id)"
              :disabled="!canSelectRow(row)"
              @change="(checked) => toggleSelection(row, checked)"
            />
            <div class="purchase-order-mobile-card__identity">
              <div class="purchase-order-mobile-card__no">{{ row.no || '-' }}</div>
              <div class="purchase-order-mobile-card__meta">
                <span>下单 {{ formatDateValue(row.orderTime) }}</span>
                <span>创建 {{ formatDateValue(row.createTime) }}</span>
                <span>归属 {{ row.businessOwnerName || row.creatorName || '-' }}</span>
              </div>
            </div>
            <div class="purchase-order-mobile-card__status">
              <el-tag size="small" effect="light" :type="resolveSourceTypeTagType(row.sourceType)">
                {{ resolveSourceTypeText(row.sourceType) }}
              </el-tag>
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
              >
                {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="resolveInboundResultTagType(row)">
                {{ resolveInboundStatus(row).resultLabel }}
              </el-tag>
              <el-tag
                v-if="resolveInboundStatus(row).processingLabel"
                size="small"
                effect="light"
                type="primary"
              >
                {{ resolveInboundStatus(row).processingLabel }}
              </el-tag>
              <el-tag size="small" effect="light" :type="resolveReturnTagType(row)">
                {{ resolveReturnText(row) }}
              </el-tag>
            </div>
          </div>

          <div class="purchase-order-mobile-card__summary">
            <div class="purchase-order-mobile-card__party">
              <div class="purchase-order-mobile-card__supplier">{{ row.supplierName || '-' }}</div>
              <div class="purchase-order-mobile-card__detail">
                <span class="purchase-order-mobile-card__detail-tag">来源销售</span>
                <span>{{ row.sourceOrderNos || '-' }}</span>
              </div>
              <div class="purchase-order-mobile-card__detail">
                <span class="purchase-order-mobile-card__detail-tag">项目</span>
                <span>{{ formatProjectIds(row) }}</span>
              </div>
            </div>
            <div class="purchase-order-mobile-card__finance">
              <span class="purchase-order-mobile-card__finance-label">订单金额</span>
              <strong class="purchase-order-mobile-card__finance-value">
                {{ formatCurrency(row.totalPrice) }}
              </strong>
              <span>货款 {{ formatCurrency(row.totalProductPrice) }}</span>
              <span>税额 {{ formatCurrency(row.totalTaxPrice) }}</span>
              <span>
                {{
                  normalizeNumber(row.depositPrice) > 0
                    ? `订金 ${formatCurrency(row.depositPrice)}`
                    : '无订金'
                }}
              </span>
            </div>
          </div>

          <div class="purchase-order-mobile-card__product">{{ row.productNames || '-' }}</div>

          <div class="purchase-order-mobile-card__metrics">
            <div class="purchase-order-mobile-card__metric">
              <div class="purchase-order-mobile-card__metric-top">
                <span>入库进度</span>
                <strong>{{ formatCount(row.inCount) }} / {{ formatCount(row.totalCount) }}</strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.inCount, row.totalCount)"
                :color="resolveInboundProgressColor(row)"
              />
            </div>
            <div class="purchase-order-mobile-card__metric">
              <div class="purchase-order-mobile-card__metric-top">
                <span>退货进度</span>
                <strong>
                  {{ formatCount(row.returnCount) }} /
                  {{ formatCount(normalizeNumber(row.inCount) || normalizeNumber(row.totalCount)) }}
                </strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getReturnPercent(row.returnCount, row.inCount, row.totalCount)"
                color="var(--erp-warning-600)"
              />
            </div>
          </div>

          <div v-if="row.lastRejectReason" class="purchase-order-mobile-card__reject">
            {{ row.lastRejectReason }}
          </div>

          <div class="purchase-order-mobile-card__actions">
            <template
              v-for="action in getInlineActionDescriptors(row)"
              :key="`mobile-${row.id}-${action.key}`"
            >
              <el-button
                link
                :type="action.type"
                :disabled="action.disabled"
                :loading="action.loading"
                @click="handleCommand(action.key, row)"
              >
                {{ action.label }}
              </el-button>
            </template>
            <el-dropdown
              v-if="getOverflowActionDescriptors(row).length"
              @command="(command) => handleCommand(command, row)"
            >
              <el-tooltip content="更多操作" placement="top">
                <el-button link type="primary" class="purchase-order-mobile-card__more">
                  <Icon icon="ep:more-filled" />
                </el-button>
              </el-tooltip>
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
        </article>
      </template>
      <div v-else-if="listLoadFailed" class="purchase-order-empty purchase-order-empty--error">
        <div class="purchase-order-empty__icon">
          <Icon icon="ep:warning-filled" />
        </div>
        <div class="purchase-order-empty__title">列表加载失败</div>
        <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
          重试加载
        </el-button>
      </div>
      <div v-else class="purchase-order-empty">
        <div class="purchase-order-empty__icon">
          <Icon icon="ep:box" />
        </div>
        <div class="purchase-order-empty__title">暂无采购订单记录</div>
      </div>
    </div>

    <div class="purchase-order-page__footer">
      <div class="purchase-order-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <PurchaseOrderForm ref="formRef" @success="getList" />
  <PurchaseInForm ref="purchaseInFormRef" @success="getList" />
  <PurchaseOrderAuditLogDialog ref="auditLogDialogRef" />
  <PurchaseOrderSubmitDialog ref="submitDialogRef" @success="getList" />
  <PurchaseOrderBatchEditDrawer
    ref="batchEditDrawerRef"
    :account-options="accountList"
    @success="handleBatchEditSuccess"
  />
  <PurchaseOrderDetailDialog ref="detailDialogRef" @edit="handleDetailDialogEdit" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import {
  PurchaseOrderApi,
  PurchaseOrderPageReqVO,
  PurchaseOrderVO,
  type PurchaseOrderBatchUpdateResultVO
} from '@/api/erp/purchase/order'
import PurchaseOrderForm from './PurchaseOrderForm.vue'
import PurchaseOrderAuditLogDialog from './PurchaseOrderAuditLogDialog.vue'
import PurchaseOrderSubmitDialog from './PurchaseOrderSubmitDialog.vue'
import PurchaseOrderBatchEditDrawer from './components/PurchaseOrderBatchEditDrawer.vue'
import PurchaseOrderDetailDialog from './PurchaseOrderDetailDialog.vue'
import PurchaseInForm from '../in/PurchaseInForm.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import * as UserApi from '@/api/system/user'
import type { SimpleUserVO } from '@/api/system/user'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { useRoute, useRouter } from 'vue-router'
import { useUserStoreWithOut } from '@/store/modules/user'
import { checkPermi } from '@/utils/permission'
import {
  getInboundStatusDescriptor,
  getPurchaseOrderRowActionDescriptor,
  getPurchaseOrderToolbarDescriptor
} from './purchaseOrderStatus.helpers'
import { resolvePurchaseOrderRouteContext } from './purchaseOrderRouteContext.helpers'
import { useWindowSize } from '@vueuse/core'

defineOptions({ name: 'ErpPurchaseOrder' })

const PURCHASE_ORDER_BPM_PROCESS_KEY = 'erp_purchase_order'

const PURCHASE_ORDER_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

type PurchaseOrderActionKey =
  | 'detail'
  | 'edit'
  | 'submit'
  | 'cancelApproval'
  | 'auditLog'
  | 'processDetail'
  | 'purchaseIn'
  | 'delete'

type PurchaseOrderActionDescriptor = {
  key: PurchaseOrderActionKey
  label: string
  type?: '' | 'primary' | 'success' | 'warning' | 'danger'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const route = useRoute()
const router = useRouter()
const { push, replace } = router
const canCreatePurchaseOrder = checkPermi(['erp:purchase-order:create'])
const canQueryPurchaseOrder = checkPermi(['erp:purchase-order:query'])
const canUpdatePurchaseOrder = checkPermi(['erp:purchase-order:update'])
const canSubmitPurchaseOrder = checkPermi(['erp:purchase-order:submit'])
const canCancelPurchaseOrderApproval = checkPermi(['erp:purchase-order:cancel-approval'])
const canDeletePurchaseOrder = checkPermi(['erp:purchase-order:delete'])
const canAccessPurchaseIn = checkPermi(['erp:purchase-in:query', 'erp:purchase-in:create'])
const message = useMessage()
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const { width } = useWindowSize()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<PurchaseOrderVO[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive<PurchaseOrderPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  supplierId: undefined,
  productId: undefined,
  sourceOrderId: undefined,
  orderTime: [],
  status: undefined,
  remark: undefined,
  creator: undefined,
  inStatus: undefined,
  returnStatus: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const cancelApprovalIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<SimpleUserVO[]>([])
const selectionList = ref<PurchaseOrderVO[]>([])
const traceOrderId = ref<number | undefined>()
const traceOrderNo = ref('')
const formRef = ref()
const pendingFormOpen = ref<{ type: string; id?: number } | null>(null)
const purchaseInFormRef = ref()
const auditLogDialogRef = ref()
const submitDialogRef = ref()
const batchEditDrawerRef = ref()
const detailDialogRef = ref()
const currentUserId = computed(() => String(userStore.getUser.id || ''))

const traceActive = computed(() => !!traceOrderId.value)
const isCompactLayout = computed(() => width.value < 1180)
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const selectedIdSet = computed(() => new Set(selectionList.value.map((item) => item.id)))
const isSaleTraceMode = computed(
  () =>
    route.name === 'ErpSaleOrderPurchaseOrderTracePage' || route.query.traceFrom === 'sale-order'
)
const shouldBlockSaleTraceByPermission = computed(
  () => isSaleTraceMode.value && !canQueryPurchaseOrder
)
const traceLabel = computed(() => {
  if (!traceOrderId.value) {
    return ''
  }
  return traceOrderNo.value
    ? `${traceOrderNo.value} (ID: ${traceOrderId.value})`
    : `ID: ${traceOrderId.value}`
})
const unauthorizedBackLabel = computed(() => {
  if (route.query.traceFrom === 'closure-workbench') {
    return '返回闭环工作台'
  }
  if (route.query.traceFrom === 'mrp-trace') {
    return '返回 MRP 追踪'
  }
  return '返回销售订单台账'
})

const advancedFilterCount = computed(() => {
  const advancedFields = [
    queryParams.productId,
    queryParams.creator,
    queryParams.status,
    queryParams.inStatus,
    queryParams.returnStatus,
    queryParams.remark
  ]
  return advancedFields.filter((item) => item !== undefined && item !== null && item !== '').length
})

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatDateValue = (value?: Date | string | number) => {
  return value ? formatDate(value as Date, 'YYYY-MM-DD') : '-'
}

const formatCount = (value?: number | string | null) => {
  const numberValue = normalizeNumber(value)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatCurrency = (value?: number | string | null) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(normalizeNumber(value))
}

const formatProjectIds = (row: PurchaseOrderVO) => {
  if (row.projectId) {
    return `#${row.projectId}`
  }
  const projectIds = Array.from(
    new Set(
      (row.items || []).map((item) => item.projectId).filter((item): item is number => !!item)
    )
  )
  if (projectIds.length === 0) {
    return '-'
  }
  return projectIds.map((item) => `#${item}`).join(' / ')
}

const resolveSourceTypeText = (sourceType?: string) => {
  if (sourceType === 'MRP') {
    return 'MRP 转单'
  }
  if (sourceType === 'MANUAL') {
    return '手工下单'
  }
  return sourceType || '未知来源'
}

const resolveSourceTypeTagType = (sourceType?: string): 'warning' | 'info' => {
  return sourceType === 'MRP' ? 'warning' : 'info'
}

const getProgressPercent = (
  current?: number | string | null,
  totalValue?: number | string | null
) => {
  const base = normalizeNumber(totalValue)
  if (base <= 0) {
    return 0
  }
  return Math.min(100, Math.round((normalizeNumber(current) / base) * 100))
}

const getReturnPercent = (
  current?: number | string | null,
  inbound?: number | string | null,
  totalValue?: number | string | null
) => {
  const base = normalizeNumber(inbound) || normalizeNumber(totalValue)
  return getProgressPercent(current, base)
}

const resolveInboundProgressColor = (row: PurchaseOrderVO) => {
  const percent = getProgressPercent(row.inCount, row.totalCount)
  if (percent >= 100) {
    return 'var(--erp-success-600)'
  }
  if (percent > 0) {
    return 'var(--erp-primary-600)'
  }
  return 'var(--erp-slate-400)'
}

const isActionCancelled = (error: unknown) =>
  error === 'cancel' || error === 'close' || (error as { type?: string })?.type === 'cancel'

const resolveInboundStatus = (row: PurchaseOrderVO) => {
  return getInboundStatusDescriptor({
    totalCount: row.totalCount,
    inCount: row.inCount,
    hasPendingPurchaseIn: row.hasPendingPurchaseIn,
    pendingPurchaseInId: row.pendingPurchaseInId
  })
}

const resolveReturnText = (row: PurchaseOrderVO) => {
  const base = normalizeNumber(row.inCount) || normalizeNumber(row.totalCount)
  const returnCount = normalizeNumber(row.returnCount)
  if (returnCount <= 0) {
    return '未退货'
  }
  if (returnCount >= base && base > 0) {
    return '已全部退货'
  }
  return '部分退货'
}

const resolveInboundResultTagType = (row: PurchaseOrderVO): 'info' | 'warning' | 'success' => {
  const totalCount = normalizeNumber(row.totalCount)
  const inCount = normalizeNumber(row.inCount)
  if (inCount <= 0 || totalCount <= 0) {
    return 'info'
  }
  if (inCount >= totalCount) {
    return 'success'
  }
  return 'warning'
}

const resolveReturnTagType = (row: PurchaseOrderVO): 'info' | 'warning' | 'danger' => {
  const base = normalizeNumber(row.inCount) || normalizeNumber(row.totalCount)
  const returnCount = normalizeNumber(row.returnCount)
  if (returnCount <= 0 || base <= 0) {
    return 'info'
  }
  if (returnCount >= base) {
    return 'danger'
  }
  return 'warning'
}

const resolveRowActionState = (row: PurchaseOrderVO) => {
  return getPurchaseOrderRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value,
    isSaleTraceMode: isSaleTraceMode.value,
    totalCount: row.totalCount,
    inCount: row.inCount,
    hasPendingPurchaseIn: row.hasPendingPurchaseIn,
    pendingPurchaseInId: row.pendingPurchaseInId
  })
}

const canEdit = (row: PurchaseOrderVO) => resolveRowActionState(row).canEdit
const canBatchEditRow = (row: PurchaseOrderVO) => resolveRowActionState(row).canBatchEdit
const canSubmit = (row: PurchaseOrderVO) => resolveRowActionState(row).canSubmit
const canCancelApproval = (row: PurchaseOrderVO) => resolveRowActionState(row).canCancelApproval
const canViewProcess = (row: PurchaseOrderVO) => resolveRowActionState(row).canViewProcess
const resolvePurchaseInAction = (row: PurchaseOrderVO) =>
  resolveRowActionState(row).purchaseInAction
const canDelete = (row: PurchaseOrderVO) => resolveRowActionState(row).canDelete
const canSelectRow = (row: PurchaseOrderVO) => canBatchEditRow(row)

const batchEditableSelectionIds = computed(() =>
  selectionList.value.filter((item) => canBatchEditRow(item)).map((item) => item.id)
)

const deletableSelectionIds = computed(() =>
  selectionList.value.filter((item) => canDelete(item)).map((item) => item.id)
)

const baseToolbarState = computed(() =>
  getPurchaseOrderToolbarDescriptor({
    isSaleTraceMode: isSaleTraceMode.value,
    batchEditableSelectionCount: batchEditableSelectionIds.value.length,
    deletableSelectionCount: deletableSelectionIds.value.length
  })
)

const toolbarState = computed(() => ({
  ...baseToolbarState.value,
  disableBatchEdit: baseToolbarState.value.disableBatchEdit || loading.value,
  disableBatchDelete: baseToolbarState.value.disableBatchDelete || deletingIds.value.length > 0
}))

const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isCancelingApproval = (id?: number) => !!id && cancelApprovalIds.value.includes(id)
const canViewAuditLog = (row: PurchaseOrderVO) =>
  row.status === PURCHASE_ORDER_STATUS.APPROVE ||
  row.status === PURCHASE_ORDER_STATUS.REJECT ||
  !!row.processInstanceId ||
  !!row.lastRejectReason

const getInlineActionDescriptors = (row: PurchaseOrderVO): PurchaseOrderActionDescriptor[] => {
  const actions: PurchaseOrderActionDescriptor[] = []
  if (canQueryPurchaseOrder) {
    actions.push({
      key: 'detail',
      label: '详情',
      type: 'primary'
    })
  }
  if (canUpdatePurchaseOrder && canEdit(row)) {
    actions.push({
      key: 'edit',
      label: '编辑'
    })
  }
  if (canSubmitPurchaseOrder && canSubmit(row)) {
    actions.push({
      key: 'submit',
      label: row.status === PURCHASE_ORDER_STATUS.REJECT ? '重新提交审批' : '提交审批'
    })
  }
  if (canCancelPurchaseOrderApproval && canCancelApproval(row)) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      type: 'warning',
      disabled: isCancelingApproval(row.id),
      loading: isCancelingApproval(row.id)
    })
  }
  if (canAccessPurchaseIn && resolvePurchaseInAction(row).visible) {
    actions.push({
      key: 'purchaseIn',
      label: resolvePurchaseInAction(row).label,
      type: 'success'
    })
  }
  if (canQueryPurchaseOrder && canViewProcess(row)) {
    actions.push({
      key: 'processDetail',
      label: '查看审批'
    })
  }
  if (canQueryPurchaseOrder && canViewAuditLog(row) && actions.length > 0 && actions.length < 3) {
    actions.push({
      key: 'auditLog',
      label: '审批历史'
    })
  }
  return actions.slice(0, 3)
}

const getOverflowActionDescriptors = (row: PurchaseOrderVO): PurchaseOrderActionDescriptor[] => {
  const inlineActionKeys = new Set(getInlineActionDescriptors(row).map((item) => item.key))
  const actions: PurchaseOrderActionDescriptor[] = []
  if (canUpdatePurchaseOrder && canEdit(row) && !inlineActionKeys.has('edit')) {
    actions.push({ key: 'edit', label: '编辑' })
  }
  if (canSubmitPurchaseOrder && canSubmit(row) && !inlineActionKeys.has('submit')) {
    actions.push({
      key: 'submit',
      label: row.status === PURCHASE_ORDER_STATUS.REJECT ? '重新提交审批' : '提交审批'
    })
  }
  if (
    canCancelPurchaseOrderApproval &&
    canCancelApproval(row) &&
    !inlineActionKeys.has('cancelApproval')
  ) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      disabled: isCancelingApproval(row.id)
    })
  }
  if (
    canQueryPurchaseOrder &&
    canViewAuditLog(row) &&
    actions.length > 0 &&
    !inlineActionKeys.has('auditLog')
  ) {
    actions.push({ key: 'auditLog', label: '审批历史' })
  }
  if (canQueryPurchaseOrder && canViewProcess(row) && !inlineActionKeys.has('processDetail')) {
    actions.push({ key: 'processDetail', label: '查看审批' })
  }
  if (
    canAccessPurchaseIn &&
    resolvePurchaseInAction(row).visible &&
    !inlineActionKeys.has('purchaseIn')
  ) {
    actions.push({
      key: 'purchaseIn',
      label: resolvePurchaseInAction(row).label
    })
  }
  if (canDeletePurchaseOrder && canDelete(row)) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: isDeletingRow(row.id),
      danger: true
    })
  }
  return actions
}

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}

const setIdsLoading = (source: Ref<number[]>, ids: number[], loadingState: boolean) => {
  if (loadingState) {
    source.value = Array.from(new Set([...source.value, ...ids]))
    return
  }
  source.value = source.value.filter((item) => !ids.includes(item))
}

const getList = async () => {
  if (shouldBlockSaleTraceByPermission.value) {
    loading.value = false
    listLoadFailed.value = false
    list.value = []
    total.value = 0
    selectionList.value = []
    return
  }
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await PurchaseOrderApi.getPurchaseOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectionList.value = []
  } catch {
    list.value = []
    total.value = 0
    selectionList.value = []
    listLoadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFilterOptions = async () => {
  const results = await Promise.allSettled([
    ProductApi.getProductSimpleList(),
    AccountApi.getAccountSimpleList(),
    SupplierApi.getSupplierSimpleList(),
    UserApi.getSimpleUserList()
  ])
  productList.value = results[0].status === 'fulfilled' ? results[0].value : []
  accountList.value = results[1].status === 'fulfilled' ? results[1].value : []
  supplierList.value = results[2].status === 'fulfilled' ? results[2].value : []
  userList.value = results[3].status === 'fulfilled' ? results[3].value : []
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  advancedSearchVisible.value = false
  if (traceOrderId.value) {
    queryParams.sourceOrderId = traceOrderId.value
  }
  handleQuery()
}

const handleRetryList = () => {
  getList()
}

const flushPendingFormOpen = () => {
  if (!formRef.value || !pendingFormOpen.value) {
    return
  }
  const { type, id } = pendingFormOpen.value
  pendingFormOpen.value = null
  formRef.value.open(type, id)
}

watch(
  () => formRef.value,
  () => {
    flushPendingFormOpen()
  }
)

const openForm = async (type: string, id?: number) => {
  if (formRef.value) {
    formRef.value.open(type, id)
    return
  }
  pendingFormOpen.value = { type, id }
  await nextTick()
  flushPendingFormOpen()
}

const openAuditLogDialog = (id: number) => {
  auditLogDialogRef.value.open(id)
}

const openSubmitDialog = (row: PurchaseOrderVO) => {
  submitDialogRef.value?.open(row)
}

const openPurchaseInList = (row: PurchaseOrderVO) => {
  push({
    path: '/scm/inbound',
    query: {
      orderNo: row.no,
      purchaseOrderId: row.id ? String(row.id) : undefined,
      from: 'purchase-order'
    }
  })
}

const openPendingPurchaseIn = (row: PurchaseOrderVO) => {
  if (row.pendingPurchaseInId) {
    push({
      path: '/scm/inbound',
      query: {
        orderNo: row.no,
        purchaseOrderId: row.id ? String(row.id) : undefined,
        openId: String(row.pendingPurchaseInId),
        openType: 'detail',
        from: 'purchase-order'
      }
    })
    return
  }
  openPurchaseInList(row)
}

const openPurchaseInForm = (row: PurchaseOrderVO) => {
  push({
    path: '/scm/inbound',
    query: {
      orderNo: row.no,
      purchaseOrderId: row.id ? String(row.id) : undefined,
      openType: 'create',
      from: 'purchase-order'
    }
  })
}

const openPurchaseOrderTodoTask = () => {
  push({
    path: '/bpm/task/todo',
    query: {
      processDefinitionKey: PURCHASE_ORDER_BPM_PROCESS_KEY
    }
  })
}

const openBatchEditDrawer = () => {
  if (!batchEditableSelectionIds.value.length) {
    message.warning('请先选择可批量编辑的采购订单')
    return
  }
  const rows = selectionList.value.filter((item) => canBatchEditRow(item))
  batchEditDrawerRef.value?.open({
    ids: batchEditableSelectionIds.value,
    rows
  })
}

const handleBatchEditSuccess = async (_result: PurchaseOrderBatchUpdateResultVO) => {
  await getList()
  selectionList.value = []
}

const openDetailDialog = (row: PurchaseOrderVO) => {
  detailDialogRef.value?.open(row)
}

const handleDetailDialogEdit = (id: number) => {
  openForm('update', id)
}

const openFormByRouteQuery = async () => {
  const openId = Number(route.query.openId)
  const openType = typeof route.query.openType === 'string' ? route.query.openType : 'detail'
  if (!openId || Number.isNaN(openId)) {
    return
  }
  if (openType === 'detail') {
    detailDialogRef.value?.open({ id: openId } as PurchaseOrderVO)
  } else {
    openForm(openType, openId)
  }
  const nextQuery = { ...route.query }
  delete nextQuery.openId
  delete nextQuery.openType
  delete nextQuery.from
  await replace({ path: route.path, query: nextQuery })
}

const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}

const syncRouteContextFromQuery = async () => {
  const routeContext = resolvePurchaseOrderRouteContext({
    sourceOrderId: route.query.sourceOrderId,
    sourceOrderNo: route.query.sourceOrderNo,
    purchaseOrderNo: route.query.purchaseOrderNo,
    from: route.query.from
  })
  traceOrderId.value = routeContext.traceOrderId
  traceOrderNo.value = routeContext.traceOrderNo
  queryParams.no = routeContext.purchaseOrderNo || undefined
  queryParams.sourceOrderId = routeContext.traceOrderId
  queryParams.pageNo = 1
  await getList()
}

const clearTraceFilter = async () => {
  const nextQuery = { ...route.query }
  delete nextQuery.sourceOrderId
  delete nextQuery.sourceOrderNo
  delete nextQuery.purchaseOrderNo
  delete nextQuery.traceFrom
  await replace({
    path: route.path,
    query: nextQuery
  })
}

const goBackFromUnauthorizedState = async () => {
  if (route.query.traceFrom === 'closure-workbench') {
    await push({ path: '/erp/sale/order/closure-workbench' })
    return
  }
  if (route.query.traceFrom === 'mrp-trace') {
    await push({
      name: 'ErpSaleOrderMrpTracePage',
      query: {
        sourceOrderId: traceOrderId.value ? String(traceOrderId.value) : undefined,
        sourceOrderNo: traceOrderNo.value || undefined
      }
    })
    return
  }
  await push({
    path: '/erp/sale/order',
    query: {
      openId: traceOrderId.value ? String(traceOrderId.value) : undefined,
      openType: traceOrderId.value ? 'detail' : undefined
    }
  })
}

const handleDelete = async (ids: number[]) => {
  const executableIds = Array.from(new Set(ids.filter((id) => !deletingIds.value.includes(id))))
  if (!executableIds.length) {
    return
  }
  try {
    await message.delConfirm()
    setIdsLoading(deletingIds, executableIds, true)
    await PurchaseOrderApi.deletePurchaseOrder(executableIds)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !executableIds.includes(item.id))
  } catch (error) {
    if (isActionCancelled(error)) {
      return
    }
    message.error('删除采购订单失败')
  } finally {
    setIdsLoading(deletingIds, executableIds, false)
  }
}

const handleCancelApproval = async (row: PurchaseOrderVO) => {
  if (!row.id || cancelApprovalIds.value.includes(row.id)) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    setIdsLoading(cancelApprovalIds, [row.id], true)
    await PurchaseOrderApi.cancelPurchaseOrderApproval({
      id: row.id,
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch (error) {
    if (isActionCancelled(error)) {
      return
    }
    message.error('撤回审批失败')
  } finally {
    setIdsLoading(cancelApprovalIds, [row.id], false)
  }
}

const handleProcessDetail = (row: PurchaseOrderVO) => {
  if (!row.processInstanceId) {
    message.warning('当前采购订单暂无审批流程')
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId
    }
  })
}

const handleCommand = (command: string, row: PurchaseOrderVO) => {
  switch (command) {
    case 'detail':
      openDetailDialog(row)
      break
    case 'edit':
      openForm('update', row.id)
      break
    case 'submit':
      openSubmitDialog(row)
      break
    case 'cancelApproval':
      handleCancelApproval(row)
      break
    case 'auditLog':
      openAuditLogDialog(row.id)
      break
    case 'processDetail':
      handleProcessDetail(row)
      break
    case 'purchaseIn': {
      const actionDescriptor = resolvePurchaseInAction(row)
      if (!actionDescriptor.visible) {
        return
      }
      if (actionDescriptor.action === 'create') {
        openPurchaseInForm(row)
        return
      }
      openPendingPurchaseIn(row)
      return
    }
    case 'delete':
      handleDelete([row.id])
      break
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await PurchaseOrderApi.exportPurchaseOrder(queryParams)
    download.excel(data, '采购订单.xls')
  } catch (error) {
    if (!isActionCancelled(error)) {
      message.error('导出采购订单失败')
    }
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: PurchaseOrderVO[]) => {
  selectionList.value = rows
}

const toggleSelection = (row: PurchaseOrderVO, checked: unknown) => {
  if (row.id === undefined || !canSelectRow(row)) {
    return
  }
  if (checked === true) {
    if (!selectedIdSet.value.has(row.id)) {
      selectionList.value = [...selectionList.value, row]
    }
    return
  }
  selectionList.value = selectionList.value.filter((item) => item.id !== row.id)
}

const initPage = async () => {
  if (shouldBlockSaleTraceByPermission.value) {
    await syncRouteContextFromQuery()
    return
  }
  await Promise.allSettled([loadFilterOptions(), syncRouteContextFromQuery()])
}

onMounted(async () => {
  await initPage()
  await openFormByRouteQuery()
})

watch(
  () => [route.query.sourceOrderId, route.query.sourceOrderNo, route.query.purchaseOrderNo],
  async () => {
    await syncRouteContextFromQuery()
  }
)

watch(
  () => [route.query.openId, route.query.openType],
  async () => {
    await openFormByRouteQuery()
  }
)
</script>

<style scoped lang="scss">
.purchase-order-page__trace-card,
.purchase-order-page__permission-card,
.purchase-order-page__filter-card,
.purchase-order-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  box-shadow: var(--erp-shadow-md);
}

.purchase-order-trace {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.purchase-order-trace__text {
  color: var(--erp-slate-600);
  font-size: 14px;
  line-height: 22px;
}

.purchase-order-page__title {
  margin-bottom: 18px;
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.purchase-order-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: var(--erp-slate-600);
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper),
  :deep(.el-input-number) {
    width: 100%;
  }

  :deep(.el-input-number .el-input__wrapper) {
    min-height: 42px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper),
  :deep(.el-input-number .el-input__wrapper) {
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid var(--erp-slate-200);
  background: linear-gradient(180deg, var(--erp-surface-white), rgba(248, 250, 252, 0.98));
  }
}

.purchase-order-query__grid {
  display: grid;
  gap: 18px 16px;
}

.purchase-order-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-order-query__grid--advanced {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-order-query__footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.purchase-order-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.purchase-order-query__filter-count {
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

.purchase-order-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
}

.purchase-order-toolbar__actions,
.purchase-order-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.purchase-order-ledger {
  :deep(.el-table__header-wrapper th) {
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), var(--erp-surface-white));
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
.ledger-progress,
.ledger-status {
  display: flex;
  flex-direction: column;
}

.ledger-order {
  gap: 6px;
}

.ledger-order__top {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.ledger-order__no {
  color: var(--erp-slate-900);
  font-size: 15px;
  font-weight: 800;
  line-height: 22px;
  letter-spacing: 0.01em;
  word-break: break-all;
}

.ledger-order__meta,
.ledger-party__meta,
.ledger-finance__meta,
.ledger-finance__sub,
.ledger-status__reject {
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.ledger-party {
  gap: 8px;
}

.ledger-party__supplier {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
}

.ledger-party__meta {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.ledger-party__tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--erp-slate-200);
  color: var(--erp-slate-600);
  font-size: 11px;
  font-weight: 700;
}

.ledger-party__text {
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-product {
  display: -webkit-box;
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-progress {
  gap: 12px;
}

.ledger-progress__section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ledger-progress__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--erp-slate-600);
  font-size: 11px;
  line-height: 17px;
}

.ledger-progress__top strong {
  color: var(--erp-slate-900);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.ledger-finance {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.ledger-finance__amount {
  color: var(--erp-slate-900);
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.ledger-finance__meta {
  color: var(--erp-success-600);
  font-weight: 600;
  text-align: right;
}

.ledger-finance__sub {
  text-align: right;
}

.ledger-status {
  gap: 8px;
}

.ledger-status__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ledger-status__reject {
  max-width: 100%;
  color: var(--erp-danger-600);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ledger-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.ledger-actions__more {
  width: 28px;
  height: 28px;
  padding: 0;
  border-radius: 999px;
}

.purchase-order-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.purchase-order-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.purchase-order-empty__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.purchase-order-empty__desc {
  max-width: 420px;
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 22px;
  text-align: center;
}

.purchase-order-empty__actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.purchase-order-empty--error .purchase-order-empty__icon {
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.12), rgba(251, 191, 36, 0.08));
  color: var(--erp-danger-600);
}

.purchase-order-empty--permission .purchase-order-empty__icon {
  background: linear-gradient(180deg, rgba(251, 191, 36, 0.14), rgba(59, 130, 246, 0.1));
  color: var(--erp-warning-600);
}

.purchase-order-mobile-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.purchase-order-mobile-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  padding: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96)),
    linear-gradient(135deg, rgba(37, 99, 235, 0.03), rgba(20, 184, 166, 0.04));
  box-shadow: var(--erp-shadow-md);
}

.purchase-order-mobile-card__head,
.purchase-order-mobile-card__meta,
.purchase-order-mobile-card__detail,
.purchase-order-mobile-card__metric-top,
.purchase-order-mobile-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.purchase-order-mobile-card__head {
  align-items: flex-start;
}

.purchase-order-mobile-card__identity,
.purchase-order-mobile-card__party {
  min-width: 0;
  flex: 1;
}

.purchase-order-mobile-card__no {
  color: var(--erp-slate-900);
  font-size: 17px;
  font-weight: 800;
  line-height: 24px;
  word-break: break-all;
}

.purchase-order-mobile-card__meta,
.purchase-order-mobile-card__finance {
  color: var(--erp-slate-500);
  font-size: 12px;
  line-height: 18px;
}

.purchase-order-mobile-card__status {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.purchase-order-mobile-card__summary {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 0.85fr);
  gap: 14px;
  margin-top: 14px;
}

.purchase-order-mobile-card__supplier,
.purchase-order-mobile-card__product {
  color: var(--erp-slate-900);
  font-size: 15px;
  font-weight: 700;
  line-height: 22px;
}

.purchase-order-mobile-card__detail {
  margin-top: 8px;
  color: var(--erp-slate-600);
  font-size: 12px;
  line-height: 18px;
}

.purchase-order-mobile-card__detail-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--erp-slate-200);
  color: var(--erp-slate-600);
  font-size: 11px;
  font-weight: 700;
}

.purchase-order-mobile-card__finance {
  align-items: flex-end;
  text-align: right;
}

.purchase-order-mobile-card__finance-label {
  color: var(--erp-slate-500);
}

.purchase-order-mobile-card__finance-value {
  color: var(--erp-slate-900);
  font-size: 24px;
  line-height: 1.05;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.purchase-order-mobile-card__product {
  margin-top: 14px;
}

.purchase-order-mobile-card__metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.purchase-order-mobile-card__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(226, 232, 240, 0.86);
}

.purchase-order-mobile-card__metric-top {
  justify-content: space-between;
  color: var(--erp-slate-600);
  font-size: 12px;
  line-height: 18px;
}

.purchase-order-mobile-card__metric-top strong {
  color: var(--erp-slate-900);
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.purchase-order-mobile-card__reject {
  margin-top: 12px;
  color: var(--erp-danger-600);
  font-size: 12px;
  line-height: 18px;
}

.purchase-order-mobile-card__actions {
  justify-content: flex-start;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.86);
}

.purchase-order-mobile-card__more {
  width: 30px;
  height: 30px;
  padding: 0;
  border-radius: 999px;
}

.purchase-order-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
}

.purchase-order-page__record-count {
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 20px;
}

.purchase-order-query-collapse-enter-active,
.purchase-order-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.purchase-order-query-collapse-enter-from,
.purchase-order-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1359px) {
  .purchase-order-query__grid--primary,
  .purchase-order-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .purchase-order-page__title {
    font-size: 24px;
  }

  .ledger-finance__amount {
    font-size: 18px;
  }
}

@media (max-width: 1179px) {
  .purchase-order-mobile-card__summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-order-mobile-card__finance {
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 1023px) {
  .purchase-order-trace,
  .purchase-order-query__grid--primary,
  .purchase-order-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-order-trace,
  .purchase-order-query__footer,
  .purchase-order-toolbar,
  .purchase-order-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-order-query__actions,
  .purchase-order-toolbar__actions,
  .purchase-order-toolbar__meta {
    width: 100%;
  }

  .purchase-order-query__actions :deep(.el-button),
  .purchase-order-toolbar__actions :deep(.el-button),
  .purchase-order-toolbar__meta :deep(.el-button) {
    flex: 1;
  }

  .purchase-order-mobile-card {
    padding: 14px;
  }

  .purchase-order-mobile-card__metrics {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 767px) {
  .purchase-order-mobile-card__status,
  .purchase-order-mobile-card__actions {
    justify-content: flex-start;
  }

  .purchase-order-mobile-card__finance-value {
    font-size: 22px;
  }
}
</style>
