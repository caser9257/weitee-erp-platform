<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px" scroll max-height="72vh">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="pr-10px">
      <div class="grid gap-16px md:grid-cols-2">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptOptions"
            :props="deptTreeProps"
            node-key="id"
            check-strictly
            filterable
            default-expand-all
            placeholder="请选择上级部门"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" controls-position="right" class="!w-full" />
        </el-form-item>
        <el-form-item label="负责人" prop="leaderUserId">
          <el-select v-model="formData.leaderUserId" clearable filterable placeholder="请选择负责人">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.nickname}${user.deptName ? `（${user.deptName}）` : ''}`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status" class="md:col-span-2">
          <el-radio-group v-model="formData.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { DeptVO } from '@/api/system/dept'
import { createDept, getDept, getSimpleDeptList, updateDept } from '@/api/system/dept'
import { getSimpleUserList } from '@/api/system/user'

defineOptions({ name: 'DeptFormDialog' })

type DeptFormMode = 'create' | 'update'

interface DeptOption extends Pick<DeptVO, 'id' | 'name' | 'parentId'> {
  children?: DeptOption[]
}

interface DeptFormModel {
  id?: number
  name: string
  parentId: number
  sort: number
  leaderUserId?: number
  phone: string
  email: string
  status: number
}

interface UserOption {
  id: number
  nickname: string
  deptName?: string
}

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const dialogVisible = ref(false)
const submitting = ref(false)
const dialogTitle = ref('新增部门')
const mode = ref<DeptFormMode>('create')
const formRef = ref()
const deptOptions = ref<DeptOption[]>([])
const userOptions = ref<UserOption[]>([])
const formData = reactive<DeptFormModel>({
  id: undefined,
  name: '',
  parentId: 0,
  sort: 1,
  leaderUserId: undefined,
  phone: '',
  email: '',
  status: 0
})

const deptTreeProps = {
  label: 'name',
  value: 'id',
  children: 'children'
}

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }]
}

const buildDeptOptions = (list: DeptVO[]) => {
  const nodeMap = new Map<number, DeptOption>()
  const root: DeptOption = { id: 0, name: '公司', parentId: 0, children: [] }
  list.forEach((item) => nodeMap.set(item.id, { id: item.id, name: item.name, parentId: item.parentId, children: [] }))
  nodeMap.forEach((node) => {
    if (node.parentId === 0) {
      root.children?.push(node)
      return
    }
    const parent = nodeMap.get(node.parentId)
    if (parent) {
      parent.children ||= []
      parent.children.push(node)
    } else {
      root.children?.push(node)
    }
  })
  return [root]
}

const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.parentId = 0
  formData.sort = 1
  formData.leaderUserId = undefined
  formData.phone = ''
  formData.email = ''
  formData.status = 0
  formRef.value?.clearValidate?.()
}

const loadOptions = async () => {
  const [deptList, userList] = await Promise.all([getSimpleDeptList(), getSimpleUserList()])
  deptOptions.value = buildDeptOptions(deptList)
  userOptions.value = userList.map((user) => ({
    id: user.id,
    nickname: user.nickname,
    deptName: (user as { deptName?: string }).deptName
  }))
}

const open = async (nextMode: DeptFormMode, deptId?: number, parentId = 0) => {
  mode.value = nextMode
  dialogTitle.value = nextMode === 'create' ? '新增部门' : '编辑部门'
  dialogVisible.value = true
  await loadOptions()
  resetForm()
  formData.parentId = parentId
  if (nextMode === 'update' && deptId) {
    const detail = await getDept(deptId)
    if (detail) {
      formData.id = detail.id
      formData.name = detail.name
      formData.parentId = detail.parentId ?? 0
      formData.sort = detail.sort ?? 1
      formData.leaderUserId = detail.leaderUserId
      formData.phone = detail.phone || ''
      formData.email = detail.email || ''
      formData.status = detail.status ?? 0
    }
  }
}

const close = () => {
  dialogVisible.value = false
  resetForm()
}

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    if (mode.value === 'create') {
      await createDept(formData as never)
    } else {
      await updateDept(formData as never)
    }
    emit('success')
    message.success('部门已保存')
    close()
  } catch (error) {
    message.error('保存失败，请检查表单后重试')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
