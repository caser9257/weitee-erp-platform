<template>
  <Dialog v-model="dialogVisible" title="反向追溯（Where-Used）" width="780px" :close-on-click-modal="false">
    <div class="mb-12px flex items-end gap-12px">
      <div class="flex-1">
        <div class="mb-6px text-12px font-medium text-slate-600">选择物料</div>
        <el-select
          v-model="materialId"
          filterable
          :loading="productLoading"
          placeholder="输入物料名称/编号搜索"
          class="w-full"
        >
          <el-option v-for="item in productList" :key="item.id" :label="`${item.name}（${item.materialCode || item.barCode || item.id}）`" :value="item.id" />
        </el-select>
      </div>
      <el-button type="primary" :loading="loading" :disabled="!materialId || loading" @click="handleQuery">
        查询
      </el-button>
    </div>

    <div v-loading="loading" class="min-h-120px">
      <div v-if="!queried" class="py-32px text-center text-13px text-slate-400">请选择物料后查询其上级研发 BOM</div>
      <div v-else-if="!whereUsedList.length" class="py-32px text-center">
        <Icon icon="ep:search" :size="24" class="mb-8px text-slate-300" />
        <div class="text-13px text-slate-400">该物料未被任何研发 BOM 引用</div>
      </div>
      <el-table v-else :data="whereUsedList" :stripe="true" max-height="420">
        <el-table-column label="层级" prop="level" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.level === 1 ? 'primary' : 'info'">L{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="研发 BOM 编码" prop="bomCode" min-width="160">
          <template #default="{ row }">
            <span class="font-mono text-13px">{{ row.bomCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成品" prop="productName" min-width="180" />
        <el-table-column label="版本" prop="version" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="STATUS_META[row.status]?.type || 'info'" size="small" effect="light">
              {{ STATUS_META[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import { RdBomApi, type RdBomWhereUsedVO } from '@/api/erp/rd/bom'

defineOptions({ name: 'WhereUsedDialog' })

const dialogVisible = ref(false)
const loading = ref(false)
const productLoading = ref(false)
const productList = ref<ProductVO[]>([])
const materialId = ref<number | undefined>()
const whereUsedList = ref<RdBomWhereUsedVO[]>([])
const queried = ref(false)

const STATUS_META: Record<number, { label: string; type: any }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  10: { label: '审批中', type: 'warning' },
  20: { label: '已审批', type: 'success' },
  30: { label: '已驳回', type: 'danger' },
  60: { label: '处理失败', type: 'danger' }
}

const open = async () => {
  dialogVisible.value = true
  queried.value = false
  whereUsedList.value = []
  materialId.value = undefined
  await loadProductList()
}
defineExpose({ open })

const loadProductList = async () => {
  productLoading.value = true
  try {
    productList.value = await ProductApi.getProductSimpleList()
  } finally {
    productLoading.value = false
  }
}

const handleQuery = async () => {
  if (!materialId.value) return
  loading.value = true
  queried.value = true
  try {
    whereUsedList.value = await RdBomApi.getWhereUsed(materialId.value)
  } finally {
    loading.value = false
  }
}
</script>
