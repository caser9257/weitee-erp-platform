<template>
  <div class="min-h-full bg-slate-50">
    <div class="flex w-full flex-col gap-4 px-3 py-3 sm:px-4 lg:px-5 xl:px-6">
      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div
          class="flex flex-col gap-4 bg-gradient-to-r from-slate-800 via-slate-700 to-slate-800 p-5 text-white lg:flex-row lg:items-center lg:justify-between"
        >
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 text-white">
              <Icon icon="ep:box" class="text-lg" />
            </div>
            <div class="space-y-1">
              <h1 class="text-2xl font-semibold tracking-tight text-white">自制入库单</h1>
              <p class="text-sm text-slate-200">
                承接成品质检后的正式入库执行，库存增加与成本快照在这里闭环。
              </p>
            </div>
          </div>

          <div class="grid w-full gap-3 sm:grid-cols-2 lg:max-w-4xl lg:grid-cols-4">
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">当前记录</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ total }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">待入库</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ statusCounts.pending }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">已入库</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ statusCounts.executed }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">已作废</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ statusCounts.canceled }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 px-5 py-4">
          <div class="text-base font-semibold text-slate-800">查询条件</div>
        </div>
        <div class="px-4 py-4 sm:px-5">
          <el-form ref="queryFormRef" :model="queryParams" label-width="92px" @submit.prevent>
            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <el-form-item label="入库单号" prop="no">
                <el-input
                  v-model="queryParams.no"
                  placeholder="请输入自制入库单号"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="质检单 ID" prop="finishQualityId">
                <el-input-number
                  v-model="queryParams.finishQualityId"
                  :min="1"
                  :precision="0"
                  :controls="false"
                  placeholder="请输入质检单 ID"
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="工单 ID" prop="productionOrderId">
                <el-input-number
                  v-model="queryParams.productionOrderId"
                  :min="1"
                  :precision="0"
                  :controls="false"
                  placeholder="请输入生产工单 ID"
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="入库状态" prop="status">
                <el-select v-model="queryParams.status" clearable placeholder="请选择入库状态">
                  <el-option
                    v-for="item in statusOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="mt-2 flex justify-end gap-2">
              <el-button type="primary" :loading="loadingList" @click="handleQuery">
                <Icon icon="ep:search" class="mr-5px" />
                查询
              </el-button>
              <el-button :disabled="loadingList" @click="resetQuery">
                <Icon icon="ep:refresh" class="mr-5px" />
                重置
              </el-button>
            </div>
          </el-form>
        </div>
      </section>

      <section class="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div
          class="flex flex-col gap-3 border-b border-slate-100 px-5 py-4 lg:flex-row lg:items-center lg:justify-between"
        >
          <div>
            <div class="text-base font-semibold text-slate-800">自制入库台账</div>
            <div class="mt-1 text-sm text-slate-500">按质检单生成，执行后正式增加库存。</div>
          </div>
          <div class="flex flex-wrap gap-2">
            <el-tag v-if="routeFilterTag" effect="light" type="primary">{{ routeFilterTag }}</el-tag>
            <el-tag v-if="queryParams.status !== undefined" effect="light" type="warning">
              当前状态：{{ resolveProductionInboundStatusLabel(queryParams.status) }}
            </el-tag>
          </div>
        </div>

        <div class="overflow-x-auto px-4 py-4 sm:px-5">
          <el-table
            v-loading="loadingList"
            :data="list"
            stripe
            border
            table-layout="fixed"
            class="erp-table"
            :empty-text="'暂无自制入库单'"
          >
            <el-table-column label="单据信息" min-width="260" fixed="left">
              <template #default="{ row }">
                <div class="space-y-1">
                  <div class="flex items-center gap-2">
                    <span
                      class="shrink-0 rounded-full bg-blue-50 px-1.5 py-0.5 text-[11px] font-medium text-blue-600"
                    >
                      入库单
                    </span>
                    <span class="min-w-0 truncate font-mono text-slate-800" :title="row.no || '-'">
                      {{ row.no || '-' }}
                    </span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span
                      class="shrink-0 rounded-full bg-emerald-50 px-1.5 py-0.5 text-[11px] font-medium text-emerald-600"
                    >
                      质检单
                    </span>
                    <span
                      class="min-w-0 truncate font-mono text-slate-600"
                      :title="row.finishQualityNo || String(row.finishQualityId || '-')"
                    >
                      {{ row.finishQualityNo || row.finishQualityId || '-' }}
                    </span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span
                      class="shrink-0 rounded-full bg-slate-100 px-1.5 py-0.5 text-[11px] font-medium text-slate-600"
                    >
                      生产工单
                    </span>
                    <span
                      class="min-w-0 truncate font-mono text-slate-500"
                      :title="row.productionOrderNo || String(row.productionOrderId || '-')"
                    >
                      {{ row.productionOrderNo || row.productionOrderId || '-' }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="仓库" min-width="120">
              <template #default="{ row }">
                <span class="text-slate-700">{{ row.warehouseName || row.warehouseId || '-' }}</span>
              </template>
            </el-table-column>

            <el-table-column label="数量 / 成本" min-width="220" align="right">
              <template #default="{ row }">
                <div class="space-y-1 text-right">
                  <div class="font-mono text-slate-800">{{ formatQty(row.inboundQty) }}</div>
                  <div class="text-xs text-slate-500">单位成本 {{ formatMoney(row.unitCost) }}</div>
                  <div class="text-xs font-medium text-rose-600">入库成本 {{ formatMoney(row.totalCost) }}</div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="成本提示" min-width="220">
              <template #default="{ row }">
                <div class="rounded-xl bg-slate-50 px-3 py-2 text-sm text-slate-600">
                  {{ getProductionInboundCostHint(row) }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="状态" min-width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="resolveProductionInboundStatusTagType(row.status)" effect="light">
                  {{ resolveProductionInboundStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="入库时间" min-width="170">
              <template #default="{ row }">
                {{ row.inboundTime ? formatDate(row.inboundTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>

            <el-table-column label="创建时间" min-width="170">
              <template #default="{ row }">
                {{ row.createTime ? formatDate(row.createTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>

            <el-table-column label="操作" min-width="260" fixed="right" align="center">
              <template #default="{ row }">
                <div class="flex flex-wrap justify-center gap-2">
                  <el-button
                    v-if="hasUpdatePermission && canExecuteProductionInbound(row)"
                    link
                    type="primary"
                    @click="handleExecute(row)"
                  >
                    执行入库
                  </el-button>
                  <el-button
                    v-if="hasUpdatePermission && canCancelProductionInbound(row)"
                    link
                    type="danger"
                    @click="handleCancel(row)"
                  >
                    作废
                  </el-button>
                  <el-button
                    v-if="hasUpdatePermission && canRevertProductionInbound(row)"
                    link
                    type="warning"
                    @click="handleRevert(row)"
                  >
                    反执行
                  </el-button>
                  <el-button
                    v-if="row.productionOrderId"
                    link
                    type="success"
                    @click="openCostDetailDrawer(row.productionOrderId, row.productionOrderNo)"
                  >
                    成本穿透
                  </el-button>
                </div>
              </template>
            </el-table-column>

            <template #empty>
              <el-empty description="暂无自制入库单">
                <template #image>
                  <div
                    class="flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400"
                  >
                    <Icon icon="ep:box" class="text-xl" />
                  </div>
                </template>
              </el-empty>
            </template>
          </el-table>
        </div>

        <div class="flex justify-end border-t border-slate-100 px-4 py-4 sm:px-5">
          <Pagination
            :total="total"
            v-model:page="queryParams.pageNo"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
          />
        </div>
      </section>
    </div>

    <ProductionCostDetailDrawer
      v-model="costDetailDrawerVisible"
      :production-order-id="costDetailProductionOrderId"
      :header-text="costDetailHeader"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'
import ProductionCostDetailDrawer from '@/views/erp/finance/cost/components/ProductionCostDetailDrawer.vue'
import {
  ProductionInboundApi,
  type ProductionInboundPageReqVO,
  type ProductionInboundVO
} from '@/api/erp/mrp/production-inbound'
import {
  PRODUCTION_INBOUND_STATUS,
  canCancelProductionInbound,
  canExecuteProductionInbound,
  canRevertProductionInbound,
  getProductionInboundCostHint,
  resolveProductionInboundStatusLabel,
  resolveProductionInboundStatusTagType
} from './productionInbound.helpers'

defineOptions({ name: 'ErpProductionInboundPage' })

const message = useMessage()
const route = useRoute()
const hasQueryPermission = checkPermi(['erp:production-inbound:query'])
const hasUpdatePermission = checkPermi(['erp:production-inbound:update'])

const queryFormRef = ref()
const loadingList = ref(false)
const list = ref<ProductionInboundVO[]>([])
const total = ref(0)

const costDetailDrawerVisible = ref(false)
const costDetailProductionOrderId = ref<number>()
const costDetailProductionOrderNo = ref('')

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

const statusCounts = computed(() =>
  list.value.reduce(
    (acc, item) => {
      if (item.status === PRODUCTION_INBOUND_STATUS.PENDING) {
        acc.pending += 1
      }
      if (item.status === PRODUCTION_INBOUND_STATUS.EXECUTED) {
        acc.executed += 1
      }
      if (item.status === PRODUCTION_INBOUND_STATUS.CANCELED) {
        acc.canceled += 1
      }
      return acc
    },
    { pending: 0, executed: 0, canceled: 0 }
  )
)

const routeFilterTag = computed(() => {
  if (queryParams.finishQualityId) {
    return `来自质检单 ${queryParams.finishQualityId}`
  }
  if (queryParams.productionOrderId) {
    return `聚焦工单 ${queryParams.productionOrderId}`
  }
  return ''
})

const costDetailHeader = computed(() =>
  costDetailProductionOrderNo.value
    ? `自制入库工单 ${costDetailProductionOrderNo.value}`
    : '自制入库成本明细'
)

const formatQty = (value?: number) => {
  if (value == null) return '-'
  const normalized = Number(value)
  return Number.isInteger(normalized)
    ? normalized.toLocaleString('zh-CN')
    : normalized.toLocaleString('zh-CN', { maximumFractionDigits: 3 })
}

const formatMoney = (value?: number) =>
  value == null
    ? '-'
    : Number(value).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })

const getList = async () => {
  if (!hasQueryPermission) return
  loadingList.value = true
  try {
    const data = await ProductionInboundApi.getProductionInboundPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
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

const openCostDetailDrawer = (productionOrderId?: number, productionOrderNo?: string) => {
  if (!productionOrderId) {
    return
  }
  costDetailProductionOrderId.value = productionOrderId
  costDetailProductionOrderNo.value = productionOrderNo || ''
  costDetailDrawerVisible.value = true
}

const handleExecute = async (row: ProductionInboundVO) => {
  if (!row.id) return
  await message.confirm(`确认执行入库单 ${row.no || row.id} 吗？`)
  await ProductionInboundApi.executeProductionInbound({ id: row.id })
  message.success('执行入库成功')
  await getList()
}

const handleCancel = async (row: ProductionInboundVO) => {
  if (!row.id) return
  await message.confirm(`确认作废入库单 ${row.no || row.id} 吗？`)
  await ProductionInboundApi.cancelProductionInbound({ id: row.id })
  message.success('作废入库单成功')
  await getList()
}

const handleRevert = async (row: ProductionInboundVO) => {
  if (!row.id) return
  await message.confirm(`确认反执行入库单 ${row.no || row.id} 吗？`)
  await ProductionInboundApi.revertProductionInbound({ id: row.id })
  message.success('反执行入库成功')
  await getList()
}

onMounted(() => {
  const finishQualityId = Number(route.query.finishQualityId || 0)
  if (finishQualityId > 0) {
    queryParams.finishQualityId = finishQualityId
  }
  const productionOrderId = Number(route.query.productionOrderId || 0)
  if (productionOrderId > 0) {
    queryParams.productionOrderId = productionOrderId
  }
  getList()
})

watch(costDetailDrawerVisible, (visible) => {
  if (!visible) {
    costDetailProductionOrderId.value = undefined
    costDetailProductionOrderNo.value = ''
  }
})
</script>
