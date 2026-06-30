import request from '@/config/axios'

export interface ErpFinanceExpenseItemVO {
  id?: number
  itemName?: string
  amount?: number
  remark?: string
  assetCandidateFlag?: boolean
}

export interface ErpFinanceExpenseVO {
  id?: number
  no?: string
  status?: number
  expenseTime?: string
  expenseType?: number
  expenseTypeName?: string
  deptId?: number
  deptName?: string
  projectId?: number
  projectName?: string
  supplierId?: number
  supplierName?: string
  financeUserId?: number
  financeUserName?: string
  accountId?: number
  accountName?: string
  expensePrice?: number
  paidPrice?: number
  remainPrice?: number
  remark?: string
  creator?: string
  creatorName?: string
  createTime?: string
  items?: ErpFinanceExpenseItemVO[]
}

export interface ErpFinanceExpensePageReqVO {
  pageNo?: number
  pageSize?: number
  no?: string
  expenseTime?: string[]
  expenseType?: number
  deptId?: number
  projectId?: number
  supplierId?: number
  financeUserId?: number
  accountId?: number
  status?: number
  remark?: string
}

export interface ErpFinanceExpenseSaveReqVO {
  id?: number
  expenseTime?: string
  expenseType?: number
  deptId?: number
  projectId?: number
  supplierId?: number
  financeUserId?: number
  accountId?: number
  expensePrice?: number
  remark?: string
  items?: Array<{
    id?: number
    itemName?: string
    amount?: number
    remark?: string
    assetCandidateFlag?: boolean
  }>
}

export interface ErpFinanceExpenseProjectSummaryReqVO {
  expenseTime?: string[]
  projectId?: number
  deptId?: number
  expenseType?: number
  status?: number
}

export interface ErpFinanceExpenseProjectSummaryVO {
  projectId?: number
  projectName?: string
  expenseType?: number
  expenseTypeName?: string
  expenseCount?: number
  totalExpensePrice?: number
  totalPaidPrice?: number
  totalRemainPrice?: number
}

export interface ErpFinanceExpenseTraceVO {
  expense?: ErpFinanceExpenseVO
  statement?: ErpFinanceExpenseTraceStatementVO | null
  statementItems?: ErpFinanceExpenseTraceStatementItemVO[]
  allocates?: ErpFinanceExpenseTraceAllocateVO[]
}

export interface ErpFinanceExpenseTraceStatementVO {
  statementId?: number
  statementNo?: string
  bizType?: number
  bizId?: number
  bizNo?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  amount?: number
  paidAmount?: number
  remainAmount?: number
  invoiceStatus?: number
  status?: number
  statusName?: string
  remark?: string
  bizDate?: string
  dueDate?: string
}

export interface ErpFinanceExpenseTraceStatementItemVO {
  id?: number
  itemType?: number
  refId?: number
  refNo?: string
  amount?: number
  afterPaidAmount?: number
  afterRemainAmount?: number
  remark?: string
  createTime?: string
}

export interface ErpFinanceExpenseTraceAllocateVO {
  paymentId?: number
  paymentItemId?: number
  paymentNo?: string
  allocateAmount?: number
  status?: number
  remark?: string
}

export interface ErpFinanceExpenseTypeVO {
  value?: number
  label?: string
  projectRequired?: boolean
}

export const ERP_FINANCE_EXPENSE_STATUS_OPTIONS = [
  { label: '未审核', value: 10 },
  { label: '已审核', value: 20 },
  { label: '已驳回', value: 30 }
]

export const FinanceExpenseApi = {
  getFinanceExpensePage: async (params: ErpFinanceExpensePageReqVO) => {
    return await request.get({ url: '/erp/finance-expense/page', params })
  },

  getFinanceExpense: async (id: number) => {
    return await request.get<ErpFinanceExpenseVO>({ url: `/erp/finance-expense/get?id=${id}` })
  },

  getFinanceExpenseTrace: async (id: number) => {
    return await request.get<ErpFinanceExpenseTraceVO>({ url: `/erp/finance-expense/get-trace?id=${id}` })
  },

  createFinanceExpense: async (data: ErpFinanceExpenseSaveReqVO) => {
    return await request.post({ url: '/erp/finance-expense/create', data })
  },

  updateFinanceExpense: async (data: ErpFinanceExpenseSaveReqVO) => {
    return await request.put({ url: '/erp/finance-expense/update', data })
  },

  updateFinanceExpenseStatus: async (id: number, status: number) => {
    return await request.put({
      url: '/erp/finance-expense/update-status',
      params: { id, status }
    })
  },

  deleteFinanceExpense: async (ids: number[]) => {
    return await request.delete({
      url: '/erp/finance-expense/delete',
      params: { ids: ids.join(',') }
    })
  },

  getFinanceExpenseProjectSummary: async (params: ErpFinanceExpenseProjectSummaryReqVO) => {
    return await request.get<ErpFinanceExpenseProjectSummaryVO[]>({
      url: '/erp/finance-expense/project-summary',
      params
    })
  },

  getFinanceExpenseTypeList: async () => {
    return await request.get<ErpFinanceExpenseTypeVO[]>({ url: '/erp/finance-expense/expense-types' })
  },

  exportFinanceExpense: async (params: ErpFinanceExpensePageReqVO) => {
    return await request.download({ url: '/erp/finance-expense/export-excel', params })
  }
}
