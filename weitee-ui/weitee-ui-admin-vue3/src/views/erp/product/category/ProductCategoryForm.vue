<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
      class="category-form"
    >
      <el-form-item label="上级分类" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="productCategoryTree"
          :props="defaultProps"
          check-strictly
          default-expand-all
          placeholder="请选择上级分类"
          class="category-form-control"
        />
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" class="category-form-control" />
      </el-form-item>
      <el-form-item label="分类编码" prop="code">
        <el-input v-model="formData.code" placeholder="请输入分类编码" class="category-form-control" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input v-model="formData.sort" placeholder="请输入排序值" class="category-form-control" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
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
    </el-form>
    <template #footer>
      <el-button
        type="primary"
        :loading="formSubmitting"
        :disabled="!canSubmit"
        @click="submitForm"
      >
        确定
      </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import { defaultProps, handleTree } from '@/utils/tree'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'ProductCategoryForm' })

type CategoryFormData = {
  id: number | undefined
  parentId: number
  name: string | undefined
  code: string | undefined
  sort: number | undefined
  status: number
}

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formSubmitting = ref(false)
const formType = ref('')
const formRef = ref()
const productCategoryTree = ref<any[]>([])

const createDefaultFormData = (): CategoryFormData => ({
  id: undefined,
  parentId: 0,
  name: undefined,
  code: undefined,
  sort: undefined,
  status: CommonStatusEnum.ENABLE
})

const formData = ref<CategoryFormData>(createDefaultFormData())
const formRules = reactive({
  parentId: [{ required: true, message: '上级分类不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})

const canSubmit = computed(() => !formLoading.value && !formSubmitting.value)

const resetForm = () => {
  formData.value = createDefaultFormData()
  formRef.value?.resetFields()
}

const getProductCategoryTree = async () => {
  productCategoryTree.value = []
  const data = await ProductCategoryApi.getProductCategoryList()
  const root = { id: 0, name: '顶级分类', children: [] as ProductCategoryVO[] }
  root.children = handleTree(data, 'id', 'parentId')
  productCategoryTree.value.push(root)
}

const open = async (type: string, id?: number, parentId = 0) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  formData.value.parentId = parentId

  if (id) {
    formLoading.value = true
    try {
      const detail = await ProductCategoryApi.getProductCategory(id)
      formData.value = {
        id: detail.id,
        parentId: detail.parentId ?? 0,
        name: detail.name,
        code: detail.code,
        sort: detail.sort,
        status: detail.status
      }
    } finally {
      formLoading.value = false
    }
  }

  await getProductCategoryTree()
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (!canSubmit.value) {
    return
  }

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  formSubmitting.value = true
  try {
    const data = formData.value as unknown as ProductCategoryVO
    if (formType.value === 'create') {
      await ProductCategoryApi.createProductCategory(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductCategoryApi.updateProductCategory(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formSubmitting.value = false
  }
}
</script>

<style scoped lang="scss">
.category-form {
  :deep(.el-form-item__content) {
    min-width: 0;
  }
}

.category-form-control {
  width: 100%;
}

@media (max-width: 768px) {
  .category-form {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }

    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }
  }
}
</style>
