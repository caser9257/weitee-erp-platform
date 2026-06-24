<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="文件夹名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入文件夹名称" />
      </el-form-item>
      <el-form-item label="上级文件夹" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="folderTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择上级文件夹"
          check-strictly
          clearable
          class="w-full"
        />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <IconSelect v-model="formData.icon" placeholder="请选择图标" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="0">正常</el-radio>
          <el-radio :value="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :disabled="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as FileFolderApi from '@/api/infra/fileFolder'
import type { FileFolderVO } from '@/api/infra/fileFolder'

defineOptions({ name: 'FileFolderForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref()
const folderTree = ref<FileFolderVO[]>([])

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  parentId: 0,
  icon: '',
  sort: 0,
  status: 0,
  remark: ''
})

const formRules = reactive({
  name: [{ required: true, message: '文件夹名称不能为空', trigger: 'blur' }],
  parentId: [{ required: true, message: '上级文件夹不能为空', trigger: 'change' }]
})

/** 打开弹窗 */
const open = async (id?: number, parentId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = id ? '编辑文件夹' : '新增文件夹'
  resetForm()

  // 加载文件夹树
  await loadFolderTree()

  if (id) {
    formLoading.value = true
    try {
      const data = await FileFolderApi.getFileFolder(id)
      Object.assign(formData, data)
    } finally {
      formLoading.value = false
    }
  } else if (parentId) {
    formData.parentId = parentId
  }
}

/** 加载文件夹树 */
const loadFolderTree = async () => {
  try {
    const data = await FileFolderApi.getFileFolderTree()
    folderTree.value = [{ id: 0, name: '根目录', children: data } as any]
  } catch (e) {
    console.error('加载文件夹树失败', e)
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
      await FileFolderApi.updateFileFolder(formData)
      message.success('更新成功')
    } else {
      await FileFolderApi.createFileFolder(formData)
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
  formData.parentId = 0
  formData.icon = ''
  formData.sort = 0
  formData.status = 0
  formData.remark = ''
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
