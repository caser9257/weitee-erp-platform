<template>
  <div class="finance-shell finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">研发报销 / 零星采购</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip">结果 {{ total }}</span>
            <span class="finance-shell__metric-chip">已选 {{ selectedIds.length }}</span>
            <span class="finance-shell__metric-chip">待审核 {{ statusCount.process }}</span>
            <span class="finance-shell__metric-chip">已审核 {{ statusCount.approve }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <span v-if="isDemoMode" class="finance-shell__page-demo-badge">示例数据</span>
          <el-button plain :loading="refreshing" :disabled="listLoading" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
        <el-button link type="primary" @click="advancedExpanded = !advancedExpanded">
          <Icon :icon="advancedExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="mr-5px" />
          {{ advancedExpanded ? '收起高级筛选' : '展开高级筛选' }}
        </el-button>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="单号" prop="no">
            <el-input v-model="queryParams.no" placeholder="请输入报销单号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="报销时间" prop="expenseTime">
            <el-date-picker
              v-model="queryParams.expenseTime"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="!w-full"
            />
          </el-form-item>
          <el-form-item label="费用类型" prop="expenseType">
            <el-select v-model="queryParams.expenseType" placeholder="请选择费用类型" clearable class="!w-full" :loading="typeLoading">
              <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="部门" prop="deptId">
            <el-select v-model="queryParams.deptId" placeholder="请选择部门" clearable filterable :loading="deptLoading" class="!w-full">
              <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目" prop="projectId">
            <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable filterable :loading="projectLoading" class="!w-full">
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in ERP_FINANCE_EXPENSE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <div v-if="advancedExpanded" class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="付款对象" prop="supplierId">
            <el-select v-model="queryParams.supplierId" placeholder="请选择付款对象" clearable filterable :loading="supplierLoading" class="!w-full">
              <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="结算账户" prop="accountId">
            <el-select v-model="queryParams.accountId" placeholder="请选择结算账户" clearable filterable :loading="accountLoading" class="!w-full">
              <el-option v-for="item in accountOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="财务人员" prop="financeUserId">
            <el-select v-model="queryParams.financeUserId" placeholder="请选择财务人员" clearable filterable :loading="userLoading" class="!w-full">
              <el-option v-for="item in userOptions" :key="item.id" :label="item.nickname" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="queryParams.remark" placeholder="请输入备注" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
        </div>

        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="listLoading" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">费用单列表</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
        <div class="finance-shell__toolbar-actions">
          <el-button
            type="primary"
            plain
            v-hasPermi="['erp:finance-expense:create']"
            :disabled="saveSubmitting"
            @click="openFormDialog()"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新建
          </el-button>
          <el-button type="success" plain v-hasPermi="['erp:finance-expense:export']" :loading="exportLoading" :disabled="listLoading" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>

      <el-alert v-if="listErrorMessage && !displayList.length" type="error" :closable="false" show-icon class="mb-12px" :title="listErrorMessage">
        <template #default>
          <el-button link type="primary" :disabled="listLoading" @click="handleRefresh">重新加载</el-button>
        </template>
      </el-alert>

      <template v-else>
        <div v-if="listLoading || displayList.length" class="finance-shell__table-wrap">
          <el-table v-loading="listLoading" :data="displayList" row-key="id" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="42" />
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  单据信息
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.no || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ formatDateValue(row.expenseTime) }}</span>
                  <div class="finance-shell__row-tags">
                    <span class="finance-shell__metric-pill finance-shell__metric-pill--primary">{{ row.expenseTypeName || getExpenseTypeLabel(row.expenseType) }}</span>
                    <span
                      class="finance-shell__metric-pill"
                      :class="resolveStatusClass(row.status, row.processInstanceId)"
                    >
                      {{ getStatusLabel(row.status, row.processInstanceId) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="220">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:office-building" class="finance-shell__column-icon" />
                  业务归属
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.projectName || row.deptName || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.supplierName || '零星采购' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="200" align="right">
              <template #header>
                <span class="finance-shell__column-header finance-shell__column-header--pipeline">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  金额
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--right">
                  <span class="finance-shell__amount finance-shell__mono">报销 {{ formatAmount(row.expensePrice) }}</span>
                  <span class="finance-shell__muted-text finance-shell__mono">已付 {{ formatAmount(row.paidPrice) }} / 剩余 {{ formatAmount(row.remainPrice) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:user" class="finance-shell__column-icon" />
                  处理人
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text">{{ row.financeUserName || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.creatorName || row.creator || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="180">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:chat-dot-round" class="finance-shell__column-icon" />
                  备注
                </span>
              </template>
              <template #default="{ row }">
                <span class="finance-shell__muted-text" :title="row.remark || '-'">{{ row.remark || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="280">
              <template #default="{ row }">
                <div class="finance-shell__row-actions">
                  <el-button
                    link
                    type="primary"
                    v-hasPermi="['erp:finance-expense:query']"
                    :disabled="isRowBusy(row.id)"
                    @click.stop="openDetailDialog(row.id)"
                  >
                    详情
                  </el-button>
                  <el-button
                    v-if="canEditRow(row)"
                    link
                    type="primary"
                    v-hasPermi="['erp:finance-expense:update']"
                    :disabled="isRowBusy(row.id)"
                    @click.stop="openFormDialog(row.id)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    v-if="canSubmitRow(row)"
                    link
                    :type="Number(row.status) === FINANCE_EXPENSE_STATUS.FAILED ? 'warning' : 'primary'"
                    v-hasPermi="['erp:finance-expense:submit']"
                    :loading="isSubmittingApproval(row.id)"
                    :disabled="isDeletingRow(row.id) || isCancelingApproval(row.id)"
                    @click.stop="openSubmitDialog(row)"
                  >
                    {{
                      Number(row.status) === FINANCE_EXPENSE_STATUS.REJECT ||
                      Number(row.status) === FINANCE_EXPENSE_STATUS.FAILED
                        ? '重新提交审批'
                        : '提交审批'
                    }}
                  </el-button>
                  <el-button
                    v-if="canCancelApprovalRow(row)"
                    link
                    type="warning"
                    v-hasPermi="['erp:finance-expense:cancel-approval']"
                    :loading="isCancelingApproval(row.id)"
                    :disabled="isDeletingRow(row.id) || isSubmittingApproval(row.id)"
                    @click.stop="handleCancelApproval(row)"
                  >
                    撤回审批
                  </el-button>
                  <el-button
                    v-if="canViewProcessRow(row)"
                    link
                    v-hasPermi="['erp:finance-expense:query']"
                    :disabled="isRowBusy(row.id)"
                    @click.stop="handleProcessDetail(row)"
                  >
                    查看审批
                  </el-button>
                  <el-button
                    v-if="canDeleteRow(row)"
                    link
                    type="danger"
                    v-hasPermi="['erp:finance-expense:delete']"
                    :loading="isDeletingRow(row.id)"
                    :disabled="isSubmittingApproval(row.id) || isCancelingApproval(row.id)"
                    @click.stop="handleDelete([row.id])"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else :description="isDemoMode ? '当前无真实数据，已展示示例数据' : '暂无费用单数据'" />
        <Pagination v-if="displayTotal > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="displayTotal" @pagination="handlePagination" />
      </template>
    </ContentWrap>

    <el-dialog v-model="formDialogVisible" :title="formDialogTitle" width="1180px" destroy-on-close :close-on-click-modal="false" :close-on-press-escape="false">
      <div class="finance-shell__dialog-panel">
        <div class="finance-shell__dialog-context">
          <div>
            <div class="finance-shell__dialog-title">{{ formDialogTitle }}</div>
            <div class="finance-shell__dialog-meta">
              <span>{{ formModel.no || '未生成单号' }}</span>
              <span>状态 {{ getStatusLabel(formModel.status, formModel.processInstanceId) }}</span>
            </div>
          </div>
          <div class="finance-shell__dialog-summary">
            <div class="finance-shell__dialog-summary-card">
              <div class="finance-shell__dialog-summary-label">报销金额</div>
              <div class="finance-shell__dialog-summary-value finance-shell__mono">{{ formatAmount(formModel.expensePrice) }}</div>
            </div>
            <div class="finance-shell__dialog-summary-card">
              <div class="finance-shell__dialog-summary-label">明细合计</div>
              <div class="finance-shell__dialog-summary-value finance-shell__mono">{{ formatAmount(itemTotal) }}</div>
            </div>
          </div>
        </div>

        <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="92px" class="finance-shell__dialog-form">
          <div class="finance-shell__dialog-grid finance-shell__dialog-grid--wide">
            <el-form-item label="报销时间" prop="expenseTime">
              <el-date-picker v-model="formModel.expenseTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择报销时间" class="!w-full" />
            </el-form-item>
            <el-form-item label="费用类型" prop="expenseType">
              <el-select v-model="formModel.expenseType" placeholder="请选择费用类型" clearable class="!w-full" :loading="typeLoading">
                <el-option v-for="item in expenseTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="formModel.deptId" placeholder="请选择部门" clearable filterable class="!w-full" :loading="deptLoading">
                <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="项目" prop="projectId">
              <el-select v-model="formModel.projectId" placeholder="请选择项目" clearable filterable class="!w-full" :loading="projectLoading">
                <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="付款对象" prop="supplierId">
              <el-select v-model="formModel.supplierId" placeholder="请选择付款对象" clearable filterable class="!w-full" :loading="supplierLoading">
                <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="结算账户" prop="accountId">
              <el-select v-model="formModel.accountId" placeholder="请选择结算账户" clearable filterable class="!w-full" :loading="accountLoading">
                <el-option v-for="item in accountOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="财务人员" prop="financeUserId">
              <el-select v-model="formModel.financeUserId" placeholder="请选择财务人员" clearable filterable class="!w-full" :loading="userLoading">
                <el-option v-for="item in userOptions" :key="item.id" :label="item.nickname" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="报销金额" prop="expensePrice">
              <el-input-number v-model="formModel.expensePrice" :min="0" :precision="2" :step="100" controls-position="right" class="!w-full" />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="finance-shell__dialog-grid--full">
              <el-input v-model="formModel.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入备注" />
            </el-form-item>
          </div>

          <div class="finance-shell__section-title">费用明细</div>
          <div class="finance-shell__detail-table">
            <el-table :data="formModel.items" border class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
              <el-table-column label="费用内容" min-width="260">
                <template #default="{ row, $index }">
                  <el-form-item :prop="`items.${$index}.itemName`" :rules="formItemRules.itemName" class="!mb-0">
                    <el-input v-model="row.itemName" placeholder="请输入费用内容" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="金额" min-width="160" align="right">
                <template #default="{ row, $index }">
                  <el-form-item :prop="`items.${$index}.amount`" :rules="formItemRules.amount" class="!mb-0">
                    <el-input-number v-model="row.amount" :min="0" :precision="2" :step="100" controls-position="right" class="!w-full" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="240">
                <template #default="{ row }">
                  <el-input v-model="row.remark" placeholder="请输入备注" />
                </template>
              </el-table-column>
              <el-table-column label="转固定资产" min-width="140" align="center">
                <template #default="{ row }">
                  <el-switch v-model="row.assetCandidateFlag" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" :disabled="formModel.items.length <= 1" @click="removeFormItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="finance-shell__detail-actions">
              <el-button plain @click="appendFormItem">
                <Icon icon="ep:plus" class="mr-5px" />
                添加明细
              </el-button>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="closeFormDialog">取消</el-button>
        <el-button type="primary" :loading="saveSubmitting" :disabled="!canSubmitForm" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawerVisible" size="960px" destroy-on-close :with-header="false" :modal-class="'finance-shell__drawer-mask'">
      <div class="finance-shell__drawer">
        <div class="finance-shell__drawer-context">
          <div>
            <div class="finance-shell__drawer-title">{{ detailData.no || '-' }}</div>
            <div class="finance-shell__drawer-meta">
              <span>{{ detailData.expenseTypeName || getExpenseTypeLabel(detailData.expenseType) }}</span>
              <span>{{ detailData.projectName || detailData.deptName || '-' }}</span>
            </div>
          </div>
          <div class="finance-shell__drawer-summary">
            <div class="finance-shell__drawer-summary-card">
              <div class="finance-shell__drawer-summary-label">报销金额</div>
              <div class="finance-shell__drawer-summary-value finance-shell__mono">{{ formatAmount(detailData.expensePrice) }}</div>
            </div>
            <div class="finance-shell__drawer-summary-card">
              <div class="finance-shell__drawer-summary-label">剩余金额</div>
              <div class="finance-shell__drawer-summary-value finance-shell__mono">{{ formatAmount(detailData.remainPrice) }}</div>
            </div>
            <div class="finance-shell__drawer-summary-card">
              <div class="finance-shell__drawer-summary-label">状态</div>
              <div class="finance-shell__drawer-summary-value">
                {{ getStatusLabel(detailData.status, detailData.processInstanceId) }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="detailLoading" class="finance-shell__drawer-loading">
          <el-skeleton :rows="6" animated />
        </div>
        <template v-else>
          <div v-if="detailLoadError" class="finance-shell__drawer-error">
            <el-alert type="error" :closable="false" show-icon :title="detailLoadError">
              <template #default>
                <el-button link type="primary" @click="loadDetail">重新加载</el-button>
              </template>
            </el-alert>
          </div>
          <div v-else class="finance-shell__drawer-body">
            <div class="finance-shell__section-title">基础信息</div>
            <div class="finance-shell__drawer-grid">
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">报销时间</div>
                <div class="finance-shell__info-value">{{ formatDateValue(detailData.expenseTime) }}</div>
              </div>
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">费用类型</div>
                <div class="finance-shell__info-value">{{ detailData.expenseTypeName || getExpenseTypeLabel(detailData.expenseType) }}</div>
              </div>
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">部门</div>
                <div class="finance-shell__info-value">{{ detailData.deptName || '-' }}</div>
              </div>
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">项目</div>
                <div class="finance-shell__info-value">{{ detailData.projectName || '-' }}</div>
              </div>
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">付款对象</div>
                <div class="finance-shell__info-value">{{ detailData.supplierName || '-' }}</div>
              </div>
              <div class="finance-shell__info-card">
                <div class="finance-shell__info-label">结算账户</div>
                <div class="finance-shell__info-value">{{ detailData.accountName || '-' }}</div>
              </div>
            </div>

            <div class="finance-shell__section-title">费用明细</div>
            <div class="finance-shell__detail-table">
              <el-table :data="detailData.items || []" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
                <el-table-column label="费用内容" min-width="240">
                  <template #default="{ row }">
                    <div class="finance-shell__primary-cell">
                      <span class="finance-shell__primary-text">{{ row.itemName || '-' }}</span>
                      <span class="finance-shell__muted-text">{{ row.remark || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="金额" min-width="140" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.amount) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="finance-shell__section-title">追溯信息</div>
            <div class="finance-shell__drawer-grid finance-shell__drawer-grid--trace">
              <div class="finance-shell__info-card finance-shell__info-card--trace">
                <div class="finance-shell__info-label">应付台账</div>
                <div class="finance-shell__info-value">{{ traceData.statement?.statementNo || '-' }}</div>
                <div class="finance-shell__info-sub">{{ traceData.statement?.statusName || '-' }}</div>
              </div>
              <div class="finance-shell__info-card finance-shell__info-card--trace">
                <div class="finance-shell__info-label">台账金额</div>
                <div class="finance-shell__info-value finance-shell__mono">{{ formatAmount(traceData.statement?.amount) }}</div>
                <div class="finance-shell__info-sub finance-shell__mono">已付 {{ formatAmount(traceData.statement?.paidAmount) }} / 剩余 {{ formatAmount(traceData.statement?.remainAmount) }}</div>
              </div>
              <div class="finance-shell__info-card finance-shell__info-card--trace">
                <div class="finance-shell__info-label">付款对象</div>
                <div class="finance-shell__info-value">{{ traceData.statement?.supplierName || '-' }}</div>
                <div class="finance-shell__info-sub">{{ traceData.statement?.accountName || '-' }}</div>
              </div>
            </div>

            <div class="finance-shell__detail-table">
              <el-table :data="traceData.statementItems || []" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
                <el-table-column label="流水单号" min-width="180">
                  <template #default="{ row }">
                    <span class="finance-shell__mono">{{ row.refNo || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="金额" min-width="140" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.amount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="220">
                  <template #default="{ row }">
                    <span class="finance-shell__muted-text">{{ row.remark || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" min-width="170">
                  <template #default="{ row }">
                    <span class="finance-shell__muted-text">{{ formatDateValue(row.createTime) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="finance-shell__detail-table">
              <el-table :data="traceData.allocates || []" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
                <el-table-column label="付款单号" min-width="180">
                  <template #default="{ row }">
                    <span class="finance-shell__mono">{{ row.paymentNo || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="分配金额" min-width="140" align="right">
                  <template #default="{ row }">
                    <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.allocateAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" min-width="120" align="center">
                  <template #default="{ row }">
                    <span class="finance-shell__metric-pill" :class="row.status === 20 ? 'finance-shell__metric-pill--success' : 'finance-shell__metric-pill--primary'">
                      {{ row.status === 20 ? '已完成' : '处理中' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="220">
                  <template #default="{ row }">
                    <span class="finance-shell__muted-text">{{ row.remark || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
    <FinanceExpenseSubmitDialog
      ref="submitDialogRef"
      @success="handleSubmitSuccess"
      @close="handleSubmitDialogClose"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import download from '@/utils/download'
import { formatDate } from '@/utils/formatTime'
import { erpPriceInputFormatter } from '@/utils'
import { resolveErpAuditStatusLabel } from '@/utils/erpAuditStatus'
import {
  ERP_FINANCE_EXPENSE_STATUS_OPTIONS,
  FinanceExpenseApi,
  type ErpFinanceExpensePageReqVO,
  type ErpFinanceExpenseProjectSummaryVO,
  type ErpFinanceExpenseSaveReqVO,
  type ErpFinanceExpenseTraceVO,
  type ErpFinanceExpenseTypeVO,
  type ErpFinanceExpenseVO
} from '@/api/erp/finance/expense'
import { SupplierApi, type SupplierVO } from '@/api/erp/purchase/supplier'
import { AccountApi, type AccountVO } from '@/api/erp/finance/account'
import { ProjectApi, type ProjectSimpleVO } from '@/api/erp/project'
import { getSimpleDeptList, type DeptVO } from '@/api/system/dept'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import FinanceExpenseSubmitDialog from './FinanceExpenseSubmitDialog.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  FINANCE_EXPENSE_STATUS,
  getFinanceExpenseRowActionDescriptor
} from './expenseStatus.helpers'

defineOptions({ name: 'ErpFinanceExpense' })

type ExpenseFormItem = {
  id?: number
  itemName: string
  amount: number | undefined
  remark: string
  assetCandidateFlag: boolean
}

const message = useMessage()
const { push } = useRouter()
const userStore = useUserStoreWithOut()
const currentUserId = computed(() => String(userStore.getUser.id || ''))
const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const submitDialogRef = ref<InstanceType<typeof FinanceExpenseSubmitDialog>>()
const advancedExpanded = ref(false)

const queryParams = reactive<ErpFinanceExpensePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: '',
  expenseTime: [],
  expenseType: undefined,
  deptId: undefined,
  projectId: undefined,
  supplierId: undefined,
  financeUserId: undefined,
  accountId: undefined,
  status: undefined,
  remark: ''
})

const list = ref<ErpFinanceExpenseVO[]>([])
const total = ref(0)
const listLoading = ref(false)
const listErrorMessage = ref('')
const refreshing = ref(false)
const exportLoading = ref(false)
const saveSubmitting = ref(false)
const deleteLoadingIds = ref<number[]>([])
const submitApprovalIds = ref<number[]>([])
const cancelApprovalIds = ref<number[]>([])
const activeSubmitRowId = ref<number>()
const selectionList = ref<ErpFinanceExpenseVO[]>([])

const deptOptions = ref<DeptVO[]>([])
const projectOptions = ref<ProjectSimpleVO[]>([])
const supplierOptions = ref<SupplierVO[]>([])
const accountOptions = ref<AccountVO[]>([])
const userOptions = ref<SimpleUserVO[]>([])
const expenseTypeOptions = ref<ErpFinanceExpenseTypeVO[]>([])

const deptLoading = ref(false)
const projectLoading = ref(false)
const supplierLoading = ref(false)
const accountLoading = ref(false)
const userLoading = ref(false)
const typeLoading = ref(false)

const formDialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formModel = reactive<
  ErpFinanceExpenseSaveReqVO & {
    no?: string
    status?: number
    processInstanceId?: string
    items: ExpenseFormItem[]
  }
>({
  expenseTime: '',
  expenseType: undefined,
  deptId: undefined,
  projectId: undefined,
  supplierId: undefined,
  financeUserId: undefined,
  accountId: undefined,
  expensePrice: undefined,
  remark: '',
  items: []
})

const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailLoadError = ref('')
const detailId = ref<number | null>(null)
const detailData = reactive<ErpFinanceExpenseVO>({})
const traceData = reactive<ErpFinanceExpenseTraceVO>({})

const isLocalDemoHost = () => {
  if (typeof window === 'undefined') {
    return false
  }
  const hostname = window.location.hostname
  return (
    import.meta.env.DEV ||
    hostname === 'localhost' ||
    hostname === '127.0.0.1' ||
    hostname === '::1' ||
    hostname.endsWith('.local')
  )
}

const demoExpenseTypes: ErpFinanceExpenseTypeVO[] = [
  { value: 10, label: '研发差旅', projectRequired: true },
  { value: 20, label: '办公采购', projectRequired: false },
  { value: 30, label: '打样费用', projectRequired: true }
]

const demoLists = {
  depts: [
    { id: 51001, name: '研发一部' },
    { id: 51002, name: '研发二部' }
  ],
  projects: [
    { id: 61001, name: '新平台控制器研发项目' },
    { id: 61002, name: '工业传感器验证项目' }
  ],
  suppliers: [
    { id: 71001, name: '苏州泽科电子有限公司' },
    { id: 71004, name: '上海景澄商贸有限公司' }
  ],
  accounts: [
    { id: 81001, name: '中国银行研发专户' },
    { id: 81002, name: '建设银行零星采购户' }
  ],
  users: [
    { id: 91001, nickname: '陈瑶' },
    { id: 91002, nickname: '周颖' }
  ]
}

const demoExpenses: ErpFinanceExpenseVO[] = [
  {
    id: 701001,
    no: 'EX-2026-0508-001',
    status: 10,
    expenseTime: '2026-05-08 09:20:00',
    expenseType: 10,
    expenseTypeName: '研发差旅',
    deptId: 51001,
    deptName: '研发一部',
    projectId: 61001,
    projectName: '新平台控制器研发项目',
    supplierId: 71004,
    supplierName: '上海景澄商贸有限公司',
    financeUserId: 91001,
    financeUserName: '陈瑶',
    accountId: 81001,
    accountName: '中国银行研发专户',
    expensePrice: 4860,
    paidPrice: 0,
    remainPrice: 4860,
    remark: '示例数据：出差样件评审与客户现场沟通',
    creator: 'zhangsan',
    creatorName: '张三',
    createTime: '2026-05-08 09:35:00',
    items: [
      { id: 701011, itemName: '高铁往返', amount: 1820, remark: '上海-杭州' },
      { id: 701012, itemName: '住宿', amount: 1680, remark: '两晚' },
      { id: 701013, itemName: '餐费', amount: 1360, remark: '客户陪同' }
    ]
  },
  {
    id: 701002,
    no: 'EX-2026-0509-004',
    status: 20,
    expenseTime: '2026-05-09 14:10:00',
    expenseType: 20,
    expenseTypeName: '办公采购',
    deptId: 51002,
    deptName: '研发二部',
    projectId: undefined,
    projectName: '',
    supplierId: 71001,
    supplierName: '苏州泽科电子有限公司',
    financeUserId: 91002,
    financeUserName: '周颖',
    accountId: 81002,
    accountName: '建设银行零星采购户',
    expensePrice: 15820,
    paidPrice: 15820,
    remainPrice: 0,
    remark: '示例数据：研发样机采购配件',
    creator: 'lisi',
    creatorName: '李四',
    createTime: '2026-05-09 14:24:00',
    items: [
      { id: 701021, itemName: '连接器', amount: 6200, remark: '样机 A' },
      { id: 701022, itemName: '屏蔽罩', amount: 9620, remark: '样机 A' }
    ]
  },
  {
    id: 701003,
    no: 'EX-2026-0510-002',
    status: 30,
    expenseTime: '2026-05-10 10:45:00',
    expenseType: 30,
    expenseTypeName: '打样费用',
    deptId: 51001,
    deptName: '研发一部',
    projectId: 61002,
    projectName: '工业传感器验证项目',
    supplierId: 71004,
    supplierName: '上海景澄商贸有限公司',
    financeUserId: 91001,
    financeUserName: '陈瑶',
    accountId: 81001,
    accountName: '中国银行研发专户',
    expensePrice: 3200,
    paidPrice: 0,
    remainPrice: 3200,
    remark: '示例数据：用于展示驳回状态',
    creator: 'wangwu',
    creatorName: '王五',
    createTime: '2026-05-10 10:52:00',
    items: [
      { id: 701031, itemName: '打样工费', amount: 2400, remark: '首轮打样' },
      { id: 701032, itemName: '耗材', amount: 800, remark: '材料费' }
    ]
  }
]

const demoTraceMap: Record<number, ErpFinanceExpenseTraceVO> = {
  701001: {
    statement: {
      statementId: 911001,
      statementNo: 'AP-2026-0508-011',
      bizType: 10,
      bizId: 701001,
      bizNo: 'EX-2026-0508-001',
      supplierId: 71004,
      supplierName: '上海景澄商贸有限公司',
      accountId: 81001,
      accountName: '中国银行研发专户',
      amount: 4860,
      paidAmount: 0,
      remainAmount: 4860,
      invoiceStatus: 10,
      status: 10,
      statusName: '待支付',
      remark: '示例数据：关联应付台账',
      bizDate: '2026-05-08 09:20:00',
      dueDate: '2026-05-15 23:59:59'
    },
    statementItems: [
      {
        id: 912001,
        itemType: 10,
        refId: 701001,
        refNo: 'EX-2026-0508-001',
        amount: 4860,
        afterPaidAmount: 0,
        afterRemainAmount: 4860,
        remark: '出差费用分配',
        createTime: '2026-05-08 09:40:00'
      }
    ],
    allocates: [
      {
        paymentId: 913001,
        paymentItemId: 913101,
        paymentNo: 'PAY-2026-0508-003',
        allocateAmount: 0,
        status: 10,
        remark: '待付款'
      }
    ]
  },
  701002: {
    statement: {
      statementId: 911002,
      statementNo: 'AP-2026-0509-018',
      bizType: 20,
      bizId: 701002,
      bizNo: 'EX-2026-0509-004',
      supplierId: 71001,
      supplierName: '苏州泽科电子有限公司',
      accountId: 81002,
      accountName: '建设银行零星采购户',
      amount: 15820,
      paidAmount: 15820,
      remainAmount: 0,
      invoiceStatus: 20,
      status: 20,
      statusName: '已支付',
      remark: '示例数据：已完成支付追溯',
      bizDate: '2026-05-09 14:10:00',
      dueDate: '2026-05-12 23:59:59'
    },
    statementItems: [
      {
        id: 912002,
        itemType: 20,
        refId: 701002,
        refNo: 'EX-2026-0509-004',
        amount: 6200,
        afterPaidAmount: 6200,
        afterRemainAmount: 0,
        remark: '连接器',
        createTime: '2026-05-09 14:14:00'
      },
      {
        id: 912003,
        itemType: 20,
        refId: 701002,
        refNo: 'EX-2026-0509-004',
        amount: 9620,
        afterPaidAmount: 9620,
        afterRemainAmount: 0,
        remark: '屏蔽罩',
        createTime: '2026-05-09 14:16:00'
      }
    ],
    allocates: [
      {
        paymentId: 913002,
        paymentItemId: 913102,
        paymentNo: 'PAY-2026-0509-012',
        allocateAmount: 15820,
        status: 20,
        remark: '已完成分配'
      }
    ]
  },
  701003: {
    statement: {
      statementId: 911003,
      statementNo: 'AP-2026-0510-006',
      bizType: 30,
      bizId: 701003,
      bizNo: 'EX-2026-0510-002',
      supplierId: 71004,
      supplierName: '上海景澄商贸有限公司',
      accountId: 81001,
      accountName: '中国银行研发专户',
      amount: 3200,
      paidAmount: 0,
      remainAmount: 3200,
      invoiceStatus: 10,
      status: 10,
      statusName: '待支付',
      remark: '示例数据：驳回后仍可追溯',
      bizDate: '2026-05-10 10:45:00',
      dueDate: '2026-05-16 23:59:59'
    },
    statementItems: [
      {
        id: 912004,
        itemType: 30,
        refId: 701003,
        refNo: 'EX-2026-0510-002',
        amount: 2400,
        afterPaidAmount: 0,
        afterRemainAmount: 2400,
        remark: '打样工费',
        createTime: '2026-05-10 10:48:00'
      },
      {
        id: 912005,
        itemType: 30,
        refId: 701003,
        refNo: 'EX-2026-0510-002',
        amount: 800,
        afterPaidAmount: 0,
        afterRemainAmount: 800,
        remark: '耗材',
        createTime: '2026-05-10 10:49:00'
      }
    ],
    allocates: [
      {
        paymentId: 913003,
        paymentItemId: 913103,
        paymentNo: 'PAY-2026-0510-004',
        allocateAmount: 0,
        status: 10,
        remark: '等待复核'
      }
    ]
  }
}

const isDemoMode = computed(() => isLocalDemoHost() && !listLoading.value && !listErrorMessage.value && list.value.length === 0)
const displayList = computed(() => (isDemoMode.value ? demoExpenses : list.value))
const displayTotal = computed(() => (isDemoMode.value ? demoExpenses.length : total.value))

const statusCount = computed(() => {
  const count = { process: 0, approve: 0, reject: 0 }
  displayList.value.forEach((item) => {
    if (item.status === FINANCE_EXPENSE_STATUS.APPROVE) count.approve += 1
    else if (item.status === FINANCE_EXPENSE_STATUS.REJECT) count.reject += 1
    else count.process += 1
  })
  return count
})

const selectedIds = computed(() => selectionList.value.map((item) => Number(item.id)).filter(Boolean))
const canQuery = computed(
  () =>
    !listLoading.value &&
    !saveSubmitting.value &&
    !deleteLoadingIds.value.length &&
    !submitApprovalIds.value.length &&
    !cancelApprovalIds.value.length
)
const canReset = computed(() => !listLoading.value)
const canSubmitForm = computed(() => !saveSubmitting.value && itemTotal.value > 0)
const formDialogTitle = computed(() => (editingId.value ? '编辑费用单' : '新建费用单'))
const isDeletingRow = (id?: number) => !!id && deleteLoadingIds.value.includes(id)
const isSubmittingApproval = (id?: number) => !!id && submitApprovalIds.value.includes(id)
const isCancelingApproval = (id?: number) => !!id && cancelApprovalIds.value.includes(id)
const isRowBusy = (id?: number) =>
  isDeletingRow(id) || isSubmittingApproval(id) || isCancelingApproval(id)

const formRules: FormRules = {
  expenseTime: [{ required: true, message: '请选择报销时间', trigger: 'change' }],
  expenseType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择结算账户', trigger: 'change' }],
  expensePrice: [{ required: true, message: '请输入报销金额', trigger: 'change' }]
}

const formItemRules = {
  itemName: [{ required: true, message: '请输入费用内容', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入明细金额', trigger: 'change' }]
}

const formatAmount = (value?: number) => {
  if (value == null) return '-'
  return erpPriceInputFormatter(value)
}

const formatDateValue = (value?: string) =>
  value ? formatDate(new Date(value), 'YYYY-MM-DD HH:mm:ss') : '-'

const getStatusLabel = (status?: number, processInstanceId?: string) =>
  resolveErpAuditStatusLabel(status, processInstanceId)

const resolveStatusClass = (status?: number, processInstanceId?: string) => {
  if (status === FINANCE_EXPENSE_STATUS.APPROVE) return 'finance-shell__metric-pill--success'
  if (status === FINANCE_EXPENSE_STATUS.REJECT) return 'finance-shell__metric-pill--warning'
  if (status === FINANCE_EXPENSE_STATUS.FAILED) return 'finance-shell__metric-pill--danger'
  if (status === FINANCE_EXPENSE_STATUS.PROCESS && processInstanceId) {
    return 'finance-shell__metric-pill--warning'
  }
  return 'finance-shell__metric-pill--primary'
}

const getExpenseTypeLabel = (value?: number) => expenseTypeOptions.value.find((item) => item.value === value)?.label || '-'

const getRowDescriptor = (row: ErpFinanceExpenseVO) =>
  getFinanceExpenseRowActionDescriptor({
    status: row.status,
    processInstanceId: row.processInstanceId,
    creator: row.creator,
    currentUserId: currentUserId.value
  })

const canEditRow = (row: ErpFinanceExpenseVO) => getRowDescriptor(row).canEdit
const canDeleteRow = (row: ErpFinanceExpenseVO) => getRowDescriptor(row).canDelete
const canSubmitRow = (row: ErpFinanceExpenseVO) => getRowDescriptor(row).canSubmit
const canCancelApprovalRow = (row: ErpFinanceExpenseVO) =>
  getRowDescriptor(row).canCancelApproval
const canViewProcessRow = (row: ErpFinanceExpenseVO) => getRowDescriptor(row).canViewProcess
const findDemoExpense = (id?: number | null) => demoExpenses.find((item) => item.id === id)

const resetFormModel = () => {
  Object.assign(formModel, {
    expenseTime: '',
    expenseType: undefined,
    deptId: undefined,
    projectId: undefined,
    supplierId: undefined,
    financeUserId: undefined,
    accountId: undefined,
    expensePrice: undefined,
    remark: '',
    status: undefined,
    processInstanceId: undefined,
    no: undefined,
    items: [
      {
        id: undefined,
        itemName: '',
        amount: undefined,
        remark: '',
        assetCandidateFlag: false
      }
    ]
  })
}

const syncExpensePriceFromItems = () => {
  if (!formModel.items.length) return
  formModel.expensePrice = Number(itemTotal.value.toFixed(2))
}

watch(
  () => formModel.items,
  () => {
    syncExpensePriceFromItems()
  },
  { deep: true }
)

watch(
  () => formModel.expenseType,
  (value) => {
    const currentType = expenseTypeOptions.value.find((item) => item.value === value)
    if (currentType?.projectRequired === false) {
      formModel.projectId = undefined
    }
  }
)

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceExpenseApi.getFinanceExpensePage(queryParams)
    const rows = data?.list || []
    list.value = rows.length ? rows : isDemoMode.value ? demoExpenses : []
    total.value = rows.length ? (data?.total || rows.length) : isDemoMode.value ? demoExpenses.length : 0
  } catch (error: any) {
    if (isLocalDemoHost()) {
      list.value = demoExpenses
      total.value = demoExpenses.length
    } else {
      list.value = []
      total.value = 0
      listErrorMessage.value = error?.message || '费用单列表加载失败'
    }
  } finally {
    listLoading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    no: '',
    expenseTime: [],
    expenseType: undefined,
    deptId: undefined,
    projectId: undefined,
    supplierId: undefined,
    financeUserId: undefined,
    accountId: undefined,
    status: undefined,
    remark: ''
  })
  advancedExpanded.value = false
  queryFormRef.value?.clearValidate()
  await getList()
}

const handlePagination = async () => {
  await getList()
}

const loadQueryOptions = async () => {
  deptLoading.value = true
  projectLoading.value = true
  supplierLoading.value = true
  accountLoading.value = true
  userLoading.value = true
  typeLoading.value = true
  try {
    const [depts, projects, suppliers, accounts, users, types] = await Promise.all([
      getSimpleDeptList(),
      ProjectApi.getProjectSimpleList(),
      SupplierApi.getSupplierSimpleList(),
      AccountApi.getAccountSimpleList(),
      getSimpleUserList(),
      FinanceExpenseApi.getFinanceExpenseTypeList()
    ])
    deptOptions.value = depts?.length ? depts : isDemoMode.value ? demoLists.depts : []
    projectOptions.value = projects?.length ? projects : isDemoMode.value ? demoLists.projects : []
    supplierOptions.value = suppliers?.length ? suppliers : isDemoMode.value ? demoLists.suppliers : []
    accountOptions.value = accounts?.length ? accounts : isDemoMode.value ? demoLists.accounts : []
    userOptions.value = users?.length ? users : isDemoMode.value ? demoLists.users : []
    expenseTypeOptions.value = types?.length ? types : isDemoMode.value ? demoExpenseTypes : []
  } catch (error: any) {
    if (isLocalDemoHost()) {
      deptOptions.value = demoLists.depts
      projectOptions.value = demoLists.projects
      supplierOptions.value = demoLists.suppliers
      accountOptions.value = demoLists.accounts
      userOptions.value = demoLists.users
      expenseTypeOptions.value = demoExpenseTypes
    } else {
      message.error(error?.message || '基础选项加载失败')
    }
  } finally {
    deptLoading.value = false
    projectLoading.value = false
    supplierLoading.value = false
    accountLoading.value = false
    userLoading.value = false
    typeLoading.value = false
  }
}

const handleSelectionChange = (rows: ErpFinanceExpenseVO[]) => {
  selectionList.value = rows
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    await getList()
  } finally {
    refreshing.value = false
  }
}

const openFormDialog = async (id?: number) => {
  editingId.value = id || null
  resetFormModel()
  formDialogVisible.value = true
  if (!id) return
  try {
    if (isLocalDemoHost()) {
      const expense = findDemoExpense(id)
      if (expense) {
        Object.assign(formModel, {
          id: expense?.id,
          no: expense?.no,
          status: expense?.status,
          processInstanceId: expense?.processInstanceId,
          expenseTime: expense?.expenseTime || '',
          expenseType: expense?.expenseType,
          deptId: expense?.deptId,
          projectId: expense?.projectId,
          supplierId: expense?.supplierId,
          financeUserId: expense?.financeUserId,
          accountId: expense?.accountId,
          expensePrice: expense?.expensePrice,
          remark: expense?.remark || '',
          items:
            expense?.items?.length && expense.items.length > 0
              ? expense.items.map((item) => ({
                  id: item.id,
                  itemName: item.itemName || '',
                  amount: Number(item.amount || 0),
                  remark: item.remark || '',
                  assetCandidateFlag: Boolean(item.assetCandidateFlag)
                }))
              : [
                  {
                    id: undefined,
                    itemName: '',
                    amount: undefined,
                    remark: '',
                    assetCandidateFlag: false
                  }
                ]
        })
        return
      }
    }
    const expense = await FinanceExpenseApi.getFinanceExpense(id)
    Object.assign(formModel, {
      id: expense?.id,
      no: expense?.no,
      status: expense?.status,
      processInstanceId: expense?.processInstanceId,
      expenseTime: expense?.expenseTime || '',
      expenseType: expense?.expenseType,
      deptId: expense?.deptId,
      projectId: expense?.projectId,
      supplierId: expense?.supplierId,
      financeUserId: expense?.financeUserId,
      accountId: expense?.accountId,
      expensePrice: expense?.expensePrice,
      remark: expense?.remark || '',
      items:
        expense?.items?.length && expense.items.length > 0
          ? expense.items.map((item) => ({
              id: item.id,
              itemName: item.itemName || '',
              amount: Number(item.amount || 0),
              remark: item.remark || '',
              assetCandidateFlag: Boolean(item.assetCandidateFlag)
            }))
          : [
              {
                id: undefined,
                itemName: '',
                amount: undefined,
                remark: '',
                assetCandidateFlag: false
              }
            ]
    })
  } catch (error: any) {
    message.error(error?.message || '费用单加载失败')
    formDialogVisible.value = false
  }
}

const closeFormDialog = () => {
  formDialogVisible.value = false
  editingId.value = null
  formRef.value?.clearValidate()
  resetFormModel()
}

const appendFormItem = () => {
  formModel.items.push({
    id: undefined,
    itemName: '',
    amount: undefined,
    remark: '',
    assetCandidateFlag: false
  })
}

const removeFormItem = (index: number) => {
  if (formModel.items.length <= 1) return
  formModel.items.splice(index, 1)
  syncExpensePriceFromItems()
}

const submitForm = async () => {
  if (saveSubmitting.value) return
  await formRef.value?.validate()
  if (itemTotal.value <= 0) {
    message.warning('请至少填写一条金额大于 0 的费用明细')
    return
  }
  const payload: ErpFinanceExpenseSaveReqVO = {
    id: editingId.value || undefined,
    expenseTime: formModel.expenseTime,
    expenseType: formModel.expenseType,
    deptId: formModel.deptId,
    projectId: formModel.projectId,
    supplierId: formModel.supplierId,
    financeUserId: formModel.financeUserId,
    accountId: formModel.accountId,
    expensePrice: Number(itemTotal.value.toFixed(2)),
    remark: formModel.remark,
    items: formModel.items
      .filter((item) => item.itemName && Number(item.amount || 0) > 0)
      .map((item) => ({
        id: item.id,
        itemName: item.itemName,
        amount: Number(Number(item.amount || 0).toFixed(2)),
        remark: item.remark,
        assetCandidateFlag: Boolean(item.assetCandidateFlag)
      }))
  }
  saveSubmitting.value = true
  try {
    if (editingId.value) {
      await FinanceExpenseApi.updateFinanceExpense(payload)
      message.success('费用单修改成功')
    } else {
      await FinanceExpenseApi.createFinanceExpense(payload)
      message.success('费用单创建成功')
    }
    closeFormDialog()
    await getList()
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    saveSubmitting.value = false
  }
}

const openDetailDialog = async (id?: number) => {
  if (!id) return
  detailDrawerVisible.value = true
  detailId.value = id
  detailLoadError.value = ''
  detailLoading.value = true
  Object.keys(detailData).forEach((key) => delete (detailData as Record<string, any>)[key])
  Object.keys(traceData).forEach((key) => delete (traceData as Record<string, any>)[key])
  try {
    if (isLocalDemoHost()) {
      const expense = findDemoExpense(id)
      const trace = demoTraceMap[id]
      Object.assign(detailData, expense || {})
      Object.assign(traceData, trace || {})
      return
    }
    const [expense, trace] = await Promise.all([FinanceExpenseApi.getFinanceExpense(id), FinanceExpenseApi.getFinanceExpenseTrace(id)])
    Object.assign(detailData, expense || {})
    Object.assign(traceData, trace || {})
  } catch (error: any) {
    if (!isLocalDemoHost()) {
      detailLoadError.value = error?.message || '费用单详情加载失败'
    } else {
      const expense = findDemoExpense(id)
      const trace = demoTraceMap[id]
      Object.assign(detailData, expense || {})
      Object.assign(traceData, trace || {})
    }
  } finally {
    detailLoading.value = false
  }
}

const loadDetail = async () => {
  if (!detailId.value) return
  await openDetailDialog(detailId.value)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length || deleteLoadingIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条费用单吗？`, '删除费用单', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      closeOnClickModal: false
    })
  } catch {
    return
  }
  deleteLoadingIds.value = Array.from(new Set([...deleteLoadingIds.value, ...ids]))
  try {
    await FinanceExpenseApi.deleteFinanceExpense(ids)
    message.success('删除成功')
    await getList()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  } finally {
    deleteLoadingIds.value = deleteLoadingIds.value.filter((id) => !ids.includes(id))
  }
}

const openSubmitDialog = (row: ErpFinanceExpenseVO) => {
  if (!row.id || isSubmittingApproval(row.id)) {
    return
  }
  activeSubmitRowId.value = Number(row.id)
  submitApprovalIds.value = Array.from(new Set([...submitApprovalIds.value, Number(row.id)]))
  submitDialogRef.value?.open(row).catch(() => {
    activeSubmitRowId.value = undefined
    submitApprovalIds.value = submitApprovalIds.value.filter((id) => id !== Number(row.id))
  })
}

const handleSubmitSuccess = async () => {
  const rowId = activeSubmitRowId.value
  try {
    await getList()
    if (!rowId) {
      message.warning('提交请求已发送，请刷新后确认状态')
      return
    }
    const latestRow = list.value.find((item) => Number(item.id) === rowId)
    if (latestRow?.status === FINANCE_EXPENSE_STATUS.FAILED) {
      message.warning('提交已受理，但流程创建失败')
    } else if (
      latestRow?.status === FINANCE_EXPENSE_STATUS.PROCESS &&
      latestRow.processInstanceId
    ) {
      message.success('已提交审批，等待流程受理')
    } else {
      message.warning('提交请求已发送，请刷新后确认状态')
    }
    if (detailDrawerVisible.value && detailId.value === rowId) {
      await loadDetail()
    }
  } finally {
    if (rowId) {
      submitApprovalIds.value = submitApprovalIds.value.filter((id) => id !== rowId)
    }
    activeSubmitRowId.value = undefined
  }
}

const handleSubmitDialogClose = () => {
  const rowId = activeSubmitRowId.value
  if (rowId) {
    submitApprovalIds.value = submitApprovalIds.value.filter((id) => id !== rowId)
  } else {
    submitApprovalIds.value = []
  }
  activeSubmitRowId.value = undefined
}

const handleCancelApproval = async (row: ErpFinanceExpenseVO) => {
  if (!row.id || cancelApprovalIds.value.includes(Number(row.id))) return
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回审批', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[\s\S]*.*\S[\s\S]*$/,
      inputErrorMessage: '撤回原因不能为空'
    })
    cancelApprovalIds.value = Array.from(
      new Set([...cancelApprovalIds.value, Number(row.id)])
    )
    await FinanceExpenseApi.cancelFinanceExpenseApproval({
      id: Number(row.id),
      reason: value
    })
    message.success('撤回审批成功')
    await getList()
    if (detailDrawerVisible.value && detailId.value === Number(row.id)) {
      await loadDetail()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      throw error
    }
  } finally {
    cancelApprovalIds.value = cancelApprovalIds.value.filter((id) => id !== Number(row.id))
  }
}

const handleProcessDetail = (row: ErpFinanceExpenseVO) => {
  if (!row.processInstanceId) {
    message.warning('当前费用单暂无审批流程')
    return
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId
    }
  })
}

const handleExport = async () => {
  if (exportLoading.value) return
  try {
    await ElMessageBox.confirm('确认导出当前筛选条件下的费用单吗？', '导出费用单', {
      type: 'info',
      confirmButtonText: '确认导出',
      cancelButtonText: '取消',
      closeOnClickModal: false
    })
  } catch {
    return
  }
  exportLoading.value = true
  try {
    const data = await FinanceExpenseApi.exportFinanceExpense(queryParams)
    download.excel(data, '研发报销零星采购.xls')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

watch(formDialogVisible, (visible) => {
  if (!visible) {
    closeFormDialog()
  }
})

watch(detailDrawerVisible, (visible) => {
  if (!visible) {
    detailId.value = null
    detailLoadError.value = ''
    Object.keys(detailData).forEach((key) => delete (detailData as Record<string, any>)[key])
    Object.keys(traceData).forEach((key) => delete (traceData as Record<string, any>)[key])
  }
})

onMounted(async () => {
  await Promise.allSettled([getList(), loadQueryOptions()])
})
</script>

<style scoped lang="scss">
@import '../shared/readOnlyPage.css';

.finance-shell__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-shell__row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.finance-shell__primary-cell--right {
  align-items: flex-end;
}

.finance-shell__dialog-panel,
.finance-shell__drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 180px);
  overflow: hidden;
}

.finance-shell__drawer {
  max-height: calc(100vh - 32px);
  padding: 0 0 16px;
}

.finance-shell__dialog-context,
.finance-shell__drawer-context {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #ffffff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.18);
}

.finance-shell__drawer-context {
  background: linear-gradient(135deg, #0f172a 0%, #134e4a 100%);
}

.finance-shell__dialog-title,
.finance-shell__drawer-title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.finance-shell__dialog-meta,
.finance-shell__drawer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
}

.finance-shell__dialog-summary,
.finance-shell__drawer-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  width: min(420px, 100%);
}

.finance-shell__drawer-summary {
  grid-template-columns: repeat(3, minmax(120px, 1fr));
}

.finance-shell__dialog-summary-card,
.finance-shell__drawer-summary-card,
.finance-shell__info-card {
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(148, 163, 184, 0.06);
}

.finance-shell__drawer-summary-card {
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  backdrop-filter: blur(6px);
}

.finance-shell__dialog-summary-label,
.finance-shell__drawer-summary-label,
.finance-shell__info-label {
  color: #64748b;
  font-size: 12px;
}

.finance-shell__drawer-summary-label {
  color: rgba(255, 255, 255, 0.7);
}

.finance-shell__dialog-summary-value,
.finance-shell__drawer-summary-value,
.finance-shell__info-value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.finance-shell__drawer-summary-value {
  color: #fff;
}

.finance-shell__info-sub {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.finance-shell__drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
  padding-right: 4px;
}

.finance-shell__drawer-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.finance-shell__drawer-grid--trace {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.finance-shell__detail-table {
  overflow-x: auto;
}

.finance-shell__detail-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.finance-shell__dialog-grid--full {
  grid-column: 1 / -1;
}

.finance-shell__drawer-loading,
.finance-shell__drawer-error {
  padding: 8px 0 0;
}

@media (max-width: 1280px) {
  .finance-shell__drawer-grid,
  .finance-shell__drawer-grid--trace {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .finance-shell__drawer-summary {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
}

@media (max-width: 1024px) {
  .finance-shell__dialog-context,
  .finance-shell__drawer-context {
    flex-direction: column;
  }

  .finance-shell__dialog-summary,
  .finance-shell__drawer-summary {
    width: 100%;
  }

  .finance-shell__dialog-grid--wide {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-shell__dialog-grid--wide,
  .finance-shell__drawer-grid,
  .finance-shell__drawer-grid--trace,
  .finance-shell__dialog-summary,
  .finance-shell__drawer-summary {
    grid-template-columns: 1fr;
  }
}
</style>
