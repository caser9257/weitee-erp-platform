<template>
  <div class="home-workbench">
    <el-skeleton :loading="loading" animated>
      <div class="workbench-shell">
        <el-row :gutter="16" class="hero-row">
          <el-col :xl="8" :lg="9" :md="24" :sm="24" :xs="24">
            <section class="hero-card">
              <div class="hero-card__accent hero-card__accent--one"></div>
              <div class="hero-card__accent hero-card__accent--two"></div>

              <div class="hero-card__inner">
                <div class="hero-card__profile">
                  <el-avatar :size="60" :src="avatar" class="hero-card__avatar">
                    {{ userInitial }}
                  </el-avatar>
                  <div class="hero-card__heading">
                    <h1>你好，{{ username }}</h1>
                    <div class="hero-card__meta">
                      <span class="hero-pill">制造执行大区</span>
                      <span class="hero-pill hero-pill--strong">系统运行良好</span>
                    </div>
                  </div>
                </div>

                <div class="hero-card__footer">
                  <div class="hero-card__date">{{ dateLabel }} {{ weekdayLabel }}</div>
                  <div class="hero-card__status">
                    <Icon icon="ep:lightning" />
                    <span>系统运行良好</span>
                  </div>
                </div>
              </div>
            </section>
          </el-col>

          <el-col :xl="16" :lg="15" :md="24" :sm="24" :xs="24">
            <div class="metric-grid">
              <article
                v-for="card in metricCards"
                :key="card.title"
                class="metric-card"
                :class="`metric-card--${card.variant}`"
              >
                <div class="metric-card__head">
                  <span>{{ card.title }}</span>
                  <div class="metric-card__icon" :class="`metric-card__icon--${card.variant}`">
                    <Icon :icon="card.icon" />
                  </div>
                </div>

                <div class="metric-card__value" :class="{ 'is-alert': card.variant === 'alert' }">
                  {{ card.value }}
                </div>

                <div class="metric-card__foot">
                  <span
                    v-if="card.trend"
                    class="metric-chip"
                    :class="card.trendType === 'negative' ? 'metric-chip--negative' : 'metric-chip--positive'"
                  >
                    <Icon :icon="card.trendType === 'negative' ? 'ep:bottom-right' : 'ep:top-right'" />
                    {{ card.trend }}
                  </span>
                  <span v-if="card.subtext" class="metric-card__note" :class="card.noteTone">
                    {{ card.subtext }}
                  </span>
                </div>
              </article>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="content-row">
          <el-col :xl="16" :lg="15" :md="24" :sm="24" :xs="24">
            <el-card shadow="never" class="surface-card surface-card--chart" body-style="padding: 0;">
              <template #header>
                <div class="section-header">
                  <div class="section-header__title">
                    <h3>产能与工单执行趋势</h3>
                  </div>
                  <el-radio-group v-model="trendRange" size="small" :disabled="loading" class="range-switch">
                    <el-radio-button label="week">近一周</el-radio-button>
                    <el-radio-button label="month">近一月</el-radio-button>
                  </el-radio-group>
                </div>
              </template>

              <div class="chart-workbench">
                <section class="chart-panel chart-panel--status">
                  <div class="chart-panel__head">
                    <h4>工单状态总览</h4>
                    <span class="chart-panel__meta">总工单量 1,024</span>
                  </div>

                  <div class="status-overview">
                    <div class="status-ring">
                      <svg class="status-ring__svg" viewBox="0 0 220 220" aria-hidden="true">
                        <path
                          v-for="segment in workOrderRingSegments"
                          :key="segment.label"
                          :d="segment.path"
                          :stroke="segment.color"
                          class="status-ring__segment"
                        />
                      </svg>
                      <div class="status-ring__center">
                        <strong>1,024</strong>
                        <span>总工单量</span>
                      </div>
                    </div>

                    <div class="status-overview__legend">
                      <article
                        v-for="item in activeStatusList"
                        :key="item.label"
                        class="status-overview__item"
                        :style="{ '--tone': item.color }"
                      >
                        <div class="status-overview__label">
                          <span class="status-overview__icon">
                            <Icon :icon="getStatusIcon(item.label)" />
                          </span>
                          <span>{{ item.label }}</span>
                        </div>
                        <div class="status-overview__value">
                          <div class="status-overview__metrics">
                            <strong>{{ item.value.toLocaleString() }}</strong>
                            <span>{{ item.rate }}</span>
                          </div>
                          <i class="status-overview__bar"></i>
                        </div>
                      </article>
                    </div>
                  </div>
                </section>

                <section class="chart-panel chart-panel--output">
                  <div class="chart-panel__head chart-panel__head--trend">
                    <div class="chart-panel__title-group">
                      <h4>车间产量执行趋势</h4>
                    </div>
                    <div class="chart-panel__legend">
                      <span class="trend-legend__item">
                        <i class="trend-legend__dot trend-legend__dot--plan"></i>
                        计划产量
                      </span>
                      <span class="trend-legend__item">
                        <i class="trend-legend__dot trend-legend__dot--actual"></i>
                        实际产量
                      </span>
                    </div>
                  </div>

                  <div class="output-chart">
                    <Echart :options="productionTrendOptions" :height="298" @click="handleTrendChartClick" />
                  </div>
                  <div class="trend-strip">
                    <button
                      v-for="item in currentTrendData"
                      :key="item.name"
                      type="button"
                      class="trend-strip__item"
                      :class="[
                        item.rate < 95 ? 'trend-strip__item--danger' : item.rate >= 100 ? 'trend-strip__item--success' : 'trend-strip__item--normal',
                        selectedTrendName === item.name ? 'is-active' : ''
                      ]"
                      @click="handleTrendCardClick(item.name)"
                    >
                      <span class="trend-strip__head">
                        <span class="trend-strip__day">{{ item.name }}</span>
                        <strong class="trend-strip__rate">{{ Math.round(item.rate) }}%</strong>
                      </span>
                      <span class="trend-strip__track">
                        <i :style="{ width: getTrendBarWidth(item) }"></i>
                      </span>
                    </button>
                  </div>
                </section>
              </div>
            </el-card>
          </el-col>

          <el-col :xl="8" :lg="9" :md="24" :sm="24" :xs="24">
            <el-card shadow="never" class="surface-card surface-card--todo" body-style="padding: 0;">
              <template #header>
                <div class="section-header">
                  <div class="section-header__title">
                    <h3>待办与审批</h3>
                  </div>
                  <span class="section-badge">重点跟进</span>
                </div>
              </template>

              <div class="todo-list">
                <article
                  v-for="item in todoList"
                  :key="item.title"
                  class="todo-item"
                  :class="`todo-item--${item.level}`"
                >
                  <div class="todo-item__icon">
                    <Icon :icon="item.icon" />
                  </div>
                  <div class="todo-item__body">
                    <h4>{{ item.title }}</h4>
                    <div class="todo-item__meta">
                      <span>发起人：{{ item.initiator }}</span>
                      <span class="todo-item__time">
                        <Icon icon="ep:clock" />
                        停留：{{ item.duration }}
                      </span>
                    </div>
                  </div>
                </article>
              </div>

              <el-button class="todo-action" :disabled="loading || !canEnterApproval" @click="handleApprovalClick">
                前往审批中心处理全部（{{ totalState.todo }}）
                <Icon icon="ep:chevron-right" />
              </el-button>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="content-row">
          <el-col :xl="16" :lg="15" :md="24" :sm="24" :xs="24">
            <el-card shadow="never" class="surface-card surface-card--table" body-style="padding: 0;">
              <template #header>
                <div class="section-header section-header--table">
                  <div class="section-header__title">
                    <h3>负责的活跃项目</h3>
                  </div>
                  <span class="section-badge section-badge--muted">查看全部</span>
                </div>
              </template>

              <div class="project-table-wrap">
                <el-table :data="projectList" size="small" :show-header="true" class="project-table">
                  <el-table-column width="56" label="图标">
                    <template #default="scope">
                      <div class="project-icon" :style="{ background: `${scope.row.color}14` }">
                        <Icon :icon="scope.row.icon" :style="{ color: scope.row.color }" />
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column prop="name" label="项目名称" min-width="240">
                    <template #default="scope">
                      <div class="project-name">{{ scope.row.name }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column prop="stage" label="当前阶段" width="160" align="center">
                    <template #default="scope">
                      <span class="stage-badge" :class="`stage-badge--${scope.row.stageType}`">
                        {{ scope.row.stage }}
                      </span>
                    </template>
                  </el-table-column>

                  <el-table-column prop="time" label="更新时间" width="140" align="right">
                    <template #default="scope">
                      <span class="project-time">{{ formatTime(scope.row.time, 'MM-dd HH:mm') }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-card>
          </el-col>

          <el-col :xl="8" :lg="9" :md="24" :sm="24" :xs="24" class="bottom-stack">
            <el-card shadow="never" class="surface-card surface-card--shortcut" body-style="padding: 0;">
              <template #header>
                <div class="section-header">
                  <div class="section-header__title">
                    <h3>快捷入口</h3>
                  </div>
                </div>
              </template>

              <div class="shortcut-grid">
                <button
                  v-for="item in shortcutList"
                  :key="item.name"
                  class="shortcut-card"
                  type="button"
                  @click="handleShortcutClick(item.url)"
                >
                  <span class="shortcut-card__icon">
                    <Icon :icon="item.icon" :style="{ color: item.color }" />
                  </span>
                  <span class="shortcut-card__label">{{ item.name }}</span>
                </button>
              </div>
            </el-card>

            <el-card shadow="never" class="surface-card surface-card--notice" body-style="padding: 0;">
              <template #header>
                <div class="section-header">
                  <div class="section-header__title">
                    <h3>系统公告</h3>
                  </div>
                  <span class="section-badge section-badge--success">最新发布</span>
                </div>
              </template>

              <div class="notice-list">
                <article v-for="item in noticeList" :key="item.title" class="notice-item">
                  <span class="notice-item__dot"></span>
                  <div class="notice-item__body">
                    <div class="notice-item__title">{{ item.title }}</div>
                  </div>
                  <span class="notice-item__time">{{ formatTime(item.date, 'MM-dd') }}</span>
                </article>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-skeleton>
  </div>
</template>

<script lang="ts" setup>
import { formatTime } from '@/utils'
import { type EChartsOption } from 'echarts'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import type { Notice, Project, Shortcut, TodoItem } from './types'

defineOptions({ name: 'HomeWorkbenchIndustrial' })

const router = useRouter()
const userStore = useUserStore()

type TrendTone = 'danger' | 'primary' | 'success'

type WorkOrderStatusItem = {
  label: string
  value: number
  rate: string
  color: string
}

type WorkOrderRingSegment = {
  label: string
  color: string
  path: string
}

type TrendPoint = {
  name: string
  plan: number
  actual: number
  rate: number
  tone: TrendTone
  snapshot: WorkOrderStatusItem[]
}

const loading = ref(true)
const trendRange = ref<'week' | 'month'>('week')
const selectedTrendName = ref<string | null>(null)

const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname || '同事'
const userInitial = computed(() => username.slice(0, 1) || '王')

const now = new Date()
const dateLabel = formatTime(now, 'yyyy年MM月dd日')
const weekdayNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const weekdayLabel = weekdayNames[now.getDay()]

const metricCards = [
  {
    title: '执行中项目',
    value: '124',
    trend: '+4.2%',
    trendType: 'positive',
    subtext: '',
    noteTone: '',
    variant: 'normal',
    icon: 'ep:briefcase'
  },
  {
    title: '今日待办事项',
    value: '10',
    trend: '',
    trendType: 'positive',
    subtext: '3 项已逾期',
    noteTone: 'is-alert',
    variant: 'warning',
    icon: 'ep:clock'
  },
  {
    title: '设备稼动率（OEE）',
    value: '89.5%',
    trend: '-1.2%',
    trendType: 'negative',
    subtext: '2 台维护中',
    noteTone: '',
    variant: 'success',
    icon: 'ep:trend-charts'
  },
  {
    title: '系统异常告警',
    value: '2',
    trend: '',
    trendType: 'positive',
    subtext: '点击立即处理',
    noteTone: 'is-alert',
    variant: 'alert',
    icon: 'ep:warning-filled'
  }
] as const

const totalState = reactive({
  access: 9987,
  todo: 10
})

const createStatusList = (items: Array<[string, number, string]>): WorkOrderStatusItem[] => {
  const total = items.reduce((sum, [, value]) => sum + value, 0)

  return items.map(([label, value, color]) => ({
    label,
    value,
    color,
    rate: `${Math.round((value / total) * 100)}%`
  }))
}

const overviewWorkOrderStatusList = createStatusList([
  ['生产中', 356, '#0f766e'],
  ['待排产', 256, '#2563eb'],
  ['已完工', 205, '#10b981'],
  ['待质检', 153, '#f59e0b'],
  ['已入库', 54, '#cbd5e1']
])

const todoList: TodoItem[] = [
  {
    title: '合同评审：PRJ-20260401 伺服电机改造',
    initiator: '张三',
    duration: '2 小时',
    icon: 'ep:document',
    level: 'urgent'
  },
  {
    title: '原材料采购申请审核（第3批次）',
    initiator: '李四',
    duration: '1 天',
    icon: 'ep:box',
    level: 'normal'
  }
]

const projects: Project[] = [
  {
    name: '微泰 MES 系统重构一期',
    icon: 'ep:monitor',
    stage: '制造执行系统',
    stageType: 'cyan',
    time: new Date('2026-04-22T09:30:00'),
    color: '#0891b2'
  },
  {
    name: '微泰 ERP 系统云端迁移',
    icon: 'ep:briefcase',
    stage: '项目计划',
    stageType: 'amber',
    time: new Date('2026-04-15T14:20:00'),
    color: '#2563eb'
  },
  {
    name: '二期车间环境监测系统安装',
    icon: 'ep:activity',
    stage: '部署与实施',
    stageType: 'emerald',
    time: new Date('2026-04-12T11:05:00'),
    color: '#10b981'
  },
  {
    name: '来料加工 M 系列壳体打磨',
    icon: 'ep:box',
    stage: '加工/委外',
    stageType: 'slate',
    time: new Date('2026-04-10T16:45:00'),
    color: '#64748b'
  }
]

const notices: Notice[] = [
  {
    title: '微泰 MES 系统车间看板功能正式上线试运行',
    date: new Date('2026-04-15')
  },
  {
    title: '关于射频通信器件生产流程优化的通知',
    date: new Date('2026-04-12')
  },
  {
    title: '生产现场防静电安全规范更新（2026版）',
    date: new Date('2026-04-10')
  }
]

const shortcuts: Shortcut[] = [
  {
    name: '工单下发',
    icon: 'ep:document-checked',
    url: '/mes/work-order',
    color: '#0891b2'
  },
  {
    name: '生产排程',
    icon: 'ep:calendar',
    url: '/mes/schedule',
    color: '#2563eb'
  },
  {
    name: '采购订单',
    icon: 'ep:shopping-cart',
    url: '/erp/purchase',
    color: '#10b981'
  }
]

const workOrderDataByRange = {
  week: {
    周一: createStatusList([
      ['生产中', 342, '#0f766e'],
      ['待排产', 252, '#2563eb'],
      ['已完工', 216, '#10b981'],
      ['待质检', 146, '#f59e0b'],
      ['已入库', 68, '#cbd5e1']
    ]),
    周二: createStatusList([
      ['生产中', 324, '#0f766e'],
      ['待排产', 264, '#2563eb'],
      ['已完工', 228, '#10b981'],
      ['待质检', 142, '#f59e0b'],
      ['已入库', 66, '#cbd5e1']
    ]),
    周三: createStatusList([
      ['生产中', 310, '#0f766e'],
      ['待排产', 258, '#2563eb'],
      ['已完工', 226, '#10b981'],
      ['待质检', 158, '#f59e0b'],
      ['已入库', 72, '#cbd5e1']
    ]),
    周四: createStatusList([
      ['生产中', 336, '#0f766e'],
      ['待排产', 250, '#2563eb'],
      ['已完工', 214, '#10b981'],
      ['待质检', 148, '#f59e0b'],
      ['已入库', 76, '#cbd5e1']
    ]),
    周五: createStatusList([
      ['生产中', 354, '#0f766e'],
      ['待排产', 242, '#2563eb'],
      ['已完工', 208, '#10b981'],
      ['待质检', 152, '#f59e0b'],
      ['已入库', 68, '#cbd5e1']
    ]),
    周六: createStatusList([
      ['生产中', 300, '#0f766e'],
      ['待排产', 246, '#2563eb'],
      ['已完工', 232, '#10b981'],
      ['待质检', 168, '#f59e0b'],
      ['已入库', 78, '#cbd5e1']
    ]),
    周日: createStatusList([
      ['生产中', 288, '#0f766e'],
      ['待排产', 238, '#2563eb'],
      ['已完工', 240, '#10b981'],
      ['待质检', 172, '#f59e0b'],
      ['已入库', 86, '#cbd5e1']
    ])
  },
  month: {
    第一周: createStatusList([
      ['生产中', 332, '#0f766e'],
      ['待排产', 250, '#2563eb'],
      ['已完工', 220, '#10b981'],
      ['待质检', 152, '#f59e0b'],
      ['已入库', 70, '#cbd5e1']
    ]),
    第二周: createStatusList([
      ['生产中', 346, '#0f766e'],
      ['待排产', 244, '#2563eb'],
      ['已完工', 214, '#10b981'],
      ['待质检', 148, '#f59e0b'],
      ['已入库', 72, '#cbd5e1']
    ]),
    第三周: createStatusList([
      ['生产中', 360, '#0f766e'],
      ['待排产', 238, '#2563eb'],
      ['已完工', 208, '#10b981'],
      ['待质检', 140, '#f59e0b'],
      ['已入库', 78, '#cbd5e1']
    ]),
    第四周: createStatusList([
      ['生产中', 318, '#0f766e'],
      ['待排产', 246, '#2563eb'],
      ['已完工', 226, '#10b981'],
      ['待质检', 160, '#f59e0b'],
      ['已入库', 74, '#cbd5e1']
    ])
  }
} as const

const trendDataMap: Record<'week' | 'month', TrendPoint[]> = {
  week: [
    { name: '周一', plan: 130, actual: 132, rate: 102, tone: 'success', snapshot: workOrderDataByRange.week.周一 },
    { name: '周二', plan: 140, actual: 146, rate: 104, tone: 'success', snapshot: workOrderDataByRange.week.周二 },
    { name: '周三', plan: 128, actual: 126, rate: 98, tone: 'primary', snapshot: workOrderDataByRange.week.周三 },
    { name: '周四', plan: 120, actual: 118, rate: 98, tone: 'primary', snapshot: workOrderDataByRange.week.周四 },
    { name: '周五', plan: 150, actual: 152, rate: 101, tone: 'success', snapshot: workOrderDataByRange.week.周五 },
    { name: '周六', plan: 112, actual: 108, rate: 96, tone: 'primary', snapshot: workOrderDataByRange.week.周六 },
    { name: '周日', plan: 106, actual: 100, rate: 94, tone: 'danger', snapshot: workOrderDataByRange.week.周日 }
  ],
  month: [
    { name: '第一周', plan: 118, actual: 118, rate: 100, tone: 'primary', snapshot: workOrderDataByRange.month.第一周 },
    { name: '第二周', plan: 132, actual: 135, rate: 102, tone: 'success', snapshot: workOrderDataByRange.month.第二周 },
    { name: '第三周', plan: 138, actual: 142, rate: 103, tone: 'success', snapshot: workOrderDataByRange.month.第三周 },
    { name: '第四周', plan: 140, actual: 139, rate: 99, tone: 'primary', snapshot: workOrderDataByRange.month.第四周 }
  ]
}

const currentTrendData = computed(() => trendDataMap[trendRange.value])
const selectedTrendPoint = computed(
  () => currentTrendData.value.find((item) => item.name === selectedTrendName.value) ?? null
)
const activeStatusList = computed<WorkOrderStatusItem[]>(() =>
  selectedTrendPoint.value?.snapshot ?? overviewWorkOrderStatusList
)
const getStatusIcon = (label: string) => {
  const iconMap: Record<string, string> = {
    生产中: 'ep:trend-charts',
    待排产: 'ep:clock',
    已完工: 'ep:select',
    待质检: 'ep:warning',
    已入库: 'ep:box'
  }

  return iconMap[label] ?? 'ep:data-analysis'
}

const polarToCartesian = (centerX: number, centerY: number, radius: number, angleInDegrees: number) => {
  const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180
  return {
    x: centerX + radius * Math.cos(angleInRadians),
    y: centerY + radius * Math.sin(angleInRadians)
  }
}

const describeArc = (
  centerX: number,
  centerY: number,
  radius: number,
  startAngle: number,
  endAngle: number
) => {
  const start = polarToCartesian(centerX, centerY, radius, endAngle)
  const end = polarToCartesian(centerX, centerY, radius, startAngle)
  const largeArcFlag = endAngle - startAngle <= 180 ? '0' : '1'

  return `M ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArcFlag} 0 ${end.x} ${end.y}`
}

const workOrderRingSegments = computed<WorkOrderRingSegment[]>(() => {
  const items = activeStatusList.value
  const total = items.reduce((sum, item) => sum + item.value, 0)
  const gapAngle = 0
  const availableAngle = 360 - gapAngle * items.length
  let currentAngle = -90 + gapAngle / 2

  return items.map((item) => {
    const span = total > 0 ? (item.value / total) * availableAngle : 0
    const startAngle = currentAngle
    const endAngle = currentAngle + span
    currentAngle = endAngle + gapAngle

    return {
      label: item.label,
      color: item.color,
      path: describeArc(110, 110, 72, startAngle, endAngle)
    }
  })
})

const productionTrendOptions = computed<EChartsOption>(() => {
  const data = currentTrendData.value
  const selectedName = selectedTrendPoint.value?.name

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: '#dbe4ee',
      borderWidth: 1,
      textStyle: {
        color: '#0f172a'
      },
      formatter: (params: { name?: string; seriesName?: string; value?: number }[]) => {
        const currentName = params?.[0]?.name ?? ''
        const currentItem = data.find((item) => item.name === currentName)

        if (!currentItem) {
          return currentName
        }

        return [
          `<strong>${currentItem.name}</strong>`,
          `计划产量：${currentItem.plan} 件`,
          `实际产量：${currentItem.actual} 件`,
          `达成率：${currentItem.rate.toFixed(1)}%`
        ].join('<br />')
      }
    },
    grid: {
      left: 8,
      right: 8,
      top: 18,
      bottom: 12,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.name),
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#e2e8f0'
        }
      },
      axisLabel: {
        color: '#64748b',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#edf2f7',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '计划产量',
        type: 'bar',
        data: data.map((item) => item.plan),
        barWidth: 12,
        itemStyle: {
          color: '#cbd5e1',
          borderRadius: [6, 6, 2, 2]
        },
        emphasis: {
          itemStyle: {
            color: '#cbd5e1'
          }
        },
        z: 1
      },
      {
        name: '实际产量',
        type: 'line',
        data: data.map((item) => ({
          value: item.actual,
          itemStyle: {
            opacity: selectedName ? (selectedName === item.name ? 1 : 0.4) : 1
          }
        })),
        symbol: 'circle',
        symbolSize: 7,
        smooth: true,
        itemStyle: {
          color: '#2563eb'
        },
        lineStyle: {
          width: 3,
          color: '#2563eb'
        },
        z: 3,
        areaStyle: {
          color: 'rgba(37, 99, 235, 0.08)'
        },
        emphasis: {
          itemStyle: {
            color: '#2563eb'
          }
        }
      }
    ]
  }
})

