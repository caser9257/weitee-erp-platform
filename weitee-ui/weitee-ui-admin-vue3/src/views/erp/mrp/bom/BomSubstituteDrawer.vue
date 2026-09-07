<template>
  <el-drawer
    v-model="drawerVisible"
    :with-header="false"
    :before-close="handleBeforeClose"
    :close-on-click-modal="!drawerSaving"
    :close-on-press-escape="!drawerSaving"
    :destroy-on-close="true"
    append-to-body
    :size="drawerSize"
    custom-class="bom-substitute-drawer"
    modal-class="bom-substitute-drawer__mask"
    @closed="handleClosed"
  >
    <div class="bom-substitute-drawer__shell">
      <div class="bom-substitute-drawer__header">
        <div class="bom-substitute-drawer__context-card">
          <div class="bom-substitute-drawer__eyebrow">替代料维护</div>
          <div class="bom-substitute-drawer__title-row">
            <h2 class="bom-substitute-drawer__title">{{ currentMaterialName }}</h2>
            <span class="bom-substitute-drawer__chip">{{ draftRows.length }} 条</span>
          </div>
          <div class="bom-substitute-drawer__meta">
            <span class="bom-substitute-drawer__mono">BOM：{{ bomCode || '-' }}</span>
            <span>行号：{{ itemLabel }}</span>
            <span>用量：{{ formatQty(itemUsageQty) }}</span>
          </div>
        </div>

        <button
          type="button"
          class="bom-substitute-drawer__close"
          :disabled="drawerSaving"
          @click="closeDrawer"
        >
          <Icon icon="ep:close" />
        </button>
      </div>

      <div class="bom-substitute-drawer__body">
        <el-alert
          v-if="showValidationError"
          :title="validationMessage"
          type="warning"
          show-icon
          :closable="false"
          class="mb-12px"
        />

        <div class="bom-substitute-drawer__toolbar">
          <div class="bom-substitute-drawer__toolbar-meta">
            <span>当前替代料数 {{ draftRows.length }}</span>
            <span v-if="isDirty" class="bom-substitute-drawer__dirty">有未保存修改</span>
          </div>
          <el-button type="primary" plain :disabled="drawerSaving" @click="handleAddRow">
            <Icon icon="ep:plus" class="mr-5px" />
            新增替代料
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="drawerSaving || !props.itemMaterialId"
            :loading="globalImporting"
            @click="handleImportGlobalSubstitutes"
          >
            <Icon icon="ep:download" class="mr-5px" />
            从全局替代料导入
          </el-button>
        </div>

        <div class="bom-substitute-drawer__table-wrap">
          <el-table
            v-if="draftRows.length"
            :data="draftRows"
            border
            size="small"
            class="bom-substitute-drawer__table"
          >
            <el-table-column type="index" label="#" width="56" align="center" />
            <el-table-column label="替代物料" min-width="220">
              <template #default="{ row }">
                <el-select
                  v-model="row.substituteMaterialId"
                  clearable
                  filterable
                  placeholder="请选择替代物料"
                  style="width: 100%"
                  :disabled="drawerSaving"
                  @change="(value) => handleSubstituteMaterialChange(row, value)"
                >
                  <el-option
                    v-for="item in productOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.priority"
                  :min="1"
                  :precision="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="drawerSaving"
                />
              </template>
            </el-table-column>
            <el-table-column label="替换比例" width="140" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.replaceRatio"
                  :min="0.000001"
                  :precision="6"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="drawerSaving"
                />
              </template>
            </el-table-column>
            <el-table-column label="自动推荐" width="120" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.enableAutoRecommend" :disabled="drawerSaving" />
              </template>
            </el-table-column>
            <el-table-column label="排序" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.sort"
                  :min="0"
                  :precision="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="drawerSaving"
                />
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="180">
              <template #default="{ row }">
                <el-input
                  v-model="row.remark"
                  placeholder="请输入备注"
                  :disabled="drawerSaving"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="96" align="center" fixed="right">
              <template #default="{ $index }">
                <el-button link type="danger" :disabled="drawerSaving" @click="handleRemoveRow($index)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-else description="暂无替代料草稿">
            <el-button type="primary" plain :disabled="drawerSaving" @click="handleAddRow">
              新增替代料
            </el-button>
          </el-empty>
        </div>
      </div>

      <div class="bom-substitute-drawer__footer">
        <el-button :disabled="drawerSaving" @click="closeDrawer">取消</el-button>
        <el-button type="primary" :loading="drawerSaving" :disabled="!canSave" @click="handleSave">
          保存
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { useMessage } from '@/hooks/web/useMessage'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import {
  cloneBomItemSubstitutes,
  createEmptyBomSubstitute,
  getBomSubstituteValidationMessage,
  serializeBomItemSubstitutes,
  type BomItemSubstituteFormData
} from './bomForm.helpers'
import { erpCountInputFormatter } from '@/utils'

