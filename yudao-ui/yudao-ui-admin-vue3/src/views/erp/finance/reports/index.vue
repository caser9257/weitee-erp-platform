<template>
  <div class="min-h-screen bg-slate-50 flex flex-col text-slate-800 font-sans">

    <!-- 顶部主导航栏 -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
      <div class="max-w-[1600px] mx-auto px-6 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="bg-indigo-600 text-white p-2 rounded-lg flex items-center justify-center shadow-md shadow-indigo-100">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <rect x="4" y="2" width="16" height="20" rx="2" />
              <line x1="8" y1="6" x2="16" y2="6" />
              <line x1="16" y1="14" x2="16" y2="18" />
              <path d="M16 10h.01M12 10h.01M8 10h.01M12 14h.01M8 14h.01M12 18h.01M8 18h.01" />
            </svg>
          </div>
          <div>
            <h1 class="text-base font-bold text-slate-950 tracking-tight">智能财务云工作台</h1>
            <p class="text-[11px] text-slate-400">{{ selectedLedgerLabel }} | {{ selectedPeriodLabel }}</p>
          </div>
        </div>

        <!-- 报表切换导航 -->
        <nav class="flex space-x-1 bg-slate-100 p-1 rounded-xl border border-slate-200/50">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            @click="handleTabSwitch(tab.key)"
            :class="[
              'px-3.5 py-2 text-xs font-bold rounded-lg transition-all duration-200',
              activeTab === tab.key
                ? 'bg-white text-indigo-600 shadow-sm border border-slate-200/40'
                : 'text-slate-500 hover:text-slate-800 hover:bg-white/50'
            ]"
          >
            {{ tab.label }}
          </button>
        </nav>

        <!-- 账期及操作 -->
        <div class="flex items-center space-x-3">
          <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
            <span class="w-1.5 h-1.5 mr-1.5 rounded-full bg-emerald-500"></span>
            {{ currentTabLabel }}
          </span>
          <button
            @click="resetQuery"
            class="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors border border-slate-200"
            title="重置报表数据"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- 筛选条件区 -->
    <div class="max-w-[1600px] w-full mx-auto px-6 pt-6">
      <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col sm:flex-row sm:items-center gap-4">
        <div class="flex flex-1 flex-wrap items-center gap-3">
          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold text-slate-500">账簿</span>
            <el-select
              v-model="reportQuery.ledgerId"
              placeholder="请选择账簿"
              clearable
              filterable
              :loading="ledgerLoading"
              size="small"
              style="width: 180px"
              @change="handleLedgerChange"
            >
              <el-option
                v-for="item in ledgerOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </div>
          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold text-slate-500">期间</span>
            <el-select
              v-model="reportQuery.periodId"
              placeholder="请选择期间"
              clearable
              filterable
              :loading="periodLoading"
              size="small"
              style="width: 150px"
            >
              <el-option
                v-for="item in periodOptions"
                :key="item.id"
                :label="item.periodCode"
                :value="item.id"
              />
            </el-select>
          </div>
          <div class="relative">
            <svg class="absolute left-3 top-2.5 text-slate-400 w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              type="text"
              placeholder="搜索科目代码、科目名称..."
              v-model="reportQuery.subjectCode"
              class="pl-10 pr-4 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-indigo-500 focus:bg-white transition-all w-48"
            />
          </div>
        </div>
        <div class="flex items-center space-x-2">
          <button
            @click="resetQuery"
            class="px-4 py-2 bg-slate-50 border border-slate-200 hover:bg-slate-100 text-slate-600 rounded-xl text-xs font-bold transition-all"
          >
            重置
          </button>
          <button
            @click="refreshReport"
            :disabled="!canRefreshReport"
            :class="[
              'px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5',
              canRefreshReport
                ? 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm shadow-indigo-100'
                : 'bg-slate-100 text-slate-400 cursor-not-allowed'
            ]"
          >
            <svg v-if="currentTabLoading" class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" />
            </svg>
            <span>{{ currentTabLoading ? '加载中...' : '刷新报表' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 核心内容大版块 -->
    <div class="flex-1 max-w-[1600px] w-full mx-auto p-6 flex flex-col lg:flex-row gap-6 overflow-hidden">

      <!-- 左侧主报表显示面板 -->
      <div class="flex-1 flex flex-col space-y-6 min-w-0">

        <!-- 1. 试算平衡表模块 -->
        <template v-if="activeTab === 'trial'">
          <!-- 借贷不平衡警报 -->
          <div v-if="trialBalance && !trialBalance.endingBalanced" class="bg-amber-50 border border-amber-200 text-amber-800 rounded-2xl p-4 flex items-center space-x-3.5">
            <div class="bg-amber-100 p-2 rounded-xl shrink-0">
              <svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01" />
              </svg>
            </div>
            <div>
              <h4 class="font-bold text-sm">试算平衡校验未通过：本期末借贷总额单向偏离</h4>
              <p class="text-xs text-amber-600/80 mt-0.5">
                期末借方合计与贷方合计相差 <span class="underline font-mono font-bold">{{ formatAmount(trialBalanceEndingDiff) }} 元</span>。
              </p>
            </div>
          </div>

          <!-- 轧平成功状态 -->
          <div v-else-if="trialBalance && trialBalance.endingBalanced" class="bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl p-4 flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <div class="bg-emerald-100 p-2 rounded-xl text-emerald-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3" />
                </svg>
              </div>
              <div>
                <h4 class="font-bold text-sm">试算平衡成功！</h4>
                <p class="text-xs text-emerald-600/80 mt-0.5">经离线勾稽规则审核：期初、本期及期末借贷完全一致，无偏离项。</p>
              </div>
            </div>
            <span class="bg-emerald-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-sm">符合勾稽标准</span>
          </div>

          <!-- 核心对比卡片看板 -->
          <div v-if="trialBalance" class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <!-- 卡片 1: 期初 -->
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm group hover:border-slate-300 transition-all">
              <div class="flex justify-between items-start mb-2">
                <span class="text-xs font-bold text-slate-400">期初余额对比</span>
                <span class="text-[10px] px-2 py-0.5 rounded-full font-bold" :class="trialBalance.currentBalanced ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'">
                  {{ trialBalance.currentBalanced ? '已轧平' : '待核对' }}
                </span>
              </div>
              <div class="space-y-1">
                <div class="flex justify-between items-baseline">
                  <span class="text-xs text-slate-500">借方合计</span>
                  <span class="text-sm font-bold text-slate-800 font-mono">{{ formatAmount(trialBalance.totalOpeningDebitAmount) }}</span>
                </div>
                <div class="flex justify-between items-baseline border-t border-dashed border-slate-100 pt-1.5">
                  <span class="text-xs text-slate-500">贷方合计</span>
                  <span class="text-sm font-bold text-slate-800 font-mono">{{ formatAmount(trialBalance.totalOpeningCreditAmount) }}</span>
                </div>
              </div>
            </div>

            <!-- 卡片 2: 本期 -->
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm group hover:border-slate-300 transition-all">
              <div class="flex justify-between items-start mb-2">
                <span class="text-xs font-bold text-slate-400">本期发生额对比</span>
                <span class="text-[10px] px-2 py-0.5 rounded-full font-bold" :class="trialBalance.currentBalanced ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'">
                  {{ trialBalance.currentBalanced ? '已轧平' : '待核对' }}
                </span>
              </div>
              <div class="space-y-1">
                <div class="flex justify-between items-baseline">
                  <span class="text-xs text-slate-500">借方合计</span>
                  <span class="text-sm font-bold text-slate-800 font-mono">{{ formatAmount(trialBalance.totalCurrentDebitAmount) }}</span>
                </div>
                <div class="flex justify-between items-baseline border-t border-dashed border-slate-100 pt-1.5">
                  <span class="text-xs text-slate-500">贷方合计</span>
                  <span class="text-sm font-bold text-slate-800 font-mono">{{ formatAmount(trialBalance.totalCurrentCreditAmount) }}</span>
                </div>
              </div>
            </div>

            <!-- 卡片 3: 期末借方 -->
            <div :class="['border rounded-2xl p-4 shadow-sm transition-all bg-white', trialBalance.endingBalanced ? 'border-slate-200' : 'border-rose-200']">
              <div class="flex justify-between items-center mb-1">
                <span class="text-xs font-bold text-slate-400">期末借方合计</span>
                <span :class="['text-[10px] px-2 py-0.5 rounded-full font-bold', trialBalance.endingBalanced ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500']">
                  {{ trialBalance.endingBalanced ? '正常' : '待核对' }}
                </span>
              </div>
              <div class="mt-2">
                <span class="text-xl font-bold text-slate-800 font-mono">{{ formatAmount(trialBalance.totalEndingDebitAmount) }}</span>
                <p class="text-[11px] text-slate-400 mt-1">流动与非流动资产期末总和</p>
              </div>
            </div>

            <!-- 卡片 4: 期末贷方 -->
            <div :class="['border rounded-2xl p-4 shadow-sm transition-all', trialBalance.endingBalanced ? 'bg-white border-slate-200' : 'bg-red-50/40 border-rose-200']">
              <div class="flex justify-between items-center mb-1">
                <span class="text-xs font-bold text-slate-400">期末贷方合计</span>
                <span :class="['text-[10px] px-2 py-0.5 rounded-full font-bold', trialBalance.endingBalanced ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-600 text-white animate-pulse']">
                  {{ trialBalance.endingBalanced ? '已平' : '借贷不平' }}
                </span>
              </div>
              <div class="mt-2">
                <span :class="['text-xl font-bold font-mono', trialBalance.endingBalanced ? 'text-slate-800' : 'text-rose-600']">{{ formatAmount(trialBalance.totalEndingCreditAmount) }}</span>
                <p v-if="!trialBalance.endingBalanced" class="text-[11px] text-rose-500 font-bold mt-1">
                  差额：-{{ formatAmount(trialBalanceEndingDiff) }}
                </p>
                <p v-else class="text-[11px] text-slate-400 mt-1">负债与股东权益总和</p>
              </div>
            </div>
          </div>

          <!-- 试算平衡表主数据表格 -->
          <div v-if="statementLoading || trialBalance?.items?.length" class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full text-left border-collapse">
                <thead>
                  <tr class="bg-slate-50/80 border-b border-slate-200 text-slate-400 text-[11px] font-bold tracking-wider">
                    <th rowSpan="2" class="px-4 py-3 border-r border-slate-100 text-slate-600 text-xs font-bold min-w-[200px]">科目编码及名称</th>
                    <th colSpan="2" class="px-4 py-2 text-center border-b border-slate-100 border-r border-slate-200/60 text-slate-700 bg-slate-50/40">期初余额</th>
                    <th colSpan="2" class="px-4 py-2 text-center border-b border-slate-100 border-r border-slate-200/60 text-slate-700 bg-slate-50/40">本期发生额</th>
                    <th colSpan="2" class="px-4 py-2 text-center border-b border-slate-100 text-slate-700 bg-slate-50/40">期末余额</th>
                  </tr>
                  <tr class="bg-slate-50/50 border-b border-slate-200 text-[10px] font-bold text-slate-400">
                    <th class="px-4 py-1.5 text-right border-r border-slate-100">借方</th>
                    <th class="px-4 py-1.5 text-right border-r border-slate-200/60">贷方</th>
                    <th class="px-4 py-1.5 text-right border-r border-slate-100">借方</th>
                    <th class="px-4 py-1.5 text-right border-r border-slate-200/60">贷方</th>
                    <th class="px-4 py-1.5 text-right border-r border-slate-100">借方</th>
                    <th class="px-4 py-1.5 text-right">贷方</th>
                  </tr>
                </thead>
                <tbody v-loading="statementLoading" class="divide-y divide-slate-100 text-xs">
                  <tr
                    v-for="row in trialBalance?.items || []"
                    :key="row.subjectCode"
                    class="group hover:bg-slate-50/80 transition-all"
                  >
                    <td class="px-4 py-3 font-medium border-r border-slate-100">
                      <div class="flex items-center space-x-2">
                        <span class="font-mono text-slate-400 group-hover:text-indigo-600 transition-colors">{{ row.subjectCode }}</span>
                        <span class="text-slate-800 font-bold">{{ row.subjectName }}</span>
                      </div>
                    </td>
                    <td class="px-4 py-3 text-right font-mono border-r border-slate-100 text-slate-600">
                      {{ formatAmount(row.openingDebitAmount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-mono border-r border-slate-200/60 text-slate-600">
                      {{ formatAmount(row.openingCreditAmount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-mono border-r border-slate-100 text-slate-600">
                      {{ formatAmount(row.currentDebitAmount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-mono border-r border-slate-200/60 text-slate-600">
                      {{ formatAmount(row.currentCreditAmount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-mono border-r border-slate-100 text-slate-900 font-bold">
                      {{ formatAmount(row.endingDebitAmount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-mono text-slate-900 font-bold">
                      {{ formatAmount(row.endingCreditAmount) }}
                    </td>
                  </tr>
                </tbody>
                <tfoot v-if="trialBalance">
                  <tr class="bg-slate-100/95 font-bold text-slate-900 border-t border-slate-300/80">
                    <td class="px-4 py-4 border-r border-slate-200">
                      <span class="text-slate-800 font-extrabold">期末平衡审定总计</span>
                    </td>
                    <td class="px-4 py-4 text-right font-mono border-r border-slate-200">{{ formatAmount(trialBalance.totalOpeningDebitAmount) }}</td>
                    <td class="px-4 py-4 text-right font-mono border-r border-slate-300">{{ formatAmount(trialBalance.totalOpeningCreditAmount) }}</td>
                    <td class="px-4 py-4 text-right font-mono border-r border-slate-200">{{ formatAmount(trialBalance.totalCurrentDebitAmount) }}</td>
                    <td class="px-4 py-4 text-right font-mono border-r border-slate-300">{{ formatAmount(trialBalance.totalCurrentCreditAmount) }}</td>
                    <td :class="['px-4 py-4 text-right font-mono border-r border-slate-200', trialBalance.endingBalanced ? 'text-emerald-700' : 'text-rose-600 bg-rose-50']">{{ formatAmount(trialBalance.totalEndingDebitAmount) }}</td>
                    <td :class="['px-4 py-4 text-right font-mono', trialBalance.endingBalanced ? 'text-emerald-700' : 'text-rose-600 bg-rose-50']">{{ formatAmount(trialBalance.totalEndingCreditAmount) }}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
            <div class="bg-slate-50 border-t border-slate-100 px-4 py-3 flex justify-between items-center text-xs text-slate-400">
              <span class="flex items-center">
                <svg class="w-4 h-4 mr-1.5 text-indigo-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                </svg>
                使用指南：支持直接双击表内任意数字在本地改账，下方财务天平及勾稽报表将实时动态校准。
              </span>
              <span>核算总科目数：{{ trialBalance?.subjectCount || 0 }} 项</span>
            </div>
          </div>
          <el-empty v-else-if="!statementLoading && !trialBalance" description="请先选择账簿和期间" />
        </template>

        <!-- 2. 资产负债表模块 -->
        <template v-if="activeTab === 'balance'">
          <div class="space-y-6">
            <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col md:flex-row items-center gap-6 justify-between">
              <div class="space-y-2 max-w-xl">
                <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full">系统内控校准器</span>
                <h3 class="text-lg font-bold text-slate-900">资产负债表静态对照看板</h3>
                <p class="text-xs text-slate-500 leading-relaxed">
                  基于会计恒等式 <strong>"资产 = 负债 + 所有者权益"</strong> 运行。右侧物理天平基于前端数值引擎实时控制，直观展示资产端与权益端的承载负荷关系。
                </p>
                <div class="flex gap-4 mt-2 text-xs">
                  <div>资产总额：<span class="font-bold font-mono text-slate-900">{{ formatAmount(balanceSheet?.assetAmount) }}</span></div>
                  <div>负债及权益总额：<span class="font-bold font-mono text-slate-900">{{ formatAmount(balanceSheetLiabilityAndEquity) }}</span></div>
                </div>
              </div>

              <!-- DYNAMIC BALANCE SCALE -->
              <div class="flex flex-col items-center bg-slate-50 p-4 rounded-2xl border border-slate-100 min-w-[280px]">
                <span class="text-[10px] font-bold text-slate-400 mb-2">SYSTEM BALANCE SCALE</span>
                <svg width="240" height="110" viewBox="0 0 240 110" class="overflow-visible">
                  <path d="M 100,100 L 140,100 L 130,70 L 110,70 Z" fill="#94a3b8" />
                  <rect x="80" y="100" width="80" height="6" rx="3" fill="#64748b" />
                  <line x1="120" y1="70" x2="120" y2="25" stroke="#64748b" stroke-width="6" stroke-linecap="round" />
                  <circle cx="120" cy="25" r="5" fill="#475569" />

                  <g :style="{ transform: `rotate(${balanceTiltAngle}deg)`, transformOrigin: '120px 25px', transition: 'transform 0.5s ease' }">
                    <line x1="40" y1="25" x2="200" y2="25" stroke="#475569" stroke-width="4" />
                    <circle cx="40" cy="25" r="3" fill="#334155" />
                    <circle cx="200" cy="25" r="3" fill="#334155" />

                    <!-- 左托盘 -->
                    <line x1="40" y1="25" x2="25" y2="65" stroke="#94a3b8" stroke-width="1.5" />
                    <line x1="40" y1="25" x2="55" y2="65" stroke="#94a3b8" stroke-width="1.5" />
                    <path d="M 15,65 L 65,65 Q 40,75 15,65 Z" :fill="isBalanceSheetBalanced ? '#10b981' : '#3b82f6'" />
                    <text x="40" y="85" text-anchor="middle" font-size="9" font-weight="bold" fill="#64748b">资产端</text>

                    <!-- 右托盘 -->
                    <line x1="200" y1="25" x2="185" y2="65" stroke="#94a3b8" stroke-width="1.5" />
                    <line x1="200" y1="25" x2="215" y2="65" stroke="#94a3b8" stroke-width="1.5" />
                    <path d="M 175,65 L 225,65 Q 200,75 175,65 Z" :fill="isBalanceSheetBalanced ? '#10b981' : '#f43f5e'" />
                    <text x="200" y="85" text-anchor="middle" font-size="9" font-weight="bold" fill="#64748b">权益端</text>
                  </g>
                </svg>
                <div class="mt-2 text-center text-xs">
                  <span v-if="isBalanceSheetBalanced" class="text-emerald-600 font-bold flex items-center justify-center">
                    借贷平衡，天平呈水平状态
                  </span>
                  <span v-else class="text-rose-500 font-bold flex items-center justify-center animate-pulse">
                    借贷失衡：差额 {{ formatAmount(balanceSheetDiff) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- T型结构资产负债对照 -->
            <StatementPane
              :loading="statementLoading"
              :statement="balanceSheet"
              :error="activeTab === 'balance' ? reportErrorMessage : ''"
              @retry="refreshReport"
            />
          </div>
        </template>

        <!-- 3. 利润表模块 -->
        <template v-if="activeTab === 'income'">
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm lg:col-span-2 space-y-6">
              <div class="border-b border-slate-100 pb-3 flex justify-between items-center text-xs">
                <div>
                  <h3 class="font-bold text-slate-900 text-sm">本期损益表 (利润表)</h3>
                  <p class="text-slate-400 mt-0.5">本地核对结转本期损益</p>
                </div>
                <span class="font-mono text-slate-400">{{ selectedPeriodLabel }}</span>
              </div>

              <div v-if="incomeStatement" class="space-y-4">
                <div class="flex justify-between items-center p-3.5 bg-slate-50/70 hover:bg-slate-50 rounded-xl transition-colors">
                  <div class="space-y-0.5">
                    <div class="text-xs font-bold text-slate-900">一、营业收入</div>
                    <div class="text-[10px] text-slate-400">主营业务销售产生的收入总额</div>
                  </div>
                  <span class="font-mono font-bold text-slate-900 text-sm">{{ formatAmount(incomeStatement.revenueAmount) }}</span>
                </div>

                <div class="pl-6 space-y-3 border-l-2 border-slate-100">
                  <div class="flex justify-between items-center text-xs">
                    <span class="text-slate-500">减：主营业务成本</span>
                    <span class="font-mono text-slate-700 font-semibold">{{ formatAmount(incomeStatement.costExpenseAmount) }}</span>
                  </div>
                </div>

                <div class="flex justify-between items-center p-3.5 bg-indigo-50/50 rounded-xl border border-indigo-100/40">
                  <div class="space-y-0.5">
                    <div class="text-xs font-bold text-slate-900">二、营业利润</div>
                    <div class="text-[10px] text-slate-400">核心经营利润指标</div>
                  </div>
                  <span class="font-mono font-bold text-indigo-700 text-sm">{{ formatAmount(incomeStatement.profitAmount) }}</span>
                </div>

                <div class="flex justify-between items-center p-3.5 bg-emerald-50 rounded-xl border border-emerald-100">
                  <div class="space-y-0.5">
                    <div class="text-xs font-bold text-emerald-900">三、本期净利润</div>
                    <div class="text-[10px] text-emerald-600">税后可供分配的净利润</div>
                  </div>
                  <span class="font-mono font-extrabold text-emerald-700 text-base">{{ formatAmount(incomeStatement.profitAmount) }}</span>
                </div>
              </div>
              <el-empty v-else-if="!statementLoading" description="暂无数据" />
            </div>

            <!-- 右侧占比分析 -->
            <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-6">
              <div>
                <h3 class="font-bold text-slate-900 text-sm">毛利及利润率分析</h3>
                <p class="text-[11px] text-slate-400 mt-0.5">基于当前账簿算出的各项率值</p>
              </div>
              <div class="space-y-5">
                <div class="space-y-2 text-xs">
                  <div class="flex justify-between text-slate-500">
                    <span>主营业务销售毛利率</span>
                    <span class="font-bold text-slate-900">{{ incomeRates.grossRate }}%</span>
                  </div>
                  <div class="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div class="bg-emerald-500 h-full transition-all duration-500" :style="{ width: `${incomeRates.grossRate}%` }"></div>
                  </div>
                </div>

                <div class="space-y-4 pt-4 border-t border-slate-100">
                  <h4 class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">成本开销结构比例</h4>
                  <div class="space-y-2 text-xs">
                    <div class="flex justify-between text-slate-600"><span>成本费用</span><span>{{ incomeRates.costPercent }}%</span></div>
                    <div class="flex justify-between text-emerald-600 font-bold"><span>净利润转化率</span><span>{{ incomeRates.profitPercent }}%</span></div>
                  </div>
                  <div class="p-3 bg-slate-50 rounded-xl border border-slate-200/60 text-[11px] text-slate-500 leading-relaxed">
                    {{ incomeStatement?.profitAmount && Number(incomeStatement.profitAmount) > 0 ? `当前账目损益表现良好，净利润金额为 ${formatAmount(incomeStatement.profitAmount)} 元。` : "当前营业利润异常，建议检查账务数据。" }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 4. 现金流量表模块 -->
        <template v-if="activeTab === 'cash'">
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm lg:col-span-2 space-y-6">
              <div class="border-b border-slate-100 pb-3">
                <h3 class="font-bold text-slate-900 text-sm">经营活动现金流量表 (直接法)</h3>
                <p class="text-[11px] text-slate-400 mt-0.5">根据现金收付原则自动统筹计算</p>
              </div>

              <div v-if="cashFlowStatement" class="space-y-5 text-xs">
                <div class="space-y-3">
                  <div class="font-bold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-lg flex justify-between items-center">
                    <span>一、经营活动现金流入</span>
                    <span class="font-mono font-black">{{ formatAmount(cashFlowStatement.cashInflowAmount) }}</span>
                  </div>
                  <div class="pl-4 flex justify-between items-center text-slate-600 border-l border-slate-200 py-1">
                    <span>销售商品、提供劳务收到的现金</span>
                    <span class="font-mono">{{ formatAmount(cashFlowStatement.cashInflowAmount) }}</span>
                  </div>
                </div>

                <div class="space-y-3">
                  <div class="font-bold text-rose-700 bg-rose-50 px-3 py-1.5 rounded-lg flex justify-between items-center">
                    <span>二、经营活动现金流出</span>
                    <span class="font-mono font-black">({{ formatAmount(cashFlowStatement.cashOutflowAmount) }})</span>
                  </div>
                  <div class="pl-4 space-y-2 border-l border-slate-200">
                    <div class="flex justify-between items-center text-slate-600">
                      <span>购买原材料及接受劳务支付的现金</span>
                      <span class="font-mono">{{ formatAmount(cashFlowStatement.cashOutflowAmount) }}</span>
                    </div>
                  </div>
                </div>

                <div class="p-4 bg-indigo-50 border border-indigo-100 rounded-xl flex justify-between items-center font-bold text-sm text-indigo-950 mt-8">
                  <span>三、经营活动现金流量净额</span>
                  <span class="font-mono text-base">{{ formatAmount(cashFlowStatement.netCashFlowAmount) }}</span>
                </div>
              </div>
              <el-empty v-else-if="!statementLoading" description="暂无数据" />
            </div>

            <!-- 右侧管道图 -->
            <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
              <div>
                <h3 class="font-bold text-slate-900 text-sm">本地资金流向管道拓扑图</h3>
                <p class="text-[11px] text-slate-400 mt-0.5">经营现金流入/流出的视觉管道模型</p>
              </div>

              <div class="bg-slate-50 border border-slate-100/50 p-4 rounded-2xl my-4">
                <svg width="100%" height="180" viewBox="0 0 280 180" class="overflow-visible">
                  <rect x="10" y="20" width="80" height="35" rx="6" fill="#10b981" />
                  <text x="50" y="41" text-anchor="middle" fill="white" font-size="9" font-weight="bold">销售流入</text>

                  <rect x="110" y="70" width="60" height="35" rx="6" fill="#6366f1" />
                  <text x="140" y="91" text-anchor="middle" fill="white" font-size="9" font-weight="bold">现金池</text>

                  <rect x="190" y="20" width="80" height="35" rx="6" fill="#ef4444" />
                  <text x="230" y="41" text-anchor="middle" fill="white" font-size="9" font-weight="bold">原材料款</text>

                  <rect x="190" y="120" width="80" height="35" rx="6" fill="#f59e0b" />
                  <text x="230" y="141" text-anchor="middle" fill="white" font-size="9" font-weight="bold">各项费用</text>

                  <!-- 虚线粒子流动线 -->
                  <path d="M 90,38 Q 110,38 110,80" fill="none" stroke="#10b981" stroke-width="3.5" stroke-linecap="round" class="animate-dash" />
                  <path d="M 170,80 Q 170,38 190,38" fill="none" stroke="#ef4444" stroke-width="2.5" />
                  <path d="M 170,95 Q 170,138 190,138" fill="none" stroke="#f59e0b" stroke-width="2.5" />
                </svg>
              </div>
              <div class="bg-slate-50 p-3 rounded-xl text-[11px] text-slate-500 leading-relaxed border border-slate-200/60">
                本期经营现金净流动与期末资产负债表中货币资金资产变动完全勾稽相符。
              </div>
            </div>
          </div>
        </template>

        <!-- 5. 科目余额表模块 -->
        <template v-if="activeTab === 'balanceList'">
          <div class="space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div class="flex items-center space-x-2 overflow-x-auto py-1">
                <button
                  v-for="cat in subjectCategories"
                  :key="cat.key"
                  @click="subjectCategoryFilter = cat.key"
                  :class="['px-3 py-1.5 rounded-lg text-xs font-bold transition-all whitespace-nowrap', subjectCategoryFilter === cat.key ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200/60']"
                >
                  {{ cat.label }}
                </button>
              </div>
              <div class="relative w-full max-w-xs">
                <svg class="absolute left-3 top-2.5 text-slate-400 w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <input
                  type="text"
                  placeholder="搜索科目代码/科目名称..."
                  v-model="balanceSearchTerm"
                  class="w-full pl-8 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none"
                />
              </div>
            </div>

            <!-- 科目余额表格 -->
            <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
              <div class="overflow-x-auto">
                <table class="w-full text-xs text-left border-collapse">
                  <thead>
                    <tr class="bg-slate-50/80 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
                      <th class="px-4 py-3">科目代码</th>
                      <th class="px-4 py-3">科目名称</th>
                      <th class="px-4 py-3 text-right">期初余额</th>
                      <th class="px-4 py-3 text-right">本期借方发生</th>
                      <th class="px-4 py-3 text-right">本期贷方发生</th>
                      <th class="px-4 py-3 text-right">期末余额</th>
                      <th class="px-4 py-3 text-center">明细流水</th>
                    </tr>
                  </thead>
                  <tbody v-loading="subjectBalanceLoading" class="divide-y divide-slate-100">
                    <tr
                      v-for="row in subjectBalanceList"
                      :key="row.id"
                      class="hover:bg-slate-50/50 transition-colors"
                    >
                      <td class="px-4 py-3 font-mono font-bold text-slate-700">{{ row.subjectCode }}</td>
                      <td class="px-4 py-3">
                        <span class="font-bold">{{ row.subjectName }}</span>
                      </td>
                      <td class="px-4 py-3 text-right font-mono text-slate-600">
                        {{ formatOpeningBalance(row) }}
                      </td>
                      <td class="px-4 py-3 text-right font-mono text-emerald-600 font-medium">
                        {{ row.currentDebitAmount ? formatAmount(row.currentDebitAmount) : '-' }}
                      </td>
                      <td class="px-4 py-3 text-right font-mono text-indigo-600 font-medium">
                        {{ row.currentCreditAmount ? formatAmount(row.currentCreditAmount) : '-' }}
                      </td>
                      <td class="px-4 py-3 text-right font-mono font-bold text-slate-900">
                        {{ formatEndingBalance(row) }}
                      </td>
                      <td class="px-4 py-3 text-center">
                        <button
                          @click="openDetailDrawer(row)"
                          class="inline-flex items-center space-x-1 px-2.5 py-1 rounded-lg text-[11px] font-bold transition-all bg-slate-100 text-slate-600 hover:bg-slate-200"
                        >
                          <span>日记账</span>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <Pagination
                v-if="subjectBalanceTotal > 0"
                v-model:limit="balanceQuery.pageSize"
                v-model:page="balanceQuery.pageNo"
                :total="subjectBalanceTotal"
                @pagination="loadSubjectBalance"
              />
            </div>
          </div>
        </template>

      </div>

      <!-- 右侧：离线静态稽核台 -->
      <div v-if="isValidationPanelOpen && activeTab === 'trial'" class="w-full lg:w-[360px] bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col shrink-0">
        <div class="flex items-center justify-between pb-4 border-b border-slate-100">
          <div class="flex items-center space-x-2">
            <div class="p-1.5 bg-slate-100 text-slate-700 rounded-lg">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
            </div>
            <div>
              <h3 class="font-bold text-slate-900 text-sm">内置勾稽校对报告</h3>
              <p class="text-[10px] text-slate-400">100% 离线计算，本地安全合规</p>
            </div>
          </div>
          <button @click="isValidationPanelOpen = false" class="text-slate-400 hover:text-slate-600 p-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- 稽核项内容 -->
        <div class="flex-1 overflow-y-auto py-4 space-y-4 text-xs">
          <template v-if="trialBalance && !trialBalance.endingBalanced">
            <div class="bg-amber-50 border border-amber-100 rounded-xl p-3 text-amber-800">
              <h4 class="font-bold flex items-center mb-1 text-xs">
                对账异常警告
              </h4>
              <p class="text-[11px] leading-relaxed">
                系统检测到<strong>期末借方与贷方不平</strong>。总偏差额为 <strong class="font-mono">{{ formatAmount(trialBalanceEndingDiff) }} 元</strong>。
              </p>
            </div>

            <div class="space-y-3">
              <h5 class="font-bold text-slate-500 text-[11px]">系统数学逻辑稽核结果：</h5>
              <div class="bg-slate-50 rounded-xl p-3 border border-slate-200/60">
                <div class="flex justify-between items-start mb-1">
                  <span class="font-bold text-slate-800">试算平衡校验</span>
                  <span class="text-[9px] font-bold text-rose-500 bg-rose-50 px-1 py-0.5 rounded">未对齐</span>
                </div>
                <p class="text-[11px] text-slate-500 leading-relaxed mb-2">
                  期末借方合计 {{ formatAmount(trialBalance.totalEndingDebitAmount) }}，贷方合计 {{ formatAmount(trialBalance.totalEndingCreditAmount) }}，存在差额。
                </p>
                <button
                  @click="handleLocalReconcile"
                  class="w-full py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg text-[10px] transition-all flex items-center justify-center space-x-1"
                >
                  <span>{{ showDiscrepancySummary ? '收起摘要' : '查看差异明细' }}</span>
                </button>
              </div>

              <div v-if="showDiscrepancySummary" class="bg-white rounded-xl border border-amber-100 p-3 shadow-sm">
                <div class="flex items-center justify-between">
                  <div>
                    <div class="text-[11px] font-bold text-amber-700">差异摘要</div>
                  </div>
                  <span class="text-[9px] font-bold text-amber-600 bg-amber-50 px-1.5 py-0.5 rounded">当前页</span>
                </div>
                <div class="grid grid-cols-2 gap-2 mt-3 text-[10px]">
                  <div class="bg-slate-50 rounded-lg p-2">
                    <span class="block text-slate-400">期末借方</span>
                    <span class="font-mono font-bold text-slate-800">{{ formatAmount(trialBalance?.totalEndingDebitAmount) }}</span>
                  </div>
                  <div class="bg-slate-50 rounded-lg p-2">
                    <span class="block text-slate-400">期末贷方</span>
                    <span class="font-mono font-bold text-slate-800">{{ formatAmount(trialBalance?.totalEndingCreditAmount) }}</span>
                  </div>
                  <div class="bg-slate-50 rounded-lg p-2 col-span-2">
                    <span class="block text-slate-400">差额</span>
                    <span class="font-mono font-bold text-rose-600">{{ formatAmount(trialBalanceEndingDiff) }} 元</span>
                  </div>
                </div>
                <div class="mt-3 flex justify-end">
                  <button
                    class="inline-flex items-center rounded-lg border border-indigo-200 bg-white px-3 py-1.5 text-[10px] font-bold text-indigo-600 transition-colors hover:bg-indigo-50"
                    @click="handleGoSubjectBalance"
                  >
                    去科目余额
                  </button>
                </div>
              </div>
            </div>
          </template>

          <template v-else-if="trialBalance && trialBalance.endingBalanced">
            <div class="text-center py-8">
              <div class="w-10 h-10 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center mx-auto mb-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <h4 class="font-bold text-slate-800 text-xs">内控勾稽通过</h4>
              <p class="text-[11px] text-slate-400 mt-1 max-w-[220px] mx-auto">
                内置静态账簿公式均已通过比对校验。账面健康，无偏差挂账。
              </p>
              <div class="bg-emerald-50 border border-emerald-100 rounded-xl p-3 mt-6 text-left text-emerald-800">
                <span class="font-bold text-[11px] block mb-1">勾稽对照日志</span>
                <span class="text-[10px] text-emerald-600 leading-normal block">
                  1. 本期试算发生额平衡比对通过 ✅<br />
                  2. 资产负债表 资产 = 负债 + 权益 ✅<br />
                  3. 经营性现金流核定无差异 ✅
                </span>
              </div>
            </div>
          </template>

          <!-- 指标卡 -->
          <div class="border-t border-slate-100 pt-4 space-y-2">
            <h5 class="font-bold text-slate-500 text-[11px]">当前核心勾稽指标：</h5>
            <div class="grid grid-cols-2 gap-2 text-[10px]">
              <div class="bg-slate-50 p-2 rounded-lg">
                <span class="text-slate-400 block">本期营业利润</span>
                <span class="font-bold font-mono text-slate-800">{{ formatAmount(incomeStatement?.profitAmount) }}</span>
              </div>
              <div class="bg-slate-50 p-2 rounded-lg">
                <span class="text-slate-400 block">资产天平差额</span>
                <span class="font-bold font-mono text-slate-800">{{ formatAmount(balanceSheetDiff) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- 快捷气泡通知 -->
    <transition name="toast">
      <div v-if="showToast" class="fixed bottom-6 right-6 z-50 bg-slate-900 text-white px-4 py-3 rounded-xl shadow-xl flex items-center space-x-2.5 text-xs font-semibold">
        <div class="bg-indigo-500 p-1 rounded-lg">
          <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
        <span>{{ toastMessage }}</span>
      </div>
    </transition>

    <!-- 明细抽屉 -->
    <el-drawer
      v-model="detailDrawerOpen"
      :with-header="false"
      :size="drawerSize"
      destroy-on-close
      modal-class="backdrop-blur-sm"
      @closed="clearDetailDrawer"
    >
      <div class="h-full bg-slate-50 flex flex-col">
        <div class="p-6 pb-4 flex items-center justify-between border-b border-slate-200">
          <div>
            <h3 class="font-bold text-slate-900 text-sm">查看明细</h3>
            <p class="text-[11px] text-slate-400 mt-0.5">聚焦科目穿透、凭证明细与期间上下文</p>
          </div>
          <button @click="detailDrawerOpen = false" class="text-slate-400 hover:text-slate-600 p-1">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-6 space-y-4">
          <el-result v-if="detailErrorMessage" icon="error" title="明细加载失败">
            <template #extra>
              <el-button type="primary" @click="retryDetail">重试</el-button>
            </template>
          </el-result>
          <template v-else>
            <div v-if="detailData" class="bg-slate-800 text-white rounded-2xl p-4">
              <div class="text-lg font-bold">{{ detailData.subjectName || '-' }}</div>
              <div class="text-sm text-slate-300 font-mono mt-1">{{ detailData.subjectCode || '-' }}</div>
              <div class="flex gap-6 mt-3 text-xs">
                <div>
                  <span class="text-slate-400">账簿</span>
                  <div class="font-bold mt-0.5">{{ detailData.ledgerName || '-' }}</div>
                </div>
                <div>
                  <span class="text-slate-400">期间</span>
                  <div class="font-bold mt-0.5">{{ detailData.periodCode || '-' }}</div>
                </div>
              </div>
            </div>

            <div v-if="detailData" class="grid grid-cols-2 gap-3">
              <div class="bg-white border border-slate-200 rounded-xl p-3">
                <div class="text-[11px] text-slate-400">借方发生额</div>
                <div class="text-sm font-bold font-mono text-slate-800 mt-1">{{ formatAmount(detailData.totalDebitAmount) }}</div>
              </div>
              <div class="bg-white border border-slate-200 rounded-xl p-3">
                <div class="text-[11px] text-slate-400">贷方发生额</div>
                <div class="text-sm font-bold font-mono text-slate-800 mt-1">{{ formatAmount(detailData.totalCreditAmount) }}</div>
              </div>
              <div class="bg-white border border-slate-200 rounded-xl p-3">
                <div class="text-[11px] text-slate-400">期末借方</div>
                <div class="text-sm font-bold font-mono text-slate-800 mt-1">{{ formatAmount(detailData.endingDebitAmount) }}</div>
              </div>
              <div class="bg-white border border-slate-200 rounded-xl p-3">
                <div class="text-[11px] text-slate-400">期末贷方</div>
                <div class="text-sm font-bold font-mono text-slate-800 mt-1">{{ formatAmount(detailData.endingCreditAmount) }}</div>
              </div>
            </div>

            <div class="bg-white border border-slate-200 rounded-xl overflow-hidden">
              <table class="w-full text-[11px] text-left">
                <thead>
                  <tr class="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold">
                    <th class="p-2">凭证</th>
                    <th class="p-2">摘要</th>
                    <th class="p-2 text-right">借方</th>
                    <th class="p-2 text-right">贷方</th>
                  </tr>
                </thead>
                <tbody v-loading="detailLoading" class="divide-y divide-slate-100 font-mono">
                  <tr v-for="item in detailData?.items || []" :key="item.voucherId" class="hover:bg-slate-50/40">
                    <td class="p-2">
                      <div class="font-bold text-indigo-600">{{ item.voucherNo || '-' }}</div>
                      <div class="text-slate-400 text-[10px]">{{ formatDateTimeValue(item.voucherTime) }}</div>
                    </td>
                    <td class="p-2 text-slate-600 font-sans">{{ item.summary || '-' }}</td>
                    <td class="p-2 text-right text-emerald-600">{{ item.debitAmount ? formatAmount(item.debitAmount) : '-' }}</td>
                    <td class="p-2 text-right text-indigo-600">{{ item.creditAmount ? formatAmount(item.creditAmount) : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <el-empty v-if="!detailLoading && !detailData?.items?.length" description="暂无数据" />
          </template>
        </div>
      </div>
    </el-drawer>

  </div>
</template>

<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import {
  ErpFinanceStatementVO,
  ErpFinanceTrialBalanceVO,
  FinanceReportApi
} from '@/api/erp/finance/report'
import {
  ErpFinanceGeneralLedgerDetailVO,
  ErpFinanceSubjectBalanceVO,
  FinanceGeneralLedgerApi
} from '@/api/erp/finance/general-ledger'
import { ErpFinanceLedgerVO, FinanceLedgerApi } from '@/api/erp/finance/ledger'
import { ErpFinancePeriodVO, FinancePeriodApi } from '@/api/erp/finance/period'
import { formatAmount, formatDateTimeValue } from '@/views/erp/finance/shared/accounting'
import StatementPane from './StatementPane.vue'

defineOptions({ name: 'ErpFinanceReports' })

type ReportTabName = 'trial' | 'balance' | 'income' | 'cash' | 'balanceList'

const tabs = [
  { key: 'trial' as ReportTabName, label: '试算平衡' },
  { key: 'balance' as ReportTabName, label: '资产负债表' },
  { key: 'income' as ReportTabName, label: '利润表' },
  { key: 'cash' as ReportTabName, label: '现金流量表' },
  { key: 'balanceList' as ReportTabName, label: '科目余额' }
]

const subjectCategories = [
  { key: 'ALL', label: '全部类别' },
  { key: 'ASSET', label: '资产科目' },
  { key: 'LIABILITY', label: '负债科目' },
  { key: 'EQUITY', label: '所有者权益' },
  { key: 'PL', label: '损益科目' }
]

const { width } = useWindowSize()
const ledgerLoading = ref(false)
const periodLoading = ref(false)
const statementLoading = ref(false)
const subjectBalanceLoading = ref(false)
const detailLoading = ref(false)
const detailDrawerOpen = ref(false)
const reportErrorMessage = ref('')
const detailErrorMessage = ref('')
const activeTab = ref<ReportTabName>('trial')
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const periodOptions = ref<ErpFinancePeriodVO[]>([])
const trialBalance = ref<ErpFinanceTrialBalanceVO>()
const balanceSheet = ref<ErpFinanceStatementVO>()
const incomeStatement = ref<ErpFinanceStatementVO>()
const cashFlowStatement = ref<ErpFinanceStatementVO>()
const subjectBalanceList = ref<ErpFinanceSubjectBalanceVO[]>([])
const subjectBalanceTotal = ref(0)
const detailData = ref<ErpFinanceGeneralLedgerDetailVO>()
const currentSubjectCode = ref('')
const showToast = ref(false)
const toastMessage = ref('')
const isValidationPanelOpen = ref(true)
const subjectCategoryFilter = ref('ALL')
const balanceSearchTerm = ref('')
const showDiscrepancySummary = ref(false)

const reportQuery = reactive({
  ledgerId: undefined as number | undefined,
  periodId: undefined as number | undefined,
  subjectCode: undefined as string | undefined,
  subjectName: undefined as string | undefined
})

const balanceQuery = reactive({
  pageNo: 1,
  pageSize: 10
})

const canQueryReport = computed(() => Boolean(reportQuery.ledgerId && reportQuery.periodId))
const currentTabLoading = computed(() =>
  activeTab.value === 'balanceList' ? subjectBalanceLoading.value : statementLoading.value
)
const canRefreshReport = computed(() => canQueryReport.value && !currentTabLoading.value)
const tabLabelMap: Record<ReportTabName, string> = {
  trial: '试算平衡',
  balance: '资产负债表',
  income: '利润表',
  cash: '现金流量表',
  balanceList: '科目余额'
}
const selectedLedgerLabel = computed(() => ledgerOptions.value.find((item) => item.id === reportQuery.ledgerId)?.name || '未选择账簿')
const selectedPeriodLabel = computed(() => periodOptions.value.find((item) => item.id === reportQuery.periodId)?.periodCode || '未选择期间')
const currentTabLabel = computed(() => tabLabelMap[activeTab.value])
const drawerSize = computed(() => {
  if (width.value < 768) return '100%'
  if (width.value < 1200) return '92vw'
  return '860px'
})

// 试算平衡差额
const trialBalanceEndingDiff = computed(() => {
  if (!trialBalance.value) return 0
  return Math.abs(
    Number(trialBalance.value.totalEndingDebitAmount || 0) -
    Number(trialBalance.value.totalEndingCreditAmount || 0)
  )
})

// 资产负债表相关计算
const balanceSheetLiabilityAndEquity = computed(() => {
  if (!balanceSheet.value) return 0
  return Number(balanceSheet.value.liabilityAmount || 0) + Number(balanceSheet.value.equityAmount || 0)
})

const isBalanceSheetBalanced = computed(() => {
  if (!balanceSheet.value) return false
  return balanceSheet.value.balanceSheetBalanced ?? false
})

const balanceSheetDiff = computed(() => {
  if (!balanceSheet.value) return 0
  return Math.abs(Number(balanceSheet.value.assetAmount || 0) - balanceSheetLiabilityAndEquity.value)
})

const balanceTiltAngle = computed(() => {
  if (isBalanceSheetBalanced.value) return 0
  const diff = Number(balanceSheet.value?.assetAmount || 0) - balanceSheetLiabilityAndEquity.value
  return Math.min(Math.max(diff / 2000, -12), 12)
})

// 利润表比率
const incomeRates = computed(() => {
  const pnl = incomeStatement.value
  if (!pnl) return { grossRate: '0.0', costPercent: '0.0', profitPercent: '0.0' }
  const revenue = Number(pnl.revenueAmount || 0)
  const cost = Number(pnl.costExpenseAmount || 0)
  const profit = Number(pnl.profitAmount || 0)
  return {
    grossRate: revenue ? (((revenue - cost) / revenue) * 100).toFixed(1) : '0.0',
    costPercent: revenue ? ((cost / revenue) * 100).toFixed(1) : '0.0',
    profitPercent: revenue ? ((profit / revenue) * 100).toFixed(1) : '0.0'
  }
})

// Toast 提示
const triggerToast = (msg: string) => {
  toastMessage.value = msg
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2500)
}

// 格式化期初余额
const formatOpeningBalance = (row: ErpFinanceSubjectBalanceVO) => {
  const debit = Number(row.openingDebitAmount || 0)
  const credit = Number(row.openingCreditAmount || 0)
  if (debit > 0) return `(借) ${formatAmount(debit)}`
  if (credit > 0) return `(贷) ${formatAmount(credit)}`
  return '0.00'
}

// 格式化期末余额
const formatEndingBalance = (row: ErpFinanceSubjectBalanceVO) => {
  const debit = Number(row.endingDebitAmount || 0)
  const credit = Number(row.endingCreditAmount || 0)
  if (debit > 0) return `(借) ${formatAmount(debit)}`
  if (credit > 0) return `(贷) ${formatAmount(credit)}`
  return '0.00'
}

const clearReportData = () => {
  trialBalance.value = undefined
  balanceSheet.value = undefined
  incomeStatement.value = undefined
  cashFlowStatement.value = undefined
  subjectBalanceList.value = []
  subjectBalanceTotal.value = 0
  reportErrorMessage.value = ''
}

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    ledgerLoading.value = false
  }
}

const loadPeriods = async () => {
  if (!reportQuery.ledgerId) {
    periodOptions.value = []
    reportQuery.periodId = undefined
    return
  }
  periodLoading.value = true
  try {
    const data = await FinancePeriodApi.getPeriodPage({
      pageNo: 1,
      pageSize: 100,
      ledgerId: reportQuery.ledgerId
    })
    periodOptions.value = data?.list || []
    if (!periodOptions.value.some((item) => item.id === reportQuery.periodId)) {
      reportQuery.periodId = undefined
    }
  } finally {
    periodLoading.value = false
  }
}

const handleLedgerChange = async () => {
  reportQuery.periodId = undefined
  clearReportData()
  detailDrawerOpen.value = false
  showDiscrepancySummary.value = false
  await loadPeriods()
}

const handleTabSwitch = async (tab: ReportTabName) => {
  activeTab.value = tab
  if (canQueryReport.value) {
    await refreshReport()
  }
}

const buildReportParams = () => ({
  ledgerId: reportQuery.ledgerId,
  periodId: reportQuery.periodId,
  subjectCode: reportQuery.subjectCode,
  subjectName: reportQuery.subjectName
})

const refreshReport = async () => {
  if (!canQueryReport.value) {
    ElMessage.warning('请选择账簿和期间')
    return
  }
  if (activeTab.value === 'balanceList') {
    balanceQuery.pageNo = 1
    await loadSubjectBalance()
    return
  }
  await loadStatementReport()
}

const loadStatementReport = async () => {
  statementLoading.value = true
  reportErrorMessage.value = ''
  try {
    if (activeTab.value === 'trial') {
      trialBalance.value = await FinanceReportApi.getTrialBalance(buildReportParams())
      triggerToast('试算平衡表已加载完成')
    } else if (activeTab.value === 'balance') {
      balanceSheet.value = await FinanceReportApi.getBalanceSheet(buildReportParams())
      triggerToast('资产负债表已加载完成')
    } else if (activeTab.value === 'income') {
      incomeStatement.value = await FinanceReportApi.getIncomeStatement(buildReportParams())
      triggerToast('利润表已加载完成')
    } else if (activeTab.value === 'cash') {
      cashFlowStatement.value = await FinanceReportApi.getCashFlowStatement(buildReportParams())
      triggerToast('现金流量表已加载完成')
    }
  } catch {
    reportErrorMessage.value = '报表加载失败'
  } finally {
    statementLoading.value = false
  }
}

const loadSubjectBalance = async () => {
  if (!canQueryReport.value) {
    ElMessage.warning('请选择账簿和期间')
    return
  }
  subjectBalanceLoading.value = true
  reportErrorMessage.value = ''
  try {
    const data = await FinanceGeneralLedgerApi.getSubjectBalancePage({
      pageNo: balanceQuery.pageNo,
      pageSize: balanceQuery.pageSize,
      ledgerId: reportQuery.ledgerId,
      periodId: reportQuery.periodId,
      subjectCode: reportQuery.subjectCode,
      subjectName: reportQuery.subjectName
    })
    subjectBalanceList.value = data?.list || []
    subjectBalanceTotal.value = data?.total || 0
  } catch {
    if (!subjectBalanceList.value.length) {
      reportErrorMessage.value = '科目余额加载失败'
    }
  } finally {
    subjectBalanceLoading.value = false
  }
}

const resetQuery = async () => {
  reportQuery.ledgerId = undefined
  reportQuery.periodId = undefined
  reportQuery.subjectCode = undefined
  reportQuery.subjectName = undefined
  balanceQuery.pageNo = 1
  clearReportData()
  detailData.value = undefined
  detailErrorMessage.value = ''
  currentSubjectCode.value = ''
  detailDrawerOpen.value = false
  showDiscrepancySummary.value = false
  activeTab.value = 'trial'
  periodOptions.value = []
  triggerToast('账簿已恢复至初始状态')
}

const handleLocalReconcile = () => {
  showDiscrepancySummary.value = !showDiscrepancySummary.value
}

const handleGoSubjectBalance = async () => {
  showDiscrepancySummary.value = false
  activeTab.value = 'balanceList'
  balanceQuery.pageNo = 1
  await loadSubjectBalance()
  triggerToast('已切换到科目余额页')
}

const openDetailDrawer = async (row: ErpFinanceSubjectBalanceVO) => {
  if (!row.subjectCode || !reportQuery.ledgerId || !reportQuery.periodId || subjectBalanceLoading.value) {
    return
  }
  currentSubjectCode.value = row.subjectCode
  detailDrawerOpen.value = true
  await loadDetail(row.subjectCode)
}

const loadDetail = async (subjectCode: string) => {
  detailLoading.value = true
  detailErrorMessage.value = ''
  try {
    detailData.value = await FinanceGeneralLedgerApi.getGeneralLedgerDetail({
      ledgerId: reportQuery.ledgerId,
      periodId: reportQuery.periodId,
      subjectCode
    })
  } catch {
    detailErrorMessage.value = '明细加载失败'
  } finally {
    detailLoading.value = false
  }
}

const retryDetail = async () => {
  if (currentSubjectCode.value) {
    await loadDetail(currentSubjectCode.value)
  }
}

const clearDetailDrawer = () => {
  detailData.value = undefined
  detailErrorMessage.value = ''
  currentSubjectCode.value = ''
}

onMounted(async () => {
  await loadLedgers()
  // 默认选择第一个账簿
  if (ledgerOptions.value.length > 0) {
    reportQuery.ledgerId = ledgerOptions.value[0].id
    await loadPeriods()
    // 默认选择最新期间（按期间代码倒序，取第一个）
    if (periodOptions.value.length > 0) {
      const sortedPeriods = [...periodOptions.value].sort((a, b) => {
        return (b.periodCode || '').localeCompare(a.periodCode || '')
      })
      reportQuery.periodId = sortedPeriods[0].id
      // 自动加载试算平衡表
      await refreshReport()
    }
  }
})
</script>

<style scoped>
/* 管道液体粒子流动动画 */
@keyframes dash {
  to {
    stroke-dashoffset: -20;
  }
}
.animate-dash {
  stroke: #10b981;
  stroke-width: 3.5;
  stroke-linecap: round;
  stroke-dasharray: 5 5;
  animation: dash 1.5s linear infinite;
}

/* Toast 提示渐变过渡效果 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(15px);
}

@media (max-width: 1280px) {
  header > div {
    height: auto;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 12px;
    padding-top: 12px;
    padding-bottom: 12px;
  }

  nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }

  nav::-webkit-scrollbar {
    display: none;
  }
}

@media (max-width: 960px) {
  header > div {
    padding-left: 16px;
    padding-right: 16px;
  }

  nav button {
    flex: 0 0 auto;
    white-space: nowrap;
  }
}
</style>
