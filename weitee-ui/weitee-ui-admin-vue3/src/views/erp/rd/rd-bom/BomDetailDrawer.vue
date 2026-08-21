<template>
  <el-drawer v-model="drawerVisible" title="研发BOM详情" size="960px" destroy-on-close>
    <div v-loading="detailLoading">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="研发BOM编码">
            {{ detailData.bomCode }}
          </el-descriptions-item>
          <el-descriptions-item label="成品">
            {{ detailData.productName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="版本">
            {{ detailData.version || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="STATUS_META[detailData.status]?.type || 'info'" effect="light">
              {{ STATUS_META[detailData.status]?.label || `状态${detailData.status}` }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已发布制造BOM">
            {{ detailData.publishedBomId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最近发布时间">
            {{ detailData.lastPublishedTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detailData.createTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ detailData.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">设计物料</el-divider>

        <el-table :data="detailData.items || []" border>
          <el-table-column type="expand" width="48">
            <template #default="{ row }">
              <div class="pl-32px pr-12px pb-12px">
                <div class="mb-8px text-12px text-[var(--el-text-color-secondary)]">替代料列表</div>
                <el-table :data="row.substitutes || []" border size="small">
                  <el-table-column type="index" label="#" width="56" align="center" />
                  <el-table-column label="替代物料" prop="substituteMaterialName" min-width="180" />
                  <el-table-column label="优先级" prop="priority" width="100" align="center" />
                  <el-table-column label="替换比例" prop="replaceRatio" width="120" align="center" />
                  <el-table-column label="自动推荐" width="120" align="center">
                    <template #default="{ row: substitute }">
                      {{ substitute.enableAutoRecommend ? '是' : '否' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="排序" prop="sort" width="100" align="center" />
                  <el-table-column label="备注" prop="remark" min-width="160" />
                </el-table>
              </div>
            </template>
          </el-table-column>
          <el-table-column type="index" label="#" width="56" align="center" />
          <el-table-column label="物料" prop="materialName" min-width="180" />
          <el-table-column label="供给方式" width="120" align="center">
            <template #default="{ row }">
              <dict-tag
                v-if="row.materialType !== undefined"
                :type="DICT_TYPE.ERP_SUPPLY_TYPE"
                :value="row.materialType"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unitName" width="120" align="center" />
          <el-table-column label="用量" prop="usageQty" width="140" align="center" />
          <el-table-column label="损耗率" prop="lossRate" width="140" align="center" />
          <el-table-column label="位号" prop="referenceDesignator" width="180" show-overflow-tooltip />
          <el-table-column label="物料位置" prop="position" min-width="180" show-overflow-tooltip />
          <el-table-column label="提前期(天)" prop="leadTimeDay" width="140" align="center" />
          <el-table-column label="排序" prop="sort" width="100" align="center" />
          <el-table-column label="备注" prop="remark" min-width="160" />
        </el-table>

        <el-divider content-position="left">变更记录（申请 / 修改）</el-divider>

        <div v-loading="changeLogLoading" class="min-h-120px">
          <el-timeline v-if="changeLogs.length">
            <el-timeline-item
              v-for="log in changeLogs"
              :key="log.id"
              :timestamp="log.createTime || ''"
              placement="top"
              :type="CHANGE_TYPE_META[log.changeType]?.type || 'info'"
            >
              <div class="rounded border border-slate-100 bg-slate-50 px-12px py-10px">
                <div class="mb-6px flex items-center gap-8px">
                  <el-tag size="small" effect="plain">{{ CHANGE_TYPE_META[log.changeType]?.label || log.changeType }}</el-tag>
                  <span v-if="log.creator" class="text-12px text-slate-500">操作人 {{ log.creator }}</span>
                </div>
                <div class="whitespace-pre-wrap break-all font-mono text-12px text-slate-600">
                  {{ log.changeDetail }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <div v-else-if="!changeLogLoading" class="py-32px text-center text-13px text-slate-400">暂无变更记录</div>
        </div>
      </template>
      <el-empty v-else description="暂无研发 BOM 数据" />
    </div>
    <template #footer>
      <div class="flex items-center justify-end gap-12px">
        <el-button
          v-if="canChange"
          type="warning"
          :loading="startChangeLoading"
          :disabled="startChangeLoading"
          @click="handleStartChange"
          v-hasPermi="['erp:rd-bom:change']"
        >
          发起变更
        </el-button>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { RdBomApi, type RdBomVO, type RdBomChangeLogVO } from '@/api/erp/rd/bom'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'RdBomDetailDrawer' })
const emit = defineEmits(['success'])
const message = useMessage()

const STATUS_META: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'danger' | '' }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  10: { label: '审批中', type: 'warning' },
  20: { label: '已审批', type: 'success' },
  30: { label: '已驳回', type: 'danger' },
  60: { label: '处理失败', type: 'danger' }
}

const CHANGE_TYPE_META: Record<string, { label: string; type: 'info' | 'success' | 'warning' | 'danger' | 'primary' }> = {
  CREATE: { label: '创建', type: 'info' },
  UPDATE: { label: '修改', type: 'primary' },
  SUBMIT: { label: '提交审批', type: 'warning' },
  APPROVE: { label: '审批通过', type: 'success' },
  REJECT: { label: '审批驳回', type: 'danger' },
  CANCEL: { label: '撤回审批', type: 'warning' },
  CHANGE_CREATE: { label: '发起变更', type: 'primary' }
}

const drawerVisible = ref(false)
const detailLoading = ref(false)
const changeLogLoading = ref(false)
const startChangeLoading = ref(false)
const detailData = ref<RdBomVO | null>(null)
const changeLogs = ref<RdBomChangeLogVO[]>([])

const canChange = computed(() => {
  const d = detailData.value
  if (!d) {
    return false
  }
  const running = d.status === 10 && !!d.processInstanceId
  return d.status === 20 && !running
})

const resetDetail = () => {
  detailData.value = null
  changeLogs.value = []
}

const open = async (id: number) => {
  drawerVisible.value = true
  detailLoading.value = true
  changeLogLoading.value = true
  try {
    detailData.value = await RdBomApi.getRdBom(id)
    changeLogs.value = await RdBomApi.getChangeLog(id)
  } finally {
    detailLoading.value = false
    changeLogLoading.value = false
  }
}

const handleStartChange = async () => {
  const id = detailData.value?.id
  if (!id || startChangeLoading.value) {
    return
  }
  startChangeLoading.value = true
  try {
    await message.confirm('确认基于当前已审批 BOM 发起升版式变更吗？将生成新版本草稿，须重新提交审批。')
    const newId = await RdBomApi.startChangeRdBom(id)
    message.success('已生成变更版本（草稿），请在新版本上编辑并重新提交审批')
    emit('success')
    await open(newId)
  } catch {
  } finally {
    startChangeLoading.value = false
  }
}

watch(drawerVisible, (visible) => {
  if (!visible) {
    detailLoading.value = false
    changeLogLoading.value = false
    startChangeLoading.value = false
    resetDetail()
  }
})

defineExpose({ open })
</script>
