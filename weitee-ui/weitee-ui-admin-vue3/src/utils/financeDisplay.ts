const HIDDEN_FINANCE_TERMS: Array<[RegExp, string]> = [
  [/双账套/g, '账目'],
  [/双帐套/g, '账目'],
  [/外部账簿/g, '账簿'],
  [/内部账簿/g, '账簿'],
  [/外部帐簿/g, '账簿'],
  [/内部帐簿/g, '账簿'],
  [/外部金额/g, '金额一'],
  [/内部金额/g, '金额二'],
  [/外部来源/g, '账目一来源'],
  [/内部来源/g, '账目二来源'],
  [/外部凭证/g, '凭证一'],
  [/内部凭证/g, '凭证二'],
  [/外部账/g, '账目'],
  [/内部账/g, '账目'],
  [/外部帐/g, '账目'],
  [/内部帐/g, '账目'],
  [/外账/g, '账目'],
  [/内账/g, '账目'],
  [/外帳/g, '账目'],
  [/內帳/g, '账目'],
  [/外部/g, '账目一'],
  [/内部/g, '账目二']
]

const getCachedFinanceRoles = (): string[] => {
  if (typeof window === 'undefined') {
    return []
  }
  try {
    const cachedUser = window.localStorage.getItem('user')
    const userInfo = cachedUser ? JSON.parse(cachedUser) : undefined
    return Array.isArray(userInfo?.roles) ? userInfo.roles : []
  } catch {
    return []
  }
}

export const isExternalOnlyFinanceUser = (roles = getCachedFinanceRoles()): boolean => {
  return roles.includes('finance_audit') && !roles.includes('super_admin')
}

const maskFinanceDisplayText = (value: unknown, fallback = '-'): string => {
  if (value == null || String(value).trim() === '') {
    return fallback
  }
  return HIDDEN_FINANCE_TERMS.reduce(
    (text, [pattern, replacement]) => text.replace(pattern, replacement),
    String(value)
  )
}

/** 仅对只能查看外账的账号隐藏账套分类词。 */
export const toFinanceDisplayText = (value: unknown, fallback = '-', roles?: string[]): string =>
  isExternalOnlyFinanceUser(roles)
    ? maskFinanceDisplayText(value, fallback)
    : value == null || String(value).trim() === ''
      ? fallback
      : String(value)

export const financeDisplayLabel = (normalText: string, maskedText: string, roles?: string[]): string =>
  isExternalOnlyFinanceUser(roles) ? maskedText : normalText

export const displayLedgerName = (value: unknown, fallback = '-'): string => toFinanceDisplayText(value, fallback)

export const displayLedgerSide = (side: 'external' | 'internal'): string =>
  side === 'external' ? '账目一' : '账目二'
