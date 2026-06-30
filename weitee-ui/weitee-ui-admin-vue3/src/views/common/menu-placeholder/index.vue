<template>
  <component v-if="resolvedComponent" :is="resolvedComponent" />
  <div v-else class="menu-placeholder-page">
    <ContentWrap>
      <div class="menu-placeholder-page__hero">
        <div class="menu-placeholder-page__main">
          <div class="menu-placeholder-page__title">{{ pageTitle }}</div>
          <div class="menu-placeholder-page__desc">
            该菜单已按最终信息架构预留，当前先由统一占位页承接，后续再接入真实业务页面。
          </div>
        </div>
        <el-tag type="warning" effect="dark">待接入业务页面</el-tag>
      </div>
    </ContentWrap>

    <ContentWrap>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前菜单">
          {{ pageTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="当前路由">
          <code>{{ route.path }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          已完成菜单挂载，等待真实业务功能接入
        </el-descriptions-item>
      </el-descriptions>
    </ContentWrap>

    <ContentWrap>
      <el-empty description="该菜单已预留，请按最终菜单规划继续完善业务实现。" />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import { useRoute } from 'vue-router'

defineOptions({ name: 'CommonMenuPlaceholder' })

const route = useRoute()

const followUpPage = defineAsyncComponent(
  () => import('@/views/pmo/project/follow-up/index.vue')
)
const projectWarningPage = defineAsyncComponent(
  () => import('@/views/pmo/project/warning/index.vue')
)
const financeExpensePage = defineAsyncComponent(
  () => import('@/views/erp/finance/expense/index.vue')
)

const resolvedComponent = computed(() => {
  const path = route.path.replace(/\/+/g, '/')
  if (path === '/pmo/project/follow-up') {
    return followUpPage
  }
  if (path === '/pmo/project/warning') {
    return projectWarningPage
  }
  if (path === '/project/warning') {
    return projectWarningPage
  }
  if (path === '/pmo/warning') {
    return projectWarningPage
  }
  if (path === '/finance/expense' || path === '/erp/finance/expense') {
    return financeExpensePage
  }
  return null
})

const pageTitle = computed(() => {
  const metaTitle = route.meta?.title
  if (typeof metaTitle === 'string' && metaTitle.trim().length > 0) {
    return metaTitle
  }
  if (typeof route.name === 'string' && route.name.trim().length > 0) {
    return route.name
  }
  return '菜单占位页'
})
</script>

<style scoped lang="scss">
.menu-placeholder-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-placeholder-page__hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.menu-placeholder-page__main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-placeholder-page__title {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.menu-placeholder-page__desc {
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

code {
  padding: 2px 6px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
}

@media (max-width: 768px) {
  .menu-placeholder-page__hero {
    flex-direction: column;
  }
}
</style>
