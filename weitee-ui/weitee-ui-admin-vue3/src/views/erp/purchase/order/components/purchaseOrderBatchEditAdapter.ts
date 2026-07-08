import type { AccountVO } from '@/api/erp/finance/account'
import type {
  PurchaseOrderBatchUpdateReqVO,
  PurchaseOrderVO
} from '@/api/erp/purchase/order'
import type {
  BatchEditableField,
  BatchEditFieldOption
} from '@/components/BatchEdit/types'

export type PurchaseOrderBatchFieldKey = 'accountId' | 'orderTime' | 'remark'

const purchaseOrderBatchFieldKeySet = new Set<PurchaseOrderBatchFieldKey>([
  'accountId',
  'orderTime',
  'remark'
])

const normalizeAccountOptions = (accountOptions: AccountVO[]): BatchEditFieldOption[] =>
  accountOptions.map((item) => ({
    label: item.name,
    value: item.id
  }))

export const createPurchaseOrderBatchEditFields = (
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
    key: 'orderTime',
    label: '采购时间',
    editorType: 'datetime',
    placeholder: '请选择采购时间',
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

export const isPurchaseOrderBatchFieldKey = (
  fieldKey: string
): fieldKey is PurchaseOrderBatchFieldKey =>
  purchaseOrderBatchFieldKeySet.has(fieldKey as PurchaseOrderBatchFieldKey)

export const normalizePurchaseOrderBatchFieldKey = (
  fieldKey?: string
): PurchaseOrderBatchFieldKey | '' =>
  fieldKey && isPurchaseOrderBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizePurchaseOrderBatchValue = (
  fieldKey: PurchaseOrderBatchFieldKey,
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

export const resolvePurchaseOrderBatchInitialValue = (
  fieldKey: PurchaseOrderBatchFieldKey | '',
  rows: PurchaseOrderVO[],
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
      : normalizePurchaseOrderBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'accountId') {
      return firstValue === undefined || firstValue === null || firstValue === ''
        ? null
        : Number(firstValue)
    }
    return normalizePurchaseOrderBatchValue(fieldKey, firstValue)
  }

  return fieldKey === 'accountId' ? null : normalizePurchaseOrderBatchValue(fieldKey, fallbackValue)
}

export const purchaseOrderBatchEditAdapter = {
  resourceKey: 'erp:purchase-order',
  rowKey: 'id',
  normalizeFieldKey: normalizePurchaseOrderBatchFieldKey,
  resolveInitialValue: resolvePurchaseOrderBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: PurchaseOrderBatchFieldKey
    value: unknown
  }): PurchaseOrderBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizePurchaseOrderBatchValue(params.fieldKey, params.value)
    }
  }
}
