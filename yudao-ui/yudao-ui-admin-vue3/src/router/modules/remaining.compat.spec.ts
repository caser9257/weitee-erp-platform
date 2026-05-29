import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const remainingModuleSource = readFileSync(new URL('./remaining.ts', import.meta.url), 'utf8')

assert.match(
  remainingModuleSource,
  /path:\s*'purchase\/order'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/erp\/purchase\/order\/index\.vue'\)/,
  'remaining.ts 应该提供 /erp/purchase/order 兼容路由，支持从业务页和旧链接跳转进入采购订单'
)

assert.match(
  remainingModuleSource,
  /path:\s*'purchase\/order'[\s\S]*activeMenu:\s*'\/scm\/purchase-order'/,
  '采购订单兼容路由应保持 activeMenu 指向 /scm/purchase-order，避免菜单高亮异常'
)

assert.match(
  remainingModuleSource,
  /path:\s*'purchase\/in'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/erp\/purchase\/in\/index\.vue'\)/,
  'remaining.ts 应该提供 /erp/purchase/in 兼容路由，供采购订单页跳转采购入库列表和创建页使用'
)

assert.match(
  remainingModuleSource,
  /path:\s*'purchase\/in'[\s\S]*activeMenu:\s*'\/scm\/inbound'/,
  '采购入库兼容路由应保持 activeMenu 指向 /scm/inbound，避免菜单高亮异常'
)

assert.match(
  remainingModuleSource,
  /path:\s*'mrp\/finish-quality'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/erp\/mrp\/finish-quality\/index\.vue'\)/,
  'remaining.ts 应该提供 /mrp/finish-quality 兼容路由，供成品质检页直达'
)

assert.match(
  remainingModuleSource,
  /path:\s*'mrp\/finish-quality'[\s\S]*activeMenu:\s*'\/qms\/finish-quality'/,
  '成品质检兼容路由应保持 activeMenu 指向 /qms/finish-quality，避免菜单高亮异常'
)

assert.match(
  remainingModuleSource,
  /path:\s*'mrp\/production-inbound'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/erp\/mrp\/production-inbound\/index\.vue'\)/,
  'remaining.ts 应该提供 /mrp/production-inbound 兼容路由，供自制入库单页直达'
)

assert.match(
  remainingModuleSource,
  /path:\s*'mrp\/production-inbound'[\s\S]*activeMenu:\s*'\/scm\/production-inbound'/,
  '自制入库单兼容路由应保持 activeMenu 指向 /scm/production-inbound，避免菜单高亮异常'
)

assert.match(
  remainingModuleSource,
  /path:\s*'sale\/order\/closure-workbench'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/erp\/sale\/order\/closure-workbench\.vue'\)/,
  'remaining.ts 应该提供 /erp/sale/order/closure-workbench 兼容路由，支持从旧链接或外部跳转进入销售闭环工作台'
)

assert.match(
  remainingModuleSource,
  /path:\s*'sale\/order\/closure-workbench'[\s\S]*activeMenu:\s*'\/sales\/closure-workbench'/,
  '销售闭环工作台兼容路由应保持 activeMenu 指向 /sales/closure-workbench，避免菜单高亮错位'
)

assert.match(
  remainingModuleSource,
  /path:\s*'warning'[\s\S]*component:\s*\(\)\s*=>\s*import\('@\/views\/pmo\/project\/warning\/index\.vue'\)/,
  'remaining.ts 应该提供 /project/warning 兼容路由，避免菜单点击后直接 404'
)
