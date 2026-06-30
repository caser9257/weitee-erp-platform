import request from '@/config/axios'

// ERP 工艺路线 VO
export interface RouteVO {
  id: number // 路线编号
  no: string // 路线编码
  name: string // 路线名称
  status: number // 状态
}

// ERP 工艺路线 API
export const RouteApi = {
  // 获得路线精简列表
  getRouteSimpleList: async () => {
    return await request.get({ url: `/erp/route/simple-list` })
  }
}
