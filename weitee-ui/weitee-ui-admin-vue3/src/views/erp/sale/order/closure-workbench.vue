<template>
  <div class="closure-workbench">
    <ContentWrap class="closure-workbench__header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">销售闭环工作台</div>
        </div>
        <el-button
          type="primary"
          plain
          :loading="refreshing"
          :disabled="!canRefresh"
          @click="handleRefresh"
        >
          <Icon icon="ep:refresh" class="mr-5px" /> 刷新
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="closure-workbench__filter-card">
      <div class="section-title">筛选条件</div>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="84px"
        class="query-form"
        @submit.prevent
      >
        <div class="query-form__grid">
          <el-form-item label="销售单号" prop="no">
            <el-input
              v-model="queryParams.no"
              placeholder="请输入销售单号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="项目" prop="projectId">
            <el-select
              v-model="queryParams.projectId"
              placeholder="请选择项目"
              clearable
              filterable
              :loading="filterLoading"
            >
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="客户" prop="customerId">
            <el-select
              v-model="queryParams.customerId"
              placeholder="请选择客户"
              clearable
              filterable
              :loading="filterLoading"
            >
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="销售员" prop="saleUserId">
            <el-select
              v-model="queryParams.saleUserId"
              placeholder="请选择销售员"
              clearable
              filterable
              :loading="filterLoading"
            >
              <el-option
                v-for="item in saleUserList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="销售状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择销售状态" clearable>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="交付准备" prop="deliveryReadyStatus">
            <el-select
              v-model="queryParams.deliveryReadyStatus"
              placeholder="请选择交付准备状态"
              clearable
            >
              <el-option
                v-for="item in deliveryReadyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="query-form__actions">
          <el-button type="primary" :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button :disabled="!canReset" @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="closure-workbench__table-card">
      <div class="table-toolbar">
        <div class="section-title">闭环列表</div>
        <div class="table-toolbar__meta">
          <span class="table-toolbar__count">共 {{ formatCount(total) }} 条</span>
        </div>
      </div>

      <el-alert
        v-if="listError && hasRows"
        type="error"
        :closable="false"
        show-icon
        class="mb-12px"
        :title="listError"
      >
        <template #default>
          <el-button link type="primary" :disabled="!canRefresh" @click="handleRefresh">
            重新加载
          </el-button>
        </template>
      </el-alert>

      <div class="closure-table__wrap">
        <el-table
          v-loading="listLoading"
          :data="list"
          stripe
          class="closure-table"
          :show-overflow-tooltip="false"
        >
          <el-table-column label="订单信息" min-width="270">
            <template #header>
              <span class="column-header">
                <Icon icon="ep:document" class="column-header__icon" />
                订单信息
              </span>
            </template>
            <template #default="{ row }">
              <div class="order-cell">
                <div class="order-cell__no" :title="row.no">{{ row.no || '-' }}</div>
                <div class="order-cell__meta">
                  <span v-if="row.projectName" :title="row.projectName">{{ row.projectName }}</span>
                  <span v-if="row.customerName" :title="row.customerName">{{
                    row.customerName
                  }}</span>
                  <span v-if="row.saleUserName">{{ row.saleUserName }}</span>
                </div>
                <div class="order-cell__tags">
                  <span
                    class="status-badge"
                    :class="resolveDeliveryReadyClass(row.deliveryReadyStatus)"
                  >
                    {{ resolveDeliveryReadyLabel(row.deliveryReadyStatus) }}
                  </span>
                  <el-tag
                    :type="resolveErpAuditStatusTagType(row.status, undefined)"
                    size="small"
                    effect="plain"
                  >
                    {{ resolveErpAuditStatusLabel(row.status, undefined) }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="闭环阶段" min-width="250">
            <template #header>
              <span class="column-header column-header--pipeline">
                <Icon icon="ep:guide" class="column-header__icon" />
                闭环阶段
              </span>
            </template>
            <template #default="{ row }">
              <div class="stage-cell">
                <span
                  class="status-badge status-badge--large"
                  :class="resolveClosureStageClass(row.closureSummary?.closureStage)"
                >
                  {{ resolveClosureStageLabel(row.closureSummary?.closureStage) }}
                </span>
                <div class="blocker-tags">
                  <span
                    v-for="blocker in resolveBlockerLabels(row.closureSummary?.blockerCodes)"
                    :key="blocker"
                    class="metric-pill metric-pill--warning"
                  >
                    {{ blocker }}
                  </span>
                  <span
                    v-if="!resolveBlockerLabels(row.closureSummary?.blockerCodes).length"
                    class="metric-pill metric-pill--success"
                  >
                    无阻塞
                  </span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="需求准备" min-width="320">
            <template #header>
              <span class="column-header column-header--pipeline">
                <Icon icon="ep:operation" class="column-header__icon" />
                需求准备
              </span>
            </template>
            <template #default="{ row }">
              <div class="pipeline-stack">
                <div class="pipeline-item">
                  <div class="pipeline-item__head">
                    <span>采购</span>
                    <strong>
                      {{ formatCount(row.closureSummary?.purchaseConvertedSuggestCount) }} /
                      {{ formatCount(row.closureSummary?.purchaseSuggestCount) }}
                    </strong>
                  </div>
                  <div class="mini-progress">
                    <span
                      class="mini-progress__bar mini-progress__bar--success"
                      :style="{
                        width: progressWidth(
                          row.closureSummary?.purchaseConvertedSuggestCount,
                          row.closureSummary?.purchaseSuggestCount
                        )
                      }"
                    ></span>
                  </div>
                  <div class="metric-pills">
                    <span class="metric-pill metric-pill--primary">
                      总 {{ formatCount(row.closureSummary?.purchaseSuggestCount) }}
                    </span>
                    <span class="metric-pill metric-pill--success">
                      确认 {{ formatCount(row.closureSummary?.purchaseConfirmedSuggestCount) }}
                    </span>
                    <span class="metric-pill metric-pill--neutral">
                      转单 {{ formatCount(row.closureSummary?.purchaseConvertedSuggestCount) }}
                    </span>
                    <span
                      v-if="toFiniteNumber(row.closureSummary?.purchaseRejectedSuggestCount) > 0"
                      class="metric-pill metric-pill--danger"
                    >
                      驳回 {{ formatCount(row.closureSummary?.purchaseRejectedSuggestCount) }}
                    </span>
                  </div>
                </div>
                <div class="pipeline-item">
                  <div class="pipeline-item__head">
                    <span>生产</span>
                    <strong>
                      {{ formatCount(row.closureSummary?.productionConvertedSuggestCount) }} /
                      {{ formatCount(row.closureSummary?.productionSuggestCount) }}
                    </strong>
                  </div>
                  <div class="mini-progress">
                    <span
                      class="mini-progress__bar mini-progress__bar--primary"
                      :style="{
                        width: progressWidth(
                          row.closureSummary?.productionConvertedSuggestCount,
                          row.closureSummary?.productionSuggestCount
                        )
                      }"
                    ></span>
                  </div>
                  <div class="metric-pills">
                    <span class="metric-pill metric-pill--primary">
                      总 {{ formatCount(row.closureSummary?.productionSuggestCount) }}
                    </span>
                    <span class="metric-pill metric-pill--success">
                      确认 {{ formatCount(row.closureSummary?.productionConfirmedSuggestCount) }}
                    </span>
                    <span class="metric-pill metric-pill--neutral">
                      转单 {{ formatCount(row.closureSummary?.productionConvertedSuggestCount) }}
                    </span>
                    <span
                      v-if="toFiniteNumber(row.closureSummary?.productionRejectedSuggestCount) > 0"
                      class="metric-pill metric-pill--danger"
                    >
                      驳回 {{ formatCount(row.closureSummary?.productionRejectedSuggestCount) }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="采购执行" min-width="310">
            <template #header>
              <span class="column-header column-header--pipeline">
                <Icon icon="ep:shopping-cart" class="column-header__icon" />
                采购执行
              </span>
            </template>
            <template #default="{ row }">
              <div class="pipeline-stack">
                <div class="pipeline-item">
                  <div class="pipeline-item__head">
                    <span>订单</span>
                    <strong>
                      {{ formatCount(row.closureSummary?.approvedPurchaseOrderCount) }} /
                      {{ formatCount(row.closureSummary?.purchaseOrderCount) }}
                    </strong>
                  </div>
                  <div class="mini-progress">
                    <span
                      class="mini-progress__bar mini-progress__bar--success"
                      :style="{
                        width: progressWidth(
                          row.closureSummary?.approvedPurchaseOrderCount,
                          row.closureSummary?.purchaseOrderCount
                        )
                      }"
                    ></span>
                  </div>
                  <div class="metric-pills">
                    <span class="metric-pill metric-pill--primary">
                      总 {{ formatCount(row.closureSummary?.purchaseOrderCount) }}
                    </span>
                    <span class="metric-pill metric-pill--success">
                      审批 {{ formatCount(row.closureSummary?.approvedPurchaseOrderCount) }}
                    </span>
                  </div>
                </div>
                <div class="pipeline-item">
                  <div class="pipeline-item__head">
                    <span>入库</span>
                    <strong>
                      {{ formatCount(row.closureSummary?.stockedPurchaseInCount) }} /
                      {{ formatCount(row.closureSummary?.purchaseInCount) }}
                    </strong>
                  </div>
                  <div class="mini-progress">
                    <span
                      class="mini-progress__bar mini-progress__bar--primary"
                      :style="{
                        width: progressWidth(
                          row.closureSummary?.stockedPurchaseInCount,
                          row.closureSummary?.purchaseInCount
                        )
                      }"
                    ></span>
                  </div>
                  <div class="metric-pills">
                    <span class="metric-pill metric-pill--warning">
                      待检 {{ formatCount(row.closureSummary?.pendingQaPurchaseInCount) }}
                    </span>
                    <span class="metric-pill metric-pill--neutral">
                      待入 {{ formatCount(row.closureSummary?.pendingStockInPurchaseInCount) }}
                    </span>
                    <span class="metric-pill metric-pill--success">
                      已入 {{ formatCount(row.closureSummary?.stockedPurchaseInCount) }}
                    </span>
                    <span
                      v-if="toFiniteNumber(row.closureSummary?.noNeedStockInPurchaseInCount) > 0"
                      class="metric-pill metric-pill--neutral"
                    >
                      免入 {{ formatCount(row.closureSummary?.noNeedStockInPurchaseInCount) }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="交付收敛" min-width="220" align="right">
            <template #header>
              <span class="column-header column-header--right column-header--pipeline">
                <Icon icon="ep:truck" class="column-header__icon" />
                交付收敛
              </span>
            </template>
            <template #default="{ row }">
              <div class="delivery-cell">
                <div class="delivery-metric">
                  <span>待发</span>
                  <strong>{{ formatQuantity(row.closureSummary?.remainingShipQty) }}</strong>
                </div>
                <div class="delivery-metric">
                  <span>合格</span>
                  <strong>{{ formatQuantity(row.closureSummary?.productionQualifiedQty) }}</strong>
                </div>
                <span
                  class="status-badge"
                  :class="resolveDeliveryReadyClass(row.deliveryReadyStatus)"
                >
                  {{ resolveDeliveryReadyLabel(row.deliveryReadyStatus) }}
                </span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" fixed="right" width="180" align="center">
            <template #header>
              <span class="column-header column-header--center">
                <Icon icon="ep:setting" class="column-header__icon" />
                操作
              </span>
            </template>
            <template #default="{ row }">
              <div class="row-actions">
                <el-button
                  link
                  type="primary"
                  :disabled="isRowNavigating(row.id)"
                  @click="openSaleOrderDetail(row)"
                >
                  查看详情
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="isRowNavigating(row.id)"
                  @click="openMrpTrace(row)"
                >
                  MRP
                </el-button>
                <el-button
                  v-if="canViewPurchaseOrder"
                  link
                  type="primary"
                  :disabled="isRowNavigating(row.id)"
                  v-hasPermi="['erp:purchase-order:query']"
                  @click="openPurchaseTrace(row)"
                >
                  采购
                </el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <div v-if="listError" class="closure-empty closure-empty--error">
              <div class="closure-empty__icon">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="closure-empty__title">闭环数据加载失败</div>
              <el-button type="primary" plain :disabled="!canRefresh" @click="handleRefresh">
                重新加载
              </el-button>
            </div>
            <div v-else class="closure-empty">
              <div class="closure-empty__icon">
                <Icon icon="ep:document" />
              </div>
              <div class="closure-empty__title">暂无闭环数据</div>
            </div>
          </template>
        </el-table>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <el-drawer
      v-model="detailDrawerVisible"
      class="sale-order-detail-drawer"
      :size="detailDrawerSize"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :destroy-on-close="true"
      append-to-body
      modal-class="sale-order-detail-drawer__mask"
      @closed="handleDetailDrawerClosed"
    >
      <template #header>
        <div class="detail-drawer__header">
          <div>
            <div class="detail-drawer__title">销售闭环详情</div>
            <div class="detail-drawer__subtitle">快速查看闭环进度与阻塞项</div>
          </div>
          <el-button link type="primary" :disabled="!canRefreshDetail" @click="refreshDetail">
            刷新详情
          </el-button>
        </div>
      </template>

      <div class="detail-drawer__body">
        <div v-if="detailLoading" class="detail-loading">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 132px" />
              <div class="mt-16px grid gap-12px">
                <el-skeleton-item variant="rect" style="width: 100%; height: 184px" />
                <el-skeleton-item variant="rect" style="width: 100%; height: 132px" />
                <el-skeleton-item variant="rect" style="width: 100%; height: 164px" />
              </div>
            </template>
          </el-skeleton>
        </div>

        <div v-else-if="showDetailError" class="detail-state detail-state--error">
          <div class="detail-state__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="detail-state__title">详情加载失败</div>
          <div class="detail-state__description">{{ detailLoadError }}</div>
          <el-button type="primary" plain @click="retryLoadDetail">重试</el-button>
        </div>

        <div v-else-if="showDetailEmpty" class="detail-state">
          <div class="detail-state__icon">
            <Icon icon="ep:document-remove" />
          </div>
          <div class="detail-state__title">暂无详情数据</div>
        </div>

        <div v-else-if="detailOrderView" class="detail-content">
          <section class="detail-summary-card">
            <div class="detail-summary-card__label">销售单号</div>
            <div class="detail-summary-card__no">{{ detailOrderView.no || '-' }}</div>
            <div class="detail-summary-card__meta">
              <span :title="detailOrderView.customerName || '-'">
                {{ detailOrderView.customerName || '-' }}
              </span>
              <span :title="detailOrderView.projectName || '未关联项目'">
                {{ detailOrderView.projectName || '未关联项目' }}
              </span>
              <span>{{ detailOrderView.saleUserName || '-' }}</span>
            </div>
            <div class="detail-summary-card__chips">
              <span
                class="detail-summary-chip"
                :class="
                  resolveSummaryChipClass(resolveDeliveryReadyClass(detailOrderView.deliveryReadyStatus))
                "
              >
                {{ resolveDeliveryReadyLabel(detailOrderView.deliveryReadyStatus) }}
              </span>
              <span
                class="detail-summary-chip"
                :class="
                  resolveSummaryChipClass(
                    `status-badge--${resolveErpAuditStatusTagType(detailOrderView.status, undefined) || 'neutral'}`
                  )
                "
              >
                {{ resolveErpAuditStatusLabel(detailOrderView.status, undefined) }}
              </span>
              <span
                class="detail-summary-chip"
                :class="resolveSummaryChipClass(resolveClosureStageClass(detailClosureSummary?.closureStage))"
              >
                {{ resolveClosureStageLabel(detailClosureSummary?.closureStage) }}
              </span>
            </div>
          </section>

          <section class="detail-panel">
            <div class="detail-panel__header">
              <div class="detail-panel__title detail-panel__title--with-icon">
                <Icon icon="ep:trend-charts" class="detail-panel__title-icon" />
                <span>闭环进度</span>
              </div>
              <span v-if="detailRefreshing" class="detail-panel__meta">刷新中...</span>
            </div>
            <div class="detail-progress-grid">
              <div class="pipeline-item">
                <div class="pipeline-item__head">
                  <div class="pipeline-item__head-label">
                    <Icon icon="ep:shopping-bag" class="pipeline-item__icon pipeline-item__icon--cyan" />
                    <span>采购建议</span>
                  </div>
                  <strong>
                    {{ formatCount(detailClosureSummary?.purchaseConvertedSuggestCount) }} /
                    {{ formatCount(detailClosureSummary?.purchaseSuggestCount) }}
                  </strong>
                </div>
                <div class="mini-progress">
                  <span
                    class="mini-progress__bar mini-progress__bar--success"
                    :style="{
                      width: progressWidth(
                        detailClosureSummary?.purchaseConvertedSuggestCount,
                        detailClosureSummary?.purchaseSuggestCount
                      )
                    }"
                  ></span>
                </div>
                <div class="metric-pills">
                  <span class="metric-pill metric-pill--primary">
                    总 {{ formatCount(detailClosureSummary?.purchaseSuggestCount) }}
                  </span>
                  <span class="metric-pill metric-pill--success">
                    确认 {{ formatCount(detailClosureSummary?.purchaseConfirmedSuggestCount) }}
                  </span>
                  <span class="metric-pill metric-pill--neutral">
                    转单 {{ formatCount(detailClosureSummary?.purchaseConvertedSuggestCount) }}
                  </span>
                </div>
              </div>

              <div class="pipeline-item">
                <div class="pipeline-item__head">
                  <div class="pipeline-item__head-label">
                    <Icon icon="ep:tools" class="pipeline-item__icon pipeline-item__icon--slate" />
                    <span>生产建议</span>
                  </div>
                  <strong>
                    {{ formatCount(detailClosureSummary?.productionConvertedSuggestCount) }} /
                    {{ formatCount(detailClosureSummary?.productionSuggestCount) }}
                  </strong>
                </div>
                <div class="mini-progress">
                  <span
                    class="mini-progress__bar mini-progress__bar--primary"
                    :style="{
                      width: progressWidth(
                        detailClosureSummary?.productionConvertedSuggestCount,
                        detailClosureSummary?.productionSuggestCount
                      )
                    }"
                  ></span>
                </div>
                <div class="metric-pills">
                  <span class="metric-pill metric-pill--primary">
                    总 {{ formatCount(detailClosureSummary?.productionSuggestCount) }}
                  </span>
                  <span class="metric-pill metric-pill--success">
                    确认 {{ formatCount(detailClosureSummary?.productionConfirmedSuggestCount) }}
                  </span>
                  <span class="metric-pill metric-pill--neutral">
                    转单 {{ formatCount(detailClosureSummary?.productionConvertedSuggestCount) }}
                  </span>
                </div>
              </div>

              <div class="pipeline-item">
                <div class="pipeline-item__head">
                  <div class="pipeline-item__head-label">
                    <Icon icon="ep:shopping-bag" class="pipeline-item__icon pipeline-item__icon--emerald" />
                    <span>采购执行</span>
                  </div>
                  <strong>
                    {{ formatCount(detailClosureSummary?.approvedPurchaseOrderCount) }} /
                    {{ formatCount(detailClosureSummary?.purchaseOrderCount) }}
                  </strong>
                </div>
                <div class="mini-progress">
                  <span
                    class="mini-progress__bar mini-progress__bar--success"
                    :style="{
                      width: progressWidth(
                        detailClosureSummary?.approvedPurchaseOrderCount,
                        detailClosureSummary?.purchaseOrderCount
                      )
                    }"
                  ></span>
                </div>
                <div class="metric-pills">
                  <span class="metric-pill metric-pill--primary">
                    订单 {{ formatCount(detailClosureSummary?.purchaseOrderCount) }}
                  </span>
                  <span class="metric-pill metric-pill--success">
                    审批 {{ formatCount(detailClosureSummary?.approvedPurchaseOrderCount) }}
                  </span>
                </div>
              </div>

              <div class="pipeline-item">
                <div class="pipeline-item__head">
                  <div class="pipeline-item__head-label">
                    <Icon icon="ep:van" class="pipeline-item__icon pipeline-item__icon--amber" />
                    <span>交付收敛</span>
                  </div>
                  <strong>
                    {{ formatQuantity(detailClosureSummary?.productionQualifiedQty) }} /
                    {{ formatQuantity(detailClosureSummary?.remainingShipQty) }}
                  </strong>
                </div>
                <div class="metric-pills">
                  <span class="metric-pill metric-pill--warning">
                    待发 {{ formatQuantity(detailClosureSummary?.remainingShipQty) }}
                  </span>
                  <span class="metric-pill metric-pill--success">
                    合格 {{ formatQuantity(detailClosureSummary?.productionQualifiedQty) }}
                  </span>
                </div>
              </div>
            </div>
          </section>

          <section class="detail-panel">
            <div class="detail-panel__title">阻塞项</div>
            <div class="blocker-tags blocker-tags--drawer">
              <span
                v-for="blocker in detailBlockerLabels"
                :key="blocker"
                class="metric-pill metric-pill--warning"
              >
                {{ blocker }}
              </span>
              <span v-if="!detailBlockerLabels.length" class="metric-pill metric-pill--success">
                无阻塞
              </span>
            </div>
          </section>

          <section class="detail-panel">
            <div class="detail-panel__title detail-panel__title--with-icon">
              <Icon icon="ep:info-filled" class="detail-panel__title-icon" />
              <span>基础信息</span>
            </div>
            <div class="detail-info-grid">
              <div class="detail-info-item">
                <span class="detail-info-item__label">交期</span>
                <strong>{{ formatDateCell(detailOrderView.deliveryDate) }}</strong>
              </div>
              <div class="detail-info-item">
                <span class="detail-info-item__label">订单数量</span>
                <strong class="detail-info-item__value-inline">
                  {{ formatQuantity(detailOrderView.totalCount) }}
                  <span v-if="detailOrderView.totalCountUnitName" class="detail-info-item__unit">
                    {{ detailOrderView.totalCountUnitName }}
                  </span>
                </strong>
              </div>
              <div class="detail-info-item">
                <span class="detail-info-item__label">订单金额</span>
                <strong>{{ formatCurrency(detailOrderView.totalPrice) }}</strong>
              </div>
              <div class="detail-info-item">
                <span class="detail-info-item__label">审核状态</span>
                <strong>{{ resolveErpAuditStatusLabel(detailOrderView.status, undefined) }}</strong>
              </div>
            </div>
          </section>
        </div>
      </div>

      <template #footer>
        <div class="detail-drawer__footer">
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
          <div class="detail-drawer__footer-actions">
            <el-button
              v-if="canViewPurchaseOrder"
              plain
              :loading="navigatingToPurchase"
              :disabled="!canNavigatePurchase"
              @click="openPurchaseTraceFromDetail"
            >
              查看采购
            </el-button>
            <el-button
              plain
              :loading="navigatingToMrp"
              :disabled="!canNavigateMrp"
              @click="openMrpTraceFromDetail"
            >
              查看 MRP
            </el-button>
            <el-button
              type="primary"
              :loading="navigatingToFullOrder"
              :disabled="!canNavigateFullOrder"
              @click="openFullOrderPage"
            >
              查看完整订单
            </el-button>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import {
  SaleOrderApi,
  type SaleOrderVO,
  type SaleOrderClosurePageReqVO,
  type SaleOrderClosurePageVO
} from '@/api/erp/sale/order'
import { resolveErpAuditStatusLabel, resolveErpAuditStatusTagType } from '@/utils/erpAuditStatus'
import { checkPermi } from '@/utils/permission'
import { ElMessage } from 'element-plus'
import { CustomerApi, type CustomerVO } from '@/api/erp/sale/customer'
import { ProjectApi, type ProjectSimpleVO } from '@/api/erp/project'
import { getSimpleUserList, type SimpleUserVO } from '@/api/system/user'
import {
  buildClosureTraceQuery,
  buildSaleOrderDetailRoute
} from './closureWorkbenchDetail.helpers'

