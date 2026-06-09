const resolveBaseUrl = () => {
  const envBaseUrl = import.meta.env.VITE_BASE_URL
  if (envBaseUrl && envBaseUrl !== '__AUTO__') {
    return envBaseUrl
  }

  return `${window.location.protocol}//${window.location.hostname}:48080`
}

const config: {
  base_url: string
  result_code: number | string
  default_headers: AxiosHeaders
  request_timeout: number
} = {
  /**
   * api请求基础路径
   */
  base_url: resolveBaseUrl() + import.meta.env.VITE_API_URL,
  /**
   * 接口成功返回状态码
   */
  result_code: 0,

  /**
   * 接口请求超时时间
   */
  request_timeout: 30000,

  /**
   * 默认接口请求类型
   * 可选值：application/x-www-form-urlencoded multipart/form-data
   */
  default_headers: 'application/json'
}

export { config }
