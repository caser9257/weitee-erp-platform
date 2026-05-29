<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="min(720px, calc(100vw - 32px))" scroll maxHeight="72vh" @closed="handleClosed">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top" v-loading="detailLoading" :disabled="disabled" class="warehouse-category-form">
      <el-form-item label="上级分类" prop="parentId">
        <el-tree-select v-model="formData.parentId" :data="categoryTree" :props="defaultProps" check-strictly default-expand-all placeholder="请选择上级分类" class="warehouse-category-form__control" />
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="分类编码" prop="code">
        <el-input v-model="formData.code" placeholder="请输入分类编码" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :precision="0" :min="0" class="warehouse-category-form__control" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="warehouse-category-form__footer">
        <el-button :disabled="disabled" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="detailLoading" @click="submitForm">确定</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { defaultProps, handleTree } from '@/utils/tree'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { WarehouseCategoryApi, WarehouseCategoryVO } from '@/api/erp/stock/warehouse-category'

defineOptions({ name: 'WarehouseCategoryForm' })

type CategoryFormData = {
  id: number | undefined
  parentId: number
  name: string | undefined
  code: string | undefined
  sort: number | undefined
  status: number
}

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const detailLoading = ref(false)
const submitLoading = ref(false)
const formType = ref<'create' | 'update'>('create')
const categoryTree = ref<any[]>([])
const currentId = ref<number>()

const createDefaultFormData = (): CategoryFormData => ({
  id: undefined,
  parentId: 0,
  name: undefined,
  code: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})

const formData = ref<CategoryFormData>(createDefaultFormData())
const formRules = reactive({
  parentId: [{ required: true, message: '上级分类不能为空', trigger: 'change' }],
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

const disabled = computed(() => detailLoading.value || submitLoading.value)

const resetForm = () => {
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
  formRef.value?.clearValidate()
}

const loadTree = async () => {
  const data = await WarehouseCategoryApi.getWarehouseCategoryList({})
  const root = { id: 0, name: '顶级分类', children: [] as WarehouseCategoryVO[] }
  root.children = handleTree(Array.isArray(data) ? data : data.list || [], 'id', 'parentId')
  categoryTree.value = [root]
}

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增分类' : '编辑分类'
  formType.value = type
  currentId.value = id
  resetForm()
  await loadTree()
  if (!id) return
  detailLoading.value = true
  try {
    const detail = await WarehouseCategoryApi.getWarehouseCategory(id)
    formData.value = { ...createDefaultFormData(), ...detail }
  } finally {
    detailLoading.value = false
  }
}

defineExpose({ open })
const emit = defineEmits(['success'])

const submitForm = async () => {
  if (disabled.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = formData.value as unknown as WarehouseCategoryVO
    if (formType.value === 'create') {
      await WarehouseCategoryApi.createWarehouseCategory(payload)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseCategoryApi.updateWarehouseCategory(payload)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

const handleClosed = () => {
  currentId.value = undefined
  detailLoading.value = false
  submitLoading.value = false
  categoryTree.value = []
  resetForm()
}
</script>

<style scoped>
.warehouse-category-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.warehouse-category-form__control {
  width: 100%;
}
.warehouse-category-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
@media (max-width: 767px) {
  .warehouse-category-form {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
