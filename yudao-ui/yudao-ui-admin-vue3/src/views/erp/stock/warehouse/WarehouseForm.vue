<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="min(1040px, calc(100vw - 32px))"
    scroll
    maxHeight="78vh"
    :close-on-click-modal="!disabled"
    :close-on-press-escape="!disabled"
    @closed="handleDialogClosed"
  >
    <div class="warehouse-form">
      <el-alert
        v-if="detailLoadError"
        type="error"
        :closable="false"
        show-icon
        class="warehouse-form__alert"
      >
        {{ detailLoadError }}
      </el-alert>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        v-loading="detailLoading"
        :disabled="disabled"
        class="warehouse-form__base"
      >
        <div class="warehouse-form__grid">
          <el-form-item label="仓库名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入仓库名称" />
          </el-form-item>
          <el-form-item label="所属分类" prop="categoryId">
            <el-select
              v-model="formData.categoryId"
              filterable
              clearable
              placeholder="请选择所属分类"
              class="warehouse-form__field"
              :loading="categoryLoading"
            >
              <el-option
                v-for="item in categoryOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="仓库状态" prop="status">
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
          <el-form-item label="仓库地址" prop="address">
            <el-input v-model="formData.address" placeholder="请输入仓库地址" />
          </el-form-item>
          <el-form-item label="负责人" prop="principal">
            <el-input v-model="formData.principal" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="仓储费" prop="warehousePrice">
            <el-input-number
              v-model="formData.warehousePrice"
              :min="0"
              :precision="2"
              class="warehouse-form__field"
            />
          </el-form-item>
          <el-form-item label="搬运费" prop="truckagePrice">
            <el-input-number
              v-model="formData.truckagePrice"
              :min="0"
              :precision="2"
              class="warehouse-form__field"
            />
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number
              v-model="formData.sort"
              :precision="0"
              class="warehouse-form__field"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="warehouse-form__remark">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
    </div>

    <template #footer>
      <div class="warehouse-form__footer">
        <el-button
          v-if="detailLoadError"
          :disabled="detailLoading || submitLoading"
          @click="reloadDialogData"
        >
          重新加载
        </el-button>
        <el-button :disabled="detailLoading || submitLoading" @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          :disabled="detailLoading || !!detailLoadError"
          @click="submitForm"
        >
          确定
        </el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { WarehouseCategoryApi, WarehouseCategoryVO } from '@/api/erp/stock/warehouse-category'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'WarehouseForm' })

type WarehouseFormData = Partial<WarehouseVO>

const { t } = useI18n()
const message = useMessage()
const currentWarehouseId = ref<number>()

const createDefaultFormData = (): WarehouseFormData => ({
  id: undefined,
  name: undefined,
  categoryId: undefined,
  address: undefined,
  sort: undefined,
  remark: undefined,
  principal: undefined,
  warehousePrice: undefined,
  truckagePrice: undefined,
  status: CommonStatusEnum.ENABLE,
  defaultStatus: false
})

const dialogVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const requestToken = ref(0)
const detailLoading = ref(false)
const submitLoading = ref(false)
const categoryLoading = ref(false)
const detailLoadError = ref('')
const formData = ref<WarehouseFormData>(createDefaultFormData())
const formRef = ref()
const categoryOptions = ref<WarehouseCategoryVO[]>([])

const dialogTitle = computed(() => (formType.value === 'create' ? '新增仓库' : '编辑仓库'))
const disabled = computed(() => detailLoading.value || submitLoading.value || categoryLoading.value)

const formRules = reactive({
  name: [{ required: true, message: '仓库名称不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '仓库状态不能为空', trigger: 'blur' }]
})

const isActiveRequest = (token: number) => token === requestToken.value && dialogVisible.value

const resetFormState = () => {
  detailLoading.value = false
  submitLoading.value = false
  detailLoadError.value = ''
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

const loadCategoryOptions = async () => {
  categoryLoading.value = true
  try {
    const data = await WarehouseCategoryApi.getWarehouseCategorySimpleList()
    categoryOptions.value = data || []
  } finally {
    categoryLoading.value = false
  }
}

const loadDetail = async (id: number, token: number) => {
  detailLoading.value = true
  detailLoadError.value = ''
  try {
    const detail = await WarehouseApi.getWarehouse(id)
    if (!isActiveRequest(token)) {
      return
    }
    formData.value = {
      ...createDefaultFormData(),
      ...detail
    }
  } catch (error: any) {
    if (isActiveRequest(token)) {
      detailLoadError.value = error?.message || '详情加载失败，请重试'
      message.error(detailLoadError.value)
    }
  } finally {
    if (isActiveRequest(token)) {
      detailLoading.value = false
    }
  }
}

const reloadDialogData = async () => {
  if (!currentWarehouseId.value) {
    return
  }
  requestToken.value += 1
  await loadDetail(currentWarehouseId.value, requestToken.value)
}

const open = async (type: 'create' | 'update', id?: number) => {
  requestToken.value += 1
  const token = requestToken.value
  resetFormState()
  formType.value = type
  currentWarehouseId.value = id
  dialogVisible.value = true
  await loadCategoryOptions()

  if (!id) {
    return
  }

  await loadDetail(id, token)
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (detailLoading.value || submitLoading.value) {
    return
  }

  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const payload = formData.value as WarehouseVO
    if (formType.value === 'create') {
      await WarehouseApi.createWarehouse(payload)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseApi.updateWarehouse(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '保存失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClosed = () => {
  requestToken.value += 1
  currentWarehouseId.value = undefined
  resetFormState()
  categoryOptions.value = []
}
</script>

<style scoped>
.warehouse-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.warehouse-form__alert {
  margin-bottom: 4px;
}

.warehouse-form__base {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  padding: 18px;
  background: #ffffff;
}

.warehouse-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.warehouse-form__field {
  width: 100%;
}

.warehouse-form__remark {
  grid-column: 1 / -1;
}

.warehouse-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

@media (max-width: 767px) {
  .warehouse-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
