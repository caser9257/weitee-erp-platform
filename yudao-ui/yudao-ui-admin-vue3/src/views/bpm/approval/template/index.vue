<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="模板名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="模板分类" prop="category">
        <el-select
          v-model="queryParams.category"
          placeholder="请选择分类"
          clearable
          class="!w-200px"
        >
          <el-option label="OA" value="OA" />
          <el-option label="财务" value="财务" />
          <el-option label="采购" value="采购" />
          <el-option label="销售" value="销售" />
          <el-option label="人事" value="人事" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 模板卡片列表 -->
  <ContentWrap>
    <div v-loading="loading" class="template-grid">
      <div
        v-for="template in list"
        :key="template.id"
        class="template-card"
        @click="handleViewDetail(template)"
      >
        <div class="template-card__icon">
          <Icon :icon="template.icon || 'ep:document'" :size="32" />
        </div>
        <div class="template-card__content">
          <div class="template-card__name">{{ template.name }}</div>
          <div class="template-card__desc">{{ template.description }}</div>
          <div class="template-card__meta">
            <el-tag size="small" effect="plain">{{ template.category }}</el-tag>
            <span class="template-card__count">已使用 {{ template.useCount }} 次</span>
          </div>
        </div>
        <div class="template-card__action">
          <el-button
            type="primary"
            size="small"
            :loading="useTemplateLoading"
            @click.stop="handleUseTemplate(template)"
          >
            <Icon icon="ep:check" class="mr-5px" />
            一键启用
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && list.length === 0" description="暂无模板" />
  </ContentWrap>

  <!-- 模板详情弹窗 -->
  <TemplateDetail ref="templateDetailRef" @use="handleUseFromDetail" />

  <!-- 流程设计器弹窗 -->
  <TemplateFlowDesigner ref="flowDesignerRef" @success="handleFlowDesignerSuccess" />
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalTemplateApi from '@/api/bpm/approval/template'
import TemplateDetail from './TemplateDetail.vue'
import TemplateFlowDesigner from './TemplateFlowDesigner.vue'

defineOptions({ name: 'BpmApprovalTemplate' })

const message = useMessage()
const router = useRouter()

const loading = ref(true)
const useTemplateLoading = ref(false)
const list = ref<ApprovalTemplateApi.ApprovalTemplateVO[]>([])
const queryParams = reactive<ApprovalTemplateApi.ApprovalTemplatePageReqVO>({
  name: undefined,
  category: undefined,
  status: undefined,
  pageNo: 1,
  pageSize: 50
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalTemplateApi.getApprovalTemplatePage(queryParams)
    console.log('[BpmApprovalTemplate] getList response:', data)
    // 确保 data.list 存在
    list.value = data?.list || []
    console.log('[BpmApprovalTemplate] list.value:', list.value)
  } catch (error: any) {
    console.error('[BpmApprovalTemplate] getList failed:', error)
    list.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = undefined
  queryParams.category = undefined
  queryParams.status = undefined
  handleQuery()
}

const templateDetailRef = ref()
const flowDesignerRef = ref()

const handleViewDetail = (template: ApprovalTemplateApi.ApprovalTemplateVO) => {
  templateDetailRef.value.open(template.id)
}

const handleUseFromDetail = async (template: ApprovalTemplateApi.ApprovalTemplateVO) => {
  await handleUseTemplate(template)
}

const handleUseTemplate = async (template: ApprovalTemplateApi.ApprovalTemplateVO) => {
  // 直接打开流程设计器，让用户可以查看和修改流程
  flowDesignerRef.value.open(template)
}

const handleFlowDesignerSuccess = async (result: any) => {
  // 流程设计器保存成功后，刷新列表
  await getList()
  message.success('审批流程已创建并启用！')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.template-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.template-card__icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
  color: var(--el-color-primary);
  margin-right: 12px;
}

.template-card__content {
  flex: 1;
  min-width: 0;
}

.template-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.template-card__desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-card__count {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.template-card__action {
  flex-shrink: 0;
  margin-left: 12px;
}
</style>
