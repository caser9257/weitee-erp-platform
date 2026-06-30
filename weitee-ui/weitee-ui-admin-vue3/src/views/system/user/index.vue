<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <div class="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur-sm">
      <div class="mx-auto flex max-w-[1800px] items-center justify-between gap-4 px-4 py-4 lg:px-6">
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-600 shadow-sm">
            <Icon icon="ep:user" />
          </div>
          <div>
            <div class="text-lg font-semibold leading-6 text-slate-900">用户与角色管理</div>
          </div>
        </div>
      </div>
    </div>

    <div class="mx-auto flex max-w-[1800px] flex-col gap-4 px-4 py-4 lg:flex-row lg:px-6">
      <aside class="w-full lg:w-[280px] lg:shrink-0">
        <el-card class="h-full rounded-xl border-slate-200 shadow-sm" shadow="never" body-style="padding:16px;">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="text-sm font-semibold text-slate-800">组织部门</span>
              <el-tag v-if="selectedDeptName" size="small" effect="light" type="info">
                {{ selectedDeptName }}
              </el-tag>
            </div>
          </template>
          <DeptTree ref="deptTreeRef" @node-click="handleDeptNodeClick" />
        </el-card>
      </aside>

      <main class="min-w-0 flex-1 space-y-4">
        <section class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <el-card
            v-for="card in summaryCards"
            :key="card.title"
            class="rounded-xl border-slate-200 shadow-sm"
            shadow="never"
            body-style="padding:16px;"
          >
            <div class="flex items-center justify-between gap-3">
              <div>
                <div class="text-xs font-medium text-slate-500">{{ card.title }}</div>
                <div class="mt-2 text-2xl font-semibold tabular-nums text-slate-900">
                  {{ card.value }}
                </div>
              </div>
              <div :class="['flex h-11 w-11 items-center justify-center rounded-xl', card.toneClass]">
                <Icon :icon="card.icon" />
              </div>
            </div>
          </el-card>
        </section>

        <el-card class="rounded-xl border-slate-200 shadow-sm" shadow="never" body-style="padding:16px;">
          <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
            <div class="flex flex-wrap items-center gap-2">
              <span class="text-sm font-semibold text-slate-800">快速筛选</span>
              <el-tag v-if="selectedDeptName" size="small" effect="light" type="success">
                当前部门：{{ selectedDeptName }}
              </el-tag>
            </div>
          </div>

          <el-form
            ref="queryFormRef"
            :model="queryParams"
            class="grid gap-4 md:grid-cols-2 xl:grid-cols-4"
            label-position="top"
          >
            <el-form-item label="用户名称/昵称" prop="username" class="!mb-0">
              <el-input
                v-model="queryParams.username"
                clearable
                placeholder="请输入用户名称或昵称"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="手机号码" prop="mobile" class="!mb-0">
              <el-input
                v-model="queryParams.mobile"
                clearable
                placeholder="请输入手机号码"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status" class="!mb-0">
              <el-select v-model="queryParams.status" clearable placeholder="全部状态">
                <el-option
                  v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间" prop="createTime" class="!mb-0">
              <el-date-picker
                v-model="queryParams.createTime"
                class="w-full"
                end-placeholder="结束日期"
                range-separator="至"
                start-placeholder="开始日期"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <div class="md:col-span-2 xl:col-span-4 flex justify-end gap-2 pt-1">
              <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card class="rounded-xl border-slate-200 shadow-sm" shadow="never" body-style="padding:16px;">
          <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
            <div class="flex flex-wrap items-center gap-2">
              <span class="text-sm font-semibold text-slate-800">用户列表</span>
              <el-tag size="small" effect="light" type="info">共 {{ total }} 条</el-tag>
              <el-tag v-if="checkedIds.length > 0" size="small" effect="light" type="warning">
                已选 {{ checkedIds.length }} 条
              </el-tag>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <el-button v-if="canDelete" :disabled="!canBatchDelete" :loading="batchDeleting" :icon="Delete" plain type="danger" @click="handleDeleteBatch">
                批量删除
              </el-button>
              <el-button v-if="canImport" :icon="Upload" plain type="warning" @click="handleImport">
                导入
              </el-button>
              <el-button v-if="canExport" :loading="exportLoading" :icon="Download" plain @click="handleExport">
                导出
              </el-button>
              <el-button v-if="canCreate" :icon="Plus" plain type="primary" @click="openForm('create')">
                新增
              </el-button>
            </div>
          </div>

          <el-alert
            v-if="listErrorMessage"
            class="mb-4"
            :title="listErrorMessage"
            type="error"
            show-icon
            :closable="false"
          >
            <template #default>
              <el-button link type="primary" @click="getList">重新加载</el-button>
            </template>
          </el-alert>

          <div class="overflow-x-auto">
            <el-table
              v-loading="listLoading"
              :data="list"
              :header-cell-style="tableHeaderStyle"
              :row-key="(row) => row.id"
              class="user-table"
              empty-text="暂无用户数据"
              @selection-change="handleRowSelectionChange"
            >
              <el-table-column type="selection" width="48" />
              <el-table-column label="用户信息" min-width="220">
                <template #default="{ row }">
                  <div class="flex items-center gap-3">
                    <div class="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-100 bg-slate-50 text-sm font-semibold text-slate-600">
                      {{ row.nickname?.[0] || row.username?.[0] || 'U' }}
                    </div>
                    <div class="min-w-0">
                      <div class="truncate text-sm font-semibold text-slate-900">
                        {{ row.nickname || row.username }}
                      </div>
                      <div class="mt-1 truncate text-xs text-slate-500 font-mono">
                        {{ row.username || '--' }}
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="所属部门" prop="deptName" min-width="160" show-overflow-tooltip />
              <el-table-column label="手机号码" prop="mobile" min-width="140">
                <template #default="{ row }">
                  <span class="font-mono text-slate-700">{{ row.mobile || '--' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <div class="flex items-center gap-2">
                    <el-switch
                      :model-value="row.status"
                      :active-value="CommonStatusEnum.ENABLE"
                      :inactive-value="CommonStatusEnum.DISABLE"
                      :loading="isRowBusy(row.id)"
                      :disabled="!canUpdate || isRowBusy(row.id)"
                      @change="(status) => handleStatusChange(row, status)"
                    />
                    <span
                      :class="[
                        'text-xs font-medium',
                        row.status === CommonStatusEnum.ENABLE ? 'text-emerald-600' : 'text-slate-500'
                      ]"
                    >
                      {{ row.status === CommonStatusEnum.ENABLE ? '启用' : '停用' }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                :formatter="dateFormatter"
                align="center"
                label="创建时间"
                min-width="180"
                prop="createTime"
              />
              <el-table-column align="center" label="操作" min-width="220" fixed="right">
                <template #default="{ row }">
                  <div class="flex items-center justify-center gap-1">
                    <el-button
                      v-if="canUpdate"
                      link
                      type="primary"
                      :disabled="isRowBusy(row.id)"
                      @click="openForm('update', row.id)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      v-if="canAssignRole"
                      link
                      type="success"
                      :disabled="isRowBusy(row.id)"
                      @click="handleRole(row)"
                    >
                      授权角色
                    </el-button>
                    <el-dropdown
                      v-if="canDelete || canResetPwd"
                      :disabled="isRowBusy(row.id)"
                      @command="(command) => handleCommand(command, row)"
                    >
                      <el-button link type="info">
                        更多
                        <Icon class="ml-1" icon="ep:arrow-down" />
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="canResetPwd" command="resetPwd">
                            重置密码
                          </el-dropdown-item>
                          <el-dropdown-item v-if="canDelete" command="delete">
                            删除
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4">
            <div class="text-xs text-slate-500">
              当前显示 {{ list.length }} 条，共 {{ total }} 条
            </div>
            <Pagination
              v-model:limit="queryParams.pageSize"
              v-model:page="queryParams.pageNo"
              :total="total"
              @pagination="handlePagination"
            />
          </div>
        </el-card>
      </main>
    </div>

    <UserForm ref="formRef" @success="handleFormSuccess" />
    <UserImportForm ref="importFormRef" @success="handleImportSuccess" />
    <UserAssignRoleForm ref="assignRoleFormRef" @success="handleAssignRoleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as UserApi from '@/api/system/user'
import UserForm from './UserForm.vue'
import UserImportForm from './UserImportForm.vue'
import UserAssignRoleForm from './UserAssignRoleForm.vue'
import DeptTree from './DeptTree.vue'
import { Download, Delete, Plus, RefreshLeft, Search, Upload } from '@element-plus/icons-vue'

defineOptions({ name: 'SystemUser' })

const message = useMessage()
const { t } = useI18n()

const getDefaultQueryParams = () => ({
  pageNo: 1,
  pageSize: 10,
  username: '',
  mobile: '',
  status: undefined as number | undefined,
  deptId: undefined as number | undefined,
  createTime: [] as string[]
})

const queryParams = reactive(getDefaultQueryParams())
const queryFormRef = ref()
const deptTreeRef = ref()

const list = ref<UserApi.UserVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listErrorMessage = ref('')
const listRequestSeq = ref(0)
const exportLoading = ref(false)
const batchDeleting = ref(false)
const checkedIds = ref<number[]>([])
const selectedDeptName = ref('')
const rowBusyIds = ref<number[]>([])

const canCreate = computed(() => checkPermi(['system:user:create']))
const canImport = computed(() => checkPermi(['system:user:import']))
const canExport = computed(() => checkPermi(['system:user:export']))
const canDelete = computed(() => checkPermi(['system:user:delete']))
const canUpdate = computed(() => checkPermi(['system:user:update']))
const canResetPwd = computed(() => checkPermi(['system:user:update-password']))
const canAssignRole = computed(() => checkPermi(['system:permission:assign-user-role']))
const canBatchDelete = computed(() => canDelete.value && checkedIds.value.length > 0 && !batchDeleting.value)

const summaryCards = computed(() => [
  {
    title: '系统账号总数',
    value: total.value,
    icon: 'ep:user',
    toneClass: 'bg-blue-50 text-blue-600'
  },
  {
    title: '当前页启用',
    value: list.value.filter((item) => item.status === CommonStatusEnum.ENABLE).length,
    icon: 'ep:check',
    toneClass: 'bg-emerald-50 text-emerald-600'
  },
  {
    title: '当前页停用',
    value: list.value.filter((item) => item.status === CommonStatusEnum.DISABLE).length,
    icon: 'ep:close',
    toneClass: 'bg-rose-50 text-rose-600'
  },
  {
    title: '已选记录',
    value: checkedIds.value.length,
    icon: 'ep:select',
    toneClass: 'bg-amber-50 text-amber-600'
  }
])

const tableHeaderStyle = {
  backgroundColor: '#f8fafc',
  color: '#64748b',
  fontWeight: 600,
  height: '44px'
}

const addRowBusy = (id: number) => {
  if (!rowBusyIds.value.includes(id)) {
    rowBusyIds.value.push(id)
  }
}

const removeRowBusy = (id: number) => {
  rowBusyIds.value = rowBusyIds.value.filter((item) => item !== id)
}

const isRowBusy = (id: number) => rowBusyIds.value.includes(id)

const resetQueryModel = () => {
  Object.assign(queryParams, getDefaultQueryParams())
}

const clearSelection = () => {
  checkedIds.value = []
}

const getList = async () => {
  const requestSeq = ++listRequestSeq.value
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await UserApi.getUserPage(queryParams)
    if (requestSeq !== listRequestSeq.value) {
      return
    }
    list.value = data.list || []
    total.value = data.total || 0
    clearSelection()
  } catch (error) {
    if (requestSeq === listRequestSeq.value) {
      listErrorMessage.value = '用户列表加载失败，请重试'
      message.error(listErrorMessage.value)
    }
  } finally {
    if (requestSeq === listRequestSeq.value) {
      listLoading.value = false
    }
  }
}

const handlePagination = async () => {
  await getList()
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  resetQueryModel()
  queryFormRef.value?.clearValidate?.()
  selectedDeptName.value = ''
  deptTreeRef.value?.resetTree?.()
  await getList()
}

const handleDeptNodeClick = async (row?: { id: number; name?: string }) => {
  queryParams.deptId = row?.id
  selectedDeptName.value = row?.name || ''
  queryParams.pageNo = 1
  await getList()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const importFormRef = ref()
const handleImport = () => {
  importFormRef.value?.open()
}

const exportUser = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await UserApi.exportUser(queryParams)
    download.excel(data, '用户数据.xls')
  } finally {
    exportLoading.value = false
  }
}

