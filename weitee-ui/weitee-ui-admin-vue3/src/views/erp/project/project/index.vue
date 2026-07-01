<template>
  <div class="project-page-shell">
    <doc-alert :title="pageDocTitle" url="https://doc.iocoder.cn/erp/project/" />

    <ContentWrap class="project-page-shell__hero">
      <div class="page-hero page-hero--compact">
        <div class="page-hero__header">
          <h1 class="page-hero__title">{{
            isProjectInitiationPage ? '销售项目立项' : '项目中心'
          }}</h1>
        </div>
        <div class="page-hero__stats page-hero__stats--row">
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--blue">
            <span class="hero-stat-card__label">当前结果</span>
            <strong class="hero-stat-card__value">{{ total }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--rose">
            <span class="hero-stat-card__label">高风险</span>
            <strong class="hero-stat-card__value">{{ projectStats.highRiskCount }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--amber">
            <span class="hero-stat-card__label">PC 待确认</span>
            <strong class="hero-stat-card__value">{{ projectStats.pcPendingCount }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--amber">
            <span class="hero-stat-card__label">MC 待确认</span>
            <strong class="hero-stat-card__value">{{ projectStats.mcPendingCount }}</strong>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="project-page-shell__search-card">
      <div class="search-card__header">
        <div>
          <div class="search-card__title">立项筛选</div>
        </div>
      </div>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
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
          <el-form-item label="风险等级" prop="riskLevel">
            <el-select v-model="queryParams.riskLevel" clearable placeholder="请选择风险等级">
              <el-option label="低" value="LOW" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="高" value="HIGH" />
            </el-select>
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="project-page-shell__table-card">
      <div class="table-toolbar">
        <div class="table-toolbar__main">
          <div class="table-toolbar__title">{{
            isProjectInitiationPage ? '立项项目列表' : '项目列表'
          }}</div>
          <div class="table-toolbar__meta">当前共 {{ total }} 条</div>
        </div>
        <div class="table-toolbar__actions">
          <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:project:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            {{ createButtonText }}
          </el-button>
          <el-button
            :loading="exportLoading"
            plain
            type="success"
            @click="handleExport"
            v-hasPermi="['erp:project:export']"
          >
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>

      <div class="table-card__body">
        <el-table
          v-loading="loading"
          :data="list"
          :show-overflow-tooltip="false"
          stripe
          class="project-ledger-table"
          @row-click="openDetailDrawer"
        >
          <el-table-column label="项目信息" min-width="260" fixed="left">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title-row">
                  <span class="cell-stack__mono">{{ row.no || '-' }}</span>
                  <span class="soft-pill" :class="riskPillClass(row.riskLevel)">
                    风险{{ getRiskLabel(row.riskLevel) }}
                  </span>
                </div>
                <div class="cell-stack__title">{{ row.name || '-' }}</div>
                <div class="cell-stack__meta">
                  <span>{{ getBusinessTypeLabel(row.businessType) }}</span>
                  <span>阶段：{{ row.currentStageCode || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="客户与交付" min-width="190">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title cell-stack__title--plain">{{
                  row.customerName || '-'
                }}</div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">交期</span>
                  <span>{{ formatDateText(row.deliveryDate, 'YYYY-MM-DD') }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">项目状态</span>
                  <span>{{ commonStatusLabel(row.status) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="PC 协同" min-width="180">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">负责人</span>
                  <span>{{ row.planCoordinatorName || '-' }}</span>
                </div>
                <span class="soft-pill" :class="rolePillClass(row.pcStatus)">
                  {{ getRoleStatusLabel(row.pcStatus) }}
                </span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="MC 协同" min-width="180">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">负责人</span>
                  <span>{{ row.materialControllerName || '-' }}</span>
                </div>
                <span class="soft-pill" :class="rolePillClass(row.mcStatus)">
                  {{ getRoleStatusLabel(row.mcStatus) }}
                </span>
              </div>
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
              <el-button
                link
                @click.stop="openDetailDrawer(row)"
                v-hasPermi="['erp:project:query']"
              >
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

          <template #empty>
            <el-empty description="暂无项目数据，试试调整筛选条件" />
          </template>
        </el-table>
      </div>

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
      size="980px"
      destroy-on-close
      :with-header="false"
      class="project-detail-drawer-shell"
    >
      <div v-loading="detailLoading" class="project-detail-drawer">
        <template v-if="detailProject">
          <div class="drawer-context">
            <div class="drawer-context__main">
              <div class="drawer-context__title">{{ detailProject.name }}</div>
              <div class="drawer-context__meta">
                <span>项目编号：{{ detailProject.no || '-' }}</span>
                <span>客户：{{ detailProject.customerName || '-' }}</span>
                <span>业务类型：{{ getBusinessTypeLabel(detailProject.businessType) }}</span>
                <span>交期：{{ formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
            <div class="drawer-context__side">
              <span class="soft-pill" :class="riskPillClass(detailProject.riskLevel)">
                风险{{ getRiskLabel(detailProject.riskLevel) }}
              </span>
              <span class="soft-pill soft-pill--blue"
                >阶段 {{ detailProject.currentStageCode || '-' }}</span
              >
            </div>
          </div>

          <div class="role-grid">
            <el-card shadow="never" class="role-card role-card-pc">
              <template #header>
                <div class="role-card-header">
                  <span>PC 协同</span>
                  <span class="soft-pill" :class="rolePillClass(detailProject.pcStatus)">
                    {{ getRoleStatusLabel(detailProject.pcStatus) }}
                  </span>
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
                  PC 确认
                </el-button>
              </div>
            </el-card>

            <el-card shadow="never" class="role-card role-card-mc">
              <template #header>
                <div class="role-card-header">
                  <span>MC 协同</span>
                  <span class="soft-pill" :class="rolePillClass(detailProject.mcStatus)">
                    {{ getRoleStatusLabel(detailProject.mcStatus) }}
                  </span>
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
                  MC 确认
                </el-button>
              </div>
            </el-card>
          </div>

          <el-tabs v-model="activeTab" class="drawer-tabs">
            <el-tab-pane label="协同任务" name="coordination">
              <el-card shadow="never" class="drawer-card">
                <template #header>
                  <div class="section-header">
                    <span>待办任务</span>
                    <span class="soft-pill soft-pill--slate">{{ detailTodoTasks.length }} 条</span>
                  </div>
                </template>

                <template v-if="detailTodoTasks.length">
                  <el-table :data="detailTodoTasks" size="small">
                    <el-table-column label="角色" width="90" align="center">
                      <template #default="{ row }">
                        {{ getTaskRoleLabel(row.roleCode) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="状态" width="100" align="center">
                      <template #default="{ row }">
                        <span class="soft-pill" :class="taskPillClass(row.taskStatus)">
                          {{ getTaskStatusLabel(row.taskStatus) }}
                        </span>
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
              <el-card shadow="never" class="drawer-card">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-item__label">项目编号</span>
                    <span class="info-item__value">{{ detailProject.no || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">项目名称</span>
                    <span class="info-item__value">{{ detailProject.name || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">项目类型</span>
                    <span class="info-item__value">{{ detailProject.projectType || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">业务类型</span>
                    <span class="info-item__value">{{
                      getBusinessTypeLabel(detailProject.businessType)
                    }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">来源项目</span>
                    <span class="info-item__value">{{ detailProject.sourceProjectId || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">销售订单</span>
                    <span class="info-item__value">{{ detailProject.saleOrderId || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">客户</span>
                    <span class="info-item__value">{{ detailProject.customerName || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">交期</span>
                    <span class="info-item__value">{{
                      formatDateText(detailProject.deliveryDate, 'YYYY-MM-DD')
                    }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">风险等级</span>
                    <span class="info-item__value">{{
                      getRiskLabel(detailProject.riskLevel)
                    }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-item__label">创建时间</span>
                    <span class="info-item__value">{{
                      formatDateText(detailProject.createTime)
                    }}</span>
                  </div>
                  <div class="info-item info-item--full">
                    <span class="info-item__label">项目备注</span>
                    <span class="info-item__value">{{ detailProject.remark || '暂无' }}</span>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </template>
        <el-empty v-else description="暂无项目详情" />
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
        </div>
      </template>
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
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { ProjectApi } from '@/api/erp/project'
import {
  ProjectCenterApi,
  type ProjectMcConfirmReqVO,
  type ProjectPcConfirmReqVO,
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
const isProjectInitiationPage = computed(
  () => route.path.replace(/\/+$/g, '') === '/sales/project-initiation'
)
const pageDocTitle = computed(() =>
  isProjectInitiationPage.value ? '【销售】销售项目立项' : '项目中心'
)
const createButtonText = computed(() => (isProjectInitiationPage.value ? '新建立项' : '新建项目'))
const detailTodoTasks = computed(() => detailProject.value?.todoTasks || [])
const detailDrawerTitle = computed(
  () => `项目详情 - ${detailProject.value?.no || detailProjectNo.value || ''}`
)
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

const projectStats = computed(() => {
  return list.value.reduce(
    (acc, item) => {
      if (item.riskLevel === 'HIGH') {
        acc.highRiskCount += 1
      }
      if (item.pcStatus === 'PENDING') {
        acc.pcPendingCount += 1
      }
      if (item.mcStatus === 'PENDING') {
        acc.mcPendingCount += 1
      }
      return acc
    },
    {
      highRiskCount: 0,
      pcPendingCount: 0,
      mcPendingCount: 0
    }
  )
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

const formatDateText = (value?: string | Date, pattern = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), pattern)
}

const commonStatusLabel = (status?: number) => {
  switch (status) {
    case 0:
      return '启用'
    case 1:
      return '停用'
    default:
      return '-'
  }
}

const riskPillClass = (level?: string) => {
  switch (level) {
    case 'HIGH':
      return 'soft-pill--rose'
    case 'MEDIUM':
      return 'soft-pill--amber'
    case 'LOW':
      return 'soft-pill--emerald'
    default:
      return 'soft-pill--slate'
  }
}

const rolePillClass = (status?: string) => {
  switch (status) {
    case 'DONE':
      return 'soft-pill--emerald'
    case 'PENDING':
      return 'soft-pill--amber'
    default:
      return 'soft-pill--slate'
  }
}

const taskPillClass = (status?: string) => {
  switch (status) {
    case 'DONE':
      return 'soft-pill--emerald'
    case 'TODO':
      return 'soft-pill--amber'
    case 'PENDING':
      return 'soft-pill--blue'
    default:
      return 'soft-pill--slate'
  }
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
.project-page-shell {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 4px 4px 20px;
  background: #f5f7fa;
}

.project-page-shell__hero,
.project-page-shell__search-card,
.project-page-shell__table-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 24px rgb(15 23 42 / 0.04);
}

.page-hero {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-hero__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-hero__title {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.3;
}

.page-hero__stats {
  display: grid;
  width: 100%;
  gap: 12px;
}

.page-hero__stats--row {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.hero-stat-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
  border-radius: 14px;
  background: var(--erp-stat-gradient-slate);
}

.hero-stat-card--compact {
  min-height: 80px;
}

.hero-stat-card__label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.hero-stat-card__value {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.1;
}

.hero-stat-card--blue {
  background: var(--erp-stat-gradient-blue);
  border-color: var(--erp-stat-border-blue);
}

.hero-stat-card--green {
  background: var(--erp-stat-gradient-green);
  border-color: var(--erp-stat-border-green);
}

.hero-stat-card--teal {
  background: var(--erp-stat-gradient-teal);
  border-color: var(--erp-stat-border-teal);
}

.hero-stat-card--slate {
  background: var(--erp-stat-gradient-slate);
  border-color: var(--erp-stat-border-slate);
}

.hero-stat-card--gold {
  background: var(--erp-stat-gradient-gold);
  border-color: var(--erp-stat-border-gold);
}

.hero-stat-card--amber {
  background: var(--erp-stat-gradient-amber);
  border-color: var(--erp-stat-border-amber);
}

.hero-stat-card--rose {
  background: var(--erp-stat-gradient-rose);
  border-color: var(--erp-stat-border-rose);
}

.search-card__header,
.table-toolbar {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.search-card__title,
.table-toolbar__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.table-toolbar__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.query-form {
  margin-top: 16px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 16px;
}

.query-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 6px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.table-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.table-card__body {
  overflow-x: auto;
}

.project-ledger-table {
  min-width: 980px;
}

.project-ledger-table :deep(.el-table__row) {
  cursor: pointer;
}

.cell-stack {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.cell-stack__title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.cell-stack__mono {
  color: #0f172a;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 700;
}

.cell-stack__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.cell-stack__title--plain {
  color: #0f172a;
}

.cell-stack__meta {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.cell-stack__label {
  color: #94a3b8;
}

.soft-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.soft-pill--blue {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
}

.soft-pill--amber {
  border-color: #fde68a;
  background: #fffbeb;
  color: #b45309;
}

.soft-pill--emerald {
  border-color: #bbf7d0;
  background: #ecfdf5;
  color: #047857;
}

.soft-pill--rose {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #be123c;
}

.soft-pill--slate {
  border-color: #cbd5e1;
  background: #f8fafc;
  color: #475569;
}

.project-detail-drawer {
  min-height: 320px;
}

.drawer-context {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}

.drawer-context__main {
  min-width: 0;
  flex: 1;
}

.drawer-context__title {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.3;
}

.drawer-context__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 12px;
  color: rgb(226 232 240 / 0.92);
  font-size: 13px;
}

.drawer-context__side {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.role-card,
.drawer-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.role-card-header,
.section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
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
  color: #64748b;
  white-space: nowrap;
}

.role-line .value {
  color: #0f172a;
  text-align: right;
}

.role-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 20px;
}

.drawer-tabs {
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.info-item {
  display: flex;
  min-height: 78px;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.info-item--full {
  grid-column: 1 / -1;
}

.info-item__label {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.info-item__value {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.6;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #475569;
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  min-height: 40px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #dbe3ef inset;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-border-color: #e2e8f0;
  --el-table-row-hover-bg-color: #f8fbff;
}

:deep(.el-table th.el-table__cell) {
  color: #64748b;
  font-weight: 700;
}

:deep(.el-table td.el-table__cell) {
  padding-top: 14px;
  padding-bottom: 14px;
  vertical-align: top;
}

@media (max-width: 1280px) {
  .page-hero__stats--row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .query-form__grid,
  .role-grid,
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .project-page-shell {
    padding: 0 0 18px;
  }

  .page-hero__stats--row {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .query-form__grid,
  .role-grid,
  .info-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .search-card__header,
  .table-toolbar,
  .table-toolbar__actions,
  .drawer-context {
    flex-direction: column;
    align-items: stretch;
  }

  .drawer-context__side {
    align-items: flex-start;
  }

  .query-form__actions {
    justify-content: stretch;
  }

  .query-form__actions :deep(.el-button),
  .table-toolbar__actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
}
</style>
