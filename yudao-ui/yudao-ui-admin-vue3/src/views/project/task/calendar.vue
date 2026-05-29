<template>
  <div class="task-calendar">
    <ContentWrap>
      <template #header>
        <div class="calendar-header">
          <div class="calendar-nav">
            <el-button @click="handlePrev">
              <Icon icon="ep:arrow-left" />
            </el-button>
            <el-button @click="handleToday">今天</el-button>
            <el-button @click="handleNext">
              <Icon icon="ep:arrow-right" />
            </el-button>
            <span class="current-date">{{ currentDateLabel }}</span>
          </div>
          <el-radio-group v-model="viewType" @change="handleViewChange">
            <el-radio-button value="month">月</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 日历网格 -->
      <div class="calendar-grid">
        <!-- 星期头部 -->
        <div class="weekday-header">
          <div v-for="day in weekDays" :key="day" class="weekday-cell">{{ day }}</div>
        </div>

        <!-- 日期格子 -->
        <div class="date-grid">
          <div
            v-for="(cell, index) in calendarCells"
            :key="index"
            class="date-cell"
            :class="{
              'is-today': cell.isToday,
              'is-other-month': !cell.isCurrentMonth,
              'is-weekend': cell.isWeekend
            }"
          >
            <div class="date-number">{{ cell.day }}</div>
            <div class="date-tasks">
              <div
                v-for="task in cell.tasks.slice(0, 3)"
                :key="task.id"
                class="task-item"
                :style="{ backgroundColor: task.priorityColor || '#409eff' }"
                @click="handleTaskClick(task)"
              >
                <span class="task-name">{{ task.name }}</span>
              </div>
              <div v-if="cell.tasks.length > 3" class="more-tasks">
                +{{ cell.tasks.length - 3 }} 更多
              </div>
            </div>
          </div>
        </div>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TaskCalendarApi, type CalendarTaskVO } from '@/api/project/task-calendar'
import dayjs from 'dayjs'

defineOptions({ name: 'TaskCalendar' })

const props = defineProps<{
  projectId: number
}>()

const emit = defineEmits(['taskClick'])

const currentDate = ref(dayjs())
const viewType = ref<'month' | 'week'>('month')
const taskList = ref<CalendarTaskVO[]>([])

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const currentDateLabel = computed(() => {
  if (viewType.value === 'month') {
    return currentDate.value.format('YYYY年MM月')
  }
  const start = currentDate.value.startOf('week')
  const end = currentDate.value.endOf('week')
  return `${start.format('MM月DD日')} - ${end.format('MM月DD日')}`
})

interface CalendarCell {
  date: dayjs.Dayjs
  day: number
  isToday: boolean
  isCurrentMonth: boolean
  isWeekend: boolean
  tasks: CalendarTaskVO[]
}

const calendarCells = computed<CalendarCell[]>(() => {
  const cells: CalendarCell[] = []
  const today = dayjs()

  if (viewType.value === 'month') {
    // 月视图：显示整个日历网格
    const startOfMonth = currentDate.value.startOf('month')
    const endOfMonth = currentDate.value.endOf('month')
    const startDate = startOfMonth.startOf('week')
    const endDate = endOfMonth.endOf('week')

    let current = startDate
    while (current.isBefore(endDate) || current.isSame(endDate, 'day')) {
      cells.push({
        date: current,
        day: current.date(),
        isToday: current.isSame(today, 'day'),
        isCurrentMonth: current.month() === currentDate.value.month(),
        isWeekend: current.day() === 0 || current.day() === 6,
        tasks: getTasksForDate(current)
      })
      current = current.add(1, 'day')
    }
  } else {
    // 周视图
    const startOfWeek = currentDate.value.startOf('week')
    for (let i = 0; i < 7; i++) {
      const current = startOfWeek.add(i, 'day')
      cells.push({
        date: current,
        day: current.date(),
        isToday: current.isSame(today, 'day'),
        isCurrentMonth: true,
        isWeekend: i === 0 || i === 6,
        tasks: getTasksForDate(current)
      })
    }
  }

  return cells
})

const getTasksForDate = (date: dayjs.Dayjs): CalendarTaskVO[] => {
  return taskList.value.filter(task => {
    const start = task.startAt ? dayjs(task.startAt) : null
    const end = task.endAt ? dayjs(task.endAt) : null
    const target = date.startOf('day')

    if (start && end) {
      return target.isAfter(start.startOf('day').subtract(1, 'day')) &&
             target.isBefore(end.startOf('day').add(1, 'day'))
    }
    if (start) {
      return start.startOf('day').isSame(target)
    }
    return false
  })
}

const fetchTasks = async () => {
  try {
    let startTime: string
    let endTime: string

    if (viewType.value === 'month') {
      startTime = currentDate.value.startOf('month').startOf('week').format('YYYY-MM-DD HH:mm:ss')
      endTime = currentDate.value.endOf('month').endOf('week').format('YYYY-MM-DD HH:mm:ss')
    } else {
      startTime = currentDate.value.startOf('week').format('YYYY-MM-DD HH:mm:ss')
      endTime = currentDate.value.endOf('week').format('YYYY-MM-DD HH:mm:ss')
    }

    taskList.value = await TaskCalendarApi.getTaskListByDate({
      projectId: props.projectId,
      startTime,
      endTime
    })
  } catch (error) {
    ElMessage.error('获取任务列表失败')
  }
}

const handlePrev = () => {
  currentDate.value = currentDate.value.subtract(1, viewType.value === 'month' ? 'month' : 'week')
  fetchTasks()
}

const handleNext = () => {
  currentDate.value = currentDate.value.add(1, viewType.value === 'month' ? 'month' : 'week')
  fetchTasks()
}

const handleToday = () => {
  currentDate.value = dayjs()
  fetchTasks()
}

const handleViewChange = () => {
  fetchTasks()
}

const handleTaskClick = (task: CalendarTaskVO) => {
  emit('taskClick', task)
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped lang="scss">
.task-calendar {
  height: 100%;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-date {
  font-size: 16px;
  font-weight: 500;
  margin-left: 8px;
}

.calendar-grid {
  border: 1px solid #eee;
  border-radius: 4px;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
}

.weekday-cell {
  padding: 8px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.date-cell {
  min-height: 100px;
  padding: 4px;
  border-right: 1px solid #eee;
  border-bottom: 1px solid #eee;

  &:nth-child(7n) {
    border-right: none;
  }

  &.is-today {
    background: #ecf5ff;
  }

  &.is-other-month {
    opacity: 0.5;
  }

  &.is-weekend {
    background: #fafafa;
  }
}

.date-number {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.date-tasks {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-item {
  padding: 2px 4px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &:hover {
    opacity: 0.8;
  }
}

.task-name {
  font-size: 12px;
}

.more-tasks {
  font-size: 12px;
  color: #999;
  text-align: center;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }
}
</style>
