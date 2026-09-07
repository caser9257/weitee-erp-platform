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
              <el-col :xs="24" :sm="12" :lg="7" :xl="6">
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
              <el-col :xs="24" :sm="12" :lg="7" :xl="6">
                <el-form-item label="物料编号" prop="materialCode" class="query-form-item">
                  <el-input
                    v-model="queryParams.materialCode"
                    placeholder="请输入物料编号"
                    clearable
                    class="query-input"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :lg="10" :xl="12">
                <el-form-item class="query-form-item query-form-actions">
                  <div class="query-actions">
                    <el-button :disabled="!canQueryProducts" @click="handleQuery">
                      <Icon icon="ep:search" class="mr-5px" />
                      {{ PAGE_COPY.search }}
                    </el-button>
                    <el-button :disabled="!canResetQuery" @click="resetQuery">
                      <Icon icon="ep:refresh" class="mr-5px" />
                      {{ PAGE_COPY.reset }}
                    </el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="primary" @click="advancedSearchVisible = !advancedSearchVisible">
                      {{ advancedSearchVisible ? '收起高级搜索' : '高级搜索' }}
                      <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-3px" />
                    </el-button>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-show="advancedSearchVisible" :gutter="16" class="query-row advanced-row">
              <el-col :xs="24" :sm="12" :lg="6" :xl="6">
                <el-form-item label="产品型号" prop="standard" class="query-form-item">
                  <el-input
                    v-model="queryParams.standard"
                    placeholder="请输入产品型号"
                    clearable
                    class="query-input"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6" :xl="6">
                <el-form-item label="品牌/制造商" prop="brandManufacturer" class="query-form-item">
                  <el-input
                    v-model="queryParams.brandManufacturer"
                    placeholder="请输入品牌/制造商"
                    clearable
                    class="query-input"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6" :xl="6">
                <el-form-item label="审批状态" prop="auditStatus" class="query-form-item">
                  <el-select
                    v-model="queryParams.auditStatus"
                    placeholder="全部"
                    clearable
                    class="query-input"
                  >
                    <el-option label="草稿" :value="0" />
                    <el-option label="审批中" :value="10" />
                    <el-option label="已审批" :value="20" />
                    <el-option label="已驳回" :value="30" />
                    <el-option label="处理失败" :value="60" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6" :xl="6">
                <el-form-item label="PCB 元器件" prop="pcbComponent" class="query-form-item">
                  <el-select
                    v-model="queryParams.pcbComponent"
                    placeholder="全部"
                    clearable
                    class="query-input"
                  >
                    <el-option label="是" :value="true" />
                    <el-option label="否" :value="false" />
                  </el-select>
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
                :border="true"
                class="product-table"
                @header-dragend="handleHeaderDragend"
              >
                <el-table-column type="expand" width="36" :resizable="false">
                  <template #default="{ row }">
                    <ProductSubstituteExpand :product-id="row.id" />
                  </template>
                </el-table-column>
                <el-table-column type="selection" width="32" :resizable="false" />
                <el-table-column label="产品名称" prop="name" min-width="140" show-overflow-tooltip />
                <el-table-column label="产品编号" min-width="130">
                  <template #default="{ row }">
                    <span class="font-mono text-slate-600">{{
                      formatMaterialCode(row.materialCode, row.prevMaterialCode)
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="产品型号" prop="standard" min-width="100" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.standard || '-' }}</template>
                </el-table-column>
                <el-table-column label="产品分类" prop="categoryName" min-width="100" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.categoryName || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.productUnitLabel"
                  align="center"
                  prop="unitName"
                  min-width="70"
                />
                <el-table-column
                  :label="PAGE_COPY.approvalStatusLabel"
                  align="center"
                  prop="status"
                  min-width="80"
                >
                  <template #default="{ row }">
                    <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
                  </template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.packagingLabel"
                  align="center"
                  prop="packaging"
                  min-width="80"
                >
                  <template #default="{ row }">{{ row.packaging || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.qualityGradeLabel"
                  align="center"
                  prop="qualityGrade"
                  min-width="80"
                >
                  <template #default="{ row }">{{ row.qualityGrade || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.brandManufacturerLabel"
                  align="center"
                  prop="brandManufacturer"
                  min-width="100"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">{{ row.brandManufacturer || '-' }}</template>
                </el-table-column>
                <el-table-column
                  :label="PAGE_COPY.alternativeModelLabel"
                  align="center"
                  prop="alternativeModel"
                  min-width="100"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">{{ row.alternativeModel || '-' }}</template>
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
                      <!-- 修改：仅未生效物料（草稿/驳回/失败）与两段式编辑中（保存暂存）可见 -->
                      <el-button
                        v-if="[0, 30, 60, 22].includes(row.auditStatus ?? -1)"
                        link
                        type="primary"
                        :disabled="isProductActionBusy"
                        @click="openForm('update', row.id)"
                        v-hasPermi="['erp:product:update']"
                      >
                        {{ PAGE_COPY.modify }}
                      </el-button>
                      <!-- 变更/废除：仅已生效物料可见（修改语义统一收敛到两段式变更） -->
                      <el-button
                        v-if="row.auditStatus === 20"
                        link
                        type="warning"
                        :disabled="isProductActionBusy"
                        @click="handleStartChange(row)"
                        v-hasPermi="['erp:product:update']"
                      >
                        变更
                      </el-button>
                      <el-button
                        v-if="row.auditStatus === 20"
                        link
                        type="danger"
                        :disabled="isProductActionBusy"
                        @click="handleStartObsolete(row)"
                        v-hasPermi="['erp:product:update']"
                      >
                        废除
                      </el-button>
                      <!-- 提交审批：变更编辑中（保存与提交分离，修改完成后手动送审） -->
                      <el-button
                        v-if="row.auditStatus === 22"
                        link
                        type="warning"
                        :disabled="isProductActionBusy"
                        @click="handleSubmitChangeConfirm(row)"
                        v-hasPermi="['erp:product:update']"
                      >
                        提交审批
                      </el-button>
                      <!-- 启停：已生效物料走启停审批（提交理由，审批通过即切换状态） -->
                      <el-button
                        v-if="row.auditStatus === 20"
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
                        v-if="
                          (row.auditStatus === 10 ||
                            [21, 23, 24, 26].includes(row.auditStatus ?? -1)) &&
                          row.processInstanceId
                        "
                        link
                        type="warning"
                        :disabled="isProductActionBusy"
                        @click="handleCancelAudit(row)"
                        v-hasPermi="['erp:product:cancel']"
                      >
                        撤回
                      </el-button>
                      <!-- 启停：仅未生效物料直改；已生效物料启停统一走「变更」两段式 -->
                      <el-button
                        v-if="[0, 30, 60].includes(row.auditStatus ?? -1)"
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
                      <!-- 删除：仅未生效物料（草稿/驳回/失败）可见，用于清理误建档；
                           已生效物料的退出路径只有停用（冻结）/废除（销号留痕），均走两段式审批 -->
                      <el-button
                        v-if="[0, 30, 60].includes(row.auditStatus ?? -1)"
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
import { formatMaterialCode } from '@/utils/erp/materialCode'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import ProductForm from './ProductForm.vue'
import ProductDetailDrawer from './ProductDetailDrawer.vue'
import ProductImportForm from './ProductImportForm.vue'
import ProductSubstituteExpand from './ProductSubstituteExpand.vue'
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
  approvalStatusLabel: '启用状态',
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

