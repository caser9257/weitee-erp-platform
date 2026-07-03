import { DICT_TYPE, getDictObj } from '@/utils/dict'
import type { ElementPlusInfoType } from '@/types/elementPlus'

const ERP_AUDIT_PROCESS_STATUS = 10
const ERP_AUDIT_STATUS_FALLBACK_MAP: Record<
  number,
  { label: string; type: ElementPlusInfoType }
> = {
  0: { label: '未审核', type: 'info' },
  10: { label: '未审核', type: 'info' },
  20: { label: '已审核', type: 'success' },
  30: { label: '已驳回', type: 'danger' },
  40: { label: '已结转', type: 'info' },
  50: { label: '已作废', type: 'info' },
  60: { label: '处理失败', type: 'danger' }
}

const hasProcessInstance = (processInstanceId?: string | number | null) => {
  return processInstanceId !== undefined && processInstanceId !== null && `${processInstanceId}` !== ''
}

const normalizeAuditTagType = (colorType?: string | null): ElementPlusInfoType => {
  switch (colorType) {
    case 'success':
    case 'info':
    case 'warning':
    case 'danger':
      return colorType
    case 'default':
    case 'primary':
    default:
      return 'info'
  }
}

const resolveErpAuditStatusDisplay = (
  status?: number,
  processInstanceId?: string | number | null
): { label: string; type: ElementPlusInfoType } => {
  if (status === ERP_AUDIT_PROCESS_STATUS && hasProcessInstance(processInstanceId)) {
    return {
      label: '\u5BA1\u6279\u4E2D',
      type: 'warning'
    }
  }
  const dict = getDictObj(DICT_TYPE.ERP_AUDIT_STATUS, status)
  const fallback = status !== undefined ? ERP_AUDIT_STATUS_FALLBACK_MAP[status] : undefined
  return {
    label: dict?.label || fallback?.label || '\u672A\u5BA1\u6838',
    type: dict?.colorType ? normalizeAuditTagType(dict.colorType) : fallback?.type || 'info'
  }
}

export const resolveErpAuditStatusLabel = (
  status?: number,
  processInstanceId?: string | number | null
) => {
  return resolveErpAuditStatusDisplay(status, processInstanceId).label
}

export const resolveErpAuditStatusTagType = (
  status?: number,
  processInstanceId?: string | number | null
) => {
  return resolveErpAuditStatusDisplay(status, processInstanceId).type
}
