<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="单位名字" prop="name">
        <el-input v-model="formData.name" placeholder="请输入单位名字" />
      </el-form-item>
      <el-form-item label="单位类型" prop="unitType">
        <el-radio-group v-model="formData.unitType">
          <el-radio :value="ProductUnitTypeEnum.BASE">基本单位</el-radio>
          <el-radio :value="ProductUnitTypeEnum.AUXILIARY">辅助单位</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="formData.unitType === ProductUnitTypeEnum.AUXILIARY">
        <el-form-item label="基本单位" prop="baseUnitId">
          <el-select
            v-model="formData.baseUnitId"
            placeholder="请选择基本单位"
            filterable
            default-first-option
            class="w-full"
          >
            <el-option
              v-for="unit in selectableBaseUnits"
              :key="unit.id"
              :label="unit.name"
              :value="unit.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="换算率" prop="conversionRate">
          <el-input-number
            v-model="formData.conversionRate"
            :min="0.000001"
            :precision="6"
            :step="1"
            placeholder="请输入换算率"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label=" ">
          <span class="text-13px" style="color: var(--erp-slate-500)">
            1 {{ formData.name || '辅助单位' }} =
            <span class="font-mono font-bold">{{ conversionRateText }}</span>
            {{ baseUnitName || '基本单位' }}
          </span>
        </el-form-item>
      </template>
      <el-form-item label="单位状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="数量精度" prop="quantityPrecision">
        <el-input-number
          v-model="formData.quantityPrecision"
          :min="0"
          :max="6"
          :step="1"
          :precision="0"
          placeholder="请输入数量精度"
          class="w-full"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import {
  ProductUnitApi,
  ProductUnitTypeEnum,
  type ProductUnitVO
} from '@/api/erp/product/unit'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'

/** ERP 产品单位表单 */
defineOptions({ name: 'ProductUnitForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const unitSimpleList = ref<ProductUnitVO[]>([]) // 单位精简列表
const formData = ref({
  id: undefined,
  name: undefined,
  status: undefined,
  unitType: ProductUnitTypeEnum.BASE,
  baseUnitId: undefined,
  conversionRate: undefined,
  quantityPrecision: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '单位名字不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '单位状态不能为空', trigger: 'blur' }],
  unitType: [{ required: true, message: '单位类型不能为空', trigger: 'change' }],
  baseUnitId: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (formData.value.unitType === ProductUnitTypeEnum.AUXILIARY && !value) {
          callback(new Error('辅助单位必须选择基本单位'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  conversionRate: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (formData.value.unitType === ProductUnitTypeEnum.AUXILIARY && !value) {
          callback(new Error('辅助单位必须填写换算率'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  quantityPrecision: [{ required: true, message: '数量精度不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 可选基本单位：启用中的基本单位，编辑时排除自己（防御性过滤：非辅助单位即为基本单位） */
const selectableBaseUnits = computed(() =>
  unitSimpleList.value.filter(
    (unit) =>
      unit.unitType !== ProductUnitTypeEnum.AUXILIARY &&
      (formData.value.id == null || unit.id !== formData.value.id)
  )
)

/** 换算预览的基本单位名称 */
const baseUnitName = computed(
  () => unitSimpleList.value.find((unit) => unit.id === formData.value.baseUnitId)?.name
)

/** 换算率展示：去掉无意义尾零 */
const conversionRateText = computed(() => {
  const rate = Number(formData.value.conversionRate)
  return Number.isFinite(rate) ? rate.toLocaleString('zh-CN', { maximumFractionDigits: 6 }) : '?'
})

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载单位精简列表，用于基本单位下拉
  unitSimpleList.value = await ProductUnitApi.getProductUnitSimpleList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductUnitApi.getProductUnit(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = {
      ...(formData.value as unknown as ProductUnitVO),
      // 基本单位显式清空换算字段（null 序列化传输，避免 undefined 被丢弃）
      ...(formData.value.unitType === ProductUnitTypeEnum.BASE
        ? { baseUnitId: null, conversionRate: null }
        : {})
    }
    if (formType.value === 'create') {
      await ProductUnitApi.createProductUnit(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductUnitApi.updateProductUnit(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    status: CommonStatusEnum.ENABLE,
    unitType: ProductUnitTypeEnum.BASE,
    baseUnitId: undefined,
    conversionRate: undefined,
    quantityPrecision: 0
  }
  formRef.value?.resetFields()
}
</script>
