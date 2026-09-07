<template>
  <div class="sale-order-page">

    <ContentWrap class="sale-order-page__filter-card">
      <div class="sale-order-page__title">销售订单管理</div>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-position="top"
        class="sale-order-query"
      >
        <div class="sale-order-query__grid sale-order-query__grid--primary">
          <el-form-item label="订单单号" prop="no">
            <el-input
              v-model="queryParams.no"
              placeholder="请输入订单单号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="客户名称" prop="customerId">
            <el-select
              v-model="queryParams.customerId"
              clearable
              filterable
              placeholder="请选择供应客户"
            >
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联项目" prop="projectId">
            <el-select
              v-model="queryParams.projectId"
              clearable
              filterable
              placeholder="请选择项目"
            >
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
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

        <transition name="sale-order-query-collapse">
          <div
            v-if="advancedSearchVisible"
            class="sale-order-query__grid sale-order-query__grid--advanced"
          >
            <el-form-item label="产品" prop="productId">
              <ProductRemoteSelect v-model="queryParams.productId" placeholder="请选择产品" />
            </el-form-item>
            <el-form-item label="交期" prop="deliveryDate">
              <el-date-picker
                v-model="queryParams.deliveryDate"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                range-separator="-"
                :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
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
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="queryParams.remark"
                placeholder="请输入备注"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="出库进度" prop="outStatus">
              <el-select v-model="queryParams.outStatus" placeholder="请选择出库进度" clearable>
                <el-option label="未出库" value="0" />
                <el-option label="部分出库" value="1" />
                <el-option label="全部出库" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="退货进度" prop="returnStatus">
              <el-select v-model="queryParams.returnStatus" placeholder="请选择退货进度" clearable>
                <el-option label="未退货" value="0" />
                <el-option label="部分退货" value="1" />
                <el-option label="全部退货" value="2" />
              </el-select>
            </el-form-item>
          </div>
        </transition>

        <div class="sale-order-query__footer">
          <el-button link type="primary" @click="toggleAdvancedSearch">
            {{ advancedSearchVisible ? '收起高级筛选' : '展开高级筛选' }}
            <span v-if="advancedFilterCount" class="sale-order-query__filter-count">
              {{ advancedFilterCount }}
            </span>
            <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
          </el-button>
          <div class="sale-order-query__actions">
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

    <ContentWrap class="sale-order-page__list-card">
      <div class="sale-order-toolbar">
        <div class="sale-order-toolbar__actions">
          <el-button
            v-if="toolbarState.showCreate"
            type="primary"
            @click="openForm('create')"
            v-hasPermi="['erp:sale-order:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 新增订单
          </el-button>
          <el-button
            type="primary"
            plain
            @click="openClosureWorkbench"
            :disabled="closureWorkbenchNavigating || loading || exportLoading"
            v-hasPermi="['erp:sale-order:query']"
          >
            <Icon icon="ep:data-analysis" class="mr-5px" /> 闭环工作台
          </el-button>
          <el-button
            v-if="toolbarState.showExport"
            plain
            @click="handleExport"
            :loading="exportLoading"
            v-hasPermi="['erp:sale-order:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出数据
          </el-button>
          <el-button
            v-if="toolbarState.showBatchEdit"
            plain
            :disabled="toolbarState.disableBatchEdit"
            @click="openBatchEditDrawer()"
            v-hasPermi="['erp:sale-order:update']"
          >
            <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
          </el-button>
          <el-button
            v-if="toolbarState.showTodo"
            plain
            @click="openSaleOrderTodoTask"
            v-hasPermi="['bpm:task:query']"
          >
            <Icon icon="ep:promotion" class="mr-5px" /> 审批待办
          </el-button>
        </div>
        <div class="sale-order-toolbar__meta">
          <el-button
            v-if="toolbarState.showBatchDelete"
            plain
            type="danger"
            :disabled="toolbarState.disableBatchDelete"
            @click="handleDelete(deletableSelectionIds)"
            v-hasPermi="['erp:sale-order:delete']"
          >
            <Icon icon="ep:delete" class="mr-5px" /> 批量删除
          </el-button>
        </div>
      </div>

      <div v-if="!isCompactLayout" class="sale-order-table-wrap">
        <el-table
          v-loading="loading"
          :data="list"
          :stripe="true"
          class="sale-order-ledger"
          @selection-change="handleSelectionChange"
        >
          <template #empty>
            <div v-if="listLoadFailed" class="sale-order-empty sale-order-empty--error">
              <div class="sale-order-empty__icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="sale-order-empty__title">列表加载失败</div>
              <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
                重试加载
              </el-button>
            </div>
            <div v-else class="sale-order-empty">
              <div class="sale-order-empty__icon">
                <Icon icon="ep:box" />
              </div>
              <div class="sale-order-empty__title">暂无订单记录</div>
            </div>
          </template>
          <el-table-column width="42" type="selection" :selectable="canDelete" />
          <el-table-column label="订单信息" min-width="190">
            <template #default="{ row }">
              <div class="ledger-order">
                <div class="ledger-order__no">{{ row.no || '-' }}</div>
                <div class="ledger-order__meta">下单 {{ formatDateValue(row.orderTime) }}</div>
                <div class="ledger-order__meta">交期 {{ formatDateValue(row.deliveryDate) }}</div>
                <div class="ledger-order__meta">创建 {{ row.creatorName || '-' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="客户与项目" min-width="180">
            <template #default="{ row }">
              <div class="ledger-party">
                <div class="ledger-party__customer">{{ row.customerName || '-' }}</div>
                <div class="ledger-party__project">
                  <span class="ledger-party__project-tag">项目</span>
                  <span>{{ row.projectName || '未关联项目' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品摘要" min-width="180">
            <template #default="{ row }">
              <div class="ledger-product" :title="row.productNames || '-'">
                {{ row.productNames || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数量 / 进度" min-width="220">
            <template #default="{ row }">
              <div class="ledger-progress">
                <div class="ledger-progress__section">
                  <div class="ledger-progress__top">
                    <span>出库进度</span>
                    <strong
                      >{{ formatCount(row.outCount) }} / {{ formatCount(row.totalCount) }}</strong
                    >
                  </div>
                  <el-progress
                    :stroke-width="6"
                    :show-text="false"
                    :percentage="getProgressPercent(row.outCount, row.totalCount)"
                    :color="resolveOutProgressColor(row)"
                  />
                </div>
                <div class="ledger-progress__section">
                  <div class="ledger-progress__top">
                    <span>退货进度</span>
                    <strong
                      >{{ formatCount(row.returnCount) }} /
                      {{ formatCount(row.totalCount) }}</strong
                    >
                  </div>
                  <el-progress
                    :stroke-width="6"
                    :show-text="false"
                    :percentage="getProgressPercent(row.returnCount, row.totalCount)"
                    color="var(--erp-warning-600)"
                  />
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="财务结算（元）" min-width="170" align="right">
            <template #default="{ row }">
              <div class="ledger-finance">
                <div class="ledger-finance__amount">{{ formatCurrency(row.totalPrice) }}</div>
                <div class="ledger-finance__meta">
                  {{
                    normalizeNumber(row.depositPrice) > 0
                      ? `已收订金：${formatCurrency(row.depositPrice)}`
                      : '无订金'
                  }}
                </div>
                <div class="ledger-finance__sub"
                  >货款 {{ formatCurrency(row.totalProductPrice) }}</div
                >
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态 / 交付" min-width="150">
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
                  <el-tag
                    size="small"
                    effect="light"
                    :type="resolveDeliveryReadyTagType(row.deliveryReadyStatus)"
                  >
                    {{ resolveDeliveryReadyLabel(row.deliveryReadyStatus) }}
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
          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <div class="ledger-actions">
                <el-button
                  link
                  type="primary"
                  @click="openForm('detail', row.id)"
                  v-hasPermi="['erp:sale-order:query']"
                >
                  详情
                </el-button>
                <el-button
                  v-if="canEdit(row)"
                  link
                  type="primary"
                  @click="openForm('update', row.id)"
                  v-hasPermi="['erp:sale-order:update']"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="canSubmit(row)"
                  link
                  type="success"
                  @click="openSubmitDialog(row)"
                  v-hasPermi="['erp:sale-order:update-status']"
                >
                  {{ row.status === SALE_ORDER_STATUS.REJECT ? '重新提交审批' : '提交审批' }}
                </el-button>
                <el-button
                  v-else-if="canTraceDownstream(row)"
                  link
                  type="primary"
                  @click="openPurchaseSuggestTrace(row)"
                  v-hasPermi="['erp:mrp-suggest:query']"
                >
                  MRP
                </el-button>
                <el-button
                  v-if="canTraceDownstream(row)"
                  link
                  type="primary"
                  @click="openPurchaseOrderTrace(row)"
                  v-hasPermi="['erp:purchase-order:query']"
                >
                  采购
                </el-button>
                <el-tooltip content="审批历史" placement="top">
                  <el-button
                    link
                    type="primary"
                    class="ledger-actions__icon"
                    @click="openAuditLogDialog(row.id)"
                    v-hasPermi="['erp:sale-order:query']"
                  >
                    <Icon icon="ep:clock" />
                  </el-button>
                </el-tooltip>
                <el-tooltip v-if="canViewProcess(row)" content="查看审批" placement="top">
                  <el-button
                    link
                    type="primary"
                    class="ledger-actions__icon"
                    @click="handleProcessDetail(row)"
                    v-hasPermi="['erp:sale-order:query']"
                  >
                    <Icon icon="ep:view" />
                  </el-button>
                </el-tooltip>
                <el-tooltip v-if="canCancelApproval(row)" content="撤回审批" placement="top">
                  <el-button
                    link
                    type="primary"
                    class="ledger-actions__icon"
                    @click="handleCancelApproval(row)"
                    v-hasPermi="['erp:sale-order:update-status']"
                  >
                    <Icon icon="ep:refresh-left" />
                  </el-button>
                </el-tooltip>
                <el-tooltip v-if="canDelete(row)" content="删除" placement="top">
                  <el-button
                    link
                    type="danger"
                    class="ledger-actions__icon ledger-actions__icon--danger"
                    @click="handleDelete([row.id])"
                    v-hasPermi="['erp:sale-order:delete']"
                  >
                    <Icon icon="ep:delete" />
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div
        v-else
        v-loading="loading"
        class="sale-order-mobile-list"
        element-loading-background="rgba(248, 250, 252, 0.78)"
      >
        <template v-if="list.length">
          <article v-for="row in list" :key="row.id" class="sale-order-mobile-card">
            <div class="sale-order-mobile-card__head">
              <el-checkbox
                :model-value="selectedIdSet.has(row.id)"
                @change="(checked) => toggleSelection(row, checked)"
              />
              <div class="sale-order-mobile-card__identity">
                <div class="sale-order-mobile-card__no">{{ row.no || '-' }}</div>
                <div class="sale-order-mobile-card__meta">
                  <span>下单 {{ formatDateValue(row.orderTime) }}</span>
                  <span>交期 {{ formatDateValue(row.deliveryDate) }}</span>
                  <span>创建 {{ row.creatorName || '-' }}</span>
                </div>
              </div>
              <div class="sale-order-mobile-card__status">
                <el-tag
                  size="small"
                  effect="light"
                  :type="resolveErpAuditStatusTagType(row.status, row.processInstanceId)"
                >
                  {{ resolveErpAuditStatusLabel(row.status, row.processInstanceId) }}
                </el-tag>
                <el-tag
                  size="small"
                  effect="light"
                  :type="resolveDeliveryReadyTagType(row.deliveryReadyStatus)"
                >
                  {{ resolveDeliveryReadyLabel(row.deliveryReadyStatus) }}
                </el-tag>
              </div>
            </div>

            <div class="sale-order-mobile-card__summary">
              <div class="sale-order-mobile-card__party">
                <div class="sale-order-mobile-card__customer">{{ row.customerName || '-' }}</div>
                <div class="sale-order-mobile-card__project">
                  <span class="sale-order-mobile-card__project-tag">项目</span>
                  <span>{{ row.projectName || '未关联项目' }}</span>
                </div>
              </div>
              <div class="sale-order-mobile-card__finance">
                <span class="sale-order-mobile-card__finance-label">订单金额</span>
                <strong class="sale-order-mobile-card__finance-value">
                  {{ formatCurrency(row.totalPrice) }}
                </strong>
                <span>
                  {{
                    normalizeNumber(row.depositPrice) > 0
                      ? `已收订金 ${formatCurrency(row.depositPrice)}`
                      : '无订金'
                  }}
                </span>
                <span>货款 {{ formatCurrency(row.totalProductPrice) }}</span>
              </div>
            </div>

            <div class="sale-order-mobile-card__product">{{ row.productNames || '-' }}</div>

            <div class="sale-order-mobile-card__metrics">
              <div class="sale-order-mobile-card__metric">
                <div class="sale-order-mobile-card__metric-top">
                  <span>出库进度</span>
                  <strong
                    >{{ formatCount(row.outCount) }} / {{ formatCount(row.totalCount) }}</strong
                  >
                </div>
                <el-progress
                  :stroke-width="6"
                  :show-text="false"
                  :percentage="getProgressPercent(row.outCount, row.totalCount)"
                  :color="resolveOutProgressColor(row)"
                />
              </div>
              <div class="sale-order-mobile-card__metric">
                <div class="sale-order-mobile-card__metric-top">
                  <span>退货进度</span>
                  <strong
                    >{{ formatCount(row.returnCount) }} / {{ formatCount(row.totalCount) }}</strong
                  >
                </div>
                <el-progress
                  :stroke-width="6"
                  :show-text="false"
                  :percentage="getProgressPercent(row.returnCount, row.totalCount)"
                  color="var(--erp-warning-600)"
                />
              </div>
            </div>

            <div v-if="row.lastRejectReason" class="sale-order-mobile-card__reject">
              {{ row.lastRejectReason }}
            </div>

            <div class="sale-order-mobile-card__actions">
              <el-button
                link
                type="primary"
                @click="openForm('detail', row.id)"
                v-hasPermi="['erp:sale-order:query']"
              >
                详情
              </el-button>
              <el-button
                v-if="canEdit(row)"
                link
                type="primary"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:sale-order:update']"
              >
                编辑
              </el-button>
              <el-button
                v-if="canSubmit(row)"
                link
                type="success"
                @click="openSubmitDialog(row)"
                v-hasPermi="['erp:sale-order:update-status']"
              >
                {{ row.status === SALE_ORDER_STATUS.REJECT ? '重新提交审批' : '提交审批' }}
              </el-button>
              <el-button
                v-else-if="canTraceDownstream(row)"
                link
                type="primary"
                @click="openPurchaseSuggestTrace(row)"
                v-hasPermi="['erp:mrp-suggest:query']"
              >
                MRP
              </el-button>
              <el-button
                v-if="canTraceDownstream(row)"
                link
                type="primary"
                @click="openPurchaseOrderTrace(row)"
                v-hasPermi="['erp:purchase-order:query']"
              >
                采购
              </el-button>
              <el-tooltip content="审批历史" placement="top">
                <el-button
                  link
                  type="primary"
                  class="ledger-actions__icon"
                  @click="openAuditLogDialog(row.id)"
                  v-hasPermi="['erp:sale-order:query']"
                >
                  <Icon icon="ep:clock" />
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="canViewProcess(row)" content="查看审批" placement="top">
                <el-button
                  link
                  type="primary"
                  class="ledger-actions__icon"
                  @click="handleProcessDetail(row)"
                  v-hasPermi="['erp:sale-order:query']"
                >
                  <Icon icon="ep:view" />
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="canCancelApproval(row)" content="撤回审批" placement="top">
                <el-button
                  link
                  type="primary"
                  class="ledger-actions__icon"
                  @click="handleCancelApproval(row)"
                  v-hasPermi="['erp:sale-order:update-status']"
                >
                  <Icon icon="ep:refresh-left" />
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="canDelete(row)" content="删除" placement="top">
                <el-button
                  link
                  type="danger"
                  class="ledger-actions__icon ledger-actions__icon--danger"
                  @click="handleDelete([row.id])"
                  v-hasPermi="['erp:sale-order:delete']"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </el-tooltip>
            </div>
          </article>
        </template>
        <div v-else-if="listLoadFailed" class="sale-order-empty sale-order-empty--error">
          <div class="sale-order-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="sale-order-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
            重试加载
          </el-button>
        </div>
        <div v-else class="sale-order-empty">
          <div class="sale-order-empty__icon">
            <Icon icon="ep:box" />
          </div>
          <div class="sale-order-empty__title">暂无订单记录</div>
        </div>
      </div>

      <div class="sale-order-page__footer">
        <div class="sale-order-page__record-count">共 {{ total }} 条记录</div>
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </ContentWrap>

  <SaleOrderForm ref="formRef" @success="getList" />
  <SaleOrderAuditLogDialog ref="auditLogDialogRef" />
  <SaleOrderSubmitDialog ref="submitDialogRef" @success="getList" />
  <SaleOrderBatchEditDrawer
    ref="batchEditDrawerRef"
    :account-options="accountList"
    @success="handleBatchEditSuccess"
  />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { SaleOrderApi, SaleOrderVO } from '@/api/erp/sale/order'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import SaleOrderForm from './SaleOrderForm.vue'
import SaleOrderAuditLogDialog from './SaleOrderAuditLogDialog.vue'
import SaleOrderSubmitDialog from './SaleOrderSubmitDialog.vue'
import SaleOrderBatchEditDrawer from './components/SaleOrderBatchEditDrawer.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { ProjectApi, type ProjectSimpleVO } from '@/api/erp/project'
import { useUserStoreWithOut } from '@/store/modules/user'
import { checkPermi } from '@/utils/permission'
import {
  getSaleOrderRowActionDescriptor,
  getSaleOrderToolbarDescriptor
} from './saleOrderStatus.helpers'
import { useWindowSize } from '@vueuse/core'

interface SaleOrderListRow extends SaleOrderVO {
  customerName?: string
  creatorName?: string
  productNames?: string
  totalProductPrice?: number
  depositPrice?: number
}

defineOptions({ name: 'ErpSaleOrder' })

const SALE_ORDER_BPM_PROCESS_KEY = 'erp_sale_order'

const SALE_ORDER_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const { push, replace } = router
const userStore = useUserStoreWithOut()
const { width } = useWindowSize()
const canQuerySaleOrder = checkPermi(['erp:sale-order:query'])
const canQueryPurchaseOrder = checkPermi(['erp:purchase-order:query'])
const canQueryMrpSuggest = checkPermi(['erp:mrp-suggest:query'])
const canQueryBpmTask = checkPermi(['bpm:task:query'])

const loading = ref(true)
const listLoadFailed = ref(false)
const list = ref<SaleOrderListRow[]>([])
const total = ref(0)
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  projectId: undefined,
  customerId: undefined,
  productId: undefined,
  orderTime: [],
  deliveryDate: [],
  status: undefined,
  remark: undefined,
  creator: undefined,
  outStatus: undefined,
  returnStatus: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const closureWorkbenchNavigating = ref(false)
const projectList = ref<ProjectSimpleVO[]>([])
const productList = ref<ProductVO[]>([])
const customerList = ref<CustomerVO[]>([])
const userList = ref<UserVO[]>([])
const accountList = ref<AccountVO[]>([])
const selectionList = ref<SaleOrderListRow[]>([])
const submitDialogRef = ref()
const formRef = ref()
const auditLogDialogRef = ref()
const batchEditDrawerRef = ref()
const currentUserId = computed(() => String(userStore.getUser.id || ''))
const isCompactLayout = computed(() => width.value < 1180)
const canRetryList = computed(() => listLoadFailed.value && !loading.value)
const selectedIdSet = computed(() => new Set(selectionList.value.map((item) => item.id)))

const advancedFilterCount = computed(() => {
  const advancedFields = [
    queryParams.productId,
    queryParams.deliveryDate?.length ? queryParams.deliveryDate : undefined,
    queryParams.creator,
    queryParams.status,
    queryParams.remark,
    queryParams.outStatus,
    queryParams.returnStatus
  ]
  return advancedFields.filter((item) => {
    if (Array.isArray(item)) {
      return item.length > 0
    }
    return item !== undefined && item !== null && item !== ''
  }).length
})

const resolveRowActionState = (row: SaleOrderListRow) => {
  return getSaleOrderRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value
  })
}

const canEdit = (row: SaleOrderListRow) => resolveRowActionState(row).canEdit
const canSubmit = (row: SaleOrderListRow) => resolveRowActionState(row).canSubmit
const canCancelApproval = (row: SaleOrderListRow) => resolveRowActionState(row).canCancelApproval
const canViewProcess = (row: SaleOrderListRow) => resolveRowActionState(row).canViewProcess
const canTraceDownstream = (row: SaleOrderListRow) => resolveRowActionState(row).canTraceDownstream
const canDelete = (row: SaleOrderListRow) => resolveRowActionState(row).canDelete

const batchEditableSelectionRows = computed(() =>
  selectionList.value.filter((item) => canEdit(item) && item.id)
)

const deletableSelectionIds = computed(() =>
  selectionList.value.filter((item) => canDelete(item)).map((item) => item.id)
)

const toolbarState = computed(() =>
  getSaleOrderToolbarDescriptor({
    batchEditableSelectionCount: batchEditableSelectionRows.value.length,
    deletableSelectionCount: deletableSelectionIds.value.length
  })
)

const normalizeNumber = (value?: number | string | null) => Number(value || 0)

const formatDateValue = (value?: Date | string) => {
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

const resolveOutProgressColor = (row: SaleOrderListRow) => {
  const percent = getProgressPercent(row.outCount, row.totalCount)
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

const resolveDeliveryReadyLabel = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return '部分就绪'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return '可发货'
  }
  return '暂无可发'
}

const resolveDeliveryReadyTagType = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return 'warning'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return 'success'
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
    const data = await SaleOrderApi.getSaleOrderPage(queryParams)
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
    ProjectApi.getProjectSimpleList(),
    Promise.resolve([]),
    CustomerApi.getCustomerSimpleList(),
    UserApi.getSimpleUserList(),
    AccountApi.getAccountSimpleList()
  ])
  projectList.value = results[0].status === 'fulfilled' ? results[0].value : []
  productList.value = results[1].status === 'fulfilled' ? results[1].value : []
  customerList.value = results[2].status === 'fulfilled' ? results[2].value : []
  userList.value = results[3].status === 'fulfilled' ? results[3].value : []
  accountList.value = results[4].status === 'fulfilled' ? results[4].value : []
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  advancedSearchVisible.value = false
  handleQuery()
}

const handleRetryList = () => {
  getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const openAuditLogDialog = (id: number) => {
  auditLogDialogRef.value.open(id)
}

const openFormByRouteQuery = async () => {
  const openId = Number(route.query.openId)
  const openType = typeof route.query.openType === 'string' ? route.query.openType : 'detail'
  if (!openId || Number.isNaN(openId)) {
    return
  }
  openForm(openType, openId)
  const nextQuery = { ...route.query }
  delete nextQuery.openId
  delete nextQuery.openType
  delete nextQuery.from
  await replace({
    path: route.path,
    query: nextQuery
  })
}

const openSaleOrderTodoTask = () => {
  if (!canQueryBpmTask) {
    message.warning('当前账号没有审批待办查看权限')
    return
  }
  push({
    path: '/approval/todo',
    query: {
      processDefinitionKey: SALE_ORDER_BPM_PROCESS_KEY
    }
  })
}

const openClosureWorkbench = async () => {
  if (!canQuerySaleOrder) {
    message.warning('当前账号没有销售订单查看权限')
    return
  }
  if (closureWorkbenchNavigating.value || loading.value || exportLoading.value) {
    return
  }
  closureWorkbenchNavigating.value = true
  try {
    const targetPath = router.getRoutes().some((item) => item.path === '/sales/closure-workbench')
      ? '/sales/closure-workbench'
      : '/erp/sale/order/closure-workbench'
    await push({ path: targetPath })
  } finally {
    closureWorkbenchNavigating.value = false
  }
}

const buildTraceQuery = (row: SaleOrderListRow, extraQuery?: Record<string, string>) => {
  return {
    sourceOrderId: String(row.id),
    sourceOrderNo: row.no,
    traceFrom: 'sale-order',
    ...extraQuery
  }
}

const openPurchaseSuggestTrace = (row: SaleOrderListRow) => {
  if (!canQueryMrpSuggest) {
    message.warning('当前账号没有 MRP 建议查看权限')
    return
  }
  push({
    name: 'ErpSaleOrderMrpTracePage',
    query: buildTraceQuery(row, { tab: 'purchase' })
  })
}

const openPurchaseOrderTrace = (row: SaleOrderListRow) => {
  if (!canQueryPurchaseOrder) {
    message.warning('当前账号没有采购订单查看权限')
    return
  }
  push({
    name: 'ErpSaleOrderPurchaseOrderTracePage',
    query: buildTraceQuery(row)
  })
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length) {
    return
  }
  try {
    await message.delConfirm()
    await SaleOrderApi.deleteSaleOrder(ids)
    message.success(t('common.delSuccess'))
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch (error) {
    if (isActionCancelled(error)) {
      return
    }
    message.error('删除销售订单失败')
  }
}

const openSubmitDialog = (row: SaleOrderListRow) => {
  submitDialogRef.value?.open(row)
}

const handleCancelApproval = async (row: SaleOrderListRow) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    await SaleOrderApi.cancelSaleOrderApproval({
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
  }
}

const handleProcessDetail = (row: SaleOrderListRow) => {
  if (!row.processInstanceId) {
    message.warning('当前销售订单暂无审批流程')
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
    const data = await SaleOrderApi.exportSaleOrder(queryParams)
    download.excel(data, '销售订单.xls')
  } catch (error) {
    if (!isActionCancelled(error)) {
      message.error('导出销售订单失败')
    }
  } finally {
    exportLoading.value = false
  }
}

const handleSelectionChange = (rows: SaleOrderListRow[]) => {
  selectionList.value = rows
}

const openBatchEditDrawer = () => {
  if (!batchEditableSelectionRows.value.length) {
    message.warning('请先选择可批量编辑的销售订单')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: batchEditableSelectionRows.value.map((item) => item.id!),
    rows: batchEditableSelectionRows.value
  })
}

const handleBatchEditSuccess = async () => {
  await getList()
}

const toggleSelection = (row: SaleOrderListRow, checked: unknown) => {
  if (row.id === undefined) {
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

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
  await openFormByRouteQuery()
})

watch(
  () => [route.query.openId, route.query.openType],
  async () => {
    await openFormByRouteQuery()
  }
)
</script>

<style scoped lang="scss">
.sale-order-page {
  --sale-primary: var(--erp-primary-600);
  --sale-primary-soft: var(--erp-primary-50);
  --sale-primary-border: var(--erp-blue-200);
  --sale-success: var(--erp-success-600);
  --sale-warning: var(--erp-warning-600);
  --sale-danger: var(--erp-danger-600);

  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  background: var(--el-bg-color-page);
}

:global(html.dark) .sale-order-page {
  --sale-primary: var(--erp-primary-600);
  --sale-primary-soft: rgb(37 99 235 / 16%);
  --sale-primary-border: rgb(96 165 250 / 34%);
  --sale-success: var(--erp-success-600);
  --sale-warning: var(--erp-warning-600);
  --sale-danger: var(--erp-danger-600);
}

.sale-order-page__filter-card,
.sale-order-page__list-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: var(--el-bg-color);
  box-shadow: 0 1px 2px rgb(15 23 42 / 6%);
}

