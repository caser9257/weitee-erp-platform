<template>
  <div class="approval-condition-designer">
    <div v-if="conditions.length && !advancedOnly" class="approval-condition-designer__head">
      <div class="approval-condition-designer__logic">
        <span class="approval-condition-designer__logic-label">条件逻辑</span>
        <el-radio-group v-model="logic" size="small" @change="emitChange">
          <el-radio-button value="AND">满足全部 (AND)</el-radio-button>
          <el-radio-button value="OR">满足任一 (OR)</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-if="conditions.length && !advancedOnly" class="approval-condition-designer__rows">
      <div
        v-for="(item, index) in conditions"
        :key="index"
        class="approval-condition-designer__row"
      >
        <el-select
          v-model="item.field"
          placeholder="选择字段"
          filterable
          allow-create
          default-first-option
          class="approval-condition-designer__field"
          @change="emitChange"
        >
          <el-option
            v-for="opt in fieldOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-select
          v-model="item.operator"
          placeholder="操作符"
          class="approval-condition-designer__operator"
          @change="emitChange"
        >
          <el-option
            v-for="opt in operatorOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-input
          v-model="item.value"
          placeholder="请输入条件值"
          clearable
          class="approval-condition-designer__value"
          :type="isRangeOperator(item.operator) ? 'textarea' : 'text'"
          :rows="1"
          @input="emitChange"
        />
        <el-button
          class="approval-condition-designer__remove"
          link
          type="danger"
          :disabled="conditions.length <= 1"
          @click="removeCondition(index)"
        >
          <Icon icon="ep:delete" />
        </el-button>
      </div>
    </div>

    <div class="approval-condition-designer__actions">
      <el-button v-if="!advancedOnly" type="primary" plain size="small" @click="addCondition">
        <Icon icon="ep:plus" class="mr-5px" />
        添加条件
      </el-button>
      <el-button link type="primary" size="small" @click="advancedVisible = !advancedVisible">
        {{ advancedVisible ? '收起高级JSON' : '高级JSON' }}
      </el-button>
    </div>

    <el-collapse-transition>
      <div v-if="advancedVisible" class="approval-condition-designer__advanced">
        <el-input
          v-model="advancedJson"
          type="textarea"
          :rows="5"
          placeholder='{"conditions":[{"field":"amount","operator":">","value":10000}],"logic":"AND"}'
          @input="advancedJsonError = ''"
          @change="applyAdvancedJson"
          @blur="applyAdvancedJson"
        />
        <span v-if="advancedJsonError" class="approval-condition-designer__advanced-error">
          {{ advancedJsonError }}
        </span>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup lang="ts">
/** 审批规则条件设计器：可视化编辑 conditionJson */
defineOptions({ name: 'ApprovalConditionDesigner' })

interface ConditionItem {
  field: string
  operator: string
  value: string | number
}

type JsonObject = Record<string, unknown>

const fieldOptions = [
  { value: 'amount', label: '金额 amount' },
  { value: 'deptId', label: '部门 deptId' },
  { value: 'projectId', label: '项目 projectId' },
  { value: 'bizId', label: '业务单号ID bizId' },
  { value: 'bizNo', label: '业务单号 bizNo' },
  { value: 'startUserId', label: '发起人 startUserId' },
  { value: 'organId', label: '组织 organId' }
]

const operatorOptions = [
  { value: '==', label: '等于 ==' },
  { value: '!=', label: '不等于 !=' },
  { value: '>', label: '大于 >' },
  { value: '>=', label: '大于等于 >=' },
  { value: '<', label: '小于 <' },
  { value: '<=', label: '小于等于 <=' },
  { value: 'in', label: '属于 in (逗号分隔)' },
  { value: 'not_in', label: '不属于 not_in (逗号分隔)' },
  { value: 'contains', label: '包含 contains' },
  { value: 'between', label: '区间 between (min,max)' }
]

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const conditions = ref<ConditionItem[]>([])
const logic = ref<'AND' | 'OR'>('AND')
const advancedVisible = ref(false)
const advancedJson = ref('')
const advancedOnly = ref(false)
const advancedJsonError = ref('')

const isRangeOperator = (operator: string) =>
  operator === 'in' || operator === 'not_in' || operator === 'between'
const numericFields = new Set(['amount', 'deptId', 'projectId', 'bizId', 'startUserId', 'organId'])
const numericOperators = new Set([
  '==',
  '!=',
  '>',
  '>=',
  '<',
  '<=',
  'equals',
  'not_equals',
  'gt',
  'gte',
  'lt',
  'lte'
])

const asJsonObject = (value: unknown): JsonObject | undefined => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return undefined
  }
  return value as JsonObject
}

const isLeafCondition = (value: unknown): boolean => {
  const object = asJsonObject(value)
  return typeof object?.field === 'string' && typeof object?.operator === 'string'
}

const isConditionNode = (value: unknown): boolean => {
  const object = asJsonObject(value)
  if (isLeafCondition(object)) {
    return true
  }
  if (!object || !Array.isArray(object.conditions)) {
    return false
  }
  const normalizedLogic = String(object.logic ?? '').toUpperCase()
  return (
    (normalizedLogic === 'AND' || normalizedLogic === 'OR') &&
    object.conditions.every((item) => isConditionNode(item))
  )
}

const isConditionGroup = (value: unknown): boolean => {
  const object = asJsonObject(value)
  return Boolean(object && Array.isArray(object.conditions) && isConditionNode(object))
}

