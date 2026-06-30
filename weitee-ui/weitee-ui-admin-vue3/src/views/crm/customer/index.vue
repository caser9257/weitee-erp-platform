<template>
  <div class="sales-page-shell">
    <doc-alert title="【客户】客户管理、公海客户" url="https://doc.iocoder.cn/crm/customer/" />
    <doc-alert title="【通用】数据权限" url="https://doc.iocoder.cn/crm/permission/" />

    <ContentWrap class="sales-page-shell__hero">
      <div class="page-hero">
        <div class="page-hero__main">
          <p class="page-hero__eyebrow">Sales Customer Hub</p>
          <h1 class="page-hero__title">客户信息</h1>
          <p class="page-hero__desc">
            统一收口客户档案、跟进节奏、归属人与状态标签，提升销售扫描效率与筛选效率。
          </p>
        </div>
        <div class="page-hero__stats">
          <div class="hero-stat-card">
            <span class="hero-stat-card__label">当前结果</span>
            <strong class="hero-stat-card__value">{{ total }}</strong>
            <span class="hero-stat-card__meta">本次筛选命中客户数</span>
          </div>
          <div class="hero-stat-card">
            <span class="hero-stat-card__label">已成交</span>
            <strong class="hero-stat-card__value">{{ customerStats.dealCount }}</strong>
            <span class="hero-stat-card__meta">当前页成交客户</span>
          </div>
          <div class="hero-stat-card">
            <span class="hero-stat-card__label">锁定客户</span>
            <strong class="hero-stat-card__value">{{ customerStats.lockedCount }}</strong>
            <span class="hero-stat-card__meta">当前页锁定状态客户</span>
          </div>
          <div class="hero-stat-card">
            <span class="hero-stat-card__label">待跟进</span>
            <strong class="hero-stat-card__value">{{ customerStats.nextFollowCount }}</strong>
            <span class="hero-stat-card__meta">已配置下次联系时间</span>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="sales-page-shell__search-card">
      <div class="search-card__header">
        <div>
          <div class="search-card__title">客户视角</div>
          <div class="search-card__subtitle">通过归属范围切换当前客户池，再组合条件精准过滤。</div>
        </div>
      </div>

      <el-tabs v-model="activeName" class="customer-scene-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="我负责的" name="1" />
        <el-tab-pane label="我参与的" name="2" />
        <el-tab-pane label="下属负责的" name="3" />
      </el-tabs>

      <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="query-form">
        <div class="query-form__grid">
          <el-form-item label="客户名称" prop="name">
            <el-input
              v-model="queryParams.name"
              clearable
              placeholder="请输入客户名称"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="mobile">
            <el-input
              v-model="queryParams.mobile"
              clearable
              placeholder="请输入手机号"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="所属行业" prop="industryId">
            <el-select v-model="queryParams.industryId" clearable placeholder="请选择所属行业">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.CRM_CUSTOMER_INDUSTRY)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="客户级别" prop="level">
            <el-select v-model="queryParams.level" clearable placeholder="请选择客户级别">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.CRM_CUSTOMER_LEVEL)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="客户来源" prop="source">
            <el-select v-model="queryParams.source" clearable placeholder="请选择客户来源">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.CRM_CUSTOMER_SOURCE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
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
          <div class="table-toolbar__title">客户列表</div>
          <div class="table-toolbar__meta">
            当前场景：<strong>{{ currentSceneLabel }}</strong>
            <span>共 {{ total }} 条</span>
          </div>
        </div>
        <div class="table-toolbar__actions">
          <el-button
            v-hasPermi="['crm:customer:create']"
            type="primary"
            @click="openForm('create')"
          >
            <Icon class="mr-5px" icon="ep:plus" />
            新增客户
          </el-button>
          <el-button v-hasPermi="['crm:customer:import']" plain @click="handleImport">
            <Icon class="mr-5px" icon="ep:upload" />
            导入
          </el-button>
          <el-button
            v-hasPermi="['crm:customer:export']"
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
          class="sales-table"
          :show-overflow-tooltip="false"
        >
          <el-table-column label="客户档案" min-width="250" fixed="left">
            <template #default="{ row }">
              <div class="cell-stack">
                <el-link
                  :underline="false"
                  type="primary"
                  class="cell-stack__title"
                  @click="openDetail(row.id)"
                >
                  {{ row.name }}
                </el-link>
                <div class="cell-stack__meta">
                  <span>{{ row.mobile || '未录入手机' }}</span>
                  <span>{{ row.telephone || '未录入电话' }}</span>
                </div>
                <div
                  class="cell-stack__meta cell-stack__meta--truncate"
                  :title="row.detailAddress || '-'"
                >
                  {{ row.detailAddress || '未录入地址' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="来源与等级" min-width="180">
            <template #default="{ row }">
              <div class="tag-stack">
                <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_SOURCE" :value="row.source" />
                <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_LEVEL" :value="row.level" />
                <dict-tag :type="DICT_TYPE.CRM_CUSTOMER_INDUSTRY" :value="row.industryId" />
              </div>
            </template>
          </el-table-column>

          <el-table-column label="跟进节奏" min-width="220">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">下次联系</span>
                  <span>{{ formatDateText(row.contactNextTime) }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">最后跟进</span>
                  <span>{{ formatDateText(row.contactLastTime) }}</span>
                </div>
                <div
                  class="cell-stack__meta cell-stack__meta--truncate"
                  :title="row.contactLastContent || '-'"
                >
                  {{ row.contactLastContent || '暂无跟进记录' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态概览" min-width="180">
            <template #default="{ row }">
              <div class="tag-stack">
                <span class="soft-pill" :class="flagClass(row.lockStatus)">
                  {{ flagLabel('锁定', row.lockStatus) }}
                </span>
                <span class="soft-pill" :class="flagClass(row.dealStatus, 'success')">
                  {{ flagLabel('成交', row.dealStatus) }}
                </span>
                <span class="soft-pill soft-pill--slate">距公海 {{ row.poolDay ?? '-' }} 天</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="负责人" min-width="170">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title cell-stack__title--plain">{{
                  row.ownerUserName || '-'
                }}</div>
                <div class="cell-stack__meta">{{ row.ownerUserDeptName || '未配置部门' }}</div>
                <div class="cell-stack__meta">创建人：{{ row.creatorName || '-' }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="时间" min-width="180">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">更新</span>
                  <span>{{ formatDateText(row.updateTime) }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">创建</span>
                  <span>{{ formatDateText(row.createTime) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column align="center" fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['crm:customer:update']"
                link
                type="primary"
                @click="openForm('update', row.id)"
              >
                编辑
              </el-button>
              <el-button
                v-hasPermi="['crm:customer:delete']"
                link
                type="danger"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="暂无客户数据，试试调整筛选条件" />
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

    <CustomerForm ref="formRef" @success="getList" />
    <CustomerImportForm ref="importFormRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import * as CustomerApi from '@/api/crm/customer'
import CustomerForm from './CustomerForm.vue'
import CustomerImportForm from './CustomerImportForm.vue'
import { TabsPaneContext } from 'element-plus'

defineOptions({ name: 'CrmCustomer' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sceneType: '1',
  name: '',
  mobile: '',
  industryId: undefined,
  level: undefined,
  source: undefined,
  pool: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const activeName = ref('1')

const sceneLabelMap: Record<string, string> = {
  '1': '我负责的',
  '2': '我参与的',
  '3': '下属负责的'
}

const currentSceneLabel = computed(() => sceneLabelMap[activeName.value] || '客户池')

const normalizeBool = (value: unknown) =>
  value === true || value === 1 || value === '1' || value === 'true'

const customerStats = computed(() => ({
  dealCount: list.value.filter((item) => normalizeBool(item.dealStatus)).length,
  lockedCount: list.value.filter((item) => normalizeBool(item.lockStatus)).length,
  nextFollowCount: list.value.filter((item) => Boolean(item.contactNextTime)).length
}))

const formatDateText = (value?: string) => {
  if (!value) {
    return '-'
  }
  return formatDate(new Date(value), 'YYYY-MM-DD HH:mm')
}

const flagClass = (value: unknown, emphasis: 'warning' | 'success' = 'warning') => {
  if (normalizeBool(value)) {
    return emphasis === 'success' ? 'soft-pill--emerald' : 'soft-pill--amber'
  }
  return 'soft-pill--slate'
}

const flagLabel = (label: string, value: unknown) => `${label}${normalizeBool(value) ? '中' : '否'}`

const handleTabClick = (tab: TabsPaneContext) => {
  queryParams.sceneType = tab.paneName as string
  handleQuery()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await CustomerApi.getCustomerPage(queryParams)
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

const { currentRoute, push } = useRouter()
const openDetail = (id: number) => {
  push({ name: 'CrmCustomerDetail', params: { id } })
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await CustomerApi.deleteCustomer(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const importFormRef = ref<InstanceType<typeof CustomerImportForm>>()
const handleImport = () => {
  importFormRef.value?.open()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await CustomerApi.exportCustomer(queryParams)
    download.excel(data, '客户.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

watch(
  () => currentRoute.value.fullPath,
  () => {
    getList()
  }
)

onMounted(() => {
  getList()
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
  gap: 20px;
  align-items: stretch;
  justify-content: space-between;
}

.page-hero__main {
  min-width: 0;
  flex: 1;
}

.page-hero__eyebrow {
  margin: 0 0 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.page-hero__title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.page-hero__desc {
  max-width: 680px;
  margin: 12px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.page-hero__stats {
  display: grid;
  width: min(520px, 100%);
  flex: 0 0 auto;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-stat-card {
  display: flex;
  min-height: 116px;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px 18px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f1f5f9 100%);
}

.hero-stat-card__label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.hero-stat-card__value {
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.hero-stat-card__meta {
  color: #94a3b8;
  font-size: 12px;
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

.search-card__subtitle,
.table-toolbar__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.customer-scene-tabs {
  margin-top: 14px;
}

.query-form {
  margin-top: 16px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
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
  min-width: 1100px;
}

.cell-stack {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
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

.cell-stack__meta--truncate {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-stack__label {
  color: #94a3b8;
}

.tag-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

.soft-pill--slate {
  border-color: #cbd5e1;
  background: #f8fafc;
  color: #475569;
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
  .page-hero {
    flex-direction: column;
  }

  .page-hero__stats {
    width: 100%;
  }

  .query-form__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .sales-page-shell {
    padding: 0 0 18px;
  }

  .page-hero__stats,
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
