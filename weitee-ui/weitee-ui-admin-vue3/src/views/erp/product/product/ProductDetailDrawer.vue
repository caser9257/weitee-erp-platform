<template>
  <el-drawer
    v-model="detailDrawerVisible"
    :size="drawerSize"
    :with-header="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="true"
    append-to-body
    modal-class="product-detail-drawer__mask"
    @closed="resetDetailState"
  >
    <div class="product-drawer-shell">
      <div class="product-drawer-head">
        <div class="product-drawer-head__main">
          <div class="product-drawer-head__title-row">
            <h2 class="product-drawer-head__title" :title="product?.name">
              {{ product?.name || '产品详情' }}
            </h2>
            <span v-if="auditStatusMeta" class="status-pill" :class="`status-pill--${auditStatusMeta.tone}`">
              {{ auditStatusMeta.label }}
            </span>
          </div>
          <div class="product-drawer-head__meta">
            <span class="product-drawer-head__mono">{{
              formatMaterialCode(product?.materialCode, product?.prevMaterialCode)
            }}</span>
            <span class="product-drawer-head__mono">{{ product?.barCode || '-' }}</span>
            <span>{{ product?.categoryName || '-' }}</span>
            <span>{{ product?.unitName || '-' }}</span>
          </div>
        </div>
        <button type="button" class="drawer-close-button" @click="close">
          <Icon icon="ep:close" />
        </button>
      </div>

      <div class="product-drawer-summary">
        <div class="summary-metric">
          <div class="summary-metric__label">产品类型</div>
          <div class="summary-metric__value">{{ productTypeLabel }}</div>
        </div>
        <div class="summary-metric">
          <div class="summary-metric__label">生产方式</div>
          <div class="summary-metric__value">{{ produceTypeLabel }}</div>
        </div>
        <div class="summary-metric">
          <div class="summary-metric__label">MRP</div>
          <div class="summary-metric__value">
            <span
              class="status-pill"
              :class="product?.mrpEnable ? 'status-pill--primary' : 'status-pill--slate'"
            >
              {{ product?.mrpEnable ? '参与' : '不参与' }}
            </span>
          </div>
        </div>
        <div class="summary-metric">
          <div class="summary-metric__label">启用状态</div>
          <div class="summary-metric__value">
            <span
              class="status-pill"
              :class="product?.status === CommonStatusEnum.ENABLE ? 'status-pill--success' : 'status-pill--danger'"
            >
              {{ product?.status === CommonStatusEnum.ENABLE ? '启用' : '停用' }}
            </span>
          </div>
        </div>
      </div>

      <div class="product-drawer-body">
        <div v-if="loadingDetail" class="drawer-state drawer-state--loading">
          <el-skeleton animated :rows="10" />
        </div>

        <div v-else-if="detailLoadFailed" class="drawer-state drawer-state--error">
          <div class="drawer-state__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="drawer-state__title">详情加载失败</div>
          <el-button type="primary" plain :disabled="loadingDetail" @click="retryLoad">重新加载</el-button>
        </div>

        <div v-else-if="detailMissing" class="drawer-state drawer-state--empty">
          <div class="drawer-state__icon">
            <Icon icon="ep:document-remove" />
          </div>
          <div class="drawer-state__title">产品不存在或已删除</div>
        </div>

        <template v-else>
          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">基础信息</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>规格</label>
                <div class="info-item__value">{{ product?.standard || '-' }}</div>
              </div>
              <div class="info-item">
                <label>产品封装</label>
                <div class="info-item__value">{{ product?.packaging || '-' }}</div>
              </div>
              <div class="info-item">
                <label>品牌/制造商</label>
                <div class="info-item__value">{{ product?.brandManufacturer || '-' }}</div>
              </div>
              <div class="info-item">
                <label>替代型号</label>
                <div class="info-item__value">{{ product?.alternativeModel || '-' }}</div>
              </div>
              <div class="info-item">
                <label>重量（kg）</label>
                <div class="info-item__value info-item__value--mono">{{ formatWeight(product?.weight) }}</div>
              </div>
              <div class="info-item">
                <label>创建时间</label>
                <div class="info-item__value info-item__value--mono">{{ formatDateTime(product?.createTime) }}</div>
              </div>
            </div>
            <div v-if="product?.remark" class="detail-card__remark">
              <span class="detail-card__remark-label">备注：</span>
              <span>{{ product.remark }}</span>
            </div>
          </section>

          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">制造属性</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>产品类型</label>
                <div class="info-item__value">
                  <span class="status-pill status-pill--primary">{{ productTypeLabel }}</span>
                </div>
              </div>
              <div class="info-item">
                <label>生产方式</label>
                <div class="info-item__value">{{ produceTypeLabel }}</div>
              </div>
              <div class="info-item">
                <label>默认工艺路线</label>
                <div class="info-item__value">{{ routeName || '-' }}</div>
              </div>
              <div class="info-item">
                <label>质检</label>
                <div class="info-item__value">
                  <span class="status-pill" :class="boolToneClass(product?.qcEnable)">
                    {{ boolLabel(product?.qcEnable) }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <label>委外支持</label>
                <div class="info-item__value">
                  <span class="status-pill" :class="boolToneClass(product?.outsourceEnable)">
                    {{ boolLabel(product?.outsourceEnable, '支持', '不支持') }}
                  </span>
                </div>
              </div>
            </div>
          </section>

          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">质量与追溯</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>批次管理</label>
                <div class="info-item__value">
                  <span class="status-pill" :class="boolToneClass(product?.batchControlFlag)">
                    {{ boolLabel(product?.batchControlFlag) }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <label>来料检验</label>
                <div class="info-item__value">
                  <span class="status-pill" :class="boolToneClass(product?.inspectionRequiredFlag)">
                    {{ boolLabel(product?.inspectionRequiredFlag) }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <label>保质期（天）</label>
                <div class="info-item__value info-item__value--mono">
                  {{ product?.expiryDay == null ? '-' : product.expiryDay }}
                </div>
              </div>
            </div>
          </section>

          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">财务信息</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>采购价格</label>
                <div class="info-item__value info-item__value--mono">
                  {{ formatCurrency(product?.purchasePrice) }}
                </div>
              </div>
              <div class="info-item">
                <label>销售价格</label>
                <div class="info-item__value info-item__value--mono info-item__value--accent">
                  {{ formatCurrency(product?.salePrice) }}
                </div>
              </div>
              <div class="info-item">
                <label>最低价格</label>
                <div class="info-item__value info-item__value--mono">
                  {{ formatCurrency(product?.minPrice) }}
                </div>
              </div>
            </div>
          </section>

          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">BOM 信息</div>
              <el-button link type="primary" :disabled="bomLoading" @click="loadBoms">刷新 BOM</el-button>
            </div>

            <div v-if="bomLoading && !boms.length" class="bom-loading">
              <el-skeleton animated :rows="3" />
            </div>

            <div v-else-if="bomLoadFailed && !boms.length" class="bom-state bom-state--error">
              <div class="drawer-state__icon drawer-state__icon--small">
                <Icon icon="ep:warning-filled" />
              </div>
              <div class="bom-state__text">BOM 信息加载失败</div>
              <el-button type="primary" plain size="small" :disabled="bomLoading" @click="loadBoms">
                重新加载
              </el-button>
            </div>

            <div v-else-if="!hasBom" class="bom-state">
              <div class="drawer-state__icon drawer-state__icon--small">
                <Icon icon="ep:box" />
              </div>
              <div class="bom-state__text">暂无 BOM</div>
            </div>

            <div v-else class="bom-list">
              <div
                v-for="bom in boms"
                :key="bom.id"
                class="bom-item"
                :class="{ 'bom-item--expanded': expandedBomId === bom.id }"
              >
                <button type="button" class="bom-item__head" @click="toggleBomExpand(bom)">
                  <div class="bom-item__identity">
                    <div class="bom-item__code">{{ bom.bomCode || '-' }}</div>
                    <div class="bom-item__version">{{ bom.version ? `V${bom.version}` : '-' }}</div>
                  </div>
                  <span
                    class="status-pill"
                    :class="`status-pill--${bomStatusMeta(bom.status)?.tone || 'slate'}`"
                  >
                    {{ bomStatusMeta(bom.status)?.label || '-' }}
                  </span>
                  <div class="bom-item__meta">
                    <span class="bom-item__count">明细 {{ bom.items?.length ?? '-' }} 行</span>
                    <span class="bom-item__time">{{ formatDateTime(bom.createTime) }}</span>
                    <Icon
                      class="bom-item__arrow"
                      :icon="expandedBomId === bom.id ? 'ep:arrow-up' : 'ep:arrow-down'"
                    />
                  </div>
                </button>
                <div v-if="expandedBomId === bom.id" class="bom-item__detail">
                  <div v-if="!bom.items?.length" class="bom-state bom-state--inline">
                    <div class="bom-state__text">暂无 BOM 明细</div>
                  </div>
                  <table v-else class="bom-table">
                    <thead>
                      <tr>
                        <th>物料</th>
                        <th class="text-right">用量</th>
                        <th class="text-right">损耗率</th>
                        <th>位号/位置</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="item in bom.items" :key="item.id">
                        <td>
                          <div class="bom-table__material">
                            <div class="bom-table__name">{{ item.materialName || '-' }}</div>
                            <div class="bom-table__sub">{{ item.referenceDesignator || '' }}</div>
                          </div>
                        </td>
                        <td class="text-right mono-cell">
                          {{ formatQty(item.usageQty) }}
                          <span class="mono-cell__unit">{{ item.unitName || '' }}</span>
                        </td>
                        <td class="text-right mono-cell">{{ formatLossRate(item.lossRate) }}</td>
                        <td>{{ item.position || '-' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>

          <section class="detail-card">
            <div class="detail-card__header">
              <div class="section-title">审核信息</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <label>审核状态</label>
                <div class="info-item__value">
                  <span
                    v-if="auditStatusMeta"
                    class="status-pill"
                    :class="`status-pill--${auditStatusMeta.tone}`"
                  >
                    {{ auditStatusMeta.label }}
                  </span>
                  <template v-else>-</template>
                </div>
              </div>
              <div class="info-item">
                <label>流程实例编号</label>
                <div class="info-item__value info-item__value--mono">
                  {{ product?.processInstanceId || '-' }}
                </div>
              </div>
            </div>
          </section>
        </template>
      </div>

      <div class="product-drawer-footer">
        <el-button @click="close">关闭</el-button>
        <div class="product-drawer-footer__actions">
          <el-button
            v-if="canCancelTwoStage"
            type="warning"
            plain
            :loading="cancelingTwoStage"
            :disabled="actionBusy"
            @click="handleCancelTwoStage"
          >
            撤回两段式审批
          </el-button>
          <el-button
            v-if="canStartObsolete"
            type="danger"
            plain
            :loading="obsoleteRequesting"
            :disabled="actionBusy"
            @click="handleStartObsolete"
          >
            发起废除
          </el-button>
          <el-button
            v-if="canSubmitChangeConfirm"
            type="warning"
            plain
            :loading="changeConfirming"
            :disabled="actionBusy"
            @click="handleSubmitChangeConfirm"
          >
            提交审批
          </el-button>
          <el-button
            v-if="canStartChange"
            type="warning"
            plain
            :loading="changeRequesting"
            :disabled="actionBusy"
            @click="handleStartChange"
          >
            发起变更
          </el-button>
          <el-button
            v-if="canCancelAudit"
            type="warning"
            plain
            :loading="cancelingAudit"
            :disabled="actionBusy"
            @click="handleCancelAudit"
          >
            撤回审核
          </el-button>
          <el-button
            v-if="canSubmitAudit"
            type="warning"
            :loading="submittingAudit"
            :disabled="actionBusy"
            @click="handleSubmitAudit"
          >
            提交审核
          </el-button>
          <el-button
            v-if="product?.processInstanceId"
            plain
            :disabled="actionBusy"
            @click="handleOpenProcessDetail"
          >
            查看审批
          </el-button>
          <el-button v-if="canEdit" type="primary" :disabled="actionBusy" @click="handleEdit">
            编辑
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWindowSize } from '@vueuse/core'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'
import { useMessage } from '@/hooks/web/useMessage'
import { CommonStatusEnum } from '@/utils/constants'
import { formatMaterialCode } from '@/utils/erp/materialCode'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import { RdBomApi, type RdBomVO } from '@/api/erp/rd/bom'
import { RouteApi, type RouteVO } from '@/api/erp/route'

defineOptions({ name: 'ProductDetailDrawer' })

const emit = defineEmits<{
  (e: 'refresh'): void
  (e: 'edit', id: number): void
}>()

const router = useRouter()
const message = useMessage()
const { width } = useWindowSize()

const AUDIT_STATUS_DRAFT = 0
const AUDIT_STATUS_PROCESS = 10
const AUDIT_STATUS_APPROVE = 20
const AUDIT_STATUS_CR_PENDING = 21
const AUDIT_STATUS_EDITING = 22
const AUDIT_STATUS_CONFIRM_PENDING = 23
const AUDIT_STATUS_OBSOLETE_CR_PENDING = 24
const AUDIT_STATUS_OBSOLETED = 27
const AUDIT_STATUS_STOP_PENDING = 28
const AUDIT_STATUS_FAILED = 60

const SUBMIT_SETTLE_RETRY_MAX = 2
const SUBMIT_SETTLE_RETRY_INTERVAL_MS = 500

/** 两段式审批流状态（任一审批中，禁止流转/修改） */
const TWO_STAGE_PENDING_STATUSES = [
  AUDIT_STATUS_CR_PENDING,
  AUDIT_STATUS_CONFIRM_PENDING,
  AUDIT_STATUS_OBSOLETE_CR_PENDING,
  AUDIT_STATUS_STOP_PENDING
]

interface StatusMeta {
  label: string
  tone: 'slate' | 'primary' | 'success' | 'warning' | 'danger'
}

const AUDIT_STATUS_META: Record<number, StatusMeta> = {
  0: { label: '草稿', tone: 'slate' },
  10: { label: '审批中', tone: 'warning' },
  20: { label: '已审批', tone: 'success' },
  21: { label: '变更申请审批中', tone: 'warning' },
  22: { label: '变更编辑中', tone: 'primary' },
  23: { label: '变更确认审批中', tone: 'warning' },
  24: { label: '废除审批中', tone: 'warning' },
  27: { label: '已废除', tone: 'danger' },
  28: { label: '启停审批中', tone: 'warning' },
  30: { label: '已驳回', tone: 'danger' },
  40: { label: '已结转', tone: 'slate' },
  50: { label: '已作废', tone: 'danger' },
  60: { label: '流程失败', tone: 'danger' }
}

const BOM_STATUS_META: Record<number, StatusMeta> = {
  0: { label: '草稿', tone: 'slate' },
  10: { label: '审批中', tone: 'warning' },
  20: { label: '已审批', tone: 'success' },
  30: { label: '已驳回', tone: 'danger' },
  50: { label: '已作废', tone: 'danger' },
  60: { label: '处理失败', tone: 'danger' }
}

const detailDrawerVisible = ref(false)
const loadingDetail = ref(false)
const detailLoadFailed = ref(false)
const detailMissing = ref(false)
const bomLoading = ref(false)
const bomLoadFailed = ref(false)
const submittingAudit = ref(false)
const cancelingAudit = ref(false)
const changeRequesting = ref(false)
const changeConfirming = ref(false)
const obsoleteRequesting = ref(false)
const cancelingTwoStage = ref(false)

const selectedProductId = ref<number>()
const product = ref<ProductVO | null>(null)
const boms = ref<RdBomVO[]>([])
const routeList = ref<RouteVO[]>([])
const expandedBomId = ref<number>()

const drawerSize = computed(() => {
  const viewportWidth = width.value || 1600
  return viewportWidth < 1280 ? Math.round(viewportWidth * 0.9) : 720
})

const isApprovalRunning = computed(
  () => product.value?.auditStatus === AUDIT_STATUS_PROCESS && !!product.value?.processInstanceId
)
/** 两段式任一审批中（禁止流转阶段、禁止修改） */
const isTwoStagePending = computed(() =>
  TWO_STAGE_PENDING_STATUSES.includes(product.value?.auditStatus ?? -1)
)
/** 两段式变更：已获编辑权限（EDITING）可编辑暂存 */
const isChangeEditing = computed(() => product.value?.auditStatus === AUDIT_STATUS_EDITING)
const isObsoleted = computed(() => product.value?.auditStatus === AUDIT_STATUS_OBSOLETED)

const canEdit = computed(
  () =>
    checkPermi(['erp:product:update']) &&
    [AUDIT_STATUS_DRAFT, 30, AUDIT_STATUS_FAILED, AUDIT_STATUS_EDITING].includes(
      product.value?.auditStatus ?? -1
    )
)
const canStartChange = computed(
  () => checkPermi(['erp:product:update']) && product.value?.auditStatus === AUDIT_STATUS_APPROVE
)
const canStartObsolete = computed(
  () => checkPermi(['erp:product:update']) && product.value?.auditStatus === AUDIT_STATUS_APPROVE
)
const canSubmitChangeConfirm = computed(
  () => checkPermi(['erp:product:update']) && isChangeEditing.value
)
const canCancelTwoStage = computed(
  () =>
    checkPermi(['erp:product:cancel']) &&
    isTwoStagePending.value &&
    !!product.value?.processInstanceId
)
const canSubmitAudit = computed(
  () =>
    checkPermi(['erp:product:submit']) &&
    [AUDIT_STATUS_DRAFT, 30, AUDIT_STATUS_FAILED].includes(product.value?.auditStatus ?? 0) &&
    !product.value?.processInstanceId
)
const canCancelAudit = computed(() => checkPermi(['erp:product:cancel']) && isApprovalRunning.value)
const actionBusy = computed(
  () =>
    loadingDetail.value ||
    submittingAudit.value ||
    cancelingAudit.value ||
    changeRequesting.value ||
    changeConfirming.value ||
    obsoleteRequesting.value ||
    cancelingTwoStage.value
)
const hasBom = computed(() => boms.value.length > 0)

const auditStatusMeta = computed(() =>
  product.value?.auditStatus == null ? undefined : AUDIT_STATUS_META[product.value.auditStatus]
)

const productTypeLabel = computed(() =>
  resolveDictLabel(DICT_TYPE.ERP_PRODUCT_TYPE, product.value?.productType)
)
const produceTypeLabel = computed(() =>
  resolveDictLabel(DICT_TYPE.ERP_PRODUCE_TYPE, product.value?.produceType)
)
const routeName = computed(() => {
  const routeId = product.value?.defaultRouteId
  if (!routeId) {
    return ''
  }
  return routeList.value.find((route) => route.id === routeId)?.name || `工艺路线 #${routeId}`
})

const resolveDictLabel = (dictType: DICT_TYPE, value?: number | null) => {
  if (value == null) {
    return '-'
  }
  const dict = getIntDictOptions(dictType).find((item) => Number(item.value) === Number(value))
  return dict?.label || String(value)
}

const bomStatusMeta = (status?: number) => (status == null ? undefined : BOM_STATUS_META[status])

const currencyFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const formatCurrency = (value?: number | null) =>
  value == null ? '-' : currencyFormatter.format(Number(value))

const formatWeight = (value?: number | null) => {
  if (value == null) {
    return '-'
  }
  const num = Number(value)
  if (Number.isInteger(num)) {
    return num.toLocaleString('zh-CN')
  }
  return String(parseFloat(num.toFixed(3)))
}

const formatQty = (value?: number | null) => {
  if (value == null) {
    return '-'
  }
  const num = Number(value)
  if (Number.isInteger(num)) {
    return num.toLocaleString('zh-CN')
  }
  return String(parseFloat(num.toFixed(4)))
}

const formatLossRate = (value?: number | null) => {
  if (value == null) {
    return '-'
  }
  const num = Number(value)
  return `${parseFloat(num.toFixed(2))}%`
}

const formatDateTime = (value?: Date | string | number) =>
  value ? formatDate(value as Date, 'YYYY-MM-DD HH:mm:ss') : '-'

const boolToneClass = (value?: boolean | null) => (value ? 'status-pill--success' : 'status-pill--slate')

const boolLabel = (
  value?: boolean | null,
  trueText = '启用',
  falseText = '未启用'
) => {
  if (value == null) {
    return '-'
  }
  return value ? trueText : falseText
}

const open = async (row: Pick<ProductVO, 'id'> & Partial<ProductVO>) => {
  if (!row?.id) {
    return
  }
  resetDetailState()
  selectedProductId.value = row.id
  product.value = row as ProductVO
  detailDrawerVisible.value = true
  await Promise.all([loadProduct(), loadBoms()])
}

const close = () => {
  detailDrawerVisible.value = false
}

const retryLoad = async () => {
  await loadProduct()
}

const loadProduct = async (silent = false) => {
  if (!selectedProductId.value) {
    detailMissing.value = true
    return
  }
  if (!silent) {
    loadingDetail.value = true
  }
  detailLoadFailed.value = false
  detailMissing.value = false
  try {
    const [productResult, routeResult] = await Promise.allSettled([
      ProductApi.getProduct(selectedProductId.value),
      RouteApi.getRouteSimpleList()
    ])
    routeList.value = routeResult.status === 'fulfilled' ? routeResult.value || [] : []
    if (productResult.status === 'rejected') {
      detailLoadFailed.value = true
      return
    }
    if (!productResult.value) {
      product.value = null
      detailMissing.value = true
      return
    }
    product.value = productResult.value
  } finally {
    if (!silent) {
      loadingDetail.value = false
    }
  }
}

const loadBoms = async () => {
  if (!selectedProductId.value) {
    return
  }
  bomLoading.value = true
  bomLoadFailed.value = false
  try {
    const pageData = await RdBomApi.getRdBomPage({
      productId: selectedProductId.value,
      pageNo: 1,
      pageSize: 10
    })
    let list: RdBomVO[] = pageData?.list || []
    const missingItemEntries = list.filter((bom) => !Array.isArray(bom.items))
    if (missingItemEntries.length) {
      const details = await Promise.allSettled(
        missingItemEntries.map((bom) => RdBomApi.getRdBom(bom.id!))
      )
      const detailById = new Map<number, RdBomVO>()
      details.forEach((result, index) => {
        const id = missingItemEntries[index].id
        if (id != null && result.status === 'fulfilled' && result.value) {
          detailById.set(id, result.value)
        }
      })
      list = list.map((bom) => {
        if (Array.isArray(bom.items)) {
          return bom
        }
        const detail = bom.id != null ? detailById.get(bom.id) : undefined
        return { ...bom, items: detail?.items ?? [] }
      })
    }
    boms.value = list
  } catch {
    bomLoadFailed.value = true
    boms.value = []
  } finally {
    bomLoading.value = false
  }
}

const toggleBomExpand = (bom: RdBomVO) => {
  expandedBomId.value = expandedBomId.value === bom.id ? undefined : bom.id
}

const reloadAfterAction = async () => {
  await loadProduct()
  emit('refresh')
}

const isAuditStateSettled = (latest?: ProductVO | null) =>
  !!latest &&
  ((latest.auditStatus === AUDIT_STATUS_PROCESS && !!latest.processInstanceId) ||
    latest.auditStatus === AUDIT_STATUS_FAILED)

const isAuditIntermediate = (latest?: ProductVO | null) =>
  !!latest &&
  latest.auditStatus === AUDIT_STATUS_PROCESS &&
  !latest.processInstanceId

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const refreshUntilAuditSettled = async () => {
  await loadProduct(true)
  for (
    let attempt = 0;
    attempt < SUBMIT_SETTLE_RETRY_MAX && isAuditIntermediate(product.value);
    attempt++
  ) {
    await wait(SUBMIT_SETTLE_RETRY_INTERVAL_MS)
    await loadProduct(true)
  }
}

const handleSubmitAudit = async () => {
  const current = product.value
  if (!current?.id || submittingAudit.value) {
    return
  }
  try {
    await message.confirm('确认提交审核吗？')
  } catch {
    return
  }
  submittingAudit.value = true
  try {
    await ProductApi.submitProduct(current.id)
    await refreshUntilAuditSettled()
    emit('refresh')
    const latest = product.value
    if (isAuditStateSettled(latest)) {
      if (latest!.auditStatus === AUDIT_STATUS_FAILED) {
        message.error('提交已受理但流程创建失败，请重试')
      } else {
        message.success('已提交审核，等待流程受理')
      }
    } else {
      message.info('提交请求已发送，可稍后刷新查看最新状态')
    }
  } catch {
  } finally {
    submittingAudit.value = false
  }
}

const handleCancelAudit = async () => {
  const current = product.value
  if (!current?.id || cancelingAudit.value) {
    return
  }
  try {
    const batchTip = current.pendingBatchId
      ? `该物料属于批量审批批次（共 ${current.pendingBatchSize ?? '?'} 条），撤回将作废整批变更。`
      : ''
    await message.confirm(`确认撤回该物料的审核吗？${batchTip}`)
  } catch {
    return
  }
  cancelingAudit.value = true
  try {
    await ProductApi.cancelProduct(current.id)
    await reloadAfterAction()
    message.success('撤回成功')
  } catch {
  } finally {
    cancelingAudit.value = false
  }
}

const handleEdit = () => {
  if (!canEdit.value || !product.value?.id) {
    return
  }
  detailDrawerVisible.value = false
  emit('edit', product.value.id)
}

// ========== 两段式变更/废除 ==========

const handleStartChange = async () => {
  const current = product.value
  if (!current?.id || changeRequesting.value) {
    return
  }
  try {
    await message.confirm('确认对该物料发起变更申请吗？')
  } catch {
    return
  }
  changeRequesting.value = true
  try {
    await ProductApi.submitChangeRequest(current.id)
    await reloadAfterAction()
    message.success('变更申请已提交，等待审批')
  } catch {
  } finally {
    changeRequesting.value = false
  }
}

const handleSubmitChangeConfirm = async () => {
  const current = product.value
  if (!current?.id || changeConfirming.value) {
    return
  }
  try {
    await message.confirm('确认提交变更审批吗？')
  } catch {
    return
  }
  changeConfirming.value = true
  try {
    await ProductApi.submitChangeConfirm(current.id)
    await reloadAfterAction()
    message.success('变更审批已提交，等待负责人审批')
  } catch {
  } finally {
    changeConfirming.value = false
  }
}

const handleStartObsolete = async () => {
  const current = product.value
  if (!current?.id || obsoleteRequesting.value) {
    return
  }
  try {
    // 废除原因是最终留痕，弹窗强制填写
    const { value } = await message.prompt('确认对该物料发起废除申请吗？', '填写废除原因')
    if (!value || !String(value).trim()) {
      message.warning('请填写废除原因')
      return
    }
    obsoleteRequesting.value = true
    try {
      await ProductApi.submitObsoleteRequest(current.id, String(value).trim())
      await reloadAfterAction()
      message.success('废除申请已提交，等待审批')
    } catch {
    } finally {
      obsoleteRequesting.value = false
    }
  } catch {}
}

const handleCancelTwoStage = async () => {
  const current = product.value
  if (!current?.id || cancelingTwoStage.value) {
    return
  }
  try {
    await message.confirm('确认撤回该物料的审批吗？')
  } catch {
    return
  }
  cancelingTwoStage.value = true
  try {
    await ProductApi.cancelTwoStageApproval(current.id)
    await reloadAfterAction()
    message.success('撤回成功')
  } catch {
  } finally {
    cancelingTwoStage.value = false
  }
}

const handleOpenProcessDetail = async () => {
  if (!product.value?.processInstanceId) {
    return
  }
  detailDrawerVisible.value = false
  await router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: product.value.processInstanceId }
  })
}

const resetDetailState = () => {
  selectedProductId.value = undefined
  product.value = null
  boms.value = []
  routeList.value = []
  expandedBomId.value = undefined
  loadingDetail.value = false
  detailLoadFailed.value = false
  detailMissing.value = false
  bomLoading.value = false
  bomLoadFailed.value = false
  submittingAudit.value = false
  cancelingAudit.value = false
}

defineExpose({
  open,
  close
})
</script>

<style scoped lang="scss">
.product-drawer-shell {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: var(--erp-slate-50);
}

.product-drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--erp-slate-900);
  color: var(--erp-surface-white);
}

