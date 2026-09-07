<template>
  <div class="bom-disable-approval-panel">
    <!-- 加载态 -->
    <div v-if="loading" v-loading="loading" class="min-h-240px"></div>

    <!-- 错误态 -->
    <div v-else-if="loadError" class="rounded-lg border border-slate-100 bg-white px-16px py-40px text-center shadow-sm">
      <div class="mb-12px text-13px text-slate-500">{{ loadError }}</div>
      <el-button size="small" @click="loadBom">重试</el-button>
    </div>

    <template v-else-if="bom">
      <!-- 上下文卡片 -->
      <div class="rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="flex flex-wrap items-center gap-x-16px gap-y-8px">
          <span class="font-mono text-18px font-semibold text-slate-800">{{ bom.bomCode }}</span>
          <span class="text-15px font-medium text-slate-700">{{ bom.productName || `成品 #${bom.productId}` }}</span>
          <el-tag effect="light" :type="bom.status === 1 ? 'success' : 'info'">
            {{ bom.status === 1 ? '已生效' : '已停用' }}
          </el-tag>
        </div>
        <div class="mt-6px flex flex-wrap items-center gap-x-16px gap-y-4px">
          <span class="font-mono text-12px text-slate-500">版本 {{ bom.version || '—' }}</span>
          <span class="font-mono text-12px text-slate-500">明细 {{ bom.items?.length || 0 }} 行</span>
          <span v-if="bom.remark" class="text-13px text-slate-600">{{ bom.remark }}</span>
        </div>
      </div>

      <!-- 停用后果 -->
      <div class="mt-12px rounded-lg border border-rose-100 bg-rose-50 px-16px py-10px text-13px text-rose-600">
        本次申请停用该制造 BOM：审批通过后状态置为停用，MRP 与新增生产单据不再引用。
      </div>

      <!-- BOM 明细 -->
      <div class="mt-12px rounded-lg border border-slate-100 bg-white p-16px shadow-sm">
        <div class="mb-12px flex items-center gap-10px">
          <span class="text-14px font-semibold text-slate-700">BOM 明细</span>
          <span class="rounded-full bg-slate-100 px-10px py-2px font-mono text-12px text-slate-500">
            共 {{ bom.items?.length || 0 }} 行
          </span>
        </div>
        <el-table v-if="bom.items?.length" :data="bom.items" :stripe="true" max-height="420">
          <el-table-column label="物料" min-width="160">
            <template #default="{ row }">{{ row.materialName || `#${row.materialId}` }}</template>
          </el-table-column>
          <el-table-column label="用量" width="110" align="right">
            <template #default="{ row }">
              <span class="font-mono">{{ row.usageQty ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">{{ row.unitName || '—' }}</template>
          </el-table-column>
          <el-table-column label="位号" min-width="120">
            <template #default="{ row }">{{ row.referenceDesignator || '—' }}</template>
          </el-table-column>
          <el-table-column label="物料位置" min-width="140">
            <template #default="{ row }">{{ row.position || '—' }}</template>
          </el-table-column>
        </el-table>
        <div v-else class="py-32px text-center text-13px text-slate-400">该 BOM 暂无明细</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { BomApi, type BomVO } from '@/api/erp/mrp/bom'

defineOptions({ name: 'BomDisableApprovalPanel' })

const props = defineProps<{
  id: string | number
}>()

const loading = ref(false)
const loadError = ref('')
const bom = ref<BomVO | null>(null)

const loadBom = async () => {
  const bomId = Number(props.id)
  if (!bomId) {
    loadError.value = '无效的 BOM 编号'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    bom.value = await BomApi.getBom(bomId)
  } catch (e) {
    bom.value = null
    loadError.value = 'BOM 详情加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.id,
  () => loadBom(),
  { immediate: true }
)
</script>
