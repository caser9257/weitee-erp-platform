import type { BatchEditableField } from '@/components/BatchEdit/types'
import type {
  WarehouseBatchUpdateReqVO,
  WarehouseBatchFieldKey,
  WarehouseVO
} from '@/api/erp/stock/warehouse'

const warehouseBatchFieldKeySet = new Set<WarehouseBatchFieldKey>([
  'address',
  'principal',
  'remark',
  'status',
  'sort'
])

export const warehouseBatchEditFields: BatchEditableField[] = [
  {
    key: 'address',
    label: '仓库地址',
    editorType: 'input',
    placeholder: '请输入仓库地址',
    maxLength: 50
  },
  {
    key: 'principal',
    label: '负责人',
    editorType: 'input',
    placeholder: '请输入负责人',
    maxLength: 20
  },
  {
    key: 'remark',
    label: '备注',
    editorType: 'textarea',
    placeholder: '请输入备注',
    maxLength: 100,
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

export const isWarehouseBatchFieldKey = (fieldKey: string): fieldKey is WarehouseBatchFieldKey =>
  warehouseBatchFieldKeySet.has(fieldKey as WarehouseBatchFieldKey)

export const normalizeWarehouseBatchFieldKey = (fieldKey?: string): WarehouseBatchFieldKey | '' =>
  fieldKey && isWarehouseBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizeWarehouseBatchValue = (fieldKey: WarehouseBatchFieldKey, value: unknown): string => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  if (fieldKey === 'status' || fieldKey === 'sort') {
    return String(Number(value))
  }
  return String(value)
}

export const resolveWarehouseBatchInitialValue = (
  fieldKey: WarehouseBatchFieldKey | '',
  rows: WarehouseVO[],
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
    return normalizeWarehouseBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'status' || fieldKey === 'sort') {
      return firstValue === undefined || firstValue === null || firstValue === ''
        ? null
        : Number(firstValue)
    }
    return normalizeWarehouseBatchValue(fieldKey, firstValue)
  }

  if (fieldKey === 'status' || fieldKey === 'sort') {
    return fallbackValue === undefined || fallbackValue === null || fallbackValue === ''
      ? null
      : Number(fallbackValue)
  }
  return normalizeWarehouseBatchValue(fieldKey, fallbackValue)
}

export const warehouseBatchEditAdapter = {
  resourceKey: 'erp:warehouse',
  rowKey: 'id',
  fields: warehouseBatchEditFields,
  normalizeFieldKey: normalizeWarehouseBatchFieldKey,
  resolveInitialValue: resolveWarehouseBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: WarehouseBatchFieldKey
    value: unknown
  }): WarehouseBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizeWarehouseBatchValue(params.fieldKey, params.value)
    }
  }
}
