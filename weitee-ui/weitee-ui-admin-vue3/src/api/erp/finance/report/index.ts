import request from '@/config/axios'

export interface ErpFinanceReportReqVO {
  ledgerId?: number
  periodId?: number
  subjectCode?: string
  subjectName?: string
}

export interface ErpFinanceTrialBalanceItemVO {
  subjectCode?: string
  subjectName?: string
  openingDebitAmount?: number | string
  openingCreditAmount?: number | string
  currentDebitAmount?: number | string
  currentCreditAmount?: number | string
  endingDebitAmount?: number | string
  endingCreditAmount?: number | string
}

export interface ErpFinanceTrialBalanceVO {
  ledgerId?: number
  ledgerName?: string
  periodId?: number
  periodCode?: string
  totalOpeningDebitAmount?: number | string
  totalOpeningCreditAmount?: number | string
  totalCurrentDebitAmount?: number | string
  totalCurrentCreditAmount?: number | string
  totalEndingDebitAmount?: number | string
  totalEndingCreditAmount?: number | string
  currentBalanced?: boolean
  endingBalanced?: boolean
  subjectCount?: number
  items?: ErpFinanceTrialBalanceItemVO[]
}

export interface ErpFinanceStatementItemVO {
  itemId?: number
  itemCode?: string
  itemName?: string
  itemCategory?: number
  itemCategoryName?: string
  amount?: number | string
  sort?: number
}

export interface ErpFinanceStatementVO {
  ledgerId?: number
  ledgerName?: string
  periodId?: number
  periodCode?: string
  reportType?: number
  reportTypeName?: string
  assetAmount?: number | string
  liabilityAmount?: number | string
  equityAmount?: number | string
  balanceSheetBalanced?: boolean
  revenueAmount?: number | string
  costExpenseAmount?: number | string
  profitAmount?: number | string
  cashInflowAmount?: number | string
  cashOutflowAmount?: number | string
  netCashFlowAmount?: number | string
  items?: ErpFinanceStatementItemVO[]
}

export const FinanceReportApi = {
  getTrialBalance: async (params: ErpFinanceReportReqVO) => {
    return await request.get({ url: '/erp/finance-report/trial-balance', params })
  },

  getBalanceSheet: async (params: ErpFinanceReportReqVO) => {
    return await request.get({ url: '/erp/finance-report/balance-sheet', params })
  },

  getIncomeStatement: async (params: ErpFinanceReportReqVO) => {
    return await request.get({ url: '/erp/finance-report/income-statement', params })
  },

  getCashFlowStatement: async (params: ErpFinanceReportReqVO) => {
    return await request.get({ url: '/erp/finance-report/cash-flow-statement', params })
  }
}
