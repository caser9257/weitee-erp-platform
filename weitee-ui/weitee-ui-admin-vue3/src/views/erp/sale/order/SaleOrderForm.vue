<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="min(1280px, calc(100vw - 24px))"
    class="sale-order-dialog"
  >
    <el-form
      v-if="!isDetailMode"
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-position="top"
      label-width="auto"
      v-loading="formLoading"
      :disabled="disabled"
      class="sale-order-form"
    >
      <el-alert
        v-if="showRejectReminder"
        class="mb-16px"
        type="warning"
        :closable="false"
        show-icon
        :title="`最近驳回原因：${formData.lastRejectReason}`"
        :description="
          formData.lastRejectTime
            ? `驳回时间：${formatDate(formData.lastRejectTime, 'YYYY-MM-DD')}`
            : ''
        "
      />
      <el-alert
        v-if="showApprovalRunningReminder"
        class="mb-16px"
        type="info"
        :closable="false"
        show-icon
        title="当前销售订单正在审批中，暂不可编辑。如需调整，请先回到列表页撤回审批。"
      />
      <el-alert
        v-else-if="showReadonlyReminder"
        class="mb-16px"
        type="success"
        :closable="false"
        show-icon
        title="当前销售订单已审批完成，仅支持查看。"
      />
      <div v-if="showProcessLink" class="mb-16px text-right">
        <el-button link type="primary" @click="openProcessDetail">查看审批流程</el-button>
      </div>
      <ContentWrap
        class="sale-order-section sale-order-section--base"
        title="基础信息"
        :body-style="{ padding: '16px 18px 10px' }"
      >
        <template #header>
          <div class="sale-order-section__header-main">
            <el-tag
              v-if="formType !== 'create'"
              :type="resolveDeliveryReadyTagType(formData.deliveryReadyStatus)"
              effect="light"
              size="small"
            >
              {{ resolveDeliveryReadyLabel(formData.deliveryReadyStatus) }}
            </el-tag>
            <el-tag effect="plain" size="small" type="info">
              明细 {{ formData.items?.length || 0 }} 行
            </el-tag>
          </div>
        </template>
        <el-row :gutter="20" class="sale-order-grid sale-order-grid--base">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="订单单号" prop="no">
              <el-input disabled v-model="formData.no" placeholder="保存时自动生成" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="订单时间" prop="orderTime">
              <el-date-picker
                v-model="formData.orderTime"
                type="date"
                value-format="x"
                placeholder="选择订单时间"
                class="!w-1/1"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                clearable
                filterable
                placeholder="请选择客户"
                class="!w-1/1"
              >
                <el-option
                  v-for="item in customerList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="项目" prop="projectId">
              <el-select
                v-model="formData.projectId"
                clearable
                filterable
                placeholder="请选择项目"
                class="!w-1/1"
              >
                <el-option
                  v-for="item in projectList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="业务类型" prop="businessType">
              <el-select
                v-model="formData.businessType"
                clearable
                placeholder="请选择业务类型"
                class="!w-1/1"
              >
                <el-option
                  v-for="item in businessTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="交期" prop="deliveryDate">
              <el-date-picker
                v-model="formData.deliveryDate"
                type="date"
                value-format="x"
                placeholder="选择交期"
                class="!w-1/1"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="结算类型" prop="settlementType">
              <el-select
                v-model="formData.settlementType"
                placeholder="请选择结算类型"
                class="!w-1/1"
              >
                <el-option
                  v-for="item in settlementTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="销售人员" prop="saleUserId">
              <el-select
                v-model="formData.saleUserId"
                clearable
                filterable
                placeholder="请选择销售人员"
                class="!w-1/1"
              >
                <el-option
                  v-for="item in userList"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="16">
            <el-form-item label="备注" prop="remark">
              <el-input
                type="textarea"
                v-model="formData.remark"
                :rows="2"
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="附件" prop="fileUrl">
              <UploadFile :is-show-tip="false" v-model="formData.fileUrl" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>
      </ContentWrap>
      <!-- 子表的表单 -->
      <ContentWrap
        class="sale-order-section sale-order-section--items"
        title="订单产品清单"
        :body-style="{ padding: '14px 16px 16px' }"
      >
        <div class="sale-order-items-shell">
          <div class="sale-order-items-toolbar">
            <div class="sale-order-items-toolbar__title">
              <span class="sale-order-items-toolbar__label">销售商品明细</span>
              <span class="sale-order-items-toolbar__count"
                >{{ formData.items?.length || 0 }} 行</span
              >
            </div>
          </div>
          <SaleOrderItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
        </div>
      </ContentWrap>
      <ContentWrap
        class="sale-order-section sale-order-section--settlement"
        title="结算信息"
        :body-style="{ padding: '16px 18px 18px' }"
      >
        <el-row :gutter="20" class="sale-order-settlement">
          <el-col :xs="24" :xl="16" class="sale-order-settlement__form">
            <el-row :gutter="20" class="sale-order-settlement__fields">
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="优惠率（%）" prop="discountPercent">
                  <el-input-number
                    v-model="formData.discountPercent"
                    controls-position="right"
                    :min="0"
                    :precision="2"
                    placeholder="请输入优惠率"
                    class="!w-1/1"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="收款优惠" prop="discountPrice">
                  <el-input
                    disabled
                    v-model="formData.discountPrice"
                    :formatter="erpPriceInputFormatter"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="优惠后金额">
                  <el-input
                    disabled
                    v-model="formData.totalPrice"
                    :formatter="erpPriceInputFormatter"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="结算账户" prop="accountId">
                  <el-select
                    v-model="formData.accountId"
                    clearable
                    filterable
                    placeholder="请选择结算账户"
                    class="!w-1/1"
                  >
                    <el-option
                      v-for="item in accountList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="收取订金" prop="depositPrice">
                  <el-input-number
                    v-model="formData.depositPrice"
                    controls-position="right"
                    :min="0"
                    :precision="2"
                    placeholder="请输入收取订金"
                    class="!w-1/1"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
          <el-col :xs="24" :xl="8" class="sale-order-settlement__summary-col">
            <div class="sale-order-settlement__summary">
              <div class="sale-order-summary-card">
                <div class="sale-order-summary-card__label">应收金额</div>
                <div class="sale-order-summary-card__value">
                  {{ erpPriceInputFormatter(formData.totalPrice) }}
                </div>
                <div class="sale-order-summary-card__meta">
                  <div class="sale-order-summary-card__meta-item">
                    <span>商品合计</span>
                    <strong>{{ erpPriceInputFormatter(productSubtotal) }}</strong>
                  </div>
                  <div class="sale-order-summary-card__meta-item">
                    <span>优惠金额</span>
                    <strong>{{ erpPriceInputFormatter(formData.discountPrice) }}</strong>
                  </div>
                  <div class="sale-order-summary-card__meta-item">
                    <span>订金</span>
                    <strong>{{ erpPriceInputFormatter(formData.depositPrice) }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </ContentWrap>
      <ContentWrap v-if="showAuditHistory" title="审批历史" class="mt-16px">
        <el-empty v-if="displayAuditLogs.length === 0" description="暂无审批历史" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(item, index) in displayAuditLogs"
            :key="index"
            :timestamp="formatAuditLogTime(item)"
            placement="top"
          >
            <div class="text-13px leading-22px">
              <div class="mb-4px">
                <span class="text-gray-500">动作：</span>
                <el-tag size="small" :type="resolveAuditTagType(item.actionType)">
                  {{ formatAuditAction(item.actionType) }}
                </el-tag>
              </div>
              <div class="mb-4px">
                <span class="text-gray-500">操作人：</span>
                <span>{{ formatAuditLogUser(item) }}</span>
              </div>
              <div v-if="item.beforeStatus != null || item.afterStatus != null" class="mb-4px">
                <span class="text-gray-500">状态变化：</span>
                <span
                  >{{ formatAuditStatus(item.beforeStatus) }} ->
                  {{ formatAuditStatus(item.afterStatus) }}</span
                >
              </div>
              <div v-if="item.reason">
                <span class="text-gray-500">说明：</span>
                <span>{{ item.reason }}</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </ContentWrap>
    </el-form>
    <div v-else v-loading="formLoading" class="sale-order-detail">
      <section class="sale-order-detail__hero">
        <div class="sale-order-detail__hero-main">
          <div class="sale-order-detail__eyebrow">销售订单</div>
          <div class="sale-order-detail__title-row">
            <h2 class="sale-order-detail__title">{{ formData.no || `销售订单 #${formData.id}` }}</h2>
            <el-tag
              size="small"
              effect="light"
              :type="resolveDeliveryReadyTagType(formData.deliveryReadyStatus)"
            >
              {{ resolveDeliveryReadyLabel(formData.deliveryReadyStatus) }}
            </el-tag>
          </div>
          <div class="sale-order-detail__meta">
            <span>客户：{{ resolveDisplayText(formData.customerName) }}</span>
            <span>项目：{{ resolveDisplayText(formData.projectName) }}</span>
            <span>销售人员：{{ resolveDisplayText(resolveSaleUserName()) }}</span>
            <span>订单时间：{{ formatDisplayDate(formData.orderTime) }}</span>
          </div>
        </div>
        <div class="sale-order-detail__hero-side">
          <div class="sale-order-detail__status-group">
            <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="formData.status" />
            <el-button
              v-if="showProcessLink"
              link
              type="primary"
              class="sale-order-detail__process-link"
              @click="openProcessDetail"
            >
              查看审批流程
            </el-button>
          </div>
          <div class="sale-order-detail__hero-metrics">
            <div class="sale-order-detail__hero-metric">
              <span>应收金额</span>
              <strong>{{ erpPriceInputFormatter(formData.totalPrice) }}</strong>
            </div>
            <div class="sale-order-detail__hero-metric">
              <span>商品行数</span>
              <strong>{{ formatInteger(formData.items?.length || 0) }}</strong>
            </div>
            <div class="sale-order-detail__hero-metric">
              <span>剩余待出库</span>
              <strong>{{ formatCountValue(remainingOutCount) }}</strong>
            </div>
          </div>
        </div>
      </section>

      <div
        v-if="displayLatestRejectReason"
        class="sale-order-detail__risk-banner"
      >
        <div class="sale-order-detail__risk-title">最近驳回</div>
        <div class="sale-order-detail__risk-text">
          {{ displayLatestRejectReason }}
        </div>
        <div v-if="displayLatestRejectTime" class="sale-order-detail__risk-time">
          {{ displayLatestRejectTime }}
        </div>
      </div>

      <section class="sale-order-detail__content">
        <div class="sale-order-detail__main">
          <ContentWrap
            class="sale-order-detail-card sale-order-detail-card--base"
            title="基础信息"
            :body-style="{ padding: '18px 20px 20px' }"
          >
            <div class="sale-order-detail-grid">
              <div
                v-for="item in detailInfoItems"
                :key="item.label"
                class="sale-order-detail-field"
                :class="{ 'sale-order-detail-field--wide': item.wide }"
              >
                <div class="sale-order-detail-field__label">{{ item.label }}</div>
                <div class="sale-order-detail-field__value" :title="item.value">
                  {{ item.value }}
                </div>
              </div>
            </div>
          </ContentWrap>

          <ContentWrap
            class="sale-order-detail-card sale-order-detail-card--tabs"
            :body-style="{ padding: '0' }"
          >
            <div class="sale-order-detail-tabs">
              <button
                type="button"
                class="sale-order-detail-tabs__item"
                :class="{ 'is-active': detailActiveTab === 'items' }"
                @click="detailActiveTab = 'items'"
              >
                订单商品明细
                <span class="sale-order-detail-tabs__count">
                  ({{ formatInteger(formData.items?.length || 0) }})
                </span>
              </button>
              <button
                type="button"
                class="sale-order-detail-tabs__item"
                :class="{ 'is-active': detailActiveTab === 'audit' }"
                @click="detailActiveTab = 'audit'"
              >
                审批流转记录
              </button>
            </div>

            <div v-if="detailActiveTab === 'items'" class="sale-order-detail-items">
              <el-empty
                v-if="!formData.items || formData.items.length === 0"
                description="暂无商品明细"
              />
              <div v-else class="sale-order-detail-ledger">
                <div class="sale-order-detail-ledger__head">
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--index">
                    #
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--product">
                    商品摘要
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    可用库存
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    下单数量
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    销售单价
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    金额（无税）
                  </div>
                  <div
                    class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number sale-order-detail-ledger__cell--gross"
                  >
                    价税合计
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--remark">
                    备注说明
                  </div>
                </div>

                <div
                  v-for="(item, index) in formData.items"
                  :key="item.id || `${item.productId || 'item'}-${index}`"
                  class="sale-order-detail-ledger__row"
                >
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--index">
                    {{ index + 1 }}
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--product">
                    <div class="sale-order-detail-ledger__product-name">
                      {{ resolveLedgerProductName(item) }}
                    </div>
                    <div class="sale-order-detail-ledger__product-meta">
                      {{ resolveLedgerBarCode(item) }}
                      <span class="sale-order-detail-ledger__dot">·</span>
                      {{ resolveLedgerUnitMeta(item) }}
                    </div>
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    {{ formatCountValue(item.stockCount) }}
                    {{ resolveLedgerUnitInline(item) }}
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    {{ formatCountValue(item.count) }}
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    {{ erpPriceInputFormatter(item.productPrice) }}
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number">
                    {{ erpPriceInputFormatter(resolveLedgerNetAmount(item)) }}
                  </div>
                  <div
                    class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--number sale-order-detail-ledger__cell--gross sale-order-detail-ledger__cell--highlight"
                  >
                    {{ erpPriceInputFormatter(resolveLedgerGrossAmount(item)) }}
                  </div>
                  <div class="sale-order-detail-ledger__cell sale-order-detail-ledger__cell--remark">
                    {{ resolveDisplayText(item.remark) }}
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="sale-order-detail-timeline">
              <el-empty v-if="displayAuditLogs.length === 0" description="暂无审批历史" />
              <el-timeline v-else>
                <el-timeline-item
                  v-for="(item, index) in displayAuditLogs"
                  :key="index"
                  :timestamp="formatAuditLogTime(item)"
                  placement="top"
                >
                  <div class="sale-order-detail-timeline__item">
                    <div class="sale-order-detail-timeline__row">
                      <span class="sale-order-detail-timeline__label">动作</span>
                      <el-tag size="small" :type="resolveAuditTagType(item.actionType)">
                        {{ formatAuditAction(item.actionType) }}
                      </el-tag>
                    </div>
                    <div class="sale-order-detail-timeline__row">
                      <span class="sale-order-detail-timeline__label">操作人</span>
                      <span>{{ formatAuditLogUser(item) }}</span>
                    </div>
                    <div
                      v-if="item.beforeStatus != null || item.afterStatus != null"
                      class="sale-order-detail-timeline__row"
                    >
                      <span class="sale-order-detail-timeline__label">状态变化</span>
                      <span>
                        {{ formatAuditStatus(item.beforeStatus) }} ->
                        {{ formatAuditStatus(item.afterStatus) }}
                      </span>
                    </div>
                    <div v-if="item.reason" class="sale-order-detail-timeline__reason">
                      {{ item.reason }}
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </ContentWrap>
        </div>

        <div class="sale-order-detail__aside">
          <ContentWrap
            class="sale-order-detail-card sale-order-detail-card--summary"
            title="财务结算"
            :body-style="{ padding: '18px' }"
          >
            <div class="sale-order-summary-panel">
              <div class="sale-order-summary-panel__amount">
                {{ erpPriceInputFormatter(formData.totalPrice) }}
              </div>
              <div class="sale-order-summary-panel__list">
                <div class="sale-order-summary-panel__item">
                  <span>商品合计</span>
                  <strong>{{ erpPriceInputFormatter(productSubtotal) }}</strong>
                </div>
                <div class="sale-order-summary-panel__item">
                  <span>税额合计</span>
                  <strong>{{ erpPriceInputFormatter(totalTaxAmount) }}</strong>
                </div>
                <div class="sale-order-summary-panel__item">
                  <span>优惠金额</span>
                  <strong>{{ erpPriceInputFormatter(formData.discountPrice) }}</strong>
                </div>
                <div class="sale-order-summary-panel__item">
                  <span>订金</span>
                  <strong>{{ erpPriceInputFormatter(formData.depositPrice) }}</strong>
                </div>
                <div class="sale-order-summary-panel__item">
                  <span>结算账户</span>
                  <strong>{{ resolveDisplayText(resolveAccountName()) }}</strong>
                </div>
              </div>
            </div>
          </ContentWrap>

          <ContentWrap
            class="sale-order-detail-card sale-order-detail-card--delivery"
            title="交付状态"
            :body-style="{ padding: '18px' }"
          >
            <div class="sale-order-delivery-panel">
              <div class="sale-order-delivery-panel__badge">
                <el-tag
                  size="small"
                  effect="light"
                  :type="resolveDeliveryReadyTagType(formData.deliveryReadyStatus)"
                >
                  {{ resolveDeliveryReadyLabel(formData.deliveryReadyStatus) }}
                </el-tag>
              </div>
              <div class="sale-order-delivery-panel__metrics">
                <div class="sale-order-delivery-panel__metric">
                  <span>订单总数</span>
                  <strong>{{ formatCountValue(formData.totalCount) }}</strong>
                </div>
                <div class="sale-order-delivery-panel__metric">
                  <span>已出库</span>
                  <strong>{{ formatCountValue(formData.outCount) }}</strong>
                </div>
                <div class="sale-order-delivery-panel__metric">
                  <span>已退货</span>
                  <strong>{{ formatCountValue(formData.returnCount) }}</strong>
                </div>
                <div class="sale-order-delivery-panel__metric">
                  <span>剩余待出库</span>
                  <strong>{{ formatCountValue(remainingOutCount) }}</strong>
                </div>
              </div>
            </div>
          </ContentWrap>
        </div>
      </section>
    </div>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading" v-if="!disabled">
        确定
      </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import {
  SaleOrderApi,
  SaleOrderAuditLogVO,
  SaleOrderRejectLogVO,
  SaleOrderVO
} from '@/api/erp/sale/order'
import SaleOrderItemForm from './components/SaleOrderItemForm.vue'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { erpPriceInputFormatter, erpPriceMultiply } from '@/utils'
import * as UserApi from '@/api/system/user'
import { ProjectApi } from '@/api/erp/project'

/** ERP 销售订单表单 */
defineOptions({ name: 'SaleOrderForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push } = useRouter()

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）提交时的数据加载；2）提交按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改；detail - 详情
const detailActiveTab = ref<'items' | 'audit'>('items')
const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const
const formData = ref<any>({
  id: undefined,
  customerId: undefined,
  projectId: undefined,
  deliveryDate: undefined,
  accountId: undefined,
  saleUserId: undefined,
  saleUserName: undefined,
  orderTime: undefined,
  remark: undefined,
  fileUrl: '',
  discountPercent: 0,
  discountPrice: 0,
  totalPrice: 0,
  depositPrice: 0,
  items: [],
  no: undefined,
  businessType: undefined,
  sourceProjectId: undefined,
  settlementType: undefined,
  sourceProductId: undefined,
  lastRejectReason: undefined,
  lastRejectTime: undefined,
  rejectLogs: [],
  auditLogs: []
})
const formRules = reactive({
  customerId: [{ required: true, message: '客户不能为空', trigger: 'blur' }],
  orderTime: [{ required: true, message: '订单时间不能为空', trigger: 'blur' }],
  businessType: [{ required: true, message: '业务类型不能为空', trigger: 'change' }],
  projectId: [
    {
      validator: (_rule, value, callback) => {
        if (formData.value.businessType === 'SELF_RESEARCH' && !value) {
          callback(new Error('自研业务请选择项目'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
})
const editable = computed(() => {
  if (formType.value === 'create') {
    return true
  }
  return (
    formData.value.status === 30 ||
    (formData.value.status === 10 && !formData.value.processInstanceId)
  )
})
const disabled = computed(
  () => formType.value === 'detail' || (formType.value !== 'create' && !editable.value)
)
const isDetailMode = computed(() => formType.value === 'detail')
const showRejectReminder = computed(
  () => formType.value === 'update' && !!formData.value.lastRejectReason
)
const showApprovalRunningReminder = computed(
  () =>
    formType.value === 'update' &&
    formData.value.status === 10 &&
    !!formData.value.processInstanceId
)
const showReadonlyReminder = computed(
  () => formType.value === 'update' && formData.value.status === 20
)
const showProcessLink = computed(() => !!formData.value.processInstanceId)
const showAuditHistory = computed(() => formType.value === 'detail')
const productSubtotal = computed(() => {
  return (formData.value.items || []).reduce((sum, item) => {
    return sum + Number(item.totalProductPrice || 0)
  }, 0)
})
const totalTaxAmount = computed(() => Number(formData.value.totalTaxPrice || 0))
const remainingOutCount = computed(() => {
  const totalCount = Number(formData.value.totalCount || 0)
  const outCount = Number(formData.value.outCount || 0)
  return Math.max(totalCount - outCount, 0)
})
const displayLatestRejectReason = computed(() => {
  if (formData.value.lastRejectReason) {
    return formData.value.lastRejectReason
  }
  return formData.value.rejectLogs?.[0]?.reason || ''
})
const displayLatestRejectTime = computed(() => {
  const value = formData.value.lastRejectTime || formData.value.rejectLogs?.[0]?.rejectTime
  return value ? formatDate(value, 'YYYY-MM-DD HH:mm') : ''
})
const businessTypeOptions = [
  { label: '自研', value: 'SELF_RESEARCH' },
  { label: '客供', value: 'CUSTOMER_SUPPLIED' },
  { label: '代工', value: 'TOLL_MANUFACTURING' }
]
const settlementTypeOptions = [
  { label: '产品销售', value: 'PRODUCT_SALE' },
  { label: '加工费', value: 'PROCESSING_FEE' }
]
const formRef = ref<any>() // 表单 Ref
const customerList = ref<CustomerVO[]>([]) // 客户列表
const projectList = ref<any[]>([]) // 项目列表
const accountList = ref<AccountVO[]>([]) // 账户列表
const userList = ref<UserApi.UserVO[]>([]) // 用户列表

const resolveDisplayText = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return '--'
  }
  return String(value)
}

const resolveLedgerProductName = (item?: {
  productId?: number
  productName?: string
}) => {
  if (item?.productName) {
    return item.productName
  }
  if (item?.productId !== undefined && item.productId !== null) {
    return `商品ID：${item.productId}`
  }
  return '商品信息待补'
}

const resolveLedgerBarCode = (item?: {
  productBarCode?: string
}) => {
  if (item?.productBarCode) {
    return `条码：${item.productBarCode}`
  }
  return '条码：待补'
}

const resolveLedgerUnitMeta = (item?: {
  productUnitId?: number
  productUnitName?: string
}) => {
  if (item?.productUnitName) {
    return `单位：${item.productUnitName}`
  }
  if (item?.productUnitId !== undefined && item.productUnitId !== null) {
    return `单位ID：${item.productUnitId}`
  }
  return '单位：待补'
}

const resolveLedgerUnitInline = (item?: {
  productUnitId?: number
  productUnitName?: string
}) => {
  if (item?.productUnitName) {
    return item.productUnitName
  }
  if (item?.productUnitId !== undefined && item.productUnitId !== null) {
    return `ID ${item.productUnitId}`
  }
  return '--'
}

const resolveLedgerGrossAmount = (item?: {
  totalPrice?: number | string | null
  totalProductPrice?: number | string | null
  taxPrice?: number | string | null
}) => {
  const grossAmount = Number(item?.totalPrice ?? 0)
  if (grossAmount > 0) {
    return grossAmount
  }
  const netAmount = Number(item?.totalProductPrice ?? 0)
  const taxAmount = Number(item?.taxPrice ?? 0)
  if (netAmount > 0 || taxAmount > 0) {
    return netAmount + taxAmount
  }
  return 0
}

const resolveLedgerNetAmount = (item?: {
  totalPrice?: number | string | null
  totalProductPrice?: number | string | null
  taxPrice?: number | string | null
}) => {
  const netAmount = Number(item?.totalProductPrice ?? 0)
  if (netAmount > 0) {
    return netAmount
  }
  const grossAmount = Number(item?.totalPrice ?? 0)
  const taxAmount = Number(item?.taxPrice ?? 0)
  if (grossAmount > 0 || taxAmount > 0) {
    return Math.max(grossAmount - taxAmount, 0)
  }
  return 0
}

const formatDisplayDate = (value?: string | number | Date | null) => {
  if (!value) {
    return '--'
  }
  return formatDate(value, 'YYYY-MM-DD')
}

const formatInteger = (value?: number | string | null) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

const formatCountValue = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const resolveBusinessTypeLabel = (value?: string) => {
  return (
    businessTypeOptions.find((item) => item.value === value)?.label || resolveDisplayText(value)
  )
}

const resolveSettlementTypeLabel = (value?: string) => {
  return (
    settlementTypeOptions.find((item) => item.value === value)?.label ||
    resolveDisplayText(value)
  )
}

const resolveSaleUserName = () => {
  if (formData.value.saleUserName) {
    return formData.value.saleUserName
  }
  const user = userList.value.find((item) => item.id === formData.value.saleUserId)
  return user?.nickname
}

const resolveAccountName = () => {
  const account = accountList.value.find((item) => item.id === formData.value.accountId)
  return account?.name
}

const detailInfoItems = computed(() => [
  { label: '订单单号', value: resolveDisplayText(formData.value.no) },
  { label: '订单时间', value: formatDisplayDate(formData.value.orderTime) },
  { label: '客户', value: resolveDisplayText(formData.value.customerName) },
  { label: '项目', value: resolveDisplayText(formData.value.projectName) },
  { label: '业务类型', value: resolveBusinessTypeLabel(formData.value.businessType) },
  { label: '结算类型', value: resolveSettlementTypeLabel(formData.value.settlementType) },
  { label: '销售人员', value: resolveDisplayText(resolveSaleUserName()) },
  { label: '交期', value: formatDisplayDate(formData.value.deliveryDate) },
  { label: '附件', value: resolveDisplayText(formData.value.fileUrl), wide: true },
  { label: '备注', value: resolveDisplayText(formData.value.remark), wide: true }
])

/** 子表的表单 */
const itemFormRef = ref<any>()

/** 计算 discountPrice、totalPrice 价格 */
watch(
  () => formData.value,
  (val) => {
    if (!val) {
      return
    }
    const totalPrice = val.items.reduce((prev, curr) => prev + curr.totalPrice, 0)
    const discountPrice =
      val.discountPercent != null ? erpPriceMultiply(totalPrice, val.discountPercent / 100.0) : 0
    formData.value.discountPrice = discountPrice
    formData.value.totalPrice = totalPrice - discountPrice
  },
  { deep: true }
)

watch(
  () => formData.value.businessType,
  (businessType) => {
    if (businessType === 'TOLL_MANUFACTURING') {
      formData.value.settlementType = 'PROCESSING_FEE'
      return
    }
    if (!businessType) {
      formData.value.settlementType = undefined
      return
    }
    if (!formData.value.settlementType || formData.value.settlementType === 'PROCESSING_FEE') {
      formData.value.settlementType = 'PRODUCT_SALE'
    }
  }
)

const actionTextMap: Record<string, string> = {
  APPROVE: '审批通过',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  REVERSE_APPROVE: '反审核'
}

const mapRejectLogsToAuditLogs = (rejectLogs: SaleOrderRejectLogVO[] = []) => {
  return rejectLogs.map((item) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  })) as SaleOrderAuditLogVO[]
}

const displayAuditLogs = computed(() => {
  const auditLogs = formData.value.auditLogs || []
  if (auditLogs.length) {
    return auditLogs
  }
  return mapRejectLogsToAuditLogs(formData.value.rejectLogs || [])
})

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

const formatAuditLogTime = (item: SaleOrderAuditLogVO) => {
  return item.createTime ? formatDate(item.createTime, 'YYYY-MM-DD') : '-'
}

const formatAuditLogUser = (item: SaleOrderAuditLogVO) => {
  return item.operatorNickname || item.operatorName || '未知'
}

const formatAuditAction = (actionType?: string) => {
  return (actionType && actionTextMap[actionType]) || actionType || '-'
}

const resolveAuditTagType = (actionType?: string) => {
  if (actionType === 'APPROVE') {
    return 'success'
  }
  if (actionType === 'REJECT') {
    return 'danger'
  }
  if (actionType === 'RESUBMIT') {
    return 'warning'
  }
  return 'info'
}

const formatAuditStatus = (status?: number) => {
  if (status == null) {
    return '-'
  }
  const dict = getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS).find(
    (item) => Number(item.value) === status
  )
  return dict?.label || String(status)
}

const openProcessDetail = () => {
  if (!formData.value.processInstanceId) {
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: formData.value.processInstanceId
    }
  })
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  detailActiveTab.value = 'items'
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await SaleOrderApi.getSaleOrder(id)
    } finally {
      formLoading.value = false
    }
  }
  customerList.value = await CustomerApi.getCustomerSimpleList()
  projectList.value = await ProjectApi.getProjectSimpleList()
  userList.value = await UserApi.getSimpleUserList()
  accountList.value = await AccountApi.getAccountSimpleList()
  const defaultAccount = accountList.value.find((item) => item.defaultStatus)
  if (defaultAccount && !formData.value.accountId) {
    formData.value.accountId = defaultAccount.id
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  await formRef.value.validate()
  await itemFormRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as SaleOrderVO
    if (formType.value === 'create') {
      await SaleOrderApi.createSaleOrder(data)
      message.success(t('common.createSuccess'))
    } else {
      await SaleOrderApi.updateSaleOrder(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  detailActiveTab.value = 'items'
  formData.value = {
    id: undefined,
    customerId: undefined,
    customerName: undefined,
    projectId: undefined,
    projectName: undefined,
    businessType: undefined,
    sourceProjectId: undefined,
    settlementType: undefined,
    sourceProductId: undefined,
    deliveryDate: undefined,
    accountId: undefined,
    saleUserId: undefined,
    saleUserName: undefined,
    totalCount: 0,
    totalTaxPrice: 0,
    outCount: 0,
    returnCount: 0,
    orderTime: undefined,
    remark: undefined,
    fileUrl: undefined,
    discountPercent: 0,
    discountPrice: 0,
    totalPrice: 0,
    depositPrice: 0,
    items: [],
    no: undefined,
    processInstanceId: undefined,
    lastRejectReason: undefined,
    lastRejectTime: undefined,
    rejectLogs: [],
    auditLogs: []
  }
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.sale-order-form {
  position: relative;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: #475569;
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-form-item__content) {
    min-width: 0;
  }

  :deep(.el-form-item.is-required:not(.is-no-asterisked) > .el-form-item__label:before) {
    color: #f97316;
    margin-right: 4px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-textarea__inner),
  :deep(.el-input-number__wrapper) {
    min-height: 42px;
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.84);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease,
      background-color 0.2s ease;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-textarea__inner:focus),
  :deep(.el-input-number__wrapper:hover),
  :deep(.el-input__wrapper:hover),
  :deep(.el-select__wrapper:hover) {
    border-color: rgba(64, 158, 255, 0.55);
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.08);
  }

  :deep(.el-input.is-disabled .el-input__wrapper),
  :deep(.el-input-number.is-disabled .el-input-number__wrapper),
  :deep(.el-select.is-disabled .el-select__wrapper),
  :deep(.el-textarea.is-disabled .el-textarea__inner) {
    border-color: transparent;
    background: #f1f5f9;
    color: #64748b;
    box-shadow: none;
  }

  :deep(.el-input__inner),
  :deep(.el-input-number .el-input__inner),
  :deep(.el-textarea__inner) {
    color: var(--el-text-color-primary);
  }

  :deep(.el-input-number .el-input__inner) {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }

  :deep(.el-textarea__inner) {
    min-height: 84px;
  }
}

.sale-order-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sale-order-detail__hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px;
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 30%),
    linear-gradient(135deg, #0f172a, #1e293b 68%, #334155);
  color: #fff;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
}

