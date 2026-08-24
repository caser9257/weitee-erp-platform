<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1200px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="研发BOM编码" prop="bomCode">
            <el-input v-model="formData.bomCode" placeholder="请输入研发 BOM 编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成品" prop="productId">
            <el-select
              v-model="formData.productId"
              clearable
              filterable
              placeholder="请选择成品"
              style="width: 100%"
            >
              <el-option
                v-for="item in productOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="版本" prop="version">
            <el-input :model-value="versionDisplayText" disabled placeholder="请输入版本号，如 V1.0" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">设计物料</el-divider>

      <div class="mb-12px flex items-center justify-between">
        <span class="text-12px text-[var(--el-text-color-secondary)]">
          保存后为研发草稿，发布动作会单独生成制造 BOM 草稿
        </span>
        <el-button type="primary" plain @click="handleAddItem">
          <Icon icon="ep:plus" class="mr-5px" />
          新增物料
        </el-button>
      </div>

      <el-table :data="formData.items" border>
        <el-table-column type="expand" width="48">
          <template #default="{ row }">
            <div class="pl-32px pr-12px pb-12px">
              <div class="mb-8px flex items-center justify-between">
                <span class="text-12px text-[var(--el-text-color-secondary)]">替代料列表</span>
                <el-button type="primary" link @click="handleAddSubstitute(row)">
                  新增替代料
                </el-button>
              </div>
              <el-table :data="row.substitutes" border size="small">
                <el-table-column type="index" label="#" width="56" align="center" />
                <el-table-column label="替代物料" min-width="220">
                  <template #default="{ row: substitute }">
                    <el-select
                      v-model="substitute.substituteMaterialId"
                      clearable
                      filterable
                      placeholder="请选择替代物料"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="item in productOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="优先级" width="120" align="center">
                  <template #default="{ row: substitute }">
                    <el-input-number
                      v-model="substitute.priority"
                      :min="1"
                      :precision="0"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="替换比例" width="140" align="center">
                  <template #default="{ row: substitute }">
                    <el-input-number
                      v-model="substitute.replaceRatio"
                      :min="0.000001"
                      :precision="6"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="自动推荐" width="120" align="center">
                  <template #default="{ row: substitute }">
                    <el-switch v-model="substitute.enableAutoRecommend" />
                  </template>
                </el-table-column>
                <el-table-column label="排序" width="120" align="center">
                  <template #default="{ row: substitute }">
                    <el-input-number
                      v-model="substitute.sort"
                      :min="0"
                      :precision="0"
                      controls-position="right"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="180">
                  <template #default="{ row: substitute }">
                    <el-input v-model="substitute.remark" placeholder="请输入备注" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="96" align="center" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" @click="handleRemoveSubstitute(row, $index)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column label="物料" min-width="220">
          <template #default="{ row }">
            <el-select
              v-model="row.materialId"
              clearable
              filterable
              placeholder="请选择物料"
              style="width: 100%"
              @change="(value) => handleMaterialChange(row, value)"
            >
              <el-option
                v-for="item in productOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="供给方式" width="120" align="center">
          <template #default="{ row }">
            <dict-tag
              v-if="row.materialType !== undefined"
              :type="DICT_TYPE.ERP_SUPPLY_TYPE"
              :value="row.materialType"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="120" align="center">
          <template #default="{ row }">
            <span>{{ resolveUnitName(row.materialId) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用量" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.usageQty"
              :min="0.000001"
              :precision="6"
              controls-position="right"
              placeholder="请输入"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="损耗率" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.lossRate"
              :min="0"
              :precision="4"
              controls-position="right"
              placeholder="如 0.05"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="位号" width="180" align="center">
          <template #default="{ row }">
            <el-input v-model="row.referenceDesignator" placeholder="如 R1,C2,U3" />
          </template>
        </el-table-column>
        <el-table-column label="物料位置" width="200" align="center">
          <template #default="{ row }">
            <el-input v-model="row.position" placeholder="如 安装于电源外壳上" />
          </template>
        </el-table-column>
        <el-table-column label="提前期(天)" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.leadTimeDay"
              :min="0"
              :precision="0"
              controls-position="right"
              placeholder="请输入"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="排序" width="120" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="0"
              :precision="0"
              controls-position="right"
              placeholder="请输入"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.remark" placeholder="请输入备注" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="handleRemoveItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-form>

    <template #footer>
      <el-button type="primary" :loading="saveSubmitting" :disabled="!canSubmit" @click="submitForm">
        确定
      </el-button>
      <el-button :disabled="saveSubmitting" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from '@/hooks/web/useMessage'
import { RdBomApi, type RdBomVO } from '@/api/erp/rd/bom'
import { type ProductVO } from '@/api/erp/product/product'
import { DICT_TYPE } from '@/utils/dict'
import {
  buildBomPayload,
  createDefaultBomFormData,
  createEmptyBomItem,
  createEmptyBomSubstitute,
  getCanSubmitBomForm,
  type BomFormData,
  type BomItemFormData
} from './bomForm.helpers'

defineOptions({ name: 'RdBomForm' })

const props = defineProps<{
  productOptions: ProductVO[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const { t } = useI18n()

const dialogVisible = ref(false)
const formLoading = ref(false)
const saveSubmitting = ref(false)
const formType = ref<'create' | 'update'>('create')
const formRef = ref()
const formData = ref<BomFormData>(createDefaultBomFormData())

const formRules = reactive({
  bomCode: [{ required: true, message: '请输入研发 BOM 编码', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择成品', trigger: 'change' }]
})

const dialogTitle = computed(() => (formType.value === 'create' ? '新增研发BOM' : '编辑研发BOM'))
const canSubmit = computed(() => getCanSubmitBomForm(formData.value, saveSubmitting.value))
const versionDisplayText = computed(() => formData.value.version || '发布时自动生成')
const productMap = computed(() =>
  props.productOptions.reduce<Record<number, ProductVO>>((acc, item) => {
    acc[item.id] = item
    return acc
  }, {})
)

const resetForm = () => {
  formData.value = createDefaultBomFormData()
  formRef.value?.clearValidate()
}

const toFormData = (data: RdBomVO): BomFormData => ({
  id: data.id,
  bomCode: data.bomCode || '',
  productId: data.productId,
  version: data.version || '',
  status: data.status ?? 0,
  remark: data.remark || '',
  items:
    data.items?.length > 0
      ? data.items.map((item) => ({
          id: item.id,
          materialId: item.materialId,
          materialType: item.materialType,
          unitId: item.unitId,
          usageQty: item.usageQty,
          lossRate: item.lossRate,
          referenceDesignator: item.referenceDesignator || '',
          position: item.position || '',
          leadTimeDay: item.leadTimeDay,
          sort: item.sort,
          remark: item.remark || '',
          substitutes: item.substitutes?.map((substitute) => ({
            id: substitute.id,
            substituteMaterialId: substitute.substituteMaterialId,
            priority: substitute.priority,
            replaceRatio: substitute.replaceRatio,
            enableAutoRecommend: substitute.enableAutoRecommend,
            sort: substitute.sort,
            remark: substitute.remark || ''
          })) || []
        }))
      : [createEmptyBomItem()]
})

const handleMaterialChange = (row: BomItemFormData, materialId?: number) => {
  if (!materialId) {
    row.materialType = undefined
    row.unitId = undefined
    return
  }
  const product = productMap.value[materialId]
  row.materialType = product?.supplyType
  row.unitId = product?.unitId
}

const resolveUnitName = (materialId?: number) => {
  if (!materialId) {
    return ''
  }
  return productMap.value[materialId]?.unitName || ''
}

const handleAddItem = () => {
  formData.value.items.push({
    ...createEmptyBomItem(),
    sort: formData.value.items.length + 1
  })
}

const handleRemoveItem = (index: number) => {
  formData.value.items.splice(index, 1)
}

const handleAddSubstitute = (row: BomItemFormData) => {
  row.substitutes.push(createEmptyBomSubstitute())
}

const handleRemoveSubstitute = (row: BomItemFormData, index: number) => {
  row.substitutes.splice(index, 1)
}

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  formType.value = type
  resetForm()
  await nextTick()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await RdBomApi.getRdBom(id)
      formData.value = toFormData(data)
    } finally {
      formLoading.value = false
    }
  }
}

const submitForm = async () => {
  if (!canSubmit.value) {
    message.warning('请完善研发 BOM 基本信息和设计物料后再提交')
    return
  }
  await formRef.value.validate()
  saveSubmitting.value = true
  try {
    const payload = buildBomPayload(formData.value)
    if (formType.value === 'create') {
      await RdBomApi.createRdBom(payload)
      message.success(t('common.createSuccess'))
    } else {
      await RdBomApi.updateRdBom(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    saveSubmitting.value = false
  }
}

watch(dialogVisible, (visible) => {
  if (!visible) {
    formLoading.value = false
    saveSubmitting.value = false
    formType.value = 'create'
    resetForm()
  }
})

defineExpose({ open })
</script>
