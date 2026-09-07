<template>
  <div class="product-unit-page">
    <!-- 搜索卡片 -->
    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="单位名字" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入单位名字"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="单位类型" prop="unitType">
          <el-select
            v-model="queryParams.unitType"
            placeholder="请选择单位类型"
            clearable
            class="!w-240px"
          >
            <el-option label="基本单位" :value="ProductUnitTypeEnum.BASE" />
            <el-option label="辅助单位" :value="ProductUnitTypeEnum.AUXILIARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="基本单位" prop="baseUnitId">
          <el-select
            v-model="queryParams.baseUnitId"
            placeholder="请选择基本单位"
            clearable
            filterable
            class="!w-240px"
          >
            <el-option
              v-for="unit in baseUnitOptions"
              :key="unit.id"
              :label="unit.name"
              :value="unit.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单位状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择单位状态"
            clearable
            class="!w-240px"
          >
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="float-right!">
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表卡片 -->
    <ContentWrap>
      <div class="mb-10px flex items-center justify-between">
        <span class="text-14px font-bold text-slate-700">单位列表</span>
        <div>
          <el-button
            type="primary"
            plain
            @click="openForm('create')"
            v-hasPermi="['erp:product-unit:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 新增
          </el-button>
          <el-button
            type="success"
            plain
            @click="handleExport"
            :loading="exportLoading"
            v-hasPermi="['erp:product-unit:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <template #empty>
          <div class="py-40px flex flex-col items-center">
            <div
              class="mb-12px h-48px w-48px flex items-center justify-center rounded-12px"
              style="background: var(--erp-slate-100)"
            >
              <Icon icon="ep:scale-to-original" :size="24" style="color: var(--erp-slate-400)" />
            </div>
            <span style="color: var(--erp-slate-400)">暂无单位数据</span>
          </div>
        </template>
        <el-table-column label="名字" align="left" prop="name" min-width="120">
          <template #default="scope">
            <span class="font-bold text-slate-700">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" prop="unitType" width="110">
          <template #default="scope">
            <span
              class="inline-block rounded-6px px-8px py-2px text-12px font-medium border border-solid"
              :class="
                scope.row.unitType === ProductUnitTypeEnum.AUXILIARY
                  ? 'unit-badge--teal'
                  : 'unit-badge--blue'
              "
            >
              {{ scope.row.unitType === ProductUnitTypeEnum.AUXILIARY ? '辅助单位' : '基本单位' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="基本单位" align="center" prop="baseUnitName" width="120">
          <template #default="scope">
            <span v-if="scope.row.baseUnitName">{{ scope.row.baseUnitName }}</span>
            <span v-else style="color: var(--erp-slate-400)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="换算率" align="right" prop="conversionRate" width="140">
          <template #default="scope">
            <span v-if="scope.row.conversionRate != null" class="font-mono">
              1 = {{ formatRate(scope.row.conversionRate) }}
            </span>
            <span v-else style="color: var(--erp-slate-400)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="数量精度" align="center" prop="quantityPrecision" width="100">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.quantityPrecision }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          align="center"
          prop="createTime"
          :formatter="dateFormatter"
          width="180"
        />
        <el-table-column label="操作" align="center" width="140" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openForm('update', scope.row.id)"
              v-hasPermi="['erp:product-unit:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-hasPermi="['erp:product-unit:delete']"
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
    <ProductUnitForm ref="formRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  ProductUnitApi,
  ProductUnitTypeEnum,
  type ProductUnitVO
} from '@/api/erp/product/unit'
import ProductUnitForm from './ProductUnitForm.vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** ERP 产品单位列表 */
defineOptions({ name: 'ErpProductUnit' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<ProductUnitVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
  unitType: undefined,
  baseUnitId: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const unitSimpleList = ref<ProductUnitVO[]>([]) // 单位精简列表

/** 基本单位下拉选项（仅启用中的基本单位） */
const baseUnitOptions = computed(() =>
  unitSimpleList.value.filter((unit) => unit.unitType === ProductUnitTypeEnum.BASE)
)

/** 换算率展示：去掉无意义尾零 */
const formatRate = (rate: number | string) => {
  return Number(rate).toLocaleString('zh-CN', { maximumFractionDigits: 10 })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductUnitApi.getProductUnitPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 加载单位精简列表 */
const getUnitSimpleList = async () => {
  unitSimpleList.value = await ProductUnitApi.getProductUnitSimpleList()
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
    await ProductUnitApi.deleteProductUnit(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
    await getUnitSimpleList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ProductUnitApi.exportProductUnit(queryParams)
    download.excel(data, '产品单位.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
  getUnitSimpleList()
})
</script>

<style scoped>
.unit-badge--blue {
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  border-color: var(--erp-primary-200);
}

.unit-badge--teal {
  background: var(--erp-teal-50);
  color: var(--erp-teal-600);
  border-color: var(--erp-teal-200);
}
</style>
