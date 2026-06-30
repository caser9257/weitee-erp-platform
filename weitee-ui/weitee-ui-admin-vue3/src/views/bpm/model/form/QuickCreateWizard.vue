<template>
  <div class="quick-create">
    <el-card shadow="never" class="quick-create__card quick-create__card--scenes">
      <template #header>
        <div class="quick-create__card-header">
          <div>
            <div class="quick-create__card-title">选择场景</div>
            <div class="quick-create__card-desc">先选一个常用场景，再继续补充审批配置</div>
          </div>
          <el-button type="primary" link @click="openAdvancedSettings">高级设置</el-button>
        </div>
      </template>
      <div class="quick-create__scene-grid">
        <button
          v-for="scene in sceneOptions"
          :key="scene.code"
          type="button"
          class="quick-create__scene"
          :class="{ 'is-active': selectedSceneCode === scene.code }"
          @click="handleSceneChange(scene)"
        >
          <div class="quick-create__scene-title">{{ scene.label }}</div>
          <div class="quick-create__scene-desc">{{ scene.description }}</div>
        </button>
      </div>
    </el-card>

    <el-card shadow="never" class="quick-create__card">
      <template #header>
        <div class="quick-create__card-header">
          <div>
            <div class="quick-create__card-title">基本信息</div>
            <div class="quick-create__card-desc">只保留创建审批流程最常用的基础配置</div>
          </div>
        </div>
      </template>
      <BasicInfo
        ref="basicInfoRef"
        v-model="modelData"
        :categoryList="props.categoryList"
        :userList="props.userList"
        :deptList="props.deptList"
        mode="quick"
      />
    </el-card>

    <el-card shadow="never" class="quick-create__card">
      <template #header>
        <div class="quick-create__card-header">
          <div>
            <div class="quick-create__card-title">表单设置</div>
            <div class="quick-create__card-desc">选择审批单据使用的表单，默认走流程表单</div>
          </div>
        </div>
      </template>
      <FormDesign ref="formDesignRef" v-model="modelData" :formList="props.formList" />
    </el-card>

    <el-card shadow="never" class="quick-create__card">
      <template #header>
        <div class="quick-create__card-header">
          <div>
            <div class="quick-create__card-title">审批步骤</div>
            <div class="quick-create__card-desc">按业务顺序配置审批节点，默认使用简化设计器</div>
          </div>
        </div>
      </template>
      <div class="quick-create__designer">
        <ProcessDesign ref="processDesignRef" v-model="modelData" />
      </div>
    </el-card>

    <el-card shadow="never" class="quick-create__card">
      <el-form label-width="0">
        <NotificationSettings v-model="modelData" title="审批提醒" />
      </el-form>
    </el-card>

    <el-drawer
      v-model="advancedSettingsVisible"
      title="高级设置"
      size="960px"
      destroy-on-close
    >
      <ExtraSettings ref="extraSettingsRef" v-model="modelData" :showNotification="false" />
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import { BpmModelFormType, BpmModelType } from '@/utils/constants'
import BasicInfo from './BasicInfo.vue'
import FormDesign from './FormDesign.vue'
import ProcessDesign from './ProcessDesign.vue'
import ExtraSettings from './ExtraSettings.vue'
import NotificationSettings from './NotificationSettings.vue'
import type { CategoryVO } from '@/api/bpm/category'
import type { UserVO } from '@/api/system/user'
import type { DeptVO } from '@/api/system/dept'

type QuickScene = {
  code: string
  label: string
  description: string
  suggestedName: string
}

const props = defineProps<{
  categoryList: CategoryVO[]
  userList: UserVO[]
  deptList: DeptVO[]
  formList: any[]
}>()

const modelData = defineModel<any>()

const basicInfoRef = ref()
const formDesignRef = ref()
const processDesignRef = ref()
const extraSettingsRef = ref()

const advancedSettingsVisible = ref(false)
const selectedSceneCode = ref('BLANK')
const lastSuggestedName = ref('')

