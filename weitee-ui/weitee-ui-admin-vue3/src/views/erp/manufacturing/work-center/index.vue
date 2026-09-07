<template>
  <ContentWrap class="center-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="center-page__header">
      <div>
        <div class="center-page__title">工作中心</div>
        <div class="center-page__count">共 {{ total }} 条记录</div>
      </div>
      <div class="center-page__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:work-center:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新增工作中心
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="center-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="center-page__section-title">筛选条件</div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="center-query">
      <div class="center-query__grid">
        <el-form-item label="工作中心编码" prop="centerCode">
          <el-input v-model="queryParams.centerCode" clearable placeholder="请输入编码" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="工作中心名称" prop="centerName">
          <el-input v-model="queryParams.centerName" clearable placeholder="请输入名称" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </div>
      <div class="center-query__footer">
        <div></div>
        <div class="center-query__actions">
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="center-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="center-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="center-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="center-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="center-page__overview-card__content">
          <div class="center-page__overview-card__value">{{ card.value }}</div>
          <div class="center-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="center-table__scroll">
      <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="center-table">
        <template #empty>
          <div class="center-empty">
            <div class="center-empty__icon">
              <Icon icon="ep:office-building" />
            </div>
            <div class="center-empty__title">暂无工作中心</div>
          </div>
        </template>

        <el-table-column label="工作中心信息" min-width="240">
          <template #default="{ row }">
            <div class="center-info">
              <div class="center-info__code">{{ row.centerCode }}</div>
              <div class="center-info__name">{{ row.centerName }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所属部门" min-width="140">
          <template #default="{ row }">
            <span class="center-cell">{{ row.deptName || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="负责人" min-width="120">
          <template #default="{ row }">
            <span class="center-cell">{{ row.managerUserName || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="设备派工" width="100" align="center">
          <template #default="{ row }">
            <span class="center-pill" :class="row.enableDeviceDispatch ? 'center-pill--success' : 'center-pill--neutral'">
              {{ row.enableDeviceDispatch ? '启用' : '关闭' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="center-pill" :class="row.status === 1 ? 'center-pill--success' : 'center-pill--danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="160">
          <template #default="{ row }">
            <span class="center-cell center-cell--truncate" :title="row.remark || '-'">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <span class="center-date">{{ formatDateValue(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm('update', row)" v-hasPermi="['erp:work-center:update']">
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              :loading="deleteLoadingId === row.id"
              :disabled="deleteLoadingId === row.id"
              @click="handleDelete(row)"
              v-hasPermi="['erp:work-center:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <el-dialog
    v-model="formVisible"
    :title="isCreate ? '新增工作中心' : '编辑工作中心'"
    width="640px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <div class="center-form__grid">
        <el-form-item label="工作中心编码" prop="centerCode">
          <el-input v-model="form.centerCode" clearable placeholder="请输入编码" maxlength="64" />
        </el-form-item>
        <el-form-item label="工作中心名称" prop="centerName">
          <el-input v-model="form.centerName" clearable placeholder="请输入名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.deptName" readonly placeholder="点击选择部门" class="center-form__picker">
            <template #append>
              <el-button :disabled="saving" @click="openDeptSelect">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.managerUserName" readonly placeholder="点击选择负责人" class="center-form__picker">
            <template #append>
              <el-button :disabled="saving" @click="openUserSelect">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="设备派工">
          <el-switch v-model="form.enableDeviceDispatch" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark" class="center-form__full">
          <el-input v-model="form.remark" clearable placeholder="请输入备注" maxlength="255" />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button :disabled="saving" @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <DeptSelectForm ref="deptSelectRef" :multiple="false" @confirm="handleDeptConfirm" />
  <UserSelectForm ref="userSelectRef" :multiple="false" @confirm="handleUserConfirm" />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'
import {
  WorkCenterApi,
  type WorkCenterPageReqVO,
  type WorkCenterSaveReqVO,
  type WorkCenterVO
} from '@/api/erp/manufacturing/work-center'
import DeptSelectForm from '@/components/DeptSelectForm/index.vue'
import UserSelectForm from '@/components/UserSelectForm/index.vue'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpManufacturingWorkCenter' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref()
const deptSelectRef = ref()
const userSelectRef = ref()

const listLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const saving = ref(false)
const formVisible = ref(false)
const isCreate = ref(true)
const list = ref<WorkCenterVO[]>([])
const total = ref(0)

const queryParams = reactive<WorkCenterPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  centerCode: undefined,
  centerName: undefined,
  status: undefined
})

const form = reactive<WorkCenterSaveReqVO & { deptName?: string; managerUserName?: string }>({
  centerCode: '',
  centerName: '',
  deptId: undefined,
  deptName: undefined,
  managerUserId: undefined,
  managerUserName: undefined,
  enableDeviceDispatch: false,
  status: 1,
  remark: undefined
})

const rules = {
  centerCode: [{ required: true, message: '工作中心编码不能为空', trigger: 'blur' }],
  centerName: [{ required: true, message: '工作中心名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

const formatCount = (value?: number | string | null) => {
  const numberValue = Number(value || 0)
  if (Number.isInteger(numberValue)) {
    return numberValue.toLocaleString('zh-CN')
  }
  return numberValue.toFixed(3).replace(/\.?0+$/, '')
}

const formatDateValue = (value?: string | number) => (value ? formatDate(value) : '-')

const summaryCards = computed(() => {
  const enabled = list.value.filter((item) => item.status === 1).length
  return [
    { label: '总工作中心', value: formatCount(total.value), icon: 'ep:office-building', colorClass: 'stat-icon--blue' },
    { label: '已启用', value: formatCount(enabled), icon: 'ep:circle-check', colorClass: 'stat-icon--green' },
    { label: '已停用', value: formatCount(list.value.length - enabled), icon: 'ep:remove', colorClass: 'stat-icon--slate' },
    { label: '设备派工', value: formatCount(list.value.filter((item) => item.enableDeviceDispatch).length), icon: 'ep:cpu', colorClass: 'stat-icon--amber' }
  ]
})

const getList = async () => {
  listLoading.value = true
  try {
    const data = await WorkCenterApi.getWorkCenterPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (e: any) {
    list.value = []
    total.value = 0
    message.error(e?.message || '工作中心列表加载失败')
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

const resetForm = () => {
  form.id = undefined
  form.centerCode = ''
  form.centerName = ''
  form.deptId = undefined
  form.deptName = undefined
  form.managerUserId = undefined
  form.managerUserName = undefined
  form.enableDeviceDispatch = false
  form.status = 1
  form.remark = undefined
}

const openForm = (type: 'create' | 'update', row?: WorkCenterVO) => {
  isCreate.value = type === 'create'
  resetForm()
  if (type === 'update' && row) {
    Object.assign(form, {
      id: row.id,
      centerCode: row.centerCode,
      centerName: row.centerName,
      deptId: row.deptId,
      deptName: row.deptName,
      managerUserId: row.managerUserId,
      managerUserName: row.managerUserName,
      enableDeviceDispatch: Boolean(row.enableDeviceDispatch),
      status: row.status,
      remark: row.remark
    })
  }
  formVisible.value = true
}

const openDeptSelect = () => {
  if (saving.value) {
    return
  }
  deptSelectRef.value?.open(form.deptId ? [{ id: form.deptId, name: form.deptName }] : undefined)
}

const handleDeptConfirm = (depts: any[]) => {
  const dept = depts?.[0]
  form.deptId = dept?.id
  form.deptName = dept?.name
}

const openUserSelect = () => {
  if (saving.value) {
    return
  }
  userSelectRef.value?.open(0, form.managerUserId ? [{ id: form.managerUserId, nickname: form.managerUserName }] : undefined)
}

const handleUserConfirm = (_: unknown, users: any[]) => {
  const user = users?.[0]
  form.managerUserId = user?.id
  form.managerUserName = user?.nickname
}

const handleSave = async () => {
  if (saving.value) {
    return
  }
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (isCreate.value) {
      await WorkCenterApi.createWorkCenter(form)
    } else {
      await WorkCenterApi.updateWorkCenter(form)
    }
    message.success(isCreate.value ? '创建成功' : '更新成功')
    formVisible.value = false
    await getList()
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: WorkCenterVO) => {
  if (!row.id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = row.id
  try {
    await message.delConfirm()
    await WorkCenterApi.deleteWorkCenter(row.id)
    message.success('删除成功')
    await getList()
  } catch {
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(getList)
</script>

<style scoped lang="scss">
.center-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.center-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.center-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.center-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.center-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.center-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.center-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.center-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.center-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.center-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.center-page__overview-card {
  display: flex;
  min-height: 96px;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.center-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.center-page__overview-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.center-page__overview-card:hover .center-page__overview-card__icon {
  transform: scale(1.08);
}

.stat-icon--blue {
  background: #eff6ff;
  color: #2563eb;
}

.stat-icon--green {
  background: #ecfdf5;
  color: #059669;
}

.stat-icon--amber {
  background: #fffbeb;
  color: #d97706;
}

.stat-icon--slate {
  background: #f1f5f9;
  color: #64748b;
}

.center-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.center-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.center-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.center-table__scroll {
  overflow-x: auto;
}

.center-table {
  min-width: 1080px;
}

.center-table :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.center-table :deep(.el-table td.el-table__cell),
.center-table :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.center-info__code {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.center-info__name {
  margin-top: 4px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.center-cell {
  color: #475569;
  font-size: 13px;
  line-height: 20px;
}

.center-cell--truncate {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}

.center-date {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.center-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.center-pill--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.center-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.center-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.center-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.center-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.center-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.center-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.center-form__picker :deep(.el-input__inner) {
  cursor: pointer;
}

.center-form__full {
  grid-column: 1 / -1;
}

@media (max-width: 1279px) {
  .center-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .center-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .center-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .center-page__actions {
    width: 100%;
  }

  .center-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .center-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .center-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .center-query__actions {
    width: 100%;
  }

  .center-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
