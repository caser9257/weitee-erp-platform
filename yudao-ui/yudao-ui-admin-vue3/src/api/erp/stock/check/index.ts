import request from '@/config/axios'

// ERP 库存盘点单 VO
export interface StockCheckVO {
  id: number // 盘点编号
  no: string // 盘点单号
  checkTime: Date // 盘点时间
  totalCount: number // 合计数量
  totalPrice: number // 合计金额，单位：元
  status: number // 状态
  remark: string // 备注
  yearEndFlag?: boolean // 是否年末盘点
  voucherId?: number // 凭证ID
  snapshotTime?: Date // 快照时间
  productNames?: string // 产品信息
  creatorName?: string // 创建人名称
  items?: StockCheckItemVO[] // 盘点项列表
}

// ERP 库存盘点单项 VO
export interface StockCheckItemVO {
  id: number // 盘点项编号
  warehouseId: number // 仓库编号
  productId: number // 产品编号
  productPrice: number // 产品单价
  stockCount: number // 账面数量
  actualCount: number // 实际数量
  count: number // 盈亏数量
  diffAmount?: number // 差异金额
  remark?: string // 备注
  productName?: string // 产品名称
  productBarCode?: string // 产品条码
  productUnitName?: string // 产品单位名称
}

// 盘点状态枚举
export const StockCheckStatus = {
  DRAFT: 0,      // 草稿
  COUNTING: 10,  // 盘点中
  REVIEWING: 20, // 审核中
  APPROVED: 30,  // 已审核
  CLOSED: 40     // 已关闭
}

// ERP 库存盘点单 API
export const StockCheckApi = {
  // 查询库存盘点单分页
  getStockCheckPage: async (params: any) => {
    return await request.get({ url: `/erp/stock-check/page`, params })
  },

  // 查询库存盘点单详情
  getStockCheck: async (id: number) => {
    return await request.get({ url: `/erp/stock-check/get?id=` + id })
  },

  // 新增库存盘点单
  createStockCheck: async (data: StockCheckVO) => {
    return await request.post({ url: `/erp/stock-check/create`, data })
  },

  // 修改库存盘点单
  updateStockCheck: async (data: StockCheckVO) => {
    return await request.put({ url: `/erp/stock-check/update`, data })
  },

  // 更新库存盘点单的状态
  updateStockCheckStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/stock-check/update-status`,
      params: {
        id,
        status
      }
    })
  },

  // 启动盘点（DRAFT → COUNTING）
  startCounting: async (id: number) => {
    return await request.put({
      url: `/erp/stock-check/start-counting`,
      params: { id }
    })
  },

  // 提交审核（COUNTING → REVIEWING）
  submitForReview: async (id: number) => {
    return await request.put({
      url: `/erp/stock-check/submit-for-review`,
      params: { id }
    })
  },

  // 审核通过（REVIEWING → APPROVED → CLOSED）
  approveAndClose: async (id: number) => {
    return await request.put({
      url: `/erp/stock-check/approve-and-close`,
      params: { id }
    })
  },

  // 驳回到上一状态
  reject: async (id: number) => {
    return await request.put({
      url: `/erp/stock-check/reject`,
      params: { id }
    })
  },

  // 获取差异报告
  getDiffReport: async (id: number) => {
    return await request.get({
      url: `/erp/stock-check/diff-report`,
      params: { id }
    })
  },

  // 删除库存盘点单
  deleteStockCheck: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/stock-check/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出库存盘点单 Excel
  exportStockCheck: async (params) => {
    return await request.download({ url: `/erp/stock-check/export-excel`, params })
  }
}
