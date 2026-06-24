<template>
<ContentWrap class="purchase-return-page__header-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('blue')">
    <div class="purchase-return-page__header">
      <div>
        <div class="purchase-return-page__title">采购退�?/div>
        <div class="purchase-return-page__count">�?{{ total }} 条记�?/div>
      </div>
      <div class="purchase-return-page__actions">
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['erp:purchase-return:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增退货单
        </el-button>
        <el-button
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['erp:purchase-return:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          plain
          type="danger"
          @click="handleDelete(selectionList.map((item) => item.id))"
          v-hasPermi="['erp:purchase-return:delete']"
          :disabled="selectionList.length === 0"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap class="purchase-return-page__filter-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('emerald')">
    <div class="purchase-return-page__section-title">筛选条�?/div>
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-position="top"
      class="purchase-return-query"
    >
      <div class="purchase-return-query__grid purchase-return-query__grid--primary">
        <el-form-item label="退货单�? prop="no">
          <el-input
            v-model="queryParams.no"
            placeholder="请输入退货单�?
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" clearable filterable placeholder="请选择产品">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="供应�? prop="supplierId">
          <el-select v-model="queryParams.supplierId" clearable filterable placeholder="请选择供应�?>
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退货时�? prop="inTime">
          <el-date-picker
            v-model="queryParams.inTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日�?
            end-placeholder="结束日期"
            range-separator="-"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          />
        </el-form-item>
      </div>

      <transition name="purchase-return-query-collapse">
        <div
          v-if="advancedSearchVisible"
          class="purchase-return-query__grid purchase-return-query__grid--advanced"
        >
          <el-form-item label="仓库" prop="warehouseId">
            <el-select v-model="queryParams.warehouseId" clearable filterable placeholder="请选择仓库">
              <el-option
                v-for="item in warehouseList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建�? prop="creator">
            <el-select v-model="queryParams.creator" clearable filterable placeholder="请选择创建�?>
              <el-option
                v-for="item in userList"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联订单" prop="orderNo">
            <el-input
              v-model="queryParams.orderNo"
              placeholder="请输入关联订�?
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="结算账户" prop="accountId">
            <el-select v-model="queryParams.accountId" clearable filterable placeholder="请选择结算账户">
              <el-option
                v-for="item in accountList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="退款状�? prop="refundStatus">
            <el-select v-model="queryParams.refundStatus" clearable placeholder="请选择退款状�?>
              <el-option label="未退�? value="0" />
              <el-option label="部分退�? value="1" />
              <el-option label="全部退�? value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状�? prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择审核状�?>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ERP_AUDIT_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="queryParams.remark"
              placeholder="请输入备�?
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </div>
      </transition>

      <div class="purchase-return-query__footer">
        <el-button link type="primary" @click="toggleAdvancedSearch">
          {{ advancedSearchVisible ? '收起高级筛�? : '展开高级筛�? }}
          <span v-if="advancedFilterCount" class="purchase-return-query__filter-count">
            {{ advancedFilterCount }}
          </span>
          <Icon :icon="advancedSearchVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
        <div class="purchase-return-query__actions">
          <el-button @click="resetQuery" :disabled="loading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="purchase-return-page__list-card" :body-style="{ padding: '24px' }" :class="getToneCardClass('slate')">
    <div class="purchase-return-table__scroll">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        class="purchase-return-table"
        @selection-change="handleSelectionChange"
      >
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleSelectionChange"
    >
      <el-table-column width="30" label="选择" type="selection" />
      <el-table-column min-width="180" label="退货单�? align="center" prop="no" />
      <el-table-column label="产品信息" align="center" prop="productNames" min-width="200" />
      <el-table-column label="供应�? align="center" prop="supplierName" />
      <el-table-column
        label="退货时�?
        align="center"
        prop="returnTime"
        :formatter="dateFormatter2"
        width="120px"
      />
      <el-table-column label="创建�? align="center" prop="creatorName" />
      <el-table-column
        label="总数�?
        align="center"
        prop="totalCount"
        :formatter="erpCountTableColumnFormatter"
      />
      <el-table-column
        label="应退金额"
        align="center"
        prop="totalPrice"
        :formatter="erpPriceTableColumnFormatter"
      />
      <el-table-column
        label="已退金额"
        align="center"
        prop="refundPrice"
        :formatter="erpPriceTableColumnFormatter"
      />
      <el-table-column label="未退金额" align="center">
        <template #default="scope">
          <span v-if="scope.row.refundPrice === scope.row.totalPrice">0</span>
          <el-tag type="danger" v-else>
            {{ erpPriceInputFormatter(scope.row.totalPrice - scope.row.refundPrice) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状�? align="center" fixed="right" width="90" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ERP_AUDIT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="220">
        <template #default="scope">
          <div class="flex flex-wrap justify-center gap-x-2 gap-y-1">
            <el-button
              link
              @click="openForm('detail', scope.row.id)"
              v-hasPermi="['erp:purchase-return:query']"
            >
              详情
            </el-button>
            <el-button
              link
              type="primary"
              @click="openPrintDialog(scope.row.id)"
              v-hasPermi="['erp:purchase-return:query']"
            >
              打印
            </el-button>
            <el-button
              link
              type="primary"
              @click="openForm('update', scope.row.id)"
              v-hasPermi="['erp:purchase-return:update']"
              :disabled="scope.row.status === 20"
            >
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleUpdateStatus(scope.row.id, 20)"
              v-hasPermi="['erp:purchase-return:update-status']"
              v-if="scope.row.status === 10"
            >
              审批
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleUpdateStatus(scope.row.id, 10)"
              v-hasPermi="['erp:purchase-return:update-status']"
              v-else
            >
              反审�?
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete([scope.row.id])"
              v-hasPermi="['erp:purchase-return:delete']"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <div class="purchase-return-page__footer">
      <div class="purchase-return-page__record-count">�?{{ total }} 条记�?/div>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </ContentWrap>

  <!-- 表单弹窗：添�?修改 -->
  <PurchaseReturnForm ref="formRef" @success="getList" />
  <PurchaseReturnPrintDialog ref="printDialogRef" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { PurchaseReturnApi, PurchaseReturnVO } from '@/api/erp/purchase/return'
import PurchaseReturnForm from './PurchaseReturnForm.vue'
import PurchaseReturnPrintDialog from './PurchaseReturnPrintDialog.vue'
import { ProductApi, ProductVO } from '@/api/erp/product/product'
import { UserVO } from '@/api/system/user'
import * as UserApi from '@/api/system/user'
import {
  erpCountTableColumnFormatter,
  erpPriceInputFormatter,
  erpPriceTableColumnFormatter
} from '@/utils'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { WarehouseApi, WarehouseVO } from '@/api/erp/stock/warehouse'
import { AccountApi, AccountVO } from '@/api/erp/finance/account'
import { getToneCardClass } from '../../stock/shared/stockTone'

/** ERP 采购退货列�?*/
defineOptions({ name: 'ErpPurchaseReturn' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际�?

const loading = ref(true) // 列表的加载中
const list = ref<PurchaseReturnVO[]>([]) // 列表的数�?
const total = ref(0) // 列表的总页�?
const advancedSearchVisible = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  no: undefined,
  supplierId: undefined,
  productId: undefined,
  warehouseId: undefined,
  returnTime: [],
  orderNo: undefined,
  accountId: undefined,
  status: undefined,
  refundStatus: undefined,
  remark: undefined,
  creator: undefined
})

const advancedFilterCount = computed(() => {
  const fields = [
    queryParams.warehouseId,
    queryParams.creator,
    queryParams.orderNo,
    queryParams.accountId,
    queryParams.refundStatus,
    queryParams.status,
    queryParams.remark
  ]
  return fields.filter((item) => item !== undefined && item !== null && item !== '').length
})

const toggleAdvancedSearch = () => {
  advancedSearchVisible.value = !advancedSearchVisible.value
}
const queryFormRef = ref() // 搜索的表�?
const exportLoading = ref(false) // 导出的加载中
const productList = ref<ProductVO[]>([]) // 产品列表
const supplierList = ref<SupplierVO[]>([]) // 供应商列�?
const userList = ref<UserVO[]>([]) // 用户列表
const warehouseList = ref<WarehouseVO[]>([]) // 仓库列表
const accountList = ref<AccountVO[]>([]) // 账户列表
const printDialogRef = ref<InstanceType<typeof PurchaseReturnPrintDialog>>()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PurchaseReturnApi.getPurchaseReturnPage(queryParams)
    list.value = data.list
    total.value = data.total
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

const openPrintDialog = (id?: number) => {
  if (!id) {
    return
  }
  printDialogRef.value?.open(id)
}

/** 删除按钮操作 */
const handleDelete = async (ids: number[]) => {
  try {
    // 删除的二次确�?
    await message.delConfirm()
    // 发起删除
    await PurchaseReturnApi.deletePurchaseReturn(ids)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
    selectionList.value = selectionList.value.filter((item) => !ids.includes(item.id))
  } catch {}
}

/** 审批/反审批操�?*/
const handleUpdateStatus = async (id: number, status: number) => {
  try {
    // 审批的二次确�?
    await message.confirm(`确定${status === 20 ? '审批' : '反审�?}该退货吗？`)
    // 发起审批
    await PurchaseReturnApi.updatePurchaseReturnStatus(id, status)
    message.success(`${status === 20 ? '审批' : '反审�?}成功`)
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确�?
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await PurchaseReturnApi.exportPurchaseReturn(queryParams)
    download.excel(data, '采购退�?xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 选中操作 */
const selectionList = ref<PurchaseReturnVO[]>([])
const handleSelectionChange = (rows: PurchaseReturnVO[]) => {
  selectionList.value = rows
}

/** 初始�?**/
onMounted(async () => {
  await getList()
  // 加载产品、仓库列表、供应商
  productList.value = await ProductApi.getProductSimpleList()
  supplierList.value = await SupplierApi.getSupplierSimpleList()
  userList.value = await UserApi.getSimpleUserList()
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  accountList.value = await AccountApi.getAccountSimpleList()
})
// TODO 芋艿：可优化功能：列表界面，支持导入
</script>

<style scoped lang="scss">
.purchase-return-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.purchase-return-page__title {
  color: #0f172a;
  font-size: 22px;
  line-height: 30px;
  font-weight: 600;
}

.purchase-return-page__count {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.purchase-return-page__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.purchase-return-page__section-title {
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.purchase-return-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.purchase-return-query__grid {
  display: grid;
  gap: 16px;
}

.purchase-return-query__grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-return-query__grid--advanced {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.purchase-return-query__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.purchase-return-query__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.purchase-return-query__filter-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  margin-left: 6px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.purchase-return-table__scroll {
  overflow-x: auto;
}

.purchase-return-table {
  min-width: 960px;
}

.purchase-return-page__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.purchase-return-page__record-count {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
}

.purchase-return-query-collapse-enter-active,
.purchase-return-query-collapse-leave-active {
  transition: all 0.2s ease;
}

.purchase-return-query-collapse-enter-from,
.purchase-return-query-collapse-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1279px) {
  .purchase-return-query__grid--primary,
  .purchase-return-query__grid--advanced {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 959px) {
  .purchase-return-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-return-page__actions {
    width: 100%;
  }

  .purchase-return-query__grid--primary,
  .purchase-return-query__grid--advanced {
    grid-template-columns: minmax(0, 1fr);
  }

  .purchase-return-query__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .purchase-return-query__actions {
    width: 100%;
  }
}
</style>
