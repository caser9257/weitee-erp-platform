<template>
  <div class="product-workbench">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="7" :xl="6">
        <ContentWrap class="category-workspace">
          <div class="workspace-header">
            <div>
              <p class="workspace-caption">{{ PAGE_COPY.workspaceCaption }}</p>
              <h3 class="workspace-title">{{ PAGE_COPY.categoryTreeTitle }}</h3>
              <p class="workspace-hint">{{ PAGE_COPY.workspaceHint }}</p>
            </div>
            <div class="workspace-header-actions">
              <el-button link type="primary" :disabled="!canSelectAllProducts" @click="handleSelectAll">
                {{ PAGE_COPY.allProducts }}
              </el-button>
              <el-tooltip
                :content="isTreeExpanded ? PAGE_COPY.collapseTree : PAGE_COPY.expandTree"
              >
                <el-button circle plain :disabled="!canToggleTreeExpanded" @click="toggleTreeExpanded">
                  <Icon :icon="isTreeExpanded ? 'ep:folder-remove' : 'ep:folder-add'" />
                </el-button>
              </el-tooltip>
            </div>
          </div>

          <div class="workspace-toolbar">
            <el-input
              v-model="categoryKeyword"
              clearable
              :placeholder="PAGE_COPY.categorySearchPlaceholder"
              class="workspace-search"
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
            <el-button
              type="primary"
              plain
              :disabled="!canCreateRootCategory"
              @click="openCategoryForm('create', undefined, 0)"
              v-hasPermi="['erp:product-category:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" />
              {{ PAGE_COPY.addRootCategory }}
            </el-button>
          </div>

          <div class="workspace-active">
            <div class="workspace-active-main">
              <span class="workspace-active-label">{{ PAGE_COPY.currentCategory }}</span>
              <el-tag round effect="light">{{ activeCategoryLabel }}</el-tag>
            </div>
            <span class="workspace-count">{{ PAGE_COPY.categoryCount(categoryCount) }}</span>
          </div>

          <div v-loading="categoryTreeLoading" class="workspace-tree">
            <template v-if="categoryLoadError">
              <el-result
                icon="error"
                :title="PAGE_COPY.categoryLoadFailed"
                :sub-title="PAGE_COPY.retryWhenReady"
              >
                <template #extra>
                  <el-button type="primary" :disabled="categoryTreeLoading" @click="loadCategoryTree">
                    {{ PAGE_COPY.reload }}
                  </el-button>
                </template>
              </el-result>
            </template>
            <template v-else-if="categoryList.length">
              <el-scrollbar class="tree-scrollbar">
                <el-tree
                  :key="categoryTreeRenderKey"
                  ref="categoryTreeRef"
                  :data="categoryList"
                  :props="defaultProps"
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  :default-expand-all="isTreeExpanded"
                  class="category-tree"
                  highlight-current
                  node-key="id"
                  @node-click="handleCategoryNodeClick"
                >
                  <template #default="{ data }">
                    <div class="tree-node">
                      <div class="tree-node-main">
                        <span class="tree-node-label">{{ data.name }}</span>
                        <span v-if="data.code" class="tree-node-code">{{ data.code }}</span>
                      </div>
                      <div class="tree-node-tools">
                        <el-tooltip :content="PAGE_COPY.addChildCategory">
                          <el-button
                            circle
                            link
                            type="primary"
                            :disabled="isCategoryDeleteBusy"
                            @click.stop="openCategoryForm('create', undefined, data.id)"
                            v-hasPermi="['erp:product-category:create']"
                          >
                            <Icon icon="ep:plus" />
                          </el-button>
                        </el-tooltip>
                        <el-tooltip :content="PAGE_COPY.editCategory">
                          <el-button
                            circle
                            link
                            type="primary"
                            :disabled="isCategoryDeleteBusy"
                            @click.stop="openCategoryForm('update', data.id)"
                            v-hasPermi="['erp:product-category:update']"
                          >
                            <Icon icon="ep:edit" />
                          </el-button>
                        </el-tooltip>
                        <el-tooltip :content="PAGE_COPY.deleteCategory">
                          <el-button
                            circle
                            link
                            type="danger"
                            :loading="categoryDeletingId === data.id"
                            :disabled="isCategoryDeleteBusy"
                            @click.stop="handleCategoryDelete(data)"
                            v-hasPermi="['erp:product-category:delete']"
                          >
                            <Icon icon="ep:delete" />
                          </el-button>
                        </el-tooltip>
                      </div>
                    </div>
                  </template>
                </el-tree>
              </el-scrollbar>
            </template>
            <template v-else>
              <el-empty :description="PAGE_COPY.noCategoryData">
                <el-button
                  type="primary"
                  plain
                  :disabled="!canCreateRootCategory"
                  @click="openCategoryForm('create', undefined, 0)"
                  v-hasPermi="['erp:product-category:create']"
                >
                  {{ PAGE_COPY.addRootCategory }}
                </el-button>
              </el-empty>
            </template>
          </div>
        </ContentWrap>
      </el-col>

      <el-col :xs="24" :lg="17" :xl="18">
        <ContentWrap class="product-panel">
          <div class="panel-header">
            <div>
              <p class="workspace-caption">{{ PAGE_COPY.panelCaption }}</p>
              <h3 class="panel-title">{{ PAGE_COPY.productListTitle }}</h3>
              <div class="panel-meta">
                <span>{{ PAGE_COPY.filterLabel }}</span>
                <el-tag type="success" effect="light">{{ activeCategoryLabel }}</el-tag>
                <span class="panel-total">{{ PAGE_COPY.totalRecords(total) }}</span>
              </div>
            </div>
            <div class="panel-summary">
              <div class="summary-card">
                <span class="summary-label">{{ PAGE_COPY.currentPageCount }}</span>
                <strong>{{ visibleProductCount }}</strong>
              </div>
            </div>
          </div>

          <el-form
            ref="queryFormRef"
            :model="queryParams"
            label-width="90px"
            class="query-form"
          >
            <el-row :gutter="16" class="query-row">
              <el-col :xs="24" :sm="14" :lg="10" :xl="8">
                <el-form-item :label="PAGE_COPY.productNameLabel" prop="name" class="query-form-item">
                  <el-input
                    v-model="queryParams.name"
                    :placeholder="PAGE_COPY.productNamePlaceholder"
                    clearable
                    class="query-input"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="10" :lg="14" :xl="16">
                <el-form-item class="query-form-item query-form-actions">
                  <el-button :disabled="!canQueryProducts" @click="handleQuery">
                    <Icon icon="ep:search" class="mr-5px" />
                    {{ PAGE_COPY.search }}
                  </el-button>
                  <el-button :disabled="!canResetQuery" @click="resetQuery">
                    <Icon icon="ep:refresh" class="mr-5px" />
                    {{ PAGE_COPY.reset }}
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <div class="panel-toolbar">
            <el-button type="primary" @click="openForm('create')" v-hasPermi="['erp:product:create']">
              <Icon icon="ep:plus" class="mr-5px" />
              {{ PAGE_COPY.addProduct }}
            </el-button>
            <el-button type="warning" plain @click="handleImport">
              <Icon icon="ep:upload" class="mr-5px" />
              {{ PAGE_COPY.importProduct }}
            </el-button>
            <el-button
              type="success"
              plain
              :loading="productExporting"
              :disabled="!canExportProducts"
              @click="handleExport"
              v-hasPermi="['erp:product:export']"
            >
              <Icon icon="ep:download" class="mr-5px" />
              {{ PAGE_COPY.exportProduct }}
            </el-button>
          </div>

          <template v-if="productLoadError">
            <el-result
              icon="error"
              :title="PAGE_COPY.productLoadFailed"
              :sub-title="PAGE_COPY.retryWhenReady"
            >
              <template #extra>
                <el-button type="primary" :disabled="productListLoading" @click="getList">
                  {{ PAGE_COPY.reload }}
                </el-button>
              </template>
            </el-result>
          </template>
          <template v-else>
            <el-table
              v-loading="productListLoading"
              :data="list"
              :stripe="true"
              :show-overflow-tooltip="true"
            >
              <el-table-column :label="PAGE_COPY.barCodeLabel" align="center" prop="barCode" min-width="150" />
              <el-table-column :label="PAGE_COPY.productNameLabel" align="center" prop="name" min-width="180" />
              <el-table-column :label="PAGE_COPY.specLabel" align="center" prop="standard" min-width="140" />
              <el-table-column :label="PAGE_COPY.categoryLabel" align="center" prop="categoryName" min-width="140" />
              <el-table-column :label="PAGE_COPY.mrpLabel" align="center" prop="mrpEnable" width="100">
                <template #default="{ row }">
                  <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="row.mrpEnable" />
                </template>
              </el-table-column>
              <el-table-column :label="PAGE_COPY.supplyTypeLabel" align="center" prop="supplyType" width="120">
                <template #default="{ row }">
                  <dict-tag :type="DICT_TYPE.ERP_SUPPLY_TYPE" :value="row.supplyType" />
                </template>
              </el-table-column>
              <el-table-column :label="PAGE_COPY.unitLabel" align="center" prop="unitName" width="90" />
              <el-table-column
                :label="PAGE_COPY.purchasePriceLabel"
                align="center"
                prop="purchasePrice"
                :formatter="erpPriceTableColumnFormatter"
                width="120"
              />
              <el-table-column
                :label="PAGE_COPY.salePriceLabel"
                align="center"
                prop="salePrice"
                :formatter="erpPriceTableColumnFormatter"
                width="120"
              />
              <el-table-column
                :label="PAGE_COPY.minPriceLabel"
                align="center"
                prop="minPrice"
                :formatter="erpPriceTableColumnFormatter"
                width="120"
              />
              <el-table-column :label="PAGE_COPY.statusLabel" align="center" prop="status" width="100">
                <template #default="{ row }">
                  <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
                </template>
              </el-table-column>
              <el-table-column
                :label="PAGE_COPY.createdAtLabel"
                align="center"
                prop="createTime"
                :formatter="dateFormatter"
                width="180"
              />
              <el-table-column :label="PAGE_COPY.actionsLabel" align="center" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button
                    link
                    type="primary"
                    :disabled="isProductDeleteBusy"
                    @click="openForm('update', row.id)"
                    v-hasPermi="['erp:product:update']"
                  >
                    {{ PAGE_COPY.edit }}
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    :loading="productDeletingId === row.id"
                    :disabled="isProductDeleteBusy"
                    @click="handleDelete(row.id)"
                    v-hasPermi="['erp:product:delete']"
                  >
                    {{ PAGE_COPY.delete }}
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty :description="PAGE_COPY.emptyProductList">
                  <el-button link type="primary" :disabled="!canResetQuery" @click="resetQuery">
                    {{ PAGE_COPY.resetFilters }}
                  </el-button>
                </el-empty>
              </template>
            </el-table>

            <div class="panel-pagination">
              <Pagination
                :total="total"
                v-model:page="queryParams.pageNo"
                v-model:limit="queryParams.pageSize"
                @pagination="getList"
              />
            </div>
          </template>
        </ContentWrap>
      </el-col>
    </el-row>

    <ProductForm ref="formRef" @success="getList" />
    <ProductImportForm ref="importFormRef" @success="getList" />
    <ProductCategoryForm ref="categoryFormRef" @success="handleCategoryFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import ProductForm from './ProductForm.vue'
