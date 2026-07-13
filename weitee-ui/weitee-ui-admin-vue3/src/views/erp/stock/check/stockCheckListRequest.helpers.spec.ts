import assert from 'node:assert/strict'
import { createStockCheckListRequestGate } from './stockCheckListRequest.helpers'

const createDeferred = <T>() => {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })

  return { promise, resolve, reject }
}

const run = async () => {
  const requestGate = createStockCheckListRequestGate()
  const firstRequestId = requestGate.issue()

  assert.equal(requestGate.isLatest(firstRequestId), true)

  const secondRequestId = requestGate.issue()

  assert.equal(requestGate.isLatest(firstRequestId), false)
  assert.equal(requestGate.isLatest(secondRequestId), true)

  const firstResponse = createDeferred<{ list: string[]; total: number }>()
  const secondResponse = createDeferred<{ list: string[]; total: number }>()
  const listState = {
    list: ['旧数据'],
    total: 1,
    listLoadFailed: false,
    loading: true
  }
  let successCalls = 0
  let failureCalls = 0
  let finallyCalls = 0
  const callbacks = {
    onSuccess: (data: { list: string[]; total: number }) => {
      successCalls += 1
      listState.list = data.list
      listState.total = data.total
      listState.listLoadFailed = false
    },
    onFailure: () => {
      failureCalls += 1
      listState.list = []
      listState.total = 0
      listState.listLoadFailed = true
    },
    onFinally: () => {
      finallyCalls += 1
      listState.loading = false
    }
  }

  const firstResult = requestGate.execute(() => firstResponse.promise, callbacks)
  const secondResult = requestGate.execute(() => secondResponse.promise, callbacks)

  secondResponse.resolve({ list: ['新数据'], total: 2 })

  assert.equal(await secondResult, 'applied')
  assert.deepEqual(listState, {
    list: ['新数据'],
    total: 2,
    listLoadFailed: false,
    loading: false
  })
  assert.equal(successCalls, 1)
  assert.equal(failureCalls, 0)
  assert.equal(finallyCalls, 1)

  firstResponse.reject(new Error('旧请求失败'))

  assert.equal(await firstResult, 'superseded')
  assert.deepEqual(listState, {
    list: ['新数据'],
    total: 2,
    listLoadFailed: false,
    loading: false
  })
  assert.equal(successCalls, 1)
  assert.equal(failureCalls, 0)
  assert.equal(finallyCalls, 1)

  const failedResponse = createDeferred<{ list: string[]; total: number }>()
  listState.loading = true
  const failedResult = requestGate.execute(() => failedResponse.promise, callbacks)

  failedResponse.reject(new Error('最新请求失败'))

  assert.equal(await failedResult, 'failed')
  assert.equal(failureCalls, 1)
  assert.equal(finallyCalls, 2)
  assert.equal(listState.listLoadFailed, true)
  assert.equal(listState.loading, false)
}

void run()
