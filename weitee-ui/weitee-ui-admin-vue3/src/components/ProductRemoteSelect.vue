<template>
  <el-select
    v-model="innerValue"
    filterable
    remote
    reserve-keyword
    :remote-method="handleRemote"
    :loading="loading"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    class="w-full"
    @change="handleSelect"
    @clear="handleClear"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.name}（${item.materialCode || item.barCode || item.id}）`"
      :value="item.id"
    />
    <template #empty>
      <div class="py-16px text-center">
        <div class="mx-auto mb-8px flex h-32px w-32px items-center justify-center rounded-lg bg-slate-100">
          <Icon icon="ep:search" :size="16" class="text-slate-400" />
        </div>
        <div class="text-12px text-slate-400">{{ keyword ? '未找到匹配物料' : '输入名称/编号搜索物料' }}</div>
      </div>
    </template>
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'

defineOptions({ name: 'ProductRemoteSelect' })

const props = withDefaults(
  defineProps<{
    modelValue?: number | null
    /** approved：仅已审核物料（BOM 引用等场景）；all：全部启用物料 */
    scope?: 'approved' | 'all'
    placeholder?: string
    clearable?: boolean
    disabled?: boolean
  }>(),
  {
    modelValue: null,
    scope: 'all',
    placeholder: '输入至少 1 个字搜索物料',
    clearable: true,
    disabled: false
  }
)
const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  change: [value: number | null]
  select: [product: ProductVO | null]
}>()

type OptionItem = ProductVO

const innerValue = ref<number | null>(props.modelValue ?? null)
const keyword = ref('')
const loading = ref(false)
const options = ref<OptionItem[]>([])

watch(
  () => props.modelValue,
  (v) => (innerValue.value = v ?? null)
)
watch(innerValue, (v) => {
  emit('update:modelValue', v as any)
  emit('change', v as any)
})

let timer: any = null
const handleRemote = (query: string) => {
  const q = (query || '').trim()
  keyword.value = q
  if (!q) {
    options.value = []
    return
  }
  if (timer) clearTimeout(timer)
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const list: OptionItem[] =
        props.scope === 'approved'
          ? ((await ProductApi.getApprovedProductSimpleList(q)) as any[])
          : ((await ProductApi.getProductSimpleList(q)) as any[])
      options.value = (list || []).slice(0, 50)
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleClear = () => {
  options.value = []
}

const handleSelect = (value: number | null) => {
  emit('select', options.value.find((item) => item.id === value) || null)
}

// 回显：初始有值但选项未加载时，按 id 拉详情补名称
watch(
  () => props.modelValue,
  async (v) => {
    if (v && !options.value.find((o) => o.id === v)) {
      try {
        const p: any = await ProductApi.getProduct(v as number)
        if (p?.name) {
          options.value = [
            p as OptionItem,
            ...options.value
          ]
        }
      } catch {}
    }
  },
  { immediate: true }
)
</script>
