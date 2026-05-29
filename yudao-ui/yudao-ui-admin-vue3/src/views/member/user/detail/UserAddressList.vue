<template>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column align="center" label="地址编号" prop="id" width="120px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column align="left" label="收件人名称" prop="name" width="140px" />
      <el-table-column align="center" label="手机号" prop="mobile" width="140px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.mobile || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="地区编码" prop="areaId" width="140px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.areaId || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="left" label="收件详细地址" prop="detailAddress" min-width="220px">
        <template #default="{ row }">
          <span class="truncate" :title="row.detailAddress">{{ row.detailAddress || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="是否默认" prop="defaultStatus" width="120px">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="Number(row.defaultStatus)" />
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
    </el-table>

    <el-result v-if="!loading && list.length === 0" icon="info" title="暂无数据" class="py-8" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as AddressApi from '@/api/member/address'

const { userId }: { userId: number } = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const list = ref([])

const getList = async () => {
  loading.value = true
  try {
    list.value = (await AddressApi.getAddressList({ userId })) || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
