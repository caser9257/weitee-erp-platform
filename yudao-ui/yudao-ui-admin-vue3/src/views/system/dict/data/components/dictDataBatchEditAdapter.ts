import type { DictDataBatchUpdateReqVO, DictDataVO } from '@/api/system/dict/dict.data'
import type { BatchEditableField } from '@/components/BatchEdit/types'

export type DictDataBatchFieldKey = 'label' | 'sort' | 'status' | 'colorType' | 'remark'

const dictDataBatchFieldKeySet = new Set<DictDataBatchFieldKey>([
  'label',
  'sort',
  'status',
  'colorType',
  'remark'
])

export const dictDataBatchEditFields: BatchEditableField[] = [
  {
    key: 'label',
    label: '字典标签',
    editorType: 'input',
    placeholder: '请输入字典标签',
    maxLength: 100
  },
  {
    key: 'sort',
    label: '显示排序',
    editorType: 'number',
    placeholder: '请输入显示排序',
    min: 0,
    step: 1,
    precision: 0,
    defaultValue: null,
    componentProps: {
      controlsPosition: 'right'
    }
  },
  {
    key: 'status',
    label: '状态',
    editorType: 'auto',
    optionStyle: 'switch',
    options: [
      { label: '开启', value: 0 },
      { label: '关闭', value: 1 }
    ]
  },
  {
    key: 'colorType',
    label: '颜色类型',
    editorType: 'auto',
    optionStyle: 'select',
    placeholder: '请选择颜色类型',
    options: [
      { label: '默认', value: 'default' },
      { label: '主要', value: 'primary' },
      { label: '成功', value: 'success' },
      { label: '信息', value: 'info' },
      { label: '警告', value: 'warning' },
      { label: '危险', value: 'danger' }
    ]
  },
  {
    key: 'remark',
    label: '备注',
    editorType: 'textarea',
    placeholder: '请输入备注',
    maxLength: 500,
    rows: 4
  }
]

export const isDictDataBatchFieldKey = (fieldKey: string): fieldKey is DictDataBatchFieldKey =>
  dictDataBatchFieldKeySet.has(fieldKey as DictDataBatchFieldKey)

export const normalizeDictDataBatchFieldKey = (fieldKey?: string): DictDataBatchFieldKey | '' =>
  fieldKey && isDictDataBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizeDictDataBatchValue = (_fieldKey: DictDataBatchFieldKey, value: unknown): string => {
  if (_fieldKey === 'sort') {
    if (value === undefined || value === null || value === '') {
      return ''
    }
    return String(value)
  }
  if (value === undefined || value === null) {
    return ''
  }
  return String(value)
}

export const resolveDictDataBatchInitialValue = (
  fieldKey: DictDataBatchFieldKey | '',
  rows: DictDataVO[],
  fallbackValue: unknown = ''
) => {
  if (!fieldKey) {
    return ''
  }
  if (!rows.length) {
    return normalizeDictDataBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'sort' || fieldKey === 'status') {
      const numericValue = Number(firstValue)
      return Number.isNaN(numericValue) ? null : numericValue
    }
    return normalizeDictDataBatchValue(fieldKey, firstValue)
  }

  if (fieldKey === 'sort' || fieldKey === 'status') {
    return fallbackValue === undefined || fallbackValue === null || fallbackValue === ''
      ? null
      : Number(fallbackValue)
  }
  return normalizeDictDataBatchValue(fieldKey, fallbackValue)
}

export const dictDataBatchEditAdapter = {
  resourceKey: 'system:dict-data',
  rowKey: 'id',
  fields: dictDataBatchEditFields,
  normalizeFieldKey: normalizeDictDataBatchFieldKey,
  resolveInitialValue: resolveDictDataBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: DictDataBatchFieldKey
    value: unknown
  }): DictDataBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizeDictDataBatchValue(params.fieldKey, params.value)
    }
  }
}
