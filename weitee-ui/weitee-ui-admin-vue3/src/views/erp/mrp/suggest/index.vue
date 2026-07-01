<template>
  <div class="mrp-workbench-page">
    <section class="mrp-workbench-header">
      <div class="mrp-workbench-brand">
        <div class="mrp-workbench-brand__logo">
          <Icon icon="ep:box" class="h-5.5 w-5.5" />
        </div>
        <div class="mrp-workbench-brand__text">
          <div class="mrp-workbench-brand__title">MRP 智能计划与供应链协同系统</div>
          <div class="mrp-workbench-brand__subtitle">全阶层BOM算力驱动 实时物料流向监控</div>
        </div>
      </div>
      <div class="mrp-workbench-header__center">
        <span class="mrp-workbench-header__dot" :class="isProdActive ? 'is-production' : 'is-purchase'"></span>
        最新 MRP 算力校准时间：2026-05-20 08:00
      </div>
      <div class="mrp-workbench-user">
        <div class="mrp-workbench-user__info">
          <div class="mrp-workbench-user__name">李计划官</div>
          <div class="mrp-workbench-user__role">高级协同调度</div>
        </div>
        <div class="mrp-workbench-user__avatar">李</div>
      </div>
    </section>

    <section class="mrp-workbench-stats">
      <article class="mrp-workbench-stat-card">
        <div class="mrp-workbench-stat-card__body">
          <div class="mrp-workbench-stat-card__label">{{ isProdActive ? '待处理排产建议' : '待批复采购申请' }}</div>
          <div class="mrp-workbench-stat-card__value">{{ isProdActive ? stats.production.pending : stats.purchase.pending }}</div>
          <div class="mrp-workbench-stat-card__badge">{{ isProdActive ? '待处理' : '待处理' }}</div>
        </div>
        <div class="mrp-workbench-stat-card__icon is-warn">
          <Icon icon="ep:clock" class="h-6 w-6" />
        </div>
      </article>
      <article class="mrp-workbench-stat-card">
        <div class="mrp-workbench-stat-card__body">
          <div class="mrp-workbench-stat-card__label">{{ isProdActive ? '加急排产件' : '急需到货采购项' }}</div>
          <div class="mrp-workbench-stat-card__value is-danger">{{ isProdActive ? stats.production.urgent : stats.purchase.urgent }}</div>
          <div class="mrp-workbench-stat-card__badge is-danger">高优先级</div>
        </div>
        <div class="mrp-workbench-stat-card__icon is-danger">
          <Icon icon="ep:warning" class="h-6 w-6" />
        </div>
      </article>
      <article class="mrp-workbench-stat-card">
        <div class="mrp-workbench-stat-card__body">
          <div class="mrp-workbench-stat-card__label">{{ isProdActive ? '本日已审核建议' : '本日完成审批采购' }}</div>
          <div class="mrp-workbench-stat-card__value is-success">{{ isProdActive ? stats.production.approved : stats.purchase.approved }}</div>
          <div class="mrp-workbench-stat-card__badge is-success">就绪</div>
        </div>
        <div class="mrp-workbench-stat-card__icon is-success">
          <Icon icon="ep:select" class="h-6 w-6" />
        </div>
      </article>
      <article
        class="mrp-workbench-stat-card mrp-workbench-stat-card--accent"
        :class="isProdActive ? 'is-production' : 'is-purchase'"
      >
        <div class="mrp-workbench-stat-card__body">
          <div class="mrp-workbench-stat-card__label">{{ isProdActive ? 'MRP 本期排产总量' : 'MRP 本期采购建议总量' }}</div>
          <div class="mrp-workbench-stat-card__value is-accent">
            {{ isProdActive ? stats.production.totalQty : stats.purchase.totalQty }}
          </div>
          <div class="mrp-workbench-stat-card__badge is-accent">{{ isProdActive ? '本期排产' : '本期采购' }}</div>
        </div>
        <div class="mrp-workbench-stat-card__icon is-accent">
          <Icon :icon="isProdActive ? 'ep:box' : 'ep:truck'" class="h-6 w-6" />
        </div>
      </article>
    </section>

    <section class="mrp-workbench-tabs">
      <button
        type="button"
        class="mrp-workbench-tabs__item"
        :class="{ 'is-active': !isProdActive }"
        @click="activeTab = 'purchase'; handleTabChange()"
      >
        <Icon icon="ep:shopping-cart" class="h-4 w-4" />
        采购物料建议看板（外部原材料直供）
      </button>
      <button
        type="button"
        class="mrp-workbench-tabs__item"
        :class="{ 'is-active': isProdActive }"
        @click="activeTab = 'production'; handleTabChange()"
      >
        <Icon icon="ep:operation" class="h-4 w-4" />
        生产计划建议看板（BOM成品及半成品）
      </button>
    </section>

  <template v-if="activeTab === 'purchase'">
    <ContentWrap v-if="purchaseTraceActive">
      <div class="mrp-trace-banner">
        <div class="mrp-trace-banner__main">
          <div class="mrp-trace-banner__eyebrow">销售追溯</div>
          <div class="mrp-trace-banner__title">
            当前按销售订单
            <el-tag class="mx-8px" type="success" effect="light">{{ purchaseTraceLabel }}</el-tag>
            查看采购建议
          </div>
        </div>
        <div class="mrp-trace-banner__actions">
          <el-button link type="primary" @click="openPurchaseOrderTraceBySourceOrder">
            查看采购订单
          </el-button>
          <el-button link @click="clearPurchaseTrace">清除筛选</el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-form
        ref="purchaseQueryFormRef"
        :model="purchaseQueryParams"
        label-position="top"
        class="mrp-query-form mrp-query-form--purchase"
      >
        <el-form-item label="计划编号" prop="planId">
          <el-input-number
            v-model="purchaseQueryParams.planId"
            :min="1"
            :precision="0"
            :controls="false"
            placeholder="请输入计划编号"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="物料" prop="materialId">
          <el-select
            v-model="purchaseQueryParams.materialId"
            clearable
            filterable
            placeholder="请选择物料"
            class="!w-full"
          >
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select
            v-model="purchaseQueryParams.status"
            clearable
            placeholder="请选择状态"
            class="!w-full"
          >
            <el-option
              v-for="item in suggestStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源销售订单" prop="sourceOrderId">
          <el-input-number
            v-model="purchaseQueryParams.sourceOrderId"
            :min="1"
            :precision="0"
            :controls="false"
            placeholder="请输入销售单 ID"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item class="mrp-query-form__actions">
          <el-button @click="resetPurchaseQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" @click="handlePurchaseQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="mrp-table-card">
      <div v-if="!isSaleTraceMode" class="mrp-toolbar">
        <el-button
          type="primary"
          @click="handlePurchaseConfirm()"
          v-hasPermi="['erp:mrp-suggest:approve']"
          :disabled="!hasApprovablePurchaseSelection"
        >
          <Icon icon="ep:select" class="mr-5px" />
          批量审核通过
        </el-button>
        <el-button
          @click="openPurchaseConvert()"
          v-hasPermi="['erp:mrp-suggest:convert-purchase']"
          :disabled="!hasConvertiblePurchaseSelection"
        >
          <Icon icon="ep:shopping-cart" class="mr-5px" />
          批量转采购订单
        </el-button>
        <el-button
          @click="handlePurchaseReject()"
          v-hasPermi="['erp:mrp-suggest:reject']"
          :disabled="!hasRejectablePurchaseSelection"
        >
          <Icon icon="ep:circle-close" class="mr-5px" />
          驳回
        </el-button>
      </div>

      <el-table
        v-loading="purchaseLoading"
        :data="purchaseList"
        :show-overflow-tooltip="false"
        class="mrp-grid-table"
        @selection-change="handlePurchaseSelectionChange"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column label="建议详情（源计划/编号）" min-width="220">
          <template #default="scope">
            <div class="mrp-cell-block">
              <div class="mrp-cell-block__title">{{ formatSuggestCode(scope.row.id) }}</div>
              <div class="mrp-cell-block__meta">源计划：{{ formatPlanCode(scope.row.planId) }}</div>
              <div class="mrp-cell-block__meta">到货日：{{ formatDisplayDate(scope.row.suggestArrivalDate) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="物料与计算依据" min-width="250">
          <template #default="scope">
            <div class="mrp-item-cell">
              <div class="mrp-item-cell__name">{{ scope.row.materialName || '-' }}</div>
              <div class="mrp-item-cell__trace">
                <span class="mrp-item-cell__trace-label">项目编号</span>
                <span class="mrp-item-cell__trace-value">{{ scope.row.projectId || '-' }}</span>
              </div>
              <el-button link type="primary" class="mrp-item-cell__link" @click="openPurchaseBasis(scope.row)">
                <Icon icon="ep:document" class="mr-4px" />
                查看运算依据
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="净需求 / 建议量" min-width="170" align="right">
          <template #default="scope">
            <div class="mrp-qty-stack">
              <div class="mrp-qty-stack__item">
                <span class="mrp-qty-stack__label">建议量</span>
                <span class="mrp-qty-stack__value">{{ formatMrpQty(scope.row.suggestQty) }}</span>
              </div>
              <div class="mrp-qty-stack__item">
                <span class="mrp-qty-stack__label">净需求</span>
                <span class="mrp-qty-stack__value mrp-qty-stack__value--accent">
                  {{ formatMrpQty(scope.row.netDemandQty) }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="120">
          <template #default="scope">
            <span class="mrp-status-chip" :class="getSuggestStatusClass(scope.row.status)">
              {{ getSuggestStatusLabel(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="业务追溯（销售/采购）" min-width="220">
          <template #default="scope">
            <div class="mrp-trace-cell">
              <div class="mrp-trace-cell__item">
                <span class="mrp-trace-cell__label">来源销售</span>
                <span class="mrp-trace-cell__value">{{ formatOrderCode(scope.row.sourceOrderId) }}</span>
              </div>
              <div class="mrp-trace-cell__item">
                <span class="mrp-trace-cell__label">采购订单</span>
                <span
                  class="mrp-trace-cell__value"
                  :class="{ 'mrp-trace-cell__value--success': scope.row.convertPurchaseOrderId }"
                >
                  {{ formatOrderCode(scope.row.convertPurchaseOrderId) }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          fixed="right"
          :show-overflow-tooltip="false"
          :width="isSaleTraceMode ? 120 : 190"
        >
          <template #default="scope">
            <div v-if="isSaleTraceMode" class="mrp-row-actions mrp-row-actions--trace">
              <el-button
                link
                type="primary"
                @click="openPurchaseOrderTraceBySourceOrder"
                :disabled="!purchaseTraceOrderId"
              >
                查看采购订单
              </el-button>
            </div>
            <div v-else class="mrp-row-actions">
              <el-button
                v-if="hasSuggestApprovePermission && canApproveSuggest(scope.row.status)"
                link
                type="primary"
                @click="handlePurchaseConfirm([scope.row.id])"
              >
                审核通过
              </el-button>
              <el-button
                v-else-if="hasPurchaseConvertPermission && canConvertSuggest(scope.row.status)"
                link
                type="success"
                @click="openPurchaseConvert([scope.row.id])"
              >
                转采购单
              </el-button>
              <el-button
                v-if="hasSuggestRejectPermission && canRejectSuggest(scope.row.status)"
                link
                type="danger"
                @click="handlePurchaseReject([scope.row.id])"
              >
                驳回
              </el-button>
              <span
                v-if="
                  !(
                    (hasSuggestApprovePermission && canApproveSuggest(scope.row.status)) ||
                    (hasPurchaseConvertPermission && canConvertSuggest(scope.row.status)) ||
                    (hasSuggestRejectPermission && canRejectSuggest(scope.row.status))
                  )
                "
                class="mrp-row-actions__placeholder"
              >
                -
              </span>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="mrp-empty-state">
            <Icon icon="ep:document-remove" class="mrp-empty-state__icon" />
            <div class="mrp-empty-state__title">暂无建议数据</div>
          </div>
        </template>
      </el-table>

      <Pagination
        :total="purchaseTotal"
        v-model:page="purchaseQueryParams.pageNo"
        v-model:limit="purchaseQueryParams.pageSize"
        @pagination="getPurchaseList"
      />
      </div>
    </ContentWrap>
  </template>

  <template v-else>
    <ContentWrap>
      <el-form
        ref="productionQueryFormRef"
        :model="productionQueryParams"
        label-position="top"
        class="mrp-query-form mrp-query-form--production"
      >
        <el-form-item label="计划编号" prop="planId">
          <el-input-number
            v-model="productionQueryParams.planId"
            :min="1"
            :precision="0"
            :controls="false"
            placeholder="请输入计划编号"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select
            v-model="productionQueryParams.productId"
            clearable
            filterable
            placeholder="请选择产品"
            class="!w-full"
          >
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select
            v-model="productionQueryParams.status"
            clearable
            placeholder="请选择状态"
            class="!w-full"
          >
            <el-option
              v-for="item in suggestStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="mrp-query-form__actions">
          <el-button @click="resetProductionQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" @click="handleProductionQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="mrp-table-card">
      <div v-if="!isSaleTraceMode" class="mrp-toolbar">
        <el-button
          type="primary"
          @click="handleProductionConfirm()"
          v-hasPermi="['erp:mrp-suggest:approve']"
          :disabled="!hasApprovableProductionSelection"
        >
          <Icon icon="ep:select" class="mr-5px" />
          批量审核通过
        </el-button>
        <el-button
          @click="openProductionConvert()"
          v-hasPermi="['erp:mrp-suggest:convert-production']"
          :disabled="!hasConvertibleProductionSelection"
        >
          <Icon icon="ep:operation" class="mr-5px" />
          批量转生产工单
        </el-button>
        <el-button
          @click="handleProductionReject()"
          v-hasPermi="['erp:mrp-suggest:reject']"
          :disabled="!hasRejectableProductionSelection"
        >
          <Icon icon="ep:circle-close" class="mr-5px" />
          驳回
        </el-button>
      </div>

      <el-table
        v-loading="productionLoading"
        :data="productionList"
        :show-overflow-tooltip="false"
        class="mrp-grid-table"
        @selection-change="handleProductionSelectionChange"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column label="建议详情（源计划/编号）" min-width="230">
          <template #default="scope">
            <div class="mrp-cell-block">
              <div class="mrp-cell-block__title">{{ formatSuggestCode(scope.row.id) }}</div>
              <div class="mrp-cell-block__meta">源计划：{{ formatPlanCode(scope.row.planId) }}</div>
              <div class="mrp-cell-block__meta">
                开工/完工：{{ formatDisplayDate(scope.row.suggestStartDate) }} / {{ formatDisplayDate(scope.row.suggestEndDate) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="产品与计算依据" min-width="250">
          <template #default="scope">
            <div class="mrp-item-cell">
              <div class="mrp-item-cell__name">{{ scope.row.productName || '-' }}</div>
              <div class="mrp-item-cell__trace">
                <span class="mrp-item-cell__trace-label">项目编号</span>
                <span class="mrp-item-cell__trace-value">{{ scope.row.projectId || '-' }}</span>
              </div>
              <el-button link type="primary" class="mrp-item-cell__link" @click="openProductionBasis(scope.row)">
                <Icon icon="ep:document" class="mr-4px" />
                查看运算依据
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="净需求 / 建议量" min-width="170" align="right">
          <template #default="scope">
            <div class="mrp-qty-stack">
              <div class="mrp-qty-stack__item">
                <span class="mrp-qty-stack__label">建议量</span>
                <span class="mrp-qty-stack__value">{{ formatMrpQty(scope.row.suggestQty) }}</span>
              </div>
              <div class="mrp-qty-stack__item">
                <span class="mrp-qty-stack__label">净需求</span>
                <span class="mrp-qty-stack__value mrp-qty-stack__value--accent">
                  {{ formatMrpQty(scope.row.netDemandQty) }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="120">
          <template #default="scope">
            <span class="mrp-status-chip" :class="getSuggestStatusClass(scope.row.status)">
              {{ getSuggestStatusLabel(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="业务追溯（销售/工单）" min-width="220">
          <template #default="scope">
            <div class="mrp-trace-cell">
              <div class="mrp-trace-cell__item">
                <span class="mrp-trace-cell__label">来源销售</span>
                <span class="mrp-trace-cell__value">{{ formatOrderCode(scope.row.sourceOrderId) }}</span>
              </div>
              <div class="mrp-trace-cell__item">
                <span class="mrp-trace-cell__label">生产工单</span>
                <span
                  class="mrp-trace-cell__value"
                  :class="{ 'mrp-trace-cell__value--success': scope.row.convertProductionOrderId }"
                >
                  {{ formatOrderCode(scope.row.convertProductionOrderId) }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          fixed="right"
          :show-overflow-tooltip="false"
          :width="190"
        >
          <template #default="scope">
            <div class="mrp-row-actions">
              <el-button
                v-if="hasSuggestApprovePermission && canApproveSuggest(scope.row.status)"
                link
                type="primary"
                @click="handleProductionConfirm([scope.row.id])"
              >
                审核通过
              </el-button>
              <el-button
                v-else-if="hasProductionConvertPermission && canConvertSuggest(scope.row.status)"
                link
                type="success"
                @click="openProductionConvert([scope.row.id])"
              >
                转工单
              </el-button>
              <el-button
                v-if="hasSuggestRejectPermission && canRejectSuggest(scope.row.status)"
                link
                type="danger"
                @click="handleProductionReject([scope.row.id])"
              >
                驳回
              </el-button>
              <span
                v-if="
                  !(
                    (hasSuggestApprovePermission && canApproveSuggest(scope.row.status)) ||
                    (hasProductionConvertPermission && canConvertSuggest(scope.row.status)) ||
                    (hasSuggestRejectPermission && canRejectSuggest(scope.row.status))
                  )
                "
                class="mrp-row-actions__placeholder"
              >
                -
              </span>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="mrp-empty-state">
            <Icon icon="ep:document-remove" class="mrp-empty-state__icon" />
            <div class="mrp-empty-state__title">暂无建议数据</div>
          </div>
        </template>
      </el-table>

      <Pagination
        :total="productionTotal"
        v-model:page="productionQueryParams.pageNo"
        v-model:limit="productionQueryParams.pageSize"
        @pagination="getProductionList"
      />
      </div>
    </ContentWrap>
  </template>

  <Dialog title="转采购订单" v-model="purchaseConvertDialogVisible" width="520px">
    <el-form
      ref="purchaseConvertFormRef"
      :model="purchaseConvertForm"
      :rules="purchaseConvertRules"
      label-width="100px"
      v-loading="purchaseConvertLoading"
    >
      <el-form-item label="建议数量">
        <el-input :model-value="`${purchaseConvertForm.ids.length} 条`" disabled />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select
          v-model="purchaseConvertForm.supplierId"
          clearable
          filterable
          placeholder="请选择供应商"
          class="!w-full"
        >
          <el-option
            v-for="item in supplierList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="结算账户" prop="accountId">
        <el-select
          v-model="purchaseConvertForm.accountId"
          clearable
          filterable
          placeholder="请选择结算账户"
          class="!w-full"
        >
          <el-option
            v-for="item in accountList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="purchaseConvertForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitPurchaseConvert" :disabled="purchaseConvertLoading">
        确定
      </el-button>
      <el-button @click="purchaseConvertDialogVisible = false">取消</el-button>
    </template>
  </Dialog>

  <Dialog title="转生产工单" v-model="productionConvertDialogVisible" width="520px">
    <el-form
      ref="productionConvertFormRef"
      :model="productionConvertForm"
      label-width="100px"
      v-loading="productionConvertLoading"
    >
      <el-form-item label="建议数量">
        <el-input :model-value="`${productionConvertForm.ids.length} 条`" disabled />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="productionConvertForm.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button
        type="primary"
        @click="submitProductionConvert"
        :disabled="productionConvertLoading"
      >
        确定
      </el-button>
      <el-button @click="productionConvertDialogVisible = false">取消</el-button>
    </template>
  </Dialog>

  <Dialog :title="basisDialogTitle" v-model="basisDialogVisible" width="920px">
    <div v-if="basisDialogData" class="basis-dialog">
      <div class="basis-trace">
        <div class="basis-trace__title">运算摘要</div>
        <div class="basis-trace__grid">
          <div class="basis-trace__item">
            <span class="basis-trace__label">计划编号</span>
            <span class="basis-trace__value">{{ basisDialogData.planId }}</span>
          </div>
          <div class="basis-trace__item">
            <span class="basis-trace__label">项目编号</span>
            <span class="basis-trace__value">{{ basisDialogData.projectId || '-' }}</span>
          </div>
          <div class="basis-trace__item">
            <span class="basis-trace__label">建议数量</span>
            <span class="basis-trace__value">{{ formatMrpQty(basisDialogData.suggestQty) }}</span>
          </div>
          <div class="basis-trace__item">
            <span class="basis-trace__label">净需求</span>
            <span class="basis-trace__value">{{ formatMrpQty(basisDialogData.netDemandQty) }}</span>
          </div>
          <div class="basis-trace__item basis-trace__item--wide">
            <span class="basis-trace__label">毛需求 / 安全库存 / 可用现货 / 在途 / 在制 / 冻结现货</span>
            <span class="basis-trace__value basis-trace__value--mono">
              {{ formatMrpQty(basisDialogData.grossDemandQty) }} / {{ formatMrpQty(basisDialogData.safetyStockQty) }} / {{ formatMrpQty(basisDialogData.availableStockQty) }} / {{ formatMrpQty(basisDialogData.incomingQty) }} / {{ formatMrpQty(basisDialogData.wipQty) }} / {{ formatMrpQty(basisDialogData.reservedStockQty) }}
            </span>
          </div>
        </div>
      </div>
      <div class="basis-hero">
        <div class="basis-hero__info">
          <div class="basis-hero__eyebrow">MRP 结论</div>
          <div class="basis-hero__title">
            {{ getBasisActionLabel(basisDialogData) }}
            <span class="basis-hero__name">{{ basisDialogData.name || '-' }}</span>
          </div>
          <div class="basis-hero__meta">
            <span>建议编号：{{ basisDialogData.id }}</span>
            <span>计划编号：{{ basisDialogData.planId }}</span>
            <span>项目编号：{{ basisDialogData.projectId || '-' }}</span>
          </div>
        </div>
        <div class="basis-hero__result">
          <div class="basis-hero__result-label">建议数量</div>
          <div class="basis-hero__result-value">{{ formatMrpQty(basisDialogData.suggestQty) }}</div>
        </div>
      </div>

      <div class="basis-summary">
        <div class="basis-summary__main">
          <div class="basis-summary__label">本次净需求</div>
          <div class="basis-summary__value">{{ formatMrpQty(basisDialogData.netDemandQty) }}</div>
        </div>
        <el-tag :type="getBasisStatusTagType(basisDialogData)">
          {{ getBasisStatusLabel(basisDialogData) }}
        </el-tag>
      </div>

      <div class="basis-explain">
        {{ getBasisExplainText(basisDialogData) }}
      </div>

      <div class="basis-cards">
        <div
          v-for="item in getBasisMetricCards(basisDialogData)"
          :key="item.label"
          class="basis-card"
        >
          <div class="basis-card__label">{{ item.label }}</div>
          <div class="basis-card__value">{{ item.value }}</div>
        </div>
      </div>

      <div class="basis-reservation">
        <div class="basis-reservation__header">
          <div class="basis-reservation__title">库存占用明细</div>
          <el-button
            link
            type="primary"
            :disabled="basisReservationLoading || !basisReservationQuery.productId"
            @click="refreshBasisReservation"
          >
            <Icon icon="ep:refresh" class="mr-4px" />
            刷新
          </el-button>
        </div>

        <el-alert
          v-if="basisReservationErrorMessage"
          :title="basisReservationErrorMessage"
          type="error"
          :closable="false"
          show-icon
          class="basis-reservation__alert"
        />

        <el-empty
          v-else-if="!basisReservationLoading && !basisReservationList.length"
          description="暂无库存占用明细"
        />

        <el-table
          v-else
          v-loading="basisReservationLoading"
          :data="basisReservationList"
          stripe
          show-overflow-tooltip
          class="basis-reservation__table"
          max-height="320px"
        >
          <el-table-column label="占用编号" prop="id" width="110" align="center" />
          <el-table-column label="计划编号" prop="planNo" min-width="140" />
          <el-table-column label="项目" prop="projectName" min-width="160" />
          <el-table-column label="物料" prop="productName" min-width="160" />
          <el-table-column label="来源销售单" prop="sourceOrderNo" min-width="140" align="center" />
          <el-table-column label="占用数量" prop="reservedQty" width="120" align="right">
            <template #default="{ row }">
              {{ formatMrpQty(row.reservedQty) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getReservationStatusTagType(row.status)">
                {{ getReservationStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" min-width="170" align="center">
            <template #default="{ row }">
              {{ formatBasisReservationTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          v-if="basisReservationTotal > 0"
          v-model:page="basisReservationQuery.pageNo"
          v-model:limit="basisReservationQuery.pageSize"
          :total="basisReservationTotal"
          @pagination="getBasisReservationList"
        />
      </div>
    </div>
  </Dialog>

  </div>
</template>

<script setup lang="ts">
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import {
  MrpSuggestApi,
  PurchaseSuggestVO,
  PurchaseSuggestPageReqVO,
  ProductionSuggestVO,
  PurchaseSuggestConvertReqVO,
  ProductionSuggestConvertReqVO
} from '@/api/erp/mrp/suggest'
import {
  StockReservationApi,
  type StockReservationPageReqVO,
  type StockReservationVO
} from '@/api/erp/mrp/stock-reservation'
import { useRoute, useRouter } from 'vue-router'
import { checkPermi } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'ErpMrpSuggest' })

const message = useMessage()
const route = useRoute()
const router = useRouter()
const isSaleTraceMode = computed(
  () => route.name === 'ErpSaleOrderMrpTracePage' || route.query.traceFrom === 'sale-order'
)
const isPurchaseDemandMode = computed(() => route.path.endsWith('/purchase-request'))

const activeTab = ref<'purchase' | 'production'>('purchase')
const isProdActive = computed(() => activeTab.value === 'production')

type SuggestTagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'
const suggestStatusOptions = [
  { label: '待确认', value: 0, type: 'warning' as SuggestTagType },
  { label: '已确认', value: 10, type: 'primary' as SuggestTagType },
  { label: '已转单', value: 20, type: 'success' as SuggestTagType },
  { label: '已驳回', value: 30, type: 'danger' as SuggestTagType }
]

const getSuggestStatusLabel = (status: number) => {
  return suggestStatusOptions.find((item) => item.value === status)?.label || '未知'
}

const canApproveSuggest = (status: number) => status === 0

const canRejectSuggest = (status: number) => status === 0 || status === 10

const canConvertSuggest = (status: number) => status === 10

const formatMrpQty = (value?: number) => {
  return Number(value ?? 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}

const formatSuggestCode = (id?: number) => {
  return id ? `建议 #${id}` : '-'
}

const formatPlanCode = (planId?: number) => {
  return planId ? `${planId}` : '-'
}

const formatOrderCode = (orderId?: number) => {
  return orderId ? `#${orderId}` : '-'
}

const formatDisplayDate = (value?: string) => {
  return value || '-'
}

const getSuggestStatusClass = (status: number) => {
  if (status === 10) {
    return 'is-confirmed'
  }
  if (status === 20) {
    return 'is-converted'
  }
  if (status === 30) {
    return 'is-rejected'
  }
  return 'is-pending'
}

const hasSuggestApprovePermission = checkPermi(['erp:mrp-suggest:approve'])
const hasSuggestRejectPermission = checkPermi(['erp:mrp-suggest:reject'])
const hasPurchaseConvertPermission = checkPermi(['erp:mrp-suggest:convert-purchase'])
const hasProductionConvertPermission = checkPermi(['erp:mrp-suggest:convert-production'])

const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const accountList = ref<AccountVO[]>([])

const purchaseLoading = ref(false)
const purchaseList = ref<PurchaseSuggestVO[]>([])
const purchaseTotal = ref(0)
const purchaseSelectionList = ref<PurchaseSuggestVO[]>([])
const purchaseQueryParams = reactive<PurchaseSuggestPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  planId: undefined as number | undefined,
  materialId: undefined as number | undefined,
  sourceOrderId: undefined as number | undefined,
  status: undefined as number | undefined
})
const purchaseQueryFormRef = ref()
const purchaseTraceOrderId = ref<number | undefined>()
const purchaseTraceOrderNo = ref('')
const routePlanId = ref<number | undefined>()
const defaultPurchaseStatus = computed<number | undefined>(() =>
  isPurchaseDemandMode.value ? 10 : undefined
)
const purchaseTraceActive = computed(
  () => activeTab.value === 'purchase' && !!purchaseQueryParams.sourceOrderId
)
const purchaseTraceLabel = computed(() => {
  if (!purchaseTraceOrderId.value) {
    return ''
  }
  return purchaseTraceOrderNo.value
    ? `${purchaseTraceOrderNo.value} (ID: ${purchaseTraceOrderId.value})`
    : `ID: ${purchaseTraceOrderId.value}`
})

const productionLoading = ref(false)
const productionList = ref<ProductionSuggestVO[]>([])
const productionTotal = ref(0)
const productionSelectionList = ref<ProductionSuggestVO[]>([])
const productionQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  planId: undefined as number | undefined,
  productId: undefined as number | undefined,
  status: undefined as number | undefined
})
const productionQueryFormRef = ref()

const buildListStats = <T extends { status: number; suggestQty?: number; netDemandQty?: number }>(list: T[]) => {
  const sumQty = list.reduce((total, item) => total + Number(item.suggestQty ?? 0), 0)
  return {
    pending: list.filter((item) => item.status === 0).length,
    urgent: list.filter((item) => item.status === 0 && Number(item.netDemandQty ?? 0) > 0).length,
    approved: list.filter((item) => item.status === 10).length,
    totalQty: sumQty
  }
}

const stats = computed(() => {
  return {
    purchase: buildListStats(purchaseList.value),
    production: buildListStats(productionList.value)
  }
})

const purchaseConvertDialogVisible = ref(false)
const purchaseConvertLoading = ref(false)
const purchaseConvertFormRef = ref()
const purchaseConvertForm = reactive<PurchaseSuggestConvertReqVO>({
  ids: [],
  supplierId: undefined,
  accountId: undefined,
  remark: undefined
})
const purchaseConvertRules = reactive({
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
})

const productionConvertDialogVisible = ref(false)
const productionConvertLoading = ref(false)
const productionConvertFormRef = ref()
const productionConvertForm = reactive<ProductionSuggestConvertReqVO>({
  ids: [],
  remark: undefined
})

const basisDialogVisible = ref(false)
const basisDialogTitle = ref('计算依据')
const basisDialogData = ref<
  | {
      id: number
      planId: number
      projectId?: number
      name?: string
      suggestQty?: number
      grossDemandQty?: number
      availableStockQty?: number
      incomingQty?: number
      wipQty?: number
      reservedStockQty?: number
      safetyStockQty?: number
      netDemandQty?: number
      mode?: 'purchase' | 'production'
    }
  | undefined
>()
const basisReservationLoading = ref(false)
const basisReservationErrorMessage = ref('')
const basisReservationList = ref<StockReservationVO[]>([])
const basisReservationTotal = ref(0)
const basisReservationQuery = reactive<StockReservationPageReqVO>({
  pageNo: 1,
  pageSize: 5,
  planId: undefined,
  projectId: undefined,
  productId: undefined,
  sourceOrderId: undefined,
  status: 0
})
let basisReservationRequestId = 0

const getBasisActionLabel = (data: NonNullable<typeof basisDialogData.value>) => {
  return data.mode === 'production' ? '建议生产' : '建议采购'
}

const getBasisStatusLabel = (data: NonNullable<typeof basisDialogData.value>) => {
  return Number(data.netDemandQty ?? 0) > 0 ? '需要处理' : '已被覆盖'
}

const getBasisStatusTagType = (data: NonNullable<typeof basisDialogData.value>) => {
  return Number(data.netDemandQty ?? 0) > 0 ? 'danger' : 'success'
}

const getBasisExplainText = (data: NonNullable<typeof basisDialogData.value>) => {
  return `系统以毛需求 ${formatMrpQty(data.grossDemandQty)} 和安全库存 ${formatMrpQty(data.safetyStockQty)} 为起点，结合现货 ${formatMrpQty(data.availableStockQty)}、在途 ${formatMrpQty(data.incomingQty)}、在制 ${formatMrpQty(data.wipQty)} 后，形成当前净需求 ${formatMrpQty(data.netDemandQty)}。`
}

const getBasisMetricCards = (data: NonNullable<typeof basisDialogData.value>) => {
  return [
    { label: '毛需求', value: formatMrpQty(data.grossDemandQty) },
    { label: '安全库存', value: formatMrpQty(data.safetyStockQty) },
    { label: '全局可用现货', value: formatMrpQty(data.availableStockQty) },
    { label: '项目在途', value: formatMrpQty(data.incomingQty) },
    { label: '项目在制', value: formatMrpQty(data.wipQty) },
    { label: '本次冻结现货', value: formatMrpQty(data.reservedStockQty) }
  ]
}

const getReservationStatusLabel = (status?: number) => {
  return status === 1 ? '已释放' : '占用中'
}

const getReservationStatusTagType = (status?: number) => {
  return status === 1 ? 'success' : 'warning'
}

const formatBasisReservationTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return formatDate(value) || String(value)
}

const resetBasisReservationState = () => {
  basisReservationRequestId += 1
  basisReservationErrorMessage.value = ''
  basisReservationList.value = []
  basisReservationTotal.value = 0
  basisReservationLoading.value = false
  basisReservationQuery.pageNo = 1
  basisReservationQuery.pageSize = 5
  basisReservationQuery.planId = undefined
  basisReservationQuery.projectId = undefined
  basisReservationQuery.productId = undefined
  basisReservationQuery.sourceOrderId = undefined
  basisReservationQuery.status = 0
}

const getBasisReservationList = async () => {
  if (!basisDialogData.value || !basisReservationQuery.planId || !basisReservationQuery.productId) {
    basisReservationList.value = []
    basisReservationTotal.value = 0
    return
  }
  const currentRequestId = ++basisReservationRequestId
  basisReservationLoading.value = true
  basisReservationErrorMessage.value = ''
  try {
    const data = await StockReservationApi.getStockReservationPage(basisReservationQuery)
    if (currentRequestId !== basisReservationRequestId) {
      return
    }
    basisReservationList.value = data.list
    basisReservationTotal.value = data.total
  } catch (error: any) {
    if (currentRequestId !== basisReservationRequestId) {
      return
    }
    basisReservationErrorMessage.value = error?.message || '占用明细加载失败'
  } finally {
    if (currentRequestId === basisReservationRequestId) {
      basisReservationLoading.value = false
    }
  }
}

const refreshBasisReservation = async () => {
  basisReservationQuery.pageNo = 1
  await getBasisReservationList()
}

const openBasisReservation = async (payload: {
  planId: number
  projectId?: number
  productId?: number
  sourceOrderId?: number
}) => {
  resetBasisReservationState()
  basisReservationQuery.planId = payload.planId
  basisReservationQuery.projectId = payload.projectId
  basisReservationQuery.productId = payload.productId
  basisReservationQuery.sourceOrderId = payload.sourceOrderId
  if (!basisReservationQuery.productId) {
    basisReservationErrorMessage.value = '当前建议缺少物料/产品信息，无法加载库存占用明细'
    return
  }
  await getBasisReservationList()
}

const getPurchaseList = async () => {
  purchaseLoading.value = true
  try {
    const data = await MrpSuggestApi.getPurchaseSuggestPage(purchaseQueryParams)
    purchaseList.value = data.list
    purchaseTotal.value = data.total
  } finally {
    purchaseLoading.value = false
  }
}

const getProductionList = async () => {
  productionLoading.value = true
  try {
    const data = await MrpSuggestApi.getProductionSuggestPage(productionQueryParams)
    productionList.value = data.list
    productionTotal.value = data.total
  } finally {
    productionLoading.value = false
  }
}

const handlePurchaseQuery = async () => {
  purchaseQueryParams.pageNo = 1
  await getPurchaseList()
}

const handleProductionQuery = async () => {
  productionQueryParams.pageNo = 1
  await getProductionList()
}

const resetPurchaseQuery = async () => {
  purchaseQueryFormRef.value?.resetFields()
  if (routePlanId.value) {
    purchaseQueryParams.planId = routePlanId.value
  }
  if (purchaseTraceOrderId.value) {
    purchaseQueryParams.sourceOrderId = purchaseTraceOrderId.value
  }
  purchaseQueryParams.status = defaultPurchaseStatus.value
  await handlePurchaseQuery()
}

const resetProductionQuery = async () => {
  productionQueryFormRef.value?.resetFields()
  if (routePlanId.value) {
    productionQueryParams.planId = routePlanId.value
  }
  await handleProductionQuery()
}

const handleTabChange = async () => {
  if (isPurchaseDemandMode.value) {
    activeTab.value = 'purchase'
    await getPurchaseList()
    return
  }
  if (activeTab.value === 'purchase') {
    await getPurchaseList()
    return
  }
  await getProductionList()
}

const handlePurchaseSelectionChange = (rows: PurchaseSuggestVO[]) => {
  purchaseSelectionList.value = rows
}

const handleProductionSelectionChange = (rows: ProductionSuggestVO[]) => {
  productionSelectionList.value = rows
}

const getSelectableSuggestIds = <T extends { id: number; status: number }>(
  sourceList: T[],
  selectionList: T[],
  ids: number[] | undefined,
  predicate: (status: number) => boolean,
  emptyMessage: string,
  skippedMessage: string
) => {
  const rawIds = ids || selectionList.map((item) => item.id)
  const targetRows = ids ? sourceList.filter((item) => rawIds.includes(item.id)) : selectionList
  const targetIds = targetRows.filter((item) => predicate(item.status)).map((item) => item.id)
  if (!targetIds.length) {
    message.warning(emptyMessage)
    return []
  }
  if (targetIds.length !== rawIds.length) {
    message.warning(skippedMessage)
  }
  return targetIds
}

const getApprovablePurchaseIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    purchaseList.value,
    purchaseSelectionList.value,
    ids,
    canApproveSuggest,
    '没有可审核通过的采购建议',
    '已自动跳过非待确认状态的采购建议'
  )
}

const getRejectablePurchaseIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    purchaseList.value,
    purchaseSelectionList.value,
    ids,
    canRejectSuggest,
    '没有可驳回的采购建议',
    '已自动跳过已转单或已驳回的采购建议'
  )
}

const getConvertiblePurchaseIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    purchaseList.value,
    purchaseSelectionList.value,
    ids,
    canConvertSuggest,
    '没有可转采购订单的采购建议',
    '已自动跳过未确认、已转单或已驳回的采购建议'
  )
}

const getApprovableProductionIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    productionList.value,
    productionSelectionList.value,
    ids,
    canApproveSuggest,
    '没有可审核通过的生产建议',
    '已自动跳过非待确认状态的生产建议'
  )
}

const getRejectableProductionIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    productionList.value,
    productionSelectionList.value,
    ids,
    canRejectSuggest,
    '没有可驳回的生产建议',
    '已自动跳过已转单或已驳回的生产建议'
  )
}

const getConvertibleProductionIds = (ids?: number[]) => {
  return getSelectableSuggestIds(
    productionList.value,
    productionSelectionList.value,
    ids,
    canConvertSuggest,
    '没有可转生产工单的生产建议',
    '已自动跳过未确认、已转单或已驳回的生产建议'
  )
}

const hasApprovablePurchaseSelection = computed(() => {
  return hasSuggestApprovePermission && purchaseSelectionList.value.some((item) => canApproveSuggest(item.status))
})

const hasRejectablePurchaseSelection = computed(() => {
  return hasSuggestRejectPermission && purchaseSelectionList.value.some((item) => canRejectSuggest(item.status))
})

const hasConvertiblePurchaseSelection = computed(() => {
  return hasPurchaseConvertPermission && purchaseSelectionList.value.some((item) => canConvertSuggest(item.status))
})

const hasApprovableProductionSelection = computed(() => {
  return hasSuggestApprovePermission && productionSelectionList.value.some((item) => canApproveSuggest(item.status))
})

const hasRejectableProductionSelection = computed(() => {
  return hasSuggestRejectPermission && productionSelectionList.value.some((item) => canRejectSuggest(item.status))
})

const hasConvertibleProductionSelection = computed(() => {
  return hasProductionConvertPermission && productionSelectionList.value.some((item) => canConvertSuggest(item.status))
})

const handlePurchaseConfirm = async (ids?: number[]) => {
  const targetIds = getApprovablePurchaseIds(ids)
  if (!targetIds.length) {
    return
  }
  try {
    await message.confirm(`确定审核通过 ${targetIds.length} 条采购建议吗？`)
    await MrpSuggestApi.confirmPurchaseSuggest(targetIds)
    message.success('采购建议审核通过成功')
    purchaseSelectionList.value = []
    await getPurchaseList()
  } catch {}
}

const handlePurchaseReject = async (ids?: number[]) => {
  const targetIds = getRejectablePurchaseIds(ids)
  if (!targetIds.length) {
    return
  }
  try {
    await message.confirm(`确定驳回 ${targetIds.length} 条采购建议吗？`)
    await MrpSuggestApi.rejectPurchaseSuggest(targetIds)
    message.success('采购建议驳回成功')
    purchaseSelectionList.value = []
    await getPurchaseList()
  } catch {}
}

const handleProductionConfirm = async (ids?: number[]) => {
  const targetIds = getApprovableProductionIds(ids)
  if (!targetIds.length) {
    return
  }
  try {
    await message.confirm(`确定审核通过 ${targetIds.length} 条生产建议吗？`)
    await MrpSuggestApi.confirmProductionSuggest(targetIds)
    message.success('生产建议审核通过成功')
    productionSelectionList.value = []
    await getProductionList()
  } catch {}
}

const handleProductionReject = async (ids?: number[]) => {
  const targetIds = getRejectableProductionIds(ids)
  if (!targetIds.length) {
    return
  }
  try {
    await message.confirm(`确定驳回 ${targetIds.length} 条生产建议吗？`)
    await MrpSuggestApi.rejectProductionSuggest(targetIds)
    message.success('生产建议驳回成功')
    productionSelectionList.value = []
    await getProductionList()
  } catch {}
}

const resetPurchaseConvertForm = () => {
  purchaseConvertForm.ids = []
  purchaseConvertForm.supplierId = undefined
  purchaseConvertForm.accountId = undefined
  purchaseConvertForm.remark = undefined
  purchaseConvertFormRef.value?.resetFields()
}

const resetProductionConvertForm = () => {
  productionConvertForm.ids = []
  productionConvertForm.remark = undefined
  productionConvertFormRef.value?.resetFields()
}

const openPurchaseConvert = (ids?: number[]) => {
  const targetIds = getConvertiblePurchaseIds(ids)
  if (!targetIds.length) {
    return
  }
  resetPurchaseConvertForm()
  purchaseConvertForm.ids = targetIds
  purchaseConvertDialogVisible.value = true
}

const openProductionConvert = (ids?: number[]) => {
  const targetIds = getConvertibleProductionIds(ids)
  if (!targetIds.length) {
    return
  }
  resetProductionConvertForm()
  productionConvertForm.ids = targetIds
  productionConvertDialogVisible.value = true
}

const openPurchaseBasis = (row: PurchaseSuggestVO) => {
  basisDialogTitle.value = '采购建议计算依据'
  basisDialogData.value = {
    id: row.id,
    planId: row.planId,
    projectId: row.projectId,
    name: row.materialName,
    suggestQty: row.suggestQty,
    grossDemandQty: row.grossDemandQty,
    availableStockQty: row.availableStockQty,
    incomingQty: row.incomingQty,
    wipQty: row.wipQty,
    reservedStockQty: row.reservedStockQty,
    safetyStockQty: row.safetyStockQty,
    netDemandQty: row.netDemandQty,
    mode: 'purchase'
  }
  basisDialogVisible.value = true
  void openBasisReservation({
    planId: row.planId,
    projectId: row.projectId,
    productId: row.materialId,
    sourceOrderId: row.sourceOrderId
  })
}

const openProductionBasis = (row: ProductionSuggestVO) => {
  basisDialogTitle.value = '生产建议计算依据'
  basisDialogData.value = {
    id: row.id,
    planId: row.planId,
    projectId: row.projectId,
    name: row.productName,
    suggestQty: row.suggestQty,
    grossDemandQty: row.grossDemandQty,
    availableStockQty: row.availableStockQty,
    incomingQty: row.incomingQty,
    wipQty: row.wipQty,
    reservedStockQty: row.reservedStockQty,
    safetyStockQty: row.safetyStockQty,
    netDemandQty: row.netDemandQty,
    mode: 'production'
  }
  basisDialogVisible.value = true
  void openBasisReservation({
    planId: row.planId,
    projectId: row.projectId,
    productId: row.productId,
    sourceOrderId: row.sourceOrderId
  })
}

const openPurchaseOrderDetail = async (purchaseOrderId: number) => {
  const query = {
    openId: String(purchaseOrderId),
    openType: 'detail',
    from: 'mrp-suggest'
  }
  if (router.hasRoute('ErpPurchaseOrder')) {
    await router.push({
      name: 'ErpPurchaseOrder',
      query
    })
    return
  }
  const target = router.resolve({
    path: '/erp/purchase/order',
    query
  })
  const matched = target.matched.filter((item) => item.name !== '404Page')
  if (matched.length > 0) {
    await router.push(target)
    return
  }
  message.warning(
    `采购订单已生成（${purchaseOrderId}），但当前会话未加载采购订单页面，请重新登录后从采购订单菜单查看`
  )
}

const normalizeRouteNumber = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : parsedValue
}

const syncTraceFromRoute = async () => {
  activeTab.value =
    !isPurchaseDemandMode.value && typeof route.query.tab === 'string' && route.query.tab === 'production'
      ? 'production'
      : 'purchase'
  const planId = normalizeRouteNumber(route.query.planId)
  const sourceOrderId = normalizeRouteNumber(route.query.sourceOrderId)
  routePlanId.value = planId
  purchaseTraceOrderId.value = sourceOrderId
  purchaseTraceOrderNo.value =
    typeof route.query.sourceOrderNo === 'string' ? route.query.sourceOrderNo : ''
  purchaseSelectionList.value = []
  productionSelectionList.value = []
  purchaseConvertDialogVisible.value = false
  productionConvertDialogVisible.value = false
  basisDialogVisible.value = false
  resetBasisReservationState()
  purchaseQueryParams.planId = planId
  purchaseQueryParams.sourceOrderId = sourceOrderId
  purchaseQueryParams.status = defaultPurchaseStatus.value
  purchaseQueryParams.pageNo = 1
  productionQueryParams.planId = planId
  productionQueryParams.pageNo = 1
  if (activeTab.value === 'purchase') {
    await getPurchaseList()
    return
  }
  await getProductionList()
}

const clearPurchaseTrace = async () => {
  const nextQuery = { ...route.query }
  delete nextQuery.sourceOrderId
  delete nextQuery.sourceOrderNo
  delete nextQuery.traceFrom
  await router.replace({
    path: route.path,
    query: nextQuery
  })
}

const openPurchaseOrderTraceBySourceOrder = async () => {
  if (!purchaseTraceOrderId.value) {
    return
  }
  const query = {
    sourceOrderId: String(purchaseTraceOrderId.value),
    sourceOrderNo: purchaseTraceOrderNo.value || undefined,
    traceFrom: route.query.traceFrom === 'sale-order' ? 'sale-order' : 'mrp-suggest'
  }
  if (route.query.traceFrom === 'sale-order') {
    await router.push({
      name: 'ErpSaleOrderPurchaseOrderTracePage',
      query
    })
    return
  }
  await router.push({
    path: '/erp/purchase/order',
    query
  })
}

const submitPurchaseConvert = async () => {
  await purchaseConvertFormRef.value?.validate()
  purchaseConvertLoading.value = true
  try {
    const purchaseOrderId = await MrpSuggestApi.convertPurchaseSuggest({
      ids: [...purchaseConvertForm.ids],
      supplierId: purchaseConvertForm.supplierId,
      accountId: purchaseConvertForm.accountId,
      remark: purchaseConvertForm.remark
    })
    purchaseConvertDialogVisible.value = false
    message.success(`已生成采购订单：${purchaseOrderId}`)
    purchaseSelectionList.value = []
    await getPurchaseList()
    await openPurchaseOrderDetail(purchaseOrderId)
  } finally {
    purchaseConvertLoading.value = false
  }
}

const submitProductionConvert = async () => {
  productionConvertLoading.value = true
  try {
    const orderIds = await MrpSuggestApi.convertProductionSuggest({
      ids: [...productionConvertForm.ids],
      remark: productionConvertForm.remark
    })
    productionConvertDialogVisible.value = false
    message.success(`已生成 ${orderIds.length} 张生产工单`)
    productionSelectionList.value = []
    await getProductionList()
  } finally {
    productionConvertLoading.value = false
  }
}

onMounted(async () => {
  const [products, suppliers, accounts] = await Promise.all([
    ProductApi.getProductSimpleList(),
    SupplierApi.getSupplierSimpleList(),
    AccountApi.getAccountSimpleList()
  ])
  productList.value = products
  supplierList.value = suppliers
  accountList.value = accounts
  await syncTraceFromRoute()
})

watch(
  () => [route.path, route.query.planId, route.query.sourceOrderId, route.query.sourceOrderNo, route.query.tab],
  async () => {
    await syncTraceFromRoute()
  }
)
</script>
<style scoped>
.mrp-workbench-page {
  min-height: 100vh;
  padding: 16px 16px 28px;
  background: #f6f8fb;
  color: #0f172a;
}

.mrp-workbench-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.mrp-workbench-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.mrp-workbench-brand__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  color: #ffffff;
  background: linear-gradient(135deg, #0ea5a4 0%, #2563eb 100%);
  box-shadow: 0 10px 24px rgba(14, 165, 164, 0.2);
}

.mrp-workbench-brand__text {
  min-width: 0;
}

.mrp-workbench-brand__title {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.15;
  color: #1f2937;
}

.mrp-workbench-brand__subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #94a3b8;
}

.mrp-workbench-header__center {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.mrp-workbench-header__dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.mrp-workbench-header__dot.is-purchase {
  background: #10b981;
}

.mrp-workbench-header__dot.is-production {
  background: #2563eb;
}

.mrp-workbench-user {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-self: end;
}

.mrp-workbench-user__info {
  text-align: right;
}

.mrp-workbench-user__name {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.mrp-workbench-user__role {
  display: inline-flex;
  align-items: center;
  margin-top: 4px;
  padding: 4px 8px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #059669;
  font-size: 12px;
  font-weight: 800;
}

.mrp-workbench-user__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: linear-gradient(180deg, #e2e8f0 0%, #dbeafe 100%);
  color: #334155;
  font-size: 18px;
  font-weight: 800;
}

.mrp-workbench-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.mrp-workbench-stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 132px;
  padding: 18px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.mrp-workbench-stat-card--accent.is-purchase {
  color: #ffffff;
  background: linear-gradient(135deg, #0f766e 0%, #0f172a 100%);
  border-color: rgba(15, 118, 110, 0.35);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.14);
}

.mrp-workbench-stat-card--accent.is-production {
  color: #ffffff;
  background: linear-gradient(135deg, #1d4ed8 0%, #0f172a 100%);
  border-color: rgba(37, 99, 235, 0.35);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.14);
}

.mrp-workbench-stat-card__body {
  min-width: 0;
}

.mrp-workbench-stat-card__label {
  font-size: 13px;
  font-weight: 700;
  color: #94a3b8;
}

.mrp-workbench-stat-card--accent .mrp-workbench-stat-card__label {
  color: #cbd5e1;
}

.mrp-workbench-stat-card__value {
  margin-top: 10px;
  font-size: 34px;
  font-weight: 900;
  line-height: 1;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.mrp-workbench-stat-card__value.is-danger {
  color: #dc2626;
}

.mrp-workbench-stat-card__value.is-success {
  color: #059669;
}

.mrp-workbench-stat-card__value.is-accent {
  color: #ffffff;
}

.mrp-workbench-stat-card__badge {
  display: inline-flex;
  align-items: center;
  margin-top: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}

.mrp-workbench-stat-card__badge.is-danger {
  color: #dc2626;
  background: #fef2f2;
}

.mrp-workbench-stat-card__badge.is-success {
  color: #059669;
  background: #ecfdf5;
}

.mrp-workbench-stat-card__badge.is-accent {
  color: #34d399;
  background: rgba(255, 255, 255, 0.1);
}

.mrp-workbench-stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 16px;
  background: #f8fafc;
  color: #0f766e;
}

.mrp-workbench-stat-card__icon.is-warn {
  background: #ecfeff;
  color: #0f766e;
}

.mrp-workbench-stat-card__icon.is-danger {
  background: #fef2f2;
  color: #dc2626;
}

.mrp-workbench-stat-card__icon.is-success {
  background: #ecfdf5;
  color: #059669;
}

.mrp-workbench-stat-card--accent.is-purchase .mrp-workbench-stat-card__icon.is-accent {
  background: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
  font-size: 30px;
  font-weight: 800;
}

.mrp-workbench-stat-card--accent.is-production .mrp-workbench-stat-card__icon.is-accent {
  background: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
  font-size: 30px;
  font-weight: 800;
}

.mrp-workbench-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0;
  margin-top: 16px;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.mrp-workbench-tabs__item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 54px;
  padding: 0 18px;
  border: 0;
  border-radius: 16px;
  background: transparent;
  color: #64748b;
  font-size: 15px;
  font-weight: 800;
  transition: all 0.2s ease;
}

.mrp-workbench-tabs__item.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, #0f9d79 0%, #0f766e 100%);
  box-shadow: 0 12px 24px rgba(16, 185, 129, 0.18);
}

.mrp-workbench-tabs__item.is-active:last-child {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.18);
}

.mrp-workbench-tabs__item:not(.is-active):hover {
  background: #f8fafc;
  color: #475569;
}

.suggest-page-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 6px 4px;
}

