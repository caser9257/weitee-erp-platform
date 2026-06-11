<template>
  <div class="approval-portal">
    <!-- 统计卡片 -->
    <div class="statistics-cards mb-4">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card__content">
              <div class="stat-card__value">{{ statistics.pendingCount }}</div>
              <div class="stat-card__label">待我审批</div>
            </div>
            <div class="stat-card__icon pending">
              <Icon icon="ep:clock" :size="24" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card__content">
              <div class="stat-card__value">{{ statistics.approvedCount }}</div>
              <div class="stat-card__label">我已审批</div>
            </div>
            <div class="stat-card__icon approved">
              <Icon icon="ep:circle-check" :size="24" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card__content">
              <div class="stat-card__value">{{ statistics.submittedCount }}</div>
              <div class="stat-card__label">我发起的</div>
            </div>
            <div class="stat-card__icon submitted">
              <Icon icon="ep:upload" :size="24" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card__content">
              <div class="stat-card__value">{{ statistics.ccCount }}</div>
              <div class="stat-card__label">抄送我的</div>
            </div>
            <div class="stat-card__icon cc">
              <Icon icon="ep:message" :size="24" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主体内容 -->
    <el-result
      v-if="bpmModuleUnavailable"
      icon="warning"
      title="审批中心暂不可用"
      :sub-title="bpmModuleUnavailableMessage"
      class="approval-portal__result"
    />
    <el-tabs v-else v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待我审批" name="pending" lazy>
        <PendingList ref="pendingListRef" @module-unavailable="handleModuleUnavailable" />
      </el-tab-pane>
      <el-tab-pane label="我已审批" name="approved" lazy>
        <ApprovedList ref="approvedListRef" @module-unavailable="handleModuleUnavailable" />
      </el-tab-pane>
      <el-tab-pane label="我发起的" name="submitted" lazy>
        <SubmittedList ref="submittedListRef" @module-unavailable="handleModuleUnavailable" />
      </el-tab-pane>
      <el-tab-pane label="抄送我的" name="cc" lazy>
        <CcList ref="ccListRef" @module-unavailable="handleModuleUnavailable" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BPM_MODULE_UNAVAILABLE_TEXT } from './moduleGuard'
import PendingList from './components/PendingList.vue'
import ApprovedList from './components/ApprovedList.vue'
import SubmittedList from './components/SubmittedList.vue'
import CcList from './components/CcList.vue'

defineOptions({ name: 'BpmApprovalPortal' })

const route = useRoute()
const router = useRouter()

// Tab name → 路由路径映射
const tabRouteMap: Record<string, string> = {
  pending: '/approval/todo',
  submitted: '/approval/submitted',
  cc: '/approval/cc'
}

// 根据路由路径设置默认 Tab
const getDefaultTab = () => {
  const path = route.path
  if (path.includes('/submitted')) return 'submitted'
  if (path.includes('/cc')) return 'cc'
  if (path.includes('/approved')) return 'approved'
  return 'pending'
}

const activeTab = ref(getDefaultTab())
const bpmModuleUnavailable = ref(false)
const bpmModuleUnavailableMessage = ref(BPM_MODULE_UNAVAILABLE_TEXT)

const statistics = reactive({
  pendingCount: 0,
  approvedCount: 0,
  submittedCount: 0,
  ccCount: 0
})

const pendingListRef = ref()
const approvedListRef = ref()
const submittedListRef = ref()
const ccListRef = ref()

const handleTabChange = (tab: string) => {
  if (bpmModuleUnavailable.value) {
    return
  }
  // 切换 Tab 时同步路由，让侧边栏菜单高亮跟随
  const targetPath = tabRouteMap[tab]
  if (targetPath && route.path !== targetPath) {
    router.push(targetPath)
    return // 路由跳转后会重新加载组件，无需手动刷新
  }
  // approved 没有独立路由，直接刷新列表
  switch (tab) {
    case 'pending':
      pendingListRef.value?.refresh()
      break
    case 'approved':
      approvedListRef.value?.refresh()
      break
    case 'submitted':
      submittedListRef.value?.refresh()
      break
    case 'cc':
      ccListRef.value?.refresh()
      break
  }
}

const handleModuleUnavailable = (message?: string) => {
  if (bpmModuleUnavailable.value) {
    return
  }
  bpmModuleUnavailable.value = true
  bpmModuleUnavailableMessage.value = message || BPM_MODULE_UNAVAILABLE_TEXT
  Object.assign(statistics, {
    pendingCount: 0,
    approvedCount: 0,
    submittedCount: 0,
    ccCount: 0
  })
}

// 加载统计数据
const loadStatistics = async () => {
  // TODO: 调用后端接口获取统计数据
  // const data = await getApprovalStatistics()
  // Object.assign(statistics, data)
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.approval-portal {
  padding: 16px;
}

.approval-portal__result {
  border-radius: 12px;
  background: #fff;
}

.statistics-cards {
  margin-bottom: 16px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-card__content {
  flex: 1;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-card__label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.stat-card__icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.stat-card__icon.pending {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.stat-card__icon.approved {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.stat-card__icon.submitted {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.stat-card__icon.cc {
  background: var(--el-color-info-light-9);
  color: var(--el-color-info);
}
</style>