defineOptions({ name: 'ErpSaleOrderClosureWorkbench' })

const DELIVERY_READY_STATUS = {
  NOT_READY: 'NOT_READY',
  PART_READY: 'PART_READY',
  READY_TO_SHIP: 'READY_TO_SHIP'
} as const

const closureStageLabelMap: Record<string, string> = {
  WAIT_SALE_APPROVAL: '待销售审核',
  WAIT_PURCHASE_SUGGEST_CONFIRM: '待采购建议确认',
  WAIT_PURCHASE_ORDER_CONVERT: '待采购转单',
  WAIT_PURCHASE_APPROVAL: '待采购审核',
  WAIT_PURCHASE_IQC: '待来料质检',
  WAIT_PURCHASE_STOCK_IN: '待采购入库',
  WAIT_PRODUCTION_SUGGEST_CONFIRM: '待生产建议确认',
  WAIT_PRODUCTION_ORDER_CONVERT: '待生产转单',
  WAIT_DELIVERY_READY: '待交付准备',
  PART_READY: '部分就绪',
  READY_TO_SHIP: '可发货',
  CLOSED: '已闭环'
}

const closureStageClassMap: Record<string, string> = {
  WAIT_SALE_APPROVAL: 'status-badge--danger',
  WAIT_PURCHASE_SUGGEST_CONFIRM: 'status-badge--warning',
  WAIT_PURCHASE_ORDER_CONVERT: 'status-badge--warning',
  WAIT_PURCHASE_APPROVAL: 'status-badge--warning',
  WAIT_PURCHASE_IQC: 'status-badge--warning',
  WAIT_PURCHASE_STOCK_IN: 'status-badge--warning',
  WAIT_PRODUCTION_SUGGEST_CONFIRM: 'status-badge--warning',
  WAIT_PRODUCTION_ORDER_CONVERT: 'status-badge--warning',
  WAIT_DELIVERY_READY: 'status-badge--neutral',
  PART_READY: 'status-badge--primary',
  READY_TO_SHIP: 'status-badge--success',
  CLOSED: 'status-badge--success'
}

