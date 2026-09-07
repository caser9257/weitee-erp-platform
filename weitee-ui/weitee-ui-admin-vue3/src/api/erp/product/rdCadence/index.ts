import request from '@/config/axios'
import type { ProductVO } from '@/api/erp/product/product'

// 研发物料(Cadence)查询参数
export interface RdCadencePageReqVO {
  pageNo?: number
  pageSize?: number
  materialCode?: string
  name?: string
  categoryId?: number
}

// 研发物料(Cadence) API
export const RdCadenceApi = {
  // 研发物料(Cadence)分页
  getPage: async (params: RdCadencePageReqVO) => {
    return await request.get({ url: `/erp/product/rd-cadence/page`, params })
  },

  // 导出研发物料(Cadence)数据（英文列名）
  exportData: async (params: RdCadencePageReqVO) => {
    return await request.download({ url: `/erp/product/rd-cadence/export`, params })
  },

  // 下载 Cadence 英文表头导入模板
  downloadTemplate: async () => {
    return await request.download({ url: `/erp/product/rd-cadence/template` })
  },

  // 导入预检查（不落库），markAllAsPcb=true 时所有行视为 PCB 元器件
  precheck: async (file: File, markAllAsPcb = false) => {
    return await request.upload({ url: `/erp/product/rd-cadence/import/precheck?markAllAsPcb=${markAllAsPcb}`, timeout: 300000, data: { file } })
  },

  // 执行 Cadence 数据导入，markAllAsPcb=true 时所有行视为 PCB 元器件
  importData: async (file: File, markAllAsPcb = false) => {
    return await request.upload({ url: `/erp/product/rd-cadence/import?markAllAsPcb=${markAllAsPcb}`, timeout: 300000, data: { file } })
  }
}

// 导入结果结构（与后端 ErpProductImportResultVO 对齐）
export interface RdCadenceImportResult {
  totalCount: number
  successCount: number
  failCount: number
  failDetails: { rowNumber: number; barCode?: string; reason?: string }[]
  successCategoryIds: number[]
  ignoredColumns?: string[]
}

export type { ProductVO }