const list = ref<ProductVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  materialCode: undefined as string | undefined,
  standard: undefined as string | undefined,
  brandManufacturer: undefined as string | undefined,
  auditStatus: undefined as number | undefined,
  pcbComponent: undefined as boolean | undefined,
  categoryId: undefined as number | undefined
})
const advancedSearchVisible = ref(false)

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
  } catch {
    productLoadError.value = true
  } finally {
    productListLoading.value = false
  }
}

const handleHeaderDragend = (_newWidth: number, _oldWidth: number, _column: any) => {
  // 列宽调整完成后的回调，可用于保存用户偏好
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
  const isApproved = row.auditStatus === 20
  productStatusUpdatingId.value = row.id
  try {
    const latestProduct = await ProductApi.getProduct(row.id)
    const isEnabled = latestProduct.status === CommonStatusEnum.ENABLE
    const actionText = isEnabled ? PAGE_COPY.disable : PAGE_COPY.enable
    const nextStatus =
      isEnabled ? CommonStatusEnum.DISABLE : CommonStatusEnum.ENABLE
    if (isApproved) {
      // 已生效物料：启停走一段式审批（提交理由，审批通过即切换状态）
      const { value } = await message.prompt(`确认${actionText}该产品吗？`, '填写启停理由')
      if (!value || !String(value).trim()) {
        message.warning('请填写启停理由')
        return
      }
      await ProductApi.submitStatusChange(row.id, nextStatus, String(value).trim())
      await getList()
      const updated: any = list.value.find((p: any) => p.id === row.id)
      if (updated?.auditStatus === 28 && updated?.processInstanceId) {
        message.success('启停审批已提交，等待审批')
      } else {
        message.error('提交已受理但流程创建失败，请重试')
      }
      return
    }
    // 未生效物料：直接切换
    await message.confirm(`确认${actionText}该产品吗？`)
    await ProductApi.updateProduct({ ...latestProduct, status: nextStatus })
    message.success(t('common.updateSuccess'))
    await getList()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      message.error(error?.msg || error?.message || '操作失败')
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
    await message.confirm('确认提交审核吗？')
    await ProductApi.submitProduct(row.id)
    // 不在此处弹受理提示：以刷新后的真实状态为准，由下方分支统一提示，避免双胶囊
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
    // 撤回原因后端必填：弹窗强制填写（新建审核/单条修改审批/两段式审批共用此入口，后端按在途状态自动分流）
    const isTwoStage = [21, 23, 24, 26].includes(row.auditStatus ?? -1)
    const batchTip = row.pendingBatchId
      ? `该物料属于批量审批批次（共 ${row.pendingBatchSize ?? '?'} 条），撤回将作废整批变更。`
      : ''
    const { value } = await message.prompt(
      `确认撤回该物料的审批吗？${batchTip}`,
      '填写撤回原因'
    )
    if (!value || !String(value).trim()) {
      message.warning('请填写撤回原因')
      return
    }
    if (isTwoStage) {
      await ProductApi.cancelTwoStageApproval(row.id, String(value).trim())
    } else {
      await ProductApi.cancelProduct(row.id, String(value).trim())
    }
    message.success('撤回成功')
    await getList()
  } catch {}
}

