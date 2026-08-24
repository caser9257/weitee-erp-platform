/**
 * 生产工单模块交付回归校验
 * 覆盖：菜单种子、权限点、字典修正、后端 summary 接口、前端页面/API 引用。
 * 运行：node scripts/verify-production-order-menu.js
 */
const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..')
const read = (p) => {
  const full = path.join(ROOT, p)
  return fs.existsSync(full) ? fs.readFileSync(full, 'utf8') : ''
}

const failures = []
const assertIncludes = (label, content, needle) => {
  if (!content.includes(needle)) failures.push(`${label}：缺少「${needle}」`)
}
const assertFileExists = (label, p) => {
  if (!fs.existsSync(path.join(ROOT, p))) failures.push(`${label}：文件缺失 ${p}`)
}

// ---------- 1. 菜单种子（11 号 SQL） ----------
const menuSql = read('sql/mysql/manufacturing/11-erp-production-order-menu.sql')
assertIncludes('菜单定义', menuSql, "'生产工单'")
assertIncludes('菜单路径', menuSql, "'production-order'")
assertIncludes('菜单组件', menuSql, "'erp/manufacturing/production-order/index'")
assertIncludes('菜单组件名', menuSql, "'ErpProductionOrder'")
assertIncludes('查询权限', menuSql, "'erp:production-order:query'")
assertIncludes('创建权限', menuSql, "'erp:production-order:create'")
assertIncludes('更新权限', menuSql, "'erp:production-order:update'")
assertIncludes('菜单幂等判重', menuSql, "`parent_id` = @mf_root_id AND `path` = 'production-order'")
assertIncludes('父菜单动态查找 /mes', menuSql, "`path` = '/mes'")
assertIncludes('管理员授权', menuSql, "rm.`role_id` = 1")
assertIncludes('供应链角色查找', menuSql, "'supply_chain_manager'")

// ---------- 2. 字典修正（12 号 SQL） ----------
const dictSql = read('sql/mysql/manufacturing/12-erp-production-order-status-dict-fix.sql')
assertIncludes('字典修正存在', dictSql, "'erp_production_order_status'")
assertIncludes('字典修正 10', dictSql, "'10'")
assertIncludes('字典修正 20', dictSql, "'20'")
assertIncludes('字典修正 30', dictSql, "'30'")

// ---------- 3. 后端 summary 接口 ----------
const javaRoot = 'weitee-module-erp/src/main/java/cn/weitee/erp/module/erp'
assertFileExists('summary VO', `${javaRoot}/controller/admin/mrp/vo/production/ErpProductionOrderSummaryRespVO.java`)
const controller = read(`${javaRoot}/controller/admin/mrp/ErpProductionOrderController.java`)
assertIncludes('summary 接口', controller, '"/summary"')
assertIncludes('summary 权限', controller, "erp:production-order:query")
const serviceImpl = read(`${javaRoot}/service/mrp/ErpProductionOrderServiceImpl.java`)
assertIncludes('summary 实现', serviceImpl, 'getSummary()')
assertIncludes('工序快照生成', serviceImpl, 'buildProductionOrderStep')
assertIncludes('路线校验', serviceImpl, 'validateRouteForRelease')
assertIncludes('完工需已下达', serviceImpl, 'PRODUCTION_ORDER_NOT_RELEASED')
assertIncludes('完工数量上限', serviceImpl, 'PRODUCTION_ORDER_FINISH_QTY_EXCEED')
assertIncludes('计划时间校验', serviceImpl, 'PRODUCTION_ORDER_PLAN_TIME_INVALID')
assertIncludes('已下达禁改', serviceImpl, 'PRODUCTION_ORDER_STATUS_INVALID')
const saveReqVO = read(`${javaRoot}/controller/admin/mrp/vo/production/ErpProductionOrderSaveReqVO.java`)
assertIncludes('计划数量下限', saveReqVO, 'DecimalMin')
const finishReqVO = read(`${javaRoot}/controller/admin/mrp/vo/production/ErpProductionOrderFinishReqVO.java`)
assertIncludes('完工数量下限', finishReqVO, 'DecimalMin')
const mapper = read(`${javaRoot}/dal/mysql/mrp/ErpProductionOrderMapper.java`)
assertIncludes('状态统计 count', mapper, 'selectCount(Integer status)')