.sale-order-page__title {
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
  font-size: 20px;
  font-weight: 800;
  line-height: 1.4;
}

.sale-order-page__subtitle {
  margin: -8px 0 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 20px;
}

.sale-order-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: var(--el-text-color-regular);
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper) {
    min-height: 40px;
    border-radius: 8px;
    box-shadow: none;
    border: 1px solid var(--el-border-color-light);
    background: var(--el-bg-color);
  }
}

.sale-order-query__grid {
  display: grid;
  gap: 18px 16px;
}

.sale-order-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.sale-order-query__grid--advanced {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--el-border-color-lighter);
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.sale-order-query__footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sale-order-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sale-order-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--sale-primary-soft);
  color: var(--sale-primary);
  font-size: 11px;
  font-weight: 700;
}

.sale-order-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.sale-order-toolbar__actions,
.sale-order-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.sale-order-table-wrap {
  overflow-x: auto;
}

.sale-order-ledger {
  min-width: 1210px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 10px;

  :deep(.el-table__header-wrapper th) {
    position: sticky;
    top: 0;
    z-index: 10;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-regular);
    font-size: 12px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    border-color: var(--el-border-color-lighter);
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
    background: var(--el-border-color-lighter);
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
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 800;
  line-height: 22px;
  word-break: break-all;
}

