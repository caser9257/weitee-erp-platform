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
      <el-table-column label="累计已领" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.issuedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="累计已退" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.returnedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="净领数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.netIssuedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="默认仓库" prop="supplyWarehouseName" min-width="140" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="Number(row.netIssuedQty || 0) <= 0"
            @click="openReturnDialog(row)"
          >
            退料
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <ContentWrap v-if="canQueryReturnHistory" title="退料历史">
    <el-table v-loading="historyLoading" :data="historyList" border stripe>
      <el-table-column label="退料单号" prop="returnNo" min-width="180" />
      <el-table-column label="工单编号" prop="productionOrderId" min-width="120" />
      <el-table-column label="退料时间" prop="returnTime" min-width="180" />
      <el-table-column label="状态" min-width="100" align="center">
        <template #default="{ row }"><el-tag type="success">{{ row.status === 20 ? '已完成' : '-' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
    </el-table>
    <Pagination
      :total="historyTotal"
      v-model:page="historyQuery.pageNo"
      v-model:limit="historyQuery.pageSize"
      @pagination="getHistoryList"
    />
  </ContentWrap>

  <ReturnBatchDialog ref="returnDialogRef" @success="handleReturnSuccess" />
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { erpCountInputFormatter } from '@/utils'
import { ProductionOrderApi, ProductionOrderVO } from '@/api/erp/mrp/production-order'
import { ProductionMaterialApi, ProductionMaterialVO } from '@/api/erp/mrp/production-material'
import { ProductionReturnApi, ProductionReturnVO } from '@/api/erp/manufacturing/material-return'
import { hasPermission } from '@/directives/permission/hasPermi'
import ReturnBatchDialog from './ReturnBatchDialog.vue'

defineOptions({ name: 'ErpManufacturingMaterialReturn' })

type ElementTagType = 'success' | 'warning' | 'primary' | 'info' | 'danger'

const queryFormRef = ref()
const returnDialogRef = ref<InstanceType<typeof ReturnBatchDialog>>()

const orderLoading = ref(false)
const materialLoading = ref(false)
const historyLoading = ref(false)
const total = ref(0)
const historyTotal = ref(0)
const currentOrderId = ref<number>()
const orderList = ref<ProductionOrderVO[]>([])
const materialList = ref<ProductionMaterialVO[]>([])
const historyList = ref<ProductionReturnVO[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: undefined as string | undefined,
  status: undefined as number | undefined
})

const historyQuery = reactive({ pageNo: 1, pageSize: 10 })

const statusOptions = [
  { label: '已创建', value: 0 },
  { label: '已下达', value: 10 },
  { label: '已完工', value: 20 },
  { label: '已关闭', value: 30 }
]

const canQueryReturnHistory = hasPermission(['erp:production-material-return:query'])

const resolveStatusLabel = (status?: number) => {
  if (status === 0) return '已创建'
  if (status === 10) return '已下达'
  if (status === 20) return '已完工'
  if (status === 30) return '已关闭'
  return '-'
}

const resolveStatusType = (status?: number): ElementTagType => {
  if (status === 10) return 'success'
  if (status === 20) return 'warning'
  if (status === 30) return 'info'
  return 'info'
}

const getOrderList = async () => {
  orderLoading.value = true
  try {
    const data = await ProductionOrderApi.getProductionOrderPage(queryParams)
    orderList.value = data.list || []
    total.value = data.total || 0
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

const handleQuery = async () => {
  queryParams.pageNo = 1
  currentOrderId.value = undefined
  materialList.value = []
  await getOrderList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await handleQuery()
}

const handleSelectOrder = async (row?: ProductionOrderVO) => {
  currentOrderId.value = row?.id
  await loadMaterials(row?.id)
}

const openReturnDialog = (row: ProductionMaterialVO) => {
  if (!currentOrderId.value) return
  returnDialogRef.value?.open(row, currentOrderId.value)
}

const handleReturnSuccess = async () => {
  await loadMaterials(currentOrderId.value)
  await getHistoryList()
}

const getHistoryList = async () => {
  historyLoading.value = true
  try {
    const data = await ProductionReturnApi.getProductionReturnPage(historyQuery)
    historyList.value = data.list || []
    historyTotal.value = data.total || 0
  } finally {
    historyLoading.value = false
  }
}

onMounted(() => {
  getOrderList()
  if (canQueryReturnHistory) {
    getHistoryList()
  }
})
</script>
