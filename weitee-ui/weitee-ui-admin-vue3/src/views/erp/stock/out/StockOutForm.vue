<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="1180"
    @closed="handleDialogClosed"
  >
    <div class="stock-out-form">
      <el-alert
        v-if="detailLoadError"
        type="error"
        :closable="false"
        show-icon
        class="stock-out-form__alert"
      >
        {{ detailLoadError }}
      </el-alert>

      <div class="stock-out-form__scroll">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          v-loading="detailLoading"
          :disabled="disabled"
          class="stock-out-form__base"
        >
          <div class="stock-out-form__grid">
            <el-form-item label="出库单号" prop="no">
              <el-input v-model="formData.no" disabled placeholder="保存时自动生成" />
            </el-form-item>
            <el-form-item label="出库时间" prop="outTime">
              <el-date-picker
                v-model="formData.outTime"
                type="date"
                value-format="x"
                placeholder="请选择出库时间"
                class="stock-out-form__field"
              />
            </el-form-item>
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                clearable
                filterable
                placeholder="请选择客户"
                class="stock-out-form__field"
              >
                <el-option
                  v-for="item in customerList"
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
            <el-form-item label="备注" prop="remark" class="stock-out-form__remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
              />
            </el-form-item>
          </div>
        </el-form>

        <section class="stock-out-form__section">
          <div class="stock-out-form__section-title">出库产品清单</div>
          <StockOutItemForm ref="itemFormRef" :items="formData.items" :disabled="disabled" />
        </section>
      </div>
    </div>

    <template #footer>
      <div class="stock-out-form__footer">
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
import { StockOutApi, StockOutVO } from '@/api/erp/stock/out'
import StockOutItemForm from './components/StockOutItemForm.vue'
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'

defineOptions({ name: 'StockOutForm' })

type StockOutFormData = Partial<StockOutVO> & {
  no?: string
  fileUrl?: string
  items: any[]
}

const { t } = useI18n()
const message = useMessage()

const createDefaultFormData = (): StockOutFormData => ({
  id: undefined,
  no: '',
  customerId: undefined,
  outTime: undefined,
  remark: undefined,
  fileUrl: '',
  items: []
})

const dialogVisible = ref(false)
const formType = ref<'create' | 'update' | 'detail'>('create')
const detailLoading = ref(false)
const submitLoading = ref(false)
const detailLoadError = ref('')
const formData = ref<StockOutFormData>(createDefaultFormData())
const customerList = ref<CustomerVO[]>([])
const formRef = ref()
const itemFormRef = ref()

const dialogTitle = computed(() => t('action.' + formType.value))
const isDetailMode = computed(() => formType.value === 'detail')
const disabled = computed(() => isDetailMode.value || detailLoading.value || submitLoading.value)

const formRules = reactive({
  outTime: [{ required: true, message: '出库时间不能为空', trigger: 'blur' }]
})

const resetFormState = () => {
  detailLoading.value = false
  submitLoading.value = false
  detailLoadError.value = ''
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  itemFormRef.value?.clearValidate?.()
}

const loadCustomerOptions = async () => {
  customerList.value = await CustomerApi.getCustomerSimpleList()
}

const open = async (type: 'create' | 'update' | 'detail', id?: number) => {
  resetFormState()
  formType.value = type
  dialogVisible.value = true
  await loadCustomerOptions()

  if (!id) {
    return
  }

  detailLoading.value = true
  try {
    formData.value = {
      ...createDefaultFormData(),
      ...(await StockOutApi.getStockOut(id))
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
    const payload = formData.value as StockOutVO
    if (formType.value === 'create') {
      await StockOutApi.createStockOut(payload)
      message.success(t('common.createSuccess'))
    } else {
      await StockOutApi.updateStockOut(payload)
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
.stock-out-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stock-out-form__alert {
  margin-bottom: 4px;
}

.stock-out-form__scroll {
  max-height: min(72vh, 860px);
  overflow-y: auto;
  padding-right: 4px;
}

.stock-out-form__base,
.stock-out-form__section {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  padding: 18px;
  background: rgba(248, 250, 252, 0.68);
}

.stock-out-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stock-out-form__field {
  width: 100%;
}

.stock-out-form__remark {
  grid-column: 1 / -1;
}

.stock-out-form__section-title {
  margin-bottom: 14px;
  color: #0f172a;
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
}

.stock-out-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

@media (max-width: 1279px) {
  .stock-out-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .stock-out-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
