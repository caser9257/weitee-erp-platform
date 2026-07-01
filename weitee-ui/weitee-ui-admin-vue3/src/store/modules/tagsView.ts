import router from '@/router'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { getRawRoute } from '@/utils/routerHelper'
import { defineStore } from 'pinia'
import { store } from '../index'
import { findIndex } from '@/utils'
import { useUserStoreWithOut } from './user'
import { getTagsViewIdentityKey } from '@/utils/tagsViewIdentity'

export interface TagsViewState {
  visitedViews: RouteLocationNormalizedLoaded[]
  cachedViews: Set<string>
  selectedTag?: RouteLocationNormalizedLoaded
}

export const useTagsViewStore = defineStore('tagsView', {
  state: (): TagsViewState => ({
    visitedViews: [],
    cachedViews: new Set(),
    selectedTag: undefined
  }),
  getters: {
    getVisitedViews(): RouteLocationNormalizedLoaded[] {
      return this.visitedViews
    },
    getCachedViews(): string[] {
      return Array.from(this.cachedViews)
    },
    getSelectedTag(): RouteLocationNormalizedLoaded | undefined {
      return this.selectedTag
    }
  },
  actions: {
    // 新增缓存和tag
    addView(view: RouteLocationNormalizedLoaded): void {
      this.addVisitedView(view)
      this.addCachedView()
    },
    // 新增tag
    addVisitedView(view: RouteLocationNormalizedLoaded) {
      if (view.meta?.noTagsView) return
      const visitedView = Object.assign({}, view, { title: view.meta?.title || 'no-name' })
      const viewIdentityKey = getTagsViewIdentityKey(visitedView)
      const existingIndex = this.visitedViews.findIndex(
        (v) => getTagsViewIdentityKey(v) === viewIdentityKey
      )
      if (existingIndex > -1) {
        const existingView = this.visitedViews[existingIndex]
        this.visitedViews[existingIndex] = Object.assign({}, existingView, visitedView, {
          meta: Object.assign({}, existingView.meta, visitedView.meta)
        })
        return
      }

      if (visitedView.meta) {
        const titleSuffixList: string[] = []
        this.visitedViews.forEach((v) => {
          if (v.path === visitedView.path && v.meta?.title === visitedView.meta?.title) {
            titleSuffixList.push(v.meta?.titleSuffix || '1')
          }
        })
        if (titleSuffixList.length) {
          let titleSuffix = 1
          while (titleSuffixList.includes(`${titleSuffix}`)) {
            titleSuffix += 1
          }
          visitedView.meta.titleSuffix = titleSuffix === 1 ? undefined : `${titleSuffix}`
        }
      }

      this.visitedViews.push(visitedView)
    },
    // 新增缓存
    addCachedView() {
      const cacheMap: Set<string> = new Set()
      for (const v of this.visitedViews) {
        const item = getRawRoute(v)
        const needCache = !item.meta?.noCache
        if (!needCache) {
          continue
        }
        const name = item.name as string
        cacheMap.add(name)
      }
      if (Array.from(this.cachedViews).sort().toString() === Array.from(cacheMap).sort().toString())
        return
      this.cachedViews = cacheMap
    },
    // 删除某个
    delView(view: RouteLocationNormalizedLoaded) {
      this.delVisitedView(view)
      this.delCachedView()
    },
    // 删除tag
    delVisitedView(view: RouteLocationNormalizedLoaded) {
      const viewIdentityKey = getTagsViewIdentityKey(view)
      for (const [i, v] of this.visitedViews.entries()) {
        if (getTagsViewIdentityKey(v) === viewIdentityKey) {
          this.visitedViews.splice(i, 1)
          break
        }
      }
    },
    // 删除缓存
    delCachedView() {
      const route = router.currentRoute.value
      const index = findIndex<string>(this.getCachedViews, (v) => v === route.name)
      // 需要注释，解决“标签页刷新无效”。相关案例：https://github.com/yudaocode/yudao-ui-admin-vue3/issues/180
      // for (const v of this.visitedViews) {
      //   if (v.name === route.name) {
      //     return
      //   }
      // }
      if (index > -1) {
        this.cachedViews.delete(this.getCachedViews[index])
      }
    },
    // 删除所有缓存和tag
    delAllViews() {
      this.delAllVisitedViews()
      this.delCachedView()
    },
    // 删除所有tag
    delAllVisitedViews() {
      const userStore = useUserStoreWithOut()

      // const affixTags = this.visitedViews.filter((tag) => tag.meta.affix)
      this.visitedViews = userStore.getUser
        ? this.visitedViews.filter((tag) => tag?.meta?.affix)
        : []
    },
    // 删除其他
    delOthersViews(view: RouteLocationNormalizedLoaded) {
      this.delOthersVisitedViews(view)
      this.addCachedView()
    },
    // 删除其他tag
    delOthersVisitedViews(view: RouteLocationNormalizedLoaded) {
      const viewIdentityKey = getTagsViewIdentityKey(view)
      this.visitedViews = this.visitedViews.filter((v) => {
        return v?.meta?.affix || getTagsViewIdentityKey(v) === viewIdentityKey
      })
    },
    // 删除左侧
    delLeftViews(view: RouteLocationNormalizedLoaded) {
      const viewIdentityKey = getTagsViewIdentityKey(view)
      const index = findIndex<RouteLocationNormalizedLoaded>(
        this.visitedViews,
        (v) => getTagsViewIdentityKey(v) === viewIdentityKey
      )
      if (index > -1) {
        this.visitedViews = this.visitedViews.filter((v, i) => {
          return v?.meta?.affix || getTagsViewIdentityKey(v) === viewIdentityKey || i > index
        })
        this.addCachedView()
      }
    },
    // 删除右侧
    delRightViews(view: RouteLocationNormalizedLoaded) {
      const viewIdentityKey = getTagsViewIdentityKey(view)
      const index = findIndex<RouteLocationNormalizedLoaded>(
        this.visitedViews,
        (v) => getTagsViewIdentityKey(v) === viewIdentityKey
      )
      if (index > -1) {
        this.visitedViews = this.visitedViews.filter((v, i) => {
          return v?.meta?.affix || getTagsViewIdentityKey(v) === viewIdentityKey || i < index
        })
        this.addCachedView()
      }
    },
    updateVisitedView(view: RouteLocationNormalizedLoaded) {
      const viewIdentityKey = getTagsViewIdentityKey(view)
      for (const [i, v] of this.visitedViews.entries()) {
        if (getTagsViewIdentityKey(v) === viewIdentityKey) {
          this.visitedViews[i] = Object.assign({}, v, view, {
            meta: Object.assign({}, v.meta, view.meta)
          })
          break
        }
      }
    },
    // 设置当前选中的 tag
    setSelectedTag(tag: RouteLocationNormalizedLoaded) {
      this.selectedTag = tag
    },
    setTitle(title: string, path?: string) {
      for (const v of this.visitedViews) {
        if (v.path === (path ?? this.selectedTag?.path)) {
          v.meta.title = title
          break
        }
      }
    }
  },
  persist: false
})

export const useTagsViewStoreWithOut = () => {
  return useTagsViewStore(store)
}