.sale-order-detail__hero-main,
.sale-order-detail__hero-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.sale-order-detail__hero-main {
  flex: 1;
}

.sale-order-detail__eyebrow {
  color: rgba(191, 219, 254, 0.95);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.sale-order-detail__title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sale-order-detail__title {
  margin: 0;
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.15;
}

.sale-order-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: rgba(226, 232, 240, 0.9);
  font-size: 13px;
  line-height: 20px;
}

.sale-order-detail__hero-side {
  width: min(360px, 100%);
  align-items: stretch;
}

.sale-order-detail__status-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.sale-order-detail__process-link {
  color: #bfdbfe;
}

.sale-order-detail__hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.sale-order-detail__hero-metric {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sale-order-detail__hero-metric span {
  color: rgba(191, 219, 254, 0.92);
  font-size: 12px;
}

.sale-order-detail__hero-metric strong {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.sale-order-detail__risk-banner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 14px 18px;
  border-radius: 16px;
  border: 1px solid rgba(251, 191, 36, 0.38);
  background: linear-gradient(180deg, rgba(255, 251, 235, 0.98), rgba(255, 247, 237, 0.98));
}

.sale-order-detail__risk-title {
  color: #b45309;
  font-size: 13px;
  font-weight: 700;
}

.sale-order-detail__risk-text,
.sale-order-detail__risk-time {
  color: #92400e;
  font-size: 13px;
  line-height: 20px;
}

.sale-order-detail__risk-time {
  white-space: nowrap;
}

.sale-order-detail__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.sale-order-detail__main,
.sale-order-detail__aside {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.sale-order-detail-card {
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
  overflow: hidden;

  :deep(.el-card__header) {
    padding: 14px 18px;
    border-bottom: 1px solid rgba(226, 232, 240, 0.9);
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98));
  }
}

