<template>
  <el-drawer
    v-model="drawerVisible"
    title="BOM树详情"
    size="1280px"
    destroy-on-close
    class="bom-tree-drawer"
  >
    <div class="bom-tree-drawer__body" v-loading="detailLoading">
      <el-result v-if="showErrorState" icon="error" title="加载失败，请重试">
        <template #extra>
          <el-button type="primary" :disabled="detailLoading || !requestParams" @click="reloadDetail">
            重试加载
          </el-button>
        </template>
      </el-result>

      <template v-else-if="detailData">
        <section class="bom-tree-drawer__summary">
          <div class="bom-tree-drawer__summary-card">
            <div class="bom-tree-drawer__section-title">基础信息</div>
            <el-descriptions :column="summaryColumns" border>
              <el-descriptions-item label="制造BOM编码">
                <span class="font-mono">{{ detailData.bomCode || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="成品">
                {{ detailData.productName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="版本">
                {{ detailData.version || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="来源研发BOM">
                {{ detailData.sourceRdBomId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="detailData.status === 1 ? 'success' : 'info'" effect="light">
                  {{ detailData.status === 1 ? '已生效' : '草稿/停用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTimeValue(detailData.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="summaryColumns">
                {{ detailData.remark || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </section>

        <section class="bom-tree-drawer__table-section">
          <div class="bom-tree-drawer__section-header">
            <div class="bom-tree-drawer__section-title">BOM结构</div>
            <div class="bom-tree-drawer__section-meta">
              <span>节点数 {{ treeNodeCount }}</span>
              <span>层级 {{ maxLevel }}</span>
            </div>
          </div>

          <el-empty v-if="showEmptyState" description="暂无BOM结构数据" />

          <div v-else class="bom-tree-drawer__table-wrap">
            <el-table
              :data="treeRows"
              row-key="rowKey"
              border
              default-expand-all
              :tree-props="{ children: 'children' }"
              :indent="20"
              class="bom-tree-drawer__table"
            >
              <el-table-column type="expand" width="48">
                <template #default="{ row }">
                  <div class="bom-tree-drawer__substitute">
                    <div class="bom-tree-drawer__substitute-title">替代料列表</div>
                    <el-table
                      v-if="row.substitutes?.length"
                      :data="row.substitutes"
                      border
                      size="small"
                    >
                      <el-table-column type="index" label="#" width="56" align="center" />
                      <el-table-column label="替代物料" prop="substituteMaterialName" min-width="180" />
                      <el-table-column label="优先级" prop="priority" width="100" align="center" />
                      <el-table-column label="替换比例" prop="replaceRatio" width="120" align="right">
                        <template #default="{ row: substitute }">
                          {{ formatCount(substitute.replaceRatio) }}
                        </template>
                      </el-table-column>
                      <el-table-column label="自动推荐" width="120" align="center">
                        <template #default="{ row: substitute }">
                          {{ substitute.enableAutoRecommend ? '是' : '否' }}
                        </template>
                      </el-table-column>
                      <el-table-column label="排序" prop="sort" width="100" align="center" />
                      <el-table-column label="备注" prop="remark" min-width="160" />
                    </el-table>
                    <el-empty v-else description="暂无替代料" :image-size="72" />
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="层级" width="72" align="center">
                <template #default="{ row }">
                  {{ row.level || '-' }}
                </template>
              </el-table-column>

              <el-table-column label="节点物料" min-width="320">
                <template #default="{ row }">
                  <div class="bom-tree-drawer__material-cell">
                    <div class="bom-tree-drawer__material-name">{{ row.materialName || '-' }}</div>
                    <div class="bom-tree-drawer__material-meta">
                      <span v-if="row.childBomCode" class="font-mono">
                        下级BOM：{{ row.childBomCode }}{{ row.childBomVersion ? ` / ${row.childBomVersion}` : '' }}
                      </span>
                      <span v-else>暂无下级BOM</span>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="节点类型" width="110" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.hasChildrenBom ? 'warning' : 'info'" effect="light">
                    {{ row.hasChildrenBom ? '子装配' : '基础件' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="供应方式" width="120" align="center">
                <template #default="{ row }">
                  <dict-tag
                    v-if="row.materialType !== undefined"
                    :type="DICT_TYPE.ERP_SUPPLY_TYPE"
                    :value="row.materialType"
                  />
                  <span v-else>-</span>
                </template>
              </el-table-column>

              <el-table-column label="单位" prop="unitName" width="90" align="center" />

              <el-table-column label="用量" width="110" align="right">
                <template #default="{ row }">
                  <span class="font-mono">{{ formatCount(row.usageQty) }}</span>
                </template>
              </el-table-column>

              <el-table-column label="损耗率" width="110" align="right">
                <template #default="{ row }">
                  <span class="font-mono">{{ formatPercent(row.lossRate) }}</span>
                </template>
              </el-table-column>

              <el-table-column label="提前期(天)" width="110" align="right">
                <template #default="{ row }">
                  <span class="font-mono">{{ formatCount(row.leadTimeDay) }}</span>
                </template>
              </el-table-column>

              <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
      </template>

      <el-empty v-else description="暂无制造BOM数据" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { BomApi, type BomItemVO, type BomTreeReqVO, type BomVO } from '@/api/erp/mrp/bom'
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { erpCountInputFormatter } from '@/utils'

defineOptions({ name: 'ManufactureBomDetailDrawer' })

type BomTreeRow = BomItemVO & {
  rowKey: string
  children?: BomTreeRow[]
}

const drawerVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref(false)
const detailData = ref<BomVO | null>(null)
const requestParams = ref<BomTreeReqVO | null>(null)
const { width } = useWindowSize()

const summaryColumns = computed(() => (width.value >= 1200 ? 2 : 1))
const treeRows = computed(() => decorateTreeRows(detailData.value?.items || []))
const treeNodeCount = computed(() => countTreeNodes(treeRows.value))
const maxLevel = computed(() => resolveMaxLevel(treeRows.value))
const showErrorState = computed(() => !detailLoading.value && detailError.value)
const showEmptyState = computed(
  () => !detailLoading.value && !detailError.value && detailData.value != null && treeRows.value.length === 0
)

const formatDateTimeValue = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  const normalizedValue =
    typeof value === 'string' && /^\d{10,13}$/.test(value.trim())
      ? Number(value.trim().length === 10 ? `${value.trim()}000` : value.trim())
      : value
  return formatDate(normalizedValue) || String(value)
}

const resetDetail = () => {
  detailData.value = null
  detailError.value = false
  requestParams.value = null
}

const loadDetail = async (params: BomTreeReqVO) => {
  drawerVisible.value = true
  detailLoading.value = true
  detailError.value = false
  requestParams.value = params
  try {
    detailData.value = await BomApi.getBomTree(params)
  } catch {
    detailData.value = null
    detailError.value = true
  } finally {
    detailLoading.value = false
  }
}

const open = async (params: BomTreeReqVO) => {
  if (!params?.bomId && !params?.productId) {
    return
  }
  await loadDetail(params)
}

const reloadDetail = async () => {
  if (!requestParams.value) {
    return
  }
  await loadDetail(requestParams.value)
}

const formatCount = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return erpCountInputFormatter(value)
}

const formatPercent = (value?: number) => {
  if (value == null) {
    return '-'
  }
  return `${erpCountInputFormatter(value)}%`
}

const decorateTreeRows = (rows: BomItemVO[], parentKey = 'root'): BomTreeRow[] => {
  return rows.map((row, index) => {
    const rowKey = `${parentKey}-${row.id || row.materialId || index}`
    return {
      ...row,
      rowKey,
      children: decorateTreeRows(row.children || [], rowKey)
    }
  })
}

const countTreeNodes = (rows: BomTreeRow[]): number => {
  return rows.reduce((count, row) => count + 1 + countTreeNodes(row.children || []), 0)
}

const resolveMaxLevel = (rows: BomTreeRow[]): number => {
  return rows.reduce((maxLevel, row) => {
    const currentLevel = row.level || 0
    return Math.max(maxLevel, currentLevel, resolveMaxLevel(row.children || []))
  }, 0)
}

watch(drawerVisible, (visible) => {
  if (!visible) {
    detailLoading.value = false
    resetDetail()
  }
})

defineExpose({ open })
</script>

<style scoped>
.bom-tree-drawer__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  padding-bottom: 12px;
}

.bom-tree-drawer__summary {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
}

.bom-tree-drawer__summary-card,
.bom-tree-drawer__table-section {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: rgb(255 255 255);
  box-shadow: 0 10px 30px -24px rgb(15 23 42 / 0.45);
}

.bom-tree-drawer__summary-card {
  padding: 16px;
}

.bom-tree-drawer__table-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.bom-tree-drawer__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.bom-tree-drawer__section-title {
  font-size: 15px;
  font-weight: 600;
  color: rgb(15 23 42);
}

.bom-tree-drawer__section-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: rgb(100 116 139);
  font-size: 12px;
}

.bom-tree-drawer__table-wrap {
  overflow-x: auto;
}

.bom-tree-drawer__table :deep(.el-table__body-wrapper) {
  max-height: calc(100vh - 360px);
  overflow-y: auto;
}

.bom-tree-drawer__material-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.bom-tree-drawer__material-name {
  font-weight: 600;
  color: rgb(15 23 42);
}

.bom-tree-drawer__material-meta {
  color: rgb(100 116 139);
  font-size: 12px;
  line-height: 1.4;
}

.bom-tree-drawer__substitute {
  padding: 0 0 8px 24px;
}

.bom-tree-drawer__substitute-title {
  margin-bottom: 8px;
  color: rgb(100 116 139);
  font-size: 12px;
}

@media (max-width: 1024px) {
  .bom-tree-drawer__table :deep(.el-table__body-wrapper) {
    max-height: calc(100vh - 420px);
  }
}
</style>
