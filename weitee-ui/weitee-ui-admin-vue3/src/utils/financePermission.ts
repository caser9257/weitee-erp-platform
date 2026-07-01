import { hasPermission } from '@/directives/permission/hasPermi'

// 财务模块权限校验工具
export function canAccessDualLedgerResult(): boolean {
  return hasPermission(['erp:finance-dual-ledger-result:query'])
}

export function canRecomputeDualLedgerResult(): boolean {
  return hasPermission(['erp:finance-dual-ledger-result:recompute'])
}

export function canExportDualLedgerResult(): boolean {
  return hasPermission(['erp:finance-dual-ledger-result:export'])
}
