<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="1240"
    @closed="handleDialogClosed"
  >
    <div class="stock-check-form">
      <el-alert
        v-if="detailLoadError"
        type="error"
        :closable="false"
        show-icon
        class="stock-check-form__alert"
      >
        {{ detailLoadError }}
      </el-alert>

      <div class="stock-check-form__scroll">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          v-loading="detailLoading"
          :disabled="disabled"
          class="stock-check-form__base"
        >
          <div class="stock-check-form__grid">
            <el-form-item label="盘点单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="盘点时间" prop="checkTime">
              <el-date-picker
                v-model="formData.checkTime"
                type="date"
                value-format="x"
                placeholder="请选择盘点时间"
                class="stock-check-form__field"
              />
            </el-form-item>
            <el-form-item label="附件" prop="fileUrl">
              <UploadFile
                v-model="formData.fileUrl"
                :is-show-tip="false"
                :limit="1"
                :disabled="disabled"
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="stock-check-form__remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
              />
            </el-form-item>
          </div>
        </el-form>

        <section class="stock-check-form__section">
          <div class="stock-check-form__section-title">盘点产品清单</div>
          <StockCheckItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
        </section>
      </div>
    </div>

    <template #footer>
      <div class="stock-check-form__footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          v-if="!isDetailMode"
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
import { StockCheckApi, StockCheckVO } from '@/api/erp/stock/check'
import StockCheckItemForm from './components/StockCheckItemForm.vue'

defineOptions({ name: 'StockCheckForm' })

type StockCheckItemRow = {
  id?: number
  warehouseId?: number
  productId?: number
  productName?: string
  productUnitName?: string
  productBarCode?: string
  productPrice?: number
  stockCount?: number
  actualCount?: number
  count?: number
  totalPrice?: number
  remark?: string
}

type StockCheckFormData = Partial<StockCheckVO> & {
  no?: string
  checkTime?: string | number
  remark?: string
  fileUrl?: string
  items: StockCheckItemRow[]
}

const { t } = useI18n()
const message = useMessage()

const createDefaultFormData = (): StockCheckFormData => ({
  id: undefined,
  no: '',
  checkTime: undefined,
  remark: undefined,
  fileUrl: '',
  items: []
})

const dialogVisible = ref(false)
const formType = ref<'create' | 'update' | 'detail'>('create')
const detailLoading = ref(false)
const submitLoading = ref(false)
const detailLoadError = ref('')
const formData = ref<StockCheckFormData>(createDefaultFormData())
const formRef = ref()
const itemFormRef = ref()

const dialogTitle = computed(() => t('action.' + formType.value))
const isDetailMode = computed(() => formType.value === 'detail')
const disabled = computed(() => isDetailMode.value || detailLoading.value || submitLoading.value)

const formRules = reactive({
  checkTime: [{ required: true, message: '盘点时间不能为空', trigger: 'blur' }]
})

const resetFormState = () => {
  detailLoading.value = false
  submitLoading.value = false
  detailLoadError.value = ''
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  itemFormRef.value?.clearValidate?.()
}

const open = async (type: 'create' | 'update' | 'detail', id?: number) => {
  resetFormState()
  formType.value = type
  dialogVisible.value = true

  if (!id) {
    return
  }

  detailLoading.value = true
  try {
    formData.value = {
      ...createDefaultFormData(),
      ...(await StockCheckApi.getStockCheck(id))
    }
    itemFormRef.value?.clearValidate?.()
  } catch (error: any) {
    detailLoadError.value = error?.message || '详情加载失败，请重试'
    message.error(detailLoadError.value)
  } finally {
    detailLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (detailLoading.value || submitLoading.value) {
    return
  }

  await formRef.value?.validate()
  await itemFormRef.value?.validate()

  submitLoading.value = true
  try {
    const payload = formData.value as StockCheckVO
    if (formType.value === 'create') {
      await StockCheckApi.createStockCheck(payload)
      message.success(t('common.createSuccess'))
    } else {
      await StockCheckApi.updateStockCheck(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClosed = () => {
  resetFormState()
}
</script>

<style scoped>
.stock-check-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-check-form__alert {
  margin-bottom: 4px;
}

.stock-check-form__scroll {
  max-height: min(72vh, 860px);
  overflow-y: auto;
  padding-right: 4px;
}

.stock-check-form__base,
.stock-check-form__section {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  padding: 18px;
  background: rgba(248, 250, 252, 0.68);
}

.stock-check-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stock-check-form__field {
  width: 100%;
}

.stock-check-form__remark {
  grid-column: 1 / -1;
}

.stock-check-form__section-title {
  margin-bottom: 14px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.stock-check-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

@media (max-width: 1279px) {
  .stock-check-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .stock-check-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
