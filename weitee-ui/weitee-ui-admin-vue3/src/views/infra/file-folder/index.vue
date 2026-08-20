<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" label-width="68px">
      <el-form-item>
        <el-button type="primary" plain @click="openForm('create', null)" v-hasPermi="['infra:file-folder:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建文件夹
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="treeData" row-key="id" default-expand-all stripe>
      <el-table-column label="文件夹名称" min-width="280">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <Icon :icon="row.icon || 'ep:folder'" class="text-amber-500 text-lg" />
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="路径" align="center" prop="path" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="success" size="small">正常</el-tag>
          <el-tag v-else type="danger" size="small">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('create', row)" v-hasPermi="['infra:file-folder:create']">新建子文件夹</el-button>
          <el-button link type="primary" @click="openForm('update', row)" v-hasPermi="['infra:file-folder:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['infra:file-folder:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="formType === 'create' ? '新建文件夹' : '编辑文件夹'" width="520px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="上级文件夹" v-if="formType === 'create'">
        <el-input :model-value="parentName" disabled placeholder="根目录" />
      </el-form-item>
      <el-form-item label="文件夹名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入文件夹名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <el-input v-model="formData.icon" placeholder="请输入图标类名，如 ep:folder" maxlength="50" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="200" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileFolderApi from '@/api/infra/fileFolder'

defineOptions({ name: 'InfraFileFolder' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const treeData = ref<FileFolderApi.FileFolderVO[]>([])

const getTree = async () => {
  loading.value = true
  try {
    const data = await FileFolderApi.getFileFolderTree()
    treeData.value = data
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const formLoading = ref(false)
const formRef = ref()
const parentName = ref('')
const parentId = ref(0)
const formData = reactive<FileFolderApi.FileFolderVO>({ id: 0, name: '', parentId: 0, path: '', icon: 'ep:folder', sort: 0, status: 0, remark: '', createTime: new Date() })

const rules = { name: [{ required: true, message: '文件夹名称不能为空', trigger: 'blur' }] }

const openForm = (type: string, row: FileFolderApi.FileFolderVO | null) => {
  formType.value = type as any
  if (type === 'create') {
    if (row) {
      parentId.value = row.id
      parentName.value = row.name
    } else {
      parentId.value = 0
      parentName.value = ''
    }
    formData.id = 0
    formData.name = ''
    formData.icon = 'ep:folder'
    formData.sort = 0
    formData.remark = ''
  } else if (row) {
    Object.assign(formData, row)
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  formLoading.value = true
  try {
    const payload = { ...formData }
    if (formType.value === 'create') {
      payload.parentId = parentId.value
      await FileFolderApi.createFileFolder(payload as any)
      message.success(t('common.createSuccess'))
    } else {
      await FileFolderApi.updateFileFolder(payload as any)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    await getTree()
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileFolderApi.deleteFileFolder(id)
    message.success(t('common.delSuccess'))
    await getTree()
  } catch {}
}

onMounted(() => { getTree() })
</script>