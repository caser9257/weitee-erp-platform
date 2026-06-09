/**
 * 费用类型 VO
 * 支持核心枚举类型和字典扩展类型的混合模式
 */
export interface ExpenseTypeVO {
  /** 类型值 */
  value: number
  /** 类型名称 */
  label: string
  /** 是否为核心类型（系统预设） */
  core: boolean
  /** 是否需要项目 */
  projectRequired: boolean
  /** 是否需要成本中心 */
  costCenterRequired: boolean
  /** 是否需要租赁合同 */
  leaseContractRequired: boolean
  /** 是否标记为资产候选 */
  assetCandidateFlag: boolean
  /** 费用分类：ADMIN-行政费用，LEASE-租赁费用，RD-研发费用，OTHER-其他 */
  category: string
  /** 是否自动生成凭证 */
  autoGenerateVoucher: boolean
  /** 凭证业务类型 */
  voucherBizType: number | null
}

/**
 * 费用分类枚举
 */
export enum ExpenseCategory {
  /** 行政费用 */
  ADMIN = 'ADMIN',
  /** 租赁费用 */
  LEASE = 'LEASE',
  /** 研发费用 */
  RD = 'RD',
  /** 其他 */
  OTHER = 'OTHER'
}
