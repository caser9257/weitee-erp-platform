import request from '@/config/axios'

export interface ProductionReturnableBatchVO {
  issueBatchId: number
  stockBatchId: number
  batchNo: string
  warehouseId?: number
  issuedQty?: number
  returnedQty?: number
  returnableQty?: number
}

export interface ProductionReturnableBatchesRespVO {
  productionMaterialId: number
  materialId: number
  warehouseId?: number
  returnableBatches: ProductionReturnableBatchVO[]
}

export interface ProductionReturnCreateBatchVO {
  issueBatchId: number
  stockBatchId: number
  batchNo: string
  returnQty: number
}

export interface ProductionReturnCreateItemVO {
  productionMaterialId: number
  materialId: number
  warehouseId: number
  returnQty: number
  remark?: string
  batches: ProductionReturnCreateBatchVO[]
}

export interface ProductionReturnCreateReqVO {
  productionOrderId: number
  remark?: string
  items: ProductionReturnCreateItemVO[]
}

export const ProductionReturnApi = {
  getReturnableBatches: async (productionMaterialId: number) => {
    return await request.get({
      url: `/erp/production-material-return/returnable-batches`,
      params: { productionMaterialId }
    })
  },

  create: async (data: ProductionReturnCreateReqVO) => {
    return await request.post({ url: `/erp/production-material-return/create`, data })
  }
}