const getTrendBarWidth = (item: TrendPoint) => `${Math.min((item.actual / item.plan) * 100, 112)}%`

watch(trendRange, () => {
  selectedTrendName.value = null
})

const handleTrendChartClick = (params: { seriesType?: string; name?: string }) => {
  if (!params.name || !['line', 'bar'].includes(params.seriesType ?? '')) {
    return
  }

  selectedTrendName.value = selectedTrendName.value === params.name ? null : params.name
}

const handleTrendCardClick = (name: string) => {
  selectedTrendName.value = selectedTrendName.value === name ? null : name
}

const projectList = computed(() => projects.slice(0, 4))
const noticeList = computed(() => notices.slice(0, 3))
const shortcutList = computed(() => shortcuts)
const canEnterApproval = computed(() => totalState.todo > 0)

const handleShortcutClick = (url: string) => {
  if (!url) {
    return
  }
  router.push(url)
}

const handleApprovalClick = () => {
  if (!canEnterApproval.value || loading.value) {
    return
  }
  router.push('/approval/todo')
}

onMounted(() => {
  loading.value = false
})
</script>

<style scoped lang="scss">
.home-workbench {
  padding: 8px;
  border-radius: 28px;
  background: #f8fafc;
}

.workbench-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-row,
.content-row {
  align-items: stretch;
}

