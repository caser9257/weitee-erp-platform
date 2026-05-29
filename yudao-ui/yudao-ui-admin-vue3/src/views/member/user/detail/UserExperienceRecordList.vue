<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="detail-filter-form"
      label-width="72px"
    >
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" clearable placeholder="请选择业务类型" class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.MEMBER_EXPERIENCE_BIZ_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          clearable
          placeholder="请输入标题"
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
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

    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column align="center" label="编号" prop="id" width="150px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="获得时间"
        prop="createTime"
        :formatter="dateFormatter"
      />
      <el-table-column align="right" label="经验" prop="experience" width="140px">
        <template #default="{ row }">
          <el-tag :type="row.experience > 0 ? 'success' : 'danger'" effect="light" round>
            {{ row.experience > 0 ? `+${row.experience}` : row.experience }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="right" label="总经验" prop="totalExperience" width="140px">
        <template #default="{ row }">
          <span class="font-mono">{{ row.totalExperience || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column align="left" label="标题" prop="title" min-width="160px">
        <template #default="{ row }">
          <span class="truncate" :title="row.title">{{ row.title || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="left" label="描述" prop="description" min-width="220px">
        <template #default="{ row }">
          <span class="truncate" :title="row.description">{{ row.description || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="业务编号" prop="bizId" width="150px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.bizId || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="业务类型" prop="bizType" width="150px">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.MEMBER_EXPERIENCE_BIZ_TYPE" :value="row.bizType" />
        </template>
      </el-table-column>
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

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as ExperienceRecordApi from '@/api/member/experience-record/index'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'UserExperienceRecordList' })

const loading = ref(false)
const total = ref(0)
const list = ref([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: null,
  bizId: null,
  bizType: null,
  title: null,
  description: null,
  experience: null,
  totalExperience: null,
  createTime: []
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ExperienceRecordApi.getExperienceRecordPage(queryParams)
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
