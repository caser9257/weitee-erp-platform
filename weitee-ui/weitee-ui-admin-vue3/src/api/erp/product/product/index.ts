import request from '@/config/axios'

// ERP 产品 VO
export interface ProductVO {
  id: number // 产品编号
  name: string // 产品名称
  barCode: string // 产品条码
  categoryId: number // 产品类型编号
  categoryName?: string // 产品类型名称
  unitId: number // 单位编号
  unitName?: string // 单位名字
  quantityPrecision?: number // 数量精度，继承自产品单位
  productType?: number // 产品类型
  produceType?: number // 生产方式
  batchEnable?: boolean // 是否启用批次
  snEnable?: boolean // 是否启用序列号
  status: number // 产品状态
  standard: string // 产品规格
  packaging?: string // 产品封装
  qualityGrade?: string // 质量等级
  brandManufacturer?: string // 品牌/制造商
  alternativeModel?: string // 替代型号
  remark: string // 产品备注
  expiryDay: number // 保质期天数
  weight: number // 重量（kg）
  purchasePrice: number // 采购价格，单位：元
  salePrice: number // 销售价格，单位：元
  minPrice: number // 最低价格，单位：元
  supplyType?: number // 供给方式
  mrpEnable: boolean // 是否参与 MRP
  defaultRouteId?: number // 默认工艺路线
  qcEnable?: boolean // 是否启用质检
  outsourceEnable?: boolean // 是否支持委外
  costMethod?: number // 成本方式
  defaultSupplierId?: number // 默认供应商
  materialCode?: string // 业务物料号
  batchControlFlag?: boolean // 是否批次管理
  inspectionRequiredFlag?: boolean // 是否来料检验
  assetFlag?: boolean // 是否固定资产候选
  auditStatus?: number // 审核状态：0草稿 10审批中 20已审批 30已驳回 60失败
  processInstanceId?: string // 流程实例编号
  createTime?: Date // 创建时间
}

// ERP 产品 API
export const ProductApi = {
  // 查询产品分页
  getProductPage: async (params: any) => {
    return await request.get({ url: `/erp/product/page`, params })
  },

  // 查询产品精简列表
  getProductSimpleList: async () => {
    return await request.get({ url: `/erp/product/simple-list` })
  },

  // 查询产品详情
  getProduct: async (id: number) => {
    return await request.get({ url: `/erp/product/get?id=` + id })
  },

  // 新增产品
  createProduct: async (data: ProductVO) => {
    return await request.post({ url: `/erp/product/create`, data })
  },

  // 修改产品
  updateProduct: async (data: ProductVO) => {
    return await request.put({ url: `/erp/product/update`, data })
  },

  // 删除产品
  deleteProduct: async (id: number) => {
    return await request.delete({ url: `/erp/product/delete?id=` + id })
  },

  // 导出产品 Excel
  exportProduct: async (params) => {
    return await request.download({ url: `/erp/product/export-excel`, params })
  },

  // 替代料
  getSubstituteList: async (productId: number) => {
    return await request.get({ url: '/erp/product/substitute/list', params: { productId } })
  },
  getHasSubstituteMap: async (productIds: number[]) => {
    return await request.get({ url: '/erp/product/substitute/has-map', params: { productIds: productIds.join(',') } })
  },
  createSubstitute: async (data: any) => {
    return await request.post({ url: '/erp/product/substitute/create', data })
  },
  deleteSubstitute: async (productId: number, substituteProductId: number) => {
    return await request.delete({ url: `/erp/product/substitute/delete?productId=${productId}&substituteProductId=${substituteProductId}` })
  },

  getApprovedProductSimpleList: async () => {
    return await request.get({ url: `/erp/product/simple-list-approved` })
  },

  submitProduct: async (id: number) => {
    return await request.post<string>({ url: `/erp/product/submit?id=${id}` })
  },

  cancelProduct: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/cancel', params })
  }
}
