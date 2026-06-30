<template>
  <div class="voucher-template-page min-h-full bg-slate-50 px-4 py-4 md:px-6">
    <div class="voucher-template-page__shell">
      <div class="voucher-template-page__header">
        <div>
          <div class="voucher-template-page__breadcrumb">
            <span>璐㈠姟鏍哥畻绠＄悊</span>
            <span>/</span>
            <span>涓氬姟閰嶇疆</span>
            <span>/</span>
            <span class="text-slate-700">鍑瘉妯℃澘</span>
          </div>
          <h1 class="voucher-template-page__title">鍑瘉妯℃澘閰嶇疆</h1>
          <p class="voucher-template-page__subtitle">閽堝涓嶅悓鐨勪笟鍔＄被鍨嬶紝缁熶竴閰嶇疆鍏跺搴旂殑璐㈠姟璁拌处鍑瘉瑙勫垯涓庡垎褰曟ā鏉裤€?</p>
        </div>

        <div class="voucher-template-page__header-actions">
          <el-button class="voucher-template-page__header-button voucher-template-page__header-button--ghost" :loading="refreshing" :disabled="!canRefresh" @click="handleRefresh">
            <Icon icon="ep:refresh-left" class="mr-1.5" />
            閲嶇疆绛涢€?          </el-button>
          <el-button
            v-hasPermi="['erp:finance-voucher-template:create']"
            class="voucher-template-page__header-button voucher-template-page__header-button--primary"
            :disabled="dialogSubmitting"
            @click="openCreateDialog"
          >
            <Icon icon="ep:plus" class="mr-1.5" />
            鏂板鍑瘉妯℃澘
          </el-button>
        </div>
      </div>

      <div class="voucher-template-page__summary-grid">
        <div v-for="card in summaryCards" :key="card.label" class="voucher-template-page__summary-card">
          <div class="voucher-template-page__summary-main">
            <div class="voucher-template-page__summary-label">{{ card.label }}</div>
            <div class="voucher-template-page__summary-value-row">
              <span class="voucher-template-page__summary-value" :class="card.valueClass">{{ card.value }}</span>
              <span class="voucher-template-page__summary-suffix">{{ card.suffix }}</span>
            </div>
          </div>
          <div class="voucher-template-page__summary-icon" :class="card.iconClass">
            <Icon :icon="card.icon" />
          </div>
        </div>
      </div>

      <div class="voucher-template-page__filter-card">
        <div class="voucher-template-page__filter-head">
          <div class="voucher-template-page__section-title-row">
            <Icon icon="ep:filter" class="voucher-template-page__section-title-icon" />
            <div class="voucher-template-page__section-title">绛涢€夋潯浠?</div>
          </div>
        </div>

        <div class="voucher-template-page__filter-grid">
          <div class="voucher-template-page__field">
            <label class="voucher-template-page__field-label">妯℃澘鍚嶇О</label>
            <el-input v-model="queryParams.name" clearable placeholder="杈撳叆鍚嶇О鍏抽敭璇?.." class="voucher-template-page__field-control" @keyup.enter="handleQuery">
              <template #prefix>
                <Icon icon="ep:search" class="voucher-template-page__search-icon" />
              </template>
            </el-input>
          </div>

          <div class="voucher-template-page__field">
            <label class="voucher-template-page__field-label">璐︾翱</label>
            <el-select v-model="queryParams.ledgerId" clearable filterable placeholder="鍏ㄩ儴璐︾翱" class="voucher-template-page__field-control" :loading="ledgerLoading">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>

          <div class="voucher-template-page__field">
            <label class="voucher-template-page__field-label">涓氬姟绫诲瀷</label>
            <el-select v-model="queryParams.bizType" clearable placeholder="鍏ㄩ儴涓氬姟" class="voucher-template-page__field-control">
              <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>

          <div class="voucher-template-page__field">
            <label class="voucher-template-page__field-label">鍚敤鐘舵€?</label>
            <el-select v-model="queryParams.status" clearable placeholder="鍏ㄩ儴鐘舵€?" class="voucher-template-page__field-control">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>

          <div class="voucher-template-page__field">
            <label class="voucher-template-page__field-label">鐢熸垚鏂瑰紡</label>
            <el-select v-model="queryParams.autoGenerate" clearable placeholder="鍏ㄩ儴鏂瑰紡" class="voucher-template-page__field-control">
              <el-option label="鑷姩鐢熸垚" :value="true" />
              <el-option label="鎵嬪伐閰嶇疆" :value="false" />
            </el-select>
          </div>
        </div>

        <div class="voucher-template-page__filter-actions voucher-template-page__filter-actions--bottom">
          <el-button class="voucher-template-page__secondary-button" :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-1.5" />
            閲嶇疆
          </el-button>
          <el-button class="voucher-template-page__query-button" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-1.5" />
            鏌ヨ
          </el-button>
        </div>
      </div>

      <div class="voucher-template-page__table-card">
        <div class="voucher-template-page__table-head">
          <div>
            <div class="voucher-template-page__table-title-row">
              <div class="voucher-template-page__section-title">妯℃澘鍒楄〃</div>
              <span class="voucher-template-page__table-filter-chip">宸茬瓫閫?{{ list.length }} / {{ total }}</span>
            </div>
            <div class="voucher-template-page__section-subtitle">鍏?{{ total }} 鏉★紝褰撳墠鏁版嵁鐩存帴鏉ヨ嚜鍚庣鍒嗛〉鎺ュ彛銆?</div>
          </div>
          <div class="voucher-template-page__table-head-right">
            <label class="voucher-template-page__remark-switch">
              <el-checkbox v-model="showRemark">鏄剧ず澶囨敞璇存槑</el-checkbox>
            </label>

          </div>
        </div>

        <el-alert v-if="listErrorMessage && !list.length" :title="listErrorMessage" type="error" :closable="false" show-icon class="mb-3" />

        <template v-else>
          <div v-if="listLoading || list.length" class="voucher-template-page__table-wrap">
            <el-table v-loading="listLoading" :data="list" row-key="id" class="voucher-template-page__table" :show-overflow-tooltip="false">
              <el-table-column label="搴忓彿" align="center" width="72">
                <template #default="{ $index }">
                  <span class="voucher-template-page__table-index">{{ (queryParams.pageNo - 1) * queryParams.pageSize + $index + 1 }}</span>
                </template>
              </el-table-column>

              <el-table-column min-width="280">
                <template #header>
                  <span class="voucher-template-page__column-header">
                    <Icon icon="ep:document" class="voucher-template-page__column-icon" />
                    妯℃澘鍚嶇О & 璇存槑
                  </span>
                </template>
                <template #default="{ row }">
                  <div class="voucher-template-page__primary-cell">
                    <div class="voucher-template-page__primary-title-row">
                      <span class="voucher-template-page__primary-title">{{ row.name || '-' }}</span>
                    </div>
                    <div v-if="showRemark" class="voucher-template-page__primary-desc">
                      {{ row.defaultSummary || row.remark || '鏈厤缃鏄?' }}
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column min-width="230">
                <template #header>
                  <span class="voucher-template-page__column-header voucher-template-page__column-header--pipeline">
                    <Icon icon="ep:office-building" class="voucher-template-page__column-icon" />
                    閫傜敤璐︾翱鍙婁笟鍔?                  </span>
                </template>
                <template #default="{ row }">
                  <div class="voucher-template-page__secondary-cell">
                    <div class="voucher-template-page__secondary-title">{{ row.ledgerName || '-' }}</div>
                    <div class="voucher-template-page__secondary-desc">{{ row.bizTypeName || getBizTypeLabel(row.bizType) }}</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="鐘舵€?" align="center" width="110">
                <template #default="{ row }">
                  <span class="voucher-template-page__status-badge" :class="resolveStatusClass(row.status)">
                    {{ getStatusLabel(row.status) }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="鏂瑰紡" align="center" width="100">
                <template #default="{ row }">
                  <span class="voucher-template-page__type-badge" :class="row.autoGenerate ? 'voucher-template-page__type-badge--auto' : 'voucher-template-page__type-badge--manual'">
                    {{ row.autoGenerate ? '鑷姩鐢熸垚' : '鎵嬪伐閰嶇疆' }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="鍒嗗綍" align="center" width="90">
                <template #default="{ row }">
                  <span
                    class="voucher-template-page__count-badge"
                    :class="(row.items?.length || 0) > 0 ? 'voucher-template-page__count-badge--ok' : 'voucher-template-page__count-badge--empty'"
                  >
                    {{ row.items?.length || 0 }}娈?                  </span>
                </template>
              </el-table-column>

              <el-table-column label="鏇存柊鏃堕棿" min-width="170">
                <template #default="{ row }">
                  <span class="voucher-template-page__time-text">{{ formatDateTimeValue(row.updateTime) }}</span>
                </template>
              </el-table-column>

              <el-table-column fixed="right" label="鎿嶄綔" align="center" width="300">
                <template #default="{ row }">
                  <div class="voucher-template-page__row-actions">
                    <el-button v-hasPermi="['erp:finance-voucher-template:query']" link type="primary" :disabled="detailLoading || rowBusy(row.id)" @click="openDetailDrawer(row.id)">
                      鏌ョ湅
                    </el-button>
                    <el-button v-hasPermi="['erp:finance-voucher-template:update']" link type="primary" :disabled="dialogSubmitting || rowBusy(row.id)" @click="openEditDialog(row.id)">
                      缂栬緫
                    </el-button>
                    <el-button v-hasPermi="['erp:finance-voucher-template:create']" link type="primary" :disabled="dialogSubmitting || rowBusy(row.id)" @click="openCopyDialog(row.id)">
                      澶嶅埗
                    </el-button>
                    <el-button v-hasPermi="['erp:finance-voucher-template:delete']" link type="danger" :disabled="deleting || rowBusy(row.id)" @click="handleDelete(row)">
                      鍒犻櫎
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else class="voucher-template-page__empty">
            <div class="voucher-template-page__empty-icon">
              <Icon icon="ep:document" />
            </div>
            <div class="voucher-template-page__empty-title">鏆傛棤鍑瘉妯℃澘鏁版嵁</div>
            <div class="voucher-template-page__empty-text">褰撳墠鏉′欢涓嬫病鏈夊尮閰嶇粨鏋滐紝鍙互灏濊瘯閲嶇疆绛涢€夋潯浠跺悗閲嶆柊鏌ヨ銆?</div>
          </div>
        </template>

        <Pagination v-if="total > 0" v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
      </div>
    </div>

    <el-drawer
      v-model="detailDrawerOpen"
      title="妯℃澘璇︽儏"
      size="760px"
      destroy-on-close
      modal-class="voucher-template-page__drawer-modal"
      @closed="resetDetailDrawer"
    >
      <el-result v-if="detailErrorMessage" icon="error" title="妯℃澘璇︽儏鍔犺浇澶辫触">
        <template #extra>
          <el-button type="primary" @click="retryDetail">閲嶈瘯</el-button>
        </template>
      </el-result>

      <template v-else>
        <div v-if="detailData" class="voucher-template-page__context-card">
          <div class="voucher-template-page__context-main">
            <div class="voucher-template-page__context-title">{{ detailData.name || '-' }}</div>
            <div class="voucher-template-page__context-subtitle">
              {{ detailData.ledgerName || '-' }} / {{ detailData.bizTypeName || getBizTypeLabel(detailData.bizType) }}
            </div>
          </div>
          <div class="voucher-template-page__context-meta">
            <div class="voucher-template-page__context-meta-item">
              <span>鐘舵€?</span>
              <span>{{ getStatusLabel(detailData.status) }}</span>
            </div>
            <div class="voucher-template-page__context-meta-item">
              <span>鑷姩鐢熸垚</span>
              <span>{{ detailData.autoGenerate ? '鏄?' : '鍚?' }}</span>
            </div>
            <div class="voucher-template-page__context-meta-item">
              <span>鐮斿彂妯℃澘</span>
              <span>{{ detailData.researchTemplate ? '鏄?' : '鍚?' }}</span>
            </div>
            <div class="voucher-template-page__context-meta-item">
              <span>鍒涘缓鏃堕棿</span>
              <span>{{ formatDateTimeValue(detailData.createTime) }}</span>
            </div>
          </div>
        </div>

        <div v-if="detailData" class="voucher-template-page__detail-grid">
          <div class="voucher-template-page__detail-card">
            <div class="voucher-template-page__detail-label">榛樿鎽樿</div>
            <div class="voucher-template-page__detail-value">{{ detailData.defaultSummary || '鏈缃?' }}</div>
          </div>
          <div class="voucher-template-page__detail-card">
            <div class="voucher-template-page__detail-label">鐮斿彂鍒嗙被</div>
            <div class="voucher-template-page__detail-value">{{ detailData.researchCategoryName || getResearchCategoryLabel(detailData.researchCategory) || '鏈厤缃?' }}</div>
          </div>
          <div class="voucher-template-page__detail-card">
            <div class="voucher-template-page__detail-label">澶囨敞</div>
            <div class="voucher-template-page__detail-value">{{ detailData.remark || '鏃?' }}</div>
          </div>
        </div>

        <el-table v-loading="detailLoading" :data="detailData?.items || []" class="voucher-template-page__detail-table" :show-overflow-tooltip="false">
          <el-table-column label="搴忓彿" prop="entryNo" align="right" width="80" />
          <el-table-column label="鏂瑰悜" min-width="90">
            <template #default="{ row }">{{ row.entryDirectionName || getDirectionLabel(row.entryDirection) }}</template>
          </el-table-column>
          <el-table-column label="绉戠洰缂栫爜" prop="subjectCode" min-width="140" />
          <el-table-column label="绉戠洰鍚嶇О" prop="subjectName" min-width="180" />
          <el-table-column label="閲戦鏉ユ簮" min-width="150">
            <template #default="{ row }">{{ row.amountSourceName || getAmountSourceLabel(row.amountSource) }}</template>
          </el-table-column>
          <el-table-column label="鏉ユ簮鍊?" align="right" min-width="120">
            <template #default="{ row }">{{ formatAmountSourceValue(row.amountSourceValue) }}</template>
          </el-table-column>
          <el-table-column label="鎽樿" prop="summary" min-width="180" />
        </el-table>
      </template>
    </el-drawer>

    <Dialog v-model="dialogVisible" :title="dialogTitle" width="1180px" scroll maxHeight="80vh" @closed="resetDialog">
      <el-form ref="dialogFormRef" :model="formData" :rules="formRules" label-width="110px" class="voucher-template-page__dialog-form">
        <div class="voucher-template-page__dialog-grid">
          <el-form-item label="璐﹀" prop="ledgerId">
            <el-select v-model="formData.ledgerId" placeholder="璇烽€夋嫨璐﹀" filterable class="!w-full">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="涓氬姟绫诲瀷" prop="bizType">
            <el-select v-model="formData.bizType" placeholder="璇烽€夋嫨涓氬姟绫诲瀷" class="!w-full">
              <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="妯℃澘鍚嶇О" prop="name">
            <el-input v-model="formData.name" placeholder="璇疯緭鍏ユā鏉垮悕绉?" />
          </el-form-item>
          <el-form-item label="鐘舵€?" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="CommonStatusEnum.ENABLE">鍚敤</el-radio>
              <el-radio :label="CommonStatusEnum.DISABLE">绂佺敤</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="鑷姩鐢熸垚" prop="autoGenerate">
            <el-switch v-model="formData.autoGenerate" />
          </el-form-item>
          <el-form-item label="鐮斿彂妯℃澘" prop="researchTemplate">
            <el-switch v-model="formData.researchTemplate" />
          </el-form-item>
          <el-form-item label="榛樿鎽樿" prop="defaultSummary" class="voucher-template-page__dialog-span">
            <el-input v-model="formData.defaultSummary" placeholder="璇疯緭鍏ラ粯璁ゆ憳瑕?" />
          </el-form-item>
          <el-form-item label="鐮斿彂鍒嗙被" prop="researchCategory">
            <el-select v-model="formData.researchCategory" placeholder="璇烽€夋嫨鐮斿彂鍒嗙被" clearable class="!w-full">
              <el-option v-for="item in researchCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="澶囨敞" prop="remark" class="voucher-template-page__dialog-span">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="璇疯緭鍏ュ娉?" />
          </el-form-item>
        </div>

        <div class="voucher-template-page__dialog-head">
          <div class="voucher-template-page__section-title">妯℃澘鍒嗗綍</div>
          <el-button size="small" type="primary" plain @click="addItem">
            <Icon icon="ep:plus" class="mr-1.5" />
            鏂板鍒嗗綍
          </el-button>
        </div>

        <div class="voucher-template-page__dialog-table-wrap">
          <el-table :data="formData.items" class="voucher-template-page__dialog-table" :show-overflow-tooltip="false">
            <el-table-column label="搴忓彿" width="90" align="right">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="鏂瑰悜" min-width="120">
              <template #default="{ row }">
                <el-select v-model="row.entryDirection" placeholder="璇烽€夋嫨鏂瑰悜" class="!w-full">
                  <el-option v-for="item in directionOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="绉戠洰缂栫爜" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.subjectCode" placeholder="璇疯緭鍏ョ鐩紪鐮?" />
              </template>
            </el-table-column>
            <el-table-column label="绉戠洰鍚嶇О" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.subjectName" placeholder="璇疯緭鍏ョ鐩悕绉?" />
              </template>
            </el-table-column>
            <el-table-column label="閲戦鏉ユ簮" min-width="160">
              <template #default="{ row }">
                <el-select v-model="row.amountSource" placeholder="璇烽€夋嫨閲戦鏉ユ簮" class="!w-full">
                  <el-option v-for="item in amountSourceOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="鏉ユ簮鍊?" min-width="140" align="right">
              <template #default="{ row }">
                <el-input-number v-if="isAmountSourceValueRequired(row.amountSource)" v-model="row.amountSourceValue" :precision="6" :step="0.01" class="!w-full" />
                <span v-else class="voucher-template-page__muted-text">鏃犻渶濉啓</span>
              </template>
            </el-table-column>
            <el-table-column label="鎽樿" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.summary" placeholder="璇疯緭鍏ュ垎褰曟憳瑕?" />
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" width="90" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeItem($index)">鍒犻櫎</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>

      <template #footer>
        <el-button :disabled="dialogSubmitting" @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" :loading="dialogSubmitting" @click="submitDialog">淇濆瓨</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormRules } from 'element-plus'
import { CommonStatusEnum, ErpBizType } from '@/utils/constants'
import { formatDateTimeValue } from '@/views/erp/finance/shared/accounting'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import {
  ErpFinanceVoucherTemplateItemVO,
  ErpFinanceVoucherTemplatePageReqVO,
  ErpFinanceVoucherTemplateSaveReqVO,
  ErpFinanceVoucherTemplateVO,
  FinanceVoucherTemplateApi
} from '@/api/erp/finance/voucher-template'

defineOptions({ name: 'ErpFinanceVoucherTemplate' })

type DialogMode = 'create' | 'edit' | 'copy'

const message = useMessage()
const dialogFormRef = ref()

const ledgerLoading = ref(false)
const listLoading = ref(false)
const refreshing = ref(false)
const detailLoading = ref(false)
const dialogSubmitting = ref(false)
const deleting = ref(false)
const detailDrawerOpen = ref(false)
const dialogVisible = ref(false)
const listErrorMessage = ref('')
const detailErrorMessage = ref('')
const detailId = ref<number>()
const rowActionLoadingId = ref<number>()
const dialogMode = ref<DialogMode>('create')
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const list = ref<ErpFinanceVoucherTemplateVO[]>([])
const detailData = ref<ErpFinanceVoucherTemplateVO>()
const total = ref(0)
const showRemark = ref(true)

type VoucherTemplatePageQueryReqVO = ErpFinanceVoucherTemplatePageReqVO

const createQueryParams = (): VoucherTemplatePageQueryReqVO => ({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined,
  bizType: undefined,
  name: undefined,
  status: undefined,
  autoGenerate: undefined
})

const queryParams = reactive<VoucherTemplatePageQueryReqVO>(createQueryParams())

const bizTypeOptions = [
  { label: '閲囪喘璁㈠崟', value: ErpBizType.PURCHASE_ORDER },
  { label: '閲囪喘鍏ュ簱', value: ErpBizType.PURCHASE_IN },
  { label: '閲囪喘閫€璐?, value: ErpBizType.PURCHASE_RETURN },
  { label: '閿€鍞鍗?, value: ErpBizType.SALE_ORDER },
  { label: '閿€鍞嚭搴?, value: ErpBizType.SALE_OUT },
  { label: '閿€鍞€€璐?, value: ErpBizType.SALE_RETURN },
  { label: '濮斿鍔犲伐璐?, value: ErpBizType.OUTSOURCE_FEE },
  { label: '濮斿鍏ュ簱', value: ErpBizType.OUTSOURCE_INBOUND },
  { label: '鑷埗鍏ュ簱', value: ErpBizType.PRODUCTION_INBOUND },
  { label: '璐圭敤鎶ラ攢', value: ErpBizType.FINANCE_EXPENSE }
] as const

const statusOptions = [
  { label: '鍚敤', value: CommonStatusEnum.ENABLE },
  { label: '绂佺敤', value: CommonStatusEnum.DISABLE }
]

const researchCategoryOptions = [
  { label: '璐圭敤鍖?, value: 10 },
  { label: '璧勬湰鍖?, value: 20 }
]

const directionOptions = [
  { label: '鍊熸柟', value: 10 },
  { label: '璐锋柟', value: 20 }
]

const amountSourceOptions = [
  { label: '涓氬姟閲戦', value: 10 },
  { label: '鍥哄畾閲戦', value: 20 },
  { label: '涓氬姟閲戦姣斾緥', value: 30 },
  { label: '鐮斿彂鏀嚭鍒嗙被', value: 40 },
  { label: '鐮斿彂椤圭洰姹囨€?, value: 50 }
]

const buildEmptyItem = (): ErpFinanceVoucherTemplateItemVO => ({
  id: undefined,
  entryNo: undefined,
  entryDirection: 10,
  subjectCode: '',
  subjectName: '',
  amountSource: 10,
  amountSourceValue: undefined,
  summary: ''
})

const buildEmptyForm = (): ErpFinanceVoucherTemplateSaveReqVO => ({
  id: undefined,
  ledgerId: undefined as unknown as number,
  bizType: undefined as unknown as number,
  name: '',
  status: CommonStatusEnum.ENABLE,
  autoGenerate: false,
  defaultSummary: '',
  remark: '',
  researchCategory: undefined,
  researchTemplate: false,
  items: [buildEmptyItem()]
})

const formData = reactive<ErpFinanceVoucherTemplateSaveReqVO>(buildEmptyForm())

const formRules: FormRules = {
  ledgerId: [{ required: true, message: '璐﹀涓嶈兘涓虹┖', trigger: 'change' }],
  bizType: [{ required: true, message: '涓氬姟绫诲瀷涓嶈兘涓虹┖', trigger: 'change' }],
  name: [{ required: true, message: '妯℃澘鍚嶇О涓嶈兘涓虹┖', trigger: 'blur' }],
  status: [{ required: true, message: '鐘舵€佷笉鑳戒负绌?, trigger: 'change' }]
}

const dialogTitle = computed(() => {
  if (dialogMode.value === 'create') return '鏂板鍑瘉妯℃澘'
  if (dialogMode.value === 'copy') return '澶嶅埗鍑瘉妯℃澘'
  return '缂栬緫鍑瘉妯℃澘'
})

const enabledCount = computed(() => list.value.filter((item) => item.status === CommonStatusEnum.ENABLE).length)
const autoGenerateCount = computed(() => list.value.filter((item) => item.autoGenerate).length)
const zeroSegmentCount = computed(() => list.value.filter((item) => !(item.items?.length || 0)).length)
const enabledPercent = computed(() => (total.value ? Math.round((enabledCount.value / total.value) * 100) : 0))

const summaryCards = computed(() => [
  {
    label: '鍑瘉妯℃澘鎬绘暟',
    value: total.value,
    suffix: '涓凡瀹氫箟',
    icon: 'ep:laptop',
    iconClass: 'voucher-template-page__summary-icon--blue',
    valueClass: 'voucher-template-page__summary-value--dark'
  },
  {
    label: '宸插惎鐢ㄦā鏉?,
    value: enabledCount.value,
    suffix: `鍗?${enabledPercent.value}%`,
    icon: 'ep:success-filled',
    iconClass: 'voucher-template-page__summary-icon--emerald',
    valueClass: 'voucher-template-page__summary-value--emerald'
  },
  {
    label: '鑷姩鐢熸垚妯℃澘',
    value: autoGenerateCount.value,
    suffix: '涓氬姟瑙﹀彂',
    icon: 'ep:lightning',
    iconClass: 'voucher-template-page__summary-icon--amber',
    valueClass: 'voucher-template-page__summary-value--amber'
  },
  {
    label: '鏃犲垎褰曟ā鏉?,
    value: zeroSegmentCount.value,
    suffix: '闇€灏藉揩琛ュ叏',
    icon: 'ep:warning-filled',
    iconClass: 'voucher-template-page__summary-icon--rose',
    valueClass: 'voucher-template-page__summary-value--rose'
  }
])

const canQuery = computed(() => !listLoading.value && !refreshing.value && !dialogSubmitting.value)
const hasActiveFilters = computed(
  () =>
    !!queryParams.ledgerId ||
    !!queryParams.bizType ||
    !!queryParams.name ||
    queryParams.status !== undefined ||
    queryParams.autoGenerate !== undefined
)
const canReset = computed(() => (hasActiveFilters.value || queryParams.pageNo !== 1) && canQuery.value)
const canRefresh = computed(() => !listLoading.value && !refreshing.value && !dialogSubmitting.value)

const rowBusy = (id?: number) => id != null && rowActionLoadingId.value === id
const isAmountSourceValueRequired = (amountSource?: number) => amountSource === 20 || amountSource === 30

const getBizTypeLabel = (value?: number) => bizTypeOptions.find((item) => item.value === value)?.label || '-'
const getStatusLabel = (value?: number) => statusOptions.find((item) => item.value === value)?.label || '-'
const getResearchCategoryLabel = (value?: number) => researchCategoryOptions.find((item) => item.value === value)?.label || ''
const getDirectionLabel = (value?: number) => directionOptions.find((item) => item.value === value)?.label || '-'
const getAmountSourceLabel = (value?: number) => amountSourceOptions.find((item) => item.value === value)?.label || '-'

const resolveStatusClass = (status?: number) => {
  if (status === CommonStatusEnum.ENABLE) return 'voucher-template-page__status-badge--success'
  if (status === CommonStatusEnum.DISABLE) return 'voucher-template-page__status-badge--neutral'
  return 'voucher-template-page__status-badge--warning'
}

const formatAmountSourceValue = (value?: number | string) => {
  if (value === undefined || value === null || value === '') return '-'
  return typeof value === 'number' ? value.toString() : value
}

const normalizeItems = (items?: ErpFinanceVoucherTemplateItemVO[]) =>
  (items?.length ? items : [buildEmptyItem()]).map((item, index) => ({
    id: item.id,
    entryNo: item.entryNo ?? index + 1,
    entryDirection: item.entryDirection ?? 10,
    entryDirectionName: item.entryDirectionName,
    subjectCode: item.subjectCode || '',
    subjectName: item.subjectName || '',
    amountSource: item.amountSource ?? 10,
    amountSourceName: item.amountSourceName,
    amountSourceValue: item.amountSourceValue,
    summary: item.summary || ''
  }))

const applyFormData = (data?: ErpFinanceVoucherTemplateVO, mode: DialogMode = 'edit') => {
  const payload = buildEmptyForm()
  payload.id = mode === 'edit' ? data?.id : undefined
  payload.ledgerId = (data?.ledgerId ?? undefined) as unknown as number
  payload.bizType = (data?.bizType ?? undefined) as unknown as number
  payload.name = mode === 'copy' ? `${data?.name || ''}-鍓湰` : data?.name || ''
  payload.status = mode === 'copy' ? CommonStatusEnum.ENABLE : data?.status ?? CommonStatusEnum.ENABLE
  payload.autoGenerate = data?.autoGenerate ?? false
  payload.defaultSummary = data?.defaultSummary || ''
  payload.remark = data?.remark || ''
  payload.researchCategory = data?.researchCategory
  payload.researchTemplate = data?.researchTemplate ?? false
  payload.items = normalizeItems(data?.items)

  formData.id = payload.id
  formData.ledgerId = payload.ledgerId
  formData.bizType = payload.bizType
  formData.name = payload.name
  formData.status = payload.status
  formData.autoGenerate = payload.autoGenerate
  formData.defaultSummary = payload.defaultSummary
  formData.remark = payload.remark
  formData.researchCategory = payload.researchCategory
  formData.researchTemplate = payload.researchTemplate
  formData.items = payload.items
}

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    ledgerLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceVoucherTemplateApi.getVoucherTemplatePage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch (error: unknown) {
    if (!list.value.length) {
      listErrorMessage.value = error instanceof Error ? error.message : '鍑瘉妯℃澘鍒楄〃鍔犺浇澶辫触'
    }
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  if (!canQuery.value) return
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canReset.value) return
  Object.assign(queryParams, createQueryParams())
  await getList()
}

const handleRefresh = async () => {
  if (!canRefresh.value) return
  refreshing.value = true
  try {
    Object.assign(queryParams, createQueryParams())
    await getList()
  } finally {
    refreshing.value = false
  }
}

const loadTemplateDetail = async (id: number) => {
  return await FinanceVoucherTemplateApi.getVoucherTemplate(id)
}

const openDetailDrawer = async (id?: number) => {
  if (!id || detailLoading.value) return
  rowActionLoadingId.value = id
  detailLoading.value = true
  detailErrorMessage.value = ''
  detailDrawerOpen.value = true
  detailId.value = id
  try {
    detailData.value = await loadTemplateDetail(id)
  } catch (error: unknown) {
    detailData.value = undefined
    detailErrorMessage.value = error instanceof Error ? error.message : '妯℃澘璇︽儏鍔犺浇澶辫触'
  } finally {
    detailLoading.value = false
    rowActionLoadingId.value = undefined
  }
}

const retryDetail = async () => {
  if (!detailId.value) return
  await openDetailDrawer(detailId.value)
}

const resetDetailDrawer = () => {
  detailId.value = undefined
  detailData.value = undefined
  detailErrorMessage.value = ''
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  applyFormData(undefined, 'create')
  dialogVisible.value = true
}

const openEditDialog = async (id?: number) => {
  if (!id || dialogSubmitting.value) return
  rowActionLoadingId.value = id
  try {
    const data = await loadTemplateDetail(id)
    dialogMode.value = 'edit'
    applyFormData(data, 'edit')
    dialogVisible.value = true
  } finally {
    rowActionLoadingId.value = undefined
  }
}

const openCopyDialog = async (id?: number) => {
  if (!id || dialogSubmitting.value) return
  rowActionLoadingId.value = id
  try {
    const data = await loadTemplateDetail(id)
    dialogMode.value = 'copy'
    applyFormData(data, 'copy')
    dialogVisible.value = true
  } finally {
    rowActionLoadingId.value = undefined
  }
}

const resetDialog = () => {
  applyFormData(undefined, 'create')
  dialogFormRef.value?.resetFields()
  dialogFormRef.value?.clearValidate?.()
}

const addItem = () => {
  formData.items.push({
    ...buildEmptyItem(),
    entryNo: formData.items.length + 1
  })
}

const removeItem = (index: number) => {
  if (formData.items.length <= 1) {
    message.warning('妯℃澘鍒嗗綍鑷冲皯淇濈暀涓€鏉?)
    return
  }
  formData.items.splice(index, 1)
  formData.items.forEach((item, itemIndex) => {
    item.entryNo = itemIndex + 1
  })
}

const validateItems = () => {
  if (!formData.items.length) {
    message.warning('妯℃澘鍒嗗綍涓嶈兘涓虹┖')
    return false
  }
  const invalidItem = formData.items.find((item) => {
    if (!item.entryDirection || !item.subjectCode || !item.subjectName || !item.amountSource) {
      return true
    }
    if (isAmountSourceValueRequired(item.amountSource) && (item.amountSourceValue === undefined || item.amountSourceValue === null || item.amountSourceValue === '')) {
      return true
    }
    return false
  })
  if (invalidItem) {
    message.warning('璇疯ˉ鍏ㄦā鏉垮垎褰曠殑鏂瑰悜銆佺鐩拰閲戦鏉ユ簮')
    return false
  }
  return true
}

const submitDialog = async () => {
  if (dialogSubmitting.value) return
  await dialogFormRef.value?.validate()
  if (!validateItems()) return
  dialogSubmitting.value = true
  try {
    const payload: ErpFinanceVoucherTemplateSaveReqVO = {
      ...formData,
      items: formData.items.map((item, index) => ({
        id: item.id,
        entryNo: index + 1,
        entryDirection: item.entryDirection,
        subjectCode: item.subjectCode,
        subjectName: item.subjectName,
        amountSource: item.amountSource,
        amountSourceValue: item.amountSourceValue,
        summary: item.summary
      }))
    }
    if (dialogMode.value === 'edit' && payload.id) {
      await FinanceVoucherTemplateApi.updateVoucherTemplate(payload)
      message.success('鏇存柊鎴愬姛')
    } else {
      await FinanceVoucherTemplateApi.createVoucherTemplate(payload)
      message.success(dialogMode.value === 'copy' ? '澶嶅埗鎴愬姛' : '鍒涘缓鎴愬姛')
    }
    dialogVisible.value = false
    await getList()
    if (detailDrawerOpen.value && detailId.value) {
      await openDetailDrawer(detailId.value)
    }
  } finally {
    dialogSubmitting.value = false
  }
}

const handleDelete = async (row: ErpFinanceVoucherTemplateVO) => {
  if (!row.id || deleting.value) return
  await message.delConfirm(`鏄惁鍒犻櫎妯℃澘鈥?{row.name || '-'}鈥濓紵`)
  deleting.value = true
  rowActionLoadingId.value = row.id
  try {
    await FinanceVoucherTemplateApi.deleteVoucherTemplate(row.id)
    message.success('鍒犻櫎鎴愬姛')
    if (detailId.value === row.id) {
      detailDrawerOpen.value = false
    }
    await getList()
  } finally {
    deleting.value = false
    rowActionLoadingId.value = undefined
  }
}

onMounted(async () => {
  await loadLedgers()
  await getList()
})
</script>

<style scoped>
.voucher-template-page {
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__shell {
  position: relative;
  overflow: hidden;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 18px;
  background: rgb(255 255 255 / 1);
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
}

.voucher-template-page__header,
.voucher-template-page__summary-grid,
.voucher-template-page__filter-card,
.voucher-template-page__table-card {
  position: relative;
  z-index: 1;
}

.voucher-template-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgb(226 232 240 / 0.8);
  background: rgb(255 255 255 / 1);
}

.voucher-template-page__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__title {
  margin-top: 4px;
  font-size: 28px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__subtitle {
  margin-top: 6px;
  font-size: 14px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.voucher-template-page__header-button {
  min-height: 40px;
  border-radius: 10px;
  font-weight: 600;
}

.voucher-template-page__header-button--ghost {
  border-color: rgb(226 232 240 / 1);
  background: rgb(255 255 255 / 1);
  color: rgb(51 65 85 / 1);
}

.voucher-template-page__header-button--primary {
  border-color: #1677ff;
  background: #1677ff;
  color: #fff;
  box-shadow: 0 8px 20px rgb(22 119 255 / 0.22);
}

.voucher-template-page__summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 14px 16px 0;
}

.voucher-template-page__summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 86px;
  padding: 16px 18px;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 16px;
  background: rgb(255 255 255 / 1);
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
}

.voucher-template-page__summary-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.voucher-template-page__summary-label {
  font-size: 14px;
  font-weight: 600;
  color: rgb(107 114 128 / 1);
}

.voucher-template-page__summary-value-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.voucher-template-page__summary-value {
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
}

.voucher-template-page__summary-suffix {
  font-size: 13px;
  color: rgb(107 114 128 / 1);
}

.voucher-template-page__summary-value--dark {
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__summary-value--emerald {
  color: rgb(5 150 105 / 1);
}

.voucher-template-page__summary-value--amber {
  color: rgb(217 119 6 / 1);
}

.voucher-template-page__summary-value--rose {
  color: rgb(225 29 72 / 1);
}

.voucher-template-page__summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  font-size: 26px;
}

.voucher-template-page__summary-icon--blue {
  background: rgb(239 246 255 / 1);
  color: rgb(37 99 235 / 1);
}

.voucher-template-page__summary-icon--emerald {
  background: rgb(236 253 245 / 1);
  color: rgb(16 185 129 / 1);
}

.voucher-template-page__summary-icon--amber {
  background: rgb(255 251 235 / 1);
  color: rgb(245 158 11 / 1);
}

.voucher-template-page__summary-icon--rose {
  background: rgb(255 241 242 / 1);
  color: rgb(244 63 94 / 1);
}

.voucher-template-page__filter-card {
  margin: 14px 16px 0;
  padding: 18px 18px 20px;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 16px;
  background: linear-gradient(135deg, rgb(236 253 253 / 0.92) 0%, rgb(241 248 255 / 0.92) 45%, rgb(255 255 255 / 0.95) 100%);
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
}

.voucher-template-page__filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.voucher-template-page__section-title-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.voucher-template-page__section-title-icon {
  color: rgb(37 99 235 / 1);
  font-size: 16px;
}

.voucher-template-page__section-title {
  font-size: 16px;
  font-weight: 700;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__filter-grid {
  display: grid;
  grid-template-columns: 2fr 1.1fr 1.1fr 1.1fr 1.1fr;
  gap: 14px;
}

.voucher-template-page__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.voucher-template-page__field-label {
  font-size: 12px;
  font-weight: 700;
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__search-icon {
  font-size: 16px;
  color: rgb(148 163 184 / 1);
}

.voucher-template-page__field-control {
  width: 100%;
}

.voucher-template-page__filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.voucher-template-page__filter-actions--bottom {
  margin-top: 16px;
}

.voucher-template-page__secondary-button {
  min-height: 40px;
  border-color: rgb(226 232 240 / 1);
  border-radius: 10px;
  background: rgb(255 255 255 / 0.9);
  color: rgb(51 65 85 / 1);
}

.voucher-template-page__query-button {
  min-height: 40px;
  border-color: #1677ff;
  border-radius: 10px;
  background: #1677ff;
  color: #fff;
  box-shadow: 0 10px 24px rgb(22 119 255 / 0.2);
}

.voucher-template-page__table-card {
  margin: 14px 16px 16px;
  padding: 18px;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 16px;
  background: rgb(255 255 255 / 1);
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
}

.voucher-template-page__table-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.voucher-template-page__table-head-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.voucher-template-page__table-note {
  font-size: 12px;
  color: rgb(148 163 184 / 1);
}

.voucher-template-page__section-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__remark-switch {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 12px;
  background: rgb(248 250 252 / 1);
}

.voucher-template-page__table-wrap {
  overflow-x: auto;
}

.voucher-template-page__table {
  width: 100%;
  border-radius: 14px;
}

.voucher-template-page__table-index {
  font-size: 14px;
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__column-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__column-header--pipeline {
  color: rgb(37 99 235 / 1);
}

.voucher-template-page__column-icon {
  font-size: 14px;
}

.voucher-template-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.voucher-template-page__primary-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.voucher-template-page__primary-title {
  font-weight: 700;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__primary-desc {
  font-size: 13px;
  line-height: 1.5;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__secondary-desc {
  font-size: 12px;
  color: rgb(148 163 184 / 1);
}

.voucher-template-page__secondary-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.voucher-template-page__secondary-title {
  font-weight: 700;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__status-badge,
.voucher-template-page__type-badge,
.voucher-template-page__count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 700;
}

.voucher-template-page__status-badge--success {
  border-color: rgb(167 243 208 / 1);
  background: rgb(236 253 245 / 1);
  color: rgb(5 150 105 / 1);
}

.voucher-template-page__status-badge--neutral {
  border-color: rgb(226 232 240 / 1);
  background: rgb(241 245 249 / 1);
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__status-badge--warning {
  border-color: rgb(254 215 170 / 1);
  background: rgb(255 247 237 / 1);
  color: rgb(217 119 6 / 1);
}

.voucher-template-page__type-badge--auto {
  border-color: rgb(191 219 254 / 1);
  background: rgb(239 246 255 / 1);
  color: rgb(37 99 235 / 1);
}

.voucher-template-page__type-badge--manual {
  border-color: rgb(209 213 219 / 1);
  background: rgb(248 250 252 / 1);
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__count-badge {
  border-color: rgb(252 165 165 / 1);
  background: rgb(255 241 241 / 1);
  color: rgb(190 24 93 / 1);
}

.voucher-template-page__time-text {
  font-size: 13px;
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.voucher-template-page__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  text-align: center;
}

.voucher-template-page__empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgb(241 245 249 / 1);
  color: rgb(148 163 184 / 1);
  font-size: 26px;
}

.voucher-template-page__empty-title {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 600;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__empty-text {
  margin-top: 6px;
  font-size: 12px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__context-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgb(219 234 254 / 1);
  border-radius: 16px;
  background: linear-gradient(135deg, rgb(248 250 252 / 1), rgb(236 253 255 / 1));
  color: rgb(15 23 42 / 1);
  box-shadow: 0 10px 24px rgb(15 23 42 / 0.06);
}

.voucher-template-page__context-main {
  min-width: 0;
}

.voucher-template-page__context-title {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.voucher-template-page__context-subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: rgb(71 85 105 / 1);
}

.voucher-template-page__context-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  min-width: 260px;
}

.voucher-template-page__context-meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__context-meta-item span:last-child {
  color: rgb(15 23 42 / 1);
  font-weight: 600;
}

.voucher-template-page__detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 14px 0;
}

.voucher-template-page__detail-card {
  padding: 14px;
  border: 1px solid rgb(226 232 240 / 1);
  border-radius: 14px;
  background: rgb(248 250 252 / 1);
}

.voucher-template-page__detail-label {
  font-size: 12px;
  color: rgb(100 116 139 / 1);
}

.voucher-template-page__detail-value {
  margin-top: 6px;
  font-size: 13px;
  color: rgb(15 23 42 / 1);
}

.voucher-template-page__dialog-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.voucher-template-page__dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.voucher-template-page__dialog-grid :deep(.el-form-item) {
  margin-bottom: 12px;
}

.voucher-template-page__dialog-span {
  grid-column: 1 / -1;
}

.voucher-template-page__dialog-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.voucher-template-page__dialog-table-wrap {
  overflow-x: auto;
}

.voucher-template-page__dialog-table {
  width: 100%;
}

.voucher-template-page :deep(.el-input__wrapper),
.voucher-template-page :deep(.el-select__wrapper) {
  border-radius: 12px;
  background: rgb(255 255 255 / 0.95);
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
}

.voucher-template-page :deep(.el-input__inner),
.voucher-template-page :deep(.el-select__selected-item) {
  color: rgb(15 23 42 / 1);
}

.voucher-template-page :deep(.el-table) {
  --el-table-border-color: rgb(241 245 249 / 1);
  --el-table-header-bg-color: rgb(248 250 252 / 1);
  --el-table-tr-bg-color: rgb(255 255 255 / 1);
  --el-table-row-hover-bg-color: rgb(248 250 252 / 1);
  border: 1px solid rgb(241 245 249 / 1);
  border-radius: 14px;
  overflow: hidden;
}

.voucher-template-page :deep(.el-table th.el-table__cell) {
  color: rgb(100 116 139 / 1);
  font-weight: 700;
}

.voucher-template-page :deep(.el-table td.el-table__cell) {
  padding-top: 16px;
  padding-bottom: 16px;
}

.voucher-template-page__drawer-modal {
  backdrop-filter: blur(6px);
}

@media (max-width: 1440px) {
  .voucher-template-page__filter-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1280px) {
  .voucher-template-page__summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .voucher-template-page__filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .voucher-template-page__filter-actions {
    margin-left: 0;
    flex-wrap: wrap;
  }

  .voucher-template-page__detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 960px) {
  .voucher-template-page__header {
    flex-direction: column;
  }

  .voucher-template-page__header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .voucher-template-page__table-head,
  .voucher-template-page__table-head-right {
    flex-direction: column;
    align-items: flex-start;
  }

  .voucher-template-page__context-card {
    grid-template-columns: minmax(0, 1fr);
  }

  .voucher-template-page__context-meta {
    min-width: 0;
  }

  .voucher-template-page__dialog-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .voucher-template-page__summary-grid,
  .voucher-template-page__filter-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
