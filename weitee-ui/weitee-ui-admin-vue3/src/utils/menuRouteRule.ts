export const FORMAL_ROOT_PATHS = [
  '/dashboard',
  '/project',
  '/master-data',
  '/sales',
  '/rd',
  '/pmo',
  '/scm',
  '/qms',
  '/process',
  '/mes',
  '/finance',
  '/hr',
  '/system',
  '/approval',
  '/bpm'
] as const

export const formalRootPaths = new Set<string>(FORMAL_ROOT_PATHS)

const normalizeRoutePath = (path?: string) => {
  if (!path) {
    return ''
  }

  const [basePath] = path.replace(/\\/g, '/').split('?')
  const normalizedPath = basePath.replace(/\/+/g, '/')
  if (normalizedPath.length > 1 && normalizedPath.endsWith('/')) {
    return normalizedPath.slice(0, -1)
  }

  return normalizedPath
}

export const getRouteRootPath = (path?: string) => {
  const normalizedPath = normalizeRoutePath(path)
  const [firstSegment] = normalizedPath.split('/').filter(Boolean)
  return firstSegment ? `/${firstSegment}` : ''
}

export const matchesFormalRootMenuPath = (path?: string) => {
  return formalRootPaths.has(getRouteRootPath(path))
}
