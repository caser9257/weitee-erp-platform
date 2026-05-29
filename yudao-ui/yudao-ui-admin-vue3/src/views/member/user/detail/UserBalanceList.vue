<template>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column align="center" label="编号" prop="id" width="120px">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-500">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column align="left" label="关联业务标题" prop="title" min-width="180px">
        <template #default="{ row }">
          <span class="truncate" :title="row.title">{{ row.title || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="交易金额" prop="price" width="140px">
        <template #default="{ row }">
          <span class="font-mono">{{ fenToYuan(row.price) }} 元</span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="钱包余额" prop="balance" width="140px">
        <template #default="{ row }">
          <span class="font-mono">{{ fenToYuan(row.balance) }} 元</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        label="交易时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
    </el-table>

    <el-result v-if="!loading && list.length === 0" icon="info" title="暂无数据" class="py-8" />

    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as WalletTransactionApi from '@/api/pay/wallet/transaction'
import { fenToYuan } from '@/utils'

defineOptions({ name: 'UserBalanceList' })
const props = defineProps({
  walletId: {
    type: Number,
    required: false
  }
})

const loading = ref(false)
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  walletId: null
})
const list = ref([])

const getList = async () => {
  loading.value = true
  try {
    queryParams.walletId = props.walletId as any
    const data = await WalletTransactionApi.getWalletTransactionPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
