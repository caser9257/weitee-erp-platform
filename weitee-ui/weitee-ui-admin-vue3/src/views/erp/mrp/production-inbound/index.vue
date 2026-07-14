<template>
  <div class="min-h-full bg-[var(--erp-slate-50)] p-3 sm:p-4 lg:p-5">
    <section class="erp-card mb-4">
      <div class="flex items-center justify-between gap-4 px-5 py-4">
        <h1 class="text-lg font-semibold text-[var(--erp-slate-800)]">自制入库</h1>
        <el-tag type="info" effect="light">{{ total }} 条</el-tag>
      </div>
    </section>

    <section class="erp-card mb-4">
      <div class="flex items-center justify-between border-b border-[var(--erp-slate-100)] px-5 py-3">
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">查询条件</h2>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="82px" class="px-5 pb-4 pt-4">
        <div class="grid gap-x-4 gap-y-2 md:grid-cols-2 xl:grid-cols-4">
          <el-form-item label="入库单号" prop="no">
            <el-input v-model="queryParams.no" clearable placeholder="请输入入库单号" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="生产工单" prop="productionOrderId">
            <el-input-number
              v-model="queryParams.productionOrderId"
              :controls="false"
              :min="1"
              :precision="0"
              class="!w-full"
              placeholder="请输入生产工单 ID"
            />
          </el-form-item>
          <el-form-item label="质检单" prop="finishQualityId">
            <el-input-number
              v-model="queryParams.finishQualityId"
              :controls="false"
              :min="1"
              :precision="0"
              class="!w-full"
              placeholder="请输入质检单 ID"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="flex justify-end gap-2">
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-1" />查询
          </el-button>
          <el-button :disabled="listLoading" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-1" />重置
          </el-button>
        </div>
      </el-form>
    </section>

    <section class="erp-card overflow-hidden">
      <div class="border-b border-[var(--erp-slate-100)] px-5 py-3">
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">入库单列表</h2>
      </div>
      <div class="overflow-x-auto">
        <el-table v-loading="listLoading" :data="list" class="min-w-[980px]" table-layout="fixed">
          <el-table-column label="入库信息" min-width="230" fixed="left">
            <template #default="{ row }">
              <div class="space-y-1 py-1">
                <div class="font-mono text-sm text-[var(--erp-slate-800)]">{{ row.no || '-' }}</div>
                <div class="truncate text-xs text-[var(--erp-slate-500)]" :title="row.productionOrderNo || '-'">
                  工单：{{ row.productionOrderNo || '-' }}
                </div>
                <div class="truncate text-xs text-[var(--erp-slate-500)]" :title="row.finishQualityNo || '-'">
                  质检：{{ row.finishQualityNo || '-' }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产品 / 仓库" min-width="160">
            <template #default="{ row }">
              <div class="space-y-1">
                <div class="font-mono text-sm text-[var(--erp-slate-800)]">产品 {{ row.productId || '-' }}</div>
                <div class="truncate text-xs text-[var(--erp-slate-500)]" :title="row.warehouseName || '-'">
                  {{ row.warehouseName || '-' }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数量" min-width="110" align="right">
            <template #default="{ row }"><span class="font-mono">{{ formatCount(row.inboundQty) }}</span></template>
          </el-table-column>
          <el-table-column label="单位成本" min-width="130" align="right">
            <template #default="{ row }"><span class="font-mono">{{ formatAmount(row.unitCost) }}</span></template>
          </el-table-column>
          <el-table-column label="入库成本" min-width="140" align="right">
            <template #default="{ row }"><span class="font-mono font-semibold">{{ formatAmount(row.totalCost) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="light">{{ getProductionInboundStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="执行时间" min-width="175">
            <template #default="{ row }">{{ row.executedTime ? formatDate(row.executedTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <el-button link type="primary" @click="openDetail(row)">查看</el-button>
                <el-button
                  v-if="canExecute(row.status)"
                  link
                  type="success"
                  :loading="operatingId === row.id"
                  @click="handleExecute(row)"
                >
                  执行
                </el-button>
                <el-button
                  v-if="canRevert(row.status)"
                  link
                  type="danger"
                  :loading="operatingId === row.id"
                  @click="handleRevert(row)"
                >
                  反执行
                </el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无自制入库单" :image-size="72" />
          </template>
        </el-table>
      </div>
      <div class="flex justify-end border-t border-[var(--erp-slate-100)] px-5 py-3">
        <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </section>

    <el-drawer v-model="detailVisible" :with-header="false" size="480px" class="production-inbound-detail">
      <div v-if="currentRow" class="flex h-full flex-col bg-[var(--erp-slate-50)]">
        <div class="bg-[var(--erp-slate-800)] px-5 py-5 text-white">
          <div class="font-mono text-base">{{ currentRow.no }}</div>
          <div class="mt-2 flex items-center justify-between gap-3">
            <span class="text-sm text-[var(--erp-slate-200)]">{{ currentRow.productionOrderNo || '-' }}</span>
            <el-tag :type="getStatusTagType(currentRow.status)" effect="light">{{ getProductionInboundStatusLabel(currentRow.status) }}</el-tag>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto p-4">
          <section class="erp-card">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="质检单">{{ currentRow.finishQualityNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产品 ID">{{ currentRow.productId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="仓库">{{ currentRow.warehouseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="入库数量">{{ formatCount(currentRow.inboundQty) }}</el-descriptions-item>
              <el-descriptions-item label="单位成本">{{ formatAmount(currentRow.unitCost) }}</el-descriptions-item>
              <el-descriptions-item label="入库成本">{{ formatAmount(currentRow.totalCost) }}</el-descriptions-item>
              <el-descriptions-item label="执行时间">
                {{ currentRow.executedTime ? formatDate(currentRow.executedTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="备注">{{ currentRow.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </section>
        </div>
        <div class="flex justify-end gap-2 border-t border-[var(--erp-slate-200)] bg-white px-4 py-3">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="canExecute(currentRow.status)"
            type="primary"
            :loading="operatingId === currentRow.id"
            @click="handleExecute(currentRow)"
          >执行</el-button>
          <el-button
            v-if="canRevert(currentRow.status)"
            type="danger"
            :loading="operatingId === currentRow.id"
            @click="handleRevert(currentRow)"
          >反执行</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'
import {
  ProductionInboundApi,
  type ProductionInboundPageReqVO,
  type ProductionInboundVO
} from '@/api/erp/mrp/production-inbound'
import {
  canExecuteProductionInbound,
  canRevertProductionInbound,
  getProductionInboundStatusLabel,
  PRODUCTION_INBOUND_STATUS
} from './productionInbound.helpers'

defineOptions({ name: 'ErpProductionInboundPage' })

const message = useMessage()
const queryFormRef = ref()
const listLoading = ref(false)
const operatingId = ref<number>()
const list = ref<ProductionInboundVO[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentRow = ref<ProductionInboundVO>()
const canQuery = checkPermi(['erp:production-inbound:query'])
const canUpdate = checkPermi(['erp:production-inbound:update'])

const queryParams = reactive<ProductionInboundPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  finishQualityId: undefined,
  productionOrderId: undefined,
  status: undefined
})

const statusOptions = [
  { label: '待入库', value: PRODUCTION_INBOUND_STATUS.PENDING },
  { label: '已入库', value: PRODUCTION_INBOUND_STATUS.EXECUTED },
  { label: '已作废', value: PRODUCTION_INBOUND_STATUS.CANCELED }
]

const formatCount = (value?: number) => {
  if (value == null) return '-'
  return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 3 })
}

const formatAmount = (value?: number) => {
  if (value == null) return '-'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getStatusTagType = (status?: number) => {
  if (status === PRODUCTION_INBOUND_STATUS.PENDING) return 'warning'
  if (status === PRODUCTION_INBOUND_STATUS.EXECUTED) return 'success'
  return 'info'
}

const canExecute = (status?: number) => canUpdate && canExecuteProductionInbound(status)
const canRevert = (status?: number) => canUpdate && canRevertProductionInbound(status)

const getList = async () => {
  if (!canQuery) return
  listLoading.value = true
  try {
    const data = await ProductionInboundApi.getProductionInboundPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    listLoading.value = false
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

const openDetail = (row: ProductionInboundVO) => {
  currentRow.value = row
  detailVisible.value = true
}

const refreshAfterOperation = async () => {
  await getList()
  if (currentRow.value?.id) {
    currentRow.value = list.value.find((item) => item.id === currentRow.value?.id)
  }
}

const handleExecute = async (row: ProductionInboundVO) => {
  if (!row.id || !canExecute(row.status)) return
  await message.confirm(`确认执行入库单 ${row.no || row.id} 吗？`)
  operatingId.value = row.id
  try {
    await ProductionInboundApi.executeProductionInbound(row.id)
    message.success('执行成功')
    await refreshAfterOperation()
  } finally {
    operatingId.value = undefined
  }
}

const handleRevert = async (row: ProductionInboundVO) => {
  if (!row.id || !canRevert(row.status)) return
  await message.confirm(`确认反执行入库单 ${row.no || row.id} 吗？`)
  operatingId.value = row.id
  try {
    await ProductionInboundApi.revertProductionInbound(row.id)
    message.success('反执行成功')
    await refreshAfterOperation()
  } finally {
    operatingId.value = undefined
  }
}

onMounted(() => {
  getList()
})
</script>