import ProductImportForm from './ProductImportForm.vue'
import ProductCategoryForm from '../category/ProductCategoryForm.vue'
import { DICT_TYPE } from '@/utils/dict'
import { defaultProps, handleTree } from '@/utils/tree'
import { erpPriceTableColumnFormatter } from '@/utils'

defineOptions({ name: 'ErpProductWorkbench' })

type CategoryTreeNode = ProductCategoryVO & {
  children?: CategoryTreeNode[]
}

const PAGE_COPY = {
  workspaceCaption: '产品工作台',
  categoryTreeTitle: '分类树',
  workspaceHint: '左侧维护分类层级，右侧同步查看当前分类下的产品列表。',
  allProducts: '全部产品',
  collapseTree: '收起分类树',
  expandTree: '展开分类树',
  categorySearchPlaceholder: '按分类名称或编码搜索',
  addRootCategory: '新增一级分类',
  currentCategory: '当前分类',
  categoryLoadFailed: '分类加载失败',
  retryWhenReady: '请稍后重试',
  reload: '重新加载',
  addChildCategory: '新增子分类',
  editCategory: '编辑分类',
  deleteCategory: '删除分类',
  noCategoryData: '暂无分类数据',
  panelCaption: '产品看板',
  productListTitle: '产品列表',
  filterLabel: '当前筛选：',
  currentPageCount: '当前页条数',
  productNameLabel: '产品名称',
  productNamePlaceholder: '请输入产品名称',
  search: '搜索',
  reset: '重置',
  addProduct: '新增产品',
  importProduct: '导入产品',
  exportProduct: '导出产品',
  productLoadFailed: '产品列表加载失败',
  barCodeLabel: '条码',
  specLabel: '规格',
  categoryLabel: '分类',
  mrpLabel: 'MRP',
  supplyTypeLabel: '供应方式',
  unitLabel: '单位',
  purchasePriceLabel: '采购价',
  salePriceLabel: '销售价',
  minPriceLabel: '最低价',
  statusLabel: '状态',
  createdAtLabel: '创建时间',
  actionsLabel: '操作',
  edit: '编辑',
  delete: '删除',
  emptyProductList: '当前筛选条件下暂无产品',
  resetFilters: '重置筛选',
  categoryCount: (count: number) => `共 ${count} 个分类`,
  totalRecords: (count: number) => `共 ${count} 条`
}

