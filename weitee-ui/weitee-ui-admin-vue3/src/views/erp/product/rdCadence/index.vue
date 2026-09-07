<template>
  <div class="rd-cadence-workbench">
    <ContentWrap class="search-card">
      <el-form ref="queryFormRef" :model="queryParams" label-width="82px" class="query-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :lg="6" :xl="6">
            <el-form-item label="物料编号" prop="materialCode">
              <el-input
                v-model="queryParams.materialCode"
                placeholder="请输入物料编号"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6" :xl="6">
            <el-form-item label="物料名称" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="请输入物料名称"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6" :xl="6">
            <el-form-item label="产品分类" prop="categoryId">
              <el-select
                v-model="queryParams.categoryId"
                placeholder="请选择分类"
                clearable
                filterable
                class="w-full"
              >
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button :disabled="!canQuery" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button :disabled="!canQuery" @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </div>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-card">
      <div class="panel-toolbar">
        <div class="panel-title">研发物料（Cadence）</div>
        <div class="toolbar-actions">
          <el-button type="primary" @click="handleImport" v-hasPermi="['erp:product:rd-cadence:import']">
            <Icon icon="ep:upload" class="mr-5px" />
            批量补充 Cadence 数据
          </el-button>
          <el-button
            type="success"
            plain
            :loading="exporting"
            :disabled="!canExport"
            @click="handleExport"
            v-hasPermi="['erp:product:rd-cadence:export']"
          >
            <Icon icon="ep:download" class="mr-5px" />
            导出当前结果
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="list"
        :border="true"
        :show-overflow-tooltip="true"
        class="rd-table"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="cadence-detail">
              <div class="detail-group">
                <div class="group-title">基础信息</div>
                <el-descriptions :column="3" border size="small">
                  <el-descriptions-item label="物料编号">
                    <span class="font-mono">{{ row.materialCode || '-' }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="物料名称">{{ row.name || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="分类">{{ row.categoryName || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="规格(Value)">{{ row.standard || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="单位">{{ row.unitName || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="审核状态">
                    <el-tag :type="auditStatusTag(row.auditStatus).type" size="small">
                      {{ auditStatusTag(row.auditStatus).text }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div class="detail-group">
                <div class="group-title">Cadence 参数</div>
                <el-descriptions :column="3" border size="small">
                  <el-descriptions-item label="原理图符号">
                    <span class="font-mono">{{ row.schematicPart || '-' }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="PCB 封装">
                    <span class="font-mono">{{ row.pcbFootprint || '-' }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="厂家型号">{{ row.manufacturerPartNumber || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="关键参数描述">{{ row.cadenceDescription || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="三维尺寸">{{ row.dimension || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="3D模型">{{ row.threeDLib || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="数据手册">{{ row.datasheet || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="生命周期">{{ row.lifecycle || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="是否优选">
                    <el-tag v-if="row.preferredPart" type="success" size="small">优选</el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="工作温度">{{ row.operatingTemperature || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="安装类型">{{ row.mountingType || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="空置(DNP)">
                    <el-tag v-if="row.dnp" type="warning" size="small">DNP</el-tag>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="进口/替代">{{ row.importedOrReplacement || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="参数描述2">{{ row.secondDescription || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="参数描述3">{{ row.thirdDescription || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="参数描述4">{{ row.fourthDescription || '-' }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="物料编号" min-width="130">
          <template #default="{ row }">
            <span class="font-mono text-slate-600">{{ row.materialCode || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物料名称" prop="name" min-width="140" show-overflow-tooltip />
        <el-table-column label="分类" prop="categoryName" min-width="100" show-overflow-tooltip />
        <el-table-column label="规格(Value)" prop="standard" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.standard || '-' }}</template>
        </el-table-column>
        <el-table-column label="原理图符号" prop="schematicPart" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="font-mono">{{ row.schematicPart || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="PCB 封装" prop="pcbFootprint" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="font-mono">{{ row.pcbFootprint || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="厂家型号" prop="manufacturerPartNumber" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.manufacturerPartNumber || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" align="center" min-width="90">
          <template #default="{ row }">
            <el-tag :type="auditStatusTag(row.auditStatus).type" size="small">
              {{ auditStatusTag(row.auditStatus).text }}
            </el-tag>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无研发物料数据" />
        </template>
      </el-table>

      <div class="panel-pagination">
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </ContentWrap>

    <RdCadenceImportForm ref="importFormRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import download from '@/utils/download'
import { RdCadenceApi, RdCadencePageReqVO } from '@/api/erp/product/rdCadence'
import { ProductCategoryApi, ProductCategoryVO } from '@/api/erp/product/category'
import RdCadenceImportForm from './RdCadenceImportForm.vue'

defineOptions({ name: 'ErpProductRdCadence' })

const AUDIT_STATUS_MAP: Record<number, { text: string; type: 'info' | 'warning' | 'success' | 'danger' | 'primary' }> = {
  0: { text: '草稿', type: 'info' },
  10: { text: '审批中', type: 'warning' },
  20: { text: '已审批', type: 'success' },
  21: { text: '变更申请审批中', type: 'warning' },
  22: { text: '变更编辑中', type: 'primary' },
  23: { text: '变更确认审批中', type: 'warning' },
  24: { text: '废除审批中', type: 'warning' },
  27: { text: '已废除', type: 'danger' },
  28: { text: '启停审批中', type: 'warning' },
  30: { text: '已驳回', type: 'danger' },
  60: { text: '失败', type: 'danger' }
}

const auditStatusTag = (status?: number) => AUDIT_STATUS_MAP[status ?? 0] ?? AUDIT_STATUS_MAP[0]

const message = useMessage()

const loading = ref(false)
const exporting = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const categoryOptions = ref<ProductCategoryVO[]>([])
const canQuery = computed(() => !loading.value)
const canExport = computed(() => total.value > 0 && !exporting.value)

const queryParams = reactive<RdCadencePageReqVO & { pageNo: number; pageSize: number }>({
  pageNo: 1,
  pageSize: 10,
  materialCode: undefined,
  name: undefined,
  categoryId: undefined
})

const queryFormRef = ref()
const importFormRef = ref()

const buildParams = () => {
  return { ...queryParams }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await RdCadenceApi.getPage(buildParams())
    list.value = data.list
    total.value = data.total
  } catch {
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  getList()
}

const handleImport = () => {
  importFormRef.value?.open()
}

const handleExport = async () => {
  if (total.value === 0) {
    return
  }
  try {
    await message.exportConfirm()
    exporting.value = true
    const data = await RdCadenceApi.exportData(buildParams())
    download.excel(data, 'Cadence物料.xls')
  } catch {
  } finally {
    exporting.value = false
  }
}

const loadCategories = async () => {
  try {
    categoryOptions.value = await ProductCategoryApi.getProductCategoryList()
  } catch {
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), getList()])
})
</script>

<style scoped lang="scss">
.rd-cadence-workbench {
  padding: 16px;
  background: var(--erp-slate-50);
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: var(--erp-shadow-sm);
}

.table-card {
  border-radius: 12px;
  box-shadow: var(--erp-shadow-sm);
}

.query-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 4px;
}

.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 12px;
}

.panel-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.rd-table {
  width: 100%;
}

.panel-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.cadence-detail {
  padding: 8px 12px;
}

.detail-group + .detail-group {
  margin-top: 16px;
}

.group-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--erp-slate-600);
}
</style>
