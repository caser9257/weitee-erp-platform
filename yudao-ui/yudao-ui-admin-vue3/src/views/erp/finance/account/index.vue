<template>
  <doc-alert
    title="【财务】采购付款、销售收款"
    url="https://doc.iocoder.cn/sale/finance-payment-receipt/"
  />

  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="76px"
      class="finance-account-page__query-form"
    >
      <div class="finance-account-page__query-grid">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入名称"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="编码" prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入编码"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            class="!w-full"
          >
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="queryParams.remark"
            placeholder="请输入备注"
            clearable
            @keyup.enter="handleQuery"
            class="!w-full"
          />
        </el-form-item>
      </div>
      <div class="finance-account-page__query-actions">
        <el-button :loading="loadingList" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button :disabled="loadingList" @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="finance-account-page__toolbar">
      <div class="finance-account-page__toolbar-actions">
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['erp:account:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          :disabled="loadingList"
          v-hasPermi="['erp:account:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
      </div>
    </div>

    <div v-if="listErrorMessage && !list.length" class="finance-account-page__state">
      <el-result icon="error" title="结算账户加载失败" :sub-title="listErrorMessage">
        <template #extra>
          <el-button type="primary" @click="getList">重试</el-button>
        </template>
      </el-result>
    </div>
    <template v-else>
      <div v-if="loadingList || list.length" class="finance-account-page__table-wrap">
        <el-table v-loading="loadingList" :data="list" stripe show-overflow-tooltip>
          <el-table-column label="账户信息" min-width="220">
            <template #default="{ row }">
              <div class="finance-account-page__primary-cell">
                <span class="finance-account-page__primary-text">{{ row.name }}</span>
                <span class="finance-account-page__mono-tag">{{ row.no || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="220">
            <template #default="{ row }">
              <span class="finance-account-page__muted-text" :title="row.remark || '-'">
                {{ row.remark || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" width="110" prop="status">
            <template #default="{ row }">
              <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="排序" align="right" width="100" prop="sort" class-name="font-mono">
            <template #default="{ row }">
              <span class="font-mono">{{ row.sort ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="是否默认" align="center" width="120" prop="defaultStatus">
            <template #default="{ row }">
              <el-switch
                v-model="row.defaultStatus"
                :active-value="true"
                :inactive-value="false"
                :disabled="isRowBusy(row.id)"
                :loading="defaultStatusLoadingId === row.id"
                @change="handleDefaultStatusChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="创建时间"
            align="center"
            prop="createTime"
            :formatter="dateFormatter"
            width="180"
          />
          <el-table-column label="操作" align="center" fixed="right" width="180">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                :disabled="isRowBusy(row.id)"
                @click="openForm('update', row.id)"
                v-hasPermi="['erp:account:update']"
              >
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                :disabled="isRowBusy(row.id)"
                :loading="deleteLoadingId === row.id"
                @click="handleDelete(row.id)"
                v-hasPermi="['erp:account:delete']"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty
        v-else
        class="finance-account-page__state"
        description="暂无结算账户数据"
      />

      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </template>
  </ContentWrap>

  <AccountForm ref="formRef" @success="handleFormSuccess" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { AccountApi, AccountPageReqVO, AccountVO } from '@/api/erp/finance/account'
import AccountForm from './AccountForm.vue'

defineOptions({ name: 'ErpAccount' })

const message = useMessage()

const loadingList = ref(false)
const exportLoading = ref(false)
const listErrorMessage = ref('')
const deleteLoadingId = ref<number>()
const defaultStatusLoadingId = ref<number>()

const list = ref<AccountVO[]>([])
const total = ref(0)

const queryParams = reactive<AccountPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  remark: undefined,
  status: undefined,
  name: undefined
})

const queryFormRef = ref()
const formRef = ref<InstanceType<typeof AccountForm>>()

const isActionCanceled = (error: unknown) => error === 'cancel' || error === 'close'
const isRowBusy = (id?: number) =>
  (id != null && deleteLoadingId.value === id) || (id != null && defaultStatusLoadingId.value === id)

const getList = async () => {
  loadingList.value = true
  listErrorMessage.value = ''
  try {
    const data = await AccountApi.getAccountPage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch {
    if (!list.value.length) {
      listErrorMessage.value = '请检查网络或稍后重试。'
    }
  } finally {
    loadingList.value = false
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
    await AccountApi.deleteAccount(id)
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

const handleDefaultStatusChange = async (row: AccountVO) => {
  if (!row.id || defaultStatusLoadingId.value) {
    row.defaultStatus = !row.defaultStatus
    return
  }
  const nextValue = row.defaultStatus
  try {
    const actionText = nextValue ? '设为' : '取消'
    await message.confirm(`确认要${actionText}“${row.name}”默认账户吗？`)
    defaultStatusLoadingId.value = row.id
    await AccountApi.updateAccountDefaultStatus(row.id, nextValue)
    message.success(`${actionText}默认账户成功`)
    await getList()
  } catch (error) {
    row.defaultStatus = !nextValue
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    defaultStatusLoadingId.value = undefined
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await AccountApi.exportAccount(queryParams)
    download.excel(data, '结算账户.xls')
  } catch (error) {
    if (!isActionCanceled(error)) {
      throw error
    }
  } finally {
    exportLoading.value = false
  }
}

const handleFormSuccess = async () => {
  queryParams.pageNo = 1
  await getList()
}

onMounted(() => {
  void Promise.allSettled([getList()])
})
</script>

<style scoped>
.finance-account-page__query-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.finance-account-page__query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.finance-account-page__query-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-account-page__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.finance-account-page__toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.finance-account-page__table-wrap {
  overflow-x: auto;
}

.finance-account-page__state {
  padding: 24px 0 8px;
}

.finance-account-page__primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.finance-account-page__primary-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.finance-account-page__mono-tag {
  width: fit-content;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 2px 8px;
  background: #f8fafc;
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, SFMono-Regular, Menlo, Monaco, Consolas,
    'Liberation Mono', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
}

.finance-account-page__muted-text {
  display: inline-block;
  color: var(--el-text-color-secondary);
}

@media (max-width: 1440px) {
  .finance-account-page__query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .finance-account-page__query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-account-page__query-grid {
    grid-template-columns: 1fr;
  }

  .finance-account-page__query-actions,
  .finance-account-page__toolbar {
    justify-content: stretch;
  }
}
</style>
