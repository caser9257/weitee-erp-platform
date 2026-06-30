<template>
  <doc-alert title="物料清单（BOM）" url="https://doc.iocoder.cn/erp/bom/" />

  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="80px"
      class="-mb-15px"
    >
      <el-form-item label="BOM编码" prop="bomCode">
        <el-input
          v-model="queryParams.bomCode"
          clearable
          placeholder="请输入 BOM 编码"
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
            v-for="item in BOM_STATUS_OPTIONS"
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
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['erp:bom:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          新增 BOM
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="listLoading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="BOM编码" prop="bomCode" min-width="160" />
      <el-table-column label="成品" prop="productName" min-width="180" />
      <el-table-column label="版本" prop="version" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '生效' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物料数" width="100" align="center">
        <template #default="{ row }">
          {{ row.items?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)" v-hasPermi="['erp:bom:query']">
            详情
          </el-button>
          <el-button link type="primary" @click="openForm('update', row.id)" v-hasPermi="['erp:bom:update']">
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            :loading="deleteLoadingId === row.id"
            :disabled="deleteLoadingId === row.id"
            @click="handleDelete(row.id)"
            v-hasPermi="['erp:bom:delete']"
          >
            删除
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from '@/hooks/web/useMessage'
import { BomApi, type BomPageReqVO, type BomVO } from '@/api/erp/mrp/bom'
import { ProductApi, type ProductVO } from '@/api/erp/product/product'
import BomDetailDrawer from './BomDetailDrawer.vue'
import BomForm from './BomForm.vue'

defineOptions({ name: 'ErpBom' })

const message = useMessage()
const { t } = useI18n()

const BOM_STATUS_OPTIONS = [
  { label: '生效', value: 1 },
  { label: '停用', value: 0 }
]

const listLoading = ref(false)
const productLoading = ref(false)
const deleteLoadingId = ref<number | undefined>()
const list = ref<BomVO[]>([])
const total = ref(0)
const productList = ref<ProductVO[]>([])
const queryFormRef = ref()
const formRef = ref()
const detailDrawerRef = ref()

const queryParams = reactive<BomPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  productId: undefined,
  bomCode: undefined,
  status: undefined
})

const getList = async () => {
  listLoading.value = true
  try {
    const data = await BomApi.getBomPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    listLoading.value = false
  }
}

const loadProductList = async () => {
  productLoading.value = true
  try {
    productList.value = await ProductApi.getProductSimpleList()
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

const handleDelete = async (id?: number) => {
  if (!id || deleteLoadingId.value) {
    return
  }
  deleteLoadingId.value = id
  try {
    await message.delConfirm()
    await BomApi.deleteBom(id)
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
