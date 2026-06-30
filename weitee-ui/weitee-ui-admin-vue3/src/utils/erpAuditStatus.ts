import { DICT_TYPE, getDictObj } from '@/utils/dict'
import type { ElementPlusInfoType } from '@/types/elementPlus'

const ERP_AUDIT_PROCESS_STATUS = 10

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
  return {
    label: dict?.label || '\u672A\u5BA1\u6838',
    type: normalizeAuditTagType(dict?.colorType)
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
