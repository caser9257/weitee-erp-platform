<template>
  <div class="shipment-release-page">
    <ContentWrap class="shipment-release-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">发货放行审核</div>
          <div class="page-header__desc">按合同条款、收款与财务审核状态统一放行</div>
        </div>
        <div class="page-header__actions">
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="shipment-release-page__filters">
      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
          <el-form-item label="订单编号" prop="orderNo">
            <el-input v-model="queryParams.orderNo" clearable placeholder="请输入订单编号" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="项目编号" prop="projectNo">
            <el-input v-model="queryParams.projectNo" clearable placeholder="请输入项目编号" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="合同编号" prop="contractNo">
            <el-input v-model="queryParams.contractNo" clearable placeholder="请输入合同编号" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="放行状态" prop="releaseStatus">
            <el-select v-model="queryParams.releaseStatus" clearable placeholder="请选择放行状态">
              <el-option label="待校验" value="PENDING" />
              <el-option label="阻塞" value="BLOCKED" />
              <el-option label="待财务审核" value="FINANCE_REVIEW" />
              <el-option label="已放行" value="RELEASED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button type="primary" :loading="loading" @click="loadData">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="shipment-release-page__stats">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-card__label">待校验</div>
          <div class="kpi-card__value">{{ stats.totalCount || 0 }}</div>
        </div>
        <div class="kpi-card kpi-card--rose">
          <div class="kpi-card__label">阻塞</div>
          <div class="kpi-card__value">{{ stats.blockedCount || 0 }}</div>
        </div>
        <div class="kpi-card kpi-card--amber">
          <div class="kpi-card__label">待财务审核</div>
          <div class="kpi-card__value">{{ stats.financeReviewCount || 0 }}</div>
        </div>
        <div class="kpi-card kpi-card--emerald">
          <div class="kpi-card__label">已放行</div>
          <div class="kpi-card__value">{{ stats.releasedCount || 0 }}</div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="shipment-release-page__table">
      <el-table :data="list" v-loading="loading" stripe class="shipment-release-table" @row-click="openDrawer">
        <el-table-column label="订单信息" min-width="260">
          <template #default="{ row }">
            <div class="table-cell-main">
              <div class="table-cell-main__title">{{ row.orderNo }}</div>
              <div class="table-cell-main__meta">{{ row.projectName || '-' }} / {{ row.customerName || '-' }}</div>
              <div class="table-cell-main__meta">合同：{{ row.contractNo || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="条款" min-width="260">
          <template #default="{ row }">
            <div class="pill-stack">
              <el-tag size="small" effect="light">{{ formatReleaseRule(row.releaseRule) }}</el-tag>
              <el-tag size="small" effect="light">{{ formatInvoiceTrigger(row.invoiceTrigger) }}</el-tag>
              <el-tag size="small" effect="light">{{ formatCollectionRule(row.collectionRule) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收款/金额" min-width="180" align="right">
          <template #default="{ row }">
            <div class="table-cell-amount">
              <div>{{ formatMoney(row.receivedAmount) }} / {{ formatMoney(row.receivableAmount) }}</div>
              <div class="muted">{{ progressText(row.receivedAmount, row.receivableAmount) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.releaseStatus)">{{ formatStatus(row.releaseStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="checkRow(row)" v-hasPermi="['erp:shipment-release:check']">校验</el-button>
            <el-button v-if="row.releaseStatus === 'FINANCE_REVIEW'" link type="success" @click.stop="approveRow(row)" v-hasPermi="['erp:shipment-release:approve']">通过</el-button>
            <el-button v-if="row.releaseStatus === 'FINANCE_REVIEW'" link type="danger" @click.stop="rejectRow(row)" v-hasPermi="['erp:shipment-release:reject']">驳回</el-button>
            <el-button v-if="row.releaseStatus === 'RELEASED'" link type="success" @click.stop="openConvertDialog(row)" v-hasPermi="['erp:shipment-release:check']">转出库单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <el-drawer v-model="drawerVisible" :size="480" :with-header="false" class="shipment-release-drawer">
      <div class="drawer-context">
        <div class="drawer-context__title">{{ currentRow?.orderNo || '-' }}</div>
        <div class="drawer-context__subtitle">{{ currentRow?.projectName || '-' }}</div>
        <div class="drawer-context__meta">{{ currentRow?.contractNo || '-' }}</div>
      </div>

      <div class="drawer-card">
        <div class="drawer-card__title">校验结果</div>
        <div class="drawer-card__content">
          <div v-if="checkResult">
            <el-result :icon="checkResult.releasable ? 'success' : 'warning'" :title="formatStatus(checkResult.releaseStatus)">
              <template #extra>
                <div class="drawer-tags">
                  <el-tag v-for="reason in checkResult.blockerReasons" :key="reason" type="danger" effect="light">{{ reason }}</el-tag>
                </div>
              </template>
            </el-result>
          </div>
          <el-empty v-else description="尚未校验" />
        </div>
      </div>

      <div class="drawer-card">
        <div class="drawer-card__title">校验明细</div>
        <div class="drawer-card__content">
          <el-timeline v-if="checkResult?.details?.length">
            <el-timeline-item v-for="item in checkResult.details" :key="item.checkItem" :type="item.passed ? 'success' : 'danger'">
              <div class="timeline-item">
                <div class="timeline-item__title">{{ item.checkItem }}</div>
                <div class="timeline-item__desc">{{ item.message }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无明细" />
        </div>
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">关闭</el-button>
          <el-button type="primary" :loading="drawerLoading" @click="runCheck">重新校验</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 转出库单 - 仓库选择弹窗 -->
    <el-dialog
      v-model="convertDialogVisible"
      title="转出库单"
      width="460px"
      :close-on-click-modal="false"
      class="convert-out-dialog"
    >
      <div class="convert-out-dialog__body">
        <div class="convert-out-dialog__info">
          <div class="convert-out-dialog__label">当前订单</div>
          <div class="convert-out-dialog__value">{{ convertRow?.orderNo || '-' }}</div>
          <div class="convert-out-dialog__meta">{{ convertRow?.customerName || '-' }}</div>
        </div>
        <el-form-item label="目标仓库" required>
          <el-select
            v-model="selectedWarehouseId"
            placeholder="请选择目标仓库"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="w in warehouseList"
              :key="w.id"
              :label="w.name"
              :value="w.id"
            />
          </el-select>
        </el-form-item>
      </div>
      <template #footer>
        <div class="convert-out-dialog__footer">
          <el-button @click="convertDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="convertLoading" @click="confirmConvert">确认创建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShipmentReleaseApi, type ShipmentReleasePageVO, type ShipmentReleaseResultVO, type ShipmentReleaseStatsVO } from '@/api/erp/sale/shipment-release'
import { WarehouseApi, type WarehouseVO } from '@/api/erp/stock/warehouse'

defineOptions({ name: 'ErpShipmentReleasePage' })

const loading = ref(false)
const drawerLoading = ref(false)
const drawerVisible = ref(false)
const list = ref<ShipmentReleasePageVO[]>([])
const stats = ref<ShipmentReleaseStatsVO>({ totalCount: 0, blockedCount: 0, financeReviewCount: 0, releasedCount: 0 })
const currentRow = ref<ShipmentReleasePageVO | null>(null)
const checkResult = ref<ShipmentReleaseResultVO | null>(null)
const queryParams = reactive({ pageNo: 1, pageSize: 20, orderNo: '', projectNo: '', contractNo: '', releaseStatus: '' })
const queryFormRef = ref()

// 转出库单相关状态
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const warehouseList = ref<Pick<WarehouseVO, 'id' | 'name'>[]>([])
const selectedWarehouseId = ref<number | undefined>(undefined)
const convertRow = ref<ShipmentReleasePageVO | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const [pageRes, statsRes] = await Promise.all([
      ShipmentReleaseApi.getShipmentReleasePage(queryParams),
      ShipmentReleaseApi.getShipmentReleaseStats()
    ])
    list.value = pageRes.list || []
    stats.value = statsRes || stats.value
  } finally {
    loading.value = false
  }
}

const resetQuery = async () => {
  queryParams.orderNo = ''
  queryParams.projectNo = ''
  queryParams.contractNo = ''
  queryParams.releaseStatus = ''
  queryParams.pageNo = 1
  await loadData()
}

const openDrawer = async (row: ShipmentReleasePageVO) => {
  currentRow.value = row
  drawerVisible.value = true
  await runCheck()
}

const runCheck = async () => {
  if (!currentRow.value?.orderId) return
  drawerLoading.value = true
  try {
    checkResult.value = await ShipmentReleaseApi.checkShipmentRelease({ orderId: currentRow.value.orderId })
  } finally {
    drawerLoading.value = false
  }
}

const approveRow = async (row: ShipmentReleasePageVO) => {
  await ElMessageBox.prompt('请输入审核意见（可选）', '财务审核通过', { confirmButtonText: '通过', cancelButtonText: '取消', inputPlaceholder: '审核意见' })
    .then(async ({ value }) => {
      await ShipmentReleaseApi.approveFinance(row.orderId, value)
      ElMessage.success('已通过')
      await loadData()
    })
    .catch(() => {})
}

const rejectRow = async (row: ShipmentReleasePageVO) => {
  await ElMessageBox.prompt('请输入驳回原因', '财务审核驳回', { confirmButtonText: '驳回', cancelButtonText: '取消', inputPlaceholder: '驳回原因' })
    .then(async ({ value }) => {
      await ShipmentReleaseApi.rejectFinance(row.orderId, value)
      ElMessage.success('已驳回')
      await loadData()
    })
    .catch(() => {})
}

const checkRow = async (row: ShipmentReleasePageVO) => {
  currentRow.value = row
  drawerVisible.value = true
  await runCheck()
}

/** 打开转出库单弹窗 */
const openConvertDialog = async (row: ShipmentReleasePageVO) => {
  convertRow.value = row
  selectedWarehouseId.value = undefined
  convertDialogVisible.value = true
  // 加载仓库列表
  try {
    const res = await WarehouseApi.getWarehouseSimpleList()
    warehouseList.value = (res || []).map((w: WarehouseVO) => ({ id: w.id, name: w.name }))
  } catch {
    warehouseList.value = []
  }
}

/** 确认转出库单 */
const confirmConvert = async () => {
  if (!selectedWarehouseId.value) {
    ElMessage.warning('请选择目标仓库')
    return
  }
  if (!convertRow.value?.orderId) return

  convertLoading.value = true
  try {
    const outId = await ShipmentReleaseApi.createSaleOutFromRelease(
      convertRow.value.orderId,
      selectedWarehouseId.value
    )
    ElMessage.success(`出库单创建成功，单号 ID：${outId}`)
    convertDialogVisible.value = false
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建出库单失败')
  } finally {
    convertLoading.value = false
  }
}

const formatReleaseRule = (value?: string) => {
  const map: Record<string, string> = {
    SIGN_AND_SHIP: '签约即发', AFTER_PAYMENT: '到账后发', AFTER_PREPAYMENT: '预付款后发', FINANCE_APPROVAL: '财务审核后发'
  }
  return map[value || ''] || value || '--'
}

const formatInvoiceTrigger = (value?: string) => {
  const map: Record<string, string> = {
    PREPAYMENT_FULL: '预付款全额开票', PREPAYMENT_PARTIAL: '预付款部分开票', PREPAYMENT_ONLY: '仅预付款开票', AFTER_SHIPMENT: '发货后开票', AFTER_DELIVERY_RECEIPT: '交付收款后开票', MANUAL: '手工决定'
  }
  return map[value || ''] || value || '--'
}

const formatCollectionRule = (value?: string) => {
  const map: Record<string, string> = { BEFORE_SHIPMENT: '发货前付款', ON_SHIPMENT: '发货时付款', AFTER_SHIPMENT: '发货后约定期限付款' }
  return map[value || ''] || value || '--'
}

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const progressText = (received?: number | string | null, receivable?: number | string | null) => {
  const r = Number(received || 0)
  const a = Number(receivable || 0)
  if (!a) return '--'
  return `${((r / a) * 100).toFixed(0)}%`
}

const formatStatus = (value?: string) => {
  const map: Record<string, string> = { PENDING: '待校验', BLOCKED: '阻塞', FINANCE_REVIEW: '待财务审核', RELEASED: '已放行' }
  return map[value || ''] || value || '--'
}

const statusTagType = (value?: string) => {
  if (value === 'RELEASED') return 'success'
  if (value === 'FINANCE_REVIEW') return 'warning'
  if (value === 'BLOCKED') return 'danger'
  return 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.shipment-release-page {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-header__title {
    font-size: 20px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .page-header__desc {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .query-form__grid {
    display: grid;
    gap: 12px;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .query-form__actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 8px;
  }

  .kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .kpi-card {
    padding: 16px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 16px;
    background: #fff;
  }

  .kpi-card__label {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .kpi-card__value {
    margin-top: 8px;
    font-size: 28px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .kpi-card--rose .kpi-card__value { color: var(--erp-danger-600); }
  .kpi-card--amber .kpi-card__value { color: var(--erp-warning-600); }
  .kpi-card--emerald .kpi-card__value { color: var(--erp-success-600); }

  .shipment-release-table {
    :deep(.el-table__header-wrapper th) {
      background: var(--erp-slate-50);
      color: var(--erp-slate-600);
    }
  }

  .table-cell-main__title {
    font-weight: 700;
    color: var(--erp-slate-900);
  }

  .table-cell-main__meta,
  .muted {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .table-cell-amount {
    text-align: right;
  }

  .pill-stack {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .shipment-release-drawer {
    .drawer-context {
      padding: 16px;
      margin: -16px -16px 16px;
      background: linear-gradient(135deg, var(--erp-slate-900), var(--erp-slate-800));
      color: #fff;
      border-radius: 0 0 16px 16px;
    }

    .drawer-context__title {
      font-size: 18px;
      font-weight: 800;
    }

    .drawer-context__subtitle {
      margin-top: 4px;
      color: var(--erp-slate-300);
    }

    .drawer-context__meta {
      margin-top: 4px;
      color: var(--erp-slate-400);
      font-size: 12px;
    }

    .drawer-card {
      padding: 12px 0;
      border-bottom: 1px solid var(--erp-slate-200);
    }

    .drawer-card__title {
      margin-bottom: 8px;
      font-weight: 700;
      color: var(--erp-slate-900);
    }

    .drawer-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;
    }

    .timeline-item__title {
      font-weight: 600;
      color: var(--erp-slate-900);
    }

    .timeline-item__desc {
      color: var(--erp-slate-500);
      font-size: 12px;
      margin-top: 4px;
    }

    .drawer-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }

  .convert-out-dialog {
    .convert-out-dialog__body {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .convert-out-dialog__info {
      padding: 14px 16px;
      background: var(--erp-slate-50);
      border: 1px solid var(--erp-slate-200);
      border-radius: 10px;
    }

    .convert-out-dialog__label {
      font-size: 12px;
      color: var(--erp-slate-500);
      margin-bottom: 4px;
    }

    .convert-out-dialog__value {
      font-size: 16px;
      font-weight: 700;
      color: var(--erp-slate-900);
      font-family: var(--font-mono, 'JetBrains Mono', monospace);
    }

    .convert-out-dialog__meta {
      margin-top: 4px;
      font-size: 12px;
      color: var(--erp-slate-500);
    }

    .convert-out-dialog__footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>
