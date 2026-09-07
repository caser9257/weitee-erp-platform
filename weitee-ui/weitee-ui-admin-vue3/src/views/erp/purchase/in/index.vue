<template>
  <ContentWrap class="purchase-in-page__filter-card">
    <div class="purchase-in-page__title">采购入库台账</div>
    <div class="purchase-in-main-control">
      <div class="purchase-in-main-control__head">
        <div class="purchase-in-main-control__title">供应链主控</div>
        <div class="purchase-in-main-control__current">
          当前阶段
          <span>{{ currentMainControlStage.label }}</span>
        </div>
      </div>
      <div class="purchase-in-main-control__stages">
        <button
          v-for="stage in PURCHASE_IN_MAIN_CONTROL_STAGES"
          :key="stage.key"
          type="button"
          class="purchase-in-main-control__stage"
          :class="[
            `purchase-in-main-control__stage--${stage.tone}`,
            { 'is-active': mainControlStage === stage.key }
          ]"
          :disabled="loading"
          @click="handleMainControlStageChange(stage.key)"
        >
          {{ stage.label }}
        </button>
      </div>
    </div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="purchase-in-query">
      <div class="purchase-in-query__grid purchase-in-query__grid--primary">
        <el-form-item label="入库单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入入库单号"
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
        <el-form-item label="入库时间" prop="inTime">
          <el-date-picker
            v-model="queryParams.inTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
      </div>

      <transition name="purchase-in-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="purchase-in-query__grid purchase-in-query__grid--advanced"
        >
          <el-form-item label="产品" prop="productId">
            <ProductRemoteSelect v-model="queryParams.productId" placeholder="请选择产品" />
          </el-form-item>
          <el-form-item label="仓库" prop="warehouseId">
            <el-select
              v-model="queryParams.warehouseId"
              clearable
              filterable
              placeholder="请选择仓库"
            >
              <el-option
                v-for="item in warehouseList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
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
          <el-form-item label="结算账户" prop="accountId">
            <el-select
              v-model="queryParams.accountId"
              clearable
              filterable
              placeholder="请选择结算账户"
            >
              <el-option
                v-for="item in accountList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="付款状态" prop="paymentStatus">
            <el-select v-model="queryParams.paymentStatus" placeholder="请选择付款状态" clearable>
              <el-option label="未付款" value="0" />
              <el-option label="部分付款" value="1" />
              <el-option label="全部付款" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择审核状态" clearable>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="质检状态" prop="qaStatus">
            <el-select v-model="queryParams.qaStatus" placeholder="请选择质检状态" clearable>
              <el-option
                v-for="item in QA_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
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

      <div class="purchase-in-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
          <span v-if="advancedFilterCount" class="purchase-in-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="purchase-in-query__actions">
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

  <ContentWrap class="purchase-in-page__list-card">
    <div class="purchase-in-toolbar">
      <div class="purchase-in-toolbar__actions">
        <el-button
          v-if="toolbarState.showCreate"
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:purchase-in:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增入库
        </el-button>
        <el-button
          v-if="toolbarState.showExport"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:purchase-in:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出数据
        </el-button>
        <el-button
          v-if="toolbarState.showBatchEdit"
          plain
          :disabled="toolbarState.disableBatchEdit"
          @click="openBatchEditDrawer()"
          v-hasPermi="['erp:purchase-in:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
        </el-button>
        <el-button
          v-if="toolbarState.showTodo"
          plain
          @click="openPurchaseInTodoTask"
          v-hasPermi="['bpm:task:query']"
        >
          <Icon icon="ep:promotion" class="mr-5px" /> 审批待办
        </el-button>
      </div>
      <div class="purchase-in-toolbar__meta">
        <el-button
          v-if="toolbarState.showBatchDelete"
          plain
          type="danger"
          :disabled="toolbarState.disableBatchDelete"
          @click="handleDelete(deletableSelectionIds)"
          v-hasPermi="['erp:purchase-in:delete']"
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
      class="purchase-in-ledger"
      @selection-change="handleSelectionChange"
    >
      <template #empty>
        <div v-if="listLoadFailed" class="purchase-in-empty purchase-in-empty--error">
          <div class="purchase-in-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="purchase-in-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
            重试加载
          </el-button>
        </div>
        <div v-else class="purchase-in-empty">
          <div class="purchase-in-empty__icon">
            <Icon icon="ep:box" />
          </div>
          <div class="purchase-in-empty__title">暂无采购入库记录</div>
        </div>
      </template>
      <el-table-column width="36" type="selection" :selectable="selectableRow" />
      <el-table-column label="入库信息" min-width="164">
        <template #default="{ row }">
          <div class="ledger-order">
            <div class="ledger-order__no">{{ row.no || '-' }}</div>
            <div class="ledger-order__meta">入库 {{ formatDateValue(row.inTime) }}</div>
            <div class="ledger-order__meta">创建 {{ formatDateValue(row.createTime) }}</div>
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
              <span class="ledger-party__text">{{ resolveWarehouseName(row) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品摘要" min-width="108">
        <template #default="{ row }">
          <div class="ledger-product">
            <div class="ledger-product__name" :title="row.productNames || '-'">
              {{ row.productNames || '-' }}
            </div>
            <div class="ledger-product__batch" :title="resolvePurchaseSourceBatchSummary(row)">
              来源批次 {{ resolvePurchaseSourceBatchSummary(row) }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额结算（元）" min-width="124" align="right">
        <template #default="{ row }">
          <div class="ledger-finance">
            <div class="ledger-finance__amount">{{ formatCurrency(row.totalPrice) }}</div>
            <div class="ledger-finance__meta">已付 {{ formatCurrency(row.paymentPrice) }}</div>
            <div class="ledger-finance__sub">未付 {{ formatCurrency(getRemainingPay(row)) }}</div>
            <div class="ledger-finance__sub">{{ getPaymentStatusLabel(row) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="质检 / 入库" min-width="164">
        <template #default="{ row }">
          <div class="ledger-progress">
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>质检合格</span>
                <strong
                  >{{ formatCount(row.qaPassCount) }} / {{ formatCount(row.totalCount) }}</strong
                >
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.qaPassCount, row.totalCount)"
                :color="resolveQaProgressColor(row)"
              />
            </div>
            <div class="ledger-progress__section">
              <div class="ledger-progress__top">
                <span>执行入库</span>
                <strong>
                  {{ formatCount(row.stockInCount) }} / {{ formatCount(resolveStockInBase(row)) }}
                </strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.stockInCount, resolveStockInBase(row))"
                :color="resolveStockInProgressColor(row)"
              />
            </div>
            <div class="ledger-progress__summary">
              <span>不合格 {{ formatCount(row.qaRejectCount) }}</span>
              <span>剩余待入库 {{ formatCount(row.remainingStockInCount) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="108">
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
              <el-tag size="small" effect="light" :type="getQaStatusTagType(row.qaStatus)">
                {{ getQaStatusLabel(row.qaStatus) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="getStockInStatus(row).tagType">
                {{ getStockInStatus(row).label }}
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
              trigger="click"
              @command="(command) => handleCommand(command, row)"
            >
              <el-button link type="primary" class="ledger-actions__more" title="更多操作">
                <Icon icon="ep:more-filled" />
              </el-button>
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
      class="purchase-in-mobile-list"
      element-loading-background="rgba(248, 250, 252, 0.78)"
    >
      <template v-if="list.length">
        <article v-for="row in list" :key="row.id" class="purchase-in-mobile-card">
          <div class="purchase-in-mobile-card__head">
            <el-checkbox
              :model-value="selectedIdSet.has(row.id)"
              :disabled="!selectableRow(row)"
              @change="(checked) => toggleSelection(row, checked)"
            />
            <div class="purchase-in-mobile-card__identity">
              <div class="purchase-in-mobile-card__no">{{ row.no || '-' }}</div>
              <div class="purchase-in-mobile-card__meta">
                <span>入库 {{ formatDateValue(row.inTime) }}</span>
                <span>创建 {{ formatDateValue(row.createTime) }}</span>
                <span>创建人 {{ row.creatorName || '-' }}</span>
              </div>
            </div>
            <div class="purchase-in-mobile-card__status">
              <el-tag
                size="small"
                effect="light"
                :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
              >
                {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="getQaStatusTagType(row.qaStatus)">
                {{ getQaStatusLabel(row.qaStatus) }}
              </el-tag>
              <el-tag size="small" effect="light" :type="getStockInStatus(row).tagType">
                {{ getStockInStatus(row).label }}
              </el-tag>
            </div>
          </div>

          <div class="purchase-in-mobile-card__summary">
            <div class="purchase-in-mobile-card__party">
              <div class="purchase-in-mobile-card__supplier">{{ row.supplierName || '-' }}</div>
              <div class="purchase-in-mobile-card__detail">
                <span class="purchase-in-mobile-card__detail-tag">关联订单</span>
                <span>{{ row.orderNo || '-' }}</span>
              </div>
              <div class="purchase-in-mobile-card__detail">
                <span class="purchase-in-mobile-card__detail-tag">仓库</span>
                <span>{{ resolveWarehouseName(row) }}</span>
              </div>
            </div>
            <div class="purchase-in-mobile-card__finance">
              <span class="purchase-in-mobile-card__finance-label">结算金额</span>
              <strong class="purchase-in-mobile-card__finance-value">
                {{ formatCurrency(row.totalPrice) }}
              </strong>
              <span>已付 {{ formatCurrency(row.paymentPrice) }}</span>
              <span>未付 {{ formatCurrency(getRemainingPay(row)) }}</span>
              <span>{{ getPaymentStatusLabel(row) }}</span>
            </div>
          </div>

          <div class="purchase-in-mobile-card__product">{{ row.productNames || '-' }}</div>
          <div class="purchase-in-mobile-card__source-batch">
            <span class="purchase-in-mobile-card__source-batch-tag">来源批次</span>
            <span>{{ resolvePurchaseSourceBatchSummary(row) }}</span>
          </div>

          <div class="purchase-in-mobile-card__metrics">
            <div class="purchase-in-mobile-card__metric">
              <div class="purchase-in-mobile-card__metric-top">
                <span>质检合格</span>
                <strong
                  >{{ formatCount(row.qaPassCount) }} / {{ formatCount(row.totalCount) }}</strong
                >
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.qaPassCount, row.totalCount)"
                :color="resolveQaProgressColor(row)"
              />
            </div>
            <div class="purchase-in-mobile-card__metric">
              <div class="purchase-in-mobile-card__metric-top">
                <span>执行入库</span>
                <strong>
                  {{ formatCount(row.stockInCount) }} / {{ formatCount(resolveStockInBase(row)) }}
                </strong>
              </div>
              <el-progress
                :stroke-width="6"
                :show-text="false"
                :percentage="getProgressPercent(row.stockInCount, resolveStockInBase(row))"
                :color="resolveStockInProgressColor(row)"
              />
            </div>
          </div>

          <div class="purchase-in-mobile-card__summary-bar">
            <span>不合格 {{ formatCount(row.qaRejectCount) }}</span>
            <span>剩余待入库 {{ formatCount(row.remainingStockInCount) }}</span>
          </div>

          <div v-if="row.lastRejectReason" class="purchase-in-mobile-card__reject">
            {{ row.lastRejectReason }}
          </div>

          <div class="purchase-in-mobile-card__actions">
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
              trigger="click"
              @command="(command) => handleCommand(command, row)"
            >
              <el-button link type="primary" class="purchase-in-mobile-card__more" title="更多操作">
                <Icon icon="ep:more-filled" />
              </el-button>
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
      <div v-else-if="listLoadFailed" class="purchase-in-empty purchase-in-empty--error">
        <div class="purchase-in-empty__icon">
          <Icon icon="ep:warning-filled" />
        </div>
        <div class="purchase-in-empty__title">列表加载失败</div>
        <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
          重试加载
        </el-button>
      </div>
      <div v-else class="purchase-in-empty">
        <div class="purchase-in-empty__icon">
          <Icon icon="ep:box" />
        </div>
        <div class="purchase-in-empty__title">暂无采购入库记录</div>
      </div>
    </div>

    <div class="purchase-in-page__footer">
      <div class="purchase-in-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <PurchaseInForm ref="formRef" @success="getList" />
  <PurchaseInBatchEditDrawer
    ref="batchEditDrawerRef"
    :account-options="accountList"
    @success="handleBatchEditSuccess"
  />
  <PurchaseInSubmitDialog ref="submitDialogRef" @success="getList" />
  <PurchaseInStockExecuteDialog ref="stockExecuteDialogRef" @success="getList" />
  <PurchaseInPrintDialog ref="printDialogRef" />
  <PurchaseInSourceBatchTraceDrawer ref="sourceBatchTraceDrawerRef" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { PurchaseInApi, PurchaseInVO } from '@/api/erp/purchase/in'
import PurchaseInForm from './PurchaseInForm.vue'
import PurchaseInBatchEditDrawer from './components/PurchaseInBatchEditDrawer.vue'
import PurchaseInSubmitDialog from './PurchaseInSubmitDialog.vue'
import PurchaseInStockExecuteDialog from './PurchaseInStockExecuteDialog.vue'
import PurchaseInPrintDialog from './PurchaseInPrintDialog.vue'
import PurchaseInSourceBatchTraceDrawer from './components/PurchaseInSourceBatchTraceDrawer.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { useRoute, useRouter } from 'vue-router'
import { useUserStoreWithOut } from '@/store/modules/user'
import { checkPermi } from '@/utils/permission'
import {
  getPurchaseInRowActionDescriptor,
  getPurchaseInStockStatusDescriptor,
  getPurchaseInToolbarDescriptor
} from './purchaseInStatus.helpers'
import { resolvePurchaseInRouteOpen } from './purchaseInRouteOpen.helpers'
import {
  getPurchaseInMainControlActions,
  getPurchaseInMainControlStageQuery,
  getPurchaseInPrimarySourceBatchId,
  getPurchaseInPrimaryStockContext,
  PURCHASE_IN_MAIN_CONTROL_STAGES,
  STOCK_RECORD_BIZ_TYPE,
  type PurchaseInMainControlActionKey,
  type PurchaseInMainControlStageKey
} from './purchaseInMainControl.helpers'
import { useWindowSize } from '@vueuse/core'

defineOptions({ name: 'ErpPurchaseIn' })

const PURCHASE_IN_BPM_PROCESS_KEY = 'erp_purchase_in'
const PURCHASE_IN_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const
const PURCHASE_IN_QA_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const
const QA_STATUS_OPTIONS = [
  { label: '待质检', value: PURCHASE_IN_QA_STATUS.TO_INSPECT },
  { label: '部分合格', value: PURCHASE_IN_QA_STATUS.PARTIAL },
  { label: '全部合格', value: PURCHASE_IN_QA_STATUS.PASSED },
  { label: '全部不合格', value: PURCHASE_IN_QA_STATUS.REJECTED }
]

type PurchaseInActionKey =
  | 'detail'
  | 'edit'
  | 'submit'
  | 'cancelApproval'
  | 'processDetail'
  | 'qualityDetail'
  | 'qualityCheck'
  | 'stockExecute'
  | 'viewPurchaseOrder'
  | 'viewStock'
  | 'traceBatch'
  | 'viewStockFlow'
  | 'print'
  | 'delete'

type PurchaseInActionDescriptor = {
  key: PurchaseInActionKey
  label: string
  type?: '' | 'primary' | 'success' | 'warning' | 'danger'
  disabled?: boolean
  loading?: boolean
  danger?: boolean
}

const route = useRoute()
const { push, replace } = useRouter()
const canQueryPurchaseIn = checkPermi(['erp:purchase-in:query'])
const canUpdatePurchaseIn = checkPermi(['erp:purchase-in:update'])
const canSubmitPurchaseIn = checkPermi(['erp:purchase-in:submit'])
const canCancelPurchaseInApproval = checkPermi(['erp:purchase-in:cancel-approval'])
const canDeletePurchaseIn = checkPermi(['erp:purchase-in:delete'])
const canUpdatePurchaseInStatus = checkPermi(['erp:purchase-in:update-status'])
const canQueryPurchaseInQuality = checkPermi(['erp:purchase-in-quality:query'])
const canQueryPurchaseSourceBatch = checkPermi(['erp:purchase-source-batch:query'])
const canCreatePurchaseInQuality = checkPermi(['erp:purchase-in-quality:create'])
const message = useMessage()
const { t } = useI18n()
const userStore = useUserStoreWithOut()
const { width } = useWindowSize()

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<PurchaseInVO[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const mainControlStage = ref<PurchaseInMainControlStageKey>('all')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  supplierId: undefined,
  productId: undefined,
  warehouseId: undefined,
  inTime: [],
  orderNo: undefined,
  paymentStatus: undefined,
  accountId: undefined,
  status: undefined,
  stockInStatus: undefined,
  qaStatus: undefined,
  remark: undefined,
  creator: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const deletingIds = ref<number[]>([])
const cancelApprovalIds = ref<number[]>([])
const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const userList = ref<UserVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const accountList = ref<AccountVO[]>([])
const selectionList = ref<PurchaseInVO[]>([])
const formRef = ref()
const batchEditDrawerRef = ref()
const submitDialogRef = ref()
const stockExecuteDialogRef = ref()
const printDialogRef = ref()
const sourceBatchTraceDrawerRef = ref()
const currentUserId = computed(() => String(userStore.getUser.id || ''))
const isCompactLayout = computed(() => width.value < 1180)
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const selectedIdSet = computed(() => new Set(selectionList.value.map((item) => item.id)))
const currentMainControlStage = computed(
  () =>
    PURCHASE_IN_MAIN_CONTROL_STAGES.find((stage) => stage.key === mainControlStage.value) ||
    PURCHASE_IN_MAIN_CONTROL_STAGES[0]
)

const advancedFilterCount = computed(() => {
  const fields = [
    queryParams.productId,
    queryParams.warehouseId,
    queryParams.creator,
    queryParams.accountId,
    queryParams.paymentStatus,
    queryParams.status,
    queryParams.qaStatus,
    queryParams.remark
  ]
  return fields.filter((item) => item !== undefined && item !== null && item !== '').length
})

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const resolveRowActionState = (row: PurchaseInVO) =>
  getPurchaseInRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value,
    qaStatus: row.qaStatus,
    stockInStatus: row.stockInStatus,
    remainingStockInCount: row.remainingStockInCount
  })

const canDeleteRow = (row: PurchaseInVO) => resolveRowActionState(row).canDelete
const canBatchEditRow = (row: PurchaseInVO) => resolveRowActionState(row).canEdit
const selectableRow = (row: PurchaseInVO) => canDeleteRow(row) || canBatchEditRow(row)

const deletableSelectionIds = computed(() =>
  selectionList.value.filter((item) => canDeleteRow(item)).map((item) => item.id!)
)

const batchEditSelectionRows = computed(() =>
  selectionList.value.filter((item) => canBatchEditRow(item) && item.id)
)

const baseToolbarState = computed(() =>
  getPurchaseInToolbarDescriptor({
    deletableSelectionCount: deletableSelectionIds.value.length
  })
)

const toolbarState = computed(() => ({
  ...baseToolbarState.value,
  showBatchEdit: canUpdatePurchaseIn,
  disableBatchEdit: batchEditSelectionRows.value.length <= 0 || loading.value,
  disableBatchDelete: baseToolbarState.value.disableBatchDelete || deletingIds.value.length > 0
}))

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

const getQaStatusLabel = (qaStatus?: number) => {
  return QA_STATUS_OPTIONS.find((item) => item.value === qaStatus)?.label || '待质检'
}

const getQaStatusTagType = (qaStatus?: number) => {
  if (qaStatus === PURCHASE_IN_QA_STATUS.PASSED) {
    return 'success'
  }
  if (qaStatus === PURCHASE_IN_QA_STATUS.PARTIAL) {
    return 'warning'
  }
  if (qaStatus === PURCHASE_IN_QA_STATUS.REJECTED) {
    return 'danger'
  }
  return 'info'
}

const getStockInStatus = (row: PurchaseInVO) =>
  getPurchaseInStockStatusDescriptor({
    status: row.status,
    qaStatus: row.qaStatus,
    stockInStatus: row.stockInStatus
  })

const resolveWarehouseName = (row: PurchaseInVO) => {
  const warehouseId = row.items?.[0]?.warehouseId
  if (!warehouseId) {
    return '-'
  }
  return warehouseList.value.find((item) => item.id === warehouseId)?.name || `#${warehouseId}`
}

const resolvePurchaseSourceBatchSummary = (row: PurchaseInVO) => {
  const batchNos = Array.from(
    new Set(
      (row.items || [])
        .map((item) => item.purchaseSourceBatchNo?.trim())
        .filter((item): item is string => !!item)
    )
  )
  if (!batchNos.length) {
    return '未关联来源批次'
  }
  if (batchNos.length <= 2) {
    return batchNos.join(' / ')
  }
  return `${batchNos.slice(0, 2).join(' / ')} 等 ${batchNos.length} 个来源批次`
}

const getRemainingPay = (row: PurchaseInVO) =>
  Math.max(0, normalizeNumber(row.totalPrice) - normalizeNumber(row.paymentPrice))

const getPaymentStatusLabel = (row: PurchaseInVO) => {
  const totalPrice = normalizeNumber(row.totalPrice)
  const paymentPrice = normalizeNumber(row.paymentPrice)
  if (paymentPrice <= 0 || totalPrice <= 0) {
    return '付款 未付'
  }
  if (paymentPrice >= totalPrice) {
    return '付款 已结清'
  }
  return '付款 部分支付'
}

const resolveStockInBase = (row: PurchaseInVO) => {
  return normalizeNumber(row.qaPassCount) || normalizeNumber(row.totalCount)
}

const resolveQaProgressColor = (row: PurchaseInVO) => {
  const percent = getProgressPercent(row.qaPassCount, row.totalCount)
  if (percent >= 100) {
    return '#10b981'
  }
  if (percent > 0) {
    return '#3b82f6'
  }
  return '#cbd5e1'
}

const resolveStockInProgressColor = (row: PurchaseInVO) => {
  const percent = getProgressPercent(row.stockInCount, resolveStockInBase(row))
  if (percent >= 100) {
    return '#10b981'
  }
  if (percent > 0) {
    return '#f59e0b'
  }
  return '#cbd5e1'
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

const isDeletingRow = (id?: number) => !!id && deletingIds.value.includes(id)
const isCancelingApproval = (id?: number) => !!id && cancelApprovalIds.value.includes(id)

const getInlineActionDescriptors = (row: PurchaseInVO): PurchaseInActionDescriptor[] => {
  const rowActionState = resolveRowActionState(row)
  const actions: PurchaseInActionDescriptor[] = []

  if (canQueryPurchaseIn) {
    actions.push({
      key: 'detail',
      label: '详情',
      type: 'primary'
    })
  }
  if (canUpdatePurchaseIn && rowActionState.canEdit) {
    actions.push({
      key: 'edit',
      label: '编辑'
    })
  }
  if (canSubmitPurchaseIn && rowActionState.canSubmit) {
    actions.push({
      key: 'submit',
      label: row.status === PURCHASE_IN_STATUS.REJECT ? '重新提交审批' : '提交审批'
    })
  }
  if (canCreatePurchaseInQuality && rowActionState.canQualityCheck) {
    actions.push({
      key: 'qualityCheck',
      label: '质检',
      type: 'warning'
    })
  }
  if (canUpdatePurchaseInStatus && rowActionState.canConfirmStockIn) {
    actions.push({
      key: 'stockExecute',
      label: '执行入库',
      type: 'success'
    })
  }
  if (canCancelPurchaseInApproval && rowActionState.canCancelApproval) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      type: 'warning',
      disabled: isCancelingApproval(row.id),
      loading: isCancelingApproval(row.id)
    })
  }
  if (canQueryPurchaseInQuality && rowActionState.canViewQualityDetail) {
    actions.push({
      key: 'qualityDetail',
      label: '查看质检'
    })
  }
  if (canQueryPurchaseIn && rowActionState.canViewProcess) {
    actions.push({
      key: 'processDetail',
      label: '查看审批'
    })
  }
  if (canQueryPurchaseIn && row.id) {
    actions.push({
      key: 'print',
      label: '打印'
    })
  }

  return actions.slice(0, 3)
}

const getOverflowActionDescriptors = (row: PurchaseInVO): PurchaseInActionDescriptor[] => {
  const rowActionState = resolveRowActionState(row)
  const inlineActionKeys = new Set(getInlineActionDescriptors(row).map((item) => item.key))
  const actions: PurchaseInActionDescriptor[] = []
  getPurchaseInMainControlActions(row)
    .filter((action) => {
      if (action.key === 'traceBatch' && !canQueryPurchaseSourceBatch) {
        return false
      }
      return !inlineActionKeys.has(action.key)
    })
    .forEach((action) =>
      actions.push({
        key: action.key,
        label: action.label,
        disabled: action.disabled
      })
    )

  if (canUpdatePurchaseIn && rowActionState.canEdit && !inlineActionKeys.has('edit')) {
    actions.push({ key: 'edit', label: '编辑' })
  }
  if (canSubmitPurchaseIn && rowActionState.canSubmit && !inlineActionKeys.has('submit')) {
    actions.push({
      key: 'submit',
      label: row.status === PURCHASE_IN_STATUS.REJECT ? '重新提交审批' : '提交审批'
    })
  }
  if (
    canCancelPurchaseInApproval &&
    rowActionState.canCancelApproval &&
    !inlineActionKeys.has('cancelApproval')
  ) {
    actions.push({
      key: 'cancelApproval',
      label: '撤回审批',
      disabled: isCancelingApproval(row.id)
    })
  }
  if (
    canQueryPurchaseInQuality &&
    rowActionState.canViewQualityDetail &&
    !inlineActionKeys.has('qualityDetail')
  ) {
    actions.push({ key: 'qualityDetail', label: '查看质检' })
  }
  if (
    canCreatePurchaseInQuality &&
    rowActionState.canQualityCheck &&
    !inlineActionKeys.has('qualityCheck')
  ) {
    actions.push({ key: 'qualityCheck', label: '质检' })
  }
  if (
    canUpdatePurchaseInStatus &&
    rowActionState.canConfirmStockIn &&
    !inlineActionKeys.has('stockExecute')
  ) {
    actions.push({ key: 'stockExecute', label: '执行入库' })
  }
  if (
    canQueryPurchaseIn &&
    rowActionState.canViewProcess &&
    !inlineActionKeys.has('processDetail')
  ) {
    actions.push({ key: 'processDetail', label: '查看审批' })
  }
  if (canQueryPurchaseIn && row.id && !inlineActionKeys.has('print')) {
    actions.push({ key: 'print', label: '打印' })
  }
  if (canDeletePurchaseIn && rowActionState.canDelete) {
    actions.push({
      key: 'delete',
      label: '删除',
      disabled: isDeletingRow(row.id),
      danger: true
    })
  }

  return actions
}

const openStockExecuteDialog = (row: PurchaseInVO) => {
  if (!row.id) {
    return
  }
  stockExecuteDialogRef.value?.open(row.id)
}

const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await PurchaseInApi.getPurchaseInPage(queryParams)
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
    Promise.resolve([]),
    SupplierApi.getSupplierSimpleList(),
    UserApi.getSimpleUserList(),
    WarehouseApi.getWarehouseSimpleList(),
    AccountApi.getAccountSimpleList()
  ])
  productList.value = results[0].status === 'fulfilled' ? results[0].value : []
  supplierList.value = results[1].status === 'fulfilled' ? results[1].value : []
  userList.value = results[2].status === 'fulfilled' ? results[2].value : []
  warehouseList.value = results[3].status === 'fulfilled' ? results[3].value : []
  accountList.value = results[4].status === 'fulfilled' ? results[4].value : []
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const applyMainControlStageQuery = () => {
  const stageQuery = getPurchaseInMainControlStageQuery(mainControlStage.value)
  queryParams.status = stageQuery.status
  queryParams.qaStatus = stageQuery.qaStatus
  queryParams.stockInStatus = stageQuery.stockInStatus
}

const handleMainControlStageChange = (stage: PurchaseInMainControlStageKey) => {
  if (loading.value || mainControlStage.value === stage) {
    return
  }
  mainControlStage.value = stage
  applyMainControlStageQuery()
  handleQuery()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  advancedSearchVisible.value = false
  mainControlStage.value = 'all'
  queryParams.stockInStatus = undefined
  handleQuery()
}

const handleRetryList = () => {
  getList()
}

const openForm = (type: string, id?: number, purchaseOrderId?: number) => {
  formRef.value.open(type, id, purchaseOrderId)
}

const openBatchEditDrawer = () => {
  const rows = batchEditSelectionRows.value
  if (!rows.length) {
    message.warning('请先选择可修改的采购入库单')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: rows.map((item) => item.id!),
    rows
  })
}

const openSubmitDialog = (row: PurchaseInVO) => {
  submitDialogRef.value?.open(row)
}

const openQualityCheckDialog = (row: PurchaseInVO) => {
  if (!row.id) {
    return
  }
  push({
    path: '/erp/purchase/in-quality/detail',
    query: {
      purchaseInId: row.id,
      mode: 'submit',
      from: 'purchase-in'
    }
  })
}

const openPurchaseInTodoTask = () => {
  push({
    path: '/approval/todo',
    query: {
      processDefinitionKey: PURCHASE_IN_BPM_PROCESS_KEY
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
    await PurchaseInApi.deletePurchaseIn(executableIds)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !executableIds.includes(item.id!))
  } catch {
  } finally {
    setIdsLoading(deletingIds, executableIds, false)
  }
}

const handleCancelApproval = async (row: PurchaseInVO) => {
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
    await PurchaseInApi.cancelPurchaseInApproval({
      id: row.id,
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
  } catch {
  } finally {
    setIdsLoading(cancelApprovalIds, [row.id], false)
  }
}

const handleProcessDetail = (row: PurchaseInVO) => {
  if (!row.processInstanceId) {
    message.warning('当前采购入库暂无审批流程')
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId
    }
  })
}

const handleQualityDetail = (row: PurchaseInVO) => {
  if (!row.id) {
    message.warning('当前采购入库单编号不能为空')
    return
  }
  push({
    path: '/erp/purchase/in-quality/detail',
    query: {
      purchaseInId: row.id,
      mode: 'view',
      from: 'purchase-in'
    }
  })
}

const handleQualityCheck = (row: PurchaseInVO) => {
  openQualityCheckDialog(row)
}

const handleMainControlAction = (command: PurchaseInMainControlActionKey, row: PurchaseInVO) => {
  const action = getPurchaseInMainControlActions(row).find((item) => item.key === command)
  if (!action || action.disabled) {
    if (action?.reason === 'missing-order') {
      message.warning('当前采购入库缺少关联订单')
    } else if (action?.reason === 'missing-stock-context') {
      message.warning('缺少库存上下文')
    } else if (action?.reason === 'missing-source-batch') {
      message.warning('缺少采购来源批次')
    } else {
      message.warning('缺少库存流水上下文')
    }
    return
  }

  const stockContext = getPurchaseInPrimaryStockContext(row)
  if (command === 'viewPurchaseOrder') {
    push({
      path: '/scm/purchase-order',
      query: {
        ...(row.orderId ? { openId: String(row.orderId), openType: 'detail' } : {}),
        ...(row.orderNo ? { purchaseOrderNo: row.orderNo } : {}),
        from: 'purchase-in'
      }
    })
    return
  }

  if (command === 'viewStock' && stockContext) {
    push({
      path: '/scm/stock',
      query: {
        productId: String(stockContext.productId),
        warehouseId: String(stockContext.warehouseId),
        returnFrom: 'purchase-in',
        purchaseInId: row.id ? String(row.id) : undefined
      }
    })
    return
  }

  if (command === 'traceBatch') {
    const sourceBatchId = getPurchaseInPrimarySourceBatchId(row)
    if (sourceBatchId) {
      sourceBatchTraceDrawerRef.value?.open(sourceBatchId)
    }
    return
  }

  push({
    path: '/scm/stock-analysis',
    query: {
      ...(stockContext
        ? {
            productId: String(stockContext.productId),
            warehouseId: String(stockContext.warehouseId)
          }
        : {}),
      bizNo: row.no,
      bizType: String(STOCK_RECORD_BIZ_TYPE.PURCHASE_IN),
      returnFrom: 'purchase-in',
      purchaseInId: row.id ? String(row.id) : undefined
    }
  })
}

const handleCommand = (command: string, row: PurchaseInVO) => {
  switch (command) {
    case 'detail':
      openForm('detail', row.id)
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
    case 'processDetail':
      handleProcessDetail(row)
      break
    case 'qualityDetail':
      handleQualityDetail(row)
      break
    case 'qualityCheck':
      handleQualityCheck(row)
      break
    case 'stockExecute':
      openStockExecuteDialog(row)
      break
    case 'viewPurchaseOrder':
    case 'viewStock':
    case 'traceBatch':
    case 'viewStockFlow':
      handleMainControlAction(command, row)
      break
    case 'print':
      if (row.id) {
        printDialogRef.value?.open(row.id)
      }
      break
    case 'delete':
      if (row.id) {
        handleDelete([row.id])
      }
      break
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await PurchaseInApi.exportPurchaseIn(queryParams)
    download.excel(data, '采购入库.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleBatchEditSuccess = async () => {
  await getList()
}

const handleSelectionChange = (rows: PurchaseInVO[]) => {
  selectionList.value = rows
}

const toggleSelection = (row: PurchaseInVO, checked: unknown) => {
  if (row.id === undefined || !selectableRow(row)) {
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

const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}

const syncOrderFilterFromRoute = async () => {
  queryParams.orderNo = typeof route.query.orderNo === 'string' ? route.query.orderNo : undefined
  applyMainControlStageQuery()
  queryParams.pageNo = 1
  await getList()
}

const openFormByRouteQuery = async () => {
  const routeOpen = resolvePurchaseInRouteOpen({
    openType: typeof route.query.openType === 'string' ? route.query.openType : '',
    openAction: typeof route.query.openAction === 'string' ? route.query.openAction : '',
    openId: normalizeRouteNumber(route.query.openId),
    purchaseOrderId: normalizeRouteNumber(route.query.purchaseOrderId)
  })
  if (routeOpen.action === 'none') {
    return
  }
  if (routeOpen.action === 'stock-execute') {
    stockExecuteDialogRef.value?.open(routeOpen.id)
  } else {
    openForm(routeOpen.formType, routeOpen.id, routeOpen.purchaseOrderId)
  }
  const nextQuery = { ...route.query }
  routeOpen.cleanupKeys.forEach((key) => delete nextQuery[key])
  await replace({ path: route.path, query: nextQuery })
}

onMounted(async () => {
  await Promise.allSettled([syncOrderFilterFromRoute(), loadFilterOptions()])
  await openFormByRouteQuery()
})

watch(
  () => [
    route.query.openId,
    route.query.openType,
    route.query.openAction,
    route.query.purchaseOrderId
  ],
  async () => {
    await openFormByRouteQuery()
  }
)

watch(
  () => route.query.orderNo,
  async () => {
    await syncOrderFilterFromRoute()
  }
)
</script>

<style scoped lang="scss">
.purchase-in-page__filter-card,
.purchase-in-page__list-card {
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.purchase-in-page__title {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.purchase-in-main-control {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 16px;
  background: var(--erp-slate-50);
}

.purchase-in-main-control__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.purchase-in-main-control__title {
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 800;
  line-height: 24px;
}

.purchase-in-main-control__current {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--erp-slate-500);
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-main-control__current span {
  color: var(--erp-primary-600);
  font-weight: 700;
}

.purchase-in-main-control__stages {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.purchase-in-main-control__stage {
  min-height: 34px;
  padding: 0 12px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 999px;
  background: var(--el-bg-color);
  color: var(--erp-slate-600);
  font-size: 13px;
  font-weight: 700;
  line-height: 20px;
  transition: all 0.2s ease;
}

.purchase-in-main-control__stage:hover:not(:disabled) {
  border-color: var(--erp-primary-100);
  color: var(--erp-primary-600);
}

.purchase-in-main-control__stage:disabled {
  cursor: not-allowed;
  color: var(--erp-slate-400);
}

.purchase-in-main-control__stage.is-active {
  border-color: var(--erp-primary-100);
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
}

.purchase-in-main-control__stage--amber.is-active {
  border-color: var(--erp-warning-100);
  background: var(--erp-warning-50);
  color: var(--erp-warning-600);
}

.purchase-in-main-control__stage--green.is-active {
  border-color: var(--erp-success-100);
  background: var(--erp-success-50);
  color: var(--erp-success-600);
}

.purchase-in-main-control__stage--slate.is-active {
  border-color: var(--erp-slate-200);
  background: var(--el-bg-color);
  color: var(--erp-slate-800);
}

.purchase-in-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: #475569;
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper) {
    min-height: 42px;
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.9);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  }
}

.purchase-in-query__grid {
  display: grid;
  gap: 18px 16px;
}

.purchase-in-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-in-query__grid--advanced {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-in-query__footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.purchase-in-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.purchase-in-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
}

.purchase-in-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.82);
}

.purchase-in-toolbar__actions,
.purchase-in-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.purchase-in-ledger {
  :deep(.el-table__header-wrapper th) {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98));
    color: #64748b;
    font-size: 11px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 7px;
    padding-left: 7px;
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
    background: rgba(226, 232, 240, 0.92);
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

.ledger-order__no {
  color: #0f172a;
  font-size: 15px;
  font-weight: 800;
  line-height: 22px;
  word-break: break-all;
}

.ledger-order__meta,
.ledger-party__meta,
.ledger-finance__meta,
.ledger-finance__sub,
.ledger-status__reject {
  color: #64748b;
  font-size: 11px;
  line-height: 17px;
}

.ledger-party {
  gap: 8px;
}

.ledger-party__supplier {
  color: #0f172a;
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
  background: rgba(226, 232, 240, 0.9);
  color: #475569;
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
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ledger-product__name {
  display: -webkit-box;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.ledger-product__batch {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  word-break: break-word;
}

.ledger-progress {
  gap: 12px;
}

.ledger-progress__section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ledger-progress__top,
.ledger-progress__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #475569;
  font-size: 11px;
  line-height: 17px;
}

.ledger-progress__top strong {
  color: #0f172a;
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
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.ledger-finance__meta {
  color: #059669;
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
  color: #dc2626;
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

.purchase-in-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.purchase-in-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.08), rgba(20, 184, 166, 0.08));
  color: #2563eb;
  font-size: 22px;
}

.purchase-in-empty__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.purchase-in-empty--error .purchase-in-empty__icon {
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.12), rgba(251, 191, 36, 0.08));
  color: #ef4444;
}

