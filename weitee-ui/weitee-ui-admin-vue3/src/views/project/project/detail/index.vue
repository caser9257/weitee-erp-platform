<template>
  <div class="project-detail-page">
    <div class="project-header">
      <div class="project-info">
        <h2>{{ project.name }}</h2>
        <p>{{ project.description }}</p>
      </div>
      <div class="project-actions">
        <el-button @click="goBack">
          <Icon class="mr-5px" icon="ep:arrow-left" />
          返回列表
        </el-button>
        <el-button type="primary" @click="showMemberDialog = true">
          <Icon class="mr-5px" icon="ep:user" />
          成员管理
        </el-button>
        <el-button @click="showSettingDialog = true">
          <Icon class="mr-5px" icon="ep:setting" />
          项目设置
        </el-button>
      </div>
    </div>

    <div class="kanban-container">
      <div v-for="column in columns" :key="column.id" class="kanban-column">
        <div class="column-header">
          <h3>{{ column.name }}</h3>
          <span class="task-count">{{ getColumnTasks(column.id).length }}</span>
        </div>
        <div
          class="column-body"
          @drop="onDrop($event, column.id)"
          @dragover.prevent
        >
          <div
            v-for="task in getColumnTasks(column.id)"
            :key="task.id"
            class="task-card"
            draggable="true"
            @dragstart="onDragStart($event, task)"
            @click="openTaskDetail(task)"
          >
            <div class="task-title">{{ task.name }}</div>
            <div class="task-meta">
              <span
                v-if="task.priorityName"
                class="priority-tag"
                :style="{ backgroundColor: task.priorityColor }"
              >
                {{ task.priorityName }}
              </span>
              <span
                v-if="task.endAt"
                class="due-date"
                :class="{ overdue: isOverdue(task.endAt) }"
              >
                {{ formatDate(task.endAt) }}
              </span>
            </div>
            <div class="task-footer">
              <div class="task-tags">
                <span
                  v-for="tag in task.taskTags"
                  :key="tag.name"
                  class="tag"
                  :style="{ backgroundColor: tag.color }"
                >
                  {{ tag.name }}
                </span>
              </div>
              <div class="task-assignees">
                <el-avatar
                  v-for="owner in task.owners"
                  :key="owner.userId"
                  :size="24"
                  :title="owner.userName"
                >
                  {{ owner.userName?.charAt(0) }}
                </el-avatar>
              </div>
            </div>
          </div>
          <div class="add-task-btn" @click="showCreateTask(column.id)">
            <Icon icon="ep:plus" />
            添加任务
          </div>
        </div>
      </div>

      <div class="add-column-btn" @click="showCreateColumn">
        <Icon icon="ep:plus" />
        添加列表
      </div>
    </div>

    <el-drawer v-model="taskDrawerVisible" title="任务详情" size="600px" destroy-on-close>
      <TaskDetail
        v-if="currentTask"
        :task="currentTask"
        @close="taskDrawerVisible = false"
        @refresh="refreshTasks"
      />
    </el-drawer>

    <el-dialog v-model="createTaskVisible" title="创建任务" width="500px" destroy-on-close>
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="80px">
        <el-form-item label="任务标题" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.ownerUserIds" multiple placeholder="选择负责人">
            <el-option
              v-for="member in members"
              :key="member.userId"
              :label="member.userName"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priorityLevel" placeholder="选择优先级">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createTaskVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTask">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" title="成员管理" width="600px" destroy-on-close>
      <ProjectMember :project-id="projectId" @close="showMemberDialog = false" />
    </el-dialog>

    <el-dialog v-model="showSettingDialog" title="项目设置" width="500px" destroy-on-close>
      <ProjectSetting :project="project" @close="showSettingDialog = false" @refresh="loadData" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ProjectManageApi } from '@/api/project/project'
import { ColumnApi } from '@/api/project/column'
import { TaskApi, type TaskVO } from '@/api/project/task'
import TaskDetail from './task.vue'
import ProjectMember from './member.vue'
import ProjectSetting from './setting.vue'

defineOptions({ name: 'ProjectDetail' })

const route = useRoute()
const router = useRouter()
const message = useMessage()

const projectId = computed(() => Number(route.params.id))

const project = ref<any>({})
const columns = ref<any[]>([])
const tasks = ref<TaskVO[]>([])
const members = ref<any[]>([])

const taskDrawerVisible = ref(false)
const currentTask = ref<TaskVO | null>(null)
const createTaskVisible = ref(false)
const showMemberDialog = ref(false)
const showSettingDialog = ref(false)
const submitting = ref(false)

