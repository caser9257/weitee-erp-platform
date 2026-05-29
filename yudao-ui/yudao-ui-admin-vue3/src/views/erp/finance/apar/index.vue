<template>
  <div class="finance-apar-page">
    <div class="finance-apar-page__summary-grid" v-loading="loadingSummary">
      <div
        v-for="card in summaryCards"
        :key="card.key"
        class="finance-apar-page__summary-card"
        :class="card.cardClass"
      >
        <div class="finance-apar-page__summary-head">
          <span class="finance-apar-page__summary-label">{{ card.label }}</span>
          <span class="finance-apar-page__summary-pill" :class="card.pillClass">
            {{ card.pillText }}
          </span>
        </div>
        <div class="finance-apar-page__summary-value" :class="card.valueClass">
          {{ card.value }}
        </div>
      </div>
    </div>

    <ContentWrap class="finance-apar-page__tabs-card">
      <el-tabs v-model="activeTab" class="finance-apar-page__tabs">
        <el-tab-pane label="应付台账" name="statement" />
        <el-tab-pane label="账龄分析" name="aging" />
        <el-tab-pane label="供应商对账" name="reconciliation" />
      </el-tabs>
    </ContentWrap>

    <ContentWrap class="finance-apar-page__filter-card">
      <template v-if="activeTab === 'statement'">
        <el-form
          ref="statementQueryFormRef"
          :model="statementQuery"
          label-width="80px"
          class="finance-apar-page__form"
        >
          <div class="finance-apar-page__card-header">
            <div class="finance-apar-page__card-title">基础筛选</div>
            <el-button link type="primary" @click="statementAdvancedExpanded = !statementAdvancedExpanded">
              <Icon :icon="statementAdvancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
              {{ statementAdvancedExpanded ? '收起高级搜索' : '展开高级搜索' }}
            </el-button>
          </div>
          <div class="finance-apar-page__form-grid finance-apar-page__form-grid--basic">
            <el-form-item label="台账编号" prop="statementNo">
              <el-input
                v-model="statementQuery.statementNo"
                placeholder="请输入台账编号"
                clearable
                @keyup.enter="handleStatementQuery"
              />
            </el-form-item>
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="statementQuery.supplierId"
                placeholder="请选择供应商"
                clearable
                filterable
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
            <el-form-item label="台账状态" prop="status">
              <el-select v-model="statementQuery.status" placeholder="请选择台账状态" clearable class="!w-full">
                <el-option
                  v-for="item in ERP_AP_STATEMENT_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="业务日期" prop="bizDate">
              <el-date-picker
                v-model="statementQuery.bizDate"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
                class="!w-full"
              />
            </el-form-item>
          </div>
          <div v-if="statementAdvancedExpanded" class="finance-apar-page__advanced-block">
            <div class="finance-apar-page__advanced-title">高级筛选</div>
            <div class="finance-apar-page__form-grid finance-apar-page__form-grid--advanced">
              <el-form-item label="业务类型" prop="bizType">
                <el-select v-model="statementQuery.bizType" placeholder="请选择业务类型" clearable class="!w-full">
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
                  v-model="statementQuery.bizNo"
                  placeholder="请输入业务单号"
                  clearable
                  @keyup.enter="handleStatementQuery"
                />
              </el-form-item>
              <el-form-item label="结算账户" prop="accountId">
                <el-select
                  v-model="statementQuery.accountId"
                  placeholder="请选择结算账户"
                  clearable
                  filterable
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
              <el-form-item label="收票状态" prop="invoiceStatus">
                <el-select
                  v-model="statementQuery.invoiceStatus"
                  placeholder="请选择收票状态"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="item in ERP_AP_INVOICE_STATUS_OPTIONS"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="到期日期" prop="dueDate">
                <el-date-picker
                  v-model="statementQuery.dueDate"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  type="daterange"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
                  class="!w-full"
                />
              </el-form-item>
            </div>
          </div>
          <div class="finance-apar-page__query-actions">
            <el-button :loading="loadingStatementList" @click="handleStatementQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button :disabled="loadingStatementList" @click="resetStatementQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>
      </template>

      <template v-else-if="activeTab === 'aging'">
        <el-form ref="agingQueryFormRef" :model="agingQuery" label-width="80px" class="finance-apar-page__form">
          <div class="finance-apar-page__card-header">
            <div class="finance-apar-page__card-title">基础筛选</div>
          </div>
          <div class="finance-apar-page__form-grid finance-apar-page__form-grid--compact">
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="agingQuery.supplierId"
                placeholder="请选择供应商"
                clearable
                filterable
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
            <el-form-item label="结算账户" prop="accountId">
              <el-select
                v-model="agingQuery.accountId"
                placeholder="请选择结算账户"
                clearable
                filterable
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
            <el-form-item label="统计日期" prop="asOfDate">
              <el-date-picker
                v-model="agingQuery.asOfDate"
                value-format="YYYY-MM-DD"
                type="date"
                placeholder="请选择统计日期"
                class="!w-full"
              />
            </el-form-item>
          </div>
          <div class="finance-apar-page__query-actions">
            <el-button :loading="loadingAging" @click="handleAgingQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button :disabled="loadingAging" @click="resetAgingQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>
      </template>

      <template v-else>
        <el-form
          ref="reconciliationQueryFormRef"
          :model="reconciliationQuery"
          label-width="80px"
          class="finance-apar-page__form"
        >
          <div class="finance-apar-page__card-header">
            <div class="finance-apar-page__card-title">基础筛选</div>
            <el-button
              link
              type="primary"
              @click="reconciliationAdvancedExpanded = !reconciliationAdvancedExpanded"
            >
              <Icon
                :icon="reconciliationAdvancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'"
                class="mr-5px"
              />
              {{ reconciliationAdvancedExpanded ? '收起高级搜索' : '展开高级搜索' }}
            </el-button>
          </div>
          <div class="finance-apar-page__form-grid finance-apar-page__form-grid--basic">
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="reconciliationQuery.supplierId"
                placeholder="请选择供应商"
                clearable
                filterable
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
            <el-form-item label="台账编号" prop="statementNo">
              <el-input
                v-model="reconciliationQuery.statementNo"
                placeholder="请输入台账编号"
                clearable
                @keyup.enter="handleReconciliationQuery"
              />
            </el-form-item>
            <el-form-item label="台账状态" prop="status">
              <el-select
                v-model="reconciliationQuery.status"
                placeholder="请选择台账状态"
                clearable
                class="!w-full"
              >
                <el-option
                  v-for="item in ERP_AP_STATEMENT_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="业务日期" prop="bizDate">
              <el-date-picker
                v-model="reconciliationQuery.bizDate"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
                class="!w-full"
              />
            </el-form-item>
          </div>
          <div v-if="reconciliationAdvancedExpanded" class="finance-apar-page__advanced-block">
            <div class="finance-apar-page__advanced-title">高级筛选</div>
            <div class="finance-apar-page__form-grid finance-apar-page__form-grid--advanced">
              <el-form-item label="业务类型" prop="bizType">
                <el-select
                  v-model="reconciliationQuery.bizType"
                  placeholder="请选择业务类型"
                  clearable
                  class="!w-full"
                >
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
                  v-model="reconciliationQuery.bizNo"
                  placeholder="请输入业务单号"
                  clearable
                  @keyup.enter="handleReconciliationQuery"
                />
              </el-form-item>
            </div>
          </div>
          <div class="finance-apar-page__query-actions">
            <el-button :loading="loadingReconciliation" @click="handleReconciliationQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button :disabled="loadingReconciliation" @click="resetReconciliationQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>
      </template>
    </ContentWrap>

    <ContentWrap class="finance-apar-page__table-card">
      <template v-if="activeTab === 'statement'">
        <div class="finance-apar-page__card-header finance-apar-page__card-header--table">
          <div class="finance-apar-page__card-title">台账记录</div>
          <div class="finance-apar-page__toolbar-actions">
            <el-button
              type="success"
              plain
              :loading="exportLoading"
              :disabled="loadingStatementList"
              @click="handleExportStatement"
            >
              <Icon icon="ep:download" class="mr-5px" />
              导出台账
            </el-button>
          </div>
        </div>

        <div v-if="statementErrorMessage && !statementList.length" class="finance-apar-page__state">
          <div class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="finance-shell__empty-title">台账加载失败</div>
            <div class="finance-shell__empty-desc">{{ statementErrorMessage }}</div>
            <el-button type="primary" plain :disabled="loadingStatementList" @click="reloadStatementTab">
              重试
            </el-button>
          </div>
        </div>
        <template v-else>
          <div v-if="loadingStatementList || statementList.length" class="finance-apar-page__table-wrap">
            <el-table
              v-loading="loadingStatementList"
              :data="statementList"
              stripe
              class="finance-apar-page__table"
            >
              <el-table-column label="台账信息" min-width="240">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.statementNo || '-' }}</span>
                    <div class="finance-apar-page__cell-tags">
                      <span class="finance-apar-page__mono-chip">{{ getBizTypeLabel(row.bizType) }}</span>
                      <span class="finance-apar-page__cell-muted" :title="row.sourceOrderNo || '-'">
                        {{ row.sourceOrderNo || '未关联来源采购单' }}
                      </span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="供应商与业务" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.supplierName || '-' }}</span>
                    <span class="finance-apar-page__cell-muted" :title="row.bizNo || '-'">
                      {{ row.bizNo || '未关联业务单号' }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="结算与日期" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.accountName || '-' }}</span>
                    <span class="finance-apar-page__cell-muted">
                      业务日期 {{ formatShortDate(row.bizDate) || '-' }}
                    </span>
                    <span class="finance-apar-page__cell-muted">
                      到期日期 {{ formatShortDate(row.dueDate) || '-' }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="amount"
                label="应付金额"
                min-width="130"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
              <el-table-column
                prop="paidAmount"
                label="已核销金额"
                min-width="130"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
              <el-table-column
                prop="remainAmount"
                label="剩余金额"
                min-width="130"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
              <el-table-column label="收票状态" min-width="110" align="center">
                <template #default="{ row }">
                  <span class="finance-apar-page__badge" :class="getInvoiceBadgeClass(row.invoiceStatus)">
                    <span class="finance-apar-page__badge-dot"></span>
                    {{ getInvoiceStatusLabel(row.invoiceStatus) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="台账状态" min-width="110" align="center">
                <template #default="{ row }">
                  <span class="finance-apar-page__badge" :class="getStatementBadgeClass(row.status)">
                    <span class="finance-apar-page__badge-dot"></span>
                    {{ getStatementStatusLabel(row.status) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="168" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
                  <el-button link type="primary" @click="openInvoiceDialog(row.id)">收票登记</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:document" />
            </div>
            <div class="finance-shell__empty-title">暂无应付台账数据</div>
          </div>

          <Pagination
            v-if="statementTotal > 0"
            v-model:page="statementQuery.pageNo"
            v-model:limit="statementQuery.pageSize"
            :total="statementTotal"
            @pagination="getStatementList"
          />
        </template>
      </template>

      <template v-else-if="activeTab === 'aging'">
        <div class="finance-apar-page__card-header finance-apar-page__card-header--table">
          <div class="finance-apar-page__card-title">账龄分布</div>
        </div>

        <div v-if="agingErrorMessage && !agingList.length" class="finance-apar-page__state">
          <div class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="finance-shell__empty-title">账龄分析加载失败</div>
            <div class="finance-shell__empty-desc">{{ agingErrorMessage }}</div>
            <el-button type="primary" plain :disabled="loadingAging" @click="getAgingList">重试</el-button>
          </div>
        </div>
        <template v-else>
          <div v-if="loadingAging || agingList.length" class="finance-apar-page__table-wrap">
            <el-table v-loading="loadingAging" :data="agingList" stripe class="finance-apar-page__table">
              <el-table-column label="供应商" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.supplierName || '-' }}</span>
                    <span class="finance-apar-page__cell-muted">
                      风险等级 {{ getAgingRiskLabel(row) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="账龄分布" min-width="420">
                <template #default="{ row }">
                  <div class="finance-apar-page__aging-stack">
                    <div
                      v-for="segment in buildAgingSegments(row)"
                      :key="segment.label"
                      class="finance-apar-page__aging-line"
                    >
                      <span class="finance-apar-page__aging-label">{{ segment.label }}</span>
                      <div class="finance-apar-page__aging-bar-track">
                        <div
                          class="finance-apar-page__aging-bar-fill"
                          :class="segment.barClass"
                          :style="{ width: `${segment.widthPercent}%` }"
                        ></div>
                      </div>
                      <span class="finance-apar-page__aging-value font-mono">
                        {{ erpPriceInputFormatter(segment.value) }}
                      </span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="totalRemainAmount"
                label="剩余合计"
                min-width="150"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
            </el-table>
          </div>

          <div v-else class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:document" />
            </div>
            <div class="finance-shell__empty-title">暂无账龄分析数据</div>
          </div>
        </template>
      </template>

      <template v-else>
        <div class="finance-apar-page__card-header finance-apar-page__card-header--table">
          <div class="finance-apar-page__card-title">供应商对账</div>
        </div>

        <div v-if="reconciliationErrorMessage && !reconciliationList.length" class="finance-apar-page__state">
          <div class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="finance-shell__empty-title">供应商对账加载失败</div>
            <div class="finance-shell__empty-desc">{{ reconciliationErrorMessage }}</div>
            <el-button type="primary" plain :disabled="loadingReconciliation" @click="getReconciliationList">
              重试
            </el-button>
          </div>
        </div>
        <template v-else>
          <div v-if="loadingReconciliation || reconciliationList.length" class="finance-apar-page__table-wrap">
            <el-table
              v-loading="loadingReconciliation"
              :data="reconciliationList"
              stripe
              class="finance-apar-page__table"
            >
              <el-table-column label="台账信息" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.statementNo || '-' }}</span>
                    <div class="finance-apar-page__cell-tags">
                      <span class="finance-apar-page__mono-chip">{{ getBizTypeLabel(row.bizType) }}</span>
                      <span class="finance-apar-page__cell-muted" :title="row.sourceOrderNo || '-'">
                        {{ row.sourceOrderNo || '未关联来源采购单' }}
                      </span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="供应商与业务" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__cell-group">
                    <span class="finance-apar-page__cell-title">{{ row.supplierName || '-' }}</span>
                    <span class="finance-apar-page__cell-muted" :title="row.bizNo || '-'">
                      {{ row.bizNo || '未关联业务单号' }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="对账进度" min-width="220">
                <template #default="{ row }">
                  <div class="finance-apar-page__progress-card">
                    <div class="finance-apar-page__progress-meta">
                      <span class="finance-apar-page__cell-muted">业务日期 {{ formatShortDate(row.bizDate) || '-' }}</span>
                      <span class="finance-apar-page__badge" :class="getStatementBadgeClass(row.status)">
                        <span class="finance-apar-page__badge-dot"></span>
                        {{ getStatementStatusLabel(row.status) }}
                      </span>
                    </div>
                    <div class="finance-apar-page__progress-track">
                      <div
                        class="finance-apar-page__progress-fill"
                        :style="{ width: `${getPaidRate(row)}%` }"
                      ></div>
                    </div>
                    <div class="finance-apar-page__progress-values">
                      <span class="font-mono">已核销 {{ erpPriceInputFormatter(row.paidAmount) }}</span>
                      <span class="font-mono">剩余 {{ erpPriceInputFormatter(row.remainAmount) }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="amount"
                label="应付金额"
                min-width="130"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
              <el-table-column
                prop="remainAmount"
                label="剩余金额"
                min-width="130"
                align="right"
                class-name="font-mono"
                :formatter="erpPriceTableColumnFormatter"
              />
            </el-table>
          </div>

          <div v-else class="finance-shell__empty">
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:document" />
            </div>
            <div class="finance-shell__empty-title">暂无供应商对账数据</div>
          </div>

          <Pagination
            v-if="reconciliationTotal > 0"
            v-model:page="reconciliationQuery.pageNo"
            v-model:limit="reconciliationQuery.pageSize"
            :total="reconciliationTotal"
            @pagination="getReconciliationList"
          />
        </template>
      </template>
    </ContentWrap>

    <ApStatementDetailDialog ref="detailDialogRef" />
    <ApStatementInvoiceDialog ref="invoiceDialogRef" @success="handleInvoiceSuccess" />
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import {
  ApStatementApi,
  type ApStatementAgingReqVO,
  type ApStatementAgingVO,
  type ApStatementPageReqVO,
  type ApStatementReconciliationReqVO,
  type ApStatementReconciliationVO,
  type ApStatementSummaryVO,
  type ApStatementVO,
  ERP_AP_BIZ_TYPE_OPTIONS,
  ERP_AP_INVOICE_STATUS_OPTIONS,
  ERP_AP_STATEMENT_STATUS_OPTIONS
} from '@/api/erp/finance/apar'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import { erpPriceInputFormatter, erpPriceTableColumnFormatter } from '@/utils'
import download from '@/utils/download'
import { formatDate } from '@/utils/formatTime'
import ApStatementDetailDialog from './ApStatementDetailDialog.vue'
import ApStatementInvoiceDialog from './ApStatementInvoiceDialog.vue'

defineOptions({ name: 'ErpFinanceApar' })

type TabName = 'statement' | 'aging' | 'reconciliation'

interface AgingSegment {
  label: string
  value: number
  widthPercent: number
  barClass: string
}

const activeTab = ref<TabName>('statement')
const message = useMessage()
const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'

const statementAdvancedExpanded = ref(false)
const reconciliationAdvancedExpanded = ref(false)

const loadingStatementList = ref(false)
const loadingSummary = ref(false)
const loadingAging = ref(false)
const loadingReconciliation = ref(false)
const exportLoading = ref(false)

const statementErrorMessage = ref('')
const agingErrorMessage = ref('')
const reconciliationErrorMessage = ref('')

const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])

const statementList = ref<ApStatementVO[]>([])
const statementTotal = ref(0)
const summaryList = ref<ApStatementSummaryVO[]>([])
const agingList = ref<ApStatementAgingVO[]>([])
const reconciliationList = ref<ApStatementReconciliationVO[]>([])
const reconciliationTotal = ref(0)

const statementQueryFormRef = ref()
const agingQueryFormRef = ref()
const reconciliationQueryFormRef = ref()
const detailDialogRef = ref<InstanceType<typeof ApStatementDetailDialog>>()
const invoiceDialogRef = ref<InstanceType<typeof ApStatementInvoiceDialog>>()

const statementQuery = reactive<ApStatementPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  statementNo: undefined,
  bizType: undefined,
  bizNo: undefined,
  supplierId: undefined,
  accountId: undefined,
  currencyCode: 'CNY',
  invoiceStatus: undefined,
  status: undefined,
  bizDate: [],
  dueDate: []
})

const agingQuery = reactive<ApStatementAgingReqVO>({
  supplierId: undefined,
  accountId: undefined,
  asOfDate: dayjs().format('YYYY-MM-DD')
})

const reconciliationQuery = reactive<ApStatementReconciliationReqVO>({
  pageNo: 1,
  pageSize: 10,
  supplierId: undefined,
  bizType: undefined,
  statementNo: undefined,
  bizNo: undefined,
  status: undefined,
  bizDate: []
})

const summaryMetrics = computed(() => ({
  supplierCount: summaryList.value.length,
  statementCount: summaryList.value.reduce((sum, item) => sum + Number(item.statementCount || 0), 0),
  totalAmount: summaryList.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0),
  totalPaidAmount: summaryList.value.reduce((sum, item) => sum + Number(item.totalPaidAmount || 0), 0),
  totalRemainAmount: summaryList.value.reduce((sum, item) => sum + Number(item.totalRemainAmount || 0), 0)
}))

