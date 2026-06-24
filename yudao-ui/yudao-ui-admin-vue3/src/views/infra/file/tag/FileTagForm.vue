<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
      v-loading="formLoading"
    >
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入标签名称" />
      </el-form-item>
      <el-form-item label="标签颜色" prop="color">
        <el-color-picker v-model="formData.color" :predefine="predefineColors" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <IconSelect v-model="formData.icon" placeholder="请选择图标" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as FileTagApi from '@/api/infra/fileTag'

defineOptions({ name: 'FileTagForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()

// 预定义颜色
const predefineColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#00C1D4',
  '#7B68EE',
  '#FF69B4'
]

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  color: '#409EFF',
  icon: '',
  sort: 0
})

const formRules = reactive({
  name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }]
})

/** 打开弹窗 */
const open = async (id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = id ? '编辑标签' : '新增标签'
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      // 从列表中获取标签信息
      const list = await FileTagApi.getFileTagList()
      const tag = list.find((item) => item.id === id)
      if (tag) {
        Object.assign(formData, tag)
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    if (formData.id) {
      await FileTagApi.updateFileTag(formData)
      message.success('更新成功')
    } else {
      await FileTagApi.createFileTag(formData)
      message.success('新增成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.color = '#409EFF'
  formData.icon = ''
  formData.sort = 0
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