const taskFormRef = ref<FormInstance>()
const taskForm = reactive({
  name: '',
  projectId: projectId.value,
  columnId: 0,
  ownerUserIds: [] as number[],
  priorityLevel: undefined as number | undefined
})

const taskRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
})

const draggedTask = ref<TaskVO | null>(null)

const loadData = async () => {
  try {
    const [projectData, columnData, taskData, memberData] = await Promise.all([
      ProjectManageApi.getProject(projectId.value),
      ColumnApi.getColumnList(projectId.value),
      TaskApi.getTaskPage({ projectId: projectId.value, pageSize: 1000 }),
      ProjectManageApi.getMembers(projectId.value)
    ])
    project.value = projectData
    columns.value = columnData
    tasks.value = taskData.list || []
    members.value = memberData
  } catch (error) {
    console.error('加载项目数据失败', error)
  }
}

const getColumnTasks = (columnId: number) => {
  return tasks.value.filter((t) => t.columnId === columnId && !t.archivedAt)
}

const onDragStart = (event: DragEvent, task: TaskVO) => {
  draggedTask.value = task
  event.dataTransfer?.setData('text/plain', task.id.toString())
}

const onDrop = async (event: DragEvent, targetColumnId: number) => {
  event.preventDefault()
  if (draggedTask.value && draggedTask.value.columnId !== targetColumnId) {
    try {
      await TaskApi.moveTask(draggedTask.value.id, targetColumnId)
      draggedTask.value.columnId = targetColumnId
      message.success('移动成功')
    } catch (error) {
      console.error('移动任务失败', error)
    }
  }
  draggedTask.value = null
}

const openTaskDetail = (task: TaskVO) => {
  currentTask.value = task
  taskDrawerVisible.value = true
}

const showCreateTask = (columnId: number) => {
  taskForm.columnId = columnId
  taskForm.name = ''
  taskForm.ownerUserIds = []
  taskForm.priorityLevel = undefined
  createTaskVisible.value = true
}

const submitTask = async () => {
  const valid = await taskFormRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await TaskApi.createTask(taskForm)
    message.success('创建成功')
    createTaskVisible.value = false
    refreshTasks()
  } catch (error) {
    console.error('创建任务失败', error)
  } finally {
    submitting.value = false
  }
}

const showCreateColumn = async () => {
  const name = prompt('请输入列表名称')
  if (name) {
    try {
      await ColumnApi.createColumn({ projectId: projectId.value, name })
      message.success('创建成功')
      loadData()
    } catch (error) {
      console.error('创建列表失败', error)
    }
  }
}

const refreshTasks = async () => {
  try {
    const taskData = await TaskApi.getTaskPage({ projectId: projectId.value, pageSize: 1000 })
    tasks.value = taskData.list || []
  } catch (error) {
    console.error('刷新任务失败', error)
  }
}

const goBack = () => {
  router.push('/project/project')
}

const isOverdue = (date: string) => {
  return new Date(date) < new Date()
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.project-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 84px);
  overflow: hidden;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  flex-shrink: 0;
}

.project-info {
  h2 {
    margin: 0 0 4px 0;
    font-size: 18px;
  }

  p {
    margin: 0;
    color: #666;
    font-size: 14px;
  }
}

.project-actions {
  display: flex;
  gap: 8px;
}

.kanban-container {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  flex: 1;
  padding-bottom: 16px;
}

.kanban-column {
  min-width: 280px;
  max-width: 320px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;

  h3 {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
  }
}

.task-count {
  background: #e0e0e0;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.column-body {
  flex: 1;
  overflow-y: auto;
  min-height: 100px;
}

.task-card {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  }
}

.task-title {
  font-size: 14px;
  margin-bottom: 8px;
}

.task-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.priority-tag {
  padding: 2px 6px;
  border-radius: 3px;
  color: #fff;
  font-size: 12px;
}

.due-date {
  font-size: 12px;
  color: #666;

  &.overdue {
    color: #f56c6c;
  }
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-tags {
  display: flex;
  gap: 4px;
}

.tag {
  padding: 2px 6px;
  border-radius: 3px;
  color: #fff;
  font-size: 11px;
}

.task-assignees {
  display: flex;
  gap: 4px;
}

.add-task-btn {
  text-align: center;
  padding: 8px;
  color: #666;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;

  &:hover {
    background: #e8e8e8;
  }
}

.add-column-btn {
  min-width: 280px;
  background: #e8e8e8;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: fit-content;

  &:hover {
    background: #d8d8d8;
  }
}
</style>