.purchase-in-mobile-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.purchase-in-mobile-card {
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  padding: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96)),
    linear-gradient(135deg, rgba(37, 99, 235, 0.03), rgba(20, 184, 166, 0.04));
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.05);
}

.purchase-in-mobile-card__head,
.purchase-in-mobile-card__meta,
.purchase-in-mobile-card__detail,
.purchase-in-mobile-card__metric-top,
.purchase-in-mobile-card__summary-bar,
.purchase-in-mobile-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.purchase-in-mobile-card__head {
  align-items: flex-start;
}

.purchase-in-mobile-card__identity,
.purchase-in-mobile-card__party {
  min-width: 0;
  flex: 1;
}

.purchase-in-mobile-card__no {
  color: #0f172a;
  font-size: 17px;
  font-weight: 800;
  line-height: 24px;
  word-break: break-all;
}

.purchase-in-mobile-card__meta,
.purchase-in-mobile-card__finance {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__status {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.purchase-in-mobile-card__summary {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 0.85fr);
  gap: 14px;
  margin-top: 14px;
}

.purchase-in-mobile-card__supplier,
.purchase-in-mobile-card__product {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 22px;
}

.purchase-in-mobile-card__detail {
  margin-top: 8px;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__detail-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.9);
  color: #475569;
  font-size: 11px;
  font-weight: 700;
}

