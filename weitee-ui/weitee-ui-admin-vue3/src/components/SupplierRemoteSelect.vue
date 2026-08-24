<template>
  <el-select
    v-model="innerValue"
    filterable
    remote
    :remote-method="handleRemote"
    :loading="loading"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    reserve-keyword
    class="w-full"
    @clear="handleClear"
  >
    <el-option v-for="item in options" :key="item.id" :label="item.name" :value="item.id" />
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { SupplierApi } from '@/api/erp/purchase/supplier'

const props = withDefaults(
  defineProps<{
    modelValue?: number | null
    placeholder?: string
    clearable?: boolean
    disabled?: boolean
  }>(),
  {
    placeholder: '输入至少 1 个字搜索供应商',
    clearable: true,
    disabled: false
  }
)
const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  change: [value: number | null]
}>()

const innerValue = ref(props.modelValue ?? null)
watch(
  () => props.modelValue,
  (v) => (innerValue.value = v ?? null)
)
watch(innerValue, (v) => {
  emit('update:modelValue', v as any)
  emit('change', v as any)
})

const loading = ref(false)
const options = ref<{ id: number; name: string }[]>([])

let timer: any = null
const handleRemote = (query: string) => {
  const q = (query || '').trim()
  if (!q) {
    options.value = []
    return
  }
  if (timer) clearTimeout(timer)
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const data: any = await SupplierApi.getSupplierPage({ pageNo: 1, pageSize: 20, name: q })
      const list = Array.isArray(data) ? data : data.list || []
      options.value = list.map((s: any) => ({ id: s.id, name: s.name }))
    } finally {
      loading.value = false
    }
  }, 300)
}

const handleClear = () => {
  options.value = []
}

// 初始若有值，回显名称
watch(
  () => props.modelValue,
  async (v) => {
    if (v && !options.value.find((o) => o.id === v)) {
      try {
        const s: any = await SupplierApi.getSupplier(v as number)
        const name = s?.name || s?.data?.name || ''
        if (name) options.value = [{ id: v as number, name }, ...options.value]
      } catch {}
    }
  },
  { immediate: true }
)
</script>
