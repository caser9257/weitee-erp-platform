import type { NoticeBatchUpdateReqVO, NoticeVO } from '@/api/system/notice'
import type { BatchEditableField } from '@/components/BatchEdit/types'

export type NoticeBatchFieldKey = 'title' | 'type' | 'content' | 'status'

export type NoticeBatchEditRequest = {
  ids: number[]
  fieldKey: NoticeBatchFieldKey
  mode: 'overwrite'
  value: string
}

export const noticeBatchEditFields: BatchEditableField[] = [
  {
    key: 'title',
    label: '公告标题',
    editorType: 'input',
    placeholder: '请输入公告标题',
    maxLength: 50
  },
  {
    key: 'type',
    label: '公告类型',
    editorType: 'auto',
    optionStyle: 'select',
    placeholder: '请选择公告类型',
    options: [
      { label: '通知', value: '1' },
      { label: '公告', value: '2' }
    ]
  },
  {
    key: 'content',
    label: '公告内容',
    editorType: 'textarea',
    placeholder: '请输入公告内容',
    maxLength: 500,
    rows: 6
  },
  {
    key: 'status',
    label: '状态',
    editorType: 'auto',
    optionStyle: 'switch',
    options: [
      { label: '开启', value: '0' },
      { label: '关闭', value: '1' }
    ]
  }
]

export const noticeBatchEditFieldKeySet = new Set<NoticeBatchFieldKey>(
  noticeBatchEditFields.map((field) => field.key as NoticeBatchFieldKey)
)

export const isNoticeBatchFieldKey = (fieldKey: string): fieldKey is NoticeBatchFieldKey =>
  noticeBatchEditFieldKeySet.has(fieldKey as NoticeBatchFieldKey)

export const normalizeNoticeBatchFieldKey = (fieldKey?: string): NoticeBatchFieldKey | '' =>
  isNoticeBatchFieldKey(fieldKey || '') ? fieldKey : ''

export const normalizeNoticeBatchValue = (_fieldKey: NoticeBatchFieldKey, value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value)
}

export const resolveNoticeBatchInitialValue = (
  fieldKey: NoticeBatchFieldKey | '',
  rows: NoticeVO[],
  fallbackValue: unknown = ''
) => {
  if (!fieldKey) {
    return ''
  }
  if (!rows.length) {
    return normalizeNoticeBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))

  if (hasSameValue) {
    return normalizeNoticeBatchValue(fieldKey, firstValue)
  }

  return normalizeNoticeBatchValue(fieldKey, fallbackValue)
}

export const noticeBatchEditAdapter = {
  resourceKey: 'system:notice',
  rowKey: 'id',
  fields: noticeBatchEditFields,
  normalizeFieldKey: normalizeNoticeBatchFieldKey,
  resolveInitialValue: resolveNoticeBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: NoticeBatchFieldKey
    value: unknown
  }): NoticeBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizeNoticeBatchValue(params.fieldKey, params.value)
    }
  }
}
