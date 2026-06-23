<template>
  <div class="three-way-match-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">三单匹配</div>
        </div>
        <div class="page-header__actions">
          <el-button type="primary" @click="handleMatch">
            <Icon icon="ep:connection" class="mr-5px" /> 执行匹配
          </el-button>
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 匹配统计 -->
    <div class="stats-grid">
      <ContentWrap class="stat-card">
        <div class="stat-card__content">
          <div class="stat-card__label">待匹配</div>
          <div class="stat-card__value">{{ pendingCount }}</div>
        </div>
      </ContentWrap>
      <ContentWrap class="stat-card">
        <div class="stat-card__content">
          <div class="stat-card__label">完全匹配</div>
          <div class="stat-card__value text-[var(--erp-success-600)]">{{ fullMatchCount }}</div>
        </div>
      </ContentWrap>
      <ContentWrap class="stat-card">
        <div class="stat-card__content">
          <div class="stat-card__label">部分匹配</div>
          <div class="stat-card__value text-[var(--erp-warning-600)]">{{ partialMatchCount }}</div>
        </div>
      </ContentWrap>
      <ContentWrap class="stat-card">
        <div class="stat-card__content">
          <div class="stat-card__label">不匹配</div>
          <div class="stat-card__value text-[var(--erp-danger-600)]">{{ mismatchCount }}</div>
        </div>
      </ContentWrap>
    </div>

    <!-- 匹配记录列表 -->
    <ContentWrap class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="合同编号" min-width="140" prop="leaseContractNo" />
        <el-table-column label="接收单编号" min-width="140" prop="serviceReceiptNo" />
        <el-table-column label="发票号" min-width="140" prop="invoiceNo" />
        <el-table-column label="合同金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.contractAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="接收单金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.receiptAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发票金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.invoiceAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="匹配结果" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="matchResultType(row.matchResult)" size="small">
              {{ matchResultLabel(row.matchResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="匹配说明" min-width="200" prop="matchRemark" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="handleConfirm(row)">确认</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <Icon icon="ep:connection" size="48" class="empty-state__icon" />
            <div class="empty-state__text">暂无三单匹配记录</div>
            <div class="empty-state__hint">点击"执行匹配"按钮开始三单匹配</div>
          </div>
        </template>
      </el-table>
    </ContentWrap>

    <!-- 匹配弹窗 -->
    <el-dialog v-model="matchDialogVisible" title="执行三单匹配" width="600px" destroy-on-close>
      <el-form ref="matchFormRef" :model="matchForm" :rules="matchFormRules" label-width="100px">
        <el-form-item label="租赁合同" prop="leaseContractId">
          <el-select v-model="matchForm.leaseContractId" filterable placeholder="请选择合同" class="!w-full" @change="handleContractChange">
            <el-option v-for="item in contractList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务接收单" prop="serviceReceiptId">
          <el-select v-model="matchForm.serviceReceiptId" filterable placeholder="请选择接收单" class="!w-full" @change="handleReceiptChange">
            <el-option v-for="item in filteredReceiptList" :key="item.id" :label="item.no" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="matchForm.invoiceNo" placeholder="请输入发票号" />
        </el-form-item>
        <el-form-item label="发票金额" prop="invoiceAmount">
          <el-input-number v-model="matchForm.invoiceAmount" :min="0" :precision="2" class="!w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="matchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="matchLoading" @click="handleSubmitMatch">执行匹配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/config/axios'
import { formatMoney } from '@/utils/formatMoney'

defineOptions({ name: 'ErpThreeWayMatch' })

const loading = ref(false)
const matchLoading = ref(false)
const list = ref<any[]>([])
const matchDialogVisible = ref(false)
const matchFormRef = ref()
const contractList = ref<any[]>([])
const receiptList = ref<any[]>([])

const matchForm = reactive({
  leaseContractId: undefined,
  serviceReceiptId: undefined,
  invoiceNo: '',
  invoiceAmount: 0
})

// 根据选中的合同过滤接收单
const filteredReceiptList = computed(() => {
  if (!matchForm.leaseContractId) return receiptList.value
  return receiptList.value.filter(r => r.leaseContractId === matchForm.leaseContractId)
})

// 选择合同后自动填充
const handleContractChange = (contractId: number) => {
  const contract = contractList.value.find(c => c.id === contractId)
  if (contract) {
    matchForm.invoiceAmount = contract.monthlyRent
  }
  // 清空接收单选择
  matchForm.serviceReceiptId = undefined
}

// 选择接收单后自动填充
const handleReceiptChange = (receiptId: number) => {
  const receipt = receiptList.value.find(r => r.id === receiptId)
  if (receipt) {
    matchForm.invoiceAmount = receipt.amount
  }
}

const matchFormRules = {
  leaseContractId: [{ required: true, message: '请选择合同', trigger: 'change' }],
  serviceReceiptId: [{ required: true, message: '请选择接收单', trigger: 'change' }],
  invoiceNo: [{ required: true, message: '请输入发票号', trigger: 'blur' }],
  invoiceAmount: [{ required: true, message: '请输入发票金额', trigger: 'blur' }]
}

// 统计
const pendingCount = computed(() => list.value.filter(r => r.status === 0).length)
const fullMatchCount = computed(() => list.value.filter(r => r.matchResult === 1).length)
const partialMatchCount = computed(() => list.value.filter(r => r.matchResult === 2).length)
const mismatchCount = computed(() => list.value.filter(r => r.matchResult === 0).length)

const matchResultLabel = (result: number) => {
  const map: Record<number, string> = { 0: '不匹配', 1: '完全匹配', 2: '部分匹配' }
  return map[result] || '未知'
}

const matchResultType = (result: number) => {
  const map: Record<number, string> = { 0: 'danger', 1: 'success', 2: 'warning' }
  return map[result] || 'info'
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '待匹配', 10: '已匹配', 20: '已生成应付' }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 10: 'success', 20: 'primary' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request.get({ url: '/erp/three-way-match/list' })
    list.value = data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleMatch = () => {
  Object.assign(matchForm, {
    leaseContractId: undefined,
    serviceReceiptId: undefined,
    invoiceNo: '',
    invoiceAmount: 0
  })
  matchDialogVisible.value = true
}

const handleSubmitMatch = async () => {
  await matchFormRef.value?.validate()
  matchLoading.value = true
  try {
    await request.post({
      url: '/erp/three-way-match/match',
      data: matchForm
    })
    ElMessage.success('匹配完成')
    matchDialogVisible.value = false
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '匹配失败')
  } finally {
    matchLoading.value = false
  }
}

const handleConfirm = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认匹配结果并生成应付台账？', '提示', { type: 'warning' })
    await request.put({ url: '/erp/three-way-match/confirm', params: { id: row.id } })
    ElMessage.success('确认成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '确认失败')
    }
  }
}

