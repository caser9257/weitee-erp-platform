<template>
<ContentWrap>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="68px"
      class="plan-rule-query-form -mb-15px"
    >
      <el-form-item label="产品" prop="productId">
        <el-select
          v-model="queryParams.productId"
          clearable
          filterable
          placeholder="请选择产品"
          class="plan-rule-query-select"
          :loading="optionLoading"
        >
          <el-option
            v-for="item in productList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状�? prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="请选择状�?
          class="plan-rule-query-select"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="plan-rule-query-actions">
        <el-button @click="handleQuery" :disabled="listLoading">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery" :disabled="listLoading">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['erp:mrp-plan-rule:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-alert
      v-if="pageErrorMessage"
      :title="pageErrorMessage"
      type="error"
      :closable="false"
      show-icon
      class="mb-16px"
    />
    <div v-if="pageErrorMessage" class="mb-16px">
      <el-button @click="reloadPageData" :disabled="listLoading || optionLoading">重新加载</el-button>
    </div>
    <el-table
      v-loading="listLoading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      :empty-text="listEmptyText"
      class="plan-rule-table"
    >
      <el-table-column label="产品" align="center" prop="productName" min-width="140" />
      <el-table-column label="供给方式" align="center" prop="supplyType" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ERP_SUPPLY_TYPE" :value="scope.row.supplyType" />
        </template>
      </el-table-column>
      <el-table-column label="补货策略" align="center" prop="replenishMode" width="120">
        <template #default="scope">
          <span>{{ getReplenishModeLabel(scope.row.replenishMode) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="安全库存" align="center" prop="safetyStock" min-width="100" />
      <el-table-column label="最小批�? align="center" prop="minOrderQty" min-width="100" />
      <el-table-column label="倍量" align="center" prop="orderMultiple" min-width="100" />
      <el-table-column label="固定批量" align="center" prop="fixedOrderQty" min-width="100">
        <template #default="scope">
          <span v-if="isFixedLotMode(scope.row.replenishMode)">{{ scope.row.fixedOrderQty ?? '--' }}</span>
          <span v-else>--</span>
        </template>
      </el-table-column>
      <el-table-column label="采购提前�? align="center" prop="purchaseLeadDay" min-width="110" />
      <el-table-column label="生产提前�? align="center" prop="makeLeadDay" min-width="110" />
      <el-table-column label="默认供应�? align="center" prop="defaultSupplierName" min-width="140">
        <template #default="scope">
          <span>{{ scope.row.defaultSupplierName || findSupplierName(scope.row.defaultSupplierId) || '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缺料预警" align="center" prop="shortageWarnFlag" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.shortageWarnFlag ? 'success' : 'info'">
            {{ scope.row.shortageWarnFlag ? '开�? : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="校验结果" align="center" prop="validationStatus" width="120">
        <template #default="scope">
          <el-tooltip :content="scope.row.validationMessage || '规则完整'" placement="top">
            <el-tag :type="getValidationTagType(scope.row.validationStatus)">
              {{ getValidationStatusLabel(scope.row.validationStatus) }}
            </el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="状�? align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['erp:mrp-plan-rule:update']"
            :disabled="deletingId === scope.row.id"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['erp:mrp-plan-rule:delete']"
            :loading="deletingId === scope.row.id"
            :disabled="deletingId !== undefined && deletingId !== scope.row.id"
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

  <PlanRuleForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { PlanRuleApi, PlanRulePageReqVO, PlanRuleVO } from '@/api/erp/mrp/plan-rule'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import PlanRuleForm from './PlanRuleForm.vue'
import {
  getReplenishModeLabel,
  getValidationStatusLabel,
  getValidationTagType,
  isFixedLotMode
} from './planRuleForm.helpers'

defineOptions({ name: 'ErpPlanRule' })

type PlanRuleViewVO = PlanRuleVO & {
  status: number
}

const message = useMessage()
const { t } = useI18n()

const listLoading = ref(false)
const optionLoading = ref(false)
const deletingId = ref<number>()
const listErrorMessage = ref('')
const optionErrorMessage = ref('')

const list = ref<PlanRuleViewVO[]>([])
const total = ref(0)
const productList = ref<ProductVO[]>([])
const supplierList = ref<SupplierVO[]>([])
const queryFormRef = ref()
const formRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  status: undefined as number | undefined
})

const pageErrorMessage = computed(() => listErrorMessage.value || optionErrorMessage.value)
const listEmptyText = computed(() => (listErrorMessage.value ? '计划参数加载失败，请重新加载' : '暂无计划参数'))

const toStatus = (enableFlag?: boolean) =>
  enableFlag ? CommonStatusEnum.ENABLE : CommonStatusEnum.DISABLE

const getList = async () => {
  listLoading.value = true
  listErrorMessage.value = ''
  try {
    const params: PlanRulePageReqVO = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      productId: queryParams.productId,
      enableFlag:
        queryParams.status === undefined
          ? undefined
          : queryParams.status === CommonStatusEnum.ENABLE
    }
    const data = await PlanRuleApi.getPlanRulePage(params)
    list.value = data.list.map((item: PlanRuleVO) => ({
      ...item,
      status: toStatus(item.enableFlag)
    }))
    total.value = data.total
  } catch {
    listErrorMessage.value = '计划参数列表加载失败，请重试'
  } finally {
    listLoading.value = false
  }
}

const loadOptions = async () => {
  optionLoading.value = true
  optionErrorMessage.value = ''
  try {
    const [products, suppliers] = await Promise.all([
      ProductApi.getProductSimpleList(),
      SupplierApi.getSupplierSimpleList()
    ])
    productList.value = products
    supplierList.value = suppliers
  } catch {
    optionErrorMessage.value = '计划参数筛选项加载失败，请重试'
  } finally {
    optionLoading.value = false
  }
}

const reloadPageData = async () => {
  await Promise.all([loadOptions(), getList()])
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
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  if (deletingId.value !== undefined) {
    return
  }
  try {
    deletingId.value = id
    await message.delConfirm()
    await PlanRuleApi.deletePlanRule(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {
  } finally {
    deletingId.value = undefined
  }
}

const findSupplierName = (supplierId?: number) => {
  if (!supplierId) {
    return ''
  }
  return supplierList.value.find((item) => item.id === supplierId)?.name || ''
}

onMounted(async () => {
  await reloadPageData()
})
</script>

<style scoped>
.plan-rule-query-select {
  width: 240px;
}

.plan-rule-query-actions :deep(.el-form-item__content) {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.plan-rule-table {
  width: 100%;
}

@media (max-width: 1280px) {
  .plan-rule-query-form {
    display: flex;
    flex-wrap: wrap;
  }

  .plan-rule-query-form :deep(.el-form-item) {
    margin-right: 12px;
  }
}

@media (max-width: 768px) {
  .plan-rule-query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .plan-rule-query-select {
    width: 100%;
  }

  .plan-rule-query-actions {
    margin-bottom: 0;
  }

  .plan-rule-query-actions :deep(.el-form-item__content) {
    width: 100%;
  }
}
</style>