.hero-card,
.metric-card,
.surface-card {
  border: 1px solid rgb(226 232 240 / 0.92);
  border-radius: 20px;
  background: rgb(255 255 255 / 0.98);
  box-shadow: 0 8px 22px rgb(148 163 184 / 0.09);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgb(14 165 233 / 0.16);
    box-shadow: 0 14px 30px rgb(148 163 184 / 0.14);
  }
}

.hero-card {
  position: relative;
  overflow: hidden;
  min-height: 196px;
  padding: 18px 20px;
  background:
    radial-gradient(circle at 84% 14%, rgb(255 255 255 / 0.18), transparent 24%),
    radial-gradient(circle at 10% 92%, rgb(34 211 238 / 0.16), transparent 28%),
    linear-gradient(138deg, #1e3a8a 0%, #2563eb 56%, #0f766e 100%);
  color: #fff;
  box-shadow: 0 14px 30px rgb(37 99 235 / 0.12);

  &::after {
    position: absolute;
    inset: 0;
    border: 1px solid rgb(255 255 255 / 0.08);
    border-radius: inherit;
    content: '';
    pointer-events: none;
  }
}

.hero-card__accent {
  position: absolute;
  border-radius: 999px;
  filter: blur(24px);
  pointer-events: none;
}

.hero-card__accent--one {
  top: -22px;
  right: -18px;
  width: 128px;
  height: 128px;
  background: rgb(255 255 255 / 0.14);
}

.hero-card__accent--two {
  left: -18px;
  bottom: -26px;
  width: 168px;
  height: 168px;
  background: rgb(34 211 238 / 0.18);
}

.hero-card__inner {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 160px;
  flex-direction: column;
  justify-content: space-between;
}

.hero-card__profile {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.hero-card__avatar {
  flex-shrink: 0;
  border: 1px solid rgb(255 255 255 / 0.28);
  background: linear-gradient(180deg, rgb(255 255 255 / 0.22), rgb(255 255 255 / 0.12));
  color: #fff;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.24),
    0 10px 22px rgb(15 23 42 / 0.08);
}

.hero-card__heading {
  min-width: 0;

  h1 {
    margin: 4px 0 0;
    font-size: 20px;
    font-weight: 700;
    line-height: 1.32;
    letter-spacing: 0.01em;
  }
}

.hero-card__eyebrow {
  margin: 0;
  color: rgb(255 255 255 / 0.72);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.hero-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgb(255 255 255 / 0.14);
  color: rgb(255 255 255 / 0.88);
  font-size: 12px;
  font-weight: 600;
}

.hero-pill--strong {
  background: rgb(255 255 255 / 0.22);
}

.hero-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgb(255 255 255 / 0.18);
}

.hero-card__date {
  color: rgb(255 255 255 / 0.88);
  font-size: 12px;
  font-weight: 600;
}

.hero-card__status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 11px;
  border-radius: 999px;
  background: rgb(255 255 255 / 0.14);
  color: #fff;
  font-size: 12px;
  font-weight: 700;

  .iconify {
    color: #fde68a;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  height: 100%;
}

.metric-card {
  position: relative;
  min-height: 92px;
  height: 100%;
  padding: 13px 16px 12px;
  border-top-width: 2px;
  border-top-color: rgb(148 163 184 / 0.42);
  background: linear-gradient(180deg, rgb(255 255 255 / 0.98), rgb(249 251 252 / 0.98));
  box-shadow:
    0 6px 16px rgb(148 163 184 / 0.08),
    inset 0 1px 0 rgb(255 255 255 / 0.85);
}

.metric-card--normal {
  border-color: rgb(219 232 252 / 0.92);
  border-top-color: rgb(96 165 250 / 0.72);
  background:
    radial-gradient(circle at top right, rgb(224 242 254 / 0.55), transparent 28%),
    linear-gradient(180deg, rgb(255 255 255 / 0.98), rgb(249 251 252 / 0.98));
}

.metric-card--alert {
  border-color: rgb(254 205 211 / 0.82);
  border-top-color: rgb(251 113 133 / 0.82);
  background:
    radial-gradient(circle at top right, rgb(255 228 230 / 0.56), transparent 30%),
    linear-gradient(180deg, rgb(255 248 249 / 0.98), rgb(255 255 255 / 0.98));
}

.metric-card--warning {
  border-color: rgb(253 230 138 / 0.72);
  border-top-color: rgb(245 158 11 / 0.72);
  background:
    radial-gradient(circle at top right, rgb(254 249 195 / 0.54), transparent 28%),
    linear-gradient(180deg, rgb(255 255 255 / 0.98), rgb(252 251 247 / 0.98));
}

.metric-card--success {
  border-color: rgb(167 243 208 / 0.72);
  border-top-color: rgb(45 212 191 / 0.72);
  background:
    radial-gradient(circle at top right, rgb(204 251 241 / 0.5), transparent 28%),
    linear-gradient(180deg, rgb(255 255 255 / 0.98), rgb(247 252 250 / 0.98));
}

.metric-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: rgb(248 250 252 / 0.9);
  border: 1px solid rgb(226 232 240 / 0.9);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 0.8);

  .iconify {
    font-size: 16px;
    color: #2563eb;
  }
}

