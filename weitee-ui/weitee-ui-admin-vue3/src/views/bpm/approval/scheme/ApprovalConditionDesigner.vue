<template>
  <div class="approval-condition-designer">
    <div v-if="conditions.length" class="approval-condition-designer__head">
      <div class="approval-condition-designer__logic">
        <span class="approval-condition-designer__logic-label">条件逻辑</span>
        <el-radio-group v-model="logic" size="small" @change="emitChange">
          <el-radio-button value="AND">满足全部 (AND)</el-radio-button>
          <el-radio-button value="OR">满足任一 (OR)</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-if="conditions.length" class="approval-condition-designer__rows">
      <div v-for="(item, index) in conditions" :key="index" class="approval-condition-designer__row">
        <el-select
          v-model="item.field"
          placeholder="选择字段"
          filterable
          allow-create
          default-first-option
          class="approval-condition-designer__field"
          @change="emitChange"
        >
          <el-option v-for="opt in fieldOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select v-model="item.operator" placeholder="操作符" class="approval-condition-designer__operator" @change="emitChange">
          <el-option v-for="opt in operatorOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-input
          v-model="item.value"
          placeholder="请输入条件值"
          clearable
          class="approval-condition-designer__value"
          :type="isRangeOperator(item.operator) ? 'textarea' : 'text'"
          :rows="1"
          @change="emitChange"
        />
        <el-button link type="danger" :disabled="conditions.length <= 1" @click="removeCondition(index)">
          <Icon icon="ep:delete" />
        </el-button>
      </div>
    </div>

    <div class="approval-condition-designer__actions">
      <el-button type="primary" plain size="small" @click="addCondition">
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
          @change="applyAdvancedJson"
        />
        <div class="approval-condition-designer__advanced-tip">高级模式可直接编辑完整 JSON，支持嵌套条件组（AND/OR）</div>
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
  value: any
}

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

const isRangeOperator = (operator: string) => operator === 'in' || operator === 'not_in' || operator === 'between'

const addCondition = () => {
  conditions.value.push({ field: '', operator: '==', value: '' })
}

const removeCondition = (index: number) => {
  conditions.value.splice(index, 1)
  emitChange()
}

const emitChange = () => {
  const payload = { conditions: conditions.value, logic: logic.value }
  const json = JSON.stringify(payload)
  emit('update:modelValue', json)
  advancedJson.value = json
}

/** 应用高级 JSON */
const applyAdvancedJson = () => {
  try {
    const parsed = JSON.parse(advancedJson.value)
    if (parsed && Array.isArray(parsed.conditions)) {
      conditions.value = parsed.conditions.map((c: any) => ({
        field: String(c.field ?? ''),
        operator: String(c.operator ?? '=='),
        value: c.value ?? ''
      }))
      logic.value = String(parsed.logic ?? 'AND').toUpperCase() === 'OR' ? 'OR' : 'AND'
    }
    emit('update:modelValue', advancedJson.value)
  } catch {
    // 非法 JSON 不应用，保留原文由后端兜底
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
      return
    }
    // 内部已同步（由本组件 emit 回写）则跳过重建
    if (val === advancedJson.value) {
      return
    }
    try {
      const parsed = JSON.parse(val)
      if (parsed && Array.isArray(parsed.conditions)) {
        conditions.value = parsed.conditions.map((c: any) => ({
          field: String(c.field ?? ''),
          operator: String(c.operator ?? '=='),
          value: c.value ?? ''
        }))
        logic.value = String(parsed.logic ?? 'AND').toUpperCase() === 'OR' ? 'OR' : 'AND'
      } else {
        conditions.value = []
        logic.value = 'AND'
      }
      advancedJson.value = val
    } catch {
      conditions.value = []
      logic.value = 'AND'
      advancedJson.value = val
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
    display: flex;
    align-items: flex-start;
    gap: 8px;
  }

  &__field {
    flex: 0 0 180px;
  }

  &__operator {
    flex: 0 0 150px;
  }

  &__value {
    flex: 1;
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

  &__advanced-tip {
    margin-top: 4px;
    font-size: 12px;
    color: var(--erp-slate-400);
  }
}
</style>