const handleExport = async () => {
  try {
    await exportUser()
  } catch {}
}

const handleRowSelectionChange = (rows: UserApi.UserVO[]) => {
  checkedIds.value = rows.map((row) => row.id)
}

const handleDelete = async (id: number) => {
  addRowBusy(id)
  try {
    await message.delConfirm()
    await UserApi.deleteUser(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
  finally {
    removeRowBusy(id)
  }
}

const handleDeleteBatch = async () => {
  if (!canBatchDelete.value) {
    return
  }
  batchDeleting.value = true
  try {
    await message.delConfirm()
    await UserApi.deleteUserList(checkedIds.value)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
  finally {
    batchDeleting.value = false
  }
}

const handleResetPwd = async (row: UserApi.UserVO) => {
  addRowBusy(row.id)
  try {
    const result = await message.prompt(`请输入“${row.username}”的新密码`, t('common.reminder'))
    const password = result.value?.trim()
    if (!password) {
      return
    }
    await UserApi.resetUserPassword(row.id, password)
    message.success('密码重置成功')
  } catch {}
  finally {
    removeRowBusy(row.id)
  }
}

const handleStatusChange = async (row: UserApi.UserVO, nextStatus: number) => {
  if (isRowBusy(row.id)) {
    return
  }
  const originStatus = row.status
  addRowBusy(row.id)
  try {
    const actionText = nextStatus === CommonStatusEnum.ENABLE ? '启用' : '停用'
    await message.confirm(`确认要${actionText}“${row.username}”用户吗？`)
    row.status = nextStatus
    await UserApi.updateUserStatus(row.id, nextStatus)
    message.success(t('common.updateSuccess'))
    await getList()
  } catch {
    row.status = originStatus
  } finally {
    removeRowBusy(row.id)
  }
}

const assignRoleFormRef = ref()
const handleRole = (row: UserApi.UserVO) => {
  assignRoleFormRef.value?.open(row)
}

const handleCommand = (command: string, row: UserApi.UserVO) => {
  if (command === 'resetPwd') {
    void handleResetPwd(row)
    return
  }
  if (command === 'delete') {
    void handleDelete(row.id)
  }
}

const handleFormSuccess = async () => {
  await getList()
}

const handleImportSuccess = async () => {
  await getList()
}

const handleAssignRoleSuccess = async () => {
  await getList()
}

onMounted(() => {
  void getList()
})
</script>

<style scoped>
.user-table :deep(.el-table__cell) {
  padding-top: 12px;
  padding-bottom: 12px;
}
</style>
