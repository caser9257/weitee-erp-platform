export type ClosureWorkbenchRouteSource = {
  id: number | string
  no?: string
}

export function buildSaleOrderDetailRoute(id: number | string) {
  return {
    path: '/sales/order',
    query: {
      openId: String(id),
      openType: 'detail',
      from: 'closure-workbench'
    }
  }
}

export function buildClosureTraceQuery(
  row: ClosureWorkbenchRouteSource,
  extraQuery?: Record<string, string>
) {
  return {
    sourceOrderId: String(row.id),
    sourceOrderNo: row.no || '',
    traceFrom: 'closure-workbench',
    ...extraQuery
  }
}
