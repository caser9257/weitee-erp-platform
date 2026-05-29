<template>
  <el-form-item class="mb-20px">
    <template #label>
      <el-text size="large" tag="b">{{ sectionTitle }}</el-text>
    </template>
    <div class="notification-settings">
      <div class="notification-settings__toolbar">
        <div class="notification-settings__toggle">
          <el-switch v-model="policy.enable" />
          <div class="notification-settings__toggle-text">
            <div class="notification-settings__label">启用通知</div>
            <el-text type="info">站内信支持在流程内直接编辑文案</el-text>
          </div>
        </div>
        <div class="notification-settings__mode">
          <span class="notification-settings__label">发布校验</span>
          <el-select v-model="policy.publishCheckMode" class="notification-settings__mode-select">
            <el-option label="严格校验" value="STRICT" />
            <el-option label="仅提醒" value="WARN" />
          </el-select>
        </div>
      </div>

      <div v-if="policy.enable" class="notification-settings__scene-list">
        <el-card
          v-for="scene in policy.scenes"
          :key="scene.sceneCode"
          shadow="never"
          class="notification-scene"
        >
          <template #header>
            <div class="notification-scene__header">
              <div class="notification-scene__title-wrap">
                <span class="notification-scene__title">{{ getSceneDefinition(scene.sceneCode).label }}</span>
                <el-tag size="small" effect="plain">
                  接收对象：{{ getReceiverLabel(scene.receiverType) }}
                </el-tag>
                <el-tag
                  size="small"
                  effect="plain"
                  :type="getSceneStatus(scene).type"
                >
                  {{ getSceneStatus(scene).text }}
                </el-tag>
              </div>
              <el-switch v-model="scene.enabled" />
            </div>
          </template>

          <div v-if="scene.enabled" class="notification-scene__body">
            <div class="notification-scene__config">
              <div class="notification-block">
                <div class="notification-block__header">
                  <span class="notification-block__title">站内信</span>
                  <el-switch v-model="scene.internalMessage.enabled" />
                </div>
                <div v-if="scene.internalMessage.enabled" class="notification-block__body">
                  <div class="notification-field">
                    <div class="notification-field__label">文案来源</div>
                    <el-radio-group
                      v-model="scene.internalMessage.sourceType"
                      @change="handleInternalSourceChange(scene)"
                    >
                      <el-radio value="DEFAULT">系统默认文案</el-radio>
                      <el-radio value="INLINE">自定义文案</el-radio>
                    </el-radio-group>
                  </div>

                  <template v-if="scene.internalMessage.sourceType === 'INLINE'">
                    <div class="notification-field">
                      <div class="notification-field__label">通知标题</div>
                      <el-input
                        v-model="scene.internalMessage.title"
                        placeholder="请输入通知标题"
                      />
                      <div class="notification-field__variables">
                        <el-button
                          v-for="variable in getSceneDefinition(scene.sceneCode).variables"
                          :key="`title-${scene.sceneCode}-${variable.key}`"
                          link
                          type="primary"
                          @click="insertTitleVariable(scene, variable.key)"
                        >
                          {{ variable.label }}
                        </el-button>
                      </div>
                    </div>

                    <div class="notification-field">
                      <div class="notification-field__label">通知内容</div>
                      <el-input
                        v-model="scene.internalMessage.content"
                        type="textarea"
                        :rows="5"
                        placeholder="请输入通知内容"
                      />
                      <div class="notification-field__variables">
                        <el-button
                          v-for="variable in getSceneDefinition(scene.sceneCode).variables"
                          :key="`content-${scene.sceneCode}-${variable.key}`"
                          link
                          type="primary"
                          @click="insertContentVariable(scene, variable.key)"
                        >
                          {{ variable.label }}
                        </el-button>
                      </div>
                      <el-button link type="primary" @click="restoreDefaultInternal(scene)">
                        恢复默认
                      </el-button>
                    </div>
                  </template>
                </div>
              </div>

              <div class="notification-block">
                <div class="notification-block__header">
                  <span class="notification-block__title">钉钉通知</span>
                  <el-switch v-model="scene.dingTalk.enabled" />
                </div>
                <div v-if="scene.dingTalk.enabled" class="notification-block__body">
                  <div class="notification-field">
                    <div class="notification-field__label">模板来源</div>
                    <el-radio-group v-model="scene.dingTalk.sourceType">
                      <el-radio value="RESERVED">预留配置</el-radio>
                      <el-radio value="TEMPLATE">模板编码</el-radio>
                    </el-radio-group>
                  </div>
                  <div
                    v-if="scene.dingTalk.sourceType === 'TEMPLATE'"
                    class="notification-field"
                  >
                    <div class="notification-field__label">模板编码</div>
                    <el-input
                      v-model="scene.dingTalk.templateCode"
                      placeholder="请输入钉钉模板编码"
                    />
                  </div>
                  <el-text type="info">
                    钉钉通道当前仅预留配置位，实际发送能力由系统统一配置
                  </el-text>
                </div>
              </div>
            </div>

            <div class="notification-preview">
              <div class="notification-preview__title">预览效果</div>
              <div class="notification-preview__meta">
                <span>文案来源：{{ getPreviewSourceLabel(scene) }}</span>
                <span>渠道：{{ getPreviewChannelLabel(scene) }}</span>
              </div>
              <div class="notification-preview__card">
                <div class="notification-preview__subject">
                  {{ getPreviewTitle(scene) || '未设置标题' }}
                </div>
                <pre class="notification-preview__content">{{ getPreviewContent(scene) }}</pre>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </el-form-item>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    title?: string
  }>(),
  {
    title: '通知设置'
  }
)

