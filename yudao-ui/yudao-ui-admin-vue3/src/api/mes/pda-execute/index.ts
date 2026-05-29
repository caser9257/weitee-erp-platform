import request from '@/config/axios'

// 占位接口骨架，后续按 PDA 终端扫码交互定稿补充字段
export interface MaterialExecuteReqVO {
  barcode: string
  actionType: 'RECEIVE' | 'RETURN'
  workOrderId?: number
  remark?: string
}

export interface ReportExecuteReqVO {
  barcode: string
  workOrderId?: number
  processCode?: string
  reportQty?: number
  workHours?: number
  remark?: string
}

export const PdaExecuteApi = {
  submitMaterialExecute: async (data: MaterialExecuteReqVO) => {
    return await request.post({ url: '/mes/pda-execute/material-submit', data })
  },

  submitReportExecute: async (data: ReportExecuteReqVO) => {
    return await request.post({ url: '/mes/pda-execute/report-submit', data })
  }
}
