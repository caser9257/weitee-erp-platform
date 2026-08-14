<template>
  <section class="po-hero">
    <div class="po-hero__header">
      <div>
        <div class="po-hero__breadcrumb">生产执行 / 生产工单</div>
        <div class="po-page__title">生产工单</div>
      </div>
    </div>

    <div class="po-kpi-grid">
      <div class="po-kpi-card po-kpi-card--blue">
        <div>
          <div class="po-kpi-card__label">工单总数</div>
          <div class="po-kpi-card__value">{{ kpi.total }}</div>
        </div>
        <div class="po-kpi-card__suffix">单</div>
      </div>
      <div class="po-kpi-card po-kpi-card--amber">
        <div>
          <div class="po-kpi-card__label">已创建</div>
          <div class="po-kpi-card__value">{{ kpi.created }}</div>
        </div>
        <div class="po-kpi-card__suffix">单</div>
      </div>
      <div class="po-kpi-card po-kpi-card--teal">
        <div>
          <div class="po-kpi-card__label">已下达</div>
          <div class="po-kpi-card__value">{{ kpi.released }}</div>
        </div>
        <div class="po-kpi-card__suffix">单</div>
      </div>
      <div class="po-kpi-card po-kpi-card--green">
        <div>
          <div class="po-kpi-card__label">已完工</div>
          <div class="po-kpi-card__value">{{ kpi.finished }}</div>
        </div>
        <div class="po-kpi-card__suffix">单</div>
      </div>
    </div>
  </section>

  <ContentWrap class="po-page__filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="po-query">
      <div class="po-query__grid">
        <el-form-item label="工单号" prop="orderNo">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="请输入工单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" clearable filterable placeholder="请选择产品">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时间" prop="planTime">
          <el-date-picker
            v-model="queryParams.planTime"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <div class="po-query__footer">
        <div class="po-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="po-page__list-card">
    <div class="po-toolbar">
      <div class="po-toolbar__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:production-order:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增工单
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" :stripe="true" class="po-ledger">
      <template #empty>
        <div v-if="listLoadFailed" class="po-empty po-empty--error">
          <div class="po-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="po-empty__title">列表加载失败</div>
          <el-button type="primary" plain @click="getList" :disabled="loading">重试加载</el-button>
        </div>
        <div v-else class="po-empty">
          <div class="po-empty__icon">
            <Icon icon="ep:tickets" />
          </div>
          <div class="po-empty__title">暂无生产工单</div>
        </div>
      </template>
      <el-table-column label="工单信息" min-width="188">
        <template #default="{ row }">
          <div class="po-ledger__order">
            <div class="po-ledger__order-top">
              <div class="po-ledger__order-no">{{ row.orderNo || '-' }}</div>
              <span class="po-badge" :class="resolveStatusBadgeClass(row.status)">
                {{ resolveStatusLabel(row.status) }}
              </span>
            </div>
            <div class="po-ledger__order-meta">创建时间 {{ formatDateValue(row.createTime) }}</div>
            <div class="po-ledger__order-meta">来源 {{ resolveSourceLabel(row.sourceType) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="产品与批次" min-width="168">
        <template #default="{ row }">
          <div class="po-ledger__party">
            <div class="po-ledger__party-name" :title="row.productName || '-'">
              {{ row.productName || '-' }}
            </div>
            <div class="po-ledger__party-meta">
              <span class="po-ledger__party-tag">批次</span>
              <span class="po-ledger__party-text">{{ row.batchNo || '-' }}</span>
            </div>
            <div class="po-ledger__party-meta">
              <span class="po-ledger__party-tag">工艺路线</span>
              <span class="po-ledger__party-text">{{ row.routeVersion || '未绑定' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="计划" min-width="132" align="right">
        <template #default="{ row }">
          <div class="po-ledger__finance">
            <div class="po-ledger__finance-qty">{{ formatCount(row.planQty) }}</div>
            <div class="po-ledger__finance-meta"
              >计划开始 {{ formatDateValue(row.planStartTime) }}</div
            >
            <div class="po-ledger__finance-sub"
              >计划结束 {{ formatDateValue(row.planEndTime) }}</div
            >
          </div>
        </template>
      </el-table-column>
      <el-table-column label="完工进度" min-width="170">
        <template #default="{ row }">
          <div class="po-ledger__progress">
            <div class="po-ledger__progress-top">
              <span>完工数量</span>
              <strong>{{ formatCount(row.finishedQty) }} / {{ formatCount(row.planQty) }}</strong>
            </div>
            <el-progress
              :stroke-width="6"
              :show-text="false"
              :percentage="getFinishPercent(row)"
              :color="resolveFinishProgressColor(row)"
            />
            <div class="po-ledger__progress-summary">
              <span>待完工 {{ formatCount(getRemainingQty(row)) }}</span>
              <el-tag
                v-if="Number(row.scrapQty || 0) > 0"
                size="small"
                effect="light"
                type="danger"
              >
                报废 {{ formatCount(row.scrapQty) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="176" align="center">
        <template #default="{ row }">
          <div class="po-row-actions">
            <el-button v-if="canEdit(row)" link type="primary" @click="openForm('edit', row)">
              编辑
            </el-button>
            <el-button
              v-if="canRelease(row)"
              link
              type="primary"
              :loading="releasingIds.includes(row.id)"
              @click="handleRelease(row)"
            >
              下达
            </el-button>
            <el-button v-if="canFinish(row)" link type="success" @click="openFinishDialog(row)">
              完工
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog
    v-model="formVisible"
    :title="formMode === 'create' ? '新增生产工单' : '编辑生产工单'"
    width="640"
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
      <el-form-item label="产品" prop="productId">
        <el-select
          v-model="form.productId"
          filterable
          clearable
          placeholder="请选择产品"
          class="!w-100%"
          @change="handleProductChange"
        >
          <el-option
            v-for="item in productList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="工艺路线" prop="routeId">
        <el-select
          v-model="form.routeId"
          filterable
          clearable
          placeholder="请选择工艺路线"
          class="!w-100%"
        >
          <el-option
            v-for="item in routeList"
            :key="item.id"
            :label="`${item.routeName}（${item.routeCode}）`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="工作中心" prop="workCenterId">
        <el-select
          v-model="form.workCenterId"
          filterable
          clearable
          placeholder="请选择工作中心"
          class="!w-100%"
        >
          <el-option
            v-for="item in workCenterList"
            :key="item.id"
            :label="`${item.centerName}（${item.centerCode}）`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="form.batchNo" placeholder="请输入批次号" maxlength="64" />
      </el-form-item>
      <el-form-item label="计划数量" prop="planQty">
        <el-input-number
          v-model="form.planQty"
          controls-position="right"
          :min="0.000001"
          :step="getQuantityStep(form.productId)"
          :precision="getQuantityPrecision(form.productId)"
          class="!w-100%"
        />
      </el-form-item>
      <el-form-item label="计划开始时间" prop="planStartTime">
        <el-date-picker
          v-model="form.planStartTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择计划开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="计划结束时间" prop="planEndTime">
        <el-date-picker
          v-model="form.planEndTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择计划结束时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          maxlength="255"
          show-word-limit
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="formLoading" @click="handleFormSubmit">保存</el-button>
    </template>
  </Dialog>

  <Dialog v-model="finishVisible" title="完工生产工单" width="480">
    <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="110px">
      <el-form-item label="完工数量" prop="finishedQty">
        <el-input-number
          v-model="finishForm.finishedQty"
          controls-position="right"
          :min="0.000001"
          :max="Number(currentOrder?.planQty || 0)"
          :step="getQuantityStep(currentOrder?.productId)"
          :precision="getQuantityPrecision(currentOrder?.productId)"
          class="!w-100%"
        />
      </el-form-item>
      <el-form-item label="入库仓库" prop="warehouseId">
        <el-select
          v-model="finishForm.warehouseId"
          filterable
          clearable
          placeholder="请选择入库仓库"
          class="!w-100%"
        >
          <el-option
            v-for="item in warehouseList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="finishVisible = false">取消</el-button>
      <el-button type="success" :loading="finishLoading" @click="handleFinishSubmit">
        完工确认
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import { getProductQuantityPrecision, getProductQuantityStep } from '@/utils/erpQuantityPrecision'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { WarehouseApi } from '@/api/erp/stock/warehouse'
import { WorkCenterApi, WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { ProcessRouteApi, ProcessRouteVO } from '@/api/erp/manufacturing/process-route'
import {
  ProductionOrderApi,
  ProductionOrderFinishReqVO,
  ProductionOrderSaveReqVO,
  ProductionOrderVO
} from '@/api/erp/mrp/production-order'

defineOptions({ name: 'ErpProductionOrder' })

const PRODUCTION_ORDER_STATUS = {
  CREATED: 0,
  RELEASED: 10,
  FINISHED: 20,
  CLOSED: 30
} as const

const message = useMessage()

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const finishFormRef = ref<FormInstance>()

const loading = ref(false)
const formLoading = ref(false)
const finishLoading = ref(false)
const formVisible = ref(false)
const finishVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const listLoadFailed = ref(false)
const releasingIds = ref<number[]>([])

const list = ref<ProductionOrderVO[]>([])
const productList = ref<ProductVO[]>([])
const routeList = ref<ProcessRouteVO[]>([])
const workCenterList = ref<WorkCenterSimpleVO[]>([])
const warehouseList = ref<{ id: number; name: string }[]>([])
const total = ref(0)
const currentOrder = ref<ProductionOrderVO>()
const kpi = reactive({
  total: 0,
  created: 0,
  released: 0,
  finished: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: undefined as string | undefined,
  productId: undefined as number | undefined,
  status: undefined as number | undefined,
  planTime: undefined as string[] | undefined
})

const statusOptions = [
  { label: '已创建', value: PRODUCTION_ORDER_STATUS.CREATED },
  { label: '已下达', value: PRODUCTION_ORDER_STATUS.RELEASED },
  { label: '已完工', value: PRODUCTION_ORDER_STATUS.FINISHED },
  { label: '已关闭', value: PRODUCTION_ORDER_STATUS.CLOSED }
]

const emptyForm = (): ProductionOrderSaveReqVO => ({
  id: undefined,
  productId: undefined as unknown as number,
  routeId: undefined,
  workCenterId: undefined,
  batchNo: undefined,
  planQty: undefined as unknown as number,
  planStartTime: undefined as unknown as string,
  planEndTime: undefined as unknown as string,
  remark: undefined
})

const form = reactive<ProductionOrderSaveReqVO>(emptyForm())

const finishForm = reactive<ProductionOrderFinishReqVO>({
  id: undefined as unknown as number,
  finishedQty: undefined as unknown as number,
  warehouseId: undefined as unknown as number
})

const formRules: FormRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  planQty: [{ required: true, message: '请输入计划数量', trigger: 'blur' }],
  planStartTime: [{ required: true, message: '请选择计划开始时间', trigger: 'change' }],
  planEndTime: [{ required: true, message: '请选择计划结束时间', trigger: 'change' }]
}

const finishRules: FormRules = {
  finishedQty: [{ required: true, message: '请输入完工数量', trigger: 'blur' }],
  warehouseId: [{ required: true, message: '请选择入库仓库', trigger: 'change' }]
}

const getQuantityPrecision = (productId?: number) =>
  getProductQuantityPrecision(productList.value, productId)

const getQuantityStep = (productId?: number) => getProductQuantityStep(productList.value, productId)

const getSummary = async () => {
  try {
    const data = await ProductionOrderApi.getProductionOrderSummary()
    kpi.total = Number(data?.total || 0)
    kpi.created = Number(data?.created || 0)
    kpi.released = Number(data?.released || 0)
    kpi.finished = Number(data?.finished || 0)
  } catch {
    kpi.total = 0
    kpi.created = 0
    kpi.released = 0
    kpi.finished = 0
  }
}

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-'

const resolveStatusLabel = (status?: number) => {
  if (status === PRODUCTION_ORDER_STATUS.CREATED) return '已创建'
  if (status === PRODUCTION_ORDER_STATUS.RELEASED) return '已下达'
  if (status === PRODUCTION_ORDER_STATUS.FINISHED) return '已完工'
  if (status === PRODUCTION_ORDER_STATUS.CLOSED) return '已关闭'
  return '-'
}

const resolveStatusBadgeClass = (status?: number) => {
  if (status === PRODUCTION_ORDER_STATUS.CREATED) return 'po-badge--slate'
  if (status === PRODUCTION_ORDER_STATUS.RELEASED) return 'po-badge--primary'
  if (status === PRODUCTION_ORDER_STATUS.FINISHED) return 'po-badge--success'
  if (status === PRODUCTION_ORDER_STATUS.CLOSED) return 'po-badge--info'
  return 'po-badge--slate'
}

const resolveSourceLabel = (sourceType?: string) => {
  if (sourceType === 'MANUAL') return '手工创建'
  if (sourceType === 'MRP_SUGGEST') return 'MRP 建议'
  if (!sourceType) return '-'
  return sourceType
}

const getFinishPercent = (row: ProductionOrderVO) => {
  const planQty = Number(row.planQty || 0)
  if (planQty <= 0) return 0
  return Math.min(Math.round((Number(row.finishedQty || 0) / planQty) * 100), 100)
}

const resolveFinishProgressColor = (row: ProductionOrderVO) => {
  if (row.status === PRODUCTION_ORDER_STATUS.FINISHED) return 'var(--erp-success-500)'
  if (row.status === PRODUCTION_ORDER_STATUS.RELEASED) return 'var(--erp-primary-500)'
  return 'var(--erp-slate-300)'
}

const getRemainingQty = (row: ProductionOrderVO) =>
  Math.max(Number(row.planQty || 0) - Number(row.finishedQty || 0), 0)

const canEdit = (row: ProductionOrderVO) => row.status === PRODUCTION_ORDER_STATUS.CREATED

const canRelease = (row: ProductionOrderVO) =>
  row.status === PRODUCTION_ORDER_STATUS.CREATED && !releasingIds.value.includes(row.id)

const canFinish = (row: ProductionOrderVO) => row.status === PRODUCTION_ORDER_STATUS.RELEASED

const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await ProductionOrderApi.getProductionOrderPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listLoadFailed.value = true
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
  getSummary()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const loadRoutes = async (productId?: number) => {
  if (!productId) {
    routeList.value = []
    form.routeId = undefined
    return
  }
  const data = await ProcessRouteApi.getProcessRoutePage({
    pageNo: 1,
    pageSize: 100,
    productId,
    status: 1
  })
  routeList.value = data.list || []
  if (!routeList.value.some((item) => item.id === form.routeId)) {
    form.routeId = undefined
  }
}

const handleProductChange = (productId: number) => {
  loadRoutes(productId)
}

const openForm = async (mode: 'create' | 'edit', row?: ProductionOrderVO) => {
  formMode.value = mode
  Object.assign(form, emptyForm())
  formVisible.value = true
  if (mode === 'edit' && row?.id) {
    const data = await ProductionOrderApi.getProductionOrder(row.id)
    Object.assign(form, {
      id: data.id,
      productId: data.productId,
      routeId: data.routeId,
      workCenterId: data.workCenterId,
      batchNo: data.batchNo,
      planQty: Number(data.planQty || 0),
      planStartTime: data.planStartTime,
      planEndTime: data.planEndTime,
      remark: data.remark
    })
    await loadRoutes(data.productId)
  }
}

const handleFormSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.planStartTime && form.planEndTime && form.planStartTime > form.planEndTime) {
    message.warning('计划结束时间不能早于计划开始时间')
    return
  }
  formLoading.value = true
  try {
    if (formMode.value === 'create') {
      await ProductionOrderApi.createProductionOrder(form)
      message.success('创建生产工单成功')
    } else {
      await ProductionOrderApi.updateProductionOrder(form)
      message.success('更新生产工单成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

const handleRelease = async (row: ProductionOrderVO) => {
  if (!row.id) return
  try {
    await message.confirm('确定下达该生产工单吗？下达后将依据有效 BOM 生成用料快照。')
    releasingIds.value = [...releasingIds.value, row.id]
    await ProductionOrderApi.releaseProductionOrder(row.id)
    message.success('下达生产工单成功')
    await getList()
  } finally {
    releasingIds.value = releasingIds.value.filter((id) => id !== row.id)
  }
}

const openFinishDialog = (row: ProductionOrderVO) => {
  currentOrder.value = row
  finishForm.id = row.id
  finishForm.finishedQty = Math.max(Number(row.planQty || 0) - Number(row.finishedQty || 0), 0)
  finishForm.warehouseId = undefined
  finishVisible.value = true
}

const handleFinishSubmit = async () => {
  const valid = await finishFormRef.value?.validate().catch(() => false)
  if (!valid) return
  finishLoading.value = true
  try {
    await ProductionOrderApi.finishProductionOrder({
      id: finishForm.id,
      finishedQty: Number(finishForm.finishedQty),
      warehouseId: finishForm.warehouseId
    })
    message.success('完工生产工单成功')
    finishVisible.value = false
    await getList()
  } finally {
    finishLoading.value = false
  }
}

onMounted(async () => {
  getList()
  getSummary()
  try {
    productList.value = await ProductApi.getProductSimpleList()
  } catch {
    productList.value = []
  }
  try {
    workCenterList.value = await WorkCenterApi.getWorkCenterSimpleList()
  } catch {
    workCenterList.value = []
  }
  try {
    warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  } catch {
    warehouseList.value = []
  }
})
</script>

<style scoped>
.po-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.po-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.po-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.po-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.po-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.po-kpi-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 108px;
  padding: 18px 20px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 16px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.po-kpi-card--blue {
  background: var(--erp-stat-gradient-blue);
}

.po-kpi-card--amber {
  background: var(--erp-stat-gradient-amber);
}

.po-kpi-card--teal {
  background: var(--erp-stat-gradient-teal);
}

.po-kpi-card--green {
  background: var(--erp-stat-gradient-green);
}

.po-kpi-card__label {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 8px;
}

.po-kpi-card__value {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.po-kpi-card__suffix {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
}

.po-page__filter-card,
.po-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.po-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-date-editor.el-input__wrapper.is-focus) {
    border-color: var(--erp-primary-300);
    background: var(--erp-surface-white);
    box-shadow: 0 0 0 3px var(--erp-primary-50);
  }

  :deep(.el-input__inner),
  :deep(.el-select__placeholder),
  :deep(.el-range-input),
  :deep(.el-input__prefix),
  :deep(.el-input__suffix) {
    font-size: 12px;
  }

  :deep(.el-input__inner::placeholder),
  :deep(.el-range-input::placeholder) {
    color: var(--erp-slate-400);
  }
}

.po-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.po-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.po-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.po-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.po-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.po-ledger {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    background: var(--erp-slate-50);
    color: var(--erp-slate-500);
    font-size: 11px;
    font-weight: 700;
  }

  :deep(.el-table__cell) {
    padding-right: 8px;
    padding-left: 8px;
  }

  :deep(.el-table__row td) {
    padding-top: 14px;
    padding-bottom: 14px;
    vertical-align: top;
  }

  :deep(.el-progress-bar__outer) {
    background: var(--erp-slate-200);
  }
}

.po-ledger__order,
.po-ledger__party,
.po-ledger__finance,
.po-ledger__progress {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.po-ledger__order-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.po-ledger__order-no,
.po-ledger__party-name,
.po-ledger__finance-qty {
  color: var(--erp-slate-900);
  font-weight: 800;
  line-height: 22px;
}

.po-ledger__order-no {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.po-ledger__order-meta,
.po-ledger__party-meta,
.po-ledger__finance-meta,
.po-ledger__finance-sub {
  color: var(--erp-slate-500);
  font-size: 11px;
  line-height: 17px;
}

.po-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
  white-space: nowrap;
}

.po-badge--slate {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-600);
}

.po-badge--primary {
  background: var(--erp-primary-50);
  border-color: var(--erp-primary-200);
  color: var(--erp-primary-600);
}

.po-badge--success {
  background: var(--erp-success-50);
  border-color: var(--erp-success-200);
  color: var(--erp-success-600);
}

.po-badge--info {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-500);
}

.po-ledger__party-name {
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.po-ledger__party-meta {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.po-ledger__party-tag {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--erp-slate-100);
  color: var(--erp-slate-600);
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.po-ledger__party-text {
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.po-ledger__finance {
  align-items: flex-end;
}

.po-ledger__finance-qty {
  font-size: 20px;
  line-height: 1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.po-ledger__finance-meta {
  text-align: right;
}

.po-ledger__finance-sub {
  text-align: right;
}

.po-ledger__progress-top,
.po-ledger__progress-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.po-ledger__progress-top {
  color: var(--erp-slate-600);
  font-size: 11px;
  line-height: 17px;
}

.po-ledger__progress-top strong {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.po-ledger__progress-summary {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.po-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.po-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.po-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.po-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.po-empty--error .po-empty__icon {
  background: var(--erp-stat-gradient-rose);
  color: var(--erp-danger-600);
}

@media (max-width: 1280px) {
  .po-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .po-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .po-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