.metric-card__icon--warning {
  background: rgb(255 251 235 / 0.92);
  border-color: rgb(253 230 138 / 0.8);

  .iconify {
    color: #d97706;
  }
}

.metric-card__icon--success {
  background: rgb(236 253 245 / 0.92);
  border-color: rgb(167 243 208 / 0.8);

  .iconify {
    color: #059669;
  }
}

.metric-card__icon--alert {
  background: rgb(255 241 242 / 0.92);
  border-color: rgb(254 205 211 / 0.92);

  .iconify {
    color: #e11d48;
  }
}

.metric-card__value {
  margin-top: 12px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.metric-card__value.is-alert {
  color: #e11d48;
}

.metric-card__foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.metric-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.metric-chip--positive {
  background: rgb(236 253 245 / 1);
  color: #059669;
}

.metric-chip--negative {
  background: rgb(254 242 242 / 1);
  color: #e11d48;
}

.metric-card__note {
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
}

.metric-card__note.is-alert {
  color: #e11d48;
}

.surface-card {
  overflow: hidden;
  background: #fff;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-header__title {
  min-width: 0;

  h3 {
    margin: 0;
    color: #0f172a;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.3;
  }
}

.section-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgb(186 230 253 / 0.9);
  background: rgb(240 249 255 / 1);
  color: #0369a1;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.section-badge--muted {
  border-color: rgb(226 232 240 / 0.92);
  background: rgb(241 245 249 / 1);
  color: #64748b;
}

