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
      <el-form-item label="场景名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入场景名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div style="overflow-x: auto">
      <el-table :data="list" v-loading="loading" :stripe="true" :show-overflow-tooltip="true" style="min-width: 1100px">
      <el-table-column label="场景名称" min-width="200">
        <template #default="{ row }">
          <div class="flex flex-col">
            <span class="font-medium text-[var(--el-text-color-primary)]">{{ row.name }}</span>
            <span class="text-xs text-[var(--el-text-color-secondary)] font-mono">{{ row.sceneCode }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" prop="bizType" min-width="120" />
      <el-table-column label="模块编码" prop="moduleCode" min-width="140" />
      <el-table-column label="触发动作" prop="actionCode" min-width="100" />
      <el-table-column label="关联方案" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.activeSchemeId" size="small" type="success" effect="plain">已绑定</el-tag>
          <el-tag v-else size="small" type="info" effect="plain">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" size="small" type="success" effect="plain">启用</el-tag>
          <el-tag v-else size="small" type="info" effect="plain">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="132">
        <template #default="{ row }">
          <div class="scene-time-cell">
            <span>{{ formatTimestamp(row.createTime).date }}</span>
            <span>{{ formatTimestamp(row.createTime).time }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" :fixed="isActionColumnFixed ? 'right' : false">
        <template #default="{ row }">
          <div class="scene-action-cell">
            <el-button
              link
              type="primary"
              @click="handleBindScheme(row)"
            >
              {{ row.activeSchemeId ? '换绑' : '绑定' }}
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="primary"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 绑定方案弹窗 -->
  <BindSchemeDialog ref="bindSchemeRef" @success="getList" />
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { ElMessageBox } from 'element-plus'
import { useMessage } from '@/hooks/web/useMessage'
import * as ApprovalSceneApi from '@/api/bpm/approval/scene'
import BindSchemeDialog from './BindSchemeDialog.vue'

defineOptions({ name: 'BpmApprovalScene' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<ApprovalSceneApi.ApprovalSceneVO[]>([])
const { width: windowWidth } = useWindowSize()
const isActionColumnFixed = computed(() => windowWidth.value >= 1280)
const queryParams = reactive<ApprovalSceneApi.ApprovalScenePageReqVO>({
  name: undefined,
  status: undefined,
  pageNo: 1,
  pageSize: 20
})

// 时间戳格式化
const formatTimestamp = (ts: number | null | undefined) => {
  if (!ts) return { date: '', time: '' }
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return {
    date: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`,
    time: `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ApprovalSceneApi.getApprovalScenePage(queryParams)
    console.log('[BpmApprovalScene] getList response:', data)
    // 确保 data.list 存在
    list.value = data?.list || []
    total.value = data?.total || 0
    console.log('[BpmApprovalScene] list.value:', list.value)
  } catch (error: any) {
    console.error('[BpmApprovalScene] getList failed:', error)
    list.value = []
    total.value = 0
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
  queryParams.status = undefined
  handleQuery()
}

const handleEnable = async (row: ApprovalSceneApi.ApprovalSceneVO) => {
  try {
    await ElMessageBox.confirm(`确认启用场景「${row.name}」？`, '启用场景', { type: 'info' })
    await ApprovalSceneApi.enableApprovalScene(row.id)
    message.success('启用成功')
    await getList()
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      message.error('启用失败：' + (error?.message || '未知错误'))
    }
  }
}

const handleDisable = async (row: ApprovalSceneApi.ApprovalSceneVO) => {
  try {
    await ElMessageBox.confirm(`确认禁用场景「${row.name}」？`, '禁用场景', { type: 'warning' })
    await ApprovalSceneApi.disableApprovalScene(row.id)
    message.success('禁用成功')
    await getList()
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      message.error('禁用失败：' + (error?.message || '未知错误'))
    }
  }
}

const handleDelete = async (row: ApprovalSceneApi.ApprovalSceneVO) => {
  try {
    await ElMessageBox.confirm(`确认删除场景「${row.name}」？删除后不可恢复。`, '删除场景', { type: 'warning' })
    // 乐观更新：先从列表移除，给用户即时反馈
    list.value = list.value.filter(item => item.id !== row.id)
    total.value--
    // 调用 API 真正删除
    await ApprovalSceneApi.deleteApprovalScene(row.id)
    message.success('删除成功')
  } catch (error: any) {
    // 删除失败，回滚：重新加载列表
    await getList()
    if (error !== 'cancel' && error?.message !== 'cancel') {
      message.error('删除失败：' + (error?.message || '未知错误'))
    }
  }
}

const bindSchemeRef = ref()

const handleBindScheme = async (row: ApprovalSceneApi.ApprovalSceneVO) => {
  console.log('[BpmApprovalScene] handleBindScheme called with:', row)
  console.log('[BpmApprovalScene] bindSchemeRef.value:', bindSchemeRef.value)
  if (bindSchemeRef.value) {
    await bindSchemeRef.value.open(row)
  } else {
    console.error('[BpmApprovalScene] bindSchemeRef is not initialized')
    message.error('绑定弹窗组件未初始化')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.scene-time-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.25;
  white-space: nowrap;
}

.scene-action-cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  row-gap: 4px;
}

.scene-action-cell :deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>