const deliveryReadyOptions = [
  { label: '暂无可发', value: DELIVERY_READY_STATUS.NOT_READY },
  { label: '部分就绪', value: DELIVERY_READY_STATUS.PART_READY },
  { label: '可发货', value: DELIVERY_READY_STATUS.READY_TO_SHIP }
]

const buildDefaultQueryParams = (): SaleOrderClosurePageReqVO => ({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  customerId: undefined,
  projectId: undefined,
  saleUserId: undefined,
  status: undefined,
  deliveryReadyStatus: undefined
})

const router = useRouter()
const { width } = useWindowSize()
const canViewPurchaseOrder = checkPermi(['erp:purchase-order:query'])
const queryFormRef = ref()
const listLoading = ref(false)
const filterLoading = ref(false)
const refreshing = ref(false)
const listError = ref('')
const navigatingRowId = ref<number>()
const list = ref<SaleOrderClosurePageVO[]>([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const detailData = ref<SaleOrderVO>()
const detailRowSnapshot = ref<SaleOrderClosurePageVO>()
const detailLoading = ref(false)
const detailRefreshing = ref(false)
const detailLoadError = ref('')
const selectedOrderId = ref<number>()
const navigatingToFullOrder = ref(false)
const navigatingToMrp = ref(false)
const navigatingToPurchase = ref(false)
const projectList = ref<ProjectSimpleVO[]>([])
const customerList = ref<CustomerVO[]>([])
const saleUserList = ref<SimpleUserVO[]>([])
const queryParams = reactive<SaleOrderClosurePageReqVO>(buildDefaultQueryParams())

const hasRows = computed(() => list.value.length > 0)
const detailDrawerSize = computed(() => (width.value < 1024 ? '92%' : '480px'))
const hasActiveFilters = computed(
  () =>
    !!queryParams.no ||
    queryParams.customerId !== undefined ||
    queryParams.projectId !== undefined ||
    queryParams.saleUserId !== undefined ||
    queryParams.status !== undefined ||
    !!queryParams.deliveryReadyStatus
)
const isListBusy = computed(() => listLoading.value || refreshing.value)
const canQuery = computed(() => !isListBusy.value)
const canRefresh = computed(() => !isListBusy.value)
const canReset = computed(
  () => (hasActiveFilters.value || queryParams.pageNo !== 1) && !isListBusy.value
)
const detailClosureSummary = computed(
  () => detailData.value?.closureSummary || detailRowSnapshot.value?.closureSummary || null
)
const detailOrderView = computed(() => ({
  ...detailRowSnapshot.value,
  ...detailData.value,
  deliveryReadyStatus:
    detailData.value?.deliveryReadyStatus || detailRowSnapshot.value?.deliveryReadyStatus,
  customerName: detailData.value?.customerName || detailRowSnapshot.value?.customerName,
  projectName: detailData.value?.projectName || detailRowSnapshot.value?.projectName,
  saleUserName: detailData.value?.saleUserName || detailRowSnapshot.value?.saleUserName
}))
const detailBlockerLabels = computed(() =>
  resolveBlockerLabels(detailClosureSummary.value?.blockerCodes || [])
)
const showDetailError = computed(() => !!detailLoadError.value && !detailLoading.value)
const showDetailEmpty = computed(
  () =>
    detailDrawerVisible.value &&
    !detailLoading.value &&
    !detailLoadError.value &&
    !detailOrderView.value?.id
)
const canRefreshDetail = computed(
  () =>
    detailDrawerVisible.value &&
    !!selectedOrderId.value &&
    !detailLoading.value &&
    !detailRefreshing.value
)
const canNavigateFullOrder = computed(
  () => !!selectedOrderId.value && !detailLoading.value && !navigatingToFullOrder.value
)
const canNavigateMrp = computed(
  () => !!selectedOrderId.value && !detailLoading.value && !navigatingToMrp.value
)
const canNavigatePurchase = computed(
  () =>
    canViewPurchaseOrder &&
    !!selectedOrderId.value &&
    !detailLoading.value &&
    !navigatingToPurchase.value
)

const numberFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 3
})

const integerFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 0
})

const toFiniteNumber = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return 0
  }
  const normalized = Number(value)
  return Number.isFinite(normalized) ? normalized : 0
}

const progressWidth = (current?: number | string | null, totalValue?: number | string | null) => {
  const totalNumber = toFiniteNumber(totalValue)
  if (totalNumber <= 0) {
    return '0%'
  }
  const currentNumber = Math.max(0, Math.min(toFiniteNumber(current), totalNumber))
  return `${Math.round((currentNumber / totalNumber) * 100)}%`
}

const resolveDeliveryReadyLabel = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return '部分就绪'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return '可发货'
  }
  return '暂无可发'
}

const resolveDeliveryReadyClass = (status?: string) => {
  if (status === DELIVERY_READY_STATUS.PART_READY) {
    return 'status-badge--warning'
  }
  if (status === DELIVERY_READY_STATUS.READY_TO_SHIP) {
    return 'status-badge--success'
  }
  return 'status-badge--neutral'
}

const resolveSummaryChipClass = (badgeClass?: string) => {
  if (badgeClass === 'status-badge--success') {
    return 'detail-summary-chip--success'
  }
  if (badgeClass === 'status-badge--warning') {
    return 'detail-summary-chip--warning'
  }
  if (badgeClass === 'status-badge--danger') {
    return 'detail-summary-chip--danger'
  }
  if (badgeClass === 'status-badge--primary') {
    return 'detail-summary-chip--primary'
  }
  return 'detail-summary-chip--neutral'
}

