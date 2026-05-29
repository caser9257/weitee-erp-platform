<template>
  <div class="space-y-6">
    <!-- 资产负债表 T型结构对照 -->
    <div v-if="statement" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 左: 资产 -->
      <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
        <div class="border-b border-slate-100 pb-3 flex justify-between items-center">
          <h4 class="font-bold text-slate-800 text-sm flex items-center">
            <span class="w-1.5 h-3.5 bg-indigo-500 rounded-full mr-2"></span>资产方 (Assets)
          </h4>
          <span class="text-xs text-slate-400">流动及固定资产</span>
        </div>
        <div class="space-y-3.5 text-xs">
          <template v-for="item in assetItems" :key="item.itemId">
            <div v-if="item.isCategory" class="font-bold text-slate-500 bg-slate-50 p-2 rounded-lg">
              {{ item.itemName }}
            </div>
            <div v-else class="flex justify-between items-center px-2 py-1 border-b border-slate-50">
              <span class="text-slate-600">{{ item.itemName }}</span>
              <span class="font-mono font-bold text-slate-800">{{ formatAmount(item.amount) }}</span>
            </div>
          </template>
          <div class="p-3 bg-indigo-50/70 border border-indigo-100/50 rounded-xl flex justify-between items-center font-bold text-indigo-900 mt-6 text-sm">
            <span>资产总计</span>
            <span class="font-mono">{{ formatAmount(statement.assetAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 右: 负债与权益 -->
      <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
        <div class="border-b border-slate-100 pb-3 flex justify-between items-center">
          <h4 class="font-bold text-slate-800 text-sm flex items-center">
            <span class="w-1.5 h-3.5 bg-rose-500 rounded-full mr-2"></span>负债及权益方 (Liabilities & Equity)
          </h4>
          <span class="text-xs text-slate-400">权益和债务对账</span>
        </div>
        <div class="space-y-3.5 text-xs">
          <template v-for="item in liabilityEquityItems" :key="item.itemId">
            <div v-if="item.isCategory" class="font-bold text-slate-500 bg-slate-50 p-2 rounded-lg">
              {{ item.itemName }}
            </div>
            <div v-else class="flex justify-between items-center px-2 py-1 border-b border-slate-50">
              <span class="text-slate-600">{{ item.itemName }}</span>
              <span class="font-mono font-bold text-slate-800">{{ formatAmount(item.amount) }}</span>
            </div>
          </template>
          <div :class="['p-3 border rounded-xl flex justify-between items-center font-bold mt-6 text-sm', isBalanced ? 'bg-emerald-50 text-emerald-900 border-emerald-100' : 'bg-rose-50 text-rose-900 border-rose-100']">
            <span>负债与权益总计</span>
            <span class="font-mono">{{ formatAmount(liabilityAndEquityTotal) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 利润表样式 -->
    <div v-if="statement && statement.revenueAmount !== undefined" class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-6">
      <div class="border-b border-slate-100 pb-3">
        <h3 class="font-bold text-slate-900 text-sm">报表明细</h3>
        <p class="text-[11px] text-slate-400 mt-0.5">保持项目、分类与金额的分层表达</p>
      </div>

      <div class="space-y-4">
        <div class="flex justify-between items-center p-3.5 bg-slate-50/70 hover:bg-slate-50 rounded-xl transition-colors">
          <div class="space-y-0.5">
            <div class="text-xs font-bold text-slate-900">营业收入</div>
            <div class="text-[10px] text-slate-400">主营业务销售产生的收入总额</div>
          </div>
          <span class="font-mono font-bold text-slate-900 text-sm">{{ formatAmount(statement.revenueAmount) }}</span>
        </div>

        <div class="pl-6 space-y-3 border-l-2 border-slate-100">
          <div class="flex justify-between items-center text-xs">
            <span class="text-slate-500">减：成本费用</span>
            <span class="font-mono text-slate-700 font-semibold">{{ formatAmount(statement.costExpenseAmount) }}</span>
          </div>
        </div>

        <div class="flex justify-between items-center p-3.5 bg-emerald-50 rounded-xl border border-emerald-100">
          <div class="space-y-0.5">
            <div class="text-xs font-bold text-emerald-900">本期净利润</div>
            <div class="text-[10px] text-emerald-600">税后可供分配的净利润</div>
          </div>
          <span class="font-mono font-extrabold text-emerald-700 text-base">{{ formatAmount(statement.profitAmount) }}</span>
        </div>
      </div>

      <!-- 明细表格 -->
      <div v-if="statement.items?.length" class="overflow-x-auto">
        <table class="w-full text-xs text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/80 border-b border-slate-200 text-slate-500 font-bold">
              <th class="px-4 py-3">项目</th>
              <th class="px-4 py-3">分类</th>
              <th class="px-4 py-3 text-right">金额</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="item in statement.items" :key="item.itemId" class="hover:bg-slate-50/50 transition-colors">
              <td class="px-4 py-3">
                <div class="flex items-center space-x-2">
                  <span class="text-slate-800 font-bold">{{ item.itemName || '-' }}</span>
                  <span class="font-mono text-slate-400 text-[10px]">{{ item.itemCode || '-' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-slate-600">{{ item.itemCategoryName || '-' }}</td>
              <td class="px-4 py-3 text-right font-mono font-bold text-slate-900">{{ formatAmount(item.amount) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 现金流量表样式 -->
    <div v-if="statement && statement.cashInflowAmount !== undefined" class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-6">
      <div class="border-b border-slate-100 pb-3">
        <h3 class="font-bold text-slate-900 text-sm">现金流量表明细</h3>
        <p class="text-[11px] text-slate-400 mt-0.5">根据现金收付原则自动统筹计算</p>
      </div>

      <div class="space-y-5 text-xs">
        <div class="space-y-3">
          <div class="font-bold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-lg flex justify-between items-center">
            <span>一、经营活动现金流入</span>
            <span class="font-mono font-black">{{ formatAmount(statement.cashInflowAmount) }}</span>
          </div>
        </div>

        <div class="space-y-3">
          <div class="font-bold text-rose-700 bg-rose-50 px-3 py-1.5 rounded-lg flex justify-between items-center">
            <span>二、经营活动现金流出</span>
            <span class="font-mono font-black">({{ formatAmount(statement.cashOutflowAmount) }})</span>
          </div>
        </div>

        <div class="p-4 bg-indigo-50 border border-indigo-100 rounded-xl flex justify-between items-center font-bold text-sm text-indigo-950 mt-8">
          <span>三、经营活动现金流量净额</span>
          <span class="font-mono text-base">{{ formatAmount(statement.netCashFlowAmount) }}</span>
        </div>
      </div>

      <!-- 明细表格 -->
      <div v-if="statement.items?.length" class="overflow-x-auto">
        <table class="w-full text-xs text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/80 border-b border-slate-200 text-slate-500 font-bold">
              <th class="px-4 py-3">项目</th>
              <th class="px-4 py-3">分类</th>
              <th class="px-4 py-3 text-right">金额</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="item in statement.items" :key="item.itemId" class="hover:bg-slate-50/50 transition-colors">
              <td class="px-4 py-3">
                <div class="flex items-center space-x-2">
                  <span class="text-slate-800 font-bold">{{ item.itemName || '-' }}</span>
                  <span class="font-mono text-slate-400 text-[10px]">{{ item.itemCode || '-' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-slate-600">{{ item.itemCategoryName || '-' }}</td>
              <td class="px-4 py-3 text-right font-mono font-bold text-slate-900">{{ formatAmount(item.amount) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 加载和空状态 -->
    <div v-if="loading" class="bg-white border border-slate-200 rounded-2xl p-12 shadow-sm flex items-center justify-center">
      <div class="text-center">
        <svg class="w-8 h-8 text-indigo-500 animate-spin mx-auto mb-3" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" />
        </svg>
        <p class="text-xs text-slate-500">报表加载中...</p>
      </div>
    </div>

    <el-result v-if="error" icon="error" title="报表加载失败">
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重试</el-button>
      </template>
    </el-result>

    <el-empty v-if="!loading && !statement && !error" description="暂无数据" />
  </div>
</template>

<script setup lang="ts">
import { ErpFinanceStatementVO } from '@/api/erp/finance/report'
import { formatAmount } from '@/views/erp/finance/shared/accounting'

defineOptions({ name: 'ErpFinanceStatementPane' })

interface StatementItemWithCategory {
  itemId?: number
  itemCode?: string
  itemName?: string
  itemCategory?: number
  itemCategoryName?: string
  amount?: number | string
  sort?: number
  isCategory?: boolean
}

const props = defineProps<{
  loading: boolean
  statement?: ErpFinanceStatementVO
  error?: string
}>()

defineEmits<{
  (event: 'retry'): void
}>()

// 负债及权益总计
const liabilityAndEquityTotal = computed(() => {
  if (!props.statement) return 0
  return Number(props.statement.liabilityAmount || 0) + Number(props.statement.equityAmount || 0)
})

// 是否平衡
const isBalanced = computed(() => {
  if (!props.statement) return false
  return props.statement.balanceSheetBalanced ?? false
})

// 资产类项目（按分类分组）
const assetItems = computed((): StatementItemWithCategory[] => {
  if (!props.statement?.items) return []
  const items = props.statement.items.filter(item =>
    item.itemCategory === 10 || item.itemCategory === 60 // 资产类或现金流入
  )
  return groupByCategory(items)
})

// 负债权益类项目（按分类分组）
const liabilityEquityItems = computed((): StatementItemWithCategory[] => {
  if (!props.statement?.items) return []
  const items = props.statement.items.filter(item =>
    item.itemCategory === 20 || item.itemCategory === 30 || item.itemCategory === 40 || item.itemCategory === 50 || item.itemCategory === 70
  )
  return groupByCategory(items)
})

// 按分类分组
const groupByCategory = (items: ErpFinanceStatementVO['items'] = []): StatementItemWithCategory[] => {
  const result: StatementItemWithCategory[] = []
  const categoryMap = new Map<number, StatementItemWithCategory[]>()

  items.forEach(item => {
    const category = item.itemCategory || 0
    if (!categoryMap.has(category)) {
      categoryMap.set(category, [])
    }
    categoryMap.get(category)!.push(item)
  })

  const categoryNames: Record<number, string> = {
    10: '流动资产',
    20: '流动负债',
    30: '所有者权益',
    40: '收入',
    50: '成本费用',
    60: '现金流入',
    70: '现金流出'
  }

  categoryMap.forEach((categoryItems, categoryId) => {
    result.push({
      itemId: -categoryId,
      itemName: categoryNames[categoryId] || `分类 ${categoryId}`,
      isCategory: true
    })
    result.push(...categoryItems)
  })

  return result
}
</script>
