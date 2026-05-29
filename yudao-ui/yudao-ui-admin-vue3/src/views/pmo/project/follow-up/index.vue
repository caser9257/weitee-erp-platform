<template>
  <div class="pmo-project-follow-up-page">
    <ContentWrap>
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">项目跟进</div>
        </div>
        <div class="page-header__tags">
          <el-tag type="primary" effect="dark">综合计划管理</el-tag>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-tabs v-model="activeRoleCode" class="role-tabs" @tab-change="handleRoleChange">
        <el-tab-pane label="PC跟进" name="PC" />
        <el-tab-pane label="MC跟进" name="MC" />
      </el-tabs>

      <el-form
        ref="queryFormRef"
        :model="queryParams"
        :inline="true"
        class="query-form"
        label-width="84px"
      >
        <el-form-item label="项目编号" prop="no">
          <el-input
            v-model="queryParams.no"
            clearable
            placeholder="请输入项目编号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入项目名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="queryParams.businessType" clearable placeholder="请选择业务类型">
            <el-option label="自研产品" value="SELF_RESEARCH" />
            <el-option label="客供料" value="CUSTOMER_SUPPLIED" />
            <el-option label="来料加工" value="TOLL_MANUFACTURING" />
            <el-option label="工艺验证" value="PROCESS_VALIDATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前阶段" prop="currentStageCode">
          <el-input
            v-model="queryParams.currentStageCode"
            clearable
            placeholder="请输入阶段编码"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="queryParams.riskLevel" clearable placeholder="请选择风险等级">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <template v-if="listError">
        <el-result icon="warning-filled" title="项目列表加载失败">
          <template #extra>
            <el-button type="primary" @click="retryList">重新加载</el-button>
          </template>
        </el-result>
      </template>
      <template v-else-if="!loadingList && !list.length">
        <el-empty description="暂无项目跟进" />
      </template>
      <template v-else>
        <el-table
          v-loading="loadingList"
          :data="list"
          :stripe="true"
          :show-overflow-tooltip="true"
          class="pmo-project-table"
          @row-click="openDetailDrawer"
        >
          <el-table-column label="项目编号" prop="no" min-width="140" fixed="left" />
          <el-table-column label="项目名称 / 阶段" min-width="220" fixed="left">
            <template #default="{ row }">
              <div class="project-name">
                <span class="project-name__text">{{ row.name }}</span>
                <el-tag size="small" :type="getBusinessTypeTagType(row.businessType)">
                  {{ getBusinessTypeLabel(row.businessType) }}
                </el-tag>
              </div>
              <div class="project-stage">当前阶段：{{ row.currentStageCode || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="客户" prop="customerName" min-width="140" />
          <el-table-column :label="`${activeRoleShortLabel}负责人`" min-width="120">
            <template #default="{ row }">
              {{ getRoleOwnerName(row) }}
            </template>
          </el-table-column>
          <el-table-column :label="`${activeRoleShortLabel}状态`" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getRoleStatusType(getRoleStatus(row))">
                {{ getRoleStatusLabel(getRoleStatus(row)) }}
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
          >
            <template #default="{ row }">
              {{ formatDateText(row.deliveryDate, 'YYYY-MM-DD') }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" align="center" width="100">
            <template #default="{ row }">
              <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" v-hasPermi="['erp:project:query']" @click.stop="openDetailDrawer(row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          v-model:limit="queryParams.pageSize"
          v-model:page="queryParams.pageNo"
          :total="total"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerVisible"
      :title="detailDrawerTitle"
      size="980px"
      destroy-on-close
      class="pmo-project-drawer"
    >
      <div v-loading="detailLoading" class="detail-drawer">
        <template v-if="detailError">
          <el-result icon="warning-filled" title="项目详情加载失败">
            <template #extra>
              <el-button type="primary" @click="retryDetail">重新加载</el-button>
            </template>
          </el-result>
        </template>
        <template v-else-if="detailProject">
          <div class="overview-hero">
            <div>
              <div class="overview-hero__title">{{ detailProject.name }}</div>
              <div class="overview-hero__meta">
                <span>项目编号：{{ detailProject.no }}</span>
                <span>业务类型：{{ getBusinessTypeLabel(detailProject.businessType) }}</span>
                <span>客户：{{ detailProject.customerName || '-' }}</span>
                <span>交期：{{ formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
            <div class="overview-hero__side">
              <el-tag :type="getRiskTagType(detailProject.riskLevel)" size="large">
                风险 {{ getRiskLabel(detailProject.riskLevel) }}
              </el-tag>
              <div class="overview-hero__stage">
                当前阶段：{{ detailProject.currentStageCode || '-' }}
              </div>
            </div>
          </div>

          <el-row :gutter="16" class="mb-16px">
            <el-col :span="24" :md="12">
              <el-card shadow="never" class="role-card role-card-pc">
                <template #header>
                  <div class="role-card-header">
                    <span>PC协同</span>
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
                    v-hasPermi="['erp:project:pc-confirm']"
                    :disabled="pcConfirmDisabled"
                    :loading="pcConfirmSubmitting"
                    type="primary"
                    @click="openPcConfirmDialog"
                  >
                    PC确认
                  </el-button>
                  <span class="hint">仅当前 PC 负责人且状态未完成时可确认</span>
                </div>
              </el-card>
            </el-col>

            <el-col :span="24" :md="12">
              <el-card shadow="never" class="role-card role-card-mc">
                <template #header>
                  <div class="role-card-header">
                    <span>MC协同</span>
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
                    v-hasPermi="['erp:project:mc-confirm']"
                    :disabled="mcConfirmDisabled"
                    :loading="mcConfirmSubmitting"
                    type="success"
                    @click="openMcConfirmDialog"
                  >
                    MC确认
                  </el-button>
                  <span class="hint">仅当前 MC 负责人且状态未完成时可确认</span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-tabs v-model="detailTab">
            <el-tab-pane label="协同任务" name="coordination">
              <el-card shadow="never">
                <template #header>
                  <div class="section-header">
                    <span>待办任务</span>
                    <el-tag size="small" type="info">{{ detailTaskCount }} 条</el-tag>
                  </div>
                </template>

                <template v-if="detailTodoTasks.length">
                  <el-table :data="detailTodoTasks" border size="small">
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
                  <el-descriptions-item label="项目编号">
                    {{ detailProject.no || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="项目名称">
                    {{ detailProject.name || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="业务类型">
                    {{ getBusinessTypeLabel(detailProject.businessType) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="客户">
                    {{ detailProject.customerName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="PC负责人">
                    {{ detailProject.planCoordinatorName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="MC负责人">
                    {{ detailProject.materialControllerName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="当前阶段">
                    {{ detailProject.currentStageCode || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="风险等级">
                    {{ getRiskLabel(detailProject.riskLevel) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="交期">
                    {{ formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD') }}
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
        <template v-else>
          <el-empty description="暂无项目详情" />
        </template>
      </div>
    </el-drawer>

    <Dialog title="PC确认" v-model="pcConfirmDialogVisible" scroll maxHeight="70vh" width="min(520px, 92vw)">
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
            :rows="4"
            placeholder="请输入 PC 确认备注"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pcConfirmDialogVisible = false">取消</el-button>
        <el-button :loading="pcConfirmSubmitting" type="primary" @click="submitPcConfirm">
          确认提交
        </el-button>
      </template>
    </Dialog>

    <Dialog title="MC确认" v-model="mcConfirmDialogVisible" scroll maxHeight="70vh" width="min(520px, 92vw)">
      <el-form
        ref="mcConfirmFormRef"
        :model="mcConfirmForm"
        :rules="mcConfirmRules"
        label-width="100px"
      >
        <el-form-item label="确认备注" prop="remark">
          <el-input
            v-model="mcConfirmForm.remark"
            :rows="4"
            placeholder="请输入 MC 确认备注"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mcConfirmDialogVisible = false">取消</el-button>
        <el-button :loading="mcConfirmSubmitting" type="primary" @click="submitMcConfirm">
          确认提交
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { ProjectCenterApi, type ProjectCenterVO } from '@/api/pmo/project'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'PmoProjectFollowUp' })

const message = useMessage()
const userStore = useUserStore()

const queryFormRef = ref<FormInstance>()
const pcConfirmFormRef = ref<FormInstance>()
const mcConfirmFormRef = ref<FormInstance>()

const activeRoleCode = ref<'PC' | 'MC'>('PC')
const loadingList = ref(false)
const listError = ref('')
const list = ref<ProjectCenterVO[]>([])
const total = ref(0)
let listRequestSeq = 0

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined as string | undefined,
  name: undefined as string | undefined,
  businessType: undefined as string | undefined,
  currentStageCode: undefined as string | undefined,
  riskLevel: undefined as string | undefined,
  status: undefined as number | undefined
})

const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detailProject = ref<ProjectCenterVO | null>(null)
const detailProjectId = ref<number | null>(null)
const detailProjectNo = ref('')
const detailTab = ref<'coordination' | 'overview'>('coordination')
let detailRequestSeq = 0

const pcConfirmDialogVisible = ref(false)
const pcConfirmSubmitting = ref(false)
const pcConfirmForm = reactive({
  projectId: 0,
  currentStageCode: '',
  remark: ''
})
const pcConfirmRules = reactive<FormRules>({
  currentStageCode: [{ required: true, message: '当前阶段不能为空', trigger: 'blur' }],
  remark: [{ required: true, message: '确认备注不能为空', trigger: 'blur' }]
})

const mcConfirmDialogVisible = ref(false)
const mcConfirmSubmitting = ref(false)
const mcConfirmForm = reactive({
  projectId: 0,
  remark: ''
})
const mcConfirmRules = reactive<FormRules>({
  remark: [{ required: true, message: '确认备注不能为空', trigger: 'blur' }]
})

const currentUserId = computed(() => userStore.getUser.id)
const activeRoleShortLabel = computed(() => activeRoleCode.value)
const detailTodoTasks = computed(() => detailProject.value?.todoTasks || [])
const detailTaskCount = computed(() => detailTodoTasks.value.length)
const detailDrawerTitle = computed(() => {
  return `项目详情 - ${detailProject.value?.no || detailProjectNo.value || ''}`
})

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

const getRoleOwnerName = (row: ProjectCenterVO) => {
  return activeRoleCode.value === 'MC' ? row.materialControllerName || '-' : row.planCoordinatorName || '-'
}

const getRoleStatus = (row: ProjectCenterVO) => {
  return activeRoleCode.value === 'MC' ? row.mcStatus : row.pcStatus
}

const formatDateText = (value?: string | Date, pattern = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), pattern)
}

const getList = async () => {
  const requestSeq = ++listRequestSeq
  loadingList.value = true
  listError.value = ''
  try {
    const data = await ProjectCenterApi.getAssignedProjectPage(activeRoleCode.value, {
      ...queryParams
    })
    if (requestSeq !== listRequestSeq) {
      return
    }
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    if (requestSeq !== listRequestSeq) {
      return
    }
    list.value = []
    total.value = 0
    listError.value = error instanceof Error ? error.message : '项目列表加载失败'
  } finally {
    if (requestSeq === listRequestSeq) {
      loadingList.value = false
    }
  }
}

const handleRoleChange = () => {
  queryParams.pageNo = 1
}

const handleQuery = () => {
  queryParams.pageNo = 1
  void getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  void getList()
}

const retryList = () => {
  void getList()
}

const loadDetail = async (id: number) => {
  const requestSeq = ++detailRequestSeq
  detailLoading.value = true
  detailError.value = ''
  try {
    const data = await ProjectCenterApi.getProject(id)
    if (requestSeq !== detailRequestSeq) {
      return
    }
    detailProject.value = data
  } catch (error) {
    if (requestSeq !== detailRequestSeq) {
      return
    }
    detailProject.value = null
    detailError.value = error instanceof Error ? error.message : '项目详情加载失败'
  } finally {
    if (requestSeq === detailRequestSeq) {
      detailLoading.value = false
    }
  }
}

const openDetailDrawer = async (row: ProjectCenterVO) => {
  detailProjectId.value = row.id
  detailProjectNo.value = row.no || ''
  detailProject.value = null
  detailError.value = ''
  detailTab.value = 'coordination'
  detailDrawerVisible.value = true
  await loadDetail(row.id)
}

const retryDetail = async () => {
  if (!detailProjectId.value) {
    return
  }
  await loadDetail(detailProjectId.value)
}

const refreshDetail = async () => {
  if (!detailProjectId.value) {
    return
  }
  await loadDetail(detailProjectId.value)
}

const resetPcConfirmForm = () => {
  pcConfirmForm.projectId = detailProject.value?.id || detailProjectId.value || 0
  pcConfirmForm.currentStageCode = detailProject.value?.currentStageCode || ''
  pcConfirmForm.remark = ''
  pcConfirmFormRef.value?.clearValidate()
}

const resetMcConfirmForm = () => {
  mcConfirmForm.projectId = detailProject.value?.id || detailProjectId.value || 0
  mcConfirmForm.remark = ''
  mcConfirmFormRef.value?.clearValidate()
}

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

watch(activeRoleCode, () => {
  queryParams.pageNo = 1
  void getList()
})

watch(detailDrawerVisible, (visible) => {
  if (!visible) {
    detailRequestSeq += 1
    detailLoading.value = false
    detailError.value = ''
    detailProject.value = null
    detailProjectId.value = null
    detailProjectNo.value = ''
    detailTab.value = 'coordination'
    pcConfirmDialogVisible.value = false
    mcConfirmDialogVisible.value = false
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
  void getList()
})
</script>

<style scoped lang="scss">
.pmo-project-follow-up-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.page-header__main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-header__title {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.page-header__tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.role-tabs {
  margin-bottom: 12px;
}

.query-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    min-width: 220px;
  }
}

.query-actions {
  margin-left: auto;
}

.pmo-project-table {
  :deep(.el-table__row) {
    cursor: pointer;
  }
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.project-name__text {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.project-stage {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.detail-drawer {
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

.overview-hero__title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.overview-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.overview-hero__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.overview-hero__stage {
  font-size: 13px;
  color: var(--el-text-color-secondary);
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

.pmo-project-drawer {
  :deep(.el-drawer) {
    max-width: 100vw;
  }
}

@media (max-width: 768px) {
  .page-header,
  .overview-hero {
    flex-direction: column;
  }

  .overview-hero__side {
    align-items: flex-start;
  }

  .query-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0;
    }

    :deep(.el-input),
    :deep(.el-select) {
      width: 100%;
      min-width: 0;
    }
  }

  .query-actions {
    margin-left: 0;
  }
}
</style>
