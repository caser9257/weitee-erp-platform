<template>
  <div class="service-receipt-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">服务接收单</div>
          <div class="page-header__desc">供应链确认仪器租赁服务完成，关联生产/研发成本中心</div>
        </div>
        <div class="page-header__actions">
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus" class="mr-5px" /> 新增接收单
          </el-button>
          <el-button :loading="loading" @click="loadData">
            <Icon icon="ep:refresh" class="mr-5px" /> 刷新
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 筛选区 -->
    <ContentWrap class="filter-card">
      <el-form :model="queryParams" label-width="88px" class="filter-form" @submit.prevent>
        <div class="filter-grid">
          <el-form-item label="单号">
            <el-input v-model="queryParams.no" placeholder="请输入单号" clearable @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="归属期间">
            <el-date-picker v-model="queryParams.period" type="month" value-format="YYYY-MM" placeholder="请选择期间" class="!w-full" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option label="草稿" :value="0" />
              <el-option label="已确认" :value="10" />
              <el-option label="已生成应付" :value="20" />
            </el-select>
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="loadData">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="接收单信息" min-width="200">
          <template #default="{ row }">
            <div>
              <div class="font-semibold">{{ row.no }}</div>
              <div class="text-slate-400 text-xs">{{ row.period }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="租赁合同" min-width="150" prop="leaseContractNo" />
        <el-table-column label="供应商" min-width="150" prop="supplierName" />
        <el-table-column label="接收日期" width="120" prop="receiptDate" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono font-semibold">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本中心" width="120" prop="costCenterName" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="handleConfirm(row)">确认</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="loadData" />
      </div>
    </ContentWrap>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单号" prop="no">
              <el-input v-model="formData.no" placeholder="请输入单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属期间" prop="period">
              <el-date-picker v-model="formData.period" type="month" value-format="YYYY-MM" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租赁合同">
              <el-select v-model="formData.leaseContractId" filterable placeholder="请选择合同" class="!w-full" @change="handleContractChange">
                <el-option v-for="item in contractList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="formData.supplierId" filterable placeholder="请选择供应商" class="!w-full">
                <el-option v-for="item in supplierList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接收日期" prop="receiptDate">
              <el-date-picker v-model="formData.receiptDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="formData.amount" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本中心">
              <el-select v-model="formData.costCenterId" filterable placeholder="请选择成本中心" class="!w-full">
                <el-option v-for="item in deptList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/config/axios'

defineOptions({ name: 'ErpServiceReceipt' })

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const contractList = ref<any[]>([])
const supplierList = ref<any[]>([])
const deptList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  period: undefined,
  status: undefined
})

const formData = reactive({
  id: undefined,
  no: '',
  leaseContractId: undefined,
  supplierId: undefined,
  receiptDate: '',
  period: '',
  amount: 0,
  costCenterId: undefined,
  remark: ''
})

const formRules = {
  no: [{ required: true, message: '单号不能为空', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  receiptDate: [{ required: true, message: '请选择接收日期', trigger: 'change' }],
  period: [{ required: true, message: '请选择归属期间', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 10: '已确认', 20: '已生成应付' }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 10: 'success', 20: 'primary' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request.get({ url: '/erp/service-receipt/page', params: queryParams })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.no = undefined
  queryParams.period = undefined
  queryParams.status = undefined
  loadData()
}

const handleCreate = () => {
  dialogTitle.value = '新增服务接收单'
  Object.assign(formData, {
    id: undefined, no: '', leaseContractId: undefined, supplierId: undefined,
    receiptDate: '', period: '', amount: 0, costCenterId: undefined, remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑服务接收单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleContractChange = (contractId: number) => {
  const contract = contractList.value.find(c => c.id === contractId)
  if (contract) {
    formData.supplierId = contract.supplierId
    formData.amount = contract.monthlyRent
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await request.put({ url: '/erp/service-receipt/update', data: formData })
    } else {
      await request.post({ url: '/erp/service-receipt/create', data: formData })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleConfirm = async (row: any) => {
  await ElMessageBox.confirm('确认该服务接收单？', '提示', { type: 'warning' })
  await request.put({ url: '/erp/service-receipt/confirm', params: { id: row.id } })
  ElMessage.success('确认成功')
  await loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除该服务接收单？', '提示', { type: 'warning' })
  await request.delete({ url: '/erp/service-receipt/delete', params: { id: row.id } })
  ElMessage.success('删除成功')
  await loadData()
}

const loadOptions = async () => {
  try {
    const [contracts, depts] = await Promise.all([
      request.get({ url: '/erp/lease-contract/list' }),
      request.get({ url: '/system/dept/simple-list' })
    ])
    contractList.value = contracts || []
    deptList.value = depts || []
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
.service-receipt-page {
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

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
