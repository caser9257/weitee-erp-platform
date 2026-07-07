import type {
  PurchaseOrderAuditLogVO,
  PurchaseOrderRejectLogVO,
  PurchaseOrderVO
} from '@/api/erp/purchase/order'

const APPROVAL_ACTION_TYPES = new Set(['APPROVE', 'REJECT', 'RESUBMIT', 'CANCEL'])

export const mapRejectLogsToAuditLogs = (
  rejectLogs: PurchaseOrderRejectLogVO[] = []
): PurchaseOrderAuditLogVO[] => {
  return rejectLogs.map((item) => ({
    actionType: 'REJECT',
    reason: item.reason,
    operatorId: item.rejectUserId,
    operatorName: item.rejectUserName,
    operatorNickname: item.rejectUserNickname || item.rejectUserName || item.creatorName,
    createTime: item.rejectTime || item.createTime
  }))
}

const filterMergedAuditLogs = (
  auditLogs: PurchaseOrderAuditLogVO[] = [],
  predicate: (item: PurchaseOrderAuditLogVO) => boolean
) => auditLogs.filter(predicate)

export const resolvePurchaseOrderApprovalLogs = (data?: Partial<PurchaseOrderVO> | null) => {
  if (!data) {
    return [] as PurchaseOrderAuditLogVO[]
  }
  if (data.approvalLogs?.length) {
    return data.approvalLogs
  }
  if (data.auditLogs?.length) {
    const approvalLogs = filterMergedAuditLogs(
      data.auditLogs,
      (item) => !!item.taskName || APPROVAL_ACTION_TYPES.has(item.actionType)
    )
    if (approvalLogs.length) {
      return approvalLogs
    }
  }
  return mapRejectLogsToAuditLogs(data.rejectLogs || [])
}

export const resolvePurchaseOrderOperationLogs = (data?: Partial<PurchaseOrderVO> | null) => {
  if (!data) {
    return [] as PurchaseOrderAuditLogVO[]
  }
  if (data.operationLogs?.length) {
    return data.operationLogs
  }
  if (data.auditLogs?.length) {
    return filterMergedAuditLogs(
      data.auditLogs,
      (item) => !item.taskName && !APPROVAL_ACTION_TYPES.has(item.actionType)
    )
  }
  return [] as PurchaseOrderAuditLogVO[]
}
