<template>
  <div class="task-gantt">
    <ContentWrap>
      <template #header>
        <div class="gantt-header">
          <span>甘特图</span>
          <div class="gantt-controls">
            <el-button @click="handleZoomIn">
              <Icon icon="ep:zoom-in" />
            </el-button>
            <el-button @click="handleZoomOut">
              <Icon icon="ep:zoom-out" />
            </el-button>
            <el-radio-group v-model="scaleType" @change="handleScaleChange">
              <el-radio-button value="day">日</el-radio-button>
              <el-radio-button value="week">周</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-if="taskList.length > 0" class="gantt-container">
        <!-- 左侧任务列表 -->
        <div class="gantt-sidebar">
          <div class="sidebar-header">任务名称</div>
          <div
            v-for="task in taskList"
            :key="task.id"
            class="sidebar-item"
            :class="{ 'is-parent': !task.parentId }"
          >
            <span class="task-name">{{ task.name }}</span>
            <span class="task-progress">{{ task.progress }}%</span>
          </div>
        </div>

        <!-- 右侧甘特图区域 -->
        <div class="gantt-chart" ref="chartRef">
          <!-- 时间轴头部 -->
          <div class="timeline-header">
            <div
              v-for="header in timelineHeaders"
              :key="header.key"
              class="timeline-cell"
              :style="{ width: cellWidth + 'px' }"
            >
              {{ header.label }}
            </div>
          </div>

          <!-- 任务条 -->
          <div class="gantt-body">
            <div
              v-for="task in taskList"
              :key="task.id"
              class="gantt-row"
            >
              <div
                class="gantt-bar"
                :style="getBarStyle(task)"
                @click="handleTaskClick(task)"
              >
                <div class="bar-progress" :style="{ width: task.progress + '%' }"></div>
                <span class="bar-label">{{ task.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无任务数据" />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TaskGanttApi, type GanttTaskVO } from '@/api/project/task-gantt'
import dayjs from 'dayjs'

defineOptions({ name: 'TaskGantt' })

const props = defineProps<{
  projectId: number
}>()

const emit = defineEmits(['taskClick'])

const taskList = ref<GanttTaskVO[]>([])
const scaleType = ref<'day' | 'week' | 'month'>('day')
const cellWidth = ref(40)
const chartRef = ref<HTMLElement | null>(null)

// 计算时间范围
const dateRange = computed(() => {
  if (taskList.value.length === 0) return { start: dayjs(), end: dayjs() }

  let minDate = dayjs()
  let maxDate = dayjs()

  taskList.value.forEach(task => {
    if (task.startAt) {
      const start = dayjs(task.startAt)
      if (start.isBefore(minDate)) minDate = start
    }
    if (task.endAt) {
      const end = dayjs(task.endAt)
      if (end.isAfter(maxDate)) maxDate = end
    }
  })

  // 扩展范围
  minDate = minDate.subtract(7, 'day')
  maxDate = maxDate.add(7, 'day')

  return { start: minDate, end: maxDate }
})

// 时间轴头部
const timelineHeaders = computed(() => {
  const headers: { key: string; label: string }[] = []
  const { start, end } = dateRange.value
  let current = start

  while (current.isBefore(end) || current.isSame(end, 'day')) {
    let label = ''
    if (scaleType.value === 'day') {
      label = current.format('MM/DD')
    } else if (scaleType.value === 'week') {
      label = current.format('MM/DD')
    } else {
      label = current.format('YYYY/MM')
    }

    headers.push({
      key: current.format('YYYY-MM-DD'),
      label
    })

    if (scaleType.value === 'day') {
      current = current.add(1, 'day')
    } else if (scaleType.value === 'week') {
      current = current.add(1, 'week')
    } else {
      current = current.add(1, 'month')
    }
  }

  return headers
})

// 计算任务条样式
const getBarStyle = (task: GanttTaskVO) => {
  if (!task.startAt) return { display: 'none' }

  const { start } = dateRange.value
  const taskStart = dayjs(task.startAt)
  const taskEnd = task.endAt ? dayjs(task.endAt) : taskStart.add(1, 'day')

  const daysFromStart = taskStart.diff(start, 'day')
  const duration = taskEnd.diff(taskStart, 'day') + 1

  const left = daysFromStart * cellWidth.value
  const width = Math.max(duration * cellWidth.value, cellWidth.value)

  return {
    left: left + 'px',
    width: width + 'px',
    backgroundColor: task.priorityColor || '#409eff'
  }
}

const fetchTasks = async () => {
  try {
    taskList.value = await TaskGanttApi.getTaskGanttData(props.projectId)
  } catch (error) {
    ElMessage.error('获取甘特图数据失败')
  }
}

const handleZoomIn = () => {
  cellWidth.value = Math.min(100, cellWidth.value + 10)
}

const handleZoomOut = () => {
  cellWidth.value = Math.max(20, cellWidth.value - 10)
}

const handleScaleChange = () => {
  // 重新计算
}

const handleTaskClick = (task: GanttTaskVO) => {
  emit('taskClick', task)
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped lang="scss">
.task-gantt {
  height: 100%;
}

.gantt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gantt-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.gantt-container {
  display: flex;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}

.gantt-sidebar {
  width: 200px;
  min-width: 200px;
  border-right: 1px solid #eee;
  background: #fafafa;
}

.sidebar-header {
  padding: 8px 12px;
  font-weight: 500;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
}

.sidebar-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #eee;
  font-size: 14px;

  &.is-parent {
    font-weight: 500;
  }
}

.task-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-progress {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.gantt-chart {
  flex: 1;
  overflow-x: auto;
}

.timeline-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
}

.timeline-cell {
  flex-shrink: 0;
  padding: 8px 4px;
  text-align: center;
  font-size: 12px;
  color: #666;
  border-right: 1px solid #eee;
}

.gantt-body {
  position: relative;
}

.gantt-row {
  position: relative;
  height: 40px;
  border-bottom: 1px solid #eee;
}

.gantt-bar {
  position: absolute;
  top: 4px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;

  &:hover {
    opacity: 0.8;
  }
}

.bar-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
}

.bar-label {
  position: relative;
  z-index: 1;
  padding: 0 8px;
  font-size: 12px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
