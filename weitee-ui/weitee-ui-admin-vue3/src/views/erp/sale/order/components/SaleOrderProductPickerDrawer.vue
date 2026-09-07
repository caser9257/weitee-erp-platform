<template>
  <el-drawer
    v-model="drawerVisible"
    :size="drawerSize"
    :show-close="false"
    :destroy-on-close="false"
    append-to-body
    class="sale-order-product-picker"
    @closed="handleClosed"
  >
    <template #header>
      <div class="picker-header">
        <div class="picker-header__main">
          <div class="picker-header__icon">
            <Icon icon="ep:menu" />
          </div>
          <div>
            <div class="picker-header__title">树形选品</div>
            <div class="picker-header__stats">
              <span>分类 {{ categoryCount }}</span>
              <span>商品 {{ productCount }}</span>
              <span>已选 {{ selectedProduct ? 1 : 0 }}/1</span>
            </div>
          </div>
        </div>
        <el-button circle text class="picker-header__close" @click="drawerVisible = false">
          <Icon icon="ep:close" />
        </el-button>
      </div>
    </template>

    <div class="picker-shell">
      <div class="picker-toolbar">
        <el-input
          v-model="searchKeyword"
          clearable
          placeholder="请输入产品名称、条码或规格"
          class="picker-search"
        >
          <template #prefix>
            <Icon icon="ep:search" />
          </template>
        </el-input>
        <div class="picker-toolbar__meta">
          <el-tag effect="light" type="success">已选商品</el-tag>
          <strong>{{ selectedProduct ? 1 : 0 }}</strong>
        </div>
      </div>

      <div v-loading="treeLoading" class="picker-body">
        <template v-if="treeLoadError">
          <el-result icon="error" title="树数据加载失败">
            <template #extra>
              <el-button type="primary" plain :disabled="treeLoading" @click="reloadTree">
                重试加载
              </el-button>
            </template>
          </el-result>
        </template>

        <template v-else-if="treeData.length">
          <div class="picker-grid">
            <section class="picker-panel picker-panel--tree">
              <div class="picker-panel__header">
                <div>
                  <div class="picker-panel__title">分类与产品树</div>
                </div>
              </div>

              <div v-if="displayTreeData.length" class="picker-tree-wrap">
                <el-scrollbar class="picker-tree-scrollbar">
                  <el-tree
                    :key="treeRenderKey"
                    ref="treeRef"
                    :data="displayTreeData"
                    :props="treeProps"
                    node-key="treeKey"
                    :expand-on-click-node="false"
                    :default-expanded-keys="expandedKeys"
                    highlight-current
                    class="picker-tree"
                    @node-click="handleNodeClick"
                    @node-expand="handleNodeExpand"
                  >
                    <template #default="{ data }">
                      <div class="picker-tree-node" :class="`is-${data.nodeType}`">
                        <div class="picker-tree-node__content">
                          <div class="picker-tree-node__title-row">
                            <span class="picker-tree-node__title">{{ data.label }}</span>
                            <el-tag
                              size="small"
                              effect="light"
                              :type="resolveNodeTagType(data.nodeType)"
                            >
                              {{ resolveNodeTagLabel(data.nodeType) }}
                            </el-tag>
                            <el-tag
                              v-if="data.sourceType === 'bom' && data.bomLoaded"
                              size="small"
                              effect="light"
                              type="warning"
                            >
                              BOM
                            </el-tag>
                          </div>
                          <div class="picker-tree-node__meta">
                            <span v-if="data.nodeType === 'category'">分类编码：{{ data.subLabel || '-' }}</span>
                            <template v-else>
                              <span>条码：{{ data.productBarCode || '-' }}</span>
                              <span>单位：{{ data.productUnitName || '-' }}</span>
                              <span v-if="data.standard">规格：{{ data.standard }}</span>
                            </template>
                          </div>
                        </div>
                        <Icon
                          v-if="data.nodeType !== 'category'"
                          icon="ep:arrow-right"
                          class="picker-tree-node__arrow"
                        />
                      </div>
                    </template>
                  </el-tree>
                </el-scrollbar>
              </div>

              <el-empty v-else description="暂无匹配结果">
                <el-button link type="primary" @click="clearSearch">清空搜索</el-button>
              </el-empty>
            </section>

            <aside class="picker-panel picker-panel--summary">
              <div class="picker-panel__header">
                <div>
                  <div class="picker-panel__title">已选商品</div>
                </div>
              </div>

              <template v-if="selectedProduct">
                <div class="selection-card">
                  <div class="selection-card__top">
                    <div class="selection-card__name">{{ selectedProduct.productName }}</div>
                    <el-tag size="small" effect="light" :type="resolveSourceTagType(selectedProduct.sourceType)">
                      {{ resolveSourceLabel(selectedProduct.sourceType) }}
                    </el-tag>
                  </div>

                  <div class="selection-card__grid">
                    <div class="selection-item">
                      <span class="selection-item__label">条码</span>
                      <strong>{{ selectedProduct.productBarCode || '-' }}</strong>
                    </div>
                    <div class="selection-item">
                      <span class="selection-item__label">分类</span>
                      <strong>{{ selectedProduct.categoryName || '-' }}</strong>
                    </div>
                    <div class="selection-item">
                      <span class="selection-item__label">单位</span>
                      <strong>{{ selectedProduct.productUnitName || '-' }}</strong>
                    </div>
                    <div class="selection-item">
                      <span class="selection-item__label">售价</span>
                      <strong>{{ formatPrice(selectedProduct.salePrice) }}</strong>
                    </div>
                    <div class="selection-item">
                      <span class="selection-item__label">库存</span>
                      <strong>
                        <span v-if="selectedStockLoading">加载中</span>
                        <span v-else>{{ formatCount(selectedStockCount) }}</span>
                      </strong>
                    </div>
                    <div class="selection-item">
                      <span class="selection-item__label">来源</span>
                      <strong>{{ resolveSourceLabel(selectedProduct.sourceType) }}</strong>
                    </div>
                  </div>

                  <template v-if="hasBomInfo">
                    <div class="selection-card__section-title">BOM信息</div>
                    <div class="selection-card__chips">
                      <el-tag v-if="selectedProduct.bomUsageQty != null" effect="light">
                        用量 {{ formatCount(selectedProduct.bomUsageQty) }}
                      </el-tag>
                      <el-tag v-if="selectedProduct.bomLossRate != null" effect="light">
                        损耗率 {{ selectedProduct.bomLossRate }}%
                      </el-tag>
                      <el-tag v-if="selectedProduct.bomLeadTimeDay != null" effect="light">
                        提前期 {{ selectedProduct.bomLeadTimeDay }} 天
                      </el-tag>
                    </div>
                  </template>
                </div>
              </template>

              <el-empty v-else description="请从左侧树中选择商品" />
            </aside>
          </div>
        </template>

        <template v-else>
          <el-empty description="暂无可选商品">
            <el-button type="primary" plain :disabled="treeLoading" @click="reloadTree">
              重试加载
            </el-button>
          </el-empty>
        </template>
      </div>

      <div class="picker-footer">
        <div class="picker-footer__actions">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!canConfirm"
            :loading="confirming"
            @click="handleConfirm"
          >
            确认加入订单
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { ProductCategoryApi, type ProductCategoryVO } from '@/api/erp/product/category'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import { BomApi, type BomItemVO, type BomVO } from '@/api/erp/mrp/bom'
import { StockApi } from '@/api/erp/stock/stock'
import { handleTree } from '@/utils/tree'
import { erpCountInputFormatter, erpPriceInputFormatter } from '@/utils'

