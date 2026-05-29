<template>
  <el-card shadow="never">
    <template #header>
      <slot name="header"></slot>
    </template>
    <el-row v-if="mode === 'member'" :gutter="16" class="basic-info-row">
      <el-col :lg="4" :md="6" :sm="24" class="mb-4">
        <ElAvatar :size="140" :src="user.avatar || undefined" shape="square" />
      </el-col>
      <el-col :lg="20" :md="18" :sm="24">
        <el-descriptions :column="2" class="basic-info-descriptions">
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:user" label="用户姓名" />
            </template>
            {{ user.name || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:user" label="昵称" />
            </template>
            {{ user.nickname || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:phone" label="手机号" />
            </template>
            {{ user.mobile || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="fa:mars-double" label="性别" />
            </template>
            <dict-tag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="user.sex" />
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:location" label="所在地区" />
            </template>
            {{ user.areaName || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:position" label="注册 IP" />
            </template>
            {{ user.registerIp || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="fa:birthday-cake" label="生日" />
            </template>
            {{ user.birthday ? formatDate(user.birthday as any) : '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:calendar" label="注册时间" />
            </template>
            {{ user.createTime ? formatDate(user.createTime as any) : '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <descriptions-item-label icon="ep:calendar" label="最近登录" />
            </template>
            {{ user.loginDate ? formatDate(user.loginDate as any) : '未填写' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-col>
    </el-row>

    <template v-if="mode === 'kefu'">
      <ElAvatar :size="140" :src="user.avatar || undefined" shape="square" />
      <el-descriptions :column="1" class="kefu-descriptions">
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:user" label="用户姓名" />
          </template>
          {{ user.name || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:user" label="昵称" />
          </template>
          {{ user.nickname || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:phone" label="手机号" />
          </template>
          {{ user.mobile || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="fa:mars-double" label="性别" />
          </template>
          <dict-tag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="user.sex" />
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:location" label="所在地区" />
          </template>
          {{ user.areaName || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:position" label="注册 IP" />
          </template>
          {{ user.registerIp || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="fa:birthday-cake" label="生日" />
          </template>
          {{ user.birthday ? formatDate(user.birthday as any) : '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:calendar" label="注册时间" />
          </template>
          {{ user.createTime ? formatDate(user.createTime as any) : '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <descriptions-item-label icon="ep:calendar" label="最近登录" />
          </template>
          {{ user.loginDate ? formatDate(user.loginDate as any) : '未填写' }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </el-card>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import * as UserApi from '@/api/member/user'
import { DescriptionsItemLabel } from '@/components/Descriptions/index'

withDefaults(defineProps<{ user: UserApi.UserVO; mode?: string }>(), {
  mode: 'member'
})
</script>

<style lang="scss" scoped>
.kefu-descriptions {
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
}

.basic-info-descriptions {
  @media (max-width: 640px) {
    :deep(.el-descriptions__cell) {
      display: flex;
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
