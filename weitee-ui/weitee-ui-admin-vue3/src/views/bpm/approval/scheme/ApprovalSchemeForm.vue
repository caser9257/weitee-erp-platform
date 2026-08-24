<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="min(1080px, calc(100vw - 32px))"
    class="approval-scheme-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="90px"
      v-loading="formLoading"
    >
      <div class="approval-scheme-form__grid">
        <el-form-item label="方案编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="如 erp.purchase.in.scheme.v1"
            :disabled="formType === 'update'"
          />
        </el-form-item>
        <el-form-item label="方案名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入方案名称" />
        </el-form-item>
        <el-form-item label="模块编码" prop="moduleCode">
          <el-input v-model="formData.moduleCode" placeholder="如 erp_purchase" />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-input v-model="formData.bizType" placeholder="如 purchase_in" />
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>

      <el-divider content-position="left">审批规则</el-divider>
      <div class="approval-scheme-form__rules-head">
        <span class="approval-scheme-form__rules-tip">必须且只能存在一条默认规则</span>
        <el-button type="primary" link @click="addRule">
          <Icon icon="ep:plus" class="mr-5px" />
          添加规则
        </el-button>
      </div>
      <div
        v-if="processDefinitionLoadFailed"
        class="approval-scheme-form__process-state approval-scheme-form__process-state--error"
      >
        加载流程定义失败，可手动输入流程定义Key
      </div>
      <div v-else-if="processDefinitionEmpty" class="approval-scheme-form__process-state">
        暂无已部署流程定义，可手动输入流程定义Key
      </div>
      <div class="approval-scheme-form__rules">
        <div
          v-for="(rule, index) in formData.rules"
          :key="index"
          class="approval-scheme-form__rule"
        >
          <div class="approval-scheme-form__rule-head">
            <span class="approval-scheme-form__rule-index">规则 {{ index + 1 }}</span>
            <el-button
              link
              type="danger"
              :disabled="formData.rules.length <= 1"
              @click="removeRule(index)"
            >
              <Icon icon="ep:delete" />
            </el-button>
          </div>
          <div class="approval-scheme-form__rule-meta">
            <div class="approval-scheme-form__rule-meta-fields">
              <el-form-item label="规则名称">
                <el-input v-model="rule.ruleName" placeholder="如 默认审批规则" />
              </el-form-item>
              <el-form-item label="规则类型">
                <el-select v-model="rule.ruleType" class="!w-full">
                  <el-option label="默认规则" value="DEFAULT" />
                  <el-option label="条件规则" value="CONDITION" />
                </el-select>
              </el-form-item>
              <el-form-item label="优先级">
                <el-input-number v-model="rule.priority" :min="1" class="!w-full" />
              </el-form-item>
            </div>
            <div class="approval-scheme-form__rule-meta-flags">
              <el-form-item label="默认规则">
                <el-switch
                  v-model="rule.defaultRule"
                  @change="(checked: boolean) => handleDefaultRuleChange(index, checked)"
                />
              </el-form-item>
              <el-form-item label="启用">
                <el-switch v-model="rule.enabled" />
              </el-form-item>
            </div>
          </div>
          <div class="approval-scheme-form__rule-section">
            <div class="approval-scheme-form__rule-section-head">
              <span class="approval-scheme-form__rule-section-title">条件配置</span>
            </div>
            <ApprovalConditionDesigner
              v-model="rule.conditionJson"
              class="approval-scheme-form__condition"
            />
          </div>
          <div class="approval-scheme-form__rule-section">
            <div class="approval-scheme-form__rule-section-head">
              <span class="approval-scheme-form__rule-section-title">流程配置</span>
              <span
                class="approval-scheme-form__flow-status"
                :class="`approval-scheme-form__flow-status--${getFlowStatusType(index)}`"
              >
                {{ getFlowStatusText(index) }}
              </span>
            </div>
            <div class="approval-scheme-form__process-field">
              <span class="approval-scheme-form__process-label">流程定义 Key</span>
              <div class="approval-scheme-form__process-editor">
                <el-autocomplete
                  :ref="(instance: unknown) => setProcessInputRef(index, instance)"
                  v-model="rule.processJson"
                  class="approval-scheme-form__json-input"
                  clearable
                  :loading="processDefinitionLoading"
                  :fetch-suggestions="getProcessDefinitionSuggestions"
                  :trigger-on-focus="true"
                  value-key="key"
                  placeholder="选择或输入流程定义Key"
                  @input="handleProcessKeyChange(index)"
                  @select="handleProcessKeyChange(index)"
                  @clear="handleProcessKeyChange(index)"
                >
                  <template #default="{ item }">
                    {{ item.name || item.key }}（{{ item.key }}）
                  </template>
                </el-autocomplete>
                <el-button
                  class="approval-scheme-form__process-confirm"
                  type="primary"
                  plain
                  :disabled="!canConfirmProcessKey(index)"
                  @click="confirmProcessKey(index)"
                >
                  <Icon icon="ep:check" class="mr-5px" />
                  确认
                </el-button>
                <el-button
                  class="approval-scheme-form__process-design"
                  type="primary"
                  plain
                  :disabled="formLoading || submitting"
                  @click="openFlowDesigner(index)"
                >
                  <Icon icon="ep:operation" class="mr-5px" />
                  设计审批流程
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="!canSubmit" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
  <ApprovalFlowDesigner
    v-model="flowDesignerVisible"
    :scheme-code="formData.code"
    :scheme-name="formData.name"
    :rule="activeRule"
    @published="handleFlowPublished"
    @status-change="handleFlowStatusChange"
  />