type SceneCode = 'TASK_ASSIGNED' | 'TASK_TIMEOUT' | 'PROCESS_APPROVE' | 'PROCESS_REJECT'
type ReceiverType = 'ASSIGNEE' | 'START_USER'
type InternalSourceType = 'DEFAULT' | 'INLINE'
type DingTalkSourceType = 'RESERVED' | 'TEMPLATE'

type InternalMessageSetting = {
  enabled: boolean
  sourceType: InternalSourceType
  title: string
  content: string
}

type DingTalkSetting = {
  enabled: boolean
  sourceType: DingTalkSourceType
  templateCode: string
}

type ScenePolicy = {
  sceneCode: SceneCode
  enabled: boolean
  receiverType: ReceiverType
  internalMessage: InternalMessageSetting
  dingTalk: DingTalkSetting
}

type NotificationPolicySetting = {
  enable: boolean
  publishCheckMode: 'STRICT' | 'WARN'
  scenes: ScenePolicy[]
}

type SceneVariable = {
  key: string
  label: string
}

type SceneDefinition = {
  code: SceneCode
  label: string
  receiverType: ReceiverType
  defaultTitle: string
  defaultContent: string
  sampleParams: Record<string, string>
  variables: SceneVariable[]
}

const modelData = defineModel<any>()
const sectionTitle = computed(() => props.title)

const SCENE_DEFINITIONS: SceneDefinition[] = [
  {
    code: 'TASK_ASSIGNED',
    label: '任务到达提醒',
    receiverType: 'ASSIGNEE',
    defaultTitle: '待审批任务提醒',
    defaultContent:
      '您收到一条新的审批任务\n流程：{processInstanceName}\n任务：{taskName}\n申请人：{startUserNickname}\n请在系统待办中查看详情',
    sampleParams: {
      processInstanceName: '采购入库审批',
      taskName: '仓库主管审批',
      startUserNickname: '张三',
      detailUrl: 'https://example.com/bpm/task/1001'
    },
    variables: [
      { key: 'processInstanceName', label: '流程名称' },
      { key: 'taskName', label: '任务名称' },
      { key: 'startUserNickname', label: '申请人' },
      { key: 'detailUrl', label: '详情链接' }
    ]
  },
  {
    code: 'TASK_TIMEOUT',
    label: '任务超时提醒',
    receiverType: 'ASSIGNEE',
    defaultTitle: '审批任务超时提醒',
    defaultContent:
      '您有一条审批任务已超时\n流程：{processInstanceName}\n任务：{taskName}\n请在系统待办中查看详情',
    sampleParams: {
      processInstanceName: '采购入库审批',
      taskName: '仓库主管审批',
      detailUrl: 'https://example.com/bpm/task/1001'
    },
    variables: [
      { key: 'processInstanceName', label: '流程名称' },
      { key: 'taskName', label: '任务名称' },
      { key: 'detailUrl', label: '详情链接' }
    ]
  },
  {
    code: 'PROCESS_APPROVE',
    label: '流程通过通知',
    receiverType: 'START_USER',
    defaultTitle: '流程审批结果通知',
    defaultContent: '您的流程已审批通过\n流程：{processInstanceName}\n请在系统中查看详情',
    sampleParams: {
      processInstanceName: '采购入库审批',
      detailUrl: 'https://example.com/bpm/instance/1001'
    },
    variables: [
      { key: 'processInstanceName', label: '流程名称' },
      { key: 'detailUrl', label: '详情链接' }
    ]
  },
  {
    code: 'PROCESS_REJECT',
    label: '流程驳回通知',
    receiverType: 'START_USER',
    defaultTitle: '流程审批结果通知',
    defaultContent:
      '您的流程已被驳回\n流程：{processInstanceName}\n驳回原因：{reason}\n请在系统中查看详情',
    sampleParams: {
      processInstanceName: '采购入库审批',
      reason: '单据金额超限，请补充说明',
      detailUrl: 'https://example.com/bpm/instance/1001'
    },
    variables: [
      { key: 'processInstanceName', label: '流程名称' },
      { key: 'reason', label: '驳回原因' },
      { key: 'detailUrl', label: '详情链接' }
    ]
  }
]

