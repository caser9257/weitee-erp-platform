<template>
  <div class="min-h-full bg-[var(--erp-slate-50)] p-3 sm:p-4 lg:p-5">
    <section class="erp-card mb-4">
      <div class="flex items-center justify-between gap-4 px-5 py-4">
        <h1 class="text-lg font-semibold text-[var(--erp-slate-800)]">生产工单</h1>
        <el-tag type="info" effect="light">{{ total }} 条</el-tag>
      </div>
    </section>

    <section class="erp-card mb-4">
      <div class="flex items-center justify-between border-b border-[var(--erp-slate-100)] px-5 py-3">
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">查询条件</h2>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="82px" class="px-5 pb-4 pt-4">
        <div class="grid gap-x-4 gap-y-2 md:grid-cols-2 xl:grid-cols-4">
          <el-form-item label="工单号" prop="orderNo">
            <el-input v-model="queryParams.orderNo" clearable placeholder="请输入工单号" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="产品 ID" prop="productId">
            <el-input-number
              v-model="queryParams.productId"
              :controls="false"
              :min="1"
              :precision="0"
              class="!w-full"
              placeholder="请输入产品 ID"
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
      <div class="flex items-center justify-between border-b border-[var(--erp-slate-100)] px-5 py-3">
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">工单列表</h2>
        <el-button v-if="canCreate" type="primary" @click="openCreate"><Icon icon="ep:plus" class="mr-1" />新增</el-button>
      </div>
      <div class="overflow-x-auto">
        <el-table v-loading="listLoading" :data="list" class="min-w-[920px]" table-layout="fixed">
          <el-table-column label="工单信息" min-width="225" fixed="left">
            <template #default="{ row }">
              <div class="space-y-1 py-1">
                <div class="font-mono text-sm text-[var(--erp-slate-800)]">{{ row.orderNo || '-' }}</div>
                <div class="truncate text-xs text-[var(--erp-slate-500)]" :title="row.productName || ''">
                  {{ row.productName || `产品 ${row.productId || '-'}` }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="计划数量" min-width="115" align="right">
            <template #default="{ row }"><span class="font-mono">{{ formatCount(row.planQty) }}</span></template>
          </el-table-column>
          <el-table-column label="完工数量" min-width="115" align="right">
            <template #default="{ row }"><span class="font-mono">{{ formatCount(row.finishedQty) }}</span></template>
          </el-table-column>
          <el-table-column label="计划时间" min-width="205">
            <template #default="{ row }">
              <div class="space-y-1 text-xs text-[var(--erp-slate-600)]">
                <div>{{ row.planStartTime ? formatDate(row.planStartTime, 'YYYY-MM-DD HH:mm') : '-' }}</div>
                <div>{{ row.planEndTime ? formatDate(row.planEndTime, 'YYYY-MM-DD HH:mm') : '-' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="来源" min-width="120">
            <template #default="{ row }">{{ row.sourceType || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="105" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="light">{{ getProductionOrderStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <el-button
                  v-if="canRelease(row.status)"
                  link
                  type="primary"
                  :loading="operatingId === row.id"
                  @click="handleRelease(row)"
                >下达</el-button>
                <el-button
                  v-if="canFinish(row.status)"
                  link
                  type="success"
                  :loading="operatingId === row.id"
                  @click="openFinish(row)"
                >完工</el-button>
                <el-button link type="primary" @click="openDetail(row)">查看</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty><el-empty description="暂无生产工单" :image-size="72" /></template>
        </el-table>
      </div>
      <div class="flex justify-end border-t border-[var(--erp-slate-100)] px-5 py-3">
        <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </section>

    <el-dialog v-model="createVisible" title="新增生产工单" width="560px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="96px">
        <el-form-item label="产品 ID" prop="productId"><el-input-number v-model="createForm.productId" :min="1" :precision="0" class="!w-full" /></el-form-item>
        <el-form-item label="计划数量" prop="planQty"><el-input-number v-model="createForm.planQty" :min="0.001" :precision="3" class="!w-full" /></el-form-item>
        <el-form-item label="计划开始" prop="planStartTime"><el-date-picker v-model="createForm.planStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="!w-full" /></el-form-item>
        <el-form-item label="计划结束" prop="planEndTime"><el-date-picker v-model="createForm.planEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="!w-full" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="createForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="finishVisible" title="生产完工" width="480px" destroy-on-close>
      <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="96px">
        <el-form-item label="工单号"><span class="font-mono">{{ finishOrder?.orderNo || '-' }}</span></el-form-item>
        <el-form-item label="完工数量" prop="finishedQty"><el-input-number v-model="finishForm.finishedQty" :min="0.001" :precision="3" class="!w-full" /></el-form-item>
        <el-form-item label="入库仓库 ID" prop="warehouseId"><el-input-number v-model="finishForm.warehouseId" :min="1" :precision="0" class="!w-full" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleFinish">确认完工</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :with-header="false" size="440px">
      <div v-if="detailOrder" class="flex h-full flex-col bg-[var(--erp-slate-50)]">
        <div class="bg-[var(--erp-slate-800)] px-5 py-5 text-white">
          <div class="font-mono text-base">{{ detailOrder.orderNo }}</div>
          <div class="mt-2 flex items-center justify-between gap-3">
            <span class="text-sm text-[var(--erp-slate-200)]">{{ detailOrder.productName || `产品 ${detailOrder.productId || '-'}` }}</span>
            <el-tag :type="getStatusTagType(detailOrder.status)" effect="light">{{ getProductionOrderStatusLabel(detailOrder.status) }}</el-tag>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto p-4">
          <section class="erp-card">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="计划数量">{{ formatCount(detailOrder.planQty) }}</el-descriptions-item>
              <el-descriptions-item label="完工数量">{{ formatCount(detailOrder.finishedQty) }}</el-descriptions-item>
              <el-descriptions-item label="计划开始">{{ detailOrder.planStartTime ? formatDate(detailOrder.planStartTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}</el-descriptions-item>
              <el-descriptions-item label="计划结束">{{ detailOrder.planEndTime ? formatDate(detailOrder.planEndTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}</el-descriptions-item>
              <el-descriptions-item label="来源">{{ detailOrder.sourceType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="备注">{{ detailOrder.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </section>
        </div>
        <div class="flex justify-end gap-2 border-t border-[var(--erp-slate-200)] bg-white px-4 py-3"><el-button @click="detailVisible = false">关闭</el-button></div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'
import {
  ProductionOrderApi,
  type ProductionOrderCreateReqVO,
  type ProductionOrderFinishReqVO,
  type ProductionOrderPageReqVO,
  type ProductionOrderVO
} from '@/api/erp/mrp/production-order'
import {
  canFinishProductionOrder,
  canReleaseProductionOrder,
  getProductionOrderStatusLabel,
  PRODUCTION_ORDER_STATUS
} from './workOrder.helpers'

defineOptions({ name: 'MesWorkOrder' })

const message = useMessage()
const queryFormRef = ref()
const createFormRef = ref()
const finishFormRef = ref()
const listLoading = ref(false)
const formLoading = ref(false)
const operatingId = ref<number>()
const list = ref<ProductionOrderVO[]>([])
const total = ref(0)
const createVisible = ref(false)
const finishVisible = ref(false)
const detailVisible = ref(false)
const finishOrder = ref<ProductionOrderVO>()
const detailOrder = ref<ProductionOrderVO>()
const canQuery = checkPermi(['erp:production-order:query'])
const canCreate = checkPermi(['erp:production-order:create'])
const canUpdate = checkPermi(['erp:production-order:update'])

const queryParams = reactive<ProductionOrderPageReqVO>({ pageNo: 1, pageSize: 10, orderNo: undefined, productId: undefined, status: undefined })
const createForm = reactive<ProductionOrderCreateReqVO>({ productId: undefined as unknown as number, planQty: undefined as unknown as number, planStartTime: '', planEndTime: '', remark: '' })
const finishForm = reactive<ProductionOrderFinishReqVO>({ id: 0, finishedQty: undefined as unknown as number, warehouseId: undefined as unknown as number })

const statusOptions = [
  { label: '待下达', value: PRODUCTION_ORDER_STATUS.CREATED },
  { label: '生产中', value: PRODUCTION_ORDER_STATUS.RELEASED },
  { label: '已完工', value: PRODUCTION_ORDER_STATUS.FINISHED },
  { label: '已关闭', value: PRODUCTION_ORDER_STATUS.CLOSED }
]
const createRules = { productId: [{ required: true, message: '请输入产品 ID', trigger: 'change' }], planQty: [{ required: true, message: '请输入计划数量', trigger: 'change' }], planStartTime: [{ required: true, message: '请选择计划开始时间', trigger: 'change' }], planEndTime: [{ required: true, message: '请选择计划结束时间', trigger: 'change' }] }
const finishRules = { finishedQty: [{ required: true, message: '请输入完工数量', trigger: 'change' }], warehouseId: [{ required: true, message: '请输入入库仓库 ID', trigger: 'change' }] }

const formatCount = (value?: number) => value == null ? '-' : Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 3 })
const getStatusTagType = (status?: number) => status === PRODUCTION_ORDER_STATUS.CREATED ? 'warning' : status === PRODUCTION_ORDER_STATUS.RELEASED ? 'primary' : status === PRODUCTION_ORDER_STATUS.FINISHED ? 'success' : 'info'
const canRelease = (status?: number) => canUpdate && canReleaseProductionOrder(status)
const canFinish = (status?: number) => canUpdate && canFinishProductionOrder(status)

const getList = async () => {
  if (!canQuery) return
  listLoading.value = true
  try {
    const data = await ProductionOrderApi.getProductionOrderPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    listLoading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }
const openDetail = (row: ProductionOrderVO) => { detailOrder.value = row; detailVisible.value = true }
const openCreate = () => { createForm.productId = undefined as unknown as number; createForm.planQty = undefined as unknown as number; createForm.planStartTime = ''; createForm.planEndTime = ''; createForm.remark = ''; createVisible.value = true }
const openFinish = (row: ProductionOrderVO) => { finishOrder.value = row; finishForm.id = row.id; finishForm.finishedQty = Number(row.planQty || 0); finishForm.warehouseId = undefined as unknown as number; finishVisible.value = true }
const handleCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  formLoading.value = true
  try { await ProductionOrderApi.createProductionOrder({ ...createForm, remark: createForm.remark?.trim() || undefined }); message.success('新增成功'); createVisible.value = false; await getList() } finally { formLoading.value = false }
}
const handleRelease = async (row: ProductionOrderVO) => {
  if (!row.id || !canRelease(row.status)) return
  await message.confirm(`确认下达工单 ${row.orderNo || row.id} 吗？`)
  operatingId.value = row.id
  try { await ProductionOrderApi.releaseProductionOrder(row.id); message.success('下达成功'); await getList() } finally { operatingId.value = undefined }
}
const handleFinish = async () => {
  const valid = await finishFormRef.value?.validate().catch(() => false)
  if (!valid) return
  formLoading.value = true
  try { await ProductionOrderApi.finishProductionOrder({ ...finishForm }); message.success('完工成功，已生成待质检记录'); finishVisible.value = false; await getList() } finally { formLoading.value = false }
}

onMounted(() => { getList() })
</script>
