<template>
  <section class="gantt-hero">
    <div class="gantt-hero__header">
      <div>
        <div class="gantt-hero__breadcrumb">制造执行管理 / 排程甘特图</div>
        <div class="gantt-page__title">排程甘特图</div>
      </div>
    </div>
  </section>

  <ContentWrap class="gantt-page__filter-card">
    <el-form label-position="top" class="gantt-query">
      <div class="gantt-query__grid">
        <el-form-item label="工作中心" prop="workCenterId">
          <el-select
            v-model="workCenterId"
            filterable
            placeholder="请选择工作中心"
            style="width: 100%"
          >
            <el-option
              v-for="item in workCenterList"
              :key="item.id"
              :label="`${item.centerName}（${item.centerCode}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="range">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="statusFilter" clearable placeholder="全部状态">
            <el-option label="已排程" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
      </div>
      <div class="gantt-query__footer">
        <div class="gantt-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="gantt-page__chart-card">
    <div class="gantt-tip">
      <span class="gantt-tip__item"><i class="gantt-dot gantt-dot--primary"></i>已排程</span>
      <span class="gantt-tip__item"><i class="gantt-dot gantt-dot--success"></i>已完成</span>
      <span class="gantt-tip__item"><i class="gantt-dot gantt-dot--warning"></i>进行中</span>
      <span class="gantt-tip__hint">拖动调整时间（默认对齐整点，按住 Shift 精确到分钟）</span>
    </div>
    <div ref="chartRef" v-loading="loading" class="gantt-chart"></div>
    <div v-if="!tasks.length && !loading" class="gantt-empty">
      <div class="gantt-empty__icon">
        <Icon icon="ep:histogram" />
      </div>
      <div class="gantt-empty__title">该范围内暂无已排程任务</div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { formatDate } from '@/utils/formatTime'
import { WorkCenterApi, WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { WorkTaskApi, WorkTaskVO } from '@/api/mes/work-task'

defineOptions({ name: 'MesWorkTaskGantt' })

const message = useMessage()

const chartRef = ref<HTMLDivElement>()
const loading = ref(false)
const workCenterId = ref<number>()
const dateRange = ref<string[]>([])
const statusFilter = ref<number>() // 状态筛选（可选）
const workCenterList = ref<WorkCenterSimpleVO[]>([])
const tasks = ref<WorkTaskVO[]>([])

let chart: echarts.ECharts | null = null
let dragState: {
  taskId: number
  mode: 'move' | 'resize-left' | 'resize-right'
  startMs: number
  endMs: number
  offsetMs: number
} | null = null
let dataCache: any[] = []

// canvas 填充不支持 CSS 变量：运行时从设计 Token 解析为具体色值（取不到时用 fallback）
const resolveCssColor = (varName: string, fallback: string) => {
  const val = getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
  return val || fallback
}

const TASK_COLOR: Record<number, string> = {
  1: resolveCssColor('--erp-primary-600', '#2563eb'),
  2: resolveCssColor('--erp-warning-600', '#d97706'),
  3: resolveCssColor('--erp-success-600', '#059669'),
  4: resolveCssColor('--erp-slate-400', '#94a3b8')
}

const statusLabel: Record<number, string> = {
  1: '已排程',
  2: '进行中',
  3: '已完成'
}

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'MM-DD HH:mm') : '-'