const sceneOptions: QuickScene[] = [
  {
    code: 'BLANK',
    label: '\u7a7a\u767d\u6d41\u7a0b',
    description: '\u4ece\u7a7a\u767d\u5ba1\u6279\u6d41\u7a0b\u5f00\u59cb\uff0c\u9002\u5408\u81ea\u5df1\u642d\u5efa\u5ba1\u6279\u94fe',
    suggestedName: '\u7a7a\u767d\u5ba1\u6279\u6d41\u7a0b'
  },
  {
    code: 'LEAVE',
    label: '\u8bf7\u5047\u5ba1\u6279',
    description: '\u9002\u5408\u8bf7\u5047\u3001\u8c03\u4f11\u3001\u5916\u51fa\u7b49\u5e38\u89c1\u4eba\u4e8b\u5ba1\u6279',
    suggestedName: '\u8bf7\u5047\u5ba1\u6279\u6d41\u7a0b'
  },
  {
    code: 'EXPENSE',
    label: '\u62a5\u9500\u5ba1\u6279',
    description: '\u9002\u5408\u5dee\u65c5\u8d39\u3001\u529e\u516c\u8d39\u548c\u65e5\u5e38\u8d39\u7528\u62a5\u9500',
    suggestedName: '\u62a5\u9500\u5ba1\u6279\u6d41\u7a0b'
  },
  {
    code: 'PURCHASE',
    label: '\u91c7\u8d2d\u7533\u8bf7',
    description: '\u9002\u5408\u7269\u6599\u3001\u529e\u516c\u7528\u54c1\u548c\u91c7\u8d2d\u7533\u8bf7\u5ba1\u6279',
    suggestedName: '\u91c7\u8d2d\u7533\u8bf7\u5ba1\u6279\u6d41\u7a0b'
  },
  {
    code: 'PAYMENT',
    label: '\u4ed8\u6b3e\u7533\u8bf7',
    description: '\u9002\u5408\u4ed8\u6b3e\u3001\u8bf7\u6b3e\u548c\u7ed3\u7b97\u5ba1\u6279\u573a\u666f',
    suggestedName: '\u4ed8\u6b3e\u7533\u8bf7\u5ba1\u6279\u6d41\u7a0b'
  },
  {
    code: 'CONTRACT',
    label: '\u5408\u540c\u5ba1\u6279',
    description: '\u9002\u5408\u5408\u540c\u7b7e\u8ba2\u3001\u7eed\u7b7e\u548c\u53d8\u66f4\u5ba1\u6279',
    suggestedName: '\u5408\u540c\u5ba1\u6279\u6d41\u7a0b'
  }
]

const buildQuickKey = () => `quick_${Date.now()}`

const ensureQuickDefaults = () => {
  if (!modelData.value) {
    return
  }
  modelData.value.type = BpmModelType.SIMPLE
  modelData.value.visible = true
  modelData.value.formType = modelData.value.formType || BpmModelFormType.NORMAL
  if (!modelData.value.key) {
    modelData.value.key = buildQuickKey()
  }
}

const applySceneDefaults = (scene: QuickScene) => {
  ensureQuickDefaults()
  selectedSceneCode.value = scene.code
  if (!modelData.value.name || modelData.value.name === lastSuggestedName.value) {
    modelData.value.name = scene.suggestedName
  }
  lastSuggestedName.value = scene.suggestedName
}

const handleSceneChange = (scene: QuickScene) => {
  applySceneDefaults(scene)
}

const openAdvancedSettings = async () => {
  advancedSettingsVisible.value = true
  await nextTick()
  extraSettingsRef.value?.initData?.()
}

const initData = () => {
  ensureQuickDefaults()
  const defaultScene =
    sceneOptions.find((scene) => scene.code === selectedSceneCode.value) || sceneOptions[0]
  if (defaultScene) {
    applySceneDefaults(defaultScene)
  }
}

const validate = async () => {
  ensureQuickDefaults()
  await basicInfoRef.value?.validate()
  await formDesignRef.value?.validate()
  await processDesignRef.value?.validate()
}

watch(
  () => modelData.value?.name,
  () => {
    ensureQuickDefaults()
  },
  {
    immediate: true
  }
)

defineExpose({
  initData,
  validate
})
</script>

<style lang="scss" scoped>
.quick-create {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-create__card {
  border-radius: 12px;
}

.quick-create__card--scenes {
  background: linear-gradient(180deg, #f7faff 0%, #ffffff 100%);
}

.quick-create__card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.quick-create__card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
}

.quick-create__card-desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 20px;
  color: #667085;
}

.quick-create__scene-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.quick-create__scene {
  border: 1px solid #dbe4f0;
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.quick-create__scene:hover {
  border-color: #7aa2ff;
  box-shadow: 0 8px 24px rgba(52, 115, 255, 0.08);
  transform: translateY(-1px);
}

.quick-create__scene.is-active {
  border-color: #3473ff;
  box-shadow: 0 0 0 3px rgba(52, 115, 255, 0.12);
}

.quick-create__scene-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
}

.quick-create__scene-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 20px;
  color: #667085;
}

.quick-create__designer {
  overflow-x: auto;
}

@media (max-width: 1439px) {
  .quick-create__scene-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .quick-create__scene-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .quick-create__card-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
