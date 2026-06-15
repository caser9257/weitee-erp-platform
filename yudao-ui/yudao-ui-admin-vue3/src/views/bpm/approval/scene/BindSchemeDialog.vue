<template>
  <el-dialog
    v-model="visible"
    title="绑定审批方案"
    width="600px"
    :destroy-on-close="true"
  >
    <div class="bind-scheme-content">
      <!-- 当前场景信息 -->
      <div class="current-info">
        <div class="info-label">当前场景</div>
        <div class="info-value">
          <span class="font-medium">{{ scene?.name }}</span>
          <span class="text-gray-400 ml-8px font-mono text-xs">{{ scene?.sceneCode }}</span>
        </div>
      </div>

      <!-- 当前绑定状态 -->
      <div v-if="scene?.activeSchemeId" class="current-binding">
        <div class="info-label">当前绑定方案</div>
        <div class="info-value">
          <el-tag type="success" effect="plain">已绑定方案 ID: {{ scene.activeSchemeId }}</el-tag>
        </div>
      </div>

      <!-- 选择方案 -->
      <div class="select-section">
        <div class="info-label mb-8px">选择要绑定的方案</div>
        <el-select
          v-model="selectedSchemeId"
          placeholder="请选择审批方案"
          clearable
          filterable
          style="width: 100%"
          :loading="schemeLoading"
        >
          <el-option
            v-for="scheme in schemeList"
            :key="scheme.id"
            :label="scheme.name"
            :value="scheme.id"
          >
            <div class="flex items-center justify-between">
              <span>{{ scheme.name }}</span>
              <el-tag size="small" :type="getSchemeStatusType(scheme.latestVersionStatus)">
                {{ getSchemeStatusLabel(scheme.latestVersionStatus) }}
              </el-tag>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 选中方案的详情 -->
      <div v-if="selectedScheme" class="scheme-detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="方案编码">
            <span class="font-mono">{{ selectedScheme.code }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="模块编码">
            {{ selectedScheme.moduleCode }}
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">
            {{ selectedScheme.bizType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="版本状态">
            <el-tag size="small" :type="getSchemeStatusType(selectedScheme.latestVersionStatus)">
              {{ getSchemeStatusLabel(selectedScheme.latestVersionStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最新版本" :span="2">
            v{{ selectedScheme.latestVersionNo || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        v-if="scene?.activeSchemeId"
        type="danger"
        plain
        :loading="unbindLoading"
        @click="handleUnbind"
      >
        解绑
      </el-button>
      <el-button
        type="primary"
        :loading="bindLoading"
        :disabled="!selectedSchemeId"
        @click="handleBind"
      >
        绑定
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalSceneApi from '@/api/bpm/approval/scene'
import * as ApprovalSchemeApi from '@/api/bpm/approval/scheme'

defineOptions({ name: 'BindSchemeDialog' })

const emit = defineEmits(['success'])

const message = useMessage()
const visible = ref(false)
const scene = ref<ApprovalSceneApi.ApprovalSceneVO | null>(null)
const selectedSchemeId = ref<number | undefined>(undefined)
const schemeList = ref<ApprovalSchemeApi.ApprovalSchemeVO[]>([])
const schemeLoading = ref(false)
const bindLoading = ref(false)
const unbindLoading = ref(false)

// 选中的方案详情
const selectedScheme = computed(() => {
  if (!selectedSchemeId.value) return null
  return schemeList.value.find(s => s.id === selectedSchemeId.value) || null
})

// 打开弹窗
const open = async (sceneData: ApprovalSceneApi.ApprovalSceneVO) => {
  visible.value = true
  scene.value = sceneData
  selectedSchemeId.value = sceneData.activeSchemeId || undefined
  // 加载方案列表
  await loadSchemeList()
}

// 加载方案列表
const loadSchemeList = async () => {
  schemeLoading.value = true
  try {
    const data = await ApprovalSchemeApi.getApprovalSchemePage({
      pageNo: 1,
      pageSize: 100
    })
    // 确保 data.list 存在
    schemeList.value = data?.list || []
    // 如果方案列表为空，提示用户
    if (schemeList.value.length === 0) {
      message.warning('暂无可用的审批方案，请先创建审批方案')
    }
  } catch (error: any) {
    message.error('获取方案列表失败：' + (error?.message || '未知错误'))
    schemeList.value = []
  } finally {
    schemeLoading.value = false
  }
}

// 绑定方案
const handleBind = async () => {
  if (!scene.value || !selectedSchemeId.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认将场景「${scene.value.name}」绑定到方案「${selectedScheme.value?.name}」？`,
      '绑定方案',
      { type: 'info' }
    )
    bindLoading.value = true
    await ApprovalSceneApi.bindScheme(scene.value.id, selectedSchemeId.value)
    message.success('绑定成功')
    visible.value = false
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      message.error('绑定失败：' + (error?.message || '未知错误'))
    }
  } finally {
    bindLoading.value = false
  }
}

// 解绑方案
const handleUnbind = async () => {
  if (!scene.value) return
  try {
    await ElMessageBox.confirm(
      `确认解绑场景「${scene.value.name}」的审批方案？解绑后该场景将无法发起审批。`,
      '解绑方案',
      { type: 'warning' }
    )
    unbindLoading.value = true
    // 传递 null 表示解绑
    await ApprovalSceneApi.bindScheme(scene.value.id, null)
    message.success('解绑成功')
    visible.value = false
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      message.error('解绑失败：' + (error?.message || '未知错误'))
    }
  } finally {
    unbindLoading.value = false
  }
}

// 方案状态标签
const getSchemeStatusType = (status: number | null) => {
  if (!status) return 'info'
  const typeMap: Record<number, string> = {
    10: 'info',    // 草稿
    20: 'warning', // 待发布
    30: 'success', // 生效中
    40: 'danger'   // 已停用
  }
  return typeMap[status] || 'info'
}

const getSchemeStatusLabel = (status: number | null) => {
  if (!status) return '-'
  const labelMap: Record<number, string> = {
    10: '草稿',
    20: '待发布',
    30: '生效中',
    40: '已停用'
  }
  return labelMap[status] || '-'
}

defineExpose({ open })
</script>

<style scoped>
.bind-scheme-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.current-info,
.current-binding,
.select-section {
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

.info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
}

.scheme-detail {
  margin-top: 8px;
}
</style>
