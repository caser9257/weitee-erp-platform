<template>
  <el-descriptions :class="{ 'account-descriptions': column === 1 }" :column="column">
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="svg-icon:member_level" label="等级" />
      </template>
      {{ user.levelName || '未设置' }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="ep:suitcase" label="成长值" />
      </template>
      {{ user.experience ?? 0 }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="ep:coin" label="当前积分" />
      </template>
      {{ user.point ?? 0 }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="ep:coin" label="总积分" />
      </template>
      {{ user.totalPoint ?? 0 }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="svg-icon:member_balance" label="当前余额" />
      </template>
      {{ fenToYuan(wallet.balance ?? 0) }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="svg-icon:member_expenditure_balance" label="支出金额" />
      </template>
      {{ fenToYuan(wallet.totalExpense ?? 0) }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <descriptions-item-label icon="svg-icon:member_recharge_balance" label="充值金额" />
      </template>
      {{ fenToYuan(wallet.totalRecharge ?? 0) }}
    </el-descriptions-item>
  </el-descriptions>
</template>

<script lang="ts" setup>
import { DescriptionsItemLabel } from '@/components/Descriptions'
import * as UserApi from '@/api/member/user'
import * as WalletApi from '@/api/pay/wallet/balance'
import { fenToYuan } from '@/utils'

withDefaults(defineProps<{ user: UserApi.UserVO; wallet: WalletApi.WalletVO; column?: number }>(), {
  column: 2
})
</script>

<style lang="scss" scoped>
.account-descriptions {
  :deep(.el-descriptions__cell) {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .el-descriptions__label {
      width: 120px;
      display: block;
      text-align: left;
    }

    .el-descriptions__content {
      flex: 1;
      text-align: end;
    }
  }

  @media (max-width: 640px) {
    :deep(.el-descriptions__cell) {
      flex-direction: column;
      align-items: flex-start;

      .el-descriptions__label,
      .el-descriptions__content {
        width: 100%;
        text-align: left;
      }

      .el-descriptions__content {
        margin-top: 4px;
      }
    }
  }
}
</style>
