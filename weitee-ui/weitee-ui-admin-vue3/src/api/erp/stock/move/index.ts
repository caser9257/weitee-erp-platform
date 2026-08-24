import request from '@/config/axios'

// ERP 库存调拨单 VO
export interface StockMoveVO {
  id: number // 调拨编号
  no: string // 调拨单号
  moveTime: Date // 调拨时间
  totalCount: number // 合计数量
  totalPrice: number // 合计金额，单位：元
  status: number // 状态
  remark: string // 备注
  processInstanceId?: string
}

export interface StockMoveItemVO {
  id?: number
  fromWarehouseId?: number
  toWarehouseId?: number
  productId?: number
  productPrice?: number
  count?: number
  totalPrice?: number
  remark?: string
  fromWarehouseName?: string
  toWarehouseName?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
  stockCount?: number
}

export interface StockMovePrintDataVO {
  stockMove?: StockMoveVO & {
    id?: number
    no?: string
    moveTime?: string | Date | number
    remark?: string
    creatorName?: string
    createTime?: string | Date | number
    fileUrl?: string
    items?: StockMoveItemVO[]
    productNames?: string
    statusName?: string
  }
  sourceAttachments?: Array<{
    name?: string
    url?: string
  }>
}

// ERP 库存调拨单 API
export const StockMoveApi = {
  // 查询库存调拨单分页
  getStockMovePage: async (params: any) => {
    return await request.get({ url: `/erp/stock-move/page`, params })
  },

  // 查询库存调拨单详情
  getStockMove: async (id: number) => {
    return await request.get({ url: `/erp/stock-move/get?id=` + id })
  },

  // 查询库存调拨打印数据
  getStockMovePrintData: async (id: number) => {
    return await request.get<StockMovePrintDataVO>({
      url: `/erp/stock-move/get-print-data?id=${id}`
    })
  },

  // 新增库存调拨单
  createStockMove: async (data: StockMoveVO) => {
    return await request.post({ url: `/erp/stock-move/create`, data })
  },

  // 修改库存调拨单
  updateStockMove: async (data: StockMoveVO) => {
    return await request.put({ url: `/erp/stock-move/update`, data })
  },

  // 更新库存调拨单的状态
  // 删除库存调度单
  submitStockMove: async (id: number) =>
    await request.post({ url: '/erp/stock-move/submit', data: { id } }),

  cancelStockMoveApproval: async (id: number, reason?: string) =>
    await request.delete({ url: '/erp/stock-move/cancel-approval', data: { id, reason } }),

  deleteStockMove: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/stock-move/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出库存调度单 Excel
  exportStockMove: async (params) => {
    return await request.download({ url: `/erp/stock-move/export-excel`, params })
  }
}
