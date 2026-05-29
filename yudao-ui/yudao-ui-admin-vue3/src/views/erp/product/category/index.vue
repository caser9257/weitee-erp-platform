<template>
  <doc-alert title="【产品】产品信息、分类、单位" url="https://doc.iocoder.cn/erp/product/" />

  <ContentWrap class="product-category-page__header-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="product-category-page__header">
      <div>
        <div class="product-category-page__title">产品分类</div>
        <div class="product-category-page__count">共 {{ total }} 条记录</div>
      </div>
      <div class="product-category-page__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:product-category:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增分类
        </el-button>
        <el-button
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:product-category:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button plain @click="toggleExpandAll">
          <Icon icon="ep:sort" class="mr-5px" /> 展开/折叠
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="product-category-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="product-category-page__section-title">筛选条件</div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="product-category-query"
    >
      <div class="product-category-query__grid">
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入分类名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="开启状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择开启状态">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </div>
      <div class="product-category-query__footer">
        <div></div>
        <div class="product-category-query__actions">
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="product-category-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="product-category-table__scroll">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        row-key="id"
        :default-expand-all="isExpandAll"
        v-if="refreshTable"
        class="product-category-table"
      >
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      row-key="id"
      :default-expand-all="isExpandAll"
      v-if="refreshTable"
    >
      <el-table-column label="编码" align="center" prop="code" />
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['erp:product-category:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['erp:product-category:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <div class="product-category-page__footer">
      <div class="product-category-page__record-count">共 {{ total }} 条记录</div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <ProductCategoryForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { handleTree } from '@/utils/tree'
import download from '@/utils/download'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import ProductCategoryForm from './ProductCategoryForm.vue'
import { getToneCardClass } from '../../stock/shared/stockTone'

/** ERP 产品分类 列表 */
defineOptions({ name: 'ErpProductCategory' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<ProductCategoryVO[]>([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const total = ref(0) // 列表的总数

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductCategoryApi.getProductCategoryList()
    list.value = handleTree(data, 'id', 'parentId')
    total.value = list.value.length
  } finally {
    loading.value = false
  }
}

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
    await ProductCategoryApi.deleteProductCategory(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ProductCategoryApi.exportProductCategory(queryParams)
    download.excel(data, '产品分类.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 展开/折叠操作 */
const isExpandAll = ref(true) // 是否展开，默认全部展开
const refreshTable = ref(true) // 重新渲染表格状态
const toggleExpandAll = async () => {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  await nextTick()
  refreshTable.value = true
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.product-category-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.product-category-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.product-category-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.product-category-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.product-category-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.product-category-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-category-query__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.product-category-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-category-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-category-table__scroll {
  overflow-x: auto;
}

.product-category-table {
  min-width: 800px;
}

.product-category-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.product-category-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 959px) {
  .product-category-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .product-category-page__actions {
    width: 100%;
  }

  .product-category-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .product-category-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .product-category-query__actions {
    width: 100%;
  }
}
</style>
