<template>
  <div class="sales-page-shell">
    <doc-alert title="【合同】合同管理、合同提醒" url="https://doc.iocoder.cn/crm/contract/" />
    <doc-alert title="【通用】数据权限" url="https://doc.iocoder.cn/crm/permission/" />

    <ContentWrap class="sales-page-shell__hero">
      <div class="page-hero page-hero--compact">
        <div class="page-hero__header">
          <h1 class="page-hero__title">客户与合同评审</h1>
        </div>
        <div class="page-hero__stats page-hero__stats--row">
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--blue">
            <span class="hero-stat-card__label">当前结果</span>
            <strong class="hero-stat-card__value">{{ total }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--teal">
            <span class="hero-stat-card__label">合同总额</span>
            <strong class="hero-stat-card__value hero-stat-card__value--mono">
              {{ formatMoney(contractStats.totalPrice) }}
            </strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--green">
            <span class="hero-stat-card__label">已回款</span>
            <strong class="hero-stat-card__value hero-stat-card__value--mono">
              {{ formatMoney(contractStats.receivedPrice) }}
            </strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--amber">
            <span class="hero-stat-card__label">待审批</span>
            <strong class="hero-stat-card__value">{{ contractStats.pendingAuditCount }}</strong>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="sales-page-shell__search-card">
      <div class="search-card__header">
        <div>
          <div class="search-card__title">合同筛选</div>
        </div>
      </div>

      <el-tabs v-model="activeName" class="contract-scene-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="我负责的" name="1" />
        <el-tab-pane label="我参与的" name="2" />
        <el-tab-pane label="下属负责的" name="3" />
      </el-tabs>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
          <el-form-item label="合同编号" prop="no">
            <el-input
              v-model="queryParams.no"
              clearable
              placeholder="请输入合同编号"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="合同名称" prop="name">
            <el-input
              v-model="queryParams.name"
              clearable
              placeholder="请输入合同名称"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="客户" prop="customerId">
            <el-select
              v-model="queryParams.customerId"
              clearable
              filterable
              placeholder="请选择客户"
            >
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.name"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button :loading="loading" type="primary" @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="sales-page-shell__table-card">
      <div class="table-toolbar">
        <div class="table-toolbar__main">
          <div class="table-toolbar__title">合同评审列表</div>
          <div class="table-toolbar__meta">
            当前场景：<strong>{{ currentSceneLabel }}</strong>
            <span>共 {{ total }} 条</span>
          </div>
        </div>
        <div class="table-toolbar__actions">
          <el-button
            v-hasPermi="['crm:contract:create']"
            type="primary"
            @click="openForm('create')"
          >
            <Icon class="mr-5px" icon="ep:plus" />
            新增合同
          </el-button>
          <el-button
            v-hasPermi="['crm:contract:export']"
            :loading="exportLoading"
            plain
            type="success"
            @click="handleExport"
          >
            <Icon class="mr-5px" icon="ep:download" />
            导出
          </el-button>
        </div>
      </div>

      <div class="table-card__body">
        <el-table
          v-loading="loading"
          :data="list"
          stripe
          class="sales-table contract-table"
          :show-overflow-tooltip="false"
        >
          <el-table-column label="合同信息" min-width="260" fixed="left">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title-row">
                  <span class="cell-stack__mono">{{ row.no || '-' }}</span>
                  <span class="soft-pill" :class="auditPillClass(row.auditStatus)">
                    {{ auditStatusLabel(row.auditStatus) }}
                  </span>
                </div>
                <el-link
                  :underline="false"
                  type="primary"
                  class="cell-stack__title"
                  @click="openDetail(row.id)"
                >
                  {{ row.name }}
                </el-link>
                <div class="cell-stack__meta">
                  <span>下单：{{ formatDateText(row.orderDate, 'YYYY-MM-DD') }}</span>
                  <span>开始：{{ formatDateText(row.startTime, 'YYYY-MM-DD') }}</span>
                  <span>结束：{{ formatDateText(row.endTime, 'YYYY-MM-DD') }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="客户与商机" min-width="220">
            <template #default="{ row }">
              <div class="cell-stack">
                <el-link
                  :underline="false"
                  type="primary"
                  class="cell-stack__title"
                  @click="openCustomerDetail(row.customerId)"
                >
                  {{ row.customerName || '-' }}
                </el-link>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">商机</span>
                  <el-link
                    :underline="false"
                    type="primary"
                    @click="openBusinessDetail(row.businessId)"
                  >
                    {{ row.businessName || '未关联商机' }}
                  </el-link>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">签约联系人</span>
                  <el-link
                    :underline="false"
                    type="primary"
                    @click="openContactDetail(row.signContactId)"
                  >
                    {{ row.signContactName || '-' }}
                  </el-link>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="签约与归属" min-width="180">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">公司签约人</span>
                  <span>{{ row.signUserName || '-' }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">负责人</span>
                  <span>{{ row.ownerUserName || '-' }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">所属部门</span>
                  <span>{{ row.ownerUserDeptName || '未配置部门' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="合同金额" min-width="190" align="right">
            <template #default="{ row }">
              <div class="amount-stack">
                <div class="amount-stack__main">{{ formatMoney(row.totalPrice) }}</div>
                <div class="amount-stack__meta"
                  >已回款 {{ formatMoney(row.totalReceivablePrice) }}</div
                >
                <div class="amount-stack__meta amount-stack__meta--warning">
                  未回款 {{ formatMoney(getUnreceivedPrice(row)) }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="跟进与备注" min-width="220">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">最后跟进</span>
                  <span>{{ formatDateText(row.contactLastTime) }}</span>
                </div>
                <div class="cell-stack__meta cell-stack__meta--truncate" :title="row.remark || '-'">
                  {{ row.remark || '暂无备注' }}
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">创建</span>
                  <span>{{ formatDateText(row.createTime) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="240">
            <template #default="{ row }">
              <el-button
                v-if="row.auditStatus === 0"
                v-hasPermi="['crm:contract:update']"
                link
                type="primary"
                @click="openForm('update', row.id)"
              >
                编辑
              </el-button>
              <el-button
                v-if="row.auditStatus === 0"
                v-hasPermi="['crm:contract:update']"
                link
                type="primary"
                @click="handleSubmit(row)"
              >
                提交审核
              </el-button>
              <el-button
                v-else
                v-hasPermi="['crm:contract:update']"
                link
                type="primary"
                @click="handleProcessDetail(row)"
              >
                查看审批
              </el-button>
              <el-button
                v-hasPermi="['crm:contract:query']"
                link
                type="primary"
                @click="openDetail(row.id)"
              >
                详情
              </el-button>
              <el-button
                v-hasPermi="['crm:contract:delete']"
                link
                type="danger"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="暂无合同数据，试试调整筛选条件" />
          </template>
        </el-table>
      </div>

      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <ContractForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import * as ContractApi from '@/api/crm/contract'
import ContractForm from './ContractForm.vue'
import * as CustomerApi from '@/api/crm/customer'
import { TabsPaneContext } from 'element-plus'

defineOptions({ name: 'CrmContract' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<ContractApi.ContractVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sceneType: '1',
  name: null,
  customerId: null,
  orderDate: [],
  no: null
})
const queryFormRef = ref()
const exportLoading = ref(false)
const activeName = ref('1')
const customerList = ref<CustomerApi.CustomerVO[]>([])

const sceneLabelMap: Record<string, string> = {
  '1': '我负责的',
  '2': '我参与的',
  '3': '下属负责的'
}

const currentSceneLabel = computed(() => sceneLabelMap[activeName.value] || '合同池')

const contractStats = computed(() => {
  return list.value.reduce(
    (acc, item) => {
      acc.totalPrice += Number(item.totalPrice || 0)
      acc.receivedPrice += Number(item.totalReceivablePrice || 0)
      if (Number(item.auditStatus) === 0) {
        acc.pendingAuditCount += 1
      }
      return acc
    },
    {
      totalPrice: 0,
      receivedPrice: 0,
      pendingAuditCount: 0
    }
  )
})

const formatDateText = (value?: string, pattern = 'YYYY-MM-DD HH:mm') => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), pattern)
}

const formatMoney = (value?: number | string | null) => {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}

const getUnreceivedPrice = (row: ContractApi.ContractVO) => {
  return Number(row.totalPrice || 0) - Number(row.totalReceivablePrice || 0)
}

const auditStatusLabel = (status?: number) => {
  switch (status) {
    case 0:
      return '待提审'
    case 10:
      return '审批中'
    case 20:
      return '已通过'
    case 30:
      return '已驳回'
    default:
      return '未知'
  }
}

const auditPillClass = (status?: number) => {
  switch (status) {
    case 20:
      return 'soft-pill--emerald'
    case 10:
      return 'soft-pill--blue'
    case 30:
      return 'soft-pill--rose'
    default:
      return 'soft-pill--amber'
  }
}

const handleTabClick = (tab: TabsPaneContext) => {
  queryParams.sceneType = tab.paneName
  handleQuery()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ContractApi.getContractPage(queryParams)
    list.value = data.list
    total.value = data.total
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

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ContractApi.deleteContract(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await ContractApi.exportContract(queryParams)
    download.excel(data, '合同.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleSubmit = async (row: ContractApi.ContractVO) => {
  await message.confirm(`您确定提交【${row.name}】审核吗？`)
  await ContractApi.submitContract(row.id)
  message.success('提交审核成功！')
  await getList()
}

const { push } = useRouter()
const handleProcessDetail = (row: ContractApi.ContractVO) => {
  push({ name: 'BpmProcessInstanceDetail', query: { id: row.processInstanceId } })
}

const openDetail = (id: number) => {
  push({ name: 'CrmContractDetail', params: { id } })
}

const openCustomerDetail = (id: number) => {
  push({ name: 'CrmCustomerDetail', params: { id } })
}

const openContactDetail = (id: number) => {
  push({ name: 'CrmContactDetail', params: { id } })
}

const openBusinessDetail = (id: number) => {
  push({ name: 'CrmBusinessDetail', params: { id } })
}

onMounted(async () => {
  await getList()
  customerList.value = await CustomerApi.getCustomerSimpleList()
})
</script>

<style lang="scss" scoped>
.sales-page-shell {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 4px 4px 20px;
  background: #f5f7fa;
}

.sales-page-shell__hero,
.sales-page-shell__search-card,
.sales-page-shell__table-card {
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

.hero-stat-card__value--mono {
  font-size: 18px;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
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

.contract-scene-tabs {
  margin-top: 14px;
}

.query-form {
  margin-top: 16px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.table-toolbar__main {
  min-width: 0;
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

.sales-table {
  min-width: 1180px;
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
  align-items: center;
  gap: 8px;
}

.cell-stack__mono {
  color: #0f172a;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 700;
}

.cell-stack__title {
  display: inline-flex;
  width: fit-content;
  max-width: 100%;
  align-items: center;
  color: #1677ff;
  font-size: 14px;
  font-weight: 700;
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

.cell-stack__meta--truncate {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-stack__label {
  color: #94a3b8;
}

.amount-stack {
  text-align: right;
}

.amount-stack__main {
  color: #0f172a;
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
  font-size: 18px;
  font-weight: 800;
}

.amount-stack__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.amount-stack__meta--warning {
  color: #b45309;
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
  line-height: 1.2;
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

:deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: #e2e8f0;
}

:deep(.el-tabs__item) {
  height: 38px;
  color: #64748b;
  font-weight: 600;
}

:deep(.el-tabs__item.is-active) {
  color: #1677ff;
}

:deep(.el-tabs__active-bar) {
  background-color: #1677ff;
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
}

@media (max-width: 900px) {
  .sales-page-shell {
    padding: 0 0 18px;
  }

  .page-hero__stats--row {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .query-form__grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .search-card__header,
  .table-toolbar,
  .table-toolbar__actions {
    flex-direction: column;
    align-items: stretch;
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
