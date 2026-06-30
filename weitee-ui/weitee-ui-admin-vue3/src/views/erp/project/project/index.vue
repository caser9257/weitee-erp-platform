<template>
  <doc-alert :title="pageDocTitle" url="https://doc.iocoder.cn/erp/project/" />

  <ContentWrap class="search-wrap">
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="88px"
      class="-mb-15px"
    >
      <el-form-item label="项目编号" prop="no">
        <el-input
          v-model="queryParams.no"
          placeholder="请输入项目编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="请选择业务类型" clearable>
          <el-option label="自研产品" value="SELF_RESEARCH" />
          <el-option label="客供料" value="CUSTOMER_SUPPLIED" />
          <el-option label="来料加工" value="TOLL_MANUFACTURING" />
          <el-option label="工艺验证" value="PROCESS_VALIDATION" />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="请选择风险等级" clearable>
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />查询
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />重置
        </el-button>
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:project:create']">
          <Icon icon="ep:plus" class="mr-5px" />{{ createButtonText }}
        </el-button>
        <el-button
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['erp:project:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      class="weitai-project-table"
      @row-click="openDetailDrawer"
    >
      <el-table-column label="项目编号" prop="no" min-width="140" fixed="left" />
      <el-table-column label="项目名称 / 阶段" min-width="220" fixed="left">
        <template #default="{ row }">
          <div class="font-600 text-[var(--el-text-color-primary)]">{{ row.name }}</div>
          <div class="mt-4px text-12px text-[var(--el-text-color-secondary)]">
            当前阶段：{{ row.currentStageCode || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getBusinessTypeTagType(row.businessType)">
            {{ getBusinessTypeLabel(row.businessType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="客户" prop="customerName" min-width="140" />
      <el-table-column label="PC负责人" min-width="120">
        <template #default="{ row }">
          {{ row.planCoordinatorName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="PC状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getRoleStatusType(row.pcStatus)">
            {{ getRoleStatusLabel(row.pcStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="MC负责人" min-width="120">
        <template #default="{ row }">
          {{ row.materialControllerName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="MC状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getRoleStatusType(row.mcStatus)">
            {{ getRoleStatusLabel(row.mcStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险等级" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="getRiskTagType(row.riskLevel)">
            {{ getRiskLabel(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="交期"
        prop="deliveryDate"
        align="center"
        width="120"
        :formatter="dateFormatter2"
      />
      <el-table-column label="状态" prop="status" align="center" width="100">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click.stop="openForm('update', row.id)"
            v-hasPermi="['erp:project:update']"
          >
            编辑
          </el-button>
          <el-button link @click.stop="openDetailDrawer(row)" v-hasPermi="['erp:project:query']">
            详情
          </el-button>
          <el-button
            link
            type="danger"
            @click.stop="handleDelete(row.id)"
            v-hasPermi="['erp:project:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      class="mt-16px"
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <ProjectForm ref="formRef" @success="getList" />

  <el-drawer
    v-model="detailDrawerVisible"
    :title="detailDrawerTitle"
    size="980px"
    destroy-on-close
  >
    <div v-loading="detailLoading" class="project-detail-drawer">
      <template v-if="detailProject">
        <div class="overview-hero">
          <div>
            <div class="text-22px font-700 leading-30px">{{ detailProject.name }}</div>
            <div class="mt-8px flex flex-wrap gap-8px text-13px text-[var(--el-text-color-secondary)]">
              <span>项目编号：{{ detailProject.no }}</span>
              <span>业务类型：{{ getBusinessTypeLabel(detailProject.businessType) }}</span>
              <span>客户：{{ detailProject.customerName || '-' }}</span>
              <span>交期：{{ formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD') }}</span>
            </div>
          </div>
          <div class="hero-side">
            <el-tag :type="getRiskTagType(detailProject.riskLevel)" size="large">
              风险 {{ getRiskLabel(detailProject.riskLevel) }}
            </el-tag>
            <div class="mt-10px text-13px text-[var(--el-text-color-secondary)]">
              当前阶段：{{ detailProject.currentStageCode || '-' }}
            </div>
          </div>
        </div>

        <el-row :gutter="16" class="mb-16px">
          <el-col :span="12">
            <el-card shadow="never" class="role-card role-card-pc">
              <template #header>
                <div class="role-card-header">
                  <span>PC 协同</span>
                  <el-tag size="small" :type="getRoleStatusType(detailProject.pcStatus)">
                    {{ getRoleStatusLabel(detailProject.pcStatus) }}
                  </el-tag>
                </div>
              </template>
              <div class="role-line">
                <span class="label">负责人</span>
                <span class="value">{{ detailProject.planCoordinatorName || '-' }}</span>
              </div>
              <div class="role-line">
                <span class="label">确认时间</span>
                <span class="value">{{ formatDateText(detailProject.pcConfirmTime) }}</span>
              </div>
              <div class="role-line role-line-top">
                <span class="label">备注</span>
                <span class="value">{{ detailProject.pcRemark || '暂无' }}</span>
              </div>
              <div class="role-actions">
                <el-button
                  type="primary"
                  :disabled="pcConfirmDisabled"
                  :loading="pcConfirmSubmitting"
                  @click="openPcConfirmDialog"
                  v-hasPermi="['erp:project:pc-confirm']"
                >
                  PC确认
                </el-button>
                <span class="hint">仅当前 PC 负责人且状态未完成时可确认</span>
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="role-card role-card-mc">
              <template #header>
                <div class="role-card-header">
                  <span>MC 协同</span>
                  <el-tag size="small" :type="getRoleStatusType(detailProject.mcStatus)">
                    {{ getRoleStatusLabel(detailProject.mcStatus) }}
                  </el-tag>
                </div>
              </template>
              <div class="role-line">
                <span class="label">负责人</span>
                <span class="value">{{ detailProject.materialControllerName || '-' }}</span>
              </div>
              <div class="role-line">
                <span class="label">确认时间</span>
                <span class="value">{{ formatDateText(detailProject.mcConfirmTime) }}</span>
              </div>
              <div class="role-line role-line-top">
                <span class="label">备注</span>
                <span class="value">{{ detailProject.mcRemark || '暂无' }}</span>
              </div>
              <div class="role-actions">
                <el-button
                  type="success"
                  :disabled="mcConfirmDisabled"
                  :loading="mcConfirmSubmitting"
                  @click="openMcConfirmDialog"
                  v-hasPermi="['erp:project:mc-confirm']"
                >
                  MC确认
                </el-button>
                <span class="hint">仅当前 MC 负责人且状态未完成时可确认</span>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="协同任务" name="coordination">
            <el-card shadow="never">
              <template #header>
                <div class="section-header">
                  <span>待办任务</span>
                  <el-tag size="small" type="info">{{ detailTodoTasks.length }} 条</el-tag>
                </div>
              </template>

              <template v-if="detailTodoTasks.length">
                <el-table :data="detailTodoTasks" size="small" border>
                  <el-table-column label="角色" width="90" align="center">
                    <template #default="{ row }">
                      {{ getTaskRoleLabel(row.roleCode) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag size="small" :type="getTaskStatusType(row.taskStatus)">
                        {{ getTaskStatusLabel(row.taskStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="任务类型" prop="taskType" min-width="220" />
                  <el-table-column label="任务摘要" prop="summary" min-width="280" />
                  <el-table-column label="截止时间" width="180" align="center">
                    <template #default="{ row }">
                      {{ formatDateText(row.dueTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="备注" prop="remark" min-width="180" />
                </el-table>
              </template>
              <el-empty v-else description="当前没有待办任务" />
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="项目概览" name="overview">
            <el-card shadow="never">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="项目编号">{{ detailProject.no || '-' }}</el-descriptions-item>
                <el-descriptions-item label="项目名称">{{ detailProject.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="项目类型">
                  {{ detailProject.projectType || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="业务类型">
                  {{ getBusinessTypeLabel(detailProject.businessType) }}
                </el-descriptions-item>
                <el-descriptions-item label="来源项目">
                  {{ detailProject.sourceProjectId || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="销售订单">
                  {{ detailProject.saleOrderId || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="客户">
                  {{ detailProject.customerName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="交期">
                  {{ formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD') }}
                </el-descriptions-item>
                <el-descriptions-item label="风险等级">
                  {{ getRiskLabel(detailProject.riskLevel) }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatDateText(detailProject.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="项目备注" :span="2">
                  {{ detailProject.remark || '暂无' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </template>
      <el-empty v-else description="暂无项目详情" />
    </div>
  </el-drawer>

  <Dialog title="PC确认" v-model="pcConfirmDialogVisible" width="520px">
    <el-form
      ref="pcConfirmFormRef"
      :model="pcConfirmForm"
      :rules="pcConfirmRules"
      label-width="100px"
    >
      <el-form-item label="当前阶段" prop="currentStageCode">
        <el-input v-model="pcConfirmForm.currentStageCode" placeholder="请输入阶段编码" />
      </el-form-item>
      <el-form-item label="确认备注" prop="remark">
        <el-input
          v-model="pcConfirmForm.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入 PC 确认备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pcConfirmDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="pcConfirmSubmitting" @click="submitPcConfirm">
        确认提交
      </el-button>
    </template>
  </Dialog>

  <Dialog title="MC确认" v-model="mcConfirmDialogVisible" width="520px">
    <el-form
      ref="mcConfirmFormRef"
      :model="mcConfirmForm"
      :rules="mcConfirmRules"
      label-width="100px"
    >
      <el-form-item label="确认备注" prop="remark">
        <el-input
          v-model="mcConfirmForm.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入 MC 确认备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="mcConfirmDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="mcConfirmSubmitting" @click="submitMcConfirm">
        确认提交
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter2, formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { ProjectApi } from '@/api/erp/project'
import {
  ProjectCenterApi,
  type ProjectMcConfirmReqVO,
  type ProjectPcConfirmReqVO,
  type ProjectRoleTaskVO,
  type ProjectVO
} from '@/api/erp/project/projectCenter'
import ProjectForm from './ProjectForm.vue'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'ErpProject' })

const message = useMessage()
const { t } = useI18n()
const userStore = useUserStore()
const route = useRoute()

const queryFormRef = ref<FormInstance>()
const formRef = ref()

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<ProjectVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined as string | undefined,
  name: undefined as string | undefined,
  businessType: undefined as string | undefined,
  riskLevel: undefined as string | undefined
})

const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailProject = ref<ProjectVO | null>(null)
const detailProjectNo = ref('')
const activeTab = ref('coordination')

const pcConfirmDialogVisible = ref(false)
const pcConfirmSubmitting = ref(false)
const pcConfirmFormRef = ref<FormInstance>()
const pcConfirmForm = reactive<ProjectPcConfirmReqVO>({
  projectId: 0,
  remark: '',
  currentStageCode: ''
})
const pcConfirmRules = reactive<FormRules>({
  currentStageCode: [{ required: true, message: '当前阶段不能为空', trigger: 'blur' }],
  remark: [{ required: true, message: '确认备注不能为空', trigger: 'blur' }]
})

const mcConfirmDialogVisible = ref(false)
const mcConfirmSubmitting = ref(false)
const mcConfirmFormRef = ref<FormInstance>()
const mcConfirmForm = reactive<ProjectMcConfirmReqVO>({
  projectId: 0,
  remark: ''
})
const mcConfirmRules = reactive<FormRules>({
  remark: [{ required: true, message: '确认备注不能为空', trigger: 'blur' }]
})

const currentUserId = computed(() => userStore.getUser.id)
const isProjectInitiationPage = computed(() => route.path.replace(/\/+$/g, '') === '/sales/project-initiation')
const pageDocTitle = computed(() => (isProjectInitiationPage.value ? '【销售】销售项目立项' : '项目中心'))
const createButtonText = computed(() => (isProjectInitiationPage.value ? '新建立项' : '新建项目'))
const detailTodoTasks = computed(() => detailProject.value?.todoTasks || [])
const detailDrawerTitle = computed(() => {
  return `项目详情 - ${detailProject.value?.no || detailProjectNo.value || ''}`
})
const canPcConfirm = computed(() => {
  if (!detailProject.value) {
    return false
  }
  return (
    detailProject.value.pcStatus !== 'DONE' &&
    detailProject.value.planCoordinatorId === currentUserId.value
  )
})
const canMcConfirm = computed(() => {
  if (!detailProject.value) {
    return false
  }
  return (
    detailProject.value.mcStatus !== 'DONE' &&
    detailProject.value.materialControllerId === currentUserId.value
  )
})
const pcConfirmDisabled = computed(() => pcConfirmSubmitting.value || !canPcConfirm.value)
const mcConfirmDisabled = computed(() => mcConfirmSubmitting.value || !canMcConfirm.value)

const getBusinessTypeLabel = (type?: string) => {
  switch (type) {
    case 'SELF_RESEARCH':
      return '自研产品'
    case 'CUSTOMER_SUPPLIED':
      return '客供料'
    case 'TOLL_MANUFACTURING':
      return '来料加工'
    case 'PROCESS_VALIDATION':
      return '工艺验证'
    default:
      return '-'
  }
}

const getBusinessTypeTagType = (type?: string) => {
  switch (type) {
    case 'SELF_RESEARCH':
      return 'primary'
    case 'CUSTOMER_SUPPLIED':
      return 'warning'
    case 'TOLL_MANUFACTURING':
      return 'success'
    case 'PROCESS_VALIDATION':
      return 'info'
    default:
      return 'info'
  }
}

const getRiskLabel = (level?: string) => {
  switch (level) {
    case 'HIGH':
      return '高'
    case 'MEDIUM':
      return '中'
    case 'LOW':
      return '低'
    default:
      return '-'
  }
}

const getRiskTagType = (level?: string) => {
  switch (level) {
    case 'HIGH':
      return 'danger'
    case 'MEDIUM':
      return 'warning'
    case 'LOW':
      return 'success'
    default:
      return 'info'
  }
}

const getRoleStatusLabel = (status?: string) => {
  switch (status) {
    case 'DONE':
      return '已确认'
    case 'PENDING':
      return '待确认'
    default:
      return status || '-'
  }
}

const getRoleStatusType = (status?: string) => {
  switch (status) {
    case 'DONE':
      return 'success'
    case 'PENDING':
      return 'warning'
    default:
      return 'info'
  }
}

const getTaskRoleLabel = (roleCode?: string) => {
  switch (roleCode) {
    case 'PC':
      return 'PC'
    case 'MC':
      return 'MC'
    default:
      return roleCode || '-'
  }
}

const getTaskStatusLabel = (status?: string) => {
  switch (status) {
    case 'DONE':
      return '已完成'
    case 'TODO':
      return '待处理'
    case 'PENDING':
      return '处理中'
    default:
      return status || '-'
  }
}

const getTaskStatusType = (status?: string) => {
  switch (status) {
    case 'DONE':
      return 'success'
    case 'TODO':
      return 'warning'
    case 'PENDING':
      return 'primary'
    default:
      return 'info'
  }
}

const formatDateText = (value?: string | Date, pattern = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), pattern)
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ProjectCenterApi.getProjectPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const loadProjectDetail = async (id: number) => {
  detailLoading.value = true
  try {
    detailProject.value = await ProjectCenterApi.getProject(id)
  } catch {
    detailDrawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const openDetailDrawer = async (row: ProjectVO) => {
  detailProjectNo.value = row.no || ''
  detailProject.value = null
  activeTab.value = 'coordination'
  detailDrawerVisible.value = true
  await loadProjectDetail(row.id)
}

const refreshDetail = async () => {
  if (!detailProject.value?.id) {
    return
  }
  await loadProjectDetail(detailProject.value.id)
}

const resetPcConfirmForm = () => {
  pcConfirmForm.projectId = detailProject.value?.id || 0
  pcConfirmForm.currentStageCode = detailProject.value?.currentStageCode || 'PLAN_CONFIRMED'
  pcConfirmForm.remark = ''
  pcConfirmFormRef.value?.clearValidate()
}

const resetMcConfirmForm = () => {
  mcConfirmForm.projectId = detailProject.value?.id || 0
  mcConfirmForm.remark = ''
  mcConfirmFormRef.value?.clearValidate()
}

const openPcConfirmDialog = () => {
  if (!detailProject.value) {
    return
  }
  resetPcConfirmForm()
  pcConfirmDialogVisible.value = true
}

const openMcConfirmDialog = () => {
  if (!detailProject.value) {
    return
  }
  resetMcConfirmForm()
  mcConfirmDialogVisible.value = true
}

const submitPcConfirm = async () => {
  const valid = await pcConfirmFormRef.value
    ?.validate()
    .then(() => true)
    .catch(() => false)
  if (!valid) {
    return
  }
  pcConfirmSubmitting.value = true
  try {
    await ProjectCenterApi.confirmPc({ ...pcConfirmForm })
    message.success('PC确认成功')
    pcConfirmDialogVisible.value = false
    await Promise.all([refreshDetail(), getList()])
  } finally {
    pcConfirmSubmitting.value = false
  }
}

const submitMcConfirm = async () => {
  const valid = await mcConfirmFormRef.value
    ?.validate()
    .then(() => true)
    .catch(() => false)
  if (!valid) {
    return
  }
  mcConfirmSubmitting.value = true
  try {
    await ProjectCenterApi.confirmMc({ ...mcConfirmForm })
    message.success('MC确认成功')
    mcConfirmDialogVisible.value = false
    await Promise.all([refreshDetail(), getList()])
  } finally {
    mcConfirmSubmitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ProjectApi.deleteProject([id])
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await ProjectApi.exportProject(queryParams)
    download.excel(data, '项目列表.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

watch(detailDrawerVisible, (visible) => {
  if (!visible) {
    detailLoading.value = false
    detailProject.value = null
    detailProjectNo.value = ''
    activeTab.value = 'coordination'
    pcConfirmDialogVisible.value = false
    mcConfirmDialogVisible.value = false
    resetPcConfirmForm()
    resetMcConfirmForm()
  }
})

watch(pcConfirmDialogVisible, (visible) => {
  if (!visible) {
    resetPcConfirmForm()
  }
})

watch(mcConfirmDialogVisible, (visible) => {
  if (!visible) {
    resetMcConfirmForm()
  }
})

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.search-wrap {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.weitai-project-table {
  :deep(.el-table__row) {
    cursor: pointer;
  }
}

.project-detail-drawer {
  min-height: 320px;
}

.overview-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #f4f7fb 100%);
}

.hero-side {
  text-align: right;
}

.role-card {
  min-height: 220px;
}

.role-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 14px;
}

.role-line-top {
  align-items: flex-start;
}

.role-line .label {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.role-line .value {
  text-align: right;
  color: var(--el-text-color-primary);
}

.role-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 20px;
}

.role-actions .hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
