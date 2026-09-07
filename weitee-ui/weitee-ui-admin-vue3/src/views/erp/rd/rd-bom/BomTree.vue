<template>
  <div>
    <div class="mb-12px flex flex-wrap items-center gap-8px rounded-lg border border-slate-100 bg-slate-50 px-12px py-10px">
      <el-button size="small" @click="expandAll">
        <Icon icon="ep:expand" class="mr-4px" />
        展开全部
      </el-button>
      <el-button size="small" @click="collapseAll">
        <Icon icon="ep:fold" class="mr-4px" />
        收起全部
      </el-button>
      <el-divider direction="vertical" />
      <span class="text-12px text-slate-500">按层级展开</span>
      <el-select v-model="expandLevel" size="small" style="width: 110px" @change="expandToLevel">
        <el-option :label="'收起全部'" :value="0" />
        <el-option v-for="n in maxLevel" :key="n" :label="`至第 ${n} 级`" :value="n" />
        <el-option :label="'全部展开'" :value="99" />
      </el-select>
    </div>

    <div v-if="treeData" class="max-h-[60vh] overflow-auto rounded-lg border border-slate-100 bg-white">
      <div class="sticky top-0 z-10 flex items-center gap-8px border-b border-slate-100 bg-slate-50 px-12px py-10px">
        <span class="font-mono text-13px font-bold text-slate-700">{{ treeData.bomCode }}</span>
        <span class="text-13px text-slate-600">{{ treeData.productName }}</span>
        <el-tag v-if="treeData.version" size="small" type="info" effect="plain">{{ treeData.version }}</el-tag>
        <span class="ml-auto text-11px text-slate-400">共 {{ totalCount }} 项</span>
      </div>
      <div class="p-8px">
        <BomTreeNode
          v-for="item in treeData.items"
          :key="item.id ?? 0"
          :item="item"
          :depth="1"
          :node-path="String(item.id ?? 0)"
        />
        <div v-if="!treeData.items || !treeData.items.length" class="py-32px text-center text-13px text-slate-400">
          该 BOM 暂无明细
        </div>
      </div>
    </div>
    <div v-else class="flex flex-col items-center justify-center rounded-lg border border-dashed border-slate-200 bg-slate-50 py-40px">
      <Icon icon="ep:document" :size="32" class="mb-8px text-slate-300" />
      <span class="text-13px text-slate-400">暂无结构树数据</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, provide, reactive, ref, watch } from 'vue'
import BomTreeNode from './BomTreeNode.vue'
import type { RdBomTreeRespVO } from '@/api/erp/rd/bom'

const props = defineProps<{
  treeData: RdBomTreeRespVO | null
}>()

const expandState = reactive<Record<string, boolean>>({})
provide('rdBomExpandState', expandState)

const maxLevel = 6
const expandLevel = ref(2)

const totalCount = computed(() => {
  if (!props.treeData?.items) return 0
  let c = 0
  const walk = (items: any[]) => {
    if (!items) return
    for (const it of items) {
      c++
      walk(it.children)
    }
  }
  walk(props.treeData.items)
  return c
})

function walk(items: any[] | undefined, depth: number, cb: (nodePath: string, depth: number) => void, parentPath = '') {
  if (!items) return
  for (const it of items) {
    const nodePath = parentPath ? `${parentPath}/${it.id ?? 0}` : String(it.id ?? 0)
    cb(nodePath, depth)
    walk(it.children, depth + 1, cb, nodePath)
  }
}

const expandAll = () => {
  walk(props.treeData?.items, 1, (nodePath) => (expandState[nodePath] = true))
}

const collapseAll = () => {
  for (const k in expandState) expandState[k] = false
}

const expandToLevel = (n: number) => {
  collapseAll()
  if (n === 0) return
  if (n === 99) {
    expandAll()
    return
  }
  walk(props.treeData?.items, 1, (nodePath, depth) => {
    if (depth < n) expandState[nodePath] = true
  })
}

// 默认按 2 级展开
watch(
  () => props.treeData,
  () => {
    collapseAll()
    if (props.treeData?.items) {
      expandToLevel(2)
      expandLevel.value = 2
    }
  },
  { immediate: true }
)
</script>
