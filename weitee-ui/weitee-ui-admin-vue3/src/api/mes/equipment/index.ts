import request from '@/config/axios'

// 占位接口骨架，后续按设备台账与点检接口定稿补充字段
export interface EquipmentVO {
  id?: number
  code?: string
  name: string
  model?: string
  location?: string
  status?: number
  maintainUserId?: number
  lastCheckTime?: Date | string
  nextCheckTime?: Date | string
  remark?: string
}

export const EquipmentApi = {
  getEquipmentPage: async (params: any) => {
    return await request.get({ url: '/mes/equipment/page', params })
  },

  getEquipment: async (id: number) => {
    return await request.get({ url: `/mes/equipment/get?id=${id}` })
  },

  createEquipment: async (data: EquipmentVO) => {
    return await request.post({ url: '/mes/equipment/create', data })
  },

  updateEquipment: async (data: EquipmentVO) => {
    return await request.put({ url: '/mes/equipment/update', data })
  }
}
