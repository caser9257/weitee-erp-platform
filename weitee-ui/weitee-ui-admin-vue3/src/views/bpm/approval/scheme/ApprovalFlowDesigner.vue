<template>
  <Dialog
    v-model="visible"
    title="审批流程设计"
    fullscreen
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="approval-flow-designer-dialog"
  >
    <div class="approval-flow-designer">
      <div class="approval-flow-designer__header">
        <div>
          <div class="approval-flow-designer__title">
            {{ flowModel?.name || props.rule?.ruleName || '审批流程' }}
          </div>
          <div class="approval-flow-designer__key">
            {{ flowModel?.key || props.rule?.processJson || '流程未配置' }}
          </div>
        </div>
        <el-tag v-if="flowPublished" type="success" effect="light">流程已发布</el-tag>
        <el-tag v-else-if="flowSaved" type="warning" effect="light">流程待发布</el-tag>
      </div>

      <div v-if="flowLoading" class="approval-flow-designer__state">流程加载中</div>
      <div
        v-else-if="flowError"
        class="approval-flow-designer__state approval-flow-designer__state--error"
      >
        <span>{{ flowError }}</span>
        <el-button type="primary" link :disabled="flowLoading" @click="initialize">
          重试加载
        </el-button>
      </div>
      <div v-else class="approval-flow-designer__canvas">
        <SimpleProcessDesigner
          ref="designerRef"
          :model-name="flowModel?.name"
          :model-form-type="BpmModelFormType.CUSTOM"
          :start-user-ids="flowModel?.startUserIds || []"
          :start-dept-ids="flowModel?.startDeptIds || []"
          @success="handleDesignerChange"
        />
      </div>

      <div class="approval-flow-designer__footer">
        <el-button :disabled="flowSaving || flowPublishing" @click="handleClose">
          返回方案
        </el-button>
        <div class="approval-flow-designer__footer-actions">
          <el-button
            type="primary"
            :loading="flowSaving"
            :disabled="!canSaveFlow || flowPublishing"
            @click="saveFlow"
          >
            保存流程
          </el-button>
          <el-button
            type="success"
            :loading="flowPublishing"
            :disabled="!canPublishFlow || flowSaving"
            @click="publishFlow"
          >
            发布流程
          </el-button>
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import * as ModelApi from '@/api/bpm/model'
import type { BpmApprovalRuleVO } from '@/api/bpm/approval/scheme'
import { BpmModelFormType, BpmModelType } from '@/utils/constants'
import { SimpleProcessDesigner } from '@/components/SimpleProcessDesignerV2/src/'
import { useUserStoreWithOut } from '@/store/modules/user'

defineOptions({ name: 'ApprovalFlowDesigner' })

type ApprovalFlowDesignerStatus = {
  modelId?: string
  processKey?: string
  saved: boolean
  published: boolean
}

type FlowModel = Record<string, any> & {
  id: string
  key: string
  name: string
  simpleModel?: unknown
  processDefinition?: { id?: string }
}

const APPROVAL_PORTAL_PATH = '/approval/portal'
const APPROVAL_MODEL_CATEGORY = 'erp_approval'

const props = defineProps<{
  modelValue: boolean
  schemeCode: string
  schemeName: string
  rule?: BpmApprovalRuleVO
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'published', processKey: string): void
  (e: 'status-change', status: ApprovalFlowDesignerStatus): void
}>()

const message = useMessage()
const userStore = useUserStoreWithOut()
const designerRef = ref()
const processData = ref<unknown>()
const flowModel = ref<FlowModel>()
const flowLoading = ref(false)
const flowSaving = ref(false)
const flowPublishing = ref(false)
const flowError = ref('')
const flowSaved = ref(false)
const flowPublished = ref(false)
const isNewModel = ref(false)

provide('processData', processData)

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const canSaveFlow = computed(
  () => !flowLoading.value && !flowError.value && Boolean(flowModel.value?.id)
)
const canPublishFlow = computed(() => canSaveFlow.value && !flowPublished.value)

