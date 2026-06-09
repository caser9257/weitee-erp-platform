<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1280">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <el-row>
        <el-col :span="8">
          <el-form-item label="合同编号" prop="no">
            <el-input disabled v-model="formData.no" placeholder="保存时自动生成" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="合同名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入合同名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="负责人" prop="ownerUserId">
            <el-select
              v-model="formData.ownerUserId"
              :disabled="formType !== 'create'"
              class="w-1/1"
            >
              <el-option
                v-for="item in userOptions"
                :key="item.id"
                :label="item.nickname"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="客户名称" prop="customerId">
            <el-select
              v-model="formData.customerId"
              placeholder="请选择客户"
              class="w-1/1"
              @change="handleCustomerChange"
            >
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="商机名称" prop="businessId">
            <el-select
              @change="handleBusinessChange"
              :disabled="!formData.customerId"
              v-model="formData.businessId"
              class="w-1/1"
            >
              <el-option
                v-for="item in getBusinessOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="下单日期" prop="orderDate">
            <el-date-picker
              v-model="formData.orderDate"
              placeholder="选择下单日期"
              type="date"
              value-format="x"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="formData.startTime"
              placeholder="选择开始时间"
              type="date"
              value-format="x"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="formData.endTime"
              placeholder="选择结束时间"
              type="date"
              value-format="x"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="公司签约人" prop="signUserId">
            <el-select v-model="formData.signUserId" class="w-1/1">
              <el-option
                v-for="item in userOptions"
                :key="item.id"
                :label="item.nickname"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="客户签约人" prop="signContactId">
            <el-select
              v-model="formData.signContactId"
              :disabled="!formData.customerId"
              class="w-1/1"
            >
              <el-option
                v-for="item in getContactOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" />
          </el-form-item>
        </el-col>
      </el-row>
      <!-- 子表的表单 -->
      <ContentWrap>
        <el-tabs v-model="subTabsName" class="-mt-15px -mb-10px">
          <el-tab-pane label="产品清单" name="product">
            <ContractProductForm
              ref="productFormRef"
              :products="formData.products"
              :disabled="disabled"
            />
          </el-tab-pane>
          <el-tab-pane label="商业条款" name="commercialTerms">
            <div class="commercial-terms-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="发货放行规则" prop="shipmentReleaseRule">
                    <el-select v-model="formData.shipmentReleaseRule" placeholder="请选择发货放行规则" class="!w-1/1">
                      <el-option label="签约即发" value="SIGN_AND_SHIP" />
                      <el-option label="到账后发" value="AFTER_PAYMENT" />
                      <el-option label="达到预付款比例后发" value="AFTER_PREPAYMENT" />
                      <el-option label="财务审核后发" value="FINANCE_APPROVAL" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开票触发条件" prop="invoiceTrigger">
                    <el-select v-model="formData.invoiceTrigger" placeholder="请选择开票触发条件" class="!w-1/1">
                      <el-option label="预付款全额开票" value="PREPAYMENT_FULL" />
                      <el-option label="预付款部分开票" value="PREPAYMENT_PARTIAL" />
                      <el-option label="仅预付款开票" value="PREPAYMENT_ONLY" />
                      <el-option label="发货后开票" value="AFTER_SHIPMENT" />
                      <el-option label="交付收款后开票" value="AFTER_DELIVERY_RECEIPT" />
                      <el-option label="手工决定" value="MANUAL" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="收款规则" prop="collectionRule">
                    <el-select v-model="formData.collectionRule" placeholder="请选择收款规则" class="!w-1/1">
                      <el-option label="发货前付款" value="BEFORE_SHIPMENT" />
                      <el-option label="发货时付款" value="ON_SHIPMENT" />
                      <el-option label="发货后约定期限付款" value="AFTER_SHIPMENT" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="预付款金额" prop="prepaymentAmount">
                    <el-input-number
                      v-model="formData.prepaymentAmount"
                      placeholder="请输入预付款金额"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      class="!w-1/1"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="预付款比例（%）" prop="prepaymentRatio">
                    <el-input-number
                      v-model="formData.prepaymentRatio"
                      placeholder="请输入预付款比例"
                      :min="0"
                      :max="100"
                      :precision="2"
                      controls-position="right"
                      class="!w-1/1"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="需要财务审核" prop="financeApprovalRequired">
                    <el-switch
                      v-model="formData.financeApprovalRequired"
                      :active-value="1"
                      :inactive-value="0"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="需要验收" prop="acceptanceRequired">
                    <el-switch
                      v-model="formData.acceptanceRequired"
                      :active-value="1"
                      :inactive-value="0"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="付款条件说明" prop="paymentTerms">
                    <el-input
                      v-model="formData.paymentTerms"
                      placeholder="请输入付款条件说明"
                      type="textarea"
                      :rows="2"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="发货条件说明" prop="shipmentConditions">
                    <el-input
                      v-model="formData.shipmentConditions"
                      placeholder="请输入发货条件说明"
                      type="textarea"
                      :rows="2"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="开票条件说明" prop="invoiceConditions">
                    <el-input
                      v-model="formData.invoiceConditions"
                      placeholder="请输入开票条件说明"
                      type="textarea"
                      :rows="2"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </ContentWrap>
      <el-row>
        <el-col :span="8">
          <el-form-item label="产品总金额" prop="totalProductPrice">
            <el-input
              disabled
              v-model="formData.totalProductPrice"
              :formatter="erpPriceInputFormatter"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="整单折扣（%）" prop="discountPercent">
            <el-input-number
              v-model="formData.discountPercent"
              placeholder="请输入整单折扣"
              controls-position="right"
              :min="0"
              :precision="2"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="折扣后金额" prop="totalPrice">
            <el-input
              disabled
              v-model="formData.totalPrice"
              placeholder="请输入商机金额"
              :formatter="erpPriceInputFormatter"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">保存</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as CustomerApi from '@/api/crm/customer'
