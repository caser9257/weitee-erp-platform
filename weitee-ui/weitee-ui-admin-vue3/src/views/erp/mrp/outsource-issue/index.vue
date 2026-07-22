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
              <h1 class="text-2xl font-semibold tracking-tight text-white">委外发料单</h1>
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
              <el-form-item label="发料单号" prop="issueNo">
                <el-input
                  v-model="queryParams.issueNo"
                  placeholder="请输入发料单号"
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
              <el-form-item label="发料类型" prop="issueType">
                <el-select v-model="queryParams.issueType" clearable placeholder="请选择发料类型">
                  <el-option v-for="item in issueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <div class="flex items-end justify-end gap-2 md:col-span-2 xl:col-span-4">
                <el-button type="primary" :loading="loadingList" @click="handleQuery" v-hasPermi="['erp:outsource-issue:query']">
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
            <div class="text-base font-semibold text-slate-800">委外发料列表</div>
          </div>
          <div class="flex flex-wrap gap-2">
            <el-tag v-if="queryParams.issueType !== undefined" effect="light" type="warning">
              类型：{{ resolveIssueTypeLabel(queryParams.issueType) }}
            </el-tag>
            <el-tag v-if="queryParams.status !== undefined" effect="light" type="info">
              当前筛选：{{ resolveStatusLabel(queryParams.status) }}
            </el-tag>
            <el-tag v-if="queryParams.issueNo" effect="light" type="primary">单号：{{ queryParams.issueNo }}</el-tag>
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
            :empty-text="'暂无委外发料单'"
          >
            <el-table-column label="发料单信息" min-width="240" fixed="left">
              <template #default="{ row }">
                <div class="space-y-1">
                  <div class="flex items-center gap-1.5">
                    <span class="shrink-0 rounded-full bg-blue-50 px-1.5 py-0.5 text-[11px] font-medium text-blue-600 whitespace-nowrap">
                      发料单号
                    </span>
                    <span class="min-w-0 flex-1 truncate font-mono text-slate-800" :title="row.issueNo || '-'">
                      {{ row.issueNo || '-' }}
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
                      创建人
                    </span>
                    <span class="min-w-0 flex-1 truncate text-slate-500" :title="row.creatorName || '-'">
                      {{ row.creatorName || '-' }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="类型 / 状态" min-width="180">
              <template #default="{ row }">
                <div class="flex flex-wrap gap-2">
                  <span class="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-600">
                    {{ resolveIssueTypeLabel(row.issueType) }}
                  </span>
                  <el-tag :type="resolveStatusTagType(row.status)" effect="light">{{ resolveStatusLabel(row.status) }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="数量 / 金额" min-width="180" align="right">
              <template #default="{ row }">
                <div class="space-y-1 text-right">
                  <div class="font-mono text-slate-800">{{ formatCount(row.issueQty) }}</div>
                  <div class="text-xs text-slate-500">金额 {{ formatMoney(row.issueAmount) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="发料时间" min-width="170">
              <template #default="{ row }">{{ row.issueTime ? formatDate(row.issueTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}</template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">
                <span class="line-clamp-2 text-slate-600">{{ row.remark || '-' }}</span>
              </template>
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

    <OutsourceIssuePrintDialog ref="printDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpPriceInputFormatter } from '@/utils'
import { OutsourceIssueApi, type OutsourceIssuePageReqVO, type OutsourceIssueVO } from '@/api/erp/mrp/outsource-issue'
import OutsourceIssuePrintDialog from './OutsourceIssuePrintDialog.vue'
import '../outsource-inbound/style.css'

defineOptions({ name: 'ErpMrpOutsourceIssuePage' })

const OUTSOURCE_ISSUE_TYPE = {
  NORMAL: 10,
  SUPPLEMENT: 20
} as const

const queryFormRef = ref()
const printDialogRef = ref<InstanceType<typeof OutsourceIssuePrintDialog>>()

const loadingList = ref(false)
const list = ref<OutsourceIssueVO[]>([])
const total = ref(0)

const queryParams = reactive<OutsourceIssuePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  issueNo: undefined,
  orderId: undefined,
  issueType: undefined,
  status: undefined
})

const issueTypeOptions = [
  { label: '普通发料', value: OUTSOURCE_ISSUE_TYPE.NORMAL },
  { label: '补发料', value: OUTSOURCE_ISSUE_TYPE.SUPPLEMENT }
]

const statusOptions = [
  { label: '已完成', value: 20 },
  { label: '已关闭', value: 30 }
]

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

const resolveIssueTypeLabel = (issueType?: number) => {
  if (issueType === OUTSOURCE_ISSUE_TYPE.SUPPLEMENT) return '补发料'
  if (issueType === OUTSOURCE_ISSUE_TYPE.NORMAL) return '普通发料'
  return '普通发料'
}

const resolveStatusLabel = (status?: number) => {
  if (status === 20) return '已完成'
  if (status === 30) return '已关闭'
  return '-'
}

const resolveStatusTagType = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 30) return 'info'
  return 'warning'
}

const formatCount = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return '-'
  }
  return numberValue.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 3
  })
}

const formatMoney = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return '-'
  }
  return erpPriceInputFormatter(numberValue)
}

const getList = async () => {
  loadingList.value = true
  try {
    const data = await OutsourceIssueApi.getOutsourceIssuePage(queryParams)
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
