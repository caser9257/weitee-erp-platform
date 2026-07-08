import type { AccountVO } from '@/api/erp/finance/account'
import type {
  PurchaseInBatchUpdateReqVO,
  PurchaseInVO
} from '@/api/erp/purchase/in'
import type {
  BatchEditableField,
  BatchEditFieldOption
} from '@/components/BatchEdit/types'

export type PurchaseInBatchFieldKey = 'accountId' | 'inTime' | 'remark'

const purchaseInBatchFieldKeySet = new Set<PurchaseInBatchFieldKey>([
  'accountId',
  'inTime',
  'remark'
])

const normalizeAccountOptions = (accountOptions: AccountVO[]): BatchEditFieldOption[] =>
  accountOptions.map((item) => ({
    label: item.name,
    value: item.id
  }))

export const createPurchaseInBatchEditFields = (
  accountOptions: AccountVO[]
): BatchEditableField[] => [
  {
    key: 'accountId',
    label: '结算账户',
    editorType: 'select',
    optionStyle: 'select',
    placeholder: '请选择结算账户',
    options: normalizeAccountOptions(accountOptions),
    componentProps: {
      filterable: true
    }
  },
  {
    key: 'inTime',
    label: '入库时间',
    editorType: 'datetime',
    placeholder: '请选择入库时间',
    valueFormat: 'YYYY-MM-DD HH:mm:ss'
  },
  {
    key: 'remark',
    label: '备注',
    editorType: 'textarea',
    placeholder: '请输入备注',
    rows: 4
  }
]

export const isPurchaseInBatchFieldKey = (
  fieldKey: string
): fieldKey is PurchaseInBatchFieldKey =>
  purchaseInBatchFieldKeySet.has(fieldKey as PurchaseInBatchFieldKey)

export const normalizePurchaseInBatchFieldKey = (
  fieldKey?: string
): PurchaseInBatchFieldKey | '' =>
  fieldKey && isPurchaseInBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizePurchaseInBatchValue = (
  fieldKey: PurchaseInBatchFieldKey,
  value: unknown
): string => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  if (fieldKey === 'accountId') {
    return String(Number(value))
  }
  return String(value)
}

export const resolvePurchaseInBatchInitialValue = (
  fieldKey: PurchaseInBatchFieldKey | '',
  rows: PurchaseInVO[],
  fallbackValue: unknown = ''
) => {
  if (!fieldKey) {
    return ''
  }
  if (!rows.length) {
    return fieldKey === 'accountId'
      ? fallbackValue === undefined || fallbackValue === null || fallbackValue === ''
        ? null
        : Number(fallbackValue)
      : normalizePurchaseInBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'accountId') {
      return firstValue === undefined || firstValue === null || firstValue === ''
        ? null
        : Number(firstValue)
    }
    return normalizePurchaseInBatchValue(fieldKey, firstValue)
  }

  return fieldKey === 'accountId' ? null : normalizePurchaseInBatchValue(fieldKey, fallbackValue)
}

export const purchaseInBatchEditAdapter = {
  resourceKey: 'erp:purchase-in',
  rowKey: 'id',
  fields: createPurchaseInBatchEditFields,
  normalizeFieldKey: normalizePurchaseInBatchFieldKey,
  resolveInitialValue: resolvePurchaseInBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: PurchaseInBatchFieldKey
    value: unknown
  }): PurchaseInBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizePurchaseInBatchValue(params.fieldKey, params.value)
    }
  }
}
