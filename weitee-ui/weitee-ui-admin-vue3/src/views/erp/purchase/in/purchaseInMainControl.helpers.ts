export type PurchaseInMainControlStageKey =
  | 'all'
  | 'pendingApproval'
  | 'pendingQuality'
  | 'pendingStockIn'
  | 'partialStockIn'

export type PurchaseInMainControlStageDescriptor = {
  key: PurchaseInMainControlStageKey
  label: '全部' | '待审批' | '待质检' | '待执行入库' | '部分执行'
  tone: 'slate' | 'blue' | 'amber' | 'green'
  query: {
    status?: number
    qaStatus?: number
    stockInStatus?: number
  }
}

export type PurchaseInMainControlRowInput = {
  id?: number | null
  orderId?: number | null
  orderNo?: string | null
  no?: string | null
  items?: Array<{
    productId?: number | null
    warehouseId?: number | null
    purchaseSourceBatchNo?: string | null
  }> | null
}

export type PurchaseInMainControlActionKey =
  | 'viewPurchaseOrder'
  | 'viewStock'
  | 'traceBatch'
  | 'viewStockFlow'

export type PurchaseInMainControlActionDescriptor = {
  key: PurchaseInMainControlActionKey
  label: '查看采购订单' | '查看库存' | '批次追溯' | '库存流水'
  disabled: boolean
  reason?: 'missing-order' | 'missing-stock-context' | 'missing-flow-context'
}

export const PURCHASE_IN_STATUS = {
  PROCESS: 10,
  APPROVE: 20,
  REJECT: 30
} as const

export const PURCHASE_IN_QA_STATUS = {
  TO_INSPECT: 10,
  PARTIAL: 20,
  PASSED: 30,
  REJECTED: 40
} as const

export const PURCHASE_IN_STOCK_IN_STATUS = {
  TO_STOCK_IN: 10,
  PARTIAL_STOCKED_IN: 15,
  STOCKED_IN: 20,
  NO_NEED_STOCK_IN: 30
} as const

export const STOCK_RECORD_BIZ_TYPE = {
  PURCHASE_IN: 70
} as const

export const PURCHASE_IN_MAIN_CONTROL_STAGES: PurchaseInMainControlStageDescriptor[] = [
  {
    key: 'all',
    label: '全部',
    tone: 'slate',
    query: {}
  },
  {
    key: 'pendingApproval',
    label: '待审批',
    tone: 'blue',
    query: {
      status: PURCHASE_IN_STATUS.PROCESS
    }
  },
  {
    key: 'pendingQuality',
    label: '待质检',
    tone: 'amber',
    query: {
      status: PURCHASE_IN_STATUS.APPROVE,
      qaStatus: PURCHASE_IN_QA_STATUS.TO_INSPECT
    }
  },
  {
    key: 'pendingStockIn',
    label: '待执行入库',
    tone: 'amber',
    query: {
      status: PURCHASE_IN_STATUS.APPROVE,
      stockInStatus: PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN
    }
  },
  {
    key: 'partialStockIn',
    label: '部分执行',
    tone: 'green',
    query: {
      status: PURCHASE_IN_STATUS.APPROVE,
      stockInStatus: PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN
    }
  }
]

const firstUsableItem = (row: PurchaseInMainControlRowInput) =>
  (row.items || []).find((item) => !!item.productId && !!item.warehouseId)

export function resolvePurchaseInMainControlStage(
  key?: string | null
): PurchaseInMainControlStageDescriptor {
  return (
    PURCHASE_IN_MAIN_CONTROL_STAGES.find((stage) => stage.key === key) ||
    PURCHASE_IN_MAIN_CONTROL_STAGES[0]
  )
}

export function getPurchaseInMainControlStageQuery(
  key?: string | null
): PurchaseInMainControlStageDescriptor['query'] {
  return { ...resolvePurchaseInMainControlStage(key).query }
}

export function getPurchaseInMainControlActions(
  row: PurchaseInMainControlRowInput
): PurchaseInMainControlActionDescriptor[] {
  const item = firstUsableItem(row)
  const hasOrder = !!row.orderNo || !!row.orderId
  const hasStockContext = !!item
  const hasFlowContext = !!row.no || !!row.id || hasStockContext

  return [
    {
      key: 'viewPurchaseOrder',
      label: '查看采购订单',
      disabled: !hasOrder,
      ...(!hasOrder ? { reason: 'missing-order' as const } : {})
    },
    {
      key: 'viewStock',
      label: '查看库存',
      disabled: !hasStockContext,
      ...(!hasStockContext ? { reason: 'missing-stock-context' as const } : {})
    },
    {
      key: 'traceBatch',
      label: '批次追溯',
      disabled: !hasStockContext,
      ...(!hasStockContext ? { reason: 'missing-stock-context' as const } : {})
    },
    {
      key: 'viewStockFlow',
      label: '库存流水',
      disabled: !hasFlowContext,
      ...(!hasFlowContext ? { reason: 'missing-flow-context' as const } : {})
    }
  ]
}

export function getPurchaseInPrimaryStockContext(row: PurchaseInMainControlRowInput) {
  const item = firstUsableItem(row)
  if (!item) {
    return undefined
  }
  return {
    productId: item.productId!,
    warehouseId: item.warehouseId!,
    batchNo: item.purchaseSourceBatchNo || undefined
  }
}
