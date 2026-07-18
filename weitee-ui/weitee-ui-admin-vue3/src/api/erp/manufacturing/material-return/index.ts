import request from '@/config/axios'

export interface ProductionReturnPageReqVO {
  pageNo: number
  pageSize: number
  returnNo?: string
  productionOrderId?: number
  status?: number
  returnTime?: [string, string]
}

export interface ProductionReturnVO {
  id: number
  returnNo: string
  productionOrderId: number
  returnTime?: string | number
  status?: number
  remark?: string
}

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
  getProductionReturnPage: async (params: ProductionReturnPageReqVO) => {
    return await request.get<PageResult<ProductionReturnVO[]>>({
      url: `/erp/production-material-return/page`,
      params
    })
  },
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
