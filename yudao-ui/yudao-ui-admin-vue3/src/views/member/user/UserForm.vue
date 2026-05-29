<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900px" :scroll="true" :max-height="'72vh'">
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <div class="grid gap-4 lg:grid-cols-2">
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="formData.mobile" placeholder="请输入手机号" />
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
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <UploadImg v-model="formData.avatar" :limit="1" :is-show-tip="false" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="用户性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio
              v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_USER_SEX)"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            placeholder="请选择出生日期"
            type="date"
            value-format="x"
          />
        </el-form-item>
        <el-form-item label="所在地区" prop="areaId">
          <el-tree-select
            v-model="formData.areaId"
            :data="areaList"
            :props="defaultProps"
            :render-after-expand="true"
          />
        </el-form-item>
        <el-form-item label="用户标签" prop="tagIds">
          <MemberTagSelect v-model="formData.tagIds" show-add />
        </el-form-item>
        <el-form-item label="用户分组" prop="groupId">
          <MemberGroupSelect v-model="formData.groupId" />
        </el-form-item>
        <el-form-item class="lg:col-span-2" label="会员备注" prop="mark">
          <el-input
            v-model="formData.mark"
            :autosize="{ minRows: 3, maxRows: 5 }"
            placeholder="请输入会员备注"
            type="textarea"
          />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <div class="flex items-center justify-end gap-2">
        <el-button :disabled="submitting || loading" type="primary" @click="submitForm">
          确定
        </el-button>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as UserApi from '@/api/member/user'
import * as AreaApi from '@/api/system/area'
import { defaultProps } from '@/utils/tree'
import MemberTagSelect from '@/views/member/tag/components/MemberTagSelect.vue'
import MemberGroupSelect from '@/views/member/group/components/MemberGroupSelect.vue'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const loading = ref(false)
const submitting = ref(false)
const formType = ref('')
const areaList = ref([])
const formRef = ref()
const formData = ref({
  id: undefined,
  mobile: undefined,
  password: undefined,
  status: undefined,
  nickname: undefined,
  avatar: undefined,
  name: undefined,
  sex: undefined,
  areaId: undefined,
  birthday: undefined,
  mark: undefined,
  tagIds: [],
  groupId: undefined
})
const formRules = reactive({
  mobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

const emptyFormData = () => ({
  id: undefined,
  mobile: undefined,
  password: undefined,
  status: undefined,
  nickname: undefined,
  avatar: undefined,
  name: undefined,
  sex: undefined,
  areaId: undefined,
  birthday: undefined,
  mark: undefined,
  tagIds: [],
  groupId: undefined
})

const resetForm = () => {
  formData.value = emptyFormData()
  formRef.value?.resetFields()
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t(`action.${type}`)
  formType.value = type
  resetForm()
  loading.value = true
  try {
    areaList.value = await AreaApi.getAreaTree()
    if (id) {
      formData.value = await UserApi.getUser(id)
    }
  } finally {
    loading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) {
    return
  }
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    if (formType.value === 'create') {
      message.success('当前页面暂无新增接口，仅保留编辑能力')
      return
    }
    await UserApi.updateUser(formData.value as unknown as UserApi.UserVO)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
