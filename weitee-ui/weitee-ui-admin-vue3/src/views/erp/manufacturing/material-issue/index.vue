<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="82px" class="-mb-15px">
      <el-form-item label="工单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入工单号"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="请选择状态"
          class="!w-220px"
        >
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap title="生产工单">
    <el-table
      v-loading="orderLoading"
      :data="orderList"
      border
      stripe
      highlight-current-row
      @current-change="handleSelectOrder"
    >
      <el-table-column label="工单号" prop="orderNo" min-width="160" />
      <el-table-column label="成品" prop="productName" min-width="180" />
      <el-table-column label="计划数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.planQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="完工数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.finishedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveStatusType(row.status)">{{ resolveStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划开始" min-width="160">
        <template #default="{ row }">
          {{ row.planStartTime ? formatDate(row.planStartTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="计划结束" min-width="160">
        <template #default="{ row }">
          {{ row.planEndTime ? formatDate(row.planEndTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getOrderList"
    />
  </ContentWrap>

  <ContentWrap title="工单物料">
    <el-empty v-if="!currentOrderId" description="请选择一个工单查看物料" />
    <el-table v-else v-loading="materialLoading" :data="materialList" border stripe>
      <el-table-column label="物料" min-width="220">
        <template #default="{ row }">
          <div class="font-600">{{ row.materialName || '-' }}</div>
          <div class="text-[var(--el-text-color-secondary)]">
            {{ row.materialCode || '-' }} / {{ row.unitName || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="应领数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.requiredQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="累计已领" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.issuedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="累计已退" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.returnedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="待领数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.remainingIssueQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="默认仓库" prop="supplyWarehouseName" min-width="140" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="Number(row.remainingIssueQty || 0) <= 0"
            @click="openIssueDialog(row)"
          >
            领料
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <ContentWrap>
    <div class="mb-12px flex flex-wrap items-center justify-between gap-3">
      <div class="text-base font-medium text-slate-800">领料记录</div>
      <el-button type="primary" plain :disabled="!latestIssueId" @click="openPrintDialog(latestIssueId)">
        打印最新领料
      </el-button>
    </div>

    <el-empty v-if="!currentOrderId" description="请选择一个工单查看领料记录" />
    <template v-else>
      <el-alert
        v-if="issueLoadError"
        class="mb-12px"
        type="error"
        :title="issueLoadError"
        show-icon
        :closable="false"
      >
        <template #default>
          <div class="mt-8px">
            <el-button type="primary" plain :disabled="issueLoading" @click="retryIssueList">重试加载</el-button>
          </div>
        </template>
      </el-alert>

      <el-table v-else v-loading="issueLoading" :data="issueList" border stripe>
        <el-table-column label="领料单号" min-width="160" prop="issueNo" />
        <el-table-column label="领料时间" min-width="160">
          <template #default="{ row }">
            {{ row.issueTime ? formatDate(row.issueTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="领料金额" min-width="120" align="right">
          <template #default="{ row }">{{ erpPriceInputFormatter(row.issueAmount || 0) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="resolveIssueStatusType(row.status)">{{ row.statusName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" min-width="120" prop="creatorName" />
        <el-table-column label="备注" min-width="180" prop="remark" />
        <el-table-column label="操作" min-width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPrintDialog(row.id)">打印</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-if="issueTotal > 0"
        :total="issueTotal"
        v-model:page="issueQueryParams.pageNo"
        v-model:limit="issueQueryParams.pageSize"
        @pagination="getIssueList"
      />
      <el-empty v-else-if="!issueLoading" description="暂无领料记录" />
    </template>
  </ContentWrap>

  <IssueBatchDialog ref="issueDialogRef" @success="handleIssueSuccess" />
  <IssuePrintDialog ref="printDialogRef" />
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter, erpPriceInputFormatter } from '@/utils'
import { ProductionOrderApi, ProductionOrderVO } from '@/api/erp/mrp/production-order'
import { ProductionMaterialApi, ProductionMaterialVO } from '@/api/erp/mrp/production-material'
import { ProductionIssueApi, ProductionIssueVO } from '@/api/erp/manufacturing/material-issue'
import IssueBatchDialog from './IssueBatchDialog.vue'
import IssuePrintDialog from './IssuePrintDialog.vue'

defineOptions({ name: 'ErpManufacturingMaterialIssue' })

const queryFormRef = ref()
const issueDialogRef = ref<InstanceType<typeof IssueBatchDialog>>()

const orderLoading = ref(false)
const materialLoading = ref(false)
const issueLoading = ref(false)
const total = ref(0)
const currentOrderId = ref<number>()
const printDialogRef = ref<InstanceType<typeof IssuePrintDialog>>()
const orderList = ref<ProductionOrderVO[]>([])
const materialList = ref<ProductionMaterialVO[]>([])
const issueList = ref<ProductionIssueVO[]>([])
const issueTotal = ref(0)
const issueLoadError = ref('')
const latestIssueId = computed(() => {
  if (issueLoading.value) {
    return undefined
  }
  return issueList.value[0]?.id
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: undefined as string | undefined,
  status: undefined as number | undefined
})

const issueQueryParams = reactive({
  pageNo: 1,
  pageSize: 5,
  productionOrderId: undefined as number | undefined
})

const statusOptions = [
  { label: '已创建', value: 0 },
  { label: '已下达', value: 10 },
  { label: '已完工', value: 20 },
  { label: '已关闭', value: 30 }
]

const resolveStatusLabel = (status?: number) => {
  if (status === 0) return '已创建'
  if (status === 10) return '已下达'
  if (status === 20) return '已完工'
  if (status === 30) return '已关闭'
  return '-'
}

const resolveStatusType = (status?: number) => {
  if (status === 10) return 'success'
  if (status === 20) return 'warning'
  if (status === 30) return 'info'
  return ''
}

const resolveIssueStatusType = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 30) return 'info'
  return 'warning'
}

const getOrderList = async () => {
  orderLoading.value = true
  try {
    const data = await ProductionOrderApi.getProductionOrderPage(queryParams)
    orderList.value = data.list || []
    total.value = data.total || 0
    if (!orderList.value.length) {
      currentOrderId.value = undefined
      materialList.value = []
      issueList.value = []
      issueTotal.value = 0
      issueLoadError.value = ''
      return
    }
    const hasCurrentOrder = currentOrderId.value && orderList.value.some((item) => item.id === currentOrderId.value)
    if (!hasCurrentOrder) {
      await handleSelectOrder(orderList.value[0])
    }
  } finally {
    orderLoading.value = false
  }
}

const loadMaterials = async (orderId?: number) => {
  if (!orderId) {
    materialList.value = []
    return
  }
  materialLoading.value = true
  try {
    materialList.value = await ProductionMaterialApi.getProductionMaterialList(orderId)
  } finally {
    materialLoading.value = false
  }
}

const getIssueList = async () => {
  if (!currentOrderId.value) {
    issueList.value = []
    issueTotal.value = 0
    issueLoadError.value = ''
    return
  }
  issueLoading.value = true
  issueLoadError.value = ''
  try {
    issueQueryParams.productionOrderId = currentOrderId.value
    const data = await ProductionIssueApi.getProductionIssuePage(issueQueryParams)
    issueList.value = data.list || []
    issueTotal.value = data.total || 0
  } catch {
    issueList.value = []
    issueTotal.value = 0
    issueLoadError.value = '领料记录加载失败，请重试。'
  } finally {
    issueLoading.value = false
  }
}

const retryIssueList = async () => {
  await getIssueList()
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  currentOrderId.value = undefined
  materialList.value = []
  issueList.value = []
  issueTotal.value = 0
  issueLoadError.value = ''
  await getOrderList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const handleSelectOrder = async (row?: ProductionOrderVO) => {
  currentOrderId.value = row?.id
  await loadMaterials(row?.id)
  issueQueryParams.pageNo = 1
  await getIssueList()
}

const openIssueDialog = (row: ProductionMaterialVO) => {
  if (!currentOrderId.value) return
  issueDialogRef.value?.open(row, currentOrderId.value)
}

const openPrintDialog = (issueId?: number) => {
  if (!issueId) return
  printDialogRef.value?.open(issueId)
}

const handleIssueSuccess = async (issueId: number) => {
  await loadMaterials(currentOrderId.value)
  await getOrderList()
  await getIssueList()
  await printDialogRef.value?.open(issueId)
}

onMounted(() => {
  getOrderList()
})
</script>
