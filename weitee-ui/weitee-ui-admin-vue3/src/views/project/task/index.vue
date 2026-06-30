<template>
  <div class="my-task-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="84px">
        <el-form-item label="任务标题" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入任务标题"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="完成状态" prop="completed">
          <el-select v-model="queryParams.completed" clearable placeholder="全部">
            <el-option label="未完成" :value="false" />
            <el-option label="已完成" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="任务标题" prop="name" min-width="160" />
        <el-table-column label="项目" prop="projectId" width="120" />
        <el-table-column label="优先级" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.priorityName" :style="{ color: row.priorityColor }">
              {{ row.priorityName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" prop="endAt" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.completeAt" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="row.endAt && new Date(row.endAt) < new Date()" type="danger" size="small">
              已逾期
            </el-tag>
            <el-tag v-else type="info" size="small">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleComplete(row)" v-if="!row.completeAt">
              完成
            </el-button>
            <el-button link type="primary" @click="handleComplete(row)" v-else>
              重新打开
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { TaskApi, type TaskVO } from '@/api/project/task'

defineOptions({ name: 'MyTask' })

const message = useMessage()

const loading = ref(false)
const list = ref<TaskVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  completed: undefined as boolean | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await TaskApi.getTaskPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
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
  handleQuery()
}

const handleComplete = async (row: TaskVO) => {
  try {
    await TaskApi.completeTask(row.id, !row.completeAt)
    message.success(row.completeAt ? '已重新打开' : '已完成')
    getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.my-task-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
