import type { RouteLocationNormalized, Router, RouteRecordNormalized } from 'vue-router'
import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { isUrl } from '@/utils/is'
import { cloneDeep, omit } from 'lodash-es'
import qs from 'qs'
import {
  registerComponent as registerAsyncComponent,
  resolveViewModule,
  hasViewModule
} from '@/utils/viewModuleResolver'

/**
 * 娉ㄥ唽涓€涓紓姝ョ粍浠?
 * @param componentPath 渚?/bpm/oa/leave/detail
 */
export const registerComponent = (componentPath: string) =>
  registerAsyncComponent(componentPath)

/* Layout */
export const Layout = () => import('@/layout/Layout.vue')

export const getParentLayout = () => {
  return () =>
    new Promise((resolve) => {
      resolve({
        name: 'ParentLayout'
      })
    })
}

// 鎸夌収璺敱涓璵eta涓嬬殑rank绛夌骇鍗囧簭鏉ユ帓搴忚矾鐢?
export const ascending = (arr: any[]) => {
  arr.forEach((v) => {
    if (v?.meta?.rank === null) v.meta.rank = undefined
    if (v?.meta?.rank === 0) {
      if (v.name !== 'home' && v.path !== '/') {
        console.warn('rank only the home page can be 0')
      }
    }
  })
  return arr.sort((a: { meta: { rank: number } }, b: { meta: { rank: number } }) => {
    return a?.meta?.rank - b?.meta?.rank
  })
}

export const getRawRoute = (route: RouteLocationNormalized): RouteLocationNormalized => {
  if (!route) return route
  const { matched, ...opt } = route
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path
        }))
      : undefined) as RouteRecordNormalized[]
  }
}

// 鍚庣鎺у埗璺敱鐢熸垚
export const generateRoute = (routes: AppCustomRouteRecordRaw[]): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []
  for (const route of routes) {
    // 1. 鐢熸垚 meta 鑿滃崟鍏冩暟鎹?
    const meta = {
      ...(route.meta || {}),
      title: route.name,
      icon: route.icon,
      hidden: !route.visible,
      noCache: !route.keepAlive,
      alwaysShow:
        route.children &&
        route.children.length > 0 &&
        (route.alwaysShow !== undefined ? route.alwaysShow : true)
    } as any
    // 鐗规畩閫昏緫锛氬鏋滃悗绔厤缃殑 MenuDO.component 鍖呭惈 ?锛屽垯琛ㄧず闇€瑕佷紶閫掑弬鏁?
    // 姝ゆ椂锛屾垜浠渶瑕佽В鏋愬弬鏁帮紝骞朵笖灏嗗弬鏁版斁鍒?meta.query 涓?
    // 杩欐牱锛屽悗缁湪 Vue 鏂囦欢涓紝鍙互閫氳繃 const { currentRoute } = useRouter() 涓紝閫氳繃 meta.query 鑾峰彇鍒板弬鏁?
    if (route.component && route.component.indexOf('?') > -1) {
      const query = route.component.split('?')[1]
      route.component = route.component.split('?')[0]
      meta.query = qs.parse(query)
    }

    // 2. 鐢熸垚 data锛圓ppRouteRecordRaw锛?
    // 璺敱鍦板潃杞瀛楁瘝澶у啓椹煎嘲锛屼綔涓鸿矾鐢卞悕绉帮紝閫傞厤keepAlive
    let data: AppRouteRecordRaw = {
      path:
        route.path.indexOf('?') > -1 && !isUrl(route.path) ? route.path.split('?')[0] : route.path, // 娉ㄦ剰锛岄渶瑕佹帓闄?http 杩欑 url锛岄伩鍏嶅畠甯?? 鍙傛暟琚埅鍙栨帀
      name:
        route.componentName && route.componentName.length > 0
          ? route.componentName
          : toCamelCase(route.path, true),
      redirect: route.redirect,
      meta: meta
    }
    //澶勭悊椤剁骇闈炵洰褰曡矾鐢?
    if (!route.children && route.parentId == 0 && route.component) {
      data.component = Layout
      data.meta = {
        hidden: meta.hidden
      }
      data.name = toCamelCase(route.path, true) + 'Parent'
      data.redirect = ''
      meta.alwaysShow = true
      const childrenData: AppRouteRecordRaw = {
        path: '',
        name:
          route.componentName && route.componentName.length > 0
            ? route.componentName
            : toCamelCase(route.path, true),
        redirect: route.redirect,
        meta: meta
      }
      const componentPath = resolveRouteComponentPath(route)
      childrenData.component = resolveViewModule(componentPath, route.path)
      data.children = [childrenData]
    } else {
      // 鐩綍
      if (route.children?.length) {
        data.component = Layout
        data.redirect = getRedirect(route.path, route.children)
        // 澶栭摼
      } else if (isUrl(route.path)) {
        data = {
          path: '/external-link',
          component: Layout,
          meta: {
            name: route.name
          },
          children: [data]
        } as AppRouteRecordRaw
        // 鑿滃崟
      } else {
        // 瀵瑰悗绔紶component缁勪欢璺緞鍜屼笉浼犲仛鍏煎锛堝鏋滃悗绔紶component缁勪欢璺緞锛岄偅涔坧ath鍙互闅忎究鍐欙紝濡傛灉涓嶄紶锛宑omponent缁勪欢璺緞浼氭牴path淇濇寔涓€鑷达級
        const componentPath = resolveRouteComponentPath(route)
        data.component = resolveViewModule(componentPath, route.path)
      }
      if (route.children) {
        data.children = generateRoute(route.children)
      }
    }
    res.push(data as AppRouteRecordRaw)
  }
  return res
}

