<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="800">
    <!-- 列表 -->
    <ContentWrap>
      <el-table v-loading="loading" :data="list">
        <el-table-column label="用户编号" align="center" prop="id" width="100" />
        <el-table-column label="用户名称" align="center" prop="username" />
        <el-table-column label="用户昵称" align="center" prop="nickname" />
        <el-table-column label="手机号码" align="center" prop="mobile" />
        <el-table-column label="状态" align="center" prop="status">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </Dialog>
</template>
<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import * as UserApi from '@/api/system/user'

defineOptions({ name: 'SystemPostUserList' })

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('岗下人员查看') // 弹窗的标题
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  postId: undefined as number | undefined
})

/** 打开弹窗 */
const open = async (postId: number, postName: string) => {
  dialogVisible.value = true
  dialogTitle.value = `查看人员 - ${postName}`
  queryParams.postId = postId
  queryParams.pageNo = 1
  getList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 查询岗位下的人员列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await UserApi.getUserPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
</script>