.sale-order-detail-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.sale-order-detail-field {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.92);
}

.sale-order-detail-field--wide {
  grid-column: span 2;
}

.sale-order-detail-field__label {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  line-height: 18px;
}

.sale-order-detail-field__value {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  word-break: break-word;
}

.sale-order-detail-tabs {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 22px;
  background: #fff;
  border-bottom: 1px solid rgba(226, 232, 240, 0.95);
}

.sale-order-detail-tabs__item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 48px;
  margin-right: 30px;
  padding: 0;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.sale-order-detail-tabs__item.is-active {
  color: #1677ff;
}

.sale-order-detail-tabs__item.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  border-radius: 999px;
  background: #1677ff;
}

.sale-order-detail-tabs__count {
  display: inline-block;
  min-width: auto;
  height: auto;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
}

.sale-order-detail-items {
  padding: 0;
}

.sale-order-detail-card--tabs {
  border-radius: 14px;
  box-shadow: none;
}

.sale-order-detail-card--tabs :deep(.el-card__header) {
  display: none;
}

.sale-order-detail-ledger {
  overflow-x: auto;
  overflow-y: hidden;
  border: none;
  border-radius: 0;
  background: #fff;
}

.sale-order-detail-ledger__head,
.sale-order-detail-ledger__row {
  display: grid;
  grid-template-columns:
    44px
    minmax(220px, 2fr)
    minmax(110px, 0.9fr)
    minmax(110px, 0.9fr)
    minmax(110px, 0.9fr)
    minmax(120px, 1fr)
    minmax(138px, 1.08fr)
    minmax(186px, 1.3fr);
  gap: 0;
  align-items: stretch;
  min-width: 1128px;
}

