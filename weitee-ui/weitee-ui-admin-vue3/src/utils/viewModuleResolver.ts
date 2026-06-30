type ViewModule = Record<string, unknown>

export const viewModules = import.meta.glob('../views/**/*.{vue,tsx}')

const fallbackViewModuleKey = '../views/Error/404.vue'

const normalizeViewLookupPath = (path: string) => {
  const normalizedPath = path.replace(/\\/g, '/').replace(/^\/+/, '')
  const [basePath] = normalizedPath.split('?')
  return basePath.replace(/\.(vue|tsx)$/i, '').replace(/\/+$/, '')
}

const buildViewModuleCandidateKeys = (componentPath: string) => {
  const normalizedPath = normalizeViewLookupPath(componentPath)
  if (!normalizedPath) {
    return []
  }

  return [
    `../views/${normalizedPath}.vue`,
    `../views/${normalizedPath}/index.vue`,
    `../views/${normalizedPath}.tsx`,
    `../views/${normalizedPath}/index.tsx`
  ]
}

export const resolveViewModuleKey = (
  componentPath: string,
  modules: ViewModule = viewModules
) => {
  const candidateKeys = buildViewModuleCandidateKeys(componentPath)
  return candidateKeys.find((item) => Object.prototype.hasOwnProperty.call(modules, item))
}

export const hasViewModule = (componentPath: string, modules: ViewModule = viewModules) => {
  return Boolean(resolveViewModuleKey(componentPath, modules))
}

export const registerComponent = (componentPath: string) => {
  const moduleKey = resolveViewModuleKey(componentPath)
  if (!moduleKey) {
    return undefined
  }

  // @ts-ignore
  return defineAsyncComponent(viewModules[moduleKey])
}

export const resolveViewModule = (
  componentPath: string,
  routePath: string,
  modules: ViewModule = viewModules
) => {
  const moduleKey = resolveViewModuleKey(componentPath, modules)

  if (!moduleKey) {
    const fallbackModule = modules[fallbackViewModuleKey]
    console.error(
      `[router] Route component not found for path "${routePath}" using component "${componentPath}", fallback to 404`
    )

    if (fallbackModule) {
      return fallbackModule
    }

    throw new Error(
      `Route component not found for path "${routePath}" using component "${componentPath}"`
    )
  }

  return modules[moduleKey]
}
