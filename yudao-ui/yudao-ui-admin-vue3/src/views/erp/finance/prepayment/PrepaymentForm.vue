<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(980px, 96vw)"
    scroll
    maxHeight="78vh"
  >
    <div class="finance-prepayment-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        v-loading="dialogLoading"
        :disabled="formDisabled"
      >
        <div class="finance-prepayment-form__panel">
          <div class="finance-prepayment-form__grid">
            <el-form-item label="预付款单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="预付时间" prop="prepaymentTime">
              <el-date-picker
                v-model="formData.prepaymentTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择预付时间"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="formData.supplierId"
                clearable
                filterable
                placeholder="请选择供应商"
                class="!w-full"
              >
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="财务人员" prop="financeUserId">
              <el-select
                v-model="formData.financeUserId"
                clearable
                filterable
                placeholder="请选择财务人员"
                class="!w-full"
              >
                <el-option
                  v-for="item in userList"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="结算账户" prop="accountId">
              <el-select
                v-model="formData.accountId"
                clearable
                filterable
                placeholder="请选择结算账户"
                class="!w-full"
              >
                <el-option
                  v-for="item in accountList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="预付金额" prop="prepaymentPrice">
              <el-input-number
                v-model="formData.prepaymentPrice"
                controls-position="right"
                :precision="2"
                :min="0.01"
                class="!w-full"
                placeholder="请输入预付金额"
              />
            </el-form-item>
            <el-form-item v-if="detailMode" label="已核销金额">
              <el-input
                :model-value="formatAmount(formData.allocatedPrice)"
                disabled
                class="font-mono"
              />
            </el-form-item>
            <el-form-item v-if="detailMode" label="剩余金额">
              <el-input
                :model-value="formatAmount(formData.remainPrice)"
                disabled
                class="font-mono"
              />
            </el-form-item>
          </div>

          <el-form-item label="备注" prop="remark" class="finance-prepayment-form__remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注"
            />
          </el-form-item>
        </div>

        <div v-if="detailMode" class="finance-prepayment-form__panel">
          <div class="finance-prepayment-form__section-title">核销记录</div>
          <div class="finance-prepayment-form__table-wrap">
            <el-table
              :data="formData.allocates || []"
              stripe
              class="finance-prepayment-form__table"
              :show-overflow-tooltip="false"
            >
              <el-table-column prop="bizNo" label="业务单号" min-width="180" />
              <el-table-column prop="statusName" label="状态" min-width="120" align="center" />
              <el-table-column label="核销金额" min-width="120" align="right">
                <template #default="{ row }">
                  <span class="finance-prepayment-form__amount">{{ formatAmount(row.allocateAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="180" />
              <el-table-column label="创建时间" min-width="180" align="center">
                <template #default="{ row }">
                  {{ formatDateTimeValue(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-if="!(formData.allocates || []).length" description="暂无核销记录" />
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button
        v-if="!detailMode"
        type="primary"
        :loading="submitLoading"
        :disabled="submitDisabled"
        @click="submitForm"
      >
        保存
      </el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import type { AccountVO } from '@/api/erp/finance/account'
import type { SupplierVO } from '@/api/erp/purchase/supplier'
import type { SimpleUserVO } from '@/api/system/user'
import {
  FinancePrepaymentApi,
  type ErpFinancePrepaymentSaveReqVO,
  type ErpFinancePrepaymentVO
} from '@/api/erp/finance/prepayment'
import { AccountApi } from '@/api/erp/finance/account'
import { SupplierApi } from '@/api/erp/purchase/supplier'
import { getSimpleUserList } from '@/api/system/user'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'FinancePrepaymentForm' })

type FormType = 'create' | 'update' | 'detail'

interface FinancePrepaymentFormData {
  id?: number
  no?: string
  prepaymentTime?: string
  financeUserId?: number
  financeUserName?: string
  supplierId?: number
  supplierName?: string
  accountId?: number
  accountName?: string
  prepaymentPrice?: number
  allocatedPrice?: number
  remainPrice?: number
  remark?: string
  allocates?: ErpFinancePrepaymentVO['allocates']
}

const message = useMessage()

const createEmptyFormData = (): FinancePrepaymentFormData => ({
  id: undefined,
  no: undefined,
  prepaymentTime: undefined,
  financeUserId: undefined,
  financeUserName: undefined,
  supplierId: undefined,
  supplierName: undefined,
  accountId: undefined,
  accountName: undefined,
  prepaymentPrice: undefined,
  allocatedPrice: 0,
  remainPrice: 0,
  remark: '',
  allocates: []
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<FinancePrepaymentFormData>(createEmptyFormData())
const formRef = ref<FormInstance>()

const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<SimpleUserVO[]>([])

const detailMode = computed(() => formType.value === 'detail')
const formDisabled = computed(() => detailMode.value || dialogLoading.value || submitLoading.value)
const submitDisabled = computed(() => detailMode.value || dialogLoading.value || submitLoading.value)

const formRules: FormRules<FinancePrepaymentFormData> = {
  prepaymentTime: [{ required: true, message: '请选择预付时间', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择结算账户', trigger: 'change' }],
  prepaymentPrice: [
    { required: true, message: '请输入预付金额', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value == null || Number(value) <= 0) {
          callback(new Error('预付金额必须大于 0'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ]
}

const normalizeFormData = (detail?: ErpFinancePrepaymentVO): FinancePrepaymentFormData => ({
  id: detail?.id,
  no: detail?.no,
  prepaymentTime: detail?.prepaymentTime ? formatDateTimeValue(detail.prepaymentTime) : undefined,
  financeUserId: detail?.financeUserId,
  financeUserName: detail?.financeUserName,
  supplierId: detail?.supplierId,
  supplierName: detail?.supplierName,
  accountId: detail?.accountId,
  accountName: detail?.accountName,
  prepaymentPrice: Number(detail?.prepaymentPrice || 0),
  allocatedPrice: Number(detail?.allocatedPrice || 0),
  remainPrice: Number(detail?.remainPrice || 0),
  remark: detail?.remark || '',
  allocates: detail?.allocates || []
})

const buildSaveData = (): ErpFinancePrepaymentSaveReqVO => ({
  id: formData.value.id,
  prepaymentTime: formData.value.prepaymentTime,
  financeUserId: formData.value.financeUserId,
  supplierId: formData.value.supplierId,
  accountId: formData.value.accountId,
  prepaymentPrice: Number(formData.value.prepaymentPrice || 0),
  remark: formData.value.remark
})

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const loadOptions = async () => {
  const [suppliers, accounts, users] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    AccountApi.getAccountSimpleList(),
    getSimpleUserList()
  ])
  supplierList.value = suppliers
  accountList.value = accounts
  userList.value = users
}

const titleMap: Record<FormType, string> = {
  create: '新建预付款',
  update: '编辑预付款',
  detail: '预付款详情'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type]
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    const [_, detail] = await Promise.all([
      loadOptions(),
      id ? FinancePrepaymentApi.getFinancePrepayment(id) : Promise.resolve(undefined)
    ])
    if (detail) {
      formData.value = normalizeFormData(detail)
    }
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    dialogLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const submitForm = async () => {
  if (submitLoading.value) {
    return
  }
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = buildSaveData()
    if (formType.value === 'create') {
      await FinancePrepaymentApi.createFinancePrepayment(payload)
      message.success('保存成功')
    } else {
      await FinancePrepaymentApi.updateFinancePrepayment(payload)
      message.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.finance-prepayment-form {
  background: #f8fafc;
}

.finance-prepayment-form__panel {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-prepayment-form__panel + .finance-prepayment-form__panel {
  margin-top: 16px;
}

.finance-prepayment-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-prepayment-form__remark {
  margin-bottom: 0;
}

.finance-prepayment-form__section-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.finance-prepayment-form__table-wrap {
  overflow-x: auto;
}

.finance-prepayment-form__table {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.finance-prepayment-form__amount {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .finance-prepayment-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-prepayment-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
