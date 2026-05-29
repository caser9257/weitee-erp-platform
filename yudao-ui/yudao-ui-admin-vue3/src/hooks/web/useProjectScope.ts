import { CACHE_KEY, useCache } from './useCache'

export interface ProjectScope {
  id?: number
  no?: string
  name?: string
}

const normalizeProjectId = (value: unknown): number | undefined => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const id = Number(value)
  return Number.isNaN(id) ? undefined : id
}

const normalizeProjectText = (value: unknown): string | undefined => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  return String(value)
}

const normalizeProjectScope = (value: unknown): ProjectScope => {
  if (!value || typeof value !== 'object') {
    return {}
  }
  const scope = value as Record<string, unknown>
  return {
    id: normalizeProjectId(scope.id),
    no: normalizeProjectText(scope.no),
    name: normalizeProjectText(scope.name)
  }
}

export const useProjectScope = () => {
  const route = useRoute()
  const { wsCache } = useCache()

  const cachedScope = ref<ProjectScope>(
    normalizeProjectScope(wsCache.get(CACHE_KEY.CurrentProject) as unknown)
  )

  const routeScope = computed<ProjectScope>(() => {
    return {
      id: normalizeProjectId(route.params.projectId ?? route.query.projectId),
      no: normalizeProjectText(route.query.projectNo),
      name: normalizeProjectText(route.query.projectName)
    }
  })

  const currentProject = computed<ProjectScope>(() => {
    return {
      id: routeScope.value.id ?? cachedScope.value.id,
      no: routeScope.value.no ?? cachedScope.value.no,
      name: routeScope.value.name ?? cachedScope.value.name
    }
  })

  const hasProjectScope = computed(() => !!currentProject.value.id)
  const projectId = computed(() => currentProject.value.id)

  const setProjectScope = (project: ProjectScope) => {
    cachedScope.value = {
      id: project.id ?? cachedScope.value.id,
      no: project.no ?? cachedScope.value.no,
      name: project.name ?? cachedScope.value.name
    }
    wsCache.set(CACHE_KEY.CurrentProject, cachedScope.value)
  }

  const clearProjectScope = () => {
    cachedScope.value = {}
    wsCache.delete(CACHE_KEY.CurrentProject)
  }

  const withProjectScope = <T extends Recordable>(params: T = {} as T) => {
    if (!projectId.value) {
      return { ...params }
    }
    return {
      ...params,
      projectId: projectId.value
    }
  }

  return {
    currentProject,
    projectId,
    hasProjectScope,
    setProjectScope,
    clearProjectScope,
    withProjectScope
  }
}
