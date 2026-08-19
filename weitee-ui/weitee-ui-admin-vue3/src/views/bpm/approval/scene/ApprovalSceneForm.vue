<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="560px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <el-form-item label="场景编码" prop="sceneCode">
        <el-input v-model="formData.sceneCode" placeholder="如 erp.finance.payment.submit" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="场景名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入场景名称" />
      </el-form-item>
      <el-form-item label="模块编码" prop="moduleCode">
        <el-input v-model="formData.moduleCode" placeholder="如 erp_finance" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-input v-model="formData.bizType" placeholder="如 payment" />
      </el-form-item>
      <el-form-item label="动作编码" prop="actionCode">
        <el-input v-model="formData.actionCode" placeholder="如 submit" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>

      <!-- 通用审批接入配置（可选） -->
      <el-divider content-position="left">通用接入配置（可选，启用后无代码接入）</el-divider>
      <div v-if="genericEnabled" class="scene-form__generic">
        <div class="scene-form__generic-section">
          <div class="scene-form__generic-label">业务表信息</div>
          <div class="scene-form__generic-grid">
            <el-form-item label="业务表" required>
              <el-input v-model="genericConfig.bizTable" placeholder="如 erp_stock_check" />
            </el-form-item>
            <el-form-item label="主键列" required>
              <el-input v-model="genericConfig.idColumn" placeholder="默认 id" />
            </el-form-item>
            <el-form-item label="状态列" required>
              <el-input v-model="genericConfig.statusColumn" placeholder="默认 status" />
            </el-form-item>
            <el-form-item label="流程实例列">
              <el-input v-model="genericConfig.processInstanceColumn" placeholder="如 process_instance_id（可选）" />
            </el-form-item>
          </div>
        </div>

        <div class="scene-form__generic-section">
          <div class="scene-form__generic-head">
            <span class="scene-form__generic-label">状态映射</span>
          </div>
          <div class="scene-form__generic-kv">
            <div v-for="(value, key) in genericConfig.statusMapping" :key="key" class="scene-form__generic-row">
              <span class="scene-form__generic-key">{{ key }}</span>
              <span class="scene-form__generic-arrow">→</span>
              <el-input-number v-model="genericConfig.statusMapping[key]" :min="-999" :max="999" size="small" controls-position="right" style="width:100px" />
              <el-button link type="danger" @click="deleteStatusMapping(key)">
                <Icon icon="ep:close" />
              </el-button>
            </div>
            <div class="scene-form__generic-row">
              <el-input v-model="newStatusKey" placeholder="如 failed" size="small" style="width:120px" />
              <el-button link type="primary" @click="addStatusMapping">
                <Icon icon="ep:plus" class="mr-5px" /> 添加
              </el-button>
            </div>
          </div>
        </div>

        <div class="scene-form__generic-section">
          <div class="scene-form__generic-head">
            <span class="scene-form__generic-label">上下文字段映射</span>
            <span class="scene-form__generic-hint">变量名 → 业务表列名</span>
          </div>
          <div class="scene-form__generic-kv">
            <div v-for="(col, field) in genericConfig.contextFields" :key="field" class="scene-form__generic-row">
              <el-input :value="field" placeholder="变量名" size="small" style="width:150px" disabled />
              <span class="scene-form__generic-arrow">→</span>
              <el-input :model-value="col" placeholder="业务表列名" size="small" style="width:180px" @change="(v: string) => updateContextField(field, v)" />
              <el-button link type="danger" @click="deleteContextField(field)">
                <Icon icon="ep:close" />
              </el-button>
            </div>
            <div class="scene-form__generic-row">
              <el-input v-model="newContextField" placeholder="如 total_price" size="small" style="width:150px" />
              <el-button link type="primary" @click="addContextField">
                <Icon icon="ep:plus" class="mr-5px" /> 添加
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <el-form-item label="启用通用接入">
        <el-switch v-model="genericEnabled" @change="handleGenericToggle" />
        <span class="scene-form__generic-hint ml-8px">开启后，该场景由通用审批桥驱动，无需业务代码接入</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ApprovalSceneApi, BpmApprovalSceneSaveReqVO } from '@/api/bpm/approval/scene'

