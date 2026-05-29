export interface PermissionRouteBuildOptions<TRoute, TAppRoute> {
  roleRouters: TRoute[]
  mergeMenus: (routes: TRoute[]) => TRoute[]
  generateRoutes: (routes: TRoute[]) => TAppRoute[]
  remainingRouter: TAppRoute[]
  notFoundRoute: TAppRoute
  cloneRoutes: (routes: TAppRoute[]) => TAppRoute[]
}

export interface PermissionRouteBuildResult<TRoute, TAppRoute> {
  routedMenus: TRoute[]
  addRouters: TAppRoute[]
  routers: TAppRoute[]
}

export const buildPermissionRoutes = <TRoute, TAppRoute>(
  options: PermissionRouteBuildOptions<TRoute, TAppRoute>
): PermissionRouteBuildResult<TRoute, TAppRoute> => {
  const routedMenus = options.mergeMenus(options.roleRouters)
  const routerMap = options.generateRoutes(routedMenus)

  return {
    routedMenus,
    addRouters: routerMap.concat([options.notFoundRoute]),
    routers: options.cloneRoutes(options.remainingRouter).concat(routerMap)
  }
}