// ---------- 4. 前端页面与 API ----------
assertFileExists('工单页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/erp/manufacturing/production-order/index.vue')
const page = read('weitee-ui/weitee-ui-admin-vue3/src/views/erp/manufacturing/production-order/index.vue')
assertIncludes('页面加载统计', page, 'getProductionOrderSummary')
assertIncludes('页面完工弹窗', page, 'finishProductionOrder')
assertIncludes('完工按钮仅已下达', page, 'row.status === PRODUCTION_ORDER_STATUS.RELEASED')
const api = read('weitee-ui/weitee-ui-admin-vue3/src/api/erp/mrp/production-order/index.ts')
assertIncludes('API 统计接口', api, 'getProductionOrderSummary')
assertIncludes('API 完工接口', api, 'finishProductionOrder')

// ---------- 5. 报工模块（阶段3） ----------
const reportController = read(`${javaRoot}/controller/admin/mrp/ErpProductionReportController.java`)
assertIncludes('报工创建接口', reportController, '"/create"')
assertIncludes('报工分页接口', reportController, '"/page"')
assertIncludes('报工权限创建', reportController, "erp:production-report:create")
const stepController = read(`${javaRoot}/controller/admin/mrp/ErpProductionOrderStepController.java`)
assertIncludes('工序开工接口', stepController, '"/start"')
assertIncludes('工序完工接口', stepController, '"/finish"')
const reportServiceImpl = read(`${javaRoot}/service/mrp/ErpProductionReportServiceImpl.java`)
assertIncludes('报工 CAS 累加', reportServiceImpl, 'updateStepQtyByCas')
assertIncludes('报工前序校验', reportServiceImpl, 'PRODUCTION_ORDER_STEP_STATUS_INVALID')
const stepServiceImpl = read(`${javaRoot}/service/mrp/ErpProductionOrderStepServiceImpl.java`)
assertIncludes('前序依赖校验', stepServiceImpl, 'PRODUCTION_ORDER_STEP_PRECEDENT_UNFINISHED')
assertFileExists('报工页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/erp/manufacturing/production-report/index.vue')
const reportPage = read('weitee-ui/weitee-ui-admin-vue3/src/views/erp/manufacturing/production-report/index.vue')
assertIncludes('报工页面提交', reportPage, 'createProductionReport')
assertIncludes('报工页面开工', reportPage, 'startStep')
assertIncludes('报工页面已替换占位', reportPage, 'pr-page__title')
const reportMapperXml = read('weitee-module-erp/src/main/resources/mapper/mrp/ErpProductionOrderStepMapper.xml')
assertIncludes('CAS SQL 存在', reportMapperXml, 'updateStepQtyByCas')

// ---------- 6. 阶段4：工序质检 + 领料按工序归集 ----------
const sqController = read(`${javaRoot}/controller/admin/mrp/ErpProductionStepQualityController.java`)
assertIncludes('工序质检分页', sqController, '"/page"')
assertIncludes('工序质检提交', sqController, '"/submit"')
const sqServiceImpl = read(`${javaRoot}/service/mrp/ErpProductionStepQualityServiceImpl.java`)
assertIncludes('质检单生成', sqServiceImpl, 'createPendingFromReport')
assertIncludes('质检提交校验', sqServiceImpl, 'PRODUCTION_STEP_QUALITY_COUNT_INVALID')
assertIncludes('工序完工门禁', stepServiceImpl, 'PRODUCTION_STEP_QUALITY_PENDING_UNFINISHED')
const stepQualitySql = read('sql/mysql/manufacturing/13-erp-production-step-quality.sql')
assertIncludes('质检表迁移', stepQualitySql, 'erp_production_step_quality')
assertIncludes('qc_flag 迁移', stepQualitySql, 'qc_flag')
assertIncludes('领料工序迁移', stepQualitySql, 'production_order_step_id')
assertIncludes('质检菜单挂 /qms', stepQualitySql, "`path` = '/qms'")
assertFileExists('工序质检页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/erp/manufacturing/step-quality/index.vue')

