<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="900px" :fullscreen="false">
    <el-tabs v-model="activeTab">
      <!-- 基础信息 Tab -->
      <el-tab-pane label="基础信息" name="basic">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          v-loading="formLoading"
        >
          <el-form-item label="方案编码" prop="code">
            <el-input
              v-model="formData.code"
              placeholder="请输入方案编码，如 purchase_apply"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item label="方案名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入方案名称" />
          </el-form-item>
          <el-form-item label="模块编码" prop="moduleCode">
            <el-input v-model="formData.moduleCode" placeholder="请输入模块编码，如 erp_purchase" />
          </el-form-item>
          <el-form-item label="业务类型" prop="bizType">
            <el-input v-model="formData.bizType" placeholder="请输入业务类型，如 purchase_apply" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 流程设计 Tab -->
      <el-tab-pane label="流程设计" name="designer">
        <div class="designer-container" style="height: 500px; overflow: auto;">
          <SimpleProcessDesigner
            :model-form-id="undefined"
            :model-form-type="0"
            @success="handleDesignerSuccess"
            ref="designerRef"
          />
        </div>
      </el-tab-pane>

      <!-- 规则配置 Tab -->
      <el-tab-pane label="规则配置" name="rules">
        <div class="mb-4">
          <el-alert
            title="规则说明：每个方案必须有且仅有一个默认规则。processJson 为流程定义 Key，用于启动 BPM 实例。"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
        <div v-if="!formData.rules || formData.rules.length === 0" class="text-center py-8">
          <el-empty description="暂无规则">
            <el-button type="primary" @click="addRule">
              <Icon icon="ep:plus" class="mr-5px" /> 添加规则
            </el-button>
          </el-empty>
        </div>
        <div v-else>
          <div
            v-for="(rule, index) in formData.rules"
            :key="index"
            class="rule-card mb-4 p-4 border border-gray-200 rounded-lg"
          >
            <div class="flex items-center justify-between mb-3">
              <span class="font-bold text-gray-700">规则 {{ index + 1 }}</span>
              <el-button
                type="danger"
                link
                @click="removeRule(index)"
                :disabled="formData.rules.length <= 1"
              >
                <Icon icon="ep:delete" class="mr-2px" /> 删除
              </el-button>
            </div>
            <el-form label-width="100px" :model="rule">
              <el-form-item label="规则名称" required>
                <el-input v-model="rule.ruleName" placeholder="请输入规则名称，如默认规则" />
              </el-form-item>
              <el-form-item label="流程定义 Key" required>
                <el-input v-model="rule.processJson" placeholder="请输入流程定义 Key，如 purchase_approval" />
              </el-form-item>
              <el-form-item label="是否默认">
                <el-radio-group v-model="rule.defaultRule">
                  <el-radio :value="true">是</el-radio>
                  <el-radio :value="false">否</el-radio>
                </el-radio-group>
                <div class="text-xs text-gray-400 mt-1">每个方案必须有且仅有一个默认规则</div>
              </el-form-item>
              <el-form-item label="启用状态">
                <el-switch v-model="rule.enabled" active-text="启用" inactive-text="禁用" />
              </el-form-item>

              <!-- 命中条件配置 -->
              <el-divider content-position="left">命中条件</el-divider>
              <div class="text-xs text-gray-400 mb-4">配置该规则的命中条件，不配置则为默认规则（兜底）</div>

              <!-- 金额区间 -->
              <el-form-item label="金额区间">
                <div class="flex items-center gap-2">
                  <el-input-number
                    v-model="getCondition(rule).amountMin"
                    :min="0"
                    :precision="2"
                    placeholder="最小金额"
                    class="!w-150px"
                  />
                  <span class="text-gray-400">~</span>
                  <el-input-number
                    v-model="getCondition(rule).amountMax"
                    :min="0"
                    :precision="2"
                    placeholder="最大金额"
                    class="!w-150px"
                  />
                </div>
              </el-form-item>

              <!-- 单据类型 -->
              <el-form-item label="单据类型">
                <el-input
                  v-model="getCondition(rule).documentType"
                  placeholder="如：NORMAL, RETURN"
                  class="!w-300px"
                />
                <div class="text-xs text-gray-400 mt-1">多个类型用逗号分隔</div>
              </el-form-item>

              <!-- 部门范围 -->
              <el-form-item label="部门范围">
                <el-input
                  v-model="getCondition(rule).deptIds"
                  placeholder="如：100, 200, 300"
                  class="!w-300px"
                />
                <div class="text-xs text-gray-400 mt-1">部门 ID，多个用逗号分隔，留空表示不限制</div>
              </el-form-item>

              <!-- 发起人范围 -->
              <el-form-item label="发起人范围">
                <el-input
                  v-model="getCondition(rule).userIds"
                  placeholder="如：1, 2, 3"
                  class="!w-300px"
                />
                <div class="text-xs text-gray-400 mt-1">用户 ID，多个用逗号分隔，留空表示不限制</div>
              </el-form-item>
            </el-form>
          </div>
          <div class="text-center mt-4">
            <el-button type="primary" plain @click="addRule">
              <Icon icon="ep:plus" class="mr-5px" /> 添加规则
            </el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 通知配置 Tab -->
      <el-tab-pane label="通知配置" name="notify">
        <div class="mb-4">
          <el-alert
            title="配置审批流程中的通知策略。支持的模板变量：{bizTitle} 业务标题、{bizNo} 业务编号、{startUser} 发起人、{reason} 审批原因"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <!-- 待办提醒 -->
        <div class="notification-section mb-6 p-4 border border-gray-200 rounded-lg">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center">
              <Icon icon="ep:bell" class="mr-2 text-blue-500" />
              <span class="font-bold text-gray-700">待办提醒</span>
              <span class="text-xs text-gray-400 ml-2">审批任务创建时通知审批人</span>
            </div>
            <el-switch
              v-model="notificationConfig.taskCreated.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
          <div v-if="notificationConfig.taskCreated.enabled">
            <el-form label-width="100px">
              <el-form-item label="标题模板">
                <el-input
                  v-model="notificationConfig.taskCreated.titleTemplate"
                  placeholder="{bizTitle} 待您审批"
                />
              </el-form-item>
              <el-form-item label="内容模板">
                <el-input
                  v-model="notificationConfig.taskCreated.contentTemplate"
                  type="textarea"
                  :rows="2"
                  placeholder="{startUser} 提交了 {bizTitle}，请及时处理"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 审批通过提醒 -->
        <div class="notification-section mb-6 p-4 border border-gray-200 rounded-lg">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center">
              <Icon icon="ep:circle-check" class="mr-2 text-emerald-500" />
              <span class="font-bold text-gray-700">审批通过提醒</span>
              <span class="text-xs text-gray-400 ml-2">审批通过时通知发起人</span>
            </div>
            <el-switch
              v-model="notificationConfig.approve.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
          <div v-if="notificationConfig.approve.enabled">
            <el-form label-width="100px">
              <el-form-item label="标题模板">
                <el-input
                  v-model="notificationConfig.approve.titleTemplate"
                  placeholder="{bizTitle} 已通过"
                />
              </el-form-item>
              <el-form-item label="内容模板">
                <el-input
                  v-model="notificationConfig.approve.contentTemplate"
                  type="textarea"
                  :rows="2"
                  placeholder="您提交的 {bizTitle} 已审批通过"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 审批驳回提醒 -->
        <div class="notification-section mb-6 p-4 border border-gray-200 rounded-lg">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center">
              <Icon icon="ep:circle-close" class="mr-2 text-rose-500" />
              <span class="font-bold text-gray-700">审批驳回提醒</span>
              <span class="text-xs text-gray-400 ml-2">审批驳回时通知发起人</span>
            </div>
            <el-switch
              v-model="notificationConfig.reject.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </div>
          <div v-if="notificationConfig.reject.enabled">
            <el-form label-width="100px">
              <el-form-item label="标题模板">
                <el-input
                  v-model="notificationConfig.reject.titleTemplate"
                  placeholder="{bizTitle} 已驳回"
                />
              </el-form-item>
              <el-form-item label="内容模板">
                <el-input
                  v-model="notificationConfig.reject.contentTemplate"
                  type="textarea"
                  :rows="2"
                  placeholder="您提交的 {bizTitle} 已被驳回，原因：{reason}"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-tab-pane>

      <!-- 调试区 Tab -->
      <el-tab-pane label="调试数据" name="debug">
        <el-alert title="以下为原始 JSON 数据，仅供调试使用" type="warning" :closable="false" show-icon class="mb-4" />
        <el-form label-width="100px">
          <el-form-item label="设计器 JSON">
            <el-input
              v-model="formData.designJson"
              type="textarea"
              :rows="8"
              readonly
              placeholder="设计器 JSON（自动生成）"
            />
          </el-form-item>
          <el-form-item label="通知配置 JSON">
            <el-input
              v-model="formData.notifyJson"
              type="textarea"
              :rows="4"
              readonly
              placeholder="通知配置 JSON"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, provide } from 'vue'