const summaryCards = computed(() => [
  {
    key: 'supplier',
    label: '供应商数',
    value: String(summaryMetrics.value.supplierCount),
    pillText: '供应范围',
    pillClass: 'finance-apar-page__summary-pill--neutral',
    cardClass: '',
    valueClass: ''
  },
  {
    key: 'statement',
    label: '台账笔数',
    value: String(summaryMetrics.value.statementCount),
    pillText: '记录规模',
    pillClass: 'finance-apar-page__summary-pill--neutral',
    cardClass: '',
    valueClass: ''
  },
  {
    key: 'amount',
    label: '应付合计',
    value: erpPriceInputFormatter(summaryMetrics.value.totalAmount),
    pillText: '应付池',
    pillClass: 'finance-apar-page__summary-pill--primary',
    cardClass: '',
    valueClass: 'font-mono'
  },
  {
    key: 'paid',
    label: '已核销合计',
    value: erpPriceInputFormatter(summaryMetrics.value.totalPaidAmount),
    pillText: '核销进度',
    pillClass: 'finance-apar-page__summary-pill--success',
    cardClass: '',
    valueClass: 'font-mono'
  },
  {
    key: 'remain',
    label: '剩余合计',
    value: erpPriceInputFormatter(summaryMetrics.value.totalRemainAmount),
    pillText: '重点关注',
    pillClass: 'finance-apar-page__summary-pill--danger',
    cardClass: 'finance-apar-page__summary-card--accent',
    valueClass: 'finance-apar-page__summary-value--accent font-mono'
  }
])

