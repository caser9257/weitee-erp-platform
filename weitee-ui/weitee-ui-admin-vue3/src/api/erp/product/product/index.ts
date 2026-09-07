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
  prevMaterialCode?: string // 最近一次变更前物料编码（编码沿革，无改码历史为空）
  batchControlFlag?: boolean // 是否批次管理
  inspectionRequiredFlag?: boolean // 是否来料检验
  assetFlag?: boolean // 是否固定资产候选
  pcbComponent?: boolean // 是否 PCB 元器件
  auditStatus?: number // 审核状态：0草稿 10审批中 20已审批 30已驳回 60失败
  processInstanceId?: string // 流程实例编号
  pendingBatchId?: number | string // 在途批量审批批次编号（雪花 ID，后端序列化为字符串；撤回将作废整批）
  pendingBatchSize?: number // 在途批量审批批次成员数
  referencedByBom?: boolean // 是否被 BOM 引用（引用后 standard 冻结；materialCode 走编码沿革可改）
  schematicPart?: string // 原理图库符号
  pcbFootprint?: string // PCB 封装
  cadenceDescription?: string // 关键参数描述
  manufacturerPartNumber?: string // 厂家型号
  dimension?: string // 三维尺寸
  threeDLib?: string // 3D模型
  datasheet?: string // 数据手册
  lifecycle?: string // 生命周期
  preferredPart?: boolean // 是否优选
  operatingTemperature?: string // 工作温度
  mountingType?: string // 安装类型
  dnp?: boolean // 空置标志
  importedOrReplacement?: string // 进口/替代物料
  secondDescription?: string // 参数描述 2
  thirdDescription?: string // 参数描述 3
  fourthDescription?: string // 参数描述 4
  createTime?: Date // 创建时间
}

// 已审核物料精简 VO（后端白名单裁剪，供 BOM 下拉与单位反查）
// 注意：后端 /erp/product/simple-list-approved 只返回以下字段，禁止按全字段 VO 消费
export interface ProductSimpleVO {
  id: number // 产品编号
  name: string // 产品名称
  materialCode?: string // 业务物料号
  prevMaterialCode?: string // 最近一次变更前物料编码（编码沿革）
  categoryId?: number // 产品类型编号
  categoryName?: string // 产品类型名称
  unitId?: number // 单位编号
  unitName?: string // 单位名字
  status?: number // 产品状态
  auditStatus?: number // 审核状态
}

// 物料修改审批视图 VO
export interface ProductApprovalViewVO {
  id: number
  current?: ProductVO
  target?: ProductVO
  diffs: ProductFieldDiffVO[]
  changedFields?: string
  pendingStatus?: number // 暂存状态：1待审 2通过 3驳回 4失败
  reason?: string
  processInstanceId?: string
}

// 字段级变更
export interface ProductFieldDiffVO {
  field: string
  label: string
  oldValue: string
  newValue: string
}

// 物料批量修改审批视图 VO
export interface ProductBatchApprovalViewVO {
  batchId: number | string // 雪花 ID，后端序列化为字符串
  status?: number // 批次状态：1待审 2通过 3驳回 4失败
  reason?: string
  processInstanceId?: string
  totalCount: number
  items: ProductBatchApprovalItemVO[]
}

// 批次内单个物料变更明细
export interface ProductBatchApprovalItemVO {
  productId: number
  materialCode?: string
  name?: string
  changedFields?: string
  diffs: ProductFieldDiffVO[]
}

// 物料替代类型：1-全局通用 2-临时替代
export const ProductSubstituteTypeEnum = {
  GLOBAL: 1,
  TEMPORARY: 2
} as const

// 替代料关联 VO
export interface ProductSubstituteVO {
  id?: number // 编号
  productId: number // 主物料编号
  substituteProductId: number // 替代料编号
  substituteProductName?: string // 替代料名称
  substituteMaterialCode?: string // 替代料编码
  substituteProductStandard?: string // 替代料规格
  priority?: number // 优先级
  replaceRatio?: number // 替换比例
  substituteType?: number // 替代类型：1-全局通用 2-临时替代
  status?: number // 启用状态：0-启用 1-停用
  remark?: string // 备注
}

// ERP 产品 API
export const ProductApi = {
  // 查询产品分页
  getProductPage: async (params: any) => {
    return await request.get({ url: `/erp/product/page`, params })
  },

  // 查询产品精简列表（keyword：名称/物料编码/条码 模糊，万级物料必须带关键字远程搜索）
  getProductSimpleList: async (keyword?: string) => {
    return await request.get({ url: `/erp/product/simple-list`, params: { name: keyword } })
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
    return await request.get<ProductSubstituteVO[]>({ url: '/erp/product/substitute/list', params: { productId } })
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
  // 批量新增/更新物料的替代料列表（diff 替换语义）
  batchUpdateSubstitutes: async (productId: number, list: ProductSubstituteVO[]) => {
    return await request.post<boolean>({ url: '/erp/product/substitute/batch-update', params: { productId }, data: list })
  },

  getApprovedProductSimpleList: async (keyword?: string) => {
    return await request.get<ProductSimpleVO[]>({ url: `/erp/product/simple-list-approved`, params: { name: keyword } })
  },

  submitProduct: async (id: number) => {
    return await request.post<string>({ url: `/erp/product/submit?id=${id}` })
  },

  cancelProduct: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/cancel', params })
  },

  // 物料修改审批视图（主表现值 vs 暂存目标值 + 字段级 diff）
  getApprovalView: async (id: number) => {
    return await request.get<ProductApprovalViewVO>({ url: `/erp/product/approval-view?id=${id}` })
  },

  // 物料批量修改审批视图（批次内全部物料的字段级 diff）；batchId 为雪花 ID，按字符串传递防精度丢失
  getBatchApprovalView: async (batchId: number | string) => {
    return await request.get<ProductBatchApprovalViewVO>({
      url: `/erp/product/batch-approval-view?batchId=${batchId}`
    })
  },

  // 两段式变更阶段一：发起变更申请
  submitChangeRequest: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/change-request', params })
  },

  // 两段式变更阶段二：提交变更完成确认
  submitChangeConfirm: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/change-confirm', params })
  },

  // 两段式废除阶段一：发起废除申请
  submitObsoleteRequest: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/obsolete-request', params })
  },

  // 启停审批（一段式）：提交理由，审批通过即切换状态
  submitStatusChange: async (id: number, targetStatus: number, reason?: string) => {
    const params: any = { id, targetStatus }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/status-change', params })
  },

  // 撤回两段式审批（变更/废除 阶段一/二 通用）
  cancelTwoStageApproval: async (id: number, reason?: string) => {
    const params: any = { id }
    if (reason) params.reason = reason
    return await request.post<boolean>({ url: '/erp/product/cancel-two-stage', params })
  }
}