defineOptions({ name: 'SaleOrderProductPickerDrawer' })

type SaleOrderItemRow = Record<string, any>
type NodeType = 'category' | 'group' | 'product'
type NodeSourceType = 'catalog' | 'bom'

interface PickerTreeNode {
  treeKey: string
  nodeType: NodeType
  label: string
  subLabel?: string
  selectable?: boolean
  isLeaf?: boolean
  sourceType?: NodeSourceType
  categoryId?: number
  categoryName?: string
  categoryCode?: string
  productId?: number
  productName?: string
  productBarCode?: string
  productUnitId?: number
  productUnitName?: string
  quantityPrecision?: number
  salePrice?: number
  standard?: string
  bomUsageQty?: number
  bomLossRate?: number
  bomLeadTimeDay?: number
  bomLoaded?: boolean
  bomLoading?: boolean
  bomLoadError?: boolean
  children?: PickerTreeNode[]
}

interface SelectionProduct {
  productId: number
  productName: string
  productBarCode?: string
  productUnitId?: number
  productUnitName?: string
  quantityPrecision?: number
  salePrice?: number
  categoryId?: number
  categoryName?: string
  sourceType?: NodeSourceType
  bomUsageQty?: number
  bomLossRate?: number
  bomLeadTimeDay?: number
  standard?: string
}