const getBizTypeLabel = (value?: number) =>
  ERP_AP_BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

const getStatementStatusLabel = (value?: number) =>
  ERP_AP_STATEMENT_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getInvoiceStatusLabel = (value?: number) =>
  ERP_AP_INVOICE_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getStatementBadgeClass = (value?: number) =>
  (
    {
      10: 'finance-apar-page__badge--warning',
      20: 'finance-apar-page__badge--primary',
      30: 'finance-apar-page__badge--success',
      40: 'finance-apar-page__badge--neutral'
    } as Record<number, string>
  )[value || 0] || 'finance-apar-page__badge--neutral'

const getInvoiceBadgeClass = (value?: number) =>
  (
    {
      0: 'finance-apar-page__badge--neutral',
      1: 'finance-apar-page__badge--warning',
      2: 'finance-apar-page__badge--success'
    } as Record<number, string>
  )[value || 0] || 'finance-apar-page__badge--neutral'

const formatShortDate = (value?: string) => (value ? formatDate(value, 'YYYY-MM-DD') : '')

const getPaidRate = (row: Pick<ApStatementVO, 'amount' | 'paidAmount'> | Pick<ApStatementReconciliationVO, 'amount' | 'paidAmount'>) => {
  const amount = Math.abs(Number(row.amount || 0))
  if (!amount) {
    return 0
  }
  return Math.min(Math.round((Math.abs(Number(row.paidAmount || 0)) / amount) * 100), 100)
}

