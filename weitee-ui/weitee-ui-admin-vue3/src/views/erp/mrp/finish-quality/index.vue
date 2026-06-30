<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="96px"
      class="-mb-15px"
    >
      <el-form-item label="质检单号" prop="no">
        <el-input
          v-model="queryParams.no"
          placeholder="请输入质检单号"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产工单ID" prop="productionOrderId">
        <el-input
          v-model="queryParams.productionOrderId"
          placeholder="请输入生产工单ID"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="销售单ID" prop="sourceOrderId">
        <el-input
          v-model="queryParams.sourceOrderId"
          placeholder="请输入销售单ID"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质检状态" prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="请选择质检状态"
          class="!w-240px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="质检单号" prop="no" min-width="180" />
      <el-table-column label="生产工单" prop="productionOrderNo" min-width="180" />
      <el-table-column label="销售单ID" prop="sourceOrderId" min-width="100" />
      <el-table-column label="产品ID" prop="productId" min-width="100" />
      <el-table-column label="报工数量" min-width="110" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.reportQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="合格数量" min-width="110" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.qualifiedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="不合格数量" min-width="120" align="right">
        <template #default="{ row }">{{ erpCountInputFormatter(row.unqualifiedQty || 0) }}</template>
      </el-table-column>
      <el-table-column label="质检状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveStatusTagType(row.status)">
            {{ resolveStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="质检时间" min-width="180">
        <template #default="{ row }">
          {{ row.checkTime ? formatDate(row.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="180" />
      <el-table-column label="操作" fixed="right" width="180" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id!, 'view')">查看</el-button>
          <el-button
            v-if="canSubmit(row)"
            link
            type="success"
            :disabled="submitLoading"
            @click="openDetail(row.id!, 'submit')"
          >
            处理质检
          </el-button>
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

  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720">
    <div v-loading="detailLoading">
      <el-empty v-if="!detailForm.id" description="未找到质检单数据" />
      <template v-else>
        <ContentWrap class="!mb-12px">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="质检单号">{{ detailForm.no || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产工单">{{ detailForm.productionOrderNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="销售单ID">{{ detailForm.sourceOrderId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品ID">{{ detailForm.productId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="报工数量">
              {{ erpCountInputFormatter(detailForm.reportQty || 0) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="resolveStatusTagType(detailForm.status)">
                {{ resolveStatusLabel(detailForm.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="质检时间">
              {{ detailForm.checkTime ? formatDate(detailForm.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ detailForm.createTime ? formatDate(detailForm.createTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </ContentWrap>

        <el-alert
          v-if="dialogMode === 'submit'"
          :type="sumMatchesReportQty ? 'info' : 'warning'"
          :closable="false"
          show-icon
          :title="
            sumMatchesReportQty
              ? '合格数量与不合格数量之和已匹配报工数量，可提交。'
              : '合格数量与不合格数量之和必须等于报工数量。'
          "
          class="mb-12px"
        />

        <el-form label-width="110px">
          <el-form-item label="合格数量" required>
            <el-input-number
              v-model="detailForm.qualifiedQty"
              controls-position="right"
              :min="0"
              :max="Number(detailForm.reportQty || 0)"
              :precision="3"
              :disabled="isReadonly"
              class="!w-100%"
              @change="syncSubmitCounts('qualified')"
            />
          </el-form-item>
          <el-form-item label="不合格数量" required>
            <el-input-number
              v-model="detailForm.unqualifiedQty"
              controls-position="right"
              :min="0"
              :max="Number(detailForm.reportQty || 0)"
              :precision="3"
              :disabled="isReadonly"
              class="!w-100%"
              @change="syncSubmitCounts('unqualified')"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="detailForm.remark"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              :readonly="isReadonly"
              placeholder="请输入质检备注"
            />
          </el-form-item>
        </el-form>
      </template>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button
        v-if="dialogMode === 'submit'"
        type="primary"
        :disabled="!canSubmitCurrent"
        :loading="submitLoading"
        @click="handleSubmit"
      >
        提交质检
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'
import { erpCountInputFormatter } from '@/utils'
import {
  PRODUCTION_FINISH_QUALITY_STATUS,
  ProductionFinishQualityApi,
  type ProductionFinishQualityPageReqVO,
  type ProductionFinishQualityVO
} from '@/api/erp/mrp/finish-quality'

defineOptions({ name: 'ErpProductionFinishQuality' })

const message = useMessage()
const route = useRoute()
const hasQueryPermission = checkPermi(['erp:production-order:query'])
const hasUpdatePermission = checkPermi(['erp:production-order:update'])

const statusOptions = [
  { label: '待质检', value: PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT },
  { label: '部分合格', value: PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL },
  { label: '全部合格', value: PRODUCTION_FINISH_QUALITY_STATUS.PASSED },
  { label: '全部不合格', value: PRODUCTION_FINISH_QUALITY_STATUS.REJECTED }
]

const listLoading = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'view' | 'submit'>('view')
const queryFormRef = ref()
const list = ref<ProductionFinishQualityVO[]>([])
const total = ref(0)
const queryParams = reactive<ProductionFinishQualityPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  productionOrderId: undefined,
  sourceOrderId: undefined,
  status: undefined
})
const detailForm = reactive<ProductionFinishQualityVO>({
  id: undefined,
  no: undefined,
  productionOrderId: undefined,
  productionOrderNo: undefined,
  sourceOrderId: undefined,
  sourceItemId: undefined,
  productId: undefined,
  reportQty: 0,
  qualifiedQty: 0,
  unqualifiedQty: 0,
  status: undefined,
  checkerUserId: undefined,
  checkTime: undefined,
  remark: '',
  createTime: undefined
})

const dialogTitle = computed(() => (dialogMode.value === 'submit' ? '处理成品质检' : '查看成品质检'))
const isReadonly = computed(
  () =>
    dialogMode.value === 'view' ||
    Number(detailForm.status) !== PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT
)
const sumMatchesReportQty = computed(() => {
  return roundCount(Number(detailForm.qualifiedQty || 0) + Number(detailForm.unqualifiedQty || 0)) ===
    roundCount(Number(detailForm.reportQty || 0))
})
const canSubmitCurrent = computed(() => {
  return (
    hasUpdatePermission &&
    dialogMode.value === 'submit' &&
    !submitLoading.value &&
    Number(detailForm.id || 0) > 0 &&
    Number(detailForm.status) === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT &&
    sumMatchesReportQty.value
  )
})

const roundCount = (value: number) => {
  return Math.round((Number(value || 0) + Number.EPSILON) * 1000) / 1000
}

const resolveStatusLabel = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return '待质检'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return '部分合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return '全部合格'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return '全部不合格'
  return '-'
}

const resolveStatusTagType = (status?: number) => {
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT) return 'warning'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PARTIAL) return 'primary'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.PASSED) return 'success'
  if (status === PRODUCTION_FINISH_QUALITY_STATUS.REJECTED) return 'danger'
  return 'info'
}

const canSubmit = (row: ProductionFinishQualityVO) => {
  return (
    hasUpdatePermission &&
    Number(row.status) === PRODUCTION_FINISH_QUALITY_STATUS.TO_INSPECT &&
    !submitLoading.value
  )
}

const resetDetailForm = () => {
  detailForm.id = undefined
  detailForm.no = undefined
  detailForm.productionOrderId = undefined
  detailForm.productionOrderNo = undefined
  detailForm.sourceOrderId = undefined
  detailForm.sourceItemId = undefined
  detailForm.productId = undefined
  detailForm.reportQty = 0
  detailForm.qualifiedQty = 0
  detailForm.unqualifiedQty = 0
  detailForm.status = undefined
  detailForm.checkerUserId = undefined
  detailForm.checkTime = undefined
  detailForm.remark = ''
  detailForm.createTime = undefined
}

const applyDetail = (data: ProductionFinishQualityVO) => {
  detailForm.id = data.id
  detailForm.no = data.no
  detailForm.productionOrderId = data.productionOrderId
  detailForm.productionOrderNo = data.productionOrderNo
  detailForm.sourceOrderId = data.sourceOrderId
  detailForm.sourceItemId = data.sourceItemId
  detailForm.productId = data.productId
  detailForm.reportQty = Number(data.reportQty || 0)
  detailForm.qualifiedQty = Number(data.qualifiedQty || 0)
  detailForm.unqualifiedQty = Number(data.unqualifiedQty || 0)
  detailForm.status = data.status
  detailForm.checkerUserId = data.checkerUserId
  detailForm.checkTime = data.checkTime
  detailForm.remark = data.remark || ''
  detailForm.createTime = data.createTime
}

const getList = async () => {
  if (!hasQueryPermission) return
  listLoading.value = true
  try {
    const data = await ProductionFinishQualityApi.getProductionFinishQualityPage(queryParams)
    list.value = data.list
    total.value = data.total
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

const openDetail = async (id: number, mode: 'view' | 'submit') => {
  dialogVisible.value = true
  dialogMode.value = mode
  resetDetailForm()
  detailLoading.value = true
  try {
    const data = await ProductionFinishQualityApi.getProductionFinishQuality(id)
    applyDetail(data)
  } finally {
    detailLoading.value = false
  }
}

const syncSubmitCounts = (changedField: 'qualified' | 'unqualified') => {
  const reportQty = Number(detailForm.reportQty || 0)
  const qualifiedQty = Math.min(Math.max(Number(detailForm.qualifiedQty || 0), 0), reportQty)
  const unqualifiedQty = Math.min(Math.max(Number(detailForm.unqualifiedQty || 0), 0), reportQty)
  if (changedField === 'qualified') {
    detailForm.qualifiedQty = roundCount(qualifiedQty)
    detailForm.unqualifiedQty = roundCount(Math.max(reportQty - qualifiedQty, 0))
    return
  }
  detailForm.unqualifiedQty = roundCount(unqualifiedQty)
  detailForm.qualifiedQty = roundCount(Math.max(reportQty - unqualifiedQty, 0))
}

const handleSubmit = async () => {
  if (!canSubmitCurrent.value) {
    if (!sumMatchesReportQty.value) {
      message.warning('合格数量与不合格数量之和必须等于报工数量')
    }
    return
  }
  submitLoading.value = true
  try {
    await ProductionFinishQualityApi.submitProductionFinishQuality({
      id: Number(detailForm.id),
      qualifiedQty: roundCount(Number(detailForm.qualifiedQty || 0)),
      unqualifiedQty: roundCount(Number(detailForm.unqualifiedQty || 0)),
      remark: detailForm.remark?.trim()
    })
    message.success('提交成品质检成功')
    await openDetail(Number(detailForm.id), 'view')
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  const sourceOrderId = Number(route.query.sourceOrderId || 0)
  if (sourceOrderId > 0) {
    queryParams.sourceOrderId = sourceOrderId
  }
  getList()
})
</script>
