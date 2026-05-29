<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="detail-filter-form"
      label-width="72px"
    >
      <el-form-item label="签到用户" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          clearable
          placeholder="请输入签到用户"
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="签到天数" prop="day">
        <el-input
          v-model="queryParams.day"
          clearable
          placeholder="请输入签到天数"
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="签到时间" prop="createTime">
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
      <el-form-item class="filter-actions">
        <el-button :disabled="loading" @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          查询
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column align="center" label="编号" prop="id" width="120px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="签到天数"
        prop="day"
        :formatter="(_, __, cellValue) => `第 ${cellValue} 天`"
      />
      <el-table-column align="right" label="获得积分" prop="point" width="120px">
        <template #default="{ row }">
          <el-tag :type="row.point > 0 ? 'success' : 'danger'" effect="light" round>
            {{ row.point > 0 ? `+${row.point}` : row.point }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="签到时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
    </el-table>

    <el-result v-if="!loading && list.length === 0" icon="info" title="暂无数据" class="py-8" />

    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as SignInRecordApi from '@/api/member/signin/record'

defineOptions({ name: 'UserSignList' })

const loading = ref(false)
const total = ref(0)
const list = ref([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: NaN,
  nickname: null,
  day: null,
  createTime: []
})

const getList = async () => {
  loading.value = true
  try {
    const data = await SignInRecordApi.getSignInRecordPage(queryParams)
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
  queryParams.pageNo = 1
  getList()
}

const { userId } = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

onMounted(() => {
  queryParams.userId = userId
  getList()
})
</script>

<style scoped lang="scss">
.detail-filter-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  .filter-actions {
    margin-left: auto;
  }
}
</style>
