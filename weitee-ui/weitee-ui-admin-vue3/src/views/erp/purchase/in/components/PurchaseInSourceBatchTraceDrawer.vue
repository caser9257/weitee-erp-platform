<template>
  <el-drawer
    v-model="visible"
    class="purchase-in-source-batch-trace-drawer"
    size="520px"
    :with-header="false"
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="source-batch-trace">
      <header class="source-batch-trace__header">
        <div>
          <div class="source-batch-trace__title">来源批次追溯</div>
          <div class="source-batch-trace__batch-no">{{ sourceBatch?.batchNo || '-' }}</div>
        </div>
        <el-button circle text aria-label="关闭" title="关闭" @click="visible = false">
          <Icon icon="ep:close" />
        </el-button>
      </header>
      <main class="source-batch-trace__body" v-loading="loading">
        <div v-if="error" class="source-batch-trace__empty source-batch-trace__empty--error">
          <Icon icon="ep:warning-filled" />
          <div>来源批次追溯加载失败</div>
          <el-button type="primary" plain :loading="loading" @click="loadTrace">重试</el-button>
        </div>
        <template v-else-if="!loading">
          <section class="source-batch-trace__section">
            <div class="source-batch-trace__section-title">来源批次</div>
            <div class="source-batch-trace__grid">
              <div
                ><span>产品</span><strong>{{ sourceBatch?.productName || '-' }}</strong></div
              >
              <div
                ><span>供应商</span><strong>{{ sourceBatch?.supplierName || '-' }}</strong></div
              >
              <div
                ><span>采购订单</span
                ><strong>{{ sourceBatch?.purchaseOrderNo || '-' }}</strong></div
              >
              <div
                ><span>状态</span
                ><strong>{{
                  sourceBatch?.status == null ? '-' : resolveErpAuditStatusLabel(sourceBatch.status)
                }}</strong></div
              >
            </div>
          </section>
          <section class="source-batch-trace__section">
            <div class="source-batch-trace__section-title">采购入库执行</div>
            <div v-if="traceItems.length" class="source-batch-trace__items">
              <article v-for="item in traceItems" :key="item.id" class="source-batch-trace__item">
                <div class="source-batch-trace__item-head">
                  <strong>{{ item.executeNo || '-' }}</strong>
                  <el-tag v-if="item.executeStatus != null" size="small" effect="light">
                    {{ resolveErpAuditStatusLabel(item.executeStatus) }}
                  </el-tag>
                </div>
                <div class="source-batch-trace__grid">
                  <div
                    ><span>库存批次</span><strong>{{ item.batchNo || '-' }}</strong></div
                  >
                  <div
                    ><span>产品</span><strong>{{ item.productName || '-' }}</strong></div
                  >
                  <div
                    ><span>仓库</span><strong>{{ item.warehouseName || '-' }}</strong></div
                  >
                  <div
                    ><span>数量</span><strong>{{ formatCount(item.count) }}</strong></div
                  >
                  <div
                    ><span>入库时间</span
                    ><strong>{{ formatTraceDate(item.inboundTime) }}</strong></div
                  >
                  <div
                    ><span>备注</span
                    ><strong>{{ item.executeRemark || item.remark || '-' }}</strong></div
                  >
                </div>
              </article>
            </div>
            <div v-else class="source-batch-trace__empty">
              <Icon icon="ep:box" />
              <div>暂无追溯记录</div>
            </div>
          </section>
        </template>
      </main>
      <footer class="source-batch-trace__footer">
        <el-button @click="visible = false">关闭</el-button>
      </footer>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { resolveErpAuditStatusLabel } from '@/utils/erpAuditStatus'
import {
  PurchaseSourceBatchApi,
  type PurchaseSourceBatchTraceItemVO,
  type PurchaseSourceBatchTraceVO
} from '@/api/erp/purchase/sourceBatch'
import { isCurrentPurchaseInSourceBatchTraceRequest } from '../purchaseInSourceBatchTrace.helpers'

defineOptions({ name: 'PurchaseInSourceBatchTraceDrawer' })

const visible = ref(false)
const loading = ref(false)
const error = ref(false)
const sourceBatchId = ref<number>()
const requestToken = ref(0)
const traceData = ref<PurchaseSourceBatchTraceVO>()
const sourceBatch = computed(() => traceData.value?.sourceBatch)
const traceItems = computed<PurchaseSourceBatchTraceItemVO[]>(
  () => traceData.value?.traceItems || []
)