.ledger-order__meta,
.ledger-party__project,
.ledger-finance__meta,
.ledger-finance__sub,
.ledger-status__reject {
  color: var(--el-text-color-secondary);
  font-size: 11px;
  line-height: 17px;
}

.ledger-party {
  gap: 8px;
}

.ledger-party__customer {
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
}

.ledger-party__project {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ledger-party__project-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 11px;
  font-weight: 700;
}

.ledger-product {
  display: -webkit-box;
  color: var(--el-text-color-primary);
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
  color: var(--el-text-color-regular);
  font-size: 11px;
  line-height: 17px;
}

.ledger-progress__top strong {
  color: var(--el-text-color-primary);
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
  color: var(--el-text-color-primary);
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.ledger-finance__meta {
  color: var(--sale-success);
  font-weight: 600;
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
  color: var(--sale-danger);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ledger-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px 8px;
  flex-wrap: wrap;
}

.ledger-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.ledger-actions__icon {
  width: 24px;
  padding: 0;
}

.ledger-actions__icon--danger {
  color: var(--sale-danger);
}

.ledger-actions__icon--danger:hover {
  color: var(--sale-danger);
}

.sale-order-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--el-text-color-secondary);
}

.sale-order-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--sale-primary-soft);
  color: var(--sale-primary);
  font-size: 22px;
}

