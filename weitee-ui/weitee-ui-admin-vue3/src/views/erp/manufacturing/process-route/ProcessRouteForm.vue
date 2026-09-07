<template>
  <el-dialog
    v-model="visible"
    :title="isCreate ? '新增工艺路线' : '编辑工艺路线'"
    width="1180px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    destroy-on-close
    class="route-form-dialog"
  >
    <div class="route-form__body">
      <div class="route-form__section-title">基础信息</div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="route-form__base">
        <el-form-item label="工艺编码" prop="routeCode">
          <el-input v-model="form.routeCode" clearable placeholder="请输入工艺编码" maxlength="64" />
        </el-form-item>
        <el-form-item label="工艺名称" prop="routeName">
          <el-input v-model="form.routeName" clearable placeholder="请输入工艺名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <ProductRemoteSelect v-model="form.productId" placeholder="请选择产品" style="width: 100%" />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" clearable placeholder="请输入版本" maxlength="32" />
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="form.effectiveDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择生效日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="失效日期" prop="expireDate">
          <el-date-picker
            v-model="form.expireDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择失效日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="默认路线">
          <el-switch v-model="form.defaultFlag" :disabled="saving" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" clearable placeholder="请输入备注" maxlength="255" />
        </el-form-item>
      </el-form>

      <div class="route-form__section-head">
        <div class="route-form__section-title">工序明细</div>
        <el-button type="primary" plain :disabled="saving" @click="handleAddStep">
          <Icon icon="ep:plus" class="mr-5px" />
          新增工序
        </el-button>
      </div>
      <div class="route-form__steps-scroll">
        <el-table :data="steps" class="route-form__steps" row-key="rowKey">
          <el-table-column label="工序编号" min-width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.stepNo" :min="0" :precision="0" :controls="false" placeholder="编号" class="route-form__num" />
            </template>
          </el-table-column>
          <el-table-column label="工序编码" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.stepCode" placeholder="编码" maxlength="64" />
            </template>
          </el-table-column>
          <el-table-column label="工序名称" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.stepName" placeholder="名称" maxlength="128" />
            </template>
          </el-table-column>
          <el-table-column label="工作中心" min-width="150">
            <template #default="{ row }">
              <el-select v-model="row.workCenterId" clearable filterable placeholder="工作中心" style="width: 100%">
                <el-option
                  v-for="item in workCenterOptions"
                  :key="item.id"
                  :label="`${item.centerCode} / ${item.centerName}`"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="执行约束" min-width="210">
            <template #default="{ row }">
              <div class="route-form__flags">
                <el-checkbox v-model="row.outsourceFlag" border size="small">委外</el-checkbox>
                <el-checkbox v-model="row.qcFlag" border size="small">质检</el-checkbox>
                <el-checkbox v-model="row.reportRequired" border size="small">报工</el-checkbox>
                <el-checkbox v-model="row.inspectRequired" border size="small">检验</el-checkbox>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="标准工时(小时)" min-width="200">
            <template #default="{ row }">
              <div class="route-form__times">
                <el-input-number v-model="row.prepareTime" :min="0" :precision="4" :controls="false" placeholder="准备" />
                <el-input-number v-model="row.processTime" :min="0" :precision="4" :controls="false" placeholder="加工" />
                <el-input-number v-model="row.moveTime" :min="0" :precision="4" :controls="false" placeholder="移动" />
                <el-input-number v-model="row.waitTime" :min="0" :precision="4" :controls="false" placeholder="等待" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="批量" min-width="90">
            <template #default="{ row }">
              <el-input-number v-model="row.batchSize" :min="0" :precision="4" :controls="false" placeholder="批量" class="route-form__num" />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="130">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="备注" maxlength="255" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ $index }">
              <div class="route-form__row-actions">
                <el-tooltip content="上移" placement="top">
                  <el-button link type="primary" :disabled="$index === 0 || saving" @click="handleMoveStep($index, -1)">
                    <Icon icon="ep:top" />
                  </el-button>
                </el-tooltip>
                <el-tooltip content="下移" placement="top">
                  <el-button link type="primary" :disabled="$index === steps.length - 1 || saving" @click="handleMoveStep($index, 1)">
                    <Icon icon="ep:bottom" />
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button link type="danger" :disabled="saving" @click="handleRemoveStep($index)">
                    <Icon icon="ep:delete" />
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <div class="route-form__footer">
        <div class="route-form__hint">{{ steps.length }} 道工序</div>
        <div class="route-form__actions">
          <el-button :disabled="saving" @click="visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import {
  ProcessRouteApi,
  type ProcessRouteSaveReqVO,
  type ProcessRouteStepVO
} from '@/api/erp/manufacturing/process-route'
import { WorkCenterApi, type WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'

defineOptions({ name: 'ErpManufacturingProcessRouteForm' })

// 本地步骤行：在保存 VO 基础上追加行级唯一键，用于列表 diff
interface StepRow extends ProcessRouteStepVO {
  rowKey: number
}

const message = useMessage()
const visible = ref(false)
const saving = ref(false)
const isCreate = ref(true)
const formRef = ref()
const productLoading = ref(false)
const productOptions = ref<ProductVO[]>([])
const workCenterOptions = ref<WorkCenterSimpleVO[]>([])
let rowKeySeed = 1

const form = reactive<ProcessRouteSaveReqVO>({
  routeCode: '',
  routeName: '',
  productId: undefined,
  version: '',
  defaultFlag: false,
  effectiveDate: undefined,
  expireDate: undefined,
  remark: undefined,
  steps: []
})
// 步骤行实际存储：带行级唯一键
const steps = ref<StepRow[]>([])

const rules = {
  routeCode: [{ required: true, message: '工艺编码不能为空', trigger: 'blur' }],
  routeName: [{ required: true, message: '工艺名称不能为空', trigger: 'blur' }],
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  version: [{ required: true, message: '版本不能为空', trigger: 'blur' }]
}

const open = async (type: 'create' | 'update', row?: any) => {
  isCreate.value = type === 'create'
  rowKeySeed = 1
  resetForm()
  await Promise.all([loadProducts(), loadWorkCenters()])
  if (type === 'update' && row?.id) {
    try {
      const detail = await ProcessRouteApi.getProcessRoute(row.id)
      Object.assign(form, {
        id: detail.id,
        routeCode: detail.routeCode,
        routeName: detail.routeName,
        productId: detail.productId,
        version: detail.version,
        defaultFlag: detail.defaultFlag,
        effectiveDate: detail.effectiveDate,
        expireDate: detail.expireDate,
        remark: detail.remark
      })
      steps.value = (detail.steps || []).map(normalizeStep)
    } catch (e: any) {
      message.error(e?.message || '工艺路线详情加载失败')
      visible.value = false
      return
    }
  } else {
    addEmptyStep()
    addEmptyStep()
  }
  visible.value = true
}

const resetForm = () => {
  form.id = undefined
  form.routeCode = ''
  form.routeName = ''
  form.productId = undefined
  form.version = ''
  form.defaultFlag = false
  form.effectiveDate = undefined
  form.expireDate = undefined
  form.remark = undefined
  steps.value = []
}

const normalizeStep = (step: ProcessRouteStepVO) => ({
  rowKey: rowKeySeed++,
  stepNo: step.stepNo ?? 0,
  stepCode: step.stepCode ?? '',
  stepName: step.stepName ?? '',
  workCenterId: step.workCenterId,
  outsourceFlag: Boolean(step.outsourceFlag),
  qcFlag: Boolean(step.qcFlag),
  reportRequired: step.reportRequired !== false,
  inspectRequired: Boolean(step.inspectRequired),
  prepareTime: step.prepareTime ?? 0,
  processTime: step.processTime ?? 0,
  moveTime: step.moveTime ?? 0,
  waitTime: step.waitTime ?? 0,
  batchSize: step.batchSize ?? 1,
  sort: step.sort ?? 0,
  remark: step.remark
})

const addEmptyStep = () => {
  const last = steps.value[steps.value.length - 1]
  steps.value.push({
    rowKey: rowKeySeed++,
    stepNo: (last?.stepNo ?? 0) + 10,
    stepCode: '',
    stepName: '',
    workCenterId: undefined,
    outsourceFlag: false,
    qcFlag: false,
    reportRequired: true,
    inspectRequired: false,
    prepareTime: 0,
    processTime: 0,
    moveTime: 0,
    waitTime: 0,
    batchSize: 1,
    sort: steps.value.length,
    remark: undefined
  })
}

const handleAddStep = () => addEmptyStep()

const handleRemoveStep = (index: number) => {
  steps.value.splice(index, 1)
}

const handleMoveStep = (index: number, offset: number) => {
  const target = index + offset
  if (target < 0 || target >= steps.value.length) {
    return
  }
  const current = steps.value[index]
  steps.value[index] = steps.value[target]
  steps.value[target] = current
}

const loadProducts = async () => {
  productLoading.value = true
  try {
    productOptions.value = []
  } catch {
    productOptions.value = []
  } finally {
    productLoading.value = false
  }
}

const loadWorkCenters = async () => {
  try {
    workCenterOptions.value = (await WorkCenterApi.getWorkCenterSimpleList()) || []
  } catch {
    workCenterOptions.value = []
  }
}

const validateSteps = (): string | null => {
  if (steps.value.length === 0) {
    return '至少需要一道工序'
  }
  const stepNos = new Set<number>()
  const stepCodes = new Set<string>()
  for (const step of steps.value) {
    if (step.stepNo === null || step.stepNo === undefined) {
      return '工序编号不能为空'
    }
    if (!step.stepCode || !step.stepCode.trim()) {
      return '工序编码不能为空'
    }
    if (!step.stepName || !step.stepName.trim()) {
      return '工序名称不能为空'
    }
    if (stepNos.has(step.stepNo)) {
      return `工序编号 ${step.stepNo} 重复`
    }
    if (stepCodes.has(step.stepCode.trim())) {
      return `工序编码 ${step.stepCode} 重复`
    }
    stepNos.add(step.stepNo)
    stepCodes.add(step.stepCode.trim())
  }
  return null
}

const handleSave = async () => {
  if (saving.value) {
    return
  }
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const stepError = validateSteps()
  if (stepError) {
    message.error(stepError)
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      steps: steps.value.map((step, index) => {
        const { rowKey, ...rest } = step as any
        return { ...rest, sort: index }
      })
    }
    if (isCreate.value) {
      await ProcessRouteApi.createProcessRoute(payload)
    } else {
      await ProcessRouteApi.updateProcessRoute(payload)
    }
    message.success(isCreate.value ? '创建成功' : '更新成功')
    visible.value = false
    emit('success')
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const emit = defineEmits<{ (e: 'success'): void }>()

defineExpose({ open })
</script>

<style scoped lang="scss">
.route-form__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 4px 4px 8px;
}

.route-form__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.route-form__section-title {
  color: var(--erp-slate-800, #1e293b);
  font-size: 15px;
  font-weight: 700;
}

.route-form__base {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
  padding: 16px;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
  border-radius: 12px;
  background: var(--erp-slate-50, #f8fafc);
}

.route-form__base :deep(.el-form-item) {
  margin-bottom: 0;
}

.route-form__steps-scroll {
  overflow-x: auto;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
  border-radius: 12px;
}

.route-form__steps {
  min-width: 1180px;
}

.route-form__steps :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.route-form__steps :deep(.el-table td.el-table__cell),
.route-form__steps :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.route-form__num {
  width: 100%;
}

.route-form__flags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.route-form__times {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 4px;
}

.route-form__row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.route-form__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.route-form__hint {
  color: var(--erp-slate-500, #64748b);
  font-size: 13px;
}

@media (max-width: 767px) {
  .route-form__base {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
