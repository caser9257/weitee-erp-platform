/**
 * BOM 位号（Reference Designator）解析工具（前端镜像，规则与后端 BomDesignatorUtils 保持一致）
 */

/** 单个区间展开数量上限：超过视为非法区间（保留原串），防御超大区间输入，与后端 MAX_RANGE_EXPAND 一致 */
const MAX_RANGE_EXPAND = 10000

function expandRange(prefix: string, startStr: string, endStr: string, segment: string): string[] {
  const start = parseInt(startStr, 10)
  const end = parseInt(endStr, 10)
  // 非法区间（起止颠倒）或展开规模超限，保留原串，不丢失信息
  if (Number.isNaN(start) || Number.isNaN(end) || end < start || end - start + 1 > MAX_RANGE_EXPAND) {
    return [segment]
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
 * 区间支持两种形态：R101-105（后半段省略前缀）、R101-R105（两侧同前缀）
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
      return expandRange(range[1], range[2], range[3], trimmed)
    }
    // 两侧同前缀区间（如 R101-R105），前缀不一致时不构成区间、保留原串
    const rangeWithPrefix = /^([A-Za-z]+)(\d+)-([A-Za-z]+)(\d+)$/.exec(trimmed)
    if (rangeWithPrefix) {
      if (rangeWithPrefix[1] !== rangeWithPrefix[3]) {
        return [trimmed]
      }
      return expandRange(rangeWithPrefix[1], rangeWithPrefix[2], rangeWithPrefix[4], trimmed)
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
