import { ref } from 'vue'
import { ProductApi } from '@/api/erp/product/product'
import { useMessage } from '@/hooks/web/useMessage'

/**
 * 从物料主数据带出全局替代料（启用 + 全局通用），
 * 映射为 BOM 明细替代行并追加到目标行。
 *
 * @returns loadingGlobal 同步 loading 态；importGlobalSubstitutes 执行函数
 */
export function useGlobalSubstituteImport() {
  const message = useMessage()
  const loadingGlobal = ref(false)

  /** 导入到 BOM 明细行，返回实际导入条数（0 表示无动作） */
  const importGlobalSubstitutes = async (row: {
    materialId?: number
    substitutes: { substituteMaterialId?: number; priority?: number; replaceRatio?: number; enableAutoRecommend?: boolean; sort?: number; remark?: string }[]
  }): Promise<number> => {
    if (!row.materialId) {
      message.warning('请先选择物料后再从全局替代料导入')
      return 0
    }
    if (loadingGlobal.value) {
      return 0
    }
    loadingGlobal.value = true
    try {
      const list = await ProductApi.getSubstituteList(row.materialId)
      const validRows = (list || []).filter(
        (item) =>
          item.substituteType === 1 &&
          item.status === 0 &&
          item.substituteProductId !== row.materialId
      )
      if (!validRows.length) {
        message.info('该物料未预设全局替代料')
        return 0
      }
      const existingIds = new Set(
        row.substitutes.map((s) => s.substituteMaterialId).filter(Boolean)
      )
      const newRows = validRows
        .filter((item) => !existingIds.has(item.substituteProductId))
        .map((item) => ({
          substituteMaterialId: item.substituteProductId,
          priority: item.priority ?? 1,
          replaceRatio: item.replaceRatio ?? 1,
          enableAutoRecommend: true,
          sort: item.priority ?? 0,
          remark: item.remark || ''
        }))
      row.substitutes.push(...newRows)
      message.success(`已从全局替代料带出 ${newRows.length} 条`)
      return newRows.length
    } catch {
      message.error('全局替代料加载失败')
      return 0
    } finally {
      loadingGlobal.value = false
    }
  }

  return {
    loadingGlobal,
    importGlobalSubstitutes
  }
}