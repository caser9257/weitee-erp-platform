<template>
  <div class="product-workbench">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="5" :xl="4">
        <ContentWrap class="category-workspace">
          <div class="workspace-header">
            <div>
              <h3 class="workspace-title">{{ PAGE_COPY.categoryTreeTitle }}</h3>
            </div>
            <div class="workspace-header-actions">
              <el-button
                link
                type="primary"
                :disabled="!canSelectAllProducts"
                @click="handleSelectAll"
              >
                {{ PAGE_COPY.allProducts }}
              </el-button>
              <el-tooltip :content="isTreeExpanded ? PAGE_COPY.collapseTree : PAGE_COPY.expandTree">
                <el-button
                  circle
                  plain
                  :disabled="!canToggleTreeExpanded"
                  @click="toggleTreeExpanded"
                >
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

          <div v-loading="categoryTreeLoading" class="workspace-tree">
            <template v-if="categoryLoadError">
              <el-result icon="error" :title="PAGE_COPY.categoryLoadFailed">
                <template #extra>
                  <el-button
                    type="primary"
                    :disabled="categoryTreeLoading"
                    @click="loadCategoryTree"
                  >
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
                      <div
                        class="tree-node-main"
                        :title="data.code ? `${data.name}（${data.code}）` : data.name"
                      >
                        <span class="tree-node-label">
                          {{ data.code ? `${data.code} ${data.name}` : data.name }}
                        </span>
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

      <el-col :xs="24" :lg="19" :xl="20">
        <ContentWrap class="product-panel">
          <div class="panel-header">
            <div>
              <h3 class="panel-title">{{ PAGE_COPY.productListTitle }}</h3>
            </div>
          </div>

          <el-form ref="queryFormRef" :model="queryParams" label-width="90px" class="query-form">
            <el-row :gutter="16" class="query-row">
              <el-col :xs="24" :sm="14" :lg="10" :xl="8">
                <el-form-item
                  :label="PAGE_COPY.productNameLabel"
                  prop="name"
                  class="query-form-item"
                >
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
            <el-button
              type="primary"
              @click="openForm('create')"
              v-hasPermi="['erp:product:create']"
            >
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
            <el-result icon="error" :title="PAGE_COPY.productLoadFailed">
              <template #extra>
                <el-button type="primary" :disabled="productListLoading" @click="getList">
                  {{ PAGE_COPY.reload }}
                </el-button>
              </template>
            </el-result>
          </template>
          <template v-else>
            <div class="product-table-wrap">
              <el-table
                v-loading="productListLoading"
                :data="list"
                :stripe="true"
                :show-overflow-tooltip="true"
                class="product-table"
                @expand-change="handleExpand"
              >
                <el-table-column type="selection" width="32" />
                <el-table-column :label="PAGE_COPY.productInfoLabel" align="left" min-width="120">
                  <template #default="{ row }">
                    <div class="product-cell">
                      <div class="product-cell-primary">{{ row.name || '-' }}</div>
                      <div class="product-cell-secondary">
                        {{ row.materialCode || row.barCode || row.id }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column :label="PAGE_COPY.specCategoryLabel" align="left" min-width="100">
                  <template #default="{ row }">
                    <div class="product-cell">
                      <div class="product-cell-primary">{{ row.standard || '-' }}</div>
                      <div class="product-cell-secondary">{{ row.categoryName || '-' }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.productUnitLabel"
                  align="center"
                  prop="unitName"
                  min-width="46"
                />
                <el-table-column
                  :label="PAGE_COPY.approvalStatusLabel"
                  align="center"
                  prop="status"
                  min-width="60"
                >
                  <template #default="{ row }">
                    <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
                  </template>
                </el-table-column>
                <el-table-column label="审核状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag
                      :type="({0:'info',10:'warning',20:'success',30:'danger',60:'danger'}[row.auditStatus] || 'info') as any"
                      size="small"
                      effect="light"
                    >
                      {{ ({0:'草稿',10:'审批中',20:'已审批',30:'已驳回',60:'失败'}[row.auditStatus] || row.auditStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.packagingLabel"
                  align="center"
                  prop="packaging"
                  min-width="68"
                >
                  <template #default="{ row }">{{ row.packaging || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.qualityGradeLabel"
                  align="center"
                  prop="qualityGrade"
                  min-width="60"
                >
                  <template #default="{ row }">{{ row.qualityGrade || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.brandManufacturerLabel"
                  align="center"
                  prop="brandManufacturer"
                  min-width="84"
                >
                  <template #default="{ row }">{{ row.brandManufacturer || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.alternativeModelLabel"
                  align="center"
                  prop="alternativeModel"
                  min-width="76"
                >
                  <template #default="{ row }">{{ row.alternativeModel || '-' }}</template>
                </el-table-column>
                <el-table-column label="替代料" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag
                      v-if="hasSubstituteMap[row.id]"
                      size="small"
                      type="warning"
                      effect="plain"
                    >
                      有替代料
                    </el-tag>
                    <span v-else class="text-11px text-slate-300">—</span>
                  </template>
                </el-table-column>
                <el-table-column type="expand" width="50">
                  <template #default="{ row }">
                    <div v-loading="subLoading[row.id]" class="px-16px py-12px bg-slate-50">
                      <div v-if="substituteMap[row.id]?.length" class="space-y-8px">
                        <div
                          v-for="sub in substituteMap[row.id]"
                          :key="sub.id"
                          class="flex items-center gap-10px rounded border border-slate-100 bg-white px-12px py-8px text-12px"
                        >
                          <span class="font-medium text-slate-700">{{ sub.substituteProductName }}</span>
                          <span class="font-mono text-slate-400">{{ sub.substituteMaterialCode || sub.substituteProductId }}</span>
                          <el-tag size="small" effect="plain">优先级 {{ sub.priority }}</el-tag>
                          <span class="text-slate-500">替换比 {{ sub.replaceRatio }}</span>
                          <span v-if="sub.remark" class="text-slate-400">{{ sub.remark }}</span>
                        </div>
                      </div>
                      <div v-else class="py-8px text-center text-12px text-slate-400">暂无替代料（展开可查看）</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.actionsLabel"
                  align="center"
                  width="220"
                  fixed="right"
                >
                  <template #default="{ row }">
                    <div class="product-actions">
                      <el-button
                        link
                        type="primary"
                        :disabled="isProductActionBusy"
                        @click="openDetail(row)"
                        v-hasPermi="['erp:product:query']"
                      >
                        {{ PAGE_COPY.detail }}
                      </el-button>
                      <el-button
                        link
                        type="primary"
                        :disabled="isProductActionBusy"
                        @click="openForm('update', row.id)"
                        v-hasPermi="['erp:product:update']"
                      >
                        {{ PAGE_COPY.modify }}
                      </el-button>
                      <el-button
                        v-if="[0,30,60].includes(row.auditStatus ?? 0) && !row.processInstanceId"
                        link
                        type="primary"
                        :disabled="isProductActionBusy"
                        @click="handleSubmitAudit(row)"
                        v-hasPermi="['erp:product:submit']"
                      >
                        提交审核
                      </el-button>
                      <el-button
                        v-if="row.auditStatus === 10 && row.processInstanceId"
                        link
                        type="warning"
                        :disabled="isProductActionBusy"
                        @click="handleCancelAudit(row)"
                        v-hasPermi="['erp:product:cancel']"
                      >
                        撤回
                      </el-button>
                      <el-button
                        link
                        type="warning"
                        :loading="productStatusUpdatingId === row.id"
                        :disabled="isProductActionBusy"
                        @click="handleStatusChange(row)"
                        v-hasPermi="['erp:product:update']"
                      >
                        {{
                          row.status === CommonStatusEnum.ENABLE
                            ? PAGE_COPY.disable
                            : PAGE_COPY.enable
                        }}
                      </el-button>
                      <el-button
                        link
                        type="danger"
                        :loading="productDeletingId === row.id"
                        :disabled="isProductActionBusy"
                        @click="handleDelete(row.id)"
                        v-hasPermi="['erp:product:delete']"
                      >
                        {{ PAGE_COPY.delete }}
                      </el-button>
                    </div>
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
            </div>

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
    <ProductDetailDrawer ref="detailDrawerRef" @refresh="getList" @edit="handleDrawerEdit" />
    <ProductImportForm ref="importFormRef" @success="handleImportSuccess" />
    <ProductCategoryForm ref="categoryFormRef" @success="handleCategoryFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import ProductForm from './ProductForm.vue'
import ProductDetailDrawer from './ProductDetailDrawer.vue'
import ProductImportForm from './ProductImportForm.vue'
import ProductCategoryForm from '../category/ProductCategoryForm.vue'
import { DICT_TYPE } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { defaultProps, handleTree } from '@/utils/tree'

defineOptions({ name: 'ErpProductWorkbench' })

type CategoryTreeNode = ProductCategoryVO & {
  children?: CategoryTreeNode[]
}

const PAGE_COPY = {
  categoryTreeTitle: '产品分类',
  allProducts: '所有分类',
  collapseTree: '收起分类树',
  expandTree: '展开分类树',
  categorySearchPlaceholder: '按分类名称或编码搜索',
  addRootCategory: '新增一级分类',
  categoryLoadFailed: '分类加载失败',
  reload: '重新加载',
  addChildCategory: '新增子分类',
  editCategory: '编辑分类',
  deleteCategory: '删除分类',
  noCategoryData: '暂无分类数据',
  productListTitle: '产品列表',
  productInfoLabel: '产品信息',
  specCategoryLabel: '规格/分类',
  productNameLabel: '产品名称',
  productNamePlaceholder: '请输入产品名称',
  search: '搜索',
  reset: '重置',
  addProduct: '新增产品',
  importProduct: '导入产品',
  exportProduct: '导出产品',
  productLoadFailed: '产品列表加载失败',
  categoryLabel: '产品分类',
  productUnitLabel: '产品单位',
  approvalStatusLabel: '审批状态',
  packagingLabel: '产品封装',
  qualityGradeLabel: '质量等级',
  brandManufacturerLabel: '品牌/制造商',
  alternativeModelLabel: '替代型号',
  actionsLabel: '操作',
  detail: '详情',
  modify: '修改',
  disable: '停用',
  enable: '启用',
  delete: '删除',
  emptyProductList: '当前筛选条件下暂无产品',
  resetFilters: '重置筛选'
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
const productStatusUpdatingId = ref<number>()

const hasSubstituteMap = ref<Record<number, boolean>>({})
const substituteMap = ref<Record<number, any[]>>({})
const subLoading = ref<Record<number, boolean>>({})

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
const detailDrawerRef = ref()
const importFormRef = ref()
const categoryFormRef = ref()

const categoryList = ref<CategoryTreeNode[]>([])
const activeCategoryId = ref<number>()
const categoryKeyword = ref('')
const isTreeExpanded = ref(true)
const categoryTreeRenderKey = ref(0)

const hasCategoryId = (nodes: CategoryTreeNode[], id: number): boolean => {
  return nodes.some((node) => node.id === id || hasCategoryId(node.children || [], id))
}

const isCategoryDeleteBusy = computed(() => categoryDeletingId.value !== undefined)
const isProductDeleteBusy = computed(() => productDeletingId.value !== undefined)
const isProductStatusBusy = computed(() => productStatusUpdatingId.value !== undefined)
const isProductActionBusy = computed(() => isProductDeleteBusy.value || isProductStatusBusy.value)
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
    await loadHasSubstituteMap()
  } catch {
    productLoadError.value = true
  } finally {
    productListLoading.value = false
  }
}

const loadHasSubstituteMap = async () => {
  if (!list.value.length) {
    hasSubstituteMap.value = {}
    return
  }
  const ids = list.value.map((p) => p.id)
  try {
    const map: any = await ProductApi.getHasSubstituteMap(ids as any)
    hasSubstituteMap.value = map || {}
  } catch {
    hasSubstituteMap.value = {}
  }
}

const handleExpand = async (row: ProductVO, expandedRows: ProductVO[]) => {
  const expanded = expandedRows.find((r) => r.id === row.id)
  if (!expanded) return
  if (substituteMap.value[row.id]) return
  subLoading.value[row.id] = true
  try {
    const subs: any = await ProductApi.getSubstituteList(row.id)
    substituteMap.value[row.id] = subs || []
    if (subs?.length) hasSubstituteMap.value[row.id] = true
  } finally {
    subLoading.value[row.id] = false
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

const openDetail = (row: ProductVO) => {
  detailDrawerRef.value?.open(row)
}

const handleDrawerEdit = (id: number) => {
  openForm('update', id)
}

const handleImport = () => {
  importFormRef.value?.open()
}

const handleImportSuccess = async (data?: { successCategoryIds?: number[] }) => {
  const categoryIds = data?.successCategoryIds ?? []
  if (categoryIds.length === 1) {
    activeCategoryId.value = categoryIds[0]
    queryParams.categoryId = categoryIds[0]
  } else {
    activeCategoryId.value = undefined
    queryParams.categoryId = undefined
  }
  queryParams.pageNo = 1
  await syncCurrentCategory()
  await getList()
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

const handleStatusChange = async (row: ProductVO) => {
  if (isProductActionBusy.value) {
    return
  }
  let actionText = row.status === CommonStatusEnum.ENABLE ? PAGE_COPY.disable : PAGE_COPY.enable
  productStatusUpdatingId.value = row.id
  try {
    const latestProduct = await ProductApi.getProduct(row.id)
    const isEnabled = latestProduct.status === CommonStatusEnum.ENABLE
    actionText = isEnabled ? PAGE_COPY.disable : PAGE_COPY.enable
    await message.confirm(`确认${actionText}产品“${row.name}”吗？`)
    const nextStatus =
      latestProduct.status === CommonStatusEnum.ENABLE
        ? CommonStatusEnum.DISABLE
        : CommonStatusEnum.ENABLE
    await ProductApi.updateProduct({ ...latestProduct, status: nextStatus })
    message.success(`${actionText}成功`)
    await getList()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      message.error(error?.msg || error?.message || `${actionText}失败`)
    }
  } finally {
    productStatusUpdatingId.value = undefined
  }
}

const handleDelete = async (id: number) => {
  if (isProductActionBusy.value) {
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

const handleSubmitAudit = async (row: ProductVO) => {
  try {
    await message.confirm(`确认提交物料“${row.name}”进行审核吗？`)
    await ProductApi.submitProduct(row.id)
    message.success('提交请求已发送，列表将刷新校验状态')
    await getList()
    const updated: any = list.value.find((p: any) => p.id === row.id)
    if (updated?.auditStatus === 10 && updated?.processInstanceId) {
      message.success('已提交审核，等待流程受理')
    } else if (updated?.auditStatus === 60) {
      message.error('提交已受理但流程创建失败，请重试')
    }
  } catch {}
}

const handleCancelAudit = async (row: ProductVO) => {
  try {
    await message.confirm(`确认撤回物料“${row.name}”的审核吗？`)
    await ProductApi.cancelProduct(row.id)
    message.success('撤回成功')
    await getList()
  } catch {}
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
  --workbench-accent-soft: var(--erp-primary-50);
  --workbench-border: var(--erp-slate-200);
  --workbench-panel: var(--erp-surface-white);
}

.category-workspace,
.product-panel {
  min-height: calc(100vh - 220px);
  background: var(--workbench-panel);
  border: 1px solid var(--workbench-border);
  box-shadow: var(--erp-shadow-sm);
}

.workspace-title,
.panel-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
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
  margin-bottom: 14px;
}

.workspace-header-actions,
.panel-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.workspace-toolbar {
  margin-bottom: 10px;
  align-items: center;
}

.workspace-search {
  flex: 1;
}

.workspace-tree {
  min-height: 480px;
}

.tree-scrollbar {
  height: 480px;
  padding-right: 4px;
}

.tree-node {
  position: relative;
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.tree-node-main {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
}

.tree-node-label {
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  font-weight: 400;
  color: var(--erp-slate-700);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-node-code {
  display: none;
}

.tree-node-tools {
  position: absolute;
  top: 50%;
  right: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  pointer-events: none;
  opacity: 0;
  transform: translateY(-50%);
  transition: opacity 0.2s ease;
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
  margin-bottom: 14px;
  justify-content: flex-end;
}

.product-table-wrap {
  overflow-x: auto;
}

.product-table {
  width: 100%;
  min-width: 0;
}

.product-table :deep(.cell) {
  padding-right: 4px;
  padding-left: 4px;
}

.product-cell {
  min-width: 0;
  line-height: 1.35;
}

.product-cell-primary {
  overflow: hidden;
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-cell-secondary {
  margin-top: 2px;
  overflow: hidden;
  font-size: 11px;
  color: var(--erp-slate-500);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-pagination :deep(.el-pagination) {
  float: none;
  width: 100%;
  justify-content: flex-end;
}

:deep(.category-tree .el-tree-node__content) {
  position: relative;
  height: 32px;
  border-radius: 8px;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
}

:deep(.category-tree .el-tree-node__content:hover) {
  transform: none;
}

:deep(.category-tree .el-tree-node__expand-icon) {
  width: 12px;
  height: 12px;
  padding: 0;
  font-size: 0;
  color: transparent;
  border: 1px solid var(--erp-slate-400);
  border-radius: 0;
  transform: none;
}

:deep(.category-tree .el-tree-node__expand-icon::before) {
  display: block;
  font-size: 12px;
  line-height: 10px;
  color: var(--erp-slate-600);
  text-align: center;
  content: '+';
}

:deep(.category-tree .el-tree-node__expand-icon.expanded::before) {
  content: '-';
}

:deep(.category-tree .el-tree-node__expand-icon.is-leaf) {
  border-color: transparent;
  visibility: hidden;
}

:deep(.category-tree .el-tree-node__children) {
  position: relative;
  padding-left: 12px;
  margin-left: 8px;
  border-left: 1px dotted var(--erp-slate-400);
}

:deep(.category-tree .el-tree-node__children .el-tree-node__content::before) {
  position: absolute;
  top: 50%;
  left: -13px;
  width: 12px;
  border-top: 1px dotted var(--erp-slate-400);
  content: '';
}

:deep(.category-tree .el-tree-node__content:hover .tree-node-tools),
:deep(.category-tree .el-tree-node.is-current > .el-tree-node__content .tree-node-tools) {
  pointer-events: auto;
  opacity: 1;
}

:deep(.category-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: var(--workbench-accent-soft);
}

@media (width <= 1200px) {
  .workspace-header,
  .panel-header,
  .workspace-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .panel-toolbar {
    justify-content: flex-start;
  }

  .query-form-actions :deep(.el-form-item__content) {
    justify-content: flex-start;
  }
}

@media (width <= 1024px) {
  .workspace-tree,
  .tree-scrollbar {
    height: 400px;
    min-height: 400px;
  }

  .panel-pagination :deep(.el-pagination) {
    justify-content: flex-start;
  }
}

@media (width <= 768px) {
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
