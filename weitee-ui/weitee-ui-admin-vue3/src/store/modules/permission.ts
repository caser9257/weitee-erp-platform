import { defineStore } from 'pinia'
import { store } from '@/store'
import { cloneDeep } from 'lodash-es'
import { mergeProjectDrivenMenus } from '@/router/modules/projectDrivenFlat'
import remainingRouter from '@/router/modules/remaining'
import { flatMultiLevelRoutes, generateRoute } from '@/utils/routerHelper'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { buildPermissionRoutes } from '@/utils/permissionRouteBuilder'

const { wsCache } = useCache()

export interface PermissionState {
  routers: AppRouteRecordRaw[]
  addRouters: AppRouteRecordRaw[]
  menuTabRouters: AppRouteRecordRaw[]
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    routers: [],
    addRouters: [],
    menuTabRouters: []
  }),
  getters: {
    getRouters(): AppRouteRecordRaw[] {
      return this.routers
    },
    getAddRouters(): AppRouteRecordRaw[] {
      return flatMultiLevelRoutes(cloneDeep(this.addRouters))
    },
    getMenuTabRouters(): AppRouteRecordRaw[] {
      return this.menuTabRouters
    }
  },
  actions: {
    async generateRoutes(): Promise<void> {
      let res: AppCustomRouteRecordRaw[] = []
      const roleRouters = wsCache.get(CACHE_KEY.ROLE_ROUTERS)
      if (roleRouters) {
        res = roleRouters as AppCustomRouteRecordRaw[]
      }

      const mergeMenus = (routes: AppCustomRouteRecordRaw[]) => {
        try {
          return mergeProjectDrivenMenus(routes)
        } catch (error) {
          console.warn('[permission] failed to inject project-driven menus', error)
          return routes
        }
      }

      const { addRouters, routers } = buildPermissionRoutes({
        roleRouters: res,
        mergeMenus,
        generateRoutes: generateRoute,
        remainingRouter,
        notFoundRoute: {
          path: '/:path(.*)*',
          component: () => import('@/views/Error/404.vue'),
          name: '404Page',
          meta: {
            hidden: true,
            breadcrumb: false
          }
        } as AppRouteRecordRaw,
        cloneRoutes: (routes) => cloneDeep(routes)
      })

      // 关键路由断言已迁移至 permission.ts 守卫：动态路由 addRoute 挂载完成后
      // 以 router.getRoutes() 权威注册表比对（此处时机过早且查的是本地树，曾长期误报）

      this.addRouters = addRouters
      this.routers = routers
    },
    setMenuTabRouters(routers: AppRouteRecordRaw[]): void {
      this.menuTabRouters = routers
    }
  },
  persist: false
})

export const usePermissionStoreWithOut = () => {
  return usePermissionStore(store)
}
