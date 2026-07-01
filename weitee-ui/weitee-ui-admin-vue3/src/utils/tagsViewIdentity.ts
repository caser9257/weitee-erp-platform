import type { RouteLocationNormalizedLoaded } from 'vue-router'

type TagsViewIdentityRoute = Pick<RouteLocationNormalizedLoaded, 'fullPath' | 'path'>

const SCM_SINGLETON_TAG_PATHS = new Set([
  '/scm/inbound',
  '/scm/purchase-order',
  '/scm/stock',
  '/scm/stock-analysis'
])

const TAG_PATH_ALIASES: Record<string, string> = {
  '/erp/purchase/in': '/scm/inbound',
  '/erp/purchase/order': '/scm/purchase-order'
}

export const normalizeTagsViewPath = (path?: string) => {
  if (!path) {
    return ''
  }
  return TAG_PATH_ALIASES[path] || path
}

export const getTagsViewIdentityKey = (view: TagsViewIdentityRoute) => {
  const normalizedPath = normalizeTagsViewPath(view.path)
  if (SCM_SINGLETON_TAG_PATHS.has(normalizedPath)) {
    return normalizedPath
  }
  return view.fullPath || normalizedPath
}
