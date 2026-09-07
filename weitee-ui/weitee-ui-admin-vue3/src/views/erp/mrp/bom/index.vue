<template>

  <ContentWrap class="bom-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="bom-page__header">
      <div>
        <div class="bom-page__title">制造BOM</div>
        <div class="bom-page__count">共 {{ total }} 条记录</div>
      </div>

    </div>
  </ContentWrap>

  <ContentWrap class="bom-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="bom-page__section-title">筛选条件</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="bom-query"
    >
      <div class="bom-query__grid">
        <el-form-item label="制造BOM编码" prop="bomCode">
          <el-input
            v-model="queryParams.bomCode"
            clearable
            placeholder="请输入制造 BOM 编码"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="成品" prop="productId">
          <ProductRemoteSelect v-model="queryParams.productId" placeholder="请选择成品" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            clearable
            placeholder="请选择状态"
          >
            <el-option
              v-for="item in BOM_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="bom-query__footer">
        <div></div>
        <div class="bom-query__actions">
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

  <ContentWrap class="bom-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="bom-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="bom-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="bom-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="bom-page__overview-card__content">
          <div class="bom-page__overview-card__value">{{ card.value }}</div>
          <div class="bom-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="bom-table__scroll">
      <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="bom-table">
      <el-table-column label="制造BOM编码" prop="bomCode" min-width="160" />
      <el-table-column label="成品" prop="productName" min-width="180" />
      <el-table-column label="版本" prop="version" width="120" align="center" />
      <el-table-column label="来源研发BOM" prop="sourceRdBomId" width="140" align="center" />
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.processInstanceId" type="warning">停用审批中</el-tag>
          <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已生效' : '草稿/停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物料数" width="100" align="center">
        <template #default="{ row }">
          {{ row.items?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" />
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
        align="center"
        :formatter="dateTimeFormatter"
      />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)" v-hasPermi="['erp:bom:query']">
            详情
          </el-button>
          <el-button
            v-if="row.status === 1 && !row.processInstanceId"
            link
            type="warning"
            :loading="statusLoadingId === row.id"
            @click="handleSubmitDisable(row)"
            v-hasPermi="['erp:bom:update-status']"
          >
            停用申请
          </el-button>
          <el-button
            v-if="row.processInstanceId"
            link
            type="danger"
            :loading="statusLoadingId === row.id"
            @click="handleCancelDisable(row)"
            v-hasPermi="['erp:bom:update-status']"
          >
            撤回
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

  <BomDetailDrawer ref="detailDrawerRef" />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'
import { BomApi, type BomPageReqVO, type BomVO } from '@/api/erp/mrp/bom'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import BomDetailDrawer from './BomDetailDrawer.vue'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpManufactureBom' })

const message = useMessage()
const { t } = useI18n()

const BOM_STATUS_OPTIONS = [
  { label: '已生效', value: 1 },
  { label: '草稿/停用', value: 0 }
]

const listLoading = ref(false)
const productLoading = ref(false)
const statusLoadingId = ref<number | undefined>()
const list = ref<BomVO[]>([])
const total = ref(0)
const productList = ref<ProductVO[]>([])
const queryFormRef = ref()
const detailDrawerRef = ref()

const queryParams = reactive<BomPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  productId: undefined,
  bomCode: undefined,
  status: undefined
})

// 统计卡片
const activeCount = computed(() => list.value.filter((item) => item.status === 1).length)
const summaryCards = computed(() => [
  { label: '总BOM数', value: formatCount(total.value), icon: 'ep:document', colorClass: 'stat-icon--blue' },
  { label: '已生效', value: formatCount(activeCount.value), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
  { label: '草稿/停用', value: formatCount(list.value.length - activeCount.value), icon: 'ep:clock', colorClass: 'stat-icon--amber' },
  { label: '当前页', value: formatCount(list.value.length), icon: 'ep:list', colorClass: 'stat-icon--slate' }
])

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const dateTimeFormatter = (_row: BomVO, _column: unknown, cellValue: string | number | undefined) => {
  if (cellValue === undefined || cellValue === null || cellValue === '') {
    return ''
  }
  const normalizedValue =
    typeof cellValue === 'string' && /^\d{10,13}$/.test(cellValue.trim())
      ? Number(cellValue.trim().length === 10 ? `${cellValue.trim()}000` : cellValue.trim())
      : cellValue
  return formatDate(normalizedValue) || String(cellValue)
}

const getList = async () => {
  listLoading.value = true
  try {
    const data = await BomApi.getBomPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    listLoading.value = false
  }
}

const loadProductList = async () => {
  productLoading.value = true
  try {
    productList.value = await ProductApi.getProductSimpleList()
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

const openDetail = (row?: BomVO) => {
  if (!row?.id && !row?.productId) {
    return
  }
  detailDrawerRef.value?.open({ bomId: row.id, productId: row.productId })
}

// 制造 BOM 为研发 BOM 发布快照：停用/废止走 BPM 申请流，通过后由系统落 DISABLE
const handleSubmitDisable = async (row: BomVO) => {
  if (!row?.id || statusLoadingId.value) {
    return
  }
  statusLoadingId.value = row.id
  try {
    await message.confirm(`确认发起制造 BOM“${row.bomCode}”的停用申请吗？`)
    await BomApi.submitDisableApproval(row.id)
    message.success('停用申请已提交，审批通过前 BOM 保持生效')
    await getList()
  } catch {
  } finally {
    statusLoadingId.value = undefined
  }
}

// 撤回制造 BOM 停用申请：原因必填（后端兜底默认值）
const handleCancelDisable = async (row: BomVO) => {
  if (!row?.id || statusLoadingId.value) {
    return
  }
  statusLoadingId.value = row.id
  try {
    const { value } = await message.prompt(`确认撤回制造 BOM“${row.bomCode}”的停用申请吗？`, '填写撤回原因')
    if (!value || !value.trim()) {
      message.warning('请填写撤回原因')
      return
    }
    await BomApi.cancelDisableApproval(row.id, String(value).trim())
    message.success('撤回成功，BOM 保持生效')
    await getList()
  } catch {
  } finally {
    statusLoadingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadProductList()])
})
</script>

<style scoped lang="scss">
.bom-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.bom-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.bom-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.bom-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.bom-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.bom-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bom-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.bom-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bom-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.bom-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.bom-page__overview-card {
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

.bom-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.bom-page__overview-card__icon {
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

.bom-page__overview-card:hover .bom-page__overview-card__icon {
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

.bom-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.bom-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.bom-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.bom-table__scroll {
  overflow-x: auto;
}

.bom-table {
  min-width: 960px;
}

@media (max-width: 1279px) {
  .bom-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bom-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .bom-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .bom-page__actions {
    width: 100%;
  }

  .bom-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .bom-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .bom-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .bom-query__actions {
    width: 100%;
  }
}
</style>