.suggest-page-header__eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: #5f7c99;
  text-transform: uppercase;
}

.suggest-page-header__content {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.suggest-page-header__title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: #1e3a5f;
}

.mrp-trace-banner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #dbe7f3;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f3f8fd 100%);
}

.mrp-trace-banner__main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mrp-trace-banner__eyebrow {
  color: #5f7c99;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.mrp-trace-banner__title {
  color: #1e3a5f;
  font-size: 14px;
}

.mrp-trace-banner__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.mrp-query-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.mrp-query-form__actions {
  grid-column: 1 / -1;
}

.mrp-query-form__actions :deep(.el-form-item__content) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mrp-table-card {
  overflow: hidden;
  border: 1px solid #dbe7f3;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.mrp-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 18px 12px;
  border-bottom: 1px solid #e7eef6;
  background: linear-gradient(180deg, #f9fbff 0%, #f2f7fc 100%);
}

.mrp-toolbar :deep(.el-button) {
  min-height: 34px;
  padding-inline: 14px;
  border-radius: 10px;
}

.mrp-grid-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.mrp-grid-table :deep(.el-table__header th) {
  height: 46px;
  background: #f7fafc;
  color: #5f7388;
  font-weight: 600;
  border-bottom: 1px solid #e6edf5;
}

.mrp-grid-table :deep(.el-table__row td) {
  padding-top: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid #edf2f7;
  vertical-align: top;
}

.mrp-grid-table :deep(.el-table__body tr:hover > td) {
  background: #f9fbff !important;
}

.mrp-table-card :deep(.pagination-container) {
  margin: 0;
  padding: 14px 18px 16px;
  border-top: 1px solid #e7eef6;
  background: #fff;
}

.mrp-cell-block,
.mrp-item-cell,
.mrp-trace-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mrp-cell-block__title,
.mrp-item-cell__name {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
}

.mrp-cell-block__meta {
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
}

.mrp-item-cell__name {
  word-break: break-word;
}

.mrp-item-cell__trace {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
  padding: 3px 8px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #e5eef7;
  color: #334155;
  font-size: 12px;
  line-height: 1.4;
}

.mrp-item-cell__trace-label {
  color: #64748b;
}

.mrp-item-cell__trace-value {
  font-weight: 600;
}

.mrp-item-cell__link {
  align-self: flex-start;
  padding: 0;
  font-size: 13px;
}

.mrp-qty-stack {
  display: inline-flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
  margin-left: auto;
}

.mrp-qty-stack__item {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.mrp-qty-stack__label {
  color: #7b8794;
  font-size: 13px;
}

.mrp-qty-stack__value {
  min-width: 64px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.mrp-qty-stack__value--accent {
  color: #d97706;
}

.mrp-status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 78px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}

.mrp-status-chip.is-pending {
  color: #c57a00;
  background: #fff7e8;
  border-color: #f8d9a0;
}

.mrp-status-chip.is-confirmed {
  color: #2563eb;
  background: #eef4ff;
  border-color: #c7dafc;
}

.mrp-status-chip.is-converted {
  color: #059669;
  background: #ecfdf3;
  border-color: #b7f0d0;
}

.mrp-status-chip.is-rejected {
  color: #e11d48;
  background: #fff1f2;
  border-color: #fecdd3;
}

.mrp-trace-cell__item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  line-height: 1.45;
}

.mrp-trace-cell__label {
  min-width: 56px;
  color: #7b8794;
}

.mrp-trace-cell__value {
  color: #2563eb;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.mrp-trace-cell__value--success {
  color: #059669;
}

.mrp-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 24px;
}