import type { FormRules } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalSchemeApi from '@/api/bpm/approval/scheme'
import { SimpleProcessDesigner } from '@/components/SimpleProcessDesignerV2/src/'

defineOptions({ name: 'SchemeForm' })

const { message } = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const designerRef = ref()
const activeTab = ref('basic')

// 流程设计器数据（通过 provide 提供给 SimpleProcessDesigner）
const processData = ref<any>(null)
provide('processData', processData)

// 通知配置数据结构
interface NotificationItem {
  enabled: boolean
  titleTemplate: string
  contentTemplate: string
}

interface NotificationConfig {
  taskCreated: NotificationItem
  approve: NotificationItem
  reject: NotificationItem
}

const notificationConfig = reactive<NotificationConfig>({
  taskCreated: {
    enabled: true,
    titleTemplate: '{bizTitle} 待您审批',
    contentTemplate: '{startUser} 提交了 {bizTitle}，请及时处理'
  },
  approve: {
    enabled: true,
    titleTemplate: '{bizTitle} 已通过',
    contentTemplate: '您提交的 {bizTitle} 已审批通过'
  },
  reject: {
    enabled: true,
    titleTemplate: '{bizTitle} 已驳回',
    contentTemplate: '您提交的 {bizTitle} 已被驳回，原因：{reason}'
  }
})

