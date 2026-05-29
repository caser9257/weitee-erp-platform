import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { shouldEnableTongji } from '../src/plugins/tongji/runtimeGate.ts'
import { resolvePurchaseInQualityDetailLoadingState } from '../src/views/erp/purchase/in-quality/detail/loadingState.ts'

const testPurchaseInQualityDetailLoadingState = () => {
  assert.deepEqual(
    resolvePurchaseInQualityDetailLoadingState({
      detailLoading: true,
      createLoading: false,
      hasQualityId: false
    }),
    {
      pageInitializing: true,
      pageRefreshing: false
    }
  )

  assert.deepEqual(
    resolvePurchaseInQualityDetailLoadingState({
      detailLoading: true,
      createLoading: false,
      hasQualityId: true
    }),
    {
      pageInitializing: false,
      pageRefreshing: true
    }
  )

  assert.deepEqual(
    resolvePurchaseInQualityDetailLoadingState({
      detailLoading: false,
      createLoading: false,
      hasQualityId: true
    }),
    {
      pageInitializing: false,
      pageRefreshing: false
    }
  )
}

const testTongjiRuntimeGate = () => {
  assert.equal(shouldEnableTongji(undefined, true), false)
  assert.equal(shouldEnableTongji('', true), false)
  assert.equal(shouldEnableTongji('test-id', false), false)
  assert.equal(shouldEnableTongji('test-id', true), true)
}

const testLayoutRouteLoadingMaskDisabled = () => {
  const layoutSource = readFileSync(
    new URL('../src/layout/components/useRenderLayout.tsx', import.meta.url),
    'utf8'
  )

  assert.equal(
    layoutSource.includes('v-loading={pageLoading.value}'),
    false,
    'layout route loading mask should be disabled to avoid global dark overlay blocking page interaction'
  )
}

const run = () => {
  testPurchaseInQualityDetailLoadingState()
  testTongjiRuntimeGate()
  testLayoutRouteLoadingMaskDisabled()
  console.log('ui overlay regression checks passed')
}

run()
