<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <!-- 分组一：基本信息 -->
      <div class="form-group-title">
        <Icon icon="ep:info-filled" class="mr-5px text-primary" /> 基本信息
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="供应商名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入供应商名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人" prop="contact">
            <el-input v-model="formData.contact" placeholder="请输入联系人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号码" prop="mobile">
            <el-input v-model="formData.mobile" placeholder="请输入手机号码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电子邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入电子邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="显示排序" prop="sort">
            <el-input-number
              v-model="formData.sort"
              placeholder="请输入排序"
              class="!w-full"
              :min="0"
              :precision="0"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开启状态" prop="status">
            <el-switch
              v-model="formData.status"
              :active-value="0"
              :inactive-value="1"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 分组二：财务信息 -->
      <div class="form-group-title mt-10px">
        <Icon icon="ep:wallet" class="mr-5px text-primary" /> 财务参数
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="纳税人识别号" prop="taxNo">
            <el-input v-model="formData.taxNo" placeholder="请输入纳税号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="默认税率(%)" prop="taxPercent">
            <el-input-number
              v-model="formData.taxPercent"
              :min="0"
              :max="100"
              :precision="2"
              placeholder="请输入税率"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开户行名称" prop="bankName">
            <el-input v-model="formData.bankName" placeholder="请输入开户行名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开户账号" prop="bankAccount">
            <el-input v-model="formData.bankAccount" placeholder="请输入银行账户号" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="开户行地址" prop="bankAddress">
            <el-input v-model="formData.bankAddress" placeholder="请输入详细开户地址" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 分组三：附加信息 -->
      <div class="form-group-title mt-10px">
        <Icon icon="ep:paperclip" class="mr-5px text-primary" /> 额外附件与备注
      </div>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="供应商备注" prop="remark">
            <el-input 
              type="textarea" 
              v-model="formData.remark" 
              placeholder="请输入合作说明或供应商背景等备注信息..." 
              :rows="3"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="资质附件" prop="attachment">
            <UploadFile v-model="formData.attachment" :limit="5" class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { SupplierApi, SupplierVO } from '@/api/erp/purchase/supplier'
import { CommonStatusEnum } from '@/utils/constants'

/** ERP 供应商表单 */
defineOptions({ name: 'SupplierForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref<SupplierVO>({
  id: undefined as unknown as number,
  name: undefined as unknown as string,
  contact: undefined as unknown as string,
  mobile: undefined as unknown as string,
  telephone: undefined as unknown as string,
  email: undefined as unknown as string,
  fax: undefined as unknown as string,
  remark: undefined as unknown as string,
  attachment: undefined as unknown as string, // 附件字段
  status: undefined as unknown as number,
  sort: undefined as unknown as number,
  taxNo: undefined as unknown as string,
  taxPercent: undefined as unknown as number,
  bankName: undefined as unknown as string,
  bankAccount: undefined as unknown as string,
  bankAddress: undefined as unknown as string
})
const formRules = reactive({
  name: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '开启状态不能为空', trigger: 'change' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await SupplierApi.getSupplier(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as SupplierVO
    if (formType.value === 'create') {
      await SupplierApi.createSupplier(data)
      message.success(t('common.createSuccess'))
    } else {
      await SupplierApi.updateSupplier(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined as unknown as number,
    name: undefined as unknown as string,
    contact: undefined as unknown as string,
    mobile: undefined as unknown as string,
    telephone: undefined as unknown as string,
    email: undefined as unknown as string,
    fax: undefined as unknown as string,
    remark: undefined as unknown as string,
    attachment: undefined as unknown as string,
    status: CommonStatusEnum.ENABLE,
    sort: undefined as unknown as number,
    taxNo: undefined as unknown as string,
    taxPercent: undefined as unknown as number,
    bankName: undefined as unknown as string,
    bankAccount: undefined as unknown as string,
    bankAddress: undefined as unknown as string
  }
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.form-group-title {
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-light);
}
</style>
