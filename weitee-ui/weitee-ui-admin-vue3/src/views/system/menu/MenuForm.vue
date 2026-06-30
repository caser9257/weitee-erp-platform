<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="isFormBusy"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="上级菜单">
        <el-tree-select
          v-model="formData.parentId"
          :data="menuTree"
          :default-expanded-keys="[0]"
          :props="defaultProps"
          check-strictly
          node-key="id"
        />
      </el-form-item>
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="formData.name" clearable placeholder="请输入菜单名称" />
      </el-form-item>
      <el-form-item label="菜单类型" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio-button
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_MENU_TYPE)"
            :key="dict.label"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 3" label="菜单图标">
        <IconSelect v-model="formData.icon" clearable />
      </el-form-item>
      <el-form-item v-if="formData.type !== 3" label="路由地址" prop="path">
        <template #label>
          <Tooltip
            message="访问的路由地址，例如：/system/user。外链请以 http(s):// 开头。"
            title="路由地址"
          />
        </template>
        <el-input v-model="formData.path" clearable placeholder="请输入路由地址" />
      </el-form-item>
      <el-form-item v-if="formData.type === 2" label="组件地址" prop="component">
        <el-input
          v-model="formData.component"
          clearable
          placeholder="例如：system/user/index"
        />
      </el-form-item>
      <el-form-item v-if="formData.type === 2" label="组件名称" prop="componentName">
        <el-input
          v-model="formData.componentName"
          clearable
          placeholder="例如：SystemUser"
        />
      </el-form-item>
      <el-form-item v-if="formData.type !== 1" label="权限标识" prop="permission">
        <template #label>
          <Tooltip
            message="Controller 方法上的权限字符，例如：@PreAuthorize(`@ss.hasPermission('system:user:list')`)"
            title="权限标识"
          />
        </template>
        <el-input v-model="formData.permission" clearable placeholder="请输入权限标识" />
      </el-form-item>
      <el-form-item label="显示排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" clearable controls-position="right" />
      </el-form-item>
      <el-form-item label="菜单状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.label"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 3" label="显示状态" prop="visible">
        <template #label>
          <Tooltip
            message="选择隐藏时，路由不会出现在侧边栏，但仍然可以访问。"
            title="显示状态"
          />
        </template>
        <el-radio-group v-model="formData.visible">
          <el-radio :value="true" border>显示</el-radio>
          <el-radio :value="false" border>隐藏</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 3" label="总是显示" prop="alwaysShow">
        <template #label>
          <Tooltip
            message="选择否时，如果该菜单只有一个子菜单，则不展示自己，直接展示子菜单。"
            title="总是显示"
          />
        </template>
        <el-radio-group v-model="formData.alwaysShow">
          <el-radio :value="true" border>总是</el-radio>
          <el-radio :value="false" border>不是</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type === 2" label="缓存状态" prop="keepAlive">
        <template #label>
          <Tooltip
            message="选择缓存时，会被 keep-alive 缓存，此时必须填写组件名称。"
            title="缓存状态"
          />
        </template>
        <el-radio-group v-model="formData.keepAlive">
          <el-radio :value="true" border>缓存</el-radio>
          <el-radio :value="false" border>不缓存</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button
        :disabled="isSubmitDisabled"
        :loading="formSubmitting"
        type="primary"
        @click="submitForm"
      >
        确定
      </el-button>
      <el-button :disabled="formSubmitting" @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as MenuApi from '@/api/system/menu'
import { CommonStatusEnum, SystemMenuTypeEnum } from '@/utils/constants'
import { matchesFormalRootMenuPath } from '@/utils/menuRouteRule'
import { syncCurrentSessionPermissionRoutes } from '@/utils/permissionRouteSync'
import { defaultProps, handleTree } from '@/utils/tree'
import { hasViewModule } from '@/utils/viewModuleResolver'

defineOptions({ name: 'SystemMenuForm' })

const { t } = useI18n()
const message = useMessage()

