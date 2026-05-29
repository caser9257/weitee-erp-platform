<template>
  <div class="member-user-detail">
    <div class="detail-context-card">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div class="min-w-0">
          <div class="text-12px text-slate-300">会员用户详情</div>
          <div class="mt-6px truncate text-24px font-semibold text-white">
            {{ user.nickname || '未获取到昵称' }}
          </div>
          <div class="mt-6px flex flex-wrap gap-2 text-12px text-slate-200">
            <span class="font-mono">{{ user.mobile || '-' }}</span>
            <span v-if="user.levelName" class="rounded-full bg-white/10 px-2 py-1">
              {{ user.levelName }}
            </span>
            <span v-if="user.groupName" class="rounded-full bg-white/10 px-2 py-1">
              {{ user.groupName }}
            </span>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <el-button plain @click="goBack">返回</el-button>
          <el-button :loading="refreshing" :disabled="refreshing" plain @click="refreshAll">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新
          </el-button>
          <el-button :disabled="refreshing" type="primary" @click="openForm('update')">
            <Icon class="mr-5px" icon="ep:edit" />
            编辑
          </el-button>
        </div>
      </div>

      <div class="mt-4 grid grid-cols-2 gap-3 text-right sm:grid-cols-4">
        <div class="rounded-2xl bg-white/10 px-3 py-2">
          <div class="text-12px text-slate-300">当前积分</div>
          <div class="mt-1 text-18px font-semibold text-white">{{ user.point || 0 }}</div>
        </div>
        <div class="rounded-2xl bg-white/10 px-3 py-2">
          <div class="text-12px text-slate-300">总积分</div>
          <div class="mt-1 text-18px font-semibold text-white">{{ user.totalPoint || 0 }}</div>
        </div>
        <div class="rounded-2xl bg-white/10 px-3 py-2">
          <div class="text-12px text-slate-300">余额</div>
          <div class="mt-1 text-18px font-semibold text-white">
            {{ fenToYuan(wallet.balance || 0) }}
          </div>
        </div>
        <div class="rounded-2xl bg-white/10 px-3 py-2">
          <div class="text-12px text-slate-300">成长值</div>
          <div class="mt-1 text-18px font-semibold text-white">{{ user.experience || 0 }}</div>
        </div>
      </div>
    </div>

    <el-skeleton v-if="initialLoading" animated :rows="8" />

    <el-result v-else-if="loadError" icon="error" title="会员详情加载失败" class="py-16">
      <template #extra>
        <el-button :loading="refreshing" type="primary" @click="refreshAll">重新加载</el-button>
      </template>
    </el-result>

    <template v-else>
      <el-row :gutter="16" class="mt-4">
        <el-col :lg="14" :md="24" class="mb-4">
          <UserBasicInfo :user="user">
            <template #header>
              <div class="card-header">
                <CardTitle title="基本信息" />
                <el-button :disabled="refreshing" size="small" text type="primary" @click="openForm('update')">
                  编辑
                </el-button>
              </div>
            </template>
          </UserBasicInfo>
        </el-col>
        <el-col :lg="10" :md="24" class="mb-4">
          <el-card class="h-full" shadow="never">
            <template #header>
              <CardTitle title="账户信息" />
            </template>
            <UserAccountInfo :column="1" :user="user" :wallet="wallet" />
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="mt-4">
        <template #header>
          <div class="flex items-center justify-between">
            <CardTitle title="账户明细" />
          </div>
        </template>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="积分" name="point">
            <UserPointList :user-id="id" />
          </el-tab-pane>
          <el-tab-pane label="签到" name="sign" lazy>
            <UserSignList :user-id="id" />
          </el-tab-pane>
          <el-tab-pane label="成长值" name="experience" lazy>
            <UserExperienceRecordList :user-id="id" />
          </el-tab-pane>
          <el-tab-pane label="余额" name="balance" lazy>
            <UserBalanceList :wallet-id="wallet.id" />
          </el-tab-pane>
          <el-tab-pane label="收货地址" name="address" lazy>
            <UserAddressList :user-id="id" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
  </div>

  <UserForm ref="formRef" @success="refreshAll" />
</template>

<script lang="ts" setup>
import * as WalletApi from '@/api/pay/wallet/balance'
import * as UserApi from '@/api/member/user'
import { useTagsViewStore } from '@/store/modules/tagsView'
import UserForm from '@/views/member/user/UserForm.vue'
import UserAccountInfo from './UserAccountInfo.vue'
import UserAddressList from './UserAddressList.vue'
import UserBasicInfo from './UserBasicInfo.vue'
import UserExperienceRecordList from './UserExperienceRecordList.vue'
import UserPointList from './UserPointList.vue'
import UserSignList from './UserSignList.vue'
import UserBalanceList from './UserBalanceList.vue'
import { CardTitle } from '@/components/Card/index'
import { ElMessage } from 'element-plus'
import { fenToYuan } from '@/utils'

defineOptions({ name: 'MemberDetail' })

const route = useRoute()
const router = useRouter()
const { currentRoute } = useRouter()
const { delView } = useTagsViewStore()

const id = Number(route.params.id || 0)
const initialLoading = ref(true)
const refreshing = ref(false)
const loadError = ref('')
const hasLoaded = ref(false)
const user = ref<UserApi.UserVO>({} as UserApi.UserVO)
const wallet = ref<WalletApi.WalletVO>({
  id: 0,
  userId: 0,
  userType: 0,
  balance: 0,
  totalExpense: 0,
  totalRecharge: 0,
  freezePrice: 0
})
const activeTab = ref('point')
const formRef = ref()

const getUserData = async () => {
  if (!id) {
    return
  }
  user.value = await UserApi.getUser(id)
}

const getUserWallet = async () => {
  if (!id) {
    return
  }
  wallet.value = (await WalletApi.getWallet({ userId: id })) || wallet.value
}

const refreshAll = async () => {
  if (refreshing.value) {
    return
  }
  refreshing.value = true
  if (!hasLoaded.value) {
    initialLoading.value = true
  }
  try {
    await Promise.all([getUserData(), getUserWallet()])
    loadError.value = ''
    hasLoaded.value = true
  } catch (error) {
    if (!hasLoaded.value) {
      loadError.value = '请稍后重试'
    } else {
      ElMessage.error('刷新失败，请稍后重试')
    }
  } finally {
    initialLoading.value = false
    refreshing.value = false
  }
}

const goBack = () => {
  router.back()
}

const openForm = (type: string) => {
  formRef.value?.open(type, id)
}

onMounted(async () => {
  if (!id) {
    ElMessage.warning('参数错误，会员编号不能为空')
    delView(unref(currentRoute))
    return
  }
  await refreshAll()
})
</script>

<style scoped lang="scss">
.member-user-detail {
  .detail-context-card {
    border-radius: 20px;
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    padding: 20px;
    box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  @media (max-width: 768px) {
    .detail-context-card {
      padding: 16px;
    }
  }
}
</style>