/** 审批场景表单 */
defineOptions({ name: 'ApprovalSceneForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const genericEnabled = ref(false)
const newStatusKey = ref('')
const newContextField = ref('')
const genericConfig = ref({
  bizTable: '',
  idColumn: 'id',
  statusColumn: 'status',
  processInstanceColumn: '',
  statusMapping: { submit: 10, approve: 20, reject: 30, cancel: 0, failed: 60 },
  contextFields: {} as Record<string, string>
})
const formData = ref<BpmApprovalSceneSaveReqVO>({
  id: undefined,
  sceneCode: '',
  name: '',
  moduleCode: '',
  bizType: '',
  actionCode: '',
  status: 1,
  remark: undefined,
  genericConfig: undefined
})
const formRules = reactive({
  sceneCode: [{ required: true, message: '场景编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '场景名称不能为空', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '模块编码不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
  actionCode: [{ required: true, message: '动作编码不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})
const formRef = ref()

const emit = defineEmits<{ (e: 'success'): void }>()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增审批场景' : '编辑审批场景'
  formType.value = type
  resetForm()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await ApprovalSceneApi.getScene(id)
      formData.value = {
        id: data.id,
        sceneCode: data.sceneCode,
        name: data.name,
        moduleCode: data.moduleCode,
        bizType: data.bizType,
        actionCode: data.actionCode,
        status: data.status,
        remark: data.remark,
        genericConfig: data.genericConfig
      }
      // 回显通用配置
      if (data.genericConfig) {
        try {
          const cfg = JSON.parse(data.genericConfig)
          genericEnabled.value = true
          genericConfig.value = {
            bizTable: cfg.bizTable || '',
            idColumn: cfg.idColumn || 'id',
            statusColumn: cfg.statusColumn || 'status',
            processInstanceColumn: cfg.processInstanceColumn || '',
            statusMapping: cfg.statusMapping || { submit: 10, approve: 20, reject: 30, cancel: 0, failed: 60 },
            contextFields: cfg.contextFields || {}
          }
        } catch { /* 旧格式忽略 */ }
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 重置表单 */
const resetForm = () => {
  genericEnabled.value = false
  newStatusKey.value = ''
  newContextField.value = ''
  genericConfig.value = {
    bizTable: '',
    idColumn: 'id',
    statusColumn: 'status',
    processInstanceColumn: '',
    statusMapping: { submit: 10, approve: 20, reject: 30, cancel: 0, failed: 60 },
    contextFields: {}
  }
  formData.value = {
    id: undefined,
    sceneCode: '',
    name: '',
    moduleCode: '',
    bizType: '',
    actionCode: '',
    status: 1,
    remark: undefined,
    genericConfig: undefined
  }
  formRef.value?.clearValidate()
}

/** 通用配置操作 */
const addStatusMapping = () => {
  const key = newStatusKey.value.trim()
  if (key && !(key in genericConfig.value.statusMapping)) {
    genericConfig.value.statusMapping[key] = 0
    newStatusKey.value = ''
  }
}
const deleteStatusMapping = (key: string) => {
  delete genericConfig.value.statusMapping[key]
}
const addContextField = () => {
  const col = newContextField.value.trim()
  if (col) {
    genericConfig.value.contextFields['var_' + (Object.keys(genericConfig.value.contextFields).length + 1)] = col
    newContextField.value = ''
  }
}
const updateContextField = (oldKey: string, newVal: string) => {
  const newFields: Record<string, string> = {}
  for (const [k, v] of Object.entries(genericConfig.value.contextFields)) {
    newFields[oldKey] = newVal
  }
  genericConfig.value.contextFields = newFields
}
const deleteContextField = (key: string) => {
  delete genericConfig.value.contextFields[key]
  genericConfig.value.contextFields = { ...genericConfig.value.contextFields }
}
const handleGenericToggle = (val: boolean) => {
  if (!val) {
    formData.value.genericConfig = undefined
  }
}
const buildGenericConfig = (): string | undefined => {
  if (!genericEnabled.value) return undefined
  const cfg = { ...genericConfig.value }
  cfg.statusMapping = { ...cfg.statusMapping }
  cfg.contextFields = { ...cfg.contextFields }
  if (!cfg.bizTable?.trim()) return undefined
  return JSON.stringify(cfg)
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const payload = { ...formData.value, genericConfig: buildGenericConfig() }
    if (formType.value === 'create') {
      await ApprovalSceneApi.createScene(payload)
    } else {
      await ApprovalSceneApi.updateScene(payload)
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
.scene-form__generic {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}
.scene-form__generic-section {
  border: 1px solid var(--erp-slate-100);
  border-radius: 8px;
  padding: 12px;
  background: var(--erp-slate-50);
}
.scene-form__generic-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.scene-form__generic-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--erp-slate-600);
}
.scene-form__generic-hint {
  font-size: 12px;
  color: var(--erp-slate-400);
}
.scene-form__generic-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 12px;
}
.scene-form__generic-kv {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.scene-form__generic-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.scene-form__generic-key {
  width: 120px;
  font-size: 13px;
  color: var(--erp-slate-600);
}
.scene-form__generic-arrow {
  color: var(--erp-slate-400);
}
</style>
