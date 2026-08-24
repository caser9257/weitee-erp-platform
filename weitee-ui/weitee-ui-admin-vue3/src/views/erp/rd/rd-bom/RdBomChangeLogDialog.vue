<template>
  <Dialog v-model="dialogVisible" title="变更记录" width="720px" :close-on-click-modal="false">
    <div v-loading="loading" class="min-h-120px">
      <el-timeline v-if="logs.length">
        <el-timeline-item
          v-for="log in logs"
          :key="log.id"
          :timestamp="log.createTime || ''"
          placement="top"
          :type="log.changeType === 'UPDATE' ? 'primary' : 'info'"
        >
          <div class="rounded border border-slate-100 bg-slate-50 px-12px py-10px">
            <div class="mb-6px flex items-center gap-8px">
              <el-tag size="small" effect="plain">{{ log.changeType }}</el-tag>
              <span v-if="log.creator" class="text-12px text-slate-500">操作人 {{ log.creator }}</span>
            </div>
            <div class="whitespace-pre-wrap break-all font-mono text-12px text-slate-600">
              {{ log.changeDetail }}
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div v-else-if="!loading" class="py-32px text-center text-13px text-slate-400">暂无变更记录</div>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RdBomApi, type RdBomChangeLogVO } from '@/api/erp/rd/bom'

defineOptions({ name: 'RdBomChangeLogDialog' })

const dialogVisible = ref(false)
const loading = ref(false)
const logs = ref<RdBomChangeLogVO[]>([])

const open = async (bomId: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    logs.value = await RdBomApi.getChangeLog(bomId)
  } finally {
    loading.value = false
  }
}
defineExpose({ open })
</script>
