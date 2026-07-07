<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="960px"
    class="invoice-form-dialog"
    destroy-on-close
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="108px">
      <div class="invoice-form-grid">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="formData.customerId" filterable clearable placeholder="请选择客户">
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单编号" prop="orderId">
          <el-input-number
            v-model="formData.orderId"
            :min="1"
            :controls="false"
            class="w-100%"
            placeholder="请输入订单编号"
            @change="loadUninvoicedItems"
          />
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceType">
          <el-select v-model="formData.invoiceType" placeholder="请选择发票类型">
            <el-option label="普通发票" value="NORMAL" />
            <el-option label="增值税专用发票" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="开票时间" prop="invoiceTime">
          <el-date-picker
            v-model="formData.invoiceTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-100%"
          />
        </el-form-item>
        <el-form-item label="发票抬头" prop="invoiceTitle">
          <el-input v-model="formData.invoiceTitle" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="纳税人识别号" prop="taxpayerNo">
          <el-input v-model="formData.taxpayerNo" maxlength="50" show-word-limit />
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
      </el-form-item>

      <div class="invoice-items-toolbar">
        <div class="invoice-items-toolbar__title">发票明细</div>
        <div class="invoice-items-toolbar__actions">
          <el-button :disabled="!formData.orderId" :loading="itemsLoading" @click="loadUninvoicedItems">
            拉取明细
          </el-button>
          <el-button type="primary" @click="addItem">新增明细</el-button>
        </div>
      </div>

      <el-table :data="formData.items" border class="invoice-items-table">
        <el-table-column label="产品编号" min-width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.productId" :min="1" :controls="false" class="w-100%" />
          </template>
        </el-table-column>
        <el-table-column label="产品名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.productName" maxlength="80" />
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.productSpec" maxlength="80" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="90">
          <template #default="{ row }">
            <el-input v-model="row.unit" maxlength="20" />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.count" :min="0.001" :precision="3" :controls="false" class="w-100%" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="130" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :min="0" :precision="2" :controls="false" class="w-100%" />
          </template>
        </el-table-column>
        <el-table-column label="税率" width="110" align="right">
          <template #default="{ row }">
            <el-input-number v-model="row.taxRate" :min="0" :max="100" :precision="2" :controls="false" class="w-100%" />
          </template>
        </el-table-column>
        <el-table-column label="价税合计" width="130" align="right">
          <template #default="{ row }">
            <span class="invoice-items-table__amount">{{ formatAmount(calcItemTotal(row)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="invoice-total-bar">
        <span>不含税 {{ formatAmount(summary.amountWithoutTax) }}</span>
        <span>税额 {{ formatAmount(summary.taxAmount) }}</span>
        <strong>价税合计 {{ formatAmount(summary.totalAmount) }}</strong>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import request from '@/config/axios'
import { CustomerApi, type CustomerVO } from '@/api/erp/sale/customer'

defineOptions({ name: 'ErpInvoiceForm' })

type FormType = 'create' | 'update'

interface InvoiceItemForm {
  productId?: number
  productName?: string
  productSpec?: string
  unit?: string
  count?: number
  price?: number
  taxRate?: number
  remark?: string
}

interface InvoiceFormData {
  id?: number
  customerId?: number
  orderId?: number
  invoiceType: string
  invoiceTitle?: string
  taxpayerNo?: string
  invoiceTime?: string
  remark?: string
  items: InvoiceItemForm[]
}

const emit = defineEmits<{
  success: []
}>()

const InvoiceApi = {
  getInvoice: async (id: number) => {
    return await request.get({ url: `/erp/invoice/get?id=${id}` })
  },
  getInvoiceItems: async (invoiceId: number) => {
    return await request.get({ url: `/erp/invoice/list-items?invoiceId=${invoiceId}` })
  },
  getUninvoicedItems: async (orderId: number) => {
    return await request.get({ url: `/erp/invoice/uninvoiced-items?orderId=${orderId}` })
  },
  createInvoice: async (data: InvoiceFormData) => {
    return await request.post({ url: '/erp/invoice/create', data })
  },
  updateInvoice: async (data: InvoiceFormData) => {
    return await request.put({ url: '/erp/invoice/update', data })
  }
}

const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const submitLoading = ref(false)
const itemsLoading = ref(false)
const formType = ref<FormType>('create')
const customerList = ref<CustomerVO[]>([])

const createEmptyItem = (): InvoiceItemForm => ({
  taxRate: 13,
  count: 1,
  price: 0
})

const createDefaultForm = (): InvoiceFormData => ({
  invoiceType: 'NORMAL',
  items: [createEmptyItem()]
})

const formData = reactive<InvoiceFormData>(createDefaultForm())

const formRules: FormRules<InvoiceFormData> = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  orderId: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }]
}

