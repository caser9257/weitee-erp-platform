<template>
  <ContentWrap class="device-page__hero-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="device-page__header">
      <div>
        <div class="device-page__title">设备台账</div>
        <div class="device-page__count">共 {{ total }} 条记录</div>
      </div>
      <div class="device-page__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:device:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新增设备
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="device-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="device-page__section-title">筛选条件</div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="device-query">
      <div class="device-query__grid">
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="queryParams.deviceCode" clearable placeholder="请输入设备编码" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="queryParams.deviceName" clearable placeholder="请输入设备名称" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="工作中心" prop="workCenterId">
          <el-select v-model="queryParams.workCenterId" clearable filterable :loading="centerLoading" placeholder="请选择工作中心">
            <el-option
              v-for="item in workCenterOptions"
              :key="item.id"
              :label="`${item.centerCode} / ${item.centerName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态" prop="deviceStatus">
          <el-select v-model="queryParams.deviceStatus" clearable placeholder="请选择状态">
            <el-option
              v-for="item in getIntDictOptions(DICT_TYPE.ERP_DEVICE_STATUS)"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="device-query__footer">
        <div></div>
        <div class="device-query__actions">
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

  <ContentWrap class="device-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="device-page__overview-grid">
      <article
        v-for="(card, index) in summaryCards"
        :key="card.label"
        class="device-page__overview-card"
        :class="resolveSummaryCardClass(index)"
      >
        <div class="device-page__overview-card__icon" :class="card.colorClass">
          <Icon :icon="card.icon" />
        </div>
        <div class="device-page__overview-card__content">
          <div class="device-page__overview-card__value">{{ card.value }}</div>
          <div class="device-page__overview-card__label">{{ card.label }}</div>
        </div>
      </article>
    </div>

    <div class="device-table__scroll">
      <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true" class="device-table">
        <template #empty>
          <div class="device-empty">
            <div class="device-empty__icon">
              <Icon icon="ep:cpu" />
            </div>
            <div class="device-empty__title">暂无设备</div>
          </div>
        </template>

        <el-table-column label="设备信息" min-width="230">
          <template #default="{ row }">
            <div class="device-info">
              <div class="device-info__code">{{ row.deviceCode }}</div>
              <div class="device-info__name">{{ row.deviceName }}</div>
              <div class="device-info__meta">{{ row.specification || '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="工作中心" min-width="140">
          <template #default="{ row }">
            <span class="device-cell">{{ row.workCenterName || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="device-pill" :class="getStatusPillClass(row.deviceStatus)">
              {{ getStatusLabel(row.deviceStatus) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="维护/点检周期(天)" min-width="150" align="center">
          <template #default="{ row }">
            <span class="device-cell">{{ row.maintenanceCycleDay ?? '-' }} / {{ row.checkCycleDay ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="购置日期" width="120" align="center">
          <template #default="{ row }">
            <span class="device-date">{{ formatDateValue(row.purchaseDate) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="制造商" min-width="140">
          <template #default="{ row }">
            <span class="device-cell device-cell--truncate" :title="row.manufacturer || '-'">{{ row.manufacturer || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <span class="device-date">{{ formatDateValue(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm('update', row)" v-hasPermi="['erp:device:update']">
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              :loading="deleteLoadingId === row.id"
              :disabled="deleteLoadingId === row.id"
              @click="handleDelete(row)"
              v-hasPermi="['erp:device:delete']"
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
    :title="isCreate ? '新增设备' : '编辑设备'"
    width="720px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <div class="device-form__grid">
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="form.deviceCode" clearable placeholder="请输入设备编码" maxlength="64" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" clearable placeholder="请输入设备名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="工作中心">
          <el-select v-model="form.workCenterId" clearable filterable :loading="centerLoading" placeholder="请选择工作中心" style="width: 100%">
            <el-option
              v-for="item in workCenterOptions"
              :key="item.id"
              :label="`${item.centerCode} / ${item.centerName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态" prop="deviceStatus">
          <el-select v-model="form.deviceStatus" style="width: 100%">
            <el-option
              v-for="item in getIntDictOptions(DICT_TYPE.ERP_DEVICE_STATUS)"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.specification" clearable placeholder="请输入规格型号" maxlength="128" />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model="form.manufacturer" clearable placeholder="请输入制造商" maxlength="128" />
        </el-form-item>
        <el-form-item label="维护周期(天)">
          <el-input-number v-model="form.maintenanceCycleDay" :min="0" :precision="0" :controls="false" placeholder="维护周期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="点检周期(天)">
          <el-input-number v-model="form.checkCycleDay" :min="0" :precision="0" :controls="false" placeholder="点检周期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="购置日期">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择购置日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用日期">
          <el-date-picker v-model="form.startUseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择启用日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark" class="device-form__full">
          <el-input v-model="form.remark" clearable placeholder="请输入备注" maxlength="255" />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button :disabled="saving" @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { formatDate } from '@/utils/formatTime'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { DeviceApi, type DevicePageReqVO, type DeviceSaveReqVO, type DeviceVO } from '@/api/erp/manufacturing/device'
import { WorkCenterApi, type WorkCenterSimpleVO } from '@/api/erp/manufacturing/work-center'
import { getToneCardClass, resolveSummaryCardClass } from '../../stock/shared/stockTone'

defineOptions({ name: 'ErpManufacturingDevice' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref()

const listLoading = ref(false)
const centerLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const saving = ref(false)
const formVisible = ref(false)
const isCreate = ref(true)
const list = ref<DeviceVO[]>([])
const total = ref(0)
const workCenterOptions = ref<WorkCenterSimpleVO[]>([])

const queryParams = reactive<DevicePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceCode: undefined,
  deviceName: undefined,
  workCenterId: undefined,
  deviceStatus: undefined
})

const form = reactive<DeviceSaveReqVO>({
  deviceCode: '',
  deviceName: '',
  workCenterId: undefined,
  specification: undefined,
  deviceStatus: 1,
  maintenanceCycleDay: undefined,
  checkCycleDay: undefined,
  purchaseDate: undefined,
  startUseDate: undefined,
  manufacturer: undefined,
  remark: undefined
})

const rules = {
  deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  deviceStatus: [{ required: true, message: '设备状态不能为空', trigger: 'change' }]
}

const statusLabelMap = new Map(
  getIntDictOptions(DICT_TYPE.ERP_DEVICE_STATUS).map((item) => [Number(item.value), item.label])
)

const getStatusLabel = (status?: number) => statusLabelMap.get(Number(status)) ?? '-'

const getStatusPillClass = (status?: number) => {
  switch (Number(status)) {
    case 2:
      return 'device-pill--success'
    case 3:
      return 'device-pill--warning'
    case 4:
      return 'device-pill--danger'
    default:
      return 'device-pill--neutral'
  }
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
  const running = list.value.filter((item) => item.deviceStatus === 2).length
  const maintenance = list.value.filter((item) => item.deviceStatus === 3).length
  const disabled = list.value.filter((item) => item.deviceStatus === 4).length
  return [
    { label: '总设备数', value: formatCount(total.value), icon: 'ep:cpu', colorClass: 'stat-icon--blue' },
    { label: '运行中', value: formatCount(running), icon: 'ep:video-play', colorClass: 'stat-icon--green' },
    { label: '维修中', value: formatCount(maintenance), icon: 'ep:tools', colorClass: 'stat-icon--amber' },
    { label: '已停用', value: formatCount(disabled), icon: 'ep:remove', colorClass: 'stat-icon--slate' }
  ]
})

const getList = async () => {
  listLoading.value = true
  try {
    const data = await DeviceApi.getDevicePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (e: any) {
    list.value = []
    total.value = 0
    message.error(e?.message || '设备列表加载失败')
  } finally {
    listLoading.value = false
  }
}

const loadWorkCenters = async () => {
  centerLoading.value = true
  try {
    workCenterOptions.value = (await WorkCenterApi.getWorkCenterSimpleList()) || []
  } finally {
    centerLoading.value = false
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
  form.deviceCode = ''
  form.deviceName = ''
  form.workCenterId = undefined
  form.specification = undefined
  form.deviceStatus = 1
  form.maintenanceCycleDay = undefined
  form.checkCycleDay = undefined
  form.purchaseDate = undefined
  form.startUseDate = undefined
  form.manufacturer = undefined
  form.remark = undefined
}

const openForm = (type: 'create' | 'update', row?: DeviceVO) => {
  isCreate.value = type === 'create'
  resetForm()
  if (type === 'update' && row) {
    Object.assign(form, {
      id: row.id,
      deviceCode: row.deviceCode,
      deviceName: row.deviceName,
      workCenterId: row.workCenterId,
      specification: row.specification,
      deviceStatus: row.deviceStatus,
      maintenanceCycleDay: row.maintenanceCycleDay,
      checkCycleDay: row.checkCycleDay,
      purchaseDate: row.purchaseDate,
      startUseDate: row.startUseDate,
      manufacturer: row.manufacturer,
      remark: row.remark
    })
  }
  formVisible.value = true
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
      await DeviceApi.createDevice(form)
    } else {
      await DeviceApi.updateDevice(form)
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

const handleDelete = async (row: DeviceVO) => {
  if (!row.id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = row.id
  try {
    await message.delConfirm()
    await DeviceApi.deleteDevice(row.id)
    message.success('删除成功')
    await getList()
  } catch {
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadWorkCenters()])
})
</script>

<style scoped lang="scss">
.device-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.device-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.device-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.device-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.device-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.device-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.device-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.device-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 统计卡片 */
.device-page__overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.device-page__overview-card {
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

.device-page__overview-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.device-page__overview-card__icon {
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

.device-page__overview-card:hover .device-page__overview-card__icon {
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

.device-page__overview-card__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.device-page__overview-card__label {
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
}

.device-page__overview-card__value {
  color: #0f172a;
  font-size: 24px;
  line-height: 30px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.device-table__scroll {
  overflow-x: auto;
}

.device-table {
  min-width: 1120px;
}

.device-table :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

.device-table :deep(.el-table td.el-table__cell),
.device-table :deep(.el-table th.el-table__cell.is-leaf) {
  border-bottom: 1px solid #f1f5f9;
}

.device-info__code {
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.device-info__name {
  margin-top: 4px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.device-info__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-cell {
  color: #475569;
  font-size: 13px;
  line-height: 20px;
}

.device-cell--truncate {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}

.device-date {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.device-pill {
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

.device-pill--success {
  border-color: #a7f3d0;
  background: #ecfdf5;
  color: #059669;
}

.device-pill--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.device-pill--danger {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.device-pill--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.device-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.device-empty__icon {
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

.device-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.device-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.device-form__full {
  grid-column: 1 / -1;
}

@media (max-width: 1279px) {
  .device-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .device-page__overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .device-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .device-page__actions {
    width: 100%;
  }

  .device-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .device-page__overview-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .device-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .device-query__actions {
    width: 100%;
  }

  .device-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
