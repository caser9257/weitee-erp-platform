import assert from 'node:assert/strict'
import { generateRoute } from './routerHelper'

const marketAlertMenu = {
  path: 'market-alert',
  name: '市场预警与统计',
  icon: 'ep:warning',
  component: 'erp/sale/market-alert/index',
  componentName: 'ErpMarketAlertPage',
  redirect: '',
  meta: {},
  visible: true,
  keepAlive: true,
  parentId: 931300,
  children: [
    {
      path: '',
      name: '查询',
      icon: '',
      component: '',
      componentName: undefined,
      redirect: '',
      meta: {},
      visible: true,
      keepAlive: true,
      parentId: 931310
    },
    {
      path: '',
      name: '更新规则',
      icon: '',
      component: '',
      componentName: undefined,
      redirect: '',
      meta: {},
      visible: true,
      keepAlive: true,
      parentId: 931310
    }
  ]
} as any

const [route] = generateRoute([marketAlertMenu])

assert.equal(route.name, 'ErpMarketAlertPage')
assert.equal(route.path, 'market-alert')
assert.equal(route.children, undefined)
assert.ok(route.component, '带按钮权限子项的菜单页仍应直接解析为页面组件路由')
