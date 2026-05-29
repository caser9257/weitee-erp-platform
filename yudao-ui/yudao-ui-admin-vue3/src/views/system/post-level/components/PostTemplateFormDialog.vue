<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px" scroll max-height="70vh">
    <el-form
      ref="formRef"
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
        <el-tree-select
          v-model="formData.deptId"
          :data="deptOptions"
          :props="deptProps"
          filterable
          check-strictly
          default-expand-all
          node-key="id"
          placeholder="请选择所属部门"
        />
      </el-form-item>
      <el-form-item label="岗位层级" prop="level">
        <el-select v-model="formData.level" placeholder="请选择岗位层级">
          <el-option label="高级" value="高级" />
          <el-option label="中级" value="中级" />
          <el-option label="初级" value="初级" />
          <el-option label="未分级" value="未分级" />
        </el-select>
      </el-form-item>
      <el-form-item label="编制人数" prop="staffQuota">
        <el-input-number v-model="formData.staffQuota" :min="1" :max="99" controls-position="right" />
      </el-form-item>
      <el-form-item label="显示顺序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="999" controls-position="right" />
      </el-form-item>
      <el-form-item label="关键岗位" prop="keyPosition">
        <el-switch v-model="formData.keyPosition" />
      </el-form-item>
      <el-form-item label="允许兼岗" prop="allowPartTime">
        <el-switch v-model="formData.allowPartTime" />
      </el-form-item>
      <el-form-item label="岗位说明" prop="jobDescription" class="md:col-span-2">
        <el-input v-model="formData.jobDescription" type="textarea" :rows="3" placeholder="请输入岗位说明" />
      </el-form-item>
      <el-form-item label="备注" prop="remark" class="md:col-span-2">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { PostVO } from '@/api/system/post'
import type { FormRules } from 'element-plus'

defineOptions({ name: 'PostTemplateFormDialog' })

interface DeptTreeOption {
  id: number
  label: string
  parentId?: number
  children?: DeptTreeOption[]
}

const props = defineProps<{
  submitAction?: (payload: PostVO) => Promise<void> | void
}>()

const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const submitting = ref(false)
const deptOptions = ref<DeptTreeOption[]>([])
const deptProps = {
  label: 'label',
  children: 'children'
}
const formRef = ref()
const formData = reactive<PostVO>({
  id: undefined,
  name: '',
  code: '',
  deptId: undefined,
  level: '未分级',
  staffQuota: 1,
  keyPosition: false,
  allowPartTime: false,
  sort: 10,
  status: 0,
  remark: '',
  jobDescription: ''
})

const formRules = reactive<FormRules>({
  name: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  level: [{ required: true, message: '请选择岗位层级', trigger: 'change' }],
  staffQuota: [{ required: true, message: '请输入编制人数', trigger: 'change' }]
})

const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.code = ''
  formData.deptId = undefined
  formData.level = '未分级'
  formData.staffQuota = 1
  formData.keyPosition = false
  formData.allowPartTime = false
  formData.sort = 10
  formData.status = 0
  formData.jobDescription = ''
  formData.remark = ''
}

const open = (type: 'create' | 'update', departments: DeptTreeOption[], payload?: Partial<PostVO>) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增岗位' : '编辑岗位'
  deptOptions.value = departments
  resetForm()
  formRef.value?.clearValidate()
  if (payload) {
    Object.assign(formData, JSON.parse(JSON.stringify(payload)))
  }
}

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !props.submitAction) return
  submitting.value = true
  try {
    await props.submitAction(JSON.parse(JSON.stringify(formData)))
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>