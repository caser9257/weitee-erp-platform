<script lang="ts" setup>
import { useWebSocket } from '@vueuse/core'
import * as NotifyMessageApi from '@/api/system/notify/message'
import { useUserStoreWithOut } from '@/store/modules/user'
import { formatDate } from '@/utils/formatTime'
import { getRefreshToken } from '@/utils/auth'
import { propTypes } from '@/utils/propTypes'
import { getNotifyMessagePreview, stripNotifyMessageLinks } from './notifyMessage'

defineOptions({ name: 'Message' })

defineProps({
  color: propTypes.string.def('')
})

const NOTIFY_MESSAGE_CREATE_TYPE = 'notify-message-create'
const TEXT = {
  fetchError: '\u83b7\u53d6\u7ad9\u5185\u4fe1\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
  newMessage: '\u60a8\u6536\u5230\u4e86 1 \u6761\u65b0\u7684\u7ad9\u5185\u4fe1',
  readSuccess: '\u5df2\u6807\u8bb0\u4e3a\u5df2\u8bfb',
  readError: '\u6807\u8bb0\u5df2\u8bfb\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
  navigateError: '\u8df3\u8f6c\u5904\u7406\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
  entryTitle: '\u4f60\u6709\u65b0\u7684\u672a\u8bfb\u901a\u77e5',
  entryDescription:
    '\u8fdb\u5165\u7cfb\u7edf\u65f6\u68c0\u6d4b\u5230\u5f53\u524d\u8d26\u53f7\u5b58\u5728\u672a\u8bfb\u7ad9\u5185\u4fe1\uff0c\u5efa\u8bae\u53ca\u65f6\u5904\u7406\u3002',
  entryLaterLabel: '\u7a0d\u540e\u5904\u7406',
  entryViewNowLabel: '\u7acb\u5373\u67e5\u770b',
  tabLabel: '\u672a\u8bfb\u7ad9\u5185\u4fe1',
  emptyLabel: '\u6682\u65e0\u672a\u8bfb\u901a\u77e5',
  readLabel: '\u6807\u4e3a\u5df2\u8bfb',
  viewAllLabel: '\u67e5\u770b\u5168\u90e8',
  socketStatus:
    '\u5b9e\u65f6\u63d0\u9192\u6682\u672a\u8fde\u63a5\uff0c\u5f53\u524d\u4f7f\u7528\u8f6e\u8be2\u515c\u5e95\u3002'
} as const

type MessageCard = {
  item: NotifyMessageApi.NotifyMessageVO
  preview: ReturnType<typeof getNotifyMessagePreview>
}

const message = useMessage()
const router = useRouter()
const userStore = useUserStoreWithOut()

const activeName = ref('notice')
const unreadCount = ref(0)
const list = ref<NotifyMessageApi.NotifyMessageVO[]>([])
const countRefreshing = ref(false)
const listRefreshing = ref(false)
const updatingIds = ref<number[]>([])
const navigatingIds = ref<number[]>([])
const popoverVisible = ref(false)
const bootCheckingUnread = ref(false)
const entryNotifyDialogVisible = ref(false)
const entryDialogDismissed = ref(false)
const entryDialogActionLoading = ref(false)
const entryPreviewMessage = ref<NotifyMessageApi.NotifyMessageVO>()

const hasUnread = computed(() => unreadCount.value > 0)
const badgeValue = computed(() => {
  if (!hasUnread.value) {
    return undefined
  }
  return unreadCount.value > 99 ? '99+' : unreadCount.value
})
const canRefreshList = computed(() => userStore.getIsSetUser && !listRefreshing.value)
const messageCards = computed<MessageCard[]>(() =>
  list.value.map((item) => ({
    item,
    preview: getNotifyMessagePreview(item)
  }))
)
const hasEntryPreviewMessage = computed(() => !!entryPreviewMessage.value)
const entryPreviewCard = computed(() =>
  entryPreviewMessage.value ? getNotifyMessagePreview(entryPreviewMessage.value) : undefined
)
const canOpenNotifyList = computed(
  () => hasEntryPreviewMessage.value && !entryDialogActionLoading.value
)
const entryPrimaryActionLabel = computed(
  () => entryPreviewCard.value?.action?.label || TEXT.entryViewNowLabel
)

let unreadCountTimer: number | undefined
const isWebSocketEnabled = import.meta.env.VITE_APP_MESSAGE_WEBSOCKET_ENABLE === 'true'

const websocketUrl = computed(() => {
  return (
    (import.meta.env.VITE_BASE_URL + '/infra/ws').replace('http', 'ws') +
    '?token=' +
    getRefreshToken()
  )
})

