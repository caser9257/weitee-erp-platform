<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item label="流程实例编号" prop="processInstanceId">
        <el-input
          v-model="queryParams.processInstanceId"
          placeholder="请输入流程实例编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="场景编码" prop="sceneCode">
        <el-input
          v-model="queryParams.sceneCode"
          placeholder="请输入场景编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="业务单据 ID" prop="bizId">
        <el-input
          v-model="queryParams.bizId"
          placeholder="请输入业务单据 ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap v-if="snapshot">
    <!-- 基本信息 -->
    <el-descriptions :column="2" border>
      <el-descriptions-item label="快照编号">{{ snapshot.id }}</el-descriptions-item>
      <el-descriptions-item label="场景编码">{{ snapshot.sceneCode }}</el-descriptions-item>
      <el-descriptions-item label="业务单据 ID">{{ snapshot.bizId }}</el-descriptions-item>
      <el-descriptions-item label="流程定义 Key">{{ snapshot.processDefinitionKey || '-' }}</el-descriptions-item>
      <el-descriptions-item label="流程实例 ID">
        <el-link
          v-if="snapshot.processInstanceId"
          type="primary"
          @click="handleViewProcessInstance(snapshot.processInstanceId)"
        >{{ snapshot.processInstanceId }}</el-link>
        <span v-else class="text-gray-400">未生成</span>
      </el-descriptions-item>
      <el-descriptions-item label="方案 ID">{{ snapshot.schemeId }}</el-descriptions-item>
      <el-descriptions-item label="方案版本 ID">{{ snapshot.schemeVersionId }}</el-descriptions-item>
      <el-descriptions-item label="规则 ID">{{ snapshot.ruleId }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag v-if="snapshot.status === 1" type="warning" effect="light">审批中</el-tag>
        <el-tag v-else-if="snapshot.status === 2" type="success" effect="light">已通过</el-tag>
        <el-tag v-else-if="snapshot.status === 3" type="danger" effect="light">已驳回</el-tag>
        <el-tag v-else-if="snapshot.status === 4" type="info" effect="light">已撤回</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="审批原因">{{ snapshot.resultReason || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">{{ snapshot.createTime }}</el-descriptions-item>
    </el-descriptions>

    <!-- 上下文信息 -->
    <el-divider content-position="left">业务上下文</el-divider>
    <div v-if="snapshot.contextJson" class="context-json">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item
          v-for="(value, key) in snapshot.contextJson"
          :key="key"
          :label="String(key)"
        >{{ formatJsonValue(value) }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <el-empty v-else description="无上下文数据" :image-size="60" />

    <!-- 通知配置 -->
    <el-divider content-position="left">通知配置</el-divider>
    <div v-if="snapshot.notifyJson" class="notify-json">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item
          v-for="(value, key) in snapshot.notifyJson"
          :key="key"
          :label="formatNotifyLabel(String(key))"
        >
          <div v-if="typeof value === 'object' && value !== null">
            <el-tag :type="value.enabled ? 'success' : 'info'" size="small" class="mr-2">
              {{ value.enabled ? '启用' : '禁用' }}
            </el-tag>
            <span v-if="value.titleTemplate" class="text-xs text-gray-500">标题: {{ value.titleTemplate }}</span>
          </div>
          <span v-else>{{ formatJsonValue(value) }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <el-empty v-else description="无通知配置" :image-size="60" />
  </ContentWrap>

  <ContentWrap v-if="!snapshot && queried">
    <el-empty description="未找到审批运行时快照" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from '@/hooks/web/useMessage'
import * as RuntimeApi from '@/api/bpm/approval/runtime'

defineOptions({ name: 'BpmApprovalRuntime' })

const message = useMessage()
const router = useRouter()

const snapshot = ref<RuntimeApi.ApprovalSnapshotVO | null>(null)
const queried = ref(false)

const queryParams = reactive({
  processInstanceId: '',
  sceneCode: '',
  bizId: ''
})

const queryFormRef = ref()

const handleQuery = async () => {
  queried.value = true
  snapshot.value = null
  try {
    if (queryParams.processInstanceId) {
      snapshot.value = await RuntimeApi.getSnapshotByProcessInstanceId(queryParams.processInstanceId)
    } else if (queryParams.sceneCode && queryParams.bizId) {
      snapshot.value = await RuntimeApi.getSnapshotBySceneCodeAndBizId(queryParams.sceneCode, queryParams.bizId)
    } else {
      message.warning('请填写流程实例编号，或同时填写场景编码和业务单据 ID')
    }
  } catch {
    // 查询失败，snapshot 保持 null
  }
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  snapshot.value = null
  queried.value = false
}

const handleViewProcessInstance = (processInstanceId: string) => {
  router.push({
    path: '/bpm/process-instance/detail',
    query: { id: processInstanceId }
  })
}

// 格式化 JSON 值
const formatJsonValue = (value: any): string => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

// 格式化通知标签
const formatNotifyLabel = (key: string): string => {
  const labelMap: Record<string, string> = {
    taskCreated: '待办提醒',
    approve: '审批通过提醒',
    reject: '审批驳回提醒'
  }
  return labelMap[key] || key
}
</script>