const message = useMessage()
const { t } = useI18n()

const categoryTreeLoading = ref(false)
const productListLoading = ref(false)
const categoryLoadError = ref(false)
const productLoadError = ref(false)
const productExporting = ref(false)
const categoryDeletingId = ref<number>()
const productDeletingId = ref<number>()

const list = ref<ProductVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  categoryId: undefined as number | undefined
})

const queryFormRef = ref()
const categoryTreeRef = ref()
const formRef = ref()
const importFormRef = ref()
const categoryFormRef = ref()

const categoryList = ref<CategoryTreeNode[]>([])
const activeCategoryId = ref<number>()
const categoryKeyword = ref('')
const isTreeExpanded = ref(true)
const categoryTreeRenderKey = ref(0)

const countCategoryNodes = (nodes: CategoryTreeNode[]): number => {
  return nodes.reduce((count, node) => count + 1 + countCategoryNodes(node.children || []), 0)
}

const findCategoryNameById = (nodes: CategoryTreeNode[], id: number): string | undefined => {
  for (const node of nodes) {
    if (node.id === id) {
      return node.name
    }
    const childName = findCategoryNameById(node.children || [], id)
    if (childName) {
      return childName
    }
  }
  return undefined
}

const hasCategoryId = (nodes: CategoryTreeNode[], id: number): boolean => {
  return nodes.some((node) => node.id === id || hasCategoryId(node.children || [], id))
}

