<template>
  <Dialog title="入库质检" v-model="dialogVisible" width="1100">
    <div v-loading="loading">
      <el-alert
        class="mb-16px"
        type="info"
        :closable="false"
        show-icon
        title="审批通过后，物料先进入待质检状态。质检提交后会进入待入库确认，只有仓库最终确认后才会正式入库。"
      />
      <el-descriptions :column="2" border class="mb-16px">
        <el-descriptions-item label="入库单号">{{ formData?.no || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购订单">{{ formData?.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ formData?.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="本单总数">
          {{ erpCountInputFormatter(formData?.totalCount) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="质检备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入本次质检结论或补充说明"
          />
        </el-form-item>
      </el-form>
      <el-table :data="formData.items" border>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="productName" label="产品" min-width="180" />
        <el-table-column prop="productBarCode" label="条码" min-width="140" />
        <el-table-column prop="productUnitName" label="单位" width="90" align="center" />
        <el-table-column label="到货数量" width="120" align="right">
          <template #default="{ row }">
            {{ erpCountInputFormatter(row.count) }}
          </template>
        </el-table-column>
        <el-table-column label="合格数量" width="170">
          <template #default="{ row }">
            <el-input-number
              v-model="row.qaPassCount"
              controls-position="right"
              :min="0"
              :max="Number(row.count || 0)"
              :step="getQuantityStep(row.productId)"
              :precision="getQuantityPrecision(row.productId)"
              class="!w-100%"
              @change="handlePassCountChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="不合格数量" width="140" align="right">
          <template #default="{ row }">
            {{ erpCountInputFormatter(row.qaRejectCount) }}
          </template>
        </el-table-column>
        <el-table-column label="质检备注" min-width="220">
          <template #default="{ row }">
            <el-input v-model="row.qaRemark" placeholder="可填写异常说明" />
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submit">提交质检</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import {
  PurchaseInApi,
  PurchaseInQualityCheckReqVO,
  PurchaseInVO
} from '@/api/erp/purchase/in'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { erpCountInputFormatter } from '@/utils'
import {
  getProductQuantityPrecision,
  getProductQuantityStep,
  roundQuantityByPrecision
} from '@/utils/erpQuantityPrecision'

defineOptions({ name: 'PurchaseInQualityCheckDialog' })

type PurchaseInQualityCheckForm = {
  id?: number
  no?: string
  orderNo?: string
  supplierName?: string
  totalCount?: number
  remark?: string
  items: Array<{
    id?: number
    productId?: number
    productName?: string
    productBarCode?: string
    productUnitName?: string
    count?: number
    qaPassCount?: number
    qaRejectCount?: number
    qaRemark?: string
  }>
}

const message = useMessage()
const dialogVisible = ref(false)
const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const productList = ref<ProductVO[]>([])
const formData = ref<PurchaseInQualityCheckForm>({
  items: []
})

const emit = defineEmits(['success'])

const resetForm = () => {
  formData.value = {
    items: []
  }
}

const normalizeCount = (value: number | undefined) => {
  return Number(value || 0)
}

const getQuantityPrecision = (productId?: number) => {
  return getProductQuantityPrecision(productList.value, productId)
}

const getQuantityStep = (productId?: number) => {
  return getProductQuantityStep(productList.value, productId)
}

const roundCount = (value: number, productId?: number) => {
  return roundQuantityByPrecision(value, getQuantityPrecision(productId))
}

const handlePassCountChange = (row: PurchaseInQualityCheckForm['items'][number]) => {
  const totalCount = normalizeCount(row.count)
  let qaPassCount = normalizeCount(row.qaPassCount)
  if (qaPassCount < 0) {
    qaPassCount = 0
  }
  if (qaPassCount > totalCount) {
    qaPassCount = totalCount
  }
  row.qaPassCount = roundCount(qaPassCount, row.productId)
  row.qaRejectCount = roundCount(totalCount - qaPassCount, row.productId)
}

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  resetForm()
  try {
    const [data] = await Promise.all([
      PurchaseInApi.getPurchaseIn(id) as Promise<PurchaseInVO>,
      ensureProductList()
    ])
    formData.value = {
      id: data.id,
      no: data.no,
      orderNo: data.orderNo,
      supplierName: data.supplierName,
      totalCount: data.totalCount,
      remark: data.qaRemark,
      items: (data.items || []).map((item) => ({
        id: item.id,
        productId: item.productId,
        productName: item.productName,
        productBarCode: item.productBarCode,
        productUnitName: item.productUnitName,
        count: item.count,
        qaPassCount: item.qaPassCount ?? item.count ?? 0,
        qaRejectCount: item.qaRejectCount ?? 0,
        qaRemark: item.qaRemark
      }))
    }
    formData.value.items.forEach((item) => handlePassCountChange(item))
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!formData.value.id || submitLoading.value) {
    return
  }
  const invalidItem = formData.value.items.find((item) => {
    const totalCount = normalizeCount(item.count)
    const qaPassCount = normalizeCount(item.qaPassCount)
    const qaRejectCount = normalizeCount(item.qaRejectCount)
    const totalPrecision = getQuantityPrecision(item.productId)
    return (
      qaPassCount < 0 ||
      qaRejectCount < 0 ||
      roundQuantityByPrecision(qaPassCount + qaRejectCount, totalPrecision) !==
        roundQuantityByPrecision(totalCount, totalPrecision)
    )
  })
  if (invalidItem) {
    message.warning('请检查质检数量，合格数与不合格数之和必须等于到货数量')
    return
  }
  submitLoading.value = true
  try {
    const payload: PurchaseInQualityCheckReqVO = {
      id: formData.value.id,
      remark: formData.value.remark,
      items: formData.value.items.map((item) => ({
        id: item.id!,
        qaPassCount: roundCount(normalizeCount(item.qaPassCount), item.productId),
        qaRejectCount: roundCount(normalizeCount(item.qaRejectCount), item.productId),
        qaRemark: item.qaRemark
      }))
    }
    await PurchaseInApi.qualityCheckPurchaseIn(payload)
    message.success('质检提交成功，等待仓库确认入库')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const ensureProductList = async () => {
  if (productList.value.length) {
    return
  }
  productList.value = await ProductApi.getProductSimpleList()
}

defineExpose({ open })
</script>
