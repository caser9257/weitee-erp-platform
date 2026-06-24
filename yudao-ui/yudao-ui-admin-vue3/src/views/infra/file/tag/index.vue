<template>
  <ContentWrap title="文件标签管理">
    <template #header>
      <el-button type="primary" plain @click="openForm()" v-hasPermi="['infra:file-tag:create']">
        <Icon icon="ep:plus" class="mr-5px" /> 新增标签
      </el-button>
    </template>

    <div v-loading="loading" class="flex flex-wrap gap-12px p-12px">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="file-tag-card"
        :style="{ borderColor: tag.color }"
      >
        <div class="file-tag-card__header">
          <div class="flex items-center gap-8px">
            <div
              class="file-tag-card__dot"
              :style="{ backgroundColor: tag.color }"
            />
            <Icon :icon="tag.icon || 'ep:price-tag'" class="text-lg" :style="{ color: tag.color }" />
            <span class="file-tag-card__name">{{ tag.name }}</span>
          </div>
          <div class="file-tag-card__actions">
            <el-button link type="primary" @click="openForm(tag.id)" v-hasPermi="['infra:file-tag:update']">
              <Icon icon="ep:edit" />
            </el-button>
            <el-button link type="danger" @click="handleDelete(tag.id)" v-hasPermi="['infra:file-tag:delete']">
              <Icon icon="ep:delete" />
            </el-button>
          </div>
        </div>
        <div class="file-tag-card__footer">
          <span class="text-xs text-[var(--erp-slate-400)]">
            创建时间：{{ dateFormatter(null, null, tag.createTime) }}
          </span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && tagList.length === 0" class="w-full flex items-center justify-center py-40px">
        <el-empty description="暂无标签" />
      </div>
    </div>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <FileTagForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as FileTagApi from '@/api/infra/fileTag'
import type { FileTagVO } from '@/api/infra/fileTag'
import FileTagForm from './FileTagForm.vue'

defineOptions({ name: 'InfraFileTag' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(false)
const tagList = ref<FileTagVO[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileTagApi.getFileTagList()
    tagList.value = data
  } finally {
    loading.value = false
  }
}

/** 新增/编辑操作 */
const formRef = ref()
const openForm = (id?: number) => {
  formRef.value.open(id)
}

/** 删除操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileTagApi.deleteFileTag(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.file-tag-card {
  width: 280px;
  padding: 16px;
  border: 1px solid var(--erp-slate-200);
  border-left-width: 4px;
  border-radius: 8px;
  background: white;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: var(--erp-shadow-sm);
    border-color: var(--erp-primary-300);
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  &__name {
    font-size: 16px;
    font-weight: 600;
    color: var(--erp-slate-800);
  }

  &__actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &:hover &__actions {
    opacity: 1;
  }

  &__footer {
    padding-top: 8px;
    border-top: 1px solid var(--erp-slate-100);
  }
}
</style>