const resolveClosureStageLabel = (stage?: string) => {
  if (!stage) {
    return '未识别'
  }
  return closureStageLabelMap[stage] || stage
}

const resolveClosureStageClass = (stage?: string) => {
  if (!stage) {
    return 'status-badge--neutral'
  }
  return closureStageClassMap[stage] || 'status-badge--neutral'
}

const resolveBlockerLabels = (blockerCodes?: string[] | null) => {
  if (!blockerCodes?.length) {
    return []
  }
  return blockerCodes.map((code) => closureStageLabelMap[code] || code)
}

const formatCount = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '0'
  }
  const normalized = Number(value)
  if (Number.isFinite(normalized)) {
    return integerFormatter.format(normalized)
  }
  return String(value)
}

const formatQuantity = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return '0'
  }
  const normalized = Number(value)
  if (Number.isFinite(normalized)) {
    return numberFormatter.format(normalized)
  }
  return String(value)
}

const formatCurrency = (value?: number | string | null) => {
  const normalized = toFiniteNumber(value)
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(normalized)
}

const formatDateCell = (value?: string | number | Date | null) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  return date.toLocaleDateString('zh-CN')
}

const getList = async (options?: { force?: boolean }) => {
  if (listLoading.value || (refreshing.value && !options?.force)) {
    return
  }
  listLoading.value = true
  listError.value = ''
  try {
    const data = await SaleOrderApi.getSaleOrderClosureSummaryPage({ ...queryParams })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    listError.value = '销售闭环数据加载失败，请重试'
    if (!hasRows.value) {
      list.value = []
      total.value = 0
    }
  } finally {
    listLoading.value = false
  }
}

