<template>
  <div class="finance-asset-page">
    <ContentWrap>
      <div class="finance-asset-page__hero">
        <div>
          <div class="finance-asset-page__title">固定资产台账</div>
        </div>
        <div class="finance-asset-page__hero-actions">
          <el-tag effect="light" round>资产管理</el-tag>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        label-width="88px"
        class="finance-asset-page__query-form"
      >
        <div class="finance-asset-page__query-grid">
          <el-form-item label="资产编号" prop="no">
            <el-input
              v-model="queryParams.no"
              placeholder="请输入资产编号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="资产名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入资产名称"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="资产分类" prop="categoryName">
            <el-input
              v-model="queryParams.categoryName"
              placeholder="请输入资产分类"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option
                v-for="item in ASSET_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-asset-page__query-actions">
          <el-button :loading="loadingList" @click="handleQuery" v-hasPermi="['erp:assets:query']">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="loadingList" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="finance-asset-page__toolbar">
        <div class="finance-asset-page__toolbar-title">资产列表</div>
        <div class="finance-asset-page__toolbar-actions">
          <el-button type="primary" plain :disabled="savingAsset" @click="openCreateDialog" v-hasPermi="['erp:assets:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增资产
          </el-button>
          <el-button plain :disabled="loadingCandidates" @click="openCandidateDialog">
            <Icon icon="ep:collection" class="mr-5px" />
            资产候选
          </el-button>
          <el-button type="success" plain :disabled="generatingDepreciation" @click="openDepreciationDialog">
            <Icon icon="ep:histogram" class="mr-5px" />
            生成折旧
          </el-button>
        </div>
      </div>

      <el-result v-if="listErrorMessage && !assetList.length" icon="error" title="固定资产列表加载失败">
        <template #extra>
          <el-button type="primary" @click="getAssetList">重试</el-button>
        </template>
      </el-result>

      <template v-else>
        <div v-if="loadingList || assetList.length" class="finance-asset-page__table-wrap">
          <el-table v-loading="loadingList" :data="assetList" row-key="id" stripe show-overflow-tooltip>
            <el-table-column label="资产信息" min-width="220">
              <template #default="{ row }">
                <div class="finance-asset-page__primary-cell">
                  <span class="finance-asset-page__primary-text">{{ row.name || '-' }}</span>
                  <span class="finance-asset-page__muted-text font-mono">{{ row.no || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分类与来源" min-width="200">
              <template #default="{ row }">
                <div class="finance-asset-page__primary-cell">
                  <span class="finance-asset-page__primary-text">{{ row.categoryName || '-' }}</span>
                  <div class="finance-asset-page__row-tags">
                    <el-tag size="small" effect="light" round :type="sourceTypeTagType(row.sourceType)">
                      {{ getSourceTypeLabel(row.sourceType) }}
                    </el-tag>
                    <span class="finance-asset-page__muted-text finance-asset-page__mono">
                      {{ row.sourceBizNo || '手工新增' }}
                    </span>
                  </div>
                  <el-button
                    v-if="canJumpToSource(row.sourceType, row.sourceBizId)"
                    link
                    type="primary"
                    class="finance-asset-page__source-link"
                    :disabled="navigatingSource"
                    @click="jumpToSource(row.sourceType, row.sourceBizId, row.sourceBizNo)"
                  >
                    查看来源
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="购置 / 启用" min-width="180">
              <template #default="{ row }">
                <div class="finance-asset-page__primary-cell">
                  <span class="finance-asset-page__muted-text">{{ formatDateValue(row.purchaseDate) }}</span>
                  <span class="finance-asset-page__muted-text">{{ formatDateValue(row.startUseDate) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="原值" align="right" min-width="120">
              <template #default="{ row }">{{ formatAmount(row.originalAmount) }}</template>
            </el-table-column>
            <el-table-column label="累计折旧" align="right" min-width="120">
              <template #default="{ row }">{{ formatAmount(row.depreciatedAmount) }}</template>
            </el-table-column>
            <el-table-column label="净值" align="right" min-width="120">
              <template #default="{ row }">{{ formatAmount(row.currentAmount) }}</template>
            </el-table-column>
            <el-table-column label="折旧方式" min-width="140">
              <template #default="{ row }">
                <div class="finance-asset-page__primary-cell">
                  <span class="finance-asset-page__muted-text">{{ row.depreciationMethod || '-' }}</span>
                  <span class="finance-asset-page__muted-text">{{ row.depreciationStartPeriod || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="110">
              <template #default="{ row }">
                <el-tag :type="assetStatusTagType(row.status)" effect="light" round>
                  {{ getAssetStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近计提期间" min-width="120">
              <template #default="{ row }">{{ row.lastDepreciationPeriod || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" fixed="right" width="280">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="loadingDetail" @click="openDetailDrawer(row.id!)">
                  详情
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="savingAsset || !canEdit(row)"
                  @click="openEditDialog(row.id!)"
                  v-hasPermi="['erp:assets:update']"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="warning"
                  :disabled="updatingStatus || !canToggleStatus(row)"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 10 ? '停用' : '启用' }}
                </el-button>
                <el-button
                  link
                  type="danger"
                  :disabled="deletingAsset || !canDelete(row)"
                  @click="handleDelete(row)"
                  v-hasPermi="['erp:assets:delete']"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无固定资产数据" />
      </template>

      <Pagination
        v-if="assetTotal > 0"
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="assetTotal"
        @pagination="getAssetList"
      />
    </ContentWrap>

    <Dialog
      v-model="assetDialogOpen"
      :title="assetDialogMode === 'create' ? '新增固定资产' : '编辑固定资产'"
      width="760px"
      scroll
      maxHeight="78vh"
      @closed="resetAssetDialog"
    >
      <el-form
        ref="assetFormRef"
        :model="assetForm"
        :rules="assetFormRules"
        label-width="110px"
        class="finance-asset-page__dialog-form"
      >
        <div class="finance-asset-page__dialog-grid">
          <el-form-item label="资产名称" prop="name">
            <el-input v-model="assetForm.name" placeholder="请输入资产名称" />
          </el-form-item>
          <el-form-item label="资产分类" prop="categoryName">
            <el-input v-model="assetForm.categoryName" placeholder="请输入资产分类" />
          </el-form-item>
          <el-form-item label="购置日期" prop="purchaseDate">
            <el-date-picker v-model="assetForm.purchaseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item label="启用日期" prop="startUseDate">
            <el-date-picker v-model="assetForm.startUseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item label="原值" prop="originalAmount">
            <el-input-number v-model="assetForm.originalAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="残值率" prop="salvageRate">
            <el-input-number v-model="assetForm.salvageRate" :min="0" :max="1" :step="0.01" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="折旧方式" prop="depreciationMethod">
            <el-select v-model="assetForm.depreciationMethod" placeholder="请选择折旧方式" class="!w-full">
              <el-option label="年限平均法" value="年限平均法" />
            </el-select>
          </el-form-item>
          <el-form-item label="折旧月数" prop="depreciationPeriodMonths">
            <el-input-number v-model="assetForm.depreciationPeriodMonths" :min="1" :precision="0" class="!w-full" />
          </el-form-item>
          <el-form-item label="折旧起始期间" prop="depreciationStartPeriod">
            <el-input v-model="assetForm.depreciationStartPeriod" placeholder="请输入如 2026-05" />
          </el-form-item>
          <el-form-item label="来源业务单号" prop="sourceBizNo">
            <el-input v-model="assetForm.sourceBizNo" placeholder="手工新增可留空" />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="finance-asset-page__dialog-grid-span">
            <el-input v-model="assetForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button :disabled="savingAsset" @click="assetDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="savingAsset" @click="submitAssetForm">保存</el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="candidateDialogOpen"
      title="资产候选"
      width="920px"
      scroll
      maxHeight="78vh"
      @closed="resetCandidateDialog"
    >
      <el-table v-loading="loadingCandidates" :data="candidateList" stripe show-overflow-tooltip>
        <el-table-column label="来源业务" min-width="220">
          <template #default="{ row }">
            <div class="finance-asset-page__primary-cell">
              <div class="finance-asset-page__row-tags">
                <el-tag size="small" effect="light" round :type="sourceTypeTagType(row.sourceType)">
                  {{ getSourceTypeLabel(row.sourceType) }}
                </el-tag>
                <span class="finance-asset-page__muted-text finance-asset-page__mono">
                  {{ row.sourceBizNo || '-' }}
                </span>
              </div>
              <el-button
                v-if="canJumpToSource(row.sourceType, row.sourceBizId)"
                link
                type="primary"
                class="finance-asset-page__source-link"
                :disabled="navigatingSource"
                @click="jumpToSource(row.sourceType, row.sourceBizId, row.sourceBizNo)"
              >
                查看来源
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="候选资产" prop="assetName" min-width="180" />
        <el-table-column label="分类" prop="categoryName" min-width="120" />
        <el-table-column label="金额" min-width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="购置日期" min-width="120">
          <template #default="{ row }">{{ formatDateValue(row.purchaseDate) }}</template>
        </el-table-column>
        <el-table-column label="来源备注" min-width="180">
          <template #default="{ row }">
            <span class="finance-asset-page__muted-text" :title="row.remark || '暂无来源备注'">
              {{ row.remark || '暂无来源备注' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="candidateStatusTagType(row.status)" effect="light" round>
              {{ getCandidateStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" align="center">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="confirmingCandidate || row.status !== 0"
              @click="openCandidateConfirm(row)"
            >
              确认入账
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-if="candidateTotal > 0"
        v-model:limit="candidateQuery.pageSize"
        v-model:page="candidateQuery.pageNo"
        :total="candidateTotal"
        @pagination="getCandidateList"
      />
    </Dialog>

    <Dialog
      v-model="candidateConfirmDialogOpen"
      title="确认生成固定资产"
      width="760px"
      scroll
      maxHeight="78vh"
      @closed="resetCandidateConfirmDialog"
      >
        <div class="finance-asset-page__context-card">
          <div class="finance-asset-page__context-main">
            <div class="finance-asset-page__context-eyebrow">来源摘要</div>
            <div class="finance-asset-page__context-title">{{ candidateConfirmForm.name || '-' }}</div>
            <div class="finance-asset-page__context-meta">
              <span>{{ getSourceTypeLabel(candidateConfirmForm.sourceType) }}</span>
              <span>{{ candidateConfirmForm.sourceBizNo || '-' }}</span>
            </div>
          </div>
          <div class="finance-asset-page__context-side">
            <div class="finance-asset-page__context-stat">
              <span>候选金额</span>
              <strong>{{ formatAmount(candidateConfirmForm.originalAmount) }}</strong>
            </div>
            <div class="finance-asset-page__context-stat">
              <span>购置日期</span>
              <strong>{{ formatDateValue(candidateConfirmForm.purchaseDate) }}</strong>
            </div>
          </div>
        </div>
        <div class="finance-asset-page__source-summary">
          <div class="finance-asset-page__source-summary-item">
            <span>来源类型</span>
            <strong>{{ getSourceTypeLabel(candidateConfirmForm.sourceType) }}</strong>
          </div>
          <div class="finance-asset-page__source-summary-item">
            <span>来源单号</span>
            <strong class="finance-asset-page__mono">{{ candidateConfirmForm.sourceBizNo || '-' }}</strong>
          </div>
          <div class="finance-asset-page__source-summary-item finance-asset-page__source-summary-item--full">
            <span>来源备注</span>
            <strong>{{ candidateConfirmForm.sourceRemark || '暂无来源备注' }}</strong>
          </div>
          <div class="finance-asset-page__source-summary-item finance-asset-page__source-summary-item--full finance-asset-page__source-summary-item--actions">
            <el-button
              v-if="canJumpToSource(candidateConfirmForm.sourceType, candidateConfirmForm.sourceBizId)"
              link
              type="primary"
              :disabled="navigatingSource"
              @click="jumpToSource(candidateConfirmForm.sourceType, candidateConfirmForm.sourceBizId, candidateConfirmForm.sourceBizNo)"
            >
              查看来源
            </el-button>
          </div>
        </div>
        <el-form
          ref="candidateFormRef"
          :model="candidateConfirmForm"
        :rules="assetFormRules"
        label-width="110px"
        class="finance-asset-page__dialog-form"
      >
        <div class="finance-asset-page__dialog-grid">
          <el-form-item label="资产名称" prop="name">
            <el-input v-model="candidateConfirmForm.name" placeholder="请输入资产名称" />
          </el-form-item>
          <el-form-item label="资产分类" prop="categoryName">
            <el-input v-model="candidateConfirmForm.categoryName" placeholder="请输入资产分类" />
          </el-form-item>
          <el-form-item label="购置日期" prop="purchaseDate">
            <el-date-picker v-model="candidateConfirmForm.purchaseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item label="启用日期" prop="startUseDate">
            <el-date-picker v-model="candidateConfirmForm.startUseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item label="原值" prop="originalAmount">
            <el-input-number v-model="candidateConfirmForm.originalAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="残值率" prop="salvageRate">
            <el-input-number v-model="candidateConfirmForm.salvageRate" :min="0" :max="1" :step="0.01" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="折旧方式" prop="depreciationMethod">
            <el-select v-model="candidateConfirmForm.depreciationMethod" placeholder="请选择折旧方式" class="!w-full">
              <el-option label="年限平均法" value="年限平均法" />
            </el-select>
          </el-form-item>
          <el-form-item label="折旧月数" prop="depreciationPeriodMonths">
            <el-input-number v-model="candidateConfirmForm.depreciationPeriodMonths" :min="1" :precision="0" class="!w-full" />
          </el-form-item>
          <el-form-item label="折旧起始期间" prop="depreciationStartPeriod">
            <el-input v-model="candidateConfirmForm.depreciationStartPeriod" placeholder="请输入如 2026-05" />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="finance-asset-page__dialog-grid-span">
            <el-input v-model="candidateConfirmForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button :disabled="confirmingCandidate" @click="candidateConfirmDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="confirmingCandidate" @click="submitCandidateConfirm">确认入账</el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="depreciationDialogOpen"
      title="生成折旧"
      width="520px"
      scroll
      maxHeight="72vh"
      @closed="resetDepreciationDialog"
    >
      <el-form label-width="110px">
        <el-form-item label="折旧期间">
          <el-input v-model="depreciationPeriod" placeholder="请输入如 2026-05" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="generatingDepreciation" @click="depreciationDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="generatingDepreciation" @click="submitDepreciation">确认生成</el-button>
      </template>
    </Dialog>

    <el-drawer
      v-model="detailDrawerOpen"
      title="资产详情"
      size="520px"
      :with-header="true"
      destroy-on-close
      @closed="clearDetailDrawer"
    >
      <div v-loading="loadingDetail" class="finance-asset-page__drawer">
        <template v-if="assetDetail">
          <div class="finance-asset-page__detail-card">
            <div class="finance-asset-page__detail-title">{{ assetDetail.name || '-' }}</div>
            <div class="finance-asset-page__detail-meta">
              <span>{{ assetDetail.no || '-' }}</span>
              <span>{{ getSourceTypeLabel(assetDetail.sourceType) }}</span>
              <span>{{ assetDetail.sourceBizNo || '手工新增' }}</span>
            </div>
            <div class="finance-asset-page__detail-kpis">
              <div>
                <span>原值</span>
                <strong>{{ formatAmount(assetDetail.originalAmount) }}</strong>
              </div>
              <div>
                <span>累计折旧</span>
                <strong>{{ formatAmount(assetDetail.depreciatedAmount) }}</strong>
              </div>
              <div>
                <span>净值</span>
                <strong>{{ formatAmount(assetDetail.currentAmount) }}</strong>
              </div>
            </div>
          </div>

          <div class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">基础信息</div>
            <div class="finance-asset-page__detail-grid">
              <div><span>资产分类</span><strong>{{ assetDetail.categoryName || '-' }}</strong></div>
              <div><span>来源类型</span><strong>{{ getSourceTypeLabel(assetDetail.sourceType) }}</strong></div>
              <div><span>购置日期</span><strong>{{ formatDateValue(assetDetail.purchaseDate) }}</strong></div>
              <div><span>启用日期</span><strong>{{ formatDateValue(assetDetail.startUseDate) }}</strong></div>
              <div><span>状态</span><strong>{{ getAssetStatusLabel(assetDetail.status) }}</strong></div>
              <div><span>折旧方式</span><strong>{{ assetDetail.depreciationMethod || '-' }}</strong></div>
              <div><span>折旧期间</span><strong>{{ assetDetail.depreciationStartPeriod || '-' }}</strong></div>
            </div>
          </div>

          <div class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">来源追溯</div>
            <div class="finance-asset-page__detail-grid">
              <div><span>来源业务</span><strong>{{ getSourceTypeLabel(assetDetail.sourceType) }}</strong></div>
              <div><span>来源单号</span><strong class="finance-asset-page__mono">{{ assetDetail.sourceBizNo || '手工新增' }}</strong></div>
              <div class="finance-asset-page__detail-grid-span">
                <span>来源操作</span>
                <strong>
                  <el-button
                    v-if="canJumpToSource(assetDetail.sourceType, assetDetail.sourceBizId)"
                    link
                    type="primary"
                    :disabled="navigatingSource"
                    @click="jumpToSource(assetDetail.sourceType, assetDetail.sourceBizId, assetDetail.sourceBizNo)"
                  >
                    查看来源
                  </el-button>
                  <span v-else>暂无可跳转来源</span>
                </strong>
              </div>
            </div>
          </div>

          <div v-if="assetTrace?.candidate" class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">候选记录</div>
            <div class="finance-asset-page__detail-grid">
              <div><span>候选资产</span><strong>{{ assetTrace.candidate.assetName || '-' }}</strong></div>
              <div><span>候选状态</span><strong>{{ getCandidateStatusLabel(assetTrace.candidate.status) }}</strong></div>
              <div><span>候选分类</span><strong>{{ assetTrace.candidate.categoryName || '-' }}</strong></div>
              <div><span>候选金额</span><strong>{{ formatAmount(assetTrace.candidate.amount) }}</strong></div>
              <div class="finance-asset-page__detail-grid-span">
                <span>候选备注</span>
                <strong>{{ assetTrace.candidate.remark || '暂无来源备注' }}</strong>
              </div>
            </div>
          </div>

          <div v-if="assetTrace?.purchaseIn" class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">采购入库摘要</div>
            <div class="finance-asset-page__detail-grid">
              <div><span>入库单号</span><strong class="finance-asset-page__mono">{{ assetTrace.purchaseIn.no || '-' }}</strong></div>
              <div><span>供应商</span><strong>{{ assetTrace.purchaseIn.supplierName || '-' }}</strong></div>
              <div><span>入库时间</span><strong>{{ formatDateValue(assetTrace.purchaseIn.inTime) }}</strong></div>
              <div><span>合计金额</span><strong>{{ formatAmount(assetTrace.purchaseIn.totalPrice) }}</strong></div>
              <div><span>已付金额</span><strong>{{ formatAmount(assetTrace.purchaseIn.paymentPrice) }}</strong></div>
              <div><span>产品摘要</span><strong>{{ assetTrace.purchaseIn.productNames || '-' }}</strong></div>
              <div v-if="assetTrace.purchaseIn.matchedItemId" class="finance-asset-page__detail-grid-span">
                <span>命中明细</span>
                <strong>{{ buildPurchaseInMatchedTraceText(assetTrace.purchaseIn.items) }}</strong>
              </div>
              <div class="finance-asset-page__detail-grid-span">
                <span>来源明细</span>
                <strong>{{ buildPurchaseInTraceItemsText(assetTrace.purchaseIn.items) }}</strong>
              </div>
            </div>
          </div>

          <div v-if="assetTrace?.expense" class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">费用报销摘要</div>
            <div class="finance-asset-page__detail-grid">
              <div><span>报销单号</span><strong class="finance-asset-page__mono">{{ assetTrace.expense.no || '-' }}</strong></div>
              <div><span>费用类型</span><strong>{{ assetTrace.expense.expenseTypeName || '-' }}</strong></div>
              <div><span>部门</span><strong>{{ assetTrace.expense.deptName || '-' }}</strong></div>
              <div><span>项目</span><strong>{{ assetTrace.expense.projectName || '-' }}</strong></div>
              <div><span>报销金额</span><strong>{{ formatAmount(assetTrace.expense.expensePrice) }}</strong></div>
              <div><span>已付 / 剩余</span><strong>{{ formatAmount(assetTrace.expense.paidPrice) }} / {{ formatAmount(assetTrace.expense.remainPrice) }}</strong></div>
              <div v-if="assetTrace.expense.matchedItemId" class="finance-asset-page__detail-grid-span">
                <span>命中明细</span>
                <strong>{{ buildExpenseMatchedTraceText(assetTrace.expense.items) }}</strong>
              </div>
              <div class="finance-asset-page__detail-grid-span">
                <span>来源明细</span>
                <strong>{{ buildExpenseTraceItemsText(assetTrace.expense.items) }}</strong>
              </div>
            </div>
          </div>

          <div class="finance-asset-page__detail-block">
            <div class="finance-asset-page__detail-block-title">折旧记录</div>
            <div v-if="depreciationList.length" class="finance-asset-page__depreciation-list">
              <div v-for="item in depreciationList" :key="item.id" class="finance-asset-page__depreciation-item">
                <div class="finance-asset-page__depreciation-head">
                  <span>{{ item.period }}</span>
                  <strong>{{ formatAmount(item.depreciationAmount) }}</strong>
                </div>
                <div class="finance-asset-page__muted-text">
                  累计折旧 {{ formatAmount(item.afterDepreciatedAmount) }}，净值 {{ formatAmount(item.afterCurrentAmount) }}
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无折旧记录" />
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { formatAmount, formatDateValue } from '@/views/erp/finance/shared/accounting'
import {
  FinanceAssetApi,
  type FinanceAssetCandidateVO,
  type FinanceAssetDepreciationVO,
  type FinanceAssetTraceVO,
  type FinanceAssetVO
} from '@/api/erp/finance/assets'

defineOptions({ name: 'ErpFinanceAssets' })

const message = useMessage()
const router = useRouter()

const ASSET_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '使用中', value: 10 },
  { label: '已停用', value: 20 },
  { label: '已处置', value: 30 }
]

const CANDIDATE_STATUS_OPTIONS = [
  { label: '待确认', value: 0 },
  { label: '已确认', value: 10 },
  { label: '已驳回', value: 20 }
]

const loadingList = ref(false)
const listErrorMessage = ref('')
const assetList = ref<FinanceAssetVO[]>([])
const assetTotal = ref(0)

const savingAsset = ref(false)
const loadingDetail = ref(false)
const deletingAsset = ref(false)
const updatingStatus = ref(false)
const loadingCandidates = ref(false)
const confirmingCandidate = ref(false)
const generatingDepreciation = ref(false)
const navigatingSource = ref(false)

const assetDialogOpen = ref(false)
const assetDialogMode = ref<'create' | 'update'>('create')
const candidateDialogOpen = ref(false)
const candidateConfirmDialogOpen = ref(false)
const depreciationDialogOpen = ref(false)
const detailDrawerOpen = ref(false)

const assetDetail = ref<FinanceAssetVO>()
const assetTrace = ref<FinanceAssetTraceVO>()
const depreciationList = ref<FinanceAssetDepreciationVO[]>([])
const candidateList = ref<FinanceAssetCandidateVO[]>([])
const candidateTotal = ref(0)
const currentCandidateId = ref<number>()
const currentDetailId = ref<number>()

const queryFormRef = ref<FormInstance>()
const assetFormRef = ref<FormInstance>()
const candidateFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined as string | undefined,
  name: undefined as string | undefined,
  categoryName: undefined as string | undefined,
  status: undefined as number | undefined
})

const candidateQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  status: 0
})

const assetForm = reactive<FinanceAssetVO>({
  name: '',
  categoryName: '',
  sourceType: 0,
  originalAmount: 0,
  salvageRate: 0.05,
  depreciationMethod: '年限平均法',
  depreciationPeriodMonths: 36,
  depreciationStartPeriod: '',
  remark: ''
})

const candidateConfirmForm = reactive<any>({
  candidateId: undefined,
  sourceType: 0,
  sourceBizId: undefined,
  sourceBizNo: '',
  sourceRemark: '',
  name: '',
  categoryName: '',
  purchaseDate: '',
  startUseDate: '',
  originalAmount: 0,
  salvageRate: 0.05,
  depreciationMethod: '年限平均法',
  depreciationPeriodMonths: 36,
  depreciationStartPeriod: '',
  remark: ''
})

const depreciationPeriod = ref('')

const assetFormRules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请输入资产分类', trigger: 'blur' }],
  originalAmount: [{ required: true, message: '请输入原值', trigger: 'change' }],
  salvageRate: [{ required: true, message: '请输入残值率', trigger: 'change' }],
  depreciationMethod: [{ required: true, message: '请选择折旧方式', trigger: 'change' }],
  depreciationPeriodMonths: [{ required: true, message: '请输入折旧月数', trigger: 'change' }],
  depreciationStartPeriod: [{ required: true, message: '请输入折旧起始期间', trigger: 'blur' }]
}

const getAssetStatusLabel = (value?: number) =>
  ASSET_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getCandidateStatusLabel = (value?: number) =>
  CANDIDATE_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'

const getSourceTypeLabel = (value?: number) => {
  if (value === 10) return '采购入库'
  if (value === 20) return '费用报销'
  return '手工新增'
}

const sourceTypeTagType = (value?: number) => {
  if (value === 10) return 'success'
  if (value === 20) return 'warning'
  return 'info'
}

const canJumpToSource = (sourceType?: number, sourceBizId?: number) =>
  Boolean(sourceBizId && (sourceType === 10 || sourceType === 20))

const buildPurchaseInTraceItemsText = (items?: Array<{ productName?: string; count?: number; totalPrice?: number; remark?: string; matched?: boolean }>) => {
  if (!items?.length) return '-'
  return items
    .map((item) => {
      const segments = [item.productName || '未命名产品']
      if (item.matched) segments.unshift('精确命中')
      if (item.count != null) segments.push(`数量 ${item.count}`)
      if (item.totalPrice != null) segments.push(`金额 ${formatAmount(item.totalPrice)}`)
      if (item.remark) segments.push(item.remark)
      return segments.join(' / ')
    })
    .join('；')
}

const buildPurchaseInMatchedTraceText = (items?: Array<{ productName?: string; count?: number; totalPrice?: number; remark?: string; matched?: boolean }>) => {
  if (!items?.length) return '-'
  const matchedItems = items.filter((item) => item.matched)
  return matchedItems.length ? buildPurchaseInTraceItemsText(matchedItems) : '-'
}

const buildExpenseTraceItemsText = (items?: Array<{ itemName?: string; amount?: number; remark?: string; assetCandidateFlag?: boolean; matched?: boolean }>) => {
  if (!items?.length) return '-'
  return items
    .map((item) => {
      const segments = [item.itemName || '未命名明细']
      if (item.matched) segments.unshift('精确命中')
      if (item.amount != null) segments.push(`金额 ${formatAmount(item.amount)}`)
      if (item.assetCandidateFlag) segments.push('已标记转固定资产')
      if (item.remark) segments.push(item.remark)
      return segments.join(' / ')
    })
    .join('；')
}

const buildExpenseMatchedTraceText = (items?: Array<{ itemName?: string; amount?: number; remark?: string; assetCandidateFlag?: boolean; matched?: boolean }>) => {
  if (!items?.length) return '-'
  const matchedItems = items.filter((item) => item.matched)
  return matchedItems.length ? buildExpenseTraceItemsText(matchedItems) : '-'
}

const assetStatusTagType = (status?: number) => {
  if (status === 10) return 'success'
  if (status === 20) return 'warning'
  if (status === 30) return 'danger'
  return 'info'
}

const candidateStatusTagType = (status?: number) => {
  if (status === 10) return 'success'
  if (status === 20) return 'danger'
  return 'warning'
}

const jumpToSource = async (sourceType?: number, sourceBizId?: number, sourceBizNo?: string) => {
  if (!canJumpToSource(sourceType, sourceBizId) || navigatingSource.value) {
    if (!sourceBizId) {
      message.warning('当前来源信息不完整，暂时无法跳转')
    }
    return
  }
  navigatingSource.value = true
  try {
    if (sourceType === 10) {
      await router.push({
        path: '/erp/purchase/in',
        query: {
          no: sourceBizNo || undefined
        }
      })
      return
    }
    if (sourceType === 20) {
      await router.push({
        path: '/erp/finance/expense',
        query: {
          no: sourceBizNo || undefined
        }
      })
      return
    }
    message.warning('当前来源类型暂不支持跳转')
  } finally {
    navigatingSource.value = false
  }
}

const canEdit = (row: FinanceAssetVO) => row.status !== 30
const canToggleStatus = (row: FinanceAssetVO) => row.status === 10 || row.status === 20
const canDelete = (row: FinanceAssetVO) => row.status === 0

const getAssetList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceAssetApi.getFinanceAssetPage(queryParams)
    assetList.value = data?.list || []
    assetTotal.value = data?.total || 0
  } catch (error: any) {
    if (!assetList.value.length) {
      listErrorMessage.value = error?.message || '固定资产列表加载失败'
    }
  } finally {
    loadingList.value = false
  }
}

const getCandidateList = async () => {
  loadingCandidates.value = true
  try {
    const data = await FinanceAssetApi.getFinanceAssetCandidatePage(candidateQuery)
    candidateList.value = data?.list || []
    candidateTotal.value = data?.total || 0
  } finally {
    loadingCandidates.value = false
  }
}

const getDepreciationList = async (assetId: number) => {
  const data = await FinanceAssetApi.getDepreciationPage({ pageNo: 1, pageSize: 20, assetId })
  depreciationList.value = data?.list || []
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getAssetList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getAssetList()
}

const openCreateDialog = () => {
  assetDialogMode.value = 'create'
  assetDialogOpen.value = true
}

const openEditDialog = async (id: number) => {
  assetDialogMode.value = 'update'
  const data = await FinanceAssetApi.getFinanceAsset(id)
  Object.assign(assetForm, data || {})
  assetDialogOpen.value = true
}

const submitAssetForm = async () => {
  const formRef = assetFormRef.value
  if (!formRef) return
  await formRef.validate()
  savingAsset.value = true
  try {
    if (assetDialogMode.value === 'create') {
      await FinanceAssetApi.createFinanceAsset(assetForm)
      message.success('保存成功')
    } else {
      await FinanceAssetApi.updateFinanceAsset(assetForm)
      message.success('更新成功')
    }
    assetDialogOpen.value = false
    await getAssetList()
  } finally {
    savingAsset.value = false
  }
}

const resetAssetDialog = () => {
  assetFormRef.value?.resetFields()
  Object.assign(assetForm, {
    id: undefined,
    name: '',
    categoryName: '',
    sourceType: 0,
    sourceBizId: undefined,
    sourceBizNo: '',
    purchaseDate: '',
    startUseDate: '',
    originalAmount: 0,
    salvageRate: 0.05,
    depreciationMethod: '年限平均法',
    depreciationPeriodMonths: 36,
    depreciationStartPeriod: '',
    remark: ''
  })
}

const handleDelete = async (row: FinanceAssetVO) => {
  await ElMessageBox.confirm(`确定删除固定资产「${row.name}」吗？`, '删除确认', {
    type: 'warning'
  })
  deletingAsset.value = true
  try {
    await FinanceAssetApi.deleteFinanceAsset([Number(row.id)])
    message.success('删除成功')
    await getAssetList()
  } finally {
    deletingAsset.value = false
  }
}

const toggleStatus = async (row: FinanceAssetVO) => {
  const nextStatus = row.status === 10 ? 20 : 10
  updatingStatus.value = true
  try {
    await FinanceAssetApi.updateFinanceAssetStatus({ id: Number(row.id), status: nextStatus })
    message.success('状态更新成功')
    await getAssetList()
    if (detailDrawerOpen.value && currentDetailId.value === row.id) {
      await openDetailDrawer(Number(row.id))
    }
  } finally {
    updatingStatus.value = false
  }
}

const openCandidateDialog = async () => {
  candidateDialogOpen.value = true
  await getCandidateList()
}

const resetCandidateDialog = () => {
  candidateList.value = []
  candidateTotal.value = 0
}

const openCandidateConfirm = (row: FinanceAssetCandidateVO) => {
  currentCandidateId.value = row.id
  Object.assign(candidateConfirmForm, {
    candidateId: row.id,
    sourceType: row.sourceType ?? 0,
    sourceBizId: row.sourceBizId,
    sourceBizNo: row.sourceBizNo || '',
    sourceRemark: row.remark || '',
    name: row.assetName,
    categoryName: row.categoryName || '',
    purchaseDate: row.purchaseDate || '',
    startUseDate: row.purchaseDate || '',
    originalAmount: row.amount || 0,
    salvageRate: 0.05,
    depreciationMethod: '年限平均法',
    depreciationPeriodMonths: 36,
    depreciationStartPeriod: '',
    remark: row.remark || ''
  })
  candidateConfirmDialogOpen.value = true
}

const submitCandidateConfirm = async () => {
  const formRef = candidateFormRef.value
  if (!formRef) return
  await formRef.validate()
  confirmingCandidate.value = true
  try {
    await FinanceAssetApi.confirmFinanceAssetCandidate(candidateConfirmForm)
    message.success('确认入账成功')
    candidateConfirmDialogOpen.value = false
    await Promise.all([getCandidateList(), getAssetList()])
  } finally {
    confirmingCandidate.value = false
  }
}

const resetCandidateConfirmDialog = () => {
  candidateFormRef.value?.resetFields()
  Object.assign(candidateConfirmForm, {
    candidateId: undefined,
    sourceType: 0,
    sourceBizId: undefined,
    sourceBizNo: '',
    sourceRemark: '',
    name: '',
    categoryName: '',
    purchaseDate: '',
    startUseDate: '',
    originalAmount: 0,
    salvageRate: 0.05,
    depreciationMethod: '年限平均法',
    depreciationPeriodMonths: 36,
    depreciationStartPeriod: '',
    remark: ''
  })
}

const openDepreciationDialog = () => {
  depreciationPeriod.value = ''
  depreciationDialogOpen.value = true
}

const submitDepreciation = async () => {
  generatingDepreciation.value = true
  try {
    const count = await FinanceAssetApi.generateDepreciation({ period: depreciationPeriod.value })
    message.success(`折旧生成成功，共生成 ${count || 0} 条记录`)
    depreciationDialogOpen.value = false
    await getAssetList()
    if (detailDrawerOpen.value && currentDetailId.value) {
      await openDetailDrawer(currentDetailId.value)
    }
  } finally {
    generatingDepreciation.value = false
  }
}

const resetDepreciationDialog = () => {
  depreciationPeriod.value = ''
}

const openDetailDrawer = async (id: number) => {
  loadingDetail.value = true
  detailDrawerOpen.value = true
  currentDetailId.value = id
  try {
    const [data, trace] = await Promise.all([
      FinanceAssetApi.getFinanceAsset(id),
      FinanceAssetApi.getFinanceAssetTrace(id)
    ])
    assetDetail.value = trace?.asset || data
    assetTrace.value = trace || undefined
    await getDepreciationList(id)
  } finally {
    loadingDetail.value = false
  }
}

const clearDetailDrawer = () => {
  assetDetail.value = undefined
  assetTrace.value = undefined
  depreciationList.value = []
  currentDetailId.value = undefined
}

onMounted(() => {
  getAssetList()
})
</script>

<style scoped>
.finance-asset-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-asset-page__hero,
.finance-asset-page__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.finance-asset-page__title,
.finance-asset-page__toolbar-title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.finance-asset-page__summary {
  margin-top: 6px;
  color: #64748b;
  line-height: 1.7;
}

.finance-asset-page__hero-actions,
.finance-asset-page__toolbar-actions,
.finance-asset-page__query-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-asset-page__row-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.finance-asset-page__query-grid,
.finance-asset-page__dialog-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-asset-page__dialog-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.finance-asset-page__dialog-grid-span {
  grid-column: 1 / -1;
}

.finance-asset-page__table-wrap {
  overflow-x: auto;
}

.finance-asset-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.finance-asset-page__primary-text {
  overflow: hidden;
  color: #1e293b;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-asset-page__muted-text {
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.finance-asset-page__mono {
  font-family: ui-monospace, SFMono-Regular, SFMono-Regular, Menlo, Monaco, Consolas, Liberation Mono, Courier New, monospace;
}

.finance-asset-page__source-link {
  padding: 0;
  align-self: flex-start;
}

.finance-asset-page__context-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #fff;
}

.finance-asset-page__context-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.finance-asset-page__context-eyebrow {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.finance-asset-page__context-title {
  font-size: 24px;
  font-weight: 700;
}

.finance-asset-page__context-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.finance-asset-page__context-side,
.finance-asset-page__source-summary {
  display: grid;
  gap: 12px;
}

.finance-asset-page__context-side {
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  width: min(360px, 100%);
}

.finance-asset-page__context-stat,
.finance-asset-page__source-summary-item {
  padding: 14px 16px;
  border: 1px solid rgba(226, 232, 240, 0.85);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(148, 163, 184, 0.06);
}

.finance-asset-page__context-stat {
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.finance-asset-page__context-stat span,
.finance-asset-page__source-summary-item span {
  display: block;
  font-size: 12px;
}

.finance-asset-page__context-stat span {
  color: rgba(255, 255, 255, 0.72);
}

.finance-asset-page__source-summary {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 16px;
}

.finance-asset-page__context-stat strong,
.finance-asset-page__source-summary-item strong {
  display: block;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.finance-asset-page__context-stat strong {
  color: #fff;
}

.finance-asset-page__source-summary-item--full,
.finance-asset-page__detail-grid-span {
  grid-column: 1 / -1;
}

.finance-asset-page__source-summary-item--actions strong {
  margin-top: 0;
}

.finance-asset-page__drawer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-asset-page__detail-card {
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #fff;
}

.finance-asset-page__detail-title {
  font-size: 22px;
  font-weight: 700;
}

.finance-asset-page__detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}

.finance-asset-page__detail-kpis {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.finance-asset-page__detail-kpis span {
  display: block;
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
}

.finance-asset-page__detail-kpis strong {
  display: block;
  margin-top: 6px;
  font-size: 18px;
}

.finance-asset-page__detail-block {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
}

.finance-asset-page__detail-block-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.finance-asset-page__detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.finance-asset-page__detail-grid span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.finance-asset-page__detail-grid strong {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.finance-asset-page__depreciation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-asset-page__depreciation-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.finance-asset-page__depreciation-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
  color: #0f172a;
}

@media (max-width: 1200px) {
  .finance-asset-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-asset-page__context-card {
    flex-direction: column;
  }

  .finance-asset-page__context-side,
  .finance-asset-page__source-summary {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .finance-asset-page__query-grid,
  .finance-asset-page__dialog-grid,
  .finance-asset-page__detail-grid,
  .finance-asset-page__detail-kpis {
    grid-template-columns: 1fr;
  }
}
</style>
