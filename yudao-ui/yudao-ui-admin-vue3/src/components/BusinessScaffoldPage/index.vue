<template>
  <div class="business-scaffold-page">
    <ContentWrap>
      <div class="hero">
        <div class="hero__main">
          <div class="hero__title">{{ props.title }}</div>
          <div class="hero__desc">{{ props.description }}</div>
        </div>
        <div class="hero__tags">
          <el-tag type="primary" effect="dark">骨架页</el-tag>
          <el-tag type="info">项目上下文</el-tag>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap>
      <div class="project-foundation">
        <div class="project-foundation__main">
          <div class="project-foundation__title">{{ text.projectFoundationTitle }}</div>
          <div class="project-foundation__desc">{{ text.projectFoundationDesc }}</div>
        </div>
        <div class="project-foundation__tags">
          <el-tag type="success" effect="dark">Project Driven</el-tag>
          <el-tag :type="hasProjectScope ? 'success' : 'warning'">
            {{ hasProjectScope ? text.projectScopeDetected : text.projectScopeMissing }}
          </el-tag>
        </div>
      </div>

      <el-descriptions :column="1" border>
        <el-descriptions-item :label="text.currentProjectId">
          <span v-if="currentProject.id !== undefined">{{ currentProject.id }}</span>
          <span v-else class="placeholder">{{ text.notProvided }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="text.currentProjectNo">
          <span v-if="currentProject.no">{{ currentProject.no }}</span>
          <span v-else class="placeholder">{{ text.notProvided }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="text.currentProjectName">
          <span v-if="currentProject.name">{{ currentProject.name }}</span>
          <span v-else class="placeholder">{{ text.notProvided }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="text.scopeSource">
          <code>路由参数 / 查询参数 / 当前项目缓存</code>
        </el-descriptions-item>
      </el-descriptions>
    </ContentWrap>

    <ContentWrap>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="text.viewPath">
          <code>{{ props.viewPath }}</code>
        </el-descriptions-item>
        <el-descriptions-item :label="text.apiPath">
          <div v-if="props.apiPaths.length" class="path-list">
            <div v-for="path in props.apiPaths" :key="path">
              <code>{{ path }}</code>
            </div>
          </div>
          <span v-else>{{ text.reuseExistingApi }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="text.reuseModule">
          <div v-if="props.reusePaths.length" class="path-list">
            <div v-for="path in props.reusePaths" :key="path">
              <code>{{ path }}</code>
            </div>
          </div>
          <span v-else>{{ text.independentScaffold }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </ContentWrap>

    <ContentWrap>
      <el-empty :description="props.note" />
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { useProjectScope } from '@/hooks/web/useProjectScope'

interface Props {
  title: string
  description: string
  viewPath: string
  apiPaths?: string[]
  reusePaths?: string[]
  note?: string
}

const text = {
  projectFoundationTitle: '\u9879\u76ee\u4e1a\u52a1\u5e95\u5ea7',
  projectFoundationDesc:
    '\u5f53\u524d\u9875\u9762\u4f1a\u4f18\u5148\u4ece\u8def\u7531\u53c2\u6570\u3001\u67e5\u8be2\u53c2\u6570\u548c\u672c\u5730\u7f13\u5b58\u8bc6\u522b\u9879\u76ee\u4e0a\u4e0b\u6587\uff0c\u540e\u7eed\u5217\u8868\u3001\u8868\u5355\u4e0e\u6d41\u7a0b\u52a8\u4f5c\u9ed8\u8ba4\u56f4\u7ed5\u5f53\u524d\u9879\u76ee\u5c55\u5f00\u3002',
  projectScopeDetected: '\u5df2\u8bc6\u522b\u9879\u76ee\u4e0a\u4e0b\u6587',
  projectScopeMissing: '\u672a\u8bc6\u522b\u9879\u76ee\u4e0a\u4e0b\u6587',
  currentProjectId: '\u5f53\u524d\u9879\u76ee ID',
  currentProjectNo: '\u5f53\u524d\u9879\u76ee\u7f16\u53f7',
  currentProjectName: '\u5f53\u524d\u9879\u76ee\u540d\u79f0',
  scopeSource: '\u4f5c\u7528\u57df\u6765\u6e90',
  viewPath: '\u9875\u9762\u8def\u5f84',
  apiPath: '\u63a5\u53e3\u8def\u5f84',
  reuseModule: '\u590d\u7528\u6a21\u5757',
  reuseExistingApi:
    '\u6cbf\u7528\u73b0\u6709\u6a21\u5757\uff0c\u4e0d\u989d\u5916\u65b0\u589e\u63a5\u53e3\u9aa8\u67b6',
  independentScaffold: '\u5f53\u524d\u9875\u9762\u4f5c\u4e3a\u72ec\u7acb\u4e1a\u52a1\u9aa8\u67b6',
  notProvided: '\u672a\u4f20\u5165'
}

const props = withDefaults(defineProps<Props>(), {
  apiPaths: () => [],
  reusePaths: () => [],
  note: '\u76ee\u5f55\u9aa8\u67b6\u5df2\u521b\u5efa\uff0c\u540e\u7eed\u518d\u8865\u83dc\u5355\u8def\u7531\u3001\u4e1a\u52a1\u7ec4\u4ef6\u548c\u771f\u5b9e\u63a5\u53e3\u3002'
})

const { currentProject, hasProjectScope } = useProjectScope()
</script>

<style scoped lang="scss">
.business-scaffold-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero__main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hero__title {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.hero__desc {
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

.hero__tags,
.project-foundation__tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.project-foundation {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.project-foundation__main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-foundation__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.project-foundation__desc {
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

.path-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.placeholder {
  color: var(--el-text-color-placeholder);
}

code {
  padding: 2px 6px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
}

@media (max-width: 768px) {
  .hero,
  .project-foundation {
    flex-direction: column;
  }
}
</style>