const loadFilterOptions = async () => {
  filterLoading.value = true
  try {
    const [projectResult, customerResult, userResult] = await Promise.allSettled([
      ProjectApi.getProjectSimpleList(),
      CustomerApi.getCustomerSimpleList(),
      getSimpleUserList()
    ])

    if (projectResult.status === 'fulfilled') {
      projectList.value = projectResult.value
    }
    if (customerResult.status === 'fulfilled') {
      customerList.value = customerResult.value
    }
    if (userResult.status === 'fulfilled') {
      saleUserList.value = userResult.value
    }
  } finally {
    filterLoading.value = false
  }
}

const handleQuery = async () => {
  if (!canQuery.value) {
    return
  }
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  if (!canReset.value) {
    return
  }
  Object.assign(queryParams, buildDefaultQueryParams())
  queryFormRef.value?.resetFields()
  await getList()
}

const handleRefresh = async () => {
  if (!canRefresh.value) {
    return
  }
  refreshing.value = true
  try {
    await getList({ force: true })
  } finally {
    refreshing.value = false
  }
}

const navigateWithRowLock = async (rowId: number, handler: () => Promise<void>) => {
  if (navigatingRowId.value === rowId) {
    return
  }
  navigatingRowId.value = rowId
  try {
    await handler()
  } finally {
    navigatingRowId.value = undefined
  }
}

const isRowNavigating = (rowId: number) => navigatingRowId.value === rowId

const resetDetailState = () => {
  detailData.value = undefined
  detailRowSnapshot.value = undefined
  selectedOrderId.value = undefined
  detailLoading.value = false
  detailRefreshing.value = false
  detailLoadError.value = ''
  navigatingToFullOrder.value = false
  navigatingToMrp.value = false
  navigatingToPurchase.value = false
}

const loadDetailData = async (row: SaleOrderClosurePageVO, mode: 'open' | 'refresh' = 'open') => {
  if (mode === 'open') {
    detailLoading.value = true
    detailLoadError.value = ''
    detailData.value = undefined
  } else {
    detailRefreshing.value = true
  }
  detailRowSnapshot.value = row
  selectedOrderId.value = row.id
  try {
    detailData.value = await SaleOrderApi.getSaleOrder(row.id)
  } catch (error: any) {
    if (mode === 'refresh' && detailOrderView.value?.id) {
      ElMessage.error(error?.message || '详情刷新失败，请重试')
    } else {
      detailLoadError.value = error?.message || '详情加载失败，请重试'
    }
  } finally {
    detailLoading.value = false
    detailRefreshing.value = false
  }
}

