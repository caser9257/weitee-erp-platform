import request from '@/config/axios'

export interface ErpFinanceVoucherTemplateVO {
  id?: number
  ledgerId?: number
  bizType?: number
  name?: string
  autoGenerate?: boolean
}

export const FinanceVoucherTemplateApi = {
  getVoucherTemplateSimpleList: async (params: { ledgerId: number; bizType: number }) => {
    return await request.get({ url: '/erp/finance-voucher-template/simple-list', params })
  }
}
