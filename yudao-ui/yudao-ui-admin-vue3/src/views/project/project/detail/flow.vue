<template>
  <div class="project-flow">
    <ContentWrap>
      <template #header>
        <div class="flow-header">
          <span>工作流配置</span>
          <el-button type="primary" size="small" :loading="saving" @click="handleSave">
            保存工作流
          </el-button>
        </div>
      </template>

      <!-- 工作流节点列表 -->
      <div v-if="flowData" class="flow-content">
        <div class="flow-name">
          <el-input v-model="flowData.name" placeholder="工作流名称" style="width: 200px" />
        </div>

        <div class="flow-nodes">
          <div
            v-for="(item, index) in flowData.items"
            :key="index"
            class="flow-node"
            :style="{ borderColor: item.color || '#409eff' }"
          >
            <div class="node-header">
              <span class="node-name">{{ item.name }}</span>
              <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
            </div>
            <div class="node-actions">
              <el-button text size="small" @click="handleEdit(index)">
                <Icon icon="ep:edit" />
              </el-button>
              <el-button text size="small" type="danger" @click="handleDelete(index)">
                <Icon icon="ep:delete" />
              </el-button>
              <el-button v-if="index > 0" text size="small" @click="handleMoveUp(index)">
                <Icon icon="ep:top" />
              </el-button>
              <el-button v-if="index < flowData.items.length - 1" text size="small" @click="handleMoveDown(index)">
                <Icon icon="ep:bottom" />
              </el-button>
            </div>
            <div class="node-arrow" v-if="index < flowData.items.length - 1">
              <Icon icon="ep:right" />
            </div>
          </div>
        </div>

        <el-button type="primary" plain @click="handleAdd">
          <Icon icon="ep:plus" class="mr-5px" />
          添加节点
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-else class="loading-content">
        <el-skeleton :rows="3" animated />
      </div>
    </ContentWrap>

    <!-- 节点编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑节点" width="400px">
      <el-form label-width="80px">
        <el-form-item label="节点名称">
          <el-input v-model="editForm.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="状态类型">
          <el-select v-model="editForm.status">
            <el-option label="开始" value="start" />
            <el-option label="进行中" value="progress" />
            <el-option label="测试" value="test" />
            <el-option label="结束" value="end" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="editForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveNode">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FlowApi } from '@/api/project/flow'

defineOptions({ name: 'ProjectFlow' })

const props = defineProps<{
  projectId: number
}>()

interface FlowItem {
  name: string
  status: string
  color: string
  sort: number
}

interface FlowData {
  id?: number
  projectId: number
  name: string
  items: FlowItem[]
}

const flowData = ref<FlowData | null>(null)
const saving = ref(false)
const editVisible = ref(false)
const editIndex = ref(-1)
const editForm = ref<FlowItem>({ name: '', status: 'progress', color: '#409eff', sort: 0 })

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    start: 'success',
    progress: '',
    test: 'warning',
    end: 'info'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    start: '开始',
    progress: '进行中',
    test: '测试',
    end: '结束'
  }
  return map[status] || status
}

const fetchFlow = async () => {
  try {
    const res = await FlowApi.getFlow(props.projectId)
    flowData.value = res || {
      projectId: props.projectId,
      name: '默认工作流',
      items: [
        { name: '待办', status: 'start', color: '#67c23a', sort: 0 },
        { name: '进行中', status: 'progress', color: '#409eff', sort: 1 },
        { name: '已完成', status: 'end', color: '#909399', sort: 2 }
      ]
    }
  } catch (error) {
    console.error('获取工作流失败', error)
  }
}

const handleAdd = () => {
  editIndex.value = -1
  editForm.value = { name: '', status: 'progress', color: '#409eff', sort: flowData.value?.items.length || 0 }
  editVisible.value = true
}

const handleEdit = (index: number) => {
  editIndex.value = index
  editForm.value = { ...flowData.value!.items[index] }
  editVisible.value = true
}

const handleSaveNode = () => {
  if (!editForm.value.name) {
    ElMessage.warning('请输入节点名称')
    return
  }
  if (editIndex.value >= 0) {
    flowData.value!.items[editIndex.value] = { ...editForm.value }
  } else {
    flowData.value!.items.push({ ...editForm.value })
  }
  editVisible.value = false
}

const handleDelete = async (index: number) => {
  await ElMessageBox.confirm('确定删除该节点？', '提示', { type: 'warning' })
  flowData.value!.items.splice(index, 1)
}

const handleMoveUp = (index: number) => {
  const items = flowData.value!.items
  ;[items[index - 1], items[index]] = [items[index], items[index - 1]]
}

const handleMoveDown = (index: number) => {
  const items = flowData.value!.items
  ;[items[index], items[index + 1]] = [items[index + 1], items[index]]
}

const handleSave = async () => {
  saving.value = true
  try {
    await FlowApi.saveFlow(flowData.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchFlow()
})
</script>

<style scoped lang="scss">
.project-flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flow-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow-name {
  margin-bottom: 8px;
}

.flow-nodes {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.flow-node {
  position: relative;
  border: 2px solid #409eff;
  border-radius: 8px;
  padding: 12px;
  min-width: 120px;
  background: #fff;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.node-name {
  font-weight: 500;
}

.node-actions {
  display: flex;
  gap: 4px;
}

.node-arrow {
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.loading-content {
  padding: 20px;
}
</style>