const emitStatus = () => {
  emit('status-change', {
    modelId: flowModel.value?.id,
    processKey: flowModel.value?.key,
    saved: flowSaved.value,
    published: flowPublished.value
  })
}

const parseSimpleModel = (value: unknown) => {
  if (!value) {
    return undefined
  }
  if (typeof value !== 'string') {
    return value
  }
  try {
    return JSON.parse(value)
  } catch {
    throw new Error('流程模型数据格式无效')
  }
}

const getModelId = (result: unknown) => {
  if (typeof result === 'string' || typeof result === 'number') {
    return String(result)
  }
  if (result && typeof result === 'object' && 'id' in result) {
    const id = (result as { id?: string | number }).id
    return id === undefined ? undefined : String(id)
  }
  return undefined
}

const buildProcessKey = () => {
  const base = `${props.schemeCode || 'approval'}_${props.rule?.ruleName || 'flow'}`
    .toLowerCase()
    .replace(/[^a-z0-9_]+/g, '_')
    .replace(/^_+|_+$/g, '')
  return `approval_${base || 'flow'}_${Date.now()}`
}

const buildCreatePayload = () => {
  const userId = Number(userStore.getUser.id)
  if (!Number.isFinite(userId) || userId <= 0) {
    throw new Error('当前用户信息无效，无法创建流程模型')
  }
  return {
    key: buildProcessKey(),
    name: `${props.schemeName || '审批方案'}-${props.rule?.ruleName || '审批流程'}`,
    category: APPROVAL_MODEL_CATEGORY,
    type: BpmModelType.SIMPLE,
    formType: BpmModelFormType.CUSTOM,
    formCustomCreatePath: APPROVAL_PORTAL_PATH,
    formCustomViewPath: APPROVAL_PORTAL_PATH,
    visible: true,
    startUserIds: [],
    startDeptIds: [],
    managerUserIds: [userId],
    allowCancelRunningProcess: true,
    allowWithdrawTask: false,
    processIdRule: { enable: false, length: 5 }
  }
}

const loadModel = async (modelId: string, deployed: boolean) => {
  const data = (await ModelApi.getModel(modelId)) as FlowModel
  if (data?.type !== undefined && data.type !== BpmModelType.SIMPLE) {
    throw new Error('当前流程不是精简流程，不能使用此设计器编辑')
  }
  flowModel.value = {
    ...data,
    id: String(data.id),
    key: data.key,
    name: data.name
  }
  processData.value = parseSimpleModel(data.simpleModel)
  flowSaved.value = Boolean(data.simpleModel)
  flowPublished.value = deployed
  emitStatus()
}

const findModelByKey = async (processKey: string) => {
  const list = (await ModelApi.getModelList(undefined)) as FlowModel[]
  return list.find((item) => item.key === processKey)
}

const createModel = async () => {
  const result = await ModelApi.createModel(buildCreatePayload())
  const modelId = getModelId(result)
  if (!modelId) {
    throw new Error('创建流程模型失败，未返回模型编号')
  }
  isNewModel.value = true
  await loadModel(modelId, false)
}

const loadExistingModel = async (processKey: string) => {
  const model = await findModelByKey(processKey)
  if (!model?.id) {
    throw new Error('未找到该流程对应的可编辑模型')
  }
  isNewModel.value = false
  await loadModel(String(model.id), Boolean(model.processDefinition?.id))
}

const initialize = async () => {
  if (!props.modelValue || !props.rule) {
    return
  }
  flowLoading.value = true
  flowError.value = ''
  flowModel.value = undefined
  processData.value = undefined
  flowSaved.value = false
  flowPublished.value = false
  try {
    const processKey = props.rule.processJson?.trim()
    if (processKey) {
      await loadExistingModel(processKey)
    } else {
      await createModel()
    }
  } catch (error: any) {
    flowError.value = error?.message || '流程加载失败'
    emitStatus()
  } finally {
    flowLoading.value = false
  }
}

