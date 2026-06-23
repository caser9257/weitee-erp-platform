<template>
  <div class="ar-statement-page">
    <!-- 页头卡片 -->
    <ContentWrap class="ar-statement-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">应收台账</div>
          <div class="page-header__desc">管理销售出库和销售退货的应收账款</div>
        </div>
        <div class="page-header__actions">
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
          <el-button @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- KPI 统计卡片 -->
    <ContentWrap class="ar-statement-page__stats">
      <div class="kpi-grid">
        <div class="kpi-card kpi-card--primary">
          <div class="kpi-card__icon">
            <Icon icon="ep:wallet" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">应收总额</div>
            <div class="kpi-card__value">{{ formatAmount(stats.totalAmount) }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--success">
          <div class="kpi-card__icon">
            <Icon icon="ep:check" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">已收总额</div>
            <div class="kpi-card__value">{{ formatAmount(stats.totalReceivedAmount) }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--warning">
          <div class="kpi-card__icon">
            <Icon icon="ep:clock" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">待收总额</div>
            <div class="kpi-card__value">{{ formatAmount(stats.totalRemainAmount) }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--info">
          <div class="kpi-card__icon">
            <Icon icon="ep:document" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">台账数量</div>
            <div class="kpi-card__value">{{ stats.statementCount || 0 }}</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 搜索卡片 -->
    <ContentWrap class="ar-statement-page__search">
      <el-form :model="queryParams" inline>
        <el-form-item label="台账编号">
          <el-input v-model="queryParams.statementNo" placeholder="请输入台账编号" clearable />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.bizType" placeholder="全部" clearable>
            <el-option label="销售出库" :value="21" />
            <el-option label="销售退货" :value="22" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="queryParams.customerId" placeholder="全部" clearable filterable>
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待收" :value="0" />
            <el-option label="部分收" :value="1" />
            <el-option label="已结清" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 表格卡片 -->
    <ContentWrap class="ar-statement-page__table">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        highlight-current-row
        @sort-change="handleSortChange"
      >
        <el-table-column label="台账信息" min-width="200">
          <template #default="{ row }">
            <div class="statement-info">
              <div class="statement-info__no">{{ row.statementNo }}</div>
              <div class="statement-info__biz">
                <el-tag :type="row.bizType === 21 ? 'primary' : 'warning'" size="small">
                  {{ row.bizType === 21 ? '销售出库' : '销售退货' }}
                </el-tag>
                <span class="statement-info__biz-no">{{ row.bizNo }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            <span>{{ row.customerName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="应收金额" min-width="120" align="right" sortable="custom" prop="amount">
          <template #default="{ row }">
            <span class="amount-text" :class="{ 'amount-negative': row.amount < 0 }">
              {{ formatAmount(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="已收金额" min-width="120" align="right" sortable="custom" prop="receivedAmount">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.receivedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余金额" min-width="120" align="right" sortable="custom" prop="remainAmount">
          <template #default="{ row }">
            <span class="amount-text amount-highlight" :class="{ 'amount-negative': row.remainAmount < 0 }">
              {{ formatAmount(row.remainAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开票状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getInvoiceStatusTagType(row.invoiceStatus)" size="small">
              {{ getInvoiceStatusLabel(row.invoiceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="业务日期" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.bizDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </ContentWrap>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="应收台账详情" width="800px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="台账编号">{{ currentDetail.statementNo }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">
            <el-tag :type="currentDetail.bizType === 21 ? 'primary' : 'warning'" size="small">
              {{ currentDetail.bizType === 21 ? '销售出库' : '销售退货' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业务单据号">{{ currentDetail.bizNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentDetail.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="应收金额">{{ formatAmount(currentDetail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="已收金额">{{ formatAmount(currentDetail.receivedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="剩余金额">
            <span class="amount-highlight">{{ formatAmount(currentDetail.remainAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentDetail.status)">
              {{ getStatusLabel(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业务日期">{{ currentDetail.bizDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ currentDetail.dueDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开票状态">
            <el-tag :type="getInvoiceStatusTagType(currentDetail.invoiceStatus)">
              {{ getInvoiceStatusLabel(currentDetail.invoiceStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 台账明细 -->
        <div v-if="currentDetail.items && currentDetail.items.length > 0" class="detail-items">
          <h4>台账明细</h4>
          <el-table :data="currentDetail.items" stripe size="small">
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getItemTypeTagType(row.itemType)" size="small">
                  {{ getItemTypeLabel(row.itemType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="关联单据" prop="refNo" min-width="150" />
            <el-table-column label="金额" prop="amount" min-width="120" align="right">
              <template #default="{ row }">
                <span :class="{ 'amount-negative': row.amount < 0 }">{{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="200" />
            <el-table-column label="时间" prop="createTime" width="160" />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArStatementApi, type ArStatementVO, type ArStatementSummaryVO } from '@/api/erp/finance/ar-statement'

defineOptions({ name: 'ErpArStatementPage' })

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<ArStatementVO[]>([])
const total = ref(0)

// 统计数据
const stats = ref<ArStatementSummaryVO>({
  totalAmount: 0,
  totalReceivedAmount: 0,
  totalRemainAmount: 0,
  statementCount: 0
})

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  statementNo: undefined as string | undefined,
  bizType: undefined as number | undefined,
  customerId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 客户列表（简化）
const customerList = ref<Array<{ id: number; name: string }>>([])

// 详情弹窗
const detailVisible = ref(false)
const currentDetail = ref<ArStatementVO | null>(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await ArStatementApi.getPage(queryParams)
    tableData.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 加载统计
const loadStats = async () => {
  try {
    const res = await ArStatementApi.getSummary()
    if (res && res.length > 0) {
      stats.value = {
        totalAmount: res.reduce((sum, item) => sum + (item.totalAmount || 0), 0),
        totalReceivedAmount: res.reduce((sum, item) => sum + (item.totalReceivedAmount || 0), 0),
        totalRemainAmount: res.reduce((sum, item) => sum + (item.totalRemainAmount || 0), 0),
        statementCount: res.reduce((sum, item) => sum + (item.statementCount || 0), 0)
      }
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNo = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.statementNo = undefined
  queryParams.bizType = undefined
  queryParams.customerId = undefined
  queryParams.status = undefined
  queryParams.pageNo = 1
  loadData()
}

// 排序
const handleSortChange = ({ prop, order }: any) => {
  // TODO: 实现排序
  console.log('排序', prop, order)
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看详情
const handleViewDetail = async (row: ArStatementVO) => {
  try {
    const res = await ArStatementApi.get(row.id!)
    currentDetail.value = res
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

// 格式化金额
const formatAmount = (amount?: number) => {
  if (amount === undefined || amount === null) return '0.00'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 状态标签
const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = {
    0: '待收',
    1: '部分收',
    2: '已结清',
    3: '已关闭'
  }
  return map[status || 0] || '未知'
}

const getStatusTagType = (status?: number) => {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return map[status || 0] || 'info'
}

// 开票状态标签
const getInvoiceStatusLabel = (status?: number) => {
  const map: Record<number, string> = {
    0: '未开票',
    1: '部分开票',
    2: '已开票'
  }
  return map[status || 0] || '未知'
}

const getInvoiceStatusTagType = (status?: number) => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return map[status || 0] || 'info'
}

// 明细类型标签
const getItemTypeLabel = (type?: number) => {
  const map: Record<number, string> = {
    1: '应收',
    2: '收款分配',
    3: '收款退回',
    4: '台账关闭'
  }
  return map[type || 0] || '未知'
}

const getItemTypeTagType = (type?: number) => {
  const map: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info'
  }
  return map[type || 0] || 'info'
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped lang="scss">
.ar-statement-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;

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

  .page-header__actions {
    display: flex;
    gap: 12px;
  }

  .kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
  }

  .kpi-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid var(--erp-slate-200);
  }

  .kpi-card__icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    font-size: 24px;
  }

  .kpi-card--primary .kpi-card__icon {
    background: var(--erp-primary-50);
    color: var(--erp-primary-500);
  }

  .kpi-card--success .kpi-card__icon {
    background: var(--erp-success-50);
    color: var(--erp-success-500);
  }

  .kpi-card--warning .kpi-card__icon {
    background: var(--erp-warning-50);
    color: var(--erp-warning-500);
  }

  .kpi-card--info .kpi-card__icon {
    background: var(--erp-slate-100);
    color: var(--erp-slate-500);
  }

  .kpi-card__label {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .kpi-card__value {
    margin-top: 4px;
    font-size: 28px;
    font-weight: 800;
    color: var(--erp-slate-900);
    font-variant-numeric: tabular-nums;
  }

  .statement-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .statement-info__no {
    font-weight: 600;
    color: var(--erp-slate-900);
  }

  .statement-info__biz {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .statement-info__biz-no {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .amount-text {
    font-variant-numeric: tabular-nums;
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  }

  .amount-highlight {
    font-weight: 600;
    color: var(--erp-primary-600);
  }

  .amount-negative {
    color: var(--erp-danger-600);
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .detail-content {
    .detail-items {
      margin-top: 24px;

      h4 {
        margin-bottom: 12px;
        font-weight: 600;
        color: var(--erp-slate-900);
      }
    }
  }
}
</style>