const loadData = async () => {
  if (!workCenterId.value) {
    message.warning('请先选择工作中心')
    return
  }
  const now = Date.now()
  const startTime =
    dateRange.value?.[0] || formatDate(new Date(now - 7 * 86400000), 'YYYY-MM-DD HH:mm:ss')
  const endTime =
    dateRange.value?.[1] || formatDate(new Date(now + 7 * 86400000), 'YYYY-MM-DD HH:mm:ss')
  loading.value = true
  try {
    tasks.value =
      (await WorkTaskApi.getGanttList(
        workCenterId.value,
        startTime,
        endTime,
        statusFilter.value
      )) || []
    await renderChart()
  } finally {
    loading.value = false
  }
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    bindDrag(chart)
  }
  const data = tasks.value.map((t) => ({
    id: t.id,
    taskNo: t.taskNo,
    start: new Date(Number(t.planStartTime)).getTime(),
    end: new Date(Number(t.planEndTime)).getTime(),
    status: t.status || 1,
    label: `${t.stepNo}·${t.stepName} ${t.taskNo}`
  }))
  dataCache = data

  const option = {
    grid: { left: 60, right: 20, top: 30, bottom: 40 },
    tooltip: {
      formatter: (params: any) => {
        const d = params.data
        if (!d) return ''
        return `${d.label}<br/>${formatDateValue(d.start)} ~ ${formatDateValue(d.end)}<br/>状态：${statusLabel[d.status] || '-'}`
      }
    },
    xAxis: {
      type: 'time',
      axisLabel: {
        color: 'var(--erp-slate-500)',
        fontSize: 11,
        formatter: (value: number) => {
          const d = new Date(value)
          return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}\n${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
        }
      },
      splitLine: { lineStyle: { color: 'var(--erp-slate-100)' } }
    },
    yAxis: {
      type: 'category',
      data: [
        workCenterList.value.find((c) => c.id === workCenterId.value)?.centerName || '工作中心'
      ],
      axisLabel: { color: 'var(--erp-slate-600)', fontSize: 12, fontWeight: 600 }
    },
    series: [
      {
        type: 'custom',
        renderItem: (params: any, api: any) => {
          const start = api.coord([api.value(1), api.value(0)])
          const end = api.coord([api.value(2), api.value(0)])
          const height = Math.min(api.size([0, 1])[1] * 0.5, 28)
          const label = String(api.value(3) || '')
          const status = api.value(4)
          const taskId = api.value(5)
          const rect = {
            x: start[0],
            y: start[1] - height / 2,
            width: Math.max(end[0] - start[0], 4),
            height,
            shape: {
              x: start[0],
              y: start[1] - height / 2,
              width: Math.max(end[0] - start[0], 4),
              height
            }
          }
          return {
            type: 'group',
            children: [
              {
                type: 'rect',
                shape: rect.shape,
                style: {
                  fill: TASK_COLOR[status] || '#94a3b8',
                  borderRadius: 4
                }
              },
              {
                type: 'text',
                style: {
                  text: label,
                  x: rect.x + 6,
                  y: rect.y + rect.height / 2,
                  textVerticalAlign: 'middle',
                  textAlign: 'left',
                  fontSize: 11,
                  fill: '#ffffff'
                }
              }
            ],
            data: { id: taskId, status }
          }
        },
        encode: { x: [1, 2], y: 0 },
        data: dataCache.map((d) => [0, d.start, d.end, d.label, d.status, d.id])
      }
    ]
  }
  chart.setOption(option as any)
}

