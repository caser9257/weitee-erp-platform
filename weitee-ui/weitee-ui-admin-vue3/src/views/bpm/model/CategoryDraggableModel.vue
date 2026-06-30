<template>
  <div class="flex items-center h-50px" v-memo="[categoryInfo.name, isCategorySorting]">
    <!-- 头部：分类名 -->
    <div class="flex items-center">
      <el-tooltip content="拖动排序" v-if="isCategorySorting">
        <Icon
          :size="22"
          icon="ic:round-drag-indicator"
          class="ml-10px category-drag-icon cursor-move text-#8a909c"
        />
      </el-tooltip>
      <h3 class="ml-20px mr-8px text-18px">{{ categoryInfo.name }}</h3>
      <div class="color-gray-600 text-16px">({{ categoryInfo.modelList?.length || 0 }})</div>
    </div>
    <!-- 头部：操作 -->
    <div class="flex-1 flex" v-show="!isCategorySorting">
      <div
        v-if="categoryInfo.modelList.length > 0"
        class="ml-20px flex items-center"
        :class="[
          'transition-transform duration-300 cursor-pointer',
          isExpand ? 'rotate-180' : 'rotate-0'
        ]"
        @click="isExpand = !isExpand"
      >
        <Icon icon="ep:arrow-down-bold" color="#999" />
      </div>
      <div class="ml-auto flex items-center" :class="isModelSorting ? 'mr-15px' : 'mr-45px'">
        <template v-if="!isModelSorting">
          <el-button
            v-if="categoryInfo.modelList.length > 0"
            link
            type="info"
            class="mr-20px"
            @click.stop="handleModelSort"
          >
            <Icon icon="fa:sort-amount-desc" class="mr-5px" />
            排序
          </el-button>
          <el-button v-else link type="info" class="mr-20px" @click.stop="openModelForm('create')">
            <Icon icon="fa:plus" class="mr-5px" />
            新建
          </el-button>
          <el-dropdown
            @command="(command) => handleCategoryCommand(command, categoryInfo)"
            placement="bottom"
          >
            <el-button link type="info">
              <Icon icon="ep:setting" class="mr-5px" />
              分类
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="handleRename">重命名</el-dropdown-item>
                <el-dropdown-item command="handleDeleteCategory">删除该类</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click.stop="handleModelSortCancel">取消</el-button>
          <el-button type="primary" @click.stop="handleModelSortSubmit">保存排序</el-button>
        </template>
      </div>
    </div>
  </div>

  <!-- 模型列表 -->
  <el-collapse-transition>
    <div v-show="isExpand">
      <el-table
        v-if="modelList && modelList.length > 0"
        :class="categoryInfo.name"
        ref="tableRef"
        :data="modelList"
        row-key="id"
        :header-cell-style="tableHeaderStyle"
        :cell-style="tableCellStyle"
        :row-style="{ height: '68px' }"
      >
        <el-table-column label="流程名" prop="name" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center">
              <el-tooltip content="拖动排序" v-if="isModelSorting">
                <Icon
                  icon="ic:round-drag-indicator"
                  class="drag-icon cursor-move text-#8a909c mr-10px"
                />
              </el-tooltip>
              <el-image v-if="row.icon" :src="row.icon" class="h-38px w-38px mr-10px rounded" />
              <div v-else class="flow-icon">
                <span style="font-size: 12px; color: #fff">{{ subString(row.name, 0, 2) }}</span>
              </div>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="可见范围" prop="startUserIds" min-width="150">
          <template #default="{ row }">
            <el-text v-if="!row.startUsers?.length && !row.startDepts?.length">全部可见</el-text>
            <el-text v-else-if="row.startUsers.length === 1">
              {{ row.startUsers[0].nickname }}
            </el-text>
            <el-text v-else-if="row.startDepts?.length === 1">
              {{ row.startDepts[0].name }}
            </el-text>
            <el-text v-else-if="row.startDepts?.length > 1">
              <el-tooltip
                class="box-item"
                effect="dark"
                placement="top"
                :content="row.startDepts.map((dept: any) => dept.name).join('、')"
              >
                {{ row.startDepts[0].name }}等 {{ row.startDepts.length }} 个部门可见
              </el-tooltip>
            </el-text>
            <el-text v-else>
              <el-tooltip
                class="box-item"
                effect="dark"
                placement="top"
                :content="row.startUsers.map((user: any) => user.nickname).join('、')"
              >
                {{ row.startUsers[0].nickname }}等 {{ row.startUsers.length }} 人可见
              </el-tooltip>
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="流程类型" prop="type" min-width="120">
          <template #default="{ row }">
            <dict-tag :value="row.type" :type="DICT_TYPE.BPM_MODEL_TYPE" />
          </template>
        </el-table-column>
        <el-table-column label="表单信息" prop="formType" min-width="150">
          <template #default="scope">
            <el-button
              v-if="scope.row.formType === BpmModelFormType.NORMAL"
              type="primary"
              link
              @click="handleFormDetail(scope.row)"
            >
              <span>{{ scope.row.formName }}</span>
            </el-button>
            <el-button
              v-else-if="scope.row.formType === BpmModelFormType.CUSTOM"
              type="primary"
              link
              @click="handleFormDetail(scope.row)"
            >
              <span>{{ scope.row.formCustomCreatePath }}</span>
            </el-button>
            <label v-else>暂无表单</label>
          </template>
        </el-table-column>
        <el-table-column label="最后发布" prop="deploymentTime" min-width="250">
          <template #default="scope">
            <div class="flex items-center gap-8px flex-wrap">
              <span v-if="scope.row.processDefinition" class="w-150px">
                {{ formatDate(scope.row.processDefinition.deploymentTime) }}
              </span>
              <el-tag v-if="scope.row.processDefinition">
                v{{ scope.row.processDefinition.version }}
              </el-tag>
              <el-tag v-else type="warning">未部署</el-tag>
              <el-tag
                v-if="scope.row.processDefinition?.suspensionState === 2"
                type="warning"
              >
                已停用
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openModelForm('update', scope.row.id)"
              :disabled="!canManageModel(scope.row) || !hasPermiUpdate"
            >
              修改
            </el-button>
            <el-button
              link
              type="primary"
              @click="openModelForm('copy', scope.row.id)"
              :disabled="!canManageModel(scope.row) || !hasPermiUpdate"
            >
              复制
            </el-button>
            <el-button
              v-if="showDeployButton(scope.row)"
              link
              class="!ml-5px"
              type="primary"
              @click="handleDeploy(scope.row)"
            >
              发布
            </el-button>
            <el-dropdown
              class="!align-middle ml-5px"
              @command="(command) => handleModelCommand(command, scope.row)"
              v-if="hasPermiMore"
            >
              <el-button type="primary" link>更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="handleDefinitionList" v-if="hasPermiPdQuery">
                    历史
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="handleReport"
                    v-if="
                      checkPermi(['bpm:process-instance:manager-query']) &&
                      scope.row.processDefinition
                    "
                    :disabled="!canManageModel(scope.row)"
                  >
                    报表
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="handleChangeState"
                    v-if="hasPermiUpdate && scope.row.processDefinition"
                    :disabled="!canManageModel(scope.row)"
                  >
                    {{ scope.row.processDefinition.suspensionState === 1 ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    type="danger"
                    command="handleClean"
                    v-if="checkPermi(['bpm:model:clean'])"
                    :disabled="!canManageModel(scope.row)"
                  >
                    清理
                  </el-dropdown-item>
                  <el-dropdown-item
                    type="danger"
                    command="handleDelete"
                    v-if="hasPermiDelete"
                    :disabled="!canManageModel(scope.row)"
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-collapse-transition>

  <!-- 弹窗：重命名分类 -->
  <Dialog :fullscreen="false" class="rename-dialog" v-model="renameCategoryVisible" width="400">
    <template #title>
      <div class="pl-10px font-bold text-18px">重命名分类</div>
    </template>
    <div class="px-30px">
      <el-input v-model="renameCategoryForm.name" />
    </div>
    <template #footer>
      <div class="pr-25px pb-25px">
        <el-button @click="renameCategoryVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRenameConfirm">确定</el-button>
      </div>
    </template>
  </Dialog>

  <!-- 弹窗：表单详情 -->
  <Dialog title="表单详情" :fullscreen="true" v-model="formDetailVisible">
    <form-create :rule="formDetailPreview.rule" :option="formDetailPreview.option" />
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { CategoryApi, CategoryVO } from '@/api/bpm/category'
import Sortable from 'sortablejs'
import { formatDate } from '@/utils/formatTime'
import * as ModelApi from '@/api/bpm/model'
import * as FormApi from '@/api/bpm/form'
import { setConfAndFields2 } from '@/utils/formCreate'
import { BpmModelFormType } from '@/utils/constants'
import { checkPermi } from '@/utils/permission'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'
import { cloneDeep, isEqual } from 'lodash-es'
import { useDebounceFn } from '@vueuse/core'
import { subString } from '@/utils/index'

defineOptions({ name: 'BpmModel' })

interface UserInfo {
  nickname: string
  [key: string]: any
}

interface ProcessDefinition {
  id?: string
  deploymentTime: string
  version: number
  suspensionState: number
}

interface DeptInfo {
  name: string
}

interface ModelInfo {
  id: number
  name: string
  icon?: string
  startUsers?: UserInfo[]
  startDepts?: DeptInfo[]
  processDefinition?: ProcessDefinition
  formType?: number
  formId?: number
  formName?: string
  formCustomCreatePath?: string
  managerUserIds?: number[]
  hasUnpublishedChanges?: boolean
  key?: string
  [key: string]: any
}

interface CategoryInfoProps {
  id: number
  name: string
  modelList: ModelInfo[]
}

const props = defineProps<{
  categoryInfo: CategoryInfoProps
  isCategorySorting: boolean
}>()

const emit = defineEmits(['success'])
const message = useMessage()
const { t } = useI18n()
const { push } = useRouter()
const userStore = useUserStoreWithOut()
const isDark = computed(() => useAppStore().getIsDark)
const router = useRouter()
const isSuperAdmin = computed(() => userStore.getRoles.includes('super_admin'))

const isModelSorting = ref(false)
const originalData = ref<ModelInfo[]>([])
const modelList = ref<ModelInfo[]>([])
const isExpand = ref(false)

const tableHeaderStyle = computed(() => ({
  backgroundColor: isDark.value ? '' : '#edeff0',
  paddingLeft: '10px'
}))

const tableCellStyle = computed(() => ({
  paddingLeft: '10px'
}))

const hasPermiUpdate = computed(() => {
  return checkPermi(['bpm:model:update'])
})
const hasPermiDelete = computed(() => {
  return checkPermi(['bpm:model:delete'])
})
const hasPermiDeploy = computed(() => {
  return checkPermi(['bpm:model:deploy'])
})
const hasPermiMore = computed(() => {
  return checkPermi(['bpm:process-definition:query', 'bpm:model:update', 'bpm:model:delete'])
})
const hasPermiPdQuery = computed(() => {
  return checkPermi(['bpm:process-definition:query'])
})

const handleModelCommand = (command: string, row: ModelInfo) => {
  switch (command) {
    case 'handleDefinitionList':
      handleDefinitionList(row)
      break
    case 'handleDelete':
      handleDelete(row)
      break
    case 'handleChangeState':
      handleChangeState(row)
      break
    case 'handleClean':
      handleClean(row)
      break
    case 'handleReport':
      router.push({
        name: 'BpmProcessInstanceReport',
        query: {
          processDefinitionId: row.processDefinition?.id,
          processDefinitionKey: row.key
        }
      })
      break
    default:
      break
  }
}

const handleCategoryCommand = async (command: string, row: CategoryInfoProps) => {
  switch (command) {
    case 'handleRename':
      renameCategoryForm.value = await CategoryApi.getCategory(row.id)
      renameCategoryVisible.value = true
      break
    case 'handleDeleteCategory':
      await handleDeleteCategory()
      break
    default:
      break
  }
}

const handleDelete = async (row: ModelInfo) => {
  try {
    await message.delConfirm()
    await ModelApi.deleteModel(row.id)
    message.success(t('common.delSuccess'))
    emit('success')
  } catch {}
}

const handleClean = async (row: ModelInfo) => {
  try {
    await message.confirm(`是否确认清理流程名字为"${row.name}"的数据项?`)
    await ModelApi.cleanModel(row.id)
    message.success('清理成功')
    emit('success')
  } catch {}
}

const handleChangeState = async (row: ModelInfo) => {
  const state = row.processDefinition!.suspensionState
  const newState = state === 1 ? 2 : 1
  try {
    const statusState = state === 1 ? '停用' : '启用'
    const content = `是否确认${statusState}流程名字为"${row.name}"的数据项?`
    await message.confirm(content)
    await ModelApi.updateModelState(row.id, newState)
    message.success(statusState + '成功')
    emit('success')
  } catch {}
}

const handleDeploy = async (row: ModelInfo) => {
  try {
    await message.confirm('是否确认发布该流程？')
    await ModelApi.deployModel(row.id)
    message.success(t('发布成功'))
    emit('success')
  } catch {}
}

const handleDefinitionList = (row: ModelInfo) => {
  push({
    name: 'BpmProcessDefinition',
    query: {
      key: row.key
    }
  })
}

const formDetailVisible = ref(false)
const formDetailPreview = ref({
  rule: [],
  option: {}
})
const handleFormDetail = async (row: ModelInfo) => {
  if (row.formType === BpmModelFormType.NORMAL) {
    const data = await FormApi.getForm(row.formId!)
    setConfAndFields2(formDetailPreview, data.conf, data.fields)
    formDetailVisible.value = true
  } else {
    await push({
      path: row.formCustomCreatePath
    })
  }
}

const isManagerUser = (row: ModelInfo) => {
  const userId = userStore.getUser.id
  return Boolean(row.managerUserIds?.includes(userId))
}

const canManageModel = (row: ModelInfo) => {
  return isSuperAdmin.value || isManagerUser(row)
}

const showDeployButton = (row: ModelInfo) => {
  return (
    hasPermiDeploy.value &&
    canManageModel(row) &&
    (row.hasUnpublishedChanges ?? !row.processDefinition)
  )
}

const handleModelSort = () => {
  if (isModelSorting.value) {
    handleModelSortCancel()
  } else {
    originalData.value = cloneDeep(props.categoryInfo.modelList)
    isModelSorting.value = true
    initSort()
  }
}

const handleModelSortSubmit = async () => {
  const ids = modelList.value.map((item) => item.id)
  await ModelApi.updateModelSortBatch(ids)
  isModelSorting.value = false
  message.success('排序模型成功')
  emit('success')
}

const handleModelSortCancel = () => {
  modelList.value = cloneDeep(originalData.value)
  isModelSorting.value = false
}

const tableRef = ref()
const initSort = useDebounceFn(() => {
  const table = document.querySelector(`.${props.categoryInfo.name} .el-table__body-wrapper tbody`)
  if (!table) return

  Sortable.create(table, {
    group: 'shared',
    animation: 150,
    draggable: '.el-table__row',
    handle: '.drag-icon',
    onEnd: ({ newDraggableIndex, oldDraggableIndex }) => {
      if (newDraggableIndex == null || oldDraggableIndex == null) {
        return
      }
      if (oldDraggableIndex !== newDraggableIndex) {
        modelList.value.splice(
          newDraggableIndex,
          0,
          modelList.value.splice(oldDraggableIndex, 1)[0]
        )
      }
    }
  })
}, 200)

const updateModeList = useDebounceFn(() => {
  const newModelList = props.categoryInfo.modelList
  if (!isEqual(modelList.value, newModelList)) {
    modelList.value = cloneDeep(newModelList)
    if (newModelList?.length > 0) {
      isExpand.value = true
    }
  }
}, 100)

const renameCategoryVisible = ref(false)
const renameCategoryForm = ref({
  name: ''
})
const handleRenameConfirm = async () => {
  if (renameCategoryForm.value?.name.length === 0) {
    return message.warning('请输入名称')
  }
  await CategoryApi.updateCategory(renameCategoryForm.value as CategoryVO)
  message.success('重命名成功')
  renameCategoryVisible.value = false
  emit('success')
}

const handleDeleteCategory = async () => {
  try {
    if (props.categoryInfo.modelList.length > 0) {
      return message.warning('该分类下仍有流程定义，不允许删除')
    }
    await message.confirm('确认删除分类吗?')
    await CategoryApi.deleteCategory(props.categoryInfo.id)
    message.success(t('common.delSuccess'))
    emit('success')
  } catch {}
}

const openModelForm = async (type: string, id?: number) => {
  if (type === 'create') {
    await push({ name: 'BpmModelCreate' })
  } else {
    await push({
      name: 'BpmModelUpdate',
      params: { id, type }
    })
  }
}

watchEffect(() => {
  if (props.categoryInfo?.modelList) {
    updateModeList()
  }

  if (props.isCategorySorting) {
    isExpand.value = false
  }
})
</script>

<style lang="scss">
.rename-dialog.el-dialog {
  padding: 0 !important;

  .el-dialog__header {
    border-bottom: none;
  }

  .el-dialog__footer {
    border-top: none !important;
  }
}
</style>
<style lang="scss" scoped>
.flow-icon {
  display: flex;
  width: 38px;
  height: 38px;
  margin-right: 10px;
  background-color: var(--el-color-primary);
  border-radius: 0.25rem;
  align-items: center;
  justify-content: center;
}

.category-draggable-model {
  :deep(.el-table__cell) {
    overflow: hidden;
    border-bottom: none !important;
  }

  :deep(.el-table__body) {
    will-change: transform;
    transform: translateZ(0);
  }
}
</style>
