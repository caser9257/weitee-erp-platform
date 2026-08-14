<template>
  <section class="sq-hero">
    <div class="sq-hero__header">
      <div>
        <div class="sq-hero__breadcrumb">生产执行 / 工序质检</div>
        <div class="sq-page__title">工序质检</div>
      </div>
    </div>
  </section>

  <ContentWrap class="sq-page__filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="sq-query">
      <div class="sq-query__grid">
        <el-form-item label="质检单号" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入质检单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="质检状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择质检状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="sq-query__footer">
        <div class="sq-query__actions">
          <el-button @click="resetQuery" :disabled="listLoading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="listLoading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="sq-page__list-card">
    <el-table v-loading="listLoading" :data="list" :stripe="true">
      <template #empty>
        <div class="sq-empty">
          <div class="sq-empty__icon">
            <Icon icon="ep:circle-check" />
          </div>
          <div class="sq-empty__title">暂无工序质检单</div>
        </div>
      </template>
      <el-table-column label="质检单号" min-width="170">
        <template #default="{ row }">
          <div class="sq-ledger__no">{{ row.no }}</div>
        </template>
      </el-table-column>
      <el-table-column label="生产工单" min-width="180" prop="productionOrderNo" />
      <el-table-column label="工序" min-width="150">
        <template #default="{ row }">
          <div class="sq-ledger__step">
            <div class="sq-ledger__step-name">{{ row.stepNo }} · {{ row.stepName }}</div>
            <div class="sq-ledger__step-meta">{{ row.stepCode }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="报工数量" min-width="100" align="right">
        <template #default="{ row }">{{ formatCount(row.reportQty) }}</template>
      </el-table-column>
      <el-table-column label="合格 / 不合格" min-width="140" align="right">
        <template #default="{ row }">
          <div class="sq-ledger__qty">
            <strong>{{ formatCount(row.qualifiedQty) }}</strong>
            <span> / {{ formatCount(row.unqualifiedQty) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="质检状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="light" :type="resolveStatusTagType(row.status)">
            {{ resolveStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="质检时间" min-width="150">
        <template #default="{ row }">{{ formatDateValue(row.checkTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="150" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          <el-button
            v-if="Number(row.status) === STEP_QUALITY_STATUS.TO_INSPECT"
            link
            type="success"
            @click="openDetail(row, 'submit')"
          >
            处理质检
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="dialogTitle" width="560">
    <el-descriptions v-if="detailForm.no" :column="2" border class="mb-12px">
      <el-descriptions-item label="质检单号">{{ detailForm.no }}</el-descriptions-item>
      <el-descriptions-item label="生产工单">{{
        detailForm.productionOrderNo || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="工序"
        >{{ detailForm.stepNo }} · {{ detailForm.stepName }}</el-descriptions-item
      >
      <el-descriptions-item label="报工数量">{{
        formatCount(detailForm.reportQty)
      }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag size="small" effect="light" :type="resolveStatusTagType(detailForm.status)">
          {{ resolveStatusLabel(detailForm.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="质检时间">{{
        formatDateValue(detailForm.checkTime)
      }}</el-descriptions-item>
    </el-descriptions>
    <el-form v-if="dialogMode === 'submit'" label-width="110px">
      <el-form-item label="合格数量" required>
        <el-input-number
          v-model="submitForm.qualifiedQty"
          controls-position="right"
          :min="0"
          :max="Number(detailForm.reportQty || 0)"
          class="!w-100%"
          @change="syncCounts"
        />
      </el-form-item>
      <el-form-item label="不合格数量" required>
        <el-input-number
          v-model="submitForm.unqualifiedQty"
          controls-position="right"
          :min="0"
          :max="Number(detailForm.reportQty || 0)"
          class="!w-100%"
          @change="syncCounts"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="submitForm.remark" type="textarea" :rows="2" maxlength="255" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button
        v-if="dialogMode === 'submit'"
        type="success"
        :loading="submitLoading"
        @click="handleSubmit"
      >
        提交质检
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'
import {
  STEP_QUALITY_STATUS,
  StepQualityApi,
  StepQualityVO
} from '@/api/erp/manufacturing/step-quality'

defineOptions({ name: 'ErpStepQuality' })

const message = useMessage()

const queryFormRef = ref()
const listLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'view' | 'submit'>('view')

const list = ref<StepQualityVO[]>([])
const total = ref(0)
const detailForm = ref<Partial<StepQualityVO>>({})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined as string | undefined,
  status: undefined as number | undefined
})

const submitForm = reactive({
  qualifiedQty: 0,
  unqualifiedQty: 0,
  remark: undefined as string | undefined
})

const statusOptions = [
  { label: '待质检', value: STEP_QUALITY_STATUS.TO_INSPECT },
  { label: '部分合格', value: STEP_QUALITY_STATUS.PARTIAL },
  { label: '全部合格', value: STEP_QUALITY_STATUS.PASSED },
  { label: '全部不合格', value: STEP_QUALITY_STATUS.REJECTED }
]

const dialogTitle = computed(() =>
  dialogMode.value === 'submit' ? '处理工序质检' : '查看工序质检'
)

const formatCount = (value?: number) => erpCountInputFormatter(value || 0)

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-'

const resolveStatusLabel = (status?: number) => {
  if (status === STEP_QUALITY_STATUS.TO_INSPECT) return '待质检'
  if (status === STEP_QUALITY_STATUS.PARTIAL) return '部分合格'
  if (status === STEP_QUALITY_STATUS.PASSED) return '全部合格'
  if (status === STEP_QUALITY_STATUS.REJECTED) return '全部不合格'
  return '-'
}

const resolveStatusTagType = (status?: number) => {
  if (status === STEP_QUALITY_STATUS.TO_INSPECT) return 'warning'
  if (status === STEP_QUALITY_STATUS.PARTIAL) return 'primary'
  if (status === STEP_QUALITY_STATUS.PASSED) return 'success'
  if (status === STEP_QUALITY_STATUS.REJECTED) return 'danger'
  return 'info'
}

const getList = async () => {
  listLoading.value = true
  try {
    const data = await StepQualityApi.getStepQualityPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openDetail = async (row: StepQualityVO, mode: 'view' | 'submit' = 'view') => {
  dialogVisible.value = true
  dialogMode.value = mode
  submitForm.qualifiedQty = 0
  submitForm.unqualifiedQty = 0
  submitForm.remark = undefined
  try {
    detailForm.value = await StepQualityApi.getStepQuality(row.id)
    if (mode === 'submit') {
      const reportQty = Number(detailForm.value.reportQty || 0)
      submitForm.qualifiedQty = reportQty
      submitForm.unqualifiedQty = 0
    }
  } catch {
    message.error('质检单详情加载失败')
  }
}

const syncCounts = () => {
  const reportQty = Number(detailForm.value.reportQty || 0)
  const qualified = Math.min(Math.max(Number(submitForm.qualifiedQty || 0), 0), reportQty)
  submitForm.qualifiedQty = qualified
  submitForm.unqualifiedQty = Math.max(reportQty - qualified, 0)
}

const handleSubmit = async () => {
  if (!detailForm.value.id) return
  submitLoading.value = true
  try {
    await StepQualityApi.submitStepQuality({
      id: detailForm.value.id,
      qualifiedQty: Number(submitForm.qualifiedQty),
      unqualifiedQty: Number(submitForm.unqualifiedQty),
      remark: submitForm.remark?.trim()
    })
    message.success('提交工序质检成功')
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.sq-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.sq-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.sq-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.sq-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.sq-page__filter-card,
.sq-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.sq-query {
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

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    border-color: var(--erp-primary-300);
    background: var(--erp-surface-white);
    box-shadow: 0 0 0 3px var(--erp-primary-50);
  }
}

.sq-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.sq-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.sq-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sq-ledger__no {
  color: var(--erp-slate-900);
  font-weight: 700;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.sq-ledger__step {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sq-ledger__step-name {
  color: var(--erp-slate-800);
  font-weight: 600;
  line-height: 20px;
}

.sq-ledger__step-meta {
  color: var(--erp-slate-400);
  font-size: 11px;
}

.sq-ledger__qty strong {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.sq-ledger__qty span {
  color: var(--erp-slate-400);
  font-size: 11px;
}

.sq-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.sq-empty__icon {
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

.sq-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .sq-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
