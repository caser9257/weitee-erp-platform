/**
 * BOM 位号（Reference Designator）解析工具（前端镜像，规则与后端 BomDesignatorUtils 保持一致）
 */

function expandRange(prefix: string, startStr: string, endStr: string): string[] {
  const start = parseInt(startStr, 10)
  const end = parseInt(endStr, 10)
  // 非法区间（起止颠倒），保留原串，不丢失信息
  if (end < start) {
    return [`${prefix}${startStr}-${endStr}`]
  }
  const width = startStr.length
  const result: string[] = []
  for (let i = start; i <= end; i++) {
    result.push(prefix + String(i).padStart(width, '0'))
  }
  return result
}

/**
 * 解析位号字符串为位号列表
 * 例："R101-R105, R108, C12" → ["R101","R102","R103","R104","R105","R108","C12"]
 */
export function parseDesignatorList(raw?: string | null): string[] {
  if (!raw || !raw.trim()) {
    return []
  }
  return raw.split(',').flatMap((segment) => {
    const trimmed = segment.trim().replace(/\s+/g, '').toUpperCase()
    if (!trimmed) {
      return []
    }
    const range = /^([A-Za-z]+)(\d+)-(\d+)$/.exec(trimmed)
    if (range) {
      return expandRange(range[1], range[2], range[3])
    }
    const single = /^([A-Za-z]+)(\d+)$/.exec(trimmed)
    if (single) {
      return [trimmed]
    }
    // 不匹配区间/单值形态，原串保留
    return [trimmed]
  })
}

/**
 * 统计位号数量（等于 parseDesignatorList 的结果长度）
 */
export function countDesignators(raw?: string | null): number {
  return parseDesignatorList(raw).length
}