const { data, status } = useWebSocket(websocketUrl, {
  autoReconnect: isWebSocketEnabled,
  heartbeat: isWebSocketEnabled,
  immediate: isWebSocketEnabled
})

const websocketConnected = computed(() => isWebSocketEnabled && status.value === 'OPEN')

const getList = async (options?: { silent?: boolean }) => {
  if (!canRefreshList.value) {
    return list.value
  }
  listRefreshing.value = true
  try {
    list.value = await NotifyMessageApi.getUnreadNotifyMessageList()
    return list.value
  } catch (error) {
    console.error(error)
    if (!options?.silent) {
      message.notifyError(TEXT.fetchError)
    }
    return []
  } finally {
    listRefreshing.value = false
  }
}

const getUnreadCount = async () => {
  if (!userStore.getIsSetUser || countRefreshing.value) {
    return unreadCount.value
  }
  countRefreshing.value = true
  try {
    unreadCount.value = await NotifyMessageApi.getUnreadNotifyMessageCount()
    return unreadCount.value
  } catch (error) {
    console.error(error)
    return unreadCount.value
  } finally {
    countRefreshing.value = false
  }
}

const addPendingId = (target: typeof updatingIds, id: number) => {
  if (target.value.includes(id)) {
    return
  }
  target.value = [...target.value, id]
}

const removePendingId = (target: typeof updatingIds, id: number) => {
  target.value = target.value.filter((itemId) => itemId !== id)
}

const isUpdating = (id: number) => updatingIds.value.includes(id)
const isNavigating = (id: number) => navigatingIds.value.includes(id)
const isBusy = (id: number) => isUpdating(id) || isNavigating(id)

const canMarkRead = (id: number) => !isBusy(id)
const canGoHandle = (card: MessageCard) => !!card.preview.action && !isBusy(card.item.id)

const syncEntryPreviewMessage = (messages: NotifyMessageApi.NotifyMessageVO[] = list.value) => {
  entryPreviewMessage.value = messages[0]
  if (!entryPreviewMessage.value) {
    entryNotifyDialogVisible.value = false
  }
}

const removeUnreadItem = (id: number) => {
  const nextList = list.value.filter((item) => item.id !== id)
  if (nextList.length === list.value.length) {
    return
  }
  list.value = nextList
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  syncEntryPreviewMessage(nextList)
}

const markItemRead = async (id: number) => {
  await NotifyMessageApi.updateNotifyMessageRead(id)
  removeUnreadItem(id)
}

const goMyList = async () => {
  await router.push({ name: 'MyNotifyMessage' })
}

const navigateByAction = async (action?: MessageCard['preview']['action']) => {
  if (action?.route) {
    await router.push(action.route)
    return
  }
  if (action?.href) {
    window.open(action.href, '_blank', 'noopener')
    return
  }
  await goMyList()
}

const handleReadOne = async (item: NotifyMessageApi.NotifyMessageVO) => {
  if (!canMarkRead(item.id)) {
    return
  }
  addPendingId(updatingIds, item.id)
  try {
    await markItemRead(item.id)
    message.success(TEXT.readSuccess)
  } catch (error) {
    console.error(error)
    message.error(TEXT.readError)
  } finally {
    removePendingId(updatingIds, item.id)
  }
}

const handleGoHandle = async (card: MessageCard) => {
  const action = card.preview.action
  if (!action || !canGoHandle(card)) {
    return
  }
  addPendingId(navigatingIds, card.item.id)
  try {
    await markItemRead(card.item.id)
    popoverVisible.value = false
    await navigateByAction(action)
  } catch (error) {
    console.error(error)
    message.error(TEXT.navigateError)
  } finally {
    removePendingId(navigatingIds, card.item.id)
  }
}

const handleNotifyMessageCreated = async (
  payload: NotifyMessageApi.NotifyMessageWebSocketPayload
) => {
  await getUnreadCount()
  if (popoverVisible.value) {
    await getList()
  }
  message.notify(stripNotifyMessageLinks(payload.templateContent) || TEXT.newMessage)
}

const handlePopoverShow = async () => {
  popoverVisible.value = true
  await getList()
}

const handleDismissEntryDialog = () => {
  entryDialogDismissed.value = true
  entryNotifyDialogVisible.value = false
}

const handleOpenNotifyListFromEntry = async () => {
  if (!canOpenNotifyList.value) {
    return
  }
  entryDialogActionLoading.value = true
  try {
    const previewMessage = entryPreviewMessage.value
    const action = entryPreviewCard.value?.action
    if (previewMessage) {
      await markItemRead(previewMessage.id)
    }
    entryDialogDismissed.value = true
    entryNotifyDialogVisible.value = false
    await navigateByAction(action)
  } catch (error) {
    console.error(error)
    entryDialogDismissed.value = false
    entryNotifyDialogVisible.value = true
    syncEntryPreviewMessage()
    message.error(TEXT.navigateError)
  } finally {
    entryDialogActionLoading.value = false
  }
}

