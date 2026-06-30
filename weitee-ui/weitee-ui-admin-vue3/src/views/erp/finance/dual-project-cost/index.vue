<template>
  <div class="finance-shell finance-shell__stack project-dual-cost-page">
    <!-- 椤靛ご -->
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">椤圭洰鍙岃处鎴愭湰</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip">缁撴灉 {{ total }}</span>
            <span class="finance-shell__metric-chip">澶栭儴璐?{{ formatAmount(totalExternal) }}</span>
            <span class="finance-shell__metric-chip">鍐呴儴璐?{{ formatAmount(totalInternal) }}</span>
            <span class="finance-shell__metric-chip">宸紓 {{ formatAmount(totalDiff) }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button plain :loading="loadingList" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            鍒锋柊
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 鎼滅储鍖?-->
    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">绛涢€夋潯浠?/div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="椤圭洰鍚嶇О" prop="projectName">
            <el-input v-model="queryParams.projectName" placeholder="璇疯緭鍏ラ」鐩悕绉? clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="椤圭洰缂栧彿" prop="projectNo">
            <el-input v-model="queryParams.projectNo" placeholder="璇疯緭鍏ラ」鐩紪鍙? clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="鏈熼棿" prop="period">
            <el-date-picker v-model="queryParams.period" type="month" placeholder="閫夋嫨鏈堜唤" value-format="YYYY-MM" class="!w-full" />
          </el-form-item>
          <el-form-item label="鎴愭湰绫诲埆" prop="costType">
            <el-select v-model="queryParams.costType" placeholder="璇烽€夋嫨鎴愭湰绫诲埆" clearable class="!w-full">
              <el-option v-for="item in COST_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            鏌ヨ
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            閲嶇疆
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 琛ㄦ牸鍖?-->
    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">椤圭洰鍙岃处鎴愭湰鍒楄〃</div>
          <div class="finance-shell__toolbar-count">褰撳墠鍏?<strong>{{ total }}</strong> 鏉?/div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button type="primary" :loading="rebuilding" @click="handleRebuildAll">
            <Icon icon="ep:refresh" class="mr-5px" />
            椤圭洰绾ч噸璺?          </el-button>
          <el-button plain :disabled="!total" @click="handleExportExternal">
            <Icon icon="ep:download" class="mr-5px" />
            瀵煎嚭澶栭儴璐?          </el-button>
          <el-button plain :disabled="!total" @click="handleExportInternal">
            <Icon icon="ep:download" class="mr-5px" />
            瀵煎嚭鍐呴儴璐?          </el-button>
        </div>
      </div>

      <el-alert v-if="listError" class="mb-12px" :closable="false" show-icon type="error" :title="listError">
        <template #default>
          <el-button link type="primary" :disabled="loadingList" @click="getList">閲嶆柊鍔犺浇</el-button>
        </template>
      </el-alert>

      <template v-if="loadingList || list.length">
        <div class="finance-shell__table-wrap">
          <el-table v-loading="loadingList" :data="list" row-key="id" stripe class="finance-shell__table finance-shell__table--dense">
            <el-table-column min-width="200">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:office-building" class="finance-shell__column-icon" />
                  椤圭洰淇℃伅
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.projectName || '-' }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">{{ row.projectNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="鏈熼棿" min-width="100" align="center">
              <template #default="{ row }">
                <span class="finance-shell__mono">{{ row.period || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="鎴愭湰绫诲埆" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ row.costTypeName || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column min-width="160" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  澶栭儴璐﹂噾棰?                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.externalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column min-width="160" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  鍐呴儴璐﹂噾棰?                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.internalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column min-width="140" align="right">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:trend-charts" class="finance-shell__column-icon" />
                  宸紓閲戦
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': Number(row.diffAmount || 0) !== 0 }">
                  {{ formatAmount(row.diffAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="鏉ユ簮鏁? min-width="80" align="center">
              <template #default="{ row }">
                <span class="finance-shell__mono">{{ row.sourceCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="鎿嶄綔" fixed="right" align="center" width="120">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button link type="primary" @click.stop="handleViewDetail(row)">
                    鏌ョ湅鏄庣粏
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <Pagination v-if="total > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </template>
      <div v-else class="project-dual-cost-page__empty">
        <el-empty description="鏆傛棤椤圭洰鍙岃处鎴愭湰鏁版嵁">
          <template #image>
            <div class="finance-shell__empty-icon">
              <Icon icon="ep:files" />
            </div>
          </template>
        </el-empty>
      </div>
    </ContentWrap>

    <!-- 鏄庣粏 Drawer -->
    <el-drawer
      v-model="detailDrawerVisible"
      size="520px"
      destroy-on-close
      :with-header="false"
      :modal-class="'finance-shell__drawer-mask'"
      @closed="clearDetailDrawer"
    >
      <div class="project-dual-cost-page__drawer">
        <div class="finance-shell__context-card">
          <div class="finance-shell__context-main">
            <div class="finance-shell__context-title">{{ detailData?.projectName || '-' }}</div>
            <div class="finance-shell__context-subtitle">{{ detailData?.projectNo || '椤圭洰鍙岃处鎴愭湰鏄庣粏' }}</div>
          </div>
          <div class="finance-shell__context-meta">
            <span class="finance-shell__page-chip">{{ detailData?.period || '-' }}</span>
            <span class="finance-shell__page-chip">{{ detailData?.costTypeName || '-' }}</span>
          </div>
        </div>

        <div v-if="loadingDetail" class="project-dual-cost-page__drawer-loading">
          <el-skeleton :rows="6" animated />
        </div>
        <template v-else>
          <div class="project-dual-cost-page__drawer-body">
            <div class="finance-shell__metric-grid project-dual-cost-page__drawer-metrics">
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">澶栭儴閲戦</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.externalAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">鍐呴儴閲戦</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.internalAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">宸紓閲戦</div>
                <div class="finance-shell__metric-value">{{ formatAmount(detailData?.diffAmount) }}</div>
              </div>
              <div class="finance-shell__metric-card">
                <div class="finance-shell__metric-label">鏉ユ簮鍗曟嵁鏁?/div>
                <div class="finance-shell__metric-value">{{ detailData?.sourceCount || 0 }}</div>
              </div>
            </div>

            <div class="finance-shell__section-title">鎴愭湰鏄庣粏</div>
            <div v-if="detailItems.length" class="project-dual-cost-page__detail-table">
              <el-table :data="detailItems" stripe class="finance-shell__table finance-shell__table--dense">
                <el-table-column label="鎴愭湰绫诲埆" min-width="100">
                  <template #default="{ row }">
                    <span>{{ row.costTypeName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="澶栭儴閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.externalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="鍐呴儴閲戦" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.internalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="宸紓" min-width="120" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono" :class="{ 'text-red-500': Number(row.diffAmount || 0) !== 0 }">
                      {{ formatAmount(row.diffAmount) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="project-dual-cost-page__empty project-dual-cost-page__empty--drawer">
              <el-empty description="鏆傛棤鏄庣粏鏁版嵁" :image-size="60" />
            </div>
          </div>
        </template>

        <div class="project-dual-cost-page__drawer-footer">
          <el-button @click="detailDrawerVisible = false">鍏抽棴</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 閲嶈窇瀵硅瘽妗?-->
    <el-dialog v-model="rebuildDialogVisible" title="椤圭洰绾ч噸璺? width="480px" destroy-on-close>
      <el-form ref="rebuildFormRef" :model="rebuildForm" label-width="88px">
        <el-form-item label="椤圭洰" required>
          <el-input :model-value="rebuildForm.projectName" disabled />
        </el-form-item>
        <el-form-item label="鏈熼棿" required>
          <el-date-picker v-model="rebuildForm.period" type="month" placeholder="閫夋嫨鏈堜唤" value-format="YYYY-MM" class="!w-full" />
        </el-form-item>
        <el-form-item label="澶囨敞">
          <el-input v-model="rebuildForm.remark" type="textarea" placeholder="璇疯緭鍏ュ娉? />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="rebuilding" @click="rebuildDialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" :loading="rebuilding" @click="confirmRebuild">纭閲嶈窇</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import download from '@/utils/download'
import {
  DualProjectCostApi,
  type DualProjectCostVO,
  type DualProjectCostPageReqVO,
  COST_TYPE_OPTIONS
} from '@/api/erp/finance/project-dual-cost'

defineOptions({ name: 'ErpFinanceDualProjectCost' })

const message = useMessage()

const loadingList = ref(false)
const list = ref<DualProjectCostVO[]>([])
const total = ref(0)
const listError = ref('')
const rebuilding = ref(false)

const queryParams = reactive<DualProjectCostPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  projectId: undefined,
  projectNo: undefined,
  projectName: undefined,
  period: undefined,
  costType: undefined
})

const queryFormRef = ref()

// KPI 璁＄畻
const totalExternal = computed(() => list.value.reduce((sum, item) => sum + Number(item.externalAmount || 0), 0))
const totalInternal = computed(() => list.value.reduce((sum, item) => sum + Number(item.internalAmount || 0), 0))
const totalDiff = computed(() => list.value.reduce((sum, item) => sum + Number(item.diffAmount || 0), 0))

// 鏄庣粏 Drawer
const detailDrawerVisible = ref(false)
const loadingDetail = ref(false)
const detailData = ref<DualProjectCostVO>()
const detailItems = ref<DualProjectCostVO[]>([])

// 閲嶈窇瀵硅瘽妗?const rebuildDialogVisible = ref(false)
const rebuildFormRef = ref()
const rebuildForm = reactive({
  projectId: undefined as number | undefined,
  projectName: '',
  period: '',
  remark: ''
})

const formatAmount = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const getList = async () => {
  loadingList.value = true
  listError.value = ''
  try {
    const data = await DualProjectCostApi.getProjectDualCostPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
    listError.value = '椤圭洰鍙岃处鎴愭湰鏁版嵁鍔犺浇澶辫触'
  } finally {
    loadingList.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  getList()
}

const handleRefresh = () => getList()

const handleViewDetail = async (row: DualProjectCostVO) => {
  detailData.value = row
  detailDrawerVisible.value = true
  loadingDetail.value = true
  try {
    detailItems.value = await DualProjectCostApi.getProjectDualCostItems(row.id!)
  } catch {
    detailItems.value = []
  } finally {
    loadingDetail.value = false
  }
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailItems.value = []
}

const handleRebuildAll = () => {
  if (!list.value.length) {
    message.warning('鏆傛棤鏁版嵁鍙噸璺?)
    return
  }
  const first = list.value[0]
  rebuildForm.projectId = first.projectId
  rebuildForm.projectName = first.projectName || ''
  rebuildForm.period = first.period || ''
  rebuildForm.remark = ''
  rebuildDialogVisible.value = true
}

const confirmRebuild = async () => {
  if (!rebuildForm.projectId || !rebuildForm.period) {
    message.warning('璇烽€夋嫨椤圭洰鍜屾湡闂?)
    return
  }
  rebuilding.value = true
  try {
    await DualProjectCostApi.rebuildProjectDualCost({
      projectId: rebuildForm.projectId,
      period: rebuildForm.period,
      remark: rebuildForm.remark
    })
    message.success('閲嶈窇鎴愬姛')
    rebuildDialogVisible.value = false
    await getList()
  } catch {
    // 閲嶈窇澶辫触
  } finally {
    rebuilding.value = false
  }
}

const handleExportExternal = async () => {
  try {
    const data = await DualProjectCostApi.exportExternalProjectCost(queryParams)
    download.excel(data, '椤圭洰澶栭儴璐︽垚鏈?xlsx')
  } catch {
    // 瀵煎嚭澶辫触
  }
}

const handleExportInternal = async () => {
  try {
    const data = await DualProjectCostApi.exportInternalProjectCost(queryParams)
    download.excel(data, '椤圭洰鍐呴儴璐︽垚鏈?xlsx')
  } catch {
    // 瀵煎嚭澶辫触
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
@import '../shared/readOnlyPage.css';

.project-dual-cost-page {
  min-height: 100%;
}

.project-dual-cost-page :deep(.finance-shell__header-card .el-card__body),
.project-dual-cost-page :deep(.finance-shell__filter-card .el-card__body),
.project-dual-cost-page :deep(.finance-shell__table-card .el-card__body) {
  padding: 14px 16px;
}

.project-dual-cost-page__empty {
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  padding: 8px;
}

.project-dual-cost-page :deep(.el-drawer__body) {
  padding: 0;
}

.project-dual-cost-page__drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f8fafc;
}

.project-dual-cost-page__drawer-loading,
.project-dual-cost-page__drawer-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
}

.project-dual-cost-page__drawer-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.project-dual-cost-page__drawer-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
}
</style>