const buildAgingSegments = (row: ApStatementAgingVO): AgingSegment[] => {
  const values = [
    { label: '0-30 天', value: Number(row.amount0To30 || 0), barClass: 'finance-apar-page__aging-bar-fill--primary' },
    { label: '31-60 天', value: Number(row.amount31To60 || 0), barClass: 'finance-apar-page__aging-bar-fill--warning' },
    { label: '61-90 天', value: Number(row.amount61To90 || 0), barClass: 'finance-apar-page__aging-bar-fill--warning-strong' },
    { label: '91 天以上', value: Number(row.amount91Plus || 0), barClass: 'finance-apar-page__aging-bar-fill--danger' }
  ]
  const maxValue = Math.max(...values.map((item) => item.value), 0)
  return values.map((item) => ({
    ...item,
    widthPercent: maxValue > 0 && item.value > 0 ? Math.max((item.value / maxValue) * 100, 8) : 0
  }))
}

const getAgingRiskLabel = (row: ApStatementAgingVO) => {
  if (Number(row.amount91Plus || 0) > 0) {
    return '高风险'
  }
  if (Number(row.amount61To90 || 0) > 0) {
    return '中风险'
  }
  if (Number(row.totalRemainAmount || 0) > 0) {
    return '可跟进'
  }
  return '已清零'
}

