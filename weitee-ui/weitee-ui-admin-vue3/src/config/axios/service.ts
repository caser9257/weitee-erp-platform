import axios, { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import qs from 'qs'
import { config } from '@/config/axios/config'
import { getAccessToken, getRefreshToken, removeToken, setToken } from '@/utils/auth'
import errorCode from './errorCode'
import { resetRouter } from '@/router'
import { deleteUserCache } from '@/hooks/web/useCache'
import { ApiEncrypt } from '@/utils/encrypt'

const { result_code, base_url, request_timeout } = config

// 这些错误会在刷新令牌流程中被兜底处理，不再重复弹窗提示
const ignoreMsgs = ['无效的刷新令牌', '刷新令牌已过期']

export const isRelogin = { show: false }

let requestList: Array<() => void> = []
let isRefreshToken = false
const whiteList: string[] = ['/login', '/refresh-token']

type RequestCustomHeaders = Record<string, any> & {
  Authorization?: string
  'Cache-Control'?: string
  Pragma?: string
  isToken?: boolean
  isEncrypt?: boolean
  isEncrypted?: boolean
}

const ensureRequestHeaders = (config: InternalAxiosRequestConfig): RequestCustomHeaders => {
  if (!config.headers) {
    config.headers = {} as InternalAxiosRequestConfig['headers']
  }
  return config.headers as RequestCustomHeaders
}

const service: AxiosInstance = axios.create({
  baseURL: base_url,
  timeout: request_timeout,
  withCredentials: false,
  paramsSerializer: (params) => {
    return qs.stringify(params, { allowDots: true })
  }
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const headers = ensureRequestHeaders(config)

    let isToken = headers.isToken === false
    whiteList.some((path) => {
      if (config.url && config.url.indexOf(path) > -1) {
        isToken = false
        return true
      }
      return false
    })

    if (getAccessToken() && !isToken) {
      headers.Authorization = 'Bearer ' + getAccessToken()
    }

    const method = config.method?.toUpperCase()
    if (method === 'GET') {
      headers['Cache-Control'] = 'no-cache'
      headers.Pragma = 'no-cache'
    } else if (method === 'POST') {
      const contentType = headers['Content-Type'] || headers['content-type']
      if (contentType === 'application/x-www-form-urlencoded') {
        if (config.data && typeof config.data !== 'string') {
          config.data = qs.stringify(config.data)
        }
      }
    }

    if (headers.isEncrypt && !headers.isEncrypted) {
      try {
        if (config.data) {
          config.data = ApiEncrypt.encryptRequest(config.data)
          headers[ApiEncrypt.getEncryptHeader()] = 'true'
        }
      } catch (error) {
        console.error('璇锋眰鏁版嵁鍔犲瘑澶辫触:', error)
        throw error
      }
    }

    return config
  },
  (error: AxiosError) => {
    console.log(error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  async (response: AxiosResponse<any>) => {
    let { data } = response
    const config = response.config
    if (!data) {
      throw new Error()
    }

    const encryptHeader = ApiEncrypt.getEncryptHeader()
    const isEncryptResponse =
      response.headers[encryptHeader] === 'true' ||
      response.headers[encryptHeader.toLowerCase()] === 'true'
    if (isEncryptResponse && typeof data === 'string') {
      try {
        data = ApiEncrypt.decryptResponse(data)
      } catch (error) {
        console.error('鍝嶅簲鏁版嵁瑙ｅ瘑澶辫触:', error)
        throw new Error('鍝嶅簲鏁版嵁瑙ｅ瘑澶辫触: ' + (error as Error).message)
      }
    }

    const { t } = useI18n()
    if (
      response.request.responseType === 'blob' ||
      response.request.responseType === 'arraybuffer'
    ) {
      if (response.data.type !== 'application/json') {
        return response.data
      }
      data = await new Response(response.data).json()
    }

    const code = data.code ?? result_code
    const msg = data.msg || errorCode[code] || errorCode['default']
    if (ignoreMsgs.includes(msg)) {
      return Promise.reject(msg)
    } else if (code === 401) {
      if (!isRefreshToken) {
        isRefreshToken = true
        if (!getRefreshToken()) {
          return handleAuthorized()
        }
        try {
          const refreshTokenRes = await refreshToken()
          setToken(refreshTokenRes.data.data)

          const headers = ensureRequestHeaders(config)
          headers.Authorization = 'Bearer ' + getAccessToken()

          requestList.forEach((cb) => cb())
          requestList = []

          if (headers.isEncrypt) {
            headers.isEncrypted = true
          }
          return service(config)
        } catch (error) {
          requestList.forEach((cb) => cb())
          return handleAuthorized()
        } finally {
          requestList = []
          isRefreshToken = false
        }
      }

      return new Promise((resolve) => {
        requestList.push(() => {
          ensureRequestHeaders(config).Authorization = 'Bearer ' + getAccessToken()
          resolve(service(config))
        })
      })
    } else if (code === 500) {
      ElMessage.error(t('sys.api.errMsg500'))
      return Promise.reject(new Error(msg))
    } else if (code === 901) {
      ElMessage.error({
        offset: 300,
        dangerouslyUseHTMLString: true,
        message:
          '<div>' +
          t('sys.api.errMsg901') +
          '</div>' +
          '<div> &nbsp; </div>' +
          '<div>鍙傝€?https://doc.iocoder.cn/ 鏁欑▼</div>' +
          '<div> &nbsp; </div>' +
          '<div>5 鍒嗛挓鎼缓鏈湴鐜</div>'
      })
      return Promise.reject(new Error(msg))
    } else if (code !== result_code) {
      if (msg === '无效的刷新令牌') {
        console.log(msg)
        return handleAuthorized()
      }
      ElNotification.error({ title: msg })
      return Promise.reject(new Error(msg))
    }

    return data
  },
  (error: AxiosError) => {
    console.log('err' + error)
    let { message } = error
    const { t } = useI18n()
    if (message === 'Network Error') {
      message = t('sys.api.errorMessage')
    } else if (message.includes('timeout')) {
      message = t('sys.api.apiTimeoutMessage')
    } else if (message.includes('Request failed with status code')) {
      message = t('sys.api.apiRequestFailed') + message.substr(message.length - 3)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

const refreshToken = async () => {
  return await axios.post(base_url + '/system/auth/refresh-token?refreshToken=' + getRefreshToken())
}

const handleAuthorized = () => {
  const { t } = useI18n()
  if (!isRelogin.show) {
    if (window.location.href.includes('login')) {
      return
    }
    isRelogin.show = true
    ElMessageBox.confirm(t('sys.api.timeoutMessage'), t('common.confirmTitle'), {
      showCancelButton: false,
      closeOnClickModal: false,
      showClose: false,
      closeOnPressEscape: false,
      confirmButtonText: t('login.relogin'),
      type: 'warning'
    }).then(() => {
      resetRouter()
      deleteUserCache()
      removeToken()
      isRelogin.show = false
      window.location.href = window.location.href
    })
  }
  return Promise.reject(t('sys.api.timeoutMessage'))
}

export { service }