interface ConfirmPayload {
  row: SaleOrderItemRow
  product: SelectionProduct
  stockCount?: number
}

const treeProps = {
  children: 'children',
  label: 'label',
  isLeaf: 'isLeaf'
}

function unwrapApiData<T>(response: any): T {
  if (response && typeof response === 'object' && 'data' in response) {
    return response.data as T
  }
  return response as T
}

function ensureArray<T>(value: T[] | null | undefined): T[] {
  return Array.isArray(value) ? value : []
}

const drawerVisible = ref(false)
const treeLoading = ref(false)
const treeLoadError = ref(false)
const confirming = ref(false)
const selectedStockLoading = ref(false)
const selectedStockCount = ref<number | undefined>()
const searchKeyword = ref('')
const treeData = ref<PickerTreeNode[]>([])
const displayTreeData = ref<PickerTreeNode[]>([])
const selectedProduct = ref<SelectionProduct | null>(null)
const selectedRow = ref<SaleOrderItemRow | null>(null)
const selectedNodeKey = ref<string>()
const treeRenderKey = ref(0)
const expandedKeys = ref<string[]>([])
const treeRef = ref()
const currentLoadToken = ref(0)
const currentStockToken = ref(0)
const treeCacheReady = ref(false)
let treeLoadPromise: Promise<void> | null = null
const { width } = useWindowSize()

const drawerSize = computed(() => {
  if (width.value < 768) {
    return '100%'
  }
  if (width.value < 1280) {
    return '920px'
  }
  return '1080px'
})

const categoryCount = computed(() => countCategoryNodes(treeData.value))
const productCount = computed(() => countCatalogProductNodes(treeData.value))
const hasBomInfo = computed(
  () =>
    selectedProduct.value != null &&
    (selectedProduct.value.bomUsageQty != null ||
      selectedProduct.value.bomLossRate != null ||
      selectedProduct.value.bomLeadTimeDay != null)
)
const canConfirm = computed(
  () => !!selectedProduct.value && !treeLoading.value && !confirming.value && !selectedStockLoading.value
)

watch(
  () => treeData.value,
  () => {
    displayTreeData.value = filterTreeNodes(treeData.value, normalizedKeyword.value)
    syncExpandedKeys()
  },
  { deep: true }
)

const normalizedKeyword = computed(() => searchKeyword.value.trim().toLowerCase())

watch(normalizedKeyword, async () => {
  displayTreeData.value = filterTreeNodes(treeData.value, normalizedKeyword.value)
  await nextTick()
  syncCurrentNode()
})

watch(selectedNodeKey, async () => {
  await nextTick()
  syncCurrentNode()
})

const resolveNodeTagLabel = (nodeType: NodeType) => {
  if (nodeType === 'category') {
    return '分类'
  }
  if (nodeType === 'group') {
    return '分组'
  }
  return '商品'
}

const resolveNodeTagType = (nodeType: NodeType) => {
  if (nodeType === 'category') {
    return 'success'
  }
  if (nodeType === 'group') {
    return 'info'
  }
  return 'primary'
}

const resolveSourceLabel = (sourceType?: NodeSourceType) => {
  if (sourceType === 'bom') {
    return 'BOM节点'
  }
  if (sourceType === 'catalog') {
    return '基础商品'
  }
  return '当前行'
}

const resolveSourceTagType = (sourceType?: NodeSourceType) => {
  if (sourceType === 'bom') {
    return 'warning'
  }
  if (sourceType === 'catalog') {
    return 'success'
  }
  return 'info'
}

const formatPrice = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return erpPriceInputFormatter(value)
}

const formatCount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return erpCountInputFormatter(value)
}

const clearSearch = () => {
  searchKeyword.value = ''
}