.sale-order-empty__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.sale-order-empty--error .sale-order-empty__icon {
  background: var(--el-color-danger-light-9);
  color: var(--sale-danger);
}

.sale-order-mobile-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sale-order-mobile-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 16px;
  background: var(--el-bg-color);
  box-shadow: 0 1px 2px rgb(15 23 42 / 6%);
}

.sale-order-mobile-card__head,
.sale-order-mobile-card__meta,
.sale-order-mobile-card__project,
.sale-order-mobile-card__metric-top,
.sale-order-mobile-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sale-order-mobile-card__head {
  align-items: flex-start;
}

.sale-order-mobile-card__identity,
.sale-order-mobile-card__party {
  min-width: 0;
  flex: 1;
}

.sale-order-mobile-card__no {
  color: var(--el-text-color-primary);
  font-size: 17px;
  font-weight: 800;
  line-height: 24px;
  word-break: break-all;
}

.sale-order-mobile-card__meta,
.sale-order-mobile-card__finance {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.sale-order-mobile-card__status {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.sale-order-mobile-card__summary {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 0.85fr);
  gap: 14px;
  margin-top: 14px;
}

.sale-order-mobile-card__customer,
.sale-order-mobile-card__product {
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 700;
  line-height: 22px;
}

.sale-order-mobile-card__project {
  margin-top: 8px;
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 18px;
}

.sale-order-mobile-card__project-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 11px;
  font-weight: 700;
}