const openSaleOrderDetail = async (row: SaleOrderClosurePageVO) => {
  await navigateWithRowLock(row.id, async () => {
    detailDrawerVisible.value = true
    await loadDetailData(row, 'open')
  })
}

const buildTraceQuery = (row: Pick<SaleOrderClosurePageVO, 'id' | 'no'>, extraQuery?: Record<string, string>) =>
  buildClosureTraceQuery(row, extraQuery)

const openMrpTrace = async (row: SaleOrderClosurePageVO) => {
  await navigateWithRowLock(row.id, async () => {
    await router.push({
      name: 'ErpSaleOrderMrpTracePage',
      query: buildTraceQuery(row, { tab: 'purchase' })
    })
  })
}

const openPurchaseTrace = async (row: SaleOrderClosurePageVO) => {
  if (!canViewPurchaseOrder) {
    return
  }
  await navigateWithRowLock(row.id, async () => {
    await router.push({
      name: 'ErpSaleOrderPurchaseOrderTracePage',
      query: buildTraceQuery(row)
    })
  })
}

const retryLoadDetail = async () => {
  if (!detailRowSnapshot.value || detailLoading.value || detailRefreshing.value) {
    return
  }
  await loadDetailData(detailRowSnapshot.value, 'open')
}

const refreshDetail = async () => {
  if (!detailRowSnapshot.value || !canRefreshDetail.value) {
    return
  }
  await loadDetailData(detailRowSnapshot.value, 'refresh')
}

const handleDetailDrawerClosed = () => {
  resetDetailState()
}

const openFullOrderPage = async () => {
  if (!selectedOrderId.value || navigatingToFullOrder.value) {
    return
  }
  navigatingToFullOrder.value = true
  try {
    await router.push(buildSaleOrderDetailRoute(selectedOrderId.value))
    detailDrawerVisible.value = false
  } finally {
    navigatingToFullOrder.value = false
  }
}

const openMrpTraceFromDetail = async () => {
  const currentOrder = detailOrderView.value
  if (!currentOrder?.id || navigatingToMrp.value) {
    return
  }
  navigatingToMrp.value = true
  try {
    await router.push({
      name: 'ErpSaleOrderMrpTracePage',
      query: buildTraceQuery(currentOrder, { tab: 'purchase' })
    })
    detailDrawerVisible.value = false
  } finally {
    navigatingToMrp.value = false
  }
}

const openPurchaseTraceFromDetail = async () => {
  const currentOrder = detailOrderView.value
  if (!canViewPurchaseOrder || !currentOrder?.id || navigatingToPurchase.value) {
    return
  }
  navigatingToPurchase.value = true
  try {
    await router.push({
      name: 'ErpSaleOrderPurchaseOrderTracePage',
      query: buildTraceQuery(currentOrder)
    })
    detailDrawerVisible.value = false
  } finally {
    navigatingToPurchase.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([getList(), loadFilterOptions()])
})
</script>

<style scoped lang="scss">
.closure-workbench {
  --closure-primary-text: #2563eb;
  --closure-primary-bg: #eff6ff;
  --closure-primary-border: #bfdbfe;

  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  background: var(--el-bg-color-page);
}

:global(html.dark) .closure-workbench {
  --closure-primary-text: #60a5fa;
  --closure-primary-bg: rgb(37 99 235 / 16%);
  --closure-primary-border: rgb(96 165 250 / 34%);
}

.closure-workbench :deep(.el-card),
.closure-workbench :deep(.content-wrap) {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgb(15 23 42 / 6%);
}

.closure-workbench :deep(.el-card__body) {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-header__main {
  min-width: 0;
}

.page-header__title {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.section-title {
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.query-form__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 16px;
}

.query-form__grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.query-form__grid :deep(.el-select),
.query-form__grid :deep(.el-input) {
  width: 100%;
}

.query-form__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.table-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.table-toolbar .section-title {
  margin-bottom: 0;
}

.table-toolbar__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.table-toolbar__count {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.closure-table__wrap {
  overflow-x: auto;
}

.closure-table {
  min-width: 1390px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 10px;
}

.closure-table :deep(.el-table__header-wrapper th) {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
}

.closure-table :deep(.el-table__cell) {
  border-color: var(--el-border-color-lighter);
}

.column-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.column-header--pipeline {
  color: var(--closure-primary-text);
}

.column-header--right {
  justify-content: flex-end;
  width: 100%;
}

.column-header--center {
  justify-content: center;
  width: 100%;
}

.column-header__icon {
  color: var(--closure-primary-text);
  font-size: 14px;
}

.order-cell {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-width: 0;
}

.order-cell__no {
  display: inline-flex;
  width: fit-content;
  max-width: 100%;
  padding: 2px 6px;
  overflow: hidden;
  color: var(--el-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
}

.order-cell__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  min-width: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.order-cell__meta span {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-cell__meta span + span::before {
  margin-right: 10px;
  color: var(--el-border-color);
  content: '/';
}

.order-cell__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  min-height: 24px;
  padding: 0 9px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
}

.status-badge--large {
  min-height: 28px;
  padding: 0 12px;
  font-size: 13px;
}

.status-badge--primary {
  color: var(--closure-primary-text);
  background: var(--closure-primary-bg);
  border-color: var(--closure-primary-border);
}

.status-badge--success {
  color: var(--el-color-success);
  background: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-7);
}

.status-badge--warning {
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-7);
}

.status-badge--danger {
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-7);
}

.status-badge--neutral {
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-light);
}

.stage-cell,
.pipeline-stack,
.delivery-cell {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.blocker-tags,
.metric-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.metric-pill {
  display: inline-flex;
  align-items: center;
  min-height: 23px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
}

.metric-pill--primary {
  color: var(--closure-primary-text);
  background: var(--closure-primary-bg);
  border-color: var(--closure-primary-border);
}

.metric-pill--success {
  color: var(--el-color-success);
  background: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-7);
}

