<template>
  <div class="product-approval-panel">
    <!-- 加载态 -->
    <div v-if="loading" v-loading="loading" class="min-h-240px"></div>

    <!-- 错误态 -->
    <div v-else-if="loadError" class="rounded-lg border border-slate-100 bg-white px-16px py-40px text-center shadow-sm">
      <div class="mb-12px text-13px text-slate-500">{{ loadError }}</div>
      <el-button size="small" @click="loadView">重试</el-button>
    </div>

    <template v-else-if="view?.current">
      <!-- 上下文卡片：当前实体关键事实 -->
      <div class="rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="flex flex-wrap items-center gap-x-16px gap-y-8px">
          <span class="font-mono text-18px font-semibold text-slate-800">
            {{ view.current.materialCode || `#${view.current.id}` }}
          </span>
          <span class="text-15px font-medium text-slate-700">{{ view.current.name }}</span>
          <el-tag effect="light" :type="PENDING_STATUS_META[view.pendingStatus ?? 0]?.type || 'info'">
            {{ PENDING_STATUS_META[view.pendingStatus ?? 0]?.label || '无在途变更' }}
          </el-tag>
        </div>
        <div class="mt-6px flex flex-wrap items-center gap-x-16px gap-y-4px">
          <span class="font-mono text-12px text-slate-500">条码 {{ view.current.barCode || '—' }}</span>
          <span class="text-13px text-slate-600">{{ view.current.categoryName || '—' }} · {{ view.current.unitName || '—' }}</span>
        </div>
      </div>

      <!-- 申请理由 -->
      <div
        v-if="view.reason"
        class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm"
      >
        <div class="mb-8px text-14px font-semibold text-slate-700">申请理由</div>
        <div class="text-13px leading-20px text-slate-600">{{ view.reason }}</div>
      </div>

      <!-- 变更对比 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px flex items-center gap-10px">
          <span class="text-14px font-semibold text-slate-700">变更对比</span>
          <span class="rounded-full bg-slate-100 px-10px py-2px font-mono text-12px text-slate-500">
            共 {{ view.diffs?.length || 0 }} 项
          </span>
        </div>
        <el-table v-if="view.diffs?.length" :data="view.diffs" :stripe="true" max-height="420">
          <el-table-column label="字段" width="140">
            <template #default="{ row }">{{ row.label }}</template>
          </el-table-column>
          <el-table-column label="变更前（现值）" min-width="180">
            <template #default="{ row }">
              <span class="font-mono text-slate-400 line-through decoration-slate-300">
                {{ row.oldValue || '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column width="40" align="center">
            <template #default>
              <Icon icon="ep:right" class="text-12px text-slate-400" />
            </template>
          </el-table-column>
          <el-table-column label="变更后（申请值）" min-width="180">
            <template #default="{ row }">
              <span class="font-mono font-medium text-slate-700">{{ row.newValue || '—' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="py-32px text-center text-13px text-slate-400">当前无待审变更</div>
      </div>

      <!-- 冻结提示 -->
      <div
        v-if="isReferenced"
        class="mt-12px rounded-lg border border-amber-100 bg-amber-50 px-16px py-10px text-12px leading-20px text-amber-600"
      >
        该物料已被 BOM 引用：规格型号已冻结；物料编码走编码沿革可改（历史单据按旧码兜底命中），其余字段调整需经审批生效。
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ProductApi, type ProductApprovalViewVO } from '@/api/erp/product/product'

defineOptions({ name: 'ProductApprovalPanel' })

const props = defineProps<{
  id: string | number
}>()

const loading = ref(false)
const loadError = ref('')
const view = ref<ProductApprovalViewVO | null>(null)

const PENDING_STATUS_META: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'danger' }> = {
  1: { label: '待审', type: 'warning' },
  2: { label: '已通过', type: 'success' },
  3: { label: '已驳回', type: 'danger' },
  4: { label: '流程失败', type: 'danger' }
}

const isReferenced = computed(() => Boolean(view.value?.current?.referencedByBom))

const loadView = async () => {
  const productId = Number(props.id)
  if (!productId) {
    loadError.value = '无效的物料编号'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    view.value = await ProductApi.getApprovalView(productId)
  } catch (e) {
    view.value = null
    loadError.value = '物料修改审批视图加载失败'
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
