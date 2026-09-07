/**
 * 物料编码沿革展示格式化
 *
 * 规则（验证 D 口径）：改过码的物料显示「新编码（旧：旧编码）」；没有旧编码直接显示新编码。
 * 编码为空返回占位符。
 */
export function formatMaterialCode(code?: string | null, prevCode?: string | null): string {
  if (!code) return '—'
  if (prevCode && prevCode !== code) {
    return `${code}（旧：${prevCode}）`
  }
  return code
}
