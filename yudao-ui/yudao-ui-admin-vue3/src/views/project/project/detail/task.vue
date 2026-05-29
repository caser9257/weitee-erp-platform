<template>
  <div class="task-detail">
    <el-form label-width="80px">
      <el-form-item label="任务标题">
        <el-input v-model="task.name" @change="handleUpdate('name', task.name)" />
      </el-form-item>
      <el-form-item label="状态">
        <el-tag v-if="task.completeAt" type="success">已完成</el-tag>
        <el-tag v-else type="info">进行中</el-tag>
      </el-form-item>
      <el-form-item label="优先级">
        <span v-if="task.priorityName" :style="{ color: task.priorityColor }">
          {{ task.priorityName }}
        </span>
        <span v-else>未设置</span>
      </el-form-item>
      <el-form-item label="负责人">
        <div v-for="owner in task.owners" :key="owner.userId">
          {{ owner.userName }}
        </div>
      </el-form-item>
      <el-form-item label="计划时间">
        <div v-if="task.startAt && task.endAt">
          {{ task.startAt }} ~ {{ task.endAt }}
        </div>
        <div v-else>未设置</div>
      </el-form-item>
      <el-form-item label="描述">
        <div v-html="task.description || '暂无描述'"></div>
      </el-form-item>
    </el-form>

    <!-- 评论区 -->
    <el-divider />
    <ProjectComment :project-id="task.projectId" :task-id="task.id" />

    <div class="actions">
      <el-button v-if="!task.completeAt" type="success" @click="handleComplete(true)">
        <Icon class="mr-5px" icon="ep:check" />
        标记完成
      </el-button>
      <el-button v-else type="warning" @click="handleComplete(false)">
        <Icon class="mr-5px" icon="ep:refresh-left" />
        重新打开
      </el-button>
      <el-button type="danger" @click="handleDelete">
        <Icon class="mr-5px" icon="ep:delete" />
        删除任务
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { TaskApi, type TaskVO } from '@/api/project/task'
import ProjectComment from '@/components/Project/Comment/index.vue'

const props = defineProps<{ task: TaskVO }>()
const emit = defineEmits(['close', 'refresh'])

const message = useMessage()

const handleUpdate = async (field: string, value: any) => {
  try {
    await TaskApi.updateTask({ id: props.task.id, [field]: value })
    message.success('更新成功')
    emit('refresh')
  } catch (error) {
    console.error('更新任务失败', error)
  }
}

const handleComplete = async (complete: boolean) => {
  try {
    await TaskApi.completeTask(props.task.id, complete)
    message.success(complete ? '已完成' : '已重新打开')
    emit('refresh')
  } catch (error) {
    console.error('完成任务失败', error)
  }
}

const handleDelete = async () => {
  try {
    await message.delConfirm()
    await TaskApi.deleteTask(props.task.id)
    message.success('删除成功')
    emit('close')
    emit('refresh')
  } catch (error) {
    console.error('删除任务失败', error)
  }
}
</script>

<style scoped lang="scss">
.task-detail {
  padding: 16px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}
</style>
