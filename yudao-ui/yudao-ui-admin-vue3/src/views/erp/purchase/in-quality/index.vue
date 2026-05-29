<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="88px" class="-mb-15px">
      <el-form-item label="质检单号" prop="no">
        <el-input
          v-model="queryParams.no"
          placeholder="请输入质检单号"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="采购入库单" prop="purchaseInNo">
        <el-input
          v-model="queryParams.purchaseInNo"
          placeholder="请输入采购入库单号"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="质检状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择质检状态" class="!w-240px">
          <el-option label="首检中" :value="PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING" />
          <el-option label="待复检" :value="PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK" />
          <el-option label="复检中" :value="PURCHASE_IN_QUALITY_STATUS.RECHECKING" />
          <el-option label="已完成" :value="PURCHASE_IN_QUALITY_STATUS.DONE" />
          <el-option label="已作废" :value="PURCHASE_IN_QUALITY_STATUS.VOID" />
        </el-select>
      </el-form-item>
      <el-form-item label="质检结果" prop="result">
        <el-select v-model="queryParams.result" clearable placeholder="请选择质检结果" class="!w-240px">
          <el-option label="部分合格" :value="PURCHASE_IN_QUALITY_RESULT.PARTIAL" />
          <el-option label="全部合格" :value="PURCHASE_IN_QUALITY_RESULT.PASSED" />
          <el-option label="全部不合格" :value="PURCHASE_IN_QUALITY_RESULT.REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="pageLoading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="质检单号" prop="no" min-width="180" />
      <el-table-column label="采购入库单" prop="purchaseInNo" min-width="180" />
      <el-table-column label="采购订单" prop="orderNo" min-width="180" />
      <el-table-column label="供应商" prop="supplierName" min-width="160" />
      <el-table-column label="质检状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getQualityStatusTagType(row.status)">
            {{ getQualityStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="质检结果" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getQualityResultTagType(row.result)">
            {{ getQualityResultLabel(row.result) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前轮次" width="100" align="center">
        <template #default="{ row }">
          {{ row.currentRoundNo || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="复检原因" min-width="180">
        <template #default="{ row }">
          <el-tooltip v-if="row.recheckReason" :content="row.recheckReason" placement="top">
            <span class="inline-block max-w-160px truncate">{{ row.recheckReason }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="指派质检人" min-width="150">
        <template #default="{ row }">
          <span>{{ row.assignedCheckerUserNickname || '-' }}</span>
          <el-tag
            v-if="Number(row.assignedCheckerUserId || 0) === currentUserId"
            size="small"
            type="success"
            class="ml-8px"
          >
            我
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最终质检人" prop="checkerUserNickname" min-width="120" />
      <el-table-column label="质检时间" min-width="180">
        <template #default="{ row }">
          {{ row.checkTime ? formatDate(row.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="240" align="center">
        <template #default="{ row }">
          <el-button v-if="canAssignChecker(row)" link type="warning" @click="openAssignDialog(row)">
            {{ row.assignedCheckerUserId ? '改派' : '指派' }}
          </el-button>
          <el-button v-if="canHandleQuality(row)" link type="success" @click="openDetail(row, 'submit')">
            处理质检
          </el-button>
          <el-button
            v-if="hasQualityQueryPermission"
            link
            type="primary"
            @click="openDetail(row, 'view')"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="assignDialogVisible" title="指派质检人" width="520">
    <el-form label-width="100px">
      <el-form-item label="质检单号">
        <span>{{ assignTargetNo || '-' }}</span>
      </el-form-item>
      <el-form-item label="质检人" required>
        <el-select
          v-model="assignForm.assignedCheckerUserId"
          filterable
          clearable
          placeholder="请选择质检人"
          class="!w-100%"
          :loading="checkerOptionsLoading"
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
      <el-button @click="closeAssignDialog">取消</el-button>
      <el-button type="primary" :disabled="!canConfirmAssign" :loading="assignSubmitting" @click="handleAssignChecker">
        保存
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { checkPermi, checkRole } from '@/utils/permission'
import { getSimpleUserList, type UserVO } from '@/api/system/user'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  PurchaseInQualityApi,
  PURCHASE_IN_QUALITY_RESULT,
  PURCHASE_IN_QUALITY_STATUS,
  type PurchaseInQualityAssignCheckerReqVO,
  type PurchaseInQualityVO
} from '@/api/erp/purchase/in-quality'

defineOptions({ name: 'ErpPurchaseInQuality' })

const router = useRouter()
const message = useMessage()
const userStore = useUserStoreWithOut()
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

const getQualityStatusLabel = (status?: number) => {
  if (status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING) return '首检中'
  if (status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return '待复检'
  if (status === PURCHASE_IN_QUALITY_STATUS.RECHECKING) return '复检中'
  if (status === PURCHASE_IN_QUALITY_STATUS.DONE) return '已完成'
  if (status === PURCHASE_IN_QUALITY_STATUS.VOID) return '已作废'
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
  if (result === PURCHASE_IN_QUALITY_RESULT.PARTIAL) return '部分合格'
  if (result === PURCHASE_IN_QUALITY_RESULT.PASSED) return '全部合格'
  if (result === PURCHASE_IN_QUALITY_RESULT.REJECTED) return '全部不合格'
  return '待判定'
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

const canConfirmAssign = computed(() => {
  return !!assignForm.id && !!assignForm.assignedCheckerUserId && !assignSubmitting.value
})

const getList = async () => {
  pageLoading.value = true
  try {
    const data = await PurchaseInQualityApi.getPurchaseInQualityPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    pageLoading.value = false
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
    message.success('指派质检人成功')
    closeAssignDialog()
    await getList()
  } finally {
    assignSubmitting.value = false
  }
}

const openDetail = (row: PurchaseInQualityVO, mode: 'submit' | 'view') => {
  router.push({
    path: '/erp/purchase/in-quality/detail',
    query: {
      id: row.id,
      mode,
      from: 'quality-list'
    }
  })
}

onMounted(() => {
  getList()
})
</script>
