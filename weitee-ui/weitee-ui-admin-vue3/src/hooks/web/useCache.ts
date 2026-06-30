/**
 * 閰嶇疆娴忚鍣ㄦ湰鍦板瓨鍌ㄧ殑鏂瑰紡锛屽彲鐩存帴瀛樺偍瀵硅薄鏁扮粍銆?
 */

import WebStorageCache from 'web-storage-cache'

type CacheType = 'localStorage' | 'sessionStorage'

export const CACHE_KEY = {
  // 鐢ㄦ埛鐩稿叧
  ROLE_ROUTERS: 'roleRouters',
  USER: 'user',
  CurrentProject: 'currentProject',
  // 绯荤粺璁剧疆
  IS_DARK: 'isDark',
  LANG: 'lang',
  THEME: 'theme',
  LAYOUT: 'layout',
  DICT_CACHE: 'dictCache',
  // 鐧诲綍琛ㄥ崟
  LoginForm: 'loginForm'
}

export const useCache = (type: CacheType = 'localStorage') => {
  const wsCache: WebStorageCache = new WebStorageCache({
    storage: type
  })

  return {
    wsCache
  }
}

export const deleteUserCache = () => {
  const { wsCache } = useCache()
  wsCache.delete(CACHE_KEY.USER)
  wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
  // 娉ㄦ剰锛屼笉瑕佹竻鐞?LoginForm 鐧诲綍琛ㄥ崟
}
