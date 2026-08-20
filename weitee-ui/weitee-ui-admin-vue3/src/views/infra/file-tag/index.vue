<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入标签名称" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['infra:file-tag:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="标签名称" align="center" prop="name" min-width="160" />
      <el-table-column label="颜色" align="center" width="120">
        <template #default="{ row }">
          <div class="flex items-center justify-center gap-2">
            <span class="inline-block w-5 h-5 rounded-full border border-slate-200" :style="{ backgroundColor: row.color || '#409EFF' }" />
            <span class="text-slate-500 text-sm font-mono">{{ row.color || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="图标" align="center" prop="icon" width="100" />
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm('update', row)" v-hasPermi="['infra:file-tag:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['infra:file-tag:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="formType === 'create' ? '新增标签' : '编辑标签'" width="500px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入标签名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="标签颜色" prop="color">
        <el-color-picker v-model="formData.color" :predefine="['#409EFF','#67C23A','#E6A23C','#F56C6C','#909399','#9B59B6','#1ABC9C','#E74C3C']" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <el-input v-model="formData.icon" placeholder="请输入图标类名，如 ep:document" maxlength="50" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="999" />
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
import * as FileTagApi from '@/api/infra/fileTag'

defineOptions({ name: 'InfraFileTag' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<FileTagApi.FileTagVO[]>([])
const queryParams = reactive({ name: undefined })

const getList = async () => {
  loading.value = true
  try {
    const data = await FileTagApi.getFileTagList()
    list.value = data
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { getList() }
const resetQuery = () => {
  queryParams.name = undefined
  getList()
}

const dialogVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const formLoading = ref(false)
const formRef = ref()
const formData = reactive<FileTagApi.FileTagVO>({ id: 0, name: '', color: '#409EFF', icon: '', sort: 0, createTime: new Date() })
const rules = { name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }] }

const openForm = (type: string, row?: FileTagApi.FileTagVO) => {
  formType.value = type as any
  if (row) {
    Object.assign(formData, row)
  } else {
    formData.id = 0
    formData.name = ''
    formData.color = '#409EFF'
    formData.icon = ''
    formData.sort = 0
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await FileTagApi.createFileTag(formData as any)
      message.success(t('common.createSuccess'))
    } else {
      await FileTagApi.updateFileTag(formData as any)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileTagApi.deleteFileTag(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => { getList() })
</script>