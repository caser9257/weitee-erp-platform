import request from '@/config/axios'

export interface MarketAlertRuleVO {
  code?: string
  name?: string
  description?: string
  enabled?: boolean
  thresholdDays?: number
  level?: string
}

export interface MarketAlertVO {
  id?: number
  ruleCode?: string
  ruleName?: string
  level?: string
  projectId?: number
  projectNo?: string
  projectName?: string
  orderId?: number
  orderNo?: string
  customerName?: string
  content?: string
  triggerTime?: string | Date
  handled?: boolean
  handleTime?: string | Date
  handleUser?: string
}

export const MarketAlertApi = {
  getAlertRules: async () => {
    return await request.get<MarketAlertRuleVO[]>({ url: '/erp/market-alert/rules' })
  },

  getCurrentAlerts: async () => {
    return await request.get<MarketAlertVO[]>({ url: '/erp/market-alert/list' })
  },

  getAlertHistory: async () => {
    return await request.get<MarketAlertVO[]>({ url: '/erp/market-alert/history' })
  },

  checkAndTriggerAlerts: async () => {
    return await request.post<number>({ url: '/erp/market-alert/check' })
  },

  updateAlertRule: async (data: MarketAlertRuleVO) => {
    return await request.put<boolean>({ url: '/erp/market-alert/rules', data })
  },

  handleAlert: async (id: number, remark?: string) => {
    return await request.put<boolean>({ url: '/erp/market-alert/handle', params: { id, remark } })
  }
}