.metric-pill--warning {
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-7);
}

.metric-pill--danger {
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-7);
}

.metric-pill--neutral {
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-light);
}

.pipeline-item {
  padding: 10px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-light);
  border-radius: 10px;
}

.pipeline-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 7px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.pipeline-item__head-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.pipeline-item__icon {
  flex-shrink: 0;
  font-size: 15px;
  line-height: 1;
}

.pipeline-item__icon--cyan {
  color: var(--closure-primary-text);
}

.pipeline-item__icon--slate {
  color: var(--el-text-color-secondary);
}

.pipeline-item__icon--emerald {
  color: var(--el-color-success);
}

.pipeline-item__icon--amber {
  color: var(--el-color-warning);
}

.pipeline-item__head strong {
  color: var(--el-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
}

.mini-progress {
  height: 6px;
  margin-bottom: 8px;
  overflow: hidden;
  background: var(--el-border-color-lighter);
  border-radius: 999px;
}

.mini-progress__bar {
  display: block;
  height: 100%;
  border-radius: inherit;
  transition: width 0.2s ease;
}

.mini-progress__bar--primary {
  background: var(--closure-primary-text);
}

.mini-progress__bar--success {
  background: var(--el-color-success);
}

.delivery-cell {
  align-items: stretch;
}

.delivery-metric {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.delivery-metric strong {
  color: var(--el-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 15px;
}

.delivery-cell .status-badge {
  align-self: flex-end;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 4px 8px;
}

.row-actions :deep(.el-button + .el-button),
.closure-table :deep(.el-button + .el-button) {
  margin-left: 0;
}

.closure-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 220px;
  padding: 36px 16px;
  color: var(--el-text-color-secondary);
}

.closure-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: var(--closure-primary-text);
  font-size: 24px;
  background: var(--closure-primary-bg);
  border: 1px solid var(--closure-primary-border);
  border-radius: 12px;
}

.closure-empty__title {
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 600;
}

.closure-empty--error .closure-empty__icon {
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-7);
}

.sale-order-detail-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 20px 24px 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-drawer__body) {
    padding: 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  :deep(.el-drawer__footer) {
    padding: 0;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

.sale-order-detail-drawer__mask {
  backdrop-filter: blur(4px);
}

.detail-drawer__header,
.detail-drawer__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.detail-drawer__title {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 700;
}

.detail-drawer__subtitle {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.detail-drawer__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
  padding: 20px 24px 24px;
  overflow-y: auto;
  overscroll-behavior: contain;
  background: #f8fafc;
}

.detail-drawer__footer {
  padding: 16px 24px 20px;
  background: #fff;
}

.detail-drawer__footer-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.detail-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 280px;
  padding: 24px;
  text-align: center;
  background: #fff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 14px;
}

.detail-state__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: var(--closure-primary-text);
  font-size: 24px;
  background: var(--closure-primary-bg);
  border: 1px solid var(--closure-primary-border);
  border-radius: 12px;
}

.detail-state__title {
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 600;
}

.detail-state__description {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.detail-state--error .detail-state__icon {
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-7);
}

.detail-summary-card {
  padding: 18px;
  color: #fff;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  border-radius: 16px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 18%);
}

.detail-summary-card__label {
  color: rgb(226 232 240 / 88%);
  font-size: 12px;
}

.detail-summary-card__no {
  margin-top: 8px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
}

.detail-summary-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 10px;
  color: rgb(226 232 240 / 88%);
  font-size: 12px;
}

.detail-summary-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.detail-summary-chip {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
}

.detail-summary-chip--neutral {
  color: #fff;
  background: rgb(255 255 255 / 12%);
  border-color: rgb(255 255 255 / 8%);
}

.detail-summary-chip--success {
  color: #86efac;
  background: rgb(16 185 129 / 16%);
  border-color: rgb(16 185 129 / 28%);
}

.detail-summary-chip--warning {
  color: #fbbf24;
  background: rgb(245 158 11 / 16%);
  border-color: rgb(245 158 11 / 28%);
}

.detail-summary-chip--danger {
  color: #fda4af;
  background: rgb(244 63 94 / 16%);
  border-color: rgb(244 63 94 / 28%);
}

.detail-summary-chip--primary {
  color: #93c5fd;
  background: rgb(59 130 246 / 16%);
  border-color: rgb(59 130 246 / 28%);
}

.detail-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 14px;
  box-shadow: 0 1px 2px rgb(15 23 42 / 5%);
}

.detail-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.detail-panel__title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 700;
}

.detail-panel__title--with-icon {
  margin-bottom: 12px;
}

.detail-panel__title-icon {
  color: var(--closure-primary-text);
  font-size: 15px;
}

.detail-panel__meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.detail-progress-grid,
.detail-info-grid {
  display: grid;
  gap: 12px;
}

.detail-progress-grid {
  grid-template-columns: minmax(0, 1fr);
}

.detail-info-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.detail-panel .pipeline-item {
  padding: 12px;
  background: #fff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
}

.blocker-tags--drawer {
  margin-top: 4px;
}

.detail-info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-light);
  border-radius: 10px;
}

.detail-info-item__label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.detail-info-item strong {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  color: var(--el-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 14px;
}

.detail-info-item__value-inline {
  font-size: 15px;
}

.detail-info-item__unit {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

@media (max-width: 1439px) {
  .query-form__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1279px) {
  .query-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .closure-table {
    min-width: 1280px;
  }
}

@media (max-width: 767px) {
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .query-form__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .query-form__actions {
    width: 100%;
  }

  .query-form__actions :deep(.el-button) {
    flex: 1 1 auto;
  }

  .table-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-drawer__header,
  .detail-drawer__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .detail-info-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .detail-drawer__footer-actions {
    flex-direction: column;
    justify-content: stretch;
  }

  .detail-drawer__footer-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
