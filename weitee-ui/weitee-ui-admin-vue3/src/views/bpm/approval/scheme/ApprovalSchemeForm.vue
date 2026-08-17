<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="860px" class="approval-scheme-form-dialog">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <div class="approval-scheme-form__grid">
        <el-form-item label="方案编码" prop="code">
          <el-input v-model="formData.code" placeholder="如 erp.purchase.in.scheme.v1" :disabled="formType === 'update'" />
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
      <div class="approval-scheme-form__rules">
        <div v-for="(rule, index) in formData.rules" :key="index" class="approval-scheme-form__rule">
          <div class="approval-scheme-form__rule-head">
            <span class="approval-scheme-form__rule-index">规则 {{ index + 1 }}</span>
            <el-button link type="danger" :disabled="formData.rules.length <= 1" @click="removeRule(index)">
              <Icon icon="ep:delete" />
            </el-button>
          </div>
          <div class="approval-scheme-form__rule-body">
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
            <el-form-item label="默认规则">
              <el-switch v-model="rule.defaultRule" @change="(checked: boolean) => handleDefaultRuleChange(index, checked)" />
            </el-form-item>
            <el-form-item label="启用">
              <el-switch v-model="rule.enabled" />
            </el-form-item>
          </div>
          <div class="approval-scheme-form__rule-json">
            <el-form-item label="条件配置">
              <ApprovalConditionDesigner v-model="rule.conditionJson" class="approval-scheme-form__condition" />
            </el-form-item>
            <el-form-item label="流程定义Key">
              <el-input
                v-model="rule.processJson"
                placeholder="如 erp_purchase_in_approval（对应 BPM 后台已部署的流程定义 Key）"
                class="approval-scheme-form__json-input"
              />
            </el-form-item>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ApprovalSchemeApi, BpmApprovalRuleVO, BpmApprovalSchemeSaveReqVO } from '@/api/bpm/approval/scheme'
import ApprovalConditionDesigner from './ApprovalConditionDesigner.vue'

/** 审批方案表单 */
defineOptions({ name: 'ApprovalSchemeForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<BpmApprovalSchemeSaveReqVO>({
  id: undefined,
  versionId: undefined,
  name: '',
  code: '',
  moduleCode: '',
  bizType: '',
  remark: undefined,
  designJson: undefined,
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

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批方案' : '编辑审批方案'
  formType.value = type
  resetForm()
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
        designJson: data.designJson,
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
    designJson: undefined,
    notifyJson: undefined,
    rules: [{ ...emptyRule(), defaultRule: true }]
  }
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
}
const handleDefaultRuleChange = (index: number, checked: boolean) => {
  if (checked) {
    formData.value.rules.forEach((rule, ruleIndex) => {
      rule.defaultRule = ruleIndex === index
    })
  }
}

/** 提交表单 */
const submitForm = async () => {
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
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await ApprovalSchemeApi.createDraft(formData.value)
    } else {
      await ApprovalSchemeApi.updateDraft(formData.value)
    }
    message.success('操作成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
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

  &__rule-body {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 0 12px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  &__rule-json {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0 12px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  &__json-input {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }
}
</style>
