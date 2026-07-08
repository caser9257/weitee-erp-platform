import request from '@/config/axios'

// ERP 销售退货 VO
export interface SaleReturnVO {
  id: number // 销售退货编号
  no: string // 销售退货号
  customerId: number // 客户编号
  customerName?: string // 客户名称
  accountId?: number // 结算账户编号
  accountName?: string // 结算账户名称
  saleUserId?: number // 退货员编号
  returnTime: Date // 退货时间
  orderId?: number // 销售订单编号
  orderNo?: string // 销售订单号
  totalCount: number // 合计数量
  totalPrice: number // 合计金额，单位：元
  refundPrice?: number // 已退款金额，单位：元
  totalProductPrice?: number // 合计产品价格，单位：元
  totalTaxPrice?: number // 合计税额，单位：元
  discountPercent?: number // 优惠率，百分比
  discountPrice?: number // 优惠金额，单位：元
  otherPrice?: number // 其它金额，单位：元
  fileUrl?: string // 附件地址
  status: number // 状态
  remark: string // 备注
  creator?: string // 创建人
  creatorName?: string // 创建人名称
  createTime?: string | Date | number // 创建时间
  items?: SaleReturnItemVO[] // 退货项列表
  productNames?: string // 产品信息
  warehouseName?: string // 仓库名称
}

export interface SaleReturnItemVO {
  id?: number
  orderItemId?: number
  warehouseId?: number
  productId?: number
  productUnitId?: number
  productPrice?: number
  count?: number
  taxPercent?: number
  taxPrice?: number
  remark?: string
  productName?: string
  productBarCode?: string
  productUnitName?: string
  stockCount?: number
}

// ERP 销售退货 API
export const SaleReturnApi = {
  // 查询销售退货分页
  getSaleReturnPage: async (params: any) => {
    return await request.get({ url: `/erp/sale-return/page`, params })
  },

  // 查询销售退货详情
  getSaleReturn: async (id: number) => {
    return await request.get({ url: `/erp/sale-return/get?id=` + id })
  },

  // 新增销售退货
  createSaleReturn: async (data: SaleReturnVO) => {
    return await request.post({ url: `/erp/sale-return/create`, data })
  },

  // 修改销售退货
  updateSaleReturn: async (data: SaleReturnVO) => {
    return await request.put({ url: `/erp/sale-return/update`, data })
  },

  // 更新销售退货的状态
  updateSaleReturnStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/sale-return/update-status`,
      params: {
        id,
        status
      }
    })
  },

  // 删除销售退货
  deleteSaleReturn: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/sale-return/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出销售退货 Excel
  exportSaleReturn: async (params: any) => {
    return await request.download({ url: `/erp/sale-return/export-excel`, params })
  }
}