const dialogTitle = computed(() => (formType.value === 'create' ? '新开发票' : '编辑发票'))

const summary = computed(() => {
  return formData.items.reduce(
    (result, item) => {
      const count = Number(item.count || 0)
      const price = Number(item.price || 0)
      const taxRate = Number(item.taxRate || 0)
      const amount = count * price
      const taxAmount = amount * (taxRate / 100)
      result.amountWithoutTax += amount
      result.taxAmount += taxAmount
      result.totalAmount += amount + taxAmount
      return result
    },
    { amountWithoutTax: 0, taxAmount: 0, totalAmount: 0 }
  )
})

const resetForm = () => {
  Object.assign(formData, createDefaultForm())
}

const loadCustomerList = async () => {
  if (customerList.value.length > 0) {
    return
  }
  customerList.value = await CustomerApi.getCustomerSimpleList()
}

const normalizeItem = (item: any): InvoiceItemForm => ({
  productId: item.productId,
  productName: item.productName,
  productSpec: item.productSpec,
  unit: item.unit,
  count: Number(item.availableCount ?? item.count ?? 1),
  price: Number(item.price ?? 0),
  taxRate: Number(item.taxRate ?? 13),
  remark: item.remark
})

const open = async (type: FormType, id?: number) => {
  formType.value = type
  resetForm()
  dialogVisible.value = true
  await loadCustomerList()
  await nextTick()
  formRef.value?.clearValidate()

  if (type === 'update' && id) {
    const invoice = await InvoiceApi.getInvoice(id)
    const items = await InvoiceApi.getInvoiceItems(id)
    Object.assign(formData, {
      id: invoice.id,
      customerId: invoice.customerId,
      orderId: invoice.orderId,
      invoiceType: invoice.invoiceType || 'NORMAL',
      invoiceTitle: invoice.invoiceTitle,
      taxpayerNo: invoice.taxpayerNo,
      invoiceTime: invoice.invoiceTime,
      remark: invoice.remark,
      items: items?.length ? items.map(normalizeItem) : [createEmptyItem()]
    })
  }
}

const loadUninvoicedItems = async () => {
  if (!formData.orderId) {
    return
  }
  itemsLoading.value = true
  try {
    const items = await InvoiceApi.getUninvoicedItems(formData.orderId)
    formData.items = items?.length ? items.map(normalizeItem) : [createEmptyItem()]
  } finally {
    itemsLoading.value = false
  }
}

const addItem = () => {
  formData.items.push(createEmptyItem())
}

const removeItem = (index: number) => {
  if (formData.items.length === 1) {
    formData.items = [createEmptyItem()]
    return
  }
  formData.items.splice(index, 1)
}

const validateItems = () => {
  return formData.items.every((item) => item.productId && Number(item.count) > 0 && Number(item.price) >= 0)
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (!validateItems()) {
    ElMessage.warning('请完善发票明细')
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      ...formData,
      items: formData.items.map((item) => ({
        ...item,
        count: Number(item.count || 0),
        price: Number(item.price || 0),
        taxRate: Number(item.taxRate || 0)
      }))
    }
    if (formType.value === 'create') {
      await InvoiceApi.createInvoice(payload)
      ElMessage.success('创建成功')
    } else {
      await InvoiceApi.updateInvoice(payload)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const calcItemTotal = (item: InvoiceItemForm) => {
  const amount = Number(item.count || 0) * Number(item.price || 0)
  return amount + amount * (Number(item.taxRate || 0) / 100)
}

const formatAmount = (value: number) =>
  value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

defineExpose({ open })
</script>

<style scoped lang="scss">
.invoice-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--erp-space-3);
}

.invoice-items-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--erp-space-3);
  margin: var(--erp-space-3) 0 var(--erp-space-2);
}

.invoice-items-toolbar__title {
  color: var(--erp-slate-900);
  font-weight: 700;
}

.invoice-items-toolbar__actions {
  display: flex;
  gap: var(--erp-space-2);
}

.invoice-items-table {
  :deep(.el-input-number .el-input__inner) {
    text-align: right;
  }
}

.invoice-items-table__amount {
  font-family: var(--erp-font-mono);
  color: var(--erp-slate-900);
  font-weight: 700;
}

.invoice-total-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--erp-space-4);
  margin-top: var(--erp-space-3);
  padding: var(--erp-space-3);
  background: var(--erp-slate-50);
  border: 1px solid var(--erp-slate-100);
  border-radius: var(--erp-radius-lg);
  color: var(--erp-slate-700);

  strong {
    color: var(--erp-danger-600);
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .invoice-form-grid {
    grid-template-columns: 1fr;
  }

  .invoice-items-toolbar,
  .invoice-total-bar {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
