<!-- ERP 产品的新增/修改 -->
<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(1120px, 92vw)"
    :fullscreen="true"
    :scroll="true"
    max-height="calc(100vh - 180px)"
    class="product-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
      class="product-form"
    >
      <el-divider content-position="left">基础信息</el-divider>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入产品名称" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="物料编码" prop="materialCode">
            <el-input v-model="formData.materialCode" placeholder="请输入物料编码" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="条码" prop="barCode">
            <el-input v-model="formData.barCode" placeholder="请输入条码" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="分类" prop="categoryId">
            <el-tree-select
              v-model="formData.categoryId"
              :data="categoryList"
              :props="defaultProps"
              check-strictly
              default-expand-all
              placeholder="请选择分类"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="单位" prop="unitId">
            <el-select v-model="formData.unitId" clearable placeholder="请选择单位" class="w-full">
              <el-option
                v-for="unit in unitList"
                :key="unit.id"
                :label="unit.name"
                :value="unit.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="规格" prop="standard">
            <el-input v-model="formData.standard" placeholder="请输入规格" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="重量（kg）" prop="weight">
            <el-input-number
              v-model="formData.weight"
              placeholder="请输入重量（kg）"
              :min="0"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status" class="status-group">
              <el-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :value="dict.value"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">质量与追溯</el-divider>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="批次管理" prop="batchControlFlag">
            <el-switch v-model="formData.batchControlFlag" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="来料检验" prop="inspectionRequiredFlag">
            <el-switch v-model="formData.inspectionRequiredFlag" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="保质期（天）" prop="expiryDay">
            <el-input-number
              v-model="formData.expiryDay"
              placeholder="请输入保质期天数"
              :min="0"
              :precision="0"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">财务与供需</el-divider>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="采购价格" prop="purchasePrice">
            <el-input-number
              v-model="formData.purchasePrice"
              placeholder="请输入采购价，单位：元"
              :min="0"
              :precision="2"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="销售价格" prop="salePrice">
            <el-input-number
              v-model="formData.salePrice"
              placeholder="请输入销售价，单位：元"
              :min="0"
              :precision="2"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="最低价格" prop="minPrice">
            <el-input-number
              v-model="formData.minPrice"
              placeholder="请输入最低价，单位：元"
              :min="0"
              :precision="2"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="参与 MRP" prop="mrpEnable">
            <el-switch v-model="formData.mrpEnable" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="供给方式" prop="supplyType">
            <el-select v-model="formData.supplyType" placeholder="请选择供给方式" clearable class="w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_SUPPLY_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="默认供应商" prop="defaultSupplierId">
            <el-select
              v-model="formData.defaultSupplierId"
              placeholder="请选择默认供应商"
              clearable
              filterable
              class="w-full"
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
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="固定资产候选" prop="assetFlag">
            <el-switch v-model="formData.assetFlag" />
          </el-form-item>
        </el-col>
        <el-col :xs="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              type="textarea"
              v-model="formData.remark"
              :rows="4"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确定</el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import { ProductUnitApi, ProductUnitVO } from '@/api/erp/product/unit'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { CommonStatusEnum } from '@/utils/constants'
import { defaultProps, handleTree } from '@/utils/tree'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'ProductForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref()
const categoryList = ref<ProductCategoryVO[]>([])
const unitList = ref<ProductUnitVO[]>([])
const supplierList = ref<SupplierVO[]>([])

const createDefaultFormData = () => ({
  id: undefined,
  name: undefined,
  materialCode: undefined,
  barCode: undefined,
  categoryId: undefined,
  unitId: undefined,
  status: CommonStatusEnum.ENABLE,
  standard: undefined,
  remark: undefined,
  expiryDay: undefined,
  batchControlFlag: false,
  inspectionRequiredFlag: false,
  weight: undefined,
  purchasePrice: undefined,
  salePrice: undefined,
  minPrice: undefined,
  mrpEnable: true,
  supplyType: undefined,
  defaultSupplierId: undefined,
  defaultRouteId: undefined,
  assetFlag: false
})

const formData = ref(createDefaultFormData())
const formRules = reactive({
  name: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  barCode: [{ required: true, message: '产品条码不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '产品分类不能为空', trigger: 'blur' }],
  unitId: [{ required: true, message: '单位不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '产品状态不能为空', trigger: 'blur' }]
})

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductApi.getProduct(id)
    } finally {
      formLoading.value = false
    }
  }
  const categoryData = await ProductCategoryApi.getProductCategorySimpleList()
  categoryList.value = handleTree(categoryData, 'id', 'parentId')
  unitList.value = await ProductUnitApi.getProductUnitSimpleList()
  supplierList.value = await SupplierApi.getSupplierSimpleList()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as ProductVO
    if (formType.value === 'create') {
      await ProductApi.createProduct(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.product-form {
  :deep(.el-form-item__content) {
    min-width: 0;
  }

  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-tree-select),
  :deep(.el-input-number) {
    width: 100%;
  }
}

.status-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

@media (max-width: 1024px) {
  .product-form {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }
  }
}

@media (max-width: 768px) {
  .product-form {
    :deep(.el-form-item) {
      margin-bottom: 16px;
    }

    :deep(.el-form-item__label) {
      padding-bottom: 6px;
      line-height: 1.4;
    }

    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }
  }
}
</style>
