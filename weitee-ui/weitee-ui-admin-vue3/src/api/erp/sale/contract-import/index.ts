import request from '@/config/axios'

export interface ContractImportFailDetailVO {
  rowNumber?: number
  contractNo?: string
  reason?: string
}

export interface ContractImportResultVO {
  totalCount?: number
  successCount?: number
  failCount?: number
  failDetails?: ContractImportFailDetailVO[]
}

export const ContractImportApi = {
  downloadTemplate: async () => {
    return await request.download({ url: '/erp/contract-import/template' })
  },

  importContracts: async (data: FormData) => {
    return await request.upload<ContractImportResultVO>({
      url: '/erp/contract-import/import',
      data
    })
  }
}
