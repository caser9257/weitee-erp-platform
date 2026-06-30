<template>
  <el-drawer
    v-model="drawerVisible"
    :append-to-body="true"
    :destroy-on-close="true"
    :modal-class="'user-role-drawer-modal'"
    :size="drawerSize"
    :with-header="false"
    class="user-role-drawer"
  >
    <div class="flex h-full flex-col bg-slate-50">
      <div class="flex items-center justify-between border-b border-slate-200 bg-white px-5 py-4">
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-600 shadow-sm">
            <Icon icon="ep:user" />
          </div>
          <div>
            <div class="text-base font-semibold text-slate-900">用户角色与权限配置</div>
          </div>
        </div>
        <el-button circle text @click="drawerVisible = false">
          <Icon icon="ep:close" />
        </el-button>
      </div>

      <div class="flex-1 overflow-y-auto px-5 py-4">
        <el-alert
          v-if="loadError"
          class="mb-4"
          :closable="false"
          :title="loadError"
          type="error"
          show-icon
        >
          <template #default>
            <el-button link type="primary" @click="reloadDrawerData">重新加载</el-button>
          </template>
        </el-alert>

        <div class="rounded-2xl bg-slate-800 px-5 py-5 text-white shadow-lg shadow-slate-200/60">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <div class="text-xs text-slate-300">当前用户</div>
              <div class="mt-2 truncate text-2xl font-semibold leading-8">
                {{ formData.nickname || formData.username || '未选择用户' }}
              </div>
              <div class="mt-1 truncate text-sm text-slate-300 font-mono">
                {{ formData.username || '--' }}
              </div>
            </div>
            <el-tag
              :effect="'light'"
              :type="formData.status === CommonStatusEnum.ENABLE ? 'success' : 'danger'"
            >
              {{ formData.status === CommonStatusEnum.ENABLE ? '启用' : '停用' }}
            </el-tag>
          </div>

          <div class="mt-5 grid gap-3 sm:grid-cols-2">
            <div class="rounded-xl bg-white/10 px-4 py-3">
              <div class="text-[11px] text-slate-300">所属部门</div>
              <div class="mt-1 truncate text-sm font-medium">
                {{ formData.deptName || '--' }}
              </div>
            </div>
            <div class="rounded-xl bg-white/10 px-4 py-3">
              <div class="text-[11px] text-slate-300">手机号码</div>
              <div class="mt-1 truncate text-sm font-medium">
                {{ formData.mobile || '--' }}
              </div>
            </div>
            <div class="rounded-xl bg-white/10 px-4 py-3">
              <div class="text-[11px] text-slate-300">用户编号</div>
              <div class="mt-1 truncate text-sm font-medium font-mono">
                {{ formData.id > 0 ? formData.id : '--' }}
              </div>
            </div>
            <div class="rounded-xl bg-white/10 px-4 py-3">
              <div class="text-[11px] text-slate-300">创建时间</div>
              <div class="mt-1 truncate text-sm font-medium">
                {{ formatDate(formData.createTime) }}
              </div>
            </div>
          </div>
        </div>

        <div class="mt-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-3 flex items-center justify-between gap-3">
            <div>
              <div class="text-sm font-semibold text-slate-900">角色授权</div>
            </div>
            <el-tag size="small" effect="light" type="info">
              共 {{ roleList.length }} 个角色
            </el-tag>
          </div>

          <div v-loading="loadingData" class="min-h-[220px]">
            <template v-if="!loadingData && roleList.length > 0">
              <div class="space-y-2.5">
                <div
                  v-for="role in roleList"
                  :key="role.id"
                  :class="[
                    'group flex cursor-pointer items-start gap-3 rounded-xl border px-4 py-3 transition-all',
                    formData.roleIds.includes(role.id)
                      ? 'border-blue-200 bg-blue-50/60'
                      : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'
                  ]"
                  @click="toggleRole(role.id)"
                >
                  <div
                    :class="[
                      'mt-0.5 flex h-5 w-5 items-center justify-center rounded-md border text-white transition-all',
                      formData.roleIds.includes(role.id)
                        ? 'border-blue-600 bg-blue-600'
                        : 'border-slate-300 bg-white text-transparent'
                    ]"
                  >
                    <Icon icon="ep:check" class="text-[12px]" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="flex flex-wrap items-center gap-2">
                      <span
                        :class="[
                          'text-sm font-semibold',
                          formData.roleIds.includes(role.id) ? 'text-blue-700' : 'text-slate-800'
                        ]"
                      >
                        {{ role.name }}
                      </span>
                      <el-tag
                        v-if="role.code === 'admin'"
                        size="small"
                        effect="light"
                        type="danger"
                      >
                        高权限
                      </el-tag>
                    </div>
                    <div class="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500">
                      <span class="font-mono">标识：{{ role.code }}</span>
                      <span class="font-mono">排序：{{ role.sort }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <el-empty
              v-else-if="!loadingData && !loadError"
              description="暂无可分配角色"
              :image-size="72"
            />
          </div>
        </div>
      </div>

      <div class="border-t border-slate-200 bg-white px-5 py-4">
        <div class="flex justify-end gap-2">
          <el-button :disabled="submitting" @click="drawerVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!canSubmit"
            @click="submitForm"
          >
            保存设置
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { CommonStatusEnum } from '@/utils/constants'
import * as PermissionApi from '@/api/system/permission'
import * as RoleApi from '@/api/system/role'
import * as UserApi from '@/api/system/user'

defineOptions({ name: 'SystemUserAssignRoleForm' })

const { t } = useI18n()
const message = useMessage()
const { width } = useWindowSize()

const drawerVisible = ref(false)
const loadingData = ref(false)
const submitting = ref(false)
const loadError = ref('')
const loadRequestSeq = ref(0)
const currentUser = ref<UserApi.UserVO | null>(null)
const roleList = ref<RoleApi.RoleVO[]>([])
const formData = ref({
  id: -1,
  username: '',
  nickname: '',
  deptName: '',
  mobile: '',
  status: CommonStatusEnum.ENABLE,
  createTime: '' as Date | string,
  roleIds: [] as number[]
})

const drawerSize = computed(() => {
  if (width.value < 768) {
    return '100%'
  }
  if (width.value < 1280) {
    return '460px'
  }
  return '520px'
})

const canSubmit = computed(() => {
  return !!formData.value.id && !loadingData.value && !submitting.value && !loadError.value
})

const formatDate = (value: Date | string | undefined) => {
  if (!value) {
    return '--'
  }
  const date = typeof value === 'string' ? new Date(value) : value
  if (Number.isNaN(date.getTime())) {
    return '--'
  }
  return date.toLocaleString('zh-CN', { hour12: false })
}

const resetForm = () => {
  currentUser.value = null
  roleList.value = []
  loadError.value = ''
  loadingData.value = false
  submitting.value = false
  formData.value = {
    id: -1,
    username: '',
    nickname: '',
    deptName: '',
    mobile: '',
    status: CommonStatusEnum.ENABLE,
    createTime: '',
    roleIds: []
  }
}

const reloadDrawerData = async () => {
  if (!currentUser.value) {
    return
  }
  const requestSeq = ++loadRequestSeq.value
  loadingData.value = true
  loadError.value = ''
  try {
    const [roleIds, roles] = await Promise.all([
      PermissionApi.getUserRoleList(currentUser.value.id),
      RoleApi.getSimpleRoleList()
    ])
    if (requestSeq !== loadRequestSeq.value) {
      return
    }
    formData.value.roleIds = roleIds || []
    roleList.value = [...(roles || [])].sort((a, b) => (a.sort || 0) - (b.sort || 0))
  } catch (error) {
    if (requestSeq !== loadRequestSeq.value) {
      return
    }
    loadError.value = '角色数据加载失败，请重试'
    message.error(loadError.value)
  } finally {
    if (requestSeq === loadRequestSeq.value) {
      loadingData.value = false
    }
  }
}

const open = async (row: UserApi.UserVO) => {
  drawerVisible.value = true
  resetForm()
  currentUser.value = row
  formData.value = {
    id: row.id,
    username: row.username || '',
    nickname: row.nickname || '',
    deptName: row.deptName || '',
    mobile: row.mobile || '',
    status: row.status ?? CommonStatusEnum.ENABLE,
    createTime: row.createTime || '',
    roleIds: []
  }
  await reloadDrawerData()
}

defineExpose({ open })

const toggleRole = (roleId: number) => {
  if (!canSubmit.value) {
    return
  }
  if (formData.value.roleIds.includes(roleId)) {
    formData.value.roleIds = formData.value.roleIds.filter((item) => item !== roleId)
    return
  }
  formData.value.roleIds = [...formData.value.roleIds, roleId]
}

const emit = defineEmits(['success'])

const submitForm = async () => {
  if (!canSubmit.value || !formData.value.id) {
    return
  }
  submitting.value = true
  try {
    await PermissionApi.assignUserRole({
      userId: formData.value.id,
      roleIds: formData.value.roleIds
    })
    message.success(t('common.updateSuccess'))
    drawerVisible.value = false
    emit('success', true)
  } finally {
    submitting.value = false
  }
}

watch(
  () => drawerVisible.value,
  (visible) => {
    if (!visible) {
      loadRequestSeq.value += 1
      resetForm()
    }
  }
)
</script>

<style scoped>
.user-role-drawer-modal {
  backdrop-filter: blur(4px);
}
</style>
