<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="min(1200px, calc(100vw - 24px))"
    class="invoice-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-position="top"
      label-width="auto"
      v-loading="formLoading"
      :disabled="disabled"
      class="invoice-form"
    >
      <!-- 基础信息区 -->
      <ContentWrap
        class="invoice-section"
        title="发票基础信息"
        :body-style="{ padding: '16px 18px 10px' }"
      >
        <template #header>
          <div class="invoice-section__header-extra">
            <el-tag effect="plain" size="small" type="info">
              明细 {{ formData.items?.length || 0 }} 行
            </el-tag>
          </div>
        </template>
        <el-row :gutter="20" class="invoice-grid">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                clearable
                filterable
                placeholder="请选择客户"
                class="!w-1/1"
                @change="handleCustomerChange"
              >
                <el-option
                  v-for="item in customerList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联订单" prop="orderId">
              <el-select
                v-model="formData.orderId"
                clearable
                filterable
                placeholder="请选择销售订单"
                class="!w-1/1"
                :disabled="!formData.customerId"
                @change="handleOrderChange"
              >
                <el-option
                  v-for="item in orderList"
                  :key="item.id"
                  :label="`${item.no} - ${formatMoney(item.totalPrice)}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select
                v-model="formData.invoiceType"
                placeholder="请选择发票类型"
                class="!w-1/1"
              >
                <el-option label="普通发票" value="NORMAL" />
                <el-option label="增值税专用发票" value="SPECIAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="发票抬头" prop="invoiceTitle">
              <el-input v-model="formData.invoiceTitle" placeholder="请输入发票抬头" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="纳税人识别号" prop="taxpayerNo">
              <el-input v-model="formData.taxpayerNo" placeholder="请输入纳税人识别号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="开票时间" prop="invoiceTime">
              <el-date-picker
                v-model="formData.invoiceTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择开票时间"
                class="!w-1/1"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="16">
            <el-form-item label="备注" prop="remark">
              <el-input
                type="textarea"
                v-model="formData.remark"
                :rows="2"
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </ContentWrap>

      <!-- 发票明细区 -->
      <ContentWrap
        class="invoice-section invoice-section--items"
        title="发票明细"
        :body-style="{ padding: '14px 16px 16px' }"
      >
        <InvoiceItemForm
          ref="itemFormRef"
          :items="formData.items"
          :disabled="disabled"
          :product-list="productList"
        />
      </ContentWrap>

      <!-- 金额汇总区 -->
      <ContentWrap
        class="invoice-section invoice-section--summary"
        title="金额汇总"
        :body-style="{ padding: '16px 18px 18px' }"
      >
        <div class="invoice-summary">
          <div class="invoice-summary__row">
            <div class="invoice-summary__field">
              <div class="invoice-summary__label">不含税金额</div>
              <div class="invoice-summary__value">{{ formatMoney(totalAmountWithoutTax) }}</div>
            </div>
            <div class="invoice-summary__field">
              <div class="invoice-summary__label">税额合计</div>
              <div class="invoice-summary__value">{{ formatMoney(totalTaxAmount) }}</div>
            </div>
            <div class="invoice-summary__field invoice-summary__field--total">
              <div class="invoice-summary__label">价税合计</div>
              <div class="invoice-summary__value invoice-summary__value--highlight">
                {{ formatMoney(totalAmountWithTax) }}
              </div>
            </div>
          </div>
        </div>
      </ContentWrap>
    </el-form>

    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading" v-if="!disabled">
        保存
      </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import InvoiceItemForm from './InvoiceItemForm.vue'
import * as CustomerApi from '@/api/erp/sale/customer'
import * as SaleOrderApi from '@/api/erp/sale/order'
import * as ProductApi from '@/api/erp/product/product'

defineOptions({ name: 'InvoiceForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('') // create / update

const formData = ref<any>({
  id: undefined,
  customerId: undefined,
  orderId: undefined,
  invoiceType: 'NORMAL',
  invoiceTitle: undefined,
  taxpayerNo: undefined,
  invoiceTime: undefined,
  remark: undefined,
  items: []
})

const formRules = reactive({
  customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
  orderId: [{ required: true, message: '关联订单不能为空', trigger: 'change' }],
  invoiceType: [{ required: true, message: '发票类型不能为空', trigger: 'change' }]
})

const disabled = computed(() => false)

// 列表数据
const customerList = ref<any[]>([])
const orderList = ref<any[]>([])
const productList = ref<any[]>([])

// 金额汇总
const totalAmountWithoutTax = computed(() => {
  return (formData.value.items || []).reduce((sum, item) => sum + Number(item.amount || 0), 0)
})
const totalTaxAmount = computed(() => {
  return (formData.value.items || []).reduce((sum, item) => sum + Number(item.taxAmount || 0), 0)
})
const totalAmountWithTax = computed(() => {
  return totalAmountWithoutTax.value + totalTaxAmount.value
})

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// API 接口
const InvoiceApi = {
  getInvoice: async (id: number) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: `/erp/invoice/get?id=${id}` })
  },
  getInvoiceItems: async (invoiceId: number) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: `/erp/invoice/list-items?invoiceId=${invoiceId}` })
  },
  getUninvoicedItems: async (orderId: number) => {
    const { request } = await import('@/config/axios')
    return await request.get({ url: `/erp/invoice/uninvoiced-items?orderId=${orderId}` })
  },
  createInvoice: async (data: any) => {
    const { request } = await import('@/config/axios')
    return await request.post({ url: '/erp/invoice/create', data })
  },
  updateInvoice: async (data: any) => {
    const { request } = await import('@/config/axios')
    return await request.put({ url: '/erp/invoice/update', data })
  }
}

