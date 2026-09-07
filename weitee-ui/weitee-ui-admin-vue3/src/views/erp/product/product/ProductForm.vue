<!-- ERP 产品的新增/修改 -->
<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    :width="'min(1120px, 92vw)'"
    :fullscreen="true"
    :scroll="true"
    max-height="calc(100vh - 180px)"
    @closed="resetForm"
    class="product-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="loadingProduct"
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
            <span
              v-if="displayPrevMaterialCode"
              class="font-mono text-xs text-slate-400"
            >旧编码：{{ displayPrevMaterialCode }}</span>
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
                v-for="unit in baseUnitList"
                :key="unit.id"
                :label="unit.name"
                :value="unit.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="规格" prop="standard">
            <el-input
              v-model="formData.standard"
              placeholder="请输入规格"
              class="w-full"
              :disabled="frozenFieldsLocked"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="产品封装" prop="packaging">
            <el-input v-model="formData.packaging" placeholder="请输入产品封装" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="质量等级" prop="qualityGrade">
            <el-input v-model="formData.qualityGrade" placeholder="请输入质量等级" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="品牌/制造商" prop="brandManufacturer">
            <el-input
              v-model="formData.brandManufacturer"
              placeholder="请输入品牌/制造商"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="替代型号" prop="alternativeModel">
            <el-input
              v-model="formData.alternativeModel"
              placeholder="请输入替代型号"
              class="w-full"
            />
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
              <el-radio v-for="dict in statusOptions" :key="dict.value" :value="dict.value">
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

      <template v-if="canManageCadence">
        <el-divider content-position="left">PCB设计信息</el-divider>
        <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="PCB 元器件" prop="pcbComponent">
            <el-switch v-model="formData.pcbComponent" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="原理图库符号" prop="schematicPart">
            <el-input v-model="formData.schematicPart" placeholder="请输入原理图库符号" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="PCB封装" prop="pcbFootprint">
            <el-input v-model="formData.pcbFootprint" placeholder="请输入PCB封装" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="厂家型号" prop="manufacturerPartNumber">
            <el-input v-model="formData.manufacturerPartNumber" placeholder="请输入厂家型号" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="生命周期" prop="lifecycle">
            <el-input v-model="formData.lifecycle" placeholder="请输入生命周期" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="工作温度" prop="operatingTemperature">
            <el-input v-model="formData.operatingTemperature" placeholder="请输入工作温度" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="安装类型" prop="mountingType">
            <el-input v-model="formData.mountingType" placeholder="请输入安装类型" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="三维尺寸" prop="dimension">
            <el-input v-model="formData.dimension" placeholder="请输入三维尺寸" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="3D模型" prop="threeDLib">
            <el-input v-model="formData.threeDLib" placeholder="请输入3D模型" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="进口/替代物料" prop="importedOrReplacement">
            <el-input
              v-model="formData.importedOrReplacement"
              placeholder="请输入进口/替代物料"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="是否优选" prop="preferredPart">
            <el-switch v-model="formData.preferredPart" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="空置标志" prop="dnp">
            <el-switch v-model="formData.dnp" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="数据手册" prop="datasheet">
            <el-input v-model="formData.datasheet" placeholder="请输入数据手册地址或编号" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24">
          <el-form-item label="关键参数描述" prop="cadenceDescription">
            <el-input
              v-model="formData.cadenceDescription"
              type="textarea"
              :rows="2"
              placeholder="请输入关键参数描述"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="参数描述2" prop="secondDescription">
            <el-input v-model="formData.secondDescription" placeholder="请输入参数描述2" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="参数描述3" prop="thirdDescription">
            <el-input v-model="formData.thirdDescription" placeholder="请输入参数描述3" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="参数描述4" prop="fourthDescription">
            <el-input v-model="formData.fourthDescription" placeholder="请输入参数描述4" class="w-full" />
          </el-form-item>
        </el-col>
        </el-row>
      </template>

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
            <el-select
              v-model="formData.supplyType"
              placeholder="请选择供给方式"
              clearable
              class="w-full"
            >
              <el-option
                v-for="dict in supplyTypeOptions"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :xl="8">
          <el-form-item label="默认供应商" prop="defaultSupplierId">
            <SupplierRemoteSelect v-model="formData.defaultSupplierId" placeholder="输入至少 1 个字搜索供应商" />
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
      <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="submitForm"> 确定 </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import { ProductUnitApi, ProductUnitTypeEnum, ProductUnitVO } from '@/api/erp/product/unit'
import SupplierRemoteSelect from '@/components/SupplierRemoteSelect.vue'
import { CommonStatusEnum } from '@/utils/constants'
import { useUserStore } from '@/store/modules/user'
import { defaultProps, handleTree } from '@/utils/tree'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'

defineOptions({ name: 'ProductForm' })