.sale-order-mobile-card__finance {
  align-items: flex-end;
  text-align: right;
}

.sale-order-mobile-card__finance-label {
  color: var(--el-text-color-secondary);
}

.sale-order-mobile-card__finance-value {
  color: var(--el-text-color-primary);
  font-size: 24px;
  line-height: 1.05;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.sale-order-mobile-card__product {
  margin-top: 14px;
}

.sale-order-mobile-card__metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.sale-order-mobile-card__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-light);
}

.sale-order-mobile-card__metric-top {
  justify-content: space-between;
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 18px;
}

.sale-order-mobile-card__metric-top strong {
  color: var(--el-text-color-primary);
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.sale-order-mobile-card__reject {
  margin-top: 12px;
  color: var(--sale-danger);
  font-size: 12px;
  line-height: 18px;
}

.sale-order-mobile-card__actions {
  justify-content: flex-end;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.sale-order-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
}

.sale-order-page__record-count {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 20px;
}

.sale-order-query-collapse-enter-active,
.sale-order-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.sale-order-query-collapse-enter-from,
.sale-order-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1359px) {
  .sale-order-query__grid--primary,
  .sale-order-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sale-order-page__title {
    font-size: 24px;
  }

  .ledger-finance__amount {
    font-size: 18px;
  }
}

@media (max-width: 1179px) {
  .sale-order-mobile-card__summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-mobile-card__finance {
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 1023px) {
  .sale-order-query__grid--primary,
  .sale-order-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-query__footer,
  .sale-order-toolbar,
  .sale-order-page__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .sale-order-query__actions,
  .sale-order-toolbar__actions,
  .sale-order-toolbar__meta {
    width: 100%;
  }

  .sale-order-query__actions :deep(.el-button),
  .sale-order-toolbar__actions :deep(.el-button),
  .sale-order-toolbar__meta :deep(.el-button) {
    flex: 1;
  }

  .sale-order-mobile-card {
    padding: 14px;
  }

  .sale-order-mobile-card__metrics {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 767px) {
  .sale-order-mobile-card__status,
  .sale-order-mobile-card__actions {
    justify-content: flex-start;
  }

  .sale-order-mobile-card__finance-value {
    font-size: 22px;
  }
}
</style>
