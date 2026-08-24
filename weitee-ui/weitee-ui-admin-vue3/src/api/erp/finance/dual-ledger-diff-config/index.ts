import request from '@/config/axios'

export interface ErpFinanceDualLedgerDiffConfigVO {
  id?: number
  bizType?: number
  bizTypeName?: string
  diffItemType?: number
  diffItemTypeName?: string
  externalSourceType?: number
  externalSourceTypeName?: string
  externalSourceValue?: number
  externalSourceValueName?: string
  internalSourceType?: number
  internalSourceTypeName?: string
  internalSourceValue?: number
  internalSourceValueName?: string
  calculationType?: number
  calculationTypeName?: string
  ratio?: number
  fixedAmount?: number
  status?: number
  remark?: string
}

export interface ErpFinanceDualLedgerDiffConfigSaveReqVO {
  id?: number
  bizType?: number
  diffItemType?: number
  externalSourceType?: number
  externalSourceValue?: number
  internalSourceType?: number
  internalSourceValue?: number
  calculationType?: number
  ratio?: number
  fixedAmount?: number
  status?: number
  remark?: string
}

export interface ErpFinanceDualLedgerDiffConfigPageReqVO {
  pageNo?: number
  pageSize?: number
  bizType?: number
  diffItemType?: number
  externalSourceType?: number
  internalSourceType?: number
  status?: number
  remark?: string
}

export const ERP_BIZ_TYPE_OPTIONS = [
  { label: '采购订单', value: 10 },
  { label: '采购入库', value: 11 },
  { label: '采购退货', value: 12 },
  { label: '销售订单', value: 20 },
  { label: '销售出库', value: 21 },
  { label: '销售退货', value: 22 },
  { label: '委外加工费', value: 30 },
  { label: '委外入库', value: 31 },
  { label: '自制入库', value: 32 },
  { label: '费用报销', value: 40 },
  { label: '研发费用化', value: 41 },
  { label: '研发资本化', value: 42 },
  { label: '研发费用月末结转', value: 43 },
  { label: '研发费用', value: 50 },
  { label: '库存盘点', value: 60 },
  { label: '资产折旧', value: 70 },
  { label: '无形资产摊销', value: 71 },
  { label: '研发无形资产摊销', value: 72 }
]

export const DUAL_LEDGER_DIFF_ITEM_OPTIONS = [
  { label: '人工成本', value: 20 },
  { label: '折旧', value: 30 },
  { label: '电费', value: 40 },
  { label: '其他制造费用', value: 50 },
  { label: '制造费用', value: 60 }
]

export const DUAL_LEDGER_DIFF_SOURCE_TYPE_OPTIONS = [
  { label: '生产成本项目', value: 10 },
  { label: '固定资产折旧', value: 20 }
]

export const DUAL_LEDGER_DIFF_CALCULATION_TYPE_OPTIONS = [
  { label: '按比例分摊', value: 1 },
  { label: '固定差额', value: 2 }
]

export const FinanceDualLedgerDiffConfigApi = {
  getDualLedgerDiffConfigPage: async (params: ErpFinanceDualLedgerDiffConfigPageReqVO) => {
    return await request.get({ url: '/erp/finance-dual-ledger-diff-config/page', params })
  },
  getDualLedgerDiffConfig: async (id: number) => {
    return await request.get({ url: `/erp/finance-dual-ledger-diff-config/get?id=${id}` })
  },
  createDualLedgerDiffConfig: async (data: ErpFinanceDualLedgerDiffConfigSaveReqVO) => {
    return await request.post({ url: '/erp/finance-dual-ledger-diff-config/create', data })
  },
  updateDualLedgerDiffConfig: async (data: ErpFinanceDualLedgerDiffConfigSaveReqVO) => {
    return await request.put({ url: '/erp/finance-dual-ledger-diff-config/update', data })
  },
  deleteDualLedgerDiffConfig: async (id: number) => {
    return await request.delete({ url: `/erp/finance-dual-ledger-diff-config/delete?id=${id}` })
  },
  getDualLedgerDiffConfigSimpleList: async (bizType?: number) => {
    return await request.get({
      url: '/erp/finance-dual-ledger-diff-config/simple-list',
      params: bizType == null ? undefined : { bizType }
    })
  }
}
