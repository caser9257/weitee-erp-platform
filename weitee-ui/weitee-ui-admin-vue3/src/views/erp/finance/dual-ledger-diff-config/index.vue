<template>
  <div class="finance-shell finance-shell__stack dual-ledger-diff-config-page">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">双账口径配置</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip finance-shell__metric-chip--primary">
              褰撳墠鍒楄〃 {{ total }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--success">
              鍚敤 {{ enabledCount }}
            </span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--warning">
              鍋滅敤 {{ disabledCount }}
            </span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button plain :loading="refreshingList" :disabled="loadingList || refreshingList" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            鍒锋柊
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="涓氬姟绫诲瀷" prop="bizType">
            <el-select v-model="queryParams.bizType" class="!w-full" clearable placeholder="璇烽€夋嫨涓氬姟绫诲瀷">
              <el-option
                v-for="item in ERP_BIZ_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="差异项" prop="diffItemType">
            <el-select v-model="queryParams.diffItemType" class="!w-full" clearable placeholder="请选择差异项">
              <el-option
                v-for="item in DUAL_LEDGER_DIFF_ITEM_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="外部账来源" prop="externalSourceType">
            <el-select
              v-model="queryParams.externalSourceType"
              class="!w-full"
              clearable
               placeholder="请选择外部账来源"
            >
              <el-option
                v-for="item in DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="内部账来源" prop="internalSourceType">
            <el-select
              v-model="queryParams.internalSourceType"
              class="!w-full"
              clearable
               placeholder="请选择内部账来源"
            >
              <el-option
                v-for="item in DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" class="!w-full" clearable placeholder="请选择状态">
              <el-option
                v-for="item in COMMON_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="澶囨敞" prop="remark">
            <el-input
              v-model="queryParams.remark"
              class="!w-full"
              clearable
               placeholder="请输入备注"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" :disabled="loadingList || refreshingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            鏌ヨ
          </el-button>
          <el-button :disabled="loadingList || refreshingList" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            閲嶇疆
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">鍙ｅ緞閰嶇疆鍒楄〃</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:finance-dual-ledger-diff-config:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            鏂板閰嶇疆
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !list.length" class="dual-ledger-diff-config-page__state">
        <el-result icon="error" title="鍙ｅ緞閰嶇疆鍔犺浇澶辫触" :sub-title="listErrorMessage">
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
          >
            <el-table-column label="涓氬姟绫诲瀷" min-width="120">
              <template #default="{ row }">
                <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">
                  {{ row.bizTypeName || getBizTypeLabel(row.bizType) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="差异项" min-width="120">
              <template #default="{ row }">
                {{ row.diffItemTypeName || getDiffItemTypeLabel(row.diffItemType) || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="外部账来源" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">
                    {{ row.externalSourceTypeName || getSourceTypeLabel(row.externalSourceType) || '-' }}
                  </span>
                  <span class="finance-shell__muted-text">
                    {{ row.externalSourceValueName || getDiffItemTypeLabel(row.externalSourceValue) || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="内部账来源" min-width="220">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">
                    {{ row.internalSourceTypeName || getSourceTypeLabel(row.internalSourceType) || '-' }}
                  </span>
                  <span class="finance-shell__muted-text">
                    {{ row.internalSourceValueName || getDiffItemTypeLabel(row.internalSourceValue) || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="璁＄畻绫诲瀷" min-width="120">
              <template #default="{ row }">
                {{ row.calculationTypeName || getCalculationTypeLabel(row.calculationType) || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="姣斾緥绯绘暟" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatRatioValue(row.ratio) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="鍥哄畾宸" min-width="120" align="right">
              <template #default="{ row }">
                <span class="finance-shell__amount">{{ formatAmount(row.fixedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="100" align="center">
              <template #default="{ row }">
                <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="200">
              <template #default="{ row }">
                <span class="finance-shell__muted-text" :title="row.remark || '-'">
                  {{ row.remark || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" align="center" width="180">
              <template #default="{ row }">
                <div class="dual-ledger-diff-config-page__row-actions">
                  <el-button
                    link
                    :loading="detailLoadingId === row.id"
                    :disabled="detailLoadingId === row.id || deleteLoadingId === row.id"
                    @click="openDetailDrawer(row)"
                    v-hasPermi="['erp:finance-dual-ledger-diff-config:query']"
                  >
                    鏌ョ湅
                  </el-button>
                  <el-button
                    link
                    type="primary"
                    :disabled="submitLoading || deleteLoadingId === row.id"
                    @click="openForm('update', row.id)"
                    v-hasPermi="['erp:finance-dual-ledger-diff-config:update']"
                  >
                    缂栬緫
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    :loading="deleteLoadingId === row.id"
                    :disabled="deleteLoadingId === row.id"
                    @click="handleDelete(row.id)"
                    v-hasPermi="['erp:finance-dual-ledger-diff-config:delete']"
                  >
                    鍒犻櫎
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else class="dual-ledger-diff-config-page__state" description="鏆傛棤鍙ｅ緞閰嶇疆" />
        <Pagination
          v-if="total > 0"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <DualLedgerDiffConfigForm ref="formRef" @success="handleFormSuccess" />

    <el-drawer
      v-model="detailDrawerVisible"
      size="520px"
      destroy-on-close
      :with-header="false"
      :modal-class="'finance-shell__drawer-mask'"
      @closed="clearDetailDrawer"
    >
      <div class="dual-ledger-diff-config-page__drawer">
        <div v-if="detailData" class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">
              {{ detailData.diffItemTypeName || getDiffItemTypeLabel(detailData.diffItemType) || '鍙ｅ緞閰嶇疆璇︽儏' }}
            </div>
            <div class="finance-shell__context-subtitle">
              {{ detailData.bizTypeName || getBizTypeLabel(detailData.bizType) || '-' }}
            </div>
          </div>
          <div class="finance-shell__context-meta">
            <div class="finance-shell__context-meta-item">
              <span>状态</span>
              <span>{{ getStatusLabel(detailData.status) }}</span>
            </div>
            <div class="finance-shell__context-meta-item">
              <span>璁＄畻绫诲瀷</span>
              <span>{{ detailData.calculationTypeName || getCalculationTypeLabel(detailData.calculationType) || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="dual-ledger-diff-config-page__drawer-body" v-loading="loadingDetail">
          <el-result v-if="detailErrorMessage" icon="error" title="瑙勫垯璇︽儏鍔犺浇澶辫触" :sub-title="detailErrorMessage">
            <template #extra>
              <el-button type="primary" @click="retryLoadDetail">閲嶈瘯</el-button>
            </template>
          </el-result>
          <template v-else-if="detailData">
            <div class="finance-shell__section">
              <div class="finance-shell__section-title">瑙勫垯璇︽儏</div>
              <div class="dual-ledger-diff-config-page__detail-list">
                <div class="dual-ledger-diff-config-page__detail-item">
                  <span>外部账来源</span>
                  <strong>
                    {{ detailData.externalSourceTypeName || getSourceTypeLabel(detailData.externalSourceType) || '-' }}
                    / {{ detailData.externalSourceValueName || getDiffItemTypeLabel(detailData.externalSourceValue) || '-' }}
                  </strong>
                </div>
                <div class="dual-ledger-diff-config-page__detail-item">
                  <span>内部账来源</span>
                  <strong>
                    {{ detailData.internalSourceTypeName || getSourceTypeLabel(detailData.internalSourceType) || '-' }}
                    / {{ detailData.internalSourceValueName || getDiffItemTypeLabel(detailData.internalSourceValue) || '-' }}
                  </strong>
                </div>
                <div class="dual-ledger-diff-config-page__detail-item">
                  <span>姣斾緥绯绘暟</span>
                  <strong>{{ formatRatioValue(detailData.ratio) }}</strong>
                </div>
                <div class="dual-ledger-diff-config-page__detail-item">
                  <span>鍥哄畾宸</span>
                  <strong>{{ formatAmount(detailData.fixedAmount) }}</strong>
                </div>
                <div class="dual-ledger-diff-config-page__detail-item dual-ledger-diff-config-page__detail-item--full">
                  <span>澶囨敞</span>
                  <strong>{{ detailData.remark || '-' }}</strong>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="dual-ledger-diff-config-page__drawer-footer">
          <el-button @click="detailDrawerVisible = false">鍏抽棴</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import { DICT_TYPE } from '@/utils/dict'
import {
  DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS,
  DUAL_LEDGER_DIFF_ITEM_OPTIONS,
  DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS,
  ERP_BIZ_TYPE_OPTIONS,
  FinanceDualLedgerDiffConfigApi,
  type ErpFinanceDualLedgerDiffConfigPageReqVO,
  type ErpFinanceDualLedgerDiffConfigVO
} from '@/api/erp/finance/dual-ledger-diff-config'
import { COMMON_STATUS_OPTIONS, formatAmount } from '@/views/erp/finance/shared/accounting'
import DualLedgerDiffConfigForm from './DualLedgerDiffConfigForm.vue'

defineOptions({ name: 'ErpFinanceDualLedgerDiffConfig' })

const message = useMessage()

const queryFormRef = ref<FormInstance>()
const formRef = ref<InstanceType<typeof DualLedgerDiffConfigForm>>()

const loadingList = ref(false)
const refreshingList = ref(false)
const submitLoading = ref(false)
const loadingDetail = ref(false)
const deleteLoadingId = ref<number>()
const detailLoadingId = ref<number>()
const detailDrawerVisible = ref(false)
const currentDetailId = ref<number>()
const listErrorMessage = ref('')
const detailErrorMessage = ref('')

const list = ref<ErpFinanceDualLedgerDiffConfigVO[]>([])
const total = ref(0)
const detailData = ref<ErpFinanceDualLedgerDiffConfigVO>()

const queryParams = reactive<ErpFinanceDualLedgerDiffConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  bizType: undefined,
  diffItemType: undefined,
  externalSourceType: undefined,
  internalSourceType: undefined,
  status: undefined,
  remark: undefined
})

const enabledCount = computed(() => list.value.filter((item) => Number(item.status) === 0).length)
const disabledCount = computed(() => list.value.filter((item) => Number(item.status) !== 0).length)

const getBizTypeLabel = (value?: number) =>
  ERP_BIZ_TYPE_OPTIONS.find((item) => item.value === value)?.label
const getDiffItemTypeLabel = (value?: number) =>
  DUAL_LEDGER_DIFF_ITEM_OPTIONS.find((item) => item.value === value)?.label
const getSourceTypeLabel = (value?: number) =>
  DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS.find((item) => item.value === value)?.label
const getCalculationTypeLabel = (value?: number) =>
  DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS.find((item) => item.value === value)?.label
const getStatusLabel = (value?: number) =>
  COMMON_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const formatRatioValue = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 4, maximumFractionDigits: 4 })

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'

const getList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceDualLedgerDiffConfigApi.getDualLedgerDiffConfigPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error: any) {
    if (!list.value.length) {
      listErrorMessage.value = error?.message || '请检查网络或稍后重试。'
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
  await handleQuery()
}

const handleRefresh = async () => {
  refreshingList.value = true
  try {
    await getList()
  } finally {
    refreshingList.value = false
  }
}

const openForm = async (type: 'create' | 'update', id?: number) => {
  submitLoading.value = true
  try {
    await formRef.value?.open(type, id)
  } finally {
    submitLoading.value = false
  }
}

const handleFormSuccess = async () => {
  await getList()
  if (detailDrawerVisible.value && currentDetailId.value) {
    await loadDetail(currentDetailId.value)
  }
}

const loadDetail = async (id: number) => {
  loadingDetail.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await FinanceDualLedgerDiffConfigApi.getDualLedgerDiffConfig(id)
  } catch (error: any) {
    detailData.value = undefined
    detailErrorMessage.value = error?.message || '请检查网络或稍后重试。'
  } finally {
    loadingDetail.value = false
  }
}

const openDetailDrawer = async (row: ErpFinanceDualLedgerDiffConfigVO) => {
  const id = Number(row.id)
  if (!id || detailLoadingId.value) {
    return
  }
  detailDrawerVisible.value = true
  currentDetailId.value = id
  detailLoadingId.value = id
  detailData.value = undefined
  try {
    await loadDetail(id)
  } finally {
    detailLoadingId.value = undefined
  }
}

const retryLoadDetail = async () => {
  if (!currentDetailId.value) {
    return
  }
  await loadDetail(currentDetailId.value)
}

const clearDetailDrawer = () => {
  currentDetailId.value = undefined
  detailData.value = undefined
  detailErrorMessage.value = ''
}

const handleDelete = async (id?: number) => {
  const targetId = Number(id)
  if (!targetId || deleteLoadingId.value) {
    return
  }
  try {
    await message.confirm('确认删除该口径配置吗？')
  } catch (error) {
    if (isActionCanceled(error)) {
      return
    }
    throw error
  }
  deleteLoadingId.value = targetId
  try {
    await FinanceDualLedgerDiffConfigApi.deleteDualLedgerDiffConfig(targetId)
    message.success('删除成功')
    if (list.value.length === 1 && Number(queryParams.pageNo) > 1) {
      queryParams.pageNo = Number(queryParams.pageNo) - 1
    }
    await getList()
    if (currentDetailId.value === targetId) {
      detailDrawerVisible.value = false
      clearDetailDrawer()
    }
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(async () => {
  await getList()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.dual-ledger-diff-config-page__state {
  min-height: 260px;
}

.dual-ledger-diff-config-page__row-actions {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0 8px;
}

.dual-ledger-diff-config-page__drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8fafc;
}

.dual-ledger-diff-config-page__drawer-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
}

.dual-ledger-diff-config-page__detail-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dual-ledger-diff-config-page__detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
}

.dual-ledger-diff-config-page__detail-item span {
  color: #64748b;
  font-size: 12px;
}

.dual-ledger-diff-config-page__detail-item strong {
  color: #0f172a;
  font-size: 14px;
  line-height: 1.6;
}

.dual-ledger-diff-config-page__detail-item--full {
  grid-column: 1 / -1;
}

.dual-ledger-diff-config-page__drawer-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
}

@media (max-width: 768px) {
  .dual-ledger-diff-config-page__detail-list {
    grid-template-columns: 1fr;
  }

  .dual-ledger-diff-config-page__drawer-body,
  .dual-ledger-diff-config-page__drawer-footer {
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>
