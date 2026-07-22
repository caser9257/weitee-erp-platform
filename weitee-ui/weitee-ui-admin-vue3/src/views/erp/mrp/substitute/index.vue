<template>

  <ContentWrap class="substitute-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="substitute-page__header">
      <div>
        <div class="substitute-page__title">替代料台账</div>
        <div class="substitute-page__count">共 {{ total }} 条记录</div>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="substitute-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="substitute-page__section-title">筛选条件</div>

    <el-alert
      v-if="optionLoadFailed"
      :title="optionErrorMessage"
      type="warning"
      show-icon
      :closable="false"
      class="mb-12px"
    >
      <template #default>
        <el-button link type="primary" :disabled="optionLoading" @click="retrySupportOptions">
          重新加载筛选项
        </el-button>
      </template>
    </el-alert>

    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="substitute-query">
      <div class="substitute-query__grid">
        <el-form-item label="BOM编号" prop="bomId">
          <el-input-number
            v-model="queryParams.bomId"
            :min="1"
            :precision="0"
            placeholder="请输入BOM编号"
            class="substitute-query__number"
          />
        </el-form-item>

        <el-form-item label="BOM物料" prop="materialId">
          <el-select
            v-model="queryParams.materialId"
            clearable
            filterable
            placeholder="请选择BOM物料"
            :loading="optionLoading"
          >
            <el-option v-for="item in productOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="替代料" prop="substituteMaterialId">
          <el-select
            v-model="queryParams.substituteMaterialId"
            clearable
            filterable
            placeholder="请选择替代料"
            :loading="optionLoading"
          >
            <el-option v-for="item in productOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="自动推荐" prop="enableAutoRecommend">
          <el-select v-model="queryParams.enableAutoRecommend" clearable placeholder="请选择">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
      </div>

      <div class="substitute-query__footer">
        <div></div>
        <div class="substitute-query__actions">
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" :loading="loadingList" :disabled="!canSearch" @click="handleQuery" v-hasPermi="['erp:mrp:substitute:query']">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="substitute-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="substitute-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="substitute-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="substitute-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="substitute-page__overview-card__content">
          <div class="substitute-page__overview-card__value">{{ card.value }}</div>
          <div class="substitute-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="substitute-table__scroll">
      <el-table
        v-loading="loadingList"
        :data="list"
        stripe
        :show-overflow-tooltip="true"
        class="substitute-table"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="substitute-empty substitute-empty--error">
            <div class="substitute-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="substitute-empty__title">替代料列表加载失败</div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="substitute-empty">
            <div class="substitute-empty__icon">
              <Icon icon="ep:box" />
            </div>
            <div class="substitute-empty__title">暂无替代料记录</div>
          </div>
        </template>

        <el-table-column label="替代信息" min-width="260">
          <template #default="{ row }">
            <div class="substitute-info">
              <div class="substitute-info__code">#{{ row.id }}</div>
              <div class="substitute-info__name">{{ row.bomCode || '-' }}</div>
              <div class="substitute-info__meta">
                <span>{{ row.productName || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="替代关系" min-width="280">
          <template #default="{ row }">
            <div class="substitute-relation">
              <div class="substitute-relation__row">
                <span class="substitute-relation__label">BOM物料</span>
                <span class="substitute-relation__value">{{ row.materialName || '-' }}</span>
              </div>
              <div class="substitute-relation__row">
                <span class="substitute-relation__label">替代料</span>
                <span class="substitute-relation__value substitute-relation__value--strong">
                  {{ row.substituteMaterialName || '-' }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="规则参数" min-width="220">
          <template #default="{ row }">
            <div class="substitute-tags">
              <span class="substitute-pill substitute-pill--primary">
                优先级 {{ row.priority ?? '-' }}
              </span>
              <span class="substitute-pill substitute-pill--success">
                比例 {{ formatQty(row.replaceRatio) }}
              </span>
              <span
                class="substitute-pill"
                :class="row.enableAutoRecommend ? 'substitute-pill--success' : 'substitute-pill--neutral'"
              >
                {{ row.enableAutoRecommend ? '自动推荐' : '手动维护' }}
              </span>
              <span class="substitute-pill substitute-pill--info">排序 {{ row.sort ?? '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            <span class="substitute-time">{{ formatDateValue(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="180">
          <template #default="{ row }">
            <span class="substitute-remark" :title="row.remark || '-'">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="substitute-page__footer">
      <div class="substitute-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import {
  BomItemSubstituteApi,
  type BomItemSubstitutePageReqVO,
  type BomItemSubstituteVO
} from '@/api/erp/mrp/substitute'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpMrpSubstitutePage' })

const message = useMessage()
const queryFormRef = ref()
const loadingList = ref(true)
const listLoadFailed = ref(false)
const list = ref<BomItemSubstituteVO[]>([])
const total = ref(0)
const productOptions = ref<ProductVO[]>([])
const optionLoading = ref(false)
const optionLoadFailed = ref(false)
const optionErrorMessage = ref('筛选项加载失败，请重试')

const queryParams = reactive<BomItemSubstitutePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  bomId: undefined,
  materialId: undefined,
  substituteMaterialId: undefined,
  enableAutoRecommend: undefined
})

// 统计卡片
const autoRecommendCount = computed(() => list.value.filter((item) => item.enableAutoRecommend).length)
const summaryCards = computed(() => [
  { label: '总记录数', value: formatCount(total.value), icon: 'ep:document', colorClass: 'stat-icon--blue' },
  { label: '自动推荐', value: formatCount(autoRecommendCount.value), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
  { label: '手动维护', value: formatCount(list.value.length - autoRecommendCount.value), icon: 'ep:edit', colorClass: 'stat-icon--amber' },
  { label: '当前页', value: formatCount(list.value.length), icon: 'ep:list', colorClass: 'stat-icon--slate' }
])

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const hasFilters = computed(() =>
  Boolean(
    queryParams.bomId ||
      queryParams.materialId ||
      queryParams.substituteMaterialId ||
      queryParams.enableAutoRecommend !== undefined
  )
)

const canSearch = computed(() => !loadingList.value && !optionLoading.value)
const canReset = computed(() => !loadingList.value && hasFilters.value)
const canRetryList = computed(() => listLoadFailed.value && !loadingList.value)

const formatQty = (value?: number) => {
  const numberValue = Number(value ?? 0)
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 6
  })
}

const formatDateValue = (value?: string) => (value ? formatDate(value) : '-')

const loadSupportOptions = async () => {
  optionLoading.value = true
  optionLoadFailed.value = false
  optionErrorMessage.value = '筛选项加载失败，请重试'
  try {
    const products = await ProductApi.getProductSimpleList()
    productOptions.value = products || []
  } catch (error: any) {
    productOptions.value = []
    optionLoadFailed.value = true
    optionErrorMessage.value = error?.message || '筛选项加载失败，请重试'
  } finally {
    optionLoading.value = false
  }
}

const getList = async () => {
  loadingList.value = true
  listLoadFailed.value = false
  try {
    const data = await BomItemSubstituteApi.getSubstitutePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error: any) {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
    message.error(error?.message || '替代料列表加载失败')
  } finally {
    loadingList.value = false
  }
}

const handleQuery = async () => {
  if (!canSearch.value) {
    return
  }
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canReset.value) {
    return
  }
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const handleRetryList = async () => {
  await getList()
}

const retrySupportOptions = async () => {
  await loadSupportOptions()
}

onMounted(async () => {
  await Promise.allSettled([loadSupportOptions(), getList()])
})
</script>

<style scoped lang="scss">
.substitute-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.substitute-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.substitute-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

/* 统计卡片 */
.substitute-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.substitute-page__overview-card {
  display: flex;
  min-height: 96px;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.substitute-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.substitute-page__overview-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.substitute-page__overview-card:hover .substitute-page__overview-card__icon {
  transform: scale(1.08);
}

.stat-icon--blue {
  background: #eff6ff;
  color: #2563eb;
}

.stat-icon--green {
  background: #ecfdf5;
  color: #059669;
}

.stat-icon--amber {
  background: #fffbeb;
  color: #d97706;
}

.stat-icon--slate {
  background: #f1f5f9;
  color: #64748b;
}

.substitute-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.substitute-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.substitute-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.substitute-page__section-title {
  color: #0f172a;
  font-size: 18px;
  line-height: 28px;
  font-weight: 700;
}

.substitute-page__section-head,
.substitute-page__footer,
.substitute-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.substitute-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.substitute-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.substitute-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.substitute-query__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.substitute-query__number {
  width: 100%;
}

.substitute-table__scroll {
  overflow-x: auto;
}

.substitute-table {
  min-width: 1180px;
}

.substitute-table :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.substitute-table :deep(.el-table td.el-table__cell),
.substitute-table :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.substitute-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.substitute-info__code,
.substitute-info__name,
.substitute-relation__value,
.substitute-remark {
  color: #0f172a;
  font-weight: 600;
}

.substitute-info__name {
  margin-top: 4px;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.substitute-info__meta,
.substitute-time,
.substitute-relation__label {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.substitute-info__meta {
  margin-top: 6px;
}

.substitute-relation {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.substitute-relation__row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.substitute-relation__label {
  min-width: 56px;
}

.substitute-relation__value {
  flex: 1;
  min-width: 0;
}

.substitute-relation__value--strong {
  color: #2563eb;
}

.substitute-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.substitute-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.substitute-pill--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.substitute-pill--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.substitute-pill--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.substitute-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.substitute-pill--info {
  border-color: #cfe8ff;
  background: #eff6ff;
  color: #0284c7;
}

.substitute-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.substitute-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.substitute-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.substitute-empty--error .substitute-empty__icon {
  background: rgba(244, 63, 94, 0.08);
  color: #e11d48;
}

@media (max-width: 1279px) {
  .substitute-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .substitute-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .substitute-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .substitute-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .substitute-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .substitute-page__section-head,
  .substitute-page__footer,
  .substitute-query__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .substitute-query__actions {
    width: 100%;
  }

  .substitute-query__actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
