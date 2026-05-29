<template>
  <el-select
    v-if="resolvedEditorType === 'select'"
    :model-value="modelValue"
    :placeholder="field?.placeholder || '请选择修改值'"
    :disabled="disabled"
    :clearable="field?.clearable !== false"
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-option
      v-for="item in field.options || []"
      :key="item.value"
      :label="item.label"
      :value="item.value"
      :disabled="item.disabled"
    />
  </el-select>

  <el-radio-group
    v-else-if="resolvedEditorType === 'radio'"
    :model-value="modelValue"
    :disabled="disabled"
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-radio
      v-for="item in field.options || []"
      :key="item.value"
      :label="item.value"
      :disabled="item.disabled"
      border
    >
      {{ item.label }}
    </el-radio>
  </el-radio-group>

  <el-switch
    v-else-if="resolvedEditorType === 'switch'"
    :model-value="modelValue"
    :disabled="disabled"
    :active-value="field?.options?.[0]?.value"
    :inactive-value="field?.options?.[1]?.value"
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  />

  <el-input-number
    v-else-if="resolvedEditorType === 'number'"
    :model-value="numberModelValue"
    :min="field?.min"
    :max="field?.max"
    :step="field?.step"
    :precision="field?.precision"
    :disabled="disabled"
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  />

  <el-date-picker
    v-else-if="resolvedEditorType === 'date' || resolvedEditorType === 'datetime'"
    :model-value="modelValue"
    :type="resolvedEditorType"
    :value-format="field?.valueFormat || (resolvedEditorType === 'date' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:mm:ss')"
    :placeholder="field?.placeholder || '请选择修改值'"
    :disabled="disabled"
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  />

  <el-input
    v-else-if="resolvedEditorType === 'textarea'"
    :model-value="modelValue"
    type="textarea"
    :rows="field?.rows || 4"
    :maxlength="field?.maxLength"
    :placeholder="field?.placeholder || '请输入修改值'"
    :disabled="disabled"
    show-word-limit
    clearable
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  />

  <el-input
    v-else-if="resolvedEditorType === 'input'"
    :model-value="modelValue"
    :maxlength="field?.maxLength"
    :placeholder="field?.placeholder || '请输入修改值'"
    :disabled="disabled"
    clearable
    v-bind="field?.componentProps"
    @update:model-value="emit('update:modelValue', $event)"
  />

  <el-input v-else model-value="" placeholder="请先选择修改字段" disabled />
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { resolveBatchEditEditorType, type BatchEditableField, type BatchEditValue } from '../types'

defineOptions({ name: 'BatchEditFieldEditor' })

const props = withDefaults(
  defineProps<{
    field: BatchEditableField | null
    modelValue: BatchEditValue
    disabled?: boolean
  }>(),
  {
    disabled: false
  }
)

const { field, modelValue, disabled } = toRefs(props)
const numberModelValue = computed(() =>
  typeof modelValue.value === 'number' ? modelValue.value : undefined
)
const resolvedEditorType = computed(() =>
  field.value ? resolveBatchEditEditorType(field.value) : null
)

const emit = defineEmits<{
  (event: 'update:modelValue', value: BatchEditValue): void
}>()
</script>