const createDefaultInternalMessage = (definition: SceneDefinition): InternalMessageSetting => ({
  enabled: true,
  sourceType: 'DEFAULT',
  title: definition.defaultTitle,
  content: definition.defaultContent
})

const createDefaultDingTalk = (): DingTalkSetting => ({
  enabled: false,
  sourceType: 'RESERVED',
  templateCode: ''
})

const createDefaultScene = (definition: SceneDefinition): ScenePolicy => ({
  sceneCode: definition.code,
  enabled: false,
  receiverType: definition.receiverType,
  internalMessage: createDefaultInternalMessage(definition),
  dingTalk: createDefaultDingTalk()
})

const createDefaultPolicySetting = (): NotificationPolicySetting => ({
  enable: false,
  publishCheckMode: 'STRICT',
  scenes: SCENE_DEFINITIONS.map((definition) => createDefaultScene(definition))
})

const normalizeScene = (scene: Partial<ScenePolicy> | undefined, definition: SceneDefinition): ScenePolicy => ({
  sceneCode: definition.code,
  enabled: scene?.enabled ?? false,
  receiverType: definition.receiverType,
  internalMessage: {
    enabled: scene?.internalMessage?.enabled ?? true,
    sourceType: scene?.internalMessage?.sourceType ?? 'DEFAULT',
    title: scene?.internalMessage?.title ?? definition.defaultTitle,
    content: scene?.internalMessage?.content ?? definition.defaultContent
  },
  dingTalk: {
    enabled: scene?.dingTalk?.enabled ?? false,
    sourceType: scene?.dingTalk?.sourceType ?? 'RESERVED',
    templateCode: scene?.dingTalk?.templateCode ?? ''
  }
})

const normalizePolicySetting = (
  setting: Partial<NotificationPolicySetting> | undefined
): NotificationPolicySetting => {
  const sceneMap = new Map((setting?.scenes || []).map((scene) => [scene.sceneCode, scene]))
  return {
    enable: setting?.enable ?? false,
    publishCheckMode: setting?.publishCheckMode ?? 'STRICT',
    scenes: SCENE_DEFINITIONS.map((definition) =>
      normalizeScene(sceneMap.get(definition.code) as Partial<ScenePolicy> | undefined, definition)
    )
  }
}

const syncNotificationPolicySetting = () => {
  if (!modelData.value) {
    return
  }
  const normalized = normalizePolicySetting(modelData.value.notificationPolicySetting)
  if (JSON.stringify(modelData.value.notificationPolicySetting) !== JSON.stringify(normalized)) {
    modelData.value.notificationPolicySetting = normalized
  }
}

watch(
  () => modelData.value?.notificationPolicySetting,
  () => syncNotificationPolicySetting(),
  { immediate: true, deep: true }
)

const policy = computed<NotificationPolicySetting>(() => {
  syncNotificationPolicySetting()
  return modelData.value.notificationPolicySetting
})

const getSceneDefinition = (sceneCode: SceneCode) =>
  SCENE_DEFINITIONS.find((item) => item.code === sceneCode) || SCENE_DEFINITIONS[0]

const getReceiverLabel = (receiverType: ReceiverType) =>
  receiverType === 'ASSIGNEE' ? '审批人' : '发起人'

const getSceneStatus = (scene: ScenePolicy) => {
  if (!scene.enabled) {
    return { text: '未启用', type: 'info' as const }
  }
  if (scene.internalMessage.enabled && scene.internalMessage.sourceType === 'INLINE') {
    return { text: '已自定义', type: 'success' as const }
  }
  return { text: '系统默认', type: 'warning' as const }
}