const runEntryUnreadCheck = async () => {
  if (!userStore.getIsSetUser || entryDialogDismissed.value || bootCheckingUnread.value) {
    return
  }
  bootCheckingUnread.value = true
  try {
    const count = await getUnreadCount()
    if (!count) {
      syncEntryPreviewMessage([])
      entryNotifyDialogVisible.value = false
      return
    }
    const unreadList = await getList({ silent: true })
    syncEntryPreviewMessage(unreadList)
    entryNotifyDialogVisible.value = !!entryPreviewMessage.value
  } finally {
    bootCheckingUnread.value = false
  }
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
  runEntryUnreadCheck()
  unreadCountTimer = window.setInterval(() => {
    if (userStore.getIsSetUser) {
      getUnreadCount()
    } else {
      unreadCount.value = 0
      list.value = []
      updatingIds.value = []
      navigatingIds.value = []
      bootCheckingUnread.value = false
      entryNotifyDialogVisible.value = false
      entryDialogDismissed.value = false
      entryDialogActionLoading.value = false
      entryPreviewMessage.value = undefined
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
      :width="460"
      placement="bottom"
      trigger="click"
      @hide="popoverVisible = false"
      @show="handlePopoverShow"
    >
      <template #reference>
        <div :class="['message-trigger', { 'is-active': hasUnread }]">
          <ElBadge :hidden="!hasUnread" :value="badgeValue" class="message-trigger__badge">
            <Icon :size="18" class="cursor-pointer" icon="ep:bell" :color="color" />
          </ElBadge>
        </div>
      </template>
      <ElTabs v-model="activeName" class="message-tabs">
        <ElTabPane :label="TEXT.tabLabel" name="notice">
          <div v-loading="listRefreshing" class="message-panel">
            <el-scrollbar class="message-list">
              <div v-if="messageCards.length === 0" class="message-empty">
                <ElEmpty :image-size="88" :description="TEXT.emptyLabel" />
              </div>
              <div v-for="card in messageCards" :key="card.item.id" class="message-card">
                <div class="message-card__header">
                  <div class="message-card__icon">
                    <Icon :icon="card.preview.icon" />
                  </div>
                  <div class="message-card__main">
                    <div class="message-card__title-row">
                      <ElTag
                        :type="card.preview.categoryType"
                        effect="light"
                        round
                        size="small"
                      >
                        {{ card.preview.categoryLabel }}
                      </ElTag>
                      <span class="message-card__title">{{ card.preview.title }}</span>
                    </div>
                    <span class="message-card__date">
                      {{ formatDate(card.item.createTime) }}
                    </span>
                  </div>
                </div>
                <div v-if="card.preview.summaryItems.length > 0" class="message-card__summary">
                  <div
                    v-for="summaryItem in card.preview.summaryItems"
                    :key="summaryItem.label"
                    class="message-card__summary-item"
                  >
                    <span class="message-card__summary-label">{{ summaryItem.label }}</span>
                    <span class="message-card__summary-value">{{ summaryItem.value }}</span>
                  </div>
                </div>
                <div v-if="card.preview.body" class="message-card__body">
                  {{ card.preview.body }}
                </div>
                <div class="message-card__actions">
                  <el-button
                    link
                    type="primary"
                    :disabled="!canMarkRead(card.item.id)"
                    :loading="isUpdating(card.item.id)"
                    @click="handleReadOne(card.item)"
                  >
                    {{ TEXT.readLabel }}
                  </el-button>
                  <el-button
                    v-if="card.preview.action"
                    link
                    type="success"
                    :disabled="!canGoHandle(card)"
                    :loading="isNavigating(card.item.id)"
                    @click="handleGoHandle(card)"
                  >
                    {{ card.preview.action.label }}
                  </el-button>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </ElTabPane>
      </ElTabs>
      <div class="message-footer">
        <XButton preIcon="ep:view" :title="TEXT.viewAllLabel" type="primary" @click="goMyList" />
      </div>
      <div v-if="!websocketConnected" class="message-status">
        {{ TEXT.socketStatus }}
      </div>
    </ElPopover>
    <ElDialog
      v-model="entryNotifyDialogVisible"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      width="420px"
      class="entry-notify-dialog"
      @close="handleDismissEntryDialog"
    >
      <div v-if="entryPreviewCard && entryPreviewMessage" class="entry-notify-card">
        <div class="entry-notify-card__hero">
          <div class="entry-notify-card__badge">
            <Icon :icon="entryPreviewCard.icon" />
          </div>
          <div class="entry-notify-card__hero-copy">
            <span class="entry-notify-card__eyebrow">{{ TEXT.entryTitle }}</span>
            <h3 class="entry-notify-card__title">{{ entryPreviewCard.title }}</h3>
          </div>
        </div>
        <div class="entry-notify-card__meta">
          <ElTag :type="entryPreviewCard.categoryType" effect="light" round size="small">
            {{ entryPreviewCard.categoryLabel }}
          </ElTag>
          <span class="entry-notify-card__date">
            {{ formatDate(entryPreviewMessage.createTime) }}
          </span>
        </div>
        <p class="entry-notify-card__description">{{ TEXT.entryDescription }}</p>
        <div v-if="entryPreviewCard.summaryItems.length > 0" class="entry-notify-card__summary">
          <div
            v-for="summaryItem in entryPreviewCard.summaryItems"
            :key="summaryItem.label"
            class="entry-notify-card__summary-item"
          >
            <span class="entry-notify-card__summary-label">{{ summaryItem.label }}</span>
            <span class="entry-notify-card__summary-value">{{ summaryItem.value }}</span>
          </div>
        </div>
        <div v-if="entryPreviewCard.body" class="entry-notify-card__body">
          {{ entryPreviewCard.body }}
        </div>
        <div class="entry-notify-card__actions">
          <el-button @click="handleDismissEntryDialog">{{ TEXT.entryLaterLabel }}</el-button>
          <el-button
            type="primary"
            :loading="entryDialogActionLoading"
            :disabled="!canOpenNotifyList"
            @click="handleOpenNotifyListFromEntry"
          >
            {{ entryPrimaryActionLabel }}
          </el-button>
        </div>
      </div>
    </ElDialog>
  </div>
</template>

<style lang="scss" scoped>
:deep(.entry-notify-dialog) {
  .el-dialog {
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 28px 80px rgba(15, 23, 42, 0.18);
  }

  .el-dialog__header {
    padding-bottom: 0;
    margin-right: 0;
  }

  .el-dialog__body {
    padding-top: 8px;
  }
}

.message-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-active {
    color: var(--el-color-danger);
    background: var(--el-color-danger-light-9);
    box-shadow: inset 0 0 0 1px var(--el-color-danger-light-7);
  }
}

:deep(.message-trigger__badge .el-badge__content) {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border: 2px solid var(--el-bg-color);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
}

.message-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 12px;
  }
}

.message-panel {
  min-height: 260px;
}

.message-list {
  height: 400px;
  padding-right: 4px;
}

.message-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;
}

