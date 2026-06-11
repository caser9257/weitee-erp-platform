export const BPM_MODULE_UNAVAILABLE_TEXT = '当前环境的工作流功能暂不可用，请先启用并初始化 BPM 模块。'

export const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message
  }
  return typeof error === 'string' ? error : ''
}

export const isBpmModuleUnavailableError = (error: unknown): boolean => {
  const message = getErrorMessage(error)
  return message.includes('yudao-module-bpm')
}
