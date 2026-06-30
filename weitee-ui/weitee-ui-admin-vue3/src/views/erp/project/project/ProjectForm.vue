<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" scroll maxHeight="70vh" width="min(600px, 92vw)">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="dialogLoading"
    >
      <el-form-item label="项目编号" prop="no">
        <el-input disabled v-model="formData.no" placeholder="保存时自动生成" />
      </el-form-item>
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入项目名称" />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="formData.customerId"
          clearable
          filterable
          placeholder="请选择客户"
          class="!w-1/1"
        >
          <el-option
            v-for="item in customerList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="PC负责人" prop="planCoordinatorId">
        <el-select
          v-model="formData.planCoordinatorId"
          clearable
          filterable
          placeholder="请选择PC负责人"
          class="!w-1/1"
        >
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="formatUserLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="MC负责人" prop="materialControllerId">
        <el-select
          v-model="formData.materialControllerId"
          clearable
          filterable
          placeholder="请选择MC负责人"
          class="!w-1/1"
        >
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="formatUserLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="交期" prop="deliveryDate">
        <el-date-picker
          v-model="formData.deliveryDate"
          type="date"
          value-format="x"
          placeholder="选择交期"
          class="!w-1/1"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input type="textarea" v-model="formData.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :loading="submitting" :disabled="submitDisabled">
        确定
      </el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ProjectApi, type ProjectVO } from '@/api/erp/project'
import { CustomerApi, type CustomerVO } from '@/api/erp/sale/customer'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'ProjectForm' })

type ProjectFormData = {
  id?: number
  no?: string
  name?: string
  customerId?: number
  planCoordinatorId?: number
  materialControllerId?: number
  status: number
  deliveryDate?: number | string | Date
  remark?: string
}

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogLoading = ref(false)
const submitting = ref(false)
const formType = ref('')
const openRequestSeq = ref(0)
const formData = ref<ProjectFormData>(createEmptyFormData())
const formRules = reactive({
  name: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
  planCoordinatorId: [{ required: true, message: 'PC负责人不能为空', trigger: 'change' }],
  materialControllerId: [{ required: true, message: 'MC负责人不能为空', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '交期不能为空', trigger: 'change' }]
})
const formRef = ref()
const customerList = ref<CustomerVO[]>([])
const userList = ref<SimpleUserVO[]>([])
const submitDisabled = computed(() => dialogLoading.value || submitting.value)

const formatUserLabel = (item: SimpleUserVO) => {
  return item.deptName ? `${item.nickname}（${item.deptName}）` : item.nickname
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'object' && error !== null) {
    const maybeMessage = (error as { message?: string; msg?: string }).message || (error as { msg?: string }).msg
    if (maybeMessage) {
      return maybeMessage
    }
  }
  return '操作失败，请稍后重试'
}

function createEmptyFormData(): ProjectFormData {
  return {
    id: undefined,
    no: undefined,
    name: undefined,
    customerId: undefined,
    planCoordinatorId: undefined,
    materialControllerId: undefined,
    status: 0,
    deliveryDate: undefined,
    remark: undefined
  }
}

const buildFormData = (project?: ProjectVO): ProjectFormData => {
  if (!project) {
    return createEmptyFormData()
  }
  return {
    ...createEmptyFormData(),
    id: project.id,
    no: project.no,
    name: project.name,
    customerId: project.customerId,
    planCoordinatorId: project.planCoordinatorId,
    materialControllerId: project.materialControllerId,
    status: project.status,
    deliveryDate: project.deliveryDate,
    remark: project.remark
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  const requestSeq = ++openRequestSeq.value
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  dialogLoading.value = true
  try {
    const [customers, users, project] = await Promise.all([
      CustomerApi.getCustomerSimpleList(),
      getSimpleUserList(),
      id ? ProjectApi.getProject(id) : Promise.resolve<ProjectVO | undefined>(undefined)
    ])

    if (requestSeq !== openRequestSeq.value) {
      return
    }

    customerList.value = customers
    userList.value = users
    formData.value = buildFormData(project)
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    if (requestSeq !== openRequestSeq.value) {
      return
    }
    message.error(getErrorMessage(error))
    dialogVisible.value = false
  } finally {
    if (requestSeq === openRequestSeq.value) {
      dialogLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (submitDisabled.value) {
    return
  }
  if (!formRef.value) {
    return
  }
  submitting.value = true
  try {
    const valid = await formRef.value
      .validate()
      .then(() => true)
      .catch(() => false)
    if (!valid) {
      return
    }
    const data = formData.value as unknown as ProjectVO
    if (formType.value === 'create') {
      await ProjectApi.createProject(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProjectApi.updateProject(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    message.error(getErrorMessage(error))
  } finally {
    submitting.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = createEmptyFormData()
  customerList.value = []
  userList.value = []
  formRef.value?.clearValidate()
}

watch(dialogVisible, (visible) => {
  if (visible) {
    return
  }
  openRequestSeq.value += 1
  dialogLoading.value = false
  submitting.value = false
  dialogTitle.value = ''
  formType.value = ''
  resetForm()
})
</script>
