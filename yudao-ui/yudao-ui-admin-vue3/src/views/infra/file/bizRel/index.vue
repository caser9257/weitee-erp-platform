<template>
  <ContentWrap title="文件业务关联">
    <!-- 搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable class="!w-200px">
          <el-option
            v-for="item in bizTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务ID" prop="bizId">
        <el-input
          v-model="queryParams.bizId"
          placeholder="请输入业务ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="文件ID" prop="fileId">
        <el-input
          v-model="queryParams.fileId"
          placeholder="请输入文件ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="关联ID" prop="id" width="100" align="center" />
      <el-table-column label="文件ID" prop="fileId" width="100" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewFile(row.fileId)">
            {{ row.fileId }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" prop="bizType" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ getBizTypeLabel(row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务ID" prop="bizId" width="120" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewBiz(row.bizType, row.bizId)">
            {{ row.bizId }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="业务单号" prop="bizNo" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="备注" prop="remark" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :formatter="dateFormatter" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="danger"
            @click="handleDelete(row.fileId, row.bizType, row.bizId)"
            v-hasPermi="['infra:file:delete']"
          >
            <Icon icon="ep:delete" class="mr-3px" /> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 文件详情弹窗 -->
  <FileDetailDialog ref="fileDetailRef" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileBizRelApi from '@/api/infra/fileBizRel'
import type { FileBizRelVO } from '@/api/infra/fileBizRel'

defineOptions({ name: 'InfraFileBizRel' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const list = ref<FileBizRelVO[]>([])
const queryFormRef = ref()

// 业务类型选项
const bizTypeOptions = [
  { label: '销售订单', value: 'sale_order' },
  { label: '销售出库', value: 'sale_out' },
  { label: '销售退货', value: 'sale_return' },
  { label: '采购订单', value: 'purchase_order' },
  { label: '采购入库', value: 'purchase_in' },
  { label: '采购退货', value: 'purchase_return' },
  { label: '库存调拨', value: 'stock_move' },
  { label: '库存盘点', value: 'stock_check' },
  { label: '供应商', value: 'supplier' },
  { label: '客户', value: 'customer' },
  { label: '其他', value: 'other' }
]

const queryParams = reactive({
  bizType: undefined as string | undefined,
  bizId: undefined as number | undefined,
  fileId: undefined as number | undefined
})

/** 获取业务类型标签 */
const getBizTypeLabel = (bizType: string) => {
  const option = bizTypeOptions.find((item) => item.value === bizType)
  return option ? option.label : bizType
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // 根据查询条件获取数据
    if (queryParams.fileId) {
      const data = await FileBizRelApi.getFileBizRelListByFile(queryParams.fileId)
      list.value = data
    } else if (queryParams.bizType && queryParams.bizId) {
      const data = await FileBizRelApi.getFileBizRelListByBiz(queryParams.bizType, queryParams.bizId)
      list.value = data
    } else {
      // 如果没有查询条件，提示用户输入
      list.value = []
      if (queryParams.bizType || queryParams.bizId) {
        message.warning('请同时输入业务类型和业务ID进行查询')
      }
    }
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  list.value = []
}

/** 查看文件详情 */
const fileDetailRef = ref()
const handleViewFile = (fileId: number) => {
  // 跳转到文件管理页面查看
  router.push({ path: '/infra/file', query: { id: fileId } })
}

/** 查看业务单据 */
const handleViewBiz = (bizType: string, bizId: number) => {
  // 根据业务类型跳转到对应的页面
  const routeMap: Record<string, string> = {
    sale_order: '/erp/sale/order',
    sale_out: '/erp/sale/out',
    sale_return: '/erp/sale/return',
    purchase_order: '/erp/purchase/order',
    purchase_in: '/erp/purchase/in',
    purchase_return: '/erp/purchase/return',
    stock_move: '/erp/stock/move',
    stock_check: '/erp/stock/check'
  }
  const path = routeMap[bizType]
  if (path) {
    router.push({ path, query: { id: bizId } })
  } else {
    message.info('暂不支持跳转到该业务类型')
  }
}

/** 删除操作 */
const handleDelete = async (fileId: number, bizType: string, bizId: number) => {
  try {
    await message.delConfirm()
    await FileBizRelApi.deleteFileBizRel(fileId, bizType, bizId)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}
</script>
