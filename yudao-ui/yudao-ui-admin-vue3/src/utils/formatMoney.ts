/**
 * 金额格式化工具函数
 */

/**
 * 格式化金额为中文货币格式
 * @param value 金额值
 * @param decimals 小数位数，默认2位
 * @returns 格式化后的金额字符串，如 ¥1,234.56
 */
export function formatMoney(value?: number | string | null, decimals: number = 2): string {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })}`
}

/**
 * 格式化金额为带符号的中文货币格式
 * @param value 金额值
 * @param decimals 小数位数，默认2位
 * @returns 格式化后的金额字符串，正数带+号，负数带-号
 */
export function formatMoneyWithSign(value?: number | string | null, decimals: number = 2): string {
  const n = Number(value || 0)
  const prefix = n > 0 ? '+' : ''
  return `${prefix}${formatMoney(n, decimals)}`
}

/**
 * 格式化数量（去掉无意义尾零）
 * @param value 数量值
 * @returns 格式化后的数量字符串，如 400 而不是 400.000
 */
export function formatQuantity(value?: number | string | null): string {
  const n = Number(value || 0)
  // 如果是整数，不显示小数位
  if (n === Math.floor(n)) {
    return n.toString()
  }
  // 否则保留2位小数
  return n.toFixed(2)
}

/**
 * 格式化百分比
 * @param value 百分比值（0-100）
 * @param decimals 小数位数，默认1位
 * @returns 格式化后的百分比字符串，如 85.5%
 */
export function formatPercent(value?: number | string | null, decimals: number = 1): string {
  const n = Number(value || 0)
  return `${n.toFixed(decimals)}%`
}
