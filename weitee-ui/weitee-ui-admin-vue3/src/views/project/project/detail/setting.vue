<template>
  <div class="project-setting">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入项目名称" />
      </el-form-item>
      <el-form-item label="项目描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入项目描述" />
      </el-form-item>
      <el-form-item label="个人项目">
        <el-switch v-model="form.personal" />
      </el-form-item>
    </el-form>

    <div class="actions">
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </div>

    <el-divider />

    <div class="danger-zone">
      <h4>危险操作</h4>
      <div class="danger-item">
        <div>
          <p class="danger-title">归档项目</p>
          <p class="danger-desc">归档后项目将只读，无法创建新任务</p>
        </div>
        <el-button type="warning" @click="handleArchive">
          {{ project.archivedAt ? '取消归档' : '归档项目' }}
        </el-button>
      </div>
      <div class="danger-item">
        <div>
          <p class="danger-title">删除项目</p>
          <p class="danger-desc">删除后项目数据将无法恢复</p>
        </div>
        <el-button type="danger" @click="handleDelete">删除项目</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ProjectManageApi } from '@/api/project/project'

const props = defineProps<{ project: any }>()
const emit = defineEmits(['close', 'refresh'])

const router = useRouter()
const message = useMessage()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  id: props.project.id,
  name: props.project.name || '',
  description: props.project.description || '',
  personal: props.project.personal || false
})

const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await ProjectManageApi.updateProject(form)
    message.success('保存成功')
    emit('refresh')
    emit('close')
  } catch (error) {
    console.error('保存项目设置失败', error)
  } finally {
    submitting.value = false
  }
}

const handleArchive = async () => {
  const archive = !props.project.archivedAt
  const msg = archive ? '确认归档该项目？' : '确认取消归档该项目？'
  try {
    await message.confirm(msg)
    await ProjectManageApi.archiveProject(props.project.id, archive)
    message.success(archive ? '归档成功' : '取消归档成功')
    emit('refresh')
  } catch (error) {
    console.error('归档项目失败', error)
  }
}

const handleDelete = async () => {
  try {
    await message.delConfirm('确认删除该项目？删除后数据将无法恢复！')
    await ProjectManageApi.deleteProject(props.project.id)
    message.success('删除成功')
    router.push('/project/project')
  } catch (error) {
    console.error('删除项目失败', error)
  }
}

onMounted(() => {
  form.id = props.project.id
  form.name = props.project.name || ''
  form.description = props.project.description || ''
  form.personal = props.project.personal || false
})
</script>

<style scoped lang="scss">
.project-setting {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.danger-zone {
  h4 {
    margin: 0 0 12px 0;
    color: #f56c6c;
  }
}

.danger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 8px;

  p {
    margin: 0;
  }
}

.danger-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.danger-desc {
  color: #666;
  font-size: 12px;
}
</style>
