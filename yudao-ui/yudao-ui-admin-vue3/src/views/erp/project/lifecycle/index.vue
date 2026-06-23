<template>
  <div class="project-lifecycle-page">
    <!-- 页头卡片 -->
    <ContentWrap class="project-lifecycle-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">项目生命周期追踪</div>
          <div class="page-header__desc">查看项目从合同签订到项目关闭的全流程状态</div>
        </div>
        <div class="page-header__actions">
          <el-select v-model="selectedProjectId" filterable placeholder="请选择项目" @change="handleProjectChange" class="project-select">
            <el-option v-for="item in projectList" :key="item.id" :label="`${item.no} - ${item.name}`" :value="item.id" />
          </el-select>
          <el-button :loading="loading" @click="loadData" :disabled="!selectedProjectId">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 项目信息卡片 -->
    <ContentWrap v-if="currentProject" class="project-lifecycle-page__info">
      <div class="project-info">
        <div class="project-info__main">
          <div class="project-info__no">{{ currentProject.no }}</div>
          <div class="project-info__name">{{ currentProject.name }}</div>
          <div class="project-info__meta">
            <span>客户：{{ currentProject.customerName || '-' }}</span>
            <span>销售员：{{ currentProject.saleUserName || '-' }}</span>
          </div>
        </div>
        <div class="project-info__stage">
          <el-tag :type="lifecycleStageType(currentProject.lifecycleStage)" size="large" effect="light">
            {{ formatLifecycleStage(currentProject.lifecycleStage) }}
          </el-tag>
        </div>
      </div>
    </ContentWrap>

    <!-- 生命周期阶段流转图 -->
    <ContentWrap class="project-lifecycle-page__flow">
      <div class="section-title">生命周期阶段</div>
      <div class="lifecycle-flow">
        <div
v-for="stage in lifecycleStages" :key="stage.code"
             :class="['flow-node', { 'flow-node--active': stage.code === currentProject?.lifecycleStage, 'flow-node--passed': isStagePassed(stage.code) }]">
          <div class="flow-node__icon">
            <Icon :icon="stage.icon" />
          </div>
          <div class="flow-node__label">{{ stage.name }}</div>
          <div class="flow-node__connector" v-if="stage.code !== 'CLOSED'">
            <Icon icon="ep:arrow-right" />
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 时间线记录 -->
    <ContentWrap class="project-lifecycle-page__timeline">
      <div class="section-title">操作记录</div>
      <el-timeline v-if="timeline.length">
        <el-timeline-item
