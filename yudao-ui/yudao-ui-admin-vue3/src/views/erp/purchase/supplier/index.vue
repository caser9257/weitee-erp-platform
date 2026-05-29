<template>
  <doc-alert title="【采购】供应商" url="https://doc.iocoder.cn/erp/purchase/" />

  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="supplier-query">
      <div class="supplier-query__grid">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="手机号码" prop="mobile">
          <el-input
            v-model="queryParams.mobile"
            placeholder="请输入手机号码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="telephone">
          <el-input
            v-model="queryParams.telephone"
            placeholder="请输入联系电话"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
      </div>
      <div class="supplier-query__footer">
        <div class="supplier-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <Icon icon="ep:search" class="mr-5px" /> 搜索
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="supplier-toolbar">
      <div class="supplier-toolbar__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:supplier:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:supplier:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          plain
          :disabled="!canOpenBatchEdit"
          @click="openBatchEditDrawer"
          v-hasPermi="['erp:supplier:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div class="supplier-table__scroll">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <div v-if="listLoadFailed" class="supplier-empty supplier-empty--error">
            <div class="supplier-empty__icon">
              <Icon icon="ep:warning-filled" />
            </div>
            <div class="supplier-empty__title">列表加载失败</div>
            <el-button type="primary" plain @click="handleRetryList" :disabled="!canRetryList">
              重试加载
            </el-button>
          </div>
          <div v-else class="supplier-empty">
            <div class="supplier-empty__icon">
              <Icon icon="ep:box" />
            </div>
            <div class="supplier-empty__title">暂无供应商记录</div>
          </div>
        </template>
        <el-table-column width="36" type="selection" />
        <el-table-column label="名称" align="center" prop="name" />
        <el-table-column label="联系人" align="center" prop="contact" />
        <el-table-column label="手机号码" align="center" prop="mobile" />
        <el-table-column label="联系电话" align="center" prop="telephone" />
        <el-table-column label="电子邮箱" align="center" prop="email" />
        <el-table-column label="传真" align="center" prop="fax" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="排序" align="center" prop="sort" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="140">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openForm('update', scope.row.id)"
              v-hasPermi="['erp:supplier:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-hasPermi="['erp:supplier:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <SupplierForm ref="formRef" @success="getList" />
  <SupplierBatchEditDrawer ref="batchEditDrawerRef" @success="handleBatchEditSuccess" />
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import download from '@/utils/download'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import SupplierForm from './SupplierForm.vue'
import SupplierBatchEditDrawer from './components/SupplierBatchEditDrawer.vue'

/** ERP 供应商 列表 */
defineOptions({ name: 'ErpSupplier' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const listLoadFailed = ref(false)
const list = ref<SupplierVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const tableRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  mobile: undefined,
  telephone: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const selectedRows = ref<SupplierVO[]>([])
const batchEditDrawerRef = ref()
const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
const canOpenBatchEdit = computed(() => selectedIds.value.length > 0 && !loading.value)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  listLoadFailed.value = false
  try {
    const data = await SupplierApi.getSupplierPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectedRows.value = []
    await nextTick()
    tableRef.value?.clearSelection?.()
  } catch {
    list.value = []
    total.value = 0
    selectedRows.value = []
    listLoadFailed.value = true
  } finally {
    loading.value = false
  }
}

const canRetryList = computed(() => !loading.value)

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const handleRetryList = () => {
  getList()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await SupplierApi.deleteSupplier(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

const handleSelectionChange = (rows: SupplierVO[]) => {
  selectedRows.value = rows
}

const openBatchEditDrawer = () => {
  if (!selectedIds.value.length) {
    message.warning('请先选择要修改的供应商')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: selectedIds.value,
    rows: selectedRows.value
  })
}

const handleBatchEditSuccess = async (_result: unknown) => {
  await getList()
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await SupplierApi.exportSupplier(queryParams)
    download.excel(data, 'ERP 供应商.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.supplier-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    color: #475569;
    font-size: 13px;
    font-weight: 600;
    line-height: 20px;
  }

  :deep(.el-input__wrapper) {
    min-height: 42px;
    border-radius: 12px;
    box-shadow: none;
    border: 1px solid rgba(203, 213, 225, 0.9);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  }
}

.supplier-query__grid {
  display: grid;
  gap: 18px 16px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.supplier-query__footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.supplier-query__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.supplier-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.82);
}

.supplier-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.supplier-table__scroll {
  overflow-x: auto;
}

.supplier-empty {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}

.supplier-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.08), rgba(20, 184, 166, 0.08));
  color: #2563eb;
  font-size: 22px;
}

.supplier-empty__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.supplier-empty--error .supplier-empty__icon {
  background: linear-gradient(180deg, rgba(248, 113, 113, 0.12), rgba(251, 191, 36, 0.08));
  color: #ef4444;
}

@media (max-width: 1024px) {
  .supplier-query__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .supplier-query__footer,
  .supplier-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .supplier-query__actions,
  .supplier-toolbar__actions {
    width: 100%;
  }

  .supplier-query__actions :deep(.el-button),
  .supplier-toolbar__actions :deep(.el-button) {
    flex: 1;
  }
}

@media (max-width: 767px) {
  .supplier-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
