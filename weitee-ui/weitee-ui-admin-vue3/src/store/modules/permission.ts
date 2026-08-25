import { defineStore } from 'pinia'
import { store } from '@/store'
import { cloneDeep } from 'lodash-es'
import { mergeProjectDrivenMenus } from '@/router/modules/projectDrivenFlat'
import remainingRouter from '@/router/modules/remaining'
import { flatMultiLevelRoutes, generateRoute } from '@/utils/routerHelper'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { buildPermissionRoutes } from '@/utils/permissionRouteBuilder'
import { CRITICAL_ROUTES, collectRoutePaths } from '@/router/criticalRoutes'

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

      // dev 环境关键路由断言：缺失立即暴露，避免运行时点菜单才发现 404
      if (import.meta.env.DEV) {
        const registered = collectRoutePaths(addRouters as any[])
        const missing = CRITICAL_ROUTES.filter((p) => !registered.has(p))
        if (missing.length) {
          console.error(
            `[route-guard] 关键路由缺失: ${missing.join(', ')} — 请检查动态菜单合并/权限过滤/菜单种子`
          )
        }
      }

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
