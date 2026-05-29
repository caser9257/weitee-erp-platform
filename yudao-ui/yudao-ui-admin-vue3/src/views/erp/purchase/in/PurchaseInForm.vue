<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1440">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
      :disabled="disabled"
    >
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="入库单号" prop="no">
            <el-input disabled v-model="formData.no" placeholder="保存时自动生成" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="入库时间" prop="inTime">
            <el-date-picker
              v-model="formData.inTime"
              type="date"
              value-format="x"
              placeholder="选择入库时间"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="关联订单" prop="orderNo">
            <el-input v-model="formData.orderNo" readonly>
              <template #append>
                <el-button @click="openPurchaseOrderInEnableList">
                  <Icon icon="ep:search" /> 选择
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="供应商" prop="supplierId">
            <el-select
              v-model="formData.supplierId"
              clearable
              filterable
              disabled
              placeholder="请选择供应商"
              class="!w-1/1"
            >
              <el-option
                v-for="item in supplierList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="备注" prop="remark">
            <el-input
              type="textarea"
              v-model="formData.remark"
              :rows="1"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="附件" prop="fileUrl">
            <UploadFile :is-show-tip="false" v-model="formData.fileUrl" :limit="1" />
          </el-form-item>
        </el-col>
      </el-row>
      <ContentWrap>
        <el-tabs v-model="subTabsName" class="-mt-15px -mb-10px">
          <el-tab-pane label="入库产品清单" name="item">
            <PurchaseInItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
          </el-tab-pane>
        </el-tabs>
      </ContentWrap>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="优惠率（%）" prop="discountPercent">
            <el-input-number
              v-model="formData.discountPercent"
              controls-position="right"
              :min="0"
              :precision="2"
              placeholder="请输入优惠率"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="付款优惠" prop="discountPrice">
            <el-input disabled v-model="formData.discountPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="优惠后金额">
            <el-input
              disabled
              :model-value="formData.totalPrice - formData.otherPrice"
              :formatter="erpPriceInputFormatter"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="其它费用" prop="otherPrice">
            <el-input-number
              v-model="formData.otherPrice"
              controls-position="right"
              :min="0"
              :precision="2"
              placeholder="请输入其它费用"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="结算账户" prop="accountId">
            <el-select
              v-model="formData.accountId"
              clearable
              filterable
              placeholder="请选择结算账户"
              class="!w-1/1"
            >
              <el-option
                v-for="item in accountList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="应付金额">
            <el-input disabled v-model="formData.totalPrice" :formatter="erpPriceInputFormatter" />
          </el-form-item>
        </el-col>
      </el-row>
      <ContentWrap v-if="disabled" title="质检信息" class="mt-16px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="质检状态">
              <el-input :model-value="getQaStatusLabel(formData.qaStatus)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="质检时间">
              <el-input :model-value="formData.qaTime || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="质检人">
              <el-input :model-value="formData.qaUserNickname || formData.qaUserId || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合格总数">
              <el-input :model-value="formData.qaPassCount ?? 0" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不合格总数">
              <el-input :model-value="formData.qaRejectCount ?? 0" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="质检备注">
              <el-input :model-value="formData.qaRemark || '-'" type="textarea" :rows="2" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </ContentWrap>
      <ContentWrap v-if="disabled" title="入库信息" class="mt-16px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库处理状态">
              <el-input :model-value="getStockInStatusLabel(formData.stockInStatus)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="已入库数量">
              <el-input :model-value="formData.stockInCount ?? 0" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="剩余待入库">
              <el-input :model-value="formData.remainingStockInCount ?? 0" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-table :data="formData.stockExecuteList || []" border stripe>
          <el-table-column label="执行单号" prop="no" min-width="160" />
          <el-table-column label="执行时间" prop="createTime" min-width="180" />
          <el-table-column label="执行人" prop="creatorName" min-width="120" />
          <el-table-column label="执行明细" min-width="260">
            <template #default="{ row }">
              <div v-for="item in row.items || []" :key="item.id">
                {{ item.productName || '-' }} / {{ item.count || 0 }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="160" />
        </el-table>
      </ContentWrap>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading" v-if="!disabled">
        确 定
      </el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <PurchaseOrderInEnableList
    ref="purchaseOrderInEnableListRef"
    @success="handlePurchaseOrderChange"
  />
</template>
<script setup lang="ts">
import { PurchaseInApi, PurchaseInVO } from '@/api/erp/purchase/in'
import PurchaseInItemForm from './components/PurchaseInItemForm.vue'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { erpPriceInputFormatter, erpPriceMultiply } from '@/utils'
import PurchaseOrderInEnableList from '@/views/erp/purchase/order/components/PurchaseOrderInEnableList.vue'
import { PurchaseOrderApi, PurchaseOrderVO } from '@/api/erp/purchase/order'
import * as UserApi from '@/api/system/user'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'

/** ERP 采购入库表单 */
defineOptions({ name: 'PurchaseInForm' })

const { t } = useI18n()
const message = useMessage()
const QA_STATUS_LABELS: Record<number, string> = {
  10: '待质检',
  20: '部分合格',
  30: '全部合格',
  40: '全部不合格'
}
const STOCK_IN_STATUS_LABELS: Record<number, string> = {
  10: '待入库',
  15: '部分入库',
  20: '已入库',
  30: '无需入库'
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<any>({
  id: undefined,
  supplierId: undefined,
  accountId: undefined,
  inTime: undefined,
  remark: undefined,
  fileUrl: '',
  discountPercent: 0,
  discountPrice: 0,
  totalPrice: 0,
  otherPrice: 0,
  orderNo: undefined,
  qaStatus: undefined,
  qaTime: undefined,
  qaUserId: undefined,
  qaUserNickname: undefined,
  qaRemark: undefined,
  qaPassCount: undefined,
  qaRejectCount: undefined,
  stockInCount: undefined,
  remainingStockInCount: undefined,
  stockExecuteList: [],
  items: [],
  no: undefined
})
const formRules = reactive({
  supplierId: [{ required: true, message: '供应商不能为空', trigger: 'blur' }],
  inTime: [{ required: true, message: '入库时间不能为空', trigger: 'blur' }]
})
const disabled = computed(() => formType.value === 'detail')
const getQaStatusLabel = (qaStatus?: number) => {
  if (!qaStatus) {
    return '待质检'
  }
  return QA_STATUS_LABELS[qaStatus] || '待质检'
}
const getStockInStatusLabel = (stockInStatus?: number) => {
  if (!stockInStatus) {
    return '待质检'
  }
  return STOCK_IN_STATUS_LABELS[stockInStatus] || '待质检'
}
const formRef = ref()
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])
const userList = ref<UserApi.UserVO[]>([])