.sale-order-detail-ledger__head {
  background: #fff;
  border-bottom: 1px solid rgba(241, 245, 249, 0.96);
}

.sale-order-detail-ledger__row + .sale-order-detail-ledger__row {
  border-top: 1px solid rgba(241, 245, 249, 0.92);
}

.sale-order-detail-ledger__row:hover {
  background: rgba(248, 250, 252, 0.42);
}

.sale-order-detail-ledger__cell {
  min-width: 0;
  padding: 15px 12px;
  color: #0f172a;
  font-size: 13px;
  line-height: 20px;
  display: flex;
  align-items: center;
}

.sale-order-detail-ledger__head .sale-order-detail-ledger__cell {
  padding-top: 15px;
  padding-bottom: 15px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
}

.sale-order-detail-ledger__cell--index {
  justify-content: center;
  color: #cbd5e1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.sale-order-detail-ledger__cell--number {
  justify-content: flex-end;
  text-align: right;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 700;
  color: #475569;
}

.sale-order-detail-ledger__cell--highlight {
  color: #0891b2;
}

.sale-order-detail-ledger__cell--gross {
  padding-right: 22px;
}

.sale-order-detail-ledger__cell--remark {
  color: #475569;
  justify-content: flex-start;
  padding-left: 26px;
  border-left: 1px solid rgba(241, 245, 249, 0.96);
}

