import request from '@/config/axios'

export interface ProductionMaterialVO {
  id: number
  productionOrderId: number
  materialId: number
  materialName?: string
  materialCode?: string
  materialBarCode?: string
  unitName?: string
  supplyWarehouseId?: number
  supplyWarehouseName?: string
  requiredQty?: number
  issuedQty?: number
  returnedQty?: number
  netIssuedQty?: number
  remainingIssueQty?: number
  batchControlFlag?: boolean
  remark?: string
}

export interface ProductionMaterialBatchCandidateVO {
  stockBatchId: number
  batchNo: string
  inboundTime?: string | Date | number
  produceDate?: string
  expireDate?: string
  availableQty?: number
}

export interface ProductionMaterialBatchCandidatesRespVO {
  productionMaterialId: number
  materialId: number
  warehouseId: number
  requiredQty?: number
  issuedQty?: number
  returnedQty?: number
  remainingQty?: number
  batchCandidates: ProductionMaterialBatchCandidateVO[]
}

export const ProductionMaterialApi = {
  getProductionMaterialList: async (productionOrderId: number) => {
    return await request.get({ url: `/erp/production-material/list`, params: { productionOrderId } })
  },

  getBatchCandidates: async (productionMaterialId: number, warehouseId: number) => {
    return await request.get({
      url: `/erp/production-material/batch-candidates`,
      params: { productionMaterialId, warehouseId }
    })
  }
}