.section-badge--success {
  border-color: rgb(167 243 208 / 0.9);
  background: rgb(236 253 245 / 1);
  color: #059669;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding: 20px;
}

.chart-panel {
  display: flex;
  min-height: 300px;
  flex-direction: column;
  padding: 16px 16px 14px;
  border: 1px solid rgb(226 232 240 / 0.9);
  border-radius: 18px;
  background: #fff;

  h4 {
    margin: 0 0 10px;
    color: #475569;
    font-size: 14px;
    font-weight: 700;
  }
}

.chart-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.chart-panel__meta {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border: 1px solid rgb(226 232 240 / 0.9);
  border-radius: 999px;
  background: rgb(248 250 252 / 0.96);
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.chart-workbench {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  padding: 18px;
}

.chart-panel--status,
.chart-panel--output {
  min-height: 290px;
}

.status-overview {
  display: grid;
  grid-template-columns: minmax(220px, 0.9fr) minmax(0, 1.1fr);
  gap: 18px;
  align-items: start;
}

.status-ring {
  position: relative;
  min-width: 0;
  max-width: 248px;
  margin: 0 auto;
}

.status-ring__svg {
  display: block;
  width: 100%;
  height: auto;
  overflow: visible;
}

.status-ring__segment {
  fill: none;
  stroke-linecap: butt;
  stroke-width: 18;
}

.status-ring__center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;

  strong {
    color: #0f172a;
    font-size: 28px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }

  span {
    margin-top: 8px;
    color: #94a3b8;
    font-size: 11px;
    font-weight: 600;
  }
}

