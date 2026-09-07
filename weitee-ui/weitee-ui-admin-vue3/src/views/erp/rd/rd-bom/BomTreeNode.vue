<template>
  <div class="bom-tree-node">
    <div
      class="flex items-center gap-8px rounded px-8px py-6px hover:bg-slate-50 transition-colors"
      :style="{ paddingLeft: (depth - 1) * 18 + 8 + 'px' }"
    >
      <span
        v-if="item.hasChildrenBom"
        class="inline-flex h-20px w-20px cursor-pointer items-center justify-center rounded text-slate-400 hover:bg-slate-100 hover:text-slate-600"
        @click="toggle"
      >
        <Icon :icon="expanded ? 'ep:arrow-down' : 'ep:arrow-right'" :size="14" />
      </span>
      <span v-else class="inline-block w-20px"></span>

      <span class="text-13px font-medium text-slate-800 truncate max-w-180px" :title="item.materialName">
        {{ item.materialName || '—' }}
      </span>
      <span class="font-mono text-11px text-slate-400">{{ item.materialId }}</span>

      <el-tag v-if="item.materialType === 1" size="small" type="info" effect="plain" class="shrink-0">装配体</el-tag>
      <el-tag v-else size="small" type="success" effect="plain" class="shrink-0">采购件</el-tag>

      <span v-if="item.referenceDesignator" class="font-mono truncate text-11px text-slate-500 max-w-200px" :title="item.referenceDesignator">
        {{ item.referenceDesignator }}
      </span>
      <span class="ml-auto text-12px tabular-nums text-slate-600">
        用量 <span class="font-mono font-medium">{{ item.usageQty }}</span>
        <span v-if="item.lossRate" class="text-slate-400"> 损耗{{ item.lossRate }}%</span>
      </span>
      <span v-if="item.level" class="text-11px text-slate-300">L{{ item.level }}</span>
    </div>

    <div v-if="expanded && item.children && item.children.length" class="border-l border-slate-100 ml-[17px] pl-0">
      <BomTreeNode
        v-for="child in item.children"
        :key="nodePath + '/' + (child.id ?? 0)"
        :item="child"
        :depth="depth + 1"
        :node-path="nodePath + '/' + (child.id ?? 0)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import type { RdBomTreeItemVO } from '@/api/erp/rd/bom'

const props = defineProps<{
  item: RdBomTreeItemVO
  depth: number
  /** 路径敏感的节点标识（父链 id 拼接）：后端防环允许同一子 BOM 在多路径出现，以 id 作 key 会导致跨分支展开联动与 duplicate-key */
  nodePath: string
}>()

const expandState = inject<Record<string, boolean>>('rdBomExpandState')!

const expanded = computed({
  get: () => !!expandState[props.nodePath],
  set: (v: boolean) => {
    expandState[props.nodePath] = v
  }
})

const toggle = () => {
  expanded.value = !expanded.value
}
</script>