v-for="item in timeline" :key="item.id"
                          :type="timelineItemType(item.stageCode)"
                          :timestamp="formatDateTime(item.happenTime)"
                          placement="top"
                          :hollow="isCurrentStage(item.stageCode)">
          <div class="timeline-content" @click="openTimelineDetail(item)">
            <div class="timeline-content__header">
              <div class="timeline-content__title">{{ item.stageName }}</div>
              <el-tag v-if="isCurrentStage(item.stageCode)" type="primary" size="small" effect="light">当前</el-tag>
            </div>
            <div class="timeline-content__meta">
              <span v-if="item.operatorName">
                <Icon icon="ep:user" class="mr-4px" />{{ item.operatorName }}
              </span>
              <span v-if="item.remark" class="timeline-content__remark-preview">
                <Icon icon="ep:document" class="mr-4px" />{{ item.remark.substring(0, 50) }}{{ item.remark.length > 50 ? '...' : '' }}
              </span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无操作记录" />
    </ContentWrap>

    <!-- 时间线详情弹窗 -->
    <el-dialog v-model="timelineDetailVisible" title="操作详情" width="500px">
      <div v-if="selectedTimeline" class="timeline-detail">
        <div class="timeline-detail__header">
          <el-tag :type="timelineItemType(selectedTimeline.stageCode)" size="large">
            {{ selectedTimeline.stageName }}
          </el-tag>
          <div class="timeline-detail__time">{{ formatDateTime(selectedTimeline.happenTime) }}</div>
        </div>
        <el-divider />
        <div class="timeline-detail__content">
          <div class="detail-item" v-if="selectedTimeline.operatorName">
            <div class="detail-item__label">操作人</div>
            <div class="detail-item__value">{{ selectedTimeline.operatorName }}</div>
          </div>
          <div class="detail-item" v-if="selectedTimeline.remark">
            <div class="detail-item__label">备注</div>
            <div class="detail-item__value detail-item__value--remark">{{ selectedTimeline.remark }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="timelineDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'
import * as UserApi from '@/api/system/user'
import { ProjectLifecycleApi, type ProjectLifecycleTimelineVO } from '@/api/erp/project/lifecycle'
import { ProjectApi } from '@/api/erp/project'

defineOptions({ name: 'ErpProjectLifecyclePage' })

const loading = ref(false)
const selectedProjectId = ref<number | undefined>(undefined)
const projectList = ref<any[]>([])
const currentProject = ref<any>(null)
const timeline = ref<ProjectLifecycleTimelineVO[]>([])
const timelineDetailVisible = ref(false)
const selectedTimeline = ref<ProjectLifecycleTimelineVO | null>(null)

const lifecycleStages = [
  { code: 'CONTRACT', name: '合同', icon: 'ep:document', description: '合同签订与生效' },
  { code: 'ORDER', name: '订单', icon: 'ep:order', description: '销售订单创建' },
  { code: 'PAYMENT', name: '收款', icon: 'ep:money', description: '满足收款条件' },
  { code: 'SHIPMENT', name: '发货', icon: 'ep:truck', description: '发货放行通过' },
  { code: 'OUTBOUND', name: '出库', icon: 'ep:box', description: '销售出库完成' },
  { code: 'INVOICE', name: '开票', icon: 'ep:receipt', description: '销项发票开具' },
  { code: 'CLOSED', name: '关闭', icon: 'ep:circle-check', description: '项目完结' }
]

const loadData = async () => {
  if (!selectedProjectId.value) return
  
  loading.value = true
  try {
    const [projectRes, timelineRes] = await Promise.all([
      ProjectApi.getProject(selectedProjectId.value),
      ProjectLifecycleApi.getTimeline(selectedProjectId.value)
    ])
    currentProject.value = projectRes
    timeline.value = timelineRes || []
  } finally {
    loading.value = false
  }
}

const handleProjectChange = async () => {
  await loadData()
}

const isStagePassed = (stageCode: string) => {
  if (!currentProject.value?.lifecycleStage) return false
  const currentStageIndex = lifecycleStages.findIndex(s => s.code === currentProject.value.lifecycleStage)
  const stageIndex = lifecycleStages.findIndex(s => s.code === stageCode)
  return stageIndex < currentStageIndex
}

const isCurrentStage = (stageCode: string) => {
  return currentProject.value?.lifecycleStage === stageCode
}

const openTimelineDetail = (item: ProjectLifecycleTimelineVO) => {
  selectedTimeline.value = item
  timelineDetailVisible.value = true
}

const formatLifecycleStage = (stage?: string) => {
  const map: Record<string, string> = {
    CONTRACT: '合同', ORDER: '订单', PAYMENT: '收款', SHIPMENT: '发货',
    OUTBOUND: '出库', INVOICE: '开票', CLOSED: '关闭',
    CONTRACT_REJECTED: '合同驳回', BLOCKED: '阻塞', RETURNED: '退货', DISPUTED: '争议'
  }
  return map[stage || ''] || stage || '--'
}

const lifecycleStageType = (stage?: string) => {
  if (stage === 'CLOSED') return 'success'
  if (stage === 'BLOCKED' || stage === 'CONTRACT_REJECTED' || stage === 'DISPUTED') return 'danger'
  if (stage === 'RETURNED') return 'warning'
  return 'primary'
}

const timelineItemType = (stageCode?: string) => {
  if (stageCode === 'CLOSED') return 'success'
  if (stageCode === 'BLOCKED' || stageCode === 'CONTRACT_REJECTED' || stageCode === 'DISPUTED') return 'danger'
  if (stageCode === 'RETURNED') return 'warning'
  return 'primary'
}

const formatDateTime = (value?: string | Date) => {
  if (!value) return '--'
  return formatDate(value, 'YYYY-MM-DD HH:mm:ss')
}

onMounted(async () => {
  projectList.value = await ProjectApi.getProjectSimpleList()
})
</script>

<style scoped lang="scss">
.project-lifecycle-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }

  .page-header__title {
    font-size: 20px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .page-header__desc {
    margin-top: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .page-header__actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .project-select {
    width: 300px;
  }

  .section-title {
    font-weight: 700;
    color: var(--erp-slate-900);
    margin-bottom: 16px;
  }

  .project-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .project-info__no {
    font-size: 18px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .project-info__name {
    margin-top: 4px;
    color: var(--erp-slate-700);
  }

  .project-info__meta {
    margin-top: 8px;
    display: flex;
    gap: 16px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .lifecycle-flow {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
  }

  .flow-node {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border: 2px solid var(--erp-slate-200);
    border-radius: 12px;
    background: #fff;
    transition: all 0.3s ease;

    &--active {
      border-color: var(--erp-primary-500);
      background: var(--erp-primary-50);
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
    }

    &--passed {
      border-color: var(--erp-success-500);
      background: var(--erp-success-50);
    }
  }

  .flow-node__icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: var(--erp-slate-100);
    color: var(--erp-slate-500);
  }

  .flow-node--active .flow-node__icon {
    background: var(--erp-primary-500);
    color: #fff;
  }

  .flow-node--passed .flow-node__icon {
    background: var(--erp-success-500);
    color: #fff;
  }

  .flow-node__label {
    font-weight: 600;
    color: var(--erp-slate-700);
  }

  .flow-node__connector {
    color: var(--erp-slate-300);
    margin-left: 8px;
  }

  .timeline-content__title {
    font-weight: 600;
    color: var(--erp-slate-900);
  }

  .timeline-content {
    cursor: pointer;
    padding: 8px;
    border-radius: 8px;
    transition: background 0.2s ease;

    &:hover {
      background: var(--erp-slate-50);
    }
  }

  .timeline-content__header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .timeline-content__title {
    font-weight: 600;
    color: var(--erp-slate-900);
  }

  .timeline-content__meta {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .timeline-content__remark-preview {
    display: flex;
    align-items: center;
  }

  .timeline-content__remark {
    margin-top: 8px;
    padding: 8px 12px;
    background: var(--erp-slate-50);
    border-radius: 8px;
    color: var(--erp-slate-600);
    font-size: 13px;
  }

  .timeline-detail {
    .timeline-detail__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .timeline-detail__time {
      color: var(--erp-slate-500);
      font-size: 14px;
    }

    .timeline-detail__content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .detail-item__label {
      color: var(--erp-slate-500);
      font-size: 12px;
    }

    .detail-item__value {
      font-weight: 600;
      color: var(--erp-slate-900);
    }

    .detail-item__value--remark {
      padding: 12px;
      background: var(--erp-slate-50);
      border-radius: 8px;
      font-weight: 400;
      white-space: pre-wrap;
    }
  }
}
</style>