const { t } = useI18n()
const message = useMessage()
const userStore = useUserStore()
const CADENCE_RD_DEPT_ID = 103
const CADENCE_FIELDS = [
  'pcbComponent', 'schematicPart', 'pcbFootprint', 'cadenceDescription',
  'manufacturerPartNumber', 'dimension', 'threeDLib', 'datasheet', 'lifecycle', 'preferredPart',
  'operatingTemperature', 'mountingType', 'dnp', 'importedOrReplacement',
  'secondDescription', 'thirdDescription', 'fourthDescription'
] as const satisfies readonly (keyof ProductVO)[]

const dialogVisible = ref(false)
const dialogTitle = ref('')
const loadingProduct = ref(false)
const submitting = ref(false)
const formType = ref('')
const formRef = ref()
const categoryList = ref<ProductCategoryVO[]>([])
const unitList = ref<ProductUnitVO[]>([])
// 产品记账单位只能是基本单位，辅助单位仅用于单据录入换算
const baseUnitList = computed(() =>
  unitList.value.filter((unit) => unit.unitType !== ProductUnitTypeEnum.AUXILIARY)
)
const statusOptions = computed(() => getIntDictOptions(DICT_TYPE.COMMON_STATUS))
const supplyTypeOptions = computed(() => getStrDictOptions(DICT_TYPE.ERP_SUPPLY_TYPE))

const createDefaultFormData = () => ({
  id: undefined,
  name: undefined,
  materialCode: undefined,
  prevMaterialCode: undefined,
  barCode: undefined,
  categoryId: undefined,
  unitId: undefined,
  status: CommonStatusEnum.ENABLE,
  standard: undefined,
  packaging: undefined,
  qualityGrade: undefined,
  brandManufacturer: undefined,
  alternativeModel: undefined,
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
  assetFlag: false,
  pcbComponent: false,
  schematicPart: undefined,
  pcbFootprint: undefined,
  cadenceDescription: undefined,
  manufacturerPartNumber: undefined,
  dimension: undefined,
  threeDLib: undefined,
  datasheet: undefined,
  lifecycle: undefined,
  preferredPart: undefined,
  operatingTemperature: undefined,
  mountingType: undefined,
  dnp: undefined,
  importedOrReplacement: undefined,
  secondDescription: undefined,
  thirdDescription: undefined,
  fourthDescription: undefined
})

const formData = ref(createDefaultFormData())
// 被 BOM 引用的物料：规格冻结（后端同步硬校验，此处仅联动禁用）；
// 物料编码已放开（编码沿革机制：改码写沿革表，历史单据/导入按旧码兜底命中）
const frozenFieldsLocked = computed(
  () => formType.value === 'update' && Boolean((formData.value as unknown as ProductVO).referencedByBom)
)
// 编辑回显：有改码历史时展示旧编码（业务事实）
const displayPrevMaterialCode = computed(() => {
  const prev = (formData.value as unknown as ProductVO).prevMaterialCode
  return formType.value === 'update' && prev ? prev : ''
})
// 编辑已生效物料：提交即进入修改审批（后端 /update 统一入口内部分流）
const isApprovedEdit = computed(
  () => formType.value === 'update' && (formData.value as unknown as ProductVO).auditStatus === 20
)
const canManageCadence = computed(() => userStore.user.deptId === CADENCE_RD_DEPT_ID)
const canSubmit = computed(() => !loadingProduct.value && !submitting.value)
const formRules = reactive({
  name: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  barCode: [{ required: true, message: '产品条码不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '产品分类不能为空', trigger: 'blur' }],
  unitId: [{ required: true, message: '单位不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '产品状态不能为空', trigger: 'blur' }]
})

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = t('action.' + type)
  resetForm()
  if (id) {
    loadingProduct.value = true
    try {
      formData.value = await ProductApi.getProduct(id)
    } finally {
      loadingProduct.value = false
    }
  }
  const categoryData = await ProductCategoryApi.getProductCategorySimpleList()
  categoryList.value = handleTree(categoryData, 'id', 'parentId')
  unitList.value = await ProductUnitApi.getProductUnitSimpleList()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!canSubmit.value) {
    return
  }
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = buildSubmitData()
    if (formType.value === 'create') {
      await ProductApi.createProduct(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct(data)
      if ((formData.value as unknown as ProductVO).auditStatus === 22) {
        // 两段式编辑中：保存写暂存，主表保持基线；提交审批由「提交审批」按钮触发
        message.success('修改已保存，提交审批后由负责人确认生效')
      } else if (isApprovedEdit.value) {
        message.success('修改申请已提交，审批通过前物料保持现值')
      } else {
        message.success(t('common.updateSuccess'))
      }
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
}

const buildSubmitData = () => {
  const data = { ...formData.value } as ProductVO
  if (!canManageCadence.value) {
    CADENCE_FIELDS.forEach((field) => {
      ;(data as Record<string, unknown>)[field] = undefined
    })
  }
  return data
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

@media (width <= 1024px) {
  .product-form {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }
  }
}

@media (width <= 768px) {
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
