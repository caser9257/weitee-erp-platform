/**
 * 财务数据权限工具函数
 */
import { useUserStoreWithOut } from '@/store/modules/user'
import { hasPermission } from '@/directives/permission/hasPermi'

/**
 * 审计角色标识
 */
const AUDIT_ROLE_CODE = 'finance_audit'
const SUPER_ADMIN_ROLE_CODE = 'super_admin'
const ADMIN_ROLE_CODE = 'admin'
const DUAL_LEDGER_RESULT_ROUTE_PATHS = new Set([
  '/finance/dual-ledger-result',
  '/erp/finance/dual-ledger-result'
])
const DUAL_LEDGER_RESULT_COMPONENT = 'erp/finance/dual-ledger-result/index'

const normalizePath = (path?: string): string => {
  if (!path) {
    return ''
  }
  const normalized = path.replace(/\/+/g, '/')
  if (normalized.length > 1 && normalized.endsWith('/')) {
    return normalized.slice(0, -1)
  }
  return normalized
}

/**
 * 检查当前用户是否为审计角色
 */
export function isAuditRole(): boolean {
  const userStore = useUserStoreWithOut()
  const roles = userStore.getRoles
  return roles.includes(AUDIT_ROLE_CODE)
}

/**
 * 检查当前用户是否为管理员角色
 */
export function isAdminRole(): boolean {
  const userStore = useUserStoreWithOut()
  const roles = userStore.getRoles
  return roles.includes(SUPER_ADMIN_ROLE_CODE) || roles.includes(ADMIN_ROLE_CODE)
}

/**
 * 当前用户是否允许访问双账套结果页
 */
export function canAccessDualLedgerResult(): boolean {
  return !isAuditRole() && hasPermission(['erp:finance-dual-ledger-result:query'])
}

/**
 * 当前用户是否允许执行双账套重算
 */
export function canRecomputeDualLedgerResult(): boolean {
  return !isAuditRole() && hasPermission(['erp:finance-dual-ledger-result:recompute'])
}

/**
 * 当前用户是否允许导出双账套凭证
 */
export function canExportDualLedgerResult(): boolean {
  return !isAuditRole() && hasPermission(['erp:finance-dual-ledger-result:export'])
}

/**
 * 是否为双账套结果页路由
 */
export function isDualLedgerResultRoute(path?: string, component?: string): boolean {
  const normalizedPath = normalizePath(path)
  const normalizedComponent = normalizePath(component)
  return (
    DUAL_LEDGER_RESULT_ROUTE_PATHS.has(normalizedPath) ||
    normalizedPath === 'dual-ledger-result' ||
    normalizedComponent === DUAL_LEDGER_RESULT_COMPONENT
  )
}

/**
 * 获取当前用户的可见账簿ID列表
 * 如果返回 null 表示不限制（可看所有账簿）
 */
export function getVisibleLedgerIds(): number[] | null {
  // 管理员不限制
  if (isAdminRole()) {
    return null
  }
  // 审计角色只能看外部账簿（具体账簿ID由后端控制）
  if (isAuditRole()) {
    // 返回空数组，表示由后端控制可见性
    return []
  }
  // 其他角色返回 null，表示不限制
  return null
}