const handleClosed = () => {
  currentLoadToken.value += 1
  currentStockToken.value += 1
  selectedRow.value = null
  selectedProduct.value = null
  selectedStockCount.value = undefined
  selectedStockLoading.value = false
  selectedNodeKey.value = undefined
  confirming.value = false
  searchKeyword.value = ''
}

const open = async (row: SaleOrderItemRow) => {
  selectedRow.value = row
  drawerVisible.value = true
  confirming.value = false
  selectedStockLoading.value = false
  selectedStockCount.value = row.stockCount
  selectedNodeKey.value = undefined
  selectedProduct.value = buildSelectionFromRow(row)
  searchKeyword.value = ''

  const token = ++currentLoadToken.value
  await ensureTreeData()
  if (token !== currentLoadToken.value) {
    return
  }

  if (row?.productId != null) {
    const node = findNodeByProductId(treeData.value, row.productId)
    if (node) {
      selectedProduct.value = buildSelectionFromNode(node)
      selectedNodeKey.value = node.treeKey
    }
  }

  await nextTick()
  syncCurrentNode()
}

defineExpose({ open })

const reloadTree = async () => {
  treeCacheReady.value = false
  await loadTreeData(true)
}

const ensureTreeData = async () => {
  if (treeCacheReady.value && treeData.value.length) {
    displayTreeData.value = filterTreeNodes(treeData.value, normalizedKeyword.value)
    syncExpandedKeys()
    return
  }
  await loadTreeData(false)
}

const loadTreeData = async (_force = false) => {
  if (treeLoadPromise) {
    return treeLoadPromise
  }
  treeLoading.value = true
  treeLoadError.value = false
  treeLoadPromise = (async () => {
    try {
      const [categoryRes, productRes] = await Promise.all([
        ProductCategoryApi.getProductCategorySimpleList(),
        ProductApi.getProductSimpleList()
      ])
      const categoryList = ensureArray<ProductCategoryVO>(unwrapApiData(categoryRes))
      const productList = ensureArray<ProductVO>(unwrapApiData(productRes))

      const builtTree = buildTree(categoryList || [], productList || [])
      treeData.value = builtTree
      displayTreeData.value = filterTreeNodes(builtTree, normalizedKeyword.value)
      treeCacheReady.value = true
      treeRenderKey.value += 1
      syncExpandedKeys()
    } catch {
      treeLoadError.value = true
      treeData.value = []
      displayTreeData.value = []
      treeCacheReady.value = false
    } finally {
      treeLoading.value = false
      treeLoadPromise = null
    }
  })()

  return treeLoadPromise
}

const buildTree = (categoryList: ProductCategoryVO[], productList: ProductVO[]) => {
  const productMap = new Map<number, ProductVO>()
  ensureArray(productList).forEach((product) => {
    productMap.set(product.id, product)
  })

  const productsByCategory = new Map<number, ProductVO[]>()
  ensureArray(productList).forEach((product) => {
    const categoryId = product.categoryId || 0
    const list = productsByCategory.get(categoryId) || []
    list.push(product)
    productsByCategory.set(categoryId, list)
  })

  const categoryTree = handleTree(categoryList, 'id', 'parentId') as ProductCategoryVO[]
  const categoryNodes = buildCategoryNodes(categoryTree, productsByCategory, 'category')
  const uncategorizedProducts = sortProducts(productsByCategory.get(0) || [])

  if (uncategorizedProducts.length === 0) {
    return categoryNodes
  }

  const uncategorizedNode: PickerTreeNode = {
    treeKey: 'group:uncategorized',
    nodeType: 'group',
    label: '未分类商品',
    selectable: false,
    isLeaf: false,
    children: uncategorizedProducts.map((product) => buildCatalogProductNode(product, 'group:uncategorized'))
  }

  return [...categoryNodes, uncategorizedNode]
}

const buildCategoryNodes = (
  nodes: ProductCategoryVO[],
  productsByCategory: Map<number, ProductVO[]>,
  parentKey: string
): PickerTreeNode[] => {
  return sortCategories(nodes).map((node) => {
    const currentKey = `${parentKey}:category:${node.id}`
    const childCategoryNodes = buildCategoryNodes(
      (node as ProductCategoryVO & { children?: ProductCategoryVO[] }).children || [],
      productsByCategory,
      currentKey
    )
    const childProducts = sortProducts(productsByCategory.get(node.id) || []).map((product) =>
      buildCatalogProductNode(product, currentKey)
    )
    const children = [...childCategoryNodes, ...childProducts]

    return {
      treeKey: currentKey,
      nodeType: 'category',
      label: node.name,
      subLabel: node.code,
      categoryId: node.id,
      categoryName: node.name,
      categoryCode: node.code,
      selectable: false,
      isLeaf: children.length === 0,
      children
    }
  })
}