.purchase-in-mobile-card__finance {
  align-items: flex-end;
  text-align: right;
}

.purchase-in-mobile-card__finance-label {
  color: #64748b;
}

.purchase-in-mobile-card__finance-value {
  color: #0f172a;
  font-size: 24px;
  line-height: 1.05;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.purchase-in-mobile-card__product {
  margin-top: 14px;
}

.purchase-in-mobile-card__source-batch {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__source-batch-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(219, 234, 254, 0.9);
  color: #1d4ed8;
  font-size: 11px;
  font-weight: 700;
}

.purchase-in-mobile-card__metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.purchase-in-mobile-card__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(226, 232, 240, 0.86);
}

.purchase-in-mobile-card__metric-top {
  justify-content: space-between;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__metric-top strong {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.purchase-in-mobile-card__summary-bar {
  justify-content: space-between;
  margin-top: 12px;
  color: #475569;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__reject {
  margin-top: 12px;
  color: #dc2626;
  font-size: 12px;
  line-height: 18px;
}

.purchase-in-mobile-card__actions {
  justify-content: flex-start;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.86);
}

.purchase-in-mobile-card__more {
  width: 30px;
  height: 30px;
  padding: 0;
  border-radius: 999px;
}

.purchase-in-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
}

.purchase-in-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.purchase-in-query-collapse-enter-active,
.purchase-in-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.purchase-in-query-collapse-enter-from,
.purchase-in-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1359px) {
  .purchase-in-query__grid--primary,
  .purchase-in-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .purchase-in-page__title {
    font-size: 24px;
  }

  .ledger-finance__amount {
    font-size: 18px;
  }
}

@media (max-width: 1179px) {
  .purchase-in-mobile-card__summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-in-mobile-card__finance {
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 1023px) {
  .purchase-in-query__grid--primary,
  .purchase-in-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-in-query__footer,
  .purchase-in-toolbar,
  .purchase-in-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-in-query__actions,
  .purchase-in-toolbar__actions,
  .purchase-in-toolbar__meta {
    width: 100%;
  }

  .purchase-in-query__actions :deep(.el-button),
  .purchase-in-toolbar__actions :deep(.el-button),
  .purchase-in-toolbar__meta :deep(.el-button) {
    flex: 1;
  }

  .purchase-in-main-control__stage {
    flex: 1;
    min-width: 118px;
  }

  .purchase-in-mobile-card {
    padding: 14px;
  }

  .purchase-in-mobile-card__metrics {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 767px) {
  .purchase-in-mobile-card__status,
  .purchase-in-mobile-card__actions {
    justify-content: flex-start;
  }

  .purchase-in-mobile-card__finance-value {
    font-size: 22px;
  }
}
</style>
