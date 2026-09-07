import type { Router } from 'vue-router'

/**
 * 关键路由清单：这些路由是框架级功能入口，缺失即视为前端不可用。
 * 断言在动态路由全部 addRoute 挂载完成后（dev 环境）执行一次，
 * 数据源取 router.getRoutes() 权威注册表——避免遍历本地路由树导致的时序/嵌套误报。
 */
export const CRITICAL_ROUTES: string[] = [
  '/approval/todo',
  '/approval/submitted',
  '/approval/done',
  '/approval/cc',
  '/bpm/process-instance/detail',
  '/rd/rd-bom'
]

let asserted = false

/**
 * 纯函数：从已注册全路径集合中找出缺失的关键路由
 */
export const findMissingCriticalRoutes = (registeredPaths: Iterable<string>): string[] => {
  const registered = new Set(registeredPaths)
  return CRITICAL_ROUTES.filter((p) => !registered.has(p))
}

/**
 * dev 断言：动态路由挂载完成后调用；缺失直接 console.error 暴露，仅执行一次
 */
export const assertCriticalRoutesMounted = (router: Router): void => {
  if (asserted) {
    return
  }
  asserted = true
  // router.getRoutes() 返回扁平化的全路径注册表（含 addRoute 动态挂载），是唯一权威来源
  const missing = findMissingCriticalRoutes(router.getRoutes().map((route) => route.path))
  if (missing.length) {
    console.error(
      `[route-guard] 关键路由缺失: ${missing.join(', ')} — 请检查动态菜单合并/权限过滤/菜单种子`
    )
  }
}