const buildDefaultFormData = () => ({
  id: undefined,
  name: '',
  permission: '',
  type: SystemMenuTypeEnum.DIR,
  sort: undefined,
  parentId: 0,
  path: '',
  icon: '',
  component: '',
  componentName: '',
  status: CommonStatusEnum.ENABLE,
  visible: true,
  keepAlive: true,
  alwaysShow: true
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formDetailLoading = ref(false)
const formSubmitting = ref(false)
const formType = ref('')
const formData = ref<any>(buildDefaultFormData())
const isFormBusy = computed(() => formDetailLoading.value || formSubmitting.value)
const isSubmitDisabled = computed(() => formDetailLoading.value || formSubmitting.value)

const formRules = reactive({
  name: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '菜单类型不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '菜单排序不能为空', trigger: 'blur' }],
  path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})

const formRef = ref()
const menuTree = ref<any[]>([])

const normalizeFormData = () => {
  formData.value.path = formData.value.path?.trim() || ''
  formData.value.component = formData.value.component?.trim() || ''
  formData.value.componentName = formData.value.componentName?.trim() || ''
}

const getBasePath = (path: string) => (path || '').split('?')[0] || ''

const isExternal = (path: string) => /^(https?:|mailto:|tel:)/.test(path)

const validateRouteBusinessRules = () => {
  normalizeFormData()

  if (
    formData.value.type === SystemMenuTypeEnum.DIR ||
    formData.value.type === SystemMenuTypeEnum.MENU
  ) {
    const routePath = getBasePath(formData.value.path)
    if (!isExternal(routePath)) {
      if (formData.value.parentId === 0 && !routePath.startsWith('/')) {
        message.error('顶级菜单路径必须以 / 开头')
        return false
      }
      if (formData.value.parentId !== 0 && routePath.startsWith('/')) {
        message.error('子菜单路径不能以 / 开头')
        return false
      }
      if (formData.value.parentId === 0 && !matchesFormalRootMenuPath(routePath)) {
        message.error('顶级菜单路径必须归属系统已定义的一级菜单路由')
        return false
      }
    }
  }

  if (formData.value.type === SystemMenuTypeEnum.MENU) {
    if (!formData.value.component) {
      message.error('组件地址不能为空')
      return false
    }
    if (!hasViewModule(formData.value.component)) {
      message.error('组件地址未匹配到页面文件，请填写精确的页面路径')
      return false
    }
    if (formData.value.keepAlive && !formData.value.componentName) {
      message.error('开启缓存时必须填写组件名称')
      return false
    }
  }

  return true
}

const getTree = async () => {
  menuTree.value = []
  const res = await MenuApi.getSimpleMenusList()
  const rootMenu = { id: 0, name: '主目录', children: handleTree(res) }
  menuTree.value.push(rootMenu)
}

const resetForm = () => {
  formData.value = buildDefaultFormData()
  formRef.value?.resetFields()
}

const open = async (type: string, id?: number, parentId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  if (parentId !== undefined) {
    formData.value.parentId = parentId
  }

  if (id) {
    formDetailLoading.value = true
    try {
      formData.value = await MenuApi.getMenu(id)
    } finally {
      formDetailLoading.value = false
    }
  }

  await getTree()
}

defineExpose({ open })

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate()
  if (!valid || !validateRouteBusinessRules()) {
    return
  }

  formSubmitting.value = true
  try {
    const data = formData.value as MenuApi.MenuVO
    if (formType.value === 'create') {
      await MenuApi.createMenu(data)
    } else {
      await MenuApi.updateMenu(data)
    }

    let routeSyncSucceeded = true
    try {
      await syncCurrentSessionPermissionRoutes()
    } catch (error) {
      routeSyncSucceeded = false
      console.error('[system-menu] failed to sync current session routes', error)
    }

    dialogVisible.value = false
    emit('success')

    const successText =
      formType.value === 'create' ? t('common.createSuccess') : t('common.updateSuccess')
    message.success(
      `${successText}${routeSyncSucceeded ? '，当前会话路由已同步刷新' : ''}`
    )
    if (!routeSyncSucceeded) {
      message.warning('菜单已保存，但当前会话路由刷新失败，请点击“刷新菜单缓存”后再访问新菜单')
    }
  } finally {
    formSubmitting.value = false
  }
}
</script>
