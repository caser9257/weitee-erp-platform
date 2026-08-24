<template>
  <section class="stf-hero">
    <div class="stf-hero__header">
      <div>
        <div class="stf-hero__breadcrumb">生产执行 / 库存失败记录</div>
        <div class="stf-page__title">库存任务失败记录</div>
      </div>
      <div class="stf-hero__stats">
        <div class="stf-stat stf-stat--danger">
          <div class="stf-stat__num">{{ pendingCount }}</div>
          <div class="stf-stat__label">待重试</div>
        </div>
        <div class="stf-stat stf-stat--success">
          <div class="stf-stat__num">{{ resolvedCount }}</div>
          <div class="stf-stat__label">已恢复</div>
        </div>
      </div>
    </div>
  </section>

  <ContentWrap class="stf-filter-card">
    <el-form :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="状态">
        <el-select
          v-model="queryStatus"
          clearable
          placeholder="全部"
          class="!w-180px"
          @change="handleQuery"
        >
          <el-option label="待重试" :value="0" />
          <el-option label="已恢复" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="listLoading" @click="getList">
          <Icon icon="ep:search" class="mr-5px" /> 查询
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="listLoading" :data="list" :stripe="true">
      <template #empty>
        <div class="stf-empty">
          <div class="stf-empty__icon"><Icon icon="ep:circle-check" /></div>
          <div class="stf-empty__title">暂无库存任务失败记录</div>
        </div>
      </template>
      <el-table-column label="业务类型" width="150">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ bizTypeLabel(row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务单据" min-width="140" align="center">
        <template #default="{ row }">
          <span class="font-mono">{{ row.bizId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="产品编号" min-width="120" align="center">
        <template #default="{ row }">
          <span class="font-mono">{{ row.productId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="仓库编号" min-width="110" align="center">
        <template #default="{ row }">
          <span class="font-mono">{{ row.warehouseId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="qty" min-width="110" align="right">
        <template #default="{ row }">
          <span class="font-mono">{{ formatQty(row.qty) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" prop="reason" min-width="240" show-overflow-tooltip />
      <el-table-column label="重试次数" prop="retryCount" width="90" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 0 ? 'warning' : 'success'"
            effect="light"
          >
            {{ row.status === 0 ? '待重试' : '已恢复' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发生时间" prop="createTime" width="170" align="center" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            link
            type="primary"
            :loading="retryLoadingId === row.id"
            :disabled="retryLoadingId === row.id"
            @click="handleRetry(row.id)"
            v-hasPermi="['erp:stock-task-failure:retry']"
          >
            重试
          </el-button>
          <span v-else class="text-slate-300">—</span>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import {
  StockTaskFailureApi,
  STOCK_TASK_FAILURE_BIZ_TYPE,
  type StockTaskFailureVO
} from '@/api/erp/manufacturing/stock-task-failure'

defineOptions({ name: 'ErpStockTaskFailure' })

const message = useMessage()

const listLoading = ref(false)
const retryLoadingId = ref<number | undefined>()
const list = ref<StockTaskFailureVO[]>([])
const queryStatus = ref<number | undefined>(undefined)

const pendingCount = computed(() => list.value.filter((r) => r.status === 0).length)
const resolvedCount = computed(() => list.value.filter((r) => r.status === 1).length)

const bizTypeLabel = (type?: string) =>
  type === STOCK_TASK_FAILURE_BIZ_TYPE.PRODUCTION_DEDUCT ? '生产领料扣减' : 'IQC 移可用'

const formatQty = (qty?: number) => (qty == null ? '-' : String(qty))

const getList = async () => {
  listLoading.value = true
  try {
    list.value = await StockTaskFailureApi.getFailureLogList(queryStatus.value)
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => getList()

const handleRetry = async (id?: number) => {
  if (!id || retryLoadingId.value) {
    return
  }
  retryLoadingId.value = id
  try {
    await message.confirm('确认重试该库存任务吗？将在行锁事务内重新执行。')
    await StockTaskFailureApi.retryFailure(id)
    message.success('重试成功，记录已置为已恢复')
    await getList()
  } catch {
  } finally {
    retryLoadingId.value = undefined
  }
}

onMounted(getList)
</script>

<style scoped>
.stf-hero {
  background: var(--erp-slate-50, #f5f7fa);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 12px;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
}
.stf-hero__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stf-hero__breadcrumb {
  font-size: 12px;
  color: var(--erp-slate-400, #94a3b8);
}
.stf-page__title {
  margin-top: 4px;
  font-size: 20px;
  font-weight: 600;
  color: var(--erp-slate-800, #1e293b);
}
.stf-hero__stats {
  display: flex;
  gap: 12px;
}
.stf-stat {
  border-radius: 10px;
  padding: 10px 18px;
  text-align: center;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
  background: #fff;
}
.stf-stat__num {
  font-size: 22px;
  font-weight: 700;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.stf-stat--danger .stf-stat__num {
  color: var(--erp-danger-600, #dc2626);
}
.stf-stat--success .stf-stat__num {
  color: var(--erp-success-600, #059669);
}
.stf-stat__label {
  font-size: 12px;
  color: var(--erp-slate-500, #64748b);
}
.stf-empty {
  padding: 32px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.stf-empty__icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: var(--erp-slate-100, #f1f5f9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--erp-slate-400, #94a3b8);
}
.stf-empty__title {
  font-size: 13px;
  color: var(--erp-slate-400, #94a3b8);
}
</style>
