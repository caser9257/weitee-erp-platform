<template>
  <div class="iqc-page">
    <ContentWrap>
      <div class="iqc-page__header">
        <div class="iqc-page__header-main">
          <div class="iqc-page__title-row">
            <div class="iqc-page__title">{{ sceneConfig.title }}</div>
            <el-tag size="small" effect="plain" type="info">
              {{ scene === 'qms' ? text.dataScopeQms : text.dataScopeErp }}
            </el-tag>
          </div>
          <div class="iqc-page__desc">{{ sceneConfig.description }}</div>
        </div>
        <div class="iqc-page__header-side">
          <div class="iqc-page__meta-card">
            <div class="iqc-page__meta-label">{{ text.projectScopeLabel }}</div>
            <div class="iqc-page__meta-value">
              {{
                hasProjectScope
                  ? currentProject.name || currentProject.no || currentProject.id
                  : text.projectMissing
              }}
            </div>
          </div>
        </div>
      </div>
      <div class="iqc-page__notice-bar" v-if="showProjectBanner">
        <el-alert
          :title="hasProjectScope ? text.projectDetectedTip : text.projectMissingTip"
          :type="hasProjectScope ? 'success' : 'warning'"
          :closable="false"
          show-icon
        />
      </div>
      <div class="iqc-page__summary">
        <div
          v-for="card in summaryCards"
          :key="card.key"
          class="iqc-summary-card"
          :class="`iqc-summary-card--${card.tone}`"
        >
          <div class="iqc-summary-card__label">{{ card.label }}</div>
          <div class="iqc-summary-card__value">
            <el-skeleton v-if="pageLoading" animated>
              <template #template>
                <el-skeleton-item variant="h3" style="width: 72px" />
              </template>
            </el-skeleton>
            <template v-else>{{ card.value }}</template>
          </div>
          <div class="iqc-summary-card__desc">{{ card.description }}</div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="88px"
        class="iqc-query-form"
      >
        <div class="iqc-query-form__header">
          <div class="iqc-query-form__title">{{ text.filterTitle }}</div>
          <div class="iqc-query-form__subtitle">{{ text.filterSubtitle }}</div>
        </div>
        <div class="iqc-query-form__grid">
          <el-form-item :label="text.noLabel" prop="no" class="iqc-query-form__item">
            <el-input
              v-model="queryParams.no"
              :placeholder="text.noPlaceholder"
              clearable
              :disabled="pageLoading"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item :label="text.purchaseInNoLabel" prop="purchaseInNo" class="iqc-query-form__item">
            <el-input
              v-model="queryParams.purchaseInNo"
              :placeholder="text.purchaseInNoPlaceholder"
              clearable
              :disabled="pageLoading"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item :label="text.statusLabel" prop="status" class="iqc-query-form__item">
            <el-select
              v-model="queryParams.status"
              clearable
              :placeholder="text.statusPlaceholder"
              :disabled="pageLoading"
            >
              <el-option :label="text.statusFirstChecking" :value="PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING" />
              <el-option :label="text.statusWaitRecheck" :value="PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK" />
              <el-option :label="text.statusRechecking" :value="PURCHASE_IN_QUALITY_STATUS.RECHECKING" />
              <el-option :label="text.statusDone" :value="PURCHASE_IN_QUALITY_STATUS.DONE" />
              <el-option :label="text.statusVoid" :value="PURCHASE_IN_QUALITY_STATUS.VOID" />
            </el-select>
          </el-form-item>
          <el-form-item :label="text.resultLabel" prop="result" class="iqc-query-form__item">
            <el-select
              v-model="queryParams.result"
              clearable
              :placeholder="text.resultPlaceholder"
              :disabled="pageLoading"
            >
              <el-option :label="text.resultPartial" :value="PURCHASE_IN_QUALITY_RESULT.PARTIAL" />
              <el-option :label="text.resultPassed" :value="PURCHASE_IN_QUALITY_RESULT.PASSED" />
              <el-option :label="text.resultRejected" :value="PURCHASE_IN_QUALITY_RESULT.REJECTED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="iqc-query-form__actions">
          <div class="iqc-query-form__tips">
            {{ hasActiveFilters ? text.filterActive : text.filterDefault }}
          </div>
          <div class="iqc-query-form__buttons">
            <el-button :disabled="pageLoading" @click="handleQuery">
              <Icon icon="ep:search" class="mr-5px" />
              {{ text.search }}
            </el-button>
            <el-button :disabled="pageLoading || !hasActiveFilters" @click="resetQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              {{ text.reset }}
            </el-button>
          </div>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-result v-if="listErrorMessage" icon="error" :title="text.listLoadFailed" :sub-title="listErrorMessage">
        <template #extra>
          <el-button type="primary" :loading="pageLoading" @click="getList">{{ text.retry }}</el-button>
        </template>
      </el-result>

      <template v-else>
        <div class="iqc-table-toolbar">
          <div class="iqc-table-toolbar__title">{{ text.tableTitle }}</div>
          <div class="iqc-table-toolbar__meta">
            <el-tag v-if="urgentCount > 0" type="danger" effect="light">
              {{ `${text.urgentPrefix}${urgentCount}${text.urgentSuffix}` }}
            </el-tag>
            <span class="iqc-table-toolbar__count">{{ `${text.totalPrefix}${total}${text.totalSuffix}` }}</span>
          </div>
        </div>
        <div class="iqc-table-wrapper">
          <el-table
            v-loading="pageLoading"
            :data="list"
            :stripe="true"
            :show-overflow-tooltip="true"
          >
            <el-table-column :label="text.noLabel" prop="no" min-width="180" />
            <el-table-column :label="text.supplierLabel" prop="supplierName" min-width="160" />
            <el-table-column :label="text.purchaseInNoLabel" prop="purchaseInNo" min-width="180" />
            <el-table-column :label="text.orderNoLabel" prop="orderNo" min-width="180" />
            <el-table-column :label="text.statusLabel" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getQualityStatusTagType(row.status)">
                  {{ getQualityStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="text.resultLabel" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getQualityResultTagType(row.result)">
                  {{ getQualityResultLabel(row.result) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="text.roundLabel" width="100" align="center">
              <template #default="{ row }">
                {{ row.currentRoundNo || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="text.todoLabel" min-width="160">
              <template #default="{ row }">
                <div class="iqc-todo-cell">
                  <el-tag
                    v-if="!row.assignedCheckerUserId && canAssignChecker(row)"
                    size="small"
                    type="warning"
                    effect="light"
                  >
                    {{ text.todoAssign }}
                  </el-tag>
                  <el-tag
                    v-else-if="canHandleQuality(row)"
                    size="small"
                    type="danger"
                    effect="light"
                  >
                    {{ text.todoHandle }}
                  </el-tag>
                  <span v-else>{{ text.todoTracked }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="text.recheckReasonLabel" min-width="180">
              <template #default="{ row }">
                <el-tooltip v-if="row.recheckReason" :content="row.recheckReason" placement="top">
                  <span class="inline-block max-w-160px truncate">{{ row.recheckReason }}</span>
                </el-tooltip>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column :label="text.assignedCheckerLabel" min-width="150">
              <template #default="{ row }">
                <span>{{ row.assignedCheckerUserNickname || '-' }}</span>
                <el-tag
                  v-if="Number(row.assignedCheckerUserId || 0) === currentUserId"
                  size="small"
                  type="success"
                  class="ml-8px"
                >
                  {{ text.me }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="text.finalCheckerLabel" prop="checkerUserNickname" min-width="120" />
            <el-table-column :label="text.checkTimeLabel" min-width="180">
              <template #default="{ row }">
                {{ row.checkTime ? formatDate(row.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="text.operation" fixed="right" width="240" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="canAssignChecker(row)"
                  link
                  type="warning"
                  :disabled="assignSubmitting"
                  @click="openAssignDialog(row)"
                >
                  {{ row.assignedCheckerUserId ? text.reassign : text.assign }}
                </el-button>
                <el-button v-if="canHandleQuality(row)" link type="success" @click="openDetail(row, 'submit')">
                  {{ text.handleQuality }}
                </el-button>
                <el-button
                  v-if="hasQualityQueryPermission"
                  link
                  type="primary"
                  @click="openDetail(row, 'view')"
                >
                  {{ text.viewDetail }}
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty :description="pageLoading ? text.loadingList : sceneConfig.emptyText" />
            </template>
          </el-table>
        </div>

        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <Dialog v-model="assignDialogVisible" :title="text.assignDialogTitle" :width="dialogWidth.assign">
      <el-form label-width="100px">
        <el-form-item :label="text.noLabel">
          <span>{{ assignTargetNo || '-' }}</span>
        </el-form-item>
        <el-form-item :label="text.checkerLabel" required>
          <el-select
            v-model="assignForm.assignedCheckerUserId"
            filterable
            clearable
            :placeholder="text.checkerPlaceholder"
            class="!w-100%"
            :loading="checkerOptionsLoading"
            :disabled="assignSubmitting"
          >
            <el-option
              v-for="user in checkerUserOptions"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="assignSubmitting" @click="closeAssignDialog">{{ text.cancel }}</el-button>
        <el-button
          type="primary"
          :disabled="!canConfirmAssign"
          :loading="assignSubmitting"
          @click="handleAssignChecker"
        >
          {{ text.save }}
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi, checkRole } from '@/utils/permission'
import { getSimpleUserList, type UserVO } from '@/api/system/user'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useProjectScope } from '@/hooks/web/useProjectScope'
import {
  PurchaseInQualityApi,
  PURCHASE_IN_QUALITY_RESULT,
  PURCHASE_IN_QUALITY_STATUS,
  type PurchaseInQualityAssignCheckerReqVO,
  type PurchaseInQualityVO
} from '@/api/erp/purchase/in-quality'

type IqcScene = 'erp' | 'qms'

interface Props {
  scene: IqcScene
}

const props = defineProps<Props>()

const text = {
  dataScopeQms: '\u5f53\u524d\u6309\u6765\u6599\u68c0\u9a8c\u53e3\u5f84\u5c55\u793a',
  dataScopeErp: '\u5f53\u524d\u6309\u91c7\u8d2d\u8d28\u68c0\u53e3\u5f84\u5c55\u793a',
  projectScopeLabel: '\u9879\u76ee\u4e0a\u4e0b\u6587',
  projectMissing: '\u672a\u8bc6\u522b\u9879\u76ee\u4e0a\u4e0b\u6587',
  projectDetectedTip: '\u5f53\u524d\u5df2\u8bc6\u522b\u9879\u76ee\u4e0a\u4e0b\u6587\uff0c\u540e\u7eed\u53ef\u6309\u9879\u76ee\u8fdb\u4e00\u6b65\u6536\u655b\u6765\u6599\u68c0\u9a8c\u8303\u56f4\u3002',
  projectMissingTip: '\u6682\u672a\u5173\u8054\u5230\u9879\u76ee\u4e0a\u4e0b\u6587\uff0c\u5f53\u524d\u5148\u6309\u91c7\u8d2d IQC \u8bb0\u5f55\u5904\u7406\u6765\u6599\u68c0\u9a8c\u4efb\u52a1\u3002',
  noLabel: '\u8d28\u68c0\u5355\u53f7',
  noPlaceholder: '\u8bf7\u8f93\u5165\u8d28\u68c0\u5355\u53f7',
  purchaseInNoLabel: '\u91c7\u8d2d\u5165\u5e93\u5355',
  purchaseInNoPlaceholder: '\u8bf7\u8f93\u5165\u91c7\u8d2d\u5165\u5e93\u5355\u53f7',
  statusLabel: '\u8d28\u68c0\u72b6\u6001',
  statusPlaceholder: '\u8bf7\u9009\u62e9\u8d28\u68c0\u72b6\u6001',
  resultLabel: '\u8d28\u68c0\u7ed3\u679c',
  resultPlaceholder: '\u8bf7\u9009\u62e9\u8d28\u68c0\u7ed3\u679c',
  statusFirstChecking: '\u9996\u68c0\u4e2d',
  statusWaitRecheck: '\u5f85\u590d\u68c0',
  statusRechecking: '\u590d\u68c0\u4e2d',
  statusDone: '\u5df2\u5b8c\u6210',
  statusVoid: '\u5df2\u4f5c\u5e9f',
  resultPartial: '\u90e8\u5206\u5408\u683c',
  resultPassed: '\u5168\u90e8\u5408\u683c',
  resultRejected: '\u5168\u90e8\u4e0d\u5408\u683c',
  filterTitle: '\u7b5b\u9009\u6761\u4ef6',
  filterSubtitle: '\u6309\u5355\u53f7\u3001\u8d28\u68c0\u72b6\u6001\u548c\u7ed3\u679c\u5feb\u901f\u5b9a\u4f4d\u4efb\u52a1',
  filterActive: '\u5df2\u542f\u7528\u7b5b\u9009\u6761\u4ef6',
  filterDefault: '\u9ed8\u8ba4\u5c55\u793a\u6700\u65b0\u6765\u6599\u68c0\u9a8c\u4efb\u52a1',
  search: '\u641c\u7d22',
  reset: '\u91cd\u7f6e',
  listLoadFailed: '\u6765\u6599\u68c0\u9a8c\u5217\u8868\u52a0\u8f7d\u5931\u8d25',
  retry: '\u91cd\u65b0\u52a0\u8f7d',
  tableTitle: '\u6765\u6599\u68c0\u9a8c\u4efb\u52a1\u5217\u8868',
  urgentPrefix: '\u5f53\u524d\u9700\u4f18\u5148\u5904\u7406 ',
  urgentSuffix: ' \u6761',
  totalPrefix: '\u5171 ',
  totalSuffix: ' \u6761',
  orderNoLabel: '\u91c7\u8d2d\u8ba2\u5355',
  supplierLabel: '\u4f9b\u5e94\u5546',
  roundLabel: '\u5f53\u524d\u8f6e\u6b21',
  todoLabel: '\u5f85\u529e',
  todoAssign: '\u5f85\u6307\u6d3e',
  todoHandle: '\u5f85\u5904\u7406',
  todoTracked: '\u5df2\u8fdb\u5165\u8ddf\u8e2a',
  recheckReasonLabel: '\u590d\u68c0\u539f\u56e0',
  assignedCheckerLabel: '\u6307\u6d3e\u8d28\u68c0\u4eba',
  finalCheckerLabel: '\u6700\u7ec8\u8d28\u68c0\u4eba',
  checkTimeLabel: '\u8d28\u68c0\u65f6\u95f4',
  operation: '\u64cd\u4f5c',
  me: '\u6211',
  reassign: '\u6539\u6d3e',
  assign: '\u6307\u6d3e',
  handleQuality: '\u5904\u7406\u8d28\u68c0',
  viewDetail: '\u67e5\u770b\u8be6\u60c5',
  loadingList: '\u6b63\u5728\u52a0\u8f7d\u6765\u6599\u68c0\u9a8c\u6570\u636e',
  assignDialogTitle: '\u6307\u6d3e\u8d28\u68c0\u4eba',
  checkerLabel: '\u8d28\u68c0\u4eba',
  checkerPlaceholder: '\u8bf7\u9009\u62e9\u8d28\u68c0\u4eba',
  cancel: '\u53d6\u6d88',
  save: '\u4fdd\u5b58',
  listLoadFallback: '\u8bf7\u68c0\u67e5\u7f51\u7edc\u6216\u7a0d\u540e\u91cd\u8bd5\u3002',
  assignSuccess: '\u6307\u6d3e\u8d28\u68c0\u4eba\u6210\u529f',
  summaryPendingAssign: '\u5f85\u6307\u6d3e',
  summaryFirstCheck: '\u5f85\u9996\u68c0',
  summaryRecheck: '\u5f85\u590d\u68c0',
  summaryDone: '\u5df2\u5b8c\u6210',
  summaryPendingAssignDesc: '\u5c1a\u672a\u6307\u5b9a\u8d28\u68c0\u4eba\u7684\u4efb\u52a1',
  summaryFirstCheckDesc: '\u5df2\u6307\u6d3e\u4f46\u4ecd\u5904\u4e8e\u9996\u68c0\u4e2d\u7684\u4efb\u52a1',
  summaryRecheckDesc: '\u5305\u542b\u5f85\u53d1\u8d77\u590d\u68c0\u4e0e\u590d\u68c0\u4e2d\u4efb\u52a1',
  summaryDoneDesc: '\u5f53\u524d\u5217\u8868\u4e2d\u5df2\u5b8c\u6210\u95ed\u73af\u7684\u4efb\u52a1'
} as const

const SCENE_CONFIG = {
  erp: {
    title: '\u91c7\u8d2d\u5165\u5e93\u8d28\u68c0',
    description: '\u56f4\u7ed5\u91c7\u8d2d\u5165\u5e93\u5355\u8fdb\u884c\u6307\u6d3e\u3001\u9996\u68c0\u3001\u590d\u68c0\u548c\u7ed3\u679c\u8ffd\u8e2a\u3002',
    detailPath: '/erp/purchase/in-quality/detail',
    emptyText: '\u6682\u65e0\u91c7\u8d2d\u5165\u5e93\u8d28\u68c0\u6570\u636e'
  },
  qms: {
    title: '\u8d28\u91cf\u7ba1\u7406 / \u6765\u6599\u68c0\u9a8c',
    description: '\u805a\u5408\u6765\u6599\u8d28\u68c0\u4efb\u52a1\uff0c\u4f18\u5148\u7a81\u51fa\u6307\u6d3e\u3001\u9996\u68c0\u3001\u590d\u68c0\u548c\u5f02\u5e38\u8ddf\u8fdb\u4efb\u52a1\u3002',
    detailPath: '/qms/iqc/detail',
    emptyText: '\u6682\u65e0\u6765\u6599\u68c0\u9a8c\u6570\u636e'
  }
} as const

defineOptions({ name: 'IqcListPage' })

const router = useRouter()
const message = useMessage()
const userStore = useUserStoreWithOut()
const { currentProject, hasProjectScope } = useProjectScope()

const scene = computed(() => props.scene)
const sceneConfig = computed(() => SCENE_CONFIG[props.scene])
const hasQualityQueryPermission = checkPermi(['erp:purchase-in-quality:query'])
const hasFirstCheckPermission = checkPermi(['erp:purchase-in-quality:first-check'])
const hasStartRecheckPermission = checkPermi(['erp:purchase-in-quality:start-recheck'])
const hasRecheckPermission = checkPermi(['erp:purchase-in-quality:recheck'])
const hasAssignCheckerPermission = checkPermi(['erp:purchase-in-quality:assign-checker'])
const isSuperAdmin = checkRole(['super_admin'])

const pageLoading = ref(false)
const checkerOptionsLoading = ref(false)
const assignSubmitting = ref(false)
const total = ref(0)
const list = ref<PurchaseInQualityVO[]>([])
const listErrorMessage = ref('')
const checkerUserOptions = ref<UserVO[]>([])
const queryFormRef = ref()
const currentUserId = computed(() => Number(userStore.getUser.id || 0))
const assignDialogVisible = ref(false)
const assignTargetNo = ref('')
const assignForm = reactive<PurchaseInQualityAssignCheckerReqVO>({
  id: 0,
  assignedCheckerUserId: 0
})
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  purchaseInNo: undefined,
  status: undefined,
  result: undefined
})

const dialogWidth = {
  assign: 'min(520px, 92vw)'
}

const showProjectBanner = computed(() => scene.value === 'qms')

const hasActiveFilters = computed(() => {
  return !!(queryParams.no || queryParams.purchaseInNo || queryParams.status || queryParams.result)
})

const getQualityStatusLabel = (status?: number) => {
  if (status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) return text.statusFirstChecking
  if (status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return text.statusWaitRecheck
  if (status === PURCHASE_IN_QUALITY_STATUS.RECHECKING) return text.statusRechecking
  if (status === PURCHASE_IN_QUALITY_STATUS.DONE) return text.statusDone
  if (status === PURCHASE_IN_QUALITY_STATUS.VOID) return text.statusVoid
  return '-'
}

const getQualityStatusTagType = (status?: number) => {
  if (status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) return 'warning'
  if (status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return 'danger'
  if (status === PURCHASE_IN_QUALITY_STATUS.RECHECKING) return 'primary'
  if (status === PURCHASE_IN_QUALITY_STATUS.DONE) return 'success'
  if (status === PURCHASE_IN_QUALITY_STATUS.VOID) return 'info'
  return 'info'
}

const getQualityResultLabel = (result?: number) => {
  if (result === PURCHASE_IN_QUALITY_RESULT.PARTIAL) return text.resultPartial
  if (result === PURCHASE_IN_QUALITY_RESULT.PASSED) return text.resultPassed
  if (result === PURCHASE_IN_QUALITY_RESULT.REJECTED) return text.resultRejected
  return '\u5f85\u5224\u5b9a'
}

const getQualityResultTagType = (result?: number) => {
  if (result === PURCHASE_IN_QUALITY_RESULT.PASSED) return 'success'
  if (result === PURCHASE_IN_QUALITY_RESULT.PARTIAL) return 'warning'
  if (result === PURCHASE_IN_QUALITY_RESULT.REJECTED) return 'danger'
  return 'info'
}

const canAssignChecker = (row: PurchaseInQualityVO) => {
  return (
    hasAssignCheckerPermission &&
    [
      PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING,
      PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK,
      PURCHASE_IN_QUALITY_STATUS.RECHECKING
    ].includes(Number(row.status))
  )
}

const isAssignedToCurrentUser = (row: PurchaseInQualityVO) => {
  return !!row.assignedCheckerUserId && Number(row.assignedCheckerUserId) === currentUserId.value
}

const canHandleQuality = (row: PurchaseInQualityVO) => {
  if (Number(row.status) === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) {
    return (hasFirstCheckPermission || isSuperAdmin) &&
      (isAssignedToCurrentUser(row) || isSuperAdmin)
  }
  if (Number(row.status) === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) {
    return hasStartRecheckPermission || isSuperAdmin
  }
  if (Number(row.status) === PURCHASE_IN_QUALITY_STATUS.RECHECKING) {
    return (hasRecheckPermission || isSuperAdmin) && (isAssignedToCurrentUser(row) || isSuperAdmin)
  }
  return false
}

const urgentCount = computed(() => {
  return list.value.filter((row) => {
    return (!row.assignedCheckerUserId && canAssignChecker(row)) || canHandleQuality(row)
  }).length
})

const summaryCards = computed(() => {
  const pendingAssign = list.value.filter((row) => !row.assignedCheckerUserId && canAssignChecker(row)).length
  const firstChecking = list.value.filter((row) => {
    return Number(row.status) === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING && !!row.assignedCheckerUserId
  }).length
  const rechecking = list.value.filter((row) => {
    return [PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK, PURCHASE_IN_QUALITY_STATUS.RECHECKING].includes(
      Number(row.status)
    )
  }).length
  const doneCount = list.value.filter((row) => Number(row.status) === PURCHASE_IN_QUALITY_STATUS.DONE).length
  return [
    {
      key: 'pendingAssign',
      label: text.summaryPendingAssign,
      value: pendingAssign,
      description: text.summaryPendingAssignDesc,
      tone: 'warning'
    },
    {
      key: 'firstChecking',
      label: text.summaryFirstCheck,
      value: firstChecking,
      description: text.summaryFirstCheckDesc,
      tone: 'primary'
    },
    {
      key: 'rechecking',
      label: text.summaryRecheck,
      value: rechecking,
      description: text.summaryRecheckDesc,
      tone: 'danger'
    },
    {
      key: 'done',
      label: text.summaryDone,
      value: doneCount,
      description: text.summaryDoneDesc,
      tone: 'success'
    }
  ]
})

const canConfirmAssign = computed(() => {
  return !!assignForm.id && !!assignForm.assignedCheckerUserId && !assignSubmitting.value
})

const getList = async () => {
  pageLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await PurchaseInQualityApi.getPurchaseInQualityPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    listErrorMessage.value = error instanceof Error ? error.message : text.listLoadFallback
    list.value = []
    total.value = 0
  } finally {
    pageLoading.value = false
  }
}

const handleQuery = () => {
  if (pageLoading.value) return
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  if (pageLoading.value || !hasActiveFilters.value) return
  queryFormRef.value?.resetFields()
  handleQuery()
}

const loadCheckerOptions = async () => {
  if (checkerUserOptions.value.length) return
  checkerOptionsLoading.value = true
  try {
    checkerUserOptions.value = await getSimpleUserList()
  } finally {
    checkerOptionsLoading.value = false
  }
}

const openAssignDialog = async (row: PurchaseInQualityVO) => {
  assignForm.id = Number(row.id || 0)
  assignForm.assignedCheckerUserId = Number(row.assignedCheckerUserId || 0)
  assignTargetNo.value = row.no || ''
  assignDialogVisible.value = true
  await loadCheckerOptions()
}

const closeAssignDialog = () => {
  if (assignSubmitting.value) return
  assignDialogVisible.value = false
  assignForm.id = 0
  assignForm.assignedCheckerUserId = 0
  assignTargetNo.value = ''
}

const handleAssignChecker = async () => {
  if (!canConfirmAssign.value) return
  assignSubmitting.value = true
  try {
    await PurchaseInQualityApi.assignChecker({
      id: assignForm.id,
      assignedCheckerUserId: assignForm.assignedCheckerUserId
    })
    message.success(text.assignSuccess)
    closeAssignDialog()
    await getList()
  } finally {
    assignSubmitting.value = false
  }
}

const openDetail = (row: PurchaseInQualityVO, mode: 'submit' | 'view') => {
  router.push({
    path: sceneConfig.value.detailPath,
    query: {
      id: row.id,
      mode,
      from: 'quality-list',
      scene: props.scene
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.iqc-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.iqc-page__header {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 16px;
}

.iqc-page__header-main {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}

.iqc-page__header-side {
  width: min(280px, 100%);
}

.iqc-page__title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
}

.iqc-page__title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.iqc-page__desc {
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

.iqc-page__meta-card {
  display: flex;
  height: 100%;
  min-height: 88px;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 16px 18px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef6ff 100%);
}

.iqc-page__meta-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.iqc-page__meta-value {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--el-text-color-primary);
}

.iqc-page__notice-bar {
  margin-top: 16px;
}

.iqc-page__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.iqc-summary-card {
  position: relative;
  display: flex;
  min-height: 116px;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
  padding: 16px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #fff;
}

.iqc-summary-card::after {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  content: '';
  background: #d1d5db;
}

.iqc-summary-card--warning::after {
  background: #f59e0b;
}

.iqc-summary-card--primary::after {
  background: #409eff;
}

.iqc-summary-card--danger::after {
  background: #ef4444;
}

.iqc-summary-card--success::after {
  background: #67c23a;
}

.iqc-summary-card__label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.iqc-summary-card__value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-text-color-primary);
}

.iqc-summary-card__desc {
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.iqc-query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.iqc-query-form__header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.iqc-query-form__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.iqc-query-form__subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.iqc-query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 12px 16px;
}

.iqc-query-form__item {
  margin-bottom: 0;
}

:deep(.iqc-query-form__item .el-form-item__content) {
  width: 100%;
}

:deep(.iqc-query-form__item .el-input),
:deep(.iqc-query-form__item .el-select) {
  width: 100%;
}

.iqc-query-form__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px 16px;
}

.iqc-query-form__tips {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.iqc-query-form__buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.iqc-table-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px 16px;
  margin-bottom: 12px;
}

.iqc-table-toolbar__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.iqc-table-toolbar__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.iqc-table-toolbar__count {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.iqc-table-wrapper {
  overflow-x: auto;
}

.iqc-todo-cell {
  display: flex;
  align-items: center;
  min-height: 24px;
}

@media (max-width: 1439px) {
  .iqc-page__summary {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }

  .iqc-query-form__grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 1023px) {
  .iqc-page__header {
    flex-direction: column;
  }

  .iqc-page__header-side {
    width: 100%;
  }

  .iqc-page__summary {
    grid-template-columns: 1fr;
  }

  .iqc-query-form__grid {
    grid-template-columns: 1fr;
  }

  .iqc-query-form__actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .iqc-query-form__buttons {
    width: 100%;
  }

  .iqc-query-form__buttons :deep(.el-button) {
    flex: 1 1 calc(50% - 4px);
    margin-left: 0;
  }
}
</style>
