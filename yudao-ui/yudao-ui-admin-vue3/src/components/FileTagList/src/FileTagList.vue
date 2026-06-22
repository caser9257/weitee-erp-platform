<template>
  <div class="file-tag-list">
    <div class="file-tag-list__header">
      <div class="file-tag-list__title">文件标签</div>
    </div>
    <div class="file-tag-list__content">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="tag-item"
        :class="{ 'tag-item--active': activeTagId === tag.id }"
        @click="handleSelectTag(tag)"
      >
        <div class="tag-item__dot" :style="{ backgroundColor: tag.color }"></div>
        <span class="tag-item__name">{{ tag.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/config/axios'

defineOptions({ name: 'FileTagList' })

interface TagItem {
  id: number
  name: string
  color?: string
}

const emit = defineEmits(['select'])

const activeTagId = ref<number | null>(null)
const tagList = ref<TagItem[]>([])

// 加载标签列表
const loadTags = async () => {
  try {
    const data = await request.get({ url: '/infra/file-tag/list' })
    tagList.value = data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

// 选择标签
const handleSelectTag = (tag: TagItem) => {
  if (activeTagId.value === tag.id) {
    activeTagId.value = null
    emit('select', null)
  } else {
    activeTagId.value = tag.id
    emit('select', tag)
  }
}

// 刷新
const refresh = () => {
  loadTags()
}

onMounted(() => {
  loadTags()
})

defineExpose({ refresh })
</script>

<style scoped lang="scss">
.file-tag-list {
  width: 200px;
  border-right: 1px solid var(--erp-slate-200);
  background: white;
}

.file-tag-list__header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--erp-slate-100);
}

.file-tag-list__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--erp-slate-700);
}

.file-tag-list__content {
  padding: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--erp-slate-50);
  }

  &--active {
    background: var(--erp-primary-50);
    color: var(--erp-primary-600);
  }
}

.tag-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.tag-item__name {
  font-size: 13px;
}
</style>
