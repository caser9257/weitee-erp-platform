<template>
  <div class="rd-bom-approval-panel">
    <!-- 加载态 -->
    <div v-if="loading" v-loading="loading" class="min-h-240px"></div>

    <!-- 错误态 -->
    <div v-else-if="loadError" class="rounded-lg border border-slate-100 bg-white px-16px py-40px text-center shadow-sm">
      <div class="mb-12px text-13px text-slate-500">{{ loadError }}</div>
      <el-button size="small" @click="loadView">重试</el-button>
    </div>

    <template v-else-if="view?.bom">
      <!-- 上下文卡片：当前实体关键事实 -->
      <div class="rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="flex flex-wrap items-center gap-x-16px gap-y-8px">
          <span class="font-mono text-18px font-semibold text-slate-800">{{ view.bom.bomCode }}</span>
          <el-tag effect="light" type="primary">{{ view.bom.version || '未定版' }}</el-tag>
          <el-tag effect="light" :type="STATUS_META[view.bom.status]?.type || 'info'">
            {{ STATUS_META[view.bom.status]?.label || `状态${view.bom.status}` }}
          </el-tag>
        </div>
        <div class="mt-6px flex flex-wrap items-center gap-x-16px gap-y-4px">
          <span class="text-13px text-slate-600">{{ view.bom.productName || '—' }}</span>
          <span v-if="view.bom.remark" class="truncate text-12px text-slate-400">{{ view.bom.remark }}</span>
        </div>
      </div>

      <!-- 变更对比 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px flex flex-wrap items-center gap-10px">
          <span class="text-14px font-semibold text-slate-700">变更对比</span>
          <template v-if="!isFirstSubmit">
            <el-tag effect="plain" size="small">{{ view.diff?.sourceVersion || view.baselineVersion || '基准版本' }}</el-tag>
            <Icon icon="ep:right" class="text-12px text-slate-400" />
            <el-tag effect="light" size="small" type="primary">{{ view.diff?.targetVersion || view.bom.version || '本版本' }}</el-tag>
          </template>
          <el-tag v-else effect="plain" size="small" type="info">首次提交，无对比基准</el-tag>
          <label v-if="hasUnchanged && !isFirstSubmit" class="ml-auto flex cursor-pointer items-center gap-4px text-12px text-slate-500">
            <el-checkbox v-model="showUnchanged" size="small">显示未变化行</el-checkbox>
          </label>
        </div>

        <template v-if="!isFirstSubmit">
          <div class="mb-12px flex flex-wrap gap-10px font-mono text-13px">
            <span class="rounded-full bg-emerald-50 px-10px py-2px text-emerald-600">新增 {{ view.diff?.addedCount || 0 }}</span>
            <span class="rounded-full bg-rose-50 px-10px py-2px text-rose-600">删除 {{ view.diff?.removedCount || 0 }}</span>
            <span class="rounded-full bg-amber-50 px-10px py-2px text-amber-600">修改 {{ view.diff?.changedCount || 0 }}</span>
            <span class="rounded-full bg-slate-100 px-10px py-2px text-slate-500">未变 {{ view.diff?.unchangedCount || 0 }}</span>
          </div>

          <div class="overflow-x-auto">
            <el-table v-if="visibleEntries.length" :data="visibleEntries" :stripe="true" max-height="420" size="default">
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
                    <div class="font-mono text-12px text-emerald-600">
                      用量 {{ formatQty(row.newItem?.usageQty) }}<template v-if="row.newItem?.referenceDesignator">
                        · 位号 {{ row.newItem.referenceDesignator }}</template>
                    </div>
                  </template>
                  <template v-else-if="row.changeType === 'REMOVED'">
                    <div class="font-mono text-12px text-rose-500">
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
            <div v-else class="py-32px text-center text-13px text-slate-400">与基准版本的明细一致</div>
          </div>
        </template>
      </div>

      <!-- 详细内容 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px flex items-center gap-10px">
          <span class="text-14px font-semibold text-slate-700">明细内容</span>
          <span class="rounded-full bg-slate-100 px-10px py-2px font-mono text-12px text-slate-500">
            共 {{ view.bom.items?.length || 0 }} 行
          </span>
        </div>
        <div class="overflow-x-auto">
          <el-table v-if="view.bom.items?.length" :data="view.bom.items" :stripe="true" max-height="420">
            <el-table-column type="index" label="#" width="52" align="center" />
            <el-table-column label="物料信息" min-width="240">
              <template #default="{ row }">
                <div class="text-13px font-medium text-slate-700">{{ row.materialName || '—' }}</div>
                <div class="font-mono text-12px text-slate-400">#{{ row.materialId }}<template v-if="row.unitName"> · {{ row.unitName }}</template></div>
                <div v-if="row.substitutes?.length" class="truncate text-12px text-slate-400">
                  替代：<span>{{ formatSubstitutes(row.substitutes) }}</span>
                </div>
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
            <el-table-column label="用量" width="110" align="right" class-name="font-mono">
              <template #default="{ row }">{{ formatQty(row.usageQty) }}</template>
            </el-table-column>
            <el-table-column label="损耗率" width="100" align="right" class-name="font-mono">
              <template #default="{ row }">{{ formatQty(row.lossRate) }}%</template>
            </el-table-column>
            <el-table-column label="位号 / 位置" min-width="180">
              <template #default="{ row }">
                <div v-if="row.referenceDesignator || row.position" class="truncate text-12px text-slate-600">
                  <span class="font-mono">{{ row.referenceDesignator || '' }}</span>
                  <template v-if="row.referenceDesignator && row.position"> · </template>
                  <span>{{ row.position || '' }}</span>
                </div>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="提前期(天)" width="100" align="right" class-name="font-mono">
              <template #default="{ row }">{{ row.leadTimeDay ?? '—' }}</template>
            </el-table-column>
          </el-table>
          <div v-else class="py-32px text-center text-13px text-slate-400">该 BOM 暂无明细行</div>
        </div>
      </div>

      <!-- 变更记录 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px text-14px font-semibold text-slate-700">变更记录</div>
        <el-timeline v-if="view.changeLogs?.length" class="pl-4px">
          <el-timeline-item
            v-for="log in view.changeLogs"
            :key="log.id"
            :timestamp="log.createTime ? formatDate(log.createTime) : ''"
            placement="top"
            :type="LOG_TYPE_META[log.changeType]?.type || 'info'"
          >
            <div class="flex items-center gap-8px">
              <el-tag size="small" effect="plain">{{ LOG_TYPE_META[log.changeType]?.label || log.changeType }}</el-tag>
              <span class="whitespace-pre-wrap break-all font-mono text-12px text-slate-600">{{ log.changeDetail }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="py-24px text-center text-13px text-slate-400">暂无变更记录</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {
  RdBomApi,
  type RdBomApprovalViewVO,
  type RdBomVersionDiffEntryVO,
  type RdBomItemSubstituteVO
} from '@/api/erp/rd/bom'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'RdBomApprovalPanel' })

