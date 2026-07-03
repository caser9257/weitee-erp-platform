<template>

  <ContentWrap>
    <div class="mrp-plan-header">MRP 计划列表</div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="mrp-plan-query-form">
      <el-form-item label="计划编号" prop="planNo">
        <el-input
          v-model="queryParams.planNo"
          placeholder="请输入完整或部分编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="计划名称" prop="planName">
        <el-input
          v-model="queryParams.planName"
          placeholder="支持模糊搜索"
          clearable
          @keyup.enter="handleQuery"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="运行状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-full">
          <el-option
            v-for="item in planStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划区间" prop="planDate">
        <el-date-picker
          v-model="queryParams.planDate"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item class="mrp-plan-query-form__actions">
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh-left" class="mr-5px" />
          重置
        </el-button>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索记录
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="mrp-plan-table-card">
      <div class="mrp-plan-toolbar">
        <el-button type="primary" @click="openCreateDialog" v-hasPermi="['erp:mrp-plan:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新建 MRP 计划
        </el-button>
      </div>

      <el-table v-loading="loading" :data="list" :show-overflow-tooltip="false" class="mrp-plan-table">
        <el-table-column label="计划标识（名称/编号）" min-width="260">
          <template #default="{ row }">
            <div class="mrp-plan-identity">
              <div class="mrp-plan-identity__title">{{ row.planName || '-' }}</div>
              <div class="mrp-plan-identity__meta">ID：{{ row.planNo || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="运行状态" align="center" width="120">
          <template #default="{ row }">
            <span class="mrp-plan-status-chip" :class="getPlanStatusClass(row.status)">
              {{ getPlanStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="计划周期（起/止）" min-width="170">
          <template #default="{ row }">
            <div class="mrp-plan-time-stack">
              <div class="mrp-plan-time-stack__item">
                <span class="mrp-plan-time-stack__label">起</span>
                <span class="mrp-plan-time-stack__value">{{ formatPlanDate(row.planStartDate) }}</span>
              </div>
              <div class="mrp-plan-time-stack__item">
                <span class="mrp-plan-time-stack__label">止</span>
                <span class="mrp-plan-time-stack__value">{{ formatPlanDate(row.planEndDate) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="系统时间（创建/运行）" min-width="210">
          <template #default="{ row }">
            <div class="mrp-plan-time-stack">
              <div class="mrp-plan-time-stack__item">
                <span class="mrp-plan-time-stack__label">创建</span>
                <span class="mrp-plan-time-stack__value">{{ formatDateTimeText(row.createTime) }}</span>
              </div>
              <div class="mrp-plan-time-stack__item">
                <span class="mrp-plan-time-stack__label">运行</span>
                <span class="mrp-plan-time-stack__value">{{ formatDateTimeText(row.runTime) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="备注信息" min-width="220">
          <template #default="{ row }">
            <div class="mrp-plan-remark">{{ row.remark || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="业务操作" fixed="right" width="220" align="center">
          <template #default="{ row }">
            <div class="mrp-plan-actions">
              <el-button
                v-if="hasPlanRunPermission && isRunnablePlan(row.status)"
                link
                type="success"
                @click="handleRun(row)"
                :disabled="runSubmittingPlanId === row.id"
              >
                {{ getRunPlanActionLabel(row.status) }}
              </el-button>
              <el-button
                v-if="hasPlanQueryPermission && isRunningPlan(row.status)"
                link
                type="primary"
                @click="openResultDialog(row)"
              >
                查看进度
              </el-button>
              <el-button
                v-if="hasPlanQueryPermission && isReadablePlan(row.status)"
                link
                type="primary"
                @click="openResultDialog(row)"
              >
                查看结果
              </el-button>
              <el-button
                v-if="hasSuggestQueryPermission && showSuggestAction(row.status)"
                link
                type="warning"
                @click="openSuggestPage(row)"
              >
                查看建议
              </el-button>
              <span v-if="!hasVisiblePlanActions(row)" class="mrp-plan-actions__placeholder">-</span>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="mrp-plan-empty">
            <Icon icon="ep:document-remove" class="mrp-plan-empty__icon" />
            <div class="mrp-plan-empty__title">暂无计划记录</div>
          </div>
        </template>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <Dialog :title="planFormTitle" v-model="planFormVisible" width="600px">
    <el-form
      ref="planFormRef"
      :model="planFormData"
      :rules="planFormRules"
      label-width="100px"
      v-loading="planFormLoading"
    >
      <el-form-item label="计划名称" prop="planName">
        <el-input v-model="planFormData.planName" placeholder="请输入计划名称" />
      </el-form-item>
      <el-form-item label="计划区间" prop="planDateRange">
        <el-date-picker
          v-model="planDateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-1/1"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="planFormData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="planFormLoading" @click="submitPlanForm">确定</el-button>
      <el-button @click="planFormVisible = false">取消</el-button>
    </template>
  </Dialog>

  <Dialog
    :title="resultDialogTitle"
    v-model="resultDialogVisible"
    width="1200px"
    @close="resetResultDialogState"
  >
    <el-tabs v-model="resultTab">
      <el-tab-pane :label="`运算结果（${resultList.length}）`" name="result" />
      <el-tab-pane :label="`缺料清单（${shortageList.length}）`" name="shortage" />
    </el-tabs>

    <el-alert
      v-if="resultLoadError"
      :title="resultLoadError"
      type="error"
      show-icon
      :closable="false"
      class="mb-12px"
    />
    <el-alert
      v-if="resultTab === 'result' && resultList.length > 0 && !hasResultTraceDetails"
      title="当前结果为旧版本，未包含 trace 明细"
      type="info"
      show-icon
      :closable="false"
      class="mb-12px"
    />
    <el-alert
      v-if="resultTab === 'shortage' && shortageList.length > 0 && !hasShortageTraceDetails"
      title="当前缺料清单为旧版本，未包含 trace 明细"
      type="info"
      show-icon
      :closable="false"
      class="mb-12px"
    />

    <template v-if="resultTab === 'result'">
      <el-form :model="resultFilterForm" label-width="70px" class="mb-12px">
        <el-row :gutter="12">
          <el-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <el-form-item label="关键字" class="!mb-12px">
              <el-input
                v-model="resultFilterForm.keyword"
                clearable
                placeholder="请输入母项 / 物料 / 销售单号"
              />
            </el-form-item>
          </el-col>
          <el-col :xl="5" :lg="8" :md="12" :sm="24" :xs="24">
            <el-form-item label="业务类型" class="!mb-12px">
              <el-select
                v-model="resultFilterForm.businessType"
                clearable
                placeholder="请选择业务类型"
                class="!w-full"
              >
                <el-option
                  v-for="item in resultBusinessTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xl="6" :lg="8" :md="12" :sm="24" :xs="24">
            <el-form-item label="跳过原因" class="!mb-12px">
              <el-select
                v-model="resultFilterForm.skipReason"
                clearable
                placeholder="请选择跳过原因"
                class="!w-full"
              >
                <el-option
                  v-for="item in resultSkipReasonOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xl="5" :lg="24" :md="12" :sm="24" :xs="24">
            <el-form-item label-width="0" class="!mb-12px">
              <div class="flex w-full flex-wrap items-center justify-end gap-8px max-sm:justify-start">
                <el-checkbox v-model="resultFilterForm.onlySkipped">仅看已跳过</el-checkbox>
                <el-button :disabled="!hasResultFilters" @click="handleResetResultFilters">
                  重置
                </el-button>
                <el-button
                  type="primary"
                  plain
                  :loading="resultExporting"
                  :disabled="!canExportResult"
                  @click="handleExportResult"
                >
                  <Icon icon="ep:download" class="mr-5px" />
                  导出结果
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div
        v-if="resultActiveFilterTags.length > 0"
        class="mb-12px flex flex-wrap items-center gap-8px rounded-4px border border-[var(--el-border-color-light)] px-12px py-10px"
      >
        <span class="text-13px text-[var(--el-text-color-secondary)]">当前筛选</span>
        <el-tag
          v-for="item in resultActiveFilterTags"
          :key="item.key"
          closable
          @close="handleRemoveResultFilterTag(item.key)"
        >
          {{ item.label }}
        </el-tag>
        <el-button link type="primary" @click="handleClearAllResultFilters">清空全部筛选</el-button>
      </div>

      <div class="mb-12px rounded-4px border border-[var(--el-border-color-light)] p-12px">
        <div class="mb-12px text-14px font-600">跳过原因复盘</div>
        <el-row :gutter="12" class="mb-12px">
          <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
            <button
              type="button"
              class="mrp-summary-card"
              :class="{ 'is-active': activeReviewMode === 'all' && !activeReviewReasonKey }"
              :disabled="resultLoading"
              @click="handleReviewFilterAll"
            >
              <div class="mrp-summary-card__label">结果总数</div>
              <div class="mrp-summary-card__value">{{ resultReviewSummary.totalCount }}</div>
            </button>
          </el-col>
          <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
            <button
              type="button"
              class="mrp-summary-card"
              :class="{ 'is-active': activeReviewMode === 'active' }"
              :disabled="resultLoading"
              @click="handleReviewFilterActive"
            >
              <div class="mrp-summary-card__label">参与运算</div>
              <div class="mrp-summary-card__value">{{ resultReviewSummary.activeCount }}</div>
            </button>
          </el-col>
          <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
            <button
              type="button"
              class="mrp-summary-card"
              :class="{ 'is-active': activeReviewMode === 'skipped' && !activeReviewReasonKey }"
              :disabled="resultLoading"
              @click="handleReviewFilterSkipped"
            >
              <div class="mrp-summary-card__label">已跳过</div>
              <div class="mrp-summary-card__value">{{ resultReviewSummary.skippedCount }}</div>
            </button>
          </el-col>
          <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
            <button
              type="button"
              class="mrp-summary-card"
              :class="{ 'is-active': activeReviewMode === 'skipped' && !activeReviewReasonKey }"
              :disabled="resultLoading"
              @click="handleReviewFilterSkipped"
            >
              <div class="mrp-summary-card__label">跳过占比</div>
              <div class="mrp-summary-card__value">{{ resultReviewSummary.skippedRatio }}</div>
            </button>
          </el-col>
        </el-row>
        <div class="flex flex-wrap items-center gap-8px">
          <el-button
            v-if="hasReviewDrillDown"
            link
            type="primary"
            :disabled="resultLoading"
            @click="handleReviewFilterAll"
          >
            清空复盘筛选
          </el-button>
          <el-tag
            v-for="item in resultReviewSummary.skipReasonBreakdown"
            :key="item.key"
            :effect="activeReviewReasonKey === item.key ? 'dark' : 'plain'"
            type="warning"
            class="cursor-pointer"
            @click="handleReviewFilterByReason(item.key)"
          >
            {{ item.label }}：{{ item.count }}
          </el-tag>
          <span
            v-if="resultReviewSummary.skipReasonBreakdown.length === 0"
            class="text-[var(--el-text-color-placeholder)]"
          >
            无
          </span>
        </div>
      </div>

      <el-table
        v-loading="resultLoading"
        :data="filteredResultList"
        :stripe="true"
        :show-overflow-tooltip="true"
        :empty-text="resultEmptyText"
      >
        <el-table-column label="追溯路径" min-width="220">
          <template #default="scope">
            <div class="mrp-trace-path">
              <div class="mrp-trace-path__line">层级：{{ formatTraceLevel(scope.row.traceLevel) }}</div>
              <div class="mrp-trace-path__line">父件：{{ formatTraceValue(scope.row.parentMaterialId) }}</div>
              <div class="mrp-trace-path__line mrp-trace-path__line--mono">
                {{ formatTraceValue(scope.row.tracePathKey) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="母项" prop="rootProductName" align="center" min-width="150">
          <template #default="scope">
            <span v-html="renderKeywordHighlight(scope.row.rootProductName, resultFilterForm.keyword)"></span>
          </template>
        </el-table-column>
        <el-table-column label="物料" prop="materialName" align="center" min-width="150">
          <template #default="scope">
            <span v-html="renderKeywordHighlight(scope.row.materialName, resultFilterForm.keyword)"></span>
          </template>
        </el-table-column>
        <el-table-column label="毛需求" prop="grossDemandQty" align="center" min-width="110" />
        <el-table-column label="现存" prop="availableStockQty" align="center" min-width="110" />
        <el-table-column label="在途" prop="incomingQty" align="center" min-width="110" />
        <el-table-column label="在制" prop="wipQty" align="center" min-width="110" />
        <el-table-column label="净需求" prop="netDemandQty" align="center" min-width="110">
          <template #default="scope">
            <el-tag type="danger">{{ formatQty(scope.row.netDemandQty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="供应方式" prop="suggestType" align="center" min-width="100">
          <template #default="scope">
            {{ getSupplyModeLabel(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="业务类型" prop="businessType" align="center" min-width="120">
          <template #default="scope">
            {{ getBusinessTypeLabel(scope.row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column label="跳过原因" prop="skipReason" align="center" min-width="220">
          <template #default="scope">
            {{ getSkipReasonLabel(scope.row.skipReason) }}
          </template>
        </el-table-column>
        <el-table-column label="建议日期" prop="suggestDate" align="center" width="120" />
        <el-table-column label="需求日期" prop="demandDate" align="center" width="120" />
        <el-table-column label="来源销售单" prop="sourceOrderId" align="center" min-width="110">
          <template #default="scope">
            <span v-html="renderKeywordHighlight(scope.row.sourceOrderId, resultFilterForm.keyword)"></span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="110" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!row.id" @click="openResultExplain(row)">
              查看解释
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template v-else>
      <el-form :model="shortageFilterForm" label-width="70px" class="mb-12px">
        <el-row :gutter="12">
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <el-form-item label="关键字" class="!mb-12px">
              <el-input
                v-model="shortageFilterForm.keyword"
                clearable
                placeholder="请输入母项 / 物料 / 销售单号"
              />
            </el-form-item>
          </el-col>
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <el-form-item label-width="0" class="!mb-12px">
              <div class="flex w-full flex-wrap items-center justify-end gap-8px max-sm:justify-start">
                <el-button :disabled="!hasShortageFilters" @click="handleResetShortageFilters">
                  重置
                </el-button>
                <el-button
                  type="primary"
                  plain
                  :loading="shortageExporting"
                  :disabled="!canExportShortage"
                  @click="handleExportShortage"
                >
                  <Icon icon="ep:download" class="mr-5px" />
                  导出缺料
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div
        v-if="shortageActiveFilterTags.length > 0"
        class="mb-12px flex flex-wrap items-center gap-8px rounded-4px border border-[var(--el-border-color-light)] px-12px py-10px"
      >
        <span class="text-13px text-[var(--el-text-color-secondary)]">当前筛选</span>
        <el-tag
          v-for="item in shortageActiveFilterTags"
          :key="item.key"
          closable
          @close="handleRemoveShortageFilterTag(item.key)"
        >
          {{ item.label }}
        </el-tag>
        <el-button link type="primary" @click="handleClearAllShortageFilters">
          清空全部筛选
        </el-button>
      </div>

      <el-table
        v-loading="resultLoading"
        :data="filteredShortageList"
        :stripe="true"
        :show-overflow-tooltip="true"
        :empty-text="shortageEmptyText"
      >
        <el-table-column type="expand" width="48">
          <template #default="{ row }">
            <div class="pl-32px pr-12px pb-12px">
              <template v-if="row.substitutes && row.substitutes.length > 0">
                <el-table :data="row.substitutes" border size="small">
                  <el-table-column type="index" label="#" width="56" align="center" />
                  <el-table-column label="替代物料" prop="substituteMaterialName" min-width="180" />
                  <el-table-column label="优先级" prop="priority" width="100" align="center" />
                  <el-table-column label="替换比例" prop="replaceRatio" width="120" align="center" />
                  <el-table-column label="自动推荐" width="120" align="center">
                    <template #default="{ row: substitute }">
                      {{ substitute.enableAutoRecommend ? '是' : '否' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="排序" prop="sort" width="100" align="center" />
                  <el-table-column label="备注" prop="remark" min-width="160" />
                </el-table>
              </template>
              <el-empty v-else description="暂无替代料推荐" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="追溯路径" min-width="220">
          <template #default="scope">
            <div class="mrp-trace-path">
              <div class="mrp-trace-path__line">层级：{{ formatTraceLevel(scope.row.traceLevel) }}</div>
              <div class="mrp-trace-path__line">父件：{{ formatTraceValue(scope.row.parentMaterialId) }}</div>
              <div class="mrp-trace-path__line mrp-trace-path__line--mono">
                {{ formatTraceValue(scope.row.tracePathKey) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="母项" prop="rootProductName" align="center" min-width="150">
          <template #default="scope">
            <span
              v-html="renderKeywordHighlight(scope.row.rootProductName, shortageFilterForm.keyword)"
            ></span>
          </template>
        </el-table-column>
        <el-table-column label="缺料物料" prop="materialName" align="center" min-width="150">
          <template #default="scope">
            <span v-html="renderKeywordHighlight(scope.row.materialName, shortageFilterForm.keyword)"></span>
          </template>
        </el-table-column>
        <el-table-column label="缺料数量" prop="shortageQty" align="center" min-width="120">
          <template #default="scope">
            <el-tag type="warning">{{ formatQty(scope.row.shortageQty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="替代料推荐" align="center" min-width="120">
          <template #default="scope">
            {{ formatShortageSubstituteSummary(scope.row.substitutes) }}
          </template>
        </el-table-column>
        <el-table-column label="需求日期" prop="requiredDate" align="center" width="120" />
        <el-table-column label="来源销售单" prop="sourceOrderId" align="center" min-width="110">
          <template #default="scope">
            <span v-html="renderKeywordHighlight(scope.row.sourceOrderId, shortageFilterForm.keyword)"></span>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </Dialog>

  <ResultExplainDialog ref="resultExplainDialogRef" />
</template>

<script setup lang="ts">
import { downloadByData } from '@/utils/filt'
import { formatDate } from '@/utils/formatTime'
import {
  MrpPlanApi,
  MrpPlanPageReqVO,
  MrpPlanSaveReqVO,
  MrpPlanVO,
  MrpResultVO,
  MrpShortageSubstituteVO,
  MrpShortageVO
} from '@/api/erp/mrp/plan'
import ResultExplainDialog from './ResultExplainDialog.vue'
import { checkPermi } from '@/utils/permission'

defineOptions({ name: 'ErpMrpPlan' })

type TagType = 'success' | 'warning' | 'primary' | 'info' | 'danger'
type ResultFilterForm = {
  keyword: string
  businessType: string | undefined
  skipReason: string | undefined
  onlySkipped: boolean
  onlyActive: boolean
}
type ShortageFilterForm = {
  keyword: string
}
type FilterTagItem = {
  key: string
  label: string
}

const message = useMessage()
const router = useRouter()
const resultExplainDialogRef = ref()
const hasPlanRunPermission = checkPermi(['erp:mrp-plan:run'])
const hasPlanQueryPermission = checkPermi(['erp:mrp-plan:query'])
const hasSuggestQueryPermission = checkPermi(['erp:mrp-suggest:query'])
const runSubmittingPlanId = ref<number>()
const planPollingTimers = new Map<number, ReturnType<typeof window.setTimeout>>()
const PLAN_DRAFT_STATUS = 0
const PLAN_RUNNING_STATUS = 10
const PLAN_FINISHED_STATUS = 20
const PLAN_CLOSED_STATUS = 30
const PLAN_FAILED_STATUS = 40
const PLAN_POLL_INTERVAL = 3000

const loading = ref(false)
const list = ref<MrpPlanVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive<MrpPlanPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  planNo: undefined,
  planName: undefined,
  status: undefined,
  planDate: undefined
})

const planStatusOptions = [
  { label: '待运行', value: PLAN_DRAFT_STATUS, type: 'info' as TagType },
  { label: '运行中', value: PLAN_RUNNING_STATUS, type: 'warning' as TagType },
  { label: '已完成', value: PLAN_FINISHED_STATUS, type: 'success' as TagType },
  { label: '已关闭', value: PLAN_CLOSED_STATUS, type: 'danger' as TagType },
  { label: '异常中止', value: PLAN_FAILED_STATUS, type: 'danger' as TagType }
]

const getPlanStatusLabel = (status?: number) => {
  return planStatusOptions.find((item) => item.value === status)?.label || '-'
}

const canRunPlan = (status?: number) => {
  return status === PLAN_DRAFT_STATUS || status === PLAN_FAILED_STATUS
}

const isRunnablePlan = (status?: number) => {
  return status === PLAN_DRAFT_STATUS || status === PLAN_FAILED_STATUS
}

const isRunningPlan = (status?: number) => {
  return status === PLAN_RUNNING_STATUS
}

const isReadablePlan = (status?: number) => {
  return status === PLAN_FINISHED_STATUS || status === PLAN_CLOSED_STATUS
}

const showSuggestAction = (status?: number) => {
  return status === PLAN_FINISHED_STATUS || status === PLAN_CLOSED_STATUS
}

const hasVisiblePlanActions = (row: MrpPlanVO) => {
  return (
    (hasPlanRunPermission && isRunnablePlan(row.status)) ||
    (hasPlanQueryPermission && (isRunningPlan(row.status) || isReadablePlan(row.status))) ||
    (hasSuggestQueryPermission && showSuggestAction(row.status))
  )
}

const getRunPlanActionLabel = (status?: number) => {
  return status === PLAN_FAILED_STATUS ? '重新运行' : '运行计划'
}

const getPlanStatusClass = (status?: number) => {
  if (status === PLAN_RUNNING_STATUS) {
    return 'is-running'
  }
  if (status === PLAN_FINISHED_STATUS) {
    return 'is-finished'
  }
  if (status === PLAN_FAILED_STATUS) {
    return 'is-failed'
  }
  if (status === PLAN_CLOSED_STATUS) {
    return 'is-closed'
  }
  return 'is-pending'
}

const formatPlanDate = (value?: string) => {
  return value || '-'
}

const formatDateTimeText = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const normalizedValue =
    typeof value === 'string' && /^\d{10,13}$/.test(value.trim())
      ? Number(value.trim().length === 10 ? `${value.trim()}000` : value.trim())
      : value
  return formatDate(normalizedValue) || String(value)
}

const getRunPlanConfirmText = (row: MrpPlanVO) => {
  return `确定${getRunPlanActionLabel(row.status)}【${row.planName}】吗？`
}

const getRunPlanSuccessText = (row: MrpPlanVO) => {
  return row.status === PLAN_FAILED_STATUS ? 'MRP 计划已重新提交后台运行' : 'MRP 计划已提交后台运行'
}

const getSuggestTypeLabel = (type?: string) => {
  if (type === 'PURCHASE') {
    return '采购'
  }
  if (type === 'MAKE') {
    return '生产'
  }
  return type || '-'
}

const getBusinessTypeLabel = (businessType?: string) => {
  if (businessType === 'SELF_RESEARCH') {
    return '自研'
  }
  if (businessType === 'CUSTOMER_SUPPLIED') {
    return '客供'
  }
  if (businessType === 'TOLL_MANUFACTURING') {
    return '来料加工'
  }
  return businessType || '-'
}

const getSkipReasonLabel = (skipReason?: string) => {
  if (!skipReason) {
    return '无'
  }
  if (skipReason === 'MRP_DISABLED') {
    return '系统未参与'
  }
  if (skipReason === 'CUSTOMER_SUPPLIED_CUSTOMER_OWNED') {
    return '客供业务下客户供料，不参与运算'
  }
  if (skipReason === 'TOLL_MANUFACTURING_DEFAULT_SKIP') {
    return '来料加工默认跳过非公司供料'
  }
  return skipReason
}

const getSupplyModeLabel = (row: MrpResultVO) => {
  if (Number(row.netDemandQty ?? 0) <= 0) {
    return '无建议'
  }
  return getSuggestTypeLabel(row.suggestType)
}

const formatQty = (value?: number) => Number(value ?? 0).toFixed(2)

const formatTraceValue = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return String(value)
}

const formatTraceLevel = (value?: number | null) => {
  if (value === undefined || value === null) {
    return '-'
  }
  return value === 0 ? '第 0 层' : `第 ${value} 层`
}

const formatShortageSubstituteSummary = (substitutes?: MrpShortageSubstituteVO[]) => {
  const count = substitutes?.length ?? 0
  return count > 0 ? `${count} 项` : '-'
}

const normalizeDateValue = (value: unknown) => {
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return value
}

const normalizePlanItem = (item: Partial<MrpPlanVO> & Record<string, any>): MrpPlanVO => {
  return {
    ...item,
    planStartDate: normalizeDateValue(item.planStartDate) as string,
    planEndDate: normalizeDateValue(item.planEndDate) as string
  } as MrpPlanVO
}

const getList = async () => {
  loading.value = true
  try {
    const data = await MrpPlanApi.getPlanPage(queryParams)
    const rawList = Array.isArray(data?.list) ? data.list : []
    list.value = rawList.map((item) => normalizePlanItem(item))
    total.value = Number(data?.total ?? 0)
    syncPollingWithList()
  } catch (error) {
    list.value = []
    total.value = 0
    Array.from(planPollingTimers.keys()).forEach((planId) => stopPlanPolling(planId))
    console.error('[MrpPlanPage][getList] error', error)
    message.error('MRP 计划列表加载失败，请刷新后重试')
  } finally {
    loading.value = false
  }
}

const stopPlanPolling = (planId: number) => {
  const timer = planPollingTimers.get(planId)
  if (timer) {
    window.clearTimeout(timer)
    planPollingTimers.delete(planId)
  }
}

const schedulePlanPolling = (planId: number) => {
  stopPlanPolling(planId)
  const timer = window.setTimeout(() => {
    pollPlanStatus(planId)
  }, PLAN_POLL_INTERVAL)
  planPollingTimers.set(planId, timer)
}

const updatePlanInList = (plan: MrpPlanVO) => {
  const index = list.value.findIndex((item) => item.id === plan.id)
  if (index >= 0) {
    list.value[index] = {
      ...list.value[index],
      ...plan
    }
  }
}

const pollPlanStatus = async (planId: number) => {
  try {
    const plan = normalizePlanItem(await MrpPlanApi.getPlan(planId))
    updatePlanInList(plan)
    if (plan.status === PLAN_RUNNING_STATUS) {
      schedulePlanPolling(planId)
      return
    }
    stopPlanPolling(planId)
    await getList()
  } catch (error) {
    stopPlanPolling(planId)
    console.error('[MrpPlanPage][pollPlanStatus] error', error)
  }
}

const startPlanPolling = (planId: number) => {
  if (planPollingTimers.has(planId)) {
    return
  }
  schedulePlanPolling(planId)
}

const syncPollingWithList = () => {
  const runningPlanIds = new Set(
    list.value.filter((item) => item.status === PLAN_RUNNING_STATUS).map((item) => item.id)
  )
  list.value.forEach((item) => {
    if (item.status === PLAN_RUNNING_STATUS) {
      startPlanPolling(item.id)
    }
  })
  Array.from(planPollingTimers.keys()).forEach((planId) => {
    if (!runningPlanIds.has(planId)) {
      stopPlanPolling(planId)
    }
  })
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const planFormVisible = ref(false)
const planFormTitle = ref('新建计划')
const planFormLoading = ref(false)
const planFormRef = ref()
const planFormData = reactive<MrpPlanSaveReqVO>({
  planName: '',
  planStartDate: '',
  planEndDate: '',
  remark: ''
})
const planDateRange = ref<string[]>([])
const planFormRules = reactive({
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planDateRange: [
    {
      validator: (_rule, _value, callback) => {
        if (!planDateRange.value || planDateRange.value.length !== 2) {
          callback(new Error('请选择计划区间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
})

const resetPlanForm = () => {
  planFormData.planName = ''
  planFormData.planStartDate = ''
  planFormData.planEndDate = ''
  planFormData.remark = ''
  planDateRange.value = []
  planFormRef.value?.resetFields()
}

const openCreateDialog = () => {
  resetPlanForm()
  planFormTitle.value = '新建 MRP 计划'
  planFormVisible.value = true
}

const submitPlanForm = async () => {
  await planFormRef.value?.validate()
  planFormData.planStartDate = planDateRange.value[0]
  planFormData.planEndDate = planDateRange.value[1]
  planFormLoading.value = true
  try {
    await MrpPlanApi.createPlan({
      planName: planFormData.planName,
      planStartDate: planFormData.planStartDate,
      planEndDate: planFormData.planEndDate,
      remark: planFormData.remark
    })
    message.success('MRP 计划创建成功')
    planFormVisible.value = false
    await getList()
  } finally {
    planFormLoading.value = false
  }
}

const handleRun = async (row: MrpPlanVO) => {
  if (!canRunPlan(row.status)) {
    message.warning('当前计划状态不允许运行')
    return
  }
  try {
    await message.confirm(getRunPlanConfirmText(row))
    runSubmittingPlanId.value = row.id
    await MrpPlanApi.runPlan(row.id)
    message.success(getRunPlanSuccessText(row))
    await getList()
    startPlanPolling(row.id)
  } catch {}
  finally {
    if (runSubmittingPlanId.value === row.id) {
      runSubmittingPlanId.value = undefined
    }
  }
}

const resultDialogVisible = ref(false)
const resultDialogTitle = ref('MRP 运算结果')
const resultLoading = ref(false)
const resultLoadError = ref('')
const resultTab = ref<'result' | 'shortage'>('result')
const currentResultPlanName = ref('')
const resultRequestId = ref(0)
const resultExporting = ref(false)
const shortageExporting = ref(false)
const resultList = ref<MrpResultVO[]>([])
const shortageList = ref<MrpShortageVO[]>([])
const resultFilterForm = reactive<ResultFilterForm>({
  keyword: '',
  businessType: undefined,
  skipReason: undefined,
  onlySkipped: false,
  onlyActive: false
})
const shortageFilterForm = reactive<ShortageFilterForm>({
  keyword: ''
})

const resetResultFilters = () => {
  resultFilterForm.keyword = ''
  resultFilterForm.businessType = undefined
  resultFilterForm.skipReason = undefined
  resultFilterForm.onlySkipped = false
  resultFilterForm.onlyActive = false
}

const resetShortageFilters = () => {
  shortageFilterForm.keyword = ''
}

const resetResultDialogState = () => {
  resultRequestId.value += 1
  resultLoading.value = false
  resultLoadError.value = ''
  resultExporting.value = false
  shortageExporting.value = false
  resultTab.value = 'result'
  currentResultPlanName.value = ''
  resultList.value = []
  shortageList.value = []
  resetResultFilters()
  resetShortageFilters()
}

const normalizeKeyword = (keyword: string) => keyword.trim().toLowerCase()

const includesKeyword = (keyword: string, values: Array<string | number | undefined>) => {
  if (!keyword) {
    return true
  }
  return values.some((value) => String(value ?? '').toLowerCase().includes(keyword))
}

const resultBusinessTypeOptions = computed(() => {
  return Array.from(new Set(resultList.value.map((item) => item.businessType).filter(Boolean))).map(
    (value) => ({
      value: value as string,
      label: getBusinessTypeLabel(value)
    })
  )
})

const resultSkipReasonOptions = computed(() => {
  return Array.from(new Set(resultList.value.map((item) => item.skipReason).filter(Boolean))).map((value) => ({
    value: value as string,
    label: getSkipReasonLabel(value)
  }))
})

const filteredResultList = computed(() => {
  const keyword = normalizeKeyword(resultFilterForm.keyword)
  return resultList.value.filter((item) => {
    if (resultFilterForm.businessType && item.businessType !== resultFilterForm.businessType) {
      return false
    }
    if (resultFilterForm.skipReason && item.skipReason !== resultFilterForm.skipReason) {
      return false
    }
    if (resultFilterForm.onlySkipped && !item.skipReason) {
      return false
    }
    if (resultFilterForm.onlyActive && item.skipReason) {
      return false
    }
    return includesKeyword(keyword, [item.rootProductName, item.materialName, item.sourceOrderId])
  })
})

const filteredShortageList = computed(() => {
  const keyword = normalizeKeyword(shortageFilterForm.keyword)
  return shortageList.value.filter((item) =>
    includesKeyword(keyword, [item.rootProductName, item.materialName, item.sourceOrderId])
  )
})

const resultReviewSummary = computed(() => {
  const totalCount = filteredResultList.value.length
  const skipReasonCountMap = filteredResultList.value.reduce<Record<string, number>>((acc, item) => {
    if (!item.skipReason) {
      return acc
    }
    acc[item.skipReason] = (acc[item.skipReason] || 0) + 1
    return acc
  }, {})
  const skippedCount = Object.values(skipReasonCountMap).reduce((sum, count) => sum + count, 0)
  return {
    totalCount,
    activeCount: totalCount - skippedCount,
    skippedCount,
    skippedRatio: totalCount > 0 ? `${((skippedCount / totalCount) * 100).toFixed(2)}%` : '0.00%',
    skipReasonBreakdown: Object.entries(skipReasonCountMap)
      .map(([key, count]) => ({
        key,
        label: getSkipReasonLabel(key),
        count
      }))
      .sort((a, b) => b.count - a.count)
  }
})

const hasTraceDetail = (item: MrpResultVO | MrpShortageVO) => {
  return Boolean(
    item.traceNodeId ||
    item.tracePathKey ||
    item.traceLevel !== undefined && item.traceLevel !== null ||
    item.parentMaterialId ||
    item.bomItemId
  )
}

const hasResultTraceDetails = computed(() => resultList.value.some((item) => hasTraceDetail(item)))
const hasShortageTraceDetails = computed(() => shortageList.value.some((item) => hasTraceDetail(item)))

const hasResultFilters = computed(() => {
  return Boolean(
      resultFilterForm.keyword ||
      resultFilterForm.businessType ||
      resultFilterForm.skipReason ||
      resultFilterForm.onlySkipped ||
      resultFilterForm.onlyActive
  )
})

const activeReviewReasonKey = computed(() => resultFilterForm.skipReason || '')

const activeReviewMode = computed<'all' | 'skipped' | 'active'>(() => {
  if (resultFilterForm.onlyActive) {
    return 'active'
  }
  if (resultFilterForm.onlySkipped || resultFilterForm.skipReason) {
    return 'skipped'
  }
  return 'all'
})

const hasReviewDrillDown = computed(() => {
  return Boolean(resultFilterForm.onlySkipped || resultFilterForm.onlyActive || resultFilterForm.skipReason)
})

const hasShortageFilters = computed(() => Boolean(shortageFilterForm.keyword))

const resultActiveFilterTags = computed<FilterTagItem[]>(() => {
  const tags: FilterTagItem[] = []
  if (resultFilterForm.keyword) {
    tags.push({ key: 'keyword', label: `关键字：${resultFilterForm.keyword}` })
  }
  if (resultFilterForm.businessType) {
    tags.push({
      key: 'businessType',
      label: `业务类型：${getBusinessTypeLabel(resultFilterForm.businessType)}`
    })
  }
  if (resultFilterForm.skipReason) {
    tags.push({
      key: 'skipReason',
      label: `跳过原因：${getSkipReasonLabel(resultFilterForm.skipReason)}`
    })
  }
  if (resultFilterForm.onlyActive) {
    tags.push({ key: 'onlyActive', label: '复盘：参与运算' })
  } else if (resultFilterForm.onlySkipped && !resultFilterForm.skipReason) {
    tags.push({ key: 'onlySkipped', label: '复盘：仅看已跳过' })
  }
  return tags
})

const shortageActiveFilterTags = computed<FilterTagItem[]>(() => {
  const tags: FilterTagItem[] = []
  if (shortageFilterForm.keyword) {
    tags.push({ key: 'keyword', label: `关键字：${shortageFilterForm.keyword}` })
  }
  return tags
})

const resultExportSummary = computed(() => {
  return resultActiveFilterTags.value.length > 0
    ? resultActiveFilterTags.value.map((item) => item.label).join('；')
    : '无筛选'
})

const shortageExportSummary = computed(() => {
  return shortageActiveFilterTags.value.length > 0
    ? shortageActiveFilterTags.value.map((item) => item.label).join('；')
    : '无筛选'
})

const canExportResult = computed(() => {
  return !resultLoading.value && !resultExporting.value && filteredResultList.value.length > 0
})

const canExportShortage = computed(() => {
  return !resultLoading.value && !shortageExporting.value && filteredShortageList.value.length > 0
})

const resultEmptyText = computed(() => {
  return resultLoadError.value ? '结果加载失败' : '当前筛选条件下暂无结果'
})

const shortageEmptyText = computed(() => {
  return resultLoadError.value ? '缺料数据加载失败' : '当前筛选条件下暂无缺料数据'
})

const buildCsvCell = (value: string | number | undefined) => {
  const text = String(value ?? '')
  return `"${text.replace(/"/g, '""')}"`
}

const buildCsvContent = (
  headers: string[],
  rows: Array<Array<string | number | undefined>>,
  metaRows: Array<Array<string | number | undefined>> = []
) => {
  const metaContent = metaRows.map((row) => row.map((item) => buildCsvCell(item)).join(','))
  const headRow = headers.map((item) => buildCsvCell(item)).join(',')
  const dataRows = rows.map((row) => row.map((item) => buildCsvCell(item)).join(','))
  const sections = [...metaContent]
  if (metaContent.length > 0) {
    sections.push('')
  }
  sections.push(headRow, ...dataRows)
  return sections.join('\n')
}

const buildExportFileName = (suffix: string) => {
  const safePlanName = (currentResultPlanName.value || 'MRP计划').replace(/[\\/:*?"<>|]/g, '-')
  return `${safePlanName}-${suffix}.csv`
}

const downloadCsv = (
  filename: string,
  headers: string[],
  rows: Array<Array<string | number | undefined>>,
  metaRows: Array<Array<string | number | undefined>> = []
) => {
  const csvContent = buildCsvContent(headers, rows, metaRows)
  downloadByData(csvContent, filename, 'text/csv;charset=utf-8;', '\ufeff')
}

const buildExportMetaRows = (tabLabel: string, summary: string) => {
  return [
    ['计划名称', currentResultPlanName.value || 'MRP计划'],
    ['导出页签', tabLabel],
    ['筛选摘要', summary]
  ]
}

const escapeHtml = (value: string) => {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const escapeRegExp = (value: string) => {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

const renderKeywordHighlight = (value: string | number | undefined, keyword: string) => {
  const text = String(value ?? '')
  const trimmedKeyword = keyword.trim()
  if (!text || !trimmedKeyword) {
    return escapeHtml(text)
  }
  const regexp = new RegExp(`(${escapeRegExp(trimmedKeyword)})`, 'ig')
  return text
    .split(regexp)
    .filter((segment) => segment !== '')
    .map((segment) =>
      segment.toLowerCase() === trimmedKeyword.toLowerCase()
        ? `<mark class="mrp-keyword-highlight">${escapeHtml(segment)}</mark>`
        : escapeHtml(segment)
    )
    .join('')
}

const handleResetResultFilters = () => {
  resetResultFilters()
}

const handleResetShortageFilters = () => {
  resetShortageFilters()
}

const handleClearAllResultFilters = () => {
  resetResultFilters()
}

const handleClearAllShortageFilters = () => {
  resetShortageFilters()
}

const handleRemoveResultFilterTag = (key: string) => {
  if (key === 'keyword') {
    resultFilterForm.keyword = ''
    return
  }
  if (key === 'businessType') {
    resultFilterForm.businessType = undefined
    return
  }
  if (key === 'skipReason') {
    resultFilterForm.skipReason = undefined
    if (!resultFilterForm.onlySkipped) {
      return
    }
    resultFilterForm.onlySkipped = false
    return
  }
  if (key === 'onlyActive') {
    resultFilterForm.onlyActive = false
    return
  }
  if (key === 'onlySkipped') {
    resultFilterForm.onlySkipped = false
  }
}

const handleRemoveShortageFilterTag = (key: string) => {
  if (key === 'keyword') {
    shortageFilterForm.keyword = ''
  }
}

const handleReviewFilterAll = () => {
  resultFilterForm.onlySkipped = false
  resultFilterForm.onlyActive = false
  resultFilterForm.skipReason = undefined
}

const handleReviewFilterSkipped = () => {
  resultFilterForm.onlySkipped = true
  resultFilterForm.onlyActive = false
  resultFilterForm.skipReason = undefined
}

const handleReviewFilterActive = () => {
  resultFilterForm.onlySkipped = false
  resultFilterForm.onlyActive = true
  resultFilterForm.skipReason = undefined
}

const handleReviewFilterByReason = (reason: string) => {
  resultFilterForm.onlySkipped = true
  resultFilterForm.onlyActive = false
  resultFilterForm.skipReason = reason
}

const handleExportResult = async () => {
  if (!canExportResult.value) {
    message.warning('暂无可导出的结果数据')
    return
  }
  resultExporting.value = true
  try {
    downloadCsv(
      buildExportFileName('运算结果'),
      [
        '母项',
        '物料',
        '毛需求',
        '现存',
        '在途',
        '在制',
        '净需求',
        '供应方式',
        '业务类型',
        '跳过原因',
        '建议日期',
        '需求日期',
        '来源销售单'
      ],
      filteredResultList.value.map((item) => [
        item.rootProductName,
        item.materialName,
        formatQty(item.grossDemandQty),
        formatQty(item.availableStockQty),
        formatQty(item.incomingQty),
        formatQty(item.wipQty),
        formatQty(item.netDemandQty),
        getSupplyModeLabel(item),
        getBusinessTypeLabel(item.businessType),
        getSkipReasonLabel(item.skipReason),
        item.suggestDate,
        item.demandDate,
        item.sourceOrderId
      ]),
      buildExportMetaRows('运算结果', resultExportSummary.value)
    )
    message.success('导出成功')
  } catch {
    message.error('导出失败，请重试')
  } finally {
    resultExporting.value = false
  }
}

const handleExportShortage = async () => {
  if (!canExportShortage.value) {
    message.warning('暂无可导出的缺料数据')
    return
  }
  shortageExporting.value = true
  try {
    downloadCsv(
      buildExportFileName('缺料清单'),
      ['母项', '缺料物料', '缺料数量', '替代料推荐', '需求日期', '来源销售单'],
      filteredShortageList.value.map((item) => [
        item.rootProductName,
        item.materialName,
        formatQty(item.shortageQty),
        formatShortageSubstituteSummary(item.substitutes),
        item.requiredDate,
        item.sourceOrderId
      ]),
      buildExportMetaRows('缺料清单', shortageExportSummary.value)
    )
    message.success('导出成功')
  } catch {
    message.error('导出失败，请重试')
  } finally {
    shortageExporting.value = false
  }
}

const openResultDialog = async (row: MrpPlanVO) => {
  const currentRequestId = resultRequestId.value + 1
  resultRequestId.value = currentRequestId
  resultLoadError.value = ''
  resultExporting.value = false
  shortageExporting.value = false
  resetResultFilters()
  resetShortageFilters()
  currentResultPlanName.value = row.planName
  resultDialogTitle.value = `MRP 结果 - ${row.planName}`
  resultTab.value = 'result'
  resultDialogVisible.value = true
  resultLoading.value = true
  resultList.value = []
  shortageList.value = []
  try {
    const [results, shortages] = await Promise.all([
      MrpPlanApi.getResultList(row.id),
      MrpPlanApi.getShortageList(row.id)
    ])
    if (currentRequestId !== resultRequestId.value || !resultDialogVisible.value) {
      return
    }
    resultList.value = results
    shortageList.value = shortages
  } catch (error: any) {
    if (currentRequestId !== resultRequestId.value) {
      return
    }
    resultLoadError.value = error?.message || '结果加载失败，请关闭后重试'
  } finally {
    if (currentRequestId === resultRequestId.value) {
      resultLoading.value = false
    }
  }
}

const openSuggestPage = async (row: MrpPlanVO) => {
  const target = router.resolve({
    path: '/scm/suggest',
    query: { planId: String(row.id) }
  })
  await router.push(target)
}

const openResultExplain = async (row: MrpResultVO) => {
  await resultExplainDialogRef.value?.open(row)
}

onMounted(() => {
  getList()
})

onBeforeUnmount(() => {
  Array.from(planPollingTimers.keys()).forEach((planId) => stopPlanPolling(planId))
})
</script>

<style scoped lang="scss">
.mrp-plan-header {
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.15;
}

.mrp-plan-query-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.mrp-plan-query-form__actions {
  grid-column: 1 / -1;
}

.mrp-plan-query-form__actions :deep(.el-form-item__content) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mrp-plan-table-card {
  overflow: hidden;
  border: 1px solid #dbe7f3;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.mrp-plan-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 18px 12px;
  border-bottom: 1px solid #e7eef6;
  background: linear-gradient(180deg, #f9fbff 0%, #f2f7fc 100%);
}

.mrp-plan-toolbar :deep(.el-button) {
  min-height: 34px;
  padding-inline: 14px;
  border-radius: 10px;
}

.mrp-plan-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.mrp-plan-table :deep(.el-table__header th) {
  height: 46px;
  background: #f7fafc;
  color: #5f7388;
  font-weight: 600;
  border-bottom: 1px solid #e6edf5;
}

.mrp-plan-table :deep(.el-table__row td) {
  padding-top: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid #edf2f7;
  vertical-align: top;
}

.mrp-plan-table :deep(.el-table__body tr:hover > td) {
  background: #f9fbff !important;
}

.mrp-plan-table-card :deep(.pagination-container) {
  margin: 0;
  padding: 14px 18px 16px;
  border-top: 1px solid #e7eef6;
  background: #fff;
}

.mrp-plan-identity {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mrp-plan-identity__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
}

.mrp-plan-identity__meta {
  display: inline-flex;
  align-self: flex-start;
  padding: 3px 10px;
  border: 1px solid #e6edf5;
  border-radius: 8px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.mrp-plan-status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 82px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}

.mrp-plan-status-chip.is-pending {
  color: #64748b;
  background: #f8fafc;
  border-color: #dbe5ef;
}

.mrp-plan-status-chip.is-running {
  color: #2563eb;
  background: #eef4ff;
  border-color: #c7dafc;
}

.mrp-plan-status-chip.is-finished {
  color: #059669;
  background: #ecfdf3;
  border-color: #b7f0d0;
}

.mrp-plan-status-chip.is-closed {
  color: #475569;
  background: #f8fafc;
  border-color: #dbe5ef;
}

.mrp-plan-status-chip.is-failed {
  color: #e11d48;
  background: #fff1f2;
  border-color: #fecdd3;
}

.mrp-plan-time-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mrp-plan-time-stack__item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.mrp-plan-time-stack__label {
  min-width: 32px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
}

.mrp-plan-time-stack__value {
  color: #334155;
  font-size: 13px;
  line-height: 1.5;
  font-variant-numeric: tabular-nums;
}

.mrp-plan-remark {
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-line;
  word-break: break-word;
}

.mrp-trace-path {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mrp-trace-path__line {
  color: #475569;
  font-size: 12px;
  line-height: 1.45;
  word-break: break-all;
}

.mrp-trace-path__line--mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #0f172a;
}

.mrp-plan-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px 12px;
  min-height: 24px;
}

.mrp-plan-actions__placeholder {
  color: #94a3b8;
}

.mrp-plan-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 36px 0;
  color: #94a3b8;
}

.mrp-plan-empty__icon {
  font-size: 28px;
}

.mrp-plan-empty__title {
  font-size: 13px;
}

.mrp-summary-card {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.mrp-summary-card:hover:not(:disabled) {
  border-color: var(--el-color-primary-light-5);
}

.mrp-summary-card:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.mrp-summary-card.is-active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: inset 0 0 0 1px var(--el-color-primary-light-5);
}

.mrp-summary-card__label {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 20px;
}

.mrp-summary-card__value {
  color: var(--el-text-color-primary);
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}

:deep(.mrp-keyword-highlight) {
  padding: 0 2px;
  border-radius: 2px;
  color: var(--el-color-warning-dark-2);
  background: var(--el-color-warning-light-8);
}

@media (max-width: 1280px) {
  .mrp-plan-query-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .mrp-plan-header {
    font-size: 28px;
  }

  .mrp-plan-query-form {
    grid-template-columns: 1fr;
  }

  .mrp-plan-query-form__actions :deep(.el-form-item__content) {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .mrp-plan-toolbar {
    padding-inline: 14px;
  }

  .mrp-plan-toolbar :deep(.el-button) {
    width: 100%;
  }
}
</style>