// ========== 两段式变更/废除（行内快捷入口，与详情抽屉同一套 API） ==========

const handleStartChange = async (row: ProductVO) => {
  try {
    await message.confirm('确认对该物料发起变更申请吗？')
    await ProductApi.submitChangeRequest(row.id)
    await getList()
    const updated: any = list.value.find((p: any) => p.id === row.id)
    if (updated?.auditStatus === 21 && updated?.processInstanceId) {
      message.success('变更申请已提交，等待审批')
    } else if (updated?.auditStatus === 20) {
      message.error('变更申请提交已受理但流程创建失败，请重试')
    }
  } catch {}
}

const handleStartObsolete = async (row: ProductVO) => {
  try {
    // 废除原因是最终留痕，弹窗强制填写
    const { value } = await message.prompt('确认对该物料发起废除申请吗？', '填写废除原因')
    if (!value || !String(value).trim()) {
      message.warning('请填写废除原因')
      return
    }
    await ProductApi.submitObsoleteRequest(row.id, String(value).trim())
    await getList()
    const updated: any = list.value.find((p: any) => p.id === row.id)
    if (updated?.auditStatus === 24 && updated?.processInstanceId) {
      message.success('废除申请已提交，等待审批')
    } else if (updated?.auditStatus === 20) {
      message.error('废除申请提交已受理但流程创建失败，请重试')
    }
  } catch {}
}

const handleSubmitChangeConfirm = async (row: ProductVO) => {
  try {
    await message.confirm('确认提交变更审批吗？')
    await ProductApi.submitChangeConfirm(row.id)
    await getList()
    const updated: any = list.value.find((p: any) => p.id === row.id)
    if (updated?.auditStatus === 23 && updated?.processInstanceId) {
      message.success('变更审批已提交，等待负责人审批')
    } else if (updated?.auditStatus === 22) {
      message.error('提交已受理但流程创建失败，请重试')
    }
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
  margin-bottom: 18px;
}

.query-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
}

.advanced-row {
  margin: 0 0 4px;
  padding: 16px 12px 2px;
  background: var(--erp-slate-50, #f8fafc);
  border: 1px solid var(--erp-slate-100, #f1f5f9);
  border-radius: 8px;
  transition: all 0.2s ease;
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

.product-table :deep(.el-table__header-wrapper .el-table__header th) {
  position: relative;
}

.product-table :deep(.el-table__header-wrapper .el-table__header th .cell) {
  font-weight: 600;
  color: var(--erp-slate-600);
}

.product-table :deep(.el-table__border-left-patch) {
  background-color: var(--erp-slate-100);
}

.product-table :deep(.el-table__border-right-patch) {
  background-color: var(--erp-slate-100);
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
