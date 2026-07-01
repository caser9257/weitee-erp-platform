<template>
  <div class="finance-shell finance-shell__stack product-dual-cost-page">
    <!-- 页头 -->
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">产品双账套成本</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip">结果 {{ total }}</span>
            <span class="finance-shell__metric-chip">外部金额{{ formatAmount(totalExternal) }}</span>
            <span class="finance-shell__metric-chip">内部金额{{ formatAmount(totalInternal) }}</span>
            <span class="finance-shell__metric-chip">差异 {{ formatAmount(totalDiff) }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button plain :loading="loadingList" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 搜索区-->
    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="产品名称" prop="productName">
            <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="产品编号" prop="productNo">
            <el-input v-model="queryParams.productNo" placeholder="请输入产品编号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="工单编号" prop="productionOrderId">
            <el-input v-model="queryParams.productionOrderId" placeholder="请输入工单ID" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="期间" prop="period">
            <el-date-picker v-model="queryParams.period" type="month" placeholder="选择月份" value-format="YYYY-MM" class="!w-full" />
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 表格区-->
    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">产品双账套成本列表</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button type="primary" :loading="rebuilding" @click="handleRebuildAll">
            <Icon icon="ep:refresh" class="mr-5px" />
            产品级重跑
          </el-button>
          <el-button plain :disabled="!total" @click="handleExportExternal">
            <Icon icon="ep:download" class="mr-5px" />
            导出外部账
          </el-button>
          <el-button plain :disabled="!total" @click="handleExportInternal">
            <Icon icon="ep:download" class="mr-5px" />
            导出内部账
          </el-button>
        </div>
      </div>

      <el-alert v-if="listError" class="mb-12px" :closable="false" show-icon type="error" :title="listError">
        <template #default>
          <el-button link type="primary" :disabled="loadingList" @click="getList">重新加载</el-button>
        </template>
      </el-alert>

      <template v-if="loadingList || list.length">
        <div class="finance-shell__table-wrap">
          <el-table v-loading="loadingList" :data="list" row-key="id" stripe class="finance-shell__table finance-shell__table--dense">
            <el-table-column min-width="200">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:goods" class="finance-shell__column-icon" />
                  产品信息
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.productName || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.productNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="工单/批次" min-width="140">
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.productionOrderNo || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.productBatchNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="期间" min-width="100" align="center">
              <template #default="{ row }">
                <span class="finance-shell__mono">{{ row.period || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column min-width="140" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  外部账总成本
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.externalTotalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column min-width="140" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  内部账总成本
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.internalTotalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column min-width="140" align="right">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:trend-charts" class="finance-shell__column-icon" />
                  差异金额
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': Number(row.diffAmount || 0) !== 0 }">
                  {{ formatAmount(row.diffAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="120">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button link type="primary" @click.stop="handleViewDetail(row)">
                    查看明细
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <Pagination v-if="total > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </template>
      <div v-else class="product-dual-cost-page__empty">
        <el-empty description="暂无产品双账套成本数据">
          <template #image>
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:files" />
            </div>
          </template>
        </el-empty>
      </div>
    </ContentWrap>

    <!-- 明细 Drawer -->
    <el-drawer
      v-model="detailDrawerVisible"
      size="520px"
      destroy-on-close
      :with-header="false"
      :modal-class="'finance-shell__drawer-mask'"
      @closed="clearDetailDrawer"
    >
      <div class="product-dual-cost-page__drawer">
        <div class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ detailData?.productName || '-' }}</div>
            <div class="finance-shell__context-subtitle">{{ detailData?.productNo || '产品双账套成本明细' }}</div>
          </div>
          <div class="finance-shell__context-meta">
            <span class="finance-shell__page-chip">{{ detailData?.period || '-' }}</span>
            <span class="finance-shell__page-chip">{{ detailData?.productionOrderNo || '-' }}</span>
          </div>
        </div>

        <div v-if="loadingDetail" class="product-dual-cost-page__drawer-loading">
          <el-skeleton :rows="6" animated />
        </div>
        <template v-else>
          <div class="product-dual-cost-page__drawer-body">
            <div class="finance-shell__metric-grid product-dual-cost-page__drawer-metrics">
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">材料成本(外)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalMaterialAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">材料成本(内)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalMaterialAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">人工成本(外)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalLaborAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">人工成本(内)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalLaborAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">制造费用(外)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalOverheadAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">制造费用(内)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalOverheadAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">总成本(外)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalTotalAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">总成本(内)</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalTotalAmount) }}</div>
              </div>
            </div>

            <div class="finance-shell__section-title">成本明细</div>
            <div v-if="detailItems.length" class="product-dual-cost-page__detail-table">
              <el-table :data="detailItems" stripe class="finance-shell__table finance-shell__table--dense">
                <el-table-column label="成本构成" min-width="100">
                  <template #default="{ row }">
                    <span>{{ row.costTypeName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="外部金额" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.externalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="内部金额" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.internalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="差异" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': Number(row.diffAmount || 0) !== 0 }">
                      {{ formatAmount(row.diffAmount) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="product-dual-cost-page__empty product-dual-cost-page__empty--drawer">
              <el-empty description="暂无明细数据" :image-size="60" />
            </div>
          </div>
        </template>

        <div class="product-dual-cost-page__drawer-footer">
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 重跑对话框-->
    <el-dialog v-model="rebuildDialogVisible" title="产品级重跑" width="480px" destroy-on-close>
      <el-form ref="rebuildFormRef" :model="rebuildForm" label-width="88px">
        <el-form-item label="产品" required>
          <el-input :model-value="rebuildForm.productName" disabled />
        </el-form-item>
        <el-form-item label="期间" required>
          <el-date-picker v-model="rebuildForm.period" type="month" placeholder="选择月份" value-format="YYYY-MM" class="!w-full" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rebuildForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="rebuilding" @click="rebuildDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rebuilding" @click="confirmRebuild">确认重跑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import download from '@/utils/download'
import {
  DualProductCostApi,
  type DualProductCostVO,
  type DualProductCostPageReqVO
} from '@/api/erp/finance/product-dual-cost'

defineOptions({ name: 'ErpFinanceDualProductCost' })

const message = useMessage()

const loadingList = ref(false)
const list = ref<DualProductCostVO[]>([])
const total = ref(0)
const listError = ref('')
const rebuilding = ref(false)

const queryParams = reactive<DualProductCostPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  productId: undefined,
  productNo: undefined,
  productName: undefined,
  productBatchNo: undefined,
  productionOrderId: undefined,
  period: undefined
})

const queryFormRef = ref()

const totalExternal = computed(() => list.value.reduce((sum, item) => sum + Number(item.externalTotalAmount || 0), 0))
const totalInternal = computed(() => list.value.reduce((sum, item) => sum + Number(item.internalTotalAmount || 0), 0))
const totalDiff = computed(() => list.value.reduce((sum, item) => sum + Number(item.diffAmount || 0), 0))

const detailDrawerVisible = ref(false)
const loadingDetail = ref(false)
const detailData = ref<DualProductCostVO>()
const detailItems = ref<DualProductCostVO[]>([])

const rebuildDialogVisible = ref(false)
const rebuildFormRef = ref()
const rebuildForm = reactive({
  productId: undefined as number | undefined,
  productName: '',
  productionOrderId: undefined as number | undefined,
  productBatchNo: '',
  period: '',
  remark: ''
})

const formatAmount = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const getList = async () => {
  loadingList.value = true
  listError.value = ''
  try {
    const data = await DualProductCostApi.getProductDualCostPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listError.value = '产品双账套成本数据加载失败'
  } finally {
    loadingList.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  getList()
}

const handleRefresh = () => getList()

const handleViewDetail = async (row: DualProductCostVO) => {
  detailData.value = row
  detailDrawerVisible.value = true
  loadingDetail.value = true
  try {
    detailItems.value = await DualProductCostApi.getProductDualCostItems(row.id!)
  } catch {
    detailItems.value = []
  } finally {
    loadingDetail.value = false
  }
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailItems.value = []
}

const handleRebuildAll = () => {
  if (!list.value.length) {
    message.warning('暂无数据可重跑')
    return
  }
  const first = list.value[0]
  rebuildForm.productId = first.productId
  rebuildForm.productName = first.productName || ''
  rebuildForm.productionOrderId = first.productionOrderId
  rebuildForm.productBatchNo = first.productBatchNo || ''
  rebuildForm.period = first.period || ''
  rebuildForm.remark = ''
  rebuildDialogVisible.value = true
}

const confirmRebuild = async () => {
  if (!rebuildForm.productId || !rebuildForm.period) {
    message.warning('请选择产品和期间')
    return
  }
  rebuilding.value = true
  try {
    await DualProductCostApi.rebuildProductDualCost({
      productId: rebuildForm.productId,
      productionOrderId: rebuildForm.productionOrderId,
      productBatchNo: rebuildForm.productBatchNo,
      period: rebuildForm.period,
      remark: rebuildForm.remark
    })
    message.success('重跑成功')
    rebuildDialogVisible.value = false
    await getList()
  } catch {
    // 重跑失败
  } finally {
    rebuilding.value = false
  }
}

const handleExportExternal = async () => {
  try {
    const data = await DualProductCostApi.exportExternalProductCost(queryParams)
    download.excel(data, '产品外部账成本.xlsx')
  } catch {
    // 导出失败
  }
}

const handleExportInternal = async () => {
  try {
    const data = await DualProductCostApi.exportInternalProductCost(queryParams)
    download.excel(data, '产品内部账成本.xlsx')
  } catch {
    // 导出失败
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.product-dual-cost-page {
  min-height: 100%;
}

.product-dual-cost-page :deep(.finance-shell__header-card .el-card__body),
.product-dual-cost-page :deep(.finance-shell__filter-card .el-card__body),
.product-dual-cost-page :deep(.finance-shell__table-card .el-card__body) {
  padding: 14px 16px;
}

.product-dual-cost-page__empty {
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  padding: 8px;
}

.product-dual-cost-page :deep(.el-drawer__body) {
  padding: 0;
}

.product-dual-cost-page__drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8fafc;
}

.product-dual-cost-page__drawer-loading,
.product-dual-cost-page__drawer-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
}

.product-dual-cost-page__drawer-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.product-dual-cost-page__drawer-footer {
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
</style>