type DraftRow = BomItemSubstituteFormData & {
  draftKey: string
}

const props = defineProps<{
  modelValue: boolean
  bomCode?: string
  itemLabel?: number
  itemMaterialId?: number
  itemMaterialName?: string
  itemUsageQty?: number
  sourceSubstitutes?: BomItemSubstituteFormData[]
  productOptions: ProductVO[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', substitutes: BomItemSubstituteFormData[], ack: () => void): void
}>()

const message = useMessage()
const { width } = useWindowSize()

const drawerSaving = ref(false)
const submitTouched = ref(false)
const globalImporting = ref(false)
const draftRows = ref<DraftRow[]>([])
const sourceSignature = ref('')

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const productMap = computed(() =>
  props.productOptions.reduce<Record<number, ProductVO>>((acc, item) => {
    acc[item.id] = item
    return acc
  }, {})
)

const currentMaterialName = computed(() => {
  if (props.itemMaterialName) {
    return props.itemMaterialName
  }
  if (!props.itemMaterialId) {
    return '未选择物料'
  }
  return productMap.value[props.itemMaterialId]?.name || '未选择物料'
})

const validationMessage = computed(() => getBomSubstituteValidationMessage(stripDraftRows(draftRows.value)))
const showValidationError = computed(() => submitTouched.value && Boolean(validationMessage.value))
const isDirty = computed(
  () => serializeBomItemSubstitutes(stripDraftRows(draftRows.value)) !== sourceSignature.value
)
const canSave = computed(() => !drawerSaving.value && isDirty.value && !validationMessage.value)
const drawerSize = computed(() => {
  if (width.value < 768) {
    return '100%'
  }
  if (width.value < 1280) {
    return '84vw'
  }
  return '960px'
})

const syncDraftFromProps = () => {
  const sourceRows = cloneBomItemSubstitutes(props.sourceSubstitutes || [])
  draftRows.value = sourceRows.map((item, index) => ({
    ...item,
    draftKey: buildDraftKey(index, item.id)
  }))
  sourceSignature.value = serializeBomItemSubstitutes(sourceRows)
  drawerSaving.value = false
  submitTouched.value = false
}

const resetDrawerState = () => {
  drawerSaving.value = false
  submitTouched.value = false
  draftRows.value = []
  sourceSignature.value = ''
}

const buildDraftKey = (index: number, id?: number) => {
  return `${id ?? 'new'}-${index}-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const stripDraftRows = (rows: DraftRow[]): BomItemSubstituteFormData[] =>
  rows.map(({ draftKey: _draftKey, ...substitute }) => substitute)

const requestClose = async (close: () => void) => {
  if (drawerSaving.value) {
    return
  }
  if (isDirty.value) {
    try {
      await message.confirm('替代料草稿尚未保存，确认放弃修改吗？')
      close()
    } catch {
      // 用户取消关闭，不做处理。
    }
    return
  }
  close()
}

const handleBeforeClose = (done: () => void) => {
  void requestClose(done)
}

const closeDrawer = () => {
  void requestClose(() => {
    drawerVisible.value = false
  })
}

const handleClosed = () => {
  resetDrawerState()
}

const handleAddRow = () => {
  draftRows.value.push({
    ...createEmptyBomSubstitute(),
    draftKey: buildDraftKey(draftRows.value.length)
  })
}

const handleRemoveRow = (index: number) => {
  draftRows.value.splice(index, 1)
}

const handleSubstituteMaterialChange = (row: DraftRow, value?: number) => {
  if (!value) {
    row.substituteMaterialName = undefined
    return
  }
  row.substituteMaterialName = productMap.value[value]?.name
}

const handleImportGlobalSubstitutes = async () => {
  if (globalImporting.value) {
    return
  }
  if (!props.itemMaterialId) {
    message.warning('请先选择物料后再从全局替代料导入')
    return
  }
  globalImporting.value = true
  try {
    const list = await ProductApi.getSubstituteList(props.itemMaterialId)
    const validRows = (list || []).filter(
      (item) =>
        item.substituteType === 1 &&
        item.status === 0 &&
        item.substituteProductId !== props.itemMaterialId
    )
    if (!validRows.length) {
      message.info('该物料未预设全局替代料')
      return
    }
    const existingIds = new Set(draftRows.value.map((r) => r.substituteMaterialId).filter(Boolean))
    const newRows = validRows
      .filter((item) => !existingIds.has(item.substituteProductId))
      .map((item) => ({
        ...createEmptyBomSubstitute(),
        substituteMaterialId: item.substituteProductId,
        substituteMaterialName: item.substituteProductName,
        priority: item.priority ?? 1,
        replaceRatio: item.replaceRatio ?? 1,
        draftKey: buildDraftKey(draftRows.value.length)
      }))
    draftRows.value.push(...newRows)
    message.success(`已从全局替代料带出 ${newRows.length} 条`)
  } catch {
    message.error('全局替代料加载失败')
  } finally {
    globalImporting.value = false
  }
}

const handleSave = () => {
  submitTouched.value = true
  const messageText = validationMessage.value
  if (messageText) {
    message.warning(messageText)
    return
  }

  drawerSaving.value = true
  emit('save', stripDraftRows(draftRows.value), () => {
    drawerSaving.value = false
    drawerVisible.value = false
  })
}

const formatQty = (value?: number) => {
  if (value === undefined || value === null) {
    return '-'
  }
  return erpCountInputFormatter(value)
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      syncDraftFromProps()
      return
    }
    resetDrawerState()
  },
  { immediate: true }
)
</script>

<style scoped>
.bom-substitute-drawer__shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: rgb(248 250 252);
}

.bom-substitute-drawer__header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px 20px 0;
}

.bom-substitute-drawer__context-card {
  flex: 1;
  border-radius: 16px;
  padding: 16px 18px;
  background: linear-gradient(135deg, rgb(15 23 42) 0%, rgb(30 41 59) 100%);
  color: white;
  box-shadow: 0 18px 40px -24px rgb(15 23 42 / 0.65);
}

.bom-substitute-drawer__eyebrow {
  font-size: 12px;
  letter-spacing: 0.08em;
  color: rgb(191 219 254);
}

.bom-substitute-drawer__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.bom-substitute-drawer__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.bom-substitute-drawer__chip {
  flex-shrink: 0;
  border-radius: 9999px;
  padding: 6px 10px;
  background: rgb(59 130 246 / 0.18);
  color: rgb(219 234 254);
  font-size: 12px;
  font-weight: 600;
}

.bom-substitute-drawer__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 10px;
  font-size: 12px;
  color: rgb(226 232 240);
}

.bom-substitute-drawer__mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.bom-substitute-drawer__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid rgb(226 232 240);
  border-radius: 9999px;
  background: white;
  color: rgb(71 85 105);
  transition: all 0.2s ease;
}

.bom-substitute-drawer__close:hover:not(:disabled) {
  background: rgb(241 245 249);
}

.bom-substitute-drawer__close:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bom-substitute-drawer__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 20px 0;
  overflow: hidden;
}

.bom-substitute-drawer__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bom-substitute-drawer__toolbar-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: rgb(71 85 105);
  font-size: 12px;
}

.bom-substitute-drawer__dirty {
  border-radius: 9999px;
  padding: 4px 8px;
  background: rgb(239 246 255);
  color: rgb(37 99 235);
  border: 1px solid rgb(191 219 254);
}

.bom-substitute-drawer__table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-bottom: 16px;
}

.bom-substitute-drawer__table {
  width: 100%;
}

.bom-substitute-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 20px 16px;
  border-top: 1px solid rgb(226 232 240);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
}

:deep(.bom-substitute-drawer__mask) {
  backdrop-filter: blur(4px);
}

:deep(.bom-substitute-drawer .el-drawer__body) {
  padding: 0;
}

@media (max-width: 768px) {
  .bom-substitute-drawer__header,
  .bom-substitute-drawer__body,
  .bom-substitute-drawer__footer {
    padding-left: 12px;
    padding-right: 12px;
  }

  .bom-substitute-drawer__title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .bom-substitute-drawer__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
