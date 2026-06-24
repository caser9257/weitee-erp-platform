<template>
<ContentWrap class="netting-policy-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="netting-policy-page__header">
      <div>
        <div class="netting-policy-page__title">净需求策�?/div>
        <div class="netting-policy-page__count">�?{{ total }} 条记�?/div>
      </div>
      <div class="netting-policy-page__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:mrp-netting-policy:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增策略
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="netting-policy-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="netting-policy-page__section-title">筛选条�?/div>

    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="netting-policy-query"
    >
      <div class="netting-policy-query__grid">
        <el-form-item label="策略编码" prop="code">
          <el-input
            v-model="queryParams.code"
            clearable
            placeholder="请输入策略编�?
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="策略名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入策略名�?
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="queryParams.businessType" clearable placeholder="请选择业务类型">
            <el-option
              v-for="item in BUSINESS_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状�? prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状�?>
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </div>

      <div class="netting-policy-query__footer">
        <div></div>
        <div class="netting-policy-query__actions">
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="netting-policy-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="netting-policy-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="netting-policy-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="netting-policy-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="netting-policy-page__overview-card__content">
          <div class="netting-policy-page__overview-card__value">{{ card.value }}</div>
          <div class="netting-policy-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="netting-policy-table__scroll">
      <el-table
        v-loading="listLoading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        class="netting-policy-table"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="netting-policy-empty netting-policy-empty--error">
            <div class="netting-policy-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="netting-policy-empty__title">净需求策略加载失�?/div>
            <el-button type="primary" plain :disabled="!canRetryList" @click="handleRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="netting-policy-empty">
            <div class="netting-policy-empty__icon">
              <Icon icon="ep:document" />
            </div>
            <div class="netting-policy-empty__title">暂无净需求策�?/div>
          </div>
        </template>

        <el-table-column label="策略信息" min-width="260">
          <template #default="{ row }">
            <div class="netting-policy-info">
              <div class="netting-policy-info__code">{{ row.code || '-' }}</div>
              <div class="netting-policy-info__name">{{ row.name || '-' }}</div>
              <div class="netting-policy-info__meta">
                <span>版本 {{ row.version ?? 0 }}</span>
                <span>{{ row.remark || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="适用业务类型" min-width="240">
          <template #default="{ row }">
            <div class="netting-policy-tags">
              <span v-for="item in row.businessTypes" :key="item" class="netting-policy-pill netting-policy-pill--neutral">
                {{ getBusinessTypeLabel(item) }}
              </span>
              <span v-if="!row.businessTypes?.length" class="netting-policy-tags__empty">未绑�?/span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="规则配置" min-width="220">
          <template #default="{ row }">
            <div class="netting-policy-tags">
              <span
                class="netting-policy-pill"
                :class="row.defaultFlag ? 'netting-policy-pill--primary' : 'netting-policy-pill--neutral'"
              >
                {{ row.defaultFlag ? '默认策略' : '常规策略' }}
              </span>
              <span
                class="netting-policy-pill"
                :class="row.enableFlag ? 'netting-policy-pill--success' : 'netting-policy-pill--warning'"
              >
                {{ row.enableFlag ? '已启�? : '已停�? }}
              </span>
              <span class="netting-policy-pill netting-policy-pill--info">
                组件 {{ row.lines?.length || 0 }} �?
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="更新时间" min-width="170">
          <template #default="{ row }">
            <span class="netting-policy-time">{{ formatUpdateTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="170" align="center">
          <template #default="{ row }">
            <div class="netting-policy-actions">
              <el-button
                link
                type="primary"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:mrp-netting-policy:update']"
                :disabled="deletingId === row.id"
              >
                编辑
              </el-button>
              <el-button
                v-if="!row.defaultFlag"
                link
                type="danger"
                @click="handleDelete(row)"
                v-hasPermi="['erp:mrp-netting-policy:delete']"
                :loading="deletingId === row.id"
                :disabled="deletingId !== undefined && deletingId !== row.id"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="netting-policy-page__footer">
      <div class="netting-policy-page__record-count">�?{{ total }} 条记�?/div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <NettingPolicyForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import {
  MrpNettingPolicyApi,
  type MrpNettingPolicyPageReqVO,
  type MrpNettingPolicyVO
} from '@/api/erp/mrp/netting-policy'
import NettingPolicyForm from './NettingPolicyForm.vue'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpMrpNettingPolicy' })

const BUSINESS_TYPE_OPTIONS = [
  { label: '自研', value: 'SELF_RESEARCH' },
  { label: '客供', value: 'CUSTOMER_SUPPLIED' },
  { label: '来料加工', value: 'TOLL_MANUFACTURING' }
]

const message = useMessage()

const queryFormRef = ref()
const formRef = ref()
const listLoading = ref(true)
const deletingId = ref<number>()
const listLoadFailed = ref(false)
const list = ref<MrpNettingPolicyVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  code: undefined as string | undefined,
  name: undefined as string | undefined,
  businessType: undefined as string | undefined,
  status: undefined as number | undefined
})

// 统计卡片
const enabledCount = computed(() => list.value.filter((item) => item.enableFlag).length)
const defaultCount = computed(() => list.value.filter((item) => item.defaultFlag).length)
const summaryCards = computed(() => [
  { label: '总策略数', value: formatCount(total.value), icon: 'ep:document', colorClass: 'stat-icon--blue' },
  { label: '已启�?, value: formatCount(enabledCount.value), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
  { label: '默认策略', value: formatCount(defaultCount.value), icon: 'ep:key', colorClass: 'stat-icon--amber' },
  { label: '当前�?, value: formatCount(list.value.length), icon: 'ep:list', colorClass: 'stat-icon--slate' }
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
    queryParams.code ||
      queryParams.name ||
      queryParams.businessType ||
      queryParams.status !== undefined
  )
)

const canQuery = computed(() => !listLoading.value)
const canReset = computed(() => !listLoading.value && hasFilters.value)
const canRetryList = computed(() => listLoadFailed.value && !listLoading.value)

const formatUpdateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const normalizedValue =
    typeof value === 'string' && /^\d{10,13}$/.test(value.trim())
      ? Number(value.trim().length === 10 ? `${value.trim()}000` : value.trim())
      : value
  return formatDate(normalizedValue) || String(value)
}

const getBusinessTypeLabel = (businessType?: string) => {
  return BUSINESS_TYPE_OPTIONS.find((item) => item.value === businessType)?.label || businessType || '-'
}

const getList = async () => {
  listLoading.value = true
  listLoadFailed.value = false
  try {
    const params: MrpNettingPolicyPageReqVO = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      code: queryParams.code,
      name: queryParams.name,
      businessType: queryParams.businessType,
      enableFlag:
        queryParams.status === undefined
          ? undefined
          : queryParams.status === CommonStatusEnum.ENABLE
    }
    const data = await MrpNettingPolicyApi.getPolicyPage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error: any) {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
    message.error(error?.message || '净需求策略列表加载失败，请重�?)
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  if (!canQuery.value) {
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

const openForm = async (type: 'create' | 'update', id?: number) => {
  await formRef.value?.open(type, id)
}

const handleDelete = async (row: MrpNettingPolicyVO) => {
  if (!row.id || row.defaultFlag || deletingId.value !== undefined) {
    return
  }
  deletingId.value = row.id
  try {
    await message.confirm(`确认删除策略�?{row.name}】吗？`)
    await MrpNettingPolicyApi.deletePolicy(row.id)
    message.success('删除成功')
    await getList()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      message.error(error?.message || '删除失败，请重试')
    }
  } finally {
    deletingId.value = undefined
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.netting-policy-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.netting-policy-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.netting-policy-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.netting-policy-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 统计卡片 */
.netting-policy-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.netting-policy-page__overview-card {
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

.netting-policy-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.netting-policy-page__overview-card__icon {
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

.netting-policy-page__overview-card:hover .netting-policy-page__overview-card__icon {
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

.netting-policy-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.netting-policy-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.netting-policy-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.netting-policy-page__section-title {
  color: #0f172a;
  font-size: 18px;
  line-height: 28px;
  font-weight: 700;
}

.netting-policy-page__section-head,
.netting-policy-page__footer,
.netting-policy-toolbar,
.netting-policy-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.netting-policy-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.netting-policy-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.netting-policy-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.netting-policy-query__actions,
.netting-policy-toolbar__actions,
.netting-policy-actions,
.netting-policy-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.netting-policy-toolbar {
  margin: 16px 0 14px;
}

.netting-policy-toolbar__actions {
  gap: 12px;
}

.netting-policy-table__scroll {
  overflow-x: auto;
}

.netting-policy-table {
  min-width: 1180px;
}

.netting-policy-table :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.netting-policy-table :deep(.el-table td.el-table__cell),
.netting-policy-table :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.netting-policy-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 3;
}

.netting-policy-info__code,
.netting-policy-info__name {
  color: #0f172a;
  font-weight: 700;
}

.netting-policy-info__name {
  margin-top: 4px;
  font-size: 15px;
  line-height: 22px;
}

.netting-policy-info__code,
.netting-policy-info__meta,
.netting-policy-time,
.netting-policy-tags__empty {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
}

.netting-policy-info__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 6px;
}

.netting-policy-pill {
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

.netting-policy-pill--primary {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
}

.netting-policy-pill--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.netting-policy-pill--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.netting-policy-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.netting-policy-pill--info {
  border-color: #cfe8ff;
  background: #eff6ff;
  color: #0284c7;
}

.netting-policy-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.netting-policy-empty__icon {
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

.netting-policy-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.netting-policy-empty--error .netting-policy-empty__icon {
  background: rgba(244, 63, 94, 0.08);
  color: #e11d48;
}

@media (max-width: 1279px) {
  .netting-policy-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .netting-policy-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .netting-policy-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .netting-policy-page__actions {
    width: 100%;
  }

  .netting-policy-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .netting-policy-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .netting-policy-page__section-head,
  .netting-policy-page__footer,
  .netting-policy-query__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .netting-policy-query__actions {
    width: 100%;
  }
}
</style>
