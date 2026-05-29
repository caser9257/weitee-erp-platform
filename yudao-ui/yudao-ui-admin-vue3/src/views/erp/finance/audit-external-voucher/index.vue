<template>
  <div class="finance-shell finance-shell__stack">
    <ContentWrap class="finance-shell__header-card">
      <div class="finance-shell__page-header">
        <div class="finance-shell__page-header-main">
          <div class="finance-shell__page-title">外部账凭证查询</div>
          <div class="finance-shell__page-metrics">
            <span class="finance-shell__metric-chip">凭证 {{ total }}</span>
          </div>
        </div>
        <div class="finance-shell__page-header-actions">
          <el-button plain :loading="loadingList" :disabled="loadingList" @click="handleRefresh">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
          <el-button type="success" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" />
            导出
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="finance-shell__filter-card">
      <div class="finance-shell__section-head">
        <div class="finance-shell__section-title">筛选条件</div>
      </div>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-shell__query-form" @submit.prevent>
        <div class="finance-shell__query-grid finance-shell__query-grid--wide">
          <el-form-item label="业务类型" prop="bizType">
            <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable class="!w-full">
              <el-option v-for="item in bizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="凭证号" prop="voucherNo">
            <el-input v-model="queryParams.voucherNo" placeholder="请输入凭证号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="业务单号" prop="bizNo">
            <el-input v-model="queryParams.bizNo" placeholder="请输入业务单号" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="凭证状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择凭证状态" clearable class="!w-full">
              <el-option label="已生成" :value="10" />
              <el-option label="已审核" :value="20" />
              <el-option label="已过账" :value="30" />
            </el-select>
          </el-form-item>
        </div>

        <div class="finance-shell__query-actions">
          <el-button type="primary" :loading="loadingList" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="finance-shell__table-card">
      <div class="finance-shell__toolbar">
        <div class="finance-shell__table-toolbar-main">
          <div class="finance-shell__section-title">外部账凭证列表</div>
          <div class="finance-shell__toolbar-count">当前共 <strong>{{ total }}</strong> 条</div>
        </div>
      </div>

      <template v-if="loadingList || list.length">
        <div class="finance-shell__table-wrap">
          <el-table v-loading="loadingList" :data="list" row-key="id" stripe class="finance-shell__table finance-shell__table--dense" :show-overflow-tooltip="false">
            <el-table-column min-width="180">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  凭证信息
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.voucherNo || '-' }}</span>
                  <span class="finance-shell__muted-text">{{ row.bizTypeName || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="150">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:document" class="finance-shell__column-icon" />
                  业务单号
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__primary-text finance-shell__mono">{{ row.bizNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="120" align="right">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  借方金额
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--right">
                  <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.totalDebitAmount) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="120" align="right">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:money" class="finance-shell__column-icon" />
                  贷方金额
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--right">
                  <span class="finance-shell__amount finance-shell__mono">{{ formatAmount(row.totalCreditAmount) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="100" align="center">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:circle-check" class="finance-shell__column-icon" />
                  状态
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell finance-shell__primary-cell--center">
                  <el-tag :type="getStatusType(row.status)" effect="light">
                    {{ getStatusName(row.status) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="150">
              <template #header>
                <span class="finance-shell__column-header">
                  <Icon icon="ep:calendar" class="finance-shell__column-icon" />
                  凭证时间
                </span>
              </template>
              <template #default="{ row }">
                <div class="finance-shell__primary-cell">
                  <span class="finance-shell__muted-text">{{ row.voucherTime || '-' }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <Pagination v-if="total > 0" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </template>
      <el-empty v-else description="暂无外部账凭证数据" />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ErpBizType } from '@/utils/constants'
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import { ElMessageBox } from 'element-plus'

defineOptions({ name: 'ErpFinanceAuditExternalVoucher' })

const message = useMessage()

const loadingList = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  ledgerId: 1, // 外部账簿ID，需要根据实际配置调整
  bizType: undefined,
  voucherNo: undefined,
  bizNo: undefined,
  status: undefined
})

const bizTypeOptions = [
  { label: '采购入库', value: ErpBizType.PURCHASE_IN },
  { label: '采购退货', value: ErpBizType.PURCHASE_RETURN },
  { label: '销售出库', value: ErpBizType.SALE_OUT },
  { label: '销售退货', value: ErpBizType.SALE_RETURN },
  { label: '委外入库', value: ErpBizType.OUTSOURCE_INBOUND },
  { label: '自制入库', value: ErpBizType.PRODUCTION_INBOUND },
  { label: '费用报销', value: ErpBizType.FINANCE_EXPENSE }
]

const formatAmount = (value?: number) =>
  value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const getStatusType = (status: number) => {
  if (status === 10) return 'info'
  if (status === 20) return 'success'
  if (status === 30) return 'warning'
  return 'info'
}

const getStatusName = (status: number) => {
  if (status === 10) return '已生成'
  if (status === 20) return '已审核'
  if (status === 30) return '已过账'
  return '未知'
}

const queryFormRef = ref()

const getList = async () => {
  loadingList.value = true
  try {
    // TODO: 调用实际的 API 获取外部账凭证列表
    // const data = await VoucherApi.getVoucherPage(queryParams)
    // list.value = data.list || []
    // total.value = data.total || 0
    list.value = []
    total.value = 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loadingList.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleRefresh = () => {
  getList()
}

const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确认导出外部账凭证数据？', '导出确认', {
      type: 'info'
    })
    // TODO: 调用导出 API
    message.success('导出成功')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  getList()
})
</script>