// 将通知配置序列化为 JSON 字符串
const serializeNotificationConfig = (): string => {
  return JSON.stringify(notificationConfig)
}

// 从 JSON 字符串解析通知配置
const deserializeNotificationConfig = (json: string) => {
  if (!json) return
  try {
    const config = JSON.parse(json)
    if (config.taskCreated) {
      Object.assign(notificationConfig.taskCreated, config.taskCreated)
    }
    if (config.approve) {
      Object.assign(notificationConfig.approve, config.approve)
    }
    if (config.reject) {
      Object.assign(notificationConfig.reject, config.reject)
    }
  } catch (e) {
    console.warn('解析 notifyJson 失败:', e)
  }
}

const formData = reactive<ApprovalSchemeApi.ApprovalSchemeSaveReqVO>({
  id: undefined,
  versionId: undefined,
  name: '',
  code: '',
  moduleCode: '',
  bizType: '',
  remark: '',
  designJson: '',
  notifyJson: '',
  rules: []
})

const formRules: FormRules = {
  code: [{ required: true, message: '方案编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '模块编码不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }]
}

// 设计器保存回调
const handleDesignerSuccess = (data: any) => {
  if (data) {
    formData.designJson = JSON.stringify(data)
  }
}

// 规则管理
const addRule = () => {
  if (!formData.rules) {
    formData.rules = []
  }
  const newRule: ApprovalSchemeApi.ApprovalRuleSaveReqVO = {
    ruleName: '',
    ruleType: 'DEFAULT',
    priority: formData.rules.length + 1,
    defaultRule: formData.rules.length === 0, // 第一条规则默认为默认规则
    processJson: '',
    enabled: true
  }
  formData.rules.push(newRule)
}

const removeRule = (index: number) => {
  if (formData.rules && formData.rules.length > 1) {
    const wasDefault = formData.rules[index].defaultRule
    formData.rules.splice(index, 1)
    // 如果删除的是默认规则，将第一条规则设为默认
    if (wasDefault && formData.rules.length > 0) {
      formData.rules[0].defaultRule = true
    }
    // 重新排序优先级
    formData.rules.forEach((rule, i) => {
      rule.priority = i + 1
    })
  }
}

// 条件数据结构
interface RuleCondition {
  amountMin?: number
  amountMax?: number
  documentType?: string
  deptIds?: string
  userIds?: string
}

// 获取或创建规则的条件对象
const conditionCache = new Map<number, RuleCondition>()
const getCondition = (rule: ApprovalSchemeApi.ApprovalRuleSaveReqVO): RuleCondition => {
  const key = rule.priority || 0
  if (!conditionCache.has(key)) {
    // 尝试从 conditionJson 解析
    let condition: RuleCondition = {}
    if (rule.conditionJson) {
      try {
        condition = JSON.parse(rule.conditionJson)
      } catch (e) {
        // ignore
      }
    }
    conditionCache.set(key, condition)
  }
  return conditionCache.get(key)!
}

