<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(1120px, 96vw)"
    scroll
    maxHeight="78vh"
  >
    <div class="finance-receipt-form">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        v-loading="dialogLoading"
        :disabled="formDisabled"
      >
        <div class="finance-receipt-form__panel">
          <div class="finance-receipt-form__grid">
            <el-form-item label="收款单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="收款时间" prop="receiptTime">
              <el-date-picker
                v-model="formData.receiptTime"
                type="date"
                value-format="x"
                placeholder="请选择收款时间"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                clearable
                filterable
                placeholder="请选择客户"
                class="!w-full"
                :disabled="customerLocked"
              >
                <el-option
                  v-for="item in customerList"
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
            <el-form-item label="收款账户" prop="accountId">
              <el-select
                v-model="formData.accountId"
                clearable
                filterable
                placeholder="请选择收款账户"
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
            <el-form-item label="优惠金额" prop="discountPrice">
              <el-input-number
                v-model="formData.discountPrice"
                controls-position="right"
                :precision="2"
                :min="0"
                :max="Math.max(Number(formData.totalPrice || 0), 0)"
                placeholder="请输入优惠金额"
                class="!w-full"
              />
            </el-form-item>
            <el-form-item label="合计收款" prop="totalPrice">
              <el-input
                v-model="formData.totalPrice"
                disabled
                :formatter="erpPriceInputFormatter"
                class="font-mono"
              />
            </el-form-item>
            <el-form-item label="实际收款" prop="receiptPrice">
              <el-input
                v-model="formData.receiptPrice"
                disabled
                :formatter="erpPriceInputFormatter"
                class="font-mono"
              />
            </el-form-item>
          </div>

          <div v-if="customerLocked && !detailMode" class="finance-receipt-form__hint">
            已添加收款明细后，客户不可再修改。
          </div>

          <el-form-item label="备注" prop="remark" class="finance-receipt-form__remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注"
            />
          </el-form-item>

          <el-form-item label="附件" prop="fileUrl" class="finance-receipt-form__upload">
            <UploadFile v-model="formData.fileUrl" :is-show-tip="false" :limit="1" />
          </el-form-item>
        </div>

        <div class="finance-receipt-form__panel">
          <div class="finance-receipt-form__section-title">收款明细</div>
          <FinanceReceiptItemForm
            ref="itemFormRef"
            :customer-id="formData.customerId"
            :items="formData.items"
            :disabled="formDisabled"
          />
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
        确定
      </el-button>
      <el-button :disabled="submitLoading" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { FinanceReceiptApi, FinanceReceiptItemVO, FinanceReceiptVO } from '@/api/erp/finance/receipt'
import FinanceReceiptItemForm from './components/FinanceReceiptItemForm.vue'
import { erpPriceInputFormatter } from '@/utils'
import * as UserApi from '@/api/system/user'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'

defineOptions({ name: 'FinanceReceiptForm' })

type FormType = 'create' | 'update' | 'detail'

interface FinanceReceiptFormData {
  id?: number
  no?: string
  customerId?: number
  customerName?: string
  accountId?: number
  accountName?: string
  financeUserId?: number
  financeUserName?: string
  receiptTime?: Date | string | number
  remark?: string
  fileUrl?: string
  totalPrice: number
  discountPrice: number
  receiptPrice: number
  items: FinanceReceiptItemVO[]
}

const { t } = useI18n()
const message = useMessage()

