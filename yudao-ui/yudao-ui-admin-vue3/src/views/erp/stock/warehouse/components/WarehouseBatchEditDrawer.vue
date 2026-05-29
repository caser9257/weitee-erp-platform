<template>
  <BatchEditDrawer
    v-model="visible"
    title="批量编辑"
    :selected-count="selectedCount"
    :loading="submitting"
    :confirm-disabled="!canSubmit || !canConfirm"
    @confirm="handleSubmit"
    @closed="handleClosed"
  >
    <template #context>
      <div class="warehouse-batch-edit__context">
        <div class="warehouse-batch-edit__context-title">仓库</div>
        <div class="warehouse-batch-edit__context-count">已选择 {{ selectedCount }} 条</div>
      </div>
    </template>

    <div class="warehouse-batch-edit__panel">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="warehouse-batch-edit-form"
      >
        <el-form-item label="修改字段" prop="fieldKey">
          <el-select
            v-model="formData.fieldKey"
            placeholder="请选择修改字段"
            :disabled="submitting"
            @change="handleFieldChange"
          >
            <el-option
              v-for="item in fieldOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="修改值" prop="value">
          <BatchEditFieldEditor
            :field="currentField"
            v-model="formData.value"
            :disabled="submitting || !currentField"
          />
        </el-form-item>
      </el-form>
    </div>
  </BatchEditDrawer>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import type { BatchEditValue } from '@/components/BatchEdit'
import { BatchEditDrawer, BatchEditFieldEditor } from '@/components/BatchEdit'
import { useBatchEdit } from '@/hooks/web/useBatchEdit'
import { useBatchEditFields } from '@/hooks/web/useBatchEditFields'
import {
  WarehouseApi,
  type WarehouseBatchUpdateResultVO,
  type WarehouseVO
} from '@/api/erp/stock/warehouse'
import {
  warehouseBatchEditAdapter,
  type WarehouseBatchFieldKey
} from './warehouseBatchEditAdapter'

defineOptions({ name: 'WarehouseBatchEditDrawer' })

const emit = defineEmits<{
  (event: 'success', result: WarehouseBatchUpdateResultVO): void
}>()

const message = useMessage()

const formRef = ref()
const formData = reactive({
  fieldKey: '' as WarehouseBatchFieldKey | '',
  value: '' as BatchEditValue
})

const { visible, submitting, selectedIds, selectedRows, selectedCount, canSubmit, open, close, reset } =
  useBatchEdit<WarehouseVO>()
const { fieldOptions, getField, getDefaultValue, getValueRules, isValueReady, serializeValue } =
  useBatchEditFields(warehouseBatchEditAdapter.fields)

const currentField = computed(() => getField(formData.fieldKey || undefined))
const canConfirm = computed(() => {
  if (!formData.fieldKey || !currentField.value) {
    return false
  }
  return isValueReady(formData.fieldKey, formData.value)
})

const formRules = computed(() => ({
  fieldKey: [{ required: true, message: '修改字段不能为空', trigger: 'change' }],
  value: getValueRules(formData.fieldKey || undefined)
}))

const resolveSuccessMessage = (result: WarehouseBatchUpdateResultVO) => {
  if (result.failureCount > 0) {
    return `批量编辑完成，成功 ${result.successCount} 条，失败 ${result.failureCount} 条`
  }
  return `批量编辑成功，共更新 ${result.successCount} 条`
}

const handleFieldChange = async () => {
  const field = currentField.value
  const fieldKey = formData.fieldKey || ''
  formData.value = field
    ? warehouseBatchEditAdapter.resolveInitialValue(fieldKey, selectedRows.value, getDefaultValue(field.key))
    : ''
  await nextTick()
  formRef.value?.clearValidate('value')
}

const handleSubmit = async () => {
  if (!selectedIds.value.length) {
    message.warning('请先选择要修改的数据')
    return
  }
  if (!formData.fieldKey || !currentField.value) {
    message.warning('请先选择修改字段')
    return
  }
  if (submitting.value || !canConfirm.value) {
    return
  }

  submitting.value = true
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    submitting.value = false
    return
  }

  try {
    const fieldKey = warehouseBatchEditAdapter.normalizeFieldKey(formData.fieldKey)
    if (!fieldKey) {
      message.warning('请先选择修改字段')
      return
    }
    const result = await WarehouseApi.batchUpdateWarehouse(
      warehouseBatchEditAdapter.buildRequest({
        ids: selectedIds.value,
        fieldKey,
        value: serializeValue(fieldKey, formData.value)
      })
    )
    reset()
    const successMessage = resolveSuccessMessage(result)
    if (result.failureCount > 0) {
      message.warning(successMessage)
    } else {
      message.success(successMessage)
    }
    emit('success', result)
  } catch {
  } finally {
    submitting.value = false
  }
}

const clearFormState = () => {
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
  formData.fieldKey = ''
  formData.value = ''
}

const openDrawer = async (payload: { ids: number[]; rows?: WarehouseVO[]; fieldKey?: string }) => {
  open({ ids: payload.ids, rows: payload.rows })
  formData.fieldKey = warehouseBatchEditAdapter.normalizeFieldKey(payload.fieldKey)
  formData.value = warehouseBatchEditAdapter.resolveInitialValue(
    formData.fieldKey,
    selectedRows.value,
    getDefaultValue(formData.fieldKey)
  )
  await nextTick()
  formRef.value?.clearValidate()
}

const handleClosed = () => {
  clearFormState()
  close()
}

defineExpose({ open: openDrawer })
</script>

<style scoped lang="scss">
.warehouse-batch-edit__context {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 18px;
  background: #0f172a;
  color: #fff;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);
}

.warehouse-batch-edit__context-title {
  font-size: 18px;
  font-weight: 800;
  line-height: 28px;
}

.warehouse-batch-edit__context-count {
  color: #bfdbfe;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.warehouse-batch-edit__panel {
  padding: 20px;
}

.warehouse-batch-edit-form {
  display: grid;
  gap: 18px;
}

@media (max-width: 1024px) {
  .warehouse-batch-edit__context {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
