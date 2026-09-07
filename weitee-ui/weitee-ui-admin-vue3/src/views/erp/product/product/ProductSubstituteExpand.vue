<template>
  <div class="substitute-expand">
    <div class="substitute-expand__toolbar">
      <span class="substitute-expand__count">替代料 {{ draftRows.length }} 条</span>
      <div class="substitute-expand__actions">
        <el-button :disabled="saving" @click="handleRefresh">刷新</el-button>
        <el-button type="primary" plain :disabled="saving" @click="handleAddRow">
          <Icon icon="ep:plus" class="mr-5px" />新增替代料
        </el-button>
        <el-button type="primary" :loading="saving" :disabled="!isDirty" @click="handleSave">
          保存
        </el-button>
      </div>
    </div>
    <el-table :data="draftRows" border size="small" class="substitute-expand__table">
      <el-table-column label="替代物料" min-width="200">
        <template #default="{ row }">
          <el-select-v2
            v-model="row.substituteProductId"
            clearable
            filterable
            :options="selectOptions"
            placeholder="请选择替代物料"
            style="width: 100%"
            :disabled="saving"
            @change="(val) => handleSubChange(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="编码" width="110" align="center">
        <template #default="{ row }">
          <span class="font-mono text-slate-500 text-12px">{{ row.substituteMaterialCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="规格" width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="text-slate-500 text-12px">{{ row.substituteProductStandard || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="90" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.priority"
            :min="1"
            :precision="0"
            controls-position="right"
            size="small"
            style="width: 100%"
            :disabled="saving"
          />
        </template>
      </el-table-column>
      <el-table-column label="比例" width="100" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.replaceRatio"
            :min="0.000001"
            :precision="4"
            controls-position="right"
            size="small"
            style="width: 100%"
            :disabled="saving"
          />
        </template>
      </el-table-column>
      <el-table-column label="替代类型" width="120" align="center">
        <template #default="{ row }">
          <el-select
            v-model="row.substituteType"
            style="width: 100%"
            size="small"
            :disabled="saving"
          >
            <el-option :value="1" label="全局通用" />
            <el-option :value="2" label="临时替代" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 0"
            :disabled="saving"
            @change="(v) => { row.status = v ? 0 : 1 }"
          />
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="140">
        <template #default="{ row }">
          <el-input
            v-model="row.remark"
            placeholder="备注"
            size="small"
            :disabled="saving"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="72" align="center" fixed="right">
        <template #default="{ $index }">
          <el-button
            link
            type="danger"
            size="small"
            :disabled="saving"
            @click="handleRemoveRow($index)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!draftRows.length && !loading" description="暂无替代料，点击「新增替代料」添加" :image-size="64" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ProductApi, ProductSubstituteVO, ProductSubstituteTypeEnum } from '@/api/erp/product/product'
import { useMessage } from '@/hooks/web/useMessage'

const props = defineProps<{
  productId: number
}>()

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const draftRows = ref<ProductSubstituteVO[]>([])
const sourceSignature = ref('')
const productOptions = ref<any[]>([])

// el-select-v2 需要 { label, value } 结构；万级物料必须走虚拟滚动，禁止平铺 el-option
const selectOptions = computed(() =>
  productOptions.value.map((p) => ({
    label: p.name,
    value: p.id,
    materialCode: p.materialCode,
    standard: p.standard
  }))
)

// 产品下拉数据全量拉取较重，做模块级缓存：同一会话多个展开行共享一次请求
let productOptionsPromise: Promise<any[]> | undefined
const getCachedProductOptions = async (): Promise<any[]> => {
  if (!productOptionsPromise) {
    productOptionsPromise = ProductApi.getProductSimpleList()
      .catch(() => [])
      .finally(() => {
        productOptionsPromise = undefined
      })
  }
  return productOptionsPromise
}

const isDirty = computed(() => {
  if (!draftRows.value.length && !sourceSignature.value) return false
  const sig = JSON.stringify(draftRows.value.map(r => ({
    id: r.id,
    substituteProductId: r.substituteProductId,
    priority: r.priority,
    replaceRatio: r.replaceRatio,
    substituteType: r.substituteType,
    status: r.status,
    remark: r.remark
  })))
  return sig !== sourceSignature.value
})

const loadProductOptions = async () => {
  productOptions.value = await getCachedProductOptions()
}

const loadSubstituteList = async () => {
  loading.value = true
  try {
    const list = await ProductApi.getSubstituteList(props.productId) || []
    draftRows.value = list.map(r => ({
      ...r,
      substituteProductName: r.substituteProductName || '',
      substituteMaterialCode: r.substituteMaterialCode || '',
      substituteProductStandard: r.substituteProductStandard || ''
    }))
    sourceSignature.value = buildSignature(draftRows.value)
  } catch {
    draftRows.value = []
    sourceSignature.value = ''
  } finally {
    loading.value = false
  }
}

const buildSignature = (rows: ProductSubstituteVO[]) => {
  return JSON.stringify(rows.map(r => ({
    id: r.id,
    substituteProductId: r.substituteProductId,
    priority: r.priority,
    replaceRatio: r.replaceRatio,
    substituteType: r.substituteType,
    status: r.status,
    remark: r.remark
  })))
}

const handleSubChange = (row: ProductSubstituteVO, value?: number) => {
  const p = selectOptions.value.find((x: any) => x.value === value)
  row.substituteProductName = p?.label || ''
  row.substituteMaterialCode = p?.materialCode || ''
  row.substituteProductStandard = p?.standard || ''
}

const handleAddRow = () => {
  draftRows.value.push({
    productId: props.productId,
    substituteProductId: undefined as any,
    substituteProductName: '',
    substituteMaterialCode: '',
    substituteProductStandard: '',
    priority: 1,
    replaceRatio: 1,
    substituteType: ProductSubstituteTypeEnum.GLOBAL,
    status: 0,
    remark: ''
  })
}

const handleRemoveRow = (index: number) => {
  draftRows.value.splice(index, 1)
}

const handleRefresh = async () => {
  await loadSubstituteList()
}

const handleSave = async () => {
  if (!isDirty.value) {
    message.info('没有需要保存的修改')
    return
  }
  const invalid = draftRows.value.some(r => !r.substituteProductId)
  if (invalid) {
    message.warning('请为所有替代料行选择物料')
    return
  }
  saving.value = true
  try {
    const rows = draftRows.value.map(r => ({
      ...r,
      productId: props.productId
    }))
    await ProductApi.batchUpdateSubstitutes(props.productId, rows)
    message.success('替代料保存成功')
    sourceSignature.value = buildSignature(draftRows.value)
  } catch (e: any) {
    message.error(e?.message || '保存失败')
    // 重新加载以恢复原始状态
    await loadSubstituteList()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadProductOptions(), loadSubstituteList()])
})
</script>

<style scoped>
.substitute-expand {
  padding: 8px 32px 12px 48px;
}
.substitute-expand__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.substitute-expand__count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.substitute-expand__actions {
  display: flex;
  gap: 8px;
}
.substitute-expand__table {
  width: 100%;
}
:deep(.substitute-expand__table .el-input-number--small) {
  width: 100%;
}
</style>