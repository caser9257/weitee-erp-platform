<template>
  <el-result v-if="error" icon="error" title="报表加载失败">
    <template #extra>
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
    </template>
  </el-result>
  <template v-else>
    <el-empty v-if="!loading && !statement" description="暂无数据" />
    <template v-else>
      <div class="finance-readonly-page__metric-grid">
        <template
          v-if="
            statement?.assetAmount !== undefined ||
            statement?.liabilityAmount !== undefined ||
            statement?.equityAmount !== undefined
          "
        >
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">资产合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.assetAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">负债合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.liabilityAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">权益合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.equityAmount) }}
            </div>
          </div>
        </template>
        <template v-if="statement?.revenueAmount !== undefined">
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">收入合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.revenueAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">成本费用合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.costExpenseAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">利润合计</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.profitAmount) }}
            </div>
          </div>
        </template>
        <template v-if="statement?.cashInflowAmount !== undefined">
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">现金流入</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.cashInflowAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">现金流出</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.cashOutflowAmount) }}
            </div>
          </div>
          <div class="finance-readonly-page__metric-card">
            <div class="finance-readonly-page__metric-label">现金流量净额</div>
            <div class="finance-readonly-page__metric-value">
              {{ formatAmount(statement?.netCashFlowAmount) }}
            </div>
          </div>
        </template>
      </div>
      <div class="finance-readonly-page__table-wrap">
        <el-table
          v-loading="loading"
          :data="statement?.items || []"
          stripe
          show-overflow-tooltip
        >
          <el-table-column label="项目" min-width="240">
            <template #default="{ row }">
              <div class="finance-readonly-page__primary-cell">
                <span class="finance-readonly-page__primary-text">{{ row.itemName || '-' }}</span>
                <span class="finance-readonly-page__muted-text font-mono">{{ row.itemCode || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" min-width="120">
            <template #default="{ row }">{{ row.itemCategoryName || '-' }}</template>
          </el-table-column>
          <el-table-column label="金额" align="right" min-width="140">
            <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-if="!loading && !statement?.items?.length" description="暂无数据" />
    </template>
  </template>
</template>

<script setup lang="ts">
import { ErpFinanceStatementVO } from '@/api/erp/finance/report'
import { formatAmount } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'ErpFinanceStatementPane' })

defineProps<{
  loading: boolean
  statement?: ErpFinanceStatementVO
  error?: string
}>()

defineEmits<{
  (event: 'retry'): void
}>()
</script>

<style scoped>
@import '../shared/readOnlyPage.css';
</style>
