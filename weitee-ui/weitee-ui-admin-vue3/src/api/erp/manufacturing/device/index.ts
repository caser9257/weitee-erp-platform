import request from '@/config/axios'

// ERP 设备台账 VO
export interface DeviceVO {
  id?: number
  deviceCode: string
  deviceName: string
  workCenterId?: number
  workCenterName?: string
  specification?: string
  deviceStatus: number
  maintenanceCycleDay?: number
  checkCycleDay?: number
  purchaseDate?: string
  startUseDate?: string
  manufacturer?: string
  remark?: string
  createTime?: string
}

export interface DevicePageReqVO {
  pageNo: number
  pageSize: number
  deviceCode?: string
  deviceName?: string
  workCenterId?: number
  deviceStatus?: number
}

export interface DeviceSaveReqVO {
  id?: number
  deviceCode: string
  deviceName: string
  workCenterId?: number
  specification?: string
  deviceStatus: number
  maintenanceCycleDay?: number
  checkCycleDay?: number
  purchaseDate?: string
  startUseDate?: string
  manufacturer?: string
  remark?: string
}

export const DeviceApi = {
  // 查询设备分页
  getDevicePage: async (params: DevicePageReqVO) => {
    return await request.get({ url: '/erp/device/page', params })
  },

  // 查询设备详情
  getDevice: async (id: number) => {
    return await request.get<DeviceVO>({ url: `/erp/device/get?id=${id}` })
  },

  // 新增设备
  createDevice: async (data: DeviceSaveReqVO) => {
    return await request.post<number>({ url: '/erp/device/create', data })
  },

  // 修改设备
  updateDevice: async (data: DeviceSaveReqVO) => {
    return await request.put<boolean>({ url: '/erp/device/update', data })
  },

  // 删除设备
  deleteDevice: async (id: number) => {
    return await request.delete<boolean>({ url: `/erp/device/delete?id=${id}` })
  }
}
