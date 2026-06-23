import assert from 'node:assert/strict'
import { existsSync } from 'node:fs'
import { generateRoute, getRedirect } from '../src/utils/routerHelper.ts'

import {
  buildPermissionRoutes
} from '../src/utils/permissionRouteBuilder.ts'
import { resolveViewModule } from '../src/utils/viewModuleResolver.ts'

type CustomRoute = {
  path: string
  component?: string
}

const testResolveViewModule = () => {
  const modules = {
    '../views/dashboard/index.vue': () => Promise.resolve('dashboard'),
    '../views/erp/manufacturing/process-route/index.vue': () => Promise.resolve('process-route'),
    '../views/erp/manufacturing/work-center/index.vue': () => Promise.resolve('work-center'),
    '../views/erp/manufacturing/device/index.vue': () => Promise.resolve('device'),
    '../views/erp/manufacturing/production-report/index.vue': () =>
      Promise.resolve('production-report'),
    '../views/erp/manufacturing/material-issue/index.vue': () => Promise.resolve('material-issue'),
    '../views/erp/manufacturing/material-return/index.vue': () => Promise.resolve('material-return'),
    '../views/Error/404.vue': () => Promise.resolve('404')
  }

  const matchedModule = resolveViewModule(modules, 'dashboard/index', '/dashboard')
  assert.equal(typeof matchedModule, 'function')

  const materialReturnModule = resolveViewModule(
    modules,
    'erp/manufacturing/material-return/index',
    'material-return'
  )
  assert.equal(typeof materialReturnModule, 'function')

  const manufacturingModules = [
    ['erp/manufacturing/process-route/index', 'route'],
    ['erp/manufacturing/work-center/index', 'work-center'],
    ['erp/manufacturing/device/index', 'device'],
    ['erp/manufacturing/production-report/index', 'production-report'],
    ['erp/manufacturing/material-issue/index', 'material-issue']
  ] as const

  manufacturingModules.forEach(([componentPath, routePath]) => {
    const matchedModule = resolveViewModule(modules, componentPath, routePath)
    assert.equal(typeof matchedModule, 'function')
  })

  const fallbackModule = resolveViewModule(modules, 'missing/page', '/missing')
  assert.equal(typeof fallbackModule, 'function')
}

const testCompatibilityViewExists = () => {
  const compatibilityViewPaths = [
    '../src/views/erp/manufacturing/process-route/index.vue',
    '../src/views/erp/manufacturing/work-center/index.vue',
    '../src/views/erp/manufacturing/device/index.vue',
    '../src/views/erp/manufacturing/production-report/index.vue',
    '../src/views/erp/manufacturing/material-issue/index.vue',
    '../src/views/erp/manufacturing/material-return/index.vue'
  ]

  compatibilityViewPaths.forEach((path) => {
    const viewPath = new URL(path, import.meta.url)
    assert.equal(existsSync(viewPath), true)
  })
}

const testBuildPermissionRoutes = () => {
  const roleRouters: CustomRoute[] = [{ path: '/dashboard', component: 'dashboard/index' }]
  const remainingRouter = [{ path: '/login' }]
  const notFoundRoute = { path: '/:path(.*)*' }

  const result = buildPermissionRoutes({
    roleRouters,
    mergeMenus: (routes) => routes,
    generateRoutes: (routes) => routes.map((route) => ({ path: route.path })),
    remainingRouter,
    notFoundRoute,
    cloneRoutes: (routes) => structuredClone(routes)
  })

  assert.deepEqual(result.addRouters, [{ path: '/dashboard' }, { path: '/:path(.*)*' }])
  assert.deepEqual(result.routers, [{ path: '/login' }, { path: '/dashboard' }])
}

const testBuildPermissionRoutesPropagatesErrors = () => {
  assert.throws(
    () =>
      buildPermissionRoutes({
        roleRouters: [] as CustomRoute[],
        mergeMenus: () => [],
        generateRoutes: () => {
          throw new Error('route generation failed')
        },
        remainingRouter: [],
        notFoundRoute: { path: '/:path(.*)*' },
        cloneRoutes: (routes) => structuredClone(routes)
      }),
    /route generation failed/
  )
}

const testGenerateRouteSkipsInvalidChildPaths = () => {
  const routes = generateRoute([
    {
      path: '/erp',
      name: 'ERP',
      icon: 'ep:menu',
      component: '',
      componentName: 'ErpRoot',
      redirect: '',
      meta: {},
      visible: true,
      keepAlive: false,
      children: [
        {
          path: null as unknown as string,
          name: '非法菜单',
          icon: 'ep:warning',
          component: 'erp/invalid/index',
          componentName: 'InvalidMenu',
          redirect: '',
          meta: {},
          visible: true,
          keepAlive: false
        },
        {
          path: 'finance',
          name: '财务管理',
          icon: 'ep:money',
          component: '',
          componentName: 'FinanceRoot',
          redirect: '',
          meta: {},
          visible: true,
          keepAlive: false,
          children: [
            {
              path: 'ledger',
              name: '总账',
              icon: 'ep:collection',
              component: 'erp/finance/ledger/index',
              componentName: 'FinanceLedger',
              redirect: '',
              meta: {},
              visible: true,
              keepAlive: false
            }
          ]
        }
      ]
    } as any
  ])

  assert.equal(routes.length, 1)
  assert.equal(routes[0].redirect, '/erp/finance/ledger')
  assert.deepEqual(
    routes[0].children?.map((child) => child.path),
    ['finance']
  )
}

const testGetRedirectFallsBackToParentWhenChildrenAreInvalid = () => {
  assert.equal(
    getRedirect('/erp', [
      {
        path: null as unknown as string,
        name: '非法节点',
        icon: '',
        component: '',
        componentName: 'InvalidNode',
        redirect: '',
        meta: {},
        visible: true,
        keepAlive: false
      } as any
    ]),
    '/erp'
  )
}

const run = () => {
  testResolveViewModule()
  testCompatibilityViewExists()
  testBuildPermissionRoutes()
  testBuildPermissionRoutesPropagatesErrors()
  testGenerateRouteSkipsInvalidChildPaths()
  testGetRedirectFallsBackToParentWhenChildrenAreInvalid()
  console.log('permission-guard regression checks passed')
}

run()