import * as ContractApi from '@/api/crm/contract'
import * as UserApi from '@/api/system/user'
import * as ContactApi from '@/api/crm/contact'
import * as BusinessApi from '@/api/crm/business'
import { erpPriceMultiply, erpPriceInputFormatter } from '@/utils'
import { useUserStore } from '@/store/modules/user'
import ContractProductForm from '@/views/crm/contract/components/ContractProductForm.vue'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref<any>({
  id: undefined,
  no: undefined,
  name: undefined,
  customerId: undefined,
  businessId: undefined,
  orderDate: undefined,
  startTime: undefined,
  endTime: undefined,
  signUserId: undefined,
  signContactId: undefined,
  ownerUserId: undefined,
  discountPercent: 0,
  totalProductPrice: undefined,
  remark: undefined,
  products: [],
  // 商业条款字段
  shipmentReleaseRule: 'SIGN_AND_SHIP',
  invoiceTrigger: 'AFTER_SHIPMENT',
  collectionRule: 'BEFORE_SHIPMENT',
  prepaymentAmount: undefined,
  prepaymentRatio: undefined,
  financeApprovalRequired: 1,
  acceptanceRequired: 0,
  paymentTerms: undefined,
  shipmentConditions: undefined,
  invoiceConditions: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '合同名称不能为空', trigger: 'blur' }],
  customerId: [{ required: true, message: '客户不能为空', trigger: 'blur' }],
  orderDate: [{ required: true, message: '下单日期不能为空', trigger: 'blur' }],
  ownerUserId: [{ required: true, message: '负责人不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const userOptions = ref<UserApi.UserVO[]>([]) // 用户列表
const customerList = ref([]) // 客户列表的数据
const businessList = ref<BusinessApi.BusinessVO[]>([])
const contactList = ref<ContactApi.ContactVO[]>([])

/** 子表的表单 */
const subTabsName = ref('product')
const productFormRef = ref<any>()

/** 计算 discountPrice、totalPrice 价格 */
watch(
  () => formData.value,
  (val) => {
    if (!val) {
      return
    }
    const totalProductPrice = val.products.reduce((prev, curr) => prev + curr.totalPrice, 0)
    const discountPrice =
      val.discountPercent != null
        ? erpPriceMultiply(totalProductPrice, val.discountPercent / 100.0)
        : 0
    const totalPrice = totalProductPrice - discountPrice
    // 赋值
    formData.value.totalProductPrice = totalProductPrice
    formData.value.totalPrice = totalPrice
  },
  { deep: true }
)

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
      formData.value = await ContractApi.getContract(id)
    } finally {
      formLoading.value = false
    }
  }
  // 获得客户列表
  customerList.value = await CustomerApi.getCustomerSimpleList()
  // 获得用户列表
  userOptions.value = await UserApi.getSimpleUserList()
  // 默认新建时选中自己
  if (formType.value === 'create') {
    formData.value.ownerUserId = useUserStore().getUser.id
  }
  // 获取联系人
  contactList.value = await ContactApi.getSimpleContactList()
  // 获得商机列表
  businessList.value = await BusinessApi.getSimpleBusinessList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  productFormRef.value.validate()
  try {
    const data = unref(formData.value) as unknown as ContractApi.ContractVO
    if (formType.value === 'create') {
      await ContractApi.createContract(data)
      message.success(t('common.createSuccess'))
    } else {
      await ContractApi.updateContract(data)
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
    id: undefined,
    no: undefined,
    name: undefined,
    customerId: undefined,
    businessId: undefined,
    orderDate: undefined,
    startTime: undefined,
    endTime: undefined,
    signUserId: undefined,
    signContactId: undefined,
    ownerUserId: undefined,
    discountPercent: 0,
    totalProductPrice: undefined,
    remark: undefined,
    products: [],
    // 商业条款字段
    shipmentReleaseRule: 'SIGN_AND_SHIP',
    invoiceTrigger: 'AFTER_SHIPMENT',
    collectionRule: 'BEFORE_SHIPMENT',
    prepaymentAmount: undefined,
    prepaymentRatio: undefined,
    financeApprovalRequired: 1,
    acceptanceRequired: 0,
    paymentTerms: undefined,
    shipmentConditions: undefined,
    invoiceConditions: undefined
  }
  formRef.value?.resetFields()
}

/** 处理切换客户 */
const handleCustomerChange = () => {
  formData.value.businessId = undefined
  formData.value.signContactId = undefined
  formData.value.products = []
}

/** 处理商机变化 */
const handleBusinessChange = async (businessId: number) => {
  const business = await BusinessApi.getBusiness(businessId)
  business.products.forEach((item) => {
    item.contractPrice = item.businessPrice
  })
  formData.value.products = business.products
}

/** 动态获取客户联系人 */
const getContactOptions = computed(() =>
  contactList.value.filter((item) => item.customerId == formData.value.customerId)
)
/** 动态获取商机 */
const getBusinessOptions = computed(() =>
  businessList.value.filter((item) => item.customerId == formData.value.customerId)
)
</script>
