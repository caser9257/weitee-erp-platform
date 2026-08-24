<template>
  <div class="bg-slate-50 min-h-screen p-4">
    <div class="max-w-7xl mx-auto space-y-4">
      <div class="flex items-center justify-between bg-white rounded-xl shadow-sm px-6 py-4 border border-slate-100">
        <div>
          <h1 class="text-lg font-semibold text-slate-800">文件权限管理</h1>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-slate-100">
        <div class="p-4 border-b border-slate-100">
          <el-form :inline="true" :model="queryParams" label-width="80px" class="-mb-15px">
            <el-form-item label="文件ID">
              <el-input v-model="queryParams.fileId" placeholder="请输入文件ID" clearable class="!w-200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">
                <Icon icon="ep:search" class="mr-5px" /> 查询
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="p-4">
          <el-table v-loading="loading" :data="list" stripe empty-text="请输入文件ID后查询">
            <el-table-column label="编号" align="center" prop="id" width="80" />
            <el-table-column label="文件ID" align="center" prop="fileId" width="80" />
            <el-table-column label="授权类型" align="center" width="120">
              <template #default="{ row }">
                <el-tag :type="row.grantType === 'USER' ? 'primary' : row.grantType === 'ROLE' ? 'success' : 'warning'" size="small">
                  {{ row.grantType === 'USER' ? '用户' : row.grantType === 'ROLE' ? '角色' : '部门' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="授权目标名称" align="center" prop="grantTargetName" min-width="140" />
            <el-table-column label="权限" align="center" prop="permissions" min-width="200">
              <template #default="{ row }">
                <div class="flex gap-1 flex-wrap justify-center">
                  <el-tag v-for="p in (row.permissions || '').split(',')" :key="p" size="small" class="!mr-0">
                    {{ permissionLabel(p) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="过期时间" align="center" prop="expireTime" width="160">
              <template #default="{ row }">
                <span v-if="row.expireTime">{{ row.expireTime }}</span>
                <span v-else class="text-slate-400">永不过期</span>
              </template>
            </el-table-column>
            <el-table-column label="授权人" align="center" prop="grantUserName" width="120" />
            <el-table-column label="授权时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FilePermissionApi from '@/api/infra/filePermission'

defineOptions({ name: 'InfraFilePermission' })

const loading = ref(false)
const list = ref<FilePermissionApi.FilePermissionVO[]>([])
const queryParams = reactive({ fileId: undefined })

const handleQuery = async () => {
  if (!queryParams.fileId) return
  loading.value = true
  try {
    const data = await FilePermissionApi.getFilePermissions(Number(queryParams.fileId))
    list.value = data
  } finally {
    loading.value = false
  }
}

const permissionLabel = (p: string) => {
  const map: Record<string, string> = { VIEW: '查看', DOWNLOAD: '下载', EDIT: '编辑', DELETE: '删除', SHARE: '分享' }
  return map[p] || p
}
</script>