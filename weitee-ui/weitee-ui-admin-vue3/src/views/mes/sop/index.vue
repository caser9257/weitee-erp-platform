<template>
  <section class="sop-hero">
    <div class="sop-hero__header">
      <div>
        <div class="sop-hero__breadcrumb">制造执行管理 / SOP 管理</div>
        <div class="sop-page__title">SOP 管理</div>
      </div>
    </div>
  </section>

  <ContentWrap class="sop-page__filter-card">
    <el-form ref="queryFormRef" :model="queryParams" label-position="top" class="sop-query">
      <div class="sop-query__grid">
        <el-form-item label="SOP 编码" prop="sopNo">
          <el-input
            v-model="queryParams.sopNo"
            placeholder="请输入 SOP 编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已停用" :value="2" />
          </el-select>
        </el-form-item>
      </div>
      <div class="sop-query__footer">
        <div class="sop-query__actions">
          <el-button @click="resetQuery" :disabled="listLoading">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" :loading="listLoading">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <ContentWrap class="sop-page__list-card">
    <div class="sop-toolbar">
      <div class="sop-toolbar__actions">
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['mes:sop:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增 SOP
        </el-button>
        <el-button plain type="success" @click="openOcrDialog" v-hasPermi="['mes:sop:import']">
          <Icon icon="ep:upload" class="mr-5px" /> OCR 导入
        </el-button>
      </div>
    </div>
    <el-table v-loading="listLoading" :data="list" :stripe="true">
      <template #empty>
        <div class="sop-empty">
          <div class="sop-empty__icon">
            <Icon icon="ep:reading" />
          </div>
          <div class="sop-empty__title">暂无 SOP</div>
        </div>
      </template>
      <el-table-column label="SOP 信息" min-width="220">
        <template #default="{ row }">
          <div class="sop-ledger__top">
            <span class="sop-ledger__no">{{ row.sopNo }}</span>
            <span class="sop-badge" :class="statusBadgeClass(row.status)">{{
              statusLabel(row.status)
            }}</span>
          </div>
          <div class="sop-ledger__title">{{ row.title }}</div>
          <div class="sop-ledger__meta"
            >版本 {{ row.version || '-' }} · 绑定工序 {{ row.routeStepIds?.length || 0 }} 道</div
          >
        </template>
      </el-table-column>
      <el-table-column label="生效期" min-width="180">
        <template #default="{ row }"
          >{{ row.effectiveDate || '-' }} ~ {{ row.expireDate || '-' }}</template
        >
      </el-table-column>
      <el-table-column label="备注" min-width="160" prop="remark" />
      <el-table-column label="创建时间" min-width="150">
        <template #default="{ row }">{{ formatDateValue(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          <el-button
            v-if="Number(row.status) !== 1"
            link
            type="primary"
            @click="openForm('edit', row)"
            >编辑</el-button
          >
          <el-button
            v-if="Number(row.status) === 0"
            link
            type="success"
            @click="handleStatus(row, 1)"
          >
            发布
          </el-button>
          <el-button
            v-if="Number(row.status) === 1"
            link
            type="warning"
            @click="handleStatus(row, 2)"
          >
            停用
          </el-button>
          <el-button v-if="Number(row.status) !== 1" link type="danger" @click="handleDelete(row)">
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

  <!-- 详情弹窗 -->
  <Dialog v-model="detailVisible" title="查看 SOP" width="640">
    <el-descriptions v-if="detailForm.sopNo" :column="2" border>
      <el-descriptions-item label="SOP 编码">{{ detailForm.sopNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <span class="sop-badge" :class="statusBadgeClass(detailForm.status)">{{
          statusLabel(detailForm.status)
        }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="标题">{{ detailForm.title }}</el-descriptions-item>
      <el-descriptions-item label="版本">{{ detailForm.version || '-' }}</el-descriptions-item>
      <el-descriptions-item label="生效期"
        >{{ detailForm.effectiveDate || '-' }} ~
        {{ detailForm.expireDate || '-' }}</el-descriptions-item
      >
      <el-descriptions-item label="创建时间">{{ formatDateValue(detailForm.createTime) }}</el-descriptions-item>
    </el-descriptions>
    <div v-if="detailForm.stepInfos?.length" class="sop-detail-content">
      <div class="sop-detail-content__label">绑定工序（{{ detailForm.stepInfos.length }} 道）</div>
      <div class="sop-detail-steps">
        <span v-for="step in detailForm.stepInfos" :key="step.id" class="sop-step-tag">
          {{ step.stepNo }} · {{ step.stepName }}（{{ step.stepCode }}）
        </span>
      </div>
    </div>
    <div v-if="detailForm.attachmentUrl" class="sop-detail-content">
      <div class="sop-detail-content__label">附件</div>
      <el-link type="primary" :href="detailForm.attachmentUrl" target="_blank">{{
        detailForm.attachmentUrl
      }}</el-link>
    </div>
    <div v-if="detailForm.content" class="sop-detail-content">
      <div class="sop-detail-content__label">步骤内容</div>
      <pre class="sop-detail-content__body">{{ detailForm.content }}</pre>
    </div>
    <div v-if="detailForm.remark" class="sop-detail-content">
      <div class="sop-detail-content__label">备注</div>
      <div class="sop-detail-content__body">{{ detailForm.remark }}</div>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </Dialog>

  <!-- 新增/编辑弹窗 -->
  <Dialog
    v-model="formVisible"
    :title="formMode === 'create' ? '新增 SOP' : '编辑 SOP'"
    width="680"
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
      <el-form-item label="SOP 编码" prop="sopNo">
        <el-input v-model="form.sopNo" placeholder="如 SOP-001" maxlength="64" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入 SOP 标题" maxlength="200" />
      </el-form-item>
      <el-form-item label="版本" prop="version">
        <el-input v-model="form.version" placeholder="如 V1.0" maxlength="32" />
      </el-form-item>
      <el-form-item label="绑定工序" prop="routeStepIds">
        <div class="sop-bind">
          <el-select
            v-model="selectedRouteId"
            filterable
            clearable
            placeholder="选择工艺路线以加载工序"
            style="width: 100%; margin-bottom: 8px"
            @change="loadRouteSteps"
          >
            <el-option
              v-for="item in routeList"
              :key="item.id"
              :label="`${item.routeName}（${item.routeCode}）`"
              :value="item.id"
            />
          </el-select>
          <el-checkbox-group v-model="form.routeStepIds" class="sop-bind__steps">
            <el-checkbox v-for="step in routeStepOptions" :key="step.id" :value="step.id">
              {{ step.stepNo }} · {{ step.stepName }}（{{ step.stepCode }}）
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </el-form-item>
      <el-form-item label="步骤内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="结构化步骤内容（每行一步，如：1. 装夹工件）"
        />
      </el-form-item>
      <el-form-item label="附件地址">
        <el-input v-model="form.attachmentUrl" placeholder="图片/文档访问地址" maxlength="500" />
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker
          v-model="form.effectiveDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择生效日期"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="失效日期">
        <el-date-picker
          v-model="form.expireDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择失效日期"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
    </template>
  </Dialog>

  <!-- OCR 导入弹窗 -->
  <Dialog v-model="ocrVisible" title="OCR 导入 SOP" width="680">
    <el-upload
      drag
      :auto-upload="false"
      :limit="1"
      accept="image/*"
      :on-change="handleOcrFileChange"
      :file-list="ocrFileList"
    >
      <Icon icon="ep:upload-filled" class="sop-ocr-upload-icon" />
      <div class="el-upload__text">拖拽或点击上传 SOP 图片</div>
    </el-upload>
    <el-alert
      v-if="ocrError"
      class="mt-12px"
      type="error"
      :title="ocrError"
      show-icon
      :closable="false"
    />
    <template v-if="ocrRecord">
      <el-form label-width="100px" class="mt-12px">
        <el-form-item label="SOP 编码">
          <el-input v-model="confirmForm.sopNo" placeholder="如 SOP-OCR-001" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="confirmForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="识别内容" required>
          <el-input
            v-model="confirmForm.content"
            type="textarea"
            :rows="8"
            placeholder="校对 OCR 识别结果"
          />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <el-button @click="ocrVisible = false">关闭</el-button>
      <el-button
        v-if="ocrRecord"
        type="success"
        :loading="ocrConfirmLoading"
        @click="handleOcrConfirm"
      >
        确认生成草稿
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { ProcessRouteApi, ProcessRouteVO } from '@/api/erp/manufacturing/process-route'
import { SopApi, SopDocumentSaveReqVO, SopDocumentVO, SopImportRecordVO } from '@/api/mes/sop'

defineOptions({ name: 'MesSop' })

const message = useMessage()

const queryFormRef = ref()
const formRef = ref<FormInstance>()
const listLoading = ref(false)
const submitLoading = ref(false)
const ocrConfirmLoading = ref(false)
const formVisible = ref(false)
const ocrVisible = ref(false)
const detailVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')

const list = ref<SopDocumentVO[]>([])
const total = ref(0)
const detailForm = ref<Partial<SopDocumentVO>>({})
const routeList = ref<ProcessRouteVO[]>([])
const routeStepOptions = ref<{ id: number; stepNo: number; stepCode: string; stepName: string }[]>(
  []
)
const selectedRouteId = ref<number>()
const ocrFileList = ref<any[]>([])
const ocrError = ref('')
const ocrRecord = ref<SopImportRecordVO>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sopNo: undefined as string | undefined,
  title: undefined as string | undefined,
  status: undefined as number | undefined
})

const form = reactive<SopDocumentSaveReqVO>({
  id: undefined,
  sopNo: '',
  title: '',
  version: 'V1.0',
  content: '',
  attachmentUrl: undefined,
  effectiveDate: undefined,
  expireDate: undefined,
  remark: undefined,
  routeStepIds: []
})

const confirmForm = reactive({
  sopNo: '',
  title: '',
  content: ''
})

const formRules: FormRules = {
  sopNo: [{ required: true, message: '请输入 SOP 编码', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  routeStepIds: [
    { required: true, type: 'array', min: 1, message: '至少绑定一道工序', trigger: 'change' }
  ]
}

const statusLabel = (status?: number) => {
  if (status === 0) return '草稿'
  if (status === 1) return '已发布'
  if (status === 2) return '已停用'
  return '-'
}

const statusBadgeClass = (status?: number) => {
  if (status === 0) return 'sop-badge--slate'
  if (status === 1) return 'sop-badge--success'
  if (status === 2) return 'sop-badge--info'
  return 'sop-badge--slate'
}

const formatDateValue = (value?: string | Date | number) =>
  value ? formatDate(value, 'YYYY-MM-DD HH:mm') : '-'

const getList = async () => {
  listLoading.value = true
  try {
    const data = await SopApi.getSopPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    listLoading.value = false
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

const loadRoutes = async () => {
  try {
    const data = await ProcessRouteApi.getProcessRoutePage({ pageNo: 1, pageSize: 100, status: 1 })
    routeList.value = data.list || []
  } catch {
    routeList.value = []
  }
}

const loadRouteSteps = async (routeId?: number) => {
  routeStepOptions.value = []
  form.routeStepIds = []
  if (!routeId) return
  try {
    const data = await ProcessRouteApi.getProcessRoute(routeId)
    routeStepOptions.value = (data.steps || []).map((s) => ({
      id: s.id,
      stepNo: s.stepNo,
      stepCode: s.stepCode,
      stepName: s.stepName
    }))
  } catch {
    routeStepOptions.value = []
  }
}

const openDetail = async (row: SopDocumentVO) => {
  if (!row.id) return
  detailVisible.value = true
  try {
    detailForm.value = await SopApi.getSop(row.id)
  } catch {
    detailForm.value = {}
    message.error('SOP 详情加载失败')
  }
}

const openForm = async (mode: 'create' | 'edit', row?: SopDocumentVO) => {
  formMode.value = mode
  Object.assign(form, {
    id: undefined,
    sopNo: '',
    title: '',
    version: 'V1.0',
    content: '',
    attachmentUrl: undefined,
    effectiveDate: undefined,
    expireDate: undefined,
    remark: undefined,
    routeStepIds: []
  })
  selectedRouteId.value = undefined
  routeStepOptions.value = []
  formVisible.value = true
  if (mode === 'edit' && row?.id) {
    const data = await SopApi.getSop(row.id)
    Object.assign(form, {
      id: data.id,
      sopNo: data.sopNo,
      title: data.title,
      version: data.version || 'V1.0',
      content: data.content || '',
      attachmentUrl: data.attachmentUrl,
      effectiveDate: data.effectiveDate,
      expireDate: data.expireDate,
      remark: data.remark,
      routeStepIds: data.routeStepIds || []
    })
    // 回显绑定工序时，从路线列表加载对应工序选项（简化：第一道工序所在路线）
    if (form.routeStepIds.length) {
      await loadRoutes()
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (formMode.value === 'create') {
      await SopApi.createSop(form)
      message.success('创建 SOP 成功')
    } else {
      await SopApi.updateSop(form)
      message.success('更新 SOP 成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleStatus = async (row: SopDocumentVO, status: number) => {
  if (!row.id) return
  const action = status === 1 ? '发布' : '停用'
  try {
    await message.confirm(`确定${action} SOP「${row.sopNo}」吗？`)
  } catch {
    return
  }
  await SopApi.updateStatus(row.id, status)
  message.success(`${action}成功`)
  await getList()
}

const handleDelete = async (row: SopDocumentVO) => {
  if (!row.id) return
  try {
    await message.confirm(`确定删除 SOP「${row.sopNo}」吗？`)
  } catch {
    return
  }
  await SopApi.deleteSop(row.id)
  message.success('删除成功')
  await getList()
}

const openOcrDialog = () => {
  ocrVisible.value = true
  ocrFileList.value = []
  ocrError.value = ''
  ocrRecord.value = undefined
  confirmForm.sopNo = ''
  confirmForm.title = ''
  confirmForm.content = ''
}

const handleOcrFileChange = async (file: any) => {
  ocrError.value = ''
  ocrRecord.value = undefined
  if (!file.raw) return
  try {
    ocrRecord.value = await SopApi.ocrImport(file.raw)
    confirmForm.content = ocrRecord.value?.ocrText || ''
    if (!confirmForm.sopNo) {
      confirmForm.sopNo = 'SOP-OCR-' + Date.now().toString().slice(-6)
    }
    if (!confirmForm.title) {
      confirmForm.title = file.name?.replace(/\.[^.]+$/, '') || 'OCR 导入 SOP'
    }
  } catch {
    ocrError.value = 'OCR 识别失败，请检查 Paddle 服务或重试'
  }
}

const handleOcrConfirm = async () => {
  if (!ocrRecord.value?.id) return
  if (!confirmForm.sopNo || !confirmForm.title) {
    message.warning('请填写 SOP 编码和标题')
    return
  }
  ocrConfirmLoading.value = true
  try {
    await SopApi.confirmImport({
      importRecordId: ocrRecord.value.id,
      sopNo: confirmForm.sopNo,
      title: confirmForm.title,
      content: confirmForm.content
    })
    message.success('已生成 SOP 草稿，可继续编辑后发布')
    ocrVisible.value = false
    await getList()
  } finally {
    ocrConfirmLoading.value = false
  }
}

onMounted(async () => {
  getList()
  await loadRoutes()
})
</script>

<style scoped>
.sop-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.sop-hero__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.sop-hero__breadcrumb {
  color: var(--erp-slate-400);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  margin-bottom: 6px;
}

.sop-page__title {
  color: var(--erp-slate-900);
  font-size: 28px;
  font-weight: 800;
  line-height: 36px;
}

.sop-page__filter-card,
.sop-page__list-card {
  border: 1px solid var(--erp-slate-200);
  border-radius: 20px;
  background: var(--erp-surface-white);
  box-shadow: var(--erp-shadow-sm);
}

.sop-query {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 6px;
    color: var(--erp-slate-600);
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid var(--erp-slate-200);
    border-radius: 10px;
    background: var(--erp-slate-50);
    box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
  }
}

.sop-query__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.sop-query__footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.sop-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--erp-slate-200);
  flex-wrap: wrap;
}

.sop-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sop-ledger__top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sop-ledger__no {
  color: var(--erp-slate-900);
  font-weight: 800;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.sop-ledger__title {
  color: var(--erp-slate-700);
  font-weight: 600;
  margin-top: 4px;
}

.sop-ledger__meta {
  color: var(--erp-slate-400);
  font-size: 11px;
  margin-top: 2px;
}

.sop-badge {
  display: inline-flex;
  align-items: center;
  min-height: 20px;
  padding: 1px 8px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
}

.sop-badge--slate {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-600);
}

.sop-badge--success {
  background: var(--erp-success-50);
  border-color: var(--erp-success-200);
  color: var(--erp-success-600);
}

.sop-badge--info {
  background: var(--erp-slate-100);
  border-color: var(--erp-slate-200);
  color: var(--erp-slate-500);
}

.sop-bind__steps {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  background: var(--erp-slate-50);
  max-height: 160px;
  overflow-y: auto;
  width: 100%;
}

.sop-ocr-upload-icon {
  font-size: 40px;
  color: var(--erp-slate-400);
  margin-bottom: 8px;
}

.sop-empty {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--erp-slate-500);
}

.sop-empty__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: var(--erp-stat-gradient-blue);
  color: var(--erp-primary-600);
  font-size: 22px;
}

.sop-empty__title {
  color: var(--erp-slate-900);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

@media (max-width: 1024px) {
  .sop-query__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

.sop-detail-content {
  margin-top: 16px;
}

.sop-detail-content__label {
  color: var(--erp-slate-600);
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.sop-detail-content__body {
  margin: 0;
  padding: 12px 14px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 10px;
  background: var(--erp-slate-50);
  color: var(--erp-slate-700);
  font-size: 13px;
  line-height: 22px;
  white-space: pre-wrap;
  word-break: break-word;
}

.sop-detail-steps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sop-step-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border: 1px solid var(--erp-slate-200);
  border-radius: 999px;
  background: var(--erp-surface-white);
  color: var(--erp-slate-700);
  font-size: 12px;
  font-weight: 600;
}
</style>
