import { getDeptList, getSimpleDeptList, type DeptVO } from '@/api/system/dept'
import { getSimpleUserList, type UserVO } from '@/api/system/user'
import { getSimplePostList, type PostVO } from '@/api/system/post'

export type OrgChartDeptVO = DeptVO
export type OrgChartUserVO = UserVO
export type OrgChartPostVO = PostVO

export const PmoOrgChartApi = {
  getDeptTree: async (params: any = {}) => {
    return await getDeptList(params)
  },

  getDeptSimpleList: async () => {
    return await getSimpleDeptList()
  },

  getUserSimpleList: async () => {
    return await getSimpleUserList()
  },

  getPostSimpleList: async () => {
    return await getSimplePostList()
  }
}
