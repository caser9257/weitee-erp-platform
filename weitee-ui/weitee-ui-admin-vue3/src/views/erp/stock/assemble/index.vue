<template>
  <ContentWrap class="stock-assemble-page__filter-card">
    <div class="stock-assemble-page__title">组装拆卸</div>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="stock-assemble-query">
      <div class="stock-assemble-query__grid">
        <el-form-item label="单号" prop="no">
          <el-input
            v-model="queryParams.no"
            clearable
            placeholder="请输入单号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="动作类型" prop="actionType">
          <el-select v-model="queryParams.actionType" clearable placeholder="请选择动作类型">
            <el-option label="组装" value="ASSEMBLE" />
            <el-option label="拆卸" value="DISASSEMBLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" clearable filterable placeholder="请选择仓库">
            <el-option
              v-for="item in warehouseList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option label="草稿" :value="ASSEMBLE_STATUS.DRAFT" />
            <el-option label="已审核" :value="ASSEMBLE_STATUS.APPROVE" />
            <el-option label="已驳回" :value="ASSEMBLE_STATUS.REJECT" />
          </el-select>
        </el-form-item>
      </div>
      <div class="stock-assemble-query__footer">
        <div></div>
        <div class="stock-assemble-query__actions">
          <el-button :disabled="listLoading" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="stock-assemble-page__list-card">
    <div class="stock-assemble-toolbar">
      <el-button
        v-if="canCreate"
        type="primary"
        v-hasPermi="['erp:stock-assemble:create']"
        @click="openForm('create')"
      >
        <Icon icon="ep:plus" class="mr-5px" /> 新增
      </el-button>
      <span class="stock-assemble-toolbar__total">共 {{ total }} 条记录</span>
    </div>

    <div class="stock-assemble-table-shell">
      <el-table v-loading="listLoading" :data="list" class="stock-assemble-table">
        <template #empty>
          <div v-if="listLoadFailed" class="stock-assemble-empty stock-assemble-empty--error">
            <div class="stock-assemble-empty__icon"><Icon icon="ep:warning-filled" /></div>
            <div class="stock-assemble-empty__title">列表加载失败</div>
            <el-button type="primary" plain :disabled="listLoading" @click="getList">重试加载</el-button>
          </div>
          <div v-else class="stock-assemble-empty">
            <div class="stock-assemble-empty__icon"><Icon icon="ep:box" /></div>
            <div class="stock-assemble-empty__title">暂无组装拆卸记录</div>
          </div>
        </template>

        <el-table-column label="单据信息" min-width="220">
          <template #default="{ row }">
            <div class="stock-assemble-order">
              <span class="stock-assemble-order__no">{{ row.no || '-' }}</span>
              <span class="stock-assemble-order__meta">{{ formatDateValue(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="动作" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="row.actionType === 'ASSEMBLE' ? 'success' : 'warning'">
              {{ row.actionTypeName || getActionTypeName(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="仓库" min-width="150">
          <template #default="{ row }">{{ row.warehouseName || getWarehouseName(row.warehouseId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="产品" min-width="190">
          <template #default="{ row }">
            <div class="stock-assemble-product">
              <span>{{ row.productName || getProductName(row.productId) || '-' }}</span>
              <span class="stock-assemble-product__meta">数量 {{ formatCount(row.count) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成本" min-width="130" align="right">
          <template #default="{ row }"><span class="stock-assemble-number">{{ formatCurrency(row.totalCost) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="stock-assemble-actions">
              <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
              <el-button
                v-if="canEdit && canEditRow(row)"
                link
                type="primary"
                :disabled="isRowBusy(row.id)"
                @click="openForm('update', row.id)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canApprove && canApproveRow(row)"
                link
                type="success"
                :loading="approvingId === row.id"
                :disabled="isRowBusy(row.id)"
                @click="handleApprove(row)"
              >
                审核
              </el-button>
              <el-button
                v-if="canDelete && canDeleteRow(row)"
                link
                type="danger"
                :loading="deletingId === row.id"
                :disabled="isRowBusy(row.id)"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="stock-assemble-page__footer">
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <Dialog v-model="formVisible" :title="formTitle" width="720" @closed="resetForm">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-position="top"
      class="stock-assemble-form"
      :disabled="formDetailLoading || saveLoading"
    >
      <el-form-item label="动作类型" prop="actionType">
        <el-radio-group v-model="formData.actionType">
          <el-radio-button label="ASSEMBLE">组装</el-radio-button>
          <el-radio-button label="DISASSEMBLE">拆卸</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <div class="stock-assemble-form__grid">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="formData.warehouseId" filterable placeholder="请选择仓库" class="stock-assemble-form__field">
            <el-option
              v-for="item in warehouseList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <ProductRemoteSelect
            v-model="formData.productId"
            class="stock-assemble-form__field"
            placeholder="请选择产品"
            @select="cacheSelectedProduct"
          />
        </el-form-item>
        <el-form-item label="数量" prop="count">
          <el-input-number
            v-model="formData.count"
            :min="getQuantityStep(formData.productId)"
            :step="getQuantityStep(formData.productId)"
            :precision="getQuantityPrecision(formData.productId)"
            controls-position="right"
            class="stock-assemble-form__field"
          />
        </el-form-item>
      </div>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="stock-assemble-form__footer">
      <el-button :disabled="saveLoading" @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="submitForm">保存</el-button>
      </div>
    </template>
  </Dialog>

  <el-drawer v-model="detailVisible" :with-header="false" size="520px" class="stock-assemble-detail-drawer">
    <div class="stock-assemble-detail" v-loading="drawerLoading">
      <div class="stock-assemble-detail__header">
        <div>
          <div class="stock-assemble-detail__title">{{ selectedRow?.no || '组装拆卸' }}</div>
          <div class="stock-assemble-detail__meta">
            {{ getActionTypeName(selectedRow?.actionType) }} · {{ selectedRow?.warehouseName || '-' }}
          </div>
        </div>
        <el-button circle text :disabled="drawerLoading" @click="detailVisible = false">
          <Icon icon="ep:close" />
        </el-button>
      </div>
      <el-alert v-if="detailLoadError" type="error" :closable="false" show-icon>{{ detailLoadError }}</el-alert>
      <template v-else-if="selectedRow">
        <section class="stock-assemble-detail__section">
          <div class="stock-assemble-detail__section-title">单据状态</div>
          <el-tag size="small" effect="light" :type="getStatusTagType(selectedRow.status)">
            {{ getStatusLabel(selectedRow.status) }}
          </el-tag>
        </section>
        <section class="stock-assemble-detail__section">
          <div class="stock-assemble-detail__section-title">基础信息</div>
          <div class="stock-assemble-detail__facts">
            <div><span>产品</span><strong>{{ selectedRow.productName || getProductName(selectedRow.productId) || '-' }}</strong></div>
            <div><span>数量</span><strong class="stock-assemble-number">{{ formatCount(selectedRow.count) }}</strong></div>
            <div><span>总成本</span><strong class="stock-assemble-number">{{ formatCurrency(selectedRow.totalCost) }}</strong></div>
            <div><span>创建时间</span><strong>{{ formatDateValue(selectedRow.createTime) }}</strong></div>
          </div>
        </section>
        <section class="stock-assemble-detail__section">
          <div class="stock-assemble-detail__section-title">库存明细</div>
          <el-table :data="selectedRow.items || []" size="small">
            <el-table-column label="产品" min-width="150">
              <template #default="{ row }">{{ row.productName || getProductName(row.productId) || '-' }}</template>
            </el-table-column>
            <el-table-column label="数量" width="90" align="right">
              <template #default="{ row }"><span class="stock-assemble-number">{{ formatCount(row.count) }}</span></template>
            </el-table-column>
            <el-table-column label="方向" width="72">
              <template #default="{ row }">{{ row.stockDirection > 0 ? '入' : '出' }}</template>
            </el-table-column>
          </el-table>
        </section>
        <section v-if="selectedRow.remark" class="stock-assemble-detail__section">
          <div class="stock-assemble-detail__section-title">备注</div>
          <div class="stock-assemble-detail__remark">{{ selectedRow.remark }}</div>
        </section>
      </template>
      <div v-else class="stock-assemble-empty">暂无详情</div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { checkPermi } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'
import type { ProductVO } from '@/api/erp/product/product'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import {
  StockAssembleApi,
  StockAssemblePageReqVO,
  StockAssembleVO
} from '@/api/erp/stock/assemble'

defineOptions({ name: 'ErpStockAssemble' })

const ASSEMBLE_STATUS = {
  DRAFT: 0,
  APPROVE: 20,
  REJECT: 30
} as const

const message = useMessage()
const canCreate = checkPermi(['erp:stock-assemble:create'])
const canEdit = checkPermi(['erp:stock-assemble:update'])
const canApprove = checkPermi(['erp:stock-assemble:update-status'])
const canDelete = checkPermi(['erp:stock-assemble:delete'])

const queryFormRef = ref()
const queryParams = reactive<StockAssemblePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  actionType: undefined,
  warehouseId: undefined,
  productId: undefined,
  status: undefined
})
const list = ref<StockAssembleVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listLoadFailed = ref(false)
const productList = ref<ProductVO[]>([])
const warehouseList = ref<WarehouseVO[]>([])
const optionsLoading = ref(false)

const formRef = ref()
const formVisible = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formData = reactive<StockAssembleVO>(createDefaultForm())
const formDetailLoading = ref(false)
const saveLoading = ref(false)

const detailVisible = ref(false)
const selectedRow = ref<StockAssembleVO>()
const detailLoadError = ref('')
const drawerLoading = ref(false)
const approvingId = ref<number>()
const deletingId = ref<number>()

const formTitle = computed(() => (formMode.value === 'create' ? '新增组装拆卸单' : '编辑组装拆卸单'))
const formRules = {
  actionType: [{ required: true, message: '动作类型不能为空', trigger: 'change' }],
  warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }],
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  count: [{ required: true, message: '数量不能为空', trigger: 'change' }]
}

function createDefaultForm(): StockAssembleVO {
  return { actionType: 'ASSEMBLE', warehouseId: undefined, productId: undefined, count: undefined, remark: '' }
}

const loadOptions = async () => {
  if (optionsLoading.value || warehouseList.value.length) return
  optionsLoading.value = true
  try {
    warehouseList.value = (await WarehouseApi.getWarehouseSimpleList()) || []
  } finally {
    optionsLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listLoadFailed.value = false
  try {
    await loadOptions()
    const data = await StockAssembleApi.getStockAssemblePage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch (error: any) {
    listLoadFailed.value = true
    message.error(error?.message || '列表加载失败，请重试')
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  void getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNo: 1,
    no: undefined,
    actionType: undefined,
    warehouseId: undefined,
    productId: undefined,
    status: undefined
  })
  queryFormRef.value?.resetFields()
  void getList()
}

const resetForm = () => {
  Object.assign(formData, createDefaultForm())
  formRef.value?.resetFields()
  formDetailLoading.value = false
  saveLoading.value = false
}

const openForm = async (mode: 'create' | 'update', id?: number) => {
  resetForm()
  formMode.value = mode
  formVisible.value = true
  await loadOptions()
  if (!id) return
  formDetailLoading.value = true
  try {
    Object.assign(formData, await StockAssembleApi.getStockAssemble(id))
  } catch (error: any) {
    formVisible.value = false
    message.error(error?.message || '详情加载失败，请重试')
  } finally {
    formDetailLoading.value = false
  }
}

const submitForm = async () => {
  if (saveLoading.value || formDetailLoading.value) return
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saveLoading.value = true
  try {
    if (formMode.value === 'create') {
      await StockAssembleApi.createStockAssemble({ ...formData })
      message.success('新增成功')
    } else {
      await StockAssembleApi.updateStockAssemble({ ...formData })
      message.success('保存成功')
    }
    formVisible.value = false
    await getList()
  } catch (error: any) {
    message.error(error?.message || '保存失败，请重试')
  } finally {
    saveLoading.value = false
  }
}

const openDetail = async (id?: number) => {
  if (!id || drawerLoading.value) return
  detailVisible.value = true
  selectedRow.value = undefined
  detailLoadError.value = ''
  drawerLoading.value = true
  try {
    selectedRow.value = await StockAssembleApi.getStockAssemble(id)
  } catch (error: any) {
    detailLoadError.value = error?.message || '详情加载失败，请重试'
  } finally {
    drawerLoading.value = false
  }
}

const handleApprove = async (row: StockAssembleVO) => {
  if (!row.id || !canApproveRow(row) || isRowBusy(row.id)) return
  try {
    await message.confirm(`确定审核单据“${row.no || '-'}”吗？`)
  } catch {
    return
  }
  approvingId.value = row.id
  try {
    await StockAssembleApi.updateStockAssembleStatus(row.id, ASSEMBLE_STATUS.APPROVE)
    message.success('审核成功')
    await getList()
  } catch (error: any) {
    message.error(error?.message || '审核失败，请重试')
  } finally {
    approvingId.value = undefined
  }
}

const handleDelete = async (row: StockAssembleVO) => {
  if (!row.id || !canDeleteRow(row) || isRowBusy(row.id)) return
  try {
    await message.confirm(`确定删除单据“${row.no || '-'}”吗？`)
  } catch {
    return
  }
  deletingId.value = row.id
  try {
    await StockAssembleApi.deleteStockAssemble([row.id])
    message.success('删除成功')
    await getList()
  } catch (error: any) {
    message.error(error?.message || '删除失败，请重试')
  } finally {
    deletingId.value = undefined
  }
}

const canEditRow = (row: StockAssembleVO) => row.status !== ASSEMBLE_STATUS.APPROVE
const canApproveRow = (row: StockAssembleVO) =>
  row.status === ASSEMBLE_STATUS.DRAFT || row.status === ASSEMBLE_STATUS.REJECT
const canDeleteRow = (row: StockAssembleVO) => row.status !== ASSEMBLE_STATUS.APPROVE
const isRowBusy = (id?: number) => id !== undefined && (approvingId.value === id || deletingId.value === id)

const getActionTypeName = (value?: string) => (value === 'DISASSEMBLE' ? '拆卸' : '组装')
const getStatusLabel = (status?: number) => {
  if (status === ASSEMBLE_STATUS.APPROVE) return '已审核'
  if (status === ASSEMBLE_STATUS.REJECT) return '已驳回'
  return '草稿'
}
const getStatusTagType = (status?: number) => {
  if (status === ASSEMBLE_STATUS.APPROVE) return 'success'
  if (status === ASSEMBLE_STATUS.REJECT) return 'danger'
  return 'warning'
}
const getProductName = (id?: number) => productList.value.find((item) => item.id === id)?.name
const cacheSelectedProduct = (product: ProductVO | null) => {
  if (product) {
    productList.value = [...productList.value.filter((item) => item.id !== product.id), product]
  }
}
const getQuantityPrecision = (productId?: number) => {
  const precision = productList.value.find((item) => item.id === productId)?.quantityPrecision
  return Number.isInteger(precision) && precision! >= 0 && precision! <= 6 ? precision! : 3
}
const getQuantityStep = (productId?: number) => 10 ** -getQuantityPrecision(productId)
const getWarehouseName = (id?: number) => warehouseList.value.find((item) => item.id === id)?.name
const formatCount = (value?: number) => (value === undefined || value === null ? '-' : Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 6 }))
const formatCurrency = (value?: number) => (value === undefined || value === null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))
const formatDateValue = (value?: Date | string | number) => (value ? formatDate(value) : '-')

onMounted(() => {
  void getList()
})
</script>

<style scoped>
.stock-assemble-page__filter-card,
.stock-assemble-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.stock-assemble-page__title {
  margin-bottom: 16px;
  color: var(--erp-slate-900);
  font-size: 20px;
  line-height: 28px;
  font-weight: 700;
}

.stock-assemble-query__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stock-assemble-query__footer,
.stock-assemble-toolbar,
.stock-assemble-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stock-assemble-query__footer {
  margin-top: 4px;
}

.stock-assemble-query__actions,
.stock-assemble-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.stock-assemble-toolbar {
  margin-bottom: 16px;
}

.stock-assemble-toolbar__total {
  color: var(--erp-slate-500);
  font-size: 13px;
}

.stock-assemble-table-shell {
  overflow-x: auto;
}

.stock-assemble-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 1;
}

.stock-assemble-order,
.stock-assemble-product {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.stock-assemble-order__no {
  color: var(--erp-slate-900);
  font-family: var(--erp-font-mono);
  word-break: break-all;
}

.stock-assemble-order__meta,
.stock-assemble-product__meta {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.stock-assemble-number {
  color: var(--erp-slate-900);
  font-family: var(--erp-font-mono);
  font-variant-numeric: tabular-nums;
}

.stock-assemble-empty {
  display: flex;
  min-height: 150px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.stock-assemble-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.stock-assemble-empty--error .stock-assemble-empty__icon {
  background: var(--erp-danger-50);
  color: var(--erp-danger-600);
}

.stock-assemble-empty__title {
  font-size: 14px;
  font-weight: 600;
}

.stock-assemble-page__footer {
  justify-content: flex-end;
  margin-top: 16px;
}

.stock-assemble-form {
  max-height: min(68vh, 720px);
  overflow-y: auto;
  padding-right: 4px;
}

.stock-assemble-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stock-assemble-form__field {
  width: 100%;
}

.stock-assemble-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

.stock-assemble-detail {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.stock-assemble-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px;
  border-radius: 10px;
  background: var(--erp-slate-800);
  color: var(--erp-surface-white);
}

.stock-assemble-detail__title {
  font-size: 18px;
  line-height: 26px;
  font-weight: 700;
  word-break: break-all;
}

.stock-assemble-detail__meta {
  margin-top: 6px;
  color: var(--erp-slate-300);
  font-size: 13px;
}

.stock-assemble-detail__section {
  padding: 16px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.stock-assemble-detail__section-title {
  margin-bottom: 12px;
  color: var(--erp-slate-800);
  font-size: 14px;
  font-weight: 700;
}

.stock-assemble-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.stock-assemble-detail__facts > div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.stock-assemble-detail__facts span {
  color: var(--erp-slate-500);
  font-size: 12px;
}

.stock-assemble-detail__facts strong {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  word-break: break-word;
}

.stock-assemble-detail__remark {
  color: var(--erp-slate-700);
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1279px) {
  .stock-assemble-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stock-assemble-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .stock-assemble-query__grid,
  .stock-assemble-form__grid,
  .stock-assemble-detail__facts {
    grid-template-columns: minmax(0, 1fr);
  }

  .stock-assemble-query__footer,
  .stock-assemble-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .stock-assemble-query__actions {
    justify-content: flex-end;
  }

  .stock-assemble-toolbar .el-button {
    width: 100%;
  }
}
</style>
