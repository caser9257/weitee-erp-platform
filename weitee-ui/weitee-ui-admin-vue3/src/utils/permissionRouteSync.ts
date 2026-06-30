import type { RouteRecordRaw } from 'vue-router'
import router from '@/router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useUserStoreWithOut } from '@/store/modules/user'

const collectRouteNames = (routes: AppRouteRecordRaw[] = [], names: Set<string> = new Set()) => {
  routes.forEach((route) => {
    if (route.name) {
      names.add(String(route.name))
    }
    if (route.children?.length) {
      collectRouteNames(route.children, names)
    }
  })
  return names
}

export const syncCurrentSessionPermissionRoutes = async () => {
  const permissionStore = usePermissionStoreWithOut()
  const userStore = useUserStoreWithOut()
  const previousRouteNames = collectRouteNames(permissionStore.addRouters)
  const activeRouteNames = new Set(
    router.currentRoute.value.matched
      .map((route) => route.name)
      .filter((name): name is string => Boolean(name))
  )

  const userInfo = await userStore.refreshUserInfoAction()
  if (!userInfo) {
    throw new Error('Current user info is empty, unable to sync permission routes')
  }

  await permissionStore.generateRoutes()

  const nextRouteNames = collectRouteNames(permissionStore.addRouters)
  previousRouteNames.forEach((routeName) => {
    if (!nextRouteNames.has(routeName) && !activeRouteNames.has(routeName) && router.hasRoute(routeName)) {
      router.removeRoute(routeName)
    }
  })

  permissionStore.getAddRouters.forEach((route) => {
    router.addRoute(route as unknown as RouteRecordRaw)
  })
}
