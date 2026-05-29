import request from '@/config/axios'

export interface WarehouseCategoryVO {
  id: number
  parentId: number
  name: string
  code: string
  sort: number
  status: number
  createTime?: string
}

export const WarehouseCategoryApi = {
  getWarehouseCategoryList: async (params: any) => {
    return await request.get({ url: `/erp/warehouse-category/list`, params })
  },

  getWarehouseCategorySimpleList: async () => {
    return await request.get({ url: `/erp/warehouse-category/simple-list` })
  },

  getWarehouseCategory: async (id: number) => {
    return await request.get({ url: `/erp/warehouse-category/get?id=` + id })
  },

  createWarehouseCategory: async (data: WarehouseCategoryVO) => {
    return await request.post({ url: `/erp/warehouse-category/create`, data })
  },

  updateWarehouseCategory: async (data: WarehouseCategoryVO) => {
    return await request.put({ url: `/erp/warehouse-category/update`, data })
  },

  deleteWarehouseCategory: async (id: number) => {
    return await request.delete({ url: `/erp/warehouse-category/delete?id=` + id })
  },

  exportWarehouseCategory: async (params) => {
    return await request.download({ url: `/erp/warehouse-category/export-excel`, params })
  }
}
