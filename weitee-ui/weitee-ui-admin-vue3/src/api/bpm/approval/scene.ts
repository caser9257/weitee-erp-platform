import request from '@/config/axios'

// 审批场景 VO
export interface BpmApprovalSceneVO {
  id?: number
  sceneCode?: string
  name?: string
  moduleCode?: string
  bizType?: string
  actionCode?: string
  activeSchemeId?: number
  activeSchemeName?: string
  ownerUserId?: number
  status?: number
  remark?: string
  createTime?: string
}

// 审批场景保存请求 VO
export interface BpmApprovalSceneSaveReqVO {
  id?: number
  sceneCode: string
  name: string
  moduleCode: string
  bizType: string
  actionCode: string
  activeSchemeId?: number
  status?: number
  remark?: string
}

// 审批场景 API
export const ApprovalSceneApi = {
  // 查询审批场景分页
  getScenePage: async (params: any) => {
    return await request.get({ url: '/bpm/approval-scene/page', params })
  },

  // 查询审批场景详情
  getScene: async (id: number) => {
    return await request.get({ url: '/bpm/approval-scene/get?id=' + id })
  },

  // 根据场景编码查询审批场景
  getSceneByCode: async (sceneCode: string) => {
    return await request.get({ url: '/bpm/approval-scene/get-by-code?sceneCode=' + sceneCode })
  },

  // 创建审批场景
  createScene: async (data: BpmApprovalSceneSaveReqVO) => {
    return await request.post({ url: '/bpm/approval-scene/create', data })
  },

  // 更新审批场景
  updateScene: async (data: BpmApprovalSceneSaveReqVO) => {
    return await request.put({ url: '/bpm/approval-scene/update', data })
  },

  // 删除审批场景
  deleteScene: async (id: number) => {
    return await request.delete({ url: '/bpm/approval-scene/delete?id=' + id })
  },

  // 启用审批场景
  enableScene: async (id: number) => {
    return await request.put({ url: '/bpm/approval-scene/enable?id=' + id })
  },

  // 禁用审批场景
  disableScene: async (id: number) => {
    return await request.put({ url: '/bpm/approval-scene/disable?id=' + id })
  },

  // 绑定审批方案
  bindScheme: async (id: number, activeSchemeId?: number) => {
    return await request.put({
      url: '/bpm/approval-scene/bind-scheme',
      params: { id, activeSchemeId: activeSchemeId ?? '' }
    })
  }
}
