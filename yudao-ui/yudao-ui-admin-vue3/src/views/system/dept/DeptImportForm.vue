<template>
  <Dialog v-model="dialogVisible" title="閮ㄩ棬瀵煎叆" width="400">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="importUrl + '?updateSupport=' + updateSupport"
      :auto-upload="false"
      :disabled="formLoading"
      :headers="uploadHeaders"
      :limit="1"
      :on-error="submitFormError"
      :on-exceed="handleExceed"
      :on-success="submitFormSuccess"
      accept=".xlsx, .xls"
      drag
    >
      <Icon icon="ep:upload" />
      <div class="el-upload__text">灏嗘枃浠舵嫋鍒版澶勶紝鎴?em>鐐瑰嚮涓婁紶</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          <div class="el-upload__tip">
            <el-checkbox v-model="updateSupport" />
            鏄惁鏇存柊宸茬粡瀛樺湪鐨勯儴闂ㄦ暟鎹?          </div>
          <span>浠呭厑璁稿鍏?xls銆亁lsx 鏍煎紡鏂囦欢銆?/span>
          <el-link
            :underline="false"
            style="font-size: 12px; vertical-align: baseline"
            type="primary"
            @click="importTemplate"
          >
            涓嬭浇妯℃澘
          </el-link>
        </div>
      </template>
    </el-upload>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">纭?瀹?/el-button>
      <el-button @click="dialogVisible = false">鍙?娑?/el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as DeptApi from '@/api/system/dept'
import { getAccessToken } from '@/utils/auth'
import download from '@/utils/download'

defineOptions({ name: 'SystemDeptImportForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const uploadRef = ref()
const importUrl =
  import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/system/dept/import'
const uploadHeaders = ref()
const fileList = ref([])
const updateSupport = ref(false)

/** 鎵撳紑寮圭獥 */
const open = () => {
  dialogVisible.value = true
  updateSupport.value = false
  fileList.value = []
  resetForm()
}
defineExpose({ open })

/** 鎻愪氦琛ㄥ崟 */
const submitForm = async () => {
  if (fileList.value.length === 0) {
    message.error('璇蜂笂浼犳枃浠?)
    return
  }
  uploadHeaders.value = {
    Authorization: 'Bearer ' + getAccessToken(),
    'tenant-id':()
  }
  formLoading.value = true
  uploadRef.value?.submit()
}

const emits = defineEmits(['success'])

/** 鏂囦欢涓婁紶鎴愬姛 */
const submitFormSuccess = (response: any) => {
  if (response.code !== 0) {
    message.error(response.msg)
    resetForm()
    return
  }
  const data = response.data
  let text = '瀵煎叆鎴愬姛鏁伴噺锛? + data.createDeptNames.length + '锛?
  for (const name of data.createDeptNames) {
    text += '< ' + name + ' >'
  }
  text += '鏇存柊鎴愬姛鏁伴噺锛? + data.updateDeptNames.length + '锛?
  for (const name of data.updateDeptNames) {
    text += '< ' + name + ' >'
  }
  text += '瀵煎叆澶辫触鏁伴噺锛? + Object.keys(data.failureDeptNames).length + '锛?
  for (const name in data.failureDeptNames) {
    text += '< ' + name + ': ' + data.failureDeptNames[name] + ' >'
  }
  message.alert(text)
  formLoading.value = false
  dialogVisible.value = false
  emits('success')
}

/** 涓婁紶閿欒鎻愮ず */
const submitFormError = () => {
  message.error('涓婁紶澶辫触锛岃鎮ㄩ噸鏂颁笂浼狅紒')
  formLoading.value = false
}

/** 閲嶇疆琛ㄥ崟 */
const resetForm = async () => {
  formLoading.value = false
  await nextTick()
  uploadRef.value?.clearFiles()
}

/** 鏂囦欢鏁拌秴鍑烘彁绀?*/
const handleExceed = () => {
  message.error('鏈€澶氬彧鑳戒笂浼犱竴涓枃浠讹紒')
}

/** 涓嬭浇妯℃澘 */
const importTemplate = async () => {
  const res = await DeptApi.importDeptTemplate()
  download.excel(res, '閮ㄩ棬瀵煎叆妯℃澘.xls')
}
</script>

