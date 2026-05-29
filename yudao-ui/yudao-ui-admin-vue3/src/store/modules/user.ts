import { store } from '@/store'
import { defineStore } from 'pinia'
import { getAccessToken, removeToken } from '@/utils/auth'
import { CACHE_KEY, useCache, deleteUserCache } from '@/hooks/web/useCache'
import { getInfo, loginOut } from '@/api/login'

const { wsCache } = useCache()

interface UserVO {
  id: number
  avatar: string
  nickname: string
  deptId: number
}

interface UserInfoVO {
  permissions: Set<string>
  roles: string[]
  isSetUser: boolean
  user: UserVO
}

interface PermissionInfoVO {
  permissions?: string[]
  roles: string[]
  user: UserVO
  menus?: AppCustomRouteRecordRaw[]
}

export const useUserStore = defineStore('admin-user', {
  state: (): UserInfoVO => ({
    permissions: new Set<string>(),
    roles: [],
    isSetUser: false,
    user: {
      id: 0,
      avatar: '',
      nickname: '',
      deptId: 0
    }
  }),
  getters: {
    getPermissions(): Set<string> {
      return this.permissions
    },
    getRoles(): string[] {
      return this.roles
    },
    getIsSetUser(): boolean {
      return this.isSetUser
    },
    getUser(): UserVO {
      return this.user
    }
  },
  actions: {
    applyUserInfo(userInfo: PermissionInfoVO) {
      this.permissions = new Set(userInfo.permissions || [])
      this.roles = userInfo.roles || []
      this.user = userInfo.user
      this.isSetUser = true
      wsCache.set(CACHE_KEY.USER, userInfo)
      wsCache.set(CACHE_KEY.ROLE_ROUTERS, userInfo.menus || [])
      return userInfo
    },
    async refreshUserInfoAction() {
      if (!getAccessToken()) {
        this.resetState()
        return null
      }
      const userInfo = (await getInfo()) as PermissionInfoVO | null
      if (!userInfo) {
        this.resetState()
        return null
      }
      return this.applyUserInfo(userInfo)
    },
    async setUserInfoAction() {
      if (!getAccessToken()) {
        this.resetState()
        return null
      }

      let userInfo = wsCache.get(CACHE_KEY.USER) as PermissionInfoVO | null
      if (!userInfo) {
        return await this.refreshUserInfoAction()
      }

      this.applyUserInfo(userInfo)
      try {
        userInfo = await this.refreshUserInfoAction()
      } catch (error) {}
      return userInfo
    },
    async setUserAvatarAction(avatar: string) {
      const userInfo = wsCache.get(CACHE_KEY.USER)
      this.user.avatar = avatar
      userInfo.user.avatar = avatar
      wsCache.set(CACHE_KEY.USER, userInfo)
    },
    async setUserNicknameAction(nickname: string) {
      const userInfo = wsCache.get(CACHE_KEY.USER)
      this.user.nickname = nickname
      userInfo.user.nickname = nickname
      wsCache.set(CACHE_KEY.USER, userInfo)
    },
    async loginOut() {
      await loginOut()
      removeToken()
      deleteUserCache()
      this.resetState()
    },
    resetState() {
      this.permissions = new Set<string>()
      this.roles = []
      this.isSetUser = false
      this.user = {
        id: 0,
        avatar: '',
        nickname: '',
        deptId: 0
      }
    }
  }
})

export const useUserStoreWithOut = () => {
  return useUserStore(store)
}
