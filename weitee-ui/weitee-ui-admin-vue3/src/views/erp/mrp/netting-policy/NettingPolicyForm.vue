<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="min(1160px, calc(100vw - 32px))"
    scroll
    maxHeight="80vh"
    :close-on-click-modal="!formLoading && !submitting"
    :close-on-press-escape="!formLoading && !submitting"
    @close="handleDialogClose"
  >
    <div class="netting-policy-form-body" v-loading="formLoading">
      <el-alert
        v-if="loadErrorMessage"
        :title="loadErrorMessage"
        type="error"
        :closable="false"
        show-icon
        class="mb-16px"
      />

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="策略编码" prop="code">
              <el-input v-model="formData.code" maxlength="32" placeholder="请输入策略编码" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="策略名称" prop="name">
              <el-input v-model="formData.name" maxlength="64" placeholder="请输入策略名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态" prop="enableFlag">
              <el-switch
                v-model="formData.enableFlag"
                inline-prompt
                active-text="启用"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="默认策略" prop="defaultFlag">
              <el-switch
                v-model="formData.defaultFlag"
                inline-prompt
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="适用业务类型" prop="businessTypes">
              <el-checkbox-group v-model="formData.businessTypes">
                <el-checkbox
                  v-for="item in BUSINESS_TYPE_OPTIONS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="组件口径" prop="lines">
              <div class="w-full overflow-x-auto rounded-4px border border-[var(--el-border-color-light)]">
                <el-table :data="formData.lines" size="small" :stripe="true">
                  <el-table-column label="组件" prop="componentName" min-width="160" />
                  <el-table-column label="口径类型" prop="componentRole" min-width="120">
                    <template #default="{ row }">
                      {{ getComponentRoleLabel(row.componentRole) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="启用" min-width="100" align="center">
                    <template #default="{ row }">
                      <el-switch v-model="row.enableFlag" />
                    </template>
                  </el-table-column>
                  <el-table-column label="抵扣顺序" min-width="140" align="center">
                    <template #default="{ row }">
                      <span v-if="row.componentRole !== 'SUPPLY_CONSUME'">-</span>
                      <el-input-number
                        v-else
                        v-model="row.sequenceNo"
                        :min="1"
                        :precision="0"
                        :disabled="!row.enableFlag"
                      />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                maxlength="255"
                show-word-limit
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <template #footer>
      <el-button v-if="loadErrorMessage" @click="reloadDialogData" :disabled="formLoading || submitting">
        重新加载
      </el-button>
      <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="submitDisabled" @click="submitForm">
        保存
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { MrpNettingPolicyApi, MrpNettingPolicyLineVO, MrpNettingPolicyVO } from '@/api/erp/mrp/netting-policy'

defineOptions({ name: 'NettingPolicyForm' })

type FormMode = 'create' | 'update'

type BusinessTypeOption = {
  label: string
  value: string
}

const BUSINESS_TYPE_OPTIONS: BusinessTypeOption[] = [
  { label: '自研', value: 'SELF_RESEARCH' },
  { label: '客供', value: 'CUSTOMER_SUPPLIED' },
  { label: '来料加工', value: 'TOLL_MANUFACTURING' }
]

const DEFAULT_LINES: MrpNettingPolicyLineVO[] = [
  {
    componentCode: 'SAFETY_STOCK',
    componentName: '安全库存',
    componentRole: 'DEMAND_ADJUST',
    enableFlag: true,
    sequenceNo: 0
  },
  {
    componentCode: 'INCOMING_PURCHASE',
    componentName: '采购在途',
    componentRole: 'SUPPLY_CONSUME',
    enableFlag: true,
    sequenceNo: 10
  },
  {
    componentCode: 'WIP_PRODUCTION',
    componentName: '生产在制',
    componentRole: 'SUPPLY_CONSUME',
    enableFlag: true,
    sequenceNo: 20
  },
  {
    componentCode: 'ON_HAND_AVAILABLE',
    componentName: '可用库存',
    componentRole: 'SUPPLY_CONSUME',
    enableFlag: true,
    sequenceNo: 30
  }
]

const createDefaultLines = (): MrpNettingPolicyLineVO[] =>
  DEFAULT_LINES.map((item) => ({
    componentCode: item.componentCode,
    componentName: item.componentName,
    componentRole: item.componentRole,
    enableFlag: item.enableFlag,
    sequenceNo: item.sequenceNo
  }))

const createDefaultFormData = (): MrpNettingPolicyVO => ({
  code: '',
  name: '',
  enableFlag: true,
  defaultFlag: false,
  remark: '',
  businessTypes: [],
  lines: createDefaultLines()
})

const normalizeLines = (lines?: MrpNettingPolicyLineVO[]) => {
  const lineMap = new Map((lines || []).map((item) => [item.componentCode, item]))
  return DEFAULT_LINES.map((item) => {
    const current = lineMap.get(item.componentCode)
    return {
      componentCode: item.componentCode,
      componentName: current?.componentName || item.componentName,
      componentRole: current?.componentRole || item.componentRole,
      enableFlag: current?.enableFlag ?? item.enableFlag,
      sequenceNo: current?.sequenceNo ?? item.sequenceNo
    }
  })
}

const getComponentRoleLabel = (role?: string) => {
  if (role === 'DEMAND_ADJUST') {
    return '需求调整'
  }
  if (role === 'SUPPLY_CONSUME') {
    return '供给抵扣'
  }
  return role || '-'
}

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('新增净需求策略')
const formMode = ref<FormMode>('create')
const currentPolicyId = ref<number>()
const requestToken = ref(0)
const detailLoading = ref(false)
const submitting = ref(false)
const loadErrorMessage = ref('')
const formRef = ref()
const formData = ref<MrpNettingPolicyVO>(createDefaultFormData())

const formLoading = computed(() => detailLoading.value)
const submitDisabled = computed(() => formLoading.value || submitting.value || !!loadErrorMessage.value)

const formRules = reactive({
  code: [{ required: true, message: '请输入策略编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }]
})

const isActiveRequest = (token: number) => token === requestToken.value && dialogVisible.value

const resetForm = () => {
  formData.value = createDefaultFormData()
  nextTick(() => {
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  })
}

const handleDialogClose = () => {
  requestToken.value += 1
  currentPolicyId.value = undefined
  detailLoading.value = false
  loadErrorMessage.value = ''
  resetForm()
}

const applyDetail = (data: MrpNettingPolicyVO) => {
  formData.value = {
    id: data.id,
    code: data.code || '',
    name: data.name || '',
    version: data.version,
    enableFlag: data.enableFlag ?? true,
    defaultFlag: data.defaultFlag ?? false,
    remark: data.remark || '',
    businessTypes: [...(data.businessTypes || [])],
    lines: normalizeLines(data.lines)
  }
}

const validateLines = () => {
  const enabledSupplyLines = formData.value.lines.filter(
    (item) => item.componentRole === 'SUPPLY_CONSUME' && item.enableFlag
  )
  const invalidLine = enabledSupplyLines.find((item) => !item.sequenceNo || item.sequenceNo <= 0)
  if (invalidLine) {
    throw new Error(`${invalidLine.componentName}的抵扣顺序必须大于 0`)
  }
  const usedSequence = new Set<number>()
  for (const line of enabledSupplyLines) {
    if (usedSequence.has(Number(line.sequenceNo))) {
      throw new Error('抵扣顺序不能重复')
    }
    usedSequence.add(Number(line.sequenceNo))
  }
}

const buildPayload = (): MrpNettingPolicyVO => ({
  id: formData.value.id,
  code: formData.value.code.trim().toUpperCase(),
  name: formData.value.name.trim(),
  enableFlag: formData.value.enableFlag ?? true,
  defaultFlag: formData.value.defaultFlag ?? false,
  remark: formData.value.remark?.trim() || '',
  businessTypes: [...formData.value.businessTypes],
  lines: formData.value.lines.map((item) => ({
    componentCode: item.componentCode,
    enableFlag: item.enableFlag ?? false,
    sequenceNo: item.componentRole === 'SUPPLY_CONSUME' ? item.sequenceNo : 0
  }))
})

const loadDetail = async (id: number, token: number) => {
  detailLoading.value = true
  try {
    const data = await MrpNettingPolicyApi.getPolicy(id)
    if (!isActiveRequest(token)) {
      return
    }
    applyDetail(data)
  } catch (error: any) {
    if (isActiveRequest(token)) {
      loadErrorMessage.value = error?.message || '净需求策略详情加载失败，请重试'
    }
  } finally {
    if (isActiveRequest(token)) {
      detailLoading.value = false
    }
  }
}

const loadDialogData = async () => {
  const token = requestToken.value + 1
  requestToken.value = token
  loadErrorMessage.value = ''
  if (formMode.value === 'create' || !currentPolicyId.value) {
    detailLoading.value = false
    return
  }
  await loadDetail(currentPolicyId.value, token)
}

const reloadDialogData = async () => {
  await loadDialogData()
}

const open = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  currentPolicyId.value = id
  dialogTitle.value = mode === 'create' ? '新增净需求策略' : '编辑净需求策略'
  dialogVisible.value = true
  resetForm()
  if (mode === 'update' && id) {
    await loadDialogData()
  }
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
    validateLines()
  } catch {
    return
  }

  const payload = buildPayload()
  submitting.value = true
  try {
    if (formMode.value === 'create') {
      await MrpNettingPolicyApi.createPolicy(payload)
      message.success('新增成功')
    } else {
      await MrpNettingPolicyApi.updatePolicy(payload)
      message.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '保存失败，请重试')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.netting-policy-form-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
