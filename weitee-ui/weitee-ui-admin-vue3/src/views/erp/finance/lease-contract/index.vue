<template>
  <div class="min-h-full bg-[var(--erp-slate-50)] p-3 sm:p-4 lg:p-5">
    <section class="erp-card mb-4">
      <div class="flex items-center justify-between gap-3 px-5 py-4">
        <h1 class="text-lg font-semibold text-[var(--erp-slate-800)]">租赁合同</h1>
        <el-tag type="info" effect="light">{{ total }} 条</el-tag>
      </div>
    </section>

    <section class="erp-card mb-4">
      <div class="border-b border-[var(--erp-slate-100)] px-5 py-3">
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">查询条件</h2>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="82px" class="px-5 pb-4 pt-4">
        <div class="grid gap-x-4 gap-y-2 md:grid-cols-2 xl:grid-cols-4">
          <el-form-item label="合同编号" prop="no">
            <el-input
              v-model="queryParams.no"
              clearable
              placeholder="请输入合同编号"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="合同名称" prop="name">
            <el-input
              v-model="queryParams.name"
              clearable
              placeholder="请输入合同名称"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="供应商" prop="supplierId">
            <el-select
              v-model="queryParams.supplierId"
              clearable
              filterable
              placeholder="请选择供应商"
            >
              <el-option
                v-for="supplier in supplierOptions"
                :key="supplier.id"
                :label="supplier.name"
                :value="supplier.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="flex flex-wrap justify-end gap-2">
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-1" />查询
          </el-button>
          <el-button :disabled="isQueryResetDisabled" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-1" />重置
          </el-button>
        </div>
      </el-form>
    </section>

    <section class="erp-card overflow-hidden">
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--erp-slate-100)] px-5 py-3"
      >
        <h2 class="text-sm font-semibold text-[var(--erp-slate-800)]">租赁合同列表</h2>
        <el-button
          type="primary"
          :disabled="!canCreate"
          @click="openForm('create')"
          v-hasPermi="['erp:lease-contract:create']"
        >
          <Icon icon="ep:plus" class="mr-1" />新增
        </el-button>
      </div>
      <div class="overflow-x-auto">
        <el-alert
          v-if="listErrorMessage"
          :title="listErrorMessage"
          type="error"
          show-icon
          class="mx-5 mt-4"
        />
        <el-table v-loading="listLoading" :data="list" class="min-w-[1080px]" table-layout="fixed">
          <el-table-column label="合同信息" min-width="240" fixed="left">
            <template #default="{ row }">
              <div class="space-y-1 py-1">
                <div
                  class="truncate font-mono text-sm text-[var(--erp-slate-800)]"
                  :title="row.no || '-'"
                  >{{ row.no || '-' }}</div
                >
                <div
                  class="truncate text-sm text-[var(--erp-slate-700)]"
                  :title="row.name || '-'"
                  >{{ row.name || '-' }}</div
                >
                <div class="text-xs text-[var(--erp-slate-500)]">{{
                  formatDateRange(row.startDate, row.endDate)
                }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="供应商" min-width="180">
            <template #default="{ row }">
              <span class="truncate" :title="resolveSupplierName(row)">{{
                resolveSupplierName(row)
              }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月租金" min-width="130" align="right">
            <template #default="{ row }"
              ><span class="font-mono">{{ formatAmount(row.monthlyRent) }}</span></template
            >
          </el-table-column>
          <el-table-column label="付款周期" min-width="110" align="right">
            <template #default="{ row }"
              ><span class="font-mono">{{ row.paymentCycle || 1 }} 个月</span></template
            >
          </el-table-column>
          <el-table-column label="合同总金额" min-width="150" align="right">
            <template #default="{ row }"
              ><span class="font-mono font-semibold">{{
                formatAmount(row.totalAmount)
              }}</span></template
            >
          </el-table-column>
          <el-table-column label="状态" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="light">{{
                getStatusLabel(row.status)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-1">
                <el-button link type="primary" @click="openForm('detail', row.id)">查看</el-button>
                <el-button
                  v-if="canEdit(row)"
                  link
                  type="primary"
                  :disabled="formLoading"
                  @click="openForm('update', row.id)"
                  v-hasPermi="['erp:lease-contract:update']"
                  >编辑</el-button
                >
                <el-button
                  v-if="canDelete(row)"
                  link
                  type="danger"
                  :loading="deletingId === row.id"
                  @click="handleDelete(row)"
                  v-hasPermi="['erp:lease-contract:delete']"
                  >删除</el-button
                >
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :description="listErrorMessage || '暂无租赁合同'" :image-size="72">
              <el-button
                v-if="listErrorMessage"
                type="primary"
                plain
                :disabled="listLoading"
                @click="getList"
                >重试</el-button
              >
            </el-empty>
          </template>
        </el-table>
      </div>
      <div class="flex justify-end border-t border-[var(--erp-slate-100)] px-5 py-3">
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </section>

    <el-dialog
      v-model="formVisible"
      :title="formTitle"
      width="760px"
      destroy-on-close
      :close-on-click-modal="!formSaving"
      :close-on-press-escape="!formSaving"
      @closed="resetFormState"
    >
      <el-alert
        v-if="formErrorMessage"
        :title="formErrorMessage"
        type="error"
        show-icon
        class="mb-4"
      />
      <el-form
        v-loading="formLoading"
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="96px"
        class="pr-4"
      >
        <div class="grid gap-x-4 md:grid-cols-2">
          <el-form-item label="合同编号" prop="no"
            ><el-input v-model="formData.no" :disabled="isDetail" placeholder="请输入合同编号"
          /></el-form-item>
          <el-form-item label="合同名称" prop="name"
            ><el-input v-model="formData.name" :disabled="isDetail" placeholder="请输入合同名称"
          /></el-form-item>
          <el-form-item label="供应商" prop="supplierId">
            <el-select
              v-model="formData.supplierId"
              :disabled="isDetail"
              filterable
              placeholder="请选择供应商"
            >
              <el-option
                v-for="supplier in supplierOptions"
                :key="supplier.id"
                :label="supplier.name"
                :value="supplier.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="成本中心 ID" prop="costCenterId"
            ><el-input-number
              v-model="formData.costCenterId"
              :disabled="isDetail"
              :controls="false"
              :min="1"
              :precision="0"
              class="!w-full"
              placeholder="请输入成本中心 ID"
          /></el-form-item>
          <el-form-item label="开始日期" prop="startDate"
            ><el-date-picker
              v-model="formData.startDate"
              :disabled="isDetail"
              class="!w-full"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择开始日期"
          /></el-form-item>
          <el-form-item label="结束日期" prop="endDate"
            ><el-date-picker
              v-model="formData.endDate"
              :disabled="isDetail"
              class="!w-full"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择结束日期"
          /></el-form-item>
          <el-form-item label="月租金" prop="monthlyRent"
            ><el-input-number
              v-model="formData.monthlyRent"
              :disabled="isDetail"
              :controls="false"
              :min="0"
              :precision="2"
              class="!w-full"
              placeholder="请输入月租金"
          /></el-form-item>
          <el-form-item label="付款周期" prop="paymentCycle"
            ><el-input-number
              v-model="formData.paymentCycle"
              :disabled="isDetail"
              :controls="false"
              :min="1"
              :precision="0"
              class="!w-full"
              placeholder="请输入付款周期"
          /></el-form-item>
          <el-form-item label="合同总金额" prop="totalAmount"
            ><el-input-number
              v-model="formData.totalAmount"
              :disabled="isDetail"
              :controls="false"
              :min="0"
              :precision="2"
              class="!w-full"
              placeholder="请输入合同总金额"
          /></el-form-item>
          <el-form-item label="附件地址" prop="fileUrl"
            ><el-input v-model="formData.fileUrl" :disabled="isDetail" placeholder="请输入附件地址"
          /></el-form-item>
        </div>
        <el-form-item label="备注" prop="remark"
          ><el-input
            v-model="formData.remark"
            :disabled="isDetail"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button
          v-if="formErrorMessage && formRecordId"
          type="primary"
          plain
          :disabled="formLoading"
          @click="retryFormLoad"
          >重试加载</el-button
        >
        <el-button :disabled="formSaving" @click="formVisible = false">取消</el-button>
        <el-button
          v-if="!isDetail"
          type="primary"
          :loading="formSaving"
          :disabled="!canSave"
          @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import {
  LEASE_CONTRACT_STATUS,
  LeaseContractApi,
  type LeaseContractPageReqVO,
  type LeaseContractSaveReqVO,
  type LeaseContractVO
} from '@/api/erp/finance/lease-contract'

defineOptions({ name: 'ErpLeaseContract' })

const message = useMessage()
const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const listLoading = ref(false)
const formLoading = ref(false)
const formSaving = ref(false)
const deletingId = ref<number>()
const listErrorMessage = ref('')
const list = ref<LeaseContractVO[]>([])
const total = ref(0)
const supplierOptions = ref<SupplierVO[]>([])
const formVisible = ref(false)
const formMode = ref<'create' | 'update' | 'detail'>('create')
const formInitialSnapshot = ref('')
const formErrorMessage = ref('')
const formRecordId = ref<number>()

const canQuery = checkPermi(['erp:lease-contract:query'])
const canCreatePermission = checkPermi(['erp:lease-contract:create'])
const canUpdatePermission = checkPermi(['erp:lease-contract:update'])
const canDeletePermission = checkPermi(['erp:lease-contract:delete'])

const queryParams = reactive<LeaseContractPageReqVO>({ pageNo: 1, pageSize: 10 })
const createEmptyForm = (): LeaseContractSaveReqVO => ({
  id: undefined,
  no: '',
  name: '',
  supplierId: undefined,
  startDate: '',
  endDate: '',
  monthlyRent: undefined,
  paymentCycle: 1
})
const formData = reactive<LeaseContractSaveReqVO>(createEmptyForm())

const statusOptions = [
  { label: '草稿', value: LEASE_CONTRACT_STATUS.DRAFT },
  { label: '审批中', value: LEASE_CONTRACT_STATUS.APPROVING },
  { label: '生效', value: LEASE_CONTRACT_STATUS.EFFECTIVE },
  { label: '到期', value: LEASE_CONTRACT_STATUS.EXPIRED },
  { label: '终止', value: LEASE_CONTRACT_STATUS.TERMINATED }
]
const formRules: FormRules<LeaseContractSaveReqVO> = {
  no: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  monthlyRent: [{ required: true, message: '请输入月租金', trigger: 'blur' }]
}

const isDetail = computed(() => formMode.value === 'detail')
const canCreate = computed(() => canCreatePermission && !formLoading.value && !formSaving.value)
const isDirty = computed(() => JSON.stringify(formData) !== formInitialSnapshot.value)
const canSavePermission = computed(() =>
  formMode.value === 'create' ? canCreatePermission : canUpdatePermission
)
const canSave = computed(
  () =>
    canSavePermission.value &&
    isDirty.value &&
    !formLoading.value &&
    !formSaving.value &&
    !isDetail.value
)
const isQueryResetDisabled = computed(() => listLoading.value)
const formTitle = computed(
  () => ({ create: '新增租赁合同', update: '编辑租赁合同', detail: '租赁合同详情' })[formMode.value]
)

const getStatusLabel = (status?: number) =>
  statusOptions.find((item) => item.value === status)?.label || '-'
const getStatusTagType = (status?: number) => {
  if (status === LEASE_CONTRACT_STATUS.DRAFT) return 'info'
  if (status === LEASE_CONTRACT_STATUS.APPROVING) return 'warning'
  if (status === LEASE_CONTRACT_STATUS.EFFECTIVE) return 'success'
  if (status === LEASE_CONTRACT_STATUS.TERMINATED) return 'danger'
  return 'info'
}
const formatAmount = (value?: number) =>
  value == null
    ? '-'
    : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatDateTime = (value?: string) => (value ? formatDate(value, 'YYYY-MM-DD HH:mm:ss') : '-')
const formatDateRange = (startDate?: string, endDate?: string) =>
  `${startDate || '-'} 至 ${endDate || '-'}`
const resolveSupplierName = (row: LeaseContractVO) =>
  row.supplierName ||
  supplierOptions.value.find((item) => item.id === row.supplierId)?.name ||
  `供应商 ${row.supplierId || '-'}`
const canEdit = (row: LeaseContractVO) =>
  canUpdatePermission &&
  row.status === LEASE_CONTRACT_STATUS.DRAFT &&
  !formLoading.value &&
  deletingId.value !== row.id
const canDelete = (row: LeaseContractVO) =>
  canDeletePermission &&
  row.status === LEASE_CONTRACT_STATUS.DRAFT &&
  deletingId.value !== row.id &&
  !formSaving.value

const loadSupplierOptions = async () => {
  supplierOptions.value = await SupplierApi.getSupplierSimpleList()
}
const getList = async () => {
  if (!canQuery || listLoading.value) return
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await LeaseContractApi.getPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error: any) {
    listErrorMessage.value = error?.message || '加载失败，请重试'
  } finally {
    listLoading.value = false
  }
}
const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}
const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await handleQuery()
}
const resetFormState = () => {
  formRef.value?.resetFields()
  Object.assign(formData, createEmptyForm())
  formInitialSnapshot.value = JSON.stringify(formData)
  formErrorMessage.value = ''
  formRecordId.value = undefined
  formMode.value = 'create'
}
const openForm = async (mode: 'create' | 'update' | 'detail', id?: number) => {
  if (
    formLoading.value ||
    formSaving.value ||
    (mode === 'create' && !canCreatePermission) ||
    (mode === 'update' && !canUpdatePermission) ||
    (mode !== 'create' && !id)
  )
    return
  formMode.value = mode
  formRecordId.value = id
  formVisible.value = true
  formErrorMessage.value = ''
  Object.assign(formData, createEmptyForm())
  formInitialSnapshot.value = JSON.stringify(formData)
  if (mode === 'create') return
  formLoading.value = true
  try {
    Object.assign(formData, await LeaseContractApi.get(id!))
    formInitialSnapshot.value = JSON.stringify(formData)
  } catch (error: any) {
    formErrorMessage.value = error?.message || '加载失败，请重试'
  } finally {
    formLoading.value = false
  }
}
const retryFormLoad = () => {
  if (formRecordId.value && formMode.value !== 'create') {
    openForm(formMode.value, formRecordId.value)
  }
}
const submitForm = async () => {
  if (formSaving.value || !canSave.value || isDetail.value || !(await formRef.value?.validate()))
    return
  formSaving.value = true
  try {
    if (formData.id) await LeaseContractApi.update(formData)
    else await LeaseContractApi.create(formData)
    message.success('保存成功')
    formVisible.value = false
    await getList()
  } catch (error: any) {
    formErrorMessage.value = error?.message || '保存失败，请重试'
  } finally {
    formSaving.value = false
  }
}
const handleDelete = async (row: LeaseContractVO) => {
  if (!row.id || !canDelete(row)) return
  try {
    await message.confirm(`确认删除租赁合同 ${row.no || row.id} 吗？`)
  } catch {
    return
  }
  if (deletingId.value) return
  deletingId.value = row.id
  try {
    await LeaseContractApi.delete(row.id)
    message.success('删除成功')
    await getList()
  } finally {
    deletingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.allSettled([loadSupplierOptions(), getList()])
})
</script>