const categoryCount = computed(() => countCategoryNodes(categoryList.value))
const visibleProductCount = computed(() => list.value.length)
const activeCategoryLabel = computed(() => {
  if (!activeCategoryId.value) {
    return PAGE_COPY.allProducts
  }
  return findCategoryNameById(categoryList.value, activeCategoryId.value) || PAGE_COPY.allProducts
})
const isCategoryDeleteBusy = computed(() => categoryDeletingId.value !== undefined)
const isProductDeleteBusy = computed(() => productDeletingId.value !== undefined)
const canSelectAllProducts = computed(() => !categoryTreeLoading.value && !!activeCategoryId.value)
const canToggleTreeExpanded = computed(
  () => !categoryTreeLoading.value && categoryList.value.length > 0
)
const canCreateRootCategory = computed(
  () => !categoryTreeLoading.value && !isCategoryDeleteBusy.value
)
const canQueryProducts = computed(() => !productListLoading.value)
const canResetQuery = computed(() => !productListLoading.value)
const canExportProducts = computed(() => !productExporting.value && total.value > 0)

watch(categoryKeyword, (value) => {
  categoryTreeRef.value?.filter(value)
})

const syncCurrentCategory = async () => {
  await nextTick()
  categoryTreeRef.value?.setCurrentKey(activeCategoryId.value ?? null)
}

