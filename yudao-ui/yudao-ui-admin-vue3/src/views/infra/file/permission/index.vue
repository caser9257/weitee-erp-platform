<template>
  <ContentWrap title="文件权限管理">
    <!-- 搜索 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="文件ID" prop="fileId">
        <el-input
          v-model="queryParams.fileId"
          placeholder="请输入文件ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-160px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openGrantDialog" v-hasPermi="['infra:file:update']">
          <Icon icon="ep:plus" class="mr-5px" /> 授权
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="权限ID" prop="id" width="80" align="center" />
      <el-table-column label="文件ID" prop="fileId" width="80" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewFile(row.fileId)">{{ row.fileId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="授权类型" prop="grantType" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getGrantTypeTagType(row.grantType)" size="small">
            {{ getGrantTypeLabel(row.grantType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="授权目标" prop="grantTargetName" min-width="120" />
      <el-table-column label="权限列表" prop="permissions" min-width="200">
        <template #default="{ row }">
          <div class="flex flex-wrap gap-4px">
            <el-tag
              v-for="perm in row.permissions?.split(',')"
              :key="perm"
              size="small"
              type="info"
            >
              {{ getPermissionLabel(perm) }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="过期时间" prop="expireTime" width="180" align="center">
        <template #default="{ row }">
          <span v-if="row.expireTime">{{ dateFormatter(null, null, row.expireTime) }}</span>
          <span v-else class="text-[var(--erp-slate-400)]">永不过期</span>
        </template>
      </el-table-column>
      <el-table-column label="授权人" prop="grantUserName" width="100" align="center" />
      <el-table-column label="备注" prop="remark" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :formatter="dateFormatter" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="danger"
            @click="handleRevoke(row.fileId, row.grantType, row.grantTargetId)"
            v-hasPermi="['infra:file:update']"
          >
            <Icon icon="ep:delete" class="mr-3px" /> 撤销
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 授权弹窗 -->
  <Dialog v-model="grantDialogVisible" title="文件权限授权" width="500px">
    <el-form
      ref="grantFormRef"
      :model="grantForm"
      :rules="grantRules"
      label-width="100px"
    >
      <el-form-item label="文件ID" prop="fileId">
        <el-input v-model="grantForm.fileId" placeholder="请输入文件ID" />
      </el-form-item>
      <el-form-item label="授权类型" prop="grantType">
        <el-radio-group v-model="grantForm.grantType">
          <el-radio value="USER">用户</el-radio>
          <el-radio value="ROLE">角色</el-radio>
          <el-radio value="DEPT">部门</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="授权目标ID" prop="grantTargetId">
        <el-input v-model="grantForm.grantTargetId" placeholder="请输入用户ID/角色ID/部门ID" />
      </el-form-item>
      <el-form-item label="授权目标名称" prop="grantTargetName">
        <el-input v-model="grantForm.grantTargetName" placeholder="请输入名称（可选）" />
      </el-form-item>
      <el-form-item label="权限类型" prop="permissions">
        <el-checkbox-group v-model="grantForm.permissions">
          <el-checkbox value="VIEW">查看</el-checkbox>
          <el-checkbox value="DOWNLOAD">下载</el-checkbox>
          <el-checkbox value="EDIT">编辑</el-checkbox>
          <el-checkbox value="DELETE">删除</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="过期时间" prop="expireTime">
        <el-date-picker
          v-model="grantForm.expireTime"
          type="datetime"
          placeholder="选择过期时间（可选）"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="grantForm.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="grantDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitGrant">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FilePermissionApi from '@/api/infra/filePermission'
import type { FilePermissionVO, FilePermissionSaveReqVO } from '@/api/infra/filePermission'

defineOptions({ name: 'InfraFilePermission' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const list = ref<FilePermissionVO[]>([])
const queryFormRef = ref()

const queryParams = reactive({
  fileId: undefined as number | undefined
})

// ========== 授权弹窗相关 ==========
const grantDialogVisible = ref(false)
const grantFormRef = ref()
const grantForm = reactive({
  fileId: undefined as number | undefined,
  grantType: 'USER',
  grantTargetId: undefined as number | undefined,
  grantTargetName: '',
  permissions: ['VIEW'] as string[],
  expireTime: '',
  remark: ''
})

const grantRules = reactive({
  fileId: [{ required: true, message: '文件ID不能为空', trigger: 'blur' }],
  grantType: [{ required: true, message: '授权类型不能为空', trigger: 'change' }],
  grantTargetId: [{ required: true, message: '授权目标ID不能为空', trigger: 'blur' }],
  permissions: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
})

/** 查询列表 */
const getList = async () => {
  if (!queryParams.fileId) {
    list.value = []
    return
  }
  loading.value = true
  try {
    const data = await FilePermissionApi.getFilePermissionList(queryParams.fileId)
    list.value = data
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  list.value = []
}

/** 打开授权弹窗 */
const openGrantDialog = () => {
  grantForm.fileId = queryParams.fileId
  grantForm.grantType = 'USER'
  grantForm.grantTargetId = undefined
  grantForm.grantTargetName = ''
  grantForm.permissions = ['VIEW']
  grantForm.expireTime = ''
  grantForm.remark = ''
  grantDialogVisible.value = true
}

/** 提交授权 */
const submitGrant = async () => {
  const valid = await grantFormRef.value.validate()
  if (!valid) return

  try {
    const data: FilePermissionSaveReqVO = {
      fileId: grantForm.fileId!,
      grantType: grantForm.grantType,
      grantTargetId: grantForm.grantTargetId!,
      grantTargetName: grantForm.grantTargetName,
      permissions: grantForm.permissions.join(','),
      expireTime: grantForm.expireTime ? new Date(grantForm.expireTime) : undefined,
      remark: grantForm.remark
    }
    await FilePermissionApi.grantFilePermission(data)
    message.success('授权成功')
    grantDialogVisible.value = false
    await getList()
  } catch {}
}

/** 撤销权限 */
const handleRevoke = async (fileId: number, grantType: string, grantTargetId: number) => {
  try {
    await message.delConfirm('确定要撤销该权限吗？')
    await FilePermissionApi.revokeFilePermission(fileId, grantType, grantTargetId)
    message.success('撤销成功')
    await getList()
  } catch {}
}

/** 查看文件详情 */
const handleViewFile = (fileId: number) => {
  router.push({ path: '/infra/file', query: { id: fileId } })
}

/** 获取授权类型标签 */
const getGrantTypeLabel = (grantType: string) => {
  const map: Record<string, string> = {
    USER: '用户',
    ROLE: '角色',
    DEPT: '部门'
  }
  return map[grantType] || grantType
}

/** 获取授权类型标签样式 */
const getGrantTypeTagType = (grantType: string) => {
  const map: Record<string, string> = {
    USER: 'primary',
    ROLE: 'success',
    DEPT: 'warning'
  }
  return map[grantType] || 'info'
}

/** 获取权限标签 */
const getPermissionLabel = (permission: string) => {
  const map: Record<string, string> = {
    VIEW: '查看',
    DOWNLOAD: '下载',
    EDIT: '编辑',
    DELETE: '删除',
    SHARE: '分享'
  }
  return map[permission] || permission
}
</script>
