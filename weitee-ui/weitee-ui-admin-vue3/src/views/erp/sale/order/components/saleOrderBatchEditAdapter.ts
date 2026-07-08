import type { AccountVO } from '@/api/erp/finance/account'
import type {
  SaleOrderBatchUpdateReqVO,
  SaleOrderVO
} from '@/api/erp/sale/order'
import type {
  BatchEditableField,
  BatchEditFieldOption
} from '@/components/BatchEdit/types'

export type SaleOrderBatchFieldKey = 'accountId' | 'orderTime' | 'deliveryDate' | 'remark'

const saleOrderBatchFieldKeySet = new Set<SaleOrderBatchFieldKey>([
  'accountId',
  'orderTime',
  'deliveryDate',
  'remark'
])

const normalizeAccountOptions = (accountOptions: AccountVO[]): BatchEditFieldOption[] =>
  accountOptions.map((item) => ({
    label: item.name,
    value: item.id
  }))

export const createSaleOrderBatchEditFields = (
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
    label: '下单时间',
    editorType: 'datetime',
    placeholder: '请选择下单时间',
    valueFormat: 'YYYY-MM-DD HH:mm:ss'
  },
  {
    key: 'deliveryDate',
    label: '交期',
    editorType: 'date',
    placeholder: '请选择交期',
    valueFormat: 'YYYY-MM-DD'
  },
  {
    key: 'remark',
    label: '备注',
    editorType: 'textarea',
    placeholder: '请输入备注',
    rows: 4
  }
]

export const isSaleOrderBatchFieldKey = (
  fieldKey: string
): fieldKey is SaleOrderBatchFieldKey =>
  saleOrderBatchFieldKeySet.has(fieldKey as SaleOrderBatchFieldKey)

export const normalizeSaleOrderBatchFieldKey = (
  fieldKey?: string
): SaleOrderBatchFieldKey | '' =>
  fieldKey && isSaleOrderBatchFieldKey(fieldKey) ? fieldKey : ''

export const normalizeSaleOrderBatchValue = (
  fieldKey: SaleOrderBatchFieldKey,
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

export const resolveSaleOrderBatchInitialValue = (
  fieldKey: SaleOrderBatchFieldKey | '',
  rows: SaleOrderVO[],
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
      : normalizeSaleOrderBatchValue(fieldKey, fallbackValue)
  }

  const firstValue = rows[0]?.[fieldKey]
  const hasSameValue = rows.every((row) => String(row[fieldKey] ?? '') === String(firstValue ?? ''))
  if (hasSameValue) {
    if (fieldKey === 'accountId') {
      return firstValue === undefined || firstValue === null || firstValue === ''
        ? null
        : Number(firstValue)
    }
    return normalizeSaleOrderBatchValue(fieldKey, firstValue)
  }

  return fieldKey === 'accountId' ? null : normalizeSaleOrderBatchValue(fieldKey, fallbackValue)
}

export const saleOrderBatchEditAdapter = {
  resourceKey: 'erp:sale-order',
  rowKey: 'id',
  fields: createSaleOrderBatchEditFields,
  normalizeFieldKey: normalizeSaleOrderBatchFieldKey,
  resolveInitialValue: resolveSaleOrderBatchInitialValue,
  buildRequest(params: {
    ids: number[]
    fieldKey: SaleOrderBatchFieldKey
    value: unknown
  }): SaleOrderBatchUpdateReqVO {
    return {
      ids: params.ids,
      fieldKey: params.fieldKey,
      mode: 'overwrite',
      value: normalizeSaleOrderBatchValue(params.fieldKey, params.value)
    }
  }
}