.mrp-row-actions__placeholder {
  color: #94a3b8;
}

.mrp-row-actions--trace {
  width: 100%;
}

.mrp-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 36px 0;
  color: #94a3b8;
}

.mrp-empty-state__icon {
  font-size: 28px;
}

.mrp-empty-state__title {
  font-size: 13px;
}

.basis-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.basis-trace {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #dbe7f3;
  background: #f8fbff;
}

.basis-trace__title {
  margin-bottom: 12px;
  color: #16324f;
  font-size: 14px;
  font-weight: 600;
}

.basis-trace__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.basis-trace__item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.basis-trace__item--wide {
  grid-column: 1 / -1;
}

.basis-trace__label {
  color: #60758d;
  font-size: 12px;
  font-weight: 600;
}

.basis-trace__value {
  color: #16324f;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.basis-trace__value--mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.basis-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
  border: 1px solid #d8e8ff;
}

.basis-hero__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.basis-hero__eyebrow {
  font-size: 12px;
  font-weight: 600;
  color: #4e6e8e;
  letter-spacing: 0.08em;
}

.basis-hero__title {
  font-size: 22px;
  font-weight: 700;
  color: #16324f;
  line-height: 1.2;
}

.basis-hero__name {
  margin-left: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #47617f;
}

.basis-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #5b7189;
}