const normalizeValue = (item: ConditionItem) => {
  if (
    !numericFields.has(item.field) ||
    !numericOperators.has(item.operator) ||
    typeof item.value !== 'string'
  ) {
    return item.value
  }
  const value = item.value.trim()
  if (!value) {
    return item.value
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : item.value
}

const toEditorValue = (value: unknown): string | number => {
  if (typeof value === 'number' || typeof value === 'string') {
    return value
  }
  if (Array.isArray(value)) {
    return value.join(',')
  }
  return value == null ? '' : String(value)
}

const applyFlatJson = (parsed: unknown) => {
  const object = asJsonObject(parsed)
  if (
    !object ||
    !Array.isArray(object.conditions) ||
    object.conditions.some((item) => !isLeafCondition(item))
  ) {
    conditions.value = []
    logic.value = 'AND'
    advancedOnly.value = true
    advancedVisible.value = true
    return false
  }
  conditions.value = object.conditions.map((item) => ({
    field: String(item.field ?? ''),
    operator: String(item.operator ?? '=='),
    value: toEditorValue(item.value)
  }))
  logic.value = String(object.logic ?? 'AND').toUpperCase() === 'OR' ? 'OR' : 'AND'
  advancedOnly.value = false
  return true
}

const applyParsedJson = (parsed: unknown) => {
  if (!isConditionGroup(parsed)) {
    advancedJsonError.value = '高级 JSON 格式无效，未应用'
    return false
  }
  if (applyFlatJson(parsed)) {
    advancedJsonError.value = ''
    return true
  }
  // 嵌套条件组由后端递归求值，保留在高级编辑模式，避免丢失结构。
  conditions.value = []
  logic.value = String((parsed as JsonObject).logic).toUpperCase() === 'OR' ? 'OR' : 'AND'
  advancedOnly.value = true
  advancedVisible.value = true
  advancedJsonError.value = ''
  return true
}

const addCondition = () => {
  conditions.value.push({ field: '', operator: '==', value: '' })
  emitChange()
}

const removeCondition = (index: number) => {
  conditions.value.splice(index, 1)
  emitChange()
}

const emitChange = () => {
  if (advancedOnly.value) {
    return
  }
  const serializedConditions = conditions.value.map((item) => ({
    field: item.field,
    operator: item.operator,
    value: normalizeValue(item)
  }))
  const payload = { conditions: serializedConditions, logic: logic.value }
  const json = JSON.stringify(payload)
  emit('update:modelValue', json)
  advancedJson.value = json
  advancedJsonError.value = ''
}

/** 应用高级 JSON */
const applyAdvancedJson = () => {
  try {
    const parsed: unknown = JSON.parse(advancedJson.value)
    if (!applyParsedJson(parsed)) {
      return
    }
    emit('update:modelValue', advancedJson.value)
  } catch {
    advancedJsonError.value = '高级 JSON 格式无效，未应用'
  }
}

/** 从外部传入 JSON 初始化（避免 v-model 回写触发重复重建导致失焦） */
watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      conditions.value = []
      logic.value = 'AND'
      advancedJson.value = ''
      advancedOnly.value = false
      advancedVisible.value = false
      advancedJsonError.value = ''
      return
    }
    // 内部已同步（由本组件 emit 回写）则跳过重建
    if (val === advancedJson.value) {
      return
    }
    try {
      const parsed: unknown = JSON.parse(val)
      if (!applyParsedJson(parsed)) {
        conditions.value = []
        logic.value = 'AND'
        advancedOnly.value = true
        advancedVisible.value = true
      }
      advancedJson.value = val
    } catch {
      conditions.value = []
      logic.value = 'AND'
      advancedJson.value = val
      advancedOnly.value = true
      advancedVisible.value = true
      advancedJsonError.value = '高级 JSON 格式无效，未应用'
    }
  },
  { immediate: true }
)

defineExpose({ emitChange })
</script>

<style scoped lang="scss">
.approval-condition-designer {
  &__head {
    margin-bottom: 8px;
  }

  &__logic {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__logic-label {
    font-size: 12px;
    color: var(--erp-slate-500);
  }

  &__rows {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__row {
    display: grid;
    grid-template-columns: minmax(160px, 0.9fr) minmax(140px, 0.75fr) minmax(180px, 1.5fr) auto;
    align-items: flex-start;
    gap: 8px;
  }

  &__field {
    min-width: 0;
  }

  &__operator {
    min-width: 0;
  }

  &__value {
    min-width: 0;
  }

  &__remove {
    justify-self: end;
  }

  &__advanced-error {
    display: block;
    margin-top: 4px;
    color: var(--erp-danger-600);
    font-size: 12px;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
  }

  &__advanced {
    margin-top: 8px;
  }
}

@media (max-width: 1024px) {
  .approval-condition-designer {
    &__row {
      grid-template-columns: minmax(150px, 1fr) minmax(130px, 0.9fr) auto;
    }

    &__value {
      grid-column: 1 / -1;
    }

    &__remove {
      grid-column: 3;
      grid-row: 1;
    }
  }
}

@media (max-width: 480px) {
  .approval-condition-designer {
    &__row {
      grid-template-columns: 1fr;
    }

    &__value,
    &__remove {
      grid-column: auto;
      grid-row: auto;
    }

    &__remove {
      justify-self: start;
    }
  }
}
</style>
