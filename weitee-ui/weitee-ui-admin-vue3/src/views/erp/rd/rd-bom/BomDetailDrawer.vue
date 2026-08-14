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
            <el-tag :type="detailData.status === 1 ? 'success' : 'info'">
              {{ detailData.status === 1 ? '已发布' : '草稿' }}
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
          <el-table-column label="提前期(天)" prop="leadTimeDay" width="140" align="center" />
          <el-table-column label="排序" prop="sort" width="100" align="center" />
          <el-table-column label="备注" prop="remark" min-width="160" />
        </el-table>
      </template>
      <el-empty v-else description="暂无研发 BOM 数据" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { RdBomApi, type RdBomVO } from '@/api/erp/rd/bom'
import { DICT_TYPE } from '@/utils/dict'

defineOptions({ name: 'RdBomDetailDrawer' })

const drawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<RdBomVO | null>(null)

const resetDetail = () => {
  detailData.value = null
}

const open = async (id: number) => {
  drawerVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await RdBomApi.getRdBom(id)
  } finally {
    detailLoading.value = false
  }
}

watch(drawerVisible, (visible) => {
  if (!visible) {
    detailLoading.value = false
    resetDetail()
  }
})

defineExpose({ open })
</script>
