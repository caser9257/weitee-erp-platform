<script lang="ts" setup>
import { useWebSocket } from '@vueuse/core'
import * as NotifyMessageApi from '@/api/system/notify/message'
import { useUserStoreWithOut } from '@/store/modules/user'
import { formatDate } from '@/utils/formatTime'
import { getRefreshToken } from '@/utils/auth'
import { propTypes } from '@/utils/propTypes'

defineOptions({ name: 'Message' })

defineProps({
  color: propTypes.string.def('')
})

const NOTIFY_MESSAGE_CREATE_TYPE = 'notify-message-create'

const message = useMessage()
const { push } = useRouter()
const userStore = useUserStoreWithOut()

const activeName = ref('notice')
const unreadCount = ref(0)
const list = ref<NotifyMessageApi.NotifyMessageVO[]>([])
const countRefreshing = ref(false)
const listRefreshing = ref(false)
const popoverVisible = ref(false)

const hasUnread = computed(() => unreadCount.value > 0)
const canRefreshList = computed(() => userStore.getIsSetUser && !listRefreshing.value)

let unreadCountTimer: number | undefined

const websocketUrl = computed(() => {
  return (
    (import.meta.env.VITE_BASE_URL + '/infra/ws').replace('http', 'ws') +
    '?token=' +
    getRefreshToken()
  )
})

const { data, status } = useWebSocket(websocketUrl, {
  autoReconnect: true,
  heartbeat: true
})

const websocketConnected = computed(() => status.value === 'OPEN')

const getList = async () => {
  if (!canRefreshList.value) {
    return
  }
  listRefreshing.value = true
  try {
    list.value = await NotifyMessageApi.getUnreadNotifyMessageList()
  } catch (error) {
    console.error(error)
    message.notifyError('获取站内信失败，请稍后重试')
  } finally {
    listRefreshing.value = false
  }
}

const getUnreadCount = async () => {
  if (!userStore.getIsSetUser || countRefreshing.value) {
    return
  }
  countRefreshing.value = true
  try {
    unreadCount.value = await NotifyMessageApi.getUnreadNotifyMessageCount()
  } catch (error) {
    console.error(error)
  } finally {
    countRefreshing.value = false
  }
}

const goMyList = () => {
  push({ name: 'MyNotifyMessage' })
}

const handleNotifyMessageCreated = async (
  payload: NotifyMessageApi.NotifyMessageWebSocketPayload
) => {
  await getUnreadCount()
  if (popoverVisible.value) {
    await getList()
  }
  message.notify(payload.templateContent || '您收到了一条新的站内信')
}

watch(data, async (value) => {
  if (!value || value === 'pong' || !userStore.getIsSetUser) {
    return
  }
  try {
    const jsonMessage = JSON.parse(value)
    if (jsonMessage?.type !== NOTIFY_MESSAGE_CREATE_TYPE) {
      return
    }
    const payload =
      typeof jsonMessage.content === 'string'
        ? JSON.parse(jsonMessage.content)
        : jsonMessage.content
    await handleNotifyMessageCreated(payload as NotifyMessageApi.NotifyMessageWebSocketPayload)
  } catch (error) {
    console.error(error)
  }
})

onMounted(() => {
  getUnreadCount()
  unreadCountTimer = window.setInterval(() => {
    if (userStore.getIsSetUser) {
      getUnreadCount()
    } else {
      unreadCount.value = 0
      list.value = []
    }
  }, 1000 * 60 * 2)
})

onUnmounted(() => {
  if (unreadCountTimer !== undefined) {
    window.clearInterval(unreadCountTimer)
  }
})
</script>

<template>
  <div class="message">
    <ElPopover
      :width="400"
      placement="bottom"
      trigger="click"
      @hide="popoverVisible = false"
      @show="popoverVisible = true"
    >
      <template #reference>
        <ElBadge :is-dot="hasUnread" class="item">
          <Icon :size="18" class="cursor-pointer" icon="ep:bell" :color="color" @click="getList" />
        </ElBadge>
      </template>
      <ElTabs v-model="activeName">
        <ElTabPane label="我的站内信" name="notice">
          <el-scrollbar class="message-list">
            <template v-for="item in list" :key="item.id">
              <div class="message-item">
                <img alt="" class="message-icon" src="@/assets/imgs/avatar.gif" />
                <div class="message-content">
                  <span class="message-title">
                    {{ item.templateNickname }}：{{ item.templateContent }}
                  </span>
                  <span class="message-date">
                    {{ formatDate(item.createTime) }}
                  </span>
                </div>
              </div>
            </template>
          </el-scrollbar>
        </ElTabPane>
      </ElTabs>
      <div style="margin-top: 10px; text-align: right">
        <XButton preIcon="ep:view" title="查看全部" type="primary" @click="goMyList" />
      </div>
      <div v-if="!websocketConnected" class="message-status">
        实时提醒暂未连接，当前使用轮询兜底
      </div>
    </ElPopover>
  </div>
</template>

<style lang="scss" scoped>
.message-list {
  display: flex;
  height: 400px;
  flex-direction: column;

  .message-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid var(--el-border-color-light);

    &:last-child {
      border: none;
    }

    .message-icon {
      width: 40px;
      height: 40px;
      margin: 0 20px 0 5px;
    }

    .message-content {
      display: flex;
      flex-direction: column;

      .message-title {
        margin-bottom: 5px;
      }

      .message-date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.message-status {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
