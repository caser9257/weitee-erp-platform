import { useAppStoreWithOut } from '@/store/modules/app'

const appStore = useAppStoreWithOut()
const PAGE_LOADING_TIMEOUT_MS = 15000

let pageLoadingTimer: number | undefined

export const usePageLoading = () => {
  const loadStart = () => {
    if (pageLoadingTimer) {
      window.clearTimeout(pageLoadingTimer)
    }
    appStore.setPageLoading(true)
    pageLoadingTimer = window.setTimeout(() => {
      console.warn(
        `[pageLoading] auto reset after ${PAGE_LOADING_TIMEOUT_MS}ms, current loading state was not released in time`
      )
      appStore.setPageLoading(false)
      pageLoadingTimer = undefined
    }, PAGE_LOADING_TIMEOUT_MS)
  }

  const loadDone = () => {
    if (pageLoadingTimer) {
      window.clearTimeout(pageLoadingTimer)
      pageLoadingTimer = undefined
    }
    appStore.setPageLoading(false)
  }

  return {
    loadStart,
    loadDone
  }
}