const loadTrace = async () => {
  const id = sourceBatchId.value
  if (!id) return
  const currentRequestToken = ++requestToken.value
  loading.value = true
  error.value = false
  try {
    const nextTraceData = (await PurchaseSourceBatchApi.getTrace(id)) || {
      traceItems: []
    }
    if (
      !isCurrentPurchaseInSourceBatchTraceRequest({
        requestToken: currentRequestToken,
        activeRequestToken: requestToken.value,
        requestSourceBatchId: id,
        activeSourceBatchId: sourceBatchId.value
      })
    ) {
      return
    }
    traceData.value = nextTraceData
  } catch {
    if (
      isCurrentPurchaseInSourceBatchTraceRequest({
        requestToken: currentRequestToken,
        activeRequestToken: requestToken.value,
        requestSourceBatchId: id,
        activeSourceBatchId: sourceBatchId.value
      })
    ) {
      error.value = true
    }
  } finally {
    if (
      isCurrentPurchaseInSourceBatchTraceRequest({
        requestToken: currentRequestToken,
        activeRequestToken: requestToken.value,
        requestSourceBatchId: id,
        activeSourceBatchId: sourceBatchId.value
      })
    ) {
      loading.value = false
    }
  }
}

const open = async (id: number) => {
  requestToken.value += 1
  sourceBatchId.value = id
  traceData.value = undefined
  error.value = false
  visible.value = true
  await loadTrace()
}

const handleClosed = () => {
  requestToken.value += 1
  sourceBatchId.value = undefined
  traceData.value = undefined
  error.value = false
  loading.value = false
}

const formatCount = (value?: number) => (value == null ? '-' : String(Number(value)))
const formatTraceDate = (value?: string) =>
  value ? formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss') : '-'

defineExpose({ open })
</script>

<style scoped lang="scss">
:deep(.purchase-in-source-batch-trace-drawer .el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

.source-batch-trace {
  display: flex;
  height: 100%;
  flex-direction: column;
  color: var(--erp-slate-800);
  background: var(--erp-slate-50);
}

.source-batch-trace__header {
  display: flex;
  padding: 20px;
  color: var(--erp-surface-white);
  background: var(--erp-slate-900);
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.source-batch-trace__title {
  font-size: 18px;
  font-weight: 700;
  line-height: 26px;
}

.source-batch-trace__batch-no {
  margin-top: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 13px;
  line-height: 20px;
  color: var(--erp-blue-200);
  word-break: break-all;
}

.source-batch-trace__body {
  min-height: 0;
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.source-batch-trace__section,
.source-batch-trace__item {
  background: var(--erp-surface-white);
  border: 1px solid var(--erp-slate-200);
  border-radius: 8px;
  box-shadow: var(--erp-shadow-sm);
}

.source-batch-trace__section {
  padding: 16px;
}

.source-batch-trace__section + .source-batch-trace__section {
  margin-top: 12px;
}

.source-batch-trace__section-title {
  font-size: 14px;
  font-weight: 700;
  line-height: 22px;
  color: var(--erp-slate-900);
}

.source-batch-trace__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.source-batch-trace__grid div {
  min-width: 0;
}

.source-batch-trace__grid span {
  display: block;
  font-size: 12px;
  line-height: 18px;
  color: var(--erp-slate-500);
}

.source-batch-trace__grid strong {
  display: block;
  margin-top: 2px;
  font-size: 13px;
  line-height: 20px;
  color: var(--erp-slate-800);
  overflow-wrap: anywhere;
}

.source-batch-trace__items {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.source-batch-trace__item {
  padding: 12px;
  background: var(--erp-slate-50);
}

.source-batch-trace__item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.source-batch-trace__item-head strong {
  min-width: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow-wrap: anywhere;
}

.source-batch-trace__empty {
  display: flex;
  min-height: 140px;
  font-size: 13px;
  color: var(--erp-slate-500);
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
}

.source-batch-trace__empty--error {
  color: var(--erp-danger-600);
}

.source-batch-trace__footer {
  display: flex;
  padding: 12px 16px;
  background: var(--erp-surface-white);
  border-top: 1px solid var(--erp-slate-200);
  justify-content: flex-end;
}

@media (width <= 640px) {
  :deep(.purchase-in-source-batch-trace-drawer) {
    width: 100vw !important;
  }

  .source-batch-trace__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
