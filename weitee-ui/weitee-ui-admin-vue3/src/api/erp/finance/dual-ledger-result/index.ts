import request from '@/config/axios'

export interface DualLedgerDiffItemDetailVO {
  diffItemTypeName?: string
  calculationTypeName?: string
  internalAmount?: number
  externalAmount?: number
  diffAmount?: number
  diffRatio?: number
}

export interface DualLedgerResultVO {
  bizNo?: string
  bizId?: number
  bizType?: number
  bizTypeName?: string
  externalVoucherNo?: string
  externalLedgerName?: string
  externalVoucherId?: number
  externalDebitAmount?: number
  externalCreditAmount?: number
  internalVoucherNo?: string
  internalLedgerName?: string
  internalVoucherId?: number
  internalDebitAmount?: number
  internalCreditAmount?: number
  debitAmountDiff?: number
  creditAmountDiff?: number
  consistent?: boolean
  compareStatusName?: string
  issueMessages?: string[]
  diffItemDetails?: DualLedgerDiffItemDetailVO[]
}

export interface DualLedgerResultPageReqVO {
  pageNo?: number
  pageSize?: number
  bizType?: number
  bizNo?: string
  compareStatus?: number
  consistent?: boolean
}

export const DUAL_LEDGER_COMPARE_STATUS_OPTIONS = [
  { label: '一致', value: 0 },
  { label: '金额差异', value: 1 },
  { label: '凭证缺失', value: 2 },
  { label: '科目差异', value: 3 }
]

export const DualLedgerResultApi = {
  getDualLedgerResultPage: async (params: DualLedgerResultPageReqVO) => {
    return await request.get({ url: `/erp/finance-dual-ledger-result/page`, params })
  },
  getDualLedgerResult: async (bizType: number, bizId: number) => {
    return await request.get({ url: `/erp/finance-dual-ledger-result/get`, params: { bizType, bizId } })
  },
  recomputeDualLedgerResult: async (data: { bizType: number; bizId: number }) => {
    return await request.post({ url: `/erp/finance-dual-ledger-result/recompute`, data })
  },
  exportSingleLedger: async (params: {
    bizType: number
    bizId: number
    ledgerSide: 'external' | 'internal'
  }) => {
    return await request.download({ url: `/erp/finance-dual-ledger-result/export-single-ledger`, params })
  }
}
