export interface GroupedMenuEntry {
  key: string
  title: string
  icon?: string
  order: number
  basePath: string
  routes: AppRouteRecordRaw[]
}

const joinRoutePath = (parentPath: string, path: string) => {
  if (!path) {
    return parentPath
  }

  const normalizedParent = parentPath.endsWith('/') ? parentPath.slice(0, -1) : parentPath
  const normalizedChild = path.startsWith('/') ? path : `/${path}`
  return `${normalizedParent}${normalizedChild}`.replace(/\/+/g, '/')
}

const isGroupedRoute = (route: AppRouteRecordRaw) => Boolean(route.meta?.menuGroupKey)

const isDatabaseGroupRoute = (route: AppRouteRecordRaw) => {
  const children = route.children || []
  const routePath = route.path || ''
  return (
    (!routePath || !routePath.startsWith('/')) &&
    children.length > 0 &&
    children.every((child) => !child.children?.length)
  )
}

const hasGroupedDescendant = (route: AppRouteRecordRaw): boolean => {
  if (isGroupedRoute(route) || isDatabaseGroupRoute(route)) {
    return true
  }
  return (route.children || []).some((child) => hasGroupedDescendant(child))
}

const routeMatchesPath = (
  route: AppRouteRecordRaw,
  targetPath: string,
  parentPath = '/'
): boolean => {
  const fullPath = route.path?.startsWith('/') ? route.path : joinRoutePath(parentPath, route.path)
  if (fullPath === targetPath) {
    return true
  }

  return (route.children || []).some((child) => routeMatchesPath(child, targetPath, fullPath))
}

const visitGroupedRoutes = (
  route: AppRouteRecordRaw,
  groupedRouteMap: Map<string, GroupedMenuEntry>,
  directRoutes: AppRouteRecordRaw[],
  parentPath = '/'
) => {
  if (route.meta?.hidden) {
    return
  }

  const groupKey = route.meta?.menuGroupKey as string | undefined
  const childRoutes = route.children || []
  const fullPath = route.path?.startsWith('/') ? route.path : joinRoutePath(parentPath, route.path)

  if (!groupKey && isDatabaseGroupRoute(route)) {
    groupedRouteMap.set(fullPath, {
      key: fullPath,
      title: (route.meta?.title as string) || route.name || '',
      icon: route.meta?.icon as string | undefined,
      order: Number(route.meta?.order ?? Number.MAX_SAFE_INTEGER),
      basePath: fullPath,
      routes: childRoutes.map((child) => ({
        ...child,
        children: child.children?.map((nestedChild) => ({ ...nestedChild }))
      }))
    })
    return
  }

  if (groupKey) {
    if (!groupedRouteMap.has(groupKey)) {
      groupedRouteMap.set(groupKey, {
        key: groupKey,
        title: (route.meta?.menuGroupTitle as string) || '',
        icon: route.meta?.menuGroupIcon as string | undefined,
        order: Number(route.meta?.menuGroupOrder ?? Number.MAX_SAFE_INTEGER),
        basePath: parentPath,
        routes: []
      })
    }

    groupedRouteMap.get(groupKey)!.routes.push({
      ...route,
      children: route.children?.map((child) => ({ ...child }))
    })
    return
  }

  if (childRoutes.some((child) => hasGroupedDescendant(child))) {
    childRoutes.forEach((child) =>
      visitGroupedRoutes(child, groupedRouteMap, directRoutes, fullPath)
    )
    return
  }

  directRoutes.push(route)
}

export const buildGroupedMenuEntries = (routes: AppRouteRecordRaw[] = [], basePath = '/') => {
  const groupedRouteMap = new Map<string, GroupedMenuEntry>()
  const directRoutes: AppRouteRecordRaw[] = []

  routes.forEach((route) => visitGroupedRoutes(route, groupedRouteMap, directRoutes, basePath))

  const groups = Array.from(groupedRouteMap.values())
    .map((group) => ({
      ...group,
      routes: [...group.routes].sort(
        (left, right) =>
          Number(left.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER) -
          Number(right.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER)
      )
    }))
    .sort((left, right) => {
      if (left.order !== right.order) {
        return left.order - right.order
      }
      return left.key.localeCompare(right.key)
    })

  return {
    groups,
    directRoutes
  }
}

export const hasGroupedMenuEntries = (routes: AppRouteRecordRaw[] = []) => {
  return routes.some((route) => hasGroupedDescendant(route))
}

export const resolveGroupedMenuActiveKey = ({
  groups,
  activeMenu,
  currentPath,
  routeMetaGroupKey
}: {
  groups: GroupedMenuEntry[]
  activeMenu: string
  currentPath: string
  routeMetaGroupKey?: string
}) => {
  if (routeMetaGroupKey && groups.some((group) => group.key === routeMetaGroupKey)) {
    return routeMetaGroupKey
  }

  const matchedByActiveMenu = groups.find((group) =>
    group.routes.some((route) => routeMatchesPath(route, activeMenu, group.basePath))
  )
  if (matchedByActiveMenu) {
    return matchedByActiveMenu.key
  }

  const matchedByCurrentPath = groups.find((group) =>
    group.routes.some((route) => routeMatchesPath(route, currentPath, group.basePath))
  )
  return matchedByCurrentPath?.key || ''
}
