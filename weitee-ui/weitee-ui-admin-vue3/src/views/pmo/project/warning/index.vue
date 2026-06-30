<template>
  <div class="project-warning-page finance-shell finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">项目预警</div>
          <div class="finance-shell__page-subtitle">聚焦高风险项目，快速定位责任人与当前阶段</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip finance-shell__metric-chip--neutral">结果 {{ displayTotal }}</span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--warning">高风险 {{ displayRiskCount.high }}</span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--primary">中风险 {{ displayRiskCount.medium }}</span>
            <span class="finance-shell__metric-chip finance-shell__metric-chip--success">低风险 {{ displayRiskCount.low }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <span v-if="isDemoMode" class="finance-shell__page-demo-badge">示例数据</span>
          <el-button plain :loading="refreshing" :disabled="listLoading" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="项目编号" prop="no">
            <el-input v-model="queryParams.no" placeholder="请输入项目编号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="项目名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入项目名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="客户" prop="customerId">
            <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable :loading="customerLoading" class="!w-full">
              <el-option v-for="item in customerOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型" prop="businessType">
            <el-select v-model="queryParams.businessType" placeholder="请选择业务类型" clearable class="!w-full">
              <el-option label="自研产品" value="SELF_RESEARCH" />
              <el-option label="客供料" value="CUSTOMER_SUPPLIED" />
              <el-option label="来料加工" value="TOLL_MANUFACTURING" />
              <el-option label="工艺验证" value="PROCESS_VALIDATION" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险等级" prop="riskLevel">
            <el-select v-model="queryParams.riskLevel" placeholder="请选择风险等级" clearable class="!w-full">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">预警工作台</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ displayTotal }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button type="success" plain :loading="exportLoading" :disabled="listLoading" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>

      <el-alert v-if="listErrorMessage && !list.length" type="error" :closable="false" show-icon class="mb-12px" :title="listErrorMessage">
        <template #default>
          <el-button link type="primary" :disabled="listLoading" @click="handleRefresh">重新加载</el-button>
        </template>
      </el-alert>

      <template v-else>
        <div v-if="listLoading || displayList.length" class="finance-shell__table-wrap">
          <el-table v-loading="listLoading" :data="displayList" row-key="id" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
            <el-table-column min-width="240">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:collection" class="finance-shell__column-icon" />
                  项目信息
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.name || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.no || '-' }}</span>
                  <div class="finance-shell__row-tags">
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--neutral">{{ resolveBusinessTypeLabel(row.businessType) }}</span>
                    <span class="finance-shell__metric-pill" :class="resolveRiskClass(row.riskLevel)">{{ resolveRiskLabel(row.riskLevel) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:warning-filled" class="finance-shell__column-icon" />
                  预警摘要
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ resolveStageLabel(row.currentStageCode) }}</span>
                  <span class="finance-shell__muted-text">{{ row.customerName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="180">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:user" class="finance-shell__column-icon" />
                  责任人
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.planCoordinatorName || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.materialControllerName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="140" align="center">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:calendar" class="finance-shell__column-icon" />
                  交期
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__muted-text">{{ formatDateValue(row.deliveryDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="120" align="center">
              <template #default="{ row }">
                <span class="finance-shell__metric-pill" :class="row.status === 1 ? 'finance-shell__metric-pill--success' : 'finance-shell__metric-pill--warning'">
                  {{ resolveStatusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="140">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button link type="primary" v-hasPermi="['erp:project:query']" @click.stop="openDetail(row.id)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else :description="isDemoMode ? '当前无真实数据，已展示示例数据' : '暂无项目预警'" />
        <Pagination v-if="displayTotal > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="displayTotal" @pagination="handlePagination" />
      </template>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerVisible"
      size="980px"
      destroy-on-close
      :with-header="false"
      :modal-class="'project-warning-page__drawer-mask'"
    >
      <div class="project-warning-page__drawer">
        <div class="project-warning-page__drawer-context">
          <div>
            <div class="project-warning-page__drawer-eyebrow">项目预警</div>
            <div class="project-warning-page__drawer-title">{{ detailData.name || '项目详情' }}</div>
            <div class="project-warning-page__drawer-meta">
              <span>{{ detailData.no || '未提供编号' }}</span>
              <span>{{ detailData.customerName || '-' }}</span>
              <span>{{ resolveRiskLabel(detailData.riskLevel) }}</span>
            </div>
          </div>
            <div class="project-warning-page__drawer-summary">
            <div class="project-warning-page__drawer-summary-card">
              <div class="project-warning-page__drawer-summary-label">当前阶段</div>
              <div class="project-warning-page__drawer-summary-value">
                {{ resolveStageLabel(detailData.currentStageCode) }}
              </div>
            </div>
            <div class="project-warning-page__drawer-summary-card">
              <div class="project-warning-page__drawer-summary-label">交期</div>
              <div class="project-warning-page__drawer-summary-value finance-shell__mono">
                {{ formatDateValue(detailData.deliveryDate) }}
              </div>
            </div>
            <div class="project-warning-page__drawer-summary-card">
              <div class="project-warning-page__drawer-summary-label">状态</div>
              <div class="project-warning-page__drawer-summary-value">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </div>
            </div>
          </div>
        </div>

        <div class="project-warning-page__drawer-body" v-loading="detailLoading">
          <el-result
            v-if="detailLoadError"
            icon="error"
            title="项目预警详情加载失败"
            :sub-title="detailLoadError"
            class="project-warning-page__drawer-error"
          >
            <template #extra>
              <el-button type="primary" :disabled="detailLoading || detailId == null" @click="loadDetail">
                重试
              </el-button>
            </template>
          </el-result>

          <template v-else-if="detailData.id">
            <div class="finance-shell__section">
              <div class="finance-shell__section-head project-warning-page__section-head--split">
                <div class="finance-shell__section-title">待办任务</div>
                <span class="finance-shell__section-badge">共 {{ detailTaskCount }} 条</span>
              </div>
              <div v-if="detailTasks.length" class="project-warning-page__todo-list">
                <div v-for="task in detailTasks" :key="task.id || `${task.roleCode}-${task.taskType}`" class="project-warning-page__todo-item">
                  <div class="project-warning-page__todo-main">
                    <div class="project-warning-page__todo-title">{{ task.summary || task.taskType || '-' }}</div>
                    <div class="project-warning-page__todo-meta">
                      <span class="project-warning-page__todo-pill">{{ task.roleCode || '-' }}</span>
                      <span class="project-warning-page__todo-pill project-warning-page__todo-pill--muted">{{ task.taskType || '-' }}</span>
                      <span class="project-warning-page__todo-pill project-warning-page__todo-pill--muted">{{ formatDateValue(task.dueTime) }}</span>
                    </div>
                    <div v-if="task.remark" class="project-warning-page__todo-sub">{{ task.remark }}</div>
                  </div>
                  <div class="project-warning-page__todo-aside">
                    <span class="finance-shell__metric-pill" :class="task.taskStatus === 'DONE' ? 'finance-shell__metric-pill--success' : 'finance-shell__metric-pill--warning'">
                      {{ task.taskStatus === 'DONE' ? '已完成' : '待处理' }}
                    </span>
                    <span class="project-warning-page__todo-progress">
                      <i :class="task.taskStatus === 'DONE' ? 'project-warning-page__todo-progress-fill' : 'project-warning-page__todo-progress-fill project-warning-page__todo-progress-fill--pending'"></i>
                    </span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无待办任务" />
            </div>

            <div class="finance-shell__section">
              <div class="finance-shell__section-head">
                <div class="finance-shell__section-title">基础信息</div>
              </div>
              <div class="finance-shell__metric-grid project-warning-page__metric-grid">
                <div class="finance-shell__metric-card">
                  <div class="finance-shell__metric-label">项目名称</div>
                  <div class="finance-shell__metric-value project-warning-page__metric-value">{{ detailData.name || '-' }}</div>
                </div>
                <div class="finance-shell__metric-card">
                  <div class="finance-shell__metric-label">客户</div>
                  <div class="finance-shell__metric-value project-warning-page__metric-value">{{ detailData.customerName || '-' }}</div>
                </div>
                <div class="finance-shell__metric-card">
                  <div class="finance-shell__metric-label">业务类型</div>
                  <div class="finance-shell__metric-value project-warning-page__metric-value">
                    {{ resolveBusinessTypeLabel(detailData.businessType) }}
                  </div>
                </div>
                <div class="finance-shell__metric-card">
                  <div class="finance-shell__metric-label">风险等级</div>
                  <div class="finance-shell__metric-value project-warning-page__metric-value">
                    {{ resolveRiskLabel(detailData.riskLevel) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="finance-shell__section">
              <div class="finance-shell__section-head">
                <div class="finance-shell__section-title">责任人</div>
              </div>
              <div class="project-warning-page__people-grid">
                <div class="project-warning-page__person-card">
                  <div class="project-warning-page__person-label">计划协调</div>
                  <div class="project-warning-page__person-value">{{ detailData.planCoordinatorName || '-' }}</div>
                </div>
                <div class="project-warning-page__person-card">
                  <div class="project-warning-page__person-label">物控</div>
                  <div class="project-warning-page__person-value">{{ detailData.materialControllerName || '-' }}</div>
                </div>
                <div class="project-warning-page__person-card">
                  <div class="project-warning-page__person-label">项目经理</div>
                  <div class="project-warning-page__person-value">{{ detailData.projectManagerName || '-' }}</div>
                </div>
              </div>
            </div>

            <div class="finance-shell__section">
              <div class="finance-shell__section-head">
                <div class="finance-shell__section-title">项目摘要</div>
              </div>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="项目编号">
                  <span class="finance-shell__mono">{{ detailData.no || '-' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="交期">
                  <span class="finance-shell__mono">{{ formatDateValue(detailData.deliveryDate) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  {{ resolveStatusLabel(detailData.status) }}
                </el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">
                  {{ detailData.remark || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

          </template>

          <el-empty v-else class="project-warning-page__drawer-empty" description="暂无项目详情" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import download from '@/utils/download'
import { formatDate } from '@/utils/formatTime'
import { ProjectApi, type ProjectVO } from '@/api/erp/project'
import { CustomerApi, type CustomerVO } from '@/api/erp/sale/customer'

defineOptions({ name: 'PmoProjectWarning' })

const message = useMessage()
const queryFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: '',
  name: '',
  customerId: undefined as number | undefined,
  businessType: '',
  riskLevel: '',
  status: undefined as number | undefined
})

const list = ref<ProjectVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listErrorMessage = ref('')
const refreshing = ref(false)
const exportLoading = ref(false)
const customerOptions = ref<CustomerVO[]>([])
const customerLoading = ref(false)
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailLoadError = ref('')
const detailId = ref<number | null>(null)
const detailData = reactive<Partial<ProjectVO>>({})
const detailTasks = ref<
  Array<{
    id?: number
    roleCode?: string
    taskType?: string
    taskStatus?: string
    summary?: string
    dueTime?: string | Date
    finishTime?: string | Date
    remark?: string
  }>
>([])
let detailRequestSeq = 0
const detailTaskCount = computed(() => detailTasks.value.length)

const demoProjects: ProjectVO[] = [
  {
    id: 900001,
    no: 'PJ-2026-0012',
    name: '华东新能源模组项目',
    customerId: 10001,
    customerName: '上海辰科电子有限公司',
    businessType: 'SELF_RESEARCH',
    currentStageCode: 'PLAN_EXECUTION',
    riskLevel: 'HIGH',
    status: 1,
    deliveryDate: '2026-05-28',
    remark: '示例数据：用于无真实数据时预览页面结构',
    planCoordinatorName: '王敏',
    materialControllerName: '赵磊',
    projectManagerName: '刘强',
    todoTasks: [
      {
        id: 910001,
        roleCode: '计划协调',
        taskType: '阶段确认',
        taskStatus: 'PENDING',
        summary: '评审项目主计划并同步关键里程碑',
        dueTime: '2026-05-12',
        remark: '需在周三前完成'
      },
      {
        id: 910002,
        roleCode: '物控',
        taskType: '物料核对',
        taskStatus: 'DONE',
        summary: '确认长周期物料到货节拍',
        dueTime: '2026-05-10',
        finishTime: '2026-05-09',
        remark: '已完成'
      }
    ]
  },
  {
    id: 900002,
    no: 'PJ-2026-0037',
    name: '汽车电子控制器试产项目',
    customerId: 10002,
    customerName: '苏州启航汽车电子',
    businessType: 'TOLL_MANUFACTURING',
    currentStageCode: 'MATERIAL_RISK',
    riskLevel: 'MEDIUM',
    status: 1,
    deliveryDate: '2026-06-08',
    remark: '示例数据：展示中风险项目的责任与阶段',
    planCoordinatorName: '陈瑶',
    materialControllerName: '孙涛',
    projectManagerName: '周颖',
    todoTasks: [
      {
        id: 910003,
        roleCode: '项目经理',
        taskType: '进度跟踪',
        taskStatus: 'PENDING',
        summary: '跟踪客户样件反馈并更新风险项',
        dueTime: '2026-05-14',
        remark: '客户待确认'
      }
    ]
  },
  {
    id: 900003,
    no: 'PJ-2026-0051',
    name: '工业传感器工艺验证项目',
    customerId: 10003,
    customerName: '宁波智联科技',
    businessType: 'PROCESS_VALIDATION',
    currentStageCode: 'CLOSING_REVIEW',
    riskLevel: 'LOW',
    status: 0,
    deliveryDate: '2026-05-20',
    remark: '示例数据：停用状态的项目也能正确展示',
    planCoordinatorName: '马倩',
    materialControllerName: '李岩',
    projectManagerName: '张宁',
    todoTasks: [
      {
        id: 910004,
        roleCode: '项目经理',
        taskType: '关闭确认',
        taskStatus: 'DONE',
        summary: '完成验证结论汇总与关闭审批',
        dueTime: '2026-05-11',
        finishTime: '2026-05-11',
        remark: '已结案'
      }
    ]
  }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const isDemoMode = computed(() => import.meta.env.DEV && !listLoading.value && !listErrorMessage.value && list.value.length === 0)
const displayList = computed(() => (isDemoMode.value ? demoProjects : list.value))
const displayTotal = computed(() => (isDemoMode.value ? demoProjects.length : total.value))
const displayRiskCount = computed(() => {
  const source = isDemoMode.value ? demoProjects : list.value
  const count = { high: 0, medium: 0, low: 0 }
  source.forEach((item) => {
    if (item.riskLevel === 'HIGH') count.high += 1
    else if (item.riskLevel === 'MEDIUM') count.medium += 1
    else count.low += 1
  })
  return count
})

const canQuery = computed(() => !listLoading.value)
const canReset = computed(() => !listLoading.value)

const formatDateValue = (value?: string | Date) => (value ? formatDate(new Date(value), 'YYYY-MM-DD') : '-')
const resolveRiskLabel = (value?: string) => {
  if (value === 'HIGH') return '高风险'
  if (value === 'MEDIUM') return '中风险'
  return '低风险'
}
const resolveRiskClass = (value?: string) => {
  if (value === 'HIGH') return 'finance-shell__metric-pill--warning'
  if (value === 'MEDIUM') return 'finance-shell__metric-pill--primary'
  return 'finance-shell__metric-pill--success'
}
const resolveBusinessTypeLabel = (value?: string) => {
  if (value === 'SELF_RESEARCH') return '自研产品'
  if (value === 'CUSTOMER_SUPPLIED') return '客供料'
  if (value === 'TOLL_MANUFACTURING') return '来料加工'
  if (value === 'PROCESS_VALIDATION') return '工艺验证'
  return value || '-'
}
const resolveStageLabel = (value?: string) => {
  if (value === 'PLAN_CONFIRMED') return '计划已确认'
  if (value === 'PLAN_EXECUTION') return '计划执行中'
  if (value === 'MATERIAL_RISK') return '物料风险'
  if (value === 'CLOSING_REVIEW') return '结项复盘'
  if (!value) return '-'
  return '未配置阶段'
}
const resolveStatusLabel = (value?: number) => {
  if (value === 1) return '启用'
  if (value === 0) return '停用'
  return '-'
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await ProjectApi.getProjectPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch (error: any) {
    list.value = []
    total.value = 0
    listErrorMessage.value = error?.message || '项目预警列表加载失败'
  } finally {
    listLoading.value = false
  }
}

const loadQueryOptions = async () => {
  customerLoading.value = true
  try {
    customerOptions.value = await CustomerApi.getCustomerSimpleList()
  } catch (error: any) {
    message.error(error?.message || '客户列表加载失败')
  } finally {
    customerLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    no: '',
    name: '',
    customerId: undefined,
    businessType: '',
    riskLevel: '',
    status: undefined
  })
  queryFormRef.value?.clearValidate()
  await getList()
}

const handlePagination = async () => {
  await getList()
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    await getList()
  } finally {
    refreshing.value = false
  }
}

const handleExport = async () => {
  if (exportLoading.value) {
    return
  }
  exportLoading.value = true
  try {
    const data = await ProjectApi.exportProject(queryParams)
    download.excel(data, '项目预警.xls')
  } catch (error: any) {
    message.error(error?.message || '项目预警导出失败')
  } finally {
    exportLoading.value = false
  }
}

const loadDetail = async () => {
  if (detailId.value == null) {
    return
  }
  const requestSeq = ++detailRequestSeq
  detailLoading.value = true
  detailLoadError.value = ''
  try {
    const data = await ProjectApi.getProject(detailId.value)
    if (requestSeq !== detailRequestSeq || !detailDrawerVisible.value || detailId.value == null) {
      return
    }
    Object.assign(detailData, data || {})
    detailTasks.value = (data?.todoTasks || []).map((item) => ({
      id: item.id,
      roleCode: item.roleCode,
      taskType: item.taskType,
      taskStatus: item.taskStatus,
      summary: item.summary,
      dueTime: item.dueTime,
      finishTime: item.finishTime,
      remark: item.remark
    }))
  } catch (error: any) {
    if (requestSeq !== detailRequestSeq) {
      return
    }
    detailLoadError.value = error?.message || '项目预警详情加载失败'
    Object.keys(detailData).forEach((key) => delete (detailData as Record<string, any>)[key])
    detailTasks.value = []
  } finally {
    if (requestSeq === detailRequestSeq) {
      detailLoading.value = false
    }
  }
}

const openDetail = async (id?: number) => {
  if (!id) {
    return
  }
  if (isDemoMode.value) {
    const demoDetail = demoProjects.find((item) => item.id === id)
    if (demoDetail) {
      detailRequestSeq += 1
      detailId.value = id
      detailDrawerVisible.value = true
      detailLoadError.value = ''
      Object.assign(detailData, demoDetail)
      detailTasks.value = demoDetail.todoTasks || []
    }
    return
  }
  detailRequestSeq += 1
  detailId.value = id
  detailDrawerVisible.value = true
  await loadDetail()
}

watch(detailDrawerVisible, (visible) => {
  if (visible) {
    return
  }
  detailRequestSeq += 1
  detailId.value = null
  detailLoadError.value = ''
  detailTasks.value = []
  Object.keys(detailData).forEach((key) => delete (detailData as Record<string, any>)[key])
})

onMounted(async () => {
  await Promise.allSettled([getList(), loadQueryOptions()])
})

</script>

<style scoped lang="scss">
@import '../../../erp/finance/shared/readOnlyPage.css';

.finance-shell__metric-chip--neutral {
  color: #334155;
  background: #f8fafc;
  border-color: #dbe4f0;
}

.finance-shell__metric-chip--primary {
  color: #2563eb;
  background: #eff6ff;
  border-color: #bfdbfe;
}

.finance-shell__metric-chip--warning {
  color: #d97706;
  background: #fffbeb;
  border-color: #fde68a;
}

.finance-shell__metric-chip--success {
  color: #059669;
  background: #ecfdf5;
  border-color: #bbf7d0;
}

.finance-shell__page-demo-badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.finance-shell__column-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.finance-shell__column-header--pipeline {
  color: #0f766e;
}

.finance-shell__column-icon {
  color: #94a3b8;
  font-size: 14px;
}

.finance-shell__row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.finance-shell__row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.project-warning-page__metric-value {
  margin-top: 8px;
  font-size: 18px;
}

.project-warning-page__drawer-mask {
  backdrop-filter: blur(6px);
}

.project-warning-page__drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 32px);
  padding: 0 0 16px;
}

.project-warning-page__drawer-context {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #134e4a 100%);
  color: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.18);
}

.project-warning-page__drawer-eyebrow {
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.project-warning-page__drawer-title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.project-warning-page__drawer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
}

.project-warning-page__drawer-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 12px;
  width: min(520px, 100%);
}

.project-warning-page__drawer-summary-card,
.project-warning-page__person-card {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  backdrop-filter: blur(6px);
}

.project-warning-page__drawer-summary-label,
.project-warning-page__person-label {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.project-warning-page__drawer-summary-value,
.project-warning-page__person-value {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 700;
}

.project-warning-page__section-head--split {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.project-warning-page__section-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
}

.project-warning-page__drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
  padding-right: 4px;
}

.project-warning-page__drawer-error {
  padding: 24px 0;
}

.project-warning-page__drawer :deep(.el-drawer__body) {
  padding: 16px 18px 18px;
  overflow: hidden;
}

.project-warning-page__drawer :deep(.el-descriptions__body) {
  background: #fff;
}

.project-warning-page__drawer :deep(.el-descriptions__label) {
  color: #64748b;
  background: #f8fafc;
}

.project-warning-page__drawer :deep(.el-descriptions__content) {
  color: #0f172a;
}

.project-warning-page__metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.project-warning-page__people-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.project-warning-page__todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.project-warning-page__todo-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
}

.project-warning-page__todo-main {
  min-width: 0;
  flex: 1;
}

.project-warning-page__todo-title {
  color: #0f172a;
  font-weight: 600;
}

.project-warning-page__todo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.project-warning-page__todo-pill {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
}

.project-warning-page__todo-pill--muted {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #64748b;
}

.project-warning-page__todo-sub {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.project-warning-page__todo-aside {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.project-warning-page__todo-progress {
  display: inline-flex;
  width: 84px;
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.project-warning-page__todo-progress-fill {
  width: 100%;
  height: 100%;
  border-radius: inherit;
  background: #10b981;
}

.project-warning-page__todo-progress-fill--pending {
  width: 42%;
  background: #f59e0b;
}

.project-warning-page__drawer-empty {
  padding-top: 24px;
}

@media (max-width: 1280px) {
  .project-warning-page__drawer-summary,
  .project-warning-page__people-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .project-warning-page__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .project-warning-page__drawer-context {
    flex-direction: column;
  }

  .project-warning-page__drawer-summary {
    width: 100%;
  }

  .project-warning-page__people-grid,
  .project-warning-page__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .project-warning-page__drawer-summary,
  .project-warning-page__people-grid,
  .project-warning-page__metric-grid {
    grid-template-columns: 1fr;
  }

  .project-warning-page__todo-item {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