.product-drawer-head__main {
  flex: 1;
  min-width: 0;
}

.product-drawer-head__title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-top: 0;
}

.product-drawer-head__title {
  margin: 0;
  overflow: hidden;
  color: var(--erp-surface-white);
  font-size: 22px;
  font-weight: 700;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-drawer-head__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
  color: rgb(226 232 240 / 82%);
  font-size: 12px;
}

.product-drawer-head__mono {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.drawer-close-button {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid rgb(255 255 255 / 18%);
  border-radius: 10px;
  color: #fff;
  background: rgb(255 255 255 / 8%);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgb(255 255 255 / 16%);
  }
}

.product-drawer-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--erp-slate-100);
  background: var(--erp-surface-white);
}

.summary-metric {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--erp-slate-100);
  border-radius: 12px;
  background: var(--erp-slate-50);
}

.summary-metric__label {
  color: var(--erp-slate-500);
  font-size: 12px;
  font-weight: 600;
}

.summary-metric__value {
  display: flex;
  align-items: center;
  min-height: 24px;
  margin-top: 6px;
  overflow: hidden;
  color: var(--erp-slate-900);
  font-size: 15px;
  font-weight: 700;
}

.product-drawer-body {
  flex: 1;
  min-height: 0;
  padding: 16px 20px 18px;
  overflow-y: auto;
}

