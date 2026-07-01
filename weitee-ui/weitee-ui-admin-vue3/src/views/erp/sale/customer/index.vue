<template>
  <div class="customer-page-shell">
    <doc-alert title="【销售】销售订单、出库、退货" url="https://doc.iocoder.cn/erp/sale/" />

    <ContentWrap class="customer-page-shell__hero">
      <div class="page-hero page-hero--compact">
        <div class="page-hero__header">
          <h1 class="page-hero__title">客户信息</h1>
        </div>
        <div class="page-hero__stats page-hero__stats--row">
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--blue">
            <span class="hero-stat-card__label">当前结果</span>
            <strong class="hero-stat-card__value">{{ total }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--green">
            <span class="hero-stat-card__label">启用客户</span>
            <strong class="hero-stat-card__value">{{ customerStats.enabledCount }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--slate">
            <span class="hero-stat-card__label">停用客户</span>
            <strong class="hero-stat-card__value">{{ customerStats.disabledCount }}</strong>
          </div>
          <div class="hero-stat-card hero-stat-card--compact hero-stat-card--teal">
            <span class="hero-stat-card__label">已留联系方式</span>
            <strong class="hero-stat-card__value">{{ customerStats.reachableCount }}</strong>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="customer-page-shell__search-card">
      <div class="search-card__header">
        <div class="search-card__title">筛选条件</div>
      </div>

      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-position="top"
        class="query-form"
        @submit.prevent
      >
        <div class="query-form__grid">
          <el-form-item label="名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="手机号码" prop="mobile">
            <el-input
              v-model="queryParams.mobile"
              placeholder="请输入手机号码"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="联系电话" prop="telephone">
            <el-input
              v-model="queryParams.telephone"
              placeholder="请输入联系电话"
              clearable
              @keyup.enter="handleQuery"
            />
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

    <ContentWrap class="customer-page-shell__table-card">
      <div class="table-toolbar">
        <div class="table-toolbar__main">
          <div class="table-toolbar__title">客户列表</div>
          <div class="table-toolbar__meta">共 {{ total }} 条</div>
        </div>
        <div class="table-toolbar__actions">
          <el-button
            v-hasPermi="['erp:customer:create']"
            type="primary"
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增客户
          </el-button>
          <el-button
            v-hasPermi="['erp:customer:export']"
            type="success"
            plain
            :loading="exportLoading"
            @click="handleExport"
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
          stripe
          class="customer-table"
          :show-overflow-tooltip="false"
        >
          <el-table-column label="客户档案" min-width="260" fixed="left">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__title" :title="row.name || '-'">{{ row.name || '-' }}</div>
                <div class="cell-stack__meta">
                  <span>{{ row.contact || '未填写联系人' }}</span>
                  <span class="mono-text">{{ formatSort(row.sort) }}</span>
                </div>
                <div class="cell-stack__meta cell-stack__meta--truncate" :title="row.remark || '-'">
                  {{ row.remark || '暂无备注' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="联系方式" min-width="240">
            <template #default="{ row }">
              <div class="cell-stack">
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">手机</span>
                  <span class="mono-text">{{ row.mobile || '-' }}</span>
                </div>
                <div class="cell-stack__meta">
                  <span class="cell-stack__label">电话</span>
                  <span class="mono-text">{{ row.telephone || '-' }}</span>
                </div>
                <div class="cell-stack__meta cell-stack__meta--truncate" :title="row.email || '-'">
                  <span class="cell-stack__label">邮箱</span>
                  <span>{{ row.email || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" min-width="130" align="center">
            <template #default="{ row }">
              <span class="soft-pill" :class="statusPillClass(row.status)">
                {{ commonStatusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" fixed="right" width="150">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['erp:customer:update']"
                link
                type="primary"
                @click="openForm('update', row.id)"
              >
                编辑
              </el-button>
              <el-button
                v-hasPermi="['erp:customer:delete']"
                link
                type="danger"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <div class="customer-empty">
              <div class="customer-empty__icon">
                <Icon icon="ep:user" />
              </div>
              <div class="customer-empty__title">暂无客户数据</div>
            </div>
          </template>
        </el-table>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <CustomerForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { CustomerApi, CustomerVO } from '@/api/erp/sale/customer'
import download from '@/utils/download'
import CustomerForm from './CustomerForm.vue'

defineOptions({ name: 'ErpCustomer' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<CustomerVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  mobile: undefined,
  telephone: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const customerStats = computed(() => ({
  enabledCount: list.value.filter((item) => Number(item.status) === 0).length,
  disabledCount: list.value.filter((item) => Number(item.status) !== 0).length,
  reachableCount: list.value.filter((item) => Boolean(item.mobile || item.telephone)).length
}))

const formatSort = (value?: number) => `排序 ${value ?? 0}`

const commonStatusLabel = (status?: number) => (Number(status) === 0 ? '启用' : '停用')

const statusPillClass = (status?: number) =>
  Number(status) === 0 ? 'soft-pill--emerald' : 'soft-pill--slate'

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

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.customer-page-shell {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 4px 4px 20px;
  background: var(--app-content-bg-color);
}

.customer-page-shell__hero,
.customer-page-shell__search-card,
.customer-page-shell__table-card {
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

.table-toolbar__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
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

.customer-table {
  min-width: 960px;
}

.cell-stack {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.cell-stack__title {
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.mono-text {
  font-family: 'DIN Alternate', 'Roboto Mono', 'Courier New', monospace;
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

.customer-empty {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 36px 16px;
}

.customer-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 22px;
}

.customer-empty__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #475569;
  font-weight: 600;
}

:deep(.el-input__wrapper) {
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

  .query-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .customer-page-shell {
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
