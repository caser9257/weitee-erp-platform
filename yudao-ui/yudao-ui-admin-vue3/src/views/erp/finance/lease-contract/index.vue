<template>
  <div class="lease-contract-page">
    <!-- 页头 -->
    <ContentWrap class="page-header-card">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">租赁合同管理</div>
          <div class="page-header__desc">管理仪器设备租赁合同、租金排程</div>
        </div>
        <div class="page-header__actions">
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus" class="mr-5px" /> 新增合同
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
          <el-form-item label="合同编号">
            <el-input v-model="queryParams.no" placeholder="请输入合同编号" clearable @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="合同名称">
            <el-input v-model="queryParams.name" placeholder="请输入合同名称" clearable @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option label="草稿" :value="0" />
              <el-option label="生效" :value="10" />
              <el-option label="到期" :value="20" />
              <el-option label="终止" :value="30" />
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
        <el-table-column label="合同信息" min-width="200">
          <template #default="{ row }">
            <div>
              <div class="font-semibold">{{ row.name }}</div>
              <div class="text-slate-400 text-xs font-mono">{{ row.no }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="供应商" min-width="150" prop="supplierName" />
        <el-table-column label="租赁期间" min-width="180">
          <template #default="{ row }">
            <div>{{ row.startDate }} ~ {{ row.endDate }}</div>
          </template>
        </el-table-column>
        <el-table-column label="月租金" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono font-semibold">{{ formatMoney(row.monthlyRent) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="合同总额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-mono">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
            <el-form-item label="合同编号" prop="no">
              <el-input v-model="formData.no" placeholder="请输入合同编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入合同名称" />
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
            <el-form-item label="月租金" prop="monthlyRent">
              <el-input-number v-model="formData.monthlyRent" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同总额" prop="totalAmount">
              <el-input-number v-model="formData.totalAmount" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款周期">
              <el-input-number v-model="formData.paymentCycle" :min="1" class="!w-full" />
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

defineOptions({ name: 'ErpLeaseContract' })

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const supplierList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  name: undefined,
  status: undefined
})

const formData = reactive({
  id: undefined,
  no: '',
  name: '',
  supplierId: undefined,
  startDate: '',
  endDate: '',
  monthlyRent: 0,
  totalAmount: 0,
  paymentCycle: 1,
  costCenterId: undefined,
  remark: ''
})

const formRules = {
  no: [{ required: true, message: '合同编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '合同名称不能为空', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  monthlyRent: [{ required: true, message: '请输入月租金', trigger: 'blur' }]
}

const formatMoney = (value?: number | string | null) => {
  const n = Number(value || 0)
  return `¥${n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 10: '生效', 20: '到期', 30: '终止' }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 10: 'success', 20: 'warning', 30: 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request.get({ url: '/erp/lease-contract/page', params: queryParams })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.no = undefined
  queryParams.name = undefined
  queryParams.status = undefined
  loadData()
}

const handleCreate = () => {
  dialogTitle.value = '新增租赁合同'
  Object.assign(formData, {
    id: undefined, no: '', name: '', supplierId: undefined,
    startDate: '', endDate: '', monthlyRent: 0, totalAmount: 0,
    paymentCycle: 1, costCenterId: undefined, remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑租赁合同'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await request.put({ url: '/erp/lease-contract/update', data: formData })
    } else {
      await request.post({ url: '/erp/lease-contract/create', data: formData })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除该租赁合同？', '提示', { type: 'warning' })
  await request.delete({ url: '/erp/lease-contract/delete', params: { id: row.id } })
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.lease-contract-page {
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
