import type { FormItemRule } from 'element-plus'

export type BatchEditMode = 'overwrite' | 'shift_days' | 'delta_add' | 'delta_subtract'

export type BatchEditEditorType =
  | 'auto'
  | 'input'
  | 'textarea'
  | 'select'
  | 'radio'
  | 'switch'
  | 'number'
  | 'date'
  | 'datetime'

export type BatchEditValue = string | number | null | undefined

export interface BatchEditFieldOption {
  label: string
  value: string | number
  disabled?: boolean
}

export interface BatchEditableField {
  key: string
  label: string
  editorType: BatchEditEditorType
  optionStyle?: 'auto' | 'select' | 'radio' | 'switch'
  placeholder?: string
  required?: boolean
  requiredMessage?: string
  invalidMessage?: string
  pattern?: RegExp
  patternMessage?: string
  options?: BatchEditFieldOption[]
  defaultValue?: BatchEditValue
  maxLength?: number
  min?: number
  max?: number
  step?: number
  precision?: number
  rows?: number
  clearable?: boolean
  valueFormat?: string
  componentProps?: Record<string, unknown>
  validate?: (value: BatchEditValue, field: BatchEditableField) => string | void | Promise<string | void>
  format?: (value: BatchEditValue, field: BatchEditableField) => BatchEditValue
  serialize?: (value: BatchEditValue, field: BatchEditableField) => unknown
}

export interface BatchEditSession<Row = unknown> {
  selectedRows: Row[]
  activeFieldKey: string
  draftValue: BatchEditValue
  mode: BatchEditMode
}

const buildRequiredMessage = (field: BatchEditableField) =>
  field.requiredMessage || `${field.label}不能为空`

const buildInvalidMessage = (field: BatchEditableField) =>
  field.invalidMessage || `${field.label}的值不合法`

const isEmptyValue = (value: BatchEditValue) =>
  value === undefined || value === null || String(value).trim().length === 0

export const resolveBatchEditEditorType = (field: BatchEditableField): Exclude<BatchEditEditorType, 'auto'> => {
  if (field.editorType !== 'auto') {
    return field.editorType
  }
  const optionCount = field.options?.length ?? 0
  if (field.optionStyle === 'switch' && optionCount === 2) {
    return 'switch'
  }
  if (field.optionStyle === 'radio') {
    return 'radio'
  }
  if (field.optionStyle === 'select') {
    return 'select'
  }
  if (optionCount > 0 && optionCount <= 3) {
    return 'radio'
  }
  return 'select'
}

export const createBatchEditValueRules = (field: BatchEditableField): FormItemRule[] => {
  const rules: FormItemRule[] = []
  const editorType = resolveBatchEditEditorType(field)

  if (field.required !== false) {
    rules.push({
      required: true,
      message: buildRequiredMessage(field),
      trigger: ['blur', 'change']
    })
  }

  if (editorType === 'input' || editorType === 'textarea') {
    if (field.maxLength && field.maxLength > 0) {
      rules.push({
        validator: (_rule, value, callback) => {
          const text = String(value ?? '')
          if (!isEmptyValue(value) && text.length > field.maxLength!) {
            callback(new Error(field.invalidMessage || `${field.label}不能超过${field.maxLength}个字符`))
            return
          }
          callback()
        },
        trigger: ['blur', 'change']
      })
    }
  }

  if ((editorType === 'select' || editorType === 'radio') && field.options?.length) {
    rules.push({
      validator: (_rule, value, callback) => {
        if (isEmptyValue(value)) {
          callback(field.required === false ? undefined : new Error(buildRequiredMessage(field)))
          return
        }
        const isAllowed = field.options!.some((option) => String(option.value) === String(value))
        if (!isAllowed) {
          callback(new Error(buildInvalidMessage(field)))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    })
  }

  if (editorType === 'switch' && field.options?.length === 2) {
    rules.push({
      validator: (_rule, value, callback) => {
        const allowedValues = field.options!.map((option) => String(option.value))
        if (isEmptyValue(value)) {
          callback(field.required === false ? undefined : new Error(buildRequiredMessage(field)))
          return
        }
        if (!allowedValues.includes(String(value))) {
          callback(new Error(buildInvalidMessage(field)))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    })
  }

  if (editorType === 'number') {
    rules.push({
      validator: (_rule, value, callback) => {
        if (isEmptyValue(value)) {
          callback(field.required === false ? undefined : new Error(buildRequiredMessage(field)))
          return
        }
        const numericValue = Number(value)
        if (Number.isNaN(numericValue)) {
          callback(new Error(buildInvalidMessage(field)))
          return
        }
        if (field.min !== undefined && numericValue < field.min) {
          callback(new Error(field.invalidMessage || `${field.label}不能小于${field.min}`))
          return
        }
        if (field.max !== undefined && numericValue > field.max) {
          callback(new Error(field.invalidMessage || `${field.label}不能大于${field.max}`))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    })
  }

  if (field.pattern && (editorType === 'input' || editorType === 'textarea')) {
    rules.push({
      validator: (_rule, value, callback) => {
        if (isEmptyValue(value)) {
          callback(field.required === false ? undefined : new Error(buildRequiredMessage(field)))
          return
        }
        if (!field.pattern!.test(String(value))) {
          callback(new Error(field.patternMessage || buildInvalidMessage(field)))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    })
  }

  if (field.validate) {
    rules.push({
      validator: async (_rule, value) => {
        const result = await field.validate!(value, field)
        if (typeof result === 'string' && result) {
          throw new Error(result)
        }
      },
      trigger: ['blur', 'change']
    })
  }

  return rules
}