.sale-order-detail-ledger__cell--product {
  justify-content: flex-start;
  flex-direction: column;
  align-items: flex-start;
}

.sale-order-detail-ledger__product-name {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 19px;
}

.sale-order-detail-ledger__product-meta {
  margin-top: 2px;
  color: #94a3b8;
  font-size: 11px;
  line-height: 16px;
}

.sale-order-detail-ledger__dot {
  margin: 0 6px;
}

.sale-order-detail-timeline {
  padding: 18px 20px 20px;
}

.sale-order-detail-timeline__item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sale-order-detail-timeline__row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  color: #0f172a;
  font-size: 13px;
  line-height: 20px;
}

.sale-order-detail-timeline__label {
  color: #64748b;
  min-width: 56px;
}

.sale-order-detail-timeline__reason {
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  line-height: 20px;
}

.sale-order-summary-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sale-order-summary-panel__amount {
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid rgba(96, 165, 250, 0.22);
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.12), transparent 42%),
    linear-gradient(180deg, rgba(239, 246, 255, 0.98), rgba(255, 255, 255, 1));
  color: #2563eb;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.sale-order-summary-panel__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sale-order-summary-panel__item,
.sale-order-delivery-panel__metric {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.9);
  color: #475569;
  font-size: 13px;
}

.sale-order-summary-panel__item strong,
.sale-order-delivery-panel__metric strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.sale-order-delivery-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sale-order-delivery-panel__badge {
  display: flex;
  justify-content: flex-start;
}