.basis-hero__result {
  min-width: 160px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #e1ebf5;
}

.basis-hero__result-label,
.basis-summary__label,
.basis-card__label {
  font-size: 13px;
  color: #60758d;
}

.basis-hero__result-value,
.basis-summary__value,
.basis-card__value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 700;
  color: #1d2f43;
  line-height: 1;
}

.basis-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 12px;
  background: #fff7f5;
  border: 1px solid #ffd9d1;
}

.basis-summary__value {
  color: #d14343;
}

.basis-explain {
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  color: #53657a;
  font-size: 14px;
  line-height: 1.7;
}

.basis-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.basis-card {
  padding: 14px 16px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e6edf5;
}

.basis-card__value {
  font-size: 22px;
}

.basis-reservation {
  padding: 16px 18px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e6edf5;
}

.basis-reservation__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.basis-reservation__title {
  color: #16324f;
  font-size: 14px;
  font-weight: 600;
}

.basis-reservation__alert {
  margin-bottom: 12px;
}

.basis-reservation__table {
  width: 100%;
}

@media (max-width: 1440px) {
  .mrp-workbench-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1280px) {
  .mrp-workbench-header {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .mrp-workbench-user {
    justify-self: start;
  }

  .mrp-query-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .mrp-workbench-page {
    padding-inline: 12px;
  }

  .mrp-workbench-stats,
  .mrp-workbench-tabs {
    grid-template-columns: 1fr;
  }

  .mrp-workbench-stat-card {
    min-height: 118px;
  }

  .mrp-workbench-header__center {
    width: 100%;
    justify-content: center;
  }

  .mrp-workbench-tabs__item {
    width: 100%;
    min-height: 50px;
  }

  .mrp-query-form {
    grid-template-columns: 1fr;
  }

  .mrp-query-form__actions :deep(.el-form-item__content) {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .mrp-toolbar {
    padding-inline: 14px;
  }

  .mrp-toolbar :deep(.el-button) {
    width: 100%;
  }

  .mrp-qty-stack {
    margin-left: 0;
  }

  .basis-hero,
  .basis-summary {
    flex-direction: column;
    align-items: flex-start;
  }

  .basis-hero__result {
    width: 100%;
  }

  .basis-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .basis-trace__grid {
    grid-template-columns: 1fr;
  }

  .basis-reservation__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>