const filterNode = (value: string, data: CategoryTreeNode) => {
  if (!value) {
    return true
  }
  return [data.name, data.code].filter(Boolean).some((item) => item.includes(value))
}

const loadCategoryTree = async () => {
  categoryTreeLoading.value = true
  categoryLoadError.value = false
  try {
    const categoryData = await ProductCategoryApi.getProductCategoryList()
    const treeData = handleTree(categoryData, 'id', 'parentId') as CategoryTreeNode[]
    categoryList.value = treeData

    if (activeCategoryId.value && !hasCategoryId(treeData, activeCategoryId.value)) {
      activeCategoryId.value = undefined
      queryParams.categoryId = undefined
      queryParams.pageNo = 1
    }

    await syncCurrentCategory()
  } catch {
    categoryLoadError.value = true
  } finally {
    categoryTreeLoading.value = false
  }
}

const handleCategoryNodeClick = async (row: CategoryTreeNode) => {
  activeCategoryId.value = row?.id
  queryParams.categoryId = row?.id
  queryParams.pageNo = 1
  await getList()
}

const getList = async () => {
  productListLoading.value = true
  productLoadError.value = false
  try {
    const data = await ProductApi.getProductPage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch {
    productLoadError.value = true
  } finally {
    productListLoading.value = false
  }
}

const handleQuery = async () => {
  if (!canQueryProducts.value) {
    return
  }
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canResetQuery.value) {
    return
  }
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.categoryId = activeCategoryId.value
  await getList()
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleImport = () => {
  importFormRef.value?.open()
}

const openCategoryForm = (type: string, id?: number, parentId = 0) => {
  categoryFormRef.value?.open(type, id, parentId)
}

const handleSelectAll = async () => {
  if (!canSelectAllProducts.value) {
    return
  }
  activeCategoryId.value = undefined
  queryParams.categoryId = undefined
  queryParams.pageNo = 1
  await syncCurrentCategory()
  await getList()
}

const toggleTreeExpanded = async () => {
  if (!canToggleTreeExpanded.value) {
    return
  }
  isTreeExpanded.value = !isTreeExpanded.value
  categoryTreeRenderKey.value += 1
  await syncCurrentCategory()
}

const handleCategoryFormSuccess = async () => {
  await loadCategoryTree()
  await getList()
}

const handleCategoryDelete = async (row: ProductCategoryVO) => {
  if (isCategoryDeleteBusy.value) {
    return
  }
  try {
    await message.delConfirm()
    categoryDeletingId.value = row.id
    await ProductCategoryApi.deleteProductCategory(row.id)
    message.success(t('common.delSuccess'))

    if (activeCategoryId.value === row.id) {
      activeCategoryId.value = undefined
      queryParams.categoryId = undefined
      queryParams.pageNo = 1
    }

    await loadCategoryTree()
    await getList()
  } catch {
  } finally {
    categoryDeletingId.value = undefined
  }
}

const handleDelete = async (id: number) => {
  if (isProductDeleteBusy.value) {
    return
  }
  try {
    await message.delConfirm()
    productDeletingId.value = id
    await ProductApi.deleteProduct(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    productDeletingId.value = undefined
  }
}

const handleExport = async () => {
  if (!canExportProducts.value) {
    return
  }
  try {
    await message.exportConfirm()
    productExporting.value = true
    const data = await ProductApi.exportProduct(queryParams)
    download.excel(data, '产品工作台.xls')
  } catch {
  } finally {
    productExporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategoryTree(), getList()])
})
</script>