const handleInternalSourceChange = (scene: ScenePolicy) => {
  if (scene.internalMessage.sourceType !== 'INLINE') {
    return
  }
  if (scene.internalMessage.title || scene.internalMessage.content) {
    return
  }
  const definition = getSceneDefinition(scene.sceneCode)
  scene.internalMessage.title = definition.defaultTitle
  scene.internalMessage.content = definition.defaultContent
}

const appendVariable = (source: string, variableKey: string) => {
  const token = `{${variableKey}}`
  if (!source) {
    return token
  }
  return `${source}${token}`
}

const insertTitleVariable = (scene: ScenePolicy, variableKey: string) => {
  scene.internalMessage.title = appendVariable(scene.internalMessage.title, variableKey)
}

const insertContentVariable = (scene: ScenePolicy, variableKey: string) => {
  scene.internalMessage.content = appendVariable(scene.internalMessage.content, variableKey)
}

const restoreDefaultInternal = (scene: ScenePolicy) => {
  const definition = getSceneDefinition(scene.sceneCode)
  scene.internalMessage.sourceType = 'INLINE'
  scene.internalMessage.title = definition.defaultTitle
  scene.internalMessage.content = definition.defaultContent
}

const PREVIEW_PARAM_OVERRIDES: Partial<Record<string, string>> = {
  detailUrl: '请在系统中查看详情'
}

const renderTemplate = (template: string, params: Record<string, string>) =>
  (template || '').replace(
    /\{([a-zA-Z][a-zA-Z0-9]*)\}/g,
    (_, key) => PREVIEW_PARAM_OVERRIDES[key] || params[key] || `{${key}}`
  )

const getPreviewSourceLabel = (scene: ScenePolicy) => {
  if (!scene.internalMessage.enabled) {
    return '未启用站内信'
  }
  return scene.internalMessage.sourceType === 'INLINE' ? '自定义文案' : '系统默认文案'
}

const getPreviewChannelLabel = (scene: ScenePolicy) => {
  const channels: string[] = []
  if (scene.internalMessage.enabled) {
    channels.push('站内信')
  }
  if (scene.dingTalk.enabled) {
    channels.push('钉钉通知')
  }
  return channels.length > 0 ? channels.join(' / ') : '未启用'
}

const getPreviewTitle = (scene: ScenePolicy) => {
  const definition = getSceneDefinition(scene.sceneCode)
  const title =
    scene.internalMessage.sourceType === 'INLINE'
      ? scene.internalMessage.title
      : definition.defaultTitle
  return renderTemplate(title, definition.sampleParams)
}

const getPreviewContent = (scene: ScenePolicy) => {
  const definition = getSceneDefinition(scene.sceneCode)
  const content =
    scene.internalMessage.sourceType === 'INLINE'
      ? scene.internalMessage.content
      : definition.defaultContent
  return renderTemplate(content, definition.sampleParams)
}
</script>

<style scoped lang="scss">
.notification-settings {
  width: 100%;
}

.notification-settings__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-extra-light);
}

.notification-settings__toggle {
  display: flex;
  gap: 12px;
  align-items: center;
}

.notification-settings__toggle-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-settings__label {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.notification-settings__mode {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
}

.notification-settings__mode-select {
  width: 140px;
}

.notification-settings__scene-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-scene__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.notification-scene__title-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.notification-scene__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.notification-scene__body {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 1fr);
  gap: 16px;
}

.notification-scene__config {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-block {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
}

.notification-block__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.notification-block__title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.notification-block__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-field__label {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.notification-field__variables {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.notification-preview {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
  background: #fafbfd;
}

.notification-preview__title {
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.notification-preview__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}

.notification-preview__card {
  border-radius: 8px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  padding: 16px;
}

.notification-preview__subject {
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.notification-preview__content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  color: var(--el-text-color-regular);
}

@media (max-width: 1279px) {
  .notification-settings__toolbar {
    flex-direction: column;
  }

  .notification-settings__mode {
    min-width: 0;
  }

  .notification-scene__body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .notification-settings__toggle {
    align-items: flex-start;
  }

  .notification-settings__mode {
    flex-direction: column;
    align-items: flex-start;
  }

  .notification-settings__mode-select {
    width: 100%;
  }
}
</style>