.status-overview__legend {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 6px;
}

.status-overview__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid rgb(241 245 249 / 0.96);

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

.status-overview__label {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
}

.status-overview__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 10px;
  background: color-mix(in srgb, var(--tone) 8%, white 92%);
  color: var(--tone);

  .iconify {
    font-size: 14px;
  }
}

.status-overview__value {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 96px;
  justify-content: flex-end;
}

.status-overview__metrics {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;

  strong {
    color: #0f172a;
    font-size: 16px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
  }

  span {
    color: #64748b;
    font-size: 10px;
    font-weight: 600;
  }
}

.status-overview__bar {
  width: 4px;
  height: 22px;
  flex-shrink: 0;
  border-radius: 999px;
  background: var(--tone);
  opacity: 0.9;
}

.output-chart {
  height: 298px;
}

.trend-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 2px 0 8px;
}

.trend-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.trend-legend__dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.trend-legend__dot--plan {
  background: #cbd5e1;
}

.trend-legend__dot--actual {
  background: #2563eb;
}

.trend-strip {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 7px;
  width: 100%;
  margin-top: 14px;
}

.trend-strip__item {
  display: flex;
  min-height: 68px;
  flex-direction: column;
  align-items: stretch;
  gap: 5px;
  padding: 8px 4px 2px;
  border: 1px solid rgb(226 232 240 / 0.65);
  border-radius: 12px;
  background: #fff;
  text-align: left;
  overflow: hidden;
  transition:
    border-color 0.18s ease,
    transform 0.18s ease,
    box-shadow 0.18s ease;

  &.is-active {
    border-color: rgb(37 99 235 / 0.28);
    background: rgb(248 250 252 / 0.92);
    box-shadow: 0 0 0 1px rgb(37 99 235 / 0.06);
    transform: translateY(-1px);
  }
}

