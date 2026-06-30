import { computed, toValue, type MaybeRefOrGetter } from 'vue'
import type { BatchEditableField, BatchEditValue } from '@/components/BatchEdit/types'
import { createBatchEditValueRules } from '@/components/BatchEdit/types'

export const useBatchEditFields = (fields: MaybeRefOrGetter<BatchEditableField[]>) => {
  const resolvedFields = computed(() => toValue(fields) || [])
  const fieldMap = computed(() =>
    new Map(resolvedFields.value.map((field) => [field.key, field] as const))
  )

  const fieldOptions = computed(() =>
    resolvedFields.value.map((field) => ({
      label: field.label,
      value: field.key
    }))
  )

  const getField = (fieldKey?: string) => {
    if (!fieldKey) {
      return null
    }
    return fieldMap.value.get(fieldKey) ?? null
  }

  const getValueRules = (fieldKey?: string) => {
    const field = getField(fieldKey)
    return field ? createBatchEditValueRules(field) : []
  }

  const getDefaultValue = (fieldKey?: string): BatchEditValue => {
    const field = getField(fieldKey)
    return field?.defaultValue !== undefined ? field.defaultValue : ''
  }

  const isValueReady = (fieldKey: string, value: BatchEditValue) => {
    const field = getField(fieldKey)
    if (!field) {
      return false
    }
    if (field.required === false) {
      return value !== undefined && value !== null
    }
    return String(value ?? '').trim().length > 0
  }

  const serializeValue = (fieldKey: string, value: BatchEditValue) => {
    const field = getField(fieldKey)
    if (!field) {
      return value
    }
    const formattedValue = field.format ? field.format(value, field) : value
    return field.serialize ? field.serialize(formattedValue, field) : formattedValue
  }

  return {
    fieldOptions,
    getDefaultValue,
    getField,
    getValueRules,
    isValueReady,
    serializeValue
  }
}
