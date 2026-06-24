<template>
  <div>
    <ContentWrap v-if="pageInitializing" class="mb-12px">
      <el-skeleton :rows="10" animated />
    </ContentWrap>
    <el-empty v-else-if="!qualityOrder?.id" description="未找到质检单数据，请返回列表后重试。">
      <el-button @click="goBack">返回</el-button>
    </el-empty>
    <template v-else>
      <ContentWrap v-if="pageRefreshing" class="mb-12px">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="正在同步最新质检数据，页面内容保持可查看。"
        />
      </ContentWrap>
      <ContentWrap class="mb-12px">
        <div class="flex items-center justify-between gap-12px flex-wrap">
          <div>
            <div class="text-18px font-600">{{ pageTitle }}</div>
            <div class="mt-6px text-12px text-[var(--el-text-color-secondary)]">
              入库单号：{{ qualityOrder.purchaseInNo || '-' }}
            </div>
          </div>
          <div class="flex items-center gap-8px flex-wrap">
            <el-tag :type="getQualityStatusTagType(qualityOrder.status)">
              {{ getQualityStatusLabel(qualityOrder.status) }}
            </el-tag>
            <el-tag :type="getQualityResultTagType(qualityOrder.result)">
              {{ getQualityResultLabel(qualityOrder.result) }}
            </el-tag>
            <el-button @click="goBack">返回</el-button>
            <el-button
              v-if="showAssignCheckerAction"
              type="warning"
              :disabled="assignCheckerLoading"
              :loading="assignCheckerLoading"
              @click="openAssignCheckerDialog"
            >
              {{ qualityOrder.assignedCheckerUserId ? '重新指派质检人' : '指派质检人' }}
            </el-button>
            <el-button
              v-if="showSubmitFirstCheckAction"
              type="primary"
              :disabled="!canSubmitFirstCheck"
              :loading="submitFirstCheckLoading"
              @click="handleSubmitFirstCheck"
            >
              提交初检
            </el-button>
            <el-button
              v-if="showSubmitRecheckAction"
              type="primary"
              :disabled="!canSubmitRecheck"
              :loading="submitRecheckLoading"
              @click="handleSubmitRecheck"
            >
              提交复检
            </el-button>
            <el-button
              v-if="showNavigateStockExecuteAction"
              type="success"
              @click="goToStockExecute"
            >
              去执行入库
            </el-button>
            <el-button
              v-if="showCreateReturnAction"
              v-hasPermi="['erp:purchase-in-quality:update']"
              type="danger"
              :loading="createReturnLoading"
              @click="handleCreateReturn"
            >
              转退货单
            </el-button>
          </div>
        </div>
      </ContentWrap>

      <ContentWrap title="质检单信息" class="mb-12px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="质检单号">{{ qualityOrder.no || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库单号">{{ qualityOrder.purchaseInNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购单号">{{ qualityOrder.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ qualityOrder.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库状态">
            <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="qualityOrder.purchaseInStatus" />
          </el-descriptions-item>
          <el-descriptions-item label="入库处理状态">
            <el-tag :type="getStockInStatusTagType(qualityOrder.stockInStatus, qualityOrder.qaStatus)">
              {{ getStockInStatusLabel(qualityOrder.stockInStatus, qualityOrder.qaStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前轮次">{{ qualityOrder.currentRoundNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="抽样方案">{{ getSamplingSchemeLabel() }}</el-descriptions-item>
          <el-descriptions-item label="指派质检人">{{ qualityOrder.assignedCheckerUserNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="指派时间">
            {{ qualityOrder.assignedCheckerTime ? formatDate(qualityOrder.assignedCheckerTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="当前质检人">{{ qualityOrder.checkerUserNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质检时间">
            {{ qualityOrder.checkTime ? formatDate(qualityOrder.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请复检人">{{ qualityOrder.recheckApplyUserNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请复检时间">
            {{ qualityOrder.recheckApplyTime ? formatDate(qualityOrder.recheckApplyTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="合格数量">{{ erpCountInputFormatter(qualityOrder.passCount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ erpCountInputFormatter(qualityOrder.rejectCount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="已入库数量">{{ erpCountInputFormatter(qualityOrder.stockInCount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="剩余待入库数量">{{ erpCountInputFormatter(qualityOrder.remainingStockInCount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="复检原因" :span="2">{{ qualityOrder.recheckReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ qualityOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </ContentWrap>

      <ContentWrap title="质检处理" class="mb-12px">
        <el-alert
          v-if="assignmentAlertMessage"
          :type="assignmentAlertType"
          :closable="false"
          show-icon
          :title="assignmentAlertMessage"
          class="mb-12px"
        />
        <el-alert
          v-if="qualityOrder.status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING"
          type="warning"
          :closable="false"
          show-icon
          title="当前处于初检阶段，请填写本轮质检结果并提交。"
          class="mb-12px"
        />
        <el-alert
          v-else-if="qualityOrder.status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK"
          type="error"
          :closable="false"
          show-icon
          title="当前待发起复检，请先填写复检原因。"
          class="mb-12px"
        />
        <el-alert
          v-else-if="qualityOrder.status === PURCHASE_IN_QUALITY_STATUS.RECHECKING"
          type="info"
          :closable="false"
          show-icon
          title="当前处于复检阶段，请完成复检结果录入并提交。"
          class="mb-12px"
        />
        <el-alert
          v-else
          type="success"
          :closable="false"
          show-icon
          title="当前阶段暂无可编辑操作，展示后端最新状态。"
          class="mb-12px"
        />
        <el-alert
          v-if="stockExecuteAlertMessage"
          :type="stockExecuteAlertType"
          :closable="false"
          show-icon
          class="mb-12px"
        >
          <template #title>
            <div class="flex items-center justify-between gap-12px flex-wrap">
              <span>{{ stockExecuteAlertMessage }}</span>
              <el-button
                v-if="showNavigateStockExecuteAction"
                link
                type="primary"
                @click="goToStockExecute"
              >
                去执行入库
              </el-button>
            </div>
          </template>
        </el-alert>

        <template v-if="canEditFirstCheckForm">
          <el-table :data="firstCheckRows" stripe border>
            <el-table-column type="index" label="#" width="60" align="center" />
            <el-table-column label="产品" prop="productName" min-width="180" />
            <el-table-column label="入库数量" width="100" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.count || 0) }}</template>
            </el-table-column>
            <el-table-column label="计划抽样数" width="110" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.sampleCount || 0) }}</template>
            </el-table-column>
            <el-table-column label="本次抽样数" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.formSampleCount"
                  controls-position="right"
                  :min="0"
                  :max="Number(row.sampleCount || 0)"
                  :precision="3"
                  class="!w-100%"
                  @change="syncFirstCheckCounts(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="本次合格数" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.roundPassCount"
                  controls-position="right"
                  :min="0"
                  :max="Number(row.formSampleCount || 0)"
                  :precision="3"
                  class="!w-100%"
                  @change="syncFirstCheckCounts(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="本次不合格数" width="120" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.roundRejectCount || 0) }}</template>
            </el-table-column>
            <el-table-column label="缺陷明细" min-width="220">
              <template #default="{ row }">
                <div class="flex items-center gap-8px">
                  <el-button link type="primary" @click="openDefectDialog('first', row)">编辑缺陷</el-button>
                  <span class="text-[var(--el-text-color-secondary)]">{{ getDefectSummary(row.defects) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.roundRemark" placeholder="请输入备注" />
              </template>
            </el-table-column>
          </el-table>
        </template>

        <template v-else-if="qualityOrder.status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK">
          <el-form label-width="100px">
            <el-form-item label="复检原因" required>
              <el-input
                v-if="showStartRecheckAction"
                v-model="recheckReason"
                type="textarea"
                :rows="3"
                maxlength="255"
                show-word-limit
                placeholder="请输入复检原因"
              />
              <el-input
                v-else
                :model-value="qualityOrder.recheckReason || recheckReason || '-'"
                type="textarea"
                :rows="3"
                readonly
              />
            </el-form-item>
            <el-form-item v-if="routeMode === 'submit'">
              <div class="w-100% flex justify-end">
                <el-button
                  v-if="showStartRecheckAction"
                  type="primary"
                  :disabled="!canStartRecheck"
                  :loading="startRecheckLoading"
                  @click="handleStartRecheck"
                >
                  发起复检
                </el-button>
              </div>
            </el-form-item>
            <el-form-item v-if="!showStartRecheckAction && startRecheckReadonlyMessage">
              <el-alert
                type="info"
                :closable="false"
                show-icon
                :title="startRecheckReadonlyMessage"
                class="w-100%"
              />
            </el-form-item>
          </el-form>
        </template>

        <template v-else-if="canEditRecheckForm">
          <el-table :data="recheckRows" stripe border>
            <el-table-column type="index" label="#" width="60" align="center" />
            <el-table-column label="产品" prop="productName" min-width="180" />
            <el-table-column label="入库数量" width="100" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.count || 0) }}</template>
            </el-table-column>
            <el-table-column label="可复检数量" width="120" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.recheckLimit || 0) }}</template>
            </el-table-column>
            <el-table-column label="本次复检数" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.formSampleCount"
                  controls-position="right"
                  :min="0"
                  :max="Number(row.recheckLimit || 0)"
                  :precision="3"
                  class="!w-100%"
                  @change="syncRecheckCounts(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="本次合格数" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.roundPassCount"
                  controls-position="right"
                  :min="0"
                  :max="Number(row.formSampleCount || 0)"
                  :precision="3"
                  class="!w-100%"
                  @change="syncRecheckCounts(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="本次不合格数" width="120" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.roundRejectCount || 0) }}</template>
            </el-table-column>
            <el-table-column label="最终合格数" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.finalPassCount"
                  controls-position="right"
                  :min="0"
                  :max="Number(row.count || 0)"
                  :precision="3"
                  class="!w-100%"
                  @change="syncRecheckFinalCounts(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="最终不合格数" width="120" align="right">
              <template #default="{ row }">{{ erpCountInputFormatter(row.finalRejectCount || 0) }}</template>
            </el-table-column>
            <el-table-column label="缺陷明细" min-width="220">
              <template #default="{ row }">
                <div class="flex items-center gap-8px">
                  <el-button link type="primary" @click="openDefectDialog('recheck', row)">编辑缺陷</el-button>
                  <span class="text-[var(--el-text-color-secondary)]">{{ getDefectSummary(row.defects) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.roundRemark" placeholder="请输入备注" />
              </template>
            </el-table-column>
          </el-table>
        </template>

        <template v-else>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="当前阶段">{{ getReadonlyStageText() }}</el-descriptions-item>
            <el-descriptions-item label="是否需要复检">
              {{ qualityOrder.recheckRequired ? '是' : '否' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </ContentWrap>

      <ContentWrap title="质检轮次记录" class="mb-12px">
        <el-table :data="qualityOrder.rounds || []" stripe border>
          <el-table-column label="轮次" width="120" align="center">
            <template #default="{ row }">{{ getRoundLabel(row.id) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.roundType === PURCHASE_IN_QUALITY_ROUND_TYPE.RECHECK ? 'primary' : 'warning'">
                {{ row.roundType === PURCHASE_IN_QUALITY_ROUND_TYPE.RECHECK ? '复检' : '初检' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="产品" min-width="160">
            <template #default="{ row }">{{ getItemName(row.qualityItemId) }}</template>
          </el-table-column>
          <el-table-column label="抽样数量" width="110" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.sampleCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="合格数量" width="110" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.passCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="不合格数量" width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.rejectCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="质检人" prop="checkerUserNickname" min-width="120" />
          <el-table-column label="质检时间" min-width="180">
            <template #default="{ row }">
              {{ row.checkTime ? formatDate(row.checkTime as Date, 'YYYY-MM-DD HH:mm:ss') : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="180" />
        </el-table>
      </ContentWrap>

      <ContentWrap title="缺陷明细记录" class="mb-12px">
        <el-table :data="qualityOrder.defects || []" stripe border>
          <el-table-column label="轮次" width="120" align="center">
            <template #default="{ row }">{{ getRoundLabel(row.roundId) }}</template>
          </el-table-column>
          <el-table-column label="产品" min-width="160">
            <template #default="{ row }">{{ getItemName(row.qualityItemId) }}</template>
          </el-table-column>
          <el-table-column label="缺陷原因" prop="defectReasonName" min-width="150" />
          <el-table-column label="缺陷数量" width="120" align="right">
            <template #default="{ row }">{{ erpCountInputFormatter(row.defectCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="备注" prop="defectRemark" min-width="180" />
        </el-table>
      </ContentWrap>

      <Dialog v-model="defectDialogVisible" title="编辑缺陷明细" :width="dialogWidth.defect">
        <div class="detail-dialog__body">
        <div class="mb-12px text-[var(--el-text-color-secondary)]">
          当前正在编辑{{ defectDialogStage === 'recheck' ? '复检' : '初检' }}阶段的不合格明细，
          不合格数量：{{ erpCountInputFormatter(editingRowRejectCount) }}
        </div>
        <div class="mb-12px text-[var(--el-text-color-secondary)]">
          已录入缺陷数量：{{ erpCountInputFormatter(defectDraftTotal) }}，
          剩余可分配：{{ erpCountInputFormatter(defectDraftRemaining) }}
        </div>
        <el-table :data="defectDraftRows" border stripe>
          <el-table-column label="缺陷原因" min-width="220">
            <template #default="{ row }">
              <div class="flex flex-col gap-8px">
                <div class="flex items-center gap-8px">
                  <el-select
                    v-if="row.inputMode === 'standard'"
                    v-model="row.defectReasonId"
                    filterable
                    clearable
                    placeholder="请选择标准缺陷原因"
                    class="!w-100%"
                    :loading="defectReasonOptionsLoading"
                    @change="handleStandardDefectReasonChange(row, $event)"
                  >
                    <el-option
                      v-for="item in defectReasonOptions"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                  <el-input
                    v-else
                    v-model="row.defectReasonName"
                    maxlength="64"
                    placeholder="请输入自定义缺陷原因"
                  />
                  <el-button
                    link
                    type="primary"
                    @click="switchDefectInputMode(row, row.inputMode === 'standard' ? 'custom' : 'standard')"
                  >
                    {{ row.inputMode === 'standard' ? '切换为自定义' : '切换为标准' }}
                  </el-button>
                </div>
                <span class="text-12px text-[var(--el-text-color-secondary)]">
                  {{ row.inputMode === 'standard' ? '标准模式下从缺陷原因库中选择。' : '自定义模式下可直接录入缺陷原因名称。' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="缺陷数量" width="180">
            <template #default="{ row }">
              <el-input-number
                v-model="row.defectCount"
                controls-position="right"
                :min="0"
                :max="getDefectDraftRowMax(row)"
                :precision="3"
                class="!w-100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.defectRemark" placeholder="请输入备注" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeDefectDraftRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt-12px">
          <el-button plain @click="addDefectDraftRow">新增一行</el-button>
        </div>
        </div>
        <template #footer>
          <el-button @click="defectDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!canSaveDefectDialog" @click="saveDefectDialog">保存</el-button>
        </template>
      </Dialog>

      <Dialog v-model="assignDialogVisible" title="指派质检人" :width="dialogWidth.assign">
        <el-form label-width="100px">
          <el-form-item label="质检单号">
            <span>{{ qualityOrder.no || '-' }}</span>
          </el-form-item>
          <el-form-item label="当前质检人">
            <span>{{ qualityOrder.assignedCheckerUserNickname || '-' }}</span>
          </el-form-item>
          <el-form-item label="指派质检人" required>
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
          <el-button @click="closeAssignCheckerDialog">取消</el-button>
          <el-button
            type="primary"
            :disabled="!canConfirmAssignChecker"
            :loading="assignCheckerLoading"
            @click="handleAssignChecker"
          >
            保存
          </el-button>
        </template>
      </Dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { checkPermi, checkRole } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE } from '@/utils/dict'
import { erpCountInputFormatter } from '@/utils'
import { getSimpleUserList, type UserVO } from '@/api/system/user'
import { useUserStoreWithOut } from '@/store/modules/user'
import { resolvePurchaseInQualityDetailLoadingState } from './loadingState'
import { deriveRecheckFinalCounts } from './recheckForm'
import {
  CUSTOM_DEFECT_REASON_ID,
  PurchaseInQualityApi,
  PURCHASE_IN_QUALITY_RESULT,
  PURCHASE_IN_QUALITY_ROUND_TYPE,
  PURCHASE_IN_QUALITY_STATUS,
  QC_SAMPLING_SCHEME_TYPE,
  type QcDefectReasonSimpleVO,
  type PurchaseInQualityAssignCheckerReqVO,
  type PurchaseInQualityDefectReqVO,
  type PurchaseInQualityDefectVO,
  type PurchaseInQualityItemVO,
  type PurchaseInQualityRoundVO,
  type PurchaseInQualityStartRecheckReqVO,
  type PurchaseInQualitySubmitFirstCheckReqVO,
  type PurchaseInQualitySubmitRecheckReqVO,
  type PurchaseInQualityVO
} from '@/api/erp/purchase/in-quality'

defineOptions({ name: 'ErpPurchaseInQualityDetailPage' })

type DefectInputMode = 'standard' | 'custom'
type EditableDefect = PurchaseInQualityDefectReqVO & { localId: string; inputMode: DefectInputMode }
type FirstCheckRow = PurchaseInQualityItemVO & {
  formSampleCount: number
  roundPassCount: number
  roundRejectCount: number
  roundRemark?: string
  defects: EditableDefect[]
}
type RecheckRow = PurchaseInQualityItemVO & {
  recheckLimit: number
  formSampleCount: number
  roundPassCount: number
  roundRejectCount: number
  finalPassCount: number
  finalRejectCount: number
  finalCountsManuallyEdited: boolean
  roundRemark?: string
  defects: EditableDefect[]
}

const PURCHASE_IN_STATUS = { PROCESS: 10, APPROVE: 20, REJECT: 30 } as const
const PURCHASE_IN_QA_STATUS = { TO_INSPECT: 10, PARTIAL: 20, PASSED: 30, REJECTED: 40 } as const
const PURCHASE_IN_STOCK_IN_STATUS = { TO_STOCK_IN: 10, PARTIAL_STOCKED_IN: 15, STOCKED_IN: 20, NO_NEED_STOCK_IN: 30 } as const

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStoreWithOut()

const detailLoading = ref(false)
const createLoading = ref(false)
const submitFirstCheckLoading = ref(false)
const startRecheckLoading = ref(false)
const submitRecheckLoading = ref(false)
const defectReasonOptionsLoading = ref(false)
const checkerOptionsLoading = ref(false)
const assignCheckerLoading = ref(false)

const qualityOrder = ref<PurchaseInQualityVO>()
const firstCheckRows = ref<FirstCheckRow[]>([])
const recheckRows = ref<RecheckRow[]>([])
const recheckReason = ref('')
const defectReasonOptions = ref<QcDefectReasonSimpleVO[]>([])
const checkerUserOptions = ref<UserVO[]>([])

const defectDialogVisible = ref(false)
const defectDialogStage = ref<'first' | 'recheck'>('first')
const editingRowId = ref<number>()
const defectDraftRows = ref<EditableDefect[]>([])
const assignDialogVisible = ref(false)
const assignForm = ref<PurchaseInQualityAssignCheckerReqVO>({ id: 0, assignedCheckerUserId: 0 })

const routeQualityId = computed(() => Number(route.query.id || 0) || undefined)
const routePurchaseInId = computed(() => Number(route.query.purchaseInId || 0) || undefined)
const routeMode = computed(() => String(route.query.mode || 'submit'))
const routeFrom = computed(() => String(route.query.from || ''))
const routeScene = computed(() => String(route.query.scene || 'erp'))
const currentUserId = computed(() => Number(userStore.getUser.id || 0))
const pageLoadingState = computed(() =>
  resolvePurchaseInQualityDetailLoadingState({
    detailLoading: detailLoading.value,
    createLoading: createLoading.value,
    hasQualityId: Boolean(qualityOrder.value?.id)
  })
)
const pageInitializing = computed(() => pageLoadingState.value.pageInitializing)
const pageRefreshing = computed(() => pageLoadingState.value.pageRefreshing)
const pageTitle = computed(() => {
  const no = qualityOrder.value?.no || `质检单 #${qualityOrder.value?.id}`
  return routeScene.value === 'qms' ? `来料检验 / ${no}` : no
})
const dialogWidth = {
  assign: 'min(520px, 92vw)',
  defect: 'min(820px, 96vw)'
}

const hasQualityCreatePermission = checkPermi(['erp:purchase-in-quality:create'])
const hasFirstCheckPermission = checkPermi(['erp:purchase-in-quality:first-check'])
const hasStartRecheckPermission = checkPermi(['erp:purchase-in-quality:start-recheck'])
const hasRecheckPermission = checkPermi(['erp:purchase-in-quality:recheck'])
const hasAssignCheckerPermission = checkPermi(['erp:purchase-in-quality:assign-checker'])
const hasStockExecutePermission = checkPermi(['erp:purchase-in:update-status'])
const isSuperAdmin = checkRole(['super_admin'])

const defectReasonOptionMap = computed<Record<number, QcDefectReasonSimpleVO>>(() => {
  return defectReasonOptions.value.reduce<Record<number, QcDefectReasonSimpleVO>>((map, item) => {
    map[item.id] = item
    return map
  }, {})
})

const hasAssignedChecker = computed(() => !!qualityOrder.value?.assignedCheckerUserId)
const isAssignedChecker = computed(() => {
  return !!qualityOrder.value?.assignedCheckerUserId &&
    Number(qualityOrder.value.assignedCheckerUserId) === currentUserId.value
})

const canEditFirstCheckForm = computed(() => {
  return (
    routeMode.value === 'submit' &&
    qualityOrder.value?.status === PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING &&
    hasFirstCheckPermission &&
    (isAssignedChecker.value || isSuperAdmin)
  )
})

const showAssignCheckerAction = computed(() => {
  return (
    routeMode.value === 'submit' &&
    hasAssignCheckerPermission &&
    [
      PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING,
      PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK,
      PURCHASE_IN_QUALITY_STATUS.RECHECKING
    ].includes(Number(qualityOrder.value?.status) as any)
  )
})

const showStartRecheckAction = computed(() => {
  return (
    routeMode.value === 'submit' &&
    qualityOrder.value?.status === PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK &&
    (hasStartRecheckPermission || isSuperAdmin)
  )
})

const startRecheckReadonlyMessage = computed(() => {
  if (qualityOrder.value?.status !== PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK) return ''
  if (routeMode.value !== 'submit') return '当前为查看模式，复检原因仅供查看。'
  if (!hasStartRecheckPermission && !isSuperAdmin) return '当前账号没有发起复检权限，复检原因仅供查看。'
  return ''
})

const canStartRecheck = computed(() => {
  return showStartRecheckAction.value &&
    !!recheckReason.value.trim() &&
    hasAssignedChecker.value &&
    !startRecheckLoading.value
})

const canEditRecheckForm = computed(() => {
  return (
    routeMode.value === 'submit' &&
    qualityOrder.value?.status === PURCHASE_IN_QUALITY_STATUS.RECHECKING &&
    hasRecheckPermission &&
    (isAssignedChecker.value || isSuperAdmin)
  )
})

const showSubmitFirstCheckAction = computed(() => canEditFirstCheckForm.value)
const canSubmitFirstCheck = computed(() => {
  return canEditFirstCheckForm.value &&
    hasAssignedChecker.value &&
    !submitFirstCheckLoading.value &&
    validateFirstCheck(false)
})
const showSubmitRecheckAction = computed(() => canEditRecheckForm.value)
const canSubmitRecheck = computed(() => {
  return canEditRecheckForm.value &&
    hasAssignedChecker.value &&
    !submitRecheckLoading.value &&
    validateRecheck(false)
})
const canConfirmAssignChecker = computed(() => {
  return !!assignForm.value.id && !!assignForm.value.assignedCheckerUserId && !assignCheckerLoading.value
})

const canNavigateToStockExecute = computed(() => {
  const order = qualityOrder.value
  if (!order?.purchaseInId || !hasStockExecutePermission) {
    return false
  }
  if (Number(order.purchaseInStatus) !== PURCHASE_IN_STATUS.APPROVE) {
    return false
  }
  if (
    ![PURCHASE_IN_QA_STATUS.PARTIAL, PURCHASE_IN_QA_STATUS.PASSED].includes(
      Number(order.qaStatus) as any
    )
  ) {
    return false
  }
  if (
    ![
      PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN,
      PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN
    ].includes(Number(order.stockInStatus) as any)
  ) {
    return false
  }
  return toNumber(order.remainingStockInCount) > 0
})

const showNavigateStockExecuteAction = computed(() => canNavigateToStockExecute.value)

// 转退货单：质检完成且有不合格品时显示
const showCreateReturnAction = computed(() => {
  const order = qualityOrder.value
  if (!order) return false
  // 质检已完成且有不合格品
  return Number(order.status) === PURCHASE_IN_QUALITY_STATUS.DONE &&
    toNumber(order.rejectCount) > 0
})

const createReturnLoading = ref(false)

const handleCreateReturn = async () => {
  try {
    await ElMessageBox.confirm(
      `确认从质检单 ${qualityOrder.value?.no || ''} 创建采购退货单？将包含 ${erpCountInputFormatter(qualityOrder.value?.rejectCount || 0)} 不合格品。`,
      '创建退货单',
      { type: 'warning' }
    )

    createReturnLoading.value = true
    const returnId = await PurchaseInQualityApi.createReturnFromQuality(qualityOrder.value!.id!)
    ElMessage.success(`退货单创建成功，单号：${returnId}`)

    // 跳转到退货单编辑页面
    router.push(`/erp/purchase/return?id=${returnId}`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '创建退货单失败')
    }
  } finally {
    createReturnLoading.value = false
  }
}

const stockExecuteAlertMessage = computed(() => {
  const order = qualityOrder.value
  if (!order || Number(order.status) !== PURCHASE_IN_QUALITY_STATUS.DONE) {
    return ''
  }
  const remainingCount = erpCountInputFormatter(order.remainingStockInCount || 0)
  if (canNavigateToStockExecute.value) {
    return `复检完成，仍有 ${remainingCount} 待入库，请继续完成入库。`
  }
  if (
    Number(order.purchaseInStatus) === PURCHASE_IN_STATUS.APPROVE &&
    [
      PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN,
      PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN
    ].includes(Number(order.stockInStatus) as any) &&
    toNumber(order.remainingStockInCount) > 0
  ) {
    return `复检完成，仍有 ${remainingCount} 待入库，请通知仓库执行入库。`
  }
  return ''
})

const stockExecuteAlertType = computed(() => {
  if (showNavigateStockExecuteAction.value) {
    return 'warning'
  }
  return 'info'
})

const assignmentAlertType = computed(() => {
  if (!hasAssignedChecker.value) return 'error'
  if (!isAssignedChecker.value &&
    !isSuperAdmin &&
    [PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING, PURCHASE_IN_QUALITY_STATUS.RECHECKING].includes(
      Number(qualityOrder.value?.status) as any
    )) {
    return 'info'
  }
  return 'warning'
})

const assignmentAlertMessage = computed(() => {
  if (!qualityOrder.value) return ''
  if (!hasAssignedChecker.value &&
    [
      PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING,
      PURCHASE_IN_QUALITY_STATUS.WAIT_RECHECK,
      PURCHASE_IN_QUALITY_STATUS.RECHECKING
    ].includes(Number(qualityOrder.value.status) as any)) {
    return '当前未指派质检人，请先完成指派后再继续处理。'
  }
  if (!isAssignedChecker.value &&
    !isSuperAdmin &&
    [PURCHASE_IN_QUALITY_STATUS.FIRST_CHECKING, PURCHASE_IN_QUALITY_STATUS.RECHECKING].includes(
      Number(qualityOrder.value.status) as any
    )) {
    return `当前质检单已指派给 ${qualityOrder.value.assignedCheckerUserNickname || '指定质检人'}，当前账号仅可查看。`
  }
  return ''
})

const qualityItemNameMap = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {}
  ;(qualityOrder.value?.items || []).forEach((item) => {
    if (item.id) map[item.id] = item.productName || `产品 #${item.id}`
  })
  return map
})

const roundMap = computed<Record<number, PurchaseInQualityRoundVO>>(() => {
  const map: Record<number, PurchaseInQualityRoundVO> = {}
  ;(qualityOrder.value?.rounds || []).forEach((round) => {
    if (round.id) map[round.id] = round
  })
  return map
})

const editingRowRejectCount = computed(() => {
  if (!editingRowId.value) return 0
  const rows = defectDialogStage.value === 'recheck' ? recheckRows.value : firstCheckRows.value
  const row = rows.find((item) => item.id === editingRowId.value)
  return toNumber(row?.roundRejectCount)
})

const defectDraftTotal = computed(() => sumDefects(defectDraftRows.value))
const defectDraftRemaining = computed(() => roundCount(Math.max(editingRowRejectCount.value - defectDraftTotal.value, 0)))
const canSaveDefectDialog = computed(() => validateDefectDraftRows(false))

const toNumber = (value?: number | string | null) => Number(value || 0)
const roundCount = (value: number) => Number(value.toFixed(3))
const hasStandardDefectReason = (defectReasonId?: number) => !!defectReasonId && !!defectReasonOptionMap.value[defectReasonId]

const applyDerivedRecheckFinalCounts = (row: RecheckRow) => {
  const { finalPassCount, finalRejectCount } = deriveRecheckFinalCounts({
    count: toNumber(row.count),
    firstRejectCount: toNumber(row.recheckLimit),
    roundPassCount: toNumber(row.roundPassCount)
  })
  row.finalPassCount = roundCount(finalPassCount)
  row.finalRejectCount = roundCount(finalRejectCount)
}

const resolveDefectInputMode = (partial?: Partial<EditableDefect>): DefectInputMode => {
  if (!partial) return 'standard'
  if (partial.inputMode) return partial.inputMode
  return hasStandardDefectReason(partial.defectReasonId) ? 'standard' : 'custom'
}

const createDefectDraft = (partial?: Partial<EditableDefect>): EditableDefect => ({
  localId: `${Date.now()}-${Math.random()}`,
  defectReasonId: partial?.defectReasonId,
  defectReasonName: partial?.defectReasonName,
  defectCount: partial?.defectCount,
  defectRemark: partial?.defectRemark,
  inputMode: resolveDefectInputMode(partial)
})

const sumDefects = (defects?: Array<EditableDefect | PurchaseInQualityDefectVO>) => {
  return roundCount((defects || []).reduce((sum, item) => sum + toNumber(item.defectCount), 0))
}

const getItemName = (qualityItemId?: number) => {
  return qualityItemId ? qualityItemNameMap.value[qualityItemId] || `产品 #${qualityItemId}` : '-'
}

const getRoundLabel = (roundId?: number) => {
  const round = roundId ? roundMap.value[roundId] : undefined
  if (!round) return '-'
  const typeLabel = round.roundType === PURCHASE_IN_QUALITY_ROUND_TYPE.RECHECK ? '复检' : '初检'
  return `第 ${round.roundNo || '-'} 轮 ${typeLabel}`
}

const resolveSavedDefectReasonId = (item: EditableDefect) => {
  return item.inputMode === 'custom' ? CUSTOM_DEFECT_REASON_ID : Number(item.defectReasonId || 0)
}

const resolveSavedDefectReasonName = (item: EditableDefect) => {
  if (item.inputMode === 'custom') {
    return item.defectReasonName?.trim()
  }
  return item.defectReasonId ? defectReasonOptionMap.value[item.defectReasonId]?.name : undefined
}

const getDefectSummary = (defects?: EditableDefect[]) => {
  if (!defects?.length) return '未维护'
  return defects
    .map((item) => {
      const reasonName = resolveSavedDefectReasonName(item) || item.defectReasonName || '未命名原因'
      return `${reasonName} x ${erpCountInputFormatter(toNumber(item.defectCount))}`
    })
    .join('，')
}

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

const getStockInStatusLabel = (stockInStatus?: number, qaStatus?: number) => {
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.STOCKED_IN) return '已入库'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN) return '部分入库'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN) return '待入库'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.NO_NEED_STOCK_IN || qaStatus === PURCHASE_IN_QA_STATUS.REJECTED) return '无需入库'
  return '待质检'
}

const getStockInStatusTagType = (stockInStatus?: number, qaStatus?: number) => {
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.STOCKED_IN) return 'success'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.PARTIAL_STOCKED_IN) return 'warning'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.TO_STOCK_IN) return 'warning'
  if (stockInStatus === PURCHASE_IN_STOCK_IN_STATUS.NO_NEED_STOCK_IN || qaStatus === PURCHASE_IN_QA_STATUS.REJECTED) return 'info'
  return 'info'
}

const getSamplingSchemeLabel = () => {
  const order = qualityOrder.value
  if (!order) return '-'
  const typeLabel =
    order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.FULL ? '全检' :
    order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.RATIO ? '按比例抽样' :
    order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.FIXED ? '固定数量抽样' :
    order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.SKIP ? '免检' : '-'

  const details: string[] = []
  if (order.samplingSchemeName) details.push(order.samplingSchemeName)
  if (order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.RATIO && order.samplingRatio !== undefined) {
    details.push(`比例 ${order.samplingRatio}%`)
  }
  if (order.samplingSchemeType === QC_SAMPLING_SCHEME_TYPE.FIXED && order.samplingFixedCount !== undefined) {
    details.push(`固定数量 ${erpCountInputFormatter(order.samplingFixedCount)}`)
  }
  if (order.minSampleCount !== undefined && order.maxSampleCount !== undefined) {
    details.push(`样本范围 ${erpCountInputFormatter(order.minSampleCount)} - ${erpCountInputFormatter(order.maxSampleCount)}`)
  }

  return [typeLabel, ...details].filter(Boolean).join(' / ') || '-'
}

const getReadonlyStageText = () => {
  if (qualityOrder.value?.status === PURCHASE_IN_QUALITY_STATUS.DONE) return '质检已完成，当前展示后端最终结果。'
  if (qualityOrder.value?.status === PURCHASE_IN_QUALITY_STATUS.VOID) return '当前质检单已作废。'
  return '当前阶段暂无可编辑操作。'
}

const buildFirstCheckRows = (items: PurchaseInQualityItemVO[] = []) => {
  firstCheckRows.value = items.map((item) => {
    const sampleCount = toNumber(item.sampleCount || item.count)
    return {
      ...item,
      formSampleCount: sampleCount,
      roundPassCount: sampleCount,
      roundRejectCount: 0,
      roundRemark: '',
      defects: []
    }
  })
}

const buildRecheckRows = (items: PurchaseInQualityItemVO[] = [], rounds: PurchaseInQualityRoundVO[] = []) => {
  const firstRoundMap: Record<number, PurchaseInQualityRoundVO> = {}
  rounds.filter((item) => item.roundNo === 1).forEach((item) => {
    if (item.qualityItemId) firstRoundMap[item.qualityItemId] = item
  })
  recheckRows.value = items.map((item) => {
    const recheckLimit = toNumber(firstRoundMap[item.id || 0]?.rejectCount)
    const row: RecheckRow = {
      ...item,
      recheckLimit,
      formSampleCount: recheckLimit,
      roundPassCount: recheckLimit,
      roundRejectCount: 0,
      finalPassCount: 0,
      finalRejectCount: 0,
      finalCountsManuallyEdited: false,
      roundRemark: '',
      defects: []
    }
    applyDerivedRecheckFinalCounts(row)
    return row
  })
}

const applyQualityOrder = (data?: PurchaseInQualityVO) => {
  qualityOrder.value = data
  recheckReason.value = data?.recheckReason || ''
  buildFirstCheckRows(data?.items || [])
  buildRecheckRows(data?.items || [], data?.rounds || [])
}

const syncFirstCheckCounts = (row: FirstCheckRow) => {
  const sample = Math.min(Math.max(toNumber(row.formSampleCount), 0), toNumber(row.sampleCount || 0))
  row.formSampleCount = roundCount(sample)
  const pass = Math.min(Math.max(toNumber(row.roundPassCount), 0), sample)
  row.roundPassCount = roundCount(pass)
  row.roundRejectCount = roundCount(sample - pass)
  if (row.roundRejectCount <= 0) row.defects = []
}

const syncRecheckCounts = (row: RecheckRow) => {
  const sample = Math.min(Math.max(toNumber(row.formSampleCount), 0), toNumber(row.recheckLimit))
  row.formSampleCount = roundCount(sample)
  const pass = Math.min(Math.max(toNumber(row.roundPassCount), 0), sample)
  row.roundPassCount = roundCount(pass)
  row.roundRejectCount = roundCount(sample - pass)
  if (!row.finalCountsManuallyEdited) {
    applyDerivedRecheckFinalCounts(row)
  }
  if (row.roundRejectCount <= 0) row.defects = []
}

const syncRecheckFinalCounts = (row: RecheckRow) => {
  const count = toNumber(row.count)
  const pass = Math.min(Math.max(toNumber(row.finalPassCount), 0), count)
  row.finalCountsManuallyEdited = true
  row.finalPassCount = roundCount(pass)
  row.finalRejectCount = roundCount(count - pass)
}

const validateFirstCheck = (showMessage: boolean) => {
  for (const row of firstCheckRows.value) {
    const rowName = row.productName || '当前产品'
    const planned = toNumber(row.sampleCount || row.count)
    const sample = toNumber(row.formSampleCount)
    const pass = toNumber(row.roundPassCount)
    const reject = toNumber(row.roundRejectCount)
    if (sample <= 0 || sample > planned) {
      if (showMessage) message.warning(`${rowName} 的本次抽样数必须大于 0 且不能超过计划抽样数。`)
      return false
    }
    if (roundCount(pass + reject) !== roundCount(sample)) {
      if (showMessage) message.warning(`${rowName} 的合格数与不合格数之和必须等于本次抽样数。`)
      return false
    }
    if (reject > 0 && roundCount(sumDefects(row.defects)) !== roundCount(reject)) {
      if (showMessage) message.warning(`${rowName} 的缺陷数量合计必须等于本次不合格数。`)
      return false
    }
  }
  return firstCheckRows.value.length > 0
}

const validateRecheck = (showMessage: boolean) => {
  for (const row of recheckRows.value) {
    const rowName = row.productName || '当前产品'
    const sample = toNumber(row.formSampleCount)
    const pass = toNumber(row.roundPassCount)
    const reject = toNumber(row.roundRejectCount)
    const finalPass = toNumber(row.finalPassCount)
    const finalReject = toNumber(row.finalRejectCount)
    const count = toNumber(row.count)
    if (sample <= 0 || sample > toNumber(row.recheckLimit)) {
      if (showMessage) message.warning(`${rowName} 的本次复检数必须大于 0 且不能超过可复检数量。`)
      return false
    }
    if (roundCount(pass + reject) !== roundCount(sample)) {
      if (showMessage) message.warning(`${rowName} 的合格数与不合格数之和必须等于本次复检数。`)
      return false
    }
    if (roundCount(finalPass + finalReject) !== roundCount(count)) {
      if (showMessage) message.warning(`${rowName} 的最终合格数与最终不合格数之和必须等于入库数量。`)
      return false
    }
    if (reject > 0 && roundCount(sumDefects(row.defects)) !== roundCount(reject)) {
      if (showMessage) message.warning(`${rowName} 的缺陷数量合计必须等于本次不合格数。`)
      return false
    }
  }
  return recheckRows.value.length > 0
}

const cloneDefects = (defects: EditableDefect[] = []) => defects.map((item) => createDefectDraft(item))
const isDefectDraftRowTouched = (item: EditableDefect) => {
  return !!resolveSavedDefectReasonName(item) || toNumber(item.defectCount) > 0 || !!item.defectRemark?.trim()
}

const getValidDefectDraftRows = () => {
  return defectDraftRows.value
    .filter((item) => isDefectDraftRowTouched(item))
    .map((item) => ({
      ...item,
      defectReasonId: resolveSavedDefectReasonId(item),
      defectReasonName: resolveSavedDefectReasonName(item),
      defectCount: roundCount(toNumber(item.defectCount)),
      defectRemark: item.defectRemark?.trim()
    }))
}

const getDefectDraftRowMax = (currentRow: EditableDefect) => {
  const totalWithoutCurrent = roundCount(
    defectDraftRows.value.reduce((sum, item) => {
      if (item.localId === currentRow.localId) return sum
      return sum + toNumber(item.defectCount)
    }, 0)
  )
  return roundCount(Math.max(editingRowRejectCount.value - totalWithoutCurrent, 0))
}

const validateDefectDraftRows = (showMessage: boolean) => {
  const rejectCount = roundCount(editingRowRejectCount.value)
  const validRows = getValidDefectDraftRows()
  for (const item of validRows) {
    if (!item.defectReasonName) {
      if (showMessage) message.warning('请填写缺陷原因。')
      return false
    }
    if (item.defectCount <= 0) {
      if (showMessage) message.warning('缺陷数量必须大于 0。')
      return false
    }
  }
  if (rejectCount <= 0) {
    return validRows.length === 0
  }
  if (validRows.length === 0) {
    if (showMessage) message.warning('请维护缺陷明细。')
    return false
  }
  const total = roundCount(validRows.reduce((sum, item) => sum + toNumber(item.defectCount), 0))
  if (total !== rejectCount) {
    if (showMessage) {
      message.warning(`缺陷数量合计 ${erpCountInputFormatter(total)}，必须等于不合格数量 ${erpCountInputFormatter(rejectCount)}。`)
    }
    return false
  }
  return true
}

const switchDefectInputMode = (row: EditableDefect, inputMode: DefectInputMode) => {
  row.inputMode = inputMode
  if (inputMode === 'standard') {
    row.defectReasonId = undefined
    row.defectReasonName = undefined
    return
  }
  row.defectReasonId = CUSTOM_DEFECT_REASON_ID
}

const handleStandardDefectReasonChange = (row: EditableDefect, defectReasonId?: number) => {
  row.defectReasonId = defectReasonId
  row.defectReasonName = defectReasonId ? defectReasonOptionMap.value[defectReasonId]?.name : undefined
}

const openDefectDialog = (stage: 'first' | 'recheck', row: FirstCheckRow | RecheckRow) => {
  defectDialogStage.value = stage
  editingRowId.value = Number(row.id)
  defectDraftRows.value = cloneDefects(row.defects)
  if (!defectDraftRows.value.length && toNumber(row.roundRejectCount) > 0) {
    defectDraftRows.value = [createDefectDraft()]
  }
  defectDialogVisible.value = true
}

const addDefectDraftRow = () => {
  defectDraftRows.value.push(createDefectDraft())
}

const removeDefectDraftRow = (index: number) => {
  defectDraftRows.value.splice(index, 1)
}

const saveDefectDialog = () => {
  if (!editingRowId.value) {
    defectDialogVisible.value = false
    return
  }
  if (!validateDefectDraftRows(true)) {
    return
  }
  const cleanedRows = getValidDefectDraftRows()
  const rows = defectDialogStage.value === 'recheck' ? recheckRows.value : firstCheckRows.value
  const targetRow = rows.find((item) => item.id === editingRowId.value)
  if (targetRow) {
    targetRow.defects = cleanedRows
  }
  defectDialogVisible.value = false
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

const openAssignCheckerDialog = async () => {
  if (!qualityOrder.value?.id) return
  assignForm.value = {
    id: qualityOrder.value.id,
    assignedCheckerUserId: Number(qualityOrder.value.assignedCheckerUserId || 0)
  }
  assignDialogVisible.value = true
  await loadCheckerOptions()
}

const closeAssignCheckerDialog = () => {
  assignDialogVisible.value = false
  assignForm.value = {
    id: 0,
    assignedCheckerUserId: 0
  }
}

const handleAssignChecker = async () => {
  if (!qualityOrder.value?.id || !canConfirmAssignChecker.value) return
  assignCheckerLoading.value = true
  try {
    await PurchaseInQualityApi.assignChecker({
      id: assignForm.value.id,
      assignedCheckerUserId: assignForm.value.assignedCheckerUserId
    })
    message.success('指派质检人成功')
    closeAssignCheckerDialog()
    await loadDetail(qualityOrder.value.id)
  } finally {
    assignCheckerLoading.value = false
  }
}

const loadDefectReasonOptions = async () => {
  defectReasonOptionsLoading.value = true
  try {
    defectReasonOptions.value =
      (await PurchaseInQualityApi.getDefectReasonSimpleList()) as QcDefectReasonSimpleVO[]
  } finally {
    defectReasonOptionsLoading.value = false
  }
}

const loadDetail = async (id: number) => {
  detailLoading.value = true
  try {
    const data = (await PurchaseInQualityApi.getPurchaseInQuality(id)) as PurchaseInQualityVO
    applyQualityOrder(data)
  } finally {
    detailLoading.value = false
  }
}

const initByPurchaseInId = async (purchaseInId: number) => {
  detailLoading.value = true
  let existed: PurchaseInQualityVO | undefined
  try {
    existed = (await PurchaseInQualityApi.getPurchaseInQualityByPurchaseInId(purchaseInId)) as PurchaseInQualityVO | undefined
    if (existed?.id) {
      applyQualityOrder(existed)
      return
    }
  } finally {
    detailLoading.value = false
  }

  if (routeMode.value !== 'submit') {
    qualityOrder.value = undefined
    message.warning('未找到对应的质检单数据。')
    return
  }

  if (!hasQualityCreatePermission) {
    qualityOrder.value = undefined
    message.warning('当前账号没有创建质检单的权限。')
    return
  }

  createLoading.value = true
  try {
    const qualityId = await PurchaseInQualityApi.createPurchaseInQuality({ purchaseInId })
    await loadDetail(Number(qualityId))
  } finally {
    createLoading.value = false
  }
}

const loadPage = async () => {
  if (routeQualityId.value) {
    await loadDetail(routeQualityId.value)
    return
  }
  if (routePurchaseInId.value) {
    await initByPurchaseInId(routePurchaseInId.value)
    return
  }
  message.warning('缺少质检单 ID 或采购入库单 ID。')
}

const handleStartRecheck = async () => {
  if (!qualityOrder.value?.id || !canStartRecheck.value) return
  startRecheckLoading.value = true
  try {
    const payload: PurchaseInQualityStartRecheckReqVO = {
      id: qualityOrder.value.id,
      recheckReason: recheckReason.value.trim()
    }
    await PurchaseInQualityApi.startRecheck(payload)
    message.success('发起复检成功')
    await loadDetail(qualityOrder.value.id)
  } finally {
    startRecheckLoading.value = false
  }
}

const handleSubmitFirstCheck = async () => {
  if (!qualityOrder.value?.id || !validateFirstCheck(true)) return
  submitFirstCheckLoading.value = true
  try {
    const payload: PurchaseInQualitySubmitFirstCheckReqVO = {
      id: qualityOrder.value.id,
      remark: qualityOrder.value.remark,
      items: firstCheckRows.value.map((row) => ({
        qualityItemId: Number(row.id),
        sampleCount: roundCount(toNumber(row.formSampleCount)),
        roundPassCount: roundCount(toNumber(row.roundPassCount)),
        roundRejectCount: roundCount(toNumber(row.roundRejectCount)),
        roundRemark: row.roundRemark,
        defects: row.defects.map((item) => ({
          defectReasonId: Number(item.defectReasonId),
          defectReasonName: item.defectReasonName,
          defectCount: roundCount(toNumber(item.defectCount)),
          defectRemark: item.defectRemark
        }))
      }))
    }
    await PurchaseInQualityApi.submitFirstCheck(payload)
    message.success('提交初检成功')
    await loadDetail(qualityOrder.value.id)
  } finally {
    submitFirstCheckLoading.value = false
  }
}

const handleSubmitRecheck = async () => {
  if (!qualityOrder.value?.id || !validateRecheck(true)) return
  submitRecheckLoading.value = true
  try {
    const payload: PurchaseInQualitySubmitRecheckReqVO = {
      id: qualityOrder.value.id,
      remark: qualityOrder.value.remark,
      items: recheckRows.value.map((row) => ({
        qualityItemId: Number(row.id),
        sampleCount: roundCount(toNumber(row.formSampleCount)),
        roundPassCount: roundCount(toNumber(row.roundPassCount)),
        roundRejectCount: roundCount(toNumber(row.roundRejectCount)),
        finalPassCount: roundCount(toNumber(row.finalPassCount)),
        finalRejectCount: roundCount(toNumber(row.finalRejectCount)),
        roundRemark: row.roundRemark,
        defects: row.defects.map((item) => ({
          defectReasonId: Number(item.defectReasonId),
          defectReasonName: item.defectReasonName,
          defectCount: roundCount(toNumber(item.defectCount)),
          defectRemark: item.defectRemark
        }))
      }))
    }
    await PurchaseInQualityApi.submitRecheck(payload)
    message.success('提交复检成功')
    await loadDetail(qualityOrder.value.id)
  } finally {
    submitRecheckLoading.value = false
  }
}

const goBack = () => {
  if (routeFrom.value === 'quality-list') {
    router.push({ path: routeScene.value === 'qms' ? '/qms/iqc' : '/erp/purchase/in-quality' })
    return
  }
  router.push({ path: '/erp/purchase/in' })
}

const goToStockExecute = () => {
  if (!qualityOrder.value?.purchaseInId) {
    return
  }
  router.push({
    path: '/erp/purchase/in',
    query: {
      orderNo: qualityOrder.value.orderNo || undefined,
      openId: String(qualityOrder.value.purchaseInId),
      openType: 'detail',
      openAction: 'stock-execute',
      from: 'quality-detail'
    }
  })
}

onMounted(() => {
  loadDefectReasonOptions()
  loadPage()
})
</script>

<style scoped lang="scss">
.detail-dialog__body {
  max-height: min(60vh, 640px);
  overflow: auto;
  padding-right: 4px;
}
</style>