const loadBaseOptions = async () => {
  const [suppliers, accounts] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    AccountApi.getAccountSimpleList()
  ])
  supplierList.value = suppliers
  accountList.value = accounts
}

const getSummaryList = async () => {
  loadingSummary.value = true
  try {
    summaryList.value = await ApStatementApi.getApStatementSummaryList(statementQuery.supplierId)
  } finally {
    loadingSummary.value = false
  }
}

const getStatementList = async () => {
  loadingStatementList.value = true
  statementErrorMessage.value = ''
  try {
    const data = await ApStatementApi.getApStatementPage(statementQuery)
    statementList.value = data.list
    statementTotal.value = data.total
  } catch {
    if (!statementList.value.length) {
      statementErrorMessage.value = '请检查网络或稍后重试。'
    }
  } finally {
    loadingStatementList.value = false
  }
}

const reloadStatementTab = async () => {
  await Promise.allSettled([getStatementList(), getSummaryList()])
}

const handleStatementQuery = async () => {
  statementQuery.pageNo = 1
  await reloadStatementTab()
}

const resetStatementQuery = async () => {
  statementQueryFormRef.value?.resetFields()
  statementQuery.currencyCode = 'CNY'
  statementQuery.pageNo = 1
  statementAdvancedExpanded.value = false
  await reloadStatementTab()
}