.message-card {
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  background: linear-gradient(180deg, var(--el-fill-color-extra-light) 0%, var(--el-bg-color) 100%);

  &:last-child {
    margin-bottom: 0;
  }
}

.message-card__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-size: 20px;
  flex-shrink: 0;
}

.message-card__main {
  min-width: 0;
  flex: 1;
}

.message-card__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.message-card__title {
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.message-card__date {
  display: inline-block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-card__summary {
  display: grid;
  gap: 6px;
  margin-top: 12px;
}

.message-card__summary-item {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 20px;
}

.message-card__summary-label {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.message-card__summary-value {
  color: var(--el-text-color-regular);
  word-break: break-all;
}

.message-card__body {
  margin-top: 12px;
  font-size: 13px;
  line-height: 20px;
  color: var(--el-text-color-regular);
}

.message-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.message-footer {
  margin-top: 12px;
  text-align: right;
}

.entry-notify-card {
  padding: 4px 4px 8px;
}

.entry-notify-card__hero {
  display: flex;
  align-items: center;
  gap: 14px;
}

.entry-notify-card__badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  color: var(--el-color-danger);
  background: linear-gradient(
    180deg,
    var(--el-color-danger-light-9) 0%,
    var(--el-color-danger-light-8) 100%
  );
  font-size: 24px;
  flex-shrink: 0;
}

.entry-notify-card__hero-copy {
  min-width: 0;
  flex: 1;
}

.entry-notify-card__eyebrow {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-color-danger);
  letter-spacing: 0.04em;
}

.entry-notify-card__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--el-text-color-primary);
}

.entry-notify-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.entry-notify-card__date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.entry-notify-card__description {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

.entry-notify-card__summary {
  display: grid;
  gap: 8px;
  margin-top: 16px;
  padding: 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 16px;
  background: linear-gradient(180deg, var(--el-fill-color-extra-light) 0%, var(--el-bg-color) 100%);
}

.entry-notify-card__summary-item {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 20px;
}

.entry-notify-card__summary-label {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.entry-notify-card__summary-value {
  color: var(--el-text-color-regular);
  word-break: break-all;
}

.entry-notify-card__body {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 13px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}

.entry-notify-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 18px;
}

.message-status {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
