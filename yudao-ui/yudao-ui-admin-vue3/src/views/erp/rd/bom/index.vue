<template>
  <doc-alert title="物料清单（BOM）" url="https://doc.iocoder.cn/erp/bom/" />

  <ContentWrap class="rd-bom-page__header-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="rd-bom-page__header">
      <div>
        <div class="rd-bom-page__title">标准 BOM</div>
        <div class="rd-bom-page__count">共 {{ total }} 条记录</div>
      </div>
      <div class="rd-bom-page__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:bom:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增 BOM
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="rd-bom-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="rd-bom-page__section-title">筛选条件</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="rd-bom-query"
    >
      <div class="rd-bom-query__grid">
        <el-form-item label="BOM编码" prop="bomCode">
          <el-input
            v-model="queryParams.bomCode"
            clearable
            placeholder="请输入 BOM 编码"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="成品" prop="productId">
          <el-select
            v-model="queryParams.productId"
            clearable
            filterable
            :loading="productLoading"
            placeholder="请选择成品"
          >
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="item in BOM_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="rd-bom-query__footer">
        <div></div>
        <div class="rd-bom-query__actions">
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="rd-bom-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="rd-bom-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="rd-bom-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="rd-bom-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="rd-bom-page__overview-card__content">
          <div class="rd-bom-page__overview-card__value">{{ card.value }}</div>
          <div class="rd-bom-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="rd-bom-table__scroll">
      <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="rd-bom-table">
      <el-table-column label="BOM编码" prop="bomCode" min-width="160" />
      <el-table-column label="成品" prop="productName" min-width="180" />
      <el-table-column label="版本" prop="version" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '生效' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物料数" width="100" align="center">
        <template #default="{ row }">
          {{ row.items?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)" v-hasPermi="['erp:bom:query']">
            详情
          </el-button>
          <el-button link type="primary" @click="openForm('update', row.id)" v-hasPermi="['erp:bom:update']">
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            :loading="deleteLoadingId === row.id"
            :disabled="deleteLoadingId === row.id"
            @click="handleDelete(row.id)"
            v-hasPermi="['erp:bom:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <div class="rd-bom-page__footer">
      <div class="rd-bom-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <BomForm ref="formRef" :product-options="productList" @success="getList" />
  <BomDetailDrawer ref="detailDrawerRef" />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from '@/hooks/web/useMessage'
import { BomApi, type BomPageReqVO, type BomVO } from '@/api/erp/mrp/bom'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import BomDetailDrawer from './BomDetailDrawer.vue'
import BomForm from './BomForm.vue'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpBom' })

const message = useMessage()
const { t } = useI18n()

const BOM_STATUS_OPTIONS = [
  { label: '生效', value: 1 },
  { label: '停用', value: 0 }
]

const listLoading = ref(false)
const productLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const list = ref<BomVO[]>([])
const total = ref(0)
const productList = ref<ProductVO[]>([])
const queryFormRef = ref()
const formRef = ref()
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

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

const openDetail = (id?: number) => {
  if (!id) {
    return
  }
  detailDrawerRef.value?.open(id)
}

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = id
  try {
    await message.delConfirm()
    await BomApi.deleteBom(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadProductList()])
})
</script>

<style scoped lang="scss">
.rd-bom-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.rd-bom-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.rd-bom-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.rd-bom-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rd-bom-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.rd-bom-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rd-bom-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.rd-bom-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.rd-bom-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.rd-bom-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.rd-bom-page__overview-card {
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

.rd-bom-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.rd-bom-page__overview-card__icon {
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

.rd-bom-page__overview-card:hover .rd-bom-page__overview-card__icon {
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

.rd-bom-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.rd-bom-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.rd-bom-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.rd-bom-table__scroll {
  overflow-x: auto;
}

.rd-bom-table {
  min-width: 960px;
}

.rd-bom-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.rd-bom-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 1279px) {
  .rd-bom-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .rd-bom-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .rd-bom-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .rd-bom-page__actions {
    width: 100%;
  }

  .rd-bom-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .rd-bom-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .rd-bom-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .rd-bom-query__actions {
    width: 100%;
  }
}
</style>
