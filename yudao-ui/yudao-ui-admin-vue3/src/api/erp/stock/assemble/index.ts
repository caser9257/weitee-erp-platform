import request from '@/config/axios'

// 占位接口骨架，后续按装配/拆卸单据接口定稿补充字段
export interface StockAssembleVO {
  id?: number
  no?: string
  actionType?: 'ASSEMBLE' | 'DISASSEMBLE'
  warehouseId?: number
  projectId?: number
  status?: number
  remark?: string
  createTime?: Date | string
}

export const StockAssembleApi = {
  getStockAssemblePage: async (params: any) => {
    return await request.get({ url: '/erp/stock-assemble/page', params })
  },

  getStockAssemble: async (id: number) => {
    return await request.get({ url: `/erp/stock-assemble/get?id=${id}` })
  },

  createStockAssemble: async (data: StockAssembleVO) => {
    return await request.post({ url: '/erp/stock-assemble/create', data })
  },

  updateStockAssemble: async (data: StockAssembleVO) => {
    return await request.put({ url: '/erp/stock-assemble/update', data })
  }
}
