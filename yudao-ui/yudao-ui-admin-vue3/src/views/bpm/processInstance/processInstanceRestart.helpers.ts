const SALE_ORDER_PROCESS_DEFINITION_KEY = 'erp_sale_order'
const SALE_ORDER_LEGACY_CREATE_PATH = '/erp/sale/order'
const SALE_ORDER_RESTART_ROUTE_NAME = 'ErpSaleOrderRestartPage'
const PROCESS_INSTANCE_STATUS_REJECT = 3
const PROCESS_INSTANCE_STATUS_CANCEL = 4
const SALE_ORDER_STATUS_PROCESS = 10
const SALE_ORDER_STATUS_APPROVE = 20
const SALE_ORDER_STATUS_REJECT = 30

export type RestartTarget = {
  path?: string
  name?: string
  query?: Record<string, string>
}

type ResolveRestartTargetOptions = {
  processDefinitionKey?: string
  formType?: number
  formCustomCreatePath?: string
  businessKey?: string | number
  processInstanceId?: string | number
}

const normalizeRouteNumber = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const parsedValue = Number(value)
  return Number.isNaN(parsedValue) || parsedValue <= 0 ? undefined : String(parsedValue)
}

export const canRestartProcessInstance = (status?: number) => {
  return status === PROCESS_INSTANCE_STATUS_REJECT || status === PROCESS_INSTANCE_STATUS_CANCEL
}

export const isSaleOrderRestartTarget = (options: ResolveRestartTargetOptions) => {
  return (
    options.processDefinitionKey === SALE_ORDER_PROCESS_DEFINITION_KEY ||
    options.formCustomCreatePath === SALE_ORDER_LEGACY_CREATE_PATH
  )
}

export const canRestartSaleOrderBusinessRecord = (options?: {
  status?: number
  processInstanceId?: string | number | null
}) => {
  if (!options) {
    return false
  }
  const hasRunningProcessInstance =
    options.processInstanceId !== undefined &&
    options.processInstanceId !== null &&
    `${options.processInstanceId}` !== ''
  return (
    options.status === SALE_ORDER_STATUS_REJECT ||
    (options.status === SALE_ORDER_STATUS_PROCESS && !hasRunningProcessInstance)
  )
}

export const resolveProcessInstanceRestartTarget = (
  options: ResolveRestartTargetOptions
): RestartTarget | undefined => {
  if (!options.formType) {
    return undefined
  }

  if (options.formType === 10) {
    const processInstanceId = normalizeRouteNumber(options.processInstanceId)
    if (!processInstanceId) {
      return undefined
    }
    return {
      name: 'BpmProcessInstanceCreate',
      query: {
        processInstanceId
      }
    }
  }

  if (options.formType !== 20) {
    return undefined
  }

  const businessKey = normalizeRouteNumber(options.businessKey)
  if (isSaleOrderRestartTarget(options)) {
    if (!businessKey) {
      return undefined
    }
    return {
      name: SALE_ORDER_RESTART_ROUTE_NAME,
      query: {
        id: businessKey,
        from: 'processInstanceRestart'
      }
    }
  }

  if (!options.formCustomCreatePath) {
    return undefined
  }

  return {
    path: options.formCustomCreatePath,
    query: businessKey
      ? {
          id: businessKey
        }
      : undefined
  }
}
