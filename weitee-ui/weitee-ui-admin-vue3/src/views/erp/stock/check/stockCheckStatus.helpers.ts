export const STOCK_CHECK_STATUS = {
  DRAFT: 0,
  COUNTING: 10,
  REVIEWING: 20,
  APPROVED: 30,
  CLOSED: 40
} as const

export const resolveStockCheckStatus = (status?: number) => {
  const map = {
    0: { label: '草稿', tone: 'neutral' },
    10: { label: '盘点中', tone: 'primary' },
    20: { label: '审核中', tone: 'warning' },
    30: { label: '已审核', tone: 'primary' },
    40: { label: '已关闭', tone: 'success' }
  } as const
  return map[status as keyof typeof map] ?? { label: '未知状态', tone: 'neutral' }
}

export const deriveStockCheckActions = (status?: number) => ({
  canEdit: status === STOCK_CHECK_STATUS.DRAFT || status === STOCK_CHECK_STATUS.COUNTING,
  canDelete: status === STOCK_CHECK_STATUS.DRAFT,
  canStart: status === STOCK_CHECK_STATUS.DRAFT,
  canSubmit: status === STOCK_CHECK_STATUS.COUNTING,
  canApproveAndClose: status === STOCK_CHECK_STATUS.REVIEWING,
  canReject: status === STOCK_CHECK_STATUS.REVIEWING || status === STOCK_CHECK_STATUS.COUNTING
})
