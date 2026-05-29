<template>
  <Dialog v-model="dialogVisible" title="分配岗位人员" width="780px" scroll max-height="70vh">
    <el-form ref="formRef" :model="formData" label-width="0">
      <div class="mb-12px flex flex-wrap items-center justify-between gap-12px">
        <div class="text-14px text-[var(--el-text-color-secondary)]">当前岗位：{{ postName || '-' }}</div>
        <div class="flex gap-8px">
          <el-button plain @click="clearAssignments">清空人员</el-button>
          <el-button type="primary" plain @click="addRow">
            <Icon icon="ep:plus" class="mr-4px" />新增一行
          </el-button>
        </div>
      </div>
      <div class="grid gap-12px">
        <div
          v-for="(item, index) in formData.assignments"
          :key="index"
          class="rounded-4px border border-[var(--el-border-color)] p-12px"
        >
          <div class="grid gap-12px md:grid-cols-2">
            <el-form-item
              :prop="`assignments.${index}.userId`"
              :rules="[{ required: true, message: '请选择人员', trigger: 'change' }]"
            >
              <el-select v-model="item.userId" filterable placeholder="选择人员">
                <el-option
                  v-for="user in userOptions"
                  :key="user.id"
                  :label="buildUserLabel(user)"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-switch v-model="item.primary" active-text="主岗" inactive-text="兼岗" />
            </el-form-item>
            <el-form-item>
              <el-date-picker
                v-model="item.startDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="开始时间"
              />
            </el-form-item>
            <el-form-item>
              <el-input v-model="item.remark" placeholder="备注" />
            </el-form-item>
          </div>
          <div class="mt-8px text-right">
            <el-button link type="danger" @click="removeRow(index)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty
        v-if="!formData.assignments.length"
        description="当前岗位暂无任职人员，可新增分配记录或直接提交清空状态"
      />
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="closeDialog">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { PostLevelAssignUserItemVO, PostLevelDetailVO } from '@/api/system/postLevel'
import type { SimpleUserVO } from '@/api/system/user'

defineOptions({ name: 'PostAssignUserForm' })

const props = defineProps<{
  submitAction?: (
    postId: number,
    assignments: PostLevelAssignUserItemVO[]
  ) => Promise<void> | void
}>()

const dialogVisible = ref(false)
const submitting = ref(false)
const postId = ref<number>()
const postName = ref('')
const userOptions = ref<SimpleUserVO[]>([])
const formRef = ref()
const formData = reactive<{ assignments: PostLevelAssignUserItemVO[] }>({
  assignments: []
})

const resetForm = () => {
  postId.value = undefined
  postName.value = ''
  userOptions.value = []
  formData.assignments = []
  formRef.value?.clearValidate()
}

const buildUserLabel = (user: SimpleUserVO) => {
  return user.deptName ? `${user.nickname}（${user.deptName}）` : user.nickname
}

const addRow = () => {
  formData.assignments.push({
    userId: undefined as unknown as number,
    primary: false,
    remark: ''
  })
}

const removeRow = (index: number) => {
  formData.assignments.splice(index, 1)
}

const clearAssignments = () => {
  formData.assignments = []
}

const closeDialog = () => {
  dialogVisible.value = false
  resetForm()
}

const open = (detail: PostLevelDetailVO, options: SimpleUserVO[]) => {
  resetForm()
  dialogVisible.value = true
  postId.value = detail.postId
  postName.value = detail.name
  userOptions.value = [...options]
  formData.assignments = detail.assignedUsers.map((item) => ({
    userId: item.userId,
    primary: item.primary,
    startDate: item.startDate,
    endDate: item.endDate,
    remark: item.remark
  }))
}

const submit = async () => {
  const valid = formData.assignments.length ? await formRef.value?.validate().catch(() => false) : true
  if (!valid || !postId.value || !props.submitAction) return
  submitting.value = true
  try {
    await props.submitAction(postId.value, formData.assignments.filter((item) => !!item.userId))
    closeDialog()
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>