.detail-card {
  overflow: hidden;
  border: 1px solid var(--erp-slate-200);
  border-radius: 12px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);

  & + .detail-card {
    margin-top: 14px;
  }
}

.detail-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px 0;
}

.section-title {
  color: var(--erp-slate-900);
  font-size: 15px;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
  padding: 16px 20px 18px;
}

.info-item {
  min-width: 0;

  label {
    display: block;
    margin-bottom: 6px;
    color: var(--erp-slate-400);
    font-size: 12px;
  }
}

.info-item__value {
  color: var(--erp-slate-800);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-all;
}

.info-item__value--mono,
.mono-cell {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.info-item__value--accent {
  color: var(--erp-teal-600);
}

.detail-card__remark {
  padding: 14px 20px 16px;
  border-top: 1px solid var(--erp-slate-100);
  color: var(--erp-slate-500);
  font-size: 13px;
  line-height: 1.7;
}

.detail-card__remark-label {
  margin-right: 8px;
  color: var(--erp-slate-400);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  min-height: 24px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill--primary {
  border-color: var(--erp-primary-100);
  color: var(--erp-primary-600);
  background: var(--erp-primary-50);
}

.status-pill--success {
  border-color: var(--erp-success-100);
  color: var(--erp-success-600);
  background: var(--erp-success-50);
}

.status-pill--warning {
  border-color: var(--erp-warning-100);
  color: var(--erp-warning-600);
  background: var(--erp-warning-50);
}

.status-pill--danger {
  border-color: var(--erp-danger-100);
  color: var(--erp-danger-600);
  background: var(--erp-danger-50);
}

.status-pill--slate {
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-600);
  background: var(--erp-slate-50);
}

.bom-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 20px 18px;
}

