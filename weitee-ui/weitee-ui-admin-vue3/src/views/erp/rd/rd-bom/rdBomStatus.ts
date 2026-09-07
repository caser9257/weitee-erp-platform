/**
 * 研发 BOM 状态派生模型（单一事实来源）
 * 列表页操作栏与详情抽屉的"可编辑/可删除/可提交"等判断必须统一复用本模块，
 * 禁止在各页面各自写 `status !== 20` 这类宽条件，避免分叉导致行为不一致。
 */
import type { RdBomVO } from '@/api/erp/rd/bom'

export const RD_BOM_STATUS = {
  DRAFT: 0,
  APPROVING: 10,
  APPROVED: 20,
  REJECTED: 30,
  VOIDED: 50,
  FAILED: 60
} as const

export const RD_BOM_STATUS_OPTIONS = [
  { label: '草稿', value: RD_BOM_STATUS.DRAFT },
  { label: '审批中', value: RD_BOM_STATUS.APPROVING },
  { label: '已审批', value: RD_BOM_STATUS.APPROVED },
  { label: '已驳回', value: RD_BOM_STATUS.REJECTED },
  { label: '已作废', value: RD_BOM_STATUS.VOIDED },
  { label: '处理失败', value: RD_BOM_STATUS.FAILED }
]

export const STATUS_META: Record<
  number,
  { label: string; type: 'info' | 'success' | 'warning' | 'danger' | '' }
> = {
  [RD_BOM_STATUS.DRAFT]: { label: '草稿', type: 'info' },
  [RD_BOM_STATUS.APPROVING]: { label: '审批中', type: 'warning' },
  [RD_BOM_STATUS.APPROVED]: { label: '已审批', type: 'success' },
  [RD_BOM_STATUS.REJECTED]: { label: '已驳回', type: 'danger' },
  [RD_BOM_STATUS.VOIDED]: { label: '已作废', type: 'info' },
  [RD_BOM_STATUS.FAILED]: { label: '处理失败', type: 'danger' }
}

type RdBomStatusRow = Pick<RdBomVO, 'status' | 'processInstanceId'>
type NullableRdBomStatusRow = RdBomStatusRow | null | undefined

/** 审批流进行中：已处于审批中状态且挂有流程实例。处于此态禁止编辑/删除/重复提交 */
export const isApprovalRunning = (row: NullableRdBomStatusRow) =>
  !!row && row.status === RD_BOM_STATUS.APPROVING && !!row.processInstanceId

export const isVoid = (row: NullableRdBomStatusRow) => row?.status === RD_BOM_STATUS.VOIDED

/** 可编辑/可删除：终态（已审批）与审批中、已作废均不可。仅草稿/已驳回/处理失败 */
export const canEdit = (row: NullableRdBomStatusRow) =>
  !!row && !isApprovalRunning(row) && row.status !== RD_BOM_STATUS.APPROVED && !isVoid(row)

export const canDelete = canEdit

export const canSubmit = (row: NullableRdBomStatusRow) => {
  const submittable = [RD_BOM_STATUS.DRAFT, RD_BOM_STATUS.REJECTED, RD_BOM_STATUS.FAILED] as number[]
  return (
    !!row &&
    submittable.includes(row.status) &&
    !isApprovalRunning(row) &&
    !isVoid(row)
  )
}

export const canCancel = (row: NullableRdBomStatusRow) =>
  isApprovalRunning(row)

export const canPublish = (row: NullableRdBomStatusRow) =>
  !!row && row.status === RD_BOM_STATUS.APPROVED && !isApprovalRunning(row)

export const canChange = (row: NullableRdBomStatusRow) =>
  !!row && row.status === RD_BOM_STATUS.APPROVED && !isApprovalRunning(row)

export const canVoid = canChange

export const canUnvoid = (row: NullableRdBomStatusRow) => isVoid(row)