export const getRedirect = (parentPath: string, children: AppCustomRouteRecordRaw[]) => {
  if (!children || children.length == 0) {
    return parentPath
  }
  const path = generateRoutePath(parentPath, children[0].path)
  // 閫掑綊瀛愯妭鐐?
  if (children[0].children) return getRedirect(path, children[0].children)
}

const generateRoutePath = (parentPath: string, path: string) => {
  if (parentPath.endsWith('/')) {
    parentPath = parentPath.slice(0, -1) // 绉婚櫎榛樿鐨?/
  }
  if (!path.startsWith('/')) {
    path = '/' + path
  }
  return parentPath + path
}

export const pathResolve = (parentPath: string, path: string) => {
  if (isUrl(path)) return path
  if (!path) return parentPath // 淇 path 涓虹┖鏃惰繑鍥?parentPath锛岄伩鍏嶆嫾鎺ュ嚭閿?https://t.zsxq.com/QVr6b
  const childPath = path.startsWith('/') ? path : `/${path}`
  return `${parentPath}${childPath}`.replace(/\/+/g, '/')
}

const resolveRouteComponentPath = (route: AppCustomRouteRecordRaw) => {
  if (route.component && hasViewModule(route.component)) {
    return route.component
  }

  return 'common/menu-placeholder/index'
}

// 璺敱闄嶇骇
export const flatMultiLevelRoutes = (routes: AppRouteRecordRaw[]) => {
  const modules: AppRouteRecordRaw[] = cloneDeep(routes)
  for (let index = 0; index < modules.length; index++) {
    const route = modules[index]
    if (!isMultipleRoute(route)) {
      continue
    }
    promoteRouteLevel(route)
  }
  return modules
}

// 灞傜骇鏄惁澶т簬2
const isMultipleRoute = (route: AppRouteRecordRaw) => {
  if (!route || !Reflect.has(route, 'children') || !route.children?.length) {
    return false
  }

  const children = route.children

  let flag = false
  for (let index = 0; index < children.length; index++) {
    const child = children[index]
    if (child.children?.length) {
      flag = true
      break
    }
  }
  return flag
}

// 鐢熸垚浜岀骇璺敱
const promoteRouteLevel = (route: AppRouteRecordRaw) => {
  let router: Router | null = createRouter({
    routes: [route as RouteRecordRaw],
    history: createWebHashHistory()
  })

  const routes = router.getRoutes()
  addToChildren(routes, route.children || [], route)
  router = null

  route.children = route.children?.map((item) => omit(item, 'children'))
}

// 娣诲姞鎵€鏈夊瓙鑿滃崟
const addToChildren = (
  routes: RouteRecordNormalized[],
  children: AppRouteRecordRaw[],
  routeModule: AppRouteRecordRaw
) => {
  for (let index = 0; index < children.length; index++) {
    const child = children[index]
    const route = routes.find((item) => item.name === child.name)
    if (!route) {
      continue
    }
    routeModule.children = routeModule.children || []
    if (!routeModule.children.find((item) => item.name === route.name)) {
      routeModule.children?.push(route as unknown as AppRouteRecordRaw)
    }
    if (child.children?.length) {
      addToChildren(routes, child.children, routeModule)
    }
  }
}

const toCamelCase = (str: string, upperCaseFirst: boolean) => {
  str = (str || '')
    .replace(/-(.)/g, function (group1: string) {
      return group1.toUpperCase()
    })
    .replaceAll('-', '')

  if (upperCaseFirst && str) {
    str = str.charAt(0).toUpperCase() + str.slice(1)
  }

  return str
}