.sale-order-delivery-panel__metrics {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sale-order-dialog {
  :deep(.el-dialog) {
    overflow: hidden;
    border: 1px solid rgba(226, 232, 240, 0.95);
    border-radius: 22px;
    background:
      radial-gradient(circle at top left, rgba(96, 165, 250, 0.07), transparent 28%),
      linear-gradient(180deg, rgba(255, 255, 255, 1), rgba(248, 250, 252, 0.98));
    box-shadow: 0 24px 60px rgba(15, 23, 42, 0.14);
  }

  :deep(.el-dialog__header) {
    padding: 18px 22px;
    border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  }

  :deep(.el-dialog__body) {
    max-height: min(78vh, 920px);
    padding: 18px 22px 12px;
    overflow: auto;
  }

  :deep(.el-dialog__footer) {
    padding: 14px 22px 20px;
    border-top: 1px solid rgba(226, 232, 240, 0.8);
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
  }
}

.sale-order-section {
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.05);

  :deep(.el-card__header) {
    padding: 14px 18px;
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98)), #fff;
    border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  }

  :deep(.el-card__body) {
    padding-top: 16px;
  }
}

.sale-order-section__header-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sale-order-section--base {
  :deep(.el-tag) {
    border-radius: 999px;
    letter-spacing: 0.04em;
  }
}

