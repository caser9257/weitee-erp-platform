import request from '@/config/axios'

export interface ErpFinanceSubjectBalanceVO {
  id?: number
  ledgerId?: number
  ledgerName?: string
  periodId?: number
  periodCode?: string
  subjectCode?: string
  subjectName?: string
  openingDebitAmount?: number | string
  openingCreditAmount?: number | string
  currentDebitAmount?: number | string
  currentCreditAmount?: number | string
  endingDebitAmount?: number | string
  endingCreditAmount?: number | string
}

export interface ErpFinanceSubjectBalancePageReqVO {
  pageNo?: number
  pageSize?: number
  ledgerId?: number
  periodId?: number
  subjectCode?: string
  subjectName?: string
}

export interface ErpFinanceGeneralLedgerDetailItemVO {
  voucherId?: number
  voucherNo?: string
  voucherTime?: string
  bizType?: number
  bizTypeName?: string
  bizNo?: string
  voucherStatus?: number
  voucherStatusName?: string
  entryNo?: number
  summary?: string
  debitAmount?: number | string
  creditAmount?: number | string
  runningDebitAmount?: number | string
  runningCreditAmount?: number | string
}

export interface ErpFinanceGeneralLedgerDetailVO {
  ledgerId?: number
  ledgerName?: string
  periodId?: number
  periodCode?: string
  subjectCode?: string
  subjectName?: string
  openingDebitAmount?: number | string
  openingCreditAmount?: number | string
  totalDebitAmount?: number | string
  totalCreditAmount?: number | string
  endingDebitAmount?: number | string
  endingCreditAmount?: number | string
  items?: ErpFinanceGeneralLedgerDetailItemVO[]
}

export interface ErpFinanceGeneralLedgerDetailReqVO {
  ledgerId?: number
  periodId?: number
  subjectCode?: string
  voucherTime?: string[]
}

export interface ErpFinanceGeneralLedgerRebuildReqVO {
  ledgerId: number
}

export interface ErpFinanceGeneralLedgerRebuildRespVO {
  ledgerId?: number
  voucherCount?: number
  entryCount?: number
  subjectCount?: number
}

export const FinanceGeneralLedgerApi = {
  getSubjectBalancePage: async (params: ErpFinanceSubjectBalancePageReqVO) => {
    return await request.get({ url: '/erp/finance-general-ledger/subject-balance-page', params })
  },

  getGeneralLedgerDetail: async (params: ErpFinanceGeneralLedgerDetailReqVO) => {
    return await request.get({ url: '/erp/finance-general-ledger/detail', params })
  },

  rebuildSubjectBalance: async (data: ErpFinanceGeneralLedgerRebuildReqVO) => {
    return await request.post<ErpFinanceGeneralLedgerRebuildRespVO>({
      url: '/erp/finance-general-ledger/rebuild-subject-balance',
      data
    })
  }
}
