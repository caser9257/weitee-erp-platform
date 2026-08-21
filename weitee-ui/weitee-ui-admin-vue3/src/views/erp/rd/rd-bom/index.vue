<template>

  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="90px"
      class="-mb-15px"
    >
      <el-form-item label="研发BOM编码" prop="bomCode">
        <el-input
          v-model="queryParams.bomCode"
          clearable
          placeholder="请输入研发 BOM 编码"
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成品" prop="productId">
        <el-select
          v-model="queryParams.productId"
          clearable
          filterable
          :loading="productLoading"
          placeholder="请选择成品"
          class="!w-240px"
        >
          <el-option
            v-for="item in productList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="请选择状态"
          class="!w-180px"
        >
          <el-option
            v-for="item in RD_BOM_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
          <el-button
            type="primary"
            plain
            @click="openForm('create')"
            v-hasPermi="['erp:rd-bom:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增研发BOM
          </el-button>
          <el-button
            type="success"
            plain
            @click="importFormRef?.open()"
            v-hasPermi="['erp:rd-bom:create']"
          >
            <Icon icon="ep:upload" class="mr-5px" />
            导入
          </el-button>
          <el-button plain @click="whereUsedDialogRef?.open()" v-hasPermi="['erp:rd-bom:query']">
            <Icon icon="ep:share" class="mr-5px" />
            反向追溯
          </el-button>
        </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="研发BOM编码" prop="bomCode" min-width="160" />
      <el-table-column label="成品" prop="productName" min-width="180" />
      <el-table-column label="版本" prop="version" width="120" align="center" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="STATUS_META[row.status]?.type || 'info'" effect="light">
            {{ STATUS_META[row.status]?.label || `状态${row.status}` }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已发布制造BOM" prop="publishedBomId" width="140" align="center" />
      <el-table-column label="最近发布时间" prop="lastPublishedTime" width="180" align="center" />
      <el-table-column label="物料数" width="100" align="center">
        <template #default="{ row }">
          {{ row.items?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="600" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openDetail(row.id)"
            v-hasPermi="['erp:rd-bom:query']"
          >
            详情
          </el-button>
          <el-button
            link
            type="primary"
            :loading="treeLoadingId === row.id"
            :disabled="treeLoadingId === row.id"
            @click="handleOpenTree(row.id)"
            v-hasPermi="['erp:rd-bom:query']"
          >
            结构树
          </el-button>
          <el-button
            link
            type="primary"
            :loading="validateLoadingId === row.id"
            :disabled="validateLoadingId === row.id"
            @click="handleValidate(row.id)"
            v-hasPermi="['erp:rd-bom:query']"
          >
            校验
          </el-button>
          <el-button
            v-if="canEdit(row)"
            link
            type="primary"
            @click="openForm('update', row.id)"
            v-hasPermi="['erp:rd-bom:update']"
          >
            编辑
          </el-button>
          <el-button
            v-if="canSubmit(row)"
            link
            type="primary"
            :loading="submitLoadingId === row.id"
            :disabled="submitLoadingId === row.id"
            @click="handleSubmit(row.id)"
            v-hasPermi="['erp:rd-bom:submit']"
          >
            提交审批
          </el-button>
          <el-button
            v-if="canCancel(row)"
            link
            type="warning"
            :loading="cancelLoadingId === row.id"
            :disabled="cancelLoadingId === row.id"
            @click="handleCancel(row.id)"
            v-hasPermi="['erp:rd-bom:cancel']"
          >
            撤回审批
          </el-button>
          <el-button
            v-if="canPublish(row)"
            link
            type="success"
            :loading="publishLoadingId === row.id"
            :disabled="publishLoadingId === row.id"
            @click="handlePublish(row.id)"
            v-hasPermi="['erp:rd-bom:publish']"
          >
            发布
          </el-button>
          <el-button
            v-if="canDelete(row)"
            link
            type="danger"
            :loading="deleteLoadingId === row.id"
            :disabled="deleteLoadingId === row.id"
            @click="handleDelete(row.id)"
            v-hasPermi="['erp:rd-bom:delete']"
          >
            删除
          </el-button>
          <el-button link type="info" @click="changeLogDialogRef?.open(row.id)" v-hasPermi="['erp:rd-bom:query']">
            变更记录
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <BomForm ref="formRef" :product-options="productList" @success="getList" />
  <BomDetailDrawer ref="detailDrawerRef" />
  <RdBomImportForm ref="importFormRef" @success="getList" />
  <WhereUsedDialog ref="whereUsedDialogRef" />
  <RdBomChangeLogDialog ref="changeLogDialogRef" />

  <Dialog v-model="treeVisible" title="研发 BOM 结构树（正向穿透）" width="860px" :close-on-click-modal="false">
    <div v-loading="treeLoading" class="min-h-200px">
      <BomTree v-if="treeData" :tree-data="treeData" />
      <div v-else-if="!treeLoading" class="py-40px text-center text-slate-400">暂无数据</div>
    </div>
    <template #footer>
      <el-button @click="treeVisible = false">关闭</el-button>
    </template>
  </Dialog>

  <Dialog v-model="validateVisible" title="研发 BOM 完整性校验" width="720px" :close-on-click-modal="false">
    <div v-if="validateIssues.length === 0" class="flex items-center justify-center py-40px text-gray-400">
      未发现问题，BOM 完整性校验通过
    </div>
    <el-table v-else :data="validateIssues" :stripe="true" :show-overflow-tooltip="true" max-height="480">
      <el-table-column label="行号" prop="rowIndex" width="80" align="center" />
      <el-table-column label="物料" min-width="180">
        <template #default="{ row }">
          <span>{{ row.materialName || '—' }}</span>
          <span v-if="row.materialId" class="ml-8px font-mono text-gray-400">{{ row.materialId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="严重程度" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.severity === 'ERROR' ? 'danger' : 'warning'" effect="light">
            {{ row.severity === 'ERROR' ? '错误' : '警告' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="问题类型" width="160">
        <template #default="{ row }">
          {{ issueTypeLabel(row.issueType) }}
        </template>
      </el-table-column>
      <el-table-column label="描述" prop="message" min-width="240" />
    </el-table>
    <template #footer>
      <el-button @click="validateVisible = false">关闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from '@/hooks/web/useMessage'
import { RdBomApi, type RdBomIntegrityIssueVO, type RdBomPageReqVO, type RdBomTreeRespVO, type RdBomVO } from '@/api/erp/rd/bom'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import BomDetailDrawer from './BomDetailDrawer.vue'
import BomForm from './BomForm.vue'
import BomTree from './BomTree.vue'
import RdBomChangeLogDialog from './RdBomChangeLogDialog.vue'
import RdBomImportForm from './RdBomImportForm.vue'
import WhereUsedDialog from './WhereUsedDialog.vue'

defineOptions({ name: 'ErpRdBom' })

const message = useMessage()
const { t } = useI18n()

const RD_BOM_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '审批中', value: 10 },
  { label: '已审批', value: 20 },
  { label: '已驳回', value: 30 },
  { label: '处理失败', value: 60 }
]

const STATUS_META: Record<number, { label: string; type: 'info' | 'success' | 'warning' | 'danger' | '' }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  10: { label: '审批中', type: 'warning' },
  20: { label: '已审批', type: 'success' },
  30: { label: '已驳回', type: 'danger' },
  60: { label: '处理失败', type: 'danger' }
}

const isApprovalRunning = (row: RdBomVO) => row.status === 10 && !!row.processInstanceId
const canEdit = (row: RdBomVO) => !isApprovalRunning(row) && row.status !== 20
const canDelete = (row: RdBomVO) => !isApprovalRunning(row) && row.status !== 20
const canSubmit = (row: RdBomVO) => [0, 30, 60].includes(row.status) && !isApprovalRunning(row)
const canCancel = (row: RdBomVO) => isApprovalRunning(row)
const canPublish = (row: RdBomVO) => row.status === 20 && !isApprovalRunning(row)

const listLoading = ref(false)
const productLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const publishLoadingId = ref<number | undefined>()
const validateLoadingId = ref<number | undefined>()
const treeLoadingId = ref<number | undefined>()
const submitLoadingId = ref<number | undefined>()
const cancelLoadingId = ref<number | undefined>()
const list = ref<RdBomVO[]>([])
const total = ref(0)
const productList = ref<ProductVO[]>([])
const queryFormRef = ref()
const formRef = ref()
const detailDrawerRef = ref()
const importFormRef = ref()
const whereUsedDialogRef = ref()
const changeLogDialogRef = ref()

const validateVisible = ref(false)
const validateIssues = ref<RdBomIntegrityIssueVO[]>([])

const treeVisible = ref(false)
const treeLoading = ref(false)
const treeData = ref<RdBomTreeRespVO | null>(null)

const ISSUE_TYPE_LABELS: Record<string, string> = {
  FLOATING_MATERIAL: '悬浮件',
  USAGE_INVALID: '用量无效',
  DESIGNATOR_ON_ASSEMBLY: '装配体位号',
  FLOATING_ASSEMBLY: '悬空装配体',
  MISSING_DESIGNATOR: '缺少位号',
  DESIGNATOR_COUNT_MISMATCH: '位号数不符'
}
const issueTypeLabel = (type?: string) => (type && ISSUE_TYPE_LABELS[type]) || `类型${type}`

const queryParams = reactive<RdBomPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  productId: undefined,
  bomCode: undefined,
  status: undefined
})

const getList = async () => {
  listLoading.value = true
  try {
    const data = await RdBomApi.getRdBomPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    listLoading.value = false
  }
}

const loadProductList = async () => {
  productLoading.value = true
  try {
    // P5：BOM 仅可引用已审核物料，过滤为已审核列表
    productList.value = await ProductApi.getApprovedProductSimpleList()
  } finally {
    productLoading.value = false
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

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

const openDetail = (id?: number) => {
  if (!id) {
    return
  }
  detailDrawerRef.value?.open(id)
}

const handleValidate = async (id?: number) => {
  if (!id || validateLoadingId.value) {
    return
  }
  validateLoadingId.value = id
  try {
    validateIssues.value = await RdBomApi.validateRdBom(id)
    validateVisible.value = true
  } finally {
    validateLoadingId.value = undefined
  }
}

const handleOpenTree = async (id?: number) => {
  if (!id || treeLoadingId.value) {
    return
  }
  treeLoadingId.value = id
  treeLoading.value = true
  try {
    treeData.value = await RdBomApi.getRdBomTree({ bomId: id })
    treeVisible.value = true
  } finally {
    treeLoading.value = false
    treeLoadingId.value = undefined
  }
}

const handleSubmit = async (id?: number) => {
  if (!id || submitLoadingId.value) {
    return
  }
  submitLoadingId.value = id
  try {
    await message.confirm('确认提交该研发 BOM 进行审批吗？')
    await RdBomApi.submitRdBom(id)
    message.success('提交请求已发送，列表将刷新校验状态')
    await getList()
    const updated = list.value.find((r) => r.id === id)
    if (updated?.status === 10 && updated?.processInstanceId) {
      message.success('已提交审批，等待流程受理')
    } else if (updated?.status === 60) {
      message.error('提交已受理但流程创建失败，请重试')
    }
  } catch {
  } finally {
    submitLoadingId.value = undefined
  }
}

const handleCancel = async (id?: number) => {
  if (!id || cancelLoadingId.value) {
    return
  }
  cancelLoadingId.value = id
  try {
    await message.confirm('确认撤回该研发 BOM 的审批吗？')
    await RdBomApi.cancelRdBom(id)
    message.success('撤回成功')
    await getList()
  } catch {
  } finally {
    cancelLoadingId.value = undefined
  }
}

const handlePublish = async (id?: number) => {
  if (!id || publishLoadingId.value) {
    return
  }
  publishLoadingId.value = id
  try {
    await message.confirm('确认将当前研发 BOM 发布为制造 BOM 草稿吗？')
    await RdBomApi.publishRdBom(id)
    message.success('发布成功，已生成制造 BOM 草稿')
    await getList()
  } catch {
  } finally {
    publishLoadingId.value = undefined
  }
}

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = id
  try {
    await message.delConfirm()
    await RdBomApi.deleteRdBom(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    deleteLoadingId.value = undefined
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadProductList()])
})
</script>