.trend-strip__item--danger {
  border-color: rgb(251 113 133 / 0.18);
}

.trend-strip__item--success {
  border-color: rgb(52 211 153 / 0.18);
}

.trend-strip__item--normal {
  background: #fff;
}

.trend-strip__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 6px;
}

.trend-strip__day {
  color: #94a3b8;
  font-size: 10px;
  font-weight: 700;
  flex: 1 1 auto;
  min-width: 0;
}

.trend-strip__rate {
  color: #0f172a;
  flex: 0 0 auto;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  letter-spacing: 0;
}

.trend-strip__item--danger .trend-strip__rate {
  color: #e11d48;
}

.trend-strip__item--success .trend-strip__rate {
  color: #059669;
}

.trend-strip__track {
  position: relative;
  display: block;
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: rgb(241 245 249 / 1);

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #93c5fd 0%, #3b82f6 100%);
  }
}

.trend-strip__item--danger .trend-strip__track i {
  background: linear-gradient(90deg, #fecdd3 0%, #f43f5e 100%);
}

.trend-strip__item--success .trend-strip__track i {
  background: linear-gradient(90deg, #bbf7d0 0%, #10b981 100%);
}

.chart-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chart-panel__head h4 {
  margin: 0;
}

.chart-panel__title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chart-panel__legend {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding-top: 2px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.todo-item {
  display: flex;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 16px;
  border: 1px solid rgb(226 232 240 / 0.9);
  background: linear-gradient(180deg, rgb(250 252 253 / 0.96), rgb(247 250 252 / 0.92));
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgb(14 165 233 / 0.16);
    background: rgb(255 255 255 / 0.98);
  }
}

.todo-item--urgent {
  border-color: rgb(251 113 133 / 0.22);
  background: linear-gradient(180deg, rgb(255 241 242 / 0.98), rgb(255 247 248 / 0.96));
  box-shadow: inset 3px 0 0 #f43f5e;
}

.todo-item--normal {
  box-shadow: inset 3px 0 0 #0ea5e9;
}

.todo-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 14px;
  flex-shrink: 0;
  background: rgb(255 255 255 / 0.88);
  border: 1px solid rgb(226 232 240 / 0.9);
  color: #0f766e;
}

.todo-item__body {
  min-width: 0;
  flex: 1;

  h4 {
    margin: 0;
    color: #0f172a;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.45;
    word-break: break-word;
  }
}

.todo-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
}

