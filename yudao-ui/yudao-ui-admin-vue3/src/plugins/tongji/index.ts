import router from '@/router'
import { shouldEnableTongji } from './runtimeGate'

window._hmt = window._hmt || []

const HM_ID = import.meta.env.VITE_APP_BAIDU_CODE
const tongjiEnabled = shouldEnableTongji(HM_ID, import.meta.env.PROD)

;(function () {
  if (!tongjiEnabled) {
    return
  }
  const hm = document.createElement('script')
  hm.src = 'https://hm.baidu.com/hm.js?' + HM_ID
  const s = document.getElementsByTagName('script')[0]
  s.parentNode?.insertBefore(hm, s)
})()

router.afterEach(function (to) {
  if (!tongjiEnabled) {
    return
  }
  window._hmt.push(['_trackPageview', to.fullPath])
})
