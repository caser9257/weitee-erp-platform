<template>
  <ContentWrap class="route-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="route-page__header">
      <div>
        <div class="route-page__title">工艺路线</div>
        <div class="route-page__count">共 {{ total }} 条记录</div>
      </div>
      <div class="route-page__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:process-route:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新增工艺路线
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="route-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="route-page__section-title">筛选条件</div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="route-query">
      <div class="route-query__grid">
        <el-form-item label="工艺编码" prop="routeCode">
          <el-input v-model="queryParams.routeCode" clearable placeholder="请输入工艺编码" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="工艺名称" prop="routeName">
          <el-input v-model="queryParams.routeName" clearable placeholder="请输入工艺名称" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" clearable filterable :loading="productLoading" placeholder="请选择产品">
            <el-option v-for="item in productOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option v-for="item in getIntDictOptions(DICT_TYPE.ERP_ROUTE_STATUS)" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </div>
      <div class="route-query__footer">
        <div></div>
        <div class="route-query__actions">
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="route-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="route-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="route-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="route-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="route-page__overview-card__content">
          <div class="route-page__overview-card__value">{{ card.value }}</div>
          <div class="route-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="route-table__scroll">
      <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="route-table">
        <template #empty>
          <div class="route-empty">
            <div class="route-empty__icon">
              <Icon icon="ep:connection" />
            </div>
            <div class="route-empty__title">暂无工艺路线</div>
          </div>
        </template>

        <el-table-column label="工艺信息" min-width="240">
          <template #default="{ row }">
            <div class="route-info">
              <div class="route-info__code">{{ row.routeCode }}</div>
              <div class="route-info__name">
                {{ row.routeName }}
                <span v-if="row.defaultFlag" class="route-info__default">默认</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="产品" min-width="160">
          <template #default="{ row }">
            <div class="route-cell">{{ row.productName || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="版本" width="100" align="center">
          <template #default="{ row }">
            <span class="route-version">{{ row.version || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="生效期" min-width="180">
          <template #default="{ row }">
            <span class="route-date">{{ formatDateValue(row.effectiveDate) }} ~ {{ formatDateValue(row.expireDate) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span class="route-pill" :class="getStatusPillClass(row.status)">
              {{ getStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="工序数" width="90" align="center">
          <template #default="{ row }">
            <span class="route-cell">{{ row.steps?.length ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <span class="route-date">{{ formatDateValue(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm('update', row)" v-hasPermi="['erp:process-route:update']">
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              :loading="statusLoadingId === row.id"
              :disabled="statusLoadingId === row.id"
              @click="handleUpdateStatus(row)"
              v-hasPermi="['erp:process-route:update']"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button
              link
              type="danger"
              :loading="deleteLoadingId === row.id"
              :disabled="deleteLoadingId === row.id"
              @click="handleDelete(row)"
              v-hasPermi="['erp:process-route:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <ProcessRouteForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { ProcessRouteApi, type ProcessRoutePageReqVO, type ProcessRouteVO } from '@/api/erp/manufacturing/process-route'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import ProcessRouteForm from './ProcessRouteForm.vue'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpManufacturingProcessRoute' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref()

const listLoading = ref(false)
const productLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const statusLoadingId = ref<number | undefined>()
const list = ref<ProcessRouteVO[]>([])
const total = ref(0)
const productOptions = ref<ProductVO[]>([])

const queryParams = reactive<ProcessRoutePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  routeCode: undefined,
  routeName: undefined,
  productId: undefined,
  status: undefined
})

const statusLabelMap = new Map(
  getIntDictOptions(DICT_TYPE.ERP_ROUTE_STATUS).map((item) => [Number(item.value), item.label])
)

const getStatusLabel = (status?: number) => statusLabelMap.get(Number(status)) ?? '-'

const getStatusPillClass = (status?: number) => {
  switch (Number(status)) {
    case 1:
      return 'route-pill--success'
    case 2:
      return 'route-pill--danger'
    default:
      return 'route-pill--neutral'
  }
}

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatDateValue = (value?: string | number) => (value ? formatDate(value) : '-')

const summaryCards = computed(() => {
  const enabled = list.value.filter((item) => item.status === 1).length
  const draft = list.value.filter((item) => item.status === 0).length
  const disabled = list.value.filter((item) => item.status === 2).length
  return [
    { label: '总路线数', value: formatCount(total.value), icon: 'ep:connection', colorClass: 'stat-icon--blue' },
    { label: '已启用', value: formatCount(enabled), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
    { label: '草稿', value: formatCount(draft), icon: 'ep:edit-pen', colorClass: 'stat-icon--amber' },
    { label: '已停用', value: formatCount(disabled), icon: 'ep:remove', colorClass: 'stat-icon--slate' }
  ]
})

const getList = async () => {
  listLoading.value = true
  try {
    const data = await ProcessRouteApi.getProcessRoutePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (e: any) {
    list.value = []
    total.value = 0
    message.error(e?.message || '工艺路线列表加载失败')
  } finally {
    listLoading.value = false
  }
}

const loadProductOptions = async () => {
  productLoading.value = true
  try {
    productOptions.value = (await ProductApi.getProductSimpleList()) || []
  } finally {
    productLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openForm = (type: 'create' | 'update', row?: ProcessRouteVO) => {
  formRef.value?.open(type, row)
}

const handleUpdateStatus = async (row: ProcessRouteVO) => {
  if (!row.id || statusLoadingId.value) {
    return
  }
  statusLoadingId.value = row.id
  try {
    const nextStatus = row.status === 1 ? 2 : 1
    await message.confirm(nextStatus === 1 ? '确认启用该工艺路线吗？' : '确认停用该工艺路线吗？')
    await ProcessRouteApi.updateProcessRouteStatus(row.id, nextStatus)
    message.success('状态更新成功')
    await getList()
  } catch {
  } finally {
    statusLoadingId.value = undefined
  }
}

const handleDelete = async (row: ProcessRouteVO) => {
  if (!row.id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = row.id
  try {
    await message.delConfirm()
    await ProcessRouteApi.deleteProcessRoute(row.id)
    message.success('删除成功')
    await getList()
  } catch {
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadProductOptions()])
})
</script>

<style scoped lang="scss">
.route-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.route-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.route-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.route-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.route-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.route-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.route-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.route-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.route-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.route-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.route-page__overview-card {
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

.route-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.route-page__overview-card__icon {
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

.route-page__overview-card:hover .route-page__overview-card__icon {
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

.route-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.route-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.route-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.route-table__scroll {
  overflow-x: auto;
}

.route-table {
  min-width: 1180px;
}

.route-table :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.route-table :deep(.el-table td.el-table__cell),
.route-table :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.route-info__code {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.route-info__name {
  margin-top: 4px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.route-info__default {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  padding: 0 8px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  line-height: 20px;
  vertical-align: middle;
}

.route-cell,
.route-date,
.route-version {
  color: #475569;
  font-size: 13px;
  line-height: 20px;
}

.route-date {
  color: #64748b;
}

.route-version {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.route-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.route-pill--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.route-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.route-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.route-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.route-empty__icon {
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

.route-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

@media (max-width: 1279px) {
  .route-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .route-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .route-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .route-page__actions {
    width: 100%;
  }

  .route-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .route-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .route-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .route-query__actions {
    width: 100%;
  }
}
</style>
