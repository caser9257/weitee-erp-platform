<template>
  <div class="product-batch-approval-panel">
    <!-- 加载态 -->
    <div v-if="loading" v-loading="loading" class="min-h-240px"></div>

    <!-- 错误态 -->
    <div v-else-if="loadError" class="rounded-lg border border-slate-100 bg-white px-16px py-40px text-center shadow-sm">
      <div class="mb-12px text-13px text-slate-500">{{ loadError }}</div>
      <el-button size="small" @click="loadView">重试</el-button>
    </div>

    <template v-else-if="view">
      <!-- 上下文卡片：批次关键事实 -->
      <div class="rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="flex flex-wrap items-center gap-x-16px gap-y-8px">
          <span class="font-mono text-15px font-semibold text-slate-800">批次 {{ view.batchId }}</span>
          <el-tag effect="light" :type="BATCH_STATUS_META[view.status ?? 0]?.type || 'info'">
            {{ BATCH_STATUS_META[view.status ?? 0]?.label || '未知' }}
          </el-tag>
          <span class="rounded-full bg-blue-50 px-10px py-2px font-mono text-12px text-blue-600">
            共 {{ view.totalCount }} 条物料
          </span>
        </div>
      </div>

      <!-- 批次变更清单 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px flex items-center gap-10px">
          <span class="text-14px font-semibold text-slate-700">变更清单</span>
          <span class="rounded-full bg-slate-100 px-10px py-2px font-mono text-12px text-slate-500">
            {{ view.items?.length || 0 }} 项
          </span>
        </div>
        <el-table
          v-if="view.items?.length"
          :data="view.items"
          :stripe="true"
          row-key="productId"
          max-height="480"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="bg-slate-50 px-24px py-12px">
                <el-table :data="row.diffs" :stripe="true" size="small">
                  <el-table-column label="字段" width="160">
                    <template #default="{ row: diff }">{{ diff.label }}</template>
                  </el-table-column>
                  <el-table-column label="变更前（现值）" min-width="180">
                    <template #default="{ row: diff }">
                      <span class="font-mono text-slate-400 line-through decoration-slate-300">
                        {{ diff.oldValue || '—' }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column width="40" align="center">
                    <template #default>
                      <Icon icon="ep:right" class="text-12px text-slate-400" />
                    </template>
                  </el-table-column>
                  <el-table-column label="变更后（申请值）" min-width="180">
                    <template #default="{ row: diff }">
                      <span class="font-mono font-medium text-slate-700">{{ diff.newValue || '—' }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="物料" min-width="220">
            <template #default="{ row }">
              <div class="flex flex-col">
                <span class="text-13px font-medium text-slate-700">{{ row.name || '—' }}</span>
                <span class="font-mono text-12px text-slate-400">{{ row.materialCode || `#${row.productId}` }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="变更字段" min-width="260">
            <template #default="{ row }">
              <div class="flex flex-wrap gap-4px">
                <span
                  v-for="diff in row.diffs"
                  :key="diff.field"
                  class="rounded-full bg-slate-100 px-8px py-1px text-12px text-slate-600"
                >
                  {{ diff.label }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="变更项" width="90" align="right">
            <template #default="{ row }">
              <span class="font-mono text-13px text-slate-600">{{ row.diffs?.length || 0 }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="py-32px text-center text-13px text-slate-400">批次内无变更明细</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ProductApi, type ProductBatchApprovalViewVO } from '@/api/erp/product/product'

defineOptions({ name: 'ProductBatchApprovalPanel' })

const props = defineProps<{
  id: string | number
}>()

const loading = ref(false)
const loadError = ref('')
const view = ref<ProductBatchApprovalViewVO | null>(null)

const BATCH_STATUS_META: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'danger' }> = {
  1: { label: '待审', type: 'warning' },
  2: { label: '已通过', type: 'success' },
  3: { label: '已驳回', type: 'danger' },
  4: { label: '流程失败', type: 'danger' }
}

const loadView = async () => {
  // 批次号为雪花 ID，超出 JS 安全整数范围，必须按字符串传递，禁止 Number() 转换（会精度丢失）
  const batchId = String(props.id ?? '').trim()
  if (!batchId || batchId === 'undefined' || batchId === 'null') {
    loadError.value = '无效的批次编号'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    view.value = await ProductApi.getBatchApprovalView(batchId)
  } catch (e) {
    view.value = null
    loadError.value = '批量修改审批视图加载失败'
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