.bom-item {
  overflow: hidden;
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: var(--erp-primary-200);
  }

  &--expanded {
    border-color: var(--erp-primary-200);
  }
}

.bom-item__head {
  display: flex;
  flex: 1;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 14px;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.bom-item__identity {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: baseline;
  gap: 8px;
}

.bom-item__code {
  overflow: hidden;
  color: var(--erp-slate-900);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bom-item__version {
  flex-shrink: 0;
  color: var(--erp-slate-500);
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
}

.bom-item__meta {
  display: flex;
  flex-shrink: 0;
  gap: 10px;
  align-items: center;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.bom-item__count {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.bom-item__time {
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.bom-item__arrow {
  color: var(--erp-slate-400);
  transition: transform 0.2s ease;
}

.bom-item__detail {
  padding: 0 14px 14px;
  border-top: 1px solid var(--erp-slate-100);
}

.bom-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 6px;

  th {
    height: 38px;
    padding: 0 10px;
    background: var(--erp-slate-50);
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    text-align: left;
  }

  td {
    padding: 10px;
    border-bottom: 1px solid var(--erp-slate-100);
    color: var(--erp-slate-700);
    font-size: 13px;
  }

  tbody tr:last-child td {
    border-bottom: none;
  }
}

.text-right {
  text-align: right !important;
}

.mono-cell {
  color: var(--erp-slate-700);
  font-size: 13px;
}

.mono-cell__unit {
  margin-left: 4px;
  color: var(--erp-slate-400);
  font-size: 11px;
}

.bom-table__material {
  min-width: 0;
}

.bom-table__name {
  overflow: hidden;
  color: var(--erp-slate-900);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bom-table__sub {
  margin-top: 2px;
  color: var(--erp-slate-400);
  font-size: 11px;
}

.bom-loading {
  padding: 14px 20px 18px;
}

.bom-state {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 24px 20px;
}

.bom-state--inline {
  flex-direction: row;
  justify-content: space-between;
  padding: 12px 0 2px;
}

.bom-state--error .drawer-state__icon--small {
  background: var(--erp-danger-50);
  color: var(--erp-danger-600);
}

.bom-state__text {
  color: var(--erp-slate-500);
  font-size: 13px;
}

.drawer-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  padding: 24px;
  text-align: center;
}

.drawer-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  color: var(--erp-primary-600);
  background: var(--erp-primary-50);
  font-size: 24px;
}

.drawer-state__icon--small {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 17px;
}

.drawer-state--error .drawer-state__icon {
  color: var(--erp-danger-600);
  background: var(--erp-danger-50);
}

.drawer-state__title {
  color: var(--erp-slate-900);
  font-size: 16px;
  font-weight: 700;
}

.product-drawer-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px 16px;
  border-top: 1px solid var(--erp-slate-100);
  background: var(--erp-surface-white);
}

.product-drawer-footer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

@media (width <= 1024px) {
  .product-drawer-summary,
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .product-drawer-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .product-drawer-footer__actions {
    justify-content: stretch;
    width: 100%;

    :deep(.el-button) {
      flex: 1;
      margin-left: 0;
    }
  }
}

@media (width <= 640px) {
  .product-drawer-summary {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .bom-item__head {
    flex-wrap: wrap;
  }

  .bom-item__meta {
    width: 100%;
    justify-content: space-between;
  }
}
</style>

<style lang="scss">
.product-detail-drawer__mask {
  backdrop-filter: blur(4px);
}

.product-detail-drawer__mask .el-drawer__body {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}
</style>
