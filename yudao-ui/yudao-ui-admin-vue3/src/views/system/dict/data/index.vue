<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="字典名称" prop="dictType">
        <el-select v-model="queryParams.dictType" class="!w-240px" @change="dictChange">
          <el-option
            v-for="item in dictTypeList"
            :key="item.type"
            :label="item.name"
            :value="item.type"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="字典标签" prop="label">
        <el-input
          v-model="queryParams.label"
          placeholder="请输入字典标签"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="数据状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['system:dict:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['system:dict:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="warning"
          plain
          :disabled="!canBatchEdit"
          @click="handleOpenBatchEdit"
          v-hasPermi="['system:dict:update']"
        >
          <Icon icon="ep:edit" class="mr-5px" /> 批量编辑
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="!canBatchDelete"
          :loading="batchDeleteLoading"
          @click="handleDeleteBatch"
          v-hasPermi="['system:dict:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="list"
      row-key="id"
      @selection-change="handleRowCheckboxChange"
    >
      <template #empty>
        <div v-if="listLoadError" class="dict-data-empty dict-data-empty--error">
          <div class="dict-data-empty__icon">
            <Icon icon="ep:warning-filled" />
          </div>
          <div class="dict-data-empty__title">列表加载失败</div>
          <el-button type="primary" plain :disabled="loading" @click="getList">重试加载</el-button>
        </div>
        <div v-else class="dict-data-empty">
          <div class="dict-data-empty__icon">
            <Icon icon="ep:document" />
          </div>
          <div class="dict-data-empty__title">暂无字典数据</div>
        </div>
      </template>
      <el-table-column type="selection" width="55" />
      <el-table-column label="字典编码" align="center" prop="id" />
      <el-table-column label="字典标签" align="center" prop="label" />
      <el-table-column label="字典键值" align="center" prop="value" />
      <el-table-column label="字典排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="颜色类型" align="center" prop="colorType" />
      <el-table-column label="样式类名" align="center" prop="cssClass" />
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['system:dict:update']"
          >
            修改
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['system:dict:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DictDataForm ref="formRef" @success="getList" />
  <DictDataBatchEditDrawer ref="batchEditDrawerRef" @success="handleBatchEditSuccess" />
</template>
<script lang="ts" setup>
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as DictDataApi from '@/api/system/dict/dict.data'
import * as DictTypeApi from '@/api/system/dict/dict.type'
import DictDataBatchEditDrawer from './components/DictDataBatchEditDrawer.vue'
import DictDataForm from './DictDataForm.vue'

defineOptions({ name: 'SystemDictData' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const route = useRoute() // 路由

const loading = ref(true) // 列表的加载中
const listLoadError = ref(false)
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const batchDeleteLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  label: '',
  status: undefined,
  dictType: route.params.dictType
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const dictTypeList = ref<DictTypeApi.DictTypeVO[]>() // 字典类型的列表
const tableRef = ref()
const selectedRows = ref<DictDataApi.DictDataVO[]>([])
const batchEditDrawerRef = ref()

const selectedRowIds = computed(() =>
  selectedRows.value.map((row) => row.id).filter((id): id is number => id !== undefined && id !== null)
)
const hasSelectedRows = computed(() => selectedRowIds.value.length > 0)
const canBatchEdit = computed(() => hasSelectedRows.value && !loading.value)
const canBatchDelete = computed(() => hasSelectedRows.value && !batchDeleteLoading.value)

const clearSelectionState = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}

/** 查询列表 */
const getList = async () => {
  clearSelectionState()
  loading.value = true
  listLoadError.value = false
  try {
    const data = await DictDataApi.getDictDataPage(queryParams)
    list.value = data.list
    total.value = data.total
  } catch {
    list.value = []
    total.value = 0
    listLoadError.value = true
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 字典类型更改同时更新列表数据 */
const dictChange = (v) => {
  queryParams.dictType = v
  handleQuery()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id, queryParams.dictType)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await DictDataApi.deleteDictData(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除按钮操作 */
const handleRowCheckboxChange = (rows: DictDataApi.DictDataVO[]) => {
  selectedRows.value = (rows || []).filter((row) => row.id !== undefined && row.id !== null)
}

const handleDeleteBatch = async () => {
  if (!hasSelectedRows.value || batchDeleteLoading.value) {
    return
  }
  try {
    // 删除的二次确认
    await message.delConfirm()
    batchDeleteLoading.value = true
    // 发起批量删除
    await DictDataApi.deleteDictDataList(selectedRowIds.value.slice())
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {
  } finally {
    batchDeleteLoading.value = false
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await DictDataApi.exportDictData(queryParams)
    download.excel(data, '字典数据.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleOpenBatchEdit = () => {
  if (!hasSelectedRows.value) {
    message.warning('请先选择要修改的数据')
    return
  }
  batchEditDrawerRef.value?.open({
    ids: selectedRowIds.value.slice(),
    rows: selectedRows.value.slice()
  })
}

const handleBatchEditSuccess = async () => {
  await getList()
}

/** 初始化 **/
onMounted(async () => {
  await getList()
  // 查询字典（精简)列表
  dictTypeList.value = await DictTypeApi.getSimpleDictTypeList()
})
</script>

<style scoped lang="scss">
.dict-data-empty {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
}

.dict-data-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 24px;
}

.dict-data-empty__title {
  color: #0f172a;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
}

.dict-data-empty--error .dict-data-empty__icon {
  background: rgba(248, 113, 113, 0.12);
  color: #e11d48;
}
</style>
