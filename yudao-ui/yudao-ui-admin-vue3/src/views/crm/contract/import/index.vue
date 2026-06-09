<template>
  <div class="contract-import-page">
    <!-- 页头卡片 -->
    <ContentWrap class="contract-import-page__header">
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-header__title">合同导入</div>
          <div class="page-header__desc">批量导入合同数据，支持 Excel 格式</div>
        </div>
        <div class="page-header__actions">
          <el-button @click="handleDownloadTemplate">
            <Icon icon="ep:download" class="mr-5px" /> 下载模板
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 导入区域 -->
    <ContentWrap class="contract-import-page__upload">
      <div class="upload-area">
        <el-upload
          ref="uploadRef"
          class="upload-dragger"
          drag
          :auto-upload="false"
          :limit="1"
          :on-exceed="handleExceed"
          :on-change="handleFileChange"
          accept=".xlsx,.xls"
        >
          <Icon icon="ep:upload" class="upload-icon" />
          <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="upload-tip">只能上传 .xlsx / .xls 文件，且不超过 10MB</div>
          </template>
        </el-upload>

        <div class="upload-actions">
          <el-button type="primary" :loading="importing" :disabled="!selectedFile" @click="handleImport">
            <Icon icon="ep:upload" class="mr-5px" /> 开始导入
          </el-button>
          <el-button @click="handleClear">
            <Icon icon="ep:refresh-left" class="mr-5px" /> 清除
          </el-button>
        </div>
      </div>
    </ContentWrap>

    <!-- 导入说明 -->
    <ContentWrap class="contract-import-page__help">
      <div class="section-title">导入说明</div>
      <div class="help-content">
        <div class="help-item">
          <div class="help-item__label">必填字段</div>
          <div class="help-item__value">合同编号、合同名称、客户名称、负责人、下单日期</div>
        </div>
        <div class="help-item">
          <div class="help-item__label">发货放行规则</div>
          <div class="help-item__value">SIGN_AND_SHIP（签约即发）、AFTER_PAYMENT（到账后发）、AFTER_PREPAYMENT（预付款后发）、FINANCE_APPROVAL（财务审核后发）</div>
        </div>
        <div class="help-item">
          <div class="help-item__label">开票触发条件</div>
          <div class="help-item__value">PREPAYMENT_FULL（预付款全额开票）、AFTER_SHIPMENT（发货后开票）、AFTER_DELIVERY_RECEIPT（交付收款后开票）、MANUAL（手工决定）</div>
        </div>
        <div class="help-item">
          <div class="help-item__label">收款规则</div>
          <div class="help-item__value">BEFORE_SHIPMENT（发货前付款）、ON_SHIPMENT（发货时付款）、AFTER_SHIPMENT（发货后约定期限付款）</div>
        </div>
        <div class="help-item">
          <div class="help-item__label">需要财务审核/验收</div>
          <div class="help-item__value">填 1 表示是，填 0 或留空表示否</div>
        </div>
      </div>
    </ContentWrap>

    <!-- 导入结果 -->
    <ContentWrap v-if="importResult" class="contract-import-page__result">
      <div class="section-title">导入结果</div>
      <div class="result-summary">
        <div class="result-item">
          <div class="result-item__label">总行数</div>
          <div class="result-item__value">{{ importResult.totalCount }}</div>
        </div>
        <div class="result-item result-item--success">
          <div class="result-item__label">成功</div>
          <div class="result-item__value">{{ importResult.successCount }}</div>
        </div>
        <div class="result-item result-item--danger">
          <div class="result-item__label">失败</div>
          <div class="result-item__value">{{ importResult.failCount }}</div>
        </div>
      </div>

      <div v-if="importResult.failDetails && importResult.failDetails.length" class="result-errors">
        <div class="result-errors__title">失败详情</div>
        <el-table :data="importResult.failDetails" stripe size="small">
          <el-table-column label="行号" prop="rowNumber" width="80" />
          <el-table-column label="合同编号" prop="contractNo" min-width="120" />
          <el-table-column label="失败原因" prop="reason" min-width="200" />
        </el-table>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance, UploadFile } from 'element-plus'

defineOptions({ name: 'ErpContractImportPage' })

// API 接口
const ContractImportApi = {
  downloadTemplate: async () => {
    const { request } = await import('@/config/axios')
    return await request.download({ url: '/erp/contract-import/template' })
  },
  importContracts: async (file: File) => {
    const { request } = await import('@/config/axios')
    const formData = new FormData()
    formData.append('file', file)
    return await request.post({ url: '/erp/contract-import/import', data: formData })
  }
}

const uploadRef = ref<UploadInstance>()
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const importResult = ref<any>(null)

const handleDownloadTemplate = async () => {
  await ContractImportApi.downloadTemplate()
  ElMessage.success('模板下载成功')
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先清除已选文件')
}

const handleFileChange = (file: UploadFile) => {
  selectedFile.value = file.raw || null
}

const handleClear = () => {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  importResult.value = null
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importing.value = true
  try {
    importResult.value = await ContractImportApi.importContracts(selectedFile.value)
    if (importResult.value.failCount === 0) {
      ElMessage.success(`导入成功，共 ${importResult.value.successCount} 条`)
    } else {
      ElMessage.warning(`导入完成，成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failCount} 条`)
    }
  } finally {
    importing.value = false
  }
}
</script>

<style scoped lang="scss">
.contract-import-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-header__title {
    font-size: 20px;
    font-weight: 800;
    color: #0f172a;
  }

  .page-header__desc {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 16px;
  }

  .upload-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  .upload-dragger {
    width: 100%;
    max-width: 600px;
  }

  .upload-icon {
    font-size: 48px;
    color: #94a3b8;
  }

  .upload-text {
    margin-top: 12px;
    color: #64748b;

    em {
      color: #3b82f6;
      font-style: normal;
    }
  }

  .upload-tip {
    margin-top: 8px;
    color: #94a3b8;
    font-size: 12px;
  }

  .upload-actions {
    display: flex;
    gap: 12px;
  }

  .help-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .help-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 8px;
  }

  .help-item__label {
    min-width: 120px;
    font-weight: 600;
    color: #334155;
  }

  .help-item__value {
    color: #64748b;
    font-size: 13px;
  }

  .result-summary {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;
  }

  .result-item {
    padding: 16px 24px;
    background: #f8fafc;
    border-radius: 8px;
    text-align: center;
  }

  .result-item__label {
    color: #64748b;
    font-size: 12px;
  }

  .result-item__value {
    margin-top: 4px;
    font-size: 24px;
    font-weight: 800;
    color: #0f172a;
  }

  .result-item--success .result-item__value {
    color: #10b981;
  }

  .result-item--danger .result-item__value {
    color: #ef4444;
  }

  .result-errors {
    margin-top: 16px;
  }

  .result-errors__title {
    font-weight: 600;
    color: #0f172a;
    margin-bottom: 12px;
  }
}
</style>
