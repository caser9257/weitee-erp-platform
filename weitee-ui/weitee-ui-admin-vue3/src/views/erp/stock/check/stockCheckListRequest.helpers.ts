export type StockCheckListRequestResult = 'applied' | 'failed' | 'superseded'

export const resolveStockCheckListRefreshMessage = (
  result: StockCheckListRequestResult,
  successText: string
) => {
  if (result === 'applied') {
    return `${successText}，列表已更新`
  }
  if (result === 'failed') {
    return '操作已完成，列表刷新失败'
  }
  return '操作已完成，列表刷新已由新的查询请求接管'
}

export type StockCheckListRequestCallbacks<T> = {
  onSuccess: (data: T) => void
  onFailure: () => void
  onFinally: () => void
}

export const createStockCheckListRequestGate = () => {
  let latestRequestId = 0

  const issue = () => {
    latestRequestId += 1
    return latestRequestId
  }

  const isLatest = (requestId: number) => requestId === latestRequestId

  const execute = async <T>(
    request: () => Promise<T>,
    callbacks: StockCheckListRequestCallbacks<T>
  ): Promise<StockCheckListRequestResult> => {
    const requestId = issue()
    try {
      const data = await request()
      if (!isLatest(requestId)) {
        return 'superseded'
      }
      callbacks.onSuccess(data)
      return 'applied'
    } catch {
      if (!isLatest(requestId)) {
        return 'superseded'
      }
      callbacks.onFailure()
      return 'failed'
    } finally {
      if (isLatest(requestId)) {
        callbacks.onFinally()
      }
    }
  }

  return { issue, isLatest, execute }
}
