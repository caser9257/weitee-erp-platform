import request from '@/config/axios'

// ERP 销售出库 VO
export interface SaleOutVO {
  id: number // 销售出库编号
  no: string // 销售出库号
  customerId: number // 客户编号
  customerName?: string // 客户名称
  accountId?: number // 结算账户编号
  saleUserId?: number // 出库员编号
  outTime: Date // 出库时间
  orderId?: number // 销售订单编号
  orderNo?: string // 销售订单号
  totalCount: number // 合计数量
  totalPrice: number // 合计金额，单位：元
  receiptPrice?: number // 已收款金额，单位：元
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
  items?: SaleOutItemVO[] // 出库项列表
  productNames?: string // 产品信息
}

export interface SaleOutItemVO {
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

export interface SaleOutPrintDataVO {
  saleOut?: SaleOutVO
  sourceAttachments?: Array<{
    name?: string
    url?: string
  }>
}

// ERP 销售出库 API
export const SaleOutApi = {
  // 查询销售出库分页
  getSaleOutPage: async (params: any) => {
    return await request.get({ url: `/erp/sale-out/page`, params })
  },

  // 查询销售出库详情
  getSaleOut: async (id: number) => {
    return await request.get({ url: `/erp/sale-out/get?id=` + id })
  },

  // 查询销售出库打印数据
  getSaleOutPrintData: async (id: number) => {
    return await request.get<SaleOutPrintDataVO>({
      url: `/erp/sale-out/get-print-data?id=${id}`
    })
  },

  // 新增销售出库
  createSaleOut: async (data: SaleOutVO) => {
    return await request.post({ url: `/erp/sale-out/create`, data })
  },

  // 修改销售出库
  updateSaleOut: async (data: SaleOutVO) => {
    return await request.put({ url: `/erp/sale-out/update`, data })
  },

  // 更新销售出库的状态
  updateSaleOutStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/erp/sale-out/update-status`,
      params: {
        id,
        status
      }
    })
  },

  // 删除销售出库
  deleteSaleOut: async (ids: number[]) => {
    return await request.delete({
      url: `/erp/sale-out/delete`,
      params: {
        ids: ids.join(',')
      }
    })
  },

  // 导出销售出库 Excel
  exportSaleOut: async (params: any) => {
    return await request.download({ url: `/erp/sale-out/export-excel`, params })
  }
}
