import request from '@/config/axios'

export interface StockAssembleItemVO {
  id?: number
  productId?: number
  productName?: string
  count?: number
  unitCost?: number
  stockDirection?: number
}

export interface StockAssembleVO {
  id?: number
  no?: string
  actionType?: 'ASSEMBLE' | 'DISASSEMBLE'
  warehouseId?: number
  warehouseName?: string
  productId?: number
  productName?: string
  bomId?: number
  count?: number
  totalCost?: number
  status?: number
  remark?: string
  createTime?: Date | string | number
  actionTypeName?: string
  items?: StockAssembleItemVO[]
}

export interface StockAssemblePageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  actionType?: StockAssembleVO['actionType']
  warehouseId?: number
  productId?: number
  status?: number
}

export const StockAssembleApi = {
  getStockAssemblePage: async (params: StockAssemblePageReqVO) => {
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
  },

  updateStockAssembleStatus: async (id: number, status: number) => {
    return await request.put({
      url: '/erp/stock-assemble/update-status',
      params: { id, status }
    })
  },

  deleteStockAssemble: async (ids: number[]) => {
    return await request.delete({
      url: '/erp/stock-assemble/delete',
      params: { ids: ids.join(',') }
    })
  }
}
