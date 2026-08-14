<template>
  <section class="wcal-hero">
    <div class="wcal-hero__header">
      <div>
        <div class="wcal-hero__breadcrumb">制造执行管理 / 工作日历</div>
        <div class="wcal-page__title">工作日历</div>
      </div>
    </div>
  </section>

  <ContentWrap class="wcal-page__filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="wcal-query">
      <div class="wcal-query__grid">
        <el-form-item label="日历名称" prop="calendarName">
          <el-input
            v-model="queryParams.calendarName"
            placeholder="请输入日历名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </div>
      <div class="wcal-query__footer">
        <div class="wcal-query__actions">
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

  <ContentWrap class="wcal-page__list-card">
    <div class="wcal-toolbar">
      <div class="wcal-toolbar__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['mes:work-calendar:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增日历
        </el-button>
      </div>
    </div>
    <el-table v-loading="listLoading" :data="list" :stripe="true">
      <template #empty>
        <div class="wcal-empty">
          <div class="wcal-empty__icon">
            <Icon icon="ep:calendar" />
          </div>
          <div class="wcal-empty__title">暂无工作日历</div>
        </div>
      </template>
      <el-table-column label="日历名称" min-width="160">
        <template #default="{ row }">
          <div class="wcal-name">
            <span class="wcal-name__text">{{ row.calendarName }}</span>
            <span v-if="Number(row.status) === 0" class="wcal-badge wcal-badge--info">停用</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="适用工作中心" min-width="140">
        <template #default="{ row }">{{
          row.workCenterId ? `#${row.workCenterId}` : '全局默认'
        }}</template>
      </el-table-column>
      <el-table-column label="开工日" min-width="160" align="center">
        <template #default="{ row }">
          <div class="wcal-week">
            <span
              v-for="(day, idx) in weekDays"
              :key="idx"
              class="wcal-week__day"
              :class="{ 'wcal-week__day--on': row.weekMask?.[idx] === '1' }"
            >
              {{ day }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="每日小时" min-width="100" align="right">
        <template #default="{ row }">{{ row.dailyHours }}</template>
      </el-table-column>
      <el-table-column label="生效期" min-width="200">
        <template #default="{ row }">
          {{ row.effectiveDate || '-' }} ~ {{ row.expireDate || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="130" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('edit', row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

  <Dialog
    v-model="formVisible"
    :title="formMode === 'create' ? '新增工作日历' : '编辑工作日历'"
    width="560"
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
      <el-form-item label="日历名称" prop="calendarName">
        <el-input v-model="form.calendarName" placeholder="请输入日历名称" maxlength="64" />
      </el-form-item>
      <el-form-item label="适用工作中心" prop="workCenterId">
        <el-select
          v-model="form.workCenterId"
          filterable
          clearable
          placeholder="请选择工作中心（留空=全局默认）"
          class="!w-100%"
        >
          <el-option
            v-for="item in workCenterList"
            :key="item.id"
            :label="`${item.centerName}（${item.centerCode}）`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开工日" prop="weekMask" required>
        <div class="wcal-form-week">
          <el-checkbox-group v-model="weekMaskArr">
            <el-checkbox v-for="(day, idx) in weekDays" :key="idx" :value="idx.toString()">
              {{ day }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </el-form-item>
      <el-form-item label="每日可用小时" prop="dailyHours">
        <el-input-number
          v-model="form.dailyHours"
          controls-position="right"
          :min="0.5"
          :max="24"
          :precision="2"
          :step="0.5"
          class="!w-100%"
        />
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker
          v-model="form.effectiveDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择生效日期"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="失效日期">
        <el-date-picker
          v-model="form.expireDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择失效日期"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { WorkCenterApi, WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { WorkCalendarApi, WorkCalendarSaveReqVO } from '@/api/mes/work-calendar'

defineOptions({ name: 'MesWorkCalendar' })

const message = useMessage()

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const queryFormRef = ref()
const formRef = ref<FormInstance>()
const listLoading = ref(false)
const submitLoading = ref(false)
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')

const list = ref<WorkCalendarSaveReqVO[]>([])
const total = ref(0)
const workCenterList = ref<WorkCenterSimpleVO[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  calendarName: undefined as string | undefined,
  status: undefined as number | undefined
})

const form = reactive<WorkCalendarSaveReqVO>({
  id: undefined,
  calendarName: '',
  workCenterId: undefined,
  effectiveDate: undefined,
  expireDate: undefined,
  weekMask: '1111100',
  dailyHours: 8,
  remark: undefined
})

const weekMaskArr = computed<string[]>({
  get: () =>
    Array.from(form.weekMask || '0000000')
      .map((c, i) => (c === '1' ? i.toString() : ''))
      .filter(Boolean),
  set: (vals: string[]) => {
    form.weekMask = weekDays.map((_, i) => (vals.includes(i.toString()) ? '1' : '0')).join('')
  }
})

const formRules: FormRules = {
  calendarName: [{ required: true, message: '请输入日历名称', trigger: 'blur' }],
  weekMask: [{ required: true, message: '请选择开工日', trigger: 'change' }],
  dailyHours: [{ required: true, message: '请输入每日可用小时', trigger: 'blur' }]
}

const getList = async () => {
  listLoading.value = true
  try {
    const data = await WorkCalendarApi.getWorkCalendarPage(queryParams)
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

const openForm = async (mode: 'create' | 'edit', row?: WorkCalendarSaveReqVO) => {
  formMode.value = mode
  Object.assign(form, {
    id: undefined,
    calendarName: '',
    workCenterId: undefined,
    effectiveDate: undefined,
    expireDate: undefined,
    weekMask: '1111100',
    dailyHours: 8,
    remark: undefined
  })
  if (mode === 'edit' && row?.id) {
    const data = await WorkCalendarApi.getWorkCalendar(row.id)
    Object.assign(form, {
      id: data.id,
      calendarName: data.calendarName,
      workCenterId: data.workCenterId,
      effectiveDate: data.effectiveDate,
      expireDate: data.expireDate,
      weekMask: data.weekMask || '0000000',
      dailyHours: Number(data.dailyHours || 8),
      remark: data.remark
    })
  }
  formVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (formMode.value === 'create') {
      await WorkCalendarApi.createWorkCalendar(form)
      message.success('创建工作日历成功')
    } else {
      await WorkCalendarApi.updateWorkCalendar(form)
      message.success('更新工作日历成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: WorkCalendarSaveReqVO) => {
  if (!row.id) return
  try {
    await message.confirm(`确定删除工作日历「${row.calendarName}」吗？`)
  } catch {
    return
  }
  await WorkCalendarApi.deleteWorkCalendar(row.id)
  message.success('删除工作日历成功')
  await getList()
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
.wcal-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.wcal-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.wcal-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.wcal-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.wcal-page__filter-card,
.wcal-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.wcal-query {
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

.wcal-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.wcal-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.wcal-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.wcal-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wcal-name__text {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.wcal-badge {
  display: inline-flex;
  align-items: center;
  min-height: 20px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  background: var(--erp-slate-100);
  color: var(--erp-slate-500);
}

.wcal-week {
  display: inline-flex;
  gap: 4px;
}

.wcal-week__day {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: var(--erp-slate-100);
  color: var(--erp-slate-400);
  font-size: 11px;
  font-weight: 600;
}

.wcal-week__day--on {
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
}

.wcal-form-week {
  padding: 4px 0;
}

.wcal-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.wcal-empty__icon {
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

.wcal-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .wcal-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