.todo-item__time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.todo-item--urgent .todo-item__time {
  color: #e11d48;
}

.todo-action {
  width: calc(100% - 40px);
  height: 40px;
  margin: 0 20px 20px;
  border-radius: 12px;
  border-color: rgb(191 219 254 / 0.92);
  background: rgb(255 255 255 / 0.98);
  color: #0f6aa9;
  font-weight: 600;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    border-color: rgb(56 189 248 / 0.3);
    background: rgb(240 249 255 / 1);
    color: #0f6aa9;
  }

  .iconify {
    margin-left: 6px;
  }
}

.project-table-wrap {
  overflow-x: auto;
}

.project-table {
  width: 100%;
}

.project-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid rgb(226 232 240 / 0.72);
}

.project-name {
  color: #0f172a;
  font-weight: 600;
  line-height: 1.45;
  font-size: 13px;
}

.stage-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 9px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.stage-badge--cyan {
  background: rgb(236 254 255 / 1);
  border-color: rgb(165 243 252 / 1);
  color: #0891b2;
}

.stage-badge--amber {
  background: rgb(255 251 235 / 1);
  border-color: rgb(253 230 138 / 1);
  color: #d97706;
}

.stage-badge--emerald {
  background: rgb(236 253 245 / 1);
  border-color: rgb(167 243 208 / 1);
  color: #059669;
}

.stage-badge--slate {
  background: rgb(241 245 249 / 1);
  border-color: rgb(226 232 240 / 1);
  color: #64748b;
}

.project-time {
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, monospace;
  font-size: 11px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 20px;
}

.shortcut-card {
  display: flex;
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 78px;
  padding: 12px 8px;
  border: 1px solid rgb(226 232 240 / 0.9);
  border-radius: 16px;
  background: rgb(248 250 252 / 0.9);
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgb(59 130 246 / 0.18);
    background: rgb(255 255 255 / 0.98);
    box-shadow: 0 10px 18px rgb(15 23 42 / 0.05);
  }
}

.shortcut-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 12px;
  background: rgb(255 255 255 / 0.98);
  border: 1px solid rgb(226 232 240 / 0.9);
}

.shortcut-card__label {
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #334155;
  border-bottom: 1px solid rgb(241 245 249 / 0.96);

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

.notice-item__dot {
  width: 6px;
  height: 6px;
  flex-shrink: 0;
  border-radius: 999px;
  background: #cbd5e1;
}

.notice-item__body {
  min-width: 0;
  flex: 1;
}

.notice-item__title {
  overflow: hidden;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item__time {
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, monospace;
  font-size: 11px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgb(226 232 240 / 0.88);
  background: linear-gradient(180deg, rgb(255 255 255 / 0.98), rgb(249 251 252 / 0.94));
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgb(226 232 240 / 0.9);
  --el-table-text-color: #334155;
  --el-table-row-hover-bg-color: rgb(59 130 246 / 0.05);
}

:deep(.el-table td),
:deep(.el-table th.is-leaf) {
  border-bottom-color: rgb(226 232 240 / 0.9);
}

:deep(.project-table .cell) {
  padding-top: 2px;
  padding-bottom: 2px;
}

:deep(.project-table .el-table__row td) {
  padding-top: 10px;
  padding-bottom: 10px;
}

:deep(.el-radio-button__inner) {
  border-color: rgb(203 213 225 / 0.92);
  background: rgb(255 255 255 / 0.96);
  color: #64748b;
  box-shadow: none;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  border-color: #2563eb;
  background: #2563eb;
  color: #fff;
}

:deep(.el-skeleton__item) {
  background: linear-gradient(
    90deg,
    rgb(233 239 245 / 0.92),
    rgb(244 247 250 / 0.98),
    rgb(233 239 245 / 0.92)
  );
}

@media (max-width: 1280px) {
  .hero-card__heading h1 {
    font-size: 18px;
  }
}

@media (max-width: 992px) {
  .metric-grid,
  .shortcut-grid,
  .chart-workbench {
    grid-template-columns: 1fr;
  }

  .hero-card {
    min-height: 212px;
  }

  .metric-card {
    min-height: 96px;
  }

  .status-overview {
    grid-template-columns: 1fr;
  }

  .status-ring {
    max-width: 228px;
  }

  .trend-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .home-workbench {
    padding: 6px;
  }

  .hero-card {
    padding: 18px;
  }

  .hero-card__inner,
  .chart-panel {
    min-height: auto;
  }

  .hero-card__footer,
  .section-header {
    flex-wrap: wrap;
  }

  .chart-panel__head {
    flex-wrap: wrap;
  }

  .chart-panel__legend {
    padding-top: 0;
  }

  .status-ring {
    max-width: 210px;
  }

  .status-ring__center strong {
    font-size: 24px;
  }

  .trend-strip {
    grid-template-columns: 1fr;
  }

  .section-header--table {
    gap: 8px;
  }

  .todo-item,
  .notice-item,
  .project-name {
    word-break: break-word;
  }

  .todo-action {
    width: calc(100% - 24px);
    margin: 0 12px 16px;
  }
}
</style>
