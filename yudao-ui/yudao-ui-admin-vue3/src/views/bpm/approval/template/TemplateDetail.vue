<template>
  <el-drawer
    v-model="visible"
    title="模板详情"
    size="600px"
    :destroy-on-close="true"
  >
    <div v-loading="loading" class="template-detail">
      <!-- 模板基本信息 -->
      <div class="detail-section">
        <div class="section-header">
          <Icon icon="ep:document" class="mr-8px text-[var(--el-color-primary)]" />
          <span class="font-medium">基本信息</span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">模板编码</span>
            <span class="info-value font-mono">{{ detail?.code }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">模板名称</span>
            <span class="info-value">{{ detail?.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">模板分类</span>
            <el-tag size="small" effect="plain">{{ detail?.category }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">使用次数</span>
            <span class="info-value">{{ detail?.useCount }} 次</span>
          </div>
          <div class="info-item col-span-2">
            <span class="info-label">模板描述</span>
            <span class="info-value">{{ detail?.description || '暂无描述' }}</span>
          </div>
        </div>
      </div>

      <!-- 表单配置 -->
      <div class="detail-section">
        <div class="section-header">
          <Icon icon="ep:edit" class="mr-8px text-[var(--el-color-success)]" />
          <span class="font-medium">表单配置</span>
        </div>
        <div v-if="formFields.length > 0" class="config-content">
          <div v-for="field in formFields" :key="field.field" class="field-item">
            <div class="field-header">
              <span class="field-name">{{ field.label }}</span>
              <el-tag size="small" :type="getFieldTagType(field.type)">{{ field.type }}</el-tag>
            </div>
            <div class="field-meta">
              <span v-if="field.required" class="text-red-500">必填</span>
              <span v-if="field.placeholder" class="text-gray-400">{{ field.placeholder }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无表单配置" :image-size="60" />
      </div>

      <!-- 流程配置 -->
      <div class="detail-section">
        <div class="section-header">
          <Icon icon="ep:share" class="mr-8px text-[var(--el-color-warning)]" />
          <span class="font-medium">流程配置</span>
        </div>
        <div v-if="flowNodes.length > 0" class="config-content">
          <div class="flow-timeline">
            <div v-for="(node, index) in flowNodes" :key="index" class="flow-node">
              <div class="flow-node__dot" :class="getNodeTypeClass(node.type)"></div>
              <div class="flow-node__content">
                <div class="flow-node__name">{{ node.name }}</div>
                <div class="flow-node__type">{{ getNodeTypeLabel(node.type) }}</div>
                <div v-if="node.assigneeType" class="flow-node__assignee">
                  审批人: {{ getAssigneeLabel(node.assigneeType) }}
                </div>
              </div>
              <div v-if="index < flowNodes.length - 1" class="flow-node__line"></div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无流程配置" :image-size="60" />
      </div>

      <!-- 通知配置 -->
      <div class="detail-section">
        <div class="section-header">
          <Icon icon="ep:bell" class="mr-8px text-[var(--el-color-info)]" />
          <span class="font-medium">通知配置</span>
        </div>
        <div v-if="notifyRules.length > 0" class="config-content">
          <div v-for="(rule, index) in notifyRules" :key="index" class="notify-item">
            <div class="notify-header">
              <el-tag size="small" effect="plain">{{ rule.event }}</el-tag>
              <span class="notify-channel">{{ rule.channel }}</span>
            </div>
            <div v-if="rule.template" class="notify-template">
              模板: {{ rule.template }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无通知配置" :image-size="60" />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleUse">
        <Icon icon="ep:check" class="mr-5px" />
        使用此模板
      </el-button>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalTemplateApi from '@/api/bpm/approval/template'

defineOptions({ name: 'TemplateDetail' })

const emit = defineEmits(['use'])

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const detail = ref<ApprovalTemplateApi.ApprovalTemplateVO | null>(null)

// 解析表单配置
const formFields = computed(() => {
  if (!detail.value?.formConfig?.fields) return []
  return detail.value.formConfig.fields as any[]
})

// 解析流程配置
const flowNodes = computed(() => {
  if (!detail.value?.flowConfig?.nodes) return []
  return detail.value.flowConfig.nodes as any[]
})

// 解析通知配置
const notifyRules = computed(() => {
  if (!detail.value?.notifyConfig?.rules) return []
  return detail.value.notifyConfig.rules as any[]
})

// 打开详情
const open = async (templateId: number) => {
  visible.value = true
  loading.value = true
  try {
    detail.value = await ApprovalTemplateApi.getApprovalTemplate(templateId)
  } catch (error: any) {
    message.error('获取模板详情失败：' + (error?.message || '未知错误'))
    visible.value = false
  } finally {
    loading.value = false
  }
}

// 使用模板
const handleUse = async () => {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      `确认使用模板「${detail.value.name}」创建审批流程？将自动创建审批场景、方案和版本。`,
      '使用模板',
      { type: 'info' }
    )
    emit('use', detail.value)
    visible.value = false
  } catch {
    // 用户取消
  }
}

// 字段类型标签样式
const getFieldTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    input: '',
    textarea: '',
    number: 'success',
    select: 'warning',
    radio: 'warning',
    checkbox: 'warning',
    date: 'info',
    time: 'info',
    switch: 'danger'
  }
  return typeMap[type] || 'info'
}

// 节点类型样式
const getNodeTypeClass = (type: string) => {
  const classMap: Record<string, string> = {
    start: 'node-start',
    approval: 'node-approval',
    condition: 'node-condition',
    cc: 'node-cc',
    end: 'node-end'
  }
  return classMap[type] || 'node-default'
}

// 节点类型标签
const getNodeTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    start: '开始节点',
    approval: '审批节点',
    condition: '条件分支',
    cc: '抄送节点',
    end: '结束节点'
  }
  return labelMap[type] || type
}

// 审批人标签
const getAssigneeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    user: '指定用户',
    role: '指定角色',
    dept_leader: '部门负责人',
    initiator: '发起人自选',
    superiors: '上级领导'
  }
  return labelMap[type] || type
}

defineExpose({ open })
</script>

<style scoped>
.template-detail {
  padding: 0 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.col-span-2 {
  grid-column: span 2;
}

.info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.info-value {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.config-content {
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  padding: 12px;
}

.field-item {
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  margin-bottom: 8px;
}

.field-item:last-child {
  margin-bottom: 0;
}

.field-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.field-name {
  font-size: 14px;
  font-weight: 500;
}

.field-meta {
  font-size: 12px;
  display: flex;
  gap: 8px;
}

/* 流程时间线 */
.flow-timeline {
  position: relative;
}

.flow-node {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding-bottom: 16px;
}

.flow-node:last-child {
  padding-bottom: 0;
}

.flow-node__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 12px;
  margin-top: 4px;
  flex-shrink: 0;
}

.node-start {
  background: var(--el-color-success);
}

.node-approval {
  background: var(--el-color-primary);
}

.node-condition {
  background: var(--el-color-warning);
}

.node-cc {
  background: var(--el-color-info);
}

.node-end {
  background: var(--el-color-danger);
}

.node-default {
  background: var(--el-text-color-placeholder);
}

.flow-node__content {
  flex: 1;
}

.flow-node__name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
}

.flow-node__type {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.flow-node__assignee {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.flow-node__line {
  position: absolute;
  left: 5px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: var(--el-border-color-lighter);
}

/* 通知配置 */
.notify-item {
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  margin-bottom: 8px;
}

.notify-item:last-child {
  margin-bottom: 0;
}

.notify-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notify-channel {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.notify-template {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
