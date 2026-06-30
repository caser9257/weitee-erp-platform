<template>
  <div class="contract-import-page">
    <ContentWrap class="contract-import-page__hero">
      <div class="contract-import-page__hero-main">
        <div class="contract-import-page__title">合同导入</div>
        <div class="contract-import-page__desc">
          下载标准模板后批量导入合同，系统会返回成功/失败明细，适合销售演示和合同批量接入场景。
        </div>
      </div>
      <div class="contract-import-page__hero-actions">
        <el-button
          type="primary"
          plain
          :loading="templateLoading"
          v-hasPermi="['erp:contract-import:template']"
          @click="handleDownloadTemplate"
        >
          <Icon icon="ep:download" class="mr-5px" />下载模板
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="contract-import-page__panel">
      <div class="contract-import-page__section-head">
        <div class="contract-import-page__section-title">导入说明</div>
      </div>
      <div class="contract-import-page__steps">
        <div class="contract-import-page__step-card">
          <div class="contract-import-page__step-index">1</div>
          <div class="contract-import-page__step-content">
            <div class="contract-import-page__step-title">下载模板</div>
            <div class="contract-import-page__step-desc">使用系统模板整理合同编号、客户、金额等基础字段。</div>
          </div>
        </div>
        <div class="contract-import-page__step-card">
          <div class="contract-import-page__step-index">2</div>
          <div class="contract-import-page__step-content">
            <div class="contract-import-page__step-title">上传文件</div>
            <div class="contract-import-page__step-desc">支持 `.xls` / `.xlsx`，每次仅导入一个文件。</div>
          </div>
        </div>
        <div class="contract-import-page__step-card">
          <div class="contract-import-page__step-index">3</div>
          <div class="contract-import-page__step-content">
            <div class="contract-import-page__step-title">查看结果</div>
            <div class="contract-import-page__step-desc">系统返回总数、成功数、失败明细，可按行定位问题。</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="contract-import-page__panel">
      <div class="contract-import-page__section-head">
        <div class="contract-import-page__section-title">上传合同文件</div>
        <div class="contract-import-page__summary">
          <span class="contract-import-page__summary-pill">总行数 {{ result.totalCount || 0 }}</span>
          <span class="contract-import-page__summary-pill contract-import-page__summary-pill--success">成功 {{ result.successCount || 0 }}</span>
          <span class="contract-import-page__summary-pill contract-import-page__summary-pill--danger">失败 {{ result.failCount || 0 }}</span>
        </div>
      </div>

      <el-upload
        ref="uploadRef"
        v-model:file-list="fileList"
        :auto-upload="false"
        :limit="1"
        :disabled="importLoading"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls"
        action="none"
        drag
        class="contract-import-page__upload"
      >
        <Icon icon="ep:upload-filled" :size="52" class="contract-import-page__upload-icon" />
        <div class="el-upload__text">将合同文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="contract-import-page__upload-tip">
            仅允许上传 Excel 模板文件，建议单次导入前先下载最新模板。
          </div>
        </template>
      </el-upload>

      <div class="contract-import-page__upload-actions">
        <el-button type="primary" :loading="importLoading" v-hasPermi="['erp:contract-import:import']" @click="handleImport">
          <Icon icon="ep:upload" class="mr-5px" />开始导入
        </el-button>
        <el-button :disabled="importLoading" @click="handleReset">重置文件</el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="contract-import-page__panel">
      <div class="contract-import-page__section-head">
        <div class="contract-import-page__section-title">导入结果</div>
      </div>
      <el-empty v-if="!hasResult" description="导入完成后将在此显示结果明细" />
      <template v-else>
        <el-alert
          v-if="result.failCount"
          type="warning"
          :closable="false"
          show-icon
          class="mb-12px"
          title="部分合同导入失败，请按行号和原因修正后重新导入"
        />
        <el-table :data="result.failDetails || []" stripe>
          <el-table-column label="行号" prop="rowNumber" width="90" align="center" />
          <el-table-column label="合同编号" prop="contractNo" min-width="180" />
          <el-table-column label="失败原因" prop="reason" min-width="260" show-overflow-tooltip />
        </el-table>
      </template>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import type { UploadInstance, UploadProps, UploadUserFile } from 'element-plus'
import download from '@/utils/download'
import { ContractImportApi, type ContractImportResultVO } from '@/api/erp/sale/contract-import'

defineOptions({ name: 'ErpContractImportPage' })

const message = useMessage()
const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])
const importLoading = ref(false)
const templateLoading = ref(false)
const result = ref<ContractImportResultVO>({})

const hasResult = computed(() =>
  Boolean(result.value.totalCount || result.value.successCount || result.value.failCount)
)

const handleExceed: UploadProps['onExceed'] = () => {
  message.error('最多只能上传一个文件')
}

const handleReset = async () => {
  fileList.value = []
  result.value = {}
  await nextTick()
  uploadRef.value?.clearFiles()
}

const handleDownloadTemplate = async () => {
  try {
    templateLoading.value = true
    const data = await ContractImportApi.downloadTemplate()
    download.excel(data, '合同导入模板.xlsx')
  } finally {
    templateLoading.value = false
  }
}

const handleImport = async () => {
  if (!fileList.value.length) {
    message.error('请先选择导入文件')
    return
  }
  const formData = new FormData()
  formData.append('file', fileList.value[0].raw as Blob)
  try {
    importLoading.value = true
    const response = await ContractImportApi.importContracts(formData)
    result.value = (response as any).data || response
    message.success('合同导入已完成')
  } catch {
    message.error('合同导入失败，请稍后重试')
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.contract-import-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contract-import-page__hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.contract-import-page__title {
  font-size: 22px;
  font-weight: 700;
  color: var(--erp-slate-900, #0f172a);
}

.contract-import-page__desc {
  margin-top: 8px;
  line-height: 1.7;
  color: var(--erp-slate-500, #64748b);
}

.contract-import-page__section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.contract-import-page__section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--erp-slate-900, #0f172a);
}

.contract-import-page__steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.contract-import-page__step-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--erp-slate-200, #e2e8f0);
  border-radius: 12px;
  background: var(--erp-slate-50, #f8fafc);
}

.contract-import-page__step-index {
  display: flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--erp-primary-600, #2563eb);
  color: #fff;
  font-weight: 700;
}

.contract-import-page__step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-900, #0f172a);
}

.contract-import-page__step-desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--erp-slate-500, #64748b);
}

.contract-import-page__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.contract-import-page__summary-pill {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--erp-primary-50, #eff6ff);
  color: var(--erp-primary-600, #2563eb);
  font-size: 12px;
  font-weight: 600;
}

.contract-import-page__summary-pill--success {
  background: var(--erp-success-50, #ecfdf5);
  color: var(--erp-success-600, #059669);
}

.contract-import-page__summary-pill--danger {
  background: var(--erp-danger-50, #fff1f2);
  color: var(--erp-danger-600, #e11d48);
}

.contract-import-page__upload {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 28px 0;
    border-color: var(--erp-slate-200, #e2e8f0);
    background: var(--erp-slate-50, #f8fafc);
  }
}

.contract-import-page__upload-icon {
  color: var(--erp-slate-300, #cbd5e1);
}

.contract-import-page__upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--erp-slate-500, #64748b);
}

.contract-import-page__upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .contract-import-page__hero,
  .contract-import-page__section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .contract-import-page__steps {
    grid-template-columns: 1fr;
  }
}
</style>
