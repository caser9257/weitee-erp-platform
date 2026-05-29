<template>
  <ContentWrap>
    <div class="mb-16px flex items-center justify-between gap-3">
      <div class="text-18px font-semibold text-slate-800">会员用户</div>
    </div>

    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="query-form"
      label-width="72px"
    >
      <el-form-item label="用户昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          class="!w-240px"
          clearable
          placeholder="请输入用户昵称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          class="!w-240px"
          clearable
          placeholder="请输入手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <template v-if="showAdvanced">
        <el-form-item label="注册时间" prop="createTime">
          <el-date-picker
            v-model="queryParams.createTime"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-240px"
            end-placeholder="结束日期"
            start-placeholder="开始日期"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="登录时间" prop="loginDate">
          <el-date-picker
            v-model="queryParams.loginDate"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-240px"
            end-placeholder="结束日期"
            start-placeholder="开始日期"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="用户标签" prop="tagIds">
          <MemberTagSelect v-model="queryParams.tagIds" />
        </el-form-item>
        <el-form-item label="用户等级" prop="levelId">
          <MemberLevelSelect v-model="queryParams.levelId" />
        </el-form-item>
        <el-form-item label="用户分组" prop="groupId">
          <MemberGroupSelect v-model="queryParams.groupId" />
        </el-form-item>
      </template>
      <el-form-item class="query-actions">
        <el-button :disabled="listLoading" @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          查询
        </el-button>
        <el-button :disabled="listLoading" @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button text type="primary" @click="showAdvanced = !showAdvanced">
          {{ showAdvanced ? '收起' : '展开' }}高级
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="mb-12px flex items-center justify-between gap-3">
      <div class="text-14px font-medium text-slate-800">用户列表</div>
    </div>

    <el-alert
      v-if="listError"
      :closable="false"
      show-icon
      title="列表加载失败，请重试"
      type="error"
      class="mb-3"
    >
      <template #default>
        <div class="flex items-center justify-between gap-3">
          <span>当前结果未更新，点击重试重新拉取数据。</span>
          <el-button size="small" :loading="listLoading" @click="getList">重新加载</el-button>
        </div>
      </template>
    </el-alert>

    <el-table
      v-loading="listLoading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      class="member-user-table"
    >
      <el-table-column align="center" label="用户编号" prop="id" width="110px">
        <template #default="{ row }">
          <div class="font-mono text-xs text-slate-500">{{ row.id }}</div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="头像" prop="avatar" width="90px">
        <template #default="{ row }">
          <ElAvatar :size="36" :src="row.avatar || undefined" shape="square" />
        </template>
      </el-table-column>
      <el-table-column align="left" label="用户信息" min-width="210px">
        <template #default="{ row }">
          <div class="flex min-w-0 flex-col">
            <span class="truncate font-medium text-slate-800">{{ row.nickname || '未填写' }}</span>
            <span class="truncate font-mono text-xs text-slate-400">{{ row.mobile }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" label="等级" prop="levelName" min-width="120px">
        <template #default="{ row }">
          <span class="truncate text-slate-700">{{ row.levelName || '未设置' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="分组" prop="groupName" min-width="120px">
        <template #default="{ row }">
          <span class="truncate text-slate-700">{{ row.groupName || '未设置' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="用户标签" prop="tagNames" min-width="180px">
        <template #default="{ row }">
          <div class="flex flex-wrap justify-center gap-1">
            <el-tag
              v-for="tagName in row.tagNames || []"
              :key="tagName"
              effect="light"
              round
              size="small"
              type="info"
            >
              {{ tagName }}
            </el-tag>
            <span v-if="!row.tagNames || row.tagNames.length === 0" class="text-slate-400">
              未设置
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="right" label="积分" prop="point" width="100px">
        <template #default="{ row }">
          <span class="font-mono">{{ formatNumber(row.point) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="90px">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="登录时间"
        prop="loginDate"
        width="180px"
      />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="注册时间"
        prop="createTime"
        width="180px"
      />
      <el-table-column fixed="right" align="center" label="操作" width="220px">
        <template #default="{ row }">
          <div class="flex items-center justify-center gap-2">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button
              v-if="checkPermi(['member:user:update'])"
              link
              type="primary"
              @click="openForm('update', row.id)"
            >
              编辑
            </el-button>
            <el-dropdown
              v-if="hasMoreActions"
              @command="(command) => handleCommand(command, row)"
            >
              <el-button link type="primary">
                <Icon icon="ep:more-filled" />
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="checkPermi(['member:user:update-level'])"
                    command="handleUpdateLevel"
                  >
                    修改等级
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="checkPermi(['member:user:update-point'])"
                    command="handleUpdatePoint"
                  >
                    修改积分
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="checkPermi(['pay:wallet:update-balance'])"
                    command="handleUpdateBalance"
                  >
                    修改余额
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-result
      v-if="!listLoading && list.length === 0"
      icon="info"
      title="暂无数据"
      class="py-12"
    />

    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <UserForm ref="formRef" @success="getList" />
  <UserLevelUpdateForm ref="updateLevelFormRef" @success="getList" />
  <UserPointUpdateForm ref="updatePointFormRef" @success="getList" />
  <UserBalanceUpdateForm ref="updateBalanceFormRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as UserApi from '@/api/member/user'
import { DICT_TYPE } from '@/utils/dict'
import UserForm from './UserForm.vue'
import MemberTagSelect from '@/views/member/tag/components/MemberTagSelect.vue'
import MemberLevelSelect from '@/views/member/level/components/MemberLevelSelect.vue'
import MemberGroupSelect from '@/views/member/group/components/MemberGroupSelect.vue'
import UserLevelUpdateForm from './components/UserLevelUpdateForm.vue'
import UserPointUpdateForm from './components/UserPointUpdateForm.vue'
import UserBalanceUpdateForm from './components/UserBalanceUpdateForm.vue'
import { checkPermi } from '@/utils/permission'

defineOptions({ name: 'MemberUser' })

const router = useRouter()
const queryFormRef = ref()
const formRef = ref()
const updateLevelFormRef = ref()
const updatePointFormRef = ref()
const updateBalanceFormRef = ref()

const listLoading = ref(false)
const listError = ref('')
const total = ref(0)
const list = ref<UserApi.UserVO[]>([])
const showAdvanced = ref(false)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  nickname: null,
  mobile: null,
  loginDate: [],
  createTime: [],
  tagIds: [],
  levelId: null,
  groupId: null
})

const hasMoreActions = computed(
  () =>
    checkPermi(['member:user:update-level']) ||
    checkPermi(['member:user:update-point']) ||
    checkPermi(['pay:wallet:update-balance'])
)

const formatNumber = (value: number | null | undefined) => {
  if (value === null || value === undefined) {
    return '0'
  }
  return Number(value).toLocaleString('zh-CN')
}

const getList = async () => {
  listLoading.value = true
  try {
    const data = await UserApi.getUserPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
    listError.value = ''
  } catch (error) {
    listError.value = '列表加载失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  showAdvanced.value = false
  getList()
}

const openDetail = (id: number) => {
  router.push({ name: 'MemberUserDetail', params: { id } })
}

const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

const handleCommand = (command: string, row: UserApi.UserVO) => {
  if (command === 'handleUpdateLevel') {
    updateLevelFormRef.value?.open(row.id)
    return
  }
  if (command === 'handleUpdatePoint') {
    updatePointFormRef.value?.open(row.id)
    return
  }
  if (command === 'handleUpdateBalance') {
    updateBalanceFormRef.value?.open(row.id)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.query-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  .query-actions {
    margin-left: auto;
  }

  @media (max-width: 1280px) {
    .query-actions {
      margin-left: 0;
    }
  }
}

.member-user-table {
  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}
</style>
