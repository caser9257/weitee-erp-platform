<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="720px"
    destroy-on-close
    class="finance-prepayment-form"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :disabled="formType === 'detail'"
      label-width="96px"
    >
      <div class="finance-prepayment-form__grid">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" filterable clearable placeholder="请选择供应商">
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结算账户" prop="accountId">
          <el-select v-model="formData.accountId" filterable clearable placeholder="请选择结算账户">
            <el-option
              v-for="item in accountList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预付时间" prop="prepaymentTime">
          <el-date-picker
            v-model="formData.prepaymentTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-100%"
          />
        </el-form-item>
        <el-form-item label="财务人员" prop="financeUserId">
          <el-select v-model="formData.financeUserId" filterable clearable placeholder="请选择财务人员">
            <el-option
              v-for="item in userList"
              :key="item.id"
              :label="item.nickname"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预付金额" prop="prepaymentPrice">
          <el-input-number
            v-model="formData.prepaymentPrice"
            :min="0.01"
            :precision="2"
            :controls="false"
            class="w-100%"
          />
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">{{ formType === 'detail' ? '关闭' : '取消' }}</el-button>
      <el-button v-if="formType !== 'detail'" type="primary" :loading="submitLoading" @click="submitForm">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import {
  FinancePrepaymentApi,
  type ErpFinancePrepaymentVO
} from '@/api/erp/finance/prepayment'

defineOptions({ name: 'ErpFinancePrepaymentForm' })

type FormType = 'create' | 'update' | 'detail'
type PrepaymentFormData = Omit<ErpFinancePrepaymentVO, 'prepaymentTime'> & {
  prepaymentTime?: string
}

const emit = defineEmits<{
  success: []
}>()

const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formType = ref<FormType>('create')
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<SimpleUserVO[]>([])

const createDefaultForm = (): PrepaymentFormData => ({
  prepaymentTime: '',
  supplierId: undefined,
  accountId: undefined,
  financeUserId: undefined,
  prepaymentPrice: undefined,
  remark: ''
})

const formData = reactive<PrepaymentFormData>(createDefaultForm())

const formRules: FormRules<PrepaymentFormData> = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择结算账户', trigger: 'change' }],
  prepaymentTime: [{ required: true, message: '请选择预付时间', trigger: 'change' }],
  prepaymentPrice: [{ required: true, message: '请输入预付金额', trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  if (formType.value === 'detail') return '预付款详情'
  return formType.value === 'create' ? '新增预付款' : '编辑预付款'
})

const resetForm = () => {
  Object.assign(formData, createDefaultForm())
}

const loadOptions = async () => {
  if (supplierList.value.length && accountList.value.length && userList.value.length) {
    return
  }
  const [suppliers, accounts, users] = await Promise.all([
    SupplierApi.getSupplierSimpleList(),
    AccountApi.getAccountSimpleList(),
    getSimpleUserList()
  ])
  supplierList.value = suppliers
  accountList.value = accounts
  userList.value = users
}

const open = async (type: FormType, id?: number) => {
  formType.value = type
  resetForm()
  dialogVisible.value = true
  await loadOptions()
  await nextTick()
  formRef.value?.clearValidate()

  if (id) {
    const data = await FinancePrepaymentApi.getFinancePrepayment(id)
    Object.assign(formData, data || {})
  }
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await FinancePrepaymentApi.createFinancePrepayment(formData)
      ElMessage.success('创建成功')
    } else {
      await FinancePrepaymentApi.updateFinancePrepayment(formData)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.finance-prepayment-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--erp-space-3);
}

@media (max-width: 768px) {
  .finance-prepayment-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
