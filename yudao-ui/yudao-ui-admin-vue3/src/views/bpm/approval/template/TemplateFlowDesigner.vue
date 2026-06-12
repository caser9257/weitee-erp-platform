<template>
  <el-dialog
    v-model="visible"
    :title="`配置流程 - ${template?.name || ''}`"
    width="90%"
    :close-on-click-modal="false"
    :destroy-on-close="true"
  >
    <div class="flow-designer-container">
      <!-- 提示信息 -->
      <el-alert
        title="请在下方设计审批流程，完成后点击「保存并启用」按钮"
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
      />

      <!-- 流程设计器 -->
      <div class="designer-wrapper" style="height: 500px; overflow: auto;">
        <SimpleProcessDesigner
          ref="designerRef"
          :model-form-id="undefined"
          :model-form-type="0"
          @success="handleDesignerSave"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="saving"
        @click="handleSave"
      >
        <Icon icon="ep:check" class="mr-5px" />
        保存并启用
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, provide, watch } from 'vue'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalTemplateApi from '@/api/bpm/approval/template'
import { SimpleProcessDesigner } from '@/components/SimpleProcessDesignerV2/src/'

defineOptions({ name: 'TemplateFlowDesigner' })

const emit = defineEmits(['success'])

const message = useMessage()
const visible = ref(false)
const saving = ref(false)
const template = ref<ApprovalTemplateApi.ApprovalTemplateVO | null>(null)
const designerRef = ref()

// 流程设计器数据（通过 provide 提供给 SimpleProcessDesigner）
const processData = ref<any>(null)
provide('processData', processData)

// 设计器保存回调
const handleDesignerSave = (data: any) => {
  if (data) {
    processData.value = data
  }
}

// 打开弹窗
const open = async (templateData: ApprovalTemplateApi.ApprovalTemplateVO) => {
  template.value = templateData
  visible.value = true
  
  // 解析模板的 flowConfig 并设置到 processData
  if (templateData.flowConfig) {
    try {
      // 如果 flowConfig 是字符串，解析它
      if (typeof templateData.flowConfig === 'string') {
        processData.value = JSON.parse(templateData.flowConfig)
      } else {
        processData.value = templateData.flowConfig
      }
    } catch (e) {
      console.warn('解析 flowConfig 失败:', e)
      processData.value = null
    }
  } else {
    processData.value = null
  }
}

// 保存并启用
const handleSave = async () => {
  if (!template.value) {
    message.error('模板数据异常')
    return
  }

  // 获取设计器当前的流程数据
  const flowData = processData.value
  if (!flowData) {
    message.warning('请先设计审批流程')
    return
  }

  saving.value = true
  try {
    // 调用后端接口，传入自定义的流程配置
    const result = await ApprovalTemplateApi.useApprovalTemplateWithFlow(
      template.value.id,
      flowData
    )
    
    message.success('流程创建并启用成功！')
    visible.value = false
    emit('success', result)
  } catch (error: any) {
    message.error('保存失败：' + (error?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.flow-designer-container {
  min-height: 400px;
}

.designer-wrapper {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background: var(--el-fill-color-lighter);
}
</style>
