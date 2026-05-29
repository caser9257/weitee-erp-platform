export interface PurchaseInQualityDetailLoadingStateInput {
  detailLoading: boolean
  createLoading: boolean
  hasQualityId: boolean
}

export interface PurchaseInQualityDetailLoadingState {
  pageInitializing: boolean
  pageRefreshing: boolean
}

export const resolvePurchaseInQualityDetailLoadingState = (
  input: PurchaseInQualityDetailLoadingStateInput
): PurchaseInQualityDetailLoadingState => {
  const busy = input.detailLoading || input.createLoading

  return {
    pageInitializing: busy && !input.hasQualityId,
    pageRefreshing: busy && input.hasQualityId
  }
}
