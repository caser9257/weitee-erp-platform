import request from '@/config/axios'

export const LEASE_CONTRACT_STATUS = {
  DRAFT: 0,
  APPROVING: 5,
  EFFECTIVE: 10,
  EXPIRED: 20,
  TERMINATED: 30
} as const

export interface LeaseContractVO {
  id?: number
  no?: string
  name?: string
  supplierId?: number
  supplierName?: string
  startDate?: string
  endDate?: string
  monthlyRent?: number
  paymentCycle?: number
  totalAmount?: number
  costCenterId?: number
  status?: number
  remark?: string
  fileUrl?: string
  createTime?: string
}

export interface LeaseContractPageReqVO {
  pageNo: number
  pageSize: number
  no?: string
  name?: string
  supplierId?: number
  status?: number
}

export interface LeaseContractSaveReqVO {
  id?: number
  no?: string
  name?: string
  supplierId?: number
  startDate?: string
  endDate?: string
  monthlyRent?: number
  paymentCycle?: number
  totalAmount?: number
  costCenterId?: number
  remark?: string
  fileUrl?: string
}

export const LeaseContractApi = {
  getPage: async (params: LeaseContractPageReqVO) => {
    return await request.get<PageResult<LeaseContractVO[]>>({
      url: '/erp/lease-contract/page',
      params
    })
  },

  get: async (id: number) => {
    return await request.get<LeaseContractVO>({ url: `/erp/lease-contract/get?id=${id}` })
  },

  create: async (data: LeaseContractSaveReqVO) => {
    return await request.post<number>({ url: '/erp/lease-contract/create', data })
  },

  update: async (data: LeaseContractSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/lease-contract/update', data })
  },

  delete: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/lease-contract/delete?id=${id}` })
  }
}
