<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="1180"
    @closed="handleDialogClosed"
  >
    <div class="stock-in-form">
      <el-alert
        v-if="detailLoadError"
        type="error"
        :closable="false"
        show-icon
        class="stock-in-form__alert"
      >
        {{ detailLoadError }}
      </el-alert>

      <div class="stock-in-form__scroll">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          v-loading="detailLoading"
          :disabled="disabled"
          class="stock-in-form__base"
        >
          <div class="stock-in-form__grid">
            <el-form-item label="入库单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="入库时间" prop="inTime">
              <el-date-picker
                v-model="formData.inTime"
                type="date"
                value-format="x"
                placeholder="请选择入库时间"
                class="stock-in-form__field"
              />
            </el-form-item>
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="formData.supplierId"
                clearable
                filterable
                placeholder="请选择供应商"
                class="stock-in-form__field"
              >
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="附件" prop="fileUrl">
              <UploadFile
                v-model="formData.fileUrl"
                :is-show-tip="false"
                :limit="1"
                :disabled="disabled"
              />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="stock-in-form__remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
              />
            </el-form-item>
          </div>
        </el-form>

        <section class="stock-in-form__section">
          <div class="stock-in-form__section-title">入库产品清单</div>
          <StockInItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
        </section>
      </div>
    </div>

    <template #footer>
      <div class="stock-in-form__footer">
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
import { normalizeItemsForUnitInput } from '@/utils/erpUnitConversion'
import { StockInApi, StockInVO } from '@/api/erp/stock/in'
import StockInItemForm from './components/StockInItemForm.vue'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'

defineOptions({ name: 'StockInForm' })

type StockInFormData = Partial<StockInVO> & {
  no?: string
  fileUrl?: string
  items: any[]
}

const { t } = useI18n()
const message = useMessage()

const createDefaultFormData = (): StockInFormData => ({
  id: undefined,
  no: '',
  supplierId: undefined,
  inTime: undefined,
  remark: undefined,
  fileUrl: '',
  items: []
})

const dialogVisible = ref(false)
const formType = ref<'create' | 'update' | 'detail'>('create')
const detailLoading = ref(false)
const submitLoading = ref(false)
const detailLoadError = ref('')
const formData = ref<StockInFormData>(createDefaultFormData())
const supplierList = ref<SupplierVO[]>([])
const formRef = ref()
const itemFormRef = ref()

const dialogTitle = computed(() => t('action.' + formType.value))
const isDetailMode = computed(() => formType.value === 'detail')
const disabled = computed(() => isDetailMode.value || detailLoading.value || submitLoading.value)

const formRules = reactive({
  inTime: [{ required: true, message: '入库时间不能为空', trigger: 'blur' }]
})

const resetFormState = () => {
  detailLoading.value = false
  submitLoading.value = false
  detailLoadError.value = ''
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  itemFormRef.value?.clearValidate?.()
}

const loadSupplierOptions = async () => {
  supplierList.value = await SupplierApi.getSupplierSimpleList()
}

const open = async (type: 'create' | 'update' | 'detail', id?: number) => {
  resetFormState()
  formType.value = type
  dialogVisible.value = true
  await loadSupplierOptions()

  if (!id) {
    return
  }

  detailLoading.value = true
  try {
    formData.value = {
      ...createDefaultFormData(),
      ...(await StockInApi.getStockIn(id))
    }
    await normalizeItemsForUnitInput(formData.value.items)
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
    const payload = formData.value as StockInVO
    if (formType.value === 'create') {
      await StockInApi.createStockIn(payload)
      message.success(t('common.createSuccess'))
    } else {
      await StockInApi.updateStockIn(payload)
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
.stock-in-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-in-form__alert {
  margin-bottom: 4px;
}

.stock-in-form__scroll {
  max-height: min(72vh, 860px);
  overflow-y: auto;
  padding-right: 4px;
}

.stock-in-form__base,
.stock-in-form__section {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  padding: 18px;
  background: rgba(248, 250, 252, 0.68);
}

.stock-in-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stock-in-form__field {
  width: 100%;
}

.stock-in-form__remark {
  grid-column: 1 / -1;
}

.stock-in-form__section-title {
  margin-bottom: 14px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.stock-in-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

@media (max-width: 1279px) {
  .stock-in-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .stock-in-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