const bindDrag = (chartInstance: echarts.ECharts) => {
  const zr = chartInstance.getZr()
  const HOUR_MS = 60 * 60 * 1000 // 小时对齐粒度
  const MIN_DURATION = 30 * 60 * 1000 // 最小时长 30 分钟
  const EDGE_PX = 8 // 边缘判定像素

  // 拖动期间改用 window 级事件：鼠标移出 canvas 不中断（zr 事件只在 canvas 内生效）
  const onWinMove = (e: MouseEvent) => {
    if (!dragState) return
    const rect = chartInstance.getDom().getBoundingClientRect()
    const offsetX = e.clientX - rect.left
    const offsetY = e.clientY - rect.top
    const point = [offsetX, offsetY]
    const time = chartInstance.convertFromPixel({ xAxisIndex: 0 }, point[0]) as unknown as number
    const { taskId, mode, endMs, offsetMs } = dragState
    let newStart: number
    let newEnd: number
    const target = dataCache.find((d) => d.id === taskId)
    if (!target) return
    if (mode === 'resize-left') {
      newStart = Math.min(time, endMs - MIN_DURATION)
      newEnd = endMs
    } else if (mode === 'resize-right') {
      newStart = target.start
      newEnd = Math.max(time, target.start + MIN_DURATION)
    } else {
      newStart = time - offsetMs
      newEnd = newStart + (endMs - (dragState.startMs || 0))
    }
    // 拖拽精度：默认对齐整小时（大范围粗调）；按住 Shift 精确到分钟（小时范围内细调）
    if (!e.shiftKey) {
      newStart = Math.round(newStart / HOUR_MS) * HOUR_MS
      newEnd = Math.round(newEnd / HOUR_MS) * HOUR_MS
    }
    dataCache = dataCache.map((d) => (d.id !== taskId ? d : { ...d, start: newStart, end: newEnd }))
    chartInstance.setOption({
      series: [{ data: dataCache.map((d) => [0, d.start, d.end, d.label, d.status, d.id]) }]
    } as any)
  }

  const onWinUp = () => {
    if (!dragState) return
    void handleDragEnd(chartInstance)
    window.removeEventListener('mousemove', onWinMove)
    window.removeEventListener('mouseup', onWinUp)
  }

  const getDragMode = (
    chart: echarts.ECharts,
    x: number,
    y: number,
    task: any
  ): 'move' | 'resize-left' | 'resize-right' => {
    // 计算块左右边缘的像素位置
    const leftPx = chart.convertToPixel({ xAxisIndex: 0 }, task.start) as unknown as number
    const rightPx = chart.convertToPixel({ xAxisIndex: 0 }, task.end) as unknown as number
    if (Math.abs(x - leftPx) <= EDGE_PX) return 'resize-left'
    if (Math.abs(x - rightPx) <= EDGE_PX) return 'resize-right'
    return 'move'
  }

  zr.on('mousedown', (e: any) => {
    const point = [e.offsetX, e.offsetY]
    const time = chartInstance.convertFromPixel({ xAxisIndex: 0 }, point[0]) as unknown as number
    // y 轴类别值（单工作中心为 0），判断点击是否落在任务行内
    const yVal = chartInstance.convertFromPixel({ yAxisIndex: 0 }, point[1]) as unknown as number
    const inRow = Math.abs(yVal - 0) < 0.6
    // 坐标反查任务块（不依赖 zrender element data，避免渲染层数据丢失）
    const task = dataCache.find((d) => time >= d.start && time <= d.end && inRow)
    if (!task) return
    // 仅已排程(1)/进行中(2)可拖动
    if (task.status !== 1 && task.status !== 2) return
    const mode = getDragMode(chartInstance, e.offsetX, e.offsetY, task)
    dragState = {
      taskId: task.id,
      mode,
      startMs: task.start,
      endMs: task.end,
      offsetMs: time - task.start
    }
    zr.setCursorStyle(mode === 'move' ? 'grabbing' : 'col-resize')
    // 注册 window 级拖动事件（鼠标移出 canvas 不中断）
    window.addEventListener('mousemove', onWinMove)
    window.addEventListener('mouseup', onWinUp)
  })
  zr.on('mousemove', (e: any) => {
    const point = [e.offsetX, e.offsetY]
    const time = chartInstance.convertFromPixel({ xAxisIndex: 0 }, point[0]) as unknown as number
    if (dragState) {
      const { taskId, mode, endMs, offsetMs } = dragState
      let newStart: number
      let newEnd: number
      const target = dataCache.find((d) => d.id === taskId)
      if (!target) return
      if (mode === 'resize-left') {
        // 左边缘拉伸：改开始，结束不变
        newStart = Math.min(time, endMs - MIN_DURATION)
        newEnd = endMs
      } else if (mode === 'resize-right') {
        // 右边缘拉伸：改结束，开始不变
        newStart = target.start
        newEnd = Math.max(time, target.start + MIN_DURATION)
      } else {
        // 平移：时长不变
        newStart = time - offsetMs
        newEnd = newStart + (endMs - (dragState.startMs || 0))
      }
      // 基于内存缓存更新块位置（custom series 的 getOption 数据结构不稳定）
      dataCache = dataCache.map((d) =>
        d.id !== taskId ? d : { ...d, start: newStart, end: newEnd }
      )
      chartInstance.setOption({
        series: [{ data: dataCache.map((d) => [0, d.start, d.end, d.label, d.status, d.id]) }]
      } as any)
      return
    }
    // 非拖动：悬停提示（边缘显示 col-resize）
    const yVal = chartInstance.convertFromPixel({ yAxisIndex: 0 }, point[1]) as unknown as number
    const inRow = Math.abs(yVal - 0) < 0.6
    const task = dataCache.find((d) => time >= d.start && time <= d.end && inRow)
    if (task && (task.status === 1 || task.status === 2)) {
      const mode = getDragMode(chartInstance, e.offsetX, e.offsetY, task)
      zr.setCursorStyle(mode === 'move' ? 'grab' : 'col-resize')
    } else {
      zr.setCursorStyle('default')
    }
  })
  zr.on('mouseup', () => {
    void handleDragEnd(chartInstance)
    window.removeEventListener('mousemove', onWinMove)
    window.removeEventListener('mouseup', onWinUp)
  })
}