const createEmptyFormData = (): FinanceReceiptFormData => ({
  id: undefined,
  no: undefined,
  customerId: undefined,
  customerName: undefined,
  accountId: undefined,
  accountName: undefined,
  financeUserId: undefined,
  financeUserName: undefined,
  receiptTime: undefined,
  remark: '',
  fileUrl: '',
  totalPrice: 0,
  discountPrice: 0,
  receiptPrice: 0,
  items: []
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const formData = ref<FinanceReceiptFormData>(createEmptyFormData())
const formRef = ref<FormInstance>()
const itemFormRef = ref<InstanceType<typeof FinanceReceiptItemForm>>()

const customerList = ref<CustomerVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<UserApi.UserVO[]>([])

const detailMode = computed(() => formType.value === 'detail')
const formDisabled = computed(() => detailMode.value || dialogLoading.value || submitLoading.value)
const customerLocked = computed(() => !detailMode.value && formData.value.items.length > 0)
const submitDisabled = computed(() => detailMode.value || dialogLoading.value || submitLoading.value)

const validateDiscountPrice = (_rule: unknown, value: number, callback: (error?: Error) => void) => {
  const discountPrice = Number(value || 0)
  const totalPrice = Number(formData.value.totalPrice || 0)
  if (discountPrice < 0) {
    callback(new Error('优惠金额不能小于 0'))
    return
  }
  if (totalPrice >= 0 && discountPrice > totalPrice) {
    callback(new Error('优惠金额不能大于合计收款'))
    return
  }
  callback()
}

const formRules: FormRules<FinanceReceiptFormData> = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  receiptTime: [{ required: true, message: '请选择收款时间', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择收款账户', trigger: 'change' }],
  discountPrice: [{ validator: validateDiscountPrice, trigger: ['change', 'blur'] }]
}

const syncAmountFields = () => {
  const totalPrice = formData.value.items.reduce(
    (sum, item) => sum + Number(item.receiptPrice || 0),
    0
  )
  const discountPrice = Number(formData.value.discountPrice || 0)
  formData.value.totalPrice = Number(totalPrice.toFixed(2))
  formData.value.receiptPrice = Number((totalPrice - discountPrice).toFixed(2))
}

watch(
  () => formData.value.items,
  () => {
    syncAmountFields()
  },
  { deep: true, immediate: true }
)

watch(
  () => formData.value.discountPrice,
  () => {
    syncAmountFields()
  }
)

watch(
  () => formData.value.customerId,
  (customerId) => {
    const customer = customerList.value.find((item) => item.id === customerId)
    if (customer) {
      formData.value.customerName = customer.name
    }
  }
)

const normalizeItems = (items?: FinanceReceiptItemVO[]) =>
  (items || []).map((item) => ({
    ...item,
    receiptPrice: Number(item.receiptPrice || 0)
  }))

const normalizeFormData = (detail?: FinanceReceiptVO): FinanceReceiptFormData => ({
  id: detail?.id,
  no: detail?.no,
  customerId: detail?.customerId,
  customerName: detail?.customerName,
  accountId: detail?.accountId,
  accountName: detail?.accountName,
  financeUserId: detail?.financeUserId,
  financeUserName: detail?.financeUserName,
  receiptTime: detail?.receiptTime,
  remark: detail?.remark || '',
  fileUrl: detail?.fileUrl || '',
  totalPrice: Number(detail?.totalPrice || 0),
  discountPrice: Number(detail?.discountPrice || 0),
  receiptPrice: Number(detail?.receiptPrice || 0),
  items: normalizeItems(detail?.items)
})

const buildSaveData = (): FinanceReceiptVO => ({
  id: formData.value.id,
  customerId: formData.value.customerId,
  accountId: formData.value.accountId,
  financeUserId: formData.value.financeUserId,
  receiptTime: formData.value.receiptTime,
  remark: formData.value.remark,
  fileUrl: formData.value.fileUrl,
  totalPrice: Number(formData.value.totalPrice || 0),
  discountPrice: Number(formData.value.discountPrice || 0),
  receiptPrice: Number(formData.value.receiptPrice || 0),
  items: formData.value.items.map((item) => ({
    ...item,
    receiptPrice: Number(item.receiptPrice || 0)
  }))
})

const resetForm = async () => {
  formData.value = createEmptyFormData()
  await nextTick()
  formRef.value?.clearValidate()
}

const loadOptions = async () => {
  const [customers, users, accounts] = await Promise.all([
    CustomerApi.getCustomerSimpleList(),
    UserApi.getSimpleUserList(),
    AccountApi.getAccountSimpleList()
  ])
  customerList.value = customers
  userList.value = users
  accountList.value = accounts
}

const applyDefaultAccount = () => {
  if (formData.value.accountId) {
    return
  }
  const defaultAccount = accountList.value.find((item) => item.defaultStatus)
  if (defaultAccount) {
    formData.value.accountId = defaultAccount.id
  }
}

const titleMap: Record<FormType, string> = {
  create: '新增收款单',
  update: '编辑收款单',
  detail: '收款单详情'
}

const open = async (type: FormType, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = titleMap[type] || t(`action.${type}`)
  submitLoading.value = false
  await resetForm()
  dialogLoading.value = true
  try {
    const [_, detail] = await Promise.all([
      loadOptions(),
      id ? FinanceReceiptApi.getFinanceReceipt(id) : Promise.resolve(undefined)
    ])
    if (detail) {
      formData.value = normalizeFormData(detail)
    }
    applyDefaultAccount()
    syncAmountFields()
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
  if (!formData.value.items.length) {
    message.error('请至少添加一条收款明细')
    return
  }
  await itemFormRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = buildSaveData()
    if (formType.value === 'create') {
      await FinanceReceiptApi.createFinanceReceipt(payload)
      message.success(t('common.createSuccess'))
    } else {
      await FinanceReceiptApi.updateFinanceReceipt(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.finance-receipt-form {
  background: #f8fafc;
}

.finance-receipt-form__panel {
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(148, 163, 184, 0.08);
}

.finance-receipt-form__panel + .finance-receipt-form__panel {
  margin-top: 16px;
}

.finance-receipt-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-receipt-form__hint {
  margin: 4px 0 14px;
  color: #64748b;
  font-size: 12px;
}

.finance-receipt-form__remark :deep(.el-form-item__content),
.finance-receipt-form__upload :deep(.el-form-item__content) {
  display: block;
}

.finance-receipt-form__section-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
}

@media (max-width: 1280px) {
  .finance-receipt-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-receipt-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
