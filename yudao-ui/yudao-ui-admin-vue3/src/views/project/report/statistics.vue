<template>
  <div class="project-statistics">
    <ContentWrap>
      <template #header>
        <span>项目统计</span>
      </template>

      <!-- KPI 卡片区 -->
      <div class="kpi-cards">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-value">{{ overview.totalTasks }}</div>
          <div class="kpi-label">总任务数</div>
        </el-card>
        <el-card shadow="hover" class="kpi-card success">
          <div class="kpi-value">{{ overview.completedTasks }}</div>
          <div class="kpi-label">已完成</div>
        </el-card>
        <el-card shadow="hover" class="kpi-card primary">
          <div class="kpi-value">{{ overview.inProgressTasks }}</div>
          <div class="kpi-label">进行中</div>
        </el-card>
        <el-card shadow="hover" class="kpi-card warning">
          <div class="kpi-value">{{ overview.overdueTasks }}</div>
          <div class="kpi-label">逾期</div>
        </el-card>
        <el-card shadow="hover" class="kpi-card info">
          <div class="kpi-value">{{ overview.completionRate }}%</div>
          <div class="kpi-label">完成率</div>
        </el-card>
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-value">{{ overview.totalMembers }}</div>
          <div class="kpi-label">成员数</div>
        </el-card>
      </div>

      <!-- 图表区 -->
      <div class="charts-container">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>任务状态分布</span>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>完成率</span>
          </template>
          <div ref="progressChartRef" class="chart-container"></div>
        </el-card>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ProjectStatisticsApi, type StatisticsOverviewVO } from '@/api/project/statistics'
import * as echarts from 'echarts'

defineOptions({ name: 'ProjectStatistics' })

const props = defineProps<{
  projectId: number
}>()

const overview = reactive<StatisticsOverviewVO>({
  totalTasks: 0,
  completedTasks: 0,
  inProgressTasks: 0,
  overdueTasks: 0,
  completionRate: 0,
  totalMembers: 0
})

const statusChartRef = ref<HTMLElement | null>(null)
const progressChartRef = ref<HTMLElement | null>(null)
let statusChart: echarts.ECharts | null = null
let progressChart: echarts.ECharts | null = null

const fetchOverview = async () => {
  try {
    const data = await ProjectStatisticsApi.getOverview(props.projectId)
    Object.assign(overview, data)
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
}

const renderCharts = () => {
  // 任务状态分布饼图
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '任务状态',
          type: 'pie',
          radius: '50%',
          data: [
            { value: overview.completedTasks, name: '已完成', itemStyle: { color: '#67c23a' } },
            { value: overview.inProgressTasks, name: '进行中', itemStyle: { color: '#409eff' } },
            { value: overview.overdueTasks, name: '逾期', itemStyle: { color: '#f56c6c' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }

  // 完成率仪表盘
  if (progressChartRef.value) {
    progressChart = echarts.init(progressChartRef.value)
    progressChart.setOption({
      series: [
        {
          name: '完成率',
          type: 'gauge',
          progress: {
            show: true,
            width: 18
          },
          axisLine: {
            lineStyle: {
              width: 18
            }
          },
          axisTick: {
            show: false
          },
          splitLine: {
            length: 15,
            lineStyle: {
              width: 2,
              color: '#999'
            }
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '12%',
            width: 20,
            offsetCenter: [0, '-60%'],
            itemStyle: {
              color: 'auto'
            }
          },
          axisLabel: {
            color: '#464646',
            fontSize: 14,
            distance: -60,
            rotate: 'tangential',
            formatter: function (value: number) {
              if (value === 100) {
                return '100%'
              }
              return ''
            }
          },
          title: {
            offsetCenter: [0, '-20%'],
            fontSize: 16
          },
          detail: {
            fontSize: 32,
            offsetCenter: [0, '0%'],
            valueAnimation: true,
            formatter: function (value: number) {
              return Math.round(value) + '%'
            },
            color: 'auto'
          },
          data: [
            {
              value: overview.completionRate,
              name: '完成率'
            }
          ]
        }
      ]
    })
  }
}

const handleResize = () => {
  statusChart?.resize()
  progressChart?.resize()
}

onMounted(() => {
  fetchOverview()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  statusChart?.dispose()
  progressChart?.dispose()
})
</script>

<style scoped lang="scss">
.project-statistics {
  height: 100%;
}

.kpi-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  text-align: center;
  padding: 16px;

  .kpi-value {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .kpi-label {
    font-size: 14px;
    color: #909399;
  }

  &.success .kpi-value {
    color: #67c23a;
  }

  &.primary .kpi-value {
    color: #409eff;
  }

  &.warning .kpi-value {
    color: #e6a23c;
  }

  &.info .kpi-value {
    color: #909399;
  }
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.chart-card {
  .chart-container {
    height: 300px;
  }
}
</style>