/** 子表的表单 */
const itemFormRef = ref<any>()
const formRef = ref<any>()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新开发票' : '编辑发票'
  formType.value = type
  resetForm()

  // 加载基础数据
  formLoading.value = true
  try {
    customerList.value = await CustomerApi.getCustomerSimpleList()
    productList.value = await ProductApi.getProductSimpleList()

    if (id) {
      // 编辑模式：加载发票数据
      const invoice = await InvoiceApi.getInvoice(id)
      const items = await InvoiceApi.getInvoiceItems(id)
      formData.value = {
        ...invoice,
        items: items || []
      }
      // 加载该客户的订单列表
      if (invoice.customerId) {
        await loadOrderList(invoice.customerId)
      }
    }
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  await itemFormRef.value.validate()

  // 校验明细行不能为空
  if (!formData.value.items || formData.value.items.length === 0) {
    message.warning('请至少添加一条发票明细')
    return
  }

  formLoading.value = true
  try {
    // 计算汇总金额
    const data = {
      ...formData.value,
      amountWithoutTax: totalAmountWithoutTax.value,
      taxAmount: totalTaxAmount.value,
      totalAmount: totalAmountWithTax.value
    }

    if (formType.value === 'create') {
      await InvoiceApi.createInvoice(data)
      message.success(t('common.createSuccess'))
    } else {
      await InvoiceApi.updateInvoice(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    customerId: undefined,
    orderId: undefined,
    invoiceType: 'NORMAL',
    invoiceTitle: undefined,
    taxpayerNo: undefined,
    invoiceTime: undefined,
    remark: undefined,
    items: []
  }
  orderList.value = []
  formRef.value?.resetFields()
}

/** 处理客户变更 */
const handleCustomerChange = async () => {
  formData.value.orderId = undefined
  if (formData.value.customerId) {
    await loadOrderList(formData.value.customerId)
    // 自动填充客户发票信息
    const customer = customerList.value.find((c) => c.id === formData.value.customerId)
    if (customer) {
      formData.value.invoiceTitle = customer.invoiceTitle || customer.name
      formData.value.taxpayerNo = customer.taxpayerNo || ''
    }
  } else {
    orderList.value = []
  }
}

/** 加载客户订单列表 */
const loadOrderList = async (customerId: number) => {
  try {
    const res = await SaleOrderApi.getSaleOrderPage({
      customerId,
      pageNo: 1,
      pageSize: 100
    })
    orderList.value = res.list || []
  } catch {
    orderList.value = []
  }
}

/** 处理订单变更 */
const handleOrderChange = async () => {
  if (formData.value.orderId) {
    // 加载订单可开票明细（排除已开票数量）
    formLoading.value = true
    try {
      const uninvoicedItems = await InvoiceApi.getUninvoicedItems(formData.value.orderId)
      if (uninvoicedItems?.length) {
        formData.value.items = uninvoicedItems.map((item) => ({
          id: undefined,
          productId: item.productId,
          productName: item.productName,
          productSpec: item.productSpec || '',
          unit: item.unit || '',
          count: item.availableCount || 1,
          price: item.price || 0,
          amount: 0,
          taxRate: 13,
          taxAmount: 0,
          totalAmount: 0,
          remark: undefined
        }))
        message.success(`已加载 ${uninvoicedItems.length} 条可开票明细`)
      } else {
        formData.value.items = []
        message.warning('该订单无可开票明细')
      }
    } catch {
      // 加载失败时回退到订单原始明细
      try {
        const order = await SaleOrderApi.getSaleOrder(formData.value.orderId)
        if (order?.items?.length) {
          formData.value.items = order.items.map((item) => ({
            id: undefined,
            productId: item.productId,
            productName: item.productName,
            productSpec: item.productBarCode || '',
            unit: item.productUnitName || '',
            count: item.count || 1,
            price: item.productPrice || 0,
            amount: 0,
            taxRate: 13,
            taxAmount: 0,
            totalAmount: 0,
            remark: undefined
          }))
        }
      } catch {
        // 加载失败不阻塞
      }
    } finally {
      formLoading.value = false
    }
  }
}
</script>

<style scoped lang="scss">
.invoice-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: var(--erp-slate-600);
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }
  :deep(.el-form-item__content) {
    min-width: 0;
  }
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-textarea__inner),
  :deep(.el-input-number__wrapper) {
    min-height: 42px;
    border-radius: 10px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.84);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }
  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-textarea__inner:focus) {
    border-color: rgba(64, 158, 255, 0.55);
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.08);
  }
  :deep(.el-input.is-disabled .el-input__wrapper),
  :deep(.el-select.is-disabled .el-select__wrapper) {
    border-color: transparent;
    background: var(--erp-slate-100);
    color: var(--erp-slate-500);
    box-shadow: none;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }
  :deep(.el-textarea__inner) {
    min-height: 60px;
  }
}

.invoice-section {
  margin-bottom: 16px;
}

.invoice-section__header-extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.invoice-summary {
  .invoice-summary__row {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .invoice-summary__field {
    flex: 1;
    min-width: 140px;
    padding: 12px 16px;
    background: var(--erp-slate-50);
    border-radius: 10px;
    border: 1px solid var(--erp-slate-100);
  }

  .invoice-summary__field--total {
    background: var(--erp-primary-50);
    border-color: var(--erp-primary-200);
  }

  .invoice-summary__label {
    font-size: 12px;
    color: var(--erp-slate-500);
    margin-bottom: 4px;
  }

  .invoice-summary__value {
    font-size: 16px;
    font-weight: 700;
    color: var(--erp-slate-900);
    font-variant-numeric: tabular-nums;
  }

  .invoice-summary__value--highlight {
    font-size: 20px;
    color: var(--erp-primary-600);
  }
}
</style>
