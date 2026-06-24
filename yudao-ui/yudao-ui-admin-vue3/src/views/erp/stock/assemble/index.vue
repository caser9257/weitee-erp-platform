<template>
<ContentWrap class="stock-assemble-page__hero-card">
    <div class="stock-assemble-page__hero">
      <div class="stock-assemble-page__hero-main">
        <div class="stock-assemble-page__title">组装与拆卸作业台</div>
        <div class="stock-assemble-page__badge-row">
          <span class="stock-assemble-page__badge stock-assemble-page__badge--warning">功能待接�?/span>
          <span class="stock-assemble-page__badge stock-assemble-page__badge--neutral">
            当前未发现已落地的组装拆卸单据流
          </span>
        </div>
      </div>
      <div class="stock-assemble-page__hero-actions">
        <el-button
          type="primary"
          :loading="isNavigatingStock"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('stock')"
        >
          <Icon icon="ep:goods" class="mr-5px" /> 库存台账
        </el-button>
        <el-button
          :loading="isNavigatingRecord"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('record')"
        >
          <Icon icon="ep:document" class="mr-5px" /> 库存流水
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="stock-assemble-page__entry-card">
    <div class="stock-assemble-page__section-head">
      <div class="stock-assemble-page__section-title">相关库存入口</div>
      <div class="stock-assemble-page__section-tag">当前可先从以下页面完成库存追�?/div>
    </div>

    <div class="stock-assemble-entry-grid">
      <section class="stock-assemble-entry-card">
        <div class="stock-assemble-entry-card__icon stock-assemble-entry-card__icon--primary">
          <Icon icon="ep:goods" />
        </div>
        <div class="stock-assemble-entry-card__content">
          <div class="stock-assemble-entry-card__title">库存台账</div>
          <div class="stock-assemble-entry-card__meta">查看产品在各仓库的当前结�?/div>
        </div>
        <el-button
          type="primary"
          plain
          :loading="isNavigatingStock"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('stock')"
        >
          前往页面
        </el-button>
      </section>

      <section class="stock-assemble-entry-card">
        <div class="stock-assemble-entry-card__icon stock-assemble-entry-card__icon--info">
          <Icon icon="ep:document" />
        </div>
        <div class="stock-assemble-entry-card__content">
          <div class="stock-assemble-entry-card__title">库存流水</div>
          <div class="stock-assemble-entry-card__meta">追踪库存增减来源与业务单�?/div>
        </div>
        <el-button
          type="primary"
          plain
          :loading="isNavigatingRecord"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('record')"
        >
          前往页面
        </el-button>
      </section>

      <section class="stock-assemble-entry-card">
        <div class="stock-assemble-entry-card__icon stock-assemble-entry-card__icon--success">
          <Icon icon="ep:zoom-in" />
        </div>
        <div class="stock-assemble-entry-card__content">
          <div class="stock-assemble-entry-card__title">其他入库</div>
          <div class="stock-assemble-entry-card__meta">处理非采购类库存入库业务</div>
        </div>
        <el-button
          type="success"
          plain
          :loading="isNavigatingStockIn"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('stockIn')"
        >
          前往页面
        </el-button>
      </section>

      <section class="stock-assemble-entry-card">
        <div class="stock-assemble-entry-card__icon stock-assemble-entry-card__icon--warning">
          <Icon icon="ep:zoom-out" />
        </div>
        <div class="stock-assemble-entry-card__content">
          <div class="stock-assemble-entry-card__title">其他出库</div>
          <div class="stock-assemble-entry-card__meta">处理非销售类库存出库业务</div>
        </div>
        <el-button
          type="warning"
          plain
          :loading="isNavigatingStockOut"
          :disabled="isAnyNavigationPending"
          @click="handleNavigate('stockOut')"
        >
          前往页面
        </el-button>
      </section>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({ name: 'ErpStockAssemble' })

type NavigationAction = 'stock' | 'record' | 'stockIn' | 'stockOut'

const router = useRouter()
const message = useMessage()

const navigatingAction = ref<NavigationAction | ''>('')

const navigationPathMap: Record<NavigationAction, string> = {
  stock: '/scm/stock',
  record: '/scm/stock-record',
  stockIn: '/scm/stock-in',
  stockOut: '/scm/stock-out'
}

const isAnyNavigationPending = computed(() => !!navigatingAction.value)
const isNavigatingStock = computed(() => navigatingAction.value === 'stock')
const isNavigatingRecord = computed(() => navigatingAction.value === 'record')
const isNavigatingStockIn = computed(() => navigatingAction.value === 'stockIn')
const isNavigatingStockOut = computed(() => navigatingAction.value === 'stockOut')

const handleNavigate = async (action: NavigationAction) => {
  if (isAnyNavigationPending.value) {
    return
  }
  navigatingAction.value = action
  try {
    await router.push(navigationPathMap[action])
  } catch {
    message.error('页面跳转失败，请重试')
  } finally {
    navigatingAction.value = ''
  }
}
</script>

<style scoped>
.stock-assemble-page__hero-card,
.stock-assemble-page__entry-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
}

.stock-assemble-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.stock-assemble-page__hero-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
}

.stock-assemble-page__title,
.stock-assemble-page__section-title,
.stock-assemble-entry-card__title {
  color: #0f172a;
  font-weight: 700;
}

.stock-assemble-page__title {
  font-size: 24px;
  line-height: 32px;
}

.stock-assemble-page__section-head,
.stock-assemble-page__badge-row,
.stock-assemble-page__hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-assemble-page__section-head {
  margin-bottom: 18px;
  justify-content: space-between;
}

.stock-assemble-page__section-title {
  font-size: 18px;
  line-height: 28px;
}

.stock-assemble-page__section-tag,
.stock-assemble-entry-card__meta {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.stock-assemble-page__badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 13px;
  line-height: 20px;
  font-weight: 600;
}

.stock-assemble-page__badge--warning {
  border-color: #fde68a;
  background: #fffbeb;
  color: #d97706;
}

.stock-assemble-page__badge--neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #475569;
}

.stock-assemble-entry-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.stock-assemble-entry-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.stock-assemble-entry-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 14px 30px rgba(148, 163, 184, 0.12);
  transform: translateY(-1px);
}

.stock-assemble-entry-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  font-size: 22px;
  flex-shrink: 0;
}

.stock-assemble-entry-card__icon--primary {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.stock-assemble-entry-card__icon--info {
  background: rgba(14, 165, 233, 0.1);
  color: #0284c7;
}

.stock-assemble-entry-card__icon--success {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.stock-assemble-entry-card__icon--warning {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.stock-assemble-entry-card__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

@media (max-width: 1280px) {
  .stock-assemble-entry-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .stock-assemble-page__hero-actions {
    width: 100%;
  }

  .stock-assemble-page__hero-actions :deep(.el-button),
  .stock-assemble-entry-card :deep(.el-button) {
    flex: 1;
    min-width: 0;
  }

  .stock-assemble-entry-card {
    align-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
