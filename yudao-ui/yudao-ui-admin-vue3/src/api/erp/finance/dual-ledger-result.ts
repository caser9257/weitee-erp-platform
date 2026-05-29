import request from '@/config/axios'

export interface DualLedgerDiffItemDetailVO {
  diffItemType?: number
  diffItemTypeName?: string
  calculationType?: number
  calculationTypeName?: string
  internalAmount?: number
  externalAmount?: number
  diffAmount?: number
  diffRatio?: number
}

export interface DualLedgerResultVO {
  bizType?: number
  bizTypeName?: string
  bizId?: number
  bizNo?: string
  compareStatus?: number
  compareStatusName?: string
  consistent?: boolean
  issueMessages?: string[]
  externalLedgerId?: number
  externalLedgerName?: string
  externalVoucherId?: number
  externalVoucherNo?: string
  externalVoucherStatus?: number
  externalDebitAmount?: number
  externalCreditAmount?: number
  internalLedgerId?: number
  internalLedgerName?: string
  internalVoucherId?: number
  internalVoucherNo?: string
  internalVoucherStatus?: number
  internalDebitAmount?: number
  internalCreditAmount?: number
  debitAmountDiff?: number
  creditAmountDiff?: number
  diffItemDetails?: DualLedgerDiffItemDetailVO[]
  voucherTime?: Date | string | number
}

export interface DualLedgerResultPageReqVO {
  pageNo?: number
  pageSize?: number
  bizType?: number
  bizNo?: string
  compareStatus?: number
  consistent?: boolean
}

export interface DualLedgerRecomputeReqVO {
  bizType: number
  bizId: number
}

export interface DualLedgerExportSingleReqVO {
  bizType: number
  bizId: number
  ledgerSide: 'external' | 'internal'
}

export const DUAL_LEDGER_COMPARE_STATUS_OPTIONS = [
  { label: '一致', value: 10 },
  { label: '缺少凭证', value: 20 },
  { label: '金额不一致', value: 30 },
  { label: '状态不一致', value: 40 }
]

export const DualLedgerResultApi = {
  getDualLedgerResultPage: async (params: DualLedgerResultPageReqVO) => {
    return await request.get({ url: '/erp/finance-dual-ledger-result/page', params })
  },

  getDualLedgerResult: async (bizType: number, bizId: number) => {
    return await request.get<DualLedgerResultVO>({
      url: `/erp/finance-dual-ledger-result/get`,
      params: { bizType, bizId }
    })
  },

  recomputeDualLedgerResult: async (data: DualLedgerRecomputeReqVO) => {
    return await request.post<DualLedgerResultVO>({
      url: '/erp/finance-dual-ledger-result/recompute',
      data
    })
  },

  exportSingleLedger: async (params: DualLedgerExportSingleReqVO) => {
    return await request.download({
      url: '/erp/finance-dual-ledger-result/export-single-ledger',
      params
    })
  }
}