const buildCatalogProductNode = (product: ProductVO, parentKey: string): PickerTreeNode => {
  return {
    treeKey: `${parentKey}:product:${product.id}`,
    nodeType: 'product',
    sourceType: 'catalog',
    label: product.name,
    subLabel: product.barCode,
    selectable: true,
    isLeaf: false,
    productId: product.id,
    productName: product.name,
    productBarCode: product.barCode,
    productUnitId: product.unitId,
    productUnitName: product.unitName,
    quantityPrecision: product.quantityPrecision,
    salePrice: product.salePrice,
    categoryId: product.categoryId,
    categoryName: product.categoryName,
    standard: product.standard,
    children: undefined
  }
}

const buildBomProductNode = (
  item: BomItemVO,
  parentKey: string,
  index: number,
  productMap: Map<number, ProductVO>
): PickerTreeNode => {
  const product = productMap.get(item.materialId)
  const selectable = !!product

  return {
    treeKey: `${parentKey}:bom:${item.id || item.materialId || index}`,
    nodeType: 'product',
    sourceType: 'bom',
    label: product?.name || item.materialName || `物料 ${item.materialId}`,
    subLabel: product?.barCode || item.materialName,
    selectable,
    isLeaf: false,
    productId: item.materialId,
    productName: product?.name || item.materialName || `物料 ${item.materialId}`,
    productBarCode: product?.barCode,
    productUnitId: product?.unitId,
    productUnitName: product?.unitName || item.unitName,
    quantityPrecision: product?.quantityPrecision,
    salePrice: product?.salePrice,
    categoryId: product?.categoryId,
    categoryName: product?.categoryName,
    standard: product?.standard,
    bomUsageQty: item.usageQty,
    bomLossRate: item.lossRate,
    bomLeadTimeDay: item.leadTimeDay,
    children: undefined
  }
}

const handleNodeClick = (data: PickerTreeNode) => {
  if (data.nodeType !== 'product') {
    return
  }
  if (!data.selectable) {
    return
  }
  selectProduct(data)
}

const handleNodeExpand = async (data: PickerTreeNode) => {
  if (data.nodeType !== 'product' || data.bomLoaded || data.bomLoading) {
    return
  }

  data.bomLoading = true
  data.bomLoadError = false
  try {
    const bomRes = await BomApi.getBomTree(data.productId!)
    const bom = unwrapApiData<BomVO>(bomRes)
    const productMap = buildProductMap()
    const childNodes = buildBomChildren(data.treeKey, bom, productMap)
    data.children = childNodes
    data.isLeaf = childNodes.length === 0
    data.bomLoaded = true
  } catch {
    data.bomLoadError = true
  } finally {
    data.bomLoading = false
  }
}

const buildBomChildren = (parentKey: string, bom: BomVO, productMap: Map<number, ProductVO>) => {
  const items = ensureArray(bom?.items)
  return items.map((item, index) => buildBomProductNode(item, parentKey, index, productMap))
}

const selectProduct = async (node: PickerTreeNode) => {
  if (!selectedRow.value) {
    return
  }

  selectedProduct.value = buildSelectionFromNode(node)
  selectedNodeKey.value = node.treeKey
  selectedStockLoading.value = true
  selectedStockCount.value = undefined

  const token = ++currentStockToken.value
  try {
    const countRes = await StockApi.getStockCount(node.productId!)
    const count = unwrapApiData<number>(countRes)
    if (token !== currentStockToken.value) {
      return
    }
    selectedStockCount.value = Number(count ?? 0)
  } catch {
    if (token !== currentStockToken.value) {
      return
    }
    selectedStockCount.value = undefined
  } finally {
    if (token === currentStockToken.value) {
      selectedStockLoading.value = false
    }
  }
}