</template>

<script setup lang="ts">
import {
  ApprovalSchemeApi,
  BpmApprovalRuleVO,
  BpmApprovalSchemeSaveReqVO
} from '@/api/bpm/approval/scheme'
import { getSimpleProcessDefinitionList } from '@/api/bpm/definition'
import ApprovalConditionDesigner from './ApprovalConditionDesigner.vue'
import ApprovalFlowDesigner from './ApprovalFlowDesigner.vue'

/** 审批方案表单 */
defineOptions({ name: 'ApprovalSchemeForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const processDefinitionLoading = ref(false)
const processDefinitionLoadFailed = ref(false)
const submitting = ref(false)
const formType = ref('')
interface ProcessDefinitionOption {
  id?: string | number
  name?: string
  key?: string
}

const processDefinitionList = ref<ProcessDefinitionOption[]>([])
const processDefinitionRequestId = ref(0)
const processDefinitionOptions = computed(() =>
  processDefinitionList.value.filter((definition) => Boolean(definition.key))
)
const getProcessDefinitionSuggestions = (
  queryString: string,
  callback: (suggestions: ProcessDefinitionOption[]) => void
) => {
  const query = queryString.trim().toLocaleLowerCase()
  if (!query) {
    callback(processDefinitionOptions.value)
    return
  }
  callback(
    processDefinitionOptions.value.filter((definition) =>
      `${definition.key} ${definition.name || ''}`.toLocaleLowerCase().includes(query)
    )
  )
}
type FlowDesignerStatus = {
  modelId?: string
  processKey?: string
  saved: boolean
  published: boolean
}

const flowDesignerVisible = ref(false)
const activeRuleIndex = ref(-1)
const flowStatusByRule = reactive<Record<number, FlowDesignerStatus>>({})
type ProcessInputInstance = { blur?: () => void }
const processInputRefs = new Map<number, ProcessInputInstance>()
const activeRule = computed(() =>
  activeRuleIndex.value >= 0 ? formData.value.rules[activeRuleIndex.value] : undefined
)
const processDefinitionEmpty = computed(
  () =>
    !processDefinitionLoading.value &&
    !processDefinitionLoadFailed.value &&
    !processDefinitionOptions.value.length
)
const formData = ref<BpmApprovalSchemeSaveReqVO>({
  id: undefined,
  versionId: undefined,
  name: '',
  code: '',
  moduleCode: '',
  bizType: '',
  remark: undefined,
  designJson: '{"nodes":[]}',
  notifyJson: undefined,
  rules: []
})
const formRules = reactive({
  code: [{ required: true, message: '方案编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '模块编码不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }]
})
const formRef = ref()
const hasUnpublishedFlow = computed(() =>
  formData.value.rules.some((_, index) => {
    const status = flowStatusByRule[index]
    return Boolean(status?.saved && !status.published)
  })
)
const canSubmit = computed(
  () =>
    !formLoading.value &&
    !submitting.value &&
    !flowDesignerVisible.value &&
    !hasUnpublishedFlow.value
)

const emit = defineEmits<{ (e: 'success'): void }>()

const emptyRule = (): BpmApprovalRuleVO => ({
  ruleName: '默认审批规则',
  ruleType: 'DEFAULT',
  priority: 1,
  defaultRule: false,
  conditionJson: undefined,
  processJson: undefined,
  enabled: true
})

const loadProcessDefinitions = async () => {
  const requestId = processDefinitionRequestId.value + 1
  processDefinitionRequestId.value = requestId
  processDefinitionLoading.value = true
  processDefinitionLoadFailed.value = false
  processDefinitionList.value = []
  try {
    const data = await getSimpleProcessDefinitionList()
    if (requestId !== processDefinitionRequestId.value) {
      return
    }
    processDefinitionList.value = Array.isArray(data) ? data : []
  } catch {
    if (requestId !== processDefinitionRequestId.value) {
      return
    }
    processDefinitionLoadFailed.value = true
    message.warning('加载流程定义失败，可手动输入流程定义Key')
  } finally {
    if (requestId === processDefinitionRequestId.value) {
      processDefinitionLoading.value = false
    }
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批方案' : '编辑审批方案'
  formType.value = type
  resetForm()
  void loadProcessDefinitions()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await ApprovalSchemeApi.getScheme(id)
      formData.value = {
        id: data.id,
        versionId: data.latestVersionId,
        name: data.name,
        code: data.code,
        moduleCode: data.moduleCode,
        bizType: data.bizType,
        remark: data.remark,
        designJson: data.designJson || '{"nodes":[]}',
        notifyJson: undefined,
        rules: (data.rules ?? []).map((rule) => ({ ...rule }))
      }
      if (!formData.value.rules.length) {
        formData.value.rules.push(emptyRule())
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    versionId: undefined,
    name: '',
    code: '',
    moduleCode: '',
    bizType: '',
    remark: undefined,
    designJson: '{"nodes":[]}',
    notifyJson: undefined,
    rules: [{ ...emptyRule(), defaultRule: true }]
  }
  Object.keys(flowStatusByRule).forEach((key) => delete flowStatusByRule[Number(key)])
  processInputRefs.clear()
  activeRuleIndex.value = -1
  flowDesignerVisible.value = false
  formRef.value?.clearValidate()
}

/** 规则操作 */
const addRule = () => {
  formData.value.rules.push(emptyRule())
}
const removeRule = (index: number) => {
  if (formData.value.rules.length <= 1) {
    return
  }
  formData.value.rules.splice(index, 1)
  if (!formData.value.rules.some((rule) => rule.defaultRule)) {
    formData.value.rules[0].defaultRule = true
  }
  Object.keys(flowStatusByRule).forEach((key) => delete flowStatusByRule[Number(key)])
  processInputRefs.clear()
}
const handleDefaultRuleChange = (index: number, checked: boolean) => {
  if (checked) {
    formData.value.rules.forEach((rule, ruleIndex) => {
      rule.defaultRule = ruleIndex === index
    })
  }
}

const openFlowDesigner = (index: number) => {
  if (formLoading.value || submitting.value) {
    return
  }
  if (!formData.value.code || !formData.value.name) {
    message.warning('请先完善方案编码和方案名称')
    return
  }
  activeRuleIndex.value = index
  flowDesignerVisible.value = true
}

const handleFlowPublished = (processKey: string) => {
  if (activeRuleIndex.value < 0) {
    return
  }
  const rule = formData.value.rules[activeRuleIndex.value]
  if (rule) {
    rule.processJson = processKey
  }
  void loadProcessDefinitions()
}

const handleFlowStatusChange = (status: FlowDesignerStatus) => {
  if (activeRuleIndex.value >= 0) {
    flowStatusByRule[activeRuleIndex.value] = status
  }
}

const handleProcessKeyChange = (index: number) => {
  delete flowStatusByRule[index]
}

const setProcessInputRef = (index: number, instance: unknown) => {
  if (instance && typeof instance === 'object') {
    processInputRefs.set(index, instance as ProcessInputInstance)
  } else {
    processInputRefs.delete(index)
  }
}

const canConfirmProcessKey = (index: number) =>
  !formLoading.value &&
  !submitting.value &&
  Boolean(formData.value.rules[index]?.processJson?.trim())

const confirmProcessKey = (index: number) => {
  const rule = formData.value.rules[index]
  const processKey = rule?.processJson?.trim()
  if (!rule || !processKey || !canConfirmProcessKey(index)) {
    return
  }
  rule.processJson = processKey
  handleProcessKeyChange(index)
  processInputRefs.get(index)?.blur?.()
}

const getFlowStatusText = (index: number) => {
  const rule = formData.value.rules[index]
  const status = flowStatusByRule[index]
  if (status?.saved && !status.published) {
    return '流程待发布'
  }
  return rule?.processJson?.trim() ? '已绑定流程定义' : '流程未配置'
}

const getFlowStatusType = (index: number) => {
  const text = getFlowStatusText(index)
  if (text === '流程待发布') {
    return 'warning'
  }
  if (text === '已绑定流程定义') {
    return 'success'
  }
  return 'muted'
}

/** 提交表单 */
const submitForm = async () => {
  if (!canSubmit.value) {
    return
  }
  submitting.value = true
  try {
    await formRef.value.validate()
    const defaultRuleCount = formData.value.rules.filter((rule) => rule.defaultRule).length
    if (defaultRuleCount === 0) {
      message.error('必须存在一条默认规则')
      return
    }
    if (defaultRuleCount > 1) {
      message.error('默认规则只能有一条')
      return
    }
    if (hasUnpublishedFlow.value) {
      message.error('请先发布已修改的审批流程')
      return
    }
    const invalidProcessKeyIndex = formData.value.rules.findIndex(
      (rule) => !rule.processJson?.trim()
    )
    if (invalidProcessKeyIndex >= 0) {
      message.error(`第 ${invalidProcessKeyIndex + 1} 条规则的流程定义Key不能为空`)
      return
    }
    if (formType.value === 'create') {
      await ApprovalSchemeApi.createDraft(formData.value)
    } else {
      await ApprovalSchemeApi.updateDraft(formData.value)
    }
    message.success('操作成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.approval-scheme-form {
  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0 16px;

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  &__rules-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__rules-tip {
    font-size: 12px;
    color: var(--erp-slate-400);
  }

  &__process-state {
    margin-bottom: 8px;
    color: var(--erp-slate-500);
    font-size: 12px;

    &--error {
      color: var(--erp-danger-600);
    }
  }

  &__rule {
    margin-bottom: 12px;
    padding: 12px;
    border: 1px solid var(--erp-slate-100);
    border-radius: 8px;
    background: var(--erp-slate-50);
  }

  &__rule-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__rule-index {
    font-size: 13px;
    font-weight: 600;
    color: var(--erp-slate-600);
  }

  &__rule-meta {
    padding-bottom: 4px;
  }

  &__rule-meta-fields {
    display: grid;
    grid-template-columns: minmax(0, 1.5fr) minmax(160px, 1fr) minmax(120px, 0.75fr);
    gap: 0 16px;
  }

  &__rule-meta-flags {
    display: flex;
    align-items: center;
    gap: 28px;
    margin-top: 2px;

    :deep(.el-form-item) {
      margin-bottom: 8px;
    }

    :deep(.el-form-item__label) {
      white-space: nowrap;
    }
  }

  &__rule-section {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--erp-slate-200);
  }

  &__rule-section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
  }

  &__rule-section-title {
    color: var(--erp-slate-700);
    font-size: 13px;
    font-weight: 600;
  }

  &__process-field {
    display: grid;
    grid-template-columns: 90px minmax(0, 1fr);
    align-items: start;
    gap: 12px;
  }

  &__process-label {
    padding-top: 8px;
    color: var(--erp-slate-600);
    font-size: 14px;
    line-height: 20px;
    text-align: right;
    white-space: nowrap;
  }

  &__process-editor {
    display: grid;
    grid-template-columns: minmax(240px, 1fr) auto auto;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__json-input {
    width: 100%;
    min-width: 0;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }

  &__process-confirm,
  &__process-design {
    flex: 0 0 auto;
    margin-left: 0;
  }

  &__flow-status {
    display: inline-flex;
    align-items: center;
    min-height: 24px;
    padding: 2px 8px;
    border: 1px solid transparent;
    border-radius: 999px;
    font-size: 12px;
    white-space: nowrap;

    &--success {
      border-color: var(--erp-success-50);
      background: var(--erp-success-50);
      color: var(--erp-success-600);
    }

    &--warning {
      border-color: var(--erp-warning-50);
      background: var(--erp-warning-50);
      color: var(--erp-warning-600);
    }

    &--muted {
      border-color: var(--erp-slate-200);
      background: var(--erp-slate-100);
      color: var(--erp-slate-500);
    }
  }

  &__condition {
    min-width: 0;
  }

  @media (max-width: 900px) {
    &__rule-meta-fields {
      grid-template-columns: minmax(0, 1.4fr) minmax(150px, 1fr);
    }

    &__rule-meta-fields :deep(.el-form-item:last-child) {
      grid-column: 1 / -1;
    }
  }

  @media (max-width: 640px) {
    &__rule-meta-fields {
      grid-template-columns: 1fr;
    }

    &__rule-meta-fields :deep(.el-form-item:last-child) {
      grid-column: auto;
    }

    &__rule-meta-flags {
      gap: 16px;
    }
  }

  @media (max-width: 480px) {
    &__process-field {
      grid-template-columns: 1fr;
      gap: 4px;
    }

    &__process-label {
      padding-top: 0;
      text-align: left;
    }

    &__process-editor {
      grid-template-columns: 1fr;
    }

    &__process-confirm,
    &__process-design {
      width: 100%;
    }
  }
}

.approval-scheme-form-dialog {
  :deep(.el-dialog__body) {
    max-height: calc(100vh - 180px);
    overflow-y: auto;
  }
}
</style>
