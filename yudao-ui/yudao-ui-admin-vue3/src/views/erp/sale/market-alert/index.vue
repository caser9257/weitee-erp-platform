<template>
  <div class="market-alert-page">
    <!-- 页头卡片 -->
    <ContentWrap class="market-alert-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">市场预警与统计</div>
          <div class="page-header__desc">监控订单执行异常，及时预警处理</div>
        </div>
        <div class="page-header__actions">
          <el-button type="primary" :loading="checking" @click="handleCheck">
            <Icon icon="ep:warning" class="mr-5px" /> 检查预警
          </el-button>
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 预警统计卡片 -->
    <ContentWrap class="market-alert-page__stats">
      <div class="kpi-grid">
        <div class="kpi-card kpi-card--danger">
          <div class="kpi-card__icon">
            <Icon icon="ep:warning" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">待处理预警</div>
            <div class="kpi-card__value">{{ alerts.length }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--warning">
          <div class="kpi-card__icon">
            <Icon icon="ep:clock" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">收款逾期</div>
            <div class="kpi-card__value">{{ receiptOverdueCount }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--danger">
          <div class="kpi-card__icon">
            <Icon icon="ep:truck" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">交期逾期</div>
            <div class="kpi-card__value">{{ deliveryOverdueCount }}</div>
          </div>
        </div>
        <div class="kpi-card kpi-card--info">
          <div class="kpi-card__icon">
            <Icon icon="ep:lock" />
          </div>
          <div class="kpi-card__content">
            <div class="kpi-card__label">放行阻塞</div>
            <div class="kpi-card__value">{{ releaseBlockedCount }}</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 预警规则配置 -->
    <ContentWrap class="market-alert-page__rules">
      <div class="section-header">
        <div class="section-header__title">预警规则</div>
        <el-button link type="primary" @click="showRuleDialog = true">
          <Icon icon="ep:setting" class="mr-5px" /> 配置规则
        </el-button>
      </div>
      <div class="rules-grid">
        <div v-for="rule in rules" :key="rule.code" class="rule-card" :class="{ 'rule-card--disabled': !rule.enabled }">
          <div class="rule-card__header">
            <div class="rule-card__name">{{ rule.name }}</div>
            <el-switch v-model="rule.enabled" @change="handleRuleChange(rule)" />
          </div>
          <div class="rule-card__desc">{{ rule.description }}</div>
          <div class="rule-card__meta">
            <el-tag :type="levelTagType(rule.level)" size="small" effect="light">{{ rule.level }}</el-tag>
            <span class="rule-card__threshold">阈值：{{ rule.thresholdDays }}天</span>
          </div>
        </div>
      </div>
    </ContentWrap>

    <!-- 预警列表 -->
    <ContentWrap class="market-alert-page__list">
      <div class="section-header">
        <div class="section-header__title">预警列表</div>
        <div class="section-header__meta">共 {{ alerts.length }} 条</div>
      </div>
      <el-table :data="alerts" v-loading="loading" stripe class="alert-table">
        <el-table-column label="预警信息" min-width="300">
          <template #default="{ row }">
            <div class="alert-cell">
              <div class="alert-cell__header">
                <el-tag :type="levelTagType(row.level)" size="small" effect="light">{{ row.ruleName }}</el-tag>
                <span class="alert-cell__order">{{ row.orderNo }}</span>
              </div>
              <div class="alert-cell__content">{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="项目" min-width="150">
          <template #default="{ row }">
            <span>{{ row.projectNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="触发时间" min-width="150">
          <template #default="{ row }">
            <span>{{ formatDate(row.triggerTime, 'YYYY-MM-DD HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.handled ? 'success' : 'warning'" size="small" effect="light">
              {{ row.handled ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAlert(row)">处理</el-button>
            <el-button link type="info" @click="handleViewOrder(row)">查看订单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>

    <!-- 规则配置弹窗 -->
    <el-dialog v-model="showRuleDialog" title="预警规则配置" width="600px">
      <div class="rule-config">
        <div v-for="rule in rules" :key="rule.code" class="rule-config__item">
          <div class="rule-config__header">
            <el-switch v-model="rule.enabled" />
            <div class="rule-config__name">{{ rule.name }}</div>
          </div>
          <div class="rule-config__desc">{{ rule.description }}</div>
          <div class="rule-config__setting">
            <span>阈值（天数）：</span>
            <el-input-number v-model="rule.thresholdDays" :min="0" :max="365" size="small" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showRuleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRules">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'ErpMarketAlertPage' })

const router = useRouter()

// API 接口
const MarketAlertApi = {
  getAlertRules: async () => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/market-alert/rules' })
  },
  getCurrentAlerts: async () => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: '/erp/market-alert/list' })
  },
  checkAndTriggerAlerts: async () => {
    const { request } = await import('@/config/axios')
    return await request.post({ url: '/erp/market-alert/check' })
  },
  updateAlertRule: async (rule: any) => {
    const { request } = await import('@/config/axios')
    return await request.put({ url: '/erp/market-alert/rules', data: rule })
  },
  handleAlert: async (id: number, remark?: string) => {
    const { request } = await import('@/config/axios')
    return await request.put({ url: '/erp/market-alert/handle', params: { id, remark } })
  }
}

const loading = ref(false)
const checking = ref(false)
const showRuleDialog = ref(false)
const rules = ref<any[]>([])
const alerts = ref<any[]>([])

const receiptOverdueCount = computed(() => alerts.value.filter(a => a.ruleCode === 'RECEIPT_OVERDUE').length)
const deliveryOverdueCount = computed(() => alerts.value.filter(a => a.ruleCode === 'DELIVERY_OVERDUE').length)
const releaseBlockedCount = computed(() => alerts.value.filter(a => a.ruleCode === 'RELEASE_BLOCKED').length)

const loadData = async () => {
  loading.value = true
  try {
    const [rulesRes, alertsRes] = await Promise.all([
      MarketAlertApi.getAlertRules(),
      MarketAlertApi.getCurrentAlerts()
    ])
    rules.value = rulesRes || []
    alerts.value = alertsRes || []
  } finally {
    loading.value = false
  }
}

const handleCheck = async () => {
  checking.value = true
  try {
    const count = await MarketAlertApi.checkAndTriggerAlerts()
    ElMessage.success(`检查完成，发现 ${count} 条预警`)
    await loadData()
  } finally {
    checking.value = false
  }
}

const handleRuleChange = async (rule: any) => {
  await MarketAlertApi.updateAlertRule(rule)
  ElMessage.success('规则已更新')
}

const saveRules = async () => {
  for (const rule of rules.value) {
    await MarketAlertApi.updateAlertRule(rule)
  }
  showRuleDialog.value = false
  ElMessage.success('规则已保存')
}

const handleAlert = async (alert: any) => {
  try {
    await ElMessageBox.confirm('确认已处理该预警？', '处理预警', { type: 'warning' })
    await MarketAlertApi.handleAlert(alert.id, '已处理')
    ElMessage.success('预警已处理')
    await loadData()
  } catch {
    // 用户取消
  }
}

const handleViewOrder = (alert: any) => {
  if (alert.orderId) {
    router.push({ path: '/erp/sale/order', query: { id: alert.orderId } })
  } else {
    ElMessage.warning('无关联订单')
  }
}

const levelTagType = (level?: string) => {
  if (level === 'DANGER') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.market-alert-page {
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
  }

  .kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
  }

  .kpi-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid var(--erp-slate-200);
  }

  .kpi-card__icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    font-size: 24px;
  }

  .kpi-card--danger .kpi-card__icon {
    background: var(--erp-amount-bg);
    color: var(--erp-danger-500);
  }

  .kpi-card--warning .kpi-card__icon {
    background: var(--erp-warning-50);
    color: var(--erp-warning-500);
  }

  .kpi-card--info .kpi-card__icon {
    background: var(--erp-primary-50);
    color: var(--erp-primary-500);
  }

  .kpi-card__label {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .kpi-card__value {
    margin-top: 4px;
    font-size: 28px;
    font-weight: 800;
    color: var(--erp-slate-900);
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .section-header__title {
    font-size: 16px;
    font-weight: 700;
    color: var(--erp-slate-900);
  }

  .section-header__meta {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .rules-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .rule-card {
    padding: 16px;
    background: #fff;
    border: 1px solid var(--erp-slate-200);
    border-radius: 12px;
    transition: all 0.2s ease;

    &--disabled {
      opacity: 0.6;
    }
  }

  .rule-card__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .rule-card__name {
    font-weight: 600;
    color: var(--erp-slate-900);
  }

  .rule-card__desc {
    margin-top: 8px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .rule-card__meta {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .rule-card__threshold {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .alert-table {
    :deep(.el-table__header-wrapper th) {
      background: var(--erp-slate-50);
      color: var(--erp-slate-600);
    }
  }

  .alert-cell__header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .alert-cell__order {
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .alert-cell__content {
    margin-top: 8px;
    color: var(--erp-slate-700);
    font-size: 13px;
  }

  .rule-config {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .rule-config__item {
    padding: 16px;
    background: var(--erp-slate-50);
    border-radius: 8px;
  }

  .rule-config__header {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .rule-config__name {
    font-weight: 600;
    color: var(--erp-slate-900);
  }

  .rule-config__desc {
    margin-top: 8px;
    color: var(--erp-slate-500);
    font-size: 12px;
  }

  .rule-config__setting {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--erp-slate-600);
    font-size: 13px;
  }
}
</style>