const loadOptions = async () => {
  try {
    const [contracts, receipts] = await Promise.all([
      request.get({ url: '/erp/lease-contract/list' }),
      request.get({ url: '/erp/service-receipt/list' })
    ])
    contractList.value = contracts || []
    receiptList.value = receipts || []
  } catch (e) {
    console.error('加载选项失败', e)
  }
}

onMounted(() => {
  loadData()
  loadOptions()
})
</script>

<style scoped lang="scss">
.three-way-match-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--erp-slate-50);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header__title {
  font-size: 20px;
  font-weight: 800;
  color: var(--erp-slate-900);
}

.page-header__desc {
  margin-top: 4px;
  color: var(--erp-slate-500);
  font-size: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  border: 1px solid var(--erp-slate-100);
}

.stat-card__content {
  padding: 16px;
  text-align: center;
}

.stat-card__label {
  font-size: 12px;
  color: var(--erp-slate-500);
  margin-bottom: 4px;
}

.stat-card__value {
  font-size: 24px;
  font-weight: 700;
  color: var(--erp-slate-900);
  font-family: 'Inter', 'SF Mono', monospace;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
}

.empty-state__icon {
  color: var(--erp-slate-300);
  margin-bottom: 16px;
}

.empty-state__text {
  font-size: 16px;
  color: var(--erp-slate-500);
  margin-bottom: 8px;
}

.empty-state__hint {
  font-size: 14px;
  color: var(--erp-slate-400);
}
</style>
