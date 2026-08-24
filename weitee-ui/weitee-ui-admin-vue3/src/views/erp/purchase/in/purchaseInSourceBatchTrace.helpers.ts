export type PurchaseInSourceBatchTraceRequestState = {
  requestToken: number
  activeRequestToken: number
  requestSourceBatchId: number
  activeSourceBatchId?: number
}

export function isCurrentPurchaseInSourceBatchTraceRequest(
  state: PurchaseInSourceBatchTraceRequestState
) {
  return (
    state.requestToken === state.activeRequestToken &&
    state.requestSourceBatchId === state.activeSourceBatchId
  )
}
