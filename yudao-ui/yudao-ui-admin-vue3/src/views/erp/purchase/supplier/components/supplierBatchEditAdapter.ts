import type {
  BatchEditableField
} from '@/components/BatchEdit/types'
import type {
  SupplierBatchUpdateReqVO,
  SupplierVO
} from '@/api/erp/purchase/supplier'

export type SupplierBatchFieldKey =
  | 'contact'
  | 'mobile'
  | 'telephone'
  | 'email'
  | 'fax'
  | 'remark'
  | 'status'
  | 'sort'

const supplierBatchFieldKeySet = new Set<SupplierBatchFieldKey>([
  'contact',
  'mobile',
  'telephone',
  'email',
  'fax',
  'remark',
  'status',
  'sort'
])

export const supplierBatchEditFields: BatchEditableField[] = [
  {
    key: 'contact',
    label: '联系人',
    editorType: 'input',
    placeholder: '请输入联系人',
    maxLength: 100
  },
  {
    key: 'mobile',
    label: '手机号码',
    editorType: 'input',
    placeholder: '请输入手机号码',
    maxLength: 20
  },
  {
    key: 'telephone',
    label: '联系电话',
    editorType: 'input',
    placeholder: '请输入联系电话',
    maxLength: 50
  },
  {
    key: 'email',
    label: '电子邮箱',
    editorType: 'input',
    placeholder: '请输入电子邮箱',
    maxLength: 100
  },
  {
    key: 'fax',
    label: '传真',
    editorType: 'input',
    placeholder: '请输入传真',
    maxLength: 50
  },
  {
    key: 'remark',
    label: '备注',
    editorType: 'textarea',
    placeholder: '请输入备注',
    maxLength: 500,
    rows: 4
  },
  {
    key: 'status',
    label: '开启状态',
    editorType: 'radio',
    options: [
      { label: '开启', value: 0 },
      { label: '关闭', value: 1 }
    ]
  },
  {
    key: 'sort',
    label: '排序',
    editorType: 'number',
    placeholder: '请输入排序',
    min: 0,
    step: 1,
    precision: 0,
    defaultValue: null,
    componentProps: {
      controlsPosition: 'right'
    }
  }
]

export const isSupplierBatchFieldKey = (fieldKey: string): fieldKey is SupplierBatchFieldKey =>
  supplierBatchFieldKeySet.has(fieldKey as SupplierBatchFieldKey)

export const normalizeSupplierBatchFieldKey = (fieldKey?: string): SupplierBatchFieldKey | '' =>
  fieldKey && isSupplierBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizeSupplierBatchValue = (fieldKey: SupplierBatchFieldKey, value: unknown): string => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  if (fieldKey === 'status' || fieldKey === 'sort') {
    return String(Number(value))
  }
  return String(value)
}

export const resolveSupplierBatchInitialValue = (
  fieldKey: SupplierBatchFieldKey | '',
  rows: SupplierVO[],
  fallbackValue: unknown = ''
) => {
  if (!fieldKey) {
    return ''
  }
  if (!rows.length) {
    if (fieldKey === 'status' || fieldKey === 'sort') {
      return fallbackValue === undefined || fallbackValue === null || fallbackValue === ''
        ? null
        : Number(fallbackValue)
    }
    return normalizeSupplierBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'status' || fieldKey === 'sort') {
      return firstValue === undefined || firstValue === null || firstValue === ''
        ? null
        : Number(firstValue)
    }
    return normalizeSupplierBatchValue(fieldKey, firstValue)
  }

  if (fieldKey === 'status' || fieldKey === 'sort') {
    return fallbackValue === undefined || fallbackValue === null || fallbackValue === ''
      ? null
      : Number(fallbackValue)
  }
  return normalizeSupplierBatchValue(fieldKey, fallbackValue)
}

export const supplierBatchEditAdapter = {
  resourceKey: 'erp:supplier',
  rowKey: 'id',
  fields: supplierBatchEditFields,
  normalizeFieldKey: normalizeSupplierBatchFieldKey,
  resolveInitialValue: resolveSupplierBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: SupplierBatchFieldKey
    value: unknown
  }): SupplierBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizeSupplierBatchValue(params.fieldKey, params.value)
    }
  }
}