const handleConfirm = async () => {
  if (!selectedRow.value || !selectedProduct.value || !canConfirm.value) {
    return
  }

  confirming.value = true
  try {
    emit('confirm', {
      row: selectedRow.value,
      product: selectedProduct.value,
      stockCount: selectedStockCount.value
    })
    drawerVisible.value = false
  } finally {
    confirming.value = false
  }
}

const emit = defineEmits<{
  (event: 'confirm', payload: ConfirmPayload): void
}>()

const buildSelectionFromNode = (node: PickerTreeNode): SelectionProduct => {
  return {
    productId: node.productId!,
    productName: node.productName || node.label,
    productBarCode: node.productBarCode,
    productUnitId: node.productUnitId,
    productUnitName: node.productUnitName,
    quantityPrecision: node.quantityPrecision,
    salePrice: node.salePrice,
    categoryId: node.categoryId,
    categoryName: node.categoryName,
    sourceType: node.sourceType,
    bomUsageQty: node.bomUsageQty,
    bomLossRate: node.bomLossRate,
    bomLeadTimeDay: node.bomLeadTimeDay,
    standard: node.standard
  }
}

const buildSelectionFromRow = (row: SaleOrderItemRow): SelectionProduct | null => {
  if (!row?.productId) {
    return null
  }
  return {
    productId: row.productId,
    productName: row.productName || '当前商品',
    productBarCode: row.productBarCode,
    productUnitId: row.productUnitId,
    productUnitName: row.productUnitName,
    quantityPrecision: row.quantityPrecision,
    salePrice: row.productPrice,
    categoryId: row.categoryId,
    categoryName: row.categoryName,
    sourceType: undefined,
    standard: row.standard
  }
}

const buildProductMap = () => {
  const map = new Map<number, ProductVO>()
  flattenTree(treeData.value).forEach((node) => {
    if (node.productId && node.sourceType !== 'bom') {
      map.set(node.productId, {
        id: node.productId,
        name: node.productName || node.label,
        barCode: node.productBarCode || '',
        categoryId: node.categoryId || 0,
        categoryName: node.categoryName || '',
        unitId: node.productUnitId || 0,
        unitName: node.productUnitName || '',
        quantityPrecision: node.quantityPrecision,
        salePrice: node.salePrice || 0,
        standard: node.standard || '',
        status: 1
      } as ProductVO)
    }
  })
  return map
}

const findNodeByProductId = (nodes: PickerTreeNode[], productId: number): PickerTreeNode | undefined => {
  for (const node of nodes) {
    if (node.productId === productId) {
      return node
    }
    const child = findNodeByProductId(node.children || [], productId)
    if (child) {
      return child
    }
  }
  return undefined
}

const flattenTree = (nodes: PickerTreeNode[]): PickerTreeNode[] => {
  const list: PickerTreeNode[] = []
  nodes.forEach((node) => {
    list.push(node)
    if (node.children && node.children.length) {
      list.push(...flattenTree(node.children))
    }
  })
  return list
}

const sortCategories = (nodes: ProductCategoryVO[]) => {
  return [...nodes].sort((a, b) => {
    const sortDiff = (a.sort || 0) - (b.sort || 0)
    if (sortDiff !== 0) {
      return sortDiff
    }
    return (a.name || '').localeCompare(b.name || '', 'zh-Hans-CN')
  })
}

const sortProducts = (nodes: ProductVO[]) => {
  return [...nodes].sort((a, b) => {
    return (a.name || '').localeCompare(b.name || '', 'zh-Hans-CN')
  })
}

const countCategoryNodes = (nodes: PickerTreeNode[]): number => {
  let count = 0
  nodes.forEach((node) => {
    if (node.nodeType === 'category' || node.nodeType === 'group') {
      count += 1
    }
    if (node.children && node.children.length) {
      count += countCategoryNodes(node.children)
    }
  })
  return count
}

const countCatalogProductNodes = (nodes: PickerTreeNode[]): number => {
  let count = 0
  nodes.forEach((node) => {
    if (node.nodeType === 'product' && node.sourceType !== 'bom') {
      count += 1
    }
    if (node.children && node.children.length) {
      count += countCatalogProductNodes(node.children)
    }
  })
  return count
}

