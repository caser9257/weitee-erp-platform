import request from '@/config/axios'

export interface MarketAlertRuleVO {
  code: string
  name: string
  description: string
  level: string
  enabled: boolean
  thresholdDays: number
}

export interface MarketAlertRecordVO {
  id: number
  ruleCode: string
  ruleName: string
  orderId: number
  orderNo: string
  projectNo?: string
  content: string
  level: string
  triggerTime: string | Date
  handled: boolean
  handleRemark?: string
}

export const MarketAlertApi = {
  // 获取预警规则列表
  getAlertRules: async () => {
    return await request.get<MarketAlertRuleVO[]>({ url: '/erp/market-alert/rules' })
  },

  // 获取当前预警列表
  getCurrentAlerts: async () => {
    return await request.get<MarketAlertRecordVO[]>({ url: '/erp/market-alert/list' })
  },

  // 检查并触发预警
  checkAndTriggerAlerts: async () => {
    return await request.post<number>({ url: '/erp/market-alert/check' })
  },

  // 更新预警规则
  updateAlertRule: async (rule: MarketAlertRuleVO) => {
    return await request.put({ url: '/erp/market-alert/rules', data: rule })
  },

  // 处理预警
  handleAlert: async (id: number, remark?: string) => {
    return await request.put({ url: '/erp/market-alert/handle', params: { id, remark } })
  }
}