const subTabsName = ref('item')
const itemFormRef = ref()

watch(
  () => formData.value,
  (val) => {
    if (!val) {
      return
    }
    const totalPrice = val.items.reduce((prev, curr) => prev + curr.totalPrice, 0)
    const discountPrice =
      val.discountPercent != null ? erpPriceMultiply(totalPrice, val.discountPercent / 100.0) : 0
    formData.value.discountPrice = discountPrice
    formData.value.totalPrice = totalPrice - discountPrice + val.otherPrice
  },
  { deep: true }
)

const open = async (type: string, id?: number, purchaseOrderId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' && purchaseOrderId ? '生成采购入库' : t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await PurchaseInApi.getPurchaseIn(id)
    } finally {
      formLoading.value = false
    }
  }
  supplierList.value = await SupplierApi.getSupplierSimpleList()
  userList.value = await UserApi.getSimpleUserList()
  accountList.value = await AccountApi.getAccountSimpleList()
  const defaultAccount = accountList.value.find((item) => item.defaultStatus)
  if (formType.value === 'create' && defaultAccount) {
    formData.value.accountId = defaultAccount.id
  }
  if (formType.value === 'create' && purchaseOrderId) {
    await loadPurchaseOrder(purchaseOrderId)
  }
}
defineExpose({ open })

const purchaseOrderInEnableListRef = ref()
const openPurchaseOrderInEnableList = () => {
  purchaseOrderInEnableListRef.value.open()
}

const buildPurchaseInItemsFromOrder = (order: PurchaseOrderVO) => {
  return (order.items || [])
    .map((item) => {
      const totalCount = Number(item.count || 0)
      const inCount = Number(item.inCount || 0)
      const remainingCount = Math.max(totalCount - inCount, 0)
      return {
        ...item,
        id: undefined,
        orderItemId: item.id,
        totalCount,
        inCount,
        count: remainingCount
      }
    })
    .filter((item) => item.count > 0)
}

const applyPurchaseOrder = (order: PurchaseOrderVO) => {
  const items = buildPurchaseInItemsFromOrder(order)
  if (!items.length) {
    message.warning('当前采购订单已无可入库数量')
    formData.value.orderId = undefined
    formData.value.orderNo = undefined
    formData.value.supplierId = undefined
    formData.value.items = []
    return
  }
  formData.value.orderId = order.id
  formData.value.orderNo = order.no
  formData.value.supplierId = order.supplierId
  formData.value.accountId = order.accountId || formData.value.accountId
  formData.value.discountPercent = order.discountPercent ?? 0
  formData.value.remark = order.remark
  formData.value.fileUrl = order.fileUrl
  formData.value.items = items
}

const loadPurchaseOrder = async (orderId: number) => {
  formLoading.value = true
  try {
    const order = await PurchaseOrderApi.getPurchaseOrder(orderId)
    applyPurchaseOrder(order)
  } finally {
    formLoading.value = false
  }
}

const handlePurchaseOrderChange = (order: PurchaseOrderVO) => {
  applyPurchaseOrder(order)
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  await itemFormRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as PurchaseInVO
    if (formType.value === 'create') {
      await PurchaseInApi.createPurchaseIn(data)
      message.success(t('common.createSuccess'))
    } else {
      await PurchaseInApi.updatePurchaseIn(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    supplierId: undefined,
    accountId: undefined,
    inTime: undefined,
    remark: undefined,
    fileUrl: undefined,
    discountPercent: 0,
    discountPrice: 0,
    totalPrice: 0,
    otherPrice: 0,
    orderNo: undefined,
    qaStatus: undefined,
    qaTime: undefined,
    qaUserId: undefined,
    qaUserNickname: undefined,
    qaRemark: undefined,
    qaPassCount: undefined,
    qaRejectCount: undefined,
    stockInCount: undefined,
    remainingStockInCount: undefined,
    stockExecuteList: [],
    items: []
  }
  formRef.value?.resetFields()
}
</script>
