export const getFinanceAssetStatusActionDescriptor = (status?: number) => {
  if (status === 0 || status === 10 || status === 20) {
    return {
      canToggleStatus: true,
      nextStatus: status === 10 ? 20 : 10
    }
  }

  return {
    canToggleStatus: false,
    nextStatus: undefined
  }
}