const props = defineProps<{
  id: string | number
}>()

const loading = ref(false)
const loadError = ref('')
const view = ref<RdBomApprovalViewVO | null>(null)
const showUnchanged = ref(false)

const STATUS_META: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'danger' }> = {
  0: { label: '草稿', type: 'info' },
  10: { label: '审批中', type: 'warning' },
  20: { label: '已审批', type: 'success' },
  30: { label: '已驳回', type: 'danger' },
  50: { label: '已作废', type: 'info' },
  60: { label: '处理失败', type: 'danger' }
}

const CHANGE_TYPE_META: Record<
  string,
  { label: string; type: 'info' | 'success' | 'warning' | 'danger' }
> = {
  ADDED: { label: '新增', type: 'success' },
  REMOVED: { label: '删除', type: 'danger' },
  CHANGED: { label: '修改', type: 'warning' },
  UNCHANGED: { label: '未变', type: 'info' }
}

const LOG_TYPE_META: Record<string, { label: string; type: 'info' | 'success' | 'warning' | 'danger' | 'primary' }> = {
  CREATE: { label: '创建', type: 'info' },
  UPDATE: { label: '修改', type: 'primary' },
  SUBMIT: { label: '提交审批', type: 'warning' },
  APPROVE: { label: '审批通过', type: 'success' },
  REJECT: { label: '审批驳回', type: 'danger' },
  CANCEL: { label: '撤回', type: 'info' },
  PUBLISH: { label: '发布', type: 'success' },
  VOID: { label: '作废', type: 'danger' }
}

const isFirstSubmit = computed(() => Boolean(view.value?.firstSubmit))

const hasUnchanged = computed(() =>
  (view.value?.diff?.entries ?? []).some((entry) => entry.changeType === 'UNCHANGED')
)

const visibleEntries = computed<RdBomVersionDiffEntryVO[]>(() => {
  const entries = view.value?.diff?.entries ?? []
  return showUnchanged.value ? entries : entries.filter((entry) => entry.changeType !== 'UNCHANGED')
})

const formatQty = (value?: number | null) => (value == null ? '—' : String(Number(value)))

const formatSubstitutes = (substitutes: RdBomItemSubstituteVO[]) =>
  substitutes
    .map((sub) => {
      const name = sub.substituteMaterialName || `#${sub.substituteMaterialId}`
      return sub.replaceRatio != null ? `${name}(1:${formatQty(sub.replaceRatio)})` : name
    })
    .join('、')

const loadView = async () => {
  const bomId = Number(props.id)
  if (!bomId) {
    loadError.value = '无效的 BOM 编号'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    view.value = await RdBomApi.getApprovalView(bomId)
  } catch (e) {
    view.value = null
    loadError.value = '研发 BOM 审批视图加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.id,
  () => loadView(),
  { immediate: true }
)
</script>