const getAgingList = async () => {
  loadingAging.value = true
  agingErrorMessage.value = ''
  try {
    agingList.value = await ApStatementApi.getAgingList(agingQuery)
  } catch {
    if (!agingList.value.length) {
      agingErrorMessage.value = '请检查网络或稍后重试。'
    }
  } finally {
    loadingAging.value = false
  }
}

const handleAgingQuery = async () => {
  await getAgingList()
}

const resetAgingQuery = async () => {
  agingQueryFormRef.value?.resetFields()
  agingQuery.asOfDate = dayjs().format('YYYY-MM-DD')
  await getAgingList()
}

const getReconciliationList = async () => {
  loadingReconciliation.value = true
  reconciliationErrorMessage.value = ''
  try {
    const data = await ApStatementApi.getReconciliationPage(reconciliationQuery)
    reconciliationList.value = data.list
    reconciliationTotal.value = data.total
  } catch {
    if (!reconciliationList.value.length) {
      reconciliationErrorMessage.value = '请检查网络或稍后重试。'
    }
  } finally {
    loadingReconciliation.value = false
  }
}

const handleReconciliationQuery = async () => {
  reconciliationQuery.pageNo = 1
  await getReconciliationList()
}

const resetReconciliationQuery = async () => {
  reconciliationQueryFormRef.value?.resetFields()
  reconciliationQuery.pageNo = 1
  reconciliationAdvancedExpanded.value = false
  await getReconciliationList()
}

