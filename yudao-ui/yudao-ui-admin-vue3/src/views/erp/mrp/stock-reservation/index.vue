<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="占用概览" name="summary" />
      <el-tab-pane label="占用明细" name="detail" />
    </el-tabs>
  </ContentWrap>

  <template v-if="activeTab === 'summary'">
    <ContentWrap>
      <el-form
        ref="summaryQueryFormRef"
        :model="summaryQueryParams"
        :inline="true"
        label-width="68px"
        class="-mb-15px summary-query-form"
      >
        <el-form-item label="物料" prop="productId">
          <el-select
            v-model="summaryQueryParams.productId"
            clearable
            filterable
            :loading="loadingOptions"
            class="query-control"
            placeholder="请选择物料"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :disabled="!canSearchSummary" @click="handleSummaryQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="!canResetSummary" @click="resetSummaryQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-alert
        v-if="summaryErrorMessage"
        :title="summaryErrorMessage"
        type="error"
        :closable="false"
        show-icon
        class="mb-12px"
      />

      <el-table v-loading="loadingSummary" :data="summaryList" stripe show-overflow-tooltip>
        <el-table-column label="物料" prop="productName" min-width="180" />
        <el-table-column label="物料总库存" prop="stockQty" min-width="120" align="center">
          <template #default="{ row }">
            {{ formatQty(row.stockQty) }}
          </template>
        </el-table-column>
        <el-table-column label="活跃占用量" prop="activeReservedQty" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ formatQty(row.activeReservedQty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="占用项目分布" min-width="240">
          <template #default="{ row }">
            <div v-if="row.projectDistributionItems?.length" class="project-distribution-cell">
              <el-tag
                v-for="item in row.projectDistributionItems"
                :key="item"
                effect="plain"
                class="project-distribution-tag"
              >
                {{ item }}
              </el-tag>
              <el-tag v-if="row.projectDistributionMoreCount" type="info" effect="plain">
                +{{ row.projectDistributionMoreCount }}
              </el-tag>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="预计可用量" prop="availableQty" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAvailableTagType(row.availableQty)">
              {{ formatQty(row.availableQty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃项目数" prop="activeProjectCount" min-width="120" align="center" />
        <el-table-column
          label="活跃占用记录数"
          prop="activeReservationCount"
          min-width="140"
          align="center"
        />
        <el-table-column
          label="最近占用时间"
          prop="lastReservedTime"
          min-width="180"
          align="center"
          :formatter="dateTimeFormatter"
        />
        <el-table-column label="操作" fixed="right" min-width="180" align="center">
          <template #default="{ row }">
            <div class="operation-cell">
              <el-button
                link
                type="primary"
                :disabled="!canOpenProjectDrawer || !row.productId"
                @click="openProjectDrawer(row)"
              >
                查看项目归属
              </el-button>
              <el-button
                link
                type="primary"
                :disabled="loadingDetail || !row.productId"
                @click="openDetailFromSummary(row)"
              >
                查看明细
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="summaryQueryParams.pageNo"
        v-model:limit="summaryQueryParams.pageSize"
        :total="summaryTotal"
        @pagination="getSummaryList"
      />
    </ContentWrap>
  </template>

  <template v-else>
    <ContentWrap>
      <el-form
        ref="detailQueryFormRef"
        :model="detailQueryParams"
        :inline="true"
        label-width="90px"
        class="-mb-15px detail-query-form"
      >
        <el-form-item label="计划编号" prop="planId">
          <el-input-number
            v-model="detailQueryParams.planId"
            :min="1"
            :precision="0"
            class="query-control query-control--number"
            placeholder="请输入计划编号"
          />
        </el-form-item>
        <el-form-item label="项目" prop="projectId">
          <el-select
            v-model="detailQueryParams.projectId"
            clearable
            filterable
            :loading="loadingOptions"
            class="query-control"
            placeholder="请选择项目"
          >
            <el-option
              v-for="item in projectOptions"
              :key="item.id"
              :label="`${item.no} - ${item.name}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物料" prop="productId">
          <el-select
            v-model="detailQueryParams.productId"
            clearable
            filterable
            :loading="loadingOptions"
            class="query-control"
            placeholder="请选择物料"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源销售单" prop="sourceOrderId">
          <el-input-number
            v-model="detailQueryParams.sourceOrderId"
            :min="1"
            :precision="0"
            :controls="false"
            class="query-control query-control--number"
            placeholder="请输入来源销售单编号"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="detailQueryParams.status"
            clearable
            class="query-control query-control--status"
            placeholder="请选择状态"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :disabled="!canSearchDetail" @click="handleDetailQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="!canResetDetail" @click="resetDetailQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-alert
        v-if="detailErrorMessage"
        :title="detailErrorMessage"
        type="error"
        :closable="false"
        show-icon
        class="mb-12px"
      />

      <el-table v-loading="loadingDetail" :data="detailList" stripe show-overflow-tooltip>
        <el-table-column label="占用编号" prop="id" width="110" align="center" />
        <el-table-column label="计划编号" prop="planNo" min-width="150" />
        <el-table-column label="项目" prop="projectName" min-width="180" />
        <el-table-column label="物料" prop="productName" min-width="180" />
        <el-table-column label="来源销售单" prop="sourceOrderNo" min-width="150" align="center" />
        <el-table-column label="占用数量" prop="reservedQty" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ formatQty(row.reservedQty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          prop="createTime"
          width="180"
          align="center"
          :formatter="dateTimeFormatter"
        />
      </el-table>

      <Pagination
        v-model:page="detailQueryParams.pageNo"
        v-model:limit="detailQueryParams.pageSize"
        :total="detailTotal"
        @pagination="getDetailList"
      />
    </ContentWrap>
  </template>

  <el-drawer
    v-model="projectDrawerVisible"
    title="项目占用归属"
    append-to-body
    size="720px"
    destroy-on-close
    @closed="handleProjectDrawerClosed"
  >
    <el-alert
      v-if="projectBreakdownErrorMessage"
      :title="projectBreakdownErrorMessage"
      type="error"
      :closable="false"
      show-icon
      class="mb-12px"
    />

    <el-empty
      v-else-if="!loadingProjectBreakdown && !projectSummaryList.length"
      description="暂无项目占用数据"
    />

    <el-table
      v-else
      v-loading="loadingProjectBreakdown"
      :data="projectSummaryList"
      stripe
      show-overflow-tooltip
      max-height="calc(100vh - 220px)"
    >
      <el-table-column label="占用项目" min-width="180">
        <template #default="{ row }">
          {{ getProjectLabel(row) }}
        </template>
      </el-table-column>
      <el-table-column label="占用量" prop="activeReservedQty" min-width="110" align="center">
        <template #default="{ row }">
          <el-tag type="warning">{{ formatQty(row.activeReservedQty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="占用记录数"
        prop="activeReservationCount"
        min-width="110"
        align="center"
      />
      <el-table-column
        label="来源销售单数"
        prop="sourceOrderCount"
        min-width="120"
        align="center"
      />
      <el-table-column
        label="最近占用时间"
        prop="lastReservedTime"
        min-width="180"
        align="center"
        :formatter="dateTimeFormatter"
      />
      <el-table-column label="操作" fixed="right" width="100" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="loadingDetail || !row.productId || !row.projectId"
            @click="openDetailFromProjectDrawer(row)"
          >
            查看明细
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import { ProjectApi, type ProjectSimpleVO } from '@/api/erp/project'
import {
  StockReservationApi,
  type StockReservationPageReqVO,
  type StockReservationProjectSummaryVO,
  type StockReservationSummaryPageReqVO,
  type StockReservationSummaryVO,
  type StockReservationVO
} from '@/api/erp/mrp/stock-reservation'

defineOptions({ name: 'ErpMrpStockReservationPage' })

const message = useMessage()

const activeTab = ref<'summary' | 'detail'>('summary')
const loadingOptions = ref(false)
const loadingSummary = ref(false)
const loadingDetail = ref(false)
const loadingProjectBreakdown = ref(false)
const summaryErrorMessage = ref('')
const detailErrorMessage = ref('')
const projectBreakdownErrorMessage = ref('')

const summaryList = ref<StockReservationSummaryVO[]>([])
const summaryTotal = ref(0)
const detailList = ref<StockReservationVO[]>([])
const detailTotal = ref(0)
const projectSummaryList = ref<StockReservationProjectSummaryVO[]>([])
const projectOptions = ref<ProjectSimpleVO[]>([])
const productOptions = ref<ProductVO[]>([])
const projectDrawerVisible = ref(false)

const summaryQueryFormRef = ref()
const detailQueryFormRef = ref()

const summaryQueryParams = reactive<StockReservationSummaryPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  productId: undefined
})

const detailQueryParams = reactive<StockReservationPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  planId: undefined,
  projectId: undefined,
  productId: undefined,
  sourceOrderId: undefined,
  status: 0
})

const statusOptions = [
  { label: '占用中', value: 0 },
  { label: '已释放', value: 1 }
]

const hasSummaryFilters = computed(() => Boolean(summaryQueryParams.productId))
const hasDetailFilters = computed(() => {
  return Boolean(
    detailQueryParams.planId ||
      detailQueryParams.projectId ||
      detailQueryParams.productId ||
      detailQueryParams.sourceOrderId ||
      detailQueryParams.status !== 0
  )
})

const canSearchSummary = computed(() => !loadingSummary.value)
const canResetSummary = computed(() => !loadingSummary.value && hasSummaryFilters.value)
const canSearchDetail = computed(() => !loadingDetail.value)
const canResetDetail = computed(() => !loadingDetail.value && hasDetailFilters.value)
const canOpenProjectDrawer = computed(() => !loadingProjectBreakdown.value)

let projectDrawerRequestId = 0

const formatQty = (value?: number) => Number(value ?? 0).toFixed(2)
const dateTimeFormatter = (_row: unknown, _column: unknown, cellValue: string | number | undefined) => {
  if (cellValue === undefined || cellValue === null || cellValue === '') {
    return ''
  }
  const normalizedValue =
    typeof cellValue === 'string' && /^\d{10,13}$/.test(cellValue.trim())
      ? Number(cellValue.trim().length === 10 ? `${cellValue.trim()}000` : cellValue.trim())
      : cellValue
  return formatDate(normalizedValue) || String(cellValue)
}

const getProjectLabel = (row: StockReservationProjectSummaryVO) => {
  if (row.projectNo && row.projectName) {
    return `${row.projectNo} - ${row.projectName}`
  }
  return row.projectName || '未关联项目'
}

const getStatusLabel = (status?: number) => {
  return status === 1 ? '已释放' : '占用中'
}

const getStatusTagType = (status?: number) => {
  return status === 1 ? 'success' : 'warning'
}

const getAvailableTagType = (value?: number) => {
  const qty = Number(value ?? 0)
  if (qty < 0) {
    return 'danger'
  }
  if (qty === 0) {
    return 'warning'
  }
  return 'success'
}

const resetProjectDrawerState = () => {
  projectSummaryList.value = []
  projectBreakdownErrorMessage.value = ''
  loadingProjectBreakdown.value = false
}

const closeProjectDrawer = () => {
  projectDrawerRequestId += 1
  projectDrawerVisible.value = false
  resetProjectDrawerState()
}

const getSummaryList = async () => {
  loadingSummary.value = true
  summaryErrorMessage.value = ''
  try {
    const data = await StockReservationApi.getStockReservationSummaryPage(summaryQueryParams)
    summaryList.value = data.list
    summaryTotal.value = data.total
  } catch (error: any) {
    summaryErrorMessage.value = error?.message || '占用概览加载失败'
  } finally {
    loadingSummary.value = false
  }
}

const getDetailList = async () => {
  loadingDetail.value = true
  detailErrorMessage.value = ''
  try {
    const data = await StockReservationApi.getStockReservationPage(detailQueryParams)
    detailList.value = data.list
    detailTotal.value = data.total
  } catch (error: any) {
    detailErrorMessage.value = error?.message || '占用明细加载失败'
  } finally {
    loadingDetail.value = false
  }
}

const openProjectDrawer = async (row: StockReservationSummaryVO) => {
  if (!row.productId || loadingProjectBreakdown.value) {
    return
  }
  const currentRequestId = ++projectDrawerRequestId
  projectDrawerVisible.value = true
  projectSummaryList.value = []
  projectBreakdownErrorMessage.value = ''
  loadingProjectBreakdown.value = true
  try {
    const data = await StockReservationApi.getStockReservationProjectSummaryList(row.productId)
    if (currentRequestId !== projectDrawerRequestId) {
      return
    }
    projectSummaryList.value = data
  } catch (error: any) {
    if (currentRequestId !== projectDrawerRequestId) {
      return
    }
    projectBreakdownErrorMessage.value = error?.message || '项目归属加载失败'
  } finally {
    if (currentRequestId === projectDrawerRequestId) {
      loadingProjectBreakdown.value = false
    }
  }
}

const handleSummaryQuery = async () => {
  summaryQueryParams.pageNo = 1
  await getSummaryList()
}

const resetSummaryQuery = async () => {
  summaryQueryFormRef.value?.resetFields()
  await handleSummaryQuery()
}

const handleDetailQuery = async () => {
  detailQueryParams.pageNo = 1
  await getDetailList()
}

const resetDetailQuery = async () => {
  detailQueryFormRef.value?.resetFields()
  detailQueryParams.status = 0
  await handleDetailQuery()
}

const applyDetailFilters = async (productId: number, projectId?: number) => {
  activeTab.value = 'detail'
  detailQueryParams.pageNo = 1
  detailQueryParams.planId = undefined
  detailQueryParams.projectId = projectId
  detailQueryParams.productId = productId
  detailQueryParams.sourceOrderId = undefined
  detailQueryParams.status = 0
  await getDetailList()
}

const openDetailFromSummary = async (row: StockReservationSummaryVO) => {
  if (!row.productId || loadingDetail.value) {
    return
  }
  await applyDetailFilters(row.productId)
}

const openDetailFromProjectDrawer = async (row: StockReservationProjectSummaryVO) => {
  if (!row.productId || !row.projectId || loadingDetail.value) {
    return
  }
  closeProjectDrawer()
  await applyDetailFilters(row.productId, row.projectId)
}

const handleProjectDrawerClosed = () => {
  resetProjectDrawerState()
}

const loadOptions = async () => {
  loadingOptions.value = true
  try {
    const [projects, products] = await Promise.all([
      ProjectApi.getProjectSimpleList(),
      ProductApi.getProductSimpleList()
    ])
    projectOptions.value = projects
    productOptions.value = products
  } catch (error: any) {
    message.error(error?.message || '物料和项目选项加载失败')
  } finally {
    loadingOptions.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await Promise.all([getSummaryList(), getDetailList()])
})
</script>

<style scoped>
.query-control {
  width: 240px;
  max-width: 100%;
}

.query-control--number {
  width: 220px;
}

.query-control--status {
  width: 180px;
}

.project-distribution-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.project-distribution-tag {
  max-width: 100%;
}

.operation-cell {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px 8px;
}

@media (max-width: 1023px) {
  .summary-query-form :deep(.el-form-item),
  .detail-query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .summary-query-form :deep(.el-form-item__content),
  .detail-query-form :deep(.el-form-item__content) {
    width: 100%;
  }

  .query-control,
  .query-control--number,
  .query-control--status {
    width: 100%;
  }
}
</style>
