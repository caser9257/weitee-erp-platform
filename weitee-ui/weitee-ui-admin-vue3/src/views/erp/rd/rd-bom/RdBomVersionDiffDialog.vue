<template>
  <Dialog v-model="dialogVisible" title="版本对比" width="860px" :close-on-click-modal="false">
    <div v-loading="loading">
      <div class="mb-12px flex flex-wrap items-center gap-10px">
        <el-tag effect="plain" size="large">{{ sourceVersion || '旧版本' }}</el-tag>
        <Icon icon="ep:right" class="text-slate-400" />
        <el-tag type="primary" effect="light" size="large">{{ targetVersion || '新版本' }}</el-tag>
        <div class="ml-auto flex gap-10px font-mono text-13px">
          <span class="rounded-full bg-emerald-50 px-10px py-2px text-emerald-600">新增 {{ diff?.addedCount || 0 }}</span>
          <span class="rounded-full bg-rose-50 px-10px py-2px text-rose-600">删除 {{ diff?.removedCount || 0 }}</span>
          <span class="rounded-full bg-amber-50 px-10px py-2px text-amber-600">修改 {{ diff?.changedCount || 0 }}</span>
          <span class="rounded-full bg-slate-100 px-10px py-2px text-slate-500">未变 {{ diff?.unchangedCount || 0 }}</span>
        </div>
      </div>

      <div class="mb-8px">
        <el-checkbox v-model="showUnchanged" size="small">显示未变化行</el-checkbox>
      </div>

      <el-table v-if="visibleEntries.length" :data="visibleEntries" :stripe="true" max-height="480">
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="CHANGE_TYPE_META[row.changeType]?.type || 'info'" effect="light" size="small">
              {{ CHANGE_TYPE_META[row.changeType]?.label || row.changeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物料" min-width="200">
          <template #default="{ row }">
            <div class="text-13px">{{ row.materialName || '—' }}</div>
            <div class="font-mono text-12px text-slate-400">#{{ row.materialId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供给方式" width="100" align="center">
          <template #default="{ row }">
            <dict-tag
              v-if="row.materialType !== undefined && row.materialType !== null"
              :type="DICT_TYPE.ERP_SUPPLY_TYPE"
              :value="row.materialType"
            />
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="变化明细" min-width="320">
          <template #default="{ row }">
            <template v-if="row.changes && row.changes.length">
              <div v-for="change in row.changes" :key="change.field" class="flex gap-6px text-12px leading-20px">
                <span class="shrink-0 text-slate-500">{{ change.label }}</span>
                <span class="truncate font-mono text-slate-400 line-through decoration-slate-300">
                  {{ change.oldValue || '—' }}
                </span>
                <span class="text-slate-300">→</span>
                <span class="truncate font-mono font-medium text-slate-700">{{ change.newValue || '—' }}</span>
              </div>
            </template>
            <template v-else-if="row.changeType === 'ADDED'">
              <div class="text-12px text-emerald-600">
                用量 {{ formatQty(row.newItem?.usageQty) }}<template v-if="row.newItem?.referenceDesignator">
                  · 位号 {{ row.newItem.referenceDesignator }}</template>
              </div>
            </template>
            <template v-else-if="row.changeType === 'REMOVED'">
              <div class="text-12px text-rose-500">
                用量 {{ formatQty(row.oldItem?.usageQty) }}<template v-if="row.oldItem?.referenceDesignator">
                  · 位号 {{ row.oldItem.referenceDesignator }}</template>
              </div>
            </template>
            <template v-else>
              <div class="text-12px text-slate-400">无变化</div>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-else-if="!loading" class="py-32px text-center text-13px text-slate-400">两版本明细一致</div>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import {
  RdBomApi,
  type RdBomVersionDiffVO,
  type RdBomVersionDiffEntryVO
} from '@/api/erp/rd/bom'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'RdBomVersionDiffDialog' })

const dialogVisible = ref(false)
const loading = ref(false)
const showUnchanged = ref(false)
const sourceVersion = ref('')
const targetVersion = ref('')
const diff = ref<RdBomVersionDiffVO | null>(null)

const CHANGE_TYPE_META: Record<
  string,
  { label: string; type: 'info' | 'success' | 'warning' | 'danger' }
> = {
  ADDED: { label: '新增', type: 'success' },
  REMOVED: { label: '删除', type: 'danger' },
  CHANGED: { label: '修改', type: 'warning' },
  UNCHANGED: { label: '未变', type: 'info' }
}

const visibleEntries = computed<RdBomVersionDiffEntryVO[]>(() => {
  const entries = diff.value?.entries ?? []
  return showUnchanged.value ? entries : entries.filter((entry) => entry.changeType !== 'UNCHANGED')
})

const formatQty = (value?: number) =>
  value == null ? '—' : String(value).replace(/\.?0+$/, '') || '0'

const open = async (sourceId: number, targetId: number, opts?: { sourceVersion?: string; targetVersion?: string }) => {
  dialogVisible.value = true
  loading.value = true
  showUnchanged.value = false
  sourceVersion.value = opts?.sourceVersion || ''
  targetVersion.value = opts?.targetVersion || ''
  diff.value = null
  try {
    diff.value = await RdBomApi.getVersionDiff(sourceId, targetId)
    sourceVersion.value = sourceVersion.value || diff.value?.sourceVersion || ''
    targetVersion.value = targetVersion.value || diff.value?.targetVersion || ''
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
