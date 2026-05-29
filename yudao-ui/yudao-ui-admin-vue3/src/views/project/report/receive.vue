<template>
  <div class="receive-report-page">
    <ContentWrap>
      <el-table v-loading="loading" :data="list" :stripe="true">
        <el-table-column label="提交人" prop="creatorName" width="120" />
        <el-table-column label="类型" prop="type" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'daily'" size="small">日报</el-tag>
            <el-tag v-else-if="row.type === 'weekly'" type="warning" size="small">周报</el-tag>
            <el-tag v-else type="success" size="small">月报</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" min-width="200" show-overflow-tooltip />
        <el-table-column label="提交时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ReportApi } from '@/api/project/report'

defineOptions({ name: 'ReceiveReport' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })

const getList = async () => {
  loading.value = true
  try {
    const data = await ReportApi.getReceiveReportPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleView = (row: any) => {
  message.alert(row.content, '日报内容')
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.receive-report-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
