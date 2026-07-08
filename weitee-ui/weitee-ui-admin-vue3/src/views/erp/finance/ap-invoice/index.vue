<template>
  <div class="finance-ap-invoice-page">
    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="88px"
        class="finance-ap-invoice-page__query-form"
      >
        <div class="finance-ap-invoice-page__card-header">
          <div class="finance-ap-invoice-page__card-title">基础筛选</div>
          <el-button link type="primary" @click="advancedExpanded = !advancedExpanded">
            <Icon :icon="advancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
            {{ advancedExpanded ? '收起高级筛选' : '展开高级筛选' }}
          </el-button>
        </div>

        <div class="finance-ap-invoice-page__query-grid finance-ap-invoice-page__query-grid--basic">
          <el-form-item label="发票号" prop="invoiceNo">
            <el-input
              v-model="queryParams.invoiceNo"
              placeholder="请输入发票号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="供应商" prop="supplierId">
            <el-select
              v-model="queryParams.supplierId"
              placeholder="请选择供应商"
              clearable
              filterable
              :loading="supplierLoading"
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
          <el-form-item label="发票类型" prop="invoiceType">
            <el-select
              v-model="queryParams.invoiceType"
              placeholder="请选择发票类型"
              clearable
              class="!w-full"
            >
              <el-option
                v-for="item in ERP_AP_INVOICE_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="匹配状态" prop="matchStatus">
            <el-select
              v-model="queryParams.matchStatus"
              placeholder="请选择匹配状态"
              clearable
              class="!w-full"
            >
              <el-option
                v-for="item in ERP_AP_INVOICE_MATCH_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="发票日期" prop="invoiceDate">
            <el-date-picker
              v-model="queryParams.invoiceDate"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-full"
            />
          </el-form-item>
        </div>

        <div v-if="advancedExpanded" class="finance-ap-invoice-page__advanced-block">
          <div class="finance-ap-invoice-page__section-title">高级筛选</div>
          <div class="finance-ap-invoice-page__query-grid finance-ap-invoice-page__query-grid--advanced">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="queryParams.remark"
                placeholder="请输入备注"
                clearable
                class="!w-full"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </div>
        </div>

        <div class="finance-ap-invoice-page__query-actions">
          <el-button :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="listLoading" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="finance-ap-invoice-page__toolbar">
        <div class="finance-ap-invoice-page__toolbar-left">
          <span v-if="isDemoMode" class="finance-ap-invoice-page__demo-badge">示例数据</span>
          <el-button
            type="primary"
            :loading="saveSubmitting"
            :disabled="listLoading"
            v-hasPermi="['erp:ap-invoice:create']"
            @click="openFormDialog()"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新建发票
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !displayList.length" class="finance-ap-invoice-page__state">
        <el-result icon="error" title="采购发票列表加载失败" :sub-title="listErrorMessage">
          <template #extra>
            <el-button type="primary" @click="getList">重试</el-button>
          </template>
        </el-result>
      </div>
      <template v-else>
        <div v-if="listLoading || displayList.length" class="finance-ap-invoice-page__table-wrap">
          <el-table v-loading="listLoading" :data="displayList" stripe row-key="id" show-overflow-tooltip>
            <el-table-column label="发票信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-ap-invoice-page__primary-cell">
                  <span class="finance-ap-invoice-page__primary-text">{{ row.invoiceNo || '-' }}</span>
                  <span class="finance-ap-invoice-page__muted-text">
                    {{ row.invoiceTypeName || getInvoiceTypeLabel(row.invoiceType) }}
                  </span>
                  <span class="finance-ap-invoice-page__muted-text">
                    {{ formatDateValue(row.invoiceDate, 'YYYY-MM-DD') }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="供应商与状态" min-width="200">
              <template #default="{ row }">
                <div class="finance-ap-invoice-page__primary-cell">
                  <span class="finance-ap-invoice-page__primary-text">{{ row.supplierName || '-' }}</span>
                  <span class="finance-ap-invoice-page__status-pill" :class="getMatchStatusClass(row.matchStatus)">
                    <span class="finance-ap-invoice-page__status-dot"></span>
                    {{ row.matchStatusName || getMatchStatusLabel(row.matchStatus) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="金额信息" min-width="200" align="right">
              <template #default="{ row }">
                <div class="finance-ap-invoice-page__amount-cell font-mono">
                  <span class="finance-ap-invoice-page__muted-text">
                    发票 {{ formatAmount(row.totalAmount) }}
                  </span>
                  <span class="finance-ap-invoice-page__muted-text">
                    已匹配 {{ formatAmount(row.matchedAmount) }}
                  </span>
                  <span class="finance-ap-invoice-page__amount-text">
                    未匹配 {{ formatAmount(row.unmatchedAmount) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="匹配进度" min-width="220">
              <template #default="{ row }">
                <div class="finance-ap-invoice-page__progress-card">
                  <div class="finance-ap-invoice-page__progress-meta">
                    <span>数量 {{ formatCount(row.matchedCount) }} / {{ formatCount(row.totalCount) }}</span>
                    <span>差额 {{ formatSignedAmount(row.differenceAmount) }}</span>
                  </div>
                  <div class="finance-ap-invoice-page__progress-track">
                    <div
                      class="finance-ap-invoice-page__progress-fill"
                      :style="{ width: `${getMatchedRate(row)}%` }"
                    ></div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="差异与备注" min-width="220">
              <template #default="{ row }">
                <div class="finance-ap-invoice-page__primary-cell">
                  <span class="finance-ap-invoice-page__muted-text">
                    差异原因 {{ row.differenceReason || '未填写' }}
                  </span>
                  <span class="finance-ap-invoice-page__muted-text">
                    {{ row.remark || '无备注' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="160">
              <template #default="{ row }">
                <span class="finance-ap-invoice-page__muted-text">
                  {{ formatDateValue(row.createTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" fixed="right" width="240">
              <template #default="{ row }">
                <el-button
                  link
                  :disabled="anyActionBusy"
                  v-hasPermi="['erp:ap-invoice:query']"
                  @click="openDetailDialog(row.id)"
                >
                  详情
                </el-button>
                <el-button
                  v-if="canEditRow(row)"
                  link
                  :disabled="anyActionBusy"
                  v-hasPermi="['erp:ap-invoice:update']"
                  @click="openFormDialog(row.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="!canOpenMatch(row) || anyActionBusy"
                  v-hasPermi="['erp:ap-invoice:update']"
                  @click="openMatchDialog(row)"
                >
                  匹配
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty
          v-else
          class="finance-ap-invoice-page__state"
          :class="{ 'finance-shell__empty--demo': isDemoMode }"
          :description="isDemoMode ? '当前无真实数据，已展示示例数据' : '暂无采购发票数据'"
        />

        <Pagination
          v-if="displayTotal > 0"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="displayTotal"
          @pagination="handlePagination"
        />
      </template>
    </ContentWrap>

    <Dialog
      v-model="formDialogVisible"
      :title="formDialogTitle"
      width="min(860px, 96vw)"
      scroll
      maxHeight="80vh"
    >
      <div class="finance-ap-invoice-dialog">
        <el-form
          ref="formRef"
          v-loading="formLoading"
          :model="formData"
          :rules="formRules"
          label-width="92px"
          class="finance-ap-invoice-dialog__form"
        >
          <div class="finance-ap-invoice-dialog__panel">
            <div class="finance-ap-invoice-dialog__panel-title">发票头信息</div>
            <div class="finance-ap-invoice-dialog__grid">
              <el-form-item label="供应商" prop="supplierId">
                <el-select
                  v-model="formData.supplierId"
                  placeholder="请选择供应商"
                  filterable
                  :disabled="formLoading || saveSubmitting"
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
              <el-form-item label="发票号" prop="invoiceNo">
                <el-input
                  v-model="formData.invoiceNo"
                  placeholder="请输入发票号"
                  :disabled="formLoading || saveSubmitting"
                />
              </el-form-item>
              <el-form-item label="发票日期" prop="invoiceDate">
                <el-date-picker
                  v-model="formData.invoiceDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择发票日期"
                  :disabled="formLoading || saveSubmitting"
                  class="!w-full"
                />
              </el-form-item>
              <el-form-item label="发票类型" prop="invoiceType">
                <el-select
                  v-model="formData.invoiceType"
                  placeholder="请选择发票类型"
                  :disabled="formLoading || saveSubmitting"
                  class="!w-full"
                >
                  <el-option
                    v-for="item in ERP_AP_INVOICE_TYPE_OPTIONS"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="发票总数量" prop="totalCount">
                <el-input-number
                  v-model="formData.totalCount"
                  :min="0"
                  :precision="3"
                  controls-position="right"
                  :disabled="formLoading || saveSubmitting"
                  class="!w-full"
                />
              </el-form-item>
              <el-form-item label="发票总金额" prop="totalAmount">
                <el-input-number
                  v-model="formData.totalAmount"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  :disabled="formLoading || saveSubmitting"
                  class="!w-full"
                />
              </el-form-item>
              <el-form-item label="尾差容忍" prop="toleranceAmount">
                <el-input-number
                  v-model="formData.toleranceAmount"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  :disabled="formLoading || saveSubmitting"
                  class="!w-full"
                />
              </el-form-item>
            </div>
            <el-form-item label="差异原因" prop="differenceReason">
              <el-input
                v-model="formData.differenceReason"
                type="textarea"
                :rows="3"
                placeholder="请输入差异原因"
                :disabled="formLoading || saveSubmitting"
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
                :disabled="formLoading || saveSubmitting"
              />
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveSubmitting" :disabled="formSubmitDisabled" @click="submitForm">
          保存
        </el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="detailDialogVisible"
      title="采购发票详情"
      width="min(1180px, 96vw)"
      scroll
      maxHeight="84vh"
    >
      <div class="finance-ap-invoice-detail" v-loading="detailLoading">
        <el-result
          v-if="detailErrorMessage"
          icon="error"
          title="采购发票详情加载失败"
          :sub-title="detailErrorMessage"
        >
          <template #extra>
            <el-button type="primary" :disabled="detailLoading || detailId == null" @click="loadDetail">
              重试
            </el-button>
          </template>
        </el-result>

        <template v-else-if="detailData">
          <div class="finance-ap-invoice-detail__hero">
            <div>
              <div class="finance-ap-invoice-detail__eyebrow">采购发票</div>
              <div class="finance-ap-invoice-detail__title">
                {{ detailData.invoiceNo || `#${detailData.id}` }}
              </div>
              <div class="finance-ap-invoice-detail__meta">
                <span>{{ detailData.supplierName || '-' }}</span>
                <span>{{ detailData.invoiceTypeName || getInvoiceTypeLabel(detailData.invoiceType) }}</span>
                <span>{{ formatDateValue(detailData.invoiceDate, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
            <span class="finance-ap-invoice-page__status-pill" :class="getMatchStatusClass(detailData.matchStatus)">
              <span class="finance-ap-invoice-page__status-dot"></span>
              {{ detailData.matchStatusName || getMatchStatusLabel(detailData.matchStatus) }}
            </span>
          </div>

          <div class="finance-ap-invoice-detail__summary-grid">
            <div class="finance-ap-invoice-detail__summary-card">
              <div class="finance-ap-invoice-detail__summary-label">发票总金额</div>
              <div class="finance-ap-invoice-detail__summary-value font-mono">
                {{ formatAmount(detailData.totalAmount) }}
              </div>
            </div>
            <div class="finance-ap-invoice-detail__summary-card">
              <div class="finance-ap-invoice-detail__summary-label">已匹配金额</div>
              <div class="finance-ap-invoice-detail__summary-value font-mono">
                {{ formatAmount(detailData.matchedAmount) }}
              </div>
            </div>
            <div class="finance-ap-invoice-detail__summary-card finance-ap-invoice-detail__summary-card--accent">
              <div class="finance-ap-invoice-detail__summary-label">差异金额</div>
              <div class="finance-ap-invoice-detail__summary-value finance-ap-invoice-detail__summary-value--accent font-mono">
                {{ formatSignedAmount(detailData.differenceAmount) }}
              </div>
            </div>
          </div>

          <div class="finance-ap-invoice-detail__panel">
            <div class="finance-ap-invoice-detail__panel-title">基础信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="供应商">
                {{ detailData.supplierName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="发票号">
                {{ detailData.invoiceNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="发票日期">
                {{ formatDateValue(detailData.invoiceDate, 'YYYY-MM-DD HH:mm:ss') }}
              </el-descriptions-item>
              <el-descriptions-item label="发票类型">
                {{ detailData.invoiceTypeName || getInvoiceTypeLabel(detailData.invoiceType) }}
              </el-descriptions-item>
              <el-descriptions-item label="发票总数量">
                {{ formatCount(detailData.totalCount) }}
              </el-descriptions-item>
              <el-descriptions-item label="已匹配数量">
                {{ formatCount(detailData.matchedCount) }}
              </el-descriptions-item>
              <el-descriptions-item label="未匹配金额">
                {{ formatAmount(detailData.unmatchedAmount) }}
              </el-descriptions-item>
              <el-descriptions-item label="尾差容忍">
                {{ formatAmount(detailData.toleranceAmount) }}
              </el-descriptions-item>
              <el-descriptions-item label="差异原因" :span="2">
                {{ detailData.differenceReason || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">
                {{ detailData.remark || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="finance-ap-invoice-detail__panel">
            <div class="finance-ap-invoice-detail__panel-head">
              <div class="finance-ap-invoice-detail__panel-title">匹配明细</div>
              <el-button
                type="danger"
                plain
                :loading="cancelSubmitting"
                :disabled="cancelSelectedDisabled"
                v-hasPermi="['erp:ap-invoice:update']"
                @click="handleCancelMatch"
              >
                撤销匹配
              </el-button>
            </div>
            <div class="finance-ap-invoice-detail__table-wrap">
              <el-table
                ref="detailTableRef"
                :data="detailData.items || []"
                stripe
                row-key="id"
                show-overflow-tooltip
                @selection-change="handleDetailSelectionChange"
              >
                <el-table-column type="selection" width="42" :selectable="canSelectMatchItem" />
                <el-table-column label="来源单据" min-width="220">
                  <template #default="{ row }">
                    <div class="finance-ap-invoice-page__primary-cell">
                      <span class="finance-ap-invoice-page__primary-text">
                        {{ row.sourcePurchaseInNo || '-' }}
                      </span>
                      <span class="finance-ap-invoice-page__muted-text">
                        {{ row.sourceOrderNo || '未关联采购订单' }}
                      </span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="物料" min-width="180">
                  <template #default="{ row }">
                    <span class="finance-ap-invoice-page__primary-text">{{ row.productName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                  label="匹配数量"
                  align="right"
                  width="120"
                  prop="matchCount"
                  class-name="font-mono"
                  :formatter="formatCountColumn"
                />
                <el-table-column
                  label="匹配金额"
                  align="right"
                  width="140"
                  prop="matchAmount"
                  class-name="font-mono"
                  :formatter="formatPriceColumn"
                />
                <el-table-column label="状态" align="center" width="110">
                  <template #default="{ row }">
                    <span class="finance-ap-invoice-page__status-pill" :class="getMatchItemStatusClass(row.status)">
                      <span class="finance-ap-invoice-page__status-dot"></span>
                      {{ row.statusName || getMatchItemStatusLabel(row.status) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="160" prop="remark" />
                <el-table-column label="创建时间" min-width="160">
                  <template #default="{ row }">
                    {{ formatDateValue(row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </template>

        <el-empty v-else description="暂无采购发票详情" />
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="matchDialogVisible"
      title="发票匹配"
      width="min(1260px, 98vw)"
      scroll
      maxHeight="86vh"
    >
      <div class="finance-ap-invoice-match">
        <div class="finance-ap-invoice-match__context-card">
          <div>
            <div class="finance-ap-invoice-match__context-eyebrow">当前发票</div>
            <div class="finance-ap-invoice-match__context-title">
              {{ matchContext.invoiceNo || '-' }}
            </div>
            <div class="finance-ap-invoice-match__context-meta">
              <span>{{ matchContext.supplierName || '-' }}</span>
              <span>总金额 {{ formatAmount(matchContext.totalAmount) }}</span>
              <span>未匹配 {{ formatAmount(matchContext.unmatchedAmount) }}</span>
            </div>
          </div>
          <div class="finance-ap-invoice-match__context-metrics">
            <div class="finance-ap-invoice-match__context-metric">
              <div class="finance-ap-invoice-match__context-label">已选数量</div>
              <div class="finance-ap-invoice-match__context-value font-mono">
                {{ formatCount(matchSelectionSummary.totalCount) }}
              </div>
            </div>
            <div class="finance-ap-invoice-match__context-metric">
              <div class="finance-ap-invoice-match__context-label">已选金额</div>
              <div class="finance-ap-invoice-match__context-value font-mono">
                {{ formatAmount(matchSelectionSummary.totalAmount) }}
              </div>
            </div>
          </div>
        </div>

        <el-form
          ref="pendingQueryFormRef"
          :model="pendingQueryParams"
          label-width="88px"
          class="finance-ap-invoice-match__query-form"
        >
          <div class="finance-ap-invoice-match__query-grid">
            <el-form-item label="采购订单" prop="sourceOrderNo">
              <el-input
                v-model="pendingQueryParams.sourceOrderNo"
                placeholder="请输入采购订单号"
                clearable
                @keyup.enter="handlePendingQuery"
              />
            </el-form-item>
            <el-form-item label="入库单号" prop="purchaseInNo">
              <el-input
                v-model="pendingQueryParams.purchaseInNo"
                placeholder="请输入入库单号"
                clearable
                @keyup.enter="handlePendingQuery"
              />
            </el-form-item>
          </div>
          <div class="finance-ap-invoice-match__query-actions">
            <el-button :loading="pendingLoading" @click="handlePendingQuery">
              <Icon icon="ep:search" class="mr-5px" />
              查询
            </el-button>
            <el-button :disabled="pendingLoading" @click="resetPendingQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </div>
        </el-form>

        <div v-if="pendingErrorMessage && !pendingList.length" class="finance-ap-invoice-match__state">
          <el-result icon="error" title="待匹配池加载失败" :sub-title="pendingErrorMessage">
            <template #extra>
              <el-button type="primary" @click="getPendingList">重试</el-button>
            </template>
          </el-result>
        </div>
        <template v-else>
          <div v-if="pendingLoading || pendingList.length" class="finance-ap-invoice-match__table-wrap">
            <el-table v-loading="pendingLoading" :data="pendingList" stripe row-key="sourcePurchaseInItemId">
              <el-table-column label="来源单据" min-width="220">
                <template #default="{ row }">
                  <div class="finance-ap-invoice-page__primary-cell">
                    <span class="finance-ap-invoice-page__primary-text">
                      {{ row.sourcePurchaseInNo || '-' }}
                    </span>
                    <span class="finance-ap-invoice-page__muted-text">
                      {{ row.sourceOrderNo || '未关联采购订单' }}
                    </span>
                    <span class="finance-ap-invoice-page__muted-text">
                      {{ formatDateValue(row.bizDate, 'YYYY-MM-DD') }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="物料" min-width="180">
                <template #default="{ row }">
                  <span class="finance-ap-invoice-page__primary-text">{{ row.productName || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="剩余可匹配" min-width="180" align="right">
                <template #default="{ row }">
                  <div class="finance-ap-invoice-page__amount-cell font-mono">
                    <span class="finance-ap-invoice-page__muted-text">
                      数量 {{ formatCount(row.remainCount) }}
                    </span>
                    <span class="finance-ap-invoice-page__amount-text">
                      金额 {{ formatAmount(row.remainAmount) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="匹配数量" width="150">
                <template #default="{ row }">
                  <el-input-number
                    v-model="matchDrafts[getPendingDraftKey(row)].matchCount"
                    :min="0"
                    :max="Number(row.remainCount || 0)"
                    :precision="3"
                    controls-position="right"
                    class="!w-full"
                  />
                </template>
              </el-table-column>
              <el-table-column label="匹配金额" width="150">
                <template #default="{ row }">
                  <el-input-number
                    v-model="matchDrafts[getPendingDraftKey(row)].matchAmount"
                    :min="0"
                    :max="Number(row.remainAmount || 0)"
                    :precision="2"
                    controls-position="right"
                    class="!w-full"
                  />
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="180">
                <template #default="{ row }">
                  <el-input
                    v-model="matchDrafts[getPendingDraftKey(row)].remark"
                    placeholder="请输入备注"
                    clearable
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-empty v-else class="finance-ap-invoice-match__state" description="暂无可匹配入库明细" />

          <Pagination
            v-if="pendingTotal > 0"
            v-model:page="pendingQueryParams.pageNo"
            v-model:limit="pendingQueryParams.pageSize"
            :total="pendingTotal"
            @pagination="getPendingList"
          />
        </template>

        <div class="finance-ap-invoice-match__difference-panel">
          <el-form label-width="88px">
            <el-form-item label="差异原因">
              <el-input
                v-model="matchDifferenceReason"
                type="textarea"
                :rows="3"
                placeholder="如存在差异，请填写差异原因"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="matchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="matchSubmitting" :disabled="matchSubmitDisabled" @click="submitMatch">
          确认匹配
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import {
  ApInvoiceApi,
  ERP_AP_INVOICE_MATCH_ITEM_STATUS_OPTIONS,
  ERP_AP_INVOICE_MATCH_STATUS_OPTIONS,
  ERP_AP_INVOICE_TYPE_OPTIONS,
  type ErpApInvoiceCancelMatchReqVO,
  type ErpApInvoiceConfirmMatchReqVO,
  type ErpApInvoiceMatchItemVO,
  type ErpApInvoicePageReqVO,
  type ErpApInvoicePendingItemPageReqVO,
  type ErpApInvoicePendingItemVO,
  type ErpApInvoiceSaveReqVO,
  type ErpApInvoiceVO
} from '@/api/erp/finance/ap-invoice'
import { SupplierApi } from '@/api/erp/purchase/supplier'
import { erpCountTableColumnFormatter, erpPriceInputFormatter } from '@/utils'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'ErpFinanceApInvoice' })

interface SupplierOption {
  id: number
  name: string
}

interface MatchDraft {
  matchCount?: number
  matchAmount?: number
  remark?: string
}

const message = useMessage()
const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const pendingQueryFormRef = ref<FormInstance>()
const detailTableRef = ref()

const advancedExpanded = ref(false)

const queryParams = reactive<ErpApInvoicePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  invoiceNo: '',
  supplierId: undefined,
  invoiceType: undefined,
  matchStatus: undefined,
  invoiceDate: [],
  remark: ''
})

const list = ref<ErpApInvoiceVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listErrorMessage = ref('')

const supplierList = ref<SupplierOption[]>([])
const supplierLoading = ref(false)

const formDialogVisible = ref(false)
const formDialogMode = ref<'create' | 'update'>('create')
const formLoading = ref(false)
const saveSubmitting = ref(false)
const editingId = ref<number>()
const formData = reactive<ErpApInvoiceSaveReqVO>({
  supplierId: undefined,
  invoiceNo: '',
  invoiceDate: '',
  invoiceType: undefined,
  totalCount: undefined,
  totalAmount: undefined,
  toleranceAmount: 1,
  differenceReason: '',
  remark: ''
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailId = ref<number | null>(null)
const detailData = ref<ErpApInvoiceVO | null>(null)
const detailErrorMessage = ref('')
const detailSelectedItems = ref<ErpApInvoiceMatchItemVO[]>([])
const cancelSubmitting = ref(false)

const matchDialogVisible = ref(false)
const pendingLoading = ref(false)
const pendingErrorMessage = ref('')
const matchSubmitting = ref(false)
const pendingList = ref<ErpApInvoicePendingItemVO[]>([])
const pendingTotal = ref(0)
const matchContext = reactive<Partial<ErpApInvoiceVO>>({
  id: undefined,
  supplierId: undefined,
  supplierName: '',
  invoiceNo: '',
  totalAmount: 0,
  unmatchedAmount: 0
})
const pendingQueryParams = reactive<ErpApInvoicePendingItemPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  invoiceId: undefined,
  supplierId: undefined,
  sourceOrderNo: '',
  purchaseInNo: ''
})
const matchDrafts = reactive<Record<string, MatchDraft>>({})
const matchDifferenceReason = ref('')

const isLocalDemoHost = () => {
  if (typeof window === 'undefined') {
    return false
  }
  const hostname = window.location.hostname
  return (
    import.meta.env.DEV ||
    hostname === 'localhost' ||
    hostname === '127.0.0.1' ||
    hostname === '::1' ||
    hostname.endsWith('.local')
  )
}

const demoSupplierList: SupplierOption[] = [
  { id: 71001, name: '苏州泽科电子有限公司' },
  { id: 71002, name: '杭州瑞联供应链有限公司' },
  { id: 71003, name: '深圳启宏科技有限公司' }
]

const demoInvoices: ErpApInvoiceVO[] = [
  {
    id: 801001,
    supplierId: 71001,
    supplierName: '苏州泽科电子有限公司',
    invoiceNo: 'INV-2026-0508-001',
    invoiceDate: '2026-05-08 10:12:00',
    invoiceType: 10,
    invoiceTypeName: '专票',
    totalCount: 160,
    matchedCount: 120,
    totalAmount: 186400.5,
    matchedAmount: 143520.25,
    unmatchedAmount: 42880.25,
    toleranceAmount: 1,
    differenceAmount: 1280.25,
    matchStatus: 20,
    matchStatusName: '部分匹配',
    differenceReason: '供应商按批次发货，暂按入库单分段匹配',
    remark: '示例数据：用于演示匹配流程',
    createTime: '2026-05-08 10:18:00',
    items: [
      {
        id: 811001,
        apStatementId: 821001,
        sourceOrderId: 831001,
        sourceOrderNo: 'PO-2026-0419-008',
        sourcePurchaseInId: 841001,
        sourcePurchaseInNo: 'PI-2026-0507-006',
        sourcePurchaseInItemId: 851001,
        productName: '控制板组件',
        matchCount: 80,
        matchAmount: 89320.25,
        status: 10,
        statusName: '生效',
        remark: '首批到货',
        createTime: '2026-05-08 10:22:00'
      },
      {
        id: 811002,
        apStatementId: 821002,
        sourceOrderId: 831001,
        sourceOrderNo: 'PO-2026-0419-008',
        sourcePurchaseInId: 841002,
        sourcePurchaseInNo: 'PI-2026-0508-002',
        sourcePurchaseInItemId: 851002,
        productName: '屏蔽罩',
        matchCount: 40,
        matchAmount: 54199.99,
        status: 10,
        statusName: '生效',
        remark: '第二批到货',
        createTime: '2026-05-08 10:24:00'
      }
    ]
  },
  {
    id: 801002,
    supplierId: 71002,
    supplierName: '杭州瑞联供应链有限公司',
    invoiceNo: 'INV-2026-0509-004',
    invoiceDate: '2026-05-09 15:26:00',
    invoiceType: 20,
    invoiceTypeName: '普票',
    totalCount: 96,
    matchedCount: 96,
    totalAmount: 96800,
    matchedAmount: 96800,
    unmatchedAmount: 0,
    toleranceAmount: 0.5,
    differenceAmount: 0,
    matchStatus: 30,
    matchStatusName: '已匹配',
    differenceReason: '金额一致',
    remark: '示例数据：已完成匹配',
    createTime: '2026-05-09 15:30:00',
    items: [
      {
        id: 811003,
        apStatementId: 821003,
        sourceOrderId: 831002,
        sourceOrderNo: 'PO-2026-0422-014',
        sourcePurchaseInId: 841003,
        sourcePurchaseInNo: 'PI-2026-0509-001',
        sourcePurchaseInItemId: 851003,
        productName: '连接器',
        matchCount: 96,
        matchAmount: 96800,
        status: 10,
        statusName: '生效',
        remark: '一次性匹配完成',
        createTime: '2026-05-09 15:28:00'
      }
    ]
  },
  {
    id: 801003,
    supplierId: 71003,
    supplierName: '深圳启宏科技有限公司',
    invoiceNo: 'INV-2026-0510-002',
    invoiceDate: '2026-05-10 09:42:00',
    invoiceType: 30,
    invoiceTypeName: '电子票',
    totalCount: 42,
    matchedCount: 0,
    totalAmount: 34800,
    matchedAmount: 0,
    unmatchedAmount: 34800,
    toleranceAmount: 0.8,
    differenceAmount: -320.5,
    matchStatus: 10,
    matchStatusName: '未匹配',
    differenceReason: '等待采购入库完成',
    remark: '示例数据：展示未匹配状态',
    createTime: '2026-05-10 09:45:00',
    items: []
  }
]

const demoPendingItems: ErpApInvoicePendingItemVO[] = [
  {
    apStatementId: 821001,
    sourceOrderId: 831001,
    sourceOrderNo: 'PO-2026-0419-008',
    sourcePurchaseInId: 841001,
    sourcePurchaseInNo: 'PI-2026-0507-006',
    sourcePurchaseInItemId: 851001,
    supplierId: 71001,
    productName: '控制板组件',
    bizDate: '2026-05-07 16:30:00',
    totalCount: 120,
    matchedCount: 80,
    remainCount: 40,
    totalAmount: 118320.25,
    matchedAmount: 89320.25,
    remainAmount: 29000,
  },
  {
    apStatementId: 821002,
    sourceOrderId: 831001,
    sourceOrderNo: 'PO-2026-0419-008',
    sourcePurchaseInId: 841002,
    sourcePurchaseInNo: 'PI-2026-0508-002',
    sourcePurchaseInItemId: 851002,
    supplierId: 71001,
    productName: '屏蔽罩',
    bizDate: '2026-05-08 11:05:00',
    totalCount: 40,
    matchedCount: 0,
    remainCount: 40,
    totalAmount: 54199.99,
    matchedAmount: 0,
    remainAmount: 54199.99
  },
  {
    apStatementId: 821004,
    sourceOrderId: 831003,
    sourceOrderNo: 'PO-2026-0428-021',
    sourcePurchaseInId: 841004,
    sourcePurchaseInNo: 'PI-2026-0510-009',
    sourcePurchaseInItemId: 851004,
    supplierId: 71001,
    productName: '热敏电阻',
    bizDate: '2026-05-10 14:20:00',
    totalCount: 100,
    matchedCount: 60,
    remainCount: 40,
    totalAmount: 26780,
    matchedAmount: 16068,
    remainAmount: 10712
  }
]

const isDemoMode = computed(() => isLocalDemoHost() && !listLoading.value && !listErrorMessage.value && list.value.length === 0)
const displayList = computed(() => (isDemoMode.value ? demoInvoices : list.value))
const displayTotal = computed(() => (isDemoMode.value ? demoInvoices.length : total.value))

const findDemoInvoice = (id?: number | null) => demoInvoices.find((item) => item.id === id)
const getDemoPendingList = (invoiceId?: number | null) => {
  if (invoiceId === 801001) {
    return demoPendingItems
  }
  if (invoiceId === 801003) {
    return demoPendingItems.slice(1, 3)
  }
  return []
}
const getDemoSupplierList = () => demoSupplierList

const formRules: FormRules<ErpApInvoiceSaveReqVO> = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  invoiceNo: [{ required: true, message: '请输入发票号', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '请选择发票日期', trigger: 'change' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入发票总金额', trigger: 'blur' }]
}

const formDialogTitle = computed(() =>
  formDialogMode.value === 'create' ? '新建采购发票' : '编辑采购发票'
)

const formSubmitDisabled = computed(
  () => formLoading.value || saveSubmitting.value || supplierLoading.value
)

const anyActionBusy = computed(
  () =>
    listLoading.value ||
    saveSubmitting.value ||
    detailLoading.value ||
    cancelSubmitting.value ||
    pendingLoading.value ||
    matchSubmitting.value
)

const cancelSelectedDisabled = computed(
  () =>
    cancelSubmitting.value ||
    !detailSelectedItems.value.length ||
    !detailSelectedItems.value.every((item) => item.status === 10)
)

const matchSelectionSummary = computed(() => {
  const entries = Object.values(matchDrafts)
  return {
    totalCount: entries.reduce((sum, item) => sum + Number(item.matchCount || 0), 0),
    totalAmount: entries.reduce((sum, item) => sum + Number(item.matchAmount || 0), 0)
  }
})

const matchSubmitDisabled = computed(() => {
  if (matchSubmitting.value || pendingLoading.value || !matchContext.id) {
    return true
  }
  return !buildMatchItems().length
})

const resetFormData = () => {
  Object.assign(formData, {
    id: undefined,
    supplierId: undefined,
    invoiceNo: '',
    invoiceDate: '',
    invoiceType: undefined,
    totalCount: undefined,
    totalAmount: undefined,
    toleranceAmount: 1,
    differenceReason: '',
    remark: ''
  })
}

const resetPendingDrafts = () => {
  Object.keys(matchDrafts).forEach((key) => delete matchDrafts[key])
}

const ensurePendingDraft = (row: ErpApInvoicePendingItemVO) => {
  const key = getPendingDraftKey(row)
  if (!matchDrafts[key]) {
    matchDrafts[key] = {
      matchCount: undefined,
      matchAmount: undefined,
      remark: ''
    }
  }
}

const getPendingDraftKey = (row: ErpApInvoicePendingItemVO) => String(row.sourcePurchaseInItemId || '')

const formatAmount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return erpPriceInputFormatter(value)
}

const formatSignedAmount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  const abs = erpPriceInputFormatter(Math.abs(Number(value)))
  if (Number(value) > 0) {
    return `+${abs}`
  }
  if (Number(value) < 0) {
    return `-${abs}`
  }
  return abs
}

const formatCount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  const numeric = Number(value)
  if (Number.isNaN(numeric)) {
    return '-'
  }
  return numeric.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 3
  })
}

const formatPriceColumn = (_row: unknown, _column: unknown, cellValue: unknown) => {
  return formatAmount(cellValue as number | undefined)
}

const formatCountColumn = (_row: unknown, _column: unknown, cellValue: unknown) => {
  return erpCountTableColumnFormatter(_row, _column, cellValue, undefined)
}

const formatDateValue = (value?: string, pattern = 'YYYY-MM-DD HH:mm:ss') => {
  return value ? formatDate(new Date(value), pattern) : '-'
}

const getInvoiceTypeLabel = (value?: number) =>
  ERP_AP_INVOICE_TYPE_OPTIONS.find((item) => item.value === value)?.label || '-'

const getMatchStatusLabel = (value?: number) =>
  ERP_AP_INVOICE_MATCH_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getMatchItemStatusLabel = (value?: number) =>
  ERP_AP_INVOICE_MATCH_ITEM_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getMatchStatusClass = (value?: number) =>
  (
    {
      10: 'finance-ap-invoice-page__status-pill--neutral',
      20: 'finance-ap-invoice-page__status-pill--warning',
      30: 'finance-ap-invoice-page__status-pill--success',
      40: 'finance-ap-invoice-page__status-pill--danger'
    } as Record<number, string>
  )[value || 0] || 'finance-ap-invoice-page__status-pill--neutral'

const getMatchItemStatusClass = (value?: number) =>
  (
    {
      10: 'finance-ap-invoice-page__status-pill--primary',
      20: 'finance-ap-invoice-page__status-pill--danger'
    } as Record<number, string>
  )[value || 0] || 'finance-ap-invoice-page__status-pill--neutral'

const getMatchedRate = (row: Pick<ErpApInvoiceVO, 'matchedAmount' | 'totalAmount'>) => {
  const totalAmount = Math.abs(Number(row.totalAmount || 0))
  if (!totalAmount) {
    return 0
  }
  return Math.min(Math.round((Math.abs(Number(row.matchedAmount || 0)) / totalAmount) * 100), 100)
}

const canEditRow = (row: ErpApInvoiceVO) => Number(row.matchStatus || 10) === 10
const canOpenMatch = (row: ErpApInvoiceVO) =>
  !!row.id && !!row.supplierId && Number(row.unmatchedAmount || 0) > 0

const canSelectMatchItem = (row: ErpApInvoiceMatchItemVO) => row.status === 10

const loadSupplierList = async () => {
  supplierLoading.value = true
  try {
    const data = await SupplierApi.getSupplierSimpleList()
    supplierList.value = data?.length ? data : isLocalDemoHost() ? getDemoSupplierList() : []
  } catch (error: any) {
    if (isLocalDemoHost()) {
      supplierList.value = getDemoSupplierList()
      return
    }
    message.error(error?.message || '供应商列表加载失败')
  } finally {
    supplierLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await ApInvoiceApi.getApInvoicePage(queryParams)
    const rows = data?.list || []
    list.value = rows.length ? rows : isLocalDemoHost() ? demoInvoices : []
    total.value = rows.length ? (data?.total || rows.length) : isLocalDemoHost() ? demoInvoices.length : 0
  } catch (error: any) {
    if (isLocalDemoHost()) {
      list.value = demoInvoices
      total.value = demoInvoices.length
    } else {
      list.value = []
      total.value = 0
      listErrorMessage.value = error?.message || '采购发票列表加载失败'
    }
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    invoiceNo: '',
    supplierId: undefined,
    invoiceType: undefined,
    matchStatus: undefined,
    invoiceDate: [],
    remark: ''
  })
  advancedExpanded.value = false
  queryFormRef.value?.clearValidate()
  await getList()
}

const handlePagination = async () => {
  await getList()
}

const openFormDialog = async (id?: number) => {
  formDialogMode.value = id ? 'update' : 'create'
  formDialogVisible.value = true
  formLoading.value = false
  saveSubmitting.value = false
  editingId.value = id
  resetFormData()
  await nextTick()
  formRef.value?.clearValidate()
  if (!id) {
    return
  }
  formLoading.value = true
  try {
    const data = await ApInvoiceApi.getApInvoice(id)
    Object.assign(formData, {
      id: data.id,
      supplierId: data.supplierId,
      invoiceNo: data.invoiceNo || '',
      invoiceDate: data.invoiceDate || '',
      invoiceType: data.invoiceType,
      totalCount: data.totalCount,
      totalAmount: data.totalAmount,
      toleranceAmount: data.toleranceAmount ?? 1,
      differenceReason: data.differenceReason || '',
      remark: data.remark || ''
    })
  } catch (error: any) {
    message.error(error?.message || '采购发票详情加载失败')
    formDialogVisible.value = false
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  if (saveSubmitting.value) {
    return
  }
  await formRef.value?.validate()
  saveSubmitting.value = true
  try {
    const payload: ErpApInvoiceSaveReqVO = {
      id: editingId.value,
      supplierId: Number(formData.supplierId),
      invoiceNo: formData.invoiceNo?.trim(),
      invoiceDate: formData.invoiceDate,
      invoiceType: Number(formData.invoiceType),
      totalCount: formData.totalCount,
      totalAmount: formData.totalAmount,
      toleranceAmount: formData.toleranceAmount,
      differenceReason: formData.differenceReason?.trim(),
      remark: formData.remark?.trim()
    }
    if (formDialogMode.value === 'create') {
      await ApInvoiceApi.createApInvoice(payload)
      message.success('采购发票创建成功')
    } else {
      await ApInvoiceApi.updateApInvoice(payload)
      message.success('采购发票更新成功')
    }
    formDialogVisible.value = false
    await getList()
  } catch (error: any) {
    message.error(error?.message || (formDialogMode.value === 'create' ? '采购发票创建失败' : '采购发票更新失败'))
  } finally {
    saveSubmitting.value = false
  }
}

const loadDetail = async () => {
  if (detailId.value == null) {
    return
  }
  detailLoading.value = true
  detailErrorMessage.value = ''
  try {
    const data = await ApInvoiceApi.getApInvoice(detailId.value)
    detailData.value = data || findDemoInvoice(detailId.value) || null
  } catch (error: any) {
    if (isLocalDemoHost()) {
      detailData.value = findDemoInvoice(detailId.value) || null
    } else {
      detailData.value = null
      detailErrorMessage.value = error?.message || '采购发票详情加载失败'
    }
  } finally {
    detailLoading.value = false
  }
}

const openDetailDialog = async (id?: number) => {
  if (id == null) {
    return
  }
  detailId.value = id
  detailData.value = isLocalDemoHost() ? findDemoInvoice(id) || null : null
  detailErrorMessage.value = ''
  detailSelectedItems.value = []
  detailDialogVisible.value = true
  if (isLocalDemoHost() && detailData.value) {
    return
  }
  await loadDetail()
}

const handleDetailSelectionChange = (rows: ErpApInvoiceMatchItemVO[]) => {
  detailSelectedItems.value = rows
}

const handleCancelMatch = async () => {
  if (cancelSelectedDisabled.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定撤销选中的 ${detailSelectedItems.value.length} 条匹配明细吗？`,
      '撤销匹配',
      {
        type: 'warning',
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    )
  } catch {
    return
  }

  cancelSubmitting.value = true
  try {
    const payload: ErpApInvoiceCancelMatchReqVO = {
      ids: detailSelectedItems.value.map((item) => Number(item.id)).filter(Boolean)
    }
    await ApInvoiceApi.cancelMatch(payload)
    message.success('匹配撤销成功')
    detailSelectedItems.value = []
    await Promise.all([loadDetail(), getList()])
  } catch (error: any) {
    message.error(error?.message || '匹配撤销失败')
  } finally {
    cancelSubmitting.value = false
  }
}

const resetPendingQueryState = () => {
  Object.assign(pendingQueryParams, {
    pageNo: 1,
    pageSize: 10,
    invoiceId: matchContext.id,
    supplierId: matchContext.supplierId,
    sourceOrderNo: '',
    purchaseInNo: ''
  })
}

const getPendingList = async () => {
  pendingLoading.value = true
  pendingErrorMessage.value = ''
  try {
    const data = await ApInvoiceApi.getPendingItemPage(pendingQueryParams)
    const rows = data?.list || []
    pendingList.value = rows.length ? rows : isLocalDemoHost() ? getDemoPendingList(pendingQueryParams.invoiceId) : []
    pendingTotal.value = rows.length ? (data?.total || rows.length) : isLocalDemoHost() ? pendingList.value.length : 0
    pendingList.value.forEach((row) => ensurePendingDraft(row))
  } catch (error: any) {
    if (isLocalDemoHost()) {
      pendingList.value = getDemoPendingList(pendingQueryParams.invoiceId)
      pendingTotal.value = pendingList.value.length
      pendingList.value.forEach((row) => ensurePendingDraft(row))
    } else {
      pendingList.value = []
      pendingTotal.value = 0
      pendingErrorMessage.value = error?.message || '待匹配池加载失败'
    }
  } finally {
    pendingLoading.value = false
  }
}

const openMatchDialog = async (row: ErpApInvoiceVO) => {
  Object.assign(matchContext, {
    id: row.id,
    supplierId: row.supplierId,
    supplierName: row.supplierName || '',
    invoiceNo: row.invoiceNo || '',
    totalAmount: row.totalAmount || 0,
    unmatchedAmount: row.unmatchedAmount || 0
  })
  matchDifferenceReason.value = row.differenceReason || ''
  resetPendingDrafts()
  resetPendingQueryState()
  pendingList.value = []
  pendingTotal.value = 0
  pendingErrorMessage.value = ''
  matchDialogVisible.value = true
  await getPendingList()
}

const handlePendingQuery = async () => {
  pendingQueryParams.pageNo = 1
  await getPendingList()
}

const resetPendingQuery = async () => {
  pendingQueryFormRef.value?.clearValidate()
  resetPendingQueryState()
  await getPendingList()
}

const buildMatchItems = () => {
  return pendingList.value
    .map((row) => {
      const draft = matchDrafts[getPendingDraftKey(row)]
      return {
        purchaseInItemId: Number(row.sourcePurchaseInItemId),
        matchCount: Number(draft?.matchCount || 0),
        matchAmount: Number(draft?.matchAmount || 0),
        remark: draft?.remark?.trim()
      }
    })
    .filter((item) => item.purchaseInItemId && item.matchCount > 0 && item.matchAmount > 0)
}

const submitMatch = async () => {
  if (matchSubmitting.value || !matchContext.id) {
    return
  }
  const items = buildMatchItems()
  if (!items.length) {
    message.warning('请至少填写一条匹配明细')
    return
  }
  matchSubmitting.value = true
  try {
    const payload: ErpApInvoiceConfirmMatchReqVO = {
      invoiceId: Number(matchContext.id),
      differenceReason: matchDifferenceReason.value?.trim(),
      items
    }
    await ApInvoiceApi.confirmMatch(payload)
    message.success('采购发票匹配成功')
    matchDialogVisible.value = false
    await Promise.all([getList(), detailDialogVisible.value && detailId.value === matchContext.id ? loadDetail() : Promise.resolve()])
  } catch (error: any) {
    message.error(error?.message || '采购发票匹配失败')
  } finally {
    matchSubmitting.value = false
  }
}

watch(formDialogVisible, (visible) => {
  if (visible) {
    return
  }
  formLoading.value = false
  saveSubmitting.value = false
  editingId.value = undefined
  resetFormData()
  formRef.value?.clearValidate()
})

watch(detailDialogVisible, async (visible) => {
  if (visible) {
    return
  }
  detailId.value = null
  detailData.value = null
  detailErrorMessage.value = ''
  detailSelectedItems.value = []
  await nextTick()
  detailTableRef.value?.clearSelection?.()
})

watch(matchDialogVisible, (visible) => {
  if (visible) {
    return
  }
  pendingList.value = []
  pendingTotal.value = 0
  pendingErrorMessage.value = ''
  matchDifferenceReason.value = ''
  resetPendingDrafts()
  Object.assign(matchContext, {
    id: undefined,
    supplierId: undefined,
    supplierName: '',
    invoiceNo: '',
    totalAmount: 0,
    unmatchedAmount: 0
  })
})

onMounted(async () => {
  await Promise.all([loadSupplierList(), getList()])
})
</script>

<style scoped>
.finance-ap-invoice-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.finance-ap-invoice-page__card-header,
.finance-ap-invoice-detail__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.finance-ap-invoice-page__card-title,
.finance-ap-invoice-page__section-title,
.finance-ap-invoice-dialog__panel-title,
.finance-ap-invoice-detail__panel-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.finance-ap-invoice-page__query-form {
  padding-top: 4px;
}

.finance-ap-invoice-page__query-grid,
.finance-ap-invoice-dialog__grid,
.finance-ap-invoice-match__query-grid {
  display: grid;
  gap: 0 16px;
}

.finance-ap-invoice-page__query-grid--basic {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.finance-ap-invoice-page__query-grid--advanced {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.finance-ap-invoice-page__advanced-block,
.finance-ap-invoice-match__difference-panel {
  margin-top: 8px;
  padding: 14px 16px 4px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.finance-ap-invoice-page__query-actions,
.finance-ap-invoice-match__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
}

.finance-ap-invoice-page__toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.finance-ap-invoice-page__toolbar-left {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.finance-ap-invoice-page__state,
.finance-ap-invoice-match__state {
  padding: 18px 0 6px;
}

.finance-ap-invoice-page__table-wrap,
.finance-ap-invoice-detail__table-wrap,
.finance-ap-invoice-match__table-wrap {
  overflow-x: auto;
}

.finance-ap-invoice-page__table-wrap :deep(.el-table__header-wrapper th),
.finance-ap-invoice-detail__table-wrap :deep(.el-table__header-wrapper th),
.finance-ap-invoice-match__table-wrap :deep(.el-table__header-wrapper th) {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #ffffff;
}

.finance-ap-invoice-page__primary-cell,
.finance-ap-invoice-page__amount-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.finance-ap-invoice-page__primary-text {
  color: #0f172a;
  font-weight: 600;
}

.finance-ap-invoice-page__muted-text {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  word-break: break-all;
}

.finance-ap-invoice-page__amount-text {
  color: #1d4ed8;
  font-weight: 700;
}

.finance-ap-invoice-page__status-pill {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.4;
}

.finance-ap-invoice-page__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.8;
}

.finance-ap-invoice-page__status-pill--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.finance-ap-invoice-page__status-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #64748b;
}

.finance-ap-invoice-page__status-pill--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.finance-ap-invoice-page__status-pill--success {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #059669;
}

.finance-ap-invoice-page__status-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.finance-ap-invoice-page__progress-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.finance-ap-invoice-page__progress-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  color: #475569;
  font-size: 12px;
}

.finance-ap-invoice-page__progress-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.finance-ap-invoice-page__progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb 0%, #0f766e 100%);
}

.finance-ap-invoice-dialog,
.finance-ap-invoice-detail,
.finance-ap-invoice-match {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.finance-ap-invoice-dialog__panel,
.finance-ap-invoice-detail__panel {
  padding: 18px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-ap-invoice-dialog__grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.finance-ap-invoice-detail__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #ffffff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.18);
}

.finance-ap-invoice-detail__eyebrow,
.finance-ap-invoice-match__context-eyebrow {
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.finance-ap-invoice-detail__title,
.finance-ap-invoice-match__context-title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.finance-ap-invoice-detail__meta,
.finance-ap-invoice-match__context-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
}

.finance-ap-invoice-detail__summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.finance-ap-invoice-detail__summary-card {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 6px 18px rgba(148, 163, 184, 0.06);
}

.finance-ap-invoice-detail__summary-card--accent {
  border-color: #fecdd3;
  background: linear-gradient(180deg, #fff1f2 0%, #ffffff 100%);
}

.finance-ap-invoice-detail__summary-label,
.finance-ap-invoice-match__context-label {
  color: #64748b;
  font-size: 12px;
}

.finance-ap-invoice-detail__summary-value,
.finance-ap-invoice-match__context-value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
}

.finance-ap-invoice-detail__summary-value--accent {
  color: #e11d48;
}

.finance-ap-invoice-match__context-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #134e4a 100%);
  color: #ffffff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.18);
}

.finance-ap-invoice-match__context-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  width: min(360px, 100%);
}

.finance-ap-invoice-match__context-metric {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
}

.finance-ap-invoice-match__query-form {
  padding: 4px 0 0;
}

.finance-ap-invoice-match__query-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1400px) {
  .finance-ap-invoice-page__query-grid--basic {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .finance-ap-invoice-page__query-grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1280px) {
  .finance-ap-invoice-detail__summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .finance-ap-invoice-dialog__grid,
  .finance-ap-invoice-match__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-ap-invoice-match__context-card {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .finance-ap-invoice-page__query-grid--basic,
  .finance-ap-invoice-page__query-grid--advanced,
  .finance-ap-invoice-detail__summary-grid {
    grid-template-columns: 1fr;
  }

  .finance-ap-invoice-detail__hero {
    flex-direction: column;
  }

  .finance-ap-invoice-page__query-actions,
  .finance-ap-invoice-match__query-actions {
    justify-content: stretch;
  }
}
</style>