const openDetail = (id?: number) => {
  if (!id) {
    return
  }
  detailDialogRef.value?.open(id)
}

const openInvoiceDialog = (id?: number) => {
  if (!id) {
    return
  }
  invoiceDialogRef.value?.open(id)
}

const handleInvoiceSuccess = async (id: number) => {
  const refreshTasks: Promise<unknown>[] = [getStatementList(), getSummaryList()]
  const detailRefreshTask = detailDialogRef.value?.refresh(id)
  if (detailRefreshTask) {
    refreshTasks.push(detailRefreshTask)
  }
  await Promise.allSettled(refreshTasks)
}

const handleExportStatement = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await ApStatementApi.exportApStatement(statementQuery)
    download.excel(data, '应付台账.xls')
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    exportLoading.value = false
  }
}

watch(activeTab, async (tab) => {
  if (tab === 'aging' && !agingList.value.length && !loadingAging.value) {
    await getAgingList()
  }
  if (tab === 'reconciliation' && !reconciliationList.value.length && !loadingReconciliation.value) {
    await getReconciliationList()
  }
})

onMounted(async () => {
  await Promise.allSettled([loadBaseOptions(), getStatementList(), getSummaryList()])
})
</script>

<style scoped>
.finance-apar-page {
  min-height: 100%;
  padding: 8px 0 20px;
  background: #f8fafc;
}

.finance-apar-page__summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.finance-apar-page__summary-card {
  padding: 18px 20px;
  border: 1px solid #dbe4f0;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(148, 163, 184, 0.08);
  transition: box-shadow 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.finance-apar-page__summary-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 16px 30px rgba(148, 163, 184, 0.12);
  transform: translateY(-1px);
}

.finance-apar-page__summary-card--accent {
  border-color: #fecdd3;
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 100%);
}