const handleDragEnd = async (chartInstance: echarts.ECharts) => {
  if (!dragState) return
  const { taskId, startMs, endMs, mode } = dragState
  dragState = null
  chartInstance.getZr().setCursorStyle('default')
  const target = dataCache.find((d) => d.id === taskId)
  if (!target) return
  const newStart = target.start
  const newEnd = target.end
  const changed = Math.abs(newStart - startMs) > 60000 || Math.abs(newEnd - endMs) > 60000
  if (!changed) return
  // 本地冲突预检：与其他块重叠则回滚
  const conflict = dataCache.some((d) => {
    if (!d || d.id === taskId) return false
    return newStart < d.end && newEnd > d.start
  })
  if (conflict) {
    message.warning(
      mode === 'move' ? '目标位置与其他任务冲突，已回滚' : '拉伸后与其他任务冲突，已回滚'
    )
    await renderChart()
    return
  }
  try {
    await WorkTaskApi.updatePlanTime({
      id: taskId,
      planStartTime: formatDate(new Date(newStart), 'YYYY-MM-DD HH:mm:ss'),
      planEndTime: formatDate(new Date(newEnd), 'YYYY-MM-DD HH:mm:ss')
    })
    message.success(mode === 'move' ? '调整计划时间成功' : '调整任务时长成功')
    await loadData()
  } catch {
    await renderChart()
  }
}

const handleQuery = () => {
  loadData()
}

const resetQuery = () => {
  workCenterId.value = undefined
  dateRange.value = []
  tasks.value = []
  chart?.clear()
}

const resize = () => {
  chart?.resize()
}

onMounted(async () => {
  try {
    workCenterList.value = await WorkCenterApi.getWorkCenterSimpleList()
  } catch {
    workCenterList.value = []
  }
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.gantt-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.gantt-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.gantt-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.gantt-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.gantt-page__filter-card,
.gantt-page__chart-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.gantt-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }
}

.gantt-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.gantt-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.gantt-tip {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--erp-slate-200);
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--erp-slate-500);
}

.gantt-tip__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.gantt-tip__hint {
  margin-left: auto;
  color: var(--erp-slate-400);
}

.gantt-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.gantt-dot--primary {
  background: var(--erp-primary-400);
}

.gantt-dot--success {
  background: var(--erp-success-400);
}

.gantt-dot--warning {
  background: var(--erp-warning-400);
}

.gantt-chart {
  width: 100%;
  height: 420px;
}

.gantt-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.gantt-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.gantt-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .gantt-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
