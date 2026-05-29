<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px" scroll max-height="70vh">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="grid gap-x-16px md:grid-cols-2"
    >
      <el-form-item label="岗位名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入岗位名称" />
      </el-form-item>
      <el-form-item label="岗位编码" prop="code">
        <el-input v-model="formData.code" placeholder="请输入岗位编码" />
      </el-form-item>
      <el-form-item label="所属部门" prop="deptId">
        <el-select v-model="formData.deptId" filterable clearable placeholder="请选择所属部门">
          <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位层级" prop="level">
        <el-select v-model="formData.level" clearable placeholder="请选择岗位层级">
          <el-option label="高级" value="高级" />
          <el-option label="中级" value="中级" />
          <el-option label="初级" value="初级" />
          <el-option label="未分级" value="未分级" />
        </el-select>
      </el-form-item>
      <el-form-item label="编制人数" prop="staffQuota">
        <el-input-number v-model="formData.staffQuota" :min="1" :max="999" controls-position="right" />
      </el-form-item>
      <el-form-item label="显示顺序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="9999" controls-position="right" />
      </el-form-item>
      <el-form-item label="关键岗位" prop="keyPosition">
        <el-switch v-model="formData.keyPosition" />
      </el-form-item>
      <el-form-item label="允许兼岗" prop="allowPartTime">
        <el-switch v-model="formData.allowPartTime" />
      </el-form-item>
      <el-form-item label="岗位状态" prop="status">
        <el-select v-model="formData.status" clearable placeholder="请选择岗位状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位说明" prop="jobDescription" class="md:col-span-2">
        <el-input v-model="formData.jobDescription" type="textarea" :rows="3" placeholder="请输入岗位说明" />
      </el-form-item>
      <el-form-item label="备注" prop="remark" class="md:col-span-2">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { FormRules } from 'element-plus'
import * as DeptApi from '@/api/system/dept'
import * as PostApi from '@/api/system/post'
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'SystemPostForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const deptOptions = ref<DeptApi.DeptVO[]>([])
const formRef = ref()
const formData = ref<PostApi.PostVO>({
  id: undefined,
  name: '',
  code: '',
  level: '未分级',
  deptId: undefined,
  staffQuota: 1,
  keyPosition: false,
  allowPartTime: false,
  jobDescription: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  level: [{ required: true, message: '请选择岗位层级', trigger: 'change' }],
  staffQuota: [{ required: true, message: '请输入编制人数', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择岗位状态', trigger: 'change' }]
})

const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    code: '',
    level: '未分级',
    deptId: undefined,
    staffQuota: 1,
    keyPosition: false,
    allowPartTime: false,
    jobDescription: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
  formRef.value?.resetFields()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  deptOptions.value = await DeptApi.getSimpleDeptList()
  if (!id) {
    return
  }
  formLoading.value = true
  try {
    formData.value = await PostApi.getPost(id)
    formData.value.level = formData.value.level || '未分级'
    formData.value.staffQuota = formData.value.staffQuota || 1
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  formLoading.value = true
  try {
    const data = formData.value
    if (formType.value === 'create') {
      await PostApi.createPost(data)
      message.success(t('common.createSuccess'))
    } else {
      await PostApi.updatePost(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
