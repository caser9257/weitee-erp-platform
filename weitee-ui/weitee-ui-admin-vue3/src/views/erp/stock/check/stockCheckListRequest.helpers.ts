export const createStockCheckListRequestGate = () => {
  let latestRequestId = 0

  const issue = () => {
    latestRequestId += 1
    return latestRequestId
  }

  const isLatest = (requestId: number) => requestId === latestRequestId

  return { issue, isLatest }
}