const handleDesignerChange = (node: unknown) => {
  if (node) {
    processData.value = node
    flowSaved.value = false
    flowPublished.value = false
    emitStatus()
  }
}

const buildUpdatePayload = (simpleModel: unknown) => ({
  ...flowModel.value,
  id: flowModel.value?.id,
  key: flowModel.value?.key,
  name: flowModel.value?.name,
  category: flowModel.value?.category || APPROVAL_MODEL_CATEGORY,
  type: BpmModelType.SIMPLE,
  formType: flowModel.value?.formType || BpmModelFormType.CUSTOM,
  formCustomCreatePath: flowModel.value?.formCustomCreatePath || APPROVAL_PORTAL_PATH,
  formCustomViewPath: flowModel.value?.formCustomViewPath || APPROVAL_PORTAL_PATH,
  visible: flowModel.value?.visible ?? true,
  startUserIds: flowModel.value?.startUserIds || [],
  startDeptIds: flowModel.value?.startDeptIds || [],
  managerUserIds: flowModel.value?.managerUserIds?.length
    ? flowModel.value.managerUserIds
    : [Number(userStore.getUser.id)],
  simpleModel
})

const persistFlow = async () => {
  const simpleModel = await designerRef.value?.getCurrentFlowData?.()
  if (!simpleModel) {
    message.warning('请先完善流程节点配置')
    return false
  }
  flowSaving.value = true
  try {
    await ModelApi.updateModel(buildUpdatePayload(simpleModel))
    processData.value = simpleModel
    flowSaved.value = true
    flowPublished.value = false
    emitStatus()
    message.success('流程保存成功')
    return true
  } catch (error: any) {
    message.error(error?.message || '流程保存失败')
    return false
  } finally {
    flowSaving.value = false
  }
}

const saveFlow = async () => {
  if (!canSaveFlow.value || flowPublishing.value) {
    return
  }
  await persistFlow()
}

const publishFlow = async () => {
  if (!canPublishFlow.value || flowSaving.value || !flowModel.value?.id) {
    return
  }
  flowPublishing.value = true
  try {
    if (!flowSaved.value && !(await persistFlow())) {
      return
    }
    await ModelApi.deployModel(flowModel.value.id)
    flowSaved.value = true
    flowPublished.value = true
    emit('published', flowModel.value.key)
    emitStatus()
    message.success('流程发布成功')
  } catch (error: any) {
    message.error(error?.message || '流程发布失败')
  } finally {
    flowPublishing.value = false
  }
}

const handleClose = async () => {
  if (flowSaving.value || flowPublishing.value) {
    return
  }
  if (isNewModel.value && !flowPublished.value) {
    try {
      await message.confirm('当前流程尚未发布，返回后流程草稿仍会保留', '返回方案')
    } catch {
      return
    }
  }
  visible.value = false
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      void initialize()
    }
  }
)
</script>

<style scoped lang="scss">
.approval-flow-designer {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 96px);
  min-height: 520px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 0 0 12px;
    border-bottom: 1px solid var(--erp-slate-100);
  }

  &__title {
    color: var(--erp-slate-800);
    font-size: 16px;
    font-weight: 600;
  }

  &__key {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 12px;
  }

  &__canvas {
    min-height: 0;
    flex: 1;
    margin-top: 12px;
    overflow: hidden;
    border: 1px solid var(--erp-slate-100);
    border-radius: 8px;
    background: var(--erp-slate-50);
  }

  &__canvas :deep(.simple-process-model-container) {
    min-height: 100%;
  }

  &__state {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    flex: 1;
    color: var(--erp-slate-500);

    &--error {
      flex-direction: column;
      color: var(--erp-danger-600);
    }
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding-top: 12px;
  }

  &__footer-actions {
    display: flex;
    gap: 8px;
  }
}

@media (max-width: 1024px) {
  .approval-flow-designer {
    height: calc(100vh - 80px);
  }
}
</style>
