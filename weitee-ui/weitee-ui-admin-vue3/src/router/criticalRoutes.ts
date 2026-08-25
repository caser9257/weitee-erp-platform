/**
 * 关键路由清单：这些路由是框架级功能入口，缺失即视为前端不可用。
 * generateRoutes 完成后（dev 环境）会断言全部已注册，缺失直接 console.error。
 */
export const CRITICAL_ROUTES: string[] = [
  '/approval/todo',
  '/approval/submitted',
  '/approval/done',
  '/approval/cc',
  '/bpm/process-instance/detail',
  '/rd/rd-bom'
]

export const collectRoutePaths = (routes: any[], acc: Set<string> = new Set()): Set<string> => {
  for (const route of routes || []) {
    if (route?.path) acc.add(route.path)
    if (route?.children?.length) collectRoutePaths(route.children, acc)
  }
  return acc
}
