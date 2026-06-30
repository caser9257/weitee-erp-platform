<template>
  <div class="min-h-full bg-slate-50">
    <div class="flex w-full flex-col gap-4 px-3 py-3 sm:px-4 lg:px-5 xl:px-6">
      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex flex-col gap-4 bg-gradient-to-r from-slate-800 via-slate-700 to-slate-800 p-5 text-white lg:flex-row lg:items-center lg:justify-between">
          <div class="flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 text-white">
              <Icon icon="ep:box" class="text-lg" />
            </div>
            <div class="space-y-1">
              <h1 class="text-2xl font-semibold tracking-tight text-white">委外入库单</h1>
            </div>
          </div>

          <div class="grid w-full gap-3 sm:grid-cols-2 lg:grid-cols-4 lg:max-w-4xl">
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">总记录</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ total }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">当前页</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ queryParams.pageNo }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">已完成</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ statusCounts.completed }}</div>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm backdrop-blur-sm">
              <div class="text-xs text-slate-200">已关闭</div>
              <div class="mt-2 text-2xl font-semibold text-white">{{ statusCounts.closed }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 px-5 py-4">
          <div class="text-base font-semibold text-slate-800">查询条件</div>
        </div>
        <div class="px-4 py-4 sm:px-5">
          <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="erp-toolbar-form">
            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <el-form-item label="入库单号" prop="inboundNo">
                <el-input
                  v-model="queryParams.inboundNo"
                  placeholder="请输入入库单号"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="委外订单ID" prop="orderId">
                <el-input-number
                  v-model="queryParams.orderId"
                  :min="1"
                  :precision="0"
                  :controls="false"
                  placeholder="请输入委外订单ID"
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <div class="flex items-end justify-end gap-2">
                <el-button type="primary" :loading="loadingList" @click="handleQuery">
                  <Icon icon="ep:search" class="mr-5px" /> 搜索
                </el-button>
                <el-button :disabled="loadingList" @click="resetQuery">
                  <Icon icon="ep:refresh" class="mr-5px" /> 重置
                </el-button>
              </div>
            </div>
          </el-form>
        </div>
      </section>

      <section class="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex flex-col gap-3 border-b border-slate-100 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <div class="text-base font-semibold text-slate-800">委外入库列表</div>
          </div>
          <div class="flex flex-wrap gap-2">
            <el-tag v-if="queryParams.status !== undefined" effect="light" type="info">
              当前筛选：{{ resolveStatusLabel(queryParams.status) }}
            </el-tag>
            <el-tag v-if="queryParams.inboundNo" effect="light" type="warning">
              单号：{{ queryParams.inboundNo }}
            </el-tag>
          </div>
        </div>

        <div class="px-4 py-4 sm:px-5">
          <el-table
            v-loading="loadingList"
            :data="list"
            border
            stripe
            class="erp-table"
            table-layout="fixed"
            :empty-text="'暂无委外入库单'"
          >
            <el-table-column label="入库单信息" min-width="220" fixed="left">
              <template #default="{ row }">
                <div class="space-y-1">
                  <div class="flex items-center gap-1.5">
                    <span class="shrink-0 rounded-full bg-blue-50 px-1.5 py-0.5 text-[11px] font-medium text-blue-600 whitespace-nowrap">
                      入库单号
                    </span>
                    <span class="min-w-0 flex-1 truncate font-mono text-slate-800" :title="row.inboundNo || '-'">
                      {{ row.inboundNo || '-' }}
                    </span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <span class="shrink-0 rounded-full bg-emerald-50 px-1.5 py-0.5 text-[11px] font-medium text-emerald-600 whitespace-nowrap">
                      委外订单号
                    </span>
                    <span class="min-w-0 flex-1 truncate font-mono text-slate-600" :title="row.orderNo || '-'">
                      {{ row.orderNo || '-' }}
                    </span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <span class="shrink-0 rounded-full bg-slate-100 px-1.5 py-0.5 text-[11px] font-medium text-slate-600 whitespace-nowrap">
                      委外入库批次
                    </span>
                    <span class="min-w-0 flex-1 truncate font-mono text-slate-500" :title="row.batchNo || '-'">
                      {{ row.batchNo || '-' }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="仓库" min-width="120">
              <template #default="{ row }">
                <span class="text-slate-700">{{ row.warehouseName || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量 / 成本" min-width="180" align="right">
              <template #default="{ row }">
                <div class="space-y-1 text-right">
                  <div class="font-mono text-slate-800">{{ formatNumber(row.inboundQty) }}</div>
                  <div class="text-xs text-slate-500">总成本 {{ formatNumber(row.totalCost) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="成本拆分" min-width="200">
              <template #default="{ row }">
                <div class="flex flex-wrap gap-2">
                  <span class="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-600">
                    材料 {{ formatNumber(row.materialCost) }}
                  </span>
                  <span class="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-600">
                    加工 {{ formatNumber(row.processFee) }}
                  </span>
                  <span class="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">
                    单位 {{ formatNumber(row.unitCost) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="resolveStatusTagType(row.status)" effect="light">{{ resolveStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="入库时间" min-width="160">
              <template #default="{ row }">{{ row.inboundTime ? formatDate(row.inboundTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="!row.id" @click="openPrint(row.id!)">打印</el-button>
              </template>
            </el-table-column>
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

    <OutsourceInboundPrintDialog ref="printDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { OutsourceInboundApi, OutsourceInboundVO } from '@/api/erp/mrp/outsource-inbound'
import OutsourceInboundPrintDialog from './OutsourceInboundPrintDialog.vue'
import './style.css'

defineOptions({ name: 'ErpMrpOutsourceInboundPage' })

const queryFormRef = ref()
const printDialogRef = ref<InstanceType<typeof OutsourceInboundPrintDialog>>()

const loadingList = ref(false)
const list = ref<OutsourceInboundVO[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  inboundNo: undefined as string | undefined,
  orderId: undefined as number | undefined,
  status: undefined as number | undefined
})

const statusOptions = [
  { label: '已完成', value: 20 },
  { label: '已关闭', value: 30 }
]

const statusTextMap: Record<number, string> = {
  20: '已完成',
  30: '已关闭'
}

const statusCounts = computed(() =>
  list.value.reduce(
    (acc, item) => {
      if (item.status === 20) {
        acc.completed += 1
      }
      if (item.status === 30) {
        acc.closed += 1
      }
      return acc
    },
    { completed: 0, closed: 0 }
  )
)

const resolveStatusLabel = (status?: number) => (status == null ? '-' : statusTextMap[status] || '-')

const resolveStatusTagType = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 30) return 'info'
  return 'warning'
}

const formatNumber = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const getList = async () => {
  loadingList.value = true
  try {
    const data = await OutsourceInboundApi.getOutsourceInboundPage(queryParams)
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

const openPrint = (id: number) => {
  printDialogRef.value?.open(id)
}

onMounted(() => {
  getList()
})
</script>