const filterTreeNodes = (nodes: PickerTreeNode[], keyword: string): PickerTreeNode[] => {
  if (!keyword) {
    return nodes
  }

  return nodes.reduce<PickerTreeNode[]>((acc, node) => {
    const children = node.children ? filterTreeNodes(node.children, keyword) : []
    const matched = matchesKeyword(node, keyword)
    if (matched) {
      acc.push({
        ...node,
        children: node.children ? [...node.children] : node.children
      })
      return acc
    }
    if (children.length) {
      acc.push({
        ...node,
        children
      })
    }
    return acc
  }, [])
}

const matchesKeyword = (node: PickerTreeNode, keyword: string) => {
  const fields = [
    node.label,
    node.subLabel,
    node.categoryName,
    node.categoryCode,
    node.productBarCode,
    node.productName,
    node.standard
  ]
  return fields.filter(Boolean).some((item) => String(item).toLowerCase().includes(keyword))
}

const syncExpandedKeys = () => {
  expandedKeys.value = collectCategoryKeys(treeData.value)
}

const collectCategoryKeys = (nodes: PickerTreeNode[]): string[] => {
  const keys: string[] = []
  nodes.forEach((node) => {
    if (node.nodeType === 'category' || node.nodeType === 'group') {
      keys.push(node.treeKey)
    }
    if (node.children && node.children.length) {
      keys.push(...collectCategoryKeys(node.children))
    }
  })
  return keys
}

const syncCurrentNode = () => {
  if (selectedNodeKey.value) {
    treeRef.value?.setCurrentKey(selectedNodeKey.value)
    return
  }
  treeRef.value?.setCurrentKey(undefined)
}
</script>

<style scoped lang="scss">
.sale-order-product-picker {
  :deep(.el-drawer__body) {
    padding: 0;
    overflow: hidden;
    background: #f7f9fc;
  }
}

.picker-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px 18px 18px;
  gap: 14px;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.picker-header__main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.picker-header__icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.1);
}

.picker-header__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.picker-header__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.picker-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.picker-search {
  flex: 1;
  min-width: 0;
}

.picker-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.picker-toolbar__meta strong {
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.picker-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.picker-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.95fr);
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.picker-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
  overflow: hidden;
}

.picker-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 14px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.picker-panel__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.picker-tree-wrap {
  flex: 1;
  min-height: 0;
}

.picker-tree-scrollbar {
  height: 100%;
}

.picker-tree {
  padding: 10px 12px 14px;
}

.picker-tree-node {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 8px 4px 8px 0;
}

.picker-tree-node__content {
  min-width: 0;
  flex: 1;
}

.picker-tree-node__title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.picker-tree-node__title {
  max-width: 100%;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.picker-tree-node__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.picker-tree-node__arrow {
  flex-shrink: 0;
  margin-top: 3px;
  color: var(--el-text-color-placeholder);
}

.selection-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 16px;
  margin: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(64, 158, 255, 0.12);
}

.selection-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.selection-card__name {
  min-width: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.selection-card__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.selection-item {
  padding: 12px 13px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.selection-item__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.selection-item strong {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.selection-card__section-title {
  margin-top: 14px;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.selection-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.picker-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.picker-footer__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.picker-tree .el-tree-node__content) {
  height: auto;
  padding: 6px 10px 6px 8px;
  border-radius: 14px;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

:deep(.picker-tree .el-tree-node__content:hover) {
  background: rgba(64, 158, 255, 0.08);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
}

:deep(.picker-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(64, 158, 255, 0.14);
  box-shadow: 0 6px 14px rgba(64, 158, 255, 0.1);
}

:deep(.picker-tree .el-tree-node__expand-icon) {
  color: var(--el-color-primary);
}

@media (max-width: 1280px) {
  .picker-grid {
    grid-template-columns: 1fr;
  }

  .picker-panel--summary {
    min-height: 300px;
  }
}

@media (max-width: 768px) {
  .picker-shell {
    padding: 12px;
  }

  .picker-toolbar,
  .picker-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .picker-toolbar__meta,
  .picker-footer__actions {
    width: 100%;
  }

  .picker-footer__actions :deep(.el-button) {
    flex: 1 1 0;
  }

  .selection-card {
    margin: 12px;
    padding: 14px;
  }

  .selection-card__grid {
    grid-template-columns: 1fr;
  }
}
</style>