<style scoped lang="scss">
.product-workbench {
  --workbench-accent: var(--el-color-primary);
  --workbench-accent-soft: color-mix(in srgb, var(--el-color-primary) 12%, white);
  --workbench-border: rgba(15, 23, 42, 0.08);
  --workbench-panel: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.96));
}

.category-workspace,
.product-panel {
  min-height: calc(100vh - 220px);
  background:
    radial-gradient(circle at top right, rgba(64, 158, 255, 0.08), transparent 34%),
    var(--workbench-panel);
  border: 1px solid var(--workbench-border);
}

.workspace-caption {
  margin: 0 0 4px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--el-text-color-secondary);
}

.workspace-title,
.panel-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.workspace-hint,
.panel-meta {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.workspace-header,
.panel-header,
.workspace-toolbar {
  display: flex;
  gap: 16px;
  justify-content: space-between;
}

.workspace-header,
.panel-header {
  align-items: flex-start;
  margin-bottom: 18px;
}

.workspace-header-actions,
.panel-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.workspace-toolbar {
  margin-bottom: 14px;
  align-items: center;
}

.workspace-search {
  flex: 1;
}

.workspace-active {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid rgba(64, 158, 255, 0.12);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08), rgba(255, 255, 255, 0.95));
}

.workspace-active-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.workspace-active-label,
.workspace-count,
.panel-total,
.summary-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.workspace-tree {
  min-height: 540px;
}

.tree-scrollbar {
  height: 540px;
  padding-right: 4px;
}

.tree-node {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.tree-node-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.tree-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.tree-node-code {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.tree-node-tools {
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.panel-summary {
  display: flex;
  gap: 12px;
}

.summary-card {
  min-width: 92px;
  padding: 14px 16px;
  border-radius: 18px;
  text-align: center;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(64, 158, 255, 0.04));
  color: var(--workbench-accent);
}

.summary-card strong {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  line-height: 1;
}

.query-form {
  margin-bottom: 10px;
}

.query-row {
  width: 100%;
}

.query-form-item {
  margin-bottom: 0;
}

.query-input {
  width: 100%;
}

.query-form-actions :deep(.el-form-item__content) {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.panel-toolbar {
  margin-bottom: 18px;
  justify-content: flex-end;
}

.panel-pagination :deep(.el-pagination) {
  float: none;
  width: 100%;
  justify-content: flex-end;
}

:deep(.category-tree .el-tree-node__content) {
  height: 40px;
  border-radius: 12px;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
}

:deep(.category-tree .el-tree-node__content:hover) {
  transform: translateX(2px);
}

:deep(.category-tree .el-tree-node__content:hover .tree-node-tools),
:deep(.category-tree .el-tree-node.is-current > .el-tree-node__content .tree-node-tools) {
  opacity: 1;
  pointer-events: auto;
}

:deep(.category-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: var(--workbench-accent-soft);
}

@media (max-width: 1200px) {
  .workspace-header,
  .panel-header,
  .workspace-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .workspace-active {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-toolbar {
    justify-content: flex-start;
  }

  .query-form-actions :deep(.el-form-item__content) {
    justify-content: flex-start;
  }
}

@media (max-width: 1024px) {
  .workspace-tree,
  .tree-scrollbar {
    min-height: 420px;
    height: 420px;
  }

  .panel-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .panel-pagination :deep(.el-pagination) {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .product-workbench {
    --el-content-wrap-padding: 16px;
  }

  .workspace-header-actions,
  .workspace-toolbar,
  .panel-toolbar {
    width: 100%;
  }

  .workspace-search,
  .workspace-toolbar :deep(.el-button),
  .panel-toolbar :deep(.el-button) {
    width: 100%;
  }

  .summary-card {
    width: 100%;
  }

  .query-form {
    margin-bottom: 16px;
  }

  .query-form-actions :deep(.el-form-item__content) {
    width: 100%;
  }

  .query-form-actions :deep(.el-button) {
    flex: 1 1 calc(50% - 6px);
    min-width: 0;
  }

  .panel-pagination :deep(.el-pagination) {
    justify-content: flex-start;
  }
}
</style>