// 保存时同步 conditionJson
const syncConditionJson = () => {
  if (!formData.rules) return
  formData.rules.forEach((rule) => {
    const condition = conditionCache.get(rule.priority || 0)
    if (condition) {
      // 过滤空值
      const cleaned: Record<string, any> = {}
      if (condition.amountMin !== undefined && condition.amountMin !== null) cleaned.amountMin = condition.amountMin
      if (condition.amountMax !== undefined && condition.amountMax !== null) cleaned.amountMax = condition.amountMax
      if (condition.documentType) cleaned.documentType = condition.documentType
      if (condition.deptIds) cleaned.deptIds = condition.deptIds
      if (condition.userIds) cleaned.userIds = condition.userIds
      rule.conditionJson = Object.keys(cleaned).length > 0 ? JSON.stringify(cleaned) : ''
    }
  })
}

// 校验默认规则唯一性
const validateDefaultRule = (): boolean => {
  if (!formData.rules || formData.rules.length === 0) {
    message.error('请至少添加一条规则')
    return false
  }
  const defaultRules = formData.rules.filter(r => r.defaultRule)
  if (defaultRules.length === 0) {
    message.error('请设置一个默认规则')
    return false
  }
  if (defaultRules.length > 1) {
    message.error('只能有一个默认规则')
    return false
  }
  // 校验规则名称和流程定义 Key 不为空
  for (let i = 0; i < formData.rules.length; i++) {
    const rule = formData.rules[i]
    if (!rule.ruleName?.trim()) {
      message.error(`规则 ${i + 1} 的名称不能为空`)
      return false
    }
    if (!rule.processJson?.trim()) {
      message.error(`规则 ${i + 1} 的流程定义 Key 不能为空`)
      return false
    }
  }
  return true
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批方案' : '编辑审批方案'
  activeTab.value = 'basic'
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      const data = await ApprovalSchemeApi.getApprovalScheme(id)
      Object.assign(formData, data)
      // 确保 rules 数组存在且格式正确
      if (!formData.rules || formData.rules.length === 0) {
        formData.rules = [{
          ruleName: '默认规则',
          ruleType: 'DEFAULT',
          priority: 1,
          defaultRule: true,
          processJson: '',
          enabled: true
        }]
      }
      // 解析 notifyJson 并设置到 notificationConfig
      if (data.notifyJson) {
        deserializeNotificationConfig(data.notifyJson)
      }
      // 解析 designJson 并设置到 processData 供设计器回显
      if (data.designJson) {
        try {
          processData.value = JSON.parse(data.designJson)
        } catch (e) {
          console.warn('解析 designJson 失败:', e)
          processData.value = null
        }
      }
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增模式，默认添加一条规则
    formData.rules = [{
      ruleName: '默认规则',
      ruleType: 'DEFAULT',
      priority: 1,
      defaultRule: true,
      processJson: '',
      enabled: true
    }]
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  // 校验默认规则唯一性
  if (!validateDefaultRule()) {
    activeTab.value = 'rules'
    return
  }
  // 同步条件配置到 conditionJson
  syncConditionJson()
  // 将通知配置序列化到 notifyJson
  formData.notifyJson = serializeNotificationConfig()
  formLoading.value = true
  try {
    if (formData.id) {
      await ApprovalSchemeApi.updateApprovalSchemeDraft(formData)
      message.success('修改成功')
    } else {
      await ApprovalSchemeApi.createApprovalSchemeDraft(formData)
      message.success('新增成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.id = undefined
  formData.versionId = undefined
  formData.name = ''
  formData.code = ''
  formData.moduleCode = ''
  formData.bizType = ''
  formData.remark = ''
  formData.designJson = ''
  formData.notifyJson = ''
  formData.rules = []
  processData.value = null
  // 重置通知配置
  notificationConfig.taskCreated.enabled = true
  notificationConfig.taskCreated.titleTemplate = '{bizTitle} 待您审批'
  notificationConfig.taskCreated.contentTemplate = '{startUser} 提交了 {bizTitle}，请及时处理'
  notificationConfig.approve.enabled = true
  notificationConfig.approve.titleTemplate = '{bizTitle} 已通过'
  notificationConfig.approve.contentTemplate = '您提交的 {bizTitle} 已审批通过'
  notificationConfig.reject.enabled = true
  notificationConfig.reject.titleTemplate = '{bizTitle} 已驳回'
  notificationConfig.reject.contentTemplate = '您提交的 {bizTitle} 已被驳回，原因：{reason}'
  formRef.value?.resetFields()
}

// 预填数据（用于从场景页一键创建方案）
const setPrefillData = (moduleCode: string, bizType: string) => {
  formData.moduleCode = moduleCode
  formData.bizType = bizType
}

defineExpose({ open, setPrefillData })
</script>

<style scoped>
.designer-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}
</style>