.sale-order-section--items {
  :deep(.el-card__body) {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.58), rgba(255, 255, 255, 0.96)), #fff;
  }
}

.sale-order-grid--base {
  margin-bottom: -2px;
}

.sale-order-items-shell {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sale-order-items-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 4px;
}

.sale-order-items-toolbar__title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sale-order-items-toolbar__label {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.sale-order-items-toolbar__count {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}

.sale-order-settlement {
  align-items: stretch;
}

.sale-order-settlement__form,
.sale-order-settlement__summary-col {
  display: flex;
}

.sale-order-settlement__fields,
.sale-order-settlement__summary {
  width: 100%;
}

.sale-order-summary-card {
  width: 100%;
  padding: 22px;
  border-radius: 20px;
  border: 1px solid rgba(96, 165, 250, 0.22);
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.12), transparent 42%),
    linear-gradient(180deg, rgba(239, 246, 255, 0.98), rgba(255, 255, 255, 1));
  box-shadow: 0 16px 34px rgba(37, 99, 235, 0.1);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sale-order-summary-card__label {
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.sale-order-summary-card__value {
  color: var(--el-color-primary);
  font-size: 34px;
  font-weight: 800;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.sale-order-summary-card__meta {
  display: grid;
  gap: 10px;
}

.sale-order-summary-card__meta-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.9);
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.sale-order-summary-card__meta-item strong {
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

:deep(.sale-order-section .el-card__body) {
  padding-bottom: 18px;
}

@media (max-width: 1280px) {
  .sale-order-detail__content {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-detail__aside {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sale-order-detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sale-order-summary-card__value {
    font-size: 30px;
  }

  .sale-order-dialog {
    :deep(.el-dialog__body) {
      max-height: 76vh;
    }
  }
}

@media (max-width: 768px) {
  .sale-order-dialog {
    :deep(.el-dialog) {
      width: calc(100vw - 16px) !important;
      border-radius: 18px;
    }

    :deep(.el-dialog__body) {
      padding: 14px 14px 8px;
      max-height: 82vh;
    }

    :deep(.el-dialog__header),
    :deep(.el-dialog__footer) {
      padding-left: 14px;
      padding-right: 14px;
    }
  }

  .sale-order-section {
    border-radius: 14px;
  }

  .sale-order-detail__hero {
    padding: 18px 16px;
    border-radius: 18px;
    flex-direction: column;
  }

  .sale-order-detail__title {
    font-size: 22px;
  }

  .sale-order-detail__hero-side {
    width: 100%;
  }

  .sale-order-detail__hero-metrics,
  .sale-order-detail__aside {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-detail__risk-banner {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .sale-order-detail-field--wide {
    grid-column: span 1;
  }

  .sale-order-detail-tabs {
    padding: 0 10px;
    overflow-x: auto;
  }

  .sale-order-detail-items,
  .sale-order-detail-timeline {
    padding: 0 0 14px;
  }

  .sale-order-detail-ledger__cell {
    padding: 14px 12px;
  }

  .sale-order-summary-card {
    padding: 16px;
  }

  .sale-order-summary-card__value {
    font-size: 28px;
  }

  .sale-order-items-toolbar,
  .sale-order-items-toolbar__title {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