// ---------- 7. MES 模块（weitee-module-mes 第一阶段） ----------
const mesTaskService = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/service/MesWorkTaskServiceImpl.java')
assertIncludes('任务生成', mesTaskService, 'createTasksByOrderReleased')
assertIncludes('任务幂等', mesTaskService, 'existedStepIds')
assertIncludes('任务 CAS 调整', mesTaskService, 'updatePlanTimeByCas')
assertIncludes('任务 CAS 取消', mesTaskService, 'cancelTaskByCas')
assertIncludes('计划时间校验', mesTaskService, 'MES_WORK_TASK_PLAN_TIME_INVALID')
const mesListener = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/listener/ErpProductionOrderReleasedListener.java')
assertIncludes('事件监听 AFTER_COMMIT', mesListener, 'AFTER_COMMIT')
const mesSql = read('sql/mysql/mes/14-erp-mes-work-task.sql')
assertIncludes('mes 建表', mesSql, 'mes_work_task')
assertIncludes('mes 菜单', mesSql, "'mes:work-task:query'")
assertIncludes('日历表', mesSql, 'mes_work_calendar')
assertFileExists('任务页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/mes/work-task/index.vue')
assertFileExists('日历页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/mes/work-calendar/index.vue')
const releasedEvent = read('weitee-module-erp/src/main/java/cn/weitee/erp/module/erp/framework/event/ErpProductionOrderReleasedEvent.java')
assertIncludes('下达事件', releasedEvent, 'ErpProductionOrderReleasedEvent')
const ldtDeserializer = read('weitee-framework/weitee-common/src/main/java/cn/weitee/erp/framework/common/util/json/databind/TimestampLocalDateTimeDeserializer.java')
assertIncludes('时间反序列化修复', ldtDeserializer, 'yyyy-MM-dd HH:mm:ss')

// ---------- 8. 第二阶段：排程引擎 + 冲突检测 ----------
const schedulingService = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/service/scheduling/MesSchedulingService.java')
assertIncludes('排程入口', schedulingService, 'scheduleByOrder')
assertIncludes('全量排程', schedulingService, 'scheduleAll')
assertIncludes('重排', schedulingService, 'clearAndReschedule')
assertIncludes('冲突检测', schedulingService, 'detectConflicts')
assertIncludes('可用窗口搜索', schedulingService, 'findAvailableWindow')
assertIncludes('交期批量加载', schedulingService, 'selectBatchIds')
const calendarResolver = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/service/scheduling/MesCalendarResolver.java')
assertIncludes('日历解析', calendarResolver, 'findWorkingWindow')
assertIncludes('兜底日历', calendarResolver, 'DEFAULT_WEEK_MASK')
const occupancy = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/service/scheduling/ResourceOccupancy.java')
assertIncludes('占用表', occupancy, 'findOverlap')
const schedSql = read('sql/mysql/mes/15-erp-mes-task-priority.sql')
assertIncludes('优先级迁移', schedSql, 'priority')

// ---------- 9. 第三阶段：实际时间回写 + OEE + 甘特 ----------
const execListener = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/listener/ErpExecutionEventListener.java')
assertIncludes('报工回写监听', execListener, 'onReportCreated')
assertIncludes('完工回写监听', execListener, 'onStepFinished')
const taskMapper = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/dal/mysql/MesWorkTaskMapper.java')
assertIncludes('实际开始回写', taskMapper, 'markActualStart')
assertIncludes('实际结束回写', taskMapper, 'markActualEnd')
assertIncludes('甘特查询', taskMapper, 'selectGanttList')
const oeeService = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/service/MesOeeServiceImpl.java')
assertIncludes('OEE 计算', oeeService, 'setAvailabilityRate')
assertIncludes('OEE 综合', oeeService, 'multiply(HUNDRED)')
const oeeController = read('weitee-module-mes/src/main/java/cn/weitee/erp/module/mes/controller/admin/MesOeeController.java')
assertIncludes('OEE 接口', oeeController, '"/summary"')
const ganttSql = read('sql/mysql/mes/16-erp-mes-gantt-oee-menu.sql')
assertIncludes('甘特菜单', ganttSql, 'work-task-gantt')
assertIncludes('OEE 菜单', ganttSql, "'mes:oee:query'")
assertFileExists('甘特页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/mes/work-task-gantt/index.vue')
assertFileExists('OEE 页面', 'weitee-ui/weitee-ui-admin-vue3/src/views/mes/oee/index.vue')
const reportEvent = read('weitee-module-erp/src/main/java/cn/weitee/erp/module/erp/framework/event/ErpProductionReportCreatedEvent.java')
assertIncludes('报工事件', reportEvent, 'ErpProductionReportCreatedEvent')
const finishEvent = read('weitee-module-erp/src/main/java/cn/weitee/erp/module/erp/framework/event/ErpProductionOrderStepFinishedEvent.java')
assertIncludes('完工事件', finishEvent, 'ErpProductionOrderStepFinishedEvent')

if (failures.length) {
  console.error('生产工单模块回归校验失败：')
  failures.forEach((f) => console.error('  - ' + f))
  process.exit(1)
}
console.log('生产工单模块回归校验通过：菜单 3207 动态挂载于制造管理、权限点/字典修正/summary 接口/前端页面齐备。')
