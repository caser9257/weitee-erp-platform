<template>
  <div class="finance-subject-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" label-width="88px" class="finance-subject-page__query-form">
        <div class="finance-subject-page__query-grid">
          <el-form-item label="账簿" prop="ledgerId">
            <el-select v-model="queryParams.ledgerId" placeholder="请选择账簿" clearable filterable :loading="ledgerLoading" class="!w-full">
              <el-option v-for="item in ledgerOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="科目编码" prop="subjectCode">
            <el-input v-model="queryParams.subjectCode" placeholder="请输入科目编码" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="科目名称" prop="subjectName">
            <el-input v-model="queryParams.subjectName" placeholder="请输入科目名称" clearable class="!w-full" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="科目类型" prop="subjectType">
            <el-select v-model="queryParams.subjectType" placeholder="请选择科目类型" clearable class="!w-full">
              <el-option v-for="item in SUBJECT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-full">
              <el-option v-for="item in COMMON_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="finance-subject-page__query-actions">
          <el-button :loading="listLoading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button :disabled="listLoading" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <div class="finance-subject-page__toolbar">
        <div class="finance-subject-page__toolbar-actions">
          <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:finance-subject:create']">
            <Icon icon="ep:plus" class="mr-5px" />
            新增
          </el-button>
        </div>
      </div>

      <div v-if="listErrorMessage && !list.length" class="finance-subject-page__state">
        <el-result icon="error" title="科目列表加载失败" :sub-title="listErrorMessage">
          <template #extra>
            <el-button type="primary" @click="getList">重试</el-button>
          </template>
        </el-result>
      </div>

      <template v-else>
        <div v-if="listLoading || list.length" class="finance-subject-page__table-wrap">
          <el-table v-loading="listLoading" :data="list" stripe show-overflow-tooltip>
            <el-table-column label="科目信息" min-width="260">
              <template #default="{ row }">
                <div class="finance-subject-page__primary-cell">
                  <span class="finance-subject-page__primary-text">{{ row.subjectName || '-' }}</span>
                  <span class="finance-subject-page__muted-text font-mono">{{ row.subjectCode || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="账簿" min-width="160">
              <template #default="{ row }">{{ row.ledgerName || '-' }}</template>
            </el-table-column>
            <el-table-column label="科目类型" width="120">
              <template #default="{ row }">{{ row.subjectTypeName || getSubjectTypeLabel(row.subjectType) }}</template>
            </el-table-column>
            <el-table-column label="余额方向" width="110">
              <template #default="{ row }">{{ row.balanceDirectionName || '-' }}</template>
            </el-table-column>
            <el-table-column label="末级" align="center" width="90">
              <template #default="{ row }">
                <el-tag :type="row.leaf ? 'success' : 'info'" effect="light" round>{{ row.leaf ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="110">
              <template #default="{ row }">
                <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sort" align="right" width="90" />
            <el-table-column label="备注" min-width="200">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" fixed="right" width="180">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="isRowBusy(row.id)" @click="openForm('update', row.id)">
                  编辑
                </el-button>
                <el-button link type="danger" :loading="deleteLoadingId === row.id" :disabled="isRowBusy(row.id)" @click="handleDelete(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-else description="暂无数据" />

        <Pagination
          v-if="total > 0"
          v-model:limit="queryParams.pageSize"
          v-model:page="queryParams.pageNo"
          :total="total"
          @pagination="getList"
        />
      </template>
    </ContentWrap>

    <SubjectForm ref="formRef" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import {
  COMMON_STATUS_OPTIONS,
  SUBJECT_TYPE_OPTIONS,
  getSubjectTypeLabel
} from '@/views/erp/finance/shared/accounting'
import {
  ErpFinanceLedgerVO,
  FinanceLedgerApi
} from '@/api/erp/finance/ledger'
import {
  ErpFinanceSubjectPageReqVO,
  ErpFinanceSubjectVO,
  FinanceSubjectApi
} from '@/api/erp/finance/subject'
import SubjectForm from './SubjectForm.vue'

defineOptions({ name: 'ErpFinanceSubject' })

const message = useMessage()
const queryFormRef = ref()
const formRef = ref<InstanceType<typeof SubjectForm>>()
const ledgerLoading = ref(false)
const listLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingId = ref<number>()
const ledgerOptions = ref<ErpFinanceLedgerVO[]>([])
const list = ref<ErpFinanceSubjectVO[]>([])
const total = ref(0)

const queryParams = reactive<ErpFinanceSubjectPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  ledgerId: undefined,
  parentId: undefined,
  subjectCode: undefined,
  subjectName: undefined,
  subjectType: undefined,
  status: undefined
})

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) => id != null && deleteLoadingId.value === id

const loadLedgers = async () => {
  ledgerLoading.value = true
  try {
    ledgerOptions.value = await FinanceLedgerApi.getLedgerSimpleList()
  } finally {
    ledgerLoading.value = false
  }
}

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const data = await FinanceSubjectApi.getSubjectPage(queryParams)
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '科目列表加载失败'
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
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getList()
}

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  try {
    await message.delConfirm()
    deleteLoadingId.value = id
    await FinanceSubjectApi.deleteSubject(id)
    message.success('删除成功')
    if (list.value.length === 1 && queryParams.pageNo > 1) {
      queryParams.pageNo -= 1
    }
    await getList()
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    deleteLoadingId.value = undefined
  }
}

const handleFormSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

onMounted(async () => {
  await Promise.allSettled([loadLedgers(), getList()])
})
</script>

<style scoped>
.finance-subject-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-subject-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-subject-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-subject-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-subject-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-subject-page__table-wrap {
  overflow-x: auto;
}

.finance-subject-page__state {
  padding: 24px 0 8px;
}

.finance-subject-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-subject-page__primary-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.finance-subject-page__muted-text {
  color: var(--el-text-color-secondary);
}

@media (max-width: 1440px) {
  .finance-subject-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-subject-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-subject-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-subject-page__query-actions,
  .finance-subject-page__toolbar {
    justify-content: stretch;
  }
}
</style>