.finance-apar-page__summary-card .finance-apar-page__summary-value {
  font-family: ui-monospace, SFMono-Regular, 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.finance-apar-page__summary-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.finance-apar-page__summary-label {
  color: #64748b;
  font-size: 13px;
}

.finance-apar-page__summary-pill {
  padding: 2px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.4;
}

.finance-apar-page__summary-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.finance-apar-page__summary-pill--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.finance-apar-page__summary-pill--success {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.finance-apar-page__summary-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.finance-apar-page__summary-value {
  margin-top: 14px;
  color: #0f172a;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
}

.finance-apar-page__summary-value--accent {
  color: #e11d48;
}

.finance-apar-page__tabs-card,
.finance-apar-page__filter-card,
.finance-apar-page__table-card {
  margin-bottom: 16px;
}

.finance-apar-page__tabs-card :deep(.el-card),
.finance-apar-page__filter-card :deep(.el-card),
.finance-apar-page__table-card :deep(.el-card) {
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(148, 163, 184, 0.08);
}

.finance-apar-page__tabs-card :deep(.el-card__body),
.finance-apar-page__filter-card :deep(.el-card__body),
.finance-apar-page__table-card :deep(.el-card__body) {
  background: #ffffff;
}

.finance-apar-page__tabs {
  margin-bottom: -8px;
}

.finance-apar-page__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-apar-page__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.finance-apar-page__card-header--table {
  margin-bottom: 14px;
}

.finance-apar-page__card-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
}

.finance-apar-page__advanced-block {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #f8fafc;
}

.finance-apar-page__advanced-title {
  margin-bottom: 12px;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

.finance-apar-page__form-grid {
  display: grid;
  gap: 0 16px;
}

.finance-apar-page__form-grid--basic {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-apar-page__form-grid--advanced {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.finance-apar-page__form-grid--compact {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.finance-apar-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-apar-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-apar-page__table-wrap {
  overflow-x: auto;
}

.finance-apar-page__table :deep(.el-table__header-wrapper th.el-table__cell) {
  position: sticky;
  top: 0;
  z-index: 3;
  background: #f8fafc;
}

.finance-apar-page__state {
  padding: 12px 0 4px;
}

.finance-apar-page__state :deep(.el-result) {
  padding: 0;
}

.finance-apar-page__cell-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-apar-page__cell-title {
  color: #0f172a;
  font-weight: 600;
  line-height: 1.45;
}

.finance-apar-page__cell-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.finance-apar-page__cell-muted {
  max-width: 100%;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-apar-page__mono-chip {
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

.finance-apar-page__badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.4;
}

.finance-apar-page__badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.8;
}

.finance-apar-page__badge--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.finance-apar-page__badge--success {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.finance-apar-page__badge--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.finance-apar-page__badge--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #64748b;
}

.finance-apar-page__aging-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finance-apar-page__aging-line {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 110px;
  align-items: center;
  gap: 10px;
}

.finance-apar-page__aging-label,
.finance-apar-page__aging-value {
  color: #475569;
  font-size: 12px;
}

.finance-apar-page__aging-bar-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.finance-apar-page__aging-bar-fill {
  height: 100%;
  border-radius: 999px;
}

.finance-apar-page__aging-bar-fill--primary {
  background: #3b82f6;
}

.finance-apar-page__aging-bar-fill--warning {
  background: #f59e0b;
}

.finance-apar-page__aging-bar-fill--warning-strong {
  background: #fb923c;
}

.finance-apar-page__aging-bar-fill--danger {
  background: #f43f5e;
}

.finance-apar-page__progress-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finance-apar-page__progress-meta,
.finance-apar-page__progress-values {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.finance-apar-page__progress-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.finance-apar-page__progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb 0%, #14b8a6 100%);
}

.finance-apar-page :deep(.finance-shell__empty) {
  min-height: 180px;
}

@media (max-width: 1360px) {
  .finance-apar-page__summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1440px) {
  .finance-apar-page__form-grid--basic {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .finance-apar-page__form-grid--advanced,
  .finance-apar-page__form-grid--compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .finance-apar-page__summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-apar-page__summary-grid,
  .finance-apar-page__form-grid--basic,
  .finance-apar-page__form-grid--advanced,
  .finance-apar-page__form-grid--compact {
    grid-template-columns: 1fr;
  }

  .finance-apar-page__query-actions,
  .finance-apar-page__toolbar-actions {
    justify-content: stretch;
  }

  .finance-apar-page__aging-line {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}
</style>
