<template>
  <section class="oee-hero">
    <div class="oee-hero__header">
      <div>
        <div class="oee-hero__breadcrumb">制造执行管理 / OEE 分析</div>
        <div class="oee-page__title">OEE 分析</div>
      </div>
    </div>
  </section>

  <ContentWrap class="oee-page__filter-card">
    <el-form label-position="top" class="oee-query">
      <div class="oee-query__grid">
        <el-form-item label="工作中心" prop="workCenterId">
          <el-select
            v-model="queryParams.workCenterId"
            clearable
            filterable
            placeholder="全部工作中心"
          >
            <el-option
              v-for="item in workCenterList"
              :key="item.id"
              :label="item.centerName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="统计区间" prop="range">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <div class="oee-query__footer">
        <div class="oee-query__actions">
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

  <div class="oee-kpi-grid">
    <div class="oee-kpi-card oee-kpi-card--blue">
      <div>
        <div class="oee-kpi-card__label">综合 OEE</div>
        <div class="oee-kpi-card__value">{{ avgOee }}%</div>
      </div>
    </div>
    <div class="oee-kpi-card oee-kpi-card--teal">
      <div>
        <div class="oee-kpi-card__label">可用率</div>
        <div class="oee-kpi-card__value">{{ avgAvailability }}%</div>
      </div>
    </div>
    <div class="oee-kpi-card oee-kpi-card--green">
      <div>
        <div class="oee-kpi-card__label">达成率</div>
        <div class="oee-kpi-card__value">{{ avgAchievement }}%</div>
      </div>
    </div>
    <div class="oee-kpi-card oee-kpi-card--amber">
      <div>
        <div class="oee-kpi-card__label">良品率</div>
        <div class="oee-kpi-card__value">{{ avgQuality }}%</div>
      </div>
    </div>
  </div>

  <ContentWrap class="oee-page__list-card">
    <el-table v-loading="loading" :data="list" :stripe="true">
      <template #empty>
        <div class="oee-empty">
          <div class="oee-empty__icon">
            <Icon icon="ep:data-analysis" />
          </div>
          <div class="oee-empty__title">暂无 OEE 数据</div>
        </div>
      </template>
      <el-table-column label="工作中心" min-width="140" prop="workCenterName" />
      <el-table-column label="统计日期" min-width="120" prop="statDate" />
      <el-table-column label="任务数" min-width="80" align="right" prop="taskCount" />
      <el-table-column label="计划/报工/合格" min-width="180" align="right">
        <template #default="{ row }">
          {{ formatCount(row.planQty) }} / {{ formatCount(row.reportedQty) }} /
          {{ formatCount(row.qualifiedQty) }}
        </template>
      </el-table-column>
      <el-table-column label="可用率" min-width="110" align="right">
        <template #default="{ row }">
          <span class="oee-rate" :class="rateClass(row.availabilityRate)"
            >{{ row.availabilityRate }}%</span
          >
        </template>
      </el-table-column>
      <el-table-column label="达成率" min-width="100" align="right">
        <template #default="{ row }">
          <span class="oee-rate" :class="rateClass(row.achievementRate)"
            >{{ row.achievementRate }}%</span
          >
        </template>
      </el-table-column>
      <el-table-column label="良品率" min-width="100" align="right">
        <template #default="{ row }">
          <span class="oee-rate" :class="rateClass(row.qualityRate)">{{ row.qualityRate }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="OEE" min-width="100" align="right">
        <template #default="{ row }">
          <strong class="oee-rate oee-rate--strong" :class="rateClass(row.oee)"
            >{{ row.oee }}%</strong
          >
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { erpCountInputFormatter } from '@/utils'
import { WorkCenterApi, WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { OeeApi, OeeSummaryVO } from '@/api/mes/oee'

defineOptions({ name: 'MesOee' })

const loading = ref(false)
const list = ref<OeeSummaryVO[]>([])
const workCenterList = ref<WorkCenterSimpleVO[]>([])
const dateRange = ref<string[]>([])

const queryParams = reactive({
  workCenterId: undefined as number | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

const avgRate = (key: 'availabilityRate' | 'achievementRate' | 'qualityRate' | 'oee') => {
  if (!list.value.length) return '0.00'
  const sum = list.value.reduce((acc, row) => acc + Number(row[key] || 0), 0)
  return (sum / list.value.length).toFixed(2)
}

const avgOee = computed(() => avgRate('oee'))
const avgAvailability = computed(() => avgRate('availabilityRate'))
const avgAchievement = computed(() => avgRate('achievementRate'))
const avgQuality = computed(() => avgRate('qualityRate'))

const rateClass = (rate: number) => {
  if (Number(rate) >= 90) return 'oee-rate--good'
  if (Number(rate) >= 70) return 'oee-rate--mid'
  return 'oee-rate--bad'
}

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const getList = async () => {
  loading.value = true
  try {
    queryParams.startDate = dateRange.value?.[0]
    queryParams.endDate = dateRange.value?.[1]
    list.value = (await OeeApi.getOeeSummary(queryParams)) || []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  getList()
}

const resetQuery = () => {
  queryParams.workCenterId = undefined
  dateRange.value = []
  getList()
}

onMounted(async () => {
  getList()
  try {
    workCenterList.value = await WorkCenterApi.getWorkCenterSimpleList()
  } catch {
    workCenterList.value = []
  }
})
</script>

<style scoped>
.oee-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.oee-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.oee-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.oee-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.oee-page__filter-card,
.oee-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.oee-query {
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

.oee-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.oee-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.oee-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.oee-kpi-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 100px;
  padding: 18px 20px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 16px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.oee-kpi-card--blue {
  background: var(--erp-stat-gradient-blue);
}

.oee-kpi-card--teal {
  background: var(--erp-stat-gradient-teal);
}

.oee-kpi-card--green {
  background: var(--erp-stat-gradient-green);
}

.oee-kpi-card--amber {
  background: var(--erp-stat-gradient-amber);
}

.oee-kpi-card__label {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 8px;
}

.oee-kpi-card__value {
  color: var(--erp-slate-900);
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.oee-rate {
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.oee-rate--strong {
  font-size: 14px;
}

.oee-rate--good {
  color: var(--erp-success-600);
}

.oee-rate--mid {
  color: var(--erp-warning-600);
}

.oee-rate--bad {
  color: var(--erp-danger-600);
}

.oee-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.oee-empty__icon {
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

.oee-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .oee-query__grid,
  .oee-kpi-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